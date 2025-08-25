package com.userreport.UserReportBackend.dto.target;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetResponseDTO {
    private Long id;
    private BigDecimal target;
    private String branchName;
    private String regionName;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifyDatetime;
}
