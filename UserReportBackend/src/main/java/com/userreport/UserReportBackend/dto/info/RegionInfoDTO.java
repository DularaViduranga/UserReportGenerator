package com.userreport.UserReportBackend.dto.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionInfoDTO {
    private Long id;
    private String rgnName;
    private String rgnDes;
}
