package com.budgetbuddy;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Interactive command-line menu for BudgetBuddy. */
public class Main {

    private static final String USAGE = """
            BudgetBuddy - split shared expenses with the fewest payments

            Usage: java -jar budgetbuddy.jar [options]
              --currency CODE   label shown after amounts   (default USD)
              --file PATH       file used by Save / Load    (default ledger.csv)
              --help            show this message
            """;

    public static void main(String[] args) {
        String currency = "USD";
        Path file = Path.of("ledger.csv");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--currency") && i + 1 < args.length) {
                currency = args[++i];
            } else if (args[i].equals("--file") && i + 1 < args.length) {
                file = Path.of(args[++i]);
            } else {
                System.out.print(USAGE);
                return;
            }
        }
        run(new Scanner(System.in), System.out, file, currency);
    }

    static void run(Scanner in, PrintStream out, Path file, String currency) {
        Ledger ledger = new Ledger();
        out.println("Welcome to BudgetBuddy!");
        try {
            while (true) {
                out.println();
                out.println("1) Add person       5) Settle up");
                out.println("2) Add expense      6) Save to " + file);
                out.println("3) List expenses    7) Load from " + file);
                out.println("4) Show balances    8) Load demo data");
                out.println("0) Quit");
                out.print("> ");
                out.flush();
                String choice = in.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> ledger.addPerson(ask(in, out, "Name: "));
                        case "2" -> addExpense(in, out, ledger);
                        case "3" -> listExpenses(out, ledger, currency);
                        case "4" -> showBalances(out, ledger, currency);
                        case "5" -> settleUp(out, ledger, currency);
                        case "6" -> {
                            LedgerStorage.save(ledger, file);
                            out.println("Saved to " + file);
                        }
                        case "7" -> {
                            if (Files.exists(file)) {
                                ledger = LedgerStorage.load(file);
                                out.println("Loaded " + ledger.people().size() + " people and "
                                        + ledger.expenses().size() + " expenses.");
                            } else {
                                out.println("No file found at " + file);
                            }
                        }
                        case "8" -> {
                            ledger = demoLedger();
                            out.println("Demo data loaded. Try option 4 or 5!");
                        }
                        case "0" -> {
                            out.println("Goodbye!");
                            return;
                        }
                        default -> out.println("Please choose a number from the menu.");
                    }
                } catch (IllegalArgumentException | IOException e) {
                    out.println("Error: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            out.println("\nGoodbye!");
        }
    }

    private static String ask(Scanner in, PrintStream out, String prompt) {
        out.print(prompt);
        out.flush();
        return in.nextLine().trim();
    }

    private static void addExpense(Scanner in, PrintStream out, Ledger ledger) {
        if (ledger.people().isEmpty()) {
            out.println("Add some people first (option 1).");
            return;
        }
        out.println("People: " + String.join(", ", ledger.people()));
        String description = ask(in, out, "What was it for? ");
        String payer = ask(in, out, "Who paid? ");
        long cents = Money.parse(ask(in, out, "Amount: "));
        String who = ask(in, out, "Split between (comma-separated names, or 'all'): ");

        List<String> participants;
        if (who.equalsIgnoreCase("all")) {
            participants = new ArrayList<>(ledger.people());
        } else {
            participants = Arrays.stream(who.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        ledger.addExpense(description, payer, cents, participants);
        out.println("Expense added.");
    }

    private static void listExpenses(PrintStream out, Ledger ledger, String currency) {
        if (ledger.expenses().isEmpty()) {
            out.println("No expenses yet.");
            return;
        }
        int n = 1;
        for (Expense e : ledger.expenses()) {
            out.printf("%d. %s - %s paid %s %s, split between %s%n", n++, e.description(), e.payer(),
                    Money.format(e.cents()), currency, String.join(", ", e.participants()));
        }
    }

    private static void showBalances(PrintStream out, Ledger ledger, String currency) {
        if (ledger.people().isEmpty()) {
            out.println("No people yet.");
            return;
        }
        for (Map.Entry<String, Long> e : ledger.balances().entrySet()) {
            long cents = e.getValue();
            if (cents > 0) {
                out.printf("%-10s is owed %s %s%n", e.getKey(), Money.format(cents), currency);
            } else if (cents < 0) {
                out.printf("%-10s owes %s %s%n", e.getKey(), Money.format(-cents), currency);
            } else {
                out.printf("%-10s is settled up%n", e.getKey());
            }
        }
    }

    private static void settleUp(PrintStream out, Ledger ledger, String currency) {
        List<Transfer> plan = DebtSimplifier.simplify(ledger.balances());
        if (plan.isEmpty()) {
            out.println("Everyone is settled up!");
            return;
        }
        for (Transfer t : plan) {
            out.printf("%s pays %s %s %s%n", t.from(), t.to(), Money.format(t.cents()), currency);
        }
        out.println(plan.size() + (plan.size() == 1 ? " payment" : " payments") + " settle everything.");
    }

    private static Ledger demoLedger() {
        Ledger ledger = new Ledger();
        for (String name : List.of("Amina", "Brian", "Cate", "Dan")) {
            ledger.addPerson(name);
        }
        ledger.addExpense("Groceries", "Amina", 6000, ledger.people());
        ledger.addExpense("Fuel", "Brian", 4500, List.of("Amina", "Brian", "Cate"));
        ledger.addExpense("Dinner", "Cate", 12000, ledger.people());
        ledger.addExpense("Airbnb", "Dan", 20000, ledger.people());
        return ledger;
    }
}
