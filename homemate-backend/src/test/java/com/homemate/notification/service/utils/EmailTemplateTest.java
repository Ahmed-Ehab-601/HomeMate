package com.homemate.notification.service.utils;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.model.RecipientType;
import com.homemate.notification.service.utils.EmailTemplate.OtpEmailContent;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateTest {

    private EmailTemplate underTest;
    private static final String RECIPIENT_EMAIL = "test@example.com";
    private static final String OTP_CODE = "123456";

    // Complete TaskDto with all required fields
    private TaskDto completeTaskDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        underTest = new EmailTemplate();
        now = LocalDateTime.now();

        // Create a fully populated TaskDto that can be used across all tests
        completeTaskDto = TaskDto.builder()
                .taskID(1L)
                .taskerID(100L)
                .startDate(now)
                .endDate(now.plusHours(2))
                .status(Status.Accepted)
                .description("Complete home cleaning service")
                .workedHours(2.5)
                .startInProgress(now)
                .bill(150.0)
                .userName("John Doe")
                .taskerName("Jane Smith")
                .serviceName("Home Cleaning")
                .chatID(500L)
                .hourRate(50.0)
                .addressDetails("123 Main St, Cityville")
                .userMail("user@example.com")
                .taskerMail("tasker@example.com")
                .build();
    }


    // Helper Methods for Test Data


    private EmailRequest createEmailRequest(EmailType emailType, RecipientType recipientType) {
        return createEmailRequest(emailType, recipientType, completeTaskDto);
    }

    private EmailRequest createEmailRequest(EmailType emailType, RecipientType recipientType, TaskDto taskDto) {
        return EmailRequest.builder()
                .task(taskDto)
                .emailType(emailType)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(recipientType)
                .build();
    }

    private TaskDto createMinimalValidTaskDto() {
        return TaskDto.builder()
                .taskID(1L)
                .serviceName("Test Service")
                .userName("Test User")
                .taskerName("Test Tasker")
                .addressDetails("Test Address")
                .userMail("user@test.com")
                .taskerMail("tasker@test.com")
                .build();
    }

    private TaskDto createTaskDtoWithStatus(Status status) {
        return TaskDto.builder()
                .taskID(1L)
                .serviceName("Test Service")
                .userName("Test User")
                .taskerName("Test Tasker")
                .addressDetails("Test Address")
                .userMail("user@test.com")
                .taskerMail("tasker@test.com")
                .status(status)
                .startDate(now)
                .endDate(now.plusHours(1))
                .build();
    }


    // buildEmailSubject Tests


    @Test
    void testBuildEmailSubjectWithNullEmailRequest() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildEmailSubject(null)
        );

        assertEquals("EmailRequest cannot be null", exception.getMessage());
        assertEquals("emailRequest", exception.getMissingField());
    }

    @Test
    void testBuildEmailSubjectWithNullEmailType() {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(RECIPIENT_EMAIL)
                .emailType(null)
                .build();

        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildEmailSubject(emailRequest)
        );

        assertEquals("Email type cannot be null", exception.getMessage());
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testBuildEmailSubjectWithTaskStatusEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_STATUS,
                RecipientType.USER
        );

        String subject = underTest.buildEmailSubject(emailRequest);

        assertNotNull(subject);
        assertFalse(subject.isEmpty());
        assertTrue(subject.contains("Task") || subject.contains("Status"));
    }

    @Test
    void testBuildEmailSubjectWithTaskRequestEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_REQUEST,
                RecipientType.TASKER
        );

        String subject = underTest.buildEmailSubject(emailRequest);

        assertNotNull(subject);
        assertFalse(subject.isEmpty());
    }

    @Test
    void testBuildEmailSubjectWithTaskRescheduleEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_RESCHEDULE,
                RecipientType.USER
        );

        String subject = underTest.buildEmailSubject(emailRequest);

        assertNotNull(subject);
        assertFalse(subject.isEmpty());
        assertTrue(subject.contains("Reschedule") || subject.contains("Schedule"));
    }

    @Test
    void testBuildEmailSubjectWithTaskResumedEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_RESUMED,
                RecipientType.USER
        );

        String subject = underTest.buildEmailSubject(emailRequest);

        assertNotNull(subject);
        assertFalse(subject.isEmpty());
    }

    @Test
    void testBuildEmailSubjectWithEmailVerificationEmailType() {
        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.EMAIL_VERIFICATION)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        String subject = underTest.buildEmailSubject(emailRequest);

        assertNotNull(subject);
        assertFalse(subject.isEmpty());
        assertTrue(subject.contains("Verify") || subject.contains("Email"));
    }

    @Test
    void testBuildEmailSubjectWithForgotPasswordEmailType() {
        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.FORGOT_PASSWORD)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        String subject = underTest.buildEmailSubject(emailRequest);

        assertNotNull(subject);
        assertFalse(subject.isEmpty());
        assertTrue(subject.contains("Password") || subject.contains("Reset"));
    }


    // buildEmailBody Tests


    @Test
    void testBuildEmailBodyWithNullEmailRequest() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildEmailBody(null)
        );

        assertEquals("EmailRequest cannot be null", exception.getMessage());
        assertEquals("emailRequest", exception.getMissingField());
    }

    @Test
    void testBuildEmailBodyWithNullEmailType() {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(RECIPIENT_EMAIL)
                .emailType(null)
                .build();

        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildEmailBody(emailRequest)
        );

        assertEquals("Email type cannot be null", exception.getMessage());
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testBuildEmailBodyWithTaskStatusEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_STATUS,
                RecipientType.USER
        );

        String body = underTest.buildEmailBody(emailRequest);

        assertNotNull(body);
        assertFalse(body.isEmpty());
        // Check if body contains important task information
        assertTrue(body.contains(completeTaskDto.getServiceName()) ||
                body.contains(completeTaskDto.getUserName()) ||
                body.contains(completeTaskDto.getTaskerName()));
    }

    @Test
    void testBuildEmailBodyWithTaskStatusDifferentStatuses() {
        // Get all statuses and filter out the unsupported ones
        Status[] allStatuses = Status.values();
        List<Status> supportedStatuses = new ArrayList<>();
        for (Status status : allStatuses) {
            if (status != Status.InReview) { // Assuming InReview is the only unsupported
                supportedStatuses.add(status);
            }
        }

        for (Status status : supportedStatuses) {
            TaskDto taskWithStatus = createTaskDtoWithStatus(status);
            EmailRequest emailRequest = createEmailRequest(
                    EmailType.TASK_STATUS,
                    RecipientType.USER,
                    taskWithStatus
            );

            assertDoesNotThrow(() -> {
                String body = underTest.buildEmailBody(emailRequest);
                assertNotNull(body);
                assertFalse(body.isEmpty());
            });
        }
    }

    @Test
    void testBuildEmailBodyWithTaskRescheduleForTasker() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_RESCHEDULE,
                RecipientType.TASKER
        );

        String body = underTest.buildEmailBody(emailRequest);

        assertNotNull(body);
        assertFalse(body.isEmpty());
        // Should contain tasker-specific information
        assertTrue(body.contains(completeTaskDto.getUserName()) ||
                body.contains(completeTaskDto.getServiceName()));
    }

    @Test
    void testBuildEmailBodyWithTaskRescheduleForUser() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_RESCHEDULE,
                RecipientType.USER
        );

        String body = underTest.buildEmailBody(emailRequest);

        assertNotNull(body);
        assertFalse(body.isEmpty());
        // Should contain user-specific information
        assertTrue(body.contains(completeTaskDto.getTaskerName()) ||
                body.contains(completeTaskDto.getServiceName()));
    }

    @Test
    void testBuildEmailBodyWithTaskRequestEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_REQUEST,
                RecipientType.TASKER
        );

        String body = underTest.buildEmailBody(emailRequest);

        assertNotNull(body);
        assertFalse(body.isEmpty());
        assertTrue(body.contains(completeTaskDto.getServiceName()) ||
                body.contains(completeTaskDto.getUserName()));
    }

    @Test
    void testBuildEmailBodyWithTaskResumedEmailType() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_RESUMED,
                RecipientType.USER
        );

        String body = underTest.buildEmailBody(emailRequest);

        assertNotNull(body);
        assertFalse(body.isEmpty());
    }

    @Test
    void testBuildEmailBodyWithEmailVerificationEmailType() {
        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.EMAIL_VERIFICATION)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> underTest.buildEmailBody(emailRequest));
    }

    @Test
    void testBuildEmailBodyWithForgotPasswordEmailType() {
        EmailRequest emailRequest = EmailRequest.builder()
                .emailType(EmailType.FORGOT_PASSWORD)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        assertThrows(UnsupportedOperationException.class, () -> underTest.buildEmailBody(emailRequest));
    }

    // buildOtpEmailContent Tests

    @Test
    void testBuildOtpEmailContentWithEmailVerification() {
        OtpEmailContent content = underTest.buildOtpEmailContent(
                EmailType.EMAIL_VERIFICATION,
                OTP_CODE
        );

        assertNotNull(content);
        assertNotNull(content.subject());
        assertNotNull(content.body());
        assertFalse(content.subject().isEmpty());
        assertFalse(content.body().isEmpty());
        assertTrue(content.body().contains(OTP_CODE));
        // Verify subject is appropriate
        assertTrue(content.subject().contains("Verify") || content.subject().contains("Email"));
    }

    @Test
    void testBuildOtpEmailContentWithForgotPassword() {
        OtpEmailContent content = underTest.buildOtpEmailContent(
                EmailType.FORGOT_PASSWORD,
                OTP_CODE
        );

        assertNotNull(content);
        assertNotNull(content.subject());
        assertNotNull(content.body());
        assertFalse(content.subject().isEmpty());
        assertFalse(content.body().isEmpty());
        assertTrue(content.body().contains(OTP_CODE));
        // Verify subject is appropriate
        assertTrue(content.subject().contains("Password") || content.subject().contains("Reset"));
    }

    @Test
    void testBuildOtpEmailContentWithTaskStatusThrowsException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildOtpEmailContent(EmailType.TASK_STATUS, OTP_CODE)
        );

        assertTrue(exception.getMessage().contains("does not support OTP codes"));
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testBuildOtpEmailContentWithTaskRequestThrowsException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildOtpEmailContent(EmailType.TASK_REQUEST, OTP_CODE)
        );

        assertTrue(exception.getMessage().contains("does not support OTP codes"));
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testBuildOtpEmailContentWithTaskRescheduleThrowsException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildOtpEmailContent(EmailType.TASK_RESCHEDULE, OTP_CODE)
        );

        assertTrue(exception.getMessage().contains("does not support OTP codes"));
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testBuildOtpEmailContentWithTaskResumedThrowsException() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildOtpEmailContent(EmailType.TASK_RESUMED, OTP_CODE)
        );

        assertTrue(exception.getMessage().contains("does not support OTP codes"));
        assertEquals("emailType", exception.getMissingField());
    }

    @Test
    void testBuildOtpEmailContentWithDifferentOtpCode() {
        String customOtp = "987654";

        OtpEmailContent content = underTest.buildOtpEmailContent(
                EmailType.EMAIL_VERIFICATION,
                customOtp
        );

        assertNotNull(content);
        assertTrue(content.body().contains(customOtp));
        assertFalse(content.body().contains(OTP_CODE));
    }


    // Edge Cases and Integration Tests


    @Test
    void testBuildEmailSubjectAndBodyConsistency() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_STATUS,
                RecipientType.USER
        );

        String subject = underTest.buildEmailSubject(emailRequest);
        String body = underTest.buildEmailBody(emailRequest);

        assertNotNull(subject);
        assertNotNull(body);
        assertFalse(subject.isEmpty());
        assertFalse(body.isEmpty());

        // Both should reference the same service/task
        assertTrue(subject.contains(completeTaskDto.getServiceName()) ||
                body.contains(completeTaskDto.getServiceName()));
    }

    @Test
    void testOtpEmailContentRecordGetters() {
        OtpEmailContent content = new OtpEmailContent("Test Subject", "Test Body");

        assertEquals("Test Subject", content.subject());
        assertEquals("Test Body", content.body());
    }

    @Test
    void testBuildOtpEmailContentWithEmptyOtpCode() {
        assertThrows(IllegalArgumentException.class, () ->
                underTest.buildOtpEmailContent(EmailType.EMAIL_VERIFICATION, "")
        );
    }

    @Test
    void testBuildEmailBodyWithNullTaskForNonTaskEmail() {
        // Use a task email type, but with null task, to see if it throws
        EmailRequest emailRequest = EmailRequest.builder()
                .task(null)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        // This should throw because task is null for a task email type
        assertThrows(EmailTemplateException.class, () -> underTest.buildEmailBody(emailRequest));
    }

    @Test
    void testBuildEmailBodyWithNullTaskForTaskEmailThrowsException() {
        EmailRequest emailRequest = EmailRequest.builder()
                .task(null)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        assertThrows(EmailTemplateException.class, () -> underTest.buildEmailBody(emailRequest));
    }

    @Test
    void testMultipleCallsWithSameEmailRequest() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_STATUS,
                RecipientType.USER
        );

        String subject1 = underTest.buildEmailSubject(emailRequest);
        String subject2 = underTest.buildEmailSubject(emailRequest);
        String body1 = underTest.buildEmailBody(emailRequest);
        String body2 = underTest.buildEmailBody(emailRequest);

        assertEquals(subject1, subject2);
        assertEquals(body1, body2);
    }

    @Test
    void testAllEmailTypesCanBuildSubject() {
        EmailType[] allTypes = EmailType.values();

        for (EmailType emailType : allTypes) {
            EmailRequest emailRequest;

            if (emailType == EmailType.EMAIL_VERIFICATION ||
                    emailType == EmailType.FORGOT_PASSWORD) {
                // These don't require a TaskDto
                emailRequest = EmailRequest.builder()
                        .emailType(emailType)
                        .recipientEmail(RECIPIENT_EMAIL)
                        .build();
            } else {
                // Task-related emails need a TaskDto
                emailRequest = createEmailRequest(emailType, RecipientType.USER);
            }

            assertDoesNotThrow(() -> {
                String subject = underTest.buildEmailSubject(emailRequest);
                assertNotNull(subject);
                assertFalse(subject.isEmpty());
            }, "Failed for email type: " + emailType);
        }
    }

    @Test
    void testAllEmailTypesCanBuildBody() {
        EmailType[] allTypes = EmailType.values();

        for (EmailType emailType : allTypes) {
            // Skip the OTP email types because they require a code to build the body
            if (emailType == EmailType.EMAIL_VERIFICATION || emailType == EmailType.FORGOT_PASSWORD) {
                continue;
            }

            EmailRequest emailRequest;

            if (emailType == EmailType.TASK_STATUS || emailType == EmailType.TASK_REQUEST ||
                    emailType == EmailType.TASK_RESCHEDULE || emailType == EmailType.TASK_RESUMED) {
                // Task-related emails need a TaskDto
                emailRequest = createEmailRequest(emailType, RecipientType.USER);
            } else {
                // Other non-OTP, non-task email types (if any) can be built without a task
                emailRequest = EmailRequest.builder()
                        .emailType(emailType)
                        .recipientEmail(RECIPIENT_EMAIL)
                        .build();
            }

            assertDoesNotThrow(() -> {
                String body = underTest.buildEmailBody(emailRequest);
                assertNotNull(body);
                assertFalse(body.isEmpty());
            }, "Failed for email type: " + emailType);
        }
    }


    @Test
    void testBuildEmailWithDifferentRecipientTypes() {
        RecipientType[] recipientTypes = RecipientType.values();

        for (RecipientType recipientType : recipientTypes) {
            EmailRequest emailRequest = createEmailRequest(
                    EmailType.TASK_RESCHEDULE,
                    recipientType
            );

            assertDoesNotThrow(() -> {
                String subject = underTest.buildEmailSubject(emailRequest);
                String body = underTest.buildEmailBody(emailRequest);

                assertNotNull(subject);
                assertNotNull(body);
            }, "Failed for recipient type: " + recipientType);
        }
    }
    
    // Exception Field Tests
    
    @Test
    void testBuildEmailSubjectNullRequestExceptionFields() {
        try {
            underTest.buildEmailSubject(null);
            fail("Should throw EmailTemplateException");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
            assertEquals("EMAIL_REQUEST", e.getTemplateType());
            assertEquals("emailRequest", e.getMissingField());
        }
    }

    @Test
    void testBuildEmailSubjectNullEmailTypeExceptionFields() {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(RECIPIENT_EMAIL)
                .emailType(null)
                .build();

        try {
            underTest.buildEmailSubject(emailRequest);
            fail("Should throw EmailTemplateException");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
            assertEquals("EMAIL_REQUEST", e.getTemplateType());
            assertEquals("emailType", e.getMissingField());
        }
    }

    @Test
    void testBuildEmailBodyNullRequestExceptionFields() {
        try {
            underTest.buildEmailBody(null);
            fail("Should throw EmailTemplateException");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
            assertEquals("EMAIL_REQUEST", e.getTemplateType());
            assertEquals("emailRequest", e.getMissingField());
        }
    }

    @Test
    void testBuildOtpEmailContentExceptionFields() {
        try {
            underTest.buildOtpEmailContent(EmailType.TASK_STATUS, OTP_CODE);
            fail("Should throw EmailTemplateException");
        } catch (EmailTemplateException e) {
            assertNotNull(e.getTemplateType());
            assertNotNull(e.getMissingField());
            assertTrue(e.getTemplateType().contains("TASK_STATUS"));
            assertEquals("emailType", e.getMissingField());
        }
    }

    @Test
    void testBuildOtpEmailContentThrowsExceptionWithTemplateType() {
        EmailTemplateException exception = assertThrows(
                EmailTemplateException.class,
                () -> underTest.buildOtpEmailContent(EmailType.TASK_REQUEST, OTP_CODE)
        );

        assertNotNull(exception.getTemplateType());
        assertTrue(exception.getMessage().contains(EmailType.TASK_REQUEST.toString()));
    }
    

    @Test
    void testEmailContentContainsRelevantTaskInformation() {
        EmailRequest emailRequest = createEmailRequest(
                EmailType.TASK_STATUS,
                RecipientType.USER
        );

        String body = underTest.buildEmailBody(emailRequest);

        // Check that important task information appears in the email body
        assertTrue(body.contains(completeTaskDto.getServiceName()) ||
                body.contains(completeTaskDto.getUserName()) ||
                body.contains(completeTaskDto.getTaskerName()) ||
                body.contains(completeTaskDto.getAddressDetails()));
    }

    @Test
    void testEmailTemplatesAreDifferentForDifferentEmailTypes() {
        EmailType[] taskRelatedTypes = {
                EmailType.TASK_STATUS,
                EmailType.TASK_REQUEST,
                EmailType.TASK_RESCHEDULE,
                EmailType.TASK_RESUMED
        };

        String previousSubject = null;
        String previousBody = null;

        for (EmailType emailType : taskRelatedTypes) {
            EmailRequest emailRequest = createEmailRequest(emailType, RecipientType.USER);

            String currentSubject = underTest.buildEmailSubject(emailRequest);
            String currentBody = underTest.buildEmailBody(emailRequest);

            assertNotNull(currentSubject);
            assertNotNull(currentBody);

            // Ensure different email types produce different content
            if (previousSubject != null) {
                assertNotEquals(previousSubject, currentSubject,
                        "Subjects should differ for different email types: " + emailType);
            }
            if (previousBody != null) {
                assertNotEquals(previousBody, currentBody,
                        "Bodies should differ for different email types: " + emailType);
            }

            previousSubject = currentSubject;
            previousBody = currentBody;
        }
    }
}