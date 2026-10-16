package com.mosaic.importer.dto;

import com.mosaic.importer.entity.ImportRowStatus;
import com.mosaic.transaction.entity.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record ImportPreviewItem(
        int rowNumber,
        ImportRowStatus status,
        String error,
        ParsedTransactionSummary parsed,
        UUID existingTransactionId
) {
    public record ParsedTransactionSummary(
            String merchantName,
            long amountMinor,
            String currency,
            Instant transactionDate,
            TransactionType transactionType,
            String categoryName,
            String description
    ) {}
}
