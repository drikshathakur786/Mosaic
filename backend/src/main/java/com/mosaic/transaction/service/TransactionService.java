package com.mosaic.transaction.service;

import com.mosaic.auth.entity.User;
import com.mosaic.auth.repository.UserRepository;
import com.mosaic.category.entity.Category;
import com.mosaic.category.repository.CategoryRepository;
import com.mosaic.common.dto.PageResponse;
import com.mosaic.common.exception.ResourceNotFoundException;
import com.mosaic.common.model.CurrencyCode;
import com.mosaic.transaction.dto.TransactionCreateRequest;
import com.mosaic.transaction.dto.TransactionFilterRequest;
import com.mosaic.transaction.dto.TransactionResponse;
import com.mosaic.transaction.dto.TransactionUpdateRequest;
import com.mosaic.transaction.entity.Transaction;
import com.mosaic.transaction.entity.TransactionSource;
import com.mosaic.transaction.entity.TransactionStatus;
import com.mosaic.transaction.repository.TransactionRepository;
import com.mosaic.transaction.repository.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(UUID userId, TransactionCreateRequest request, Locale locale) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmountMinor(request.amountMinor());

        String currency = request.currency();
        if (currency == null || currency.isBlank()) {
            currency = (user.getPreferences() != null && user.getPreferences().getDefaultCurrency() != null)
                    ? user.getPreferences().getDefaultCurrency()
                    : "USD";
        }
        transaction.setCurrency(CurrencyCode.normalize(currency));
        transaction.setMerchantName(request.merchantName().trim());
        transaction.setDescription(request.description() != null ? request.description().trim() : null);
        transaction.setTransactionType(request.transactionType());
        transaction.setTransactionStatus(request.transactionStatus() != null ? request.transactionStatus() : TransactionStatus.COMPLETED);
        transaction.setTransactionDate(request.transactionDate());
        transaction.setTimezone(request.timezone() != null && !request.timezone().isBlank() ? request.timezone() : "UTC");
        transaction.setSource(TransactionSource.MANUAL);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId().toString()));
            transaction.setCategory(category);
        }

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved, locale);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(UUID userId,
                                                            TransactionFilterRequest filter,
                                                            Pageable pageable,
                                                            Locale locale) {
        Specification<Transaction> spec = TransactionSpecification.build(userId, filter);
        Page<Transaction> page = transactionRepository.findAll(spec, pageable);

        List<TransactionResponse> content = page.getContent()
                .stream()
                .map(t -> TransactionResponse.fromEntity(t, locale))
                .toList();

        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID userId, UUID id, Locale locale) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id.toString()));

        return TransactionResponse.fromEntity(transaction, locale);
    }

    @Transactional
    public TransactionResponse updateTransaction(UUID userId, UUID id, TransactionUpdateRequest request, Locale locale) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id.toString()));

        if (request.amountMinor() != null) {
            transaction.setAmountMinor(request.amountMinor());
        }
        if (request.currency() != null) {
            transaction.setCurrency(CurrencyCode.normalize(request.currency()));
        }
        if (request.merchantName() != null) {
            transaction.setMerchantName(request.merchantName().trim());
        }
        if (request.description() != null) {
            transaction.setDescription(request.description().trim());
        }
        if (request.transactionType() != null) {
            transaction.setTransactionType(request.transactionType());
        }
        if (request.transactionStatus() != null) {
            transaction.setTransactionStatus(request.transactionStatus());
        }
        if (request.transactionDate() != null) {
            transaction.setTransactionDate(request.transactionDate());
        }
        if (request.timezone() != null) {
            transaction.setTimezone(request.timezone().trim());
        }
        if (request.isReviewed() != null) {
            transaction.setReviewed(request.isReviewed());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId().toString()));
            transaction.setCategory(category);
        }

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved, locale);
    }

    @Transactional
    public void deleteTransaction(UUID userId, UUID id) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id.toString()));

        transaction.setDeletedAt(Instant.now());
        transactionRepository.save(transaction);
    }
}
