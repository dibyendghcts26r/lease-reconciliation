# AI-Assisted Lease Payment Reconciliation

Hackathon — Use case: business problem: **Manual Lease Payment Reconciliation**.

A Spring Boot service that reconciles incoming bank payments against expected lease schedules using a **hybrid approach**:

- **Deterministic rules** handle the clean, easy payments (exact lease id + exact amount) — fast, auditable, no AI.
- **AI** handles only the messy leftovers (garbled references, partial/overpayments) — and explains every decision in plain language for the audit trail.

The app runs end-to-end with **zero configuration** using a built-in mock AI, then swaps to a real LLM with a one-line change when you have model access.

---

## Requirements

- Java 17+
- Maven 3.9+ (or use the bundled `mvnw` if you add the wrapper)
- No database to install — uses in-memory H2.

## Run it

```bash
mvn spring-boot:run
```

Then:
- Swagger UI (browse + test all endpoints): http://localhost:8080/swagger-ui.html
- H2 DB console: http://localhost:8080/h2-console  (JDBC URL `jdbc:h2:mem:reconciliation`, user `sa`, blank password)

On startup you'll see: `[DataLoader] Seeded 5 leases and 6 transactions.`

---

## Endpoints (test in Postman)

| Method | URL | What it does |
|---|---|---|
| GET  | `/api/leases`       | View seeded expected lease schedule |
| GET  | `/api/transactions` | View seeded incoming bank payments (some messy) |
| POST | `/api/reconcile`    | Run the hybrid reconciliation, get the full report |
| POST | `/api/transactions` | Add your own transaction live during the demo |

### 1. See the data
`GET http://localhost:8080/api/transactions`

### 2. Run reconciliation
`POST http://localhost:8080/api/reconcile`  (no body needed)

Sample response (trimmed):

```json
{
  "totalTransactions": 6,
  "matchedByRules": 2,
  "matchedByAi": 1,
  "flagged": 2,
  "unmatched": 1,
  "aiMode": "mock",
  "results": [
    {
      "txnId": "TXN-1001",
      "matchedLeaseId": "DLL-LEASE-4471",
      "status": "MATCHED_EXACT",
      "method": "DETERMINISTIC",
      "confidence": 1.0,
      "reason": "Exact match: reference contains DLL-LEASE-4471 and amount equals the expected payment."
    },
    {
      "txnId": "TXN-1003",
      "matchedLeaseId": "DLL-LEASE-4473",
      "status": "MATCHED_AI",
      "method": "AI",
      "confidence": 0.9,
      "reason": "Reference text pointed to DLL-LEASE-4473 and the amount matches the expected payment exactly."
    },
    {
      "txnId": "TXN-1004",
      "matchedLeaseId": "DLL-LEASE-4474",
      "status": "FLAGGED",
      "method": "AI",
      "confidence": 0.75,
      "reason": "Likely PARTIAL payment for DLL-LEASE-4474. Received 600.00 vs expected 990.00 (short by 390.00)."
    },
    {
      "txnId": "TXN-1006",
      "matchedLeaseId": null,
      "status": "UNMATCHED",
      "method": "AI",
      "confidence": 0.2,
      "reason": "Reference 'monthly lease payment thanks' has no recognizable lease identifier. Needs manual review."
    }
  ]
}
```

### 3. (Demo flourish) add a messy transaction live, then reconcile again
`POST http://localhost:8080/api/transactions`

```json
{
  "txnId": "TXN-1007",
  "amount": 1200.00,
  "date": "2026-01-30",
  "reference": "jan lease dll 4471 thanks!"
}
```

Then re-run `POST /api/reconcile` and show the AI resolving your new messy reference.

---

## Demo script (2 minutes)

1. `GET /api/transactions` — point out the clean ones and the deliberately messy ones (typo'd id, partial, no id).
2. `POST /api/reconcile` — show the summary: some matched by rules instantly, the rest handled by AI.
3. Zoom into `TXN-1003` (AI matched a garbled reference), `TXN-1004` (AI flagged a partial), and `TXN-1006` (AI correctly gave up and sent it to a human).
4. Land the point: **rules give speed + auditability, AI adds judgment on the hard cases, and every decision is explained.**

---

## Switching on the real AI

The base project ships with a **mock** AI (pure Java heuristics) so it runs with no key. To use a real LLM:

1. In `pom.xml`, uncomment the Spring AI starter dependency.
   Get the exact artifact + version from https://start.spring.io (search "OpenAI" or "Anthropic") — the Spring AI artifact names changed around the 1.0 release, so generating it there guarantees the correct, version-matched string. Use whatever provider the hackathon gives you access to (e.g. Azure OpenAI).
2. Rename `src/main/java/com/cognizant/reconciliation/ai/SpringAiRealMatcher.java.txt`
   to `SpringAiRealMatcher.java`.
3. In `application.properties`, set `recon.ai.mode=real` and fill in the provider key/model
   properties (examples are in the file, commented out).
4. Run again. Everything else is unchanged — both matchers implement the same `AiMatcher`
   interface, so nothing else in the code needs to change.

---

## How it's structured

```
src/main/java/com/cognizant/reconciliation/
├── ReconciliationApplication.java      # main class
├── model/         LeaseSchedule, BankTransaction        # JPA entities
├── dto/           MatchResult, ReconciliationResponse   # API response shapes
├── repository/    *Repository                           # Spring Data JPA
├── ai/            AiMatcher (interface)
│                  MockAiMatcher       (default, no key)
│                  SpringAiRealMatcher.java.txt (real, plug-in)
├── service/       ReconciliationService                 # the hybrid pipeline
├── controller/    ReconciliationController              # REST endpoints
└── config/        DataLoader                            # seeds sample data
```

The key design idea: the **deterministic pass and the AI pass are separated**, and the AI is
hidden behind a single `AiMatcher` interface — so it's swappable, testable, and the safe/fast
path never depends on the model.
