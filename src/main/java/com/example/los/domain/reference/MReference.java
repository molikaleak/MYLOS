package com.example.los.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "m_reference", schema = "public")
public class MReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

}