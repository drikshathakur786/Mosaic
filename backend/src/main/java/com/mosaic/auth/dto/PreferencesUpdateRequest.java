package com.mosaic.auth.dto;

import jakarta.validation.constraints.Pattern;

public record PreferencesUpdateRequest(
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
        String defaultCurrency,

        String dateFormat,

        String numberFormat,

        String theme,

        Boolean aiEnabled
) {}
