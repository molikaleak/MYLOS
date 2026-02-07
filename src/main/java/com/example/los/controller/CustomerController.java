package com.example.los.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.los.application.dto.CustomerRequest;
import com.example.los.application.dto.CustomerResponse;
import com.example.los.application.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody CustomerRequest request) {
        try {
            CustomerResponse response = customerService.createCustomer(request);
            log.info("Customer created successfully: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomerResponse.builder().build()
            );
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CustomerResponse.builder().build()
            );
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        try {
            CustomerResponse response = customerService.getCustomerById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Customer not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                CustomerResponse.builder().build()
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        try {
            List<CustomerResponse> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error fetching customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomersByName(@RequestParam String name) {
        try {
            List<CustomerResponse> customers = customerService.searchCustomersByName(name);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error searching customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(@PathVariable String phone) {
        try {
            return customerService.getCustomerByPhone(phone)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        CustomerResponse.builder().build()
                    ));
        } catch (Exception e) {
            log.error("Error fetching customer by phone: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CustomerResponse.builder().build()
            );
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id, 
            @RequestBody CustomerRequest request) {
        try {
            CustomerResponse response = customerService.updateCustomer(id, request);
            log.info("Customer updated successfully: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update customer {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomerResponse.builder().build()
            );
        } catch (Exception e) {
            log.error("Error updating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CustomerResponse.builder().build()
            );
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            log.info("Customer deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Customer not found for deletion: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Cannot delete customer {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countCustomers() {
        try {
            long count = customerService.countCustomers();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/created-between")
    public ResponseEntity<List<CustomerResponse>> getCustomersCreatedBetween(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        try {
            List<CustomerResponse> customers = customerService.getCustomersCreatedBetween(startDate, endDate);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error fetching customers by date range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}/credit-score")
    public ResponseEntity<Double> getCustomerCreditScore(@PathVariable Long id) {
        try {
            // This would return a BigDecimal, converting to Double for simplicity
            var creditScore = customerService.calculateCustomerCreditScore(id);
            return ResponseEntity.ok(creditScore.doubleValue());
        } catch (Exception e) {
            log.error("Error calculating credit score for customer {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer service is running");
    }
}