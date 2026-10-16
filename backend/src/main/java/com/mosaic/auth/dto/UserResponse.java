package com.mosaic.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        boolean emailVerified,
        ProfileResponse profile,
        PreferencesResponse preferences,
        Instant createdAt
) {}
