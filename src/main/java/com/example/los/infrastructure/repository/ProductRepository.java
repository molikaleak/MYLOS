package com.example.los.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.MProduct;

@Repository
public interface ProductRepository extends JpaRepository<MProduct, Long> {
    
    Optional<MProduct> findByProductCode(String productCode);
    
    List<MProduct> findByProductType(String productType);
    
    List<MProduct> findByStatus(String status);
    
    List<MProduct> findByMinAmountLessThanEqualAndMaxAmountGreaterThanEqual(Double amount, Double amount2);
    
    @Query("SELECT p FROM MProduct p WHERE p.status = 'ACTIVE' AND p.minAmount <= :amount AND p.maxAmount >= :amount")
    List<MProduct> findActiveProductsForAmount(@Param("amount") Double amount);
    
    @Query("SELECT p FROM MProduct p WHERE p.status = 'ACTIVE' AND p.productType = :productType")
    List<MProduct> findActiveProductsByType(@Param("productType") String productType);
    
    @Query("SELECT COUNT(p) FROM MProduct p WHERE p.status = 'ACTIVE'")
    long countActiveProducts();
    
    @Query("SELECT p FROM MProduct p WHERE p.status = 'ACTIVE' AND (LOWER(p.productNameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.productNameKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<MProduct> searchActiveProducts(@Param("search") String search);
    
    boolean existsByProductCode(String productCode);
    
    @Query("SELECT p FROM MProduct p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<MProduct> findLatestActiveProducts();
    
    @Query("SELECT p FROM MProduct p WHERE p.status = 'ACTIVE' AND p.interestRate <= :maxInterestRate")
    List<MProduct> findActiveProductsWithMaxInterestRate(@Param("maxInterestRate") Double maxInterestRate);
}