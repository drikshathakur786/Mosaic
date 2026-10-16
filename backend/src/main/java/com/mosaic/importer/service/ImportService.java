package com.mosaic.importer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosaic.auth.entity.User;
import com.mosaic.auth.repository.UserRepository;
import com.mosaic.category.entity.Category;
import com.mosaic.category.repository.CategoryRepository;
import com.mosaic.common.dto.PageResponse;
import com.mosaic.common.exception.DuplicateResourceException;
import com.mosaic.common.exception.ResourceNotFoundException;
import com.mosaic.common.model.CurrencyCode;
import com.mosaic.importer.dto.ColumnMapping;
import com.mosaic.importer.dto.CsvUploadResponse;
import com.mosaic.importer.dto.ImportConfirmRequest;
import com.mosaic.importer.dto.ImportConfirmResponse;
import com.mosaic.importer.dto.ImportPreviewItem;
import com.mosaic.importer.dto.ImportPreviewRequest;
import com.mosaic.importer.dto.ImportPreviewResponse;
import com.mosaic.importer.dto.ImportResponse;
import com.mosaic.importer.entity.ImportRowStatus;
import com.mosaic.importer.entity.ImportStatus;
import com.mosaic.importer.entity.TransactionImport;
import com.mosaic.importer.entity.TransactionImportRow;
import com.mosaic.importer.repository.TransactionImportRepository;
import com.mosaic.importer.repository.TransactionImportRowRepository;
import com.mosaic.transaction.entity.Transaction;
import com.mosaic.transaction.entity.TransactionSource;
import com.mosaic.transaction.entity.TransactionStatus;
import com.mosaic.transaction.entity.TransactionType;
import com.mosaic.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final TransactionImportRepository importRepository;
    private final TransactionImportRowRepository importRowRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CsvParserService csvParserService;
    private final ObjectMapper objectMapper;

    public ImportService(TransactionImportRepository importRepository,
                         TransactionImportRowRepository importRowRepository,
                         UserRepository userRepository,
                         TransactionRepository transactionRepository,
                         CategoryRepository categoryRepository,
                         CsvParserService csvParserService,
                         ObjectMapper objectMapper) {
        this.importRepository = importRepository;
        this.importRowRepository = importRowRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.csvParserService = csvParserService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CsvUploadResponse uploadCsv(UUID userId, MultipartFile file, String currency, String dateFormat) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        try {
            byte[] bytes = file.getBytes();
            String hash = csvParserService.computeSha256(bytes);

            if (importRepository.existsByUserIdAndFileHash(userId, hash)) {
                throw new DuplicateResourceException("Import", "file_hash",
                        "A file with identical content has already been imported");
            }

            char delimiter = csvParserService.detectDelimiter(bytes);
            List<String> headers = csvParserService.extractHeaders(bytes, delimiter);
            List<Map<String, String>> parsedRows = csvParserService.parseCsvRows(bytes, delimiter);

            TransactionImport txImport = new TransactionImport();
            txImport.setUser(user);
            txImport.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "import.csv");
            txImport.setFileHash(hash);
            txImport.setTotalRows(parsedRows.size());
            txImport.setStatus(ImportStatus.UPLOADED);
            txImport.setCurrency(CurrencyCode.normalize(currency));
            txImport.setDateFormat(dateFormat);

            TransactionImport savedImport = importRepository.save(txImport);

            List<TransactionImportRow> rowsToSave = new ArrayList<>();
            for (int i = 0; i < parsedRows.size(); i++) {
                Map<String, String> rowMap = parsedRows.get(i);
                String rawJson = objectMapper.writeValueAsString(rowMap);
                TransactionImportRow importRow = new TransactionImportRow(i + 1, rawJson);
                importRow.setTransactionImport(savedImport);
                rowsToSave.add(importRow);
            }
            importRowRepository.saveAll(rowsToSave);

            List<Map<String, String>> sampleRows = parsedRows.stream().limit(5).toList();

            return new CsvUploadResponse(
                    savedImport.getId(),
                    savedImport.getFilename(),
                    savedImport.getStatus(),
                    savedImport.getTotalRows(),
                    headers,
                    sampleRows
            );
        } catch (DuplicateResourceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse and store CSV upload: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing CSV upload: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ImportPreviewResponse previewImport(UUID userId, UUID importId, ImportPreviewRequest request) {
        TransactionImport txImport = importRepository.findByIdAndUserId(importId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionImport", importId.toString()));

        List<TransactionImportRow> rows = importRowRepository.findAllByTransactionImportIdOrderByRowNumberAsc(importId);
        ColumnMapping mapping = request.columnMapping();

        int validCount = 0;
        int invalidCount = 0;
        int duplicateCount = 0;
        List<ImportPreviewItem> previewItems = new ArrayList<>();

        String targetCurrency = request.currency() != null
                ? CurrencyCode.normalize(request.currency())
                : (txImport.getCurrency() != null ? txImport.getCurrency() : "USD");

        boolean amountIsNegativeForExpense = request.amountIsNegativeForExpense() == null || request.amountIsNegativeForExpense();

        for (TransactionImportRow row : rows) {
            try {
                Map<String, String> rowData = objectMapper.readValue(row.getRawData(), new TypeReference<>() {});

                String dateStr = rowData.get(mapping.date());
                String merchantStr = rowData.get(mapping.merchantName());

                if (merchantStr == null || merchantStr.isBlank()) {
                    throw new IllegalArgumentException("Merchant name is missing or blank");
                }

                LocalDate date = csvParserService.parseDate(dateStr, request.dateFormat());
                Instant transactionDate = date.atStartOfDay(ZoneOffset.UTC).toInstant();

                // Compute amount
                long amountMinor = resolveAmount(rowData, mapping);
                TransactionType type;

                if (amountMinor < 0) {
                    type = amountIsNegativeForExpense ? TransactionType.EXPENSE : TransactionType.INCOME;
                    amountMinor = Math.abs(amountMinor);
                } else {
                    type = amountIsNegativeForExpense ? TransactionType.INCOME : TransactionType.EXPENSE;
                }

                // Check for potential duplicate transactions in database
                String normalizedMerchant = Transaction.normalizeMerchant(merchantStr);
                final long targetAmountMinor = amountMinor;
                final Instant targetDate = transactionDate;
                final String targetMerchant = normalizedMerchant;

                Optional<Transaction> potentialDuplicate = transactionRepository.findAll(
                        (root, query, cb) -> cb.and(
                                cb.equal(root.get("user").get("id"), userId),
                                cb.equal(root.get("normalizedMerchantName"), targetMerchant),
                                cb.equal(root.get("amountMinor"), targetAmountMinor),
                                cb.equal(root.get("transactionDate"), targetDate),
                                cb.isNull(root.get("deletedAt"))
                        )
                ).stream().findFirst();

                ImportRowStatus status;
                UUID existingTxId = null;

                if (potentialDuplicate.isPresent()) {
                    status = ImportRowStatus.DUPLICATE;
                    existingTxId = potentialDuplicate.get().getId();
                    duplicateCount++;
                } else {
                    status = ImportRowStatus.VALID;
                    validCount++;
                }

                row.setStatus(status);
                row.setErrorMessage(null);

                ImportPreviewItem.ParsedTransactionSummary parsed = new ImportPreviewItem.ParsedTransactionSummary(
                        merchantStr,
                        amountMinor,
                        targetCurrency,
                        transactionDate,
                        type,
                        mapping.category() != null ? rowData.get(mapping.category()) : null,
                        mapping.description() != null ? rowData.get(mapping.description()) : null
                );

                previewItems.add(new ImportPreviewItem(row.getRowNumber(), status, null, parsed, existingTxId));

            } catch (Exception e) {
                invalidCount++;
                row.setStatus(ImportRowStatus.INVALID);
                row.setErrorMessage(e.getMessage());
                previewItems.add(new ImportPreviewItem(row.getRowNumber(), ImportRowStatus.INVALID, e.getMessage(), null, null));
            }
        }

        importRowRepository.saveAll(rows);

        txImport.setStatus(ImportStatus.PREVIEWING);
        txImport.setValidRows(validCount);
        txImport.setInvalidRows(invalidCount);
        txImport.setDuplicateRows(duplicateCount);
        txImport.setDateFormat(request.dateFormat());
        txImport.setCurrency(targetCurrency);
        try {
            txImport.setColumnMapping(objectMapper.writeValueAsString(mapping));
        } catch (Exception ignored) {}

        importRepository.save(txImport);

        return new ImportPreviewResponse(
                importId,
                rows.size(),
                validCount,
                invalidCount,
                duplicateCount,
                previewItems
        );
    }

    @Transactional
    public ImportConfirmResponse confirmImport(UUID userId, UUID importId, ImportConfirmRequest request) {
        TransactionImport txImport = importRepository.findByIdAndUserId(importId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionImport", importId.toString()));

        if (txImport.getStatus() != ImportStatus.PREVIEWING && txImport.getStatus() != ImportStatus.UPLOADED) {
            throw new IllegalStateException("Import cannot be confirmed from status: " + txImport.getStatus());
        }

        List<TransactionImportRow> rows = importRowRepository.findAllByTransactionImportIdOrderByRowNumberAsc(importId);
        boolean skipDuplicates = request.shouldSkipDuplicates();

        List<Transaction> transactionsToSave = new ArrayList<>();
        int committedCount = 0;
        int skippedCount = 0;

        ColumnMapping mapping;
        try {
            mapping = objectMapper.readValue(txImport.getColumnMapping(), ColumnMapping.class);
        } catch (Exception e) {
            throw new IllegalStateException("Column mapping not found or invalid for import");
        }

        for (TransactionImportRow row : rows) {
            if (row.getStatus() == ImportRowStatus.INVALID) {
                skippedCount++;
                row.setStatus(ImportRowStatus.SKIPPED);
                continue;
            }

            if (row.getStatus() == ImportRowStatus.DUPLICATE && skipDuplicates) {
                skippedCount++;
                row.setStatus(ImportRowStatus.SKIPPED);
                continue;
            }

            try {
                Map<String, String> rowData = objectMapper.readValue(row.getRawData(), new TypeReference<>() {});
                String merchantStr = rowData.get(mapping.merchantName());
                String dateStr = rowData.get(mapping.date());
                LocalDate date = csvParserService.parseDate(dateStr, txImport.getDateFormat());
                Instant transactionDate = date.atStartOfDay(ZoneOffset.UTC).toInstant();

                long amountMinor = resolveAmount(rowData, mapping);
                TransactionType type;
                if (amountMinor < 0) {
                    type = TransactionType.EXPENSE;
                    amountMinor = Math.abs(amountMinor);
                } else {
                    type = TransactionType.INCOME;
                }

                Transaction tx = new Transaction();
                tx.setUser(txImport.getUser());
                tx.setMerchantName(merchantStr);
                tx.setAmountMinor(amountMinor);
                tx.setCurrency(txImport.getCurrency());
                tx.setTransactionDate(transactionDate);
                tx.setTransactionType(type);
                tx.setTransactionStatus(TransactionStatus.COMPLETED);
                tx.setSource(TransactionSource.CSV_IMPORT);
                tx.setImportId(importId);

                if (mapping.description() != null && rowData.get(mapping.description()) != null) {
                    tx.setDescription(rowData.get(mapping.description()).trim());
                }

                transactionsToSave.add(tx);
                committedCount++;
                row.setStatus(ImportRowStatus.COMMITTED);
            } catch (Exception e) {
                skippedCount++;
                row.setStatus(ImportRowStatus.SKIPPED);
            }
        }

        List<Transaction> savedTransactions = transactionRepository.saveAll(transactionsToSave);

        // Associate saved transaction IDs with import rows
        for (int i = 0; i < savedTransactions.size() && i < rows.size(); i++) {
            rows.get(i).setTransactionId(savedTransactions.get(i).getId());
        }
        importRowRepository.saveAll(rows);

        txImport.setStatus(ImportStatus.COMPLETED);
        txImport.setCompletedAt(Instant.now());
        importRepository.save(txImport);

        log.info("Completed CSV import {} with {} committed and {} skipped rows",
                importId, committedCount, skippedCount);

        return new ImportConfirmResponse(
                importId,
                ImportStatus.COMPLETED,
                committedCount,
                skippedCount
        );
    }

    @Transactional
    public void cancelImport(UUID userId, UUID importId) {
        TransactionImport txImport = importRepository.findByIdAndUserId(importId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionImport", importId.toString()));

        txImport.setStatus(ImportStatus.CANCELLED);
        importRepository.save(txImport);
    }

    @Transactional(readOnly = true)
    public ImportResponse getImportById(UUID userId, UUID importId) {
        TransactionImport txImport = importRepository.findByIdAndUserId(importId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionImport", importId.toString()));
        return ImportResponse.fromEntity(txImport);
    }

    @Transactional(readOnly = true)
    public PageResponse<ImportResponse> getImports(UUID userId, Pageable pageable) {
        Page<TransactionImport> page = importRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<ImportResponse> content = page.getContent()
                .stream()
                .map(ImportResponse::fromEntity)
                .toList();

        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private long resolveAmount(Map<String, String> rowData, ColumnMapping mapping) {
        if (mapping.amount() != null && rowData.containsKey(mapping.amount()) && !rowData.get(mapping.amount()).isBlank()) {
            return csvParserService.parseAmountToMinor(rowData.get(mapping.amount()));
        }

        if (mapping.debit() != null && rowData.containsKey(mapping.debit()) && !rowData.get(mapping.debit()).isBlank()) {
            long debit = Math.abs(csvParserService.parseAmountToMinor(rowData.get(mapping.debit())));
            return -debit; // negative for expense
        }

        if (mapping.credit() != null && rowData.containsKey(mapping.credit()) && !rowData.get(mapping.credit()).isBlank()) {
            return Math.abs(csvParserService.parseAmountToMinor(rowData.get(mapping.credit()))); // positive for income
        }

        throw new IllegalArgumentException("No valid amount, debit, or credit column found in row");
    }
}
