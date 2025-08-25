package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.branch.BranchResponseDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSaveRequestDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSaveResponseDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSummaryResponseDTO;
import com.userreport.UserReportBackend.dto.info.CollectionInfoDTO;
import com.userreport.UserReportBackend.dto.info.RegionInfoDTO;
import com.userreport.UserReportBackend.dto.info.TargetInfoDTO;
import com.userreport.UserReportBackend.dto.region.RegionSaveResponseDTO;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.CollectionRepo;
import com.userreport.UserReportBackend.repository.RegionRepo;
import com.userreport.UserReportBackend.repository.TargetRepo;
import com.userreport.UserReportBackend.services.BranchService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {
    private final BranchRepo branchRepo;
    private final RegionRepo regionRepo;
    private final TargetRepo targetRepo;
    private final CollectionRepo collectionRepo;

    public BranchServiceImpl(BranchRepo branchRepo, RegionRepo regionRepo, TargetRepo targetRepo, CollectionRepo collectionRepo) {
        this.branchRepo = branchRepo;
        this.regionRepo = regionRepo;
        this.targetRepo = targetRepo;
        this.collectionRepo = collectionRepo;
    }

    @Override
    public BranchSaveResponseDTO saveBranch(BranchSaveRequestDTO branchSaveRequestDTO) {
        if (branchSaveRequestDTO.getBrnName() == null || branchSaveRequestDTO.getBrnName().isEmpty()) {
            return new BranchSaveResponseDTO(null, "Branch name cannot be empty");
        }
        if (branchSaveRequestDTO.getBrnDes() == null || branchSaveRequestDTO.getBrnDes().isEmpty()) {
            return new BranchSaveResponseDTO(null, "Branch description cannot be empty");
        }
        if (branchSaveRequestDTO.getRegion() == null) {
            return new BranchSaveResponseDTO(null, "Region cannot be null");
        }
        if (branchRepo.existsByBrnName(branchSaveRequestDTO.getBrnName().toUpperCase())) {
            return new BranchSaveResponseDTO(null, "Branch already exists");
        }
        String regionName = branchSaveRequestDTO.getRegion().toUpperCase();
        if (!regionRepo.existsByRgnName(regionName)) {
            return new BranchSaveResponseDTO(null, "Region with name " + regionName + " does not exist");
        }
        BranchEntity branchEntity = new BranchEntity(
                branchSaveRequestDTO.getBrnName().toUpperCase(),
                branchSaveRequestDTO.getBrnDes(),
                regionRepo.findByRgnName(regionName)
        );
        try {
            branchRepo.save(branchEntity);
            return new BranchSaveResponseDTO("Branch saved successfully", null);
        } catch (Exception e) {
            return new BranchSaveResponseDTO(null, "Error saving branch: " + e.getMessage());
        }
    }

    @Override
    public List<BranchEntity> getAllBranches() {
        List<BranchEntity> branches = branchRepo.findAll();
        if (branches.isEmpty()) {
            return List.of(); // Return an empty list if no branches are found
        }
        return branches;
    }

    @Override
    public List<BranchResponseDTO> getAllBranchResponses() {
        List<BranchEntity> branches = branchRepo.findAll();
        return branches.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BranchSummaryResponseDTO> getAllBranchSummaries() {
        List<BranchEntity> branches = branchRepo.findAll();
        return branches.stream()
                .map(this::convertToSummaryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponseDTO getBranchResponseById(Long id) {
        BranchEntity branch = branchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return convertToResponseDTO(branch);
    }

    @Override
    public List<BranchResponseDTO> getBranchResponsesByRegionId(Long regionId) {
        List<BranchEntity> branches = branchRepo.findAll().stream()
                .filter(branch -> branch.getRegion().getId())
                .collect(Collectors.toList());

        return branches.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private BranchResponseDTO convertToResponseDTO(BranchEntity branch) {
        BranchResponseDTO dto = new BranchResponseDTO();
        dto.setId(branch.getId());
        dto.setBrnName(branch.getBrnName());
        dto.setBrnDes(branch.getBrnDes());

        // Set region info
        RegionInfoDTO regionInfo = new RegionInfoDTO();
        regionInfo.setId((long) branch.getRegion().getId());
        regionInfo.setRgnName(branch.getRegion().getRgnName());
        regionInfo.setRgnDes(branch.getRegion().getRgnDes());
        dto.setRegion(regionInfo);

        // Set target info if exists
        TargetEntity target = targetRepo.findByBranch(branch).orElse(null);
        if (target != null) {
            TargetInfoDTO targetInfo = new TargetInfoDTO();
            targetInfo.setId(target.getId());
            targetInfo.setTarget(target.getTarget());
            targetInfo.setCreatedDatetime(target.getCreatedDatetime());
            targetInfo.setModifyDatetime(target.getModifyDatetime());
            dto.setTarget(targetInfo);
        }

        // Set collection info if exists
        CollectionEntity collection = collectionRepo.findByBranch(branch).orElse(null);
        if (collection != null) {
            CollectionInfoDTO collectionInfo = new CollectionInfoDTO();
            collectionInfo.setId(collection.getId());
            collectionInfo.setTarget(collection.getTarget());
            collectionInfo.setDue(collection.getDue());
            collectionInfo.setCollectionAmount(collection.getCollectionAmount());
            collectionInfo.setPercentage(collection.getPercentage());
            collectionInfo.setCreatedDatetime(collection.getCreatedDatetime());
            collectionInfo.setModifyDatetime(collection.getModifyDatetime());
            dto.setCollection(collectionInfo);
        }

        return dto;
    }

    private BranchSummaryResponseDTO convertToSummaryResponseDTO(BranchEntity branch) {
        BranchSummaryResponseDTO dto = new BranchSummaryResponseDTO();
        dto.setId(branch.getId());
        dto.setBrnName(branch.getBrnName());
        dto.setBrnDes(branch.getBrnDes());
        dto.setRegionName(branch.getRegion().getRgnName());
        dto.setHasTarget(targetRepo.existsByBranch(branch));
        dto.setHasCollection(collectionRepo.existsByBranch(branch));
        return dto;
    }

}
