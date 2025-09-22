package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.target.*;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.services.TargetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/targets")
public class TargetController {

    private final TargetService targetService;

    public TargetController(TargetService targetService) {
        this.targetService = targetService;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetSaveResponseDTO> createTarget(@RequestBody TargetSaveRequestDTO targetSaveRequestDTO) {
        TargetSaveResponseDTO response = targetService.saveTarget(targetSaveRequestDTO);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/upload/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadTargetsFromExcel(
            @RequestParam("file") MultipartFile file,
            @PathVariable int year,
            @PathVariable int month) {

        try {
            targetService.saveTargetsFromExcel(file, year, month);
            return ResponseEntity.ok("Targets uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload targets: " + e.getMessage());
        }
    }


    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TargetSaveResponseDTO> updateTarget(@PathVariable Long id,
                                                              @RequestBody TargetUpdateRequestDTO targetUpdateRequestDTO) {
        TargetSaveResponseDTO response = targetService.updateTarget(id, targetUpdateRequestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTarget(@PathVariable Long id) {
        targetService.deleteTarget(id);
        return ResponseEntity.ok("Target deleted successfully");
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllTargets() {
        List<TargetResponseDTO> targets = targetService.getAllTargetResponses();
        if (targets.isEmpty()) {
            return ResponseEntity.ok("No targets found");
        }
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/targetById/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetResponseDTO> getTargetById(@PathVariable Long id) {
        TargetResponseDTO target = targetService.getTargetResponseById(id);
        return ResponseEntity.ok(target);
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetResponseDTO> getTargetByBranchId(@PathVariable Long branchId) {
        TargetResponseDTO target = targetService.getTargetResponseByBranchId(branchId);
        return ResponseEntity.ok(target);
    }

    @GetMapping("/branch/{branchId}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetResponseDTO> getTargetByBranchIdAndYearMonth(@PathVariable Long branchId,
                                                                        @PathVariable Integer year,
                                                                        @PathVariable Integer month) {
        TargetResponseDTO target = targetService.getTargetResponseByBranchIdAndYearMonth(branchId, year, month);
        return ResponseEntity.ok(target);
    }

    @GetMapping("/branch/{branchId}/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetResponseDTO>> getTargetsByBranchIdAndYear(@PathVariable Long branchId,
                                                                          @PathVariable Integer year) {
        List<TargetResponseDTO> targets = targetService.getTargetResponsesByBranchIdAndYear(branchId, year);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/region/{regionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetResponseDTO>> getTargetsByRegionId(@PathVariable Long regionId) {
        List<TargetResponseDTO> targets = targetService.getTargetResponsesByRegionId(regionId);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/region/{regionId}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetResponseDTO>> getTargetsByRegionIdAndYearMonth(@PathVariable Long regionId,
                                                                               @PathVariable Integer year,
                                                                               @PathVariable Integer month) {
        List<TargetResponseDTO> targets = targetService.getTargetResponsesByRegionIdAndYearMonth(regionId, year, month);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/minimum/{amount}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetResponseDTO>> getTargetsByMinimumAmount(@PathVariable BigDecimal amount) {
        List<TargetResponseDTO> targets = targetService.getTargetResponsesByMinimumAmount(amount);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetResponseDTO>> getTargetsByYear(@PathVariable Integer year) {
        List<TargetResponseDTO> targets = targetService.getTargetResponsesByYear(year);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetResponseDTO>> getTargetsByYearAndMonth(@PathVariable Integer year,
                                                                       @PathVariable Integer month) {
        List<TargetResponseDTO> targets = targetService.getTargetResponsesByYearAndMonth(year, month);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/region/{regionId}/total")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalTargetByRegion(@PathVariable Long regionId) {
        BigDecimal totalTarget = targetService.getTotalTargetByRegion(regionId);
        return ResponseEntity.ok(totalTarget);
    }

    @GetMapping("/region/{regionId}/total/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalTargetByRegionAndYearMonth(@PathVariable Long regionId,
                                                                         @PathVariable Integer year,
                                                                         @PathVariable Integer month) {
        BigDecimal totalTarget = targetService.getTotalTargetByRegionAndYearMonth(regionId, year, month);
        return ResponseEntity.ok(totalTarget);
    }

    @GetMapping("/summary/monthly/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyTargetSummaryDTO> getMonthlyTargetSummary(@PathVariable Integer year,
                                                                           @PathVariable Integer month) {
        MonthlyTargetSummaryDTO summary = targetService.getMonthlyTargetSummary(year, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/monthly/region/{regionId}/year/{year}/month/{month}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonthlyTargetSummaryDTO> getMonthlyTargetSummaryByRegion(@PathVariable Long regionId,
                                                                                   @PathVariable Integer year,
                                                                                   @PathVariable Integer month) {
        MonthlyTargetSummaryDTO summary = targetService.getMonthlyTargetSummaryByRegion(regionId, year, month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/yearly/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<YearlyTargetSummaryDTO> getYearlyTargetSummary(@PathVariable Integer year) {
        YearlyTargetSummaryDTO summary = targetService.getYearlyTargetSummary(year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/yearly/region/{regionId}/year/{year}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<YearlyTargetSummaryDTO> getYearlyTargetSummaryByRegion(@PathVariable Long regionId,
                                                                                 @PathVariable Integer year) {
        YearlyTargetSummaryDTO summary = targetService.getYearlyTargetSummaryByRegion(regionId, year);
        return ResponseEntity.ok(summary);
    }
}
