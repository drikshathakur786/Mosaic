package com.mosaic.importer.dto;

import com.mosaic.importer.entity.ImportStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CsvUploadResponse(
        UUID id,
        String filename,
        ImportStatus status,
        int totalRows,
        List<String> columns,
        List<Map<String, String>> sampleRows
) {}
