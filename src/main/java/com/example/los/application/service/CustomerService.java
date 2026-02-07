package com.example.los.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.los.application.dto.CustomerRequest;
import com.example.los.application.dto.CustomerResponse;
import com.example.los.domain.entity.TCustomer;
import com.example.los.infrastructure.repository.CustomerRepository;
import com.example.los.infrastructure.repository.LoanApplicationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating new customer: {}", request.getNameEn());
        
        // Validate phone number uniqueness
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists: " + request.getPhone());
        }
        
        // Create customer entity
        TCustomer customer = new TCustomer();
        customer.setNameEn(request.getNameEn());
        customer.setNameKh(request.getNameKh());
        customer.setPhone(request.getPhone());
        customer.setAddressId(request.getAddressId());
        customer.setCreatedAt(Instant.now());
        
        // Save customer
        TCustomer savedCustomer = customerRepository.save(customer);
        log.info("Customer created with ID: {}", savedCustomer.getId());
        
        return mapToResponse(savedCustomer);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer with ID: {}", id);
        
        TCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        return mapToResponse(customer);
    }
    
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        
        List<TCustomer> customers = customerRepository.findAll();
        
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomersByName(String name) {
        log.debug("Searching customers by name: {}", name);
        
        List<TCustomer> customers = customerRepository.searchByName(name);
        
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<CustomerResponse> getCustomerByPhone(String phone) {
        log.debug("Fetching customer by phone: {}", phone);
        
        return customerRepository.findByPhone(phone)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        
        TCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        // Validate phone number uniqueness if changing
        if (request.getPhone() != null && !request.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("Phone number already exists: " + request.getPhone());
            }
            customer.setPhone(request.getPhone());
        }
        
        // Update fields
        if (request.getNameEn() != null) {
            customer.setNameEn(request.getNameEn());
        }
        
        if (request.getNameKh() != null) {
            customer.setNameKh(request.getNameKh());
        }
        
        if (request.getAddressId() != null) {
            customer.setAddressId(request.getAddressId());
        }
        
        TCustomer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated with ID: {}", id);
        
        return mapToResponse(updatedCustomer);
    }
    
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        
        TCustomer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
        
        // Check if customer has any loan applications
        List<com.example.los.domain.entity.TLoanApplication> applications = 
            loanApplicationRepository.findByCustomerId(id);
        
        if (!applications.isEmpty()) {
            throw new IllegalStateException(
                String.format("Cannot delete customer with ID %d. Customer has %d loan applications.", 
                    id, applications.size()));
        }
        
        customerRepository.delete(customer);
        log.info("Customer deleted with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public long countCustomers() {
        log.debug("Counting total customers");
        
        return customerRepository.count();
    }
    
    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomersCreatedBetween(Instant startDate, Instant endDate) {
        log.debug("Fetching customers created between {} and {}", startDate, endDate);
        
        List<TCustomer> customers = customerRepository.findByCreatedAtBetween(startDate, endDate);
        
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateCustomerCreditScore(Long customerId) {
        log.debug("Calculating credit score for customer: {}", customerId);
        
        // This is a simplified credit score calculation
        // In a real system, this would consider:
        // 1. Payment history
        // 2. Current debt
        // 3. Length of credit history
        // 4. Types of credit used
        // 5. New credit applications
        
        // For now, return a basic score
        return BigDecimal.valueOf(650.0); // Base score
    }
    
    private CustomerResponse mapToResponse(TCustomer customer) {
        // Get loan application statistics
        List<com.example.los.domain.entity.TLoanApplication> applications = 
            loanApplicationRepository.findByCustomerId(customer.getId());
        
        int totalLoanApplications = applications.size();
        int activeLoans = (int) applications.stream()
                .filter(app -> "ACTIVE".equals(app.getStatusCode()))
                .count();
        
        BigDecimal totalLoanAmount = applications.stream()
                .map(app -> app.getLoanAmount() != null ? app.getLoanAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CustomerResponse.builder()
                .id(customer.getId())
                .nameEn(customer.getNameEn())
                .nameKh(customer.getNameKh())
                .phone(customer.getPhone())
                .addressId(customer.getAddressId())
                .createdAt(customer.getCreatedAt())
                .totalLoanApplications(totalLoanApplications)
                .activeLoans(activeLoans)
                .totalLoanAmount(totalLoanAmount)
                .build();
    }
}