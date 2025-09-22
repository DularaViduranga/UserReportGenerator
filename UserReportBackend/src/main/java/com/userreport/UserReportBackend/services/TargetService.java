package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.target.*;
import com.userreport.UserReportBackend.entity.TargetEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface TargetService {
    TargetSaveResponseDTO saveTarget(TargetSaveRequestDTO targetSaveRequestDTO);



    TargetSaveResponseDTO updateTarget(Long id, TargetUpdateRequestDTO targetUpdateRequestDTO);

    void deleteTarget(Long id);

    List<TargetEntity> getAllTargets();

    List<TargetResponseDTO> getAllTargetResponses();

    TargetEntity getTargetById(Long id);

    TargetResponseDTO getTargetResponseById(Long id);

    TargetEntity getTargetByBranchId(Long branchId);

    TargetResponseDTO getTargetResponseByBranchId(Long branchId);

    TargetEntity getTargetByBranchIdAndYearMonth(Long branchId, Integer year, Integer month);

    TargetResponseDTO getTargetResponseByBranchIdAndYearMonth(Long branchId, Integer year, Integer month);

    List<TargetEntity> getTargetsByBranchIdAndYear(Long branchId, Integer year);

    List<TargetResponseDTO> getTargetResponsesByBranchIdAndYear(Long branchId, Integer year);

    List<TargetEntity> getTargetsByRegionId(Long regionId);

    List<TargetResponseDTO> getTargetResponsesByRegionId(Long regionId);

    List<TargetEntity> getTargetsByRegionIdAndYearMonth(Long regionId, Integer year, Integer month);

    List<TargetResponseDTO> getTargetResponsesByRegionIdAndYearMonth(Long regionId, Integer year, Integer month);

    List<TargetEntity> getTargetsByMinimumAmount(BigDecimal amount);

    List<TargetResponseDTO> getTargetResponsesByMinimumAmount(BigDecimal amount);

    List<TargetEntity> getTargetsByYear(Integer year);

    List<TargetResponseDTO> getTargetResponsesByYear(Integer year);

    List<TargetEntity> getTargetsByYearAndMonth(Integer year, Integer month);

    List<TargetResponseDTO> getTargetResponsesByYearAndMonth(Integer year, Integer month);

    BigDecimal getTotalTargetByRegion(Long regionId);

    BigDecimal getTotalTargetByRegionAndYearMonth(Long regionId, Integer year, Integer month);

    MonthlyTargetSummaryDTO getMonthlyTargetSummary(Integer year, Integer month);

    MonthlyTargetSummaryDTO getMonthlyTargetSummaryByRegion(Long regionId, Integer year, Integer month);

    YearlyTargetSummaryDTO getYearlyTargetSummary(Integer year);

    YearlyTargetSummaryDTO getYearlyTargetSummaryByRegion(Long regionId, Integer year);

    void saveTargetsFromExcel(MultipartFile file, int year, int month);
}
