package com.userreport.UserReportBackend.dto.region;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionSaveRequestDTO {
    private String rgnName;
    private String rgnDes;
}
