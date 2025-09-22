package com.userreport.UserReportBackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    private String name;

    private String email;

    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "branch_id")
    private Long branchId;

    // Constructor for creating a new user
    public UserEntity(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = email.endsWith("@admin.com") ? Role.ADMIN :
                (email.matches(".*@\\w+\\.user\\.com$") ? Role.USER : Role.USER);    }

    // Constructor for creating a user with specific role
    public UserEntity(String name, String email, String username, String password, Role role) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Constructor for creating a user with specific role and branch
    public UserEntity(String name, String email, String username, String password, Role role, Long branchId) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.branchId = branchId;
    }
}
