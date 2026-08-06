package com.cognizant.reconciliation.config;

import com.cognizant.reconciliation.model.BankTransaction;
import com.cognizant.reconciliation.model.LeaseSchedule;
import com.cognizant.reconciliation.repository.BankTransactionRepository;
import com.cognizant.reconciliation.repository.LeaseScheduleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the in-memory H2 database on startup with:
 *   - a clean lease schedule (what SHOULD be paid), and
 *   - a set of bank transactions, some clean and some DELIBERATELY MESSY,
 *     so the demo shows both the deterministic pass and the AI pass working.
 *
 * The comments on each transaction tell you which path it is designed to exercise.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final LeaseScheduleRepository leaseRepo;
    private final BankTransactionRepository txnRepo;

    public DataLoader(LeaseScheduleRepository leaseRepo, BankTransactionRepository txnRepo) {
        this.leaseRepo = leaseRepo;
        this.txnRepo = txnRepo;
    }

    @Override
    public void run(String... args) {
        // ---- Expected lease payments (the source of truth) ----
        leaseRepo.saveAll(List.of(
                new LeaseSchedule("DLL-LEASE-4471", "De Lage Landen", new BigDecimal("1200.00"), LocalDate.of(2026, 1, 15)),
                new LeaseSchedule("DLL-LEASE-4472", "De Lage Landen", new BigDecimal("850.00"),  LocalDate.of(2026, 1, 15)),
                new LeaseSchedule("DLL-LEASE-4473", "De Lage Landen", new BigDecimal("2300.00"), LocalDate.of(2026, 1, 20)),
                new LeaseSchedule("DLL-LEASE-4474", "De Lage Landen", new BigDecimal("990.00"),  LocalDate.of(2026, 1, 25)),
                new LeaseSchedule("DLL-LEASE-4475", "De Lage Landen", new BigDecimal("1500.00"), LocalDate.of(2026, 1, 28))
        ));

        // ---- Incoming bank payments ----
        txnRepo.saveAll(List.of(
                // CLEAN -> handled by the deterministic pass (MATCHED_EXACT)
                new BankTransaction("TXN-1001", new BigDecimal("1200.00"), LocalDate.of(2026, 1, 15),
                        "Payment DLL-LEASE-4471 January"),
                new BankTransaction("TXN-1002", new BigDecimal("850.00"), LocalDate.of(2026, 1, 16),
                        "DLL-LEASE-4472 lease pmt"),

                // MESSY reference, correct amount -> AI should MATCH ("4473" only)
                new BankTransaction("TXN-1003", new BigDecimal("2300.00"), LocalDate.of(2026, 1, 21),
                        "lease pmt jan dll4473"),

                // MESSY reference, PARTIAL amount -> AI should FLAG (partial)
                new BankTransaction("TXN-1004", new BigDecimal("600.00"), LocalDate.of(2026, 1, 26),
                        "part payment 4474"),

                // MESSY reference, OVERPAYMENT -> AI should FLAG (overpayment)
                new BankTransaction("TXN-1005", new BigDecimal("1600.00"), LocalDate.of(2026, 1, 28),
                        "lease 4475 pmt incl late fee"),

                // NO usable identifier -> AI should return UNMATCHED (human review)
                new BankTransaction("TXN-1006", new BigDecimal("990.00"), LocalDate.of(2026, 1, 27),
                        "monthly lease payment thanks")
        ));

        System.out.println("[DataLoader] Seeded "
                + leaseRepo.count() + " leases and "
                + txnRepo.count() + " transactions.");
    }
}
