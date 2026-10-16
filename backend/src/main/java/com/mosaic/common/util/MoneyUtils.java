package com.mosaic.common.util;

import com.mosaic.common.model.Money;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static String format(long amountMinor, String currencyCode, Locale locale) {
        Locale targetLocale = locale != null ? locale : Locale.US;
        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            NumberFormat format = NumberFormat.getCurrencyInstance(targetLocale);
            format.setCurrency(currency);
            BigDecimal major = Money.ofMinor(amountMinor, currencyCode).toMajor();
            return format.format(major);
        } catch (Exception e) {
            BigDecimal major = Money.ofMinor(amountMinor, currencyCode).toMajor();
            return currencyCode + " " + major.toPlainString();
        }
    }

    public static String format(Money money, Locale locale) {
        if (money == null) {
            return "$0.00";
        }
        return format(money.amountMinor(), money.currency(), locale);
    }
}
