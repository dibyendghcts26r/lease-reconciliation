package com.cognizant.reconciliation.repository;

import com.cognizant.reconciliation.model.LeaseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaseScheduleRepository extends JpaRepository<LeaseSchedule, String> {
}
