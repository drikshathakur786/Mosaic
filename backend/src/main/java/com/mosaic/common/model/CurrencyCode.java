package com.mosaic.common.model;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public final class CurrencyCode {

    private static final Set<String> VALID_CODES = Currency.getAvailableCurrencies()
            .stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toSet());

    private CurrencyCode() {}

    public static boolean isValid(String code) {
        if (code == null || code.length() != 3) {
            return false;
        }
        return VALID_CODES.contains(code.toUpperCase());
    }

    public static String normalize(String code) {
        if (code == null) {
            return "USD";
        }
        String upper = code.trim().toUpperCase();
        return isValid(upper) ? upper : "USD";
    }
}
