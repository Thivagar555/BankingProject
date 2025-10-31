package com.banking.system;

import java.security.MessageDigest;
import java.sql.*;

// Admin Manager class for admin operations
public class AdminManager {

    public boolean verifyAdmin(String username, String password) {
        // For demo purposes - in production, use secure authentication
        String adminHash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"; // 'admin'
        String userHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"; // 'password'

        String inputHash = hash(password);

        return ("admin".equals(username) && adminHash.equals(inputHash)) ||
                ("user".equals(username) && userHash.equals(inputHash));
    }

    public void viewSystemStatistics(Connection conn) {
        try {
            String sql = "SELECT " +
                    "COUNT(*) as total_accounts, " +
                    "SUM(balance) as total_balance, " +
                    "AVG(balance) as avg_balance, " +
                    "COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_accounts, " +
                    "COUNT(CASE WHEN status = 'FROZEN' THEN 1 END) as frozen_accounts, " +
                    "COUNT(CASE WHEN account_type = 'SAVINGS' THEN 1 END) as savings_accounts, " +
                    "COUNT(CASE WHEN account_type = 'CURRENT' THEN 1 END) as current_accounts " +
                    "FROM accounts";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    System.out.println("\n=== System Statistics ===");
                    System.out.println("Total Accounts: " + rs.getInt("total_accounts"));
                    System.out.println("Total Balance: " + rs.getBigDecimal("total_balance"));
                    System.out.println("Average Balance: " + rs.getBigDecimal("avg_balance"));
                    System.out.println("Active Accounts: " + rs.getInt("active_accounts"));
                    System.out.println("Frozen Accounts: " + rs.getInt("frozen_accounts"));
                    System.out.println("Savings Accounts: " + rs.getInt("savings_accounts"));
                    System.out.println("Current Accounts: " + rs.getInt("current_accounts"));
                }
            }

            // Recent activity
            System.out.println("\n=== Recent Activity ===");
            String activitySql = "SELECT transaction_type, COUNT(*) as count FROM transactions " +
                    "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                    "GROUP BY transaction_type";

            try (PreparedStatement pstmt = conn.prepareStatement(activitySql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    System.out.println(rs.getString("transaction_type") + " transactions: " + rs.getInt("count"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching system statistics: " + e.getMessage());
        }
    }

    public boolean updateAccountField(Connection conn, String accountNumber, String field, String value) {
        try {
            String sql = "UPDATE accounts SET " + field + " = ? WHERE account_number = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, value);
                pstmt.setString(2, accountNumber);

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error updating account field: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAccount(Connection conn, String accountNumber) {
        try {
            // First delete related transactions
            String deleteTransactionsSQL = "DELETE FROM transactions WHERE from_account = ? OR to_account = ?";
            // Then delete account
            String deleteAccountSQL = "DELETE FROM accounts WHERE account_number = ?";

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteTransactionsSQL)) {
                pstmt1.setString(1, accountNumber);
                pstmt1.setString(2, accountNumber);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteAccountSQL)) {
                pstmt2.setString(1, accountNumber);
                int rowsAffected = pstmt2.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            System.out.println("Error deleting account: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }
}