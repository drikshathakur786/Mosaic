package com.mosaic.auth.controller;

import com.mosaic.auth.dto.AuthResponse;
import com.mosaic.auth.dto.LoginRequest;
import com.mosaic.auth.dto.RefreshRequest;
import com.mosaic.auth.dto.RegisterRequest;
import com.mosaic.auth.dto.UserResponse;
import com.mosaic.auth.security.UserPrincipal;
import com.mosaic.auth.service.AuthService;
import com.mosaic.auth.service.UserService;
import com.mosaic.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, authentication, and token management")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new user account with default profile and preferences")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user credentials and returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Retrieves account, profile, and preferences for current user")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = userService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }
}
