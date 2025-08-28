package com.userreport.UserReportBackend.dto.target;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearlyTargetSummaryDTO {
    private Integer year;
    private BigDecimal totalTarget;
    private Integer totalBranches;
    private List<MonthlyTargetSummaryDTO> monthlyData;
}
