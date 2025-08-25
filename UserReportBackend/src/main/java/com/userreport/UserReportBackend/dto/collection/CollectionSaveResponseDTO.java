package com.userreport.UserReportBackend.dto.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionSaveResponseDTO {
    private String message;
    private String error;
}
