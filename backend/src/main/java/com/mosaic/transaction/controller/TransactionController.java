package com.mosaic.transaction.controller;

import com.mosaic.auth.security.UserPrincipal;
import com.mosaic.common.dto.ApiResponse;
import com.mosaic.common.dto.PageResponse;
import com.mosaic.transaction.dto.TransactionCreateRequest;
import com.mosaic.transaction.dto.TransactionFilterRequest;
import com.mosaic.transaction.dto.TransactionResponse;
import com.mosaic.transaction.dto.TransactionUpdateRequest;
import com.mosaic.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transaction creation, filtering, pagination, and updates")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Create transaction", description = "Creates a new manual financial transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TransactionCreateRequest request,
            Locale locale) {
        TransactionResponse response = transactionService.createTransaction(principal.getId(), request, locale);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transaction created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Retrieves paginated and filtered list of user transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject TransactionFilterRequest filter,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable,
            Locale locale) {
        PageResponse<TransactionResponse> response = transactionService.getTransactions(
                principal.getId(), filter, pageable, locale);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves details of a specific transaction owned by current user")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            Locale locale) {
        TransactionResponse response = transactionService.getTransactionById(principal.getId(), id, locale);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Updates transaction metadata, amount, or categorization")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request,
            Locale locale) {
        TransactionResponse response = transactionService.updateTransaction(principal.getId(), id, request, locale);
        return ResponseEntity.ok(ApiResponse.ok("Transaction updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Soft-deletes transaction from user account")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        transactionService.deleteTransaction(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
