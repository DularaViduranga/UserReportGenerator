package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.target.TargetSaveRequestDTO;
import com.userreport.UserReportBackend.dto.target.TargetSaveResponseDTO;
import com.userreport.UserReportBackend.dto.target.TargetUpdateRequestDTO;
import com.userreport.UserReportBackend.dto.target.TargetResponseDTO;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.services.TargetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
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
        List<TargetEntity> targets = targetService.getAllTargets();
        if (targets.isEmpty()) {
            return ResponseEntity.ok("No targets found");
        }
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/responses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllTargetResponses() {
        List<TargetResponseDTO> targets = targetService.getAllTargetResponses();
        if (targets.isEmpty()) {
            return ResponseEntity.ok("No targets found");
        }
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetEntity> getTargetById(@PathVariable Long id) {
        TargetEntity target = targetService.getTargetById(id);
        return ResponseEntity.ok(target);
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetEntity> getTargetByBranchId(@PathVariable Long branchId) {
        TargetEntity target = targetService.getTargetByBranchId(branchId);
        return ResponseEntity.ok(target);
    }

    @GetMapping("/region/{regionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetEntity>> getTargetsByRegionId(@PathVariable Long regionId) {
        List<TargetEntity> targets = targetService.getTargetsByRegionId(regionId);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/minimum/{amount}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetEntity>> getTargetsByMinimumAmount(@PathVariable BigDecimal amount) {
        List<TargetEntity> targets = targetService.getTargetsByMinimumAmount(amount);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/region/{regionId}/total")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalTargetByRegion(@PathVariable Long regionId) {
        BigDecimal totalTarget = targetService.getTotalTargetByRegion(regionId);
        return ResponseEntity.ok(totalTarget);
    }
}
