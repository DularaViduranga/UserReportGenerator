package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.region.*;
import com.userreport.UserReportBackend.entity.RegionEntity;
import com.userreport.UserReportBackend.services.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/regions")
public class RegionController {
    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegionSaveResponseDTO> createRegion(@RequestBody RegionSaveRequestDTO regionSaveRequestDTO){
        RegionSaveResponseDTO res = regionService.saveRegion(regionSaveRequestDTO);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllRegions() {
        List<RegionResponseDTO> regions = regionService.getAllRegionResponses();
        if (regions.isEmpty()) {
            return ResponseEntity.ok("No regions found");
        }
        return ResponseEntity.ok(regions);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionSaveResponseDTO> updateRegion(@PathVariable Long id, @RequestBody RegionSaveRequestDTO regionSaveRequestDTO) {
        RegionSaveResponseDTO res = regionService.updateRegion(id, regionSaveRequestDTO);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/updateRegionDescription/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionSaveResponseDTO> updateRegionDescription(@PathVariable Long id, @RequestBody RegionDescriptionUpdateRequestDTO regionDescriptionUpdateRequestDTO) {
        RegionSaveResponseDTO res = regionService.updateRegionDescription(id, regionDescriptionUpdateRequestDTO);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return ResponseEntity.ok("Region deleted successfully");
    }

    @GetMapping("/summaries")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllRegionSummaries() {
        List<RegionSummaryDTO> regions = regionService.getAllRegionSummaries();
        if (regions.isEmpty()) {
            return ResponseEntity.ok("No regions found");
        }
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/response/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegionResponseDTO> getRegionResponseById(@PathVariable Long id) {
        RegionResponseDTO region = regionService.getRegionResponseById(id);
        return ResponseEntity.ok(region);
    }

}
