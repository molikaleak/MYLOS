package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "t_loan_application", schema = "public")
public class TLoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "application_no", length = 100)
    private String applicationNo;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "loan_amount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "tenure_month")
    private Integer tenureMonth;

    @Column(name = "interest_rate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "processing_fee", precision = 18, scale = 2)
    private BigDecimal processingFee;

    @Column(name = "status_code", length = 50)
    private String statusCode;

    @Column(name = "created_at")
    private Instant createdAt;

}