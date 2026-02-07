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
public class RepaymentSchedule {
    
    private int installmentNumber;
    
    private LocalDate paymentDate;
    
    private BigDecimal emi;
    
    private BigDecimal principalComponent;
    
    private BigDecimal interestComponent;
    
    private BigDecimal remainingBalance;
}