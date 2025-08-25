package com.userreport.UserReportBackend.dto.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionInfoDTO {
    private Long id;
    private BigDecimal target;
    private BigDecimal due;
    private BigDecimal collectionAmount;
    private BigDecimal percentage;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifyDatetime;
}
