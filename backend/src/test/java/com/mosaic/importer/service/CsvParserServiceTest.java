package com.mosaic.importer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvParserServiceTest {

    private CsvParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new CsvParserService();
    }

    @Test
    @DisplayName("Should detect comma, semicolon, and tab delimiters correctly")
    void delimiterDetection() {
        byte[] commaData = "Date,Description,Amount\n2024-03-01,Store,10.00".getBytes(StandardCharsets.UTF_8);
        byte[] semicolonData = "Date;Description;Amount\n2024-03-01;Store;10,00".getBytes(StandardCharsets.UTF_8);
        byte[] tabData = "Date\tDescription\tAmount\n2024-03-01\tStore\t10.00".getBytes(StandardCharsets.UTF_8);

        assertThat(parserService.detectDelimiter(commaData)).isEqualTo(',');
        assertThat(parserService.detectDelimiter(semicolonData)).isEqualTo(';');
        assertThat(parserService.detectDelimiter(tabData)).isEqualTo('\t');
    }

    @Test
    @DisplayName("Should parse various currency and negative formatting into minor units")
    void amountParsing() {
        assertThat(parserService.parseAmountToMinor("$1,234.56")).isEqualTo(123456L);
        assertThat(parserService.parseAmountToMinor("-$45.00")).isEqualTo(-4500L);
        assertThat(parserService.parseAmountToMinor("(45.00)")).isEqualTo(-4500L);
        assertThat(parserService.parseAmountToMinor("12,50 €")).isEqualTo(1250L);
        assertThat(parserService.parseAmountToMinor("0.99")).isEqualTo(99L);

        assertThatThrownBy(() -> parserService.parseAmountToMinor("not-a-number"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should parse various date patterns into LocalDate")
    void dateParsing() {
        assertThat(parserService.parseDate("2024-03-15", null)).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(parserService.parseDate("03/15/2024", "MM/dd/yyyy")).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(parserService.parseDate("15/03/2024", "dd/MM/yyyy")).isEqualTo(LocalDate.of(2024, 3, 15));
    }

    @Test
    @DisplayName("Should parse CSV headers and rows")
    void parseCsvData() throws Exception {
        String csv = "Date,Description,Amount\n2024-03-01,Trader Joe's,45.50\n2024-03-02,Starbucks,6.75";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        List<String> headers = parserService.extractHeaders(bytes, ',');
        assertThat(headers).containsExactly("Date", "Description", "Amount");

        List<Map<String, String>> rows = parserService.parseCsvRows(bytes, ',');
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("Description")).isEqualTo("Trader Joe's");
        assertThat(rows.get(0).get("Amount")).isEqualTo("45.50");
    }
}
