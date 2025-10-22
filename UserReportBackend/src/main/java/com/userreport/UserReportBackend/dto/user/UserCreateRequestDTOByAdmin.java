package com.userreport.UserReportBackend.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreateRequestDTOByAdmin {
    private String name;
    private String email;
    private String username;
    private String password;
    private Long branchId;

}
