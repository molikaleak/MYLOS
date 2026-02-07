package com.example.los.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.los.application.dto.ApprovalWorkflowResponse;
import com.example.los.domain.entity.TLoanApplication;
import com.example.los.domain.entity.TLoanApproval;
import com.example.los.infrastructure.repository.LoanApplicationRepository;
import com.example.los.infrastructure.repository.LoanApprovalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalWorkflowService {
    
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApprovalRepository loanApprovalRepository;
    
    @Transactional
    public ApprovalWorkflowResponse submitForApproval(Long loanApplicationId, String submittedBy) {
        log.info("Submitting loan application {} for approval by {}", loanApplicationId, submittedBy);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + loanApplicationId));
        
        // Validate current status
        if (!"DRAFT".equals(loanApplication.getStatusCode())) {
            throw new IllegalStateException(
                String.format("Loan application must be in DRAFT status to submit for approval. Current status: %s", 
                    loanApplication.getStatusCode()));
        }
        
        // Update loan application status
        loanApplication.setStatusCode("SUBMITTED");
        loanApplicationRepository.save(loanApplication);
        
        // Create initial approval record
        TLoanApproval approval = new TLoanApproval();
        approval.setLoanApplicationId(loanApplicationId);
        approval.setApprovalLevel(1); // First level approval
        approval.setApproverRole("LOAN_OFFICER");
        approval.setStatus("PENDING");
        approval.setRemarks("Submitted for initial review");
        approval.setCreatedAt(Instant.now());
        approval.setCreatedBy(submittedBy);
        
        TLoanApproval savedApproval = loanApprovalRepository.save(approval);
        log.info("Loan application {} submitted for level 1 approval", loanApplicationId);
        
        return mapToResponse(savedApproval, loanApplication);
    }
    
    @Transactional
    public ApprovalWorkflowResponse approveLevel(Long approvalId, String approverUsername, String remarks) {
        log.info("Approving level for approval ID: {} by {}", approvalId, approverUsername);
        
        TLoanApproval approval = loanApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval record not found with ID: " + approvalId));
        
        if (!"PENDING".equals(approval.getStatus())) {
            throw new IllegalStateException("Approval is not in PENDING status");
        }
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(approval.getLoanApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
        
        // Update approval record
        approval.setStatus("APPROVED");
        approval.setApprovedAt(Instant.now());
        approval.setApprovedBy(approverUsername);
        approval.setRemarks(remarks);
        loanApprovalRepository.save(approval);
        
        // Check if we need to create next level approval
        Integer nextLevel = getNextApprovalLevel(approval.getApprovalLevel(), loanApplication.getLoanAmount());
        
        if (nextLevel != null) {
            // Create next level approval
            TLoanApproval nextApproval = new TLoanApproval();
            nextApproval.setLoanApplicationId(loanApplication.getId());
            nextApproval.setApprovalLevel(nextLevel);
            nextApproval.setApproverRole(getApproverRoleForLevel(nextLevel));
            nextApproval.setStatus("PENDING");
            nextApproval.setRemarks("Awaiting level " + nextLevel + " approval");
            nextApproval.setCreatedAt(Instant.now());
            nextApproval.setCreatedBy(approverUsername);
            loanApprovalRepository.save(nextApproval);
            
            // Update loan application status
            loanApplication.setStatusCode("UNDER_REVIEW");
            loanApplicationRepository.save(loanApplication);
            
            log.info("Created level {} approval for loan application {}", nextLevel, loanApplication.getId());
        } else {
            // Final approval - update loan application status
            loanApplication.setStatusCode("APPROVED");
            loanApplicationRepository.save(loanApplication);
            
            log.info("Loan application {} fully approved", loanApplication.getId());
        }
        
        return mapToResponse(approval, loanApplication);
    }
    
    @Transactional
    public ApprovalWorkflowResponse rejectLevel(Long approvalId, String approverUsername, String rejectionReason) {
        log.info("Rejecting level for approval ID: {} by {}", approvalId, approverUsername);
        
        TLoanApproval approval = loanApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval record not found with ID: " + approvalId));
        
        if (!"PENDING".equals(approval.getStatus())) {
            throw new IllegalStateException("Approval is not in PENDING status");
        }
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(approval.getLoanApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
        
        // Update approval record
        approval.setStatus("REJECTED");
        approval.setApprovedAt(Instant.now());
        approval.setApprovedBy(approverUsername);
        approval.setRemarks("Rejected: " + rejectionReason);
        loanApprovalRepository.save(approval);
        
        // Update loan application status
        loanApplication.setStatusCode("REJECTED");
        loanApplicationRepository.save(loanApplication);
        
        log.info("Loan application {} rejected at level {}", loanApplication.getId(), approval.getApprovalLevel());
        
        return mapToResponse(approval, loanApplication);
    }
    
    @Transactional
    public ApprovalWorkflowResponse requestMoreInfo(Long approvalId, String approverUsername, String infoRequest) {
        log.info("Requesting more info for approval ID: {} by {}", approvalId, approverUsername);
        
        TLoanApproval approval = loanApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval record not found with ID: " + approvalId));
        
        if (!"PENDING".equals(approval.getStatus())) {
            throw new IllegalStateException("Approval is not in PENDING status");
        }
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(approval.getLoanApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
        
        // Update approval record
        approval.setStatus("MORE_INFO_NEEDED");
        approval.setApprovedAt(Instant.now());
        approval.setApprovedBy(approverUsername);
        approval.setRemarks("More information requested: " + infoRequest);
        loanApprovalRepository.save(approval);
        
        // Update loan application status
        loanApplication.setStatusCode("REQUIRES_MORE_INFO");
        loanApplicationRepository.save(loanApplication);
        
        log.info("More info requested for loan application {} at level {}", loanApplication.getId(), approval.getApprovalLevel());
        
        return mapToResponse(approval, loanApplication);
    }
    
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowResponse> getApprovalHistory(Long loanApplicationId) {
        log.debug("Fetching approval history for loan application: {}", loanApplicationId);
        
        List<TLoanApproval> approvals = loanApprovalRepository.findByLoanApplicationId(loanApplicationId);
        
        TLoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found with ID: " + loanApplicationId));
        
        List<ApprovalWorkflowResponse> responses = new ArrayList<>();
        for (TLoanApproval approval : approvals) {
            responses.add(mapToResponse(approval, loanApplication));
        }
        
        return responses;
    }
    
    @Transactional(readOnly = true)
    public ApprovalWorkflowResponse getCurrentApprovalLevel(Long loanApplicationId) {
        log.debug("Fetching current approval level for loan application: {}", loanApplicationId);
        
        List<TLoanApproval> approvals = loanApprovalRepository.findByLoanApplicationIdAndStatus(loanApplicationId, "PENDING");
        
        if (approvals.isEmpty()) {
            // Check if there are any approvals at all
            List<TLoanApproval> allApprovals = loanApprovalRepository.findByLoanApplicationId(loanApplicationId);
            if (allApprovals.isEmpty()) {
                throw new IllegalArgumentException("No approval records found for loan application: " + loanApplicationId);
            }
            // Return the last approval (final decision made)
            TLoanApproval lastApproval = allApprovals.get(allApprovals.size() - 1);
            TLoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
            return mapToResponse(lastApproval, loanApplication);
        }
        
        // Return the first pending approval (current level)
        TLoanApproval currentApproval = approvals.get(0);
        TLoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
        
        return mapToResponse(currentApproval, loanApplication);
    }
    
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowResponse> getPendingApprovalsByRole(String approverRole) {
        log.debug("Fetching pending approvals for role: {}", approverRole);
        
        List<TLoanApproval> approvals = loanApprovalRepository.findByApproverRoleAndStatus(approverRole, "PENDING");
        
        List<ApprovalWorkflowResponse> responses = new ArrayList<>();
        for (TLoanApproval approval : approvals) {
            TLoanApplication loanApplication = loanApplicationRepository.findById(approval.getLoanApplicationId())
                    .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));
            responses.add(mapToResponse(approval, loanApplication));
        }
        
        return responses;
    }
    
    private Integer getNextApprovalLevel(Integer currentLevel, BigDecimal loanAmount) {
        // Define approval levels based on loan amount
        if (currentLevel == null) {
            return 1; // Start with level 1
        }
        
        // Level 1: Loan Officer (up to $10,000)
        // Level 2: Branch Manager (up to $50,000)
        // Level 3: Regional Director (up to $200,000)
        // Level 4: Chief Credit Officer (above $200,000)
        
        if (currentLevel == 1 && loanAmount.compareTo(new BigDecimal("10000")) > 0) {
            return 2;
        } else if (currentLevel == 2 && loanAmount.compareTo(new BigDecimal("50000")) > 0) {
            return 3;
        } else if (currentLevel == 3 && loanAmount.compareTo(new BigDecimal("200000")) > 0) {
            return 4;
        }
        
        return null; // No more levels needed
    }
    
    private String getApproverRoleForLevel(Integer level) {
        switch (level) {
            case 1: return "LOAN_OFFICER";
            case 2: return "BRANCH_MANAGER";
            case 3: return "REGIONAL_DIRECTOR";
            case 4: return "CHIEF_CREDIT_OFFICER";
            default: return "UNKNOWN";
        }
    }
    
    private ApprovalWorkflowResponse mapToResponse(TLoanApproval approval, TLoanApplication loanApplication) {
        return ApprovalWorkflowResponse.builder()
                .id(approval.getId())
                .loanApplicationId(approval.getLoanApplicationId())
                .approvalLevel(approval.getApprovalLevel())
                .approverRole(approval.getApproverRole())
                .status(approval.getStatus())
                .remarks(approval.getRemarks())
                .createdAt(approval.getCreatedAt())
                .createdBy(approval.getCreatedBy())
                .approvedAt(approval.getApprovedAt())
                .approvedBy(approval.getApprovedBy())
                .loanApplicationStatus(loanApplication.getStatusCode())
                .loanAmount(loanApplication.getLoanAmount())
                .build();
    }
}