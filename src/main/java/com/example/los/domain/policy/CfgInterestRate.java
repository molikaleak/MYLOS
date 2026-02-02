package com.example.los.domain.policy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "cfg_interest_rate", schema = "public")
public class CfgInterestRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "min_tenure")
    private Integer minTenure;

    @Column(name = "max_tenure")
    private Integer maxTenure;

    @Column(name = "base_rate", precision = 8, scale = 4)
    private BigDecimal baseRate;

    @Column(name = "risk_premium", precision = 8, scale = 4)
    private BigDecimal riskPremium;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

}