package com.cognizant.reconciliation.dto;

import java.util.List;

/**
 * The full reconciliation report returned to the caller:
 * a headline summary plus the line-by-line results.
 */
public class ReconciliationResponse {

    private int totalTransactions;
    private int matchedByRules;
    private int matchedByAi;
    private int flagged;
    private int unmatched;
    private String aiMode;            // "mock" or "real"
    private List<MatchResult> results;

    public ReconciliationResponse() {
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public int getMatchedByRules() {
        return matchedByRules;
    }

    public void setMatchedByRules(int matchedByRules) {
        this.matchedByRules = matchedByRules;
    }

    public int getMatchedByAi() {
        return matchedByAi;
    }

    public void setMatchedByAi(int matchedByAi) {
        this.matchedByAi = matchedByAi;
    }

    public int getFlagged() {
        return flagged;
    }

    public void setFlagged(int flagged) {
        this.flagged = flagged;
    }

    public int getUnmatched() {
        return unmatched;
    }

    public void setUnmatched(int unmatched) {
        this.unmatched = unmatched;
    }

    public String getAiMode() {
        return aiMode;
    }

    public void setAiMode(String aiMode) {
        this.aiMode = aiMode;
    }

    public List<MatchResult> getResults() {
        return results;
    }

    public void setResults(List<MatchResult> results) {
        this.results = results;
    }
}
