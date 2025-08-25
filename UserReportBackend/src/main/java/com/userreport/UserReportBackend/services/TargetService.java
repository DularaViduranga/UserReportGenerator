package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.target.TargetResponseDTO;
import com.userreport.UserReportBackend.dto.target.TargetSaveRequestDTO;
import com.userreport.UserReportBackend.dto.target.TargetSaveResponseDTO;
import com.userreport.UserReportBackend.dto.target.TargetUpdateRequestDTO;
import com.userreport.UserReportBackend.entity.TargetEntity;

import java.math.BigDecimal;
import java.util.List;

public interface TargetService {
    TargetSaveResponseDTO saveTarget(TargetSaveRequestDTO targetSaveRequestDTO);

    TargetSaveResponseDTO updateTarget(Long id, TargetUpdateRequestDTO targetUpdateRequestDTO);

    void deleteTarget(Long id);

    List<TargetEntity> getAllTargets();

    List<TargetResponseDTO> getAllTargetResponses();

    TargetEntity getTargetById(Long id);

    TargetEntity getTargetByBranchId(Long branchId);

    List<TargetEntity> getTargetsByRegionId(Long regionId);

    List<TargetEntity> getTargetsByMinimumAmount(BigDecimal amount);

    BigDecimal getTotalTargetByRegion(Long regionId);
}
