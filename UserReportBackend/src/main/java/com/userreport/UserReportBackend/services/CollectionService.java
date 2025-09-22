package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.collection.*;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.SequencedCollection;

public interface CollectionService {
    CollectionSaveResponseDTO saveCollection(CollectionSaveRequestDTO collectionSaveRequestDTO);

    CollectionSaveResponseDTO updateCollection(Long id, CollectionUpdateRequestDTO collectionUpdateRequestDTO);

    void deleteCollection(Long id);

    List<CollectionResponseDTO> getAllCollectionResponses();

    CollectionResponseDTO getCollectionResponseById(Long id);

    CollectionResponseDTO getCollectionResponseByBranchId(Long branchId);


    CollectionResponseDTO getCollectionResponseByBranchIdAndYearMonth(Long branchId, Integer year, Integer month);

    List<CollectionResponseDTO> getCollectionResponsesByBranchIdAndYear(Long branchId, Integer year);

    List<CollectionResponseDTO> getCollectionResponsesByRegionId(Long regionId);

    List<CollectionResponseDTO> getCollectionResponsesByRegionIdAndYearMonth(Long regionId, Integer year, Integer month);

    List<CollectionResponseDTO> getCollectionResponsesByPercentageThreshold(BigDecimal threshold);

    List<CollectionResponseDTO> getCollectionResponsesByYear(Integer year);

    List<CollectionResponseDTO> getCollectionResponsesByYearAndMonth(Integer year, Integer month);

    List<CollectionEntity> getAllCollections();

    CollectionEntity getCollectionById(Long id);

    CollectionEntity getCollectionByBranchId(Long branchId);

    CollectionEntity getCollectionByBranchIdAndYearMonth(Long branchId, Integer year, Integer month);

    BigDecimal getTotalCollectionByRegionAndYearMonth(Long regionId, Integer year, Integer month);

    MonthlyCollectionSummaryDTO getMonthlyCollectionSummary(Integer year, Integer month);

    MonthlyCollectionSummaryDTO getMonthlyCollectionSummaryByRegion(Long regionId, Integer year, Integer month);

    YearlyCollectionSummaryDTO getYearlyCollectionSummary(Integer year);

    YearlyCollectionSummaryDTO getYearlyCollectionSummaryByRegion(Long regionId, Integer year);

    void saveCollectionsFromExcel(MultipartFile file, int year, int month);

    void updateCollectionsFromExcel(MultipartFile file, int year, int month);

    List<CollectionResponseDTO> getCollectionResponsesByRegionIdAndYear(Long regionId, Integer year);
}
