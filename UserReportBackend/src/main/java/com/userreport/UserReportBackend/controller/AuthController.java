package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.user.LoginRequestDTO;
import com.userreport.UserReportBackend.dto.user.LoginResponseDTO;
import com.userreport.UserReportBackend.dto.user.RegisterRequestDTO;
import com.userreport.UserReportBackend.dto.user.RegisterResponseDTO;
import com.userreport.UserReportBackend.entity.Role;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO){
        LoginResponseDTO res = authService.login(loginRequestDTO);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO){
        RegisterResponseDTO res = authService.register(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(){
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("Current user: " + auth.getName());
            System.out.println("Authorities: " + auth.getAuthorities());
            return ResponseEntity.ok(Map.of(
                    "username", auth.getName(),
                    "authorities", auth.getAuthorities().stream().map(Object::toString).toList()
            ));
        }
        return ResponseEntity.ok("No authenticated user");
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsersAdmin(){
        System.out.println("Admin users endpoint called");
        List<UserEntity> users = authService.getAllUsers();
        System.out.println("Returning " + users.size() + " users to admin");
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegisterResponseDTO> createAdminUser(@RequestBody RegisterRequestDTO registerRequestDTO){
        RegisterResponseDTO res = authService.createAdminUser(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }


    @DeleteMapping("/admin/delete-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId){
        authService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/admin/update-role/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestParam Role role){
        authService.updateUserRole(userId, role);
        return ResponseEntity.ok("User role updated successfully");
    }
}
