package com.banking.system;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionRecord {
    private UUID txId;
    private TransactionType txType;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String description;

    public TransactionRecord(TransactionType txType, String fromAccount, String toAccount, BigDecimal amount, String description) {
        this.txId = UUID.randomUUID();
        this.txType = txType;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.description = description;
    }

    // Getters
    public UUID getTxId() { return txId; }
    public TransactionType getTxType() { return txType; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getDescription() { return description; }

    // Setters for loading from database
    public void setTxId(UUID txId) { this.txId = txId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean involvesAccount(String accountNumber) {
        return accountNumber.equals(fromAccount) || accountNumber.equals(toAccount);
    }
}