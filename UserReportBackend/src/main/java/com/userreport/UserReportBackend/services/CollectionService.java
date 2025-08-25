package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.collection.CollectionResponseDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionSaveRequestDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionSaveResponseDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionUpdateRequestDTO;
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

    CollectionEntity getCollectionByBranchId(Long branchId);

    List<CollectionEntity> getCollectionsByRegionId(Long regionId);

    List<CollectionEntity> getCollectionsByPercentageThreshold(BigDecimal threshold);
}
