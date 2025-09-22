package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.collection.*;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.CollectionRepo;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.CollectionService;
import com.userreport.UserReportBackend.services.ExcelUploadService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepo collectionRepo;
    private final BranchRepo branchRepo;
    private final UserRepo userRepo;
    private final ExcelUploadService excelUploadService;

    public CollectionServiceImpl(CollectionRepo collectionRepo, BranchRepo branchRepo, UserRepo userRepo, ExcelUploadService excelUploadService) {
        this.collectionRepo = collectionRepo;
        this.branchRepo = branchRepo;
        this.userRepo = userRepo;
        this.excelUploadService = excelUploadService;
    }

    @Override
    public CollectionSaveResponseDTO saveCollection(CollectionSaveRequestDTO collectionSaveRequestDTO) {

        if (collectionSaveRequestDTO.getCollectionAmount() == null || collectionSaveRequestDTO.getCollectionAmount().compareTo(BigDecimal.ZERO) < 0) {
            return new CollectionSaveResponseDTO(null, "Collection amount cannot be negative");
        }
        if (collectionSaveRequestDTO.getBranchId() == null) {
            return new CollectionSaveResponseDTO(null, "Branch ID cannot be null");
        }
        if (collectionSaveRequestDTO.getCollectionYear() == null || collectionSaveRequestDTO.getCollectionYear() < 2000 || collectionSaveRequestDTO.getCollectionYear() > 2100) {
            return new CollectionSaveResponseDTO(null, "Collection year must be between 2000 and 2100");
        }
        if (collectionSaveRequestDTO.getCollectionMonth() == null || collectionSaveRequestDTO.getCollectionMonth() < 1 || collectionSaveRequestDTO.getCollectionMonth() > 12) {
            return new CollectionSaveResponseDTO(null, "Collection month must be between 1 and 12");
        }

        // Check if branch exists
        BranchEntity branch = branchRepo.findById(collectionSaveRequestDTO.getBranchId()).orElse(null);
        if (branch == null) {
            return new CollectionSaveResponseDTO(null, "Branch not found");
        }

        // Get target entity
        TargetEntity target = branchRepo.findByBranchIdAndYearAndMonth(
                collectionSaveRequestDTO.getBranchId(),
                collectionSaveRequestDTO.getCollectionYear(),
                collectionSaveRequestDTO.getCollectionMonth()
        ).orElse(null);

        if (target == null) {
            return new CollectionSaveResponseDTO(null, "Target not found for this branch, year and month");
        }

        // Calculate due amount
        BigDecimal dueAmount = target.getTargetAmount().subtract(collectionSaveRequestDTO.getCollectionAmount());

        // Check if collection already exists for this branch, year, and month
        if (collectionRepo.existsByBranchAndCollectionYearAndCollectionMonth(branch,
                collectionSaveRequestDTO.getCollectionYear(), collectionSaveRequestDTO.getCollectionMonth())) {
            return new CollectionSaveResponseDTO(null, "Collection already exists for this branch in " +
                    getMonthName(collectionSaveRequestDTO.getCollectionMonth()) + " " + collectionSaveRequestDTO.getCollectionYear());
        }

        // Get current user
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            return new CollectionSaveResponseDTO(null, "User not authenticated");
        }

        try {
            // Calculate percentage
            BigDecimal percentage = calculatePercentage(
                    collectionSaveRequestDTO.getCollectionAmount(),
                    target.getTargetAmount()
            );

            CollectionEntity collectionEntity = new CollectionEntity(
                    target.getTargetAmount(),
                    dueAmount,
                    collectionSaveRequestDTO.getCollectionAmount(),
                    collectionSaveRequestDTO.getCollectionYear(),
                    collectionSaveRequestDTO.getCollectionMonth(),
                    branch,
                    currentUser
            );

            // Set percentage explicitly
            collectionEntity.setPercentage(percentage);

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
        if (collectionUpdateRequestDTO.getCollectionYear() != null &&
                (collectionUpdateRequestDTO.getCollectionYear() < 2000 || collectionUpdateRequestDTO.getCollectionYear() > 2100)) {
            return new CollectionSaveResponseDTO(null, "Collection year must be between 2000 and 2100");
        }
        if (collectionUpdateRequestDTO.getCollectionMonth() != null &&
                (collectionUpdateRequestDTO.getCollectionMonth() < 1 || collectionUpdateRequestDTO.getCollectionMonth() > 12)) {
            return new CollectionSaveResponseDTO(null, "Collection month must be between 1 and 12");
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

            if (collectionUpdateRequestDTO.getCollectionYear() != null) {
                existingCollection.setCollectionYear(collectionUpdateRequestDTO.getCollectionYear());
            }
            if (collectionUpdateRequestDTO.getCollectionMonth() != null) {
                existingCollection.setCollectionMonth(collectionUpdateRequestDTO.getCollectionMonth());
            }

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
        List<CollectionEntity> collections = collectionRepo.findCollectionsByBranchId(branchId);
        if (collections.isEmpty()) {
            throw new RuntimeException("Collection not found for branch id: " + branchId);
        }
        // Return the most recent collection
        return collections.stream()
                .max((c1, c2) -> c1.getCreatedDatetime().compareTo(c2.getCreatedDatetime()))
                .orElseThrow(() -> new RuntimeException("Collection not found for branch id: " + branchId));
    }

    @Override
    public CollectionEntity getCollectionByBranchIdAndYearMonth(Long branchId, Integer year, Integer month) {
        return collectionRepo.findByBranchIdAndYearAndMonth(branchId, year, month)
                .orElseThrow(() -> new RuntimeException("Collection not found for branch id: " + branchId +
                        " in " + getMonthName(month) + " " + year));
    }


    @Override
    public BigDecimal getTotalCollectionByRegionAndYearMonth(Long regionId, Integer year, Integer month) {
        BigDecimal total = collectionRepo.getTotalCollectionByRegionAndYearMonth(regionId, year, month);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public MonthlyCollectionSummaryDTO getMonthlyCollectionSummary(Integer year, Integer month) {
        List<CollectionEntity> collections = collectionRepo.findByCollectionYearAndCollectionMonth(year, month);
        return createMonthlyCollectionSummary(year, month, collections);
    }

    @Override
    public MonthlyCollectionSummaryDTO getMonthlyCollectionSummaryByRegion(Long regionId, Integer year, Integer month) {
        List<CollectionEntity> collections = collectionRepo.findByRegionIdAndYearAndMonth(regionId, year, month);
        return createMonthlyCollectionSummary(year, month, collections);
    }

    @Override
    public YearlyCollectionSummaryDTO getYearlyCollectionSummary(Integer year) {
        List<CollectionEntity> collections = collectionRepo.findByCollectionYear(year);
        return createYearlyCollectionSummary(year, collections);
    }

    @Override
    public YearlyCollectionSummaryDTO getYearlyCollectionSummaryByRegion(Long regionId, Integer year) {
        List<CollectionEntity> collections = collectionRepo.findByRegionId(regionId).stream()
                .filter(c -> c.getCollectionYear().equals(year))
                .collect(Collectors.toList());
        return createYearlyCollectionSummary(year, collections);
    }

    @Override
    public void saveCollectionsFromExcel(MultipartFile file, int year, int month) {
        if (!excelUploadService.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid Excel file format");
        }

        try {
            // Fix: Change targetMonth to collectionMonth
            if(!collectionRepo.findByCollectionYearAndCollectionMonth(year, month).isEmpty()) {
                throw new IllegalArgumentException("Collections for " + getMonthName(month) + " " + year + " already exist. Please use update instead of save.");
            } else {
                List<CollectionEntity> collection = excelUploadService.getCollectionsFromExcel(
                        file.getInputStream(), year, month);
                collectionRepo.saveAll(collection);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file", e);
        }
    }

    @Override
    public void updateCollectionsFromExcel(MultipartFile file, int year, int month) {
        if (!excelUploadService.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Invalid Excel file format");
        }

        try {
            // Check if collections exist for this year and month
            List<CollectionEntity> existingCollections = collectionRepo.findByCollectionYearAndCollectionMonth(year, month);

            if (existingCollections.isEmpty()) {
                throw new IllegalArgumentException("No collections found for " + getMonthName(month) + " " + year + ". Please use save instead of update.");
            }

            // Get updated collections from Excel
            List<CollectionEntity> updatedCollections = excelUploadService.updateCollectionsFromExcel(
                    file.getInputStream(), year, month);

            // Save the updated collections
            collectionRepo.saveAll(updatedCollections);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file", e);
        }
    }



    @Override
    public List<CollectionResponseDTO> getAllCollectionResponses() {
        List<CollectionEntity> collections = collectionRepo.findAll();
        return collections.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CollectionResponseDTO getCollectionResponseById(Long id) {
        CollectionEntity entity = collectionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));
        return convertToResponseDTO(entity);
    }

    @Override
    public CollectionResponseDTO getCollectionResponseByBranchId(Long branchId) {
        List<CollectionEntity> collections = collectionRepo.findCollectionsByBranchId(branchId);
        if (collections.isEmpty()) {
            throw new RuntimeException("Collection not found for branch id: " + branchId);
        }

        CollectionEntity entity = collections.stream()
                .max((c1, c2) -> c1.getCreatedDatetime().compareTo(c2.getCreatedDatetime()))
                .orElseThrow(() -> new RuntimeException("Collection not found for branch id: " + branchId));
        return convertToResponseDTO(entity);
    }


    @Override
    public CollectionResponseDTO getCollectionResponseByBranchIdAndYearMonth(Long branchId, Integer year, Integer month) {
        CollectionEntity entity = collectionRepo.findByBranchIdAndYearAndMonth(branchId, year, month)
                .orElseThrow(() -> new RuntimeException("Collection not found for branch id: " + branchId +
                        " in " + getMonthName(month) + " " + year));
        return convertToResponseDTO(entity);
    }

    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByBranchIdAndYear(Long branchId, Integer year) {
        List<CollectionEntity> entities = collectionRepo.findByBranchIdAndYear(branchId, year);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByRegionId(Long regionId) {
        List<CollectionEntity> entities = collectionRepo.findByRegionId(regionId);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByRegionIdAndYearMonth(Long regionId, Integer year, Integer month) {
        List<CollectionEntity> entities = collectionRepo.findByRegionIdAndYearAndMonth(regionId, year, month);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByRegionIdAndYear(Long regionId, Integer year) {
        List<CollectionEntity> entities = collectionRepo.findByRegionId(regionId).stream()
                                                        .filter(c -> c.getCollectionYear().equals(year))
                                                        .collect(Collectors.toList());
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByPercentageThreshold(BigDecimal threshold) {
        List<CollectionEntity> entities = collectionRepo.findByPercentageGreaterThanEqual(threshold);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByYear(Integer year) {
        List<CollectionEntity> entities = collectionRepo.findByCollectionYear(year);
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectionResponseDTO> getCollectionResponsesByYearAndMonth(Integer year, Integer month) {
        List<CollectionEntity> entities = collectionRepo.findByCollectionYearAndCollectionMonth(year, month);
        return entities.stream()
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
        dto.setCollectionYear(collection.getCollectionYear());
        dto.setCollectionMonth(collection.getCollectionMonth());
        dto.setBranchName(collection.getBranch().getBrnName());
        dto.setRegionName(collection.getBranch().getRegion().getRgnName());
        dto.setCreatedDatetime(collection.getCreatedDatetime());
        dto.setModifyDatetime(collection.getModifyDatetime());

        if (collection.getCreatedBy() != null) {
            dto.setCreatedByUsername(collection.getCreatedBy().getUsername());
        }
        if (collection.getModifyBy() != null) {
            dto.setModifyByUsername(collection.getModifyBy().getUsername());
        }

        return dto;
    }

    private MonthlyCollectionSummaryDTO createMonthlyCollectionSummary(Integer year, Integer month, List<CollectionEntity> collections) {
        MonthlyCollectionSummaryDTO summary = new MonthlyCollectionSummaryDTO();
        summary.setYear(year);
        summary.setMonth(month);
        summary.setBranchCount(collections.size());

        BigDecimal totalTarget = collections.stream()
                .map(CollectionEntity::getTarget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalTarget(totalTarget);

        BigDecimal totalCollection = collections.stream()
                .map(CollectionEntity::getCollectionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalCollection(totalCollection);

        BigDecimal totalDue = collections.stream()
                .map(CollectionEntity::getDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalDue(totalDue);

        // Calculate overall percentage
        BigDecimal overallPercentage = BigDecimal.ZERO;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            overallPercentage = totalCollection.multiply(BigDecimal.valueOf(100))
                    .divide(totalTarget, 2, RoundingMode.HALF_UP);
        }
        summary.setOverallPercentage(overallPercentage);

        List<CollectionResponseDTO> collectionResponses = collections.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        summary.setCollections(collectionResponses);

        return summary;
    }

    private YearlyCollectionSummaryDTO createYearlyCollectionSummary(Integer year, List<CollectionEntity> collections) {
        YearlyCollectionSummaryDTO summary = new YearlyCollectionSummaryDTO();
        summary.setYear(year);

        BigDecimal totalTarget = collections.stream()
                .map(CollectionEntity::getTarget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalTarget(totalTarget);

        BigDecimal totalCollection = collections.stream()
                .map(CollectionEntity::getCollectionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalCollection(totalCollection);

        BigDecimal totalDue = collections.stream()
                .map(CollectionEntity::getDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalDue(totalDue);

        // Calculate overall percentage
        BigDecimal overallPercentage = BigDecimal.ZERO;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            overallPercentage = totalCollection.multiply(BigDecimal.valueOf(100))
                    .divide(totalTarget, 2, RoundingMode.HALF_UP);
        }
        summary.setOverallPercentage(overallPercentage);

        // Group by month
        Map<Integer, List<CollectionEntity>> monthlyCollections = collections.stream()
                .collect(Collectors.groupingBy(CollectionEntity::getCollectionMonth));

        List<MonthlyCollectionSummaryDTO> monthlyData = monthlyCollections.entrySet().stream()
                .map(entry -> createMonthlyCollectionSummary(year, entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth()))
                .collect(Collectors.toList());

        summary.setMonthlyData(monthlyData);
        summary.setTotalBranches(collections.stream()
                .collect(Collectors.groupingBy(c -> c.getBranch().getId()))
                .size());

        return summary;
    }

    private BigDecimal calculatePercentage(BigDecimal collectionAmount, BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return collectionAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
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
