package com.mosaic.auth.service;

import com.mosaic.auth.dto.PreferencesResponse;
import com.mosaic.auth.dto.PreferencesUpdateRequest;
import com.mosaic.auth.dto.ProfileResponse;
import com.mosaic.auth.dto.ProfileUpdateRequest;
import com.mosaic.auth.dto.UserResponse;
import com.mosaic.auth.entity.User;
import com.mosaic.auth.entity.UserPreferences;
import com.mosaic.auth.entity.UserProfile;
import com.mosaic.auth.repository.UserPreferencesRepository;
import com.mosaic.auth.repository.UserProfileRepository;
import com.mosaic.auth.repository.UserRepository;
import com.mosaic.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserPreferencesRepository preferencesRepository;

    public UserService(UserRepository userRepository,
                       UserProfileRepository profileRepository,
                       UserPreferencesRepository preferencesRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.preferencesRepository = preferencesRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return AuthService.mapToUserResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    UserProfile newProfile = new UserProfile(user, null, null, "UTC", "en-US");
                    return profileRepository.save(newProfile);
                });

        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName().trim());
        }
        if (request.countryCode() != null) {
            profile.setCountryCode(request.countryCode().trim().toUpperCase());
        }
        if (request.timezone() != null) {
            profile.setTimezone(request.timezone().trim());
        }
        if (request.locale() != null) {
            profile.setLocale(request.locale().trim());
        }

        UserProfile saved = profileRepository.save(profile);
        return new ProfileResponse(
                saved.getId(),
                saved.getDisplayName(),
                saved.getCountryCode(),
                saved.getTimezone(),
                saved.getLocale()
        );
    }

    @Transactional
    public PreferencesResponse updatePreferences(UUID userId, PreferencesUpdateRequest request) {
        UserPreferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    UserPreferences newPrefs = new UserPreferences(user, "USD");
                    return preferencesRepository.save(newPrefs);
                });

        if (request.defaultCurrency() != null) {
            preferences.setDefaultCurrency(request.defaultCurrency().trim().toUpperCase());
        }
        if (request.dateFormat() != null) {
            preferences.setDateFormat(request.dateFormat().trim());
        }
        if (request.numberFormat() != null) {
            preferences.setNumberFormat(request.numberFormat().trim());
        }
        if (request.theme() != null) {
            preferences.setTheme(request.theme().trim().toUpperCase());
        }
        if (request.aiEnabled() != null) {
            preferences.setAiEnabled(request.aiEnabled());
        }

        UserPreferences saved = preferencesRepository.save(preferences);
        return new PreferencesResponse(
                saved.getId(),
                saved.getDefaultCurrency(),
                saved.getDateFormat(),
                saved.getNumberFormat(),
                saved.getTheme(),
                saved.isAiEnabled()
        );
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setDeletedAt(Instant.now());
        user.setActive(false);
        userRepository.save(user);
    }
}
