package com.userreport.UserReportBackend.dto.region;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionSummaryDTO {
    private Long id;
    private String rgnName;
    private String rgnDes;
    private int totalBranches;
}
