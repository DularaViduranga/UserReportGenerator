package com.userreport.UserReportBackend.repository;

import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Repository
public interface CollectionRepo extends JpaRepository<CollectionEntity, Long> {

    // Find all collections by branch (returns List instead of Optional)
    List<CollectionEntity> findByBranch(BranchEntity branch);

    // Find collections by branch ID
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.id = :branchId")
    List<CollectionEntity> findCollectionsByBranchId(@Param("branchId") Long branchId);

    // Find collection by branch ID and specific month/year
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.id = :branchId AND c.collectionYear = :year AND c.collectionMonth = :month")
    Optional<CollectionEntity> findByBranchIdAndYearAndMonth(@Param("branchId") Long branchId,
                                                             @Param("year") Integer year,
                                                             @Param("month") Integer month);

    // Find all collections for a branch in a specific year
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.id = :branchId AND c.collectionYear = :year")
    List<CollectionEntity> findByBranchIdAndYear(@Param("branchId") Long branchId, @Param("year") Integer year);

    // Check if collection exists for a branch
    boolean existsByBranch(BranchEntity branch);

    // Check if collection exists for specific branch, year, and month
    boolean existsByBranchAndCollectionYearAndCollectionMonth(BranchEntity branch, Integer collectionYear, Integer collectionMonth);

    // Find collections by region (through branch relationship)
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.region.id = :regionId")
    List<CollectionEntity> findByRegionId(@Param("regionId") Long regionId);

    // Find collections by region for specific year and month
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.region.id = :regionId AND c.collectionYear = :year AND c.collectionMonth = :month")
    List<CollectionEntity> findByRegionIdAndYearAndMonth(@Param("regionId") Long regionId,
                                                         @Param("year") Integer year,
                                                         @Param("month") Integer month);

    // Find collections with percentage above threshold
    @Query("SELECT c FROM CollectionEntity c WHERE c.percentage >= :threshold")
    List<CollectionEntity> findByPercentageGreaterThanEqual(@Param("threshold") BigDecimal threshold);

    // Find the latest collection for a branch (most recent by datetime)
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch = :branch ORDER BY c.createdDatetime DESC")
    List<CollectionEntity> findLatestByBranch(@Param("branch") BranchEntity branch);

    // Find collections by year
    List<CollectionEntity> findByCollectionYear(Integer year);

    // Find collections by year and month
    List<CollectionEntity> findByCollectionYearAndCollectionMonth(Integer year, Integer month);

    // Get total collection amount by region for specific month/year
    @Query("SELECT SUM(c.collectionAmount) FROM CollectionEntity c WHERE c.branch.region.id = :regionId AND c.collectionYear = :year AND c.collectionMonth = :month")
    BigDecimal getTotalCollectionByRegionAndYearMonth(@Param("regionId") Long regionId,
                                                      @Param("year") Integer year,
                                                      @Param("month") Integer month);

    // Get total collection amount by region
    @Query("SELECT SUM(c.collectionAmount) FROM CollectionEntity c WHERE c.branch.region.id = :regionId")
    BigDecimal getTotalCollectionByRegion(@Param("regionId") Long regionId);

    // Sum collection amount by branch and year (needed for Dashboard)
    @Query("SELECT SUM(c.collectionAmount) FROM CollectionEntity c WHERE c.branch.id = :branchId AND c.collectionYear = :year")
    BigDecimal sumCollectionByBranchAndYear(@Param("branchId") Long branchId, @Param("year") int year);
}