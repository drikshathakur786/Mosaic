package com.mosaic.importer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosaic.auth.dto.LoginRequest;
import com.mosaic.auth.dto.RegisterRequest;
import com.mosaic.importer.dto.ColumnMapping;
import com.mosaic.importer.dto.ImportConfirmRequest;
import com.mosaic.importer.dto.ImportPreviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "importuser@example.com",
                "Password123!",
                "Import Tester",
                "USD",
                "US",
                "UTC",
                "en-US"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("importuser@example.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        userToken = jsonNode.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("Complete CSV import pipeline: upload -> preview -> confirm -> verify transactions")
    void fullImportPipeline() throws Exception {
        String csvContent = """
                Date,Description,Amount,Category
                2024-03-01,Whole Foods Market,-42.99,Groceries
                2024-03-02,Lyft Ride,-18.50,Transportation
                2024-03-03,Paycheck Deposit,2500.00,Income
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bank_statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Step 1: Upload CSV
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/imports/upload")
                        .file(file)
                        .param("currency", "USD")
                        .param("dateFormat", "yyyy-MM-dd")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("UPLOADED"))
                .andExpect(jsonPath("$.data.totalRows").value(3))
                .andExpect(jsonPath("$.data.columns").isArray())
                .andReturn();

        String importId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Step 2: Preview with column mapping
        ColumnMapping mapping = new ColumnMapping("Date", "Description", "Amount", null, null, "Category", null);
        ImportPreviewRequest previewRequest = new ImportPreviewRequest(mapping, "yyyy-MM-dd", "USD", true);

        mockMvc.perform(post("/api/v1/imports/" + importId + "/preview")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(previewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRows").value(3))
                .andExpect(jsonPath("$.data.validRows").value(3))
                .andExpect(jsonPath("$.data.invalidRows").value(0))
                .andExpect(jsonPath("$.data.preview").isArray());

        // Step 3: Confirm and commit import
        ImportConfirmRequest confirmRequest = new ImportConfirmRequest(true);
        mockMvc.perform(post("/api/v1/imports/" + importId + "/confirm")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.committedCount").value(3));

        // Step 4: Verify transactions are visible via transactions API
        mockMvc.perform(get("/api/v1/transactions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    @DisplayName("Upload duplicate file should return 409 Conflict")
    void uploadDuplicateFileReturns409() throws Exception {
        String csvContent = "Date,Merchant,Amount\n2024-03-01,Test Store,10.00\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // First upload succeeds
        mockMvc.perform(multipart("/api/v1/imports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());

        // Second upload with identical bytes fails with 409
        mockMvc.perform(multipart("/api/v1/imports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }
}
