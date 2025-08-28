package com.userreport.UserReportBackend.dto.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionSaveRequestDTO {
    private BigDecimal collectionAmount;
    private Integer collectionYear;
    private Integer collectionMonth;
    private Long branchId;
}
