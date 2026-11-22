package com.budgetbuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class LedgerStorageTest {

    @Test
    void saveThenLoadGivesTheSameLedger() throws IOException {
        Ledger original = new Ledger();
        original.addPerson("Amina");
        original.addPerson("Brian");
        original.addExpense("Dinner, drinks and dessert", "Amina", 4599, List.of("Amina", "Brian"));
        original.addExpense("Taxi", "Brian", 1200, List.of("Brian"));

        Path file = Files.createTempFile("budgetbuddy", ".csv");
        try {
            LedgerStorage.save(original, file);
            Ledger loaded = LedgerStorage.load(file);

            assertEquals(List.copyOf(original.people()), List.copyOf(loaded.people()));
            assertEquals(original.expenses(), loaded.expenses());
            assertEquals(original.balances(), loaded.balances());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void corruptFilesAreReportedWithALineNumber() throws IOException {
        Path file = Files.createTempFile("budgetbuddy", ".csv");
        try {
            Files.write(file, List.of("PERSON,Amina", "EXPENSE,Amina,not-a-number,Amina,Dinner"));
            IOException error = assertThrows(IOException.class, () -> LedgerStorage.load(file));
            assertEquals(true, error.getMessage().contains("line 2"));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
