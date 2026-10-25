package com.tanaz;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;


public class BudgetOverviewTest {

    private static PipedOutputStream out;
    private static PipedInputStream in;
    private String testFileDirectory = "src/test/resources/";

    @BeforeAll
    public static void setup() throws IOException {
        out = new PipedOutputStream();
        in = new PipedInputStream();
        PipedInputStream mockIn = new PipedInputStream(out);
        PipedOutputStream mockOut = new PipedOutputStream(in);
        System.setIn(mockIn);
        System.setOut(new PrintStream(mockOut));
    }

    @AfterAll
    public static void after() throws IOException {
        out.close();
    }

    @AfterEach
    public void afterEach() {
        flushConsole();
    }

    @Test
    public void testAddOneTransactionAndCheckBalance() {
        BudgetOverview budgetOverview = new BudgetOverview();
        writeUserInput("123\n321\n1\n");
        budgetOverview.addTransaction();
        BigDecimal balance = budgetOverview.viewBalanceFor("123");
        assertEquals(BigDecimal.valueOf(321), balance);
    }

    @Test
    public void testAddTransaction() {
        BudgetOverview budgetOverview = new BudgetOverview();
        writeUserInput("456\n321\n2\n");
        budgetOverview.addTransaction();
        writeUserInput("555\n200\n1\n");
        budgetOverview.addTransaction();
        writeUserInput("456\n380.50\n1\n");
        budgetOverview.addTransaction();
        assertEquals(3,budgetOverview.transactions.size());
        assertEquals("456",budgetOverview.transactions.getAll().get(0).accountNumber());
        assertEquals(BigDecimal.valueOf(200),budgetOverview.transactions.getAll().get(1).amount());
        assertEquals(TransactionType.INCOME,budgetOverview.transactions.getAll().get(2).type());

    }

    @Test
    public void testAddOneTransactionAndCheckThatItExists() {
        BudgetOverview budgetOverview = new BudgetOverview();
        writeUserInput("123\n321\n1\n");
        budgetOverview.addTransaction();
        assertEquals(1, budgetOverview.transactions.size());
    }

    @Test
    public void testViewBalanceWithOneExpense() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("356", LocalDateTime.now(), BigDecimal.valueOf(125), TransactionType.EXPENSE));
        BigDecimal balance = budgetOverview.viewBalanceFor("356");
        assertEquals(BigDecimal.valueOf(-125), balance);
    }

    @Test
    public void testViewBalanceWithExpensesAndIncomesFromTwoDifferentAccountNumbers() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("356", LocalDateTime.now(), BigDecimal.valueOf(125), TransactionType.EXPENSE));
        budgetOverview.transactions.add(new TransactionDetails("356", LocalDateTime.now(), BigDecimal.valueOf(300), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("356", LocalDateTime.now(), BigDecimal.valueOf(49.22), TransactionType.EXPENSE));
        budgetOverview.transactions.add(new TransactionDetails("357", LocalDateTime.now(), BigDecimal.valueOf(244), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("357", LocalDateTime.now(), BigDecimal.valueOf(44), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("356", LocalDateTime.now(), BigDecimal.valueOf(57.71), TransactionType.EXPENSE));
        budgetOverview.transactions.add(new TransactionDetails("356", LocalDateTime.now(), BigDecimal.valueOf(9201.01), TransactionType.INCOME));
        BigDecimal balance1 = budgetOverview.viewBalanceFor("356");
        BigDecimal balance2 = budgetOverview.viewBalanceFor("357");
        assertEquals(BigDecimal.valueOf(9269.08), balance1);
        assertEquals(BigDecimal.valueOf(288), balance2);
    }

    @Test
    public void testViewBalanceWithOneIncomeAndDecimal() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("777", LocalDateTime.now(), BigDecimal.valueOf(106.45), TransactionType.INCOME));
        BigDecimal balance = budgetOverview.viewBalanceFor("777");
        assertEquals(BigDecimal.valueOf(106.45), balance);
    }

    @Test
    public void testViewBalanceWithOneIncome() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.now(), BigDecimal.valueOf(912), TransactionType.INCOME));
        BigDecimal balance = budgetOverview.viewBalanceFor("222");
        assertEquals(BigDecimal.valueOf(912), balance);
    }

    @Test
    public void testViewTransactionWithoutTransactions() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.viewAllTransactions();
        assertConsoleLines("There are no transactions");
    }

    @Test
    public void testViewTransactionWithOneTransactions() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        budgetOverview.viewAllTransactions();
        assertConsoleLines("Type: INCOME", "From account: 222", "Amount: 912", "Date: 2026-10-25 02.24", "------------------------------");
    }

    @Test
    public void testViewTransactionWithTwoTransactions() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 12, 3, 1, 20), BigDecimal.valueOf(950.45), TransactionType.EXPENSE));
        budgetOverview.viewAllTransactions();
        assertConsoleLines("Type: INCOME", "From account: 222", "Amount: 912", "Date: 2026-10-25 02.24", "------------------------------", "Type: EXPENSE", "From account: 222", "Amount: 950.45", "Date: 2026-12-03 01.20", "------------------------------");
    }

    @Test
    public void testViewTransactionWithFourTransactionsFromTwoDifferentAccountNumbers() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 12, 3, 1, 20), BigDecimal.valueOf(950.45), TransactionType.EXPENSE));
        budgetOverview.transactions.add(new TransactionDetails("666", LocalDateTime.of(2026, 6, 20, 3, 30), BigDecimal.valueOf(50.50), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("666", LocalDateTime.of(2026, 8, 16, 1, 20), BigDecimal.valueOf(90), TransactionType.EXPENSE));
        budgetOverview.viewAllTransactions();
        assertConsoleLines("Type: INCOME", "From account: 222", "Amount: 912", "Date: 2026-10-25 02.24", "------------------------------", "Type: EXPENSE", "From account: 222", "Amount: 950.45", "Date: 2026-12-03 01.20", "------------------------------", "Type: INCOME", "From account: 666", "Amount: 50.5", "Date: 2026-06-20 03.30", "------------------------------", "Type: EXPENSE", "From account: 666", "Amount: 90", "Date: 2026-08-16 01.20", "------------------------------");
    }

    @Test
    public void testViewFilterTransactionsWithFiveTransactions() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 5, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("224", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("224", LocalDateTime.of(2026, 12, 3, 1, 20), BigDecimal.valueOf(950.45), TransactionType.EXPENSE));
        budgetOverview.transactions.add(new TransactionDetails("666", LocalDateTime.of(2026, 6, 20, 3, 30), BigDecimal.valueOf(50.50), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("666", LocalDateTime.of(2026, 8, 16, 1, 20), BigDecimal.valueOf(90), TransactionType.EXPENSE));
        writeUserInput("224\n");
        budgetOverview.filterTransactions();
        assertConsoleLines("Account number:", "Type: INCOME", "From account: 224", "Amount: 912", "Date: 2026-10-25 02.24", "------------------------------", "Type: EXPENSE", "From account: 224", "Amount: 950.45", "Date: 2026-12-03 01.20", "------------------------------");
    }

    @Test
    public void testViewFilterTransactionsWithOneTransaction() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        writeUserInput("222\n");
        budgetOverview.filterTransactions();
        assertConsoleLines("Account number:", "Type: INCOME", "From account: 222", "Amount: 912", "Date: 2026-10-25 02.24", "------------------------------");
    }

    @Test
    public void testSaveTransactionsToFileWithoutTransaction(){
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.saveToFile();
        assertConsoleLines("There are no transactions to save.");
    }

    @Test
    public void testSaveTransactionsWithOneTransactionToFile() throws IOException {
        BudgetOverview budgetOverview = new BudgetOverview();
        String filename = testFileDirectory + "test-save1.csv";
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        writeUserInput(filename + "\n");
        budgetOverview.saveToFile();
        assertConsoleLines("Filename:", "Transactions saved successfully!");
        List<String> savedLines = Files.readAllLines(Path.of(filename));
        assertEquals(1, savedLines.size());
        assertEquals("222;2026-10-25 02.24;912;INCOME", savedLines.get(0));
        Files.deleteIfExists(Path.of(filename));
    }

    @Test
    public void testSaveTransactionsWithManyTransactionToFile() throws IOException {
        BudgetOverview budgetOverview = new BudgetOverview();
        String filename = testFileDirectory + "test-save2.csv";
        budgetOverview.transactions.add(new TransactionDetails("222", LocalDateTime.of(2026, 10, 25, 2, 24), BigDecimal.valueOf(912), TransactionType.INCOME));
        budgetOverview.transactions.add(new TransactionDetails("223", LocalDateTime.of(2025, 10, 25, 2, 24), BigDecimal.valueOf(912.41), TransactionType.EXPENSE));
        budgetOverview.transactions.add(new TransactionDetails("512", LocalDateTime.of(2016, 10, 25, 2, 01), BigDecimal.valueOf(821), TransactionType.INCOME));
        writeUserInput(filename + "\n");
        budgetOverview.saveToFile();
        assertConsoleLines("Filename:", "Transactions saved successfully!");
        List<String> savedLines = Files.readAllLines(Path.of(filename));
        assertEquals(3, savedLines.size());
        assertEquals("222;2026-10-25 02.24;912;INCOME", savedLines.get(0));
        assertEquals("223;2025-10-25 02.24;912.41;EXPENSE", savedLines.get(1));
        assertEquals("512;2016-10-25 02.01;821;INCOME", savedLines.get(2));
        Files.deleteIfExists(Path.of(filename));
    }

    @Test
    public void testAddTransactionFromString() {
        BudgetOverview budgetOverview = new BudgetOverview();
        budgetOverview.addTransactionFromString("222;2026-10-25 02.24;912;INCOME");
        budgetOverview.addTransactionFromString("223;2025-10-25 02.24;912.41;EXPENSE");
        assertEquals(2, budgetOverview.transactions.size());
        TransactionDetails transaction1 = budgetOverview.transactions.getAll().get(0);
        TransactionDetails transaction2 = budgetOverview.transactions.getAll().get(1);
        assertEquals("222", transaction1.accountNumber());
        assertEquals(LocalDateTime.of(2026, 10, 25, 2, 24), transaction1.date());
        assertEquals(BigDecimal.valueOf(912), transaction1.amount());
        assertEquals(TransactionType.INCOME, transaction1.type());
        assertEquals("223", transaction2.accountNumber());
        assertEquals(LocalDateTime.of(2025, 10, 25, 2, 24), transaction2.date());
        assertEquals(BigDecimal.valueOf(912.41), transaction2.amount());
        assertEquals(TransactionType.EXPENSE, transaction2.type());
    }

    @Test
    public void testLoadTransactionsFromEmptyFile() {
        BudgetOverview budgetOverview = new BudgetOverview();
        String filename = testFileDirectory + "test-load1.csv";
        writeUserInput(filename + "\n");
        budgetOverview.loadFromFile();
        assertConsoleLines("Filename:", "0 transactions loaded successfully!");
    }

    @Test
    public void testLoad2TransactionsFromFile() {
        BudgetOverview budgetOverview = new BudgetOverview();
        String filename = testFileDirectory + "test-load2.csv";
        writeUserInput(filename + "\n");
        budgetOverview.loadFromFile();
        assertConsoleLines("Filename:", "2 transactions loaded successfully!");
        TransactionDetails transaction1 = budgetOverview.transactions.getAll().get(0);
        TransactionDetails transaction2 = budgetOverview.transactions.getAll().get(1);
        assertEquals("223", transaction1.accountNumber());
        assertEquals(LocalDateTime.of(2025, 10, 25, 2, 24), transaction1.date());
        assertEquals(BigDecimal.valueOf(912.41), transaction1.amount());
        assertEquals(TransactionType.EXPENSE, transaction1.type());
        assertEquals("222", transaction2.accountNumber());
        assertEquals(LocalDateTime.of(2026, 10, 25, 2, 24), transaction2.date());
        assertEquals(BigDecimal.valueOf(912), transaction2.amount());
        assertEquals(TransactionType.INCOME, transaction2.type());
    }

    private void writeUserInput(String input) {
        try {
            out.write(input.getBytes());
        } catch (IOException e) {
            fail(e);
        }
    }

    private void flushConsole() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            while(reader.ready()) {
                reader.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertConsoleLines(String... expected) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String[] actual = IntStream.range(0, expected.length).mapToObj(_ -> {
            try {
                if (!reader.ready()) {
                    return "";
                }
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList().toArray(new String[expected.length]);
        assertArrayEquals(expected, actual);
        try {
            boolean hasMoreLines = reader.ready();
            assertFalse(hasMoreLines, "Expected no more lines printed, but next line was: \"" + (hasMoreLines ? reader.readLine() : "") + "\"");
        } catch (IOException e) {
            fail(e);
        }
    }
}
