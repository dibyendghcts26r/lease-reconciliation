package com.cognizant.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the AI-assisted Lease Payment Reconciliation service.
 *
 * Run with:  mvn spring-boot:run
 * Then open: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
public class ReconciliationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReconciliationApplication.class, args);
    }
}
