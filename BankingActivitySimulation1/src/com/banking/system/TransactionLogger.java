package com.banking.system;

import java.math.BigDecimal;
import java.sql.*;
import java.io.*;
import java.util.UUID;

public class TransactionLogger {
    private static final String CSV_FILE = "transactions.csv";

    public TransactionLogger() {
        initializeCSVFile();
    }

    private void initializeCSVFile() {
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE, true))) {
                    pw.println("TransactionID,Type,FromAccount,ToAccount,Amount,Timestamp,Description");
                }
                System.out.println("Transaction log file created: " + CSV_FILE);
            }
        } catch (IOException e) {
            System.out.println("Error initializing transaction log: " + e.getMessage());
        }
    }

    public void logTransaction(TransactionType txType, String fromAccount, String toAccount,
                               BigDecimal amount, String description) {
        TransactionRecord record = new TransactionRecord(txType, fromAccount, toAccount, amount, description);

        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            pw.printf("%s,%s,%s,%s,%.2f,%s,%s%n",
                    record.getTxId(),
                    record.getTxType(),
                    record.getFromAccount() != null ? record.getFromAccount() : "",
                    record.getToAccount() != null ? record.getToAccount() : "",
                    record.getAmount(),
                    record.getCreatedAt(),
                    record.getDescription());
        } catch (IOException e) {
            System.out.println("Error writing to transaction log: " + e.getMessage());
        }
    }

    public void logTransactionToDB(Connection conn, TransactionType txType, String fromAccount,
                                   String toAccount, BigDecimal amount, String description) {
        TransactionRecord record = new TransactionRecord(txType, fromAccount, toAccount, amount, description);

        String sql = "INSERT INTO transactions (transaction_id, transaction_type, from_account, to_account, amount, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getTxId().toString());
            pstmt.setString(2, record.getTxType().toString());
            pstmt.setString(3, record.getFromAccount());
            pstmt.setString(4, record.getToAccount());
            pstmt.setBigDecimal(5, record.getAmount());
            pstmt.setString(6, record.getDescription());
            pstmt.setTimestamp(7, Timestamp.valueOf(record.getCreatedAt()));

            pstmt.executeUpdate();

            logTransaction(txType, fromAccount, toAccount, amount, description);
        } catch (SQLException e) {
            System.out.println("Error logging transaction to database: " + e.getMessage());
            logTransaction(txType, fromAccount, toAccount, amount, description);
        }
    }

    public TransactionRecord findTransactionById(Connection conn, String transactionId) {
        String sql = "SELECT transaction_id, transaction_type, from_account, to_account, amount, description, created_at " +
                "FROM transactions WHERE transaction_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    TransactionType type = TransactionType.valueOf(rs.getString("transaction_type"));
                    String fromAcc = rs.getString("from_account");
                    String toAcc = rs.getString("to_account");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String description = rs.getString("description");

                    // Create a transaction record with the existing ID
                    TransactionRecord record = new TransactionRecord(type, fromAcc, toAcc, amount, description);
                    // Set the existing transaction ID instead of generating new one
                    record.setTxId(UUID.fromString(rs.getString("transaction_id")));
                    record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    return record;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding transaction by ID: " + e.getMessage());
        }
        return null;
    }

    public void printUserTransactionHistoryFromDB(Connection conn, String accountNumber) {
        String sql = "SELECT t.transaction_id, t.transaction_type, t.from_account, t.to_account, " +
                "t.amount, t.description, t.created_at, " +
                "a1.holder_name as from_holder, a2.holder_name as to_holder " +
                "FROM transactions t " +
                "LEFT JOIN accounts a1 ON t.from_account = a1.account_number " +
                "LEFT JOIN accounts a2 ON t.to_account = a2.account_number " +
                "WHERE t.from_account = ? OR t.to_account = ? " +
                "ORDER BY t.created_at DESC LIMIT 20";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean foundTransactions = false;

                System.out.printf("%-15s %-10s %-20s %-20s %-10s %-20s %s%n",
                        "TransactionID", "Type", "From", "To", "Amount", "Timestamp", "Description");
                System.out.println("-------------------------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    String fromDisplay = "";
                    if (rs.getString("from_account") != null) {
                        fromDisplay = rs.getString("from_holder") + " (" + rs.getString("from_account") + ")";
                    }

                    String toDisplay = "";
                    if (rs.getString("to_account") != null) {
                        toDisplay = rs.getString("to_holder") + " (" + rs.getString("to_account") + ")";
                    }

                    System.out.printf("%-15s %-10s %-20s %-20s %-10s %-20s %s%n",
                            rs.getString("transaction_id").substring(0, 8) + "...",
                            rs.getString("transaction_type"),
                            fromDisplay,
                            toDisplay,
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("created_at"),
                            rs.getString("description"));
                    foundTransactions = true;
                }

                if (!foundTransactions) {
                    System.out.println("No transactions found for this account.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading transaction history from database: " + e.getMessage());
        }
    }

    public void printAllTransactionsFromDB(Connection conn) {
        String sql = "SELECT t.transaction_id, t.transaction_type, t.from_account, t.to_account, " +
                "t.amount, t.description, t.created_at, " +
                "a1.holder_name as from_holder, a2.holder_name as to_holder " +
                "FROM transactions t " +
                "LEFT JOIN accounts a1 ON t.from_account = a1.account_number " +
                "LEFT JOIN accounts a2 ON t.to_account = a2.account_number " +
                "ORDER BY t.created_at DESC LIMIT 50";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n=== All Transactions (Last 50) ===");
            System.out.printf("%-15s %-10s %-20s %-20s %-10s %-20s %s%n",
                    "TransactionID", "Type", "From", "To", "Amount", "Timestamp", "Description");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            boolean foundTransactions = false;
            while (rs.next()) {
                String fromDisplay = "";
                if (rs.getString("from_account") != null) {
                    fromDisplay = rs.getString("from_holder") + " (" + rs.getString("from_account") + ")";
                }

                String toDisplay = "";
                if (rs.getString("to_account") != null) {
                    toDisplay = rs.getString("to_holder") + " (" + rs.getString("to_account") + ")";
                }

                System.out.printf("%-15s %-10s %-20s %-20s %-10s %-20s %s%n",
                        rs.getString("transaction_id").substring(0, 8) + "...",
                        rs.getString("transaction_type"),
                        fromDisplay,
                        toDisplay,
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("created_at"),
                        rs.getString("description"));
                foundTransactions = true;
            }

            if (!foundTransactions) {
                System.out.println("No transactions found.");
            }
        } catch (SQLException e) {
            System.out.println("Error reading all transactions from database: " + e.getMessage());
        }
    }
}