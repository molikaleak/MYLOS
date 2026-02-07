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
public class CustomerResponse {
    
    private Long id;
    
    private String nameEn;
    
    private String nameKh;
    
    private String phone;
    
    private Long addressId;
    
    private String email;
    
    private String idNumber;
    
    private String idType;
    
    private String dateOfBirth;
    
    private String gender;
    
    private String occupation;
    
    private BigDecimal monthlyIncome;
    
    private String maritalStatus;
    
    private String nationality;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private Integer totalLoanApplications;
    
    private Integer activeLoans;
    
    private BigDecimal totalLoanAmount;
}