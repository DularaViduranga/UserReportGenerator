package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.collection.CollectionSaveRequestDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionSaveResponseDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionUpdateRequestDTO;
import com.userreport.UserReportBackend.dto.collection.CollectionResponseDTO;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.services.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionSaveResponseDTO> createCollection(@RequestBody CollectionSaveRequestDTO collectionSaveRequestDTO) {
        CollectionSaveResponseDTO response = collectionService.saveCollection(collectionSaveRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionSaveResponseDTO> updateCollection(@PathVariable Long id,
                                                                      @RequestBody CollectionUpdateRequestDTO collectionUpdateRequestDTO) {
        CollectionSaveResponseDTO response = collectionService.updateCollection(id, collectionUpdateRequestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok("Collection deleted successfully");
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCollections() {
        List<CollectionEntity> collections = collectionService.getAllCollections();
        if (collections.isEmpty()) {
            return ResponseEntity.ok("No collections found");
        }
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/responses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCollectionResponses() {
        List<CollectionResponseDTO> collections = collectionService.getAllCollectionResponses();
        if (collections.isEmpty()) {
            return ResponseEntity.ok("No collections found");
        }
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionEntity> getCollectionById(@PathVariable Long id) {
        CollectionEntity collection = collectionService.getCollectionById(id);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionEntity> getCollectionByBranchId(@PathVariable Long branchId) {
        CollectionEntity collection = collectionService.getCollectionByBranchId(branchId);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/region/{regionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionEntity>> getCollectionsByRegionId(@PathVariable Long regionId) {
        List<CollectionEntity> collections = collectionService.getCollectionsByRegionId(regionId);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/percentage/{threshold}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionEntity>> getCollectionsByPercentageThreshold(@PathVariable BigDecimal threshold) {
        List<CollectionEntity> collections = collectionService.getCollectionsByPercentageThreshold(threshold);
        return ResponseEntity.ok(collections);
    }
}
