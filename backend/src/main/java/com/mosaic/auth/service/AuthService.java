package com.mosaic.auth.service;

import com.mosaic.auth.dto.AuthResponse;
import com.mosaic.auth.dto.LoginRequest;
import com.mosaic.auth.dto.PreferencesResponse;
import com.mosaic.auth.dto.ProfileResponse;
import com.mosaic.auth.dto.RefreshRequest;
import com.mosaic.auth.dto.RegisterRequest;
import com.mosaic.auth.dto.UserResponse;
import com.mosaic.auth.entity.User;
import com.mosaic.auth.entity.UserPreferences;
import com.mosaic.auth.entity.UserProfile;
import com.mosaic.auth.repository.UserRepository;
import com.mosaic.auth.security.JwtTokenProvider;
import com.mosaic.common.exception.DuplicateResourceException;
import com.mosaic.common.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("User already exists with email: " + normalizedEmail);
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(normalizedEmail, hashedPassword);

        // Initialize User Profile
        UserProfile profile = new UserProfile(
                user,
                request.displayName(),
                request.countryCode(),
                request.timezone(),
                request.locale()
        );
        user.setProfile(profile);

        // Initialize User Preferences
        UserPreferences preferences = new UserPreferences(user, request.defaultCurrency());
        user.setPreferences(preferences);

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmailWithDetails(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        String displayName = user.getProfile() != null ? user.getProfile().getDisplayName() : null;
        AuthResponse.UserSummary userSummary = new AuthResponse.UserSummary(user.getId(), user.getEmail(), displayName);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                tokenProvider.getAccessTokenExpirySeconds(),
                userSummary
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshRequest request) {
        String token = request.refreshToken();

        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenType = tokenProvider.extractTokenType(token);
        if (!JwtTokenProvider.TYPE_REFRESH.equalsIgnoreCase(tokenType)) {
            throw new UnauthorizedException("Provided token is not a refresh token");
        }

        UUID userId = tokenProvider.extractUserId(token);
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        String displayName = user.getProfile() != null ? user.getProfile().getDisplayName() : null;
        AuthResponse.UserSummary userSummary = new AuthResponse.UserSummary(user.getId(), user.getEmail(), displayName);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                tokenProvider.getAccessTokenExpirySeconds(),
                userSummary
        );
    }

    public static UserResponse mapToUserResponse(User user) {
        ProfileResponse profileResponse = null;
        if (user.getProfile() != null) {
            profileResponse = new ProfileResponse(
                    user.getProfile().getId(),
                    user.getProfile().getDisplayName(),
                    user.getProfile().getCountryCode(),
                    user.getProfile().getTimezone(),
                    user.getProfile().getLocale()
            );
        }

        PreferencesResponse preferencesResponse = null;
        if (user.getPreferences() != null) {
            preferencesResponse = new PreferencesResponse(
                    user.getPreferences().getId(),
                    user.getPreferences().getDefaultCurrency(),
                    user.getPreferences().getDateFormat(),
                    user.getPreferences().getNumberFormat(),
                    user.getPreferences().getTheme(),
                    user.getPreferences().isAiEnabled()
            );
        }

        String displayName = user.getProfile() != null ? user.getProfile().getDisplayName() : null;

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                displayName,
                user.isEmailVerified(),
                profileResponse,
                preferencesResponse,
                user.getCreatedAt()
        );
    }
}
