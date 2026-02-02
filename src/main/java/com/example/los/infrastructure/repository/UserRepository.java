package com.example.los.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.los.domain.auth.TUser;

@Repository
public interface UserRepository extends JpaRepository<TUser, Long> {
    
    Optional<TUser> findByUsername(String username);
    
    Optional<TUser> findByEmail(String email);
    
    Optional<TUser> findByRefreshToken(String refreshToken);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}