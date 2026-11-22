package com.budgetbuddy;

/** One payment in a settlement plan: {@code from} pays {@code to} the given amount (in cents). */
public record Transfer(String from, String to, long cents) {
}
