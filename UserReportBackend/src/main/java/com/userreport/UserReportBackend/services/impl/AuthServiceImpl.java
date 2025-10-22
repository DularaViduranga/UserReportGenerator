package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.user.*;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.Role;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.exception.UserNotFoundException;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.AuthService;
import com.userreport.UserReportBackend.services.JWTservice;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepo userRepo;
    private final BranchRepo branchRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTservice jwtservice;

    public AuthServiceImpl(UserRepo userRepo, BranchRepo branchRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTservice jwtservice) {
        this.userRepo = userRepo;
        this.branchRepo = branchRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtservice = jwtservice;
    }

    @Override
    public List<UserEntity> getAllUsers() {
        System.out.println("Getting all users...");
        List<UserEntity> users = userRepo.findAll();
        System.out.println("Found " + users.size() + " users");
        users.forEach(user -> System.out.println("User: " + user.getUsername() + ", Role: " + user.getRole()));
        return users;
    }

    @Override
    public UserEntity createUser(RegisterRequestDTO userData) {
        Role role;
        Long branchId = null;

        String branchName = userData.getUsername().toUpperCase();

        // Find the branch in the database
        BranchEntity branch = branchRepo.findByBrnNameIgnoreCase(branchName)
                .orElseThrow(() -> new RuntimeException("Branch '" + branchName + "' not found. Please contact administrator."));

        branchId = branch.getId();

        UserEntity newUser = new UserEntity(
                userData.getName(),
                userData.getEmail(),
                userData.getUsername(),
                userData.getPassword(),
//                passwordEncoder.encode(userData.getPassword()),
                Role.USER,
                branchId
        );
        return userRepo.save(newUser);
    }


    @Override
    public RegisterResponseDTO createAdminUser(RegisterRequestDTO registerRequestDTO) {
        if (isUsernameTaken(registerRequestDTO.getUsername())) return new RegisterResponseDTO(null, "User already exists");

        UserEntity newUser = new UserEntity(
                registerRequestDTO.getName(),
                registerRequestDTO.getEmail(),
                registerRequestDTO.getUsername(),
                registerRequestDTO.getPassword(),
//                passwordEncoder.encode(registerRequestDTO.getPassword()),
                Role.ADMIN
        );
        var user = userRepo.save(newUser);

        if (user.getId() == null) {
            throw new RuntimeException("Admin user creation failed");
        }
        return new RegisterResponseDTO("Admin user successfully created at " + LocalDateTime.now(), null);
    }

    @Override
    public RegisterResponseDTO createBranchUser(UserCreateRequestDTOByAdmin userCreateRequestDTOByAdmin) {
        if (isUsernameTaken(userCreateRequestDTOByAdmin.getUsername())) {
            return new RegisterResponseDTO(null, "Username already exists");
        }

        if (isEmailTaken(userCreateRequestDTOByAdmin.getEmail())) {
            return new RegisterResponseDTO(null, "Email already exists");
        }

        UserEntity newUser = new UserEntity(
                userCreateRequestDTOByAdmin.getName(),
                userCreateRequestDTOByAdmin.getEmail(),
                userCreateRequestDTOByAdmin.getUsername(),
                userCreateRequestDTOByAdmin.getPassword(),
//                passwordEncoder.encode(userCreateRequestDTOByAdmin.getPassword()),
                Role.USER,
                userCreateRequestDTOByAdmin.getBranchId()
        );
        var user = userRepo.save(newUser);

        if (user.getId() == null) {
            throw new RuntimeException("Branch user creation failed");
        }
        return new RegisterResponseDTO("Branch user successfully created at " + LocalDateTime.now(), null);
    }


    @Override
    public boolean deleteUser(Long userId) {
        if (userRepo.existsById(userId)) {
            userRepo.deleteById(userId);
            return true;
        }
        throw new UserNotFoundException("User not found with id: " + userId);
    }

    @Override
    public boolean updateUserRole(Long userId, Role role) {
        return userRepo.findById(userId)
                .map(user -> {
                    user.setRole(role);
                    userRepo.save(user);
                    return true;
                })
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }


    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        UserEntity user = userRepo.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getPassword().equals(loginRequestDTO.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        
        // Add branch information if user has a branch
        if (user.getBranchId() != null) {
            claims.put("branchId", user.getBranchId());
            // Also include branch name for easier access
            BranchEntity branch = branchRepo.findById(user.getBranchId()).orElse(null);
            if (branch != null) {
                claims.put("branchName", branch.getBrnName());
            }
        }

        String jwtToken = jwtservice.getJwtToken(loginRequestDTO.getUsername(), claims);

        return new LoginResponseDTO(jwtToken, LocalDateTime.now(), null, "Token Generate Successfully");
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        try {
            // Check if user already exists by username or email
            if (isUsernameTaken(registerRequestDTO.getUsername())) {
                return new RegisterResponseDTO(null, "Username already exists");
            }
            
            if (isEmailTaken(registerRequestDTO.getEmail())) {
                return new RegisterResponseDTO(null, "Email already exists");
            }
            
            var user = this.createUser(registerRequestDTO);

            if (user.getId() == null) {
                throw new RuntimeException("User creation failed");
            }

            return new RegisterResponseDTO("User successfully created at " + LocalDateTime.now(), null);
        } catch (Exception e) {
            return new RegisterResponseDTO(null, "Registration failed: " + e.getMessage());
        }
    }




    private Boolean isUsernameTaken(String username) {
        return userRepo.findByUsername(username).isPresent();
    }
    
    private Boolean isEmailTaken(String email) {
        return userRepo.findByEmail(email).isPresent();
    }
}
