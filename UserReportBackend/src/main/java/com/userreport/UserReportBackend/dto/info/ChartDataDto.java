package com.userreport.UserReportBackend.dto.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataDto {
    private String name; // This will be the branch name
    private BigDecimal target;
    private BigDecimal collection;
    private BigDecimal achievement;
}
