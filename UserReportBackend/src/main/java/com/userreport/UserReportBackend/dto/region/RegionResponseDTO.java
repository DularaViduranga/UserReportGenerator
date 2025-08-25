package com.userreport.UserReportBackend.dto.region;

import com.userreport.UserReportBackend.dto.branch.BranchSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionResponseDTO {
    private Long id;
    private String rgnName;
    private String rgnDes;
    private List<BranchSummaryDTO> branches;
    private int totalBranches;
}
