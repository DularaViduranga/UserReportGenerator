package com.userreport.UserReportBackend.repository;

import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CollectionRepo extends JpaRepository<CollectionEntity, Long> {

    // Find collection by branch
    Optional<CollectionEntity> findByBranch(BranchEntity branch);

    // Find collection by branch ID
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.id = :branchId")
    Optional<CollectionEntity> findCollectionByBranchId(@Param("branchId") Long branchId);

    // Check if collection exists for a branch
    boolean existsByBranch(BranchEntity branch);

    // Find collections by region (through branch relationship)
    @Query("SELECT c FROM CollectionEntity c WHERE c.branch.region.id = :regionId")
    List<CollectionEntity> findByRegionId(@Param("regionId") Long regionId);

    // Find collections with percentage above threshold
    @Query("SELECT c FROM CollectionEntity c WHERE c.percentage >= :threshold")
    List<CollectionEntity> findByPercentageGreaterThanEqual(@Param("threshold") java.math.BigDecimal threshold);
}