package com.userreport.UserReportBackend.dto.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyCollectionSummaryDTO {
    private Integer year;
    private Integer month;
    private BigDecimal totalTarget;
    private BigDecimal totalDue;
    private BigDecimal totalCollection;
    private BigDecimal overallPercentage;
    private Integer branchCount;
    private List<CollectionResponseDTO> collections;
}
