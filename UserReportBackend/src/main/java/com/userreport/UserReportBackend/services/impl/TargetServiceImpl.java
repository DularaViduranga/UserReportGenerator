package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.target.TargetSaveRequestDTO;
import com.userreport.UserReportBackend.dto.target.TargetSaveResponseDTO;
import com.userreport.UserReportBackend.dto.target.TargetUpdateRequestDTO;
import com.userreport.UserReportBackend.dto.target.TargetResponseDTO;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.TargetRepo;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.TargetService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TargetServiceImpl implements TargetService {

    private final TargetRepo targetRepo;
    private final BranchRepo branchRepo;
    private final UserRepo userRepo;

    public TargetServiceImpl(TargetRepo targetRepo, BranchRepo branchRepo, UserRepo userRepo) {
        this.targetRepo = targetRepo;
        this.branchRepo = branchRepo;
        this.userRepo = userRepo;
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

        // Check if branch exists
        BranchEntity branch = branchRepo.findById(targetSaveRequestDTO.getBranchId()).orElse(null);
        if (branch == null) {
            return new TargetSaveResponseDTO(null, "Branch not found");
        }

        // Check if target already exists for this branch
        if (targetRepo.existsByBranch(branch)) {
            return new TargetSaveResponseDTO(null, "Target already exists for this branch");
        }

        // Get current user
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            return new TargetSaveResponseDTO(null, "User not authenticated");
        }

        try {
            TargetEntity targetEntity = new TargetEntity();
            targetEntity.setTarget(targetSaveRequestDTO.getTarget());
            targetEntity.setBranch(branch);
            targetEntity.setCreatedBy(currentUser);
            targetEntity.setCreatedDatetime(LocalDateTime.now());

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
        return targetRepo.findTargetByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Target not found for branch id: " + branchId));
    }

    @Override
    public List<TargetEntity> getTargetsByRegionId(Long regionId) {
        return targetRepo.findByRegionId(regionId);
    }

    @Override
    public List<TargetEntity> getTargetsByMinimumAmount(BigDecimal minimumAmount) {
        return targetRepo.findByTargetGreaterThanEqual(minimumAmount);
    }

    @Override
    public BigDecimal getTotalTargetByRegion(Long regionId) {
        BigDecimal total = targetRepo.getTotalTargetByRegion(regionId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<TargetResponseDTO> getAllTargetResponses() {
        List<TargetEntity> targets = targetRepo.findAll();
        return targets.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private TargetResponseDTO convertToResponseDTO(TargetEntity target) {
        TargetResponseDTO dto = new TargetResponseDTO();
        dto.setId(target.getId());
        dto.setTarget(target.getTarget());
        dto.setBranchName(target.getBranch().getBrnName());
        dto.setRegionName(target.getBranch().getRegion().getRgnName());
        dto.setCreatedDatetime(target.getCreatedDatetime());
        dto.setModifyDatetime(target.getModifyDatetime());
        return dto;
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepo.findByUsername(username).orElse(null);
    }
}