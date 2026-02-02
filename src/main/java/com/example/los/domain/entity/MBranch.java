package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "m_branch", schema = "public")
public class MBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address")
    private String address;

}