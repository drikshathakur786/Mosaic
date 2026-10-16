package com.mosaic.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
        String defaultCurrency,

        @Size(min = 2, max = 2, message = "Country code must be a 2-letter ISO code")
        String countryCode,

        String timezone,

        String locale
) {}
