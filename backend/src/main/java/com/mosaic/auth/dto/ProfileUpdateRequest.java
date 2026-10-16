package com.mosaic.auth.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        @Size(min = 2, max = 2, message = "Country code must be a 2-letter ISO code")
        String countryCode,

        String timezone,

        String locale
) {}
