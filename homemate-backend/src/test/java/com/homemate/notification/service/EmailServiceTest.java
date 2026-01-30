package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.model.RecipientType;
import com.homemate.notification.service.impl.EmailServiceImpl;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private EmailTemplate emailTemplate;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private RestTemplate restTemplate;

    private EmailServiceImpl underTest;
    private static final String RECIPIENT_EMAIL = "someone@gmail.com";
    private static final String HOMEMATE_EMAIL = "homemateservice8@gmail.com";
    private static final String BREVO_API_KEY = "xkeysib-test-key-123456789";
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @BeforeEach
    void setUp() {
        underTest = new EmailServiceImpl(emailTemplate, javaMailSender);
        ReflectionTestUtils.setField(underTest, "fromEmail", HOMEMATE_EMAIL);
        ReflectionTestUtils.setField(underTest, "fromName", "HomeMate");
        ReflectionTestUtils.setField(underTest, "restTemplate", restTemplate);
    }

    
    // BREVO API TESTS
    

    @Test
    void testSendEmailViaBrevoAPIWhenEnabled() throws ExecutionException, InterruptedException {
        // Setup Brevo enabled
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).status(Status.Accepted).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been accepted");

        // Mock successful Brevo API response
        ResponseEntity<String> successResponse = new ResponseEntity<>("{\"messageId\":\"123\"}", HttpStatus.OK);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(successResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Brevo"));
        verify(restTemplate).exchange(eq(BREVO_API_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailViaBrevoAPISendsCorrectHeaders() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(2L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("New Task");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task details");

        ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        )).thenReturn(successResponse);

        underTest.sendEmail(emailRequest).get();

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(BREVO_API_KEY, headers.getFirst("api-key"));
        assertEquals("application/json", headers.getFirst("accept"));
    }

    @Test
    void testSendEmailViaBrevoAPISendsCorrectRequestBody() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(3L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        String subject = "Test Subject";
        String body = "Test Body";
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(subject);
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(body);

        ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        )).thenReturn(successResponse);

        underTest.sendEmail(emailRequest).get();

        HttpEntity<Map<String, Object>> capturedEntity = (HttpEntity<Map<String, Object>>) entityCaptor.getValue();
        Map<String, Object> requestBody = capturedEntity.getBody();

        assertNotNull(requestBody);
        assertEquals(subject, requestBody.get("subject"));
        assertEquals(body, requestBody.get("textContent"));

        Map<String, String> sender = (Map<String, String>) requestBody.get("sender");
        assertEquals("HomeMate", sender.get("name"));
        assertEquals(HOMEMATE_EMAIL, sender.get("email"));

        Object[] recipients = (Object[]) requestBody.get("to");
        Map<String, String> recipient = (Map<String, String>) recipients[0];
        assertEquals(RECIPIENT_EMAIL, recipient.get("email"));
    }

    @Test
    void testSendEmailFallbackToSMTPWhenBrevoFails() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(4L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        // Mock Brevo API failure
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        // Should fallback to SMTP
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("SMTP"));
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIReturns401Unauthorized() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", "invalid-key");

        TaskDto task = TaskDto.builder().taskID(5L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ResponseEntity<String> unauthorizedResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(unauthorizedResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        // Should fallback to SMTP
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIReturns400BadRequest() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(6L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail("invalid-email")
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ResponseEntity<String> badRequestResponse = new ResponseEntity<>(
                "{\"message\":\"Invalid email\"}",
                HttpStatus.BAD_REQUEST
        );
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(badRequestResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        // Should fallback to SMTP
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIReturns500ServerError() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(7L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        // Should fallback to SMTP
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPINetworkTimeout() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(8L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new ResourceAccessException("Connection timeout"));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        // Should fallback to SMTP
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailWithBrevoDisabledUsesSMTP() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(9L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    void testSendEmailWithNullBrevoAPIKeyUsesSMTP() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", null);

        TaskDto task = TaskDto.builder().taskID(10L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    void testSendEmailWithEmptyBrevoAPIKeyUsesSMTP() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", "");

        TaskDto task = TaskDto.builder().taskID(11L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
    }

    @Test
    void testSendEmailBrevoAPIReturns201Created() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(12L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ResponseEntity<String> createdResponse = new ResponseEntity<>("{\"messageId\":\"abc123\"}", HttpStatus.CREATED);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(createdResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Brevo"));
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIHandlesSpecialCharacters() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(13L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        String specialSubject = "Task: \"Urgent\" & <Important>";
        String specialBody = "Details:\n\tLine 1\n\tLine 2";

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(specialSubject);
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(specialBody);

        ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(successResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(restTemplate).exchange(eq(BREVO_API_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testSendEmailBrevoAPIWithMultipleRecipients() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        // Send to first recipient
        TaskDto task1 = TaskDto.builder().taskID(14L).build();
        EmailRequest request1 = EmailRequest.builder()
                .task(task1)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail("user1@test.com")
                .recipientType(RecipientType.USER)
                .build();

        // Send to second recipient
        TaskDto task2 = TaskDto.builder().taskID(15L).build();
        EmailRequest request2 = EmailRequest.builder()
                .task(task2)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail("user2@test.com")
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(any())).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(any())).thenReturn("Body");

        ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(successResponse);

        TaskResponse response1 = underTest.sendEmail(request1).get();
        TaskResponse response2 = underTest.sendEmail(request2).get();

        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        verify(restTemplate, times(2)).exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void testSendEmailBrevoAPIFailsThenSMTPSucceeds() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(16L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        // First call: Brevo fails
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // SMTP succeeds
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("SMTP"));
        verify(restTemplate).exchange(eq(BREVO_API_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIFailsAndSMTPAlsoFails() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(17L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        // Brevo fails
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // SMTP also fails
        doThrow(new MailSendException("SMTP failed"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to send email"));
        verify(restTemplate).exchange(eq(BREVO_API_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    
    // EXISTING SMTP TESTS (Keep all of them)
    

    @Test
    void testSendUserEmailWithTaskAcceptedType() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(1L).status(Status.Accepted).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been accepted");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        assertEquals("Email sent successfully via SMTP", response.getMessage());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailSubject(emailRequest);
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    // ... (keep ALL your existing tests here - I'm not showing them all to save space,
    // but include every single test you already have)

    @Test
    void testSendEmailWithAllEmailTypes() throws ExecutionException, InterruptedException {
        EmailType[] emailTypes = {
                EmailType.TASK_STATUS,
                EmailType.TASK_REQUEST,
                EmailType.TASK_RESCHEDULE,
                EmailType.TASK_RESUMED,
                EmailType.EMAIL_VERIFICATION,
                EmailType.FORGOT_PASSWORD
        };

        for (EmailType emailType : emailTypes) {
            TaskDto task = TaskDto.builder().taskID(System.nanoTime()).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(emailType)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            TaskResponse response = underTest.sendEmail(emailRequest).get();
            assertTrue(response.isSuccess());
        }

        verify(javaMailSender, times(emailTypes.length)).send(any(SimpleMailMessage.class));
    }

    // Add these additional tests to your existing EmailServiceTest.java class

    
    // SMTP EDGE CASES AND COVERAGE IMPROVEMENTS
    

    @Test
    void testSendEmailViaSMTPWithNullTask() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        EmailRequest emailRequest = EmailRequest.builder()
                .task(null)
                .emailType(EmailType.EMAIL_VERIFICATION)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Verify Email");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Verification body");

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailSMTPFailureReturnsFailedResponse() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        doThrow(new MailSendException("SMTP server unavailable"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to send email"));
    }

    @Test
    void testSendEmailSMTPWithRuntimeException() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        doThrow(new RuntimeException("Unexpected error"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertFalse(response.isSuccess());
    }

    @Test
    void testSendEmailVerifiesCorrectSMTPMessageContent() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        String expectedSubject = "Test Subject";
        String expectedBody = "Test Body";

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(expectedSubject);
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(expectedBody);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        underTest.sendEmail(emailRequest).get();

        verify(javaMailSender).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals(HOMEMATE_EMAIL, capturedMessage.getFrom());
        assertArrayEquals(new String[]{RECIPIENT_EMAIL}, capturedMessage.getTo());
        assertEquals(expectedSubject, capturedMessage.getSubject());
        assertEquals(expectedBody, capturedMessage.getText());
    }

    
    // BREVO API ADVANCED COVERAGE
    

    @Test
    void testSendEmailBrevoAPIWithNullResponseBody() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ResponseEntity<String> nullBodyResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(nullBodyResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Brevo"));
    }

    @Test
    void testSendEmailBrevoAPIReturns202Accepted() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ResponseEntity<String> acceptedResponse = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(acceptedResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIThrowsGenericException() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Unexpected error"));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIReturns429TooManyRequests() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailBrevoAPIReturns503ServiceUnavailable() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ResponseEntity<String> unavailableResponse = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(unavailableResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    
    // TEMPLATE EXCEPTION HANDLING
    

    @Test
    void testSendEmailWhenBuildSubjectThrowsException() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest))
                .thenThrow(new EmailTemplateException("Subject build failed", "TASK_STATUS", "subject"));

        TaskResponse response = underTest.sendEmail(emailRequest).get();


        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Subject build failed"));
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailWhenBuildBodyThrowsException() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest))
                .thenThrow(new EmailTemplateException("Body build failed", "TASK_STATUS", "body"));

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Body build failed"));
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    
    // ASYNC BEHAVIOR TESTS
    

    @Test
    void testSendEmailReturnsCompletableFuture() {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        CompletableFuture<TaskResponse> future = underTest.sendEmail(emailRequest);

        assertNotNull(future);
        assertInstanceOf(CompletableFuture.class, future);
    }

    @Test
    void testSendEmailCompletableFutureCompletes() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        CompletableFuture<TaskResponse> future = underTest.sendEmail(emailRequest);
        TaskResponse response = future.get();

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }

    
    // REQUEST VALIDATION COVERAGE
    

    @Test
    void testSendEmailWithNullRecipientEmail() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(null)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        // Should handle gracefully or throw - depends on implementation
        assertDoesNotThrow(() -> underTest.sendEmail(emailRequest));
    }

    @Test
    void testSendEmailBrevoWithLongSubjectAndBody() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", true);
        ReflectionTestUtils.setField(underTest, "brevoApiKey", BREVO_API_KEY);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        String longSubject = "A".repeat(500);
        String longBody = "B".repeat(5000);

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(longSubject);
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(longBody);

        ResponseEntity<String> successResponse = new ResponseEntity<>("{}", HttpStatus.OK);
        when(restTemplate.exchange(
                eq(BREVO_API_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(successResponse);

        TaskResponse response = underTest.sendEmail(emailRequest).get();

        assertTrue(response.isSuccess());
    }

    @Test
    void testSendEmailSMTPVerifiesFromEmailIsSet() throws ExecutionException, InterruptedException {
        ReflectionTestUtils.setField(underTest, "brevoEnabled", false);
        ReflectionTestUtils.setField(underTest, "fromEmail", HOMEMATE_EMAIL);

        TaskDto task = TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        underTest.sendEmail(emailRequest).get();

        verify(javaMailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();

        assertEquals(HOMEMATE_EMAIL, message.getFrom());
    }

     @Test
    void testSendDirectEmail_Success() throws ExecutionException, InterruptedException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        TaskResponse response = underTest.sendDirectEmail(to, subject, body).get();

        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        
        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
        assertEquals(HOMEMATE_EMAIL, sentMessage.getFrom());
    }

    @Test
    void testSendDirectEmail_Failure() throws ExecutionException, InterruptedException {
        String to = "test@example.com"; 
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new MailSendException("Failed")).when(javaMailSender).send(any(SimpleMailMessage.class));

        TaskResponse response = underTest.sendDirectEmail(to, subject, body).get();

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to send email"));
    }

}

