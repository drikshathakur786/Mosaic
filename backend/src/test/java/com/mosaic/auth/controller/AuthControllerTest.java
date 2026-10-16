package com.mosaic.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosaic.auth.dto.LoginRequest;
import com.mosaic.auth.dto.RefreshRequest;
import com.mosaic.auth.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/auth/register should create account and return 201")
    void registerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "alex@example.com",
                "StrongPassword123!",
                "Alex Doe",
                "USD",
                "US",
                "America/New_York",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("alex@example.com"))
                .andExpect(jsonPath("$.data.displayName").value("Alex Doe"))
                .andExpect(jsonPath("$.data.profile.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.data.preferences.defaultCurrency").value("USD"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email should return 409")
    void registerDuplicateEmailReturns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "duplicate@example.com",
                "StrongPassword123!",
                "First User",
                "USD",
                "US",
                "UTC",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with short password should return 400")
    void registerWeakPasswordReturns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "short@example.com",
                "short",
                "Alex",
                "USD",
                "US",
                "UTC",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials returns 200 and JWT tokens")
    void loginSuccess() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "loginuser@example.com",
                "ValidSecret123!",
                "Login User",
                "EUR",
                "DE",
                "Europe/Berlin",
                "de-DE"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("loginuser@example.com", "ValidSecret123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.user.email").value("loginuser@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid password returns 401")
    void loginInvalidPasswordReturns401() throws Exception {
        LoginRequest loginReq = new LoginRequest("nonexistent@example.com", "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Token refresh flow: use refresh token to obtain new access token")
    void tokenRefreshSuccess() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "refreshuser@example.com",
                "Password123!",
                "Refresh Tester",
                "GBP",
                "GB",
                "Europe/London",
                "en-GB"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("refreshuser@example.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = jsonNode.path("data").path("refreshToken").asText();

        RefreshRequest refreshReq = new RefreshRequest(refreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me without token returns 401 with standard ErrorResponse")
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with valid Bearer token returns current user")
    void meWithBearerTokenReturnsUser() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "meuser@example.com",
                "Password123!",
                "Me Tester",
                "USD",
                "US",
                "UTC",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("meuser@example.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = jsonNode.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("meuser@example.com"))
                .andExpect(jsonPath("$.data.displayName").value("Me Tester"));
    }
}
