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
public class ProductResponse {
    
    private Long id;
    
    private String code;
    
    private String name;
    
    private String description;
    
    private BigDecimal minAmount;
    
    private BigDecimal maxAmount;
    
    private Integer tenureMonth;
    
    private BigDecimal interestRate;
    
    private String productType;
    
    private String statusCode;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
}