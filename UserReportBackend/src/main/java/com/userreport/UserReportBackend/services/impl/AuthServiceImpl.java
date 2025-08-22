package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.user.LoginRequestDTO;
import com.userreport.UserReportBackend.dto.user.LoginResponseDTO;
import com.userreport.UserReportBackend.dto.user.RegisterRequestDTO;
import com.userreport.UserReportBackend.dto.user.RegisterResponseDTO;
import com.userreport.UserReportBackend.entity.Role;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.exception.UserNotFoundException;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTservice jwtservice;

    public AuthServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTservice jwtservice) {
        this.userRepo = userRepo;
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
        UserEntity newUser = new UserEntity(
                userData.getName(),
                userData.getEmail(),
                userData.getUsername(),
                passwordEncoder.encode(userData.getPassword())
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
                passwordEncoder.encode(registerRequestDTO.getPassword()),
                Role.ADMIN
        );
        var user = userRepo.save(newUser);

        if (user.getId() == null) {
            throw new RuntimeException("Admin user creation failed");
        }
        return new RegisterResponseDTO("Admin user successfully created at " + LocalDateTime.now(), null);
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
        );

        UserEntity user = userRepo.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());

        String jwtToken = jwtservice.getJwtToken(loginRequestDTO.getUsername(), claims);

        return new LoginResponseDTO(jwtToken, LocalDateTime.now(), null, "Token Generate Successfully");
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        if (isUsernameTaken(registerRequestDTO.getUsername())) {
            throw new IllegalArgumentException("User already exists");
        }
        var user = this.createUser(registerRequestDTO);

        if (user.getId() == null) {
            throw new RuntimeException("User creation failed");
        }

        return new RegisterResponseDTO("User successfully created at " + LocalDateTime.now(), null);
    }

    private Boolean isUsernameTaken(String username) {
        return userRepo.findByUsername(username).isPresent();
    }
}
