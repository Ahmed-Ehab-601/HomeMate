package com.homemate.security.filter;

// Change import from io.jsonwebtoken.io.IOException to java.io.IOException
import java.io.IOException;

import com.homemate.security.service.AppUserDetailsService;
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
            "/api/login", "/api/signup"
    );

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    JwtAuthFilter(JwtService jwtService, AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        for (String p : PUBLIC_URLS) {
            if (path.equals(p)) {
                try {
                    filterChain.doFilter(request, response);
                } catch (ServletException | IOException e) {
                    System.err.println("Exception thrown during filterChain.doFilter for public path: " + e.getMessage());
                    throw e;
                }
                return;
            }
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write("Missing or invalid Authorization header");
            } catch (IOException e) {
                System.err.println("IOException trying to write response for missing header: " + e.getMessage());
                throw e;
            }
            return;
        }

        String jwt = authHeader.substring(7);

        if (!jwtService.isTokenValid(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write("Invalid or expired token");
            } catch (IOException e) {
                System.err.println("IOException trying to write response for invalid token: " + e.getMessage());
            }
            return;
        }

        Long userId = jwtService.extractUserId(jwt);

        // Fetch user details
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserById(userId);
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write("Invalid or expired token");
            } catch (IOException ioException) {
                System.err.println("IOException trying to write response for user details error: " + ioException.getMessage());
            }
            return;
        }

        // Create an Authentication object
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // Set the Authentication object in the Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException e) {
            System.err.println("Exception thrown during filterChain.doFilter after successful authentication: " + e.getMessage());
            throw e;
        }
    }
}