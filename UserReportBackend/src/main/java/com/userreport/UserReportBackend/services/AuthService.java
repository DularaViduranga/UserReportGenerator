package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.user.*;
import com.userreport.UserReportBackend.entity.Role;
import com.userreport.UserReportBackend.entity.UserEntity;

import java.util.List;

public interface AuthService {
    List<UserEntity> getAllUsers();
    UserEntity createUser(RegisterRequestDTO userData);
    RegisterResponseDTO createAdminUser(RegisterRequestDTO registerRequestDTO);
    boolean deleteUser(Long userId);
    boolean updateUserRole(Long userId, Role role);
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO);

    RegisterResponseDTO createBranchUser(UserCreateRequestDTOByAdmin userCreateRequestDTOByAdmin);
}
