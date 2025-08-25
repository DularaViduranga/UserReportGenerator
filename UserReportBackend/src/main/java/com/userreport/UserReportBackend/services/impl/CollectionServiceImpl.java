package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.collection.CollectionSaveRequestDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionSaveResponseDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionUpdateRequestDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionResponseDTO;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.CollectionRepo;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.CollectionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepo collectionRepo;
    private final BranchRepo branchRepo;
    private final UserRepo userRepo;

    public CollectionServiceImpl(CollectionRepo collectionRepo, BranchRepo branchRepo, UserRepo userRepo) {
        this.collectionRepo = collectionRepo;
        this.branchRepo = branchRepo;
        this.userRepo = userRepo;
    }

    @Override
    public CollectionSaveResponseDTO saveCollection(CollectionSaveRequestDTO collectionSaveRequestDTO) {
        // Validation
        if (collectionSaveRequestDTO.getTarget() == null || collectionSaveRequestDTO.getTarget().compareTo(BigDecimal.ZERO) <= 0) {
            return new CollectionSaveResponseDTO(null, "Target amount must be greater than zero");
        }
        if (collectionSaveRequestDTO.getDue() == null || collectionSaveRequestDTO.getDue().compareTo(BigDecimal.ZERO) < 0) {
            return new CollectionSaveResponseDTO(null, "Due amount cannot be negative");
        }
        if (collectionSaveRequestDTO.getCollectionAmount() == null || collectionSaveRequestDTO.getCollectionAmount().compareTo(BigDecimal.ZERO) < 0) {
            return new CollectionSaveResponseDTO(null, "Collection amount cannot be negative");
        }
        if (collectionSaveRequestDTO.getBranchId() == null) {
            return new CollectionSaveResponseDTO(null, "Branch ID cannot be null");
        }

        // Check if branch exists
        BranchEntity branch = branchRepo.findById(collectionSaveRequestDTO.getBranchId()).orElse(null);
        if (branch == null) {
            return new CollectionSaveResponseDTO(null, "Branch not found");
        }

        // Check if collection already exists for this branch
        if (collectionRepo.existsByBranch(branch)) {
            return new CollectionSaveResponseDTO(null, "Collection already exists for this branch");
        }

        // Get current user
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            return new CollectionSaveResponseDTO(null, "User not authenticated");
        }

        try {
            // Calculate percentage
            BigDecimal percentage = calculatePercentage(collectionSaveRequestDTO.getCollectionAmount(),
                    collectionSaveRequestDTO.getTarget());

            CollectionEntity collectionEntity = new CollectionEntity();
            collectionEntity.setTarget(collectionSaveRequestDTO.getTarget());
            collectionEntity.setDue(collectionSaveRequestDTO.getDue());
            collectionEntity.setCollectionAmount(collectionSaveRequestDTO.getCollectionAmount());
            collectionEntity.setPercentage(percentage);
            collectionEntity.setBranch(branch);
            collectionEntity.setCreatedBy(currentUser);
            collectionEntity.setCreatedDatetime(LocalDateTime.now());

            collectionRepo.save(collectionEntity);
            return new CollectionSaveResponseDTO("Collection saved successfully", null);
        } catch (Exception e) {
            return new CollectionSaveResponseDTO(null, "Error saving collection: " + e.getMessage());
        }
    }

    @Override
    public CollectionSaveResponseDTO updateCollection(Long id, CollectionUpdateRequestDTO collectionUpdateRequestDTO) {
        // Validation
        if (collectionUpdateRequestDTO.getTarget() == null || collectionUpdateRequestDTO.getTarget().compareTo(BigDecimal.ZERO) <= 0) {
            return new CollectionSaveResponseDTO(null, "Target amount must be greater than zero");
        }
        if (collectionUpdateRequestDTO.getDue() == null || collectionUpdateRequestDTO.getDue().compareTo(BigDecimal.ZERO) < 0) {
            return new CollectionSaveResponseDTO(null, "Due amount cannot be negative");
        }
        if (collectionUpdateRequestDTO.getCollectionAmount() == null || collectionUpdateRequestDTO.getCollectionAmount().compareTo(BigDecimal.ZERO) < 0) {
            return new CollectionSaveResponseDTO(null, "Collection amount cannot be negative");
        }

        CollectionEntity existingCollection = collectionRepo.findById(id).orElse(null);
        if (existingCollection == null) {
            return new CollectionSaveResponseDTO(null, "Collection not found");
        }

        // Get current user
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            return new CollectionSaveResponseDTO(null, "User not authenticated");
        }

        try {
            // Calculate percentage
            BigDecimal percentage = calculatePercentage(collectionUpdateRequestDTO.getCollectionAmount(),
                    collectionUpdateRequestDTO.getTarget());

            existingCollection.setTarget(collectionUpdateRequestDTO.getTarget());
            existingCollection.setDue(collectionUpdateRequestDTO.getDue());
            existingCollection.setCollectionAmount(collectionUpdateRequestDTO.getCollectionAmount());
            existingCollection.setPercentage(percentage);
            existingCollection.setModifyBy(currentUser);
            existingCollection.setModifyDatetime(LocalDateTime.now());

            collectionRepo.save(existingCollection);
            return new CollectionSaveResponseDTO("Collection updated successfully", null);
        } catch (Exception e) {
            return new CollectionSaveResponseDTO(null, "Error updating collection: " + e.getMessage());
        }
    }

    @Override
    public void deleteCollection(Long id) {
        if (!collectionRepo.existsById(id)) {
            throw new RuntimeException("Collection not found");
        }
        try {
            collectionRepo.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting collection: " + e.getMessage());
        }
    }

    @Override
    public List<CollectionEntity> getAllCollections() {
        return collectionRepo.findAll();
    }

    @Override
    public CollectionEntity getCollectionById(Long id) {
        return collectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));
    }

    @Override
    public CollectionEntity getCollectionByBranchId(Long branchId) {
        return collectionRepo.findCollectionByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Collection not found for branch id: " + branchId));
    }

    @Override
    public List<CollectionEntity> getCollectionsByRegionId(Long regionId) {
        return collectionRepo.findByRegionId(regionId);
    }

    @Override
    public List<CollectionEntity> getCollectionsByPercentageThreshold(BigDecimal threshold) {
        return collectionRepo.findByPercentageGreaterThanEqual(threshold);
    }

    @Override
    public List<CollectionResponseDTO> getAllCollectionResponses() {
        List<CollectionEntity> collections = collectionRepo.findAll();
        return collections.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private CollectionResponseDTO convertToResponseDTO(CollectionEntity collection) {
        CollectionResponseDTO dto = new CollectionResponseDTO();
        dto.setId(collection.getId());
        dto.setTarget(collection.getTarget());
        dto.setDue(collection.getDue());
        dto.setCollectionAmount(collection.getCollectionAmount());
        dto.setPercentage(collection.getPercentage());
        dto.setBranchName(collection.getBranch().getBrnName());
        dto.setRegionName(collection.getBranch().getRegion().getRgnName());
        dto.setCreatedDatetime(collection.getCreatedDatetime());
        dto.setModifyDatetime(collection.getModifyDatetime());
        return dto;
    }

    private BigDecimal calculatePercentage(BigDecimal collectionAmount, BigDecimal target) {
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return collectionAmount.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
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