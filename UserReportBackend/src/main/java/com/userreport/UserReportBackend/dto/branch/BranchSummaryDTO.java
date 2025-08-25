package com.userreport.UserReportBackend.dto.branch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchSummaryDTO {
    private Long id;
    private String brnName;
    private String brnDes;
    private boolean hasTarget;
    private boolean hasCollection;
}
