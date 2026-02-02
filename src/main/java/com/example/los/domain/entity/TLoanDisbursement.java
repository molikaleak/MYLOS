package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "t_loan_disbursement", schema = "public")
public class TLoanDisbursement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "loan_application_id")
    private Long loanApplicationId;

    @Column(name = "disbursement_amount", precision = 18, scale = 2)
    private BigDecimal disbursementAmount;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "method_code", length = 50)
    private String methodCode;

}