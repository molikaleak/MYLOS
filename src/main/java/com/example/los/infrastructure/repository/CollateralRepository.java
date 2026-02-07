package com.example.los.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.TLoanCollateral;

@Repository
public interface CollateralRepository extends JpaRepository<TLoanCollateral, Long> {
    
    List<TLoanCollateral> findByLoanApplicationId(Long loanApplicationId);
    
    List<TLoanCollateral> findByCustomerId(Long customerId);
    
    List<TLoanCollateral> findByCollateralType(String collateralType);
    
    List<TLoanCollateral> findByStatus(String status);
    
    @Query("SELECT c FROM TLoanCollateral c WHERE c.loanApplicationId = :loanApplicationId AND c.status = 'ACTIVE'")
    List<TLoanCollateral> findActiveCollateralsByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT SUM(c.estimatedValue) FROM TLoanCollateral c WHERE c.loanApplicationId = :loanApplicationId AND c.status = 'ACTIVE'")
    Optional<Double> sumActiveCollateralValueByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT c FROM TLoanCollateral c WHERE c.customerId = :customerId AND c.status = 'ACTIVE'")
    List<TLoanCollateral> findActiveCollateralsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(c) FROM TLoanCollateral c WHERE c.loanApplicationId = :loanApplicationId")
    long countByLoanApplicationId(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT c FROM TLoanCollateral c WHERE c.estimatedValue >= :minValue")
    List<TLoanCollateral> findByEstimatedValueGreaterThanEqual(@Param("minValue") Double minValue);
    
    @Query("SELECT c FROM TLoanCollateral c WHERE c.loanApplicationId = :loanApplicationId AND c.collateralType = :collateralType")
    Optional<TLoanCollateral> findByLoanApplicationAndType(@Param("loanApplicationId") Long loanApplicationId, 
                                                         @Param("collateralType") String collateralType);
}