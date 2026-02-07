package com.example.los.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EMICalculationRequest {
    
    private BigDecimal principalAmount;
    
    private BigDecimal annualInterestRate;
    
    private int tenureMonths;
    
    private LocalDate startDate;
}