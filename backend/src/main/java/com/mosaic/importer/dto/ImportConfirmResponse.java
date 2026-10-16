package com.mosaic.importer.dto;

import com.mosaic.importer.entity.ImportStatus;

import java.util.UUID;

public record ImportConfirmResponse(
        UUID importId,
        ImportStatus status,
        int committedCount,
        int skippedCount
) {}
