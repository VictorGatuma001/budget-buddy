package com.budgetbuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LedgerTest {

    private static Ledger group(String... names) {
        Ledger ledger = new Ledger();
        for (String name : names) {
            ledger.addPerson(name);
        }
        return ledger;
    }

    @Test
    void payerIsOwedAndEveryoneElseOwesTheirShare() {
        Ledger ledger = group("A", "B", "C");
        ledger.addExpense("Dinner", "A", 3000, List.of("A", "B", "C"));

        Map<String, Long> balances = ledger.balances();
        assertEquals(2000L, (long) balances.get("A"));
        assertEquals(-1000L, (long) balances.get("B"));
        assertEquals(-1000L, (long) balances.get("C"));
    }

    @Test
    void sharesAlwaysAddUpEvenWhenTheAmountDoesNotDivideEvenly() {
        Expense expense = new Expense("Taxi", "A", 1000, List.of("A", "B", "C"));
        Map<String, Long> shares = expense.shares();

        assertEquals(1000L, shares.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(334L, (long) shares.get("A"));
        assertEquals(333L, (long) shares.get("B"));
        assertEquals(333L, (long) shares.get("C"));
    }

    @Test
    void balancesAlwaysSumToZero() {
        Ledger ledger = group("A", "B", "C", "D");
        ledger.addExpense("One", "A", 1001, List.of("A", "B", "C"));
        ledger.addExpense("Two", "D", 777, List.of("B", "D"));
        ledger.addExpense("Three", "C", 1, List.of("A", "B", "C", "D"));

        assertEquals(0L, ledger.balances().values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void duplicateParticipantsAreCountedOnce() {
        Ledger ledger = group("A", "B");
        ledger.addExpense("Snacks", "A", 1000, List.of("A", "B", "B"));
        assertEquals(500L, (long) ledger.balances().get("A"));
    }

    @Test
    void rejectsUnknownPeople() {
        Ledger ledger = group("A", "B");
        assertThrows(IllegalArgumentException.class, () -> ledger.addExpense("x", "Zed", 100, List.of("A")));
        assertThrows(IllegalArgumentException.class, () -> ledger.addExpense("x", "A", 100, List.of("A", "Zed")));
    }

    @Test
    void rejectsBadAmountsAndEmptySplits() {
        Ledger ledger = group("A", "B");
        assertThrows(IllegalArgumentException.class, () -> ledger.addExpense("x", "A", 0, List.of("A")));
        assertThrows(IllegalArgumentException.class, () -> ledger.addExpense("x", "A", -5, List.of("A")));
        assertThrows(IllegalArgumentException.class, () -> ledger.addExpense("x", "A", 100, List.of()));
    }

    @Test
    void rejectsInvalidNames() {
        Ledger ledger = new Ledger();
        assertThrows(IllegalArgumentException.class, () -> ledger.addPerson("   "));
        assertThrows(IllegalArgumentException.class, () -> ledger.addPerson("Smith, John"));
        assertThrows(IllegalArgumentException.class, () -> ledger.addPerson("A;B"));
    }

    @Test
    void moneyParsingAndFormatting() {
        assertEquals(1250L, Money.parse("12.5"));
        assertEquals(1250L, Money.parse(" 12.50 "));
        assertEquals(99L, Money.parse("0.99"));
        assertEquals(2000L, Money.parse("20"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("1.234"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("abc"));

        assertEquals("12.50", Money.format(1250));
        assertEquals("0.05", Money.format(5));
        assertEquals("-12.50", Money.format(-1250));
    }
}
