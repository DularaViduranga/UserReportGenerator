package com.userreport.UserReportBackend.dto.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionUpdateRequestDTO {
    private BigDecimal target;
    private BigDecimal due;
    private BigDecimal collectionAmount;
}
