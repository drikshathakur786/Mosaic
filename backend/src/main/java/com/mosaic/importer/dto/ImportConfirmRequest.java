package com.mosaic.importer.dto;

import com.mosaic.importer.entity.ImportStatus;

import java.util.UUID;

public record ImportConfirmRequest(
        Boolean skipDuplicates
) {
    public boolean shouldSkipDuplicates() {
        return skipDuplicates == null || skipDuplicates;
    }
}
