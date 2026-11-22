package com.budgetbuddy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money is stored as whole cents in a {@code long}. Doubles can't represent values like 0.10
 * exactly, and tiny errors would stop the balances from adding up to exactly zero.
 */
public final class Money {
    private Money() {
    }

    /** Parses "12", "12.5" or "12.50" into cents. Rejects more than two decimals. */
    public static long parse(String text) {
        try {
            BigDecimal value = new BigDecimal(text.trim());
            return value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount '" + text + "' (use up to 2 decimals, e.g. 12.50)");
        }
    }

    /** Formats cents as "12.50" (with a leading "-" for negatives). */
    public static String format(long cents) {
        long abs = Math.abs(cents);
        return (cents < 0 ? "-" : "") + abs / 100 + "." + String.format("%02d", abs % 100);
    }
}
