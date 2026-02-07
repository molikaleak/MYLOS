package com.example.los.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.TLoanApproval;

@Repository
public interface LoanApprovalRepository extends JpaRepository<TLoanApproval, Long> {
    
    List<TLoanApproval> findByLoanApplicationId(Long loanApplicationId);
    
    List<TLoanApproval> findByLoanApplicationIdAndStatus(Long loanApplicationId, String status);
    
    List<TLoanApproval> findByApproverRoleAndStatus(String approverRole, String status);
    
    @Query("SELECT la FROM TLoanApproval la WHERE la.loanApplicationId = :loanApplicationId ORDER BY la.createdAt DESC")
    List<TLoanApproval> findLatestByLoanApplicationId(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT la FROM TLoanApproval la WHERE la.approverRole = :approverRole AND la.status = 'PENDING' ORDER BY la.createdAt ASC")
    List<TLoanApproval> findPendingByApproverRole(@Param("approverRole") String approverRole);
    
    @Query("SELECT COUNT(la) FROM TLoanApproval la WHERE la.loanApplicationId = :loanApplicationId AND la.status = 'APPROVED'")
    long countApprovedByLoanApplicationId(@Param("loanApplicationId") Long loanApplicationId);
}