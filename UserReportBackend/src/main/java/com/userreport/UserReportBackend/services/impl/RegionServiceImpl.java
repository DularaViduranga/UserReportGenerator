package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.region.RegionSaveRequestDTO;
import com.userreport.UserReportBackend.dto.region.RegionSaveResponseDTO;
import com.userreport.UserReportBackend.dto.region.RegionUpdateRequestDTO;
import com.userreport.UserReportBackend.entity.RegionEntity;
import com.userreport.UserReportBackend.repository.RegionRepo;
import com.userreport.UserReportBackend.services.RegionService;

import org.springframework.stereotype.Service;

import java.util.List;

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
    public RegionSaveResponseDTO updateRegionDescription(Long id, RegionUpdateRequestDTO regionUpdateRequestDTO) {
        if (regionUpdateRequestDTO.getRgnDes() == null || regionUpdateRequestDTO.getRgnDes().isEmpty()) {
            return new RegionSaveResponseDTO(null, "Region description cannot be empty");
        }
        if (!regionRepo.existsById(id)) {
            return new RegionSaveResponseDTO(null, "Region not found");
        }
        try {
            RegionEntity regionEntity = regionRepo.findById(id).get();
            regionEntity.setRgnDes(regionUpdateRequestDTO.getRgnDes());
            regionRepo.save(regionEntity);
            return new RegionSaveResponseDTO("Region description updated successfully", null);
        } catch (Exception e) {
            return new RegionSaveResponseDTO(null, "Error updating region description: " + e.getMessage());
        }
    }


}
