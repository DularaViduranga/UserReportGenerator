package com.userreport.UserReportBackend.repository;

import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.TargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepo extends JpaRepository<TargetEntity, Long> {
    // Find target by branch
    Optional<TargetEntity> findByBranch(BranchEntity branch);

    // Find target by branch ID
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.id = :branchId")
    Optional<TargetEntity> findTargetByBranchId(@Param("branchId") Long branchId);

    // Check if target exists for a branch
    boolean existsByBranch(BranchEntity branch);

    // Find targets by region (through branch relationship)
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.region.id = :regionId")
    List<TargetEntity> findByRegionId(@Param("regionId") Long regionId);

    // Find targets above certain amount
    @Query("SELECT t FROM TargetEntity t WHERE t.target >= :amount")
    List<TargetEntity> findByTargetGreaterThanEqual(@Param("amount") java.math.BigDecimal amount);

    // Get total target amount by region
    @Query("SELECT SUM(t.target) FROM TargetEntity t WHERE t.branch.region.id = :regionId")
    java.math.BigDecimal getTotalTargetByRegion(@Param("regionId") Long regionId);
}
