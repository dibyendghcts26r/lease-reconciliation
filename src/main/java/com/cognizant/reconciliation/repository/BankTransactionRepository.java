package com.cognizant.reconciliation.repository;

import com.cognizant.reconciliation.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, String> {
}
