package com.userreport.UserReportBackend.dto.branch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchSummaryResponseDTO {
    private Long id;
    private String brnName;
    private String brnDes;
    private String regionName;
    private boolean hasTarget;
    private boolean hasCollection;
}
