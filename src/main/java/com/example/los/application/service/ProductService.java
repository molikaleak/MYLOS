package com.example.los.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.los.application.dto.ProductResponse;
import com.example.los.domain.entity.MProduct;
import com.example.los.infrastructure.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all products");
        
        List<MProduct> products = productRepository.findAll();
        
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        log.debug("Fetching active products");
        
        List<MProduct> products = productRepository.findByStatus("ACTIVE");
        
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        
        MProduct product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductByCode(String productCode) {
        log.debug("Fetching product with code: {}", productCode);
        
        MProduct product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with code: " + productCode));
        
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByType(String productType) {
        log.debug("Fetching products by type: {}", productType);
        
        List<MProduct> products = productRepository.findByProductType(productType);
        
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String search) {
        log.debug("Searching products with term: {}", search);
        
        List<MProduct> products = productRepository.searchActiveProducts(search);
        
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsForAmount(Double amount) {
        log.debug("Fetching products suitable for amount: {}", amount);
        
        List<MProduct> products = productRepository.findActiveProductsForAmount(amount);
        
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long countActiveProducts() {
        log.debug("Counting active products");
        
        return productRepository.countActiveProducts();
    }
    
    private ProductResponse mapToResponse(MProduct product) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .minAmount(product.getMinAmount())
                .maxAmount(product.getMaxAmount())
                .tenureMonth(product.getTenureMonth())
                .statusCode(product.getStatusCode())
                .build();
    }
}