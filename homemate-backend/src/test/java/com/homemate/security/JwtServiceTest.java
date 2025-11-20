package com.homemate.security;

import com.homemate.security.service.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void testGenerateToken_NotNullAndContainsUserId() {
        String token = jwtService.generateToken(123L);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Long extractedId = jwtService.extractUserId(token);
        assertEquals(123L, extractedId);
    }

    @Test
    void testExtractUserId_CorrectValue() {
        String token = jwtService.generateToken(55L);

        Long userId = jwtService.extractUserId(token);

        assertEquals(55L, userId);
    }

    @Test
    void testIsTokenValid_ValidToken() {
        String token = jwtService.generateToken(99L);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testIsTokenValid_InvalidSignature() {
        String validToken = jwtService.generateToken(77L);

        String tampered = validToken + "abcd";

        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void testIsTokenValid_ExpiredToken() throws InterruptedException {
        // Create a custom JwtService with tiny expiry by subclassing
        JwtService tinyExpiryJwt = new JwtService() {
            private static final long serialVersionUID = 1L;
            @Override
            public String generateToken(Long userId) {
                return io.jsonwebtoken.Jwts.builder()
                        .claim("id", userId)
                        .setIssuedAt(new java.util.Date())
                        .setExpiration(new java.util.Date(System.currentTimeMillis() + 1)) // expires immediately
                        .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256,
                                "NOT_SECRET_KEY_123456789012345678".getBytes())
                        .compact();
            }
        };

        String token = tinyExpiryJwt.generateToken(5L);

        Thread.sleep(2); // Ensure expiration happens

        assertFalse(tinyExpiryJwt.isTokenValid(token));
    }
}
