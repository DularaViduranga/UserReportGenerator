package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.target.*;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.TargetRepo;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.ExcelUploadService;
import com.userreport.UserReportBackend.services.TargetService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TargetServiceImpl implements TargetService {

    private final TargetRepo targetRepo;
    private final BranchRepo branchRepo;
    private final UserRepo userRepo;
    private final ExcelUploadService excelUploadService;


    public TargetServiceImpl(TargetRepo targetRepo, BranchRepo branchRepo, UserRepo userRepo, ExcelUploadService excelUploadService) {
        this.targetRepo = targetRepo;
        this.branchRepo = branchRepo;
        this.userRepo = userRepo;
        this.excelUploadService = excelUploadService;
    }

    @Override
    public TargetSaveResponseDTO saveTarget(TargetSaveRequestDTO targetSaveRequestDTO) {
        // Validation
        if (targetSaveRequestDTO.getTarget() == null || targetSaveRequestDTO.getTarget().compareTo(BigDecimal.ZERO) <= 0) {
            return new TargetSaveResponseDTO(null, "Target amount must be greater than zero");
        }
        if (targetSaveRequestDTO.getBranchId() == null) {
            return new TargetSaveResponseDTO(null, "Branch ID cannot be null");
        }
        if (targetSaveRequestDTO.getTargetYear() == null || targetSaveRequestDTO.getTargetYear() < 2000 || targetSaveRequestDTO.getTargetYear() > 2100) {
            return new TargetSaveResponseDTO(null, "Target year must be between 2000 and 2100");
        }
        if (targetSaveRequestDTO.getTargetMonth() == null || targetSaveRequestDTO.getTargetMonth() < 1 || targetSaveRequestDTO.getTargetMonth() > 12) {
            return new TargetSaveResponseDTO(null, "Target month must be between 1 and 12");
        }

        // Check if branch exists
        BranchEntity branch = branchRepo.findById(targetSaveRequestDTO.getBranchId()).orElse(null);
        if (branch == null) {
            return new TargetSaveResponseDTO(null, "Branch not found");
        }

        // Check if target already exists for this branch, year, and month
        if (targetRepo.existsByBranchAndTargetYearAndTargetMonth(branch, targetSaveRequestDTO.getTargetYear(), targetSaveRequestDTO.getTargetMonth())) {
            return new TargetSaveResponseDTO(null, "Target already exists for this branch in " +
                    getMonthName(targetSaveRequestDTO.getTargetMonth()) + " " + targetSaveRequestDTO.getTargetYear());
        }

        // Get current user
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            return new TargetSaveResponseDTO(null, "User not authenticated");
        }

        try {
            TargetEntity targetEntity = new TargetEntity(
                    targetSaveRequestDTO.getTarget(),
                    targetSaveRequestDTO.getTargetYear(),
                    targetSaveRequestDTO.getTargetMonth(),
                    branch,
                    currentUser
            );

            targetRepo.save(targetEntity);
            return new TargetSaveResponseDTO("Target saved successfully", null);
        } catch (Exception e) {
            return new TargetSaveResponseDTO(null, "Error saving target: " + e.getMessage());
        }
    }




    @Override
    public TargetSaveResponseDTO updateTarget(Long id, TargetUpdateRequestDTO targetUpdateRequestDTO) {
        // Validation
        if (targetUpdateRequestDTO.getTarget() == null || targetUpdateRequestDTO.getTarget().compareTo(BigDecimal.ZERO) <= 0) {
            return new TargetSaveResponseDTO(null, "Target amount must be greater than zero");
        }
        if (targetUpdateRequestDTO.getTargetYear() != null &&
                (targetUpdateRequestDTO.getTargetYear() < 2000 || targetUpdateRequestDTO.getTargetYear() > 2100)) {
            return new TargetSaveResponseDTO(null, "Target year must be between 2000 and 2100");
        }
        if (targetUpdateRequestDTO.getTargetMonth() != null &&
                (targetUpdateRequestDTO.getTargetMonth() < 1 || targetUpdateRequestDTO.getTargetMonth() > 12)) {
            return new TargetSaveResponseDTO(null, "Target month must be between 1 and 12");
        }

        TargetEntity existingTarget = targetRepo.findById(id).orElse(null);
        if (existingTarget == null) {
            return new TargetSaveResponseDTO(null, "Target not found");
        }

        // Get current user
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            return new TargetSaveResponseDTO(null, "User not authenticated");
        }

        try {
            existingTarget.setTarget(targetUpdateRequestDTO.getTarget());
            if (targetUpdateRequestDTO.getTargetYear() != null) {
                existingTarget.setTargetYear(targetUpdateRequestDTO.getTargetYear());
            }
            if (targetUpdateRequestDTO.getTargetMonth() != null) {
                existingTarget.setTargetMonth(targetUpdateRequestDTO.getTargetMonth());
            }
            existingTarget.setModifyBy(currentUser);
            existingTarget.setModifyDatetime(LocalDateTime.now());

            targetRepo.save(existingTarget);
            return new TargetSaveResponseDTO("Target updated successfully", null);
        } catch (Exception e) {
            return new TargetSaveResponseDTO(null, "Error updating target: " + e.getMessage());
        }
    }

    @Override
    public void deleteTarget(Long id) {
        if (!targetRepo.existsById(id)) {
            throw new RuntimeException("Target not found");
        }
        try {
            targetRepo.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting target: " + e.getMessage());
        }
    }

    @Override
    public List<TargetEntity> getAllTargets() {
        return targetRepo.findAll();
    }

    @Override
    public TargetEntity getTargetById(Long id) {
        return targetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Target not found with id: " + id));
    }

    @Override
    public TargetEntity getTargetByBranchId(Long branchId) {
        List<TargetEntity> targets = targetRepo.findTargetsByBranchId(branchId);
        if (targets.isEmpty()) {
            throw new RuntimeException("Target not found for branch id: " + branchId);
        }
        // Return the most recent target
        return targets.stream()
                .max((t1, t2) -> t1.getCreatedDatetime().compareTo(t2.getCreatedDatetime()))
                .orElseThrow(() -> new RuntimeException("Target not found for branch id: " + branchId));
    }

    @Override
    public TargetEntity getTargetByBranchIdAndYearMonth(Long branchId, Integer year, Integer month) {
        return targetRepo.findByBranchIdAndYearAndMonth(branchId, year, month)
                .orElseThrow(() -> new RuntimeException("Target not found for branch id: " + branchId +
                        " in " + getMonthName(month) + " " + year));
    }

    @Override
    public List<TargetEntity> getTargetsByBranchIdAndYear(Long branchId, Integer year) {
        return targetRepo.findByBranchIdAndYear(branchId, year);
    }

    @Override
    public List<TargetEntity> getTargetsByRegionId(Long regionId) {
        return targetRepo.findByRegionId(regionId);
    }

    @Override
    public List<TargetEntity> getTargetsByRegionIdAndYearMonth(Long regionId, Integer year, Integer month) {
        return targetRepo.findByRegionIdAndYearAndMonth(regionId, year, month);
    }

    @Override
    public List<TargetEntity> getTargetsByMinimumAmount(BigDecimal minimumAmount) {
        return targetRepo.findByTargetGreaterThanEqual(minimumAmount);
    }

    @Override
    public List<TargetEntity> getTargetsByYear(Integer year) {
        return targetRepo.findByTargetYear(year);
    }

    @Override
    public List<TargetEntity> getTargetsByYearAndMonth(Integer year, Integer month) {
        return targetRepo.findByTargetYearAndTargetMonth(year, month);
    }

    @Override
    public BigDecimal getTotalTargetByRegion(Long regionId) {
        BigDecimal total = targetRepo.getTotalTargetByRegion(regionId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalTargetByRegionAndYearMonth(Long regionId, Integer year, Integer month) {
        BigDecimal total = targetRepo.getTotalTargetByRegionAndYearMonth(regionId, year, month);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public MonthlyTargetSummaryDTO getMonthlyTargetSummary(Integer year, Integer month) {
        List<TargetEntity> targets = targetRepo.findByTargetYearAndTargetMonth(year, month);
        return createMonthlyTargetSummary(year, month, targets);
    }

    @Override
    public MonthlyTargetSummaryDTO getMonthlyTargetSummaryByRegion(Long regionId, Integer year, Integer month) {
        List<TargetEntity> targets = targetRepo.findByRegionIdAndYearAndMonth(regionId, year, month);
        return createMonthlyTargetSummary(year, month, targets);
    }

    @Override
    public YearlyTargetSummaryDTO getYearlyTargetSummary(Integer year) {
        List<TargetEntity> targets = targetRepo.findByTargetYear(year);
        return createYearlyTargetSummary(year, targets);
    }

    @Override
    public YearlyTargetSummaryDTO getYearlyTargetSummaryByRegion(Long regionId, Integer year) {
        List<TargetEntity> targets = targetRepo.findByRegionId(regionId).stream()
                .filter(t -> t.getTargetYear().equals(year))
                .collect(Collectors.toList());
        return createYearlyTargetSummary(year, targets);
    }

    @Override
    public void saveTargetsFromExcel(MultipartFile file, int year, int month) {
        if (!excelUploadService.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid Excel file format");
        }

        try {
            if(targetRepo.findByTargetYearAndTargetMonth(year, month).size() > 0) {
                throw new IllegalArgumentException("Targets for " + getMonthName(month) + " " + year + " already exist. Please delete existing targets before uploading new ones.");
            }else {
                List<TargetEntity> targets = excelUploadService.getTargetsFromExcel(
                        file.getInputStream(), year, month);
                targetRepo.saveAll(targets);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file", e);
        }
    }



    @Override
    public List<TargetResponseDTO> getAllTargetResponses() {
        List<TargetEntity> targets = targetRepo.findAll();
        return targets.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TargetResponseDTO getTargetResponseById(Long id) {
        TargetEntity entity = getTargetById(id);
        return convertToResponseDTO(entity);
    }

    @Override
    public TargetResponseDTO getTargetResponseByBranchId(Long branchId) {
        TargetEntity entity = getTargetByBranchId(branchId);
        return convertToResponseDTO(entity);
    }

    @Override
    public TargetResponseDTO getTargetResponseByBranchIdAndYearMonth(Long branchId, Integer year, Integer month) {
        TargetEntity entity = getTargetByBranchIdAndYearMonth(branchId, year, month);
        return convertToResponseDTO(entity);
    }

    @Override
    public List<TargetResponseDTO> getTargetResponsesByBranchIdAndYear(Long branchId, Integer year) {
        List<TargetEntity> entities = getTargetsByBranchIdAndYear(branchId, year);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetResponseDTO> getTargetResponsesByRegionId(Long regionId) {
        List<TargetEntity> entities = getTargetsByRegionId(regionId);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetResponseDTO> getTargetResponsesByRegionIdAndYearMonth(Long regionId, Integer year, Integer month) {
        List<TargetEntity> entities = getTargetsByRegionIdAndYearMonth(regionId, year, month);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetResponseDTO> getTargetResponsesByMinimumAmount(BigDecimal amount) {
        List<TargetEntity> entities = getTargetsByMinimumAmount(amount);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetResponseDTO> getTargetResponsesByYear(Integer year) {
        List<TargetEntity> entities = getTargetsByYear(year);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetResponseDTO> getTargetResponsesByYearAndMonth(Integer year, Integer month) {
        List<TargetEntity> entities = getTargetsByYearAndMonth(year, month);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private TargetResponseDTO convertToResponseDTO(TargetEntity target) {
        TargetResponseDTO dto = new TargetResponseDTO();
        dto.setId(target.getId());
        dto.setTarget(target.getTarget());
        dto.setTargetYear(target.getTargetYear());
        dto.setTargetMonth(target.getTargetMonth());
        dto.setBranchName(target.getBranch().getBrnName());
        dto.setRegionName(target.getBranch().getRegion().getRgnName());
        dto.setCreatedDatetime(target.getCreatedDatetime());
        dto.setModifyDatetime(target.getModifyDatetime());

        if (target.getCreatedBy() != null) {
            dto.setCreatedByUsername(target.getCreatedBy().getUsername());
        }
        if (target.getModifyBy() != null) {
            dto.setModifyByUsername(target.getModifyBy().getUsername());
        }

        return dto;
    }

    private MonthlyTargetSummaryDTO createMonthlyTargetSummary(Integer year, Integer month, List<TargetEntity> targets) {
        MonthlyTargetSummaryDTO summary = new MonthlyTargetSummaryDTO();
        summary.setYear(year);
        summary.setMonth(month);
        summary.setBranchCount(targets.size());

        BigDecimal totalTarget = targets.stream()
                .map(TargetEntity::getTarget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalTarget(totalTarget);

        List<TargetResponseDTO> targetResponses = targets.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        summary.setTargets(targetResponses);

        return summary;
    }

    private YearlyTargetSummaryDTO createYearlyTargetSummary(Integer year, List<TargetEntity> targets) {
        YearlyTargetSummaryDTO summary = new YearlyTargetSummaryDTO();
        summary.setYear(year);

        BigDecimal totalTarget = targets.stream()
                .map(TargetEntity::getTarget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalTarget(totalTarget);

        // Group by month
        Map<Integer, List<TargetEntity>> monthlyTargets = targets.stream()
                .collect(Collectors.groupingBy(TargetEntity::getTargetMonth));

        List<MonthlyTargetSummaryDTO> monthlyData = monthlyTargets.entrySet().stream()
                .map(entry -> createMonthlyTargetSummary(year, entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth()))
                .collect(Collectors.toList());

        summary.setMonthlyData(monthlyData);
        summary.setTotalBranches(targets.stream()
                .collect(Collectors.groupingBy(t -> t.getBranch().getId()))
                .size());

        return summary;
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepo.findByUsername(username).orElse(null);
    }

    private String getMonthName(Integer month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month];
    }
}