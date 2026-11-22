package com.budgetbuddy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Saves and loads a ledger as a simple CSV file:
 * <pre>
 * PERSON,Amina
 * EXPENSE,Amina,3000,Amina;Brian;Cate,Dinner at Java House
 * </pre>
 * The description is the last column, so it may safely contain commas.
 */
public final class LedgerStorage {
    private LedgerStorage() {
    }

    public static void save(Ledger ledger, Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String person : ledger.people()) {
            lines.add("PERSON," + person);
        }
        for (Expense e : ledger.expenses()) {
            lines.add("EXPENSE," + e.payer() + "," + e.cents() + "," + String.join(";", e.participants())
                    + "," + e.description());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static Ledger load(Path file) throws IOException {
        Ledger ledger = new Ledger();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                if (line.startsWith("PERSON,")) {
                    ledger.addPerson(line.substring("PERSON,".length()));
                } else if (line.startsWith("EXPENSE,")) {
                    String[] parts = line.split(",", 5);  // limit 5 keeps commas inside the description
                    if (parts.length < 5) {
                        throw new IllegalArgumentException("Expected 5 columns");
                    }
                    ledger.addExpense(parts[4], parts[1], Long.parseLong(parts[2]),
                            Arrays.asList(parts[3].split(";")));
                } else {
                    throw new IllegalArgumentException("Unknown record type");
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("Bad data on line " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return ledger;
    }
}
