package com.mosaic.importer.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvParserService {

    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);

    private static final List<DateTimeFormatter> DEFAULT_DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    public String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public char detectDelimiter(byte[] data) {
        String sample = new String(data, 0, Math.min(data.length, 2048), StandardCharsets.UTF_8);
        int commas = countOccurrences(sample, ',');
        int semicolons = countOccurrences(sample, ';');
        int tabs = countOccurrences(sample, '\t');

        if (semicolons > commas && semicolons > tabs) {
            return ';';
        }
        if (tabs > commas && tabs > semicolons) {
            return '\t';
        }
        return ',';
    }

    public List<Map<String, String>> parseCsvRows(byte[] data, char delimiter) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));
             CSVParser parser = format.parse(reader)) {

            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                record.toMap().forEach((k, v) -> {
                    if (k != null) {
                        row.put(k.trim(), v != null ? v.trim() : "");
                    }
                });
                rows.add(row);
            }
        }

        return rows;
    }

    public List<String> extractHeaders(byte[] data, char delimiter) throws Exception {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));
             CSVParser parser = format.parse(reader)) {
            return new ArrayList<>(parser.getHeaderNames());
        }
    }

    public LocalDate parseDate(String rawDate, String customPattern) {
        if (rawDate == null || rawDate.isBlank()) {
            throw new IllegalArgumentException("Date string is empty");
        }

        String cleaned = rawDate.trim();

        if (customPattern != null && !customPattern.isBlank()) {
            try {
                return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(customPattern.trim()));
            } catch (DateTimeParseException ignored) {}
        }

        for (DateTimeFormatter formatter : DEFAULT_DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {}
        }

        throw new IllegalArgumentException("Unable to parse date: '" + rawDate + "'");
    }

    public long parseAmountToMinor(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            throw new IllegalArgumentException("Amount string is empty");
        }

        String cleaned = rawAmount.trim()
                .replaceAll("[$€£₹¥\\s]", ""); // Remove currency signs and spaces

        boolean isNegative = false;
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            isNegative = true;
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        } else if (cleaned.startsWith("-")) {
            isNegative = true;
            cleaned = cleaned.substring(1);
        }

        // Handle European comma decimals (e.g. "12,50" -> "12.50")
        if (cleaned.contains(",") && !cleaned.contains(".")) {
            cleaned = cleaned.replace(',', '.');
        } else if (cleaned.contains(",") && cleaned.contains(".")) {
            // Handle "1,250.50"
            cleaned = cleaned.replace(",", "");
        }

        try {
            BigDecimal major = new BigDecimal(cleaned);
            BigDecimal minor = major.movePointRight(2).setScale(0, RoundingMode.HALF_UP);
            long minorVal = minor.longValueExact();
            return isNegative ? -minorVal : minorVal;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid monetary amount: '" + rawAmount + "'");
        }
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) count++;
        }
        return count;
    }
}
