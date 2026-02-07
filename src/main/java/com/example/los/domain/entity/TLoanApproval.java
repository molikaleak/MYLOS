package com.example.los.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
    private String status;

    @Column(name = "remark")
    private String remarks;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approval_level")
    private Integer approvalLevel;

    @Column(name = "approver_role", length = 50)
    private String approverRole;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}