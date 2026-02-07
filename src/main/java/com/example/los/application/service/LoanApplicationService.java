package com.example.los.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.los.application.dto.LoanApplicationRequest;
import com.example.los.application.dto.LoanApplicationResponse;
import com.example.los.domain.entity.MProduct;
import com.example.los.domain.entity.TCustomer;
import com.example.los.domain.entity.TLoanApplication;
import com.example.los.infrastructure.repository.CustomerRepository;
import com.example.los.infrastructure.repository.LoanApplicationRepository;
import com.example.los.infrastructure.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationService {
    
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    
    @Transactional
    public LoanApplicationResponse createLoanApplication(LoanApplicationRequest request) {
        log.info("Creating loan application for customer: {}", request.getCustomerId());
        
        // Validate customer exists
        TCustomer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomerId()));
        
        // Validate product exists and is active
        MProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + request.getProductId()));
        
        if (!"ACTIVE".equals(product.getStatusCode())) {
            throw new IllegalArgumentException("Product is not active: " + product.getCode());
        }
        
        // Validate loan amount is within product limits
        BigDecimal appliedAmount = request.getAppliedAmount();
        if (appliedAmount.compareTo(product.getMinAmount()) < 0 || 
            appliedAmount.compareTo(product.getMaxAmount()) > 0) {
            throw new IllegalArgumentException(
                String.format("Applied amount %.2f is outside product limits [%.2f - %.2f]", 
                    appliedAmount, product.getMinAmount(), product.getMaxAmount()));
        }
        
        // Create loan application
        TLoanApplication loanApplication = new TLoanApplication();
        loanApplication.setCustomerId(customer.getId());
        loanApplication.setProductId(product.getId());
        loanApplication.setApplicationNo(generateApplicationNumber());
        loanApplication.setLoanAmount(appliedAmount);
        loanApplication.setTenureMonth(request.getLoanTermMonths());
        loanApplication.setStatusCode("DRAFT");
        loanApplication.setCreatedAt(Instant.now());
        
        // Save loan application
        TLoanApplication savedApplication = loanApplicationRepository.save(loanApplication);
        log.info("Loan application created with ID: {}", savedApplication.getId());
        
        return mapToResponse(savedApplication);
    }
    
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long id) {
        log.debug("Fetching loan application with ID: {}", id);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + id));
        
        return mapToResponse(loanApplication);
    }
    
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplicationsByCustomerId(Long customerId) {
        log.debug("Fetching loan applications for customer: {}", customerId);
        
        List<TLoanApplication> applications = loanApplicationRepository.findByCustomerId(customerId);
        
        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplicationsByStatus(String statusCode) {
        log.debug("Fetching loan applications with status: {}", statusCode);
        
        List<TLoanApplication> applications = loanApplicationRepository.findByStatusCode(statusCode);
        
        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public LoanApplicationResponse updateLoanApplicationStatus(Long id, String statusCode, String remarks) {
        log.info("Updating loan application {} status to: {}", id, statusCode);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + id));
        
        // Validate status transition
        if (!isValidStatusTransition(loanApplication.getStatusCode(), statusCode)) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s", 
                    loanApplication.getStatusCode(), statusCode));
        }
        
        loanApplication.setStatusCode(statusCode);
        
        TLoanApplication updatedApplication = loanApplicationRepository.save(loanApplication);
        log.info("Loan application {} status updated to: {}", id, statusCode);
        
        return mapToResponse(updatedApplication);
    }
    
    @Transactional
    public LoanApplicationResponse approveLoanApplication(Long id, BigDecimal approvedAmount, String approvedBy) {
        log.info("Approving loan application: {}", id);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + id));
        
        if (!"UNDER_REVIEW".equals(loanApplication.getStatusCode())) {
            throw new IllegalArgumentException("Loan application must be in UNDER_REVIEW status for approval");
        }
        
        // Validate approved amount
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Approved amount must be greater than zero");
        }
        
        if (approvedAmount.compareTo(loanApplication.getLoanAmount()) > 0) {
            throw new IllegalArgumentException("Approved amount cannot exceed applied amount");
        }
        
        loanApplication.setStatusCode("APPROVED");
        loanApplication.setLoanAmount(approvedAmount); // Update with approved amount
        
        TLoanApplication updatedApplication = loanApplicationRepository.save(loanApplication);
        log.info("Loan application {} approved with amount: {}", id, approvedAmount);
        
        return mapToResponse(updatedApplication);
    }
    
    @Transactional
    public LoanApplicationResponse rejectLoanApplication(Long id, String rejectionReason, String rejectedBy) {
        log.info("Rejecting loan application: {}", id);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + id));
        
        if (!"UNDER_REVIEW".equals(loanApplication.getStatusCode())) {
            throw new IllegalArgumentException("Loan application must be in UNDER_REVIEW status for rejection");
        }
        
        loanApplication.setStatusCode("REJECTED");
        
        TLoanApplication updatedApplication = loanApplicationRepository.save(loanApplication);
        log.info("Loan application {} rejected", id);
        
        return mapToResponse(updatedApplication);
    }
    
    @Transactional
    public void deleteLoanApplication(Long id) {
        log.info("Deleting loan application: {}", id);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + id));
        
        // Only allow deletion of DRAFT applications
        if (!"DRAFT".equals(loanApplication.getStatusCode())) {
            throw new IllegalArgumentException("Only DRAFT loan applications can be deleted");
        }
        
        loanApplicationRepository.delete(loanApplication);
        log.info("Loan application {} deleted", id);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalApprovedAmountByBranch(Long branchId) {
        log.debug("Calculating total approved amount for branch: {}", branchId);
        
        Optional<Double> total = loanApplicationRepository.sumApprovedAmountByBranch(branchId);
        return BigDecimal.valueOf(total.orElse(0.0));
    }
    
    @Transactional(readOnly = true)
    public long countApplicationsByStatusSince(String statusCode, Instant sinceDate) {
        log.debug("Counting {} applications since: {}", statusCode, sinceDate);
        
        return loanApplicationRepository.countByStatusCodeSinceDate(statusCode, sinceDate);
    }
    
    private String generateApplicationNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "APP-" + timestamp.substring(timestamp.length() - 6) + "-" + random;
    }
    
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case "DRAFT":
                return List.of("SUBMITTED", "CANCELLED").contains(newStatus);
            case "SUBMITTED":
                return List.of("UNDER_REVIEW", "CANCELLED").contains(newStatus);
            case "UNDER_REVIEW":
                return List.of("APPROVED", "REJECTED", "REQUIRES_MORE_INFO").contains(newStatus);
            case "REQUIRES_MORE_INFO":
                return List.of("UNDER_REVIEW", "CANCELLED").contains(newStatus);
            case "APPROVED":
                return List.of("DISBURSED", "CANCELLED").contains(newStatus);
            case "DISBURSED":
                return List.of("ACTIVE", "CANCELLED").contains(newStatus);
            case "ACTIVE":
                return List.of("CLOSED", "DEFAULTED").contains(newStatus);
            default:
                return false;
        }
    }
    
    private LoanApplicationResponse mapToResponse(TLoanApplication loanApplication) {
        return LoanApplicationResponse.builder()
                .id(loanApplication.getId())
                .customerId(loanApplication.getCustomerId())
                .productId(loanApplication.getProductId())
                .applicationNumber(loanApplication.getApplicationNo())
                .appliedAmount(loanApplication.getLoanAmount())
                .approvedAmount(loanApplication.getLoanAmount()) // Using same field for simplicity
                .loanTermMonths(loanApplication.getTenureMonth())
                .statusCode(loanApplication.getStatusCode())
                .statusDescription(getStatusDescription(loanApplication.getStatusCode()))
                .createdAt(loanApplication.getCreatedAt())
                .build();
    }
    
    private String getStatusDescription(String statusCode) {
        switch (statusCode) {
            case "DRAFT": return "Draft - Application being prepared";
            case "SUBMITTED": return "Submitted - Application submitted for review";
            case "UNDER_REVIEW": return "Under Review - Being evaluated by loan officer";
            case "REQUIRES_MORE_INFO": return "Requires More Information - Additional documents needed";
            case "APPROVED": return "Approved - Loan application approved";
            case "REJECTED": return "Rejected - Loan application rejected";
            case "DISBURSED": return "Disbursed - Loan amount disbursed to customer";
            case "ACTIVE": return "Active - Loan is active and repayments ongoing";
            case "CLOSED": return "Closed - Loan fully repaid";
            case "DEFAULTED": return "Defaulted - Loan in default";
            case "CANCELLED": return "Cancelled - Application cancelled by customer";
            default: return "Unknown Status";
        }
    }
}