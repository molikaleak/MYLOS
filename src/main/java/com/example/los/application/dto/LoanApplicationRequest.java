package com.example.los.application.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {
    
    private Long customerId;
    
    private Long productId;
    
    private Long branchId;
    
    private BigDecimal appliedAmount;
    
    private Integer loanTermMonths;
    
    private String purpose;
    
    private String collateralDescription;
    
    private BigDecimal collateralValue;
    
    private String remarks;
    
    private String repaymentFrequency;
    
    private String currencyCode;
}