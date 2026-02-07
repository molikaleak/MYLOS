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
public class LoanApplicationResponse {
    
    private Long id;
    
    private Long customerId;
    
    private Long productId;
    
    private Long branchId;
    
    private String applicationNumber;
    
    private BigDecimal appliedAmount;
    
    private BigDecimal approvedAmount;
    
    private Integer loanTermMonths;
    
    private String purpose;
    
    private String collateralDescription;
    
    private BigDecimal collateralValue;
    
    private String statusCode;
    
    private String statusDescription;
    
    private String remarks;
    
    private String repaymentFrequency;
    
    private String currencyCode;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
}