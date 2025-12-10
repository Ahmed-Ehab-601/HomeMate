package com.homemate.security.filter;

// Change import from io.jsonwebtoken.io.IOException to java.io.IOException
import java.io.IOException;

import com.homemate.security.model.AppUserDetails;
import com.homemate.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_URLS = List.of(
            "/api/auth/login",
            "/api/auth/google",
            "/api/auth/otp/send",
            "/api/auth/otp/verify",
            "/api/auth/signup/google/init",
            "/api/user/signup",
            "/api/tasker/signup",
            "/api/services",
            "/api/taskers/search",
            "/api/users/taskers/",
            "/api/tasker-profile/reviews"
    );

    private final JwtService jwtService;

    JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        String authHeader = request.getHeader("Authorization");

        // 1. Attempt Authentication if header is present
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String jwt = authHeader.substring(7);
                UserDetails userDetails = jwtService.extractUserDetails(jwt);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token extraction failed (expired, invalid, etc.)
                // We proceed without authentication. If the URL is protected, Spring Security will handle the 403.
                System.err.println("JWT Authentication warning: " + e.getMessage());
            }
        }

        // 2. Check if we are authenticated or if the URL is public
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        boolean isPublic = false;

        for (String p : PUBLIC_URLS) {
            if (path.equals(p) || path.startsWith(p)) {
                isPublic = true;
                break;
            }
        }

        if (isAuthenticated || isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Neither authenticated nor public -> 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Missing or invalid Authorization header");
    }
}