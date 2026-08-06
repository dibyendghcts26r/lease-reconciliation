package com.cognizant.reconciliation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One incoming bank payment - i.e. what ACTUALLY arrived.
 *
 * The `reference` field is deliberately messy free-text (like a real bank feed):
 * it may contain a clean lease id, a typo'd one, no id at all, or a note like
 * "partial". This messiness is exactly what the AI layer is meant to interpret.
 */
@Entity
public class BankTransaction {

    @Id
    private String txnId;            // e.g. "TXN-1001"

    private BigDecimal amount;

    private LocalDate date;

    private String reference;        // messy free text, e.g. "lease pmt jan dll4471"

    public BankTransaction() {
    }

    public BankTransaction(String txnId, BigDecimal amount, LocalDate date, String reference) {
        this.txnId = txnId;
        this.amount = amount;
        this.date = date;
        this.reference = reference;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
