package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.collection.*;
import com.userreport.UserReportBackend.entity.CollectionEntity;

import java.math.BigDecimal;
import java.util.List;

public interface CollectionService {
    CollectionSaveResponseDTO saveCollection(CollectionSaveRequestDTO collectionSaveRequestDTO);

    CollectionSaveResponseDTO updateCollection(Long id, CollectionUpdateRequestDTO collectionUpdateRequestDTO);

    void deleteCollection(Long id);

    List<CollectionEntity> getAllCollections();

    List<CollectionResponseDTO> getAllCollectionResponses();

    CollectionEntity getCollectionById(Long id);

    CollectionResponseDTO getCollectionResponseById(Long id);

    CollectionEntity getCollectionByBranchId(Long branchId);

    CollectionResponseDTO getCollectionResponseByBranchId(Long branchId);

    CollectionEntity getCollectionByBranchIdAndYearMonth(Long branchId, Integer year, Integer month);

    CollectionResponseDTO getCollectionResponseByBranchIdAndYearMonth(Long branchId, Integer year, Integer month);

    List<CollectionEntity> getCollectionsByBranchIdAndYear(Long branchId, Integer year);

    List<CollectionResponseDTO> getCollectionResponsesByBranchIdAndYear(Long branchId, Integer year);

    List<CollectionEntity> getCollectionsByRegionId(Long regionId);

    List<CollectionResponseDTO> getCollectionResponsesByRegionId(Long regionId);

    List<CollectionEntity> getCollectionsByRegionIdAndYearMonth(Long regionId, Integer year, Integer month);

    List<CollectionResponseDTO> getCollectionResponsesByRegionIdAndYearMonth(Long regionId, Integer year, Integer month);

    List<CollectionEntity> getCollectionsByPercentageThreshold(BigDecimal threshold);

    List<CollectionResponseDTO> getCollectionResponsesByPercentageThreshold(BigDecimal threshold);

    List<CollectionEntity> getCollectionsByYear(Integer year);

    List<CollectionResponseDTO> getCollectionResponsesByYear(Integer year);

    List<CollectionEntity> getCollectionsByYearAndMonth(Integer year, Integer month);

    List<CollectionResponseDTO> getCollectionResponsesByYearAndMonth(Integer year, Integer month);

    BigDecimal getTotalCollectionByRegionAndYearMonth(Long regionId, Integer year, Integer month);

    MonthlyCollectionSummaryDTO getMonthlyCollectionSummary(Integer year, Integer month);

    MonthlyCollectionSummaryDTO getMonthlyCollectionSummaryByRegion(Long regionId, Integer year, Integer month);

    YearlyCollectionSummaryDTO getYearlyCollectionSummary(Integer year);

    YearlyCollectionSummaryDTO getYearlyCollectionSummaryByRegion(Long regionId, Integer year);
}
