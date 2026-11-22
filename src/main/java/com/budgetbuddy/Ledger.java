package com.budgetbuddy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Keeps track of who is in the group and every expense that has been recorded. */
public class Ledger {
    private final Set<String> people = new LinkedHashSet<>();
    private final List<Expense> expenses = new ArrayList<>();

    public void addPerson(String name) {
        String clean = validateName(name);
        people.add(clean);
    }

    public void addExpense(String description, String payer, long cents, Collection<String> participants) {
        if (!people.contains(payer)) {
            throw new IllegalArgumentException("Unknown person: " + payer);
        }
        List<String> unique = new ArrayList<>(new LinkedHashSet<>(participants));
        for (String p : unique) {
            if (!people.contains(p)) {
                throw new IllegalArgumentException("Unknown person: " + p);
            }
        }
        String cleanDescription = description.replaceAll("[\\r\\n]+", " ").trim();
        expenses.add(new Expense(cleanDescription, payer, cents, unique));
    }

    /**
     * Net balance per person in cents. Positive = the group owes them money,
     * negative = they owe the group. All balances always add up to zero.
     */
    public Map<String, Long> balances() {
        Map<String, Long> balances = new LinkedHashMap<>();
        for (String person : people) {
            balances.put(person, 0L);
        }
        for (Expense e : expenses) {
            balances.merge(e.payer(), e.cents(), Long::sum);
            e.shares().forEach((person, share) -> balances.merge(person, -share, Long::sum));
        }
        return balances;
    }

    public Set<String> people() {
        return Collections.unmodifiableSet(people);
    }

    public List<Expense> expenses() {
        return Collections.unmodifiableList(expenses);
    }

    // Names are stored in a comma/semicolon-separated file, so those characters are not allowed.
    private static String validateName(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (clean.matches(".*[,;\\r\\n].*")) {
            throw new IllegalArgumentException("Names can't contain commas, semicolons or line breaks");
        }
        return clean;
    }
}
