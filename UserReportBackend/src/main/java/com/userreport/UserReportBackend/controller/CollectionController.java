package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.collection.*;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.services.CollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;


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

    @PostMapping("/upload/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadCollectionsFromExcel(
            @RequestParam("file") MultipartFile file,
            @PathVariable int year,
            @PathVariable int month) {

        try {
            collectionService.saveCollectionsFromExcel(file, year, month);
            return ResponseEntity.ok("Collections uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload collections: " + e.getMessage());
        }
    }
    @PostMapping("/upload/update/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateCollectionsFromExcel(
            @RequestParam("file") MultipartFile file,
            @PathVariable int year,
            @PathVariable int month) {

        try {
            collectionService.updateCollectionsFromExcel(file, year, month);
            return ResponseEntity.ok("Collections updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update collections: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
        List<CollectionResponseDTO> collections = collectionService.getAllCollectionResponses();
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

    @GetMapping("/getCollectionById/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionResponseDTO> getCollectionById(@PathVariable Long id) {
        CollectionResponseDTO collection = collectionService.getCollectionResponseById(id);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/getCollectionByBranchId/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionResponseDTO> getCollectionByBranchId(@PathVariable Long branchId) {
        CollectionResponseDTO collection = collectionService.getCollectionResponseByBranchId(branchId);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/branch/{branchId}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionResponseDTO> getCollectionByBranchIdAndYearMonth(@PathVariable Long branchId,
                                                                                @PathVariable Integer year,
                                                                                @PathVariable Integer month) {
        CollectionResponseDTO collection = collectionService.getCollectionResponseByBranchIdAndYearMonth(branchId, year, month);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/branch/{branchId}/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByBranchIdAndYear(@PathVariable Long branchId,
                                                                                  @PathVariable Integer year) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByBranchIdAndYear(branchId, year);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/getCollectionsByRegionId/{regionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByRegionId(@PathVariable Long regionId) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByRegionId(regionId);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/region/{regionId}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByRegionIdAndYearMonth(@PathVariable Long regionId,
                                                                                       @PathVariable Integer year,
                                                                                       @PathVariable Integer month) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByRegionIdAndYearMonth(regionId, year, month);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/region/{regionId}/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByRegionIdAndYear(@PathVariable Long regionId,
                                                                                            @PathVariable Integer year) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByRegionIdAndYear(regionId, year);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/getCollectionsByPercentageThreshold/{threshold}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByPercentageThreshold(@PathVariable BigDecimal threshold) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByPercentageThreshold(threshold);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByYear(@PathVariable Integer year) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByYear(year);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollectionResponseDTO>> getCollectionsByYearAndMonth(@PathVariable Integer year,
                                                                               @PathVariable Integer month) {
        List<CollectionResponseDTO> collections = collectionService.getCollectionResponsesByYearAndMonth(year, month);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/region/{regionId}/total/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalCollectionByRegionAndYearMonth(@PathVariable Long regionId,
                                                                             @PathVariable Integer year,
                                                                             @PathVariable Integer month) {
        BigDecimal totalCollection = collectionService.getTotalCollectionByRegionAndYearMonth(regionId, year, month);
        return ResponseEntity.ok(totalCollection);
    }



    @GetMapping("/summary/monthly/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyCollectionSummaryDTO> getMonthlyCollectionSummary(@PathVariable Integer year,
                                                                                   @PathVariable Integer month) {
        MonthlyCollectionSummaryDTO summary = collectionService.getMonthlyCollectionSummary(year, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/monthly/region/{regionId}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyCollectionSummaryDTO> getMonthlyCollectionSummaryByRegion(@PathVariable Long regionId,
                                                                                           @PathVariable Integer year,
                                                                                           @PathVariable Integer month) {
        MonthlyCollectionSummaryDTO summary = collectionService.getMonthlyCollectionSummaryByRegion(regionId, year, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/yearly/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<YearlyCollectionSummaryDTO> getYearlyCollectionSummary(@PathVariable Integer year) {
        YearlyCollectionSummaryDTO summary = collectionService.getYearlyCollectionSummary(year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/yearly/region/{regionId}/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<YearlyCollectionSummaryDTO> getYearlyCollectionSummaryByRegion(@PathVariable Long regionId,
                                                                                         @PathVariable Integer year) {
        YearlyCollectionSummaryDTO summary = collectionService.getYearlyCollectionSummaryByRegion(regionId, year);
        return ResponseEntity.ok(summary);
    }
}
