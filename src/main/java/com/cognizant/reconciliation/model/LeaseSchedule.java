package com.cognizant.reconciliation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One expected lease payment - i.e. what SHOULD arrive.
 * This is the "source of truth" we reconcile incoming bank payments against.
 */
@Entity
public class LeaseSchedule {

    @Id
    private String leaseId;          // e.g. "DLL-LEASE-4471"

    private String clientName;       // e.g. "De Lage Landen"

    private BigDecimal expectedAmount;

    private LocalDate dueDate;

    public LeaseSchedule() {
    }

    public LeaseSchedule(String leaseId, String clientName, BigDecimal expectedAmount, LocalDate dueDate) {
        this.leaseId = leaseId;
        this.clientName = clientName;
        this.expectedAmount = expectedAmount;
        this.dueDate = dueDate;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
