package com.mosaic.importer.controller;

import com.mosaic.auth.security.UserPrincipal;
import com.mosaic.common.dto.ApiResponse;
import com.mosaic.common.dto.PageResponse;
import com.mosaic.importer.dto.CsvUploadResponse;
import com.mosaic.importer.dto.ImportConfirmRequest;
import com.mosaic.importer.dto.ImportConfirmResponse;
import com.mosaic.importer.dto.ImportPreviewRequest;
import com.mosaic.importer.dto.ImportPreviewResponse;
import com.mosaic.importer.dto.ImportResponse;
import com.mosaic.importer.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/imports")
@Tag(name = "CSV Imports", description = "4-step CSV import pipeline: upload, column map, preview & validate, and confirm")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload CSV file", description = "Uploads a bank export CSV file and infers headers and sample rows")
    public ResponseEntity<ApiResponse<CsvUploadResponse>> uploadCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "dateFormat", required = false) String dateFormat) {
        CsvUploadResponse response = importService.uploadCsv(principal.getId(), file, currency, dateFormat);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("File uploaded successfully", response));
    }

    @PostMapping("/{id}/preview")
    @Operation(summary = "Preview import", description = "Applies column mapping and validates rows for errors and duplicate transactions")
    public ResponseEntity<ApiResponse<ImportPreviewResponse>> previewImport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ImportPreviewRequest request) {
        ImportPreviewResponse response = importService.previewImport(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Import preview generated", response));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm import", description = "Commits validated transactions into user account")
    public ResponseEntity<ApiResponse<ImportConfirmResponse>> confirmImport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) ImportConfirmRequest request) {
        ImportConfirmRequest confirmReq = request != null ? request : new ImportConfirmRequest(true);
        ImportConfirmResponse response = importService.confirmImport(principal.getId(), id, confirmReq);
        return ResponseEntity.ok(ApiResponse.ok("Import confirmed and transactions created", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel import", description = "Cancels an ongoing import process")
    public ResponseEntity<ApiResponse<Void>> cancelImport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        importService.cancelImport(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Import cancelled", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get import status", description = "Retrieves metadata and processing status for an import")
    public ResponseEntity<ApiResponse<ImportResponse>> getImportById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        ImportResponse response = importService.getImportById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List imports", description = "Returns historical CSV imports for authenticated user")
    public ResponseEntity<ApiResponse<PageResponse<ImportResponse>>> getImports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ImportResponse> response = importService.getImports(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
