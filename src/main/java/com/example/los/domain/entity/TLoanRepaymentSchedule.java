package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "t_loan_repayment_schedule", schema = "public")
public class TLoanRepaymentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "loan_application_id")
    private Long loanApplicationId;

    @Column(name = "installment_no")
    private Integer installmentNo;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "principal_amount", precision = 18, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @ColumnDefault("0")
    @Column(name = "penalty_amount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @ColumnDefault("0")
    @Column(name = "paid_principal", precision = 18, scale = 2)
    private BigDecimal paidPrincipal;

    @ColumnDefault("0")
    @Column(name = "paid_interest", precision = 18, scale = 2)
    private BigDecimal paidInterest;

    @ColumnDefault("0")
    @Column(name = "paid_penalty", precision = 18, scale = 2)
    private BigDecimal paidPenalty;

    @Column(name = "payment_status_code", length = 50)
    private String paymentStatusCode;

    @Column(name = "created_at")
    private Instant createdAt;

}