package com.cognizant.reconciliation.ai;

import com.cognizant.reconciliation.dto.MatchResult;
import com.cognizant.reconciliation.model.BankTransaction;
import com.cognizant.reconciliation.model.LeaseSchedule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DEFAULT AI implementation - simulates the reasoning an LLM would do, using
 * plain Java heuristics. This lets the whole app run and demo end-to-end with
 * ZERO configuration and no API key.
 *
 * Active unless you set  recon.ai.mode=real  in application.properties.
 *
 * What it does with a messy reference like "lease pmt jan dll4471":
 *   1. Pull the digits out of the reference ("4471").
 *   2. Find the lease whose id contains those digits.
 *   3. Compare the amount to decide EXACT vs PARTIAL vs OVERPAYMENT.
 *   4. Return a plain-language reason (the audit trail).
 */
@Component
@ConditionalOnProperty(name = "recon.ai.mode", havingValue = "mock", matchIfMissing = true)
public class MockAiMatcher implements AiMatcher {

    @Override
    public String mode() {
        return "mock";
    }

    @Override
    public List<MatchResult> resolve(List<BankTransaction> unresolved, List<LeaseSchedule> candidates) {
        List<MatchResult> out = new ArrayList<>();

        for (BankTransaction txn : unresolved) {
            String digits = digitsOnly(txn.getReference());

            LeaseSchedule best = null;
            if (!digits.isEmpty()) {
                for (LeaseSchedule lease : candidates) {
                    if (digitsOnly(lease.getLeaseId()).contains(digits) && digits.length() >= 3) {
                        best = lease;
                        break;
                    }
                }
            }

            if (best == null) {
                // Could not confidently identify a lease -> send to a human.
                out.add(new MatchResult(
                        txn.getTxnId(), null, txn.getAmount(),
                        "UNMATCHED", "AI", 0.20,
                        "Reference '" + txn.getReference() + "' has no recognizable lease identifier. Needs manual review."
                ));
                continue;
            }

            int cmp = txn.getAmount().compareTo(best.getExpectedAmount());
            if (cmp == 0) {
                out.add(new MatchResult(
                        txn.getTxnId(), best.getLeaseId(), txn.getAmount(),
                        "MATCHED_AI", "AI", 0.90,
                        "Reference text pointed to " + best.getLeaseId()
                                + " and the amount matches the expected payment exactly."
                ));
            } else if (cmp < 0) {
                BigDecimal shortfall = best.getExpectedAmount().subtract(txn.getAmount());
                out.add(new MatchResult(
                        txn.getTxnId(), best.getLeaseId(), txn.getAmount(),
                        "FLAGGED", "AI", 0.75,
                        "Likely PARTIAL payment for " + best.getLeaseId()
                                + ". Received " + txn.getAmount() + " vs expected "
                                + best.getExpectedAmount() + " (short by " + shortfall + ")."
                ));
            } else {
                BigDecimal excess = txn.getAmount().subtract(best.getExpectedAmount());
                out.add(new MatchResult(
                        txn.getTxnId(), best.getLeaseId(), txn.getAmount(),
                        "FLAGGED", "AI", 0.75,
                        "Likely OVERPAYMENT for " + best.getLeaseId()
                                + ". Received " + txn.getAmount() + " vs expected "
                                + best.getExpectedAmount() + " (over by " + excess + ")."
                ));
            }
        }
        return out;
    }

    /** Keep only the digit characters, e.g. "dll4471 partial" -> "4471". */
    private String digitsOnly(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase(Locale.ROOT).replaceAll("[^0-9]", "");
    }
}
