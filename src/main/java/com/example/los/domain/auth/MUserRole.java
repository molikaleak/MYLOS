package com.example.los.domain.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "m_user_role", schema = "public")
public class MUserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

}