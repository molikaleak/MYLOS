package com.example.los.domain.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "t_user", schema = "public")
public class TUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "status_code", length = 50)
    private String statusCode;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "refresh_token_expiry")
    private Instant refreshTokenExpiry;

}