package com.example.los.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.los.application.dto.ProductResponse;
import com.example.los.application.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        try {
            List<ProductResponse> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        try {
            List<ProductResponse> products = productService.getActiveProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching active products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            log.warn("Product not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/code/{productCode}")
    public ResponseEntity<ProductResponse> getProductByCode(@PathVariable String productCode) {
        try {
            ProductResponse product = productService.getProductByCode(productCode);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            log.warn("Product not found with code: {}", productCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/type/{productType}")
    public ResponseEntity<List<ProductResponse>> getProductsByType(@PathVariable String productType) {
        try {
            List<ProductResponse> products = productService.getProductsByType(productType);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching products by type {}: {}", productType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String search) {
        try {
            List<ProductResponse> products = productService.searchProducts(search);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/for-amount")
    public ResponseEntity<List<ProductResponse>> getProductsForAmount(@RequestParam Double amount) {
        try {
            List<ProductResponse> products = productService.getProductsForAmount(amount);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching products for amount {}: {}", amount, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveProducts() {
        try {
            long count = productService.countActiveProducts();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting active products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Product service is running");
    }
}