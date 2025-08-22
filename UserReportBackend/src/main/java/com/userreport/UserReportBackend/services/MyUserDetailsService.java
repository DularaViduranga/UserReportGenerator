package com.userreport.UserReportBackend.services;


import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.UserRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MyUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;

    public MyUserDetailsService( UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userData = userRepo.findByUsername(username).orElse(null);

        if (userData == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        UserDetails user = User.builder()
                .username(userData.getUsername())
                .password(userData.getPassword())
                .authorities("ROLE_" + userData.getRole().name())
                .build();

        return user;
    }
}
