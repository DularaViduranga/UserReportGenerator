package com.userreport.UserReportBackend.dto.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionResponseDTO {
    private Long id;
    private BigDecimal target;
    private BigDecimal due;
    private BigDecimal collectionAmount;
    private BigDecimal percentage;
    private Integer collectionYear;
    private Integer collectionMonth;
    private String branchName;
    private String regionName;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifyDatetime;
    private String createdByUsername;
    private String modifyByUsername;
}