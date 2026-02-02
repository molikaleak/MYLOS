package com.example.los.domain.policy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "cfg_approval_limit", schema = "public")
public class CfgApprovalLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

}