package com.mosaic.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummary user
) {
    public record UserSummary(
            UUID id,
            String email,
            String displayName
    ) {}
}
