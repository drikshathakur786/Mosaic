package com.mosaic.auth.dto;

import java.util.UUID;

public record PreferencesResponse(
        UUID id,
        String defaultCurrency,
        String dateFormat,
        String numberFormat,
        String theme,
        boolean aiEnabled
) {}
