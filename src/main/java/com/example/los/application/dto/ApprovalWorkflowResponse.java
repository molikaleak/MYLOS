package com.example.los.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowResponse {
    
    private Long id;
    
    private Long loanApplicationId;
    
    private Integer approvalLevel;
    
    private String approverRole;
    
    private String status;
    
    private String remarks;
    
    private Instant createdAt;
    
    private String createdBy;
    
    private Instant approvedAt;
    
    private String approvedBy;
    
    private String loanApplicationStatus;
    
    private BigDecimal loanAmount;
}