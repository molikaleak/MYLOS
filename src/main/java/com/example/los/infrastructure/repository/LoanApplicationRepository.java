package com.example.los.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.TLoanApplication;

@Repository
public interface LoanApplicationRepository extends JpaRepository<TLoanApplication, Long> {
    
    List<TLoanApplication> findByCustomerId(Long customerId);
    
    List<TLoanApplication> findByProductId(Long productId);
    
    List<TLoanApplication> findByBranchId(Long branchId);
    
    List<TLoanApplication> findByStatusCode(String statusCode);
    
    @Query("SELECT la FROM TLoanApplication la WHERE la.customerId = :customerId AND la.statusCode = :statusCode")
    List<TLoanApplication> findByCustomerIdAndStatusCode(@Param("customerId") Long customerId, 
                                                        @Param("statusCode") String statusCode);
    
    @Query("SELECT la FROM TLoanApplication la WHERE la.productId = :productId AND la.statusCode = :statusCode")
    List<TLoanApplication> findByProductIdAndStatusCode(@Param("productId") Long productId, 
                                                       @Param("statusCode") String statusCode);
    
    @Query("SELECT COUNT(la) FROM TLoanApplication la WHERE la.branchId = :branchId AND la.statusCode = :statusCode")
    long countByBranchIdAndStatusCode(@Param("branchId") Long branchId, 
                                     @Param("statusCode") String statusCode);
    
    @Query("SELECT la FROM TLoanApplication la WHERE la.loanAmount BETWEEN :minAmount AND :maxAmount")
    List<TLoanApplication> findByAppliedAmountBetween(@Param("minAmount") Double minAmount,
                                                     @Param("maxAmount") Double maxAmount);
    
    @Query("SELECT la FROM TLoanApplication la WHERE la.createdAt >= :startDate AND la.createdAt <= :endDate")
    List<TLoanApplication> findByCreatedAtBetween(@Param("startDate") java.time.Instant startDate, 
                                                 @Param("endDate") java.time.Instant endDate);
    
    @Query("SELECT la FROM TLoanApplication la WHERE la.customerId = :customerId ORDER BY la.createdAt DESC")
    List<TLoanApplication> findLatestByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(la.loanAmount) FROM TLoanApplication la WHERE la.branchId = :branchId AND la.statusCode = 'APPROVED'")
    Optional<Double> sumApprovedAmountByBranch(@Param("branchId") Long branchId);
    
    @Query("SELECT COUNT(la) FROM TLoanApplication la WHERE la.statusCode = :statusCode AND la.createdAt >= :startDate")
    long countByStatusCodeSinceDate(@Param("statusCode") String statusCode, 
                                   @Param("startDate") java.time.Instant startDate);
}