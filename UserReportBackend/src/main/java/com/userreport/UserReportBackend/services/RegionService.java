package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.region.RegionSaveRequestDTO;
import com.userreport.UserReportBackend.dto.region.RegionSaveResponseDTO;
import com.userreport.UserReportBackend.dto.region.RegionDescriptionUpdateRequestDTO;
import com.userreport.UserReportBackend.entity.RegionEntity;

import java.util.List;

public interface RegionService {
    RegionSaveResponseDTO saveRegion(RegionSaveRequestDTO regionSaveRequestDTO);

    List<RegionEntity> getAllRegions();

    RegionSaveResponseDTO updateRegion(Long id, RegionSaveRequestDTO regionSaveRequestDTO);

    void deleteRegion(Long id);

    RegionSaveResponseDTO updateRegionDescription(Long id, RegionDescriptionUpdateRequestDTO regionDescriptionUpdateRequestDTO);
}
