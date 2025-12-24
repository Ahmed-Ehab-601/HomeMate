package com.homemate.hashing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HashingServiceTest {

    @InjectMocks
    private HashingService underTest;

    @Test
    void testThatCorrectPasswordIsVerifiedSuccessfully() {
        // Given
        String plainPassword = "Ahmed$601";

        // When
        String hashedPassword = underTest.hashPassword(plainPassword);
        boolean isVerified = underTest.verifyPassword(plainPassword, hashedPassword);

        // Then
        Assertions.assertNotNull(hashedPassword);
        Assertions.assertNotEquals(plainPassword, hashedPassword);
        Assertions.assertTrue(isVerified);
    }

    @Test
    void testThatWrongPasswordFailsVerification() {
        // Given
        String correctPassword = "Ahmed$601";
        String wrongPassword = "WrongPassword123";

        // When
        String hashedPassword = underTest.hashPassword(correctPassword);
        boolean isVerified = underTest.verifyPassword(wrongPassword, hashedPassword);

        // Then
        Assertions.assertFalse(isVerified);
    }

    @Test
    void testThatPasswordLongerThan72BytesThrowsException() {
        // Given - Create a string longer than 72 bytes
        // BCrypt has a maximum password length of 72 bytes
        String veryLongPassword = "a".repeat(100);

        // Then - Expect IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            // When
            underTest.hashPassword(veryLongPassword);
        });
    }
}