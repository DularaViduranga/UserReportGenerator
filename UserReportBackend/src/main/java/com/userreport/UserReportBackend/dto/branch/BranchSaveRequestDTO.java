package com.userreport.UserReportBackend.dto.branch;

import com.userreport.UserReportBackend.entity.RegionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchSaveRequestDTO {
    private String brnName;
    private String brnDes;
    private String region;

}
