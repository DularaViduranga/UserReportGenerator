package com.userreport.UserReportBackend.repository;

import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.TargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepo extends JpaRepository<TargetEntity, Long> {
    // Find all targets by branch (returns List instead of Optional)
    List<TargetEntity> findByBranch(BranchEntity branch);

    // Find target by branch ID
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.id = :branchId")
    List<TargetEntity> findTargetsByBranchId(@Param("branchId") Long branchId);

    // Find target by branch ID and specific month/year
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.id = :branchId AND t.targetYear = :year AND t.targetMonth = :month")
    Optional<TargetEntity> findByBranchIdAndYearAndMonth(@Param("branchId") Long branchId,
                                                         @Param("year") Integer year,
                                                         @Param("month") Integer month);

    // Find all targets for a branch in a specific year
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.id = :branchId AND t.targetYear = :year")
    List<TargetEntity> findByBranchIdAndYear(@Param("branchId") Long branchId, @Param("year") Integer year);

    // Check if target exists for a branch
    boolean existsByBranch(BranchEntity branch);

    // Check if target exists for specific branch, year, and month
    boolean existsByBranchAndTargetYearAndTargetMonth(BranchEntity branch, Integer targetYear, Integer targetMonth);

    // Find targets by region (through branch relationship)
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.region.id = :regionId")
    List<TargetEntity> findByRegionId(@Param("regionId") Long regionId);

    // Find targets by region for specific year and month
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.region.id = :regionId AND t.targetYear = :year AND t.targetMonth = :month")
    List<TargetEntity> findByRegionIdAndYearAndMonth(@Param("regionId") Long regionId,
                                                     @Param("year") Integer year,
                                                     @Param("month") Integer month);

    // Find targets above certain amount
    @Query("SELECT t FROM TargetEntity t WHERE t.target >= :amount")
    List<TargetEntity> findByTargetGreaterThanEqual(@Param("amount") BigDecimal amount);

    // Find the latest target for a branch (most recent by datetime)
    @Query("SELECT t FROM TargetEntity t WHERE t.branch = :branch ORDER BY t.createdDatetime DESC")
    List<TargetEntity> findLatestByBranch(@Param("branch") BranchEntity branch);

    // Get total target amount by region for specific month/year
    @Query("SELECT SUM(t.target) FROM TargetEntity t WHERE t.branch.region.id = :regionId AND t.targetYear = :year AND t.targetMonth = :month")
    BigDecimal getTotalTargetByRegionAndYearMonth(@Param("regionId") Long regionId,
                                                  @Param("year") Integer year,
                                                  @Param("month") Integer month);

    // Get total target amount by region
    @Query("SELECT SUM(t.target) FROM TargetEntity t WHERE t.branch.region.id = :regionId")
    BigDecimal getTotalTargetByRegion(@Param("regionId") Long regionId);

    // Find targets by year
    List<TargetEntity> findByTargetYear(Integer year);

    // Find targets by year and month
    List<TargetEntity> findByTargetYearAndTargetMonth(Integer year, Integer month);

    @Query("SELECT SUM(t.target) FROM TargetEntity t WHERE t.branch.id = :branchId AND t.targetYear = :year")
    BigDecimal sumTargetByBranchAndYear(@Param("branchId") Long branchId, @Param("year") int year);
}
