# BudgetBuddy
 
A Java 17 command-line app that splits shared expenses between friends and works out **who should pay whom, using as few payments as possible**.
 
> Four flatmates, a dozen shared bills, and everyone is confused about who owes what. BudgetBuddy turns that tangle into a short list of payments.
 
```
> 5
Brian pays Dan 65.00 KES
Amina pays Dan 40.00 KES
Amina pays Cate 10.00 KES
3 payments settle everything.
```
*(Add a screenshot or GIF of a session here.)*
 
## Features
- Add people and expenses; split equally between any subset of the group
- **Debt simplification:** collapses chains like "A owes B, B owes C" into a single payment
- Exact money maths: amounts are stored as whole cents, never floating-point, so balances always add up to exactly zero
- Uneven splits are handled fairly: leftover cents (10.00 split 3 ways = 3.34 + 3.33 + 3.33) are assigned so shares always sum to the total
- Save and load your ledger as a plain CSV file
- Input validation with clear error messages
- Built-in demo data so you can try it in 10 seconds
## How the algorithm works
1. For each person, compute a **net balance** = (what they paid) - (their share of everything). Positive means they are owed money, negative means they owe it. All balances sum to zero.
2. Put everyone owed money into a max-heap (`PriorityQueue`) of **creditors**, and everyone who owes money into a max-heap of **debtors**.
3. Repeatedly take the biggest creditor and the biggest debtor. The debtor pays the smaller of the two amounts. That fully settles at least one of them, so they leave the heap; the other goes back with the remainder.
4. Stop when both heaps are empty.
Each step clears at least one person, so the plan needs **at most n - 1 payments** for n people, and it takes O(n log n) time.
 
**An honest note:** finding the *true* minimum number of payments is NP-hard (it is related to the subset-sum problem). This greedy method is fast and gives excellent results in practice, but it does not guarantee the absolute minimum in every case.
 
## Build & run
Requires JDK 17+ and Maven.
```bash
mvn package
java -jar target/budgetbuddy.jar
java -jar target/budgetbuddy.jar --currency KES --file trip.csv
```
Then choose `8` for demo data, `4` to see balances, and `5` to settle up.
 
Without Maven:
```bash
javac -d out src/main/java/com/budgetbuddy/*.java
java -cp out com.budgetbuddy.Main
```
 
## Testing
```bash
mvn test
```
JUnit 5 tests cover:
- balances always sum to zero, and uneven splits never lose or invent a cent
- chains of debts collapse into one payment
- **300 randomised groups**: after applying the plan every balance is exactly zero, and there are never more than n - 1 payments
- invalid input (unknown people, zero or negative amounts, bad names, malformed amounts like `1.234`)
- saving then loading gives back an identical ledger, and corrupt files report the failing line
## Project layout
```
src/main/java/com/budgetbuddy/
  Main.java            interactive menu
  Ledger.java          people, expenses, net balances
  Expense.java         one expense and its equal split (record)
  DebtSimplifier.java  the settlement algorithm
  Transfer.java        "A pays B amount" (record)
  Money.java           parse/format cents
  LedgerStorage.java   CSV save/load
src/test/java/com/budgetbuddy/   JUnit 5 tests
```
 
## Ideas for next steps
- Unequal splits (by percentage or exact amounts)
- Multiple currencies with exchange rates
- A JavaFX or Swing interface
- Export a settlement summary to PDF
 
