package com.example.los.domain.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "m_address", schema = "public")
public class MAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private Integer level;

}