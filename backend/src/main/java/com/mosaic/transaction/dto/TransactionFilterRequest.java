package com.mosaic.transaction.dto;

import com.mosaic.transaction.entity.TransactionSource;
import com.mosaic.transaction.entity.TransactionStatus;
import com.mosaic.transaction.entity.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

public record TransactionFilterRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant endDate,

        UUID categoryId,

        TransactionType type,

        TransactionStatus status,

        TransactionSource source,

        String merchant,

        Long minAmountMinor,

        Long maxAmountMinor,

        Boolean isReviewed
) {}
