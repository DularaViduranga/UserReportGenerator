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
public interface BranchRepo extends JpaRepository<BranchEntity,Long> {
    boolean existsByBrnName(String upperCase);
    
    @Query("SELECT b FROM BranchEntity b WHERE LOWER(b.brnName) = LOWER(:branchName)")
    Optional<BranchEntity> findByBrnNameIgnoreCase(@Param("branchName") String branchName);

    // Change this method in BranchRepo.java
    @Query("SELECT t FROM TargetEntity t WHERE t.branch.id = :branchId AND t.targetYear = :year AND t.targetMonth = :month")
    Optional<TargetEntity> findByBranchIdAndYearAndMonth(@Param("branchId") Long branchId,
                                                         @Param("year") Integer year,
                                                         @Param("month") Integer month);

    @Query("SELECT b FROM BranchEntity b WHERE b.region.id = :regionId")
    List<BranchEntity> findByRegionId(@Param("regionId") Long regionId);


    BranchEntity findByBrnName(String stringCellValue);
}