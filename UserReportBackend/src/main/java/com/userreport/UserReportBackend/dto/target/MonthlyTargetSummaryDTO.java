package com.userreport.UserReportBackend.dto.target;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTargetSummaryDTO {
    private Integer year;
    private Integer month;
    private BigDecimal totalTarget;
    private Integer branchCount;
    private List<TargetResponseDTO> targets;
}
