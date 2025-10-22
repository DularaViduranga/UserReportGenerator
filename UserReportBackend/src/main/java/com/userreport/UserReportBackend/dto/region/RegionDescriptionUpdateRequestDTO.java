package com.userreport.UserReportBackend.dto.region;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request DTO for updating region description")
public class RegionDescriptionUpdateRequestDTO {
    @Schema(description = "Region description", example = "North Region")
    private String rgnDes;
}
