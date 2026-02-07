package com.example.los.application.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EMICalculationResponse {
    
    private BigDecimal principalAmount;
    
    private BigDecimal annualInterestRate;
    
    private int tenureMonths;
    
    private BigDecimal emi;
    
    private BigDecimal totalPayment;
    
    private BigDecimal totalInterest;
    
    private List<RepaymentSchedule> repaymentSchedule;
}