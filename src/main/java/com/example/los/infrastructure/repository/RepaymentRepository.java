package com.example.los.infrastructure.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.TLoanRepayment;

@Repository
public interface RepaymentRepository extends JpaRepository<TLoanRepayment, Long> {
    
    List<TLoanRepayment> findByLoanApplicationId(Long loanApplicationId);
    
    List<TLoanRepayment> findByCustomerId(Long customerId);
    
    List<TLoanRepayment> findByStatus(String status);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.status = 'PENDING' ORDER BY r.dueDate ASC")
    List<TLoanRepayment> findPendingRepaymentsByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.status = 'PAID' ORDER BY r.paymentDate DESC")
    List<TLoanRepayment> findPaidRepaymentsByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.customerId = :customerId AND r.status = 'OVERDUE'")
    List<TLoanRepayment> findOverdueRepaymentsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(r.amountPaid) FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.status = 'PAID'")
    Optional<Double> sumPaidAmountByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT SUM(r.amountDue) FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.status = 'PENDING'")
    Optional<Double> sumPendingAmountByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.dueDate BETWEEN :startDate AND :endDate")
    List<TLoanRepayment> findRepaymentsDueBetween(@Param("startDate") Instant startDate, 
                                                 @Param("endDate") Instant endDate);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.dueDate <= :date AND r.status = 'PENDING'")
    List<TLoanRepayment> findOverdueRepayments(@Param("loanApplicationId") Long loanApplicationId, 
                                              @Param("date") Instant date);
    
    @Query("SELECT COUNT(r) FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.status = 'PAID'")
    long countPaidRepaymentsByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT COUNT(r) FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId")
    long countTotalRepaymentsByLoanApplication(@Param("loanApplicationId") Long loanApplicationId);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.customerId = :customerId AND r.dueDate <= :date AND r.status = 'PENDING'")
    List<TLoanRepayment> findCustomerOverdueRepayments(@Param("customerId") Long customerId, 
                                                      @Param("date") Instant date);
    
    @Query("SELECT r FROM TLoanRepayment r WHERE r.loanApplicationId = :loanApplicationId AND r.installmentNumber = :installmentNumber")
    Optional<TLoanRepayment> findByLoanApplicationAndInstallmentNumber(@Param("loanApplicationId") Long loanApplicationId, 
                                                                     @Param("installmentNumber") Integer installmentNumber);
}