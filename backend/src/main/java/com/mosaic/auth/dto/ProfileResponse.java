package com.mosaic.auth.dto;

import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String displayName,
        String countryCode,
        String timezone,
        String locale
) {}
