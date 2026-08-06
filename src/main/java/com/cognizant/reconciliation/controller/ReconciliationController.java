package com.cognizant.reconciliation.controller;

import com.cognizant.reconciliation.dto.ReconciliationResponse;
import com.cognizant.reconciliation.model.BankTransaction;
import com.cognizant.reconciliation.model.LeaseSchedule;
import com.cognizant.reconciliation.repository.BankTransactionRepository;
import com.cognizant.reconciliation.repository.LeaseScheduleRepository;
import com.cognizant.reconciliation.service.ReconciliationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for the demo. Test these in Postman (or the built-in Swagger UI
 * at http://localhost:8080/swagger-ui.html).
 *
 *   GET  /api/leases        - view the expected lease schedule (seeded)
 *   GET  /api/transactions  - view the incoming bank payments (seeded, messy)
 *   POST /api/reconcile     - run the hybrid reconciliation, get the report
 *   POST /api/transactions  - (optional) add your own transaction live in the demo
 */
@RestController
@RequestMapping("/api")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final LeaseScheduleRepository leaseRepo;
    private final BankTransactionRepository txnRepo;

    public ReconciliationController(ReconciliationService reconciliationService,
                                    LeaseScheduleRepository leaseRepo,
                                    BankTransactionRepository txnRepo) {
        this.reconciliationService = reconciliationService;
        this.leaseRepo = leaseRepo;
        this.txnRepo = txnRepo;
    }

    @GetMapping("/leases")
    public List<LeaseSchedule> getLeases() {
        return leaseRepo.findAll();
    }

    @GetMapping("/transactions")
    public List<BankTransaction> getTransactions() {
        return txnRepo.findAll();
    }

    @PostMapping("/transactions")
    public BankTransaction addTransaction(@RequestBody BankTransaction txn) {
        return txnRepo.save(txn);
    }

    @PostMapping("/reconcile")
    public ReconciliationResponse reconcile() {
        return reconciliationService.reconcileAll();
    }
}
