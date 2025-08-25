package com.userreport.UserReportBackend.dto.target;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetSaveResponseDTO {
    private String message;
    private String error;
}
