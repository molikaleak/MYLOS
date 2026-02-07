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
public class InterestCalculationResponse {
    
    private BigDecimal principalAmount;
    
    private BigDecimal annualInterestRate;
    
    private BigDecimal timeYears;
    
    private BigDecimal interest;
    
    private BigDecimal totalAmount;
    
    private String interestType;
}