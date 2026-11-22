package com.budgetbuddy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Turns net balances into a short list of payments.
 *
 * <p>Greedy idea: repeatedly match the person owed the most with the person who owes the most.
 * The smaller of the two amounts is settled, which fully clears at least one of them, so the
 * result never needs more than (people - 1) payments.
 *
 * <p>Note: finding the absolute minimum number of payments is NP-hard (it can be reduced to the
 * subset-sum problem), so this greedy approach is a fast, near-optimal heuristic rather than a
 * guarantee of the true minimum.
 */
public final class DebtSimplifier {
    private DebtSimplifier() {
    }

    private record Entry(String name, long cents) {
    }

    public static List<Transfer> simplify(Map<String, Long> balances) {
        long total = balances.values().stream().mapToLong(Long::longValue).sum();
        if (total != 0) {
            throw new IllegalArgumentException("Balances must add up to zero but add up to " + total);
        }

        Comparator<Entry> largestFirst = Comparator.comparingLong(Entry::cents).reversed()
                .thenComparing(Entry::name);
        PriorityQueue<Entry> creditors = new PriorityQueue<>(largestFirst);
        PriorityQueue<Entry> debtors = new PriorityQueue<>(largestFirst);

        for (Map.Entry<String, Long> e : balances.entrySet()) {
            if (e.getValue() > 0) {
                creditors.add(new Entry(e.getKey(), e.getValue()));
            } else if (e.getValue() < 0) {
                debtors.add(new Entry(e.getKey(), -e.getValue()));
            }
        }

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Entry creditor = creditors.poll();
            Entry debtor = debtors.poll();
            long amount = Math.min(creditor.cents(), debtor.cents());
            transfers.add(new Transfer(debtor.name(), creditor.name(), amount));

            if (creditor.cents() > amount) {
                creditors.add(new Entry(creditor.name(), creditor.cents() - amount));
            }
            if (debtor.cents() > amount) {
                debtors.add(new Entry(debtor.name(), debtor.cents() - amount));
            }
        }
        return transfers;
    }
}
