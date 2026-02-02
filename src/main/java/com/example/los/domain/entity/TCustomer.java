package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "t_customer", schema = "public")
public class TCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_kh")
    private String nameKh;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "created_at")
    private Instant createdAt;

}