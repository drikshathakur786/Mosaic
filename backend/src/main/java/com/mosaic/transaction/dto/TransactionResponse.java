package com.mosaic.transaction.dto;

import com.mosaic.category.dto.CategoryResponse;
import com.mosaic.common.util.MoneyUtils;
import com.mosaic.transaction.entity.Transaction;
import com.mosaic.transaction.entity.TransactionSource;
import com.mosaic.transaction.entity.TransactionStatus;
import com.mosaic.transaction.entity.TransactionType;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        long amountMinor,
        String amountFormatted,
        String currency,
        String merchantName,
        String normalizedMerchantName,
        String description,
        CategoryResponse category,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        Instant transactionDate,
        String timezone,
        TransactionSource source,
        String sourceReference,
        boolean isReviewed,
        Instant createdAt,
        Instant updatedAt
) {
    public static TransactionResponse fromEntity(Transaction entity, Locale locale) {
        String formatted = MoneyUtils.format(entity.getAmountMinor(), entity.getCurrency(), locale);

        return new TransactionResponse(
                entity.getId(),
                entity.getAmountMinor(),
                formatted,
                entity.getCurrency(),
                entity.getMerchantName(),
                entity.getNormalizedMerchantName(),
                entity.getDescription(),
                CategoryResponse.fromEntity(entity.getCategory()),
                entity.getTransactionType(),
                entity.getTransactionStatus(),
                entity.getTransactionDate(),
                entity.getTimezone(),
                entity.getSource(),
                entity.getSourceReference(),
                entity.isReviewed(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
