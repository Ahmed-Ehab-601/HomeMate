package com.homemate.chat.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Token Provider - Handles token validation and parsing
 * NO @Value annotations - uses hardcoded secret for simplicity
 */
@Component
public class JwtTokenProvider {

    // Hardcoded secret - CHANGE THIS in production!
    // This should match the secret used by your authentication service
    private final String jwtSecret;
    private final long jwtExpiration = 86400000; // 24 hours

    /**
     * Constructor - generates a consistent secret key
     * In production, this should come from configuration/environment
     */
    public JwtTokenProvider() {
        // Use a fixed secret for development (CHANGE IN PRODUCTION!)
        this.jwtSecret = "YourSuperSecretKeyThatShouldBeAtLeast256BitsLongForHS256AlgorithmAndShouldBeKeptSecure";

        // Alternatively, generate a random secret (but this changes on restart!)
        // SecureRandom random = new SecureRandom();
        // byte[] bytes = new byte[64];
        // random.nextBytes(bytes);
        // this.jwtSecret = Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Get the signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Get user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Try different claim names for user ID
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            userIdObj = claims.get("id");
        }
        if (userIdObj == null) {
            userIdObj = claims.get("user_id");
        }

        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get Authentication object from token
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    /**
     * Extract authorities/roles from claims
     */
    private Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        // Try different claim names for roles
        Object rolesObj = claims.get("roles");
        if (rolesObj == null) {
            rolesObj = claims.get("authorities");
        }
        if (rolesObj == null) {
            rolesObj = claims.get("role");
        }
        if (rolesObj == null) {
            rolesObj = claims.get("authority");
        }

        if (rolesObj != null) {
            if (rolesObj instanceof String) {
                String role = (String) rolesObj;
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }
                return Collections.singletonList(new SimpleGrantedAuthority(role));
            } else if (rolesObj instanceof Collection) {
                return ((Collection<?>) rolesObj).stream()
                        .map(role -> {
                            String roleStr = role.toString();
                            if (!roleStr.startsWith("ROLE_")) {
                                roleStr = "ROLE_" + roleStr;
                            }
                            return new SimpleGrantedAuthority(roleStr);
                        })
                        .collect(Collectors.toList());
            }
        }

        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Get all claims from token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Generate new JWT token
     */
    public String generateToken(String username, Long userId, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}