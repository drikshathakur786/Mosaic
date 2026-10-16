package com.mosaic.common.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Immutable value object representing a monetary amount in minor units (e.g. cents, paise).
 * ADR-003: Store money as integers (BIGINT minor units) to avoid floating-point inaccuracies.
 */
public record Money(long amountMinor, String currency) {

    public Money {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be empty");
        }
        currency = currency.trim().toUpperCase();
    }

    public static Money ofMinor(long amountMinor, String currency) {
        return new Money(amountMinor, currency);
    }

    public static Money ofMajor(BigDecimal amountMajor, String currency) {
        if (amountMajor == null) {
            return new Money(0L, currency);
        }
        int fractionDigits = getFractionDigits(currency);
        long minor = amountMajor.movePointRight(fractionDigits)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Money(minor, currency);
    }

    public BigDecimal toMajor() {
        int fractionDigits = getFractionDigits(currency);
        return BigDecimal.valueOf(amountMinor).movePointLeft(fractionDigits);
    }

    public static int getFractionDigits(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode.toUpperCase()).getDefaultFractionDigits();
        } catch (Exception e) {
            return 2;
        }
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amountMinor + other.amountMinor, this.currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(this.amountMinor - other.amountMinor, this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equalsIgnoreCase(other.currency)) {
            throw new IllegalArgumentException(
                    String.format("Cannot perform arithmetic on different currencies: %s and %s",
                            this.currency, other.currency));
        }
    }
}
