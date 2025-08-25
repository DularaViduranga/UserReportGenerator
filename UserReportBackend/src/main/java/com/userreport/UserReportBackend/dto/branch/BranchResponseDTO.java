package com.userreport.UserReportBackend.dto.branch;

import com.userreport.UserReportBackend.dto.info.CollectionInfoDTO;
import com.userreport.UserReportBackend.dto.info.RegionInfoDTO;
import com.userreport.UserReportBackend.dto.info.TargetInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchResponseDTO {
    private Long id;
    private String brnName;
    private String brnDes;
    private RegionInfoDTO region;
    private TargetInfoDTO target;
    private CollectionInfoDTO collection;
}
