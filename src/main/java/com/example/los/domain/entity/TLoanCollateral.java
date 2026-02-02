package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "t_loan_collateral", schema = "public")
public class TLoanCollateral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "loan_application_id")
    private Long loanApplicationId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "collateral_type_code", length = 50)
    private String collateralTypeCode;

    @Column(name = "collateral_value", precision = 18, scale = 2)
    private BigDecimal collateralValue;

}