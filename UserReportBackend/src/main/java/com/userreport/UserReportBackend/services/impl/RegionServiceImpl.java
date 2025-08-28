package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.branch.BranchSummaryDTO;
import com.userreport.UserReportBackend.dto.region.*;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.RegionEntity;
import com.userreport.UserReportBackend.repository.RegionRepo;
import com.userreport.UserReportBackend.services.RegionService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegionServiceImpl implements RegionService {
    private final RegionRepo regionRepo;

    public RegionServiceImpl(RegionRepo regionRepo) {
        this.regionRepo = regionRepo;
    }

    @Override
    public RegionSaveResponseDTO saveRegion(RegionSaveRequestDTO regionSaveRequestDTO) {
        if (regionSaveRequestDTO.getRgnName() == null || regionSaveRequestDTO.getRgnName().isEmpty()) {
            return new RegionSaveResponseDTO(null, "Region name cannot be empty");
        }
        if (regionSaveRequestDTO.getRgnDes() == null || regionSaveRequestDTO.getRgnDes().isEmpty()) {
            return new RegionSaveResponseDTO(null, "Region description cannot be empty");
        }
        if (regionRepo.existsByRgnName(regionSaveRequestDTO.getRgnName().toUpperCase())) {
            return new RegionSaveResponseDTO(null, "Region already exists");
        }
        RegionEntity regionEntity = new RegionEntity(
                regionSaveRequestDTO.getRgnName().toUpperCase(),
                regionSaveRequestDTO.getRgnDes()
        );
        try {
            regionRepo.save(regionEntity);
            return new RegionSaveResponseDTO("Region saved successfully", null);
        } catch (Exception e) {
            return new RegionSaveResponseDTO(null, "Error saving region: " + e.getMessage());
        }
    }

    @Override
    public List<RegionEntity> getAllRegions() {
        List<RegionEntity> regions =regionRepo.findAll();
        if (regions.isEmpty()) {
            return List.of(); // Return an empty list if no regions are found
        }
        return regions;
    }

    @Override
    public RegionSaveResponseDTO updateRegion(Long id, RegionSaveRequestDTO regionSaveRequestDTO) {
        if (regionSaveRequestDTO.getRgnName() == null || regionSaveRequestDTO.getRgnName().isEmpty()) {
            return new RegionSaveResponseDTO(null, "Region name cannot be empty");
        }
        if (regionSaveRequestDTO.getRgnDes() == null || regionSaveRequestDTO.getRgnDes().isEmpty()) {
            return new RegionSaveResponseDTO(null, "Region description cannot be empty");
        }
        if (!regionRepo.existsById(id)) {
            return new RegionSaveResponseDTO(null, "Region not found");
        }
        if (regionRepo.existsByRgnName(regionSaveRequestDTO.getRgnName().toUpperCase())) {
            return new RegionSaveResponseDTO(null, "Region already exists");
        }
        try {
            RegionEntity regionEntity = regionRepo.findById(id).get();
            regionEntity.setRgnName(regionSaveRequestDTO.getRgnName().toUpperCase());
            regionEntity.setRgnDes(regionSaveRequestDTO.getRgnDes());
            regionRepo.save(regionEntity);
            return new RegionSaveResponseDTO("Region updated successfully", null);
        } catch (Exception e) {
            return new RegionSaveResponseDTO(null, "Error updating region: " + e.getMessage());
        }
    }

    @Override
    public void deleteRegion(Long id) {
        if (!regionRepo.existsById(id)) {
            throw new RuntimeException("Region not found");
        }
        try {
            regionRepo.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting region: " + e.getMessage());
        }
    }

    @Override
    public RegionSaveResponseDTO updateRegionDescription(Long id, RegionDescriptionUpdateRequestDTO regionDescriptionUpdateRequestDTO) {
        if (regionDescriptionUpdateRequestDTO.getRgnDes() == null || regionDescriptionUpdateRequestDTO.getRgnDes().isEmpty()) {
            return new RegionSaveResponseDTO(null, "Region description cannot be empty");
        }
        if (!regionRepo.existsById(id)) {
            return new RegionSaveResponseDTO(null, "Region not found");
        }
        try {
            RegionEntity regionEntity = regionRepo.findById(id).get();
            regionEntity.setRgnDes(regionDescriptionUpdateRequestDTO.getRgnDes());
            regionRepo.save(regionEntity);
            return new RegionSaveResponseDTO("Region description updated successfully", null);
        } catch (Exception e) {
            return new RegionSaveResponseDTO(null, "Error updating region description: " + e.getMessage());
        }
    }

    @Override
    public List<RegionResponseDTO> getAllRegionResponses() {
        List<RegionEntity> regions = regionRepo.findAll();
        return regions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegionSummaryDTO> getAllRegionSummaries() {
        List<RegionEntity> regions = regionRepo.findAll();
        return regions.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RegionResponseDTO getRegionResponseById(Long id) {
        RegionEntity region = regionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found with id: " + id));
        return convertToResponseDTO(region);
    }


    private RegionResponseDTO convertToResponseDTO(RegionEntity region) {
        RegionResponseDTO dto = new RegionResponseDTO();
        dto.setId((long) region.getId());
        dto.setRgnName(region.getRgnName());
        dto.setRgnDes(region.getRgnDes());
        dto.setTotalBranches(region.getBranches().size());

        // Convert branches to summary DTOs
        List<BranchSummaryDTO> branchSummaries = region.getBranches().stream()
                .map(this::convertBranchToSummaryDTO)
                .collect(Collectors.toList());
        dto.setBranches(branchSummaries);

        return dto;
    }

    private RegionSummaryDTO convertToSummaryDTO(RegionEntity region) {
        RegionSummaryDTO dto = new RegionSummaryDTO();
        dto.setId((long) region.getId());
        dto.setRgnName(region.getRgnName());
        dto.setRgnDes(region.getRgnDes());
        dto.setTotalBranches(region.getBranches().size());
        return dto;
    }

    private BranchSummaryDTO convertBranchToSummaryDTO(BranchEntity branch) {
        BranchSummaryDTO dto = new BranchSummaryDTO();
        dto.setId(branch.getId());
        dto.setBrnName(branch.getBrnName());
        dto.setBrnDes(branch.getBrnDes());
        dto.setHasTarget(!branch.getTargets().isEmpty());
        dto.setHasCollection(false); // Will be set based on collection relationship
        return dto;
    }

}
