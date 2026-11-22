package com.budgetbuddy;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Something one person paid for, shared equally between the listed participants. */
public record Expense(String description, String payer, long cents, List<String> participants) {

    public Expense {
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(payer, "payer");
        Objects.requireNonNull(participants, "participants");
        if (cents <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("An expense needs at least one participant");
        }
        if (new HashSet<>(participants).size() != participants.size()) {
            throw new IllegalArgumentException("Participants must not repeat");
        }
        participants = List.copyOf(participants);
    }

    /**
     * Splits the amount equally. When it doesn't divide evenly the leftover cents go to the
     * first participants, so the shares always add up to exactly the total.
     */
    public Map<String, Long> shares() {
        long base = cents / participants.size();
        long remainder = cents % participants.size();
        Map<String, Long> shares = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            shares.put(participants.get(i), base + (i < remainder ? 1 : 0));
        }
        return shares;
    }
}
