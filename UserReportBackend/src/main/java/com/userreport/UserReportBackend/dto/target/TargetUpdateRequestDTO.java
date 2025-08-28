package com.userreport.UserReportBackend.dto.target;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetUpdateRequestDTO {
    private BigDecimal target;
    private Integer targetYear;
    private Integer targetMonth;
}