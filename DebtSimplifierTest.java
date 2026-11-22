package com.budgetbuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DebtSimplifierTest {

    private static Map<String, Long> balances(Object... namesAndCents) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 0; i < namesAndCents.length; i += 2) {
            map.put((String) namesAndCents[i], (Long) namesAndCents[i + 1]);
        }
        return map;
    }

    @Test
    void balancedGroupNeedsNoPayments() {
        assertTrue(DebtSimplifier.simplify(balances("A", 0L, "B", 0L)).isEmpty());
    }

    @Test
    void emptyGroupNeedsNoPayments() {
        assertTrue(DebtSimplifier.simplify(balances()).isEmpty());
    }

    @Test
    void everyoneOwesTheOnePersonWhoPaid() {
        List<Transfer> plan = DebtSimplifier.simplify(balances("A", 2000L, "B", -1000L, "C", -1000L));
        assertEquals(2, plan.size());
        assertTrue(plan.contains(new Transfer("B", "A", 1000)));
        assertTrue(plan.contains(new Transfer("C", "A", 1000)));
    }

    @Test
    void chainOfDebtsCollapsesIntoOnePayment() {
        // A owes B 10 and B owes C 10: B's balance is zero, so A should pay C directly.
        List<Transfer> plan = DebtSimplifier.simplify(balances("A", -1000L, "B", 0L, "C", 1000L));
        assertEquals(List.of(new Transfer("A", "C", 1000)), plan);
    }

    @Test
    void rejectsBalancesThatDoNotAddUpToZero() {
        assertThrows(IllegalArgumentException.class, () -> DebtSimplifier.simplify(balances("A", 500L, "B", -100L)));
    }

    @Test
    void randomGroupsAreAlwaysSettledWithAtMostNMinusOnePayments() {
        Random random = new Random(7);
        for (int round = 0; round < 300; round++) {
            int n = 2 + random.nextInt(7);
            Ledger ledger = new Ledger();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                names.add("P" + i);
                ledger.addPerson("P" + i);
            }
            for (int e = 0; e < 1 + random.nextInt(12); e++) {
                List<String> participants = new ArrayList<>();
                for (String name : names) {
                    if (random.nextBoolean()) {
                        participants.add(name);
                    }
                }
                if (participants.isEmpty()) {
                    participants.add(names.get(0));
                }
                ledger.addExpense("expense", names.get(random.nextInt(n)), 1 + random.nextInt(50_000), participants);
            }

            Map<String, Long> balances = ledger.balances();
            List<Transfer> plan = DebtSimplifier.simplify(balances);

            Map<String, Long> afterPayments = new LinkedHashMap<>(balances);
            for (Transfer t : plan) {
                assertTrue(t.cents() > 0, "payments must be positive");
                assertTrue(!t.from().equals(t.to()), "nobody pays themselves");
                afterPayments.merge(t.from(), t.cents(), Long::sum);
                afterPayments.merge(t.to(), -t.cents(), Long::sum);
            }
            for (long remaining : afterPayments.values()) {
                assertEquals(0L, remaining);
            }
            assertTrue(plan.size() <= n - 1, "never more than n-1 payments");
        }
    }
}
