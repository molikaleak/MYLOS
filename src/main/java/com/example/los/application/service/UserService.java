package com.example.los.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.los.application.dto.RegisterRequest;
import com.example.los.domain.auth.TUser;
import com.example.los.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<TUser> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(username);
        }
        
        TUser user = userOptional.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with username/email: " + username));
        
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new ArrayList<>()) // Add roles/authorities as needed
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!"ACTIVE".equals(user.getStatusCode()))
                .build();
    }
    
    @Transactional
    public TUser createUser(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + registerRequest.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }
        
        TUser user = new TUser();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setBranchId(registerRequest.getBranchId());
        user.setRoleCode(registerRequest.getRoleCode());
        user.setStatusCode("ACTIVE");
        user.setCreatedAt(Instant.now());
        
        return userRepository.save(user);
    }
    
    @Transactional
    public TUser updateUser(Long userId, RegisterRequest updateRequest) {
        TUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Check if new username is taken by another user
        if (updateRequest.getUsername() != null && 
            !updateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + updateRequest.getUsername());
            }
            user.setUsername(updateRequest.getUsername());
        }
        
        // Check if new email is taken by another user
        if (updateRequest.getEmail() != null && 
            !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + updateRequest.getEmail());
            }
            user.setEmail(updateRequest.getEmail());
        }
        
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        
        if (updateRequest.getBranchId() != null) {
            user.setBranchId(updateRequest.getBranchId());
        }
        
        if (updateRequest.getRoleCode() != null) {
            user.setRoleCode(updateRequest.getRoleCode());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void deactivateUser(Long userId) {
        TUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        user.setStatusCode("INACTIVE");
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }
    
    @Transactional
    public void activateUser(Long userId) {
        TUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        user.setStatusCode("ACTIVE");
        userRepository.save(user);
        log.info("User activated: {}", user.getUsername());
    }
    
    public Optional<TUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<TUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<TUser> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}