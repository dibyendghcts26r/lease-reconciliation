package com.cognizant.reconciliation.service;

import com.cognizant.reconciliation.ai.AiMatcher;
import com.cognizant.reconciliation.dto.MatchResult;
import com.cognizant.reconciliation.dto.ReconciliationResponse;
import com.cognizant.reconciliation.model.BankTransaction;
import com.cognizant.reconciliation.model.LeaseSchedule;
import com.cognizant.reconciliation.repository.BankTransactionRepository;
import com.cognizant.reconciliation.repository.LeaseScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The heart of the project: a HYBRID reconciliation pipeline.
 *
 *   Step 1 - DETERMINISTIC: plain Java matches the clean, easy transactions
 *            (exact lease id in the reference + exact amount). Fast, auditable,
 *            no AI, no hallucination risk. This is the "easy 95%".
 *
 *   Step 2 - AI: only the leftovers (messy references, partials, unknown ids)
 *            go to the AiMatcher. This is the "hard 5%" where judgment adds value.
 *
 *   Step 3 - REPORT: everything is combined into one report, and every AI
 *            decision carries a plain-language reason for the audit trail.
 */
@Service
public class ReconciliationService {

    private final LeaseScheduleRepository leaseRepo;
    private final BankTransactionRepository txnRepo;
    private final AiMatcher aiMatcher;

    public ReconciliationService(LeaseScheduleRepository leaseRepo,
                                 BankTransactionRepository txnRepo,
                                 AiMatcher aiMatcher) {
        this.leaseRepo = leaseRepo;
        this.txnRepo = txnRepo;
        this.aiMatcher = aiMatcher;
    }

    public ReconciliationResponse reconcileAll() {
        List<LeaseSchedule> leases = leaseRepo.findAll();
        List<BankTransaction> txns = txnRepo.findAll();

        List<MatchResult> results = new ArrayList<>();
        List<BankTransaction> unresolved = new ArrayList<>();

        // ---- Step 1: deterministic pass ----
        for (BankTransaction txn : txns) {
            LeaseSchedule exact = tryExactMatch(txn, leases);
            if (exact != null) {
                results.add(new MatchResult(
                        txn.getTxnId(), exact.getLeaseId(), txn.getAmount(),
                        "MATCHED_EXACT", "DETERMINISTIC", 1.0,
                        "Exact match: reference contains " + exact.getLeaseId()
                                + " and amount equals the expected payment."
                ));
            } else {
                unresolved.add(txn);
            }
        }

        // ---- Step 2: AI handles only what's left ----
        List<MatchResult> aiResults = aiMatcher.resolve(unresolved, leases);
        results.addAll(aiResults);

        // ---- Step 3: build the summary report ----
        return buildResponse(results, txns.size());
    }

    /**
     * Deterministic rule: the reference must CONTAIN the exact lease id
     * (case-insensitive) AND the amount must equal the expected amount.
     */
    private LeaseSchedule tryExactMatch(BankTransaction txn, List<LeaseSchedule> leases) {
        String ref = txn.getReference() == null
                ? ""
                : txn.getReference().toLowerCase(Locale.ROOT);

        for (LeaseSchedule lease : leases) {
            boolean idPresent = ref.contains(lease.getLeaseId().toLowerCase(Locale.ROOT));
            boolean amountMatches = txn.getAmount().compareTo(lease.getExpectedAmount()) == 0;
            if (idPresent && amountMatches) {
                return lease;
            }
        }
        return null;
    }

    private ReconciliationResponse buildResponse(List<MatchResult> results, int total) {
        ReconciliationResponse resp = new ReconciliationResponse();
        resp.setResults(results);
        resp.setTotalTransactions(total);
        resp.setAiMode(aiMatcher.mode());

        int rules = 0, ai = 0, flagged = 0, unmatched = 0;
        for (MatchResult r : results) {
            switch (r.getStatus()) {
                case "MATCHED_EXACT" -> rules++;
                case "MATCHED_AI" -> ai++;
                case "FLAGGED" -> flagged++;
                case "UNMATCHED" -> unmatched++;
                default -> { /* no-op */ }
            }
        }
        resp.setMatchedByRules(rules);
        resp.setMatchedByAi(ai);
        resp.setFlagged(flagged);
        resp.setUnmatched(unmatched);
        return resp;
    }
}
