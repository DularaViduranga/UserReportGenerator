package com.userreport.UserReportBackend.repository;

import com.userreport.UserReportBackend.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepo extends JpaRepository<RegionEntity,Long> {
    boolean existsByRgnName(String rgnName);

    RegionEntity findByRgnName(String regionName);
}
