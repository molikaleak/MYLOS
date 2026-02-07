package com.example.los.domain.entity;

import java.math.BigDecimal;

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
@Table(name = "m_product", schema = "public")
public class MProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code", length = 50)
    private String productCode;

    @Column(name = "name")
    private String productNameEn;

    @Column(name = "name_kh")
    private String productNameKh;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "interest_rate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "tenure_month")
    private Integer tenureMonth;

    @Column(name = "status_code", length = 50)
    private String status;

    @Column(name = "created_at")
    private java.time.Instant createdAt;

}