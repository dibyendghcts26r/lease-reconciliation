package com.cognizant.reconciliation.dto;

import java.math.BigDecimal;

/**
 * The outcome of trying to reconcile ONE bank transaction.
 *
 * status:
 *   MATCHED_EXACT  - deterministic rule matched it (fast, no AI)
 *   MATCHED_AI     - AI figured out the most likely lease
 *   FLAGGED        - AI matched but with a caveat (partial/overpayment)
 *   UNMATCHED      - nobody could confidently match it -> human review
 */
public class MatchResult {

    private String txnId;
    private String matchedLeaseId;   // null if unmatched
    private BigDecimal amount;
    private String status;
    private String method;           // "DETERMINISTIC" or "AI"
    private double confidence;       // 0.0 - 1.0
    private String reason;           // plain-language explanation (audit trail)

    public MatchResult() {
    }

    public MatchResult(String txnId, String matchedLeaseId, BigDecimal amount,
                       String status, String method, double confidence, String reason) {
        this.txnId = txnId;
        this.matchedLeaseId = matchedLeaseId;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.confidence = confidence;
        this.reason = reason;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMatchedLeaseId() {
        return matchedLeaseId;
    }

    public void setMatchedLeaseId(String matchedLeaseId) {
        this.matchedLeaseId = matchedLeaseId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
