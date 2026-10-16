package com.mosaic.transaction.dto;

import com.mosaic.transaction.entity.TransactionStatus;
import com.mosaic.transaction.entity.TransactionType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record TransactionUpdateRequest(
        @Positive(message = "Amount must be greater than zero")
        Long amountMinor,

        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
        String currency,

        @Size(max = 255, message = "Merchant name cannot exceed 255 characters")
        String merchantName,

        String description,

        UUID categoryId,

        TransactionType transactionType,

        TransactionStatus transactionStatus,

        Instant transactionDate,

        String timezone,

        Boolean isReviewed
) {}
