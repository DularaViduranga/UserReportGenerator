package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.branch.BranchResponseDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSaveRequestDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSaveResponseDTO;
import com.userreport.UserReportBackend.dto.branch.BranchSummaryResponseDTO;
import com.userreport.UserReportBackend.entity.BranchEntity;

import java.util.List;

public interface BranchService {
    BranchSaveResponseDTO saveBranch(BranchSaveRequestDTO branchSaveRequestDTO);

    List<BranchEntity> getAllBranches();

    List<BranchResponseDTO> getAllBranchResponses();

    List<BranchSummaryResponseDTO> getAllBranchSummaries();

    BranchResponseDTO getBranchResponseById(Long id);

    List<BranchResponseDTO> getBranchResponsesByRegionId(Long regionId);
}
