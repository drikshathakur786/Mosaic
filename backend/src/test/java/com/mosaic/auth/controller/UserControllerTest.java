package com.mosaic.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosaic.auth.dto.LoginRequest;
import com.mosaic.auth.dto.PreferencesUpdateRequest;
import com.mosaic.auth.dto.ProfileUpdateRequest;
import com.mosaic.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "profileuser@example.com",
                "Password123!",
                "Initial Name",
                "USD",
                "US",
                "America/New_York",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("profileuser@example.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        userToken = jsonNode.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("GET /api/v1/users/me should return authenticated user profile")
    void getMeSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profileuser@example.com"))
                .andExpect(jsonPath("$.data.displayName").value("Initial Name"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/profile should update profile metadata")
    void updateProfileSuccess() throws Exception {
        ProfileUpdateRequest updateReq = new ProfileUpdateRequest(
                "Updated Name",
                "GB",
                "Europe/London",
                "en-GB"
        );

        mockMvc.perform(patch("/api/v1/users/me/profile")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Updated Name"))
                .andExpect(jsonPath("$.data.countryCode").value("GB"))
                .andExpect(jsonPath("$.data.timezone").value("Europe/London"))
                .andExpect(jsonPath("$.data.locale").value("en-GB"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/preferences should update preferences")
    void updatePreferencesSuccess() throws Exception {
        PreferencesUpdateRequest updateReq = new PreferencesUpdateRequest(
                "EUR",
                "DD/MM/YYYY",
                "SPACES_COMMA",
                "DARK",
                false
        );

        mockMvc.perform(patch("/api/v1/users/me/preferences")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultCurrency").value("EUR"))
                .andExpect(jsonPath("$.data.dateFormat").value("DD/MM/YYYY"))
                .andExpect(jsonPath("$.data.theme").value("DARK"))
                .andExpect(jsonPath("$.data.aiEnabled").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me should soft delete account and return 204")
    void deleteAccountSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Subsequent calls with that token should fail because account is deactivated
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }
}
