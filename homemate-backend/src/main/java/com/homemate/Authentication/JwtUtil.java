package com.homemate.Authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUtil {

    private static final String SECRET = "NOT_SECRET_KEY_1234567890";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    // Create JWT with only the userId inside
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())   // optional but good
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                .compact();
    }

    // Extract the userId
    public Long extractUserId(String jwt) {
        return Long.parseLong(extractAllClaims(jwt).get("id").toString());
    }

    // Validate token (signature + expiration)
    public boolean isTokenValid(String jwt) {
        try {
            Claims claims = extractAllClaims(jwt);
            return !isExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
