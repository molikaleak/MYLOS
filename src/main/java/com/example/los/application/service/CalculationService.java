package com.example.los.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.los.application.dto.EMICalculationRequest;
import com.example.los.application.dto.EMICalculationResponse;
import com.example.los.application.dto.InterestCalculationRequest;
import com.example.los.application.dto.InterestCalculationResponse;
import com.example.los.application.dto.RepaymentSchedule;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalculationService {
    
    /**
     * Calculate EMI (Equated Monthly Installment) using the formula:
     * EMI = [P x R x (1+R)^N]/[(1+R)^N-1]
     * Where:
     * P = Principal loan amount
     * R = Monthly interest rate (annual rate / 12 / 100)
     * N = Loan tenure in months
     */
    public EMICalculationResponse calculateEMI(EMICalculationRequest request) {
        log.debug("Calculating EMI for amount: {}, interest rate: {}, tenure: {} months", 
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths());
        
        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal annualInterestRate = request.getAnnualInterestRate();
        int tenureMonths = request.getTenureMonths();
        
        // Validate inputs
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount must be greater than zero");
        }
        
        if (annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be greater than zero months");
        }
        
        // Calculate monthly interest rate (annual rate / 12 / 100)
        BigDecimal monthlyInterestRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        // Calculate (1+R)^N
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal onePlusRPowN = pow(onePlusR, tenureMonths);
        
        // Calculate EMI using formula: EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
        BigDecimal numerator = principal.multiply(monthlyInterestRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);
        BigDecimal emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        
        // Calculate total payment
        BigDecimal totalPayment = emi.multiply(BigDecimal.valueOf(tenureMonths));
        
        // Calculate total interest
        BigDecimal totalInterest = totalPayment.subtract(principal);
        
        // Generate repayment schedule
        List<RepaymentSchedule> schedule = generateRepaymentSchedule(
                principal, annualInterestRate, tenureMonths, emi, request.getStartDate());
        
        log.info("EMI calculated: {} for principal: {}, rate: {}%, tenure: {} months", 
                emi, principal, annualInterestRate, tenureMonths);
        
        return EMICalculationResponse.builder()
                .principalAmount(principal)
                .annualInterestRate(annualInterestRate)
                .tenureMonths(tenureMonths)
                .emi(emi)
                .totalPayment(totalPayment)
                .totalInterest(totalInterest)
                .repaymentSchedule(schedule)
                .build();
    }
    
    /**
     * Calculate simple interest
     * Simple Interest = (P x R x T) / 100
     * Where:
     * P = Principal amount
     * R = Annual interest rate
     * T = Time in years
     */
    public InterestCalculationResponse calculateSimpleInterest(InterestCalculationRequest request) {
        log.debug("Calculating simple interest for amount: {}, rate: {}, time: {} years", 
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTimeYears());
        
        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal rate = request.getAnnualInterestRate();
        BigDecimal time = request.getTimeYears();
        
        // Calculate interest
        BigDecimal interest = principal.multiply(rate)
                .multiply(time)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Calculate total amount
        BigDecimal totalAmount = principal.add(interest);
        
        return InterestCalculationResponse.builder()
                .principalAmount(principal)
                .annualInterestRate(rate)
                .timeYears(time)
                .interest(interest)
                .totalAmount(totalAmount)
                .interestType("SIMPLE")
                .build();
    }
    
    /**
     * Calculate compound interest
     * Compound Interest = P(1 + R/100)^T - P
     * Where:
     * P = Principal amount
     * R = Annual interest rate
     * T = Time in years
     */
    public InterestCalculationResponse calculateCompoundInterest(InterestCalculationRequest request) {
        log.debug("Calculating compound interest for amount: {}, rate: {}, time: {} years", 
                request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTimeYears());
        
        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal rate = request.getAnnualInterestRate();
        BigDecimal time = request.getTimeYears();
        
        // Calculate compound amount: A = P(1 + R/100)^T
        BigDecimal onePlusRateOver100 = BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        BigDecimal compoundAmount = principal.multiply(pow(onePlusRateOver100, time.intValue()));
        
        // Calculate interest
        BigDecimal interest = compoundAmount.subtract(principal)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Round compound amount
        compoundAmount = compoundAmount.setScale(2, RoundingMode.HALF_UP);
        
        return InterestCalculationResponse.builder()
                .principalAmount(principal)
                .annualInterestRate(rate)
                .timeYears(time)
                .interest(interest)
                .totalAmount(compoundAmount)
                .interestType("COMPOUND")
                .build();
    }
    
    /**
     * Calculate processing fee based on loan amount
     * Typically 1-2% of loan amount with a minimum fee
     */
    public BigDecimal calculateProcessingFee(BigDecimal loanAmount, BigDecimal percentage, BigDecimal minFee) {
        log.debug("Calculating processing fee for amount: {}, percentage: {}, min fee: {}", 
                loanAmount, percentage, minFee);
        
        if (loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }
        
        // Calculate fee as percentage of loan amount
        BigDecimal fee = loanAmount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Apply minimum fee
        if (fee.compareTo(minFee) < 0) {
            fee = minFee;
        }
        
        log.debug("Processing fee calculated: {}", fee);
        return fee;
    }
    
    /**
     * Calculate late payment penalty
     * Typically a fixed amount or percentage of overdue amount
     */
    public BigDecimal calculateLatePaymentPenalty(BigDecimal overdueAmount, BigDecimal fixedPenalty, 
                                                 BigDecimal percentagePenalty, Integer daysLate) {
        log.debug("Calculating late payment penalty for overdue amount: {}, days late: {}", 
                overdueAmount, daysLate);
        
        BigDecimal penalty = BigDecimal.ZERO;
        
        // Add fixed penalty
        if (fixedPenalty != null) {
            penalty = penalty.add(fixedPenalty);
        }
        
        // Add percentage penalty
        if (percentagePenalty != null && overdueAmount != null) {
            BigDecimal percentageAmount = overdueAmount.multiply(percentagePenalty)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            penalty = penalty.add(percentageAmount);
        }
        
        // Apply daily penalty for extended delays
        if (daysLate != null && daysLate > 30) {
            // Additional penalty for delays over 30 days
            BigDecimal dailyPenalty = overdueAmount != null ? 
                    overdueAmount.multiply(BigDecimal.valueOf(0.0005)) : // 0.05% daily
                    BigDecimal.valueOf(5); // $5 daily
            int extraDays = daysLate - 30;
            penalty = penalty.add(dailyPenalty.multiply(BigDecimal.valueOf(extraDays)));
        }
        
        log.debug("Late payment penalty calculated: {}", penalty);
        return penalty;
    }
    
    /**
     * Calculate loan-to-value (LTV) ratio
     * LTV = (Loan Amount / Property Value) x 100
     */
    public BigDecimal calculateLTVRatio(BigDecimal loanAmount, BigDecimal propertyValue) {
        log.debug("Calculating LTV ratio for loan: {}, property value: {}", loanAmount, propertyValue);
        
        if (propertyValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Property value must be greater than zero");
        }
        
        BigDecimal ltv = loanAmount.multiply(BigDecimal.valueOf(100))
                .divide(propertyValue, 2, RoundingMode.HALF_UP);
        
        log.debug("LTV ratio calculated: {}%", ltv);
        return ltv;
    }
    
    /**
     * Generate amortization schedule (repayment schedule)
     */
    private List<RepaymentSchedule> generateRepaymentSchedule(BigDecimal principal, 
                                                             BigDecimal annualInterestRate,
                                                             int tenureMonths,
                                                             BigDecimal emi,
                                                             LocalDate startDate) {
        List<RepaymentSchedule> schedule = new ArrayList<>();
        
        BigDecimal remainingPrincipal = principal;
        BigDecimal monthlyInterestRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        LocalDate paymentDate = startDate != null ? startDate : LocalDate.now().plusMonths(1);
        
        for (int month = 1; month <= tenureMonths; month++) {
            // Calculate interest for this month
            BigDecimal interest = remainingPrincipal.multiply(monthlyInterestRate)
                    .setScale(2, RoundingMode.HALF_UP);
            
            // Calculate principal for this month
            BigDecimal principalComponent = emi.subtract(interest);
            
            // Adjust for last month to avoid rounding errors
            if (month == tenureMonths) {
                principalComponent = remainingPrincipal;
                BigDecimal lastEMI = principalComponent.add(interest);
                if (lastEMI.compareTo(emi) != 0) {
                    // Recalculate EMI for last month
                    emi = lastEMI;
                }
            }
            
            // Update remaining principal
            remainingPrincipal = remainingPrincipal.subtract(principalComponent);
            if (remainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                remainingPrincipal = BigDecimal.ZERO;
            }
            
            // Create schedule entry
            RepaymentSchedule entry = RepaymentSchedule.builder()
                    .installmentNumber(month)
                    .paymentDate(paymentDate)
                    .emi(emi)
                    .principalComponent(principalComponent)
                    .interestComponent(interest)
                    .remainingBalance(remainingPrincipal)
                    .build();
            
            schedule.add(entry);
            
            // Move to next month
            paymentDate = paymentDate.plusMonths(1);
        }
        
        return schedule;
    }
    
    /**
     * Helper method to calculate power for BigDecimal
     */
    private BigDecimal pow(BigDecimal base, int exponent) {
        BigDecimal result = BigDecimal.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(base);
        }
        return result;
    }
}