package com.mosaic.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("Should convert minor units to major units correctly")
    void minorToMajorConversion() {
        Money usd = Money.ofMinor(1250, "USD"); // $12.50
        assertThat(usd.toMajor()).isEqualByComparingTo(new BigDecimal("12.50"));

        Money jpy = Money.ofMinor(1000, "JPY"); // ¥1000 (0 fraction digits)
        assertThat(jpy.toMajor()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("Should convert major units to minor units without loss")
    void majorToMinorConversion() {
        Money usd = Money.ofMajor(new BigDecimal("49.99"), "USD");
        assertThat(usd.amountMinor()).isEqualTo(4999L);
    }

    @Test
    @DisplayName("Should perform arithmetic on identical currencies")
    void arithmeticSameCurrency() {
        Money a = Money.ofMinor(1000, "EUR");
        Money b = Money.ofMinor(250, "EUR");

        assertThat(a.plus(b).amountMinor()).isEqualTo(1250L);
        assertThat(a.minus(b).amountMinor()).isEqualTo(750L);
    }

    @Test
    @DisplayName("Should reject arithmetic across different currencies")
    void arithmeticDifferentCurrenciesFails() {
        Money usd = Money.ofMinor(1000, "USD");
        Money eur = Money.ofMinor(1000, "EUR");

        assertThatThrownBy(() -> usd.plus(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot perform arithmetic on different currencies");
    }
}
