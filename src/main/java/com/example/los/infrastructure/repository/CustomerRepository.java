package com.example.los.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.TCustomer;

@Repository
public interface CustomerRepository extends JpaRepository<TCustomer, Long> {
    
    Optional<TCustomer> findByPhone(String phone);
    
    List<TCustomer> findByNameEnContainingIgnoreCase(String name);
    
    List<TCustomer> findByNameKhContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM TCustomer c WHERE LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.nameKh) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<TCustomer> searchByName(@Param("name") String name);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT COUNT(c) FROM TCustomer c WHERE c.addressId = :addressId")
    long countByAddressId(@Param("addressId") Long addressId);
    
    @Query("SELECT c FROM TCustomer c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<TCustomer> findByCreatedAtBetween(@Param("startDate") java.time.Instant startDate, 
                                          @Param("endDate") java.time.Instant endDate);
}