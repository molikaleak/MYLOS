package com.example.los.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.los.domain.entity.MBranch;

@Repository
public interface BranchRepository extends JpaRepository<MBranch, Long> {
    
    Optional<MBranch> findByBranchCode(String branchCode);
    
    List<MBranch> findByStatus(String status);
    
    List<MBranch> findByRegion(String region);
    
    @Query("SELECT b FROM MBranch b WHERE b.status = 'ACTIVE'")
    List<MBranch> findActiveBranches();
    
    @Query("SELECT b FROM MBranch b WHERE b.status = 'ACTIVE' AND b.region = :region")
    List<MBranch> findActiveBranchesByRegion(@Param("region") String region);
    
    @Query("SELECT COUNT(b) FROM MBranch b WHERE b.status = 'ACTIVE'")
    long countActiveBranches();
    
    boolean existsByBranchCode(String branchCode);
    
    @Query("SELECT b FROM MBranch b WHERE LOWER(b.branchNameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.branchNameKh) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<MBranch> searchByName(@Param("search") String search);
    
    @Query("SELECT b FROM MBranch b WHERE b.status = 'ACTIVE' AND (b.branchNameEn LIKE %:search% OR b.branchNameKh LIKE %:search% OR b.branchCode LIKE %:search%)")
    List<MBranch> searchActiveBranches(@Param("search") String search);
}