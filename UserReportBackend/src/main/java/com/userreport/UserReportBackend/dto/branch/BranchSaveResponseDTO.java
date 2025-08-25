package com.userreport.UserReportBackend.dto.branch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchSaveResponseDTO {
    private String message;
    private String error;
}
