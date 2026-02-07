package com.example.los.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.TCustomerDocument;

@Repository
public interface DocumentRepository extends JpaRepository<TCustomerDocument, Long> {
    
    List<TCustomerDocument> findByCustomerId(Long customerId);
    
    List<TCustomerDocument> findByCustomerIdAndDocumentType(Long customerId, String documentType);
    
    List<TCustomerDocument> findByDocumentType(String documentType);
    
    List<TCustomerDocument> findByStatus(String status);
    
    @Query("SELECT d FROM TCustomerDocument d WHERE d.customerId = :customerId AND d.status = :status")
    List<TCustomerDocument> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                                     @Param("status") String status);
    
    @Query("SELECT d FROM TCustomerDocument d WHERE d.customerId = :customerId AND d.documentType = :documentType AND d.status = 'VERIFIED'")
    Optional<TCustomerDocument> findVerifiedDocumentByCustomerAndType(@Param("customerId") Long customerId, 
                                                                     @Param("documentType") String documentType);
    
    @Query("SELECT COUNT(d) FROM TCustomerDocument d WHERE d.customerId = :customerId AND d.status = 'VERIFIED'")
    long countVerifiedDocumentsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT d FROM TCustomerDocument d WHERE d.customerId = :customerId AND d.uploadedAt >= :startDate")
    List<TCustomerDocument> findByCustomerIdAndUploadedAfter(@Param("customerId") Long customerId, 
                                                            @Param("startDate") java.time.Instant startDate);
    
    @Query("SELECT d FROM TCustomerDocument d WHERE d.status = 'PENDING' ORDER BY d.uploadedAt ASC")
    List<TCustomerDocument> findPendingDocuments();
    
    @Query("SELECT d FROM TCustomerDocument d WHERE d.customerId = :customerId ORDER BY d.uploadedAt DESC")
    List<TCustomerDocument> findLatestDocumentsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT DISTINCT d.documentType FROM TCustomerDocument d WHERE d.customerId = :customerId")
    List<String> findDocumentTypesByCustomer(@Param("customerId") Long customerId);
}