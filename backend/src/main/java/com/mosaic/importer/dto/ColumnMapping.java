package com.mosaic.importer.dto;

import jakarta.validation.constraints.NotBlank;

public record ColumnMapping(
        @NotBlank(message = "Date column is required")
        String date,

        @NotBlank(message = "Merchant / Description column is required")
        String merchantName,

        String amount,

        String debit,

        String credit,

        String category,

        String description
) {}
