package com.tanaz;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BudgetOverview {
    protected final TransactionStore<TransactionDetails> transactions;
    private final DateTimeFormatter formatter;

    public BudgetOverview() {
        this.transactions = new TransactionStore<>();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm");
    }

    public void printMenu() {
        IO.println("Menu");
        IO.println("Select an option:");
        IO.println("1. Add transaction");
        IO.println("2. View all transactions");
        IO.println("3. View balance");
        IO.println("4. Filter transactions");
        IO.println("5. Save to a file");
        IO.println("6. Load from file");
        IO.println("e. Exit");
    }

    public void start() {
        while (true) {
            printMenu();
            String option = IO.readln();
            if ("1".equals(option)) {
                addTransaction();
            } else if ("2".equals(option)) {
                viewAllTransactions();
            } else if ("3".equals(option)) {
                viewBalance();
            } else if ("4".equals(option)) {
                filterTransactions();
            } else if ("5".equals(option)) {
                saveToFile();
            } else if ("6".equals(option)) {
                loadFromFile();
            } else if ("e".equals(option)) {
                IO.println("Bye bye");
                break;
            }
        }
    }

    public void addTransaction() {
        String accountNumber = readAccountNumber();
        BigDecimal amount = readAmount();
        TransactionType type = readTransactionType();
        registerTransaction(accountNumber, amount, type);
    }

    private String readAccountNumber() {
        while (true) {
            IO.println("Account number:");
            String accountNumber = IO.readln();
            if (accountNumber != null && !accountNumber.isBlank()) {
                return accountNumber.trim();
            }

            IO.println("You have to specify an account.");
        }
    }

    private BigDecimal readAmount() {
        while (true) {
            IO.println("Amount:");
            String input = IO.readln();

            try {
                BigDecimal amount = new BigDecimal(input.trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    IO.println("Amount must be greater than zero!");
                    continue;
                }

                return amount;
            } catch (NumberFormatException e) {
                IO.println("Invalid amount! Please enter a valid number.");
            }
        }
    }

    public void printTransactionType() {
        IO.println("Menu");
        IO.println("Select an option:");
        IO.println("1. Income");
        IO.println("2. Expense");
    }

    public TransactionType readTransactionType() {
        while (true) {
            printTransactionType();
            String option = IO.readln();

            if ("1".equals(option)) {
                return TransactionType.INCOME;
            } else if ("2".equals(option)) {
                return TransactionType.EXPENSE;
            } else {
                IO.println("Invalid option! Please choose 1, 2 or 3.");
            }
        }
    }

    private void registerTransaction(String accountNumber, BigDecimal amount, TransactionType type) {
        LocalDateTime dateTime = LocalDateTime.now();

        TransactionDetails details = new TransactionDetails(accountNumber, dateTime, amount, type);
        transactions.add(details);
        IO.println("Transaction date and time: " + dateTime.format(formatter));
        IO.println("Thank you! The transaction is now registered");
    }

    public void viewAllTransactions() {
        if (transactions.isEmpty()) {
            IO.println("There are no transactions");
            return;
        }

        for (TransactionDetails transaction : transactions.getAll()) {
            IO.println("Type: " + transaction.type());
            IO.println("From account: " + transaction.accountNumber());
            IO.println("Amount: " + transaction.amount());
            IO.println("Date: " + transaction.date().format(formatter));
            IO.println("------------------------------");
        }
    }

    public void viewBalance() {
        String accountNumber = readAccountNumber();
        BigDecimal balance = viewBalanceFor(accountNumber);
        IO.println("Balance: " + balance);
    }

    protected BigDecimal viewBalanceFor(String accountNumber) {
        BigDecimal balance = BigDecimal.ZERO;

        for (TransactionDetails transaction : transactions.getAll()) {
            if (transaction.accountNumber().equals(accountNumber)) {
                if (transaction.type() == TransactionType.INCOME) {
                    balance = balance.add(transaction.amount());
                } else if (transaction.type() == TransactionType.EXPENSE) {
                    balance = balance.subtract(transaction.amount());
                }
            }
        }

        return balance;
    }

    public void filterTransactions() {
        String accountNumber = readAccountNumber();
        boolean found = false;

        for (TransactionDetails transaction : transactions.getAll()) {
            if (transaction.accountNumber().equals(accountNumber)) {
                IO.println("Type: " + transaction.type());
                IO.println("From account: " + transaction.accountNumber());
                IO.println("Amount: " + transaction.amount());
                IO.println("Date: " + transaction.date().format(formatter));
                IO.println("------------------------------");
                found = true;
            }
        }

        if (!found) {
            IO.println("No transactions found for account: " + accountNumber);
        }
    }

    public void saveToFile() {
        if (transactions.isEmpty()) {
            IO.println("There are no transactions to save.");
            return;
        }
        IO.println("Filename:");
        String fileName = IO.readln();
        try {
            List<String> lines = transactions.getAll().stream()
                    .map((transaction) -> transaction.accountNumber() + ";" + transaction.date().format(formatter) + ";" + transaction.amount() + ";" + transaction.type())
                    .toList();

            Files.write(Path.of(fileName), lines);
            IO.println("Transactions saved successfully!");
        } catch (IOException exception) {
            IO.println("Could not save file: " + exception.getMessage());
        }
    }

    protected void loadFromFile() {
        IO.println("Filename:");
        String fileName = IO.readln();
        try {
            List<String> lines = Files.readAllLines(Path.of(fileName));
            lines.forEach(this::addTransactionFromString);
            IO.println(lines.size() + " transactions loaded successfully!");
        } catch (IOException exception) {
            IO.println("Could not load file: " + exception.getMessage());
        }
    }

    protected void addTransactionFromString(String line) {
        String[] values = line.split(";");
        transactions.add(new TransactionDetails(values[0], LocalDateTime.parse(values[1], formatter), new BigDecimal(values[2]), TransactionType.valueOf(values[3])));
    }
}



