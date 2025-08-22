package com.userreport.UserReportBackend.filter;

import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.JWTservice;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;


import java.io.IOException;


@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTservice jwtservice;
    private final UserRepo userRepo;

    public JWTFilter(JWTservice jwtservice, UserRepo userRepo) {
        this.jwtservice = jwtservice;
        this.userRepo = userRepo;
    }


    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt_token = authorization.substring(7);
        String userName = jwtservice.getUserName(jwt_token);

        if (userName == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserEntity userData = userRepo.findByUsername(userName).orElse(null);

        if (userData == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Create user details with authorities
        String authority = "ROLE_" + userData.getRole().name();
        System.out.println("Creating user details for: " + userData.getUsername() + " with authority: " + authority);

        UserDetails user = User.builder()
                .username(userData.getUsername())
                .password(userData.getPassword())
                .authorities(authority)
                .build();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(token);

        System.out.println("JWT Token processed for user: " + userName);
        filterChain.doFilter(request, response);
    }
}
