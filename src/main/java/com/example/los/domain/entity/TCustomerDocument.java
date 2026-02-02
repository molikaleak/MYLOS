package com.example.los.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "t_customer_document", schema = "public")
public class TCustomerDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "document_type_code", length = 50)
    private String documentTypeCode;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

}