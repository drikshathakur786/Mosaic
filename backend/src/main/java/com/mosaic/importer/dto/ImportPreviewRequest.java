package com.mosaic.importer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ImportPreviewRequest(
        @NotNull(message = "Column mapping is required")
        @Valid
        ColumnMapping columnMapping,

        String dateFormat,

        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
        String currency,

        Boolean amountIsNegativeForExpense
) {}
