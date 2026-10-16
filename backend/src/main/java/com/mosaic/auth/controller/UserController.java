package com.mosaic.auth.controller;

import com.mosaic.auth.dto.PreferencesResponse;
import com.mosaic.auth.dto.PreferencesUpdateRequest;
import com.mosaic.auth.dto.ProfileResponse;
import com.mosaic.auth.dto.ProfileUpdateRequest;
import com.mosaic.auth.dto.UserResponse;
import com.mosaic.auth.security.UserPrincipal;
import com.mosaic.auth.service.UserService;
import com.mosaic.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and preference management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get user account details", description = "Returns profile and preference configuration for authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = userService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me/profile")
    @Operation(summary = "Update user profile", description = "Updates display name, country code, timezone, or locale")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse response = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", response));
    }

    @PatchMapping("/me/preferences")
    @Operation(summary = "Update user preferences", description = "Updates currency, date format, number format, theme, or AI features")
    public ResponseEntity<ApiResponse<PreferencesResponse>> updatePreferences(@AuthenticationPrincipal UserPrincipal principal,
                                                                              @Valid @RequestBody PreferencesUpdateRequest request) {
        PreferencesResponse response = userService.updatePreferences(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Preferences updated successfully", response));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete user account", description = "Performs account soft deletion and deactivates access")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteAccount(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
