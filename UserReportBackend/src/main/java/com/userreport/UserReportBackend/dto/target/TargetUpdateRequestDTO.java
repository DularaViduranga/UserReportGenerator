package com.userreport.UserReportBackend.dto.target;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request DTO for updating target")
public class TargetUpdateRequestDTO {
    @Schema(description = "Target value", example = "1000.50")
    private BigDecimal target;

    @Schema(description = "Target year", example = "2024")
    private Integer targetYear;

    @Schema(description = "Target month", example = "10")
    private Integer targetMonth;
}