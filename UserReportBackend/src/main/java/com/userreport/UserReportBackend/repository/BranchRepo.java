package com.userreport.UserReportBackend.repository;

import com.userreport.UserReportBackend.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepo extends JpaRepository<BranchEntity,Long> {
}
