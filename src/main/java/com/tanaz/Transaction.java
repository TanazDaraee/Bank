package com.tanaz;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Transaction {
    private final TransactionStore<TransactionDetails> transactions = new TransactionStore<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm");
    private TransactionType type;
    private String counterAccountNumber;
    private HashMap<String, TransactionType> transactionTypes; //Ska dessa bort?
    private HashMap<String, TransactionDetails> transactionDetails;

    public Transaction() {
        this.transactionTypes = new HashMap<>();
        this.transactionDetails = new HashMap<>();
    }

    public void printMenu() {
        IO.println("Menu");
        IO.println("Select an option:");
        IO.println("1. Add transaction");
        IO.println("2. View all transactions");
        IO.println("3. View balance");
        IO.println("4. Filter transactions");
        IO.println("5. Save to a BLL file");
        IO.println("e. Exit");
    }

    public void start() {
        while (true) {
            printMenu();
            String option = IO.readln();
            if ("1".equals(option)) {
                addTransaction();
            } else if ("2".equals(option)) {
                viewTransaction();
            } else if ("3".equals(option)) {
                viewBalance();
            } else if ("4".equals(option)) {
                filterTransaction();
            } else if ("5".equals(option)) {
                saveFile();
            } else if ("e".equals(option)) {
                exit();
            }
        }
    }

    public void exit() {
        IO.println("Bye bye");
        System.exit(0);
    }

    public void addTransaction() {
        String accountNumber = readAccountNumber("Accountnumber:");
        BigDecimal amount = readAmount();
        option(accountNumber, amount);
    }

    private String readAccountNumber(String input) {
        while (true) {
            IO.println(input);
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
        IO.println("1. Deposit");
        IO.println("2. Withdrawal");
        IO.println("3. Transfer");
    }

    public void option(String accountNumber, BigDecimal amount) {
        while (true) {
            printTransactionType();
            String option = IO.readln();

            if ("1".equals(option)) {
                deposit();
                break;
            } else if ("2".equals(option)) {
                withdrawal();
                break;
            } else if ("3".equals(option)) {
                transfer();
                break;
            } else {
                IO.println("Invalid option! Please choose 1, 2 or 3.");
            }
        }

        registerTransaction(accountNumber, amount);
    }

    public void deposit() {
        type = TransactionType.DEPOSIT;
        counterAccountNumber = null;
        IO.println("Deposit selected");
    }

    public void withdrawal() {
        type = TransactionType.WITHDRAWAL;
        counterAccountNumber = null;
        IO.println("Withdrawal selected");
    }

    public void transfer() {
        type = TransactionType.TRANSFER;
        counterAccountNumber = readAccountNumber("Recipient account number:");
        IO.println("Transfer selected");
    }

    private void registerTransaction(String accountNumber, BigDecimal amount) {
        LocalDateTime dateTime = LocalDateTime.now();

        TransactionDetails details = new TransactionDetails(accountNumber, counterAccountNumber, dateTime, amount, type);

        transactions.add(details);

        IO.println("Transaction date and time: " + dateTime.format(formatter));
        IO.println("Thank you! The transaction is now registered");
    }

    public void viewTransaction() {
        if (transactions.getAll().isEmpty()) {
            IO.println("There are no transactions");
            return;
        }

        for (TransactionDetails transaction : transactions.getAll()) {
            IO.println("Type: " + transaction.transactionType());
            IO.println("From account: " + transaction.accountNumber());

            if (transaction.counterAccountNumber() != null) {
                IO.println("To counter account: " + transaction.counterAccountNumber());
            }

            IO.println("Amount: " + transaction.amount());
            IO.println("Date: " + transaction.date().format(formatter));
            IO.println("------------------------------");
        }
    }

    public void viewBalance() {
        String accountNumber = readAccountNumber("Accountnumber:");
        BigDecimal balance = viewBalanceFor(accountNumber);
        IO.println("Balance: " + balance); 
    }

    private BigDecimal viewBalanceFor(String accountNumber) {
        BigDecimal balance = BigDecimal.ZERO;

        for (TransactionDetails transaction : transactions.getAll()) {
            if (transaction.accountNumber().equals(accountNumber)) {
                if (transaction.transactionType() == TransactionType.DEPOSIT) {
                    balance = balance.add(transaction.amount());
                } else if (transaction.transactionType() == TransactionType.WITHDRAWAL) {
                    balance = balance.subtract(transaction.amount());
                }
            }
        }

        return balance;
    }

    public void filterTransaction() {
        String accountNumber = readAccountNumber("Enter accountnumber to filter:");
        boolean found = false;

        for (TransactionDetails transaction : transactions.getAll()) {
            if (transaction.accountNumber().equals(accountNumber)) {
                IO.println("Type: " + transaction.transactionType());
                IO.println("From account: " + transaction.accountNumber());
                if (transaction.counterAccountNumber() != null) {
                    IO.println("Counter account: " + transaction.counterAccountNumber());
                }

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

    public void saveFile() {
        if (transactions.getAll().isEmpty()) {
            IO.println("There are no transactions to save.");
            return;
        }
        try {
            List<String> lines = new java.util.ArrayList<>();

            for (TransactionDetails transaction : transactions.getAll()) {String line = transaction.accountNumber() + " " + transaction.counterAccountNumber() + " " + transaction.date() + " " + transaction.amount() + " " + transaction.transactionType();lines.add(line);}

            Files.write(Path.of("transactions.txt1"), lines);
            IO.println("Transactions saved successfully!");

        } catch (IOException exception) {
            IO.println("Could not save file.");
        }
    }
}



