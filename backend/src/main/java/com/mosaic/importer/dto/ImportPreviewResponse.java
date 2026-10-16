package com.mosaic.importer.dto;

import java.util.List;
import java.util.UUID;

public record ImportPreviewResponse(
        UUID importId,
        int totalRows,
        int validRows,
        int invalidRows,
        int duplicateRows,
        List<ImportPreviewItem> preview
) {}
