package com.mosaic.transaction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosaic.auth.dto.LoginRequest;
import com.mosaic.auth.dto.RegisterRequest;
import com.mosaic.category.entity.Category;
import com.mosaic.category.repository.CategoryRepository;
import com.mosaic.transaction.dto.TransactionCreateRequest;
import com.mosaic.transaction.entity.TransactionType;
import com.mosaic.transaction.dto.TransactionUpdateRequest;
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

import java.time.Instant;
import java.util.UUID;

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
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private String userToken;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() throws Exception {
        Category cat = new Category("Food & Dining", "food-dining-tx-test", "utensils", "#EF4444", true, 1);
        testCategoryId = categoryRepository.save(cat).getId();

        RegisterRequest regReq = new RegisterRequest(
                "txuser@example.com",
                "Password123!",
                "Tx Tester",
                "USD",
                "US",
                "UTC",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("txuser@example.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        userToken = jsonNode.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("POST /api/v1/transactions creates transaction and returns 201")
    void createTransactionSuccess() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                4500L, // $45.00
                "USD",
                "Trader Joe's",
                "Weekly grocery shopping",
                testCategoryId,
                TransactionType.EXPENSE,
                null,
                Instant.now(),
                "America/New_York"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchantName").value("Trader Joe's"))
                .andExpect(jsonPath("$.data.amountMinor").value(4500))
                .andExpect(jsonPath("$.data.category.id").value(testCategoryId.toString()))
                .andExpect(jsonPath("$.data.transactionType").value("EXPENSE"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions retrieves paginated user transactions")
    void getTransactionsPaginated() throws Exception {
        TransactionCreateRequest req = new TransactionCreateRequest(
                1200L,
                "USD",
                "Blue Bottle Coffee",
                "Morning latte",
                testCategoryId,
                TransactionType.EXPENSE,
                null,
                Instant.now(),
                "UTC"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transactions?page=0&size=10&merchant=Blue Bottle")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].merchantName").value("Blue Bottle Coffee"));
    }

    @Test
    @DisplayName("PATCH /api/v1/transactions/{id} updates amount and description")
    void updateTransactionSuccess() throws Exception {
        TransactionCreateRequest req = new TransactionCreateRequest(
                2000L,
                "USD",
                "Original Merchant",
                "Initial note",
                testCategoryId,
                TransactionType.EXPENSE,
                null,
                Instant.now(),
                "UTC"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String txId = jsonNode.path("data").path("id").asText();

        TransactionUpdateRequest updateReq = new TransactionUpdateRequest(
                2500L,
                null,
                "Updated Merchant",
                "Updated note",
                null,
                null,
                null,
                null,
                null,
                true
        );

        mockMvc.perform(patch("/api/v1/transactions/" + txId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amountMinor").value(2500))
                .andExpect(jsonPath("$.data.merchantName").value("Updated Merchant"))
                .andExpect(jsonPath("$.data.description").value("Updated note"))
                .andExpect(jsonPath("$.data.isReviewed").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/transactions/{id} soft-deletes transaction")
    void deleteTransactionSuccess() throws Exception {
        TransactionCreateRequest req = new TransactionCreateRequest(
                1500L,
                "USD",
                "To Delete Store",
                null,
                testCategoryId,
                TransactionType.EXPENSE,
                null,
                Instant.now(),
                "UTC"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String txId = jsonNode.path("data").path("id").asText();

        mockMvc.perform(delete("/api/v1/transactions/" + txId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Subsequent get should return 404 (not found / deleted)
        mockMvc.perform(get("/api/v1/transactions/" + txId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Tenant isolation: User B cannot access User A's transaction")
    void tenantIsolationPreventsCrossUserAccess() throws Exception {
        // Create transaction as User A
        TransactionCreateRequest req = new TransactionCreateRequest(
                5000L,
                "USD",
                "User A Confidential Merchant",
                null,
                testCategoryId,
                TransactionType.EXPENSE,
                null,
                Instant.now(),
                "UTC"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String txId = jsonNode.path("data").path("id").asText();

        // Register and login as User B
        RegisterRequest userBReq = new RegisterRequest(
                "userb@example.com",
                "Password123!",
                "User B",
                "USD",
                "US",
                "UTC",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBReq)))
                .andExpect(status().isCreated());

        LoginRequest loginBReq = new LoginRequest("userb@example.com", "Password123!");
        MvcResult loginBResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBReq)))
                .andExpect(status().isOk())
                .andReturn();

        String userBToken = objectMapper.readTree(loginBResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // User B attempts to access User A's transaction -> 404 (prevents resource enumeration)
        mockMvc.perform(get("/api/v1/transactions/" + txId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions with negative amount should fail validation with 400")
    void negativeAmountFailsValidation() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
                -500L,
                "USD",
                "Invalid Store",
                null,
                testCategoryId,
                TransactionType.EXPENSE,
                null,
                Instant.now(),
                "UTC"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
