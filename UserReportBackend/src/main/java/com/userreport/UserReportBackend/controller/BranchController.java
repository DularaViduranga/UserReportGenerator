package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.branch.BranchResponseDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSaveRequestDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSaveResponseDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSummaryResponseDTO;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.RegionEntity;
import com.userreport.UserReportBackend.services.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BranchSaveResponseDTO> createBranch(@RequestBody BranchSaveRequestDTO branchSaveRequestDTO) {
        BranchSaveResponseDTO res = branchService.saveBranch(branchSaveRequestDTO);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllBranches() {
        List<BranchEntity> branches = branchService.getAllBranches();
        if (branches.isEmpty()) {
            return ResponseEntity.ok("No regions found");
        }
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/responses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllBranchResponses() {
        List<BranchResponseDTO> branches = branchService.getAllBranchResponses();
        if (branches.isEmpty()) {
            return ResponseEntity.ok("No branches found");
        }
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/summaries")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllBranchSummaries() {
        List<BranchSummaryResponseDTO> branches = branchService.getAllBranchSummaries();
        if (branches.isEmpty()) {
            return ResponseEntity.ok("No branches found");
        }
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/{id}/response")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BranchResponseDTO> getBranchResponseById(@PathVariable Long id) {
        BranchResponseDTO branch = branchService.getBranchResponseById(id);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/region/{regionId}/responses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BranchResponseDTO>> getBranchResponsesByRegionId(@PathVariable Long regionId) {
        List<BranchResponseDTO> branches = branchService.getBranchResponsesByRegionId(regionId);
        return ResponseEntity.ok(branches);
    }



}
