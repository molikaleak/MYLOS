package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "t_loan_approval", schema = "public")
public class TLoanApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "loan_application_id")
    private Long loanApplicationId;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "decision_code", length = 50)
    private String decisionCode;

    @Column(name = "remark")
    private String remark;

    @Column(name = "approved_at")
    private Instant approvedAt;

}