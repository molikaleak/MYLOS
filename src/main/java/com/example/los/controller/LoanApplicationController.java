package com.example.los.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.los.application.dto.LoanApplicationRequest;
import com.example.los.application.dto.LoanApplicationResponse;
import com.example.los.application.service.LoanApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {
    
    private final LoanApplicationService loanApplicationService;
    
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(@RequestBody LoanApplicationRequest request) {
        try {
            LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);
            log.info("Loan application created successfully: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create loan application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                LoanApplicationResponse.builder()
                    .remarks(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error creating loan application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                LoanApplicationResponse.builder()
                    .remarks("Internal server error")
                    .build()
            );
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getLoanApplicationById(@PathVariable Long id) {
        try {
            LoanApplicationResponse response = loanApplicationService.getLoanApplicationById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LoanApplicationResponse.builder()
                    .remarks(e.getMessage())
                    .build()
            );
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplicationResponse>> getLoanApplicationsByCustomerId(@PathVariable Long customerId) {
        try {
            List<LoanApplicationResponse> applications = loanApplicationService.getLoanApplicationsByCustomerId(customerId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error fetching loan applications for customer {}: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{statusCode}")
    public ResponseEntity<List<LoanApplicationResponse>> getLoanApplicationsByStatus(@PathVariable String statusCode) {
        try {
            List<LoanApplicationResponse> applications = loanApplicationService.getLoanApplicationsByStatus(statusCode);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error fetching loan applications with status {}: {}", statusCode, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanApplicationResponse> updateLoanApplicationStatus(
            @PathVariable Long id,
            @RequestParam String statusCode,
            @RequestParam(required = false) String remarks) {
        try {
            LoanApplicationResponse response = loanApplicationService.updateLoanApplicationStatus(id, statusCode, remarks);
            log.info("Loan application {} status updated to: {}", id, statusCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update loan application {} status: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                LoanApplicationResponse.builder()
                    .remarks(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error updating loan application status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                LoanApplicationResponse.builder()
                    .remarks("Internal server error")
                    .build()
            );
        }
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<LoanApplicationResponse> approveLoanApplication(
            @PathVariable Long id,
            @RequestParam BigDecimal approvedAmount,
            @RequestParam String approvedBy) {
        try {
            LoanApplicationResponse response = loanApplicationService.approveLoanApplication(id, approvedAmount, approvedBy);
            log.info("Loan application {} approved with amount: {}", id, approvedAmount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to approve loan application {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                LoanApplicationResponse.builder()
                    .remarks(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error approving loan application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                LoanApplicationResponse.builder()
                    .remarks("Internal server error")
                    .build()
            );
        }
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<LoanApplicationResponse> rejectLoanApplication(
            @PathVariable Long id,
            @RequestParam String rejectionReason,
            @RequestParam String rejectedBy) {
        try {
            LoanApplicationResponse response = loanApplicationService.rejectLoanApplication(id, rejectionReason, rejectedBy);
            log.info("Loan application {} rejected", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to reject loan application {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                LoanApplicationResponse.builder()
                    .remarks(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error rejecting loan application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                LoanApplicationResponse.builder()
                    .remarks("Internal server error")
                    .build()
            );
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoanApplication(@PathVariable Long id) {
        try {
            loanApplicationService.deleteLoanApplication(id);
            log.info("Loan application deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Loan application not found for deletion: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Cannot delete loan application {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting loan application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/branch/{branchId}/total-approved")
    public ResponseEntity<BigDecimal> getTotalApprovedAmountByBranch(@PathVariable Long branchId) {
        try {
            BigDecimal total = loanApplicationService.calculateTotalApprovedAmountByBranch(branchId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            log.error("Error calculating total approved amount for branch {}: {}", branchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{statusCode}/count-since")
    public ResponseEntity<Long> countApplicationsByStatusSince(
            @PathVariable String statusCode,
            @RequestParam Instant sinceDate) {
        try {
            long count = loanApplicationService.countApplicationsByStatusSince(statusCode, sinceDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting applications with status {} since {}: {}", statusCode, sinceDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Loan application service is running");
    }
}