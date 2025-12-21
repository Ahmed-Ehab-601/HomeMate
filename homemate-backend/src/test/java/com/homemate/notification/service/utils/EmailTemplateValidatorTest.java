package com.homemate.notification.service.utils;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.model.RecipientType;
import com.homemate.taskmanagement.dto.TaskDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateValidatorTest {

    private static final String TEST_OPERATION = "TEST_OPERATION";
    private static final String TEST_FIELD = "testField";
    private static final String VALID_VALUE = "validValue";

    
    // validateTaskDto Tests
    

    @Test
    void testValidateTaskDtoWithNullTaskShouldThrowException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> EmailTemplateValidator.validateTaskDto(null, TEST_OPERATION)
        );

        assertEquals("TaskDto cannot be null for " + TEST_OPERATION, exception.getMessage());
        assertEquals(TEST_OPERATION, exception.getTemplateType());
        assertEquals("taskDto", exception.getMissingField());
        assertNotNull(exception.getTemplateType());
        assertNotNull(exception.getMissingField());
    }

    @Test
    void testValidateTaskDtoWithValidTaskShouldNotThrow() {
        TaskDto validTask = TaskDto.builder()
                .taskID(1L)
                .build();

        assertDoesNotThrow(
                () -> EmailTemplateValidator.validateTaskDto(validTask, TEST_OPERATION)
        );
    }

    @Test
    void testValidateTaskDtoExceptionFieldsAreAccessible() {
        try {
            EmailTemplateValidator.validateTaskDto(null, "buildSubject");
            fail("Should have thrown EmailTemplateException");
        } catch (EmailTemplateException exception) {
            // Verify getTemplateType() and getMissingField() work
            String templateType = exception.getTemplateType();
            String missingField = exception.getMissingField();

            assertNotNull(templateType);
            assertNotNull(missingField);
            assertEquals("buildSubject", templateType);
            assertEquals("taskDto", missingField);
        }
    }

    @Test
    void testValidateTaskDtoWithDifferentOperationsShouldPreserveOperationInException() {
        String[] operations = {"buildSubject", "buildBody", "sendEmail"};

        for (String operation : operations) {
            try {
                EmailTemplateValidator.validateTaskDto(null, operation);
                fail("Should throw exception for operation: " + operation);
            } catch (EmailTemplateException exception) {
                assertEquals(operation, exception.getTemplateType());
                assertEquals("taskDto", exception.getMissingField());
            }
        }
    }

    
    // validateEmailRequest Tests
    

    @Test
    void testValidateEmailRequestWithNullRequestShouldThrowException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> EmailTemplateValidator.validateEmailRequest(null)
        );

        assertEquals("EmailRequest cannot be null", exception.getMessage());
        assertEquals("EMAIL_REQUEST", exception.getTemplateType());
        assertEquals("emailRequest", exception.getMissingField());
        assertNotNull(exception.getTemplateType());
        assertNotNull(exception.getMissingField());
    }

    @Test
    void testValidateEmailRequestWithNullEmailTypeShouldThrowException() {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail("test@example.com")
                .emailType(null)
                .build();

        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> EmailTemplateValidator.validateEmailRequest(emailRequest)
        );

        assertEquals("Email type cannot be null", exception.getMessage());
        assertEquals("EMAIL_REQUEST", exception.getTemplateType());
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testValidateEmailRequestExceptionFieldsAreAccessible() {
        try {
            EmailTemplateValidator.validateEmailRequest(null);
            fail("Should have thrown EmailTemplateException");
        } catch (EmailTemplateException exception) {
            String templateType = exception.getTemplateType();
            String missingField = exception.getMissingField();

            assertNotNull(templateType);
            assertNotNull(missingField);
            assertEquals("EMAIL_REQUEST", templateType);
            assertEquals("emailRequest", missingField);
        }
    }

    @Test
    void testValidateEmailRequestWithNullEmailTypeExceptionFields() {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail("test@example.com")
                .emailType(null)
                .build();

        try {
            EmailTemplateValidator.validateEmailRequest(emailRequest);
            fail("Should have thrown EmailTemplateException");
        } catch (EmailTemplateException exception) {
            String templateType = exception.getTemplateType();
            String missingField = exception.getMissingField();

            assertNotNull(templateType);
            assertNotNull(missingField);
            assertEquals("EMAIL_REQUEST", templateType);
            assertEquals("emailType", missingField);
        }
    }

    @Test
    void testValidateEmailRequestWithValidRequestShouldNotThrow() {
        EmailRequest validRequest = EmailRequest.builder()
                .recipientEmail("test@example.com")
                .emailType(EmailType.TASK_STATUS)
                .recipientType(RecipientType.USER)
                .build();

        assertDoesNotThrow(
                () -> EmailTemplateValidator.validateEmailRequest(validRequest)
        );
    }

    
    // requireNonNull Tests
    

    @Test
    void testRequireNonNullWithNullValueShouldThrowException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> EmailTemplateValidator.requireNonNull(null, TEST_FIELD, TEST_OPERATION)
        );

        String expectedMessage = String.format(
                "Required field '%s' is null or empty for %s",
                TEST_FIELD,
                TEST_OPERATION
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_OPERATION, exception.getTemplateType());
        assertEquals(TEST_FIELD, exception.getMissingField());
        assertNotNull(exception.getTemplateType());
        assertNotNull(exception.getMissingField());
    }

    @Test
    void testRequireNonNullWithEmptyStringShouldThrowException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> EmailTemplateValidator.requireNonNull("", TEST_FIELD, TEST_OPERATION)
        );

        String expectedMessage = String.format(
                "Required field '%s' is null or empty for %s",
                TEST_FIELD,
                TEST_OPERATION
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_OPERATION, exception.getTemplateType());
        assertEquals(TEST_FIELD, exception.getMissingField());
    }

    @Test
    void testRequireNonNullWithWhitespaceOnlyStringShouldThrowException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> EmailTemplateValidator.requireNonNull("   ", TEST_FIELD, TEST_OPERATION)
        );

        String expectedMessage = String.format(
                "Required field '%s' is null or empty for %s",
                TEST_FIELD,
                TEST_OPERATION
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_OPERATION, exception.getTemplateType());
        assertEquals(TEST_FIELD, exception.getMissingField());
    }

    @Test
    void testRequireNonNullExceptionFieldsAreAccessible() {
        try {
            EmailTemplateValidator.requireNonNull(null, "emailField", "sendOperation");
            fail("Should have thrown EmailTemplateException");
        } catch (EmailTemplateException exception) {
            String templateType = exception.getTemplateType();
            String missingField = exception.getMissingField();

            assertNotNull(templateType);
            assertNotNull(missingField);
            assertEquals("sendOperation", templateType);
            assertEquals("emailField", missingField);
        }
    }

    @Test
    void testRequireNonNullWithTabsAndSpacesShouldThrowException() {
        try {
            EmailTemplateValidator.requireNonNull("\t\n  ", TEST_FIELD, TEST_OPERATION);
            fail("Should have thrown EmailTemplateException");
        } catch (EmailTemplateException exception) {
            assertTrue(exception.getMessage().contains("null or empty"));
            assertEquals(TEST_OPERATION, exception.getTemplateType());
            assertEquals(TEST_FIELD, exception.getMissingField());
        }
    }

    @Test
    void testRequireNonNullWithValidValueShouldReturnValue() {
        String result = EmailTemplateValidator.requireNonNull(
                VALID_VALUE,
                TEST_FIELD,
                TEST_OPERATION
        );

        assertEquals(VALID_VALUE, result);
    }

    @Test
    void testRequireNonNullWithValueContainingSpacesShouldReturnValue() {
        String valueWithSpaces = "valid value with spaces";

        String result = EmailTemplateValidator.requireNonNull(
                valueWithSpaces,
                TEST_FIELD,
                TEST_OPERATION
        );

        assertEquals(valueWithSpaces, result);
    }

    @Test
    void testRequireNonNullWithDifferentFieldNamesPreservesFieldInException() {
        String[] fieldNames = {"email", "username", "taskId"};

        for (String fieldName : fieldNames) {
            try {
                EmailTemplateValidator.requireNonNull(null, fieldName, TEST_OPERATION);
                fail("Should throw exception for field: " + fieldName);
            } catch (EmailTemplateException exception) {
                assertTrue(exception.getMessage().contains(fieldName));
                assertEquals(fieldName, exception.getMissingField());
                assertEquals(TEST_OPERATION, exception.getTemplateType());
            }
        }
    }

    @Test
    void testRequireNonNullWithDifferentOperationsPreservesOperationInException() {
        String[] operations = {"buildEmail", "validateInput", "processRequest"};

        for (String operation : operations) {
            try {
                EmailTemplateValidator.requireNonNull(null, TEST_FIELD, operation);
                fail("Should throw exception for operation: " + operation);
            } catch (EmailTemplateException exception) {
                assertTrue(exception.getMessage().contains(operation));
                assertEquals(operation, exception.getTemplateType());
                assertEquals(TEST_FIELD, exception.getMissingField());
            }
        }
    }

    
    // Additional Edge Cases
    

    @Test
    void testRequireNonNullWithSingleCharacterShouldReturnValue() {
        String singleChar = "a";
        String result = EmailTemplateValidator.requireNonNull(singleChar, TEST_FIELD, TEST_OPERATION);
        assertEquals(singleChar, result);
    }

    @Test
    void testRequireNonNullWithSpecialCharactersShouldReturnValue() {
        String specialValue = "test@#$%^&*()";
        String result = EmailTemplateValidator.requireNonNull(specialValue, TEST_FIELD, TEST_OPERATION);
        assertEquals(specialValue, result);
    }

    @Test
    void testAllValidationMethodsThrowEmailTemplateExceptionWithAccessibleFields() {
        // Test validateTaskDto
        try {
            EmailTemplateValidator.validateTaskDto(null, "op1");
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
        }

        // Test validateEmailRequest
        try {
            EmailTemplateValidator.validateEmailRequest(null);
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
        }

        // Test requireNonNull
        try {
            EmailTemplateValidator.requireNonNull(null, "field", "op");
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
        }
    }

    @Test
    void testExceptionMessageFormattingIsConsistent() {
        // Test message format for requireNonNull
        try {
            EmailTemplateValidator.requireNonNull(null, "testField", "testOp");
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertTrue(e.getMessage().contains("testField"));
            assertTrue(e.getMessage().contains("testOp"));
            assertTrue(e.getMessage().contains("null or empty"));
        }

        // Test message format for validateTaskDto
        try {
            EmailTemplateValidator.validateTaskDto(null, "testOp2");
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertTrue(e.getMessage().contains("TaskDto"));
            assertTrue(e.getMessage().contains("testOp2"));
        }
    }

    @Test
    void testValidateTaskDtoWithComplexTaskObjectShouldNotThrow() {
        TaskDto complexTask = TaskDto.builder()
                .taskID(100L)
                .description("Complex Task")
                .build();

        assertDoesNotThrow(() -> EmailTemplateValidator.validateTaskDto(complexTask, TEST_OPERATION));
    }

    @Test
    void testValidateEmailRequestWithAllEmailTypesShouldNotThrow() {
        EmailType[] allTypes = EmailType.values();

        for (EmailType emailType : allTypes) {
            EmailRequest emailRequest = EmailRequest.builder()
                    .recipientEmail("test@example.com")
                    .emailType(emailType)
                    .build();

            assertDoesNotThrow(() -> EmailTemplateValidator.validateEmailRequest(emailRequest));
        }
    }

    @Test
    void testRequireNonNullReturnsSameReferenceForValidInput() {
        String testValue = "test@example.com";
        String result = EmailTemplateValidator.requireNonNull(testValue, "email", "sendEmail");
        assertSame(testValue, result);
    }

    
    // Constructor Tests for EmailTemplateException
    

    @Test
    void testEmailTemplateExceptionTwoArgumentConstructor() {
        try {
            throw new EmailTemplateException("Test message", "TEST_TYPE");
        } catch (EmailTemplateException e) {
            assertEquals("Test message", e.getMessage());
            assertEquals("TEST_TYPE", e.getTemplateType());
            assertNull(e.getMissingField());
        }
    }

    @Test
    void testEmailTemplateExceptionThreeArgumentConstructor() {
        try {
            throw new EmailTemplateException("Test message", "TEST_TYPE", "testField");
        } catch (EmailTemplateException e) {
            assertEquals("Test message", e.getMessage());
            assertEquals("TEST_TYPE", e.getTemplateType());
            assertEquals("testField", e.getMissingField());
        }
    }

    @Test
    void testValidateTaskDtoUsesThreeArgumentConstructor() {
        try {
            EmailTemplateValidator.validateTaskDto(null, "operation");
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getMessage());
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
        }
    }

    @Test
    void testValidateEmailRequestUsesThreeArgumentConstructor() {
        try {
            EmailTemplateValidator.validateEmailRequest(null);
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getMessage());
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
        }
    }

    @Test
    void testRequireNonNullUsesThreeArgumentConstructor() {
        try {
            EmailTemplateValidator.requireNonNull(null, "field", "operation");
            fail("Should throw exception");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getMessage());
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
        }
    }
}