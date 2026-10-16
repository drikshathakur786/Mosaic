package com.mosaic.importer.dto;

import com.mosaic.importer.entity.ImportStatus;
import com.mosaic.importer.entity.TransactionImport;

import java.time.Instant;
import java.util.UUID;

public record ImportResponse(
        UUID id,
        String filename,
        ImportStatus status,
        int totalRows,
        int validRows,
        int invalidRows,
        int duplicateRows,
        String currency,
        String dateFormat,
        Instant createdAt,
        Instant completedAt
) {
    public static ImportResponse fromEntity(TransactionImport entity) {
        return new ImportResponse(
                entity.getId(),
                entity.getFilename(),
                entity.getStatus(),
                entity.getTotalRows(),
                entity.getValidRows(),
                entity.getInvalidRows(),
                entity.getDuplicateRows(),
                entity.getCurrency(),
                entity.getDateFormat(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }
}
