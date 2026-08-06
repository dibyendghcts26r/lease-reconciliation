package com.cognizant.reconciliation.ai;

import com.cognizant.reconciliation.dto.MatchResult;
import com.cognizant.reconciliation.model.BankTransaction;
import com.cognizant.reconciliation.model.LeaseSchedule;

import java.util.List;

/**
 * The single "seam" between our app and the AI.
 *
 * The deterministic rules handle the easy 95%. Whatever they can't match
 * confidently is handed here. There are two implementations:
 *
 *   MockAiMatcher    - default; pure Java heuristics, runs with no API key.
 *   SpringAiRealMatcher - calls a real LLM (see the .txt template + README).
 *
 * Because both implement this one interface, swapping mock -> real is a
 * one-line config change and touches nothing else in the codebase.
 */
public interface AiMatcher {

    /**
     * @param unresolved transactions the rules could not match
     * @param candidates all known lease schedules to match against
     * @return one MatchResult per unresolved transaction
     */
    List<MatchResult> resolve(List<BankTransaction> unresolved, List<LeaseSchedule> candidates);

    /** "mock" or "real" - surfaced in the API response so the demo is transparent. */
    String mode();
}
