package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "t_loan_repayment", schema = "public")
public class TLoanRepayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "loan_application_id")
    private Long loanApplicationId;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "principal_paid", precision = 18, scale = 2)
    private BigDecimal principalPaid;

    @Column(name = "interest_paid", precision = 18, scale = 2)
    private BigDecimal interestPaid;

    @Column(name = "penalty_paid", precision = 18, scale = 2)
    private BigDecimal penaltyPaid;

    @Column(name = "payment_method_code", length = 50)
    private String paymentMethodCode;

    @Column(name = "received_by")
    private Long receivedBy;

    @Column(name = "created_at")
    private Instant createdAt;

}