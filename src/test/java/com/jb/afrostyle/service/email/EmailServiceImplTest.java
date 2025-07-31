package com.jb.afrostyle.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailServiceImpl.
 * 
 * Teste le service centralisé d'envoi d'emails.
 * 
 * @author AfroStyle Team
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailServiceImpl emailService;

    private final String fromEmail = "noreply@afrostyle.be";

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
    }

    @Test
    void sendTextEmail_ShouldSendSuccessfully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        // When
        CompletableFuture<Void> result = emailService.sendTextEmail(to, subject, content);

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.get());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlEmail_ShouldSendSuccessfully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String htmlContent = "<h1>Test HTML Content</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        CompletableFuture<Void> result = emailService.sendHtmlEmail(to, subject, htmlContent);

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.get());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldSendWithCorrectTemplate() {
        // Given
        String email = "user@example.com";
        String resetLink = "https://afrostyle.be/reset?token=abc123";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        CompletableFuture<Void> result = emailService.sendPasswordResetEmail(email, resetLink);

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.get());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBookingConfirmationEmail_ShouldSendWithCorrectTemplate() {
        // Given
        String to = "customer@example.com";
        String customerName = "John Doe";
        String salonName = "AfroStyle Salon";
        String bookingDetails = "Coupe + Coloration - 2025-01-20 14:00";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        CompletableFuture<Void> result = emailService.sendBookingConfirmationEmail(
            to, customerName, salonName, bookingDetails);

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.get());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPaymentConfirmationEmail_ShouldSendWithCorrectTemplate() {
        // Given
        String to = "customer@example.com";
        String customerName = "Jane Doe";
        String paymentAmount = "85.50 EUR";
        String transactionId = "pi_1234567890";
        String serviceDetails = "Coupe + Coloration";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        CompletableFuture<Void> result = emailService.sendPaymentConfirmationEmail(
            to, customerName, paymentAmount, transactionId, serviceDetails);

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> result.get());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendTextEmail_WithNullParameters_ShouldHandleGracefully() {
        // Given
        String to = null;
        String subject = null;
        String content = null;

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> result = emailService.sendTextEmail(to, subject, content);
            // Should complete without throwing exception
        });
    }

    @Test
    void sendHtmlEmail_WithEmptyParameters_ShouldHandleGracefully() {
        // Given
        String to = "";
        String subject = "";
        String htmlContent = "";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> result = emailService.sendHtmlEmail(to, subject, htmlContent);
            // Should complete without throwing exception
        });
    }

    @Test
    void emailService_WithMailSenderException_ShouldHandleGracefully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        doThrow(new RuntimeException("SMTP Server Error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> result = emailService.sendTextEmail(to, subject, content);
            // Should complete without throwing exception, but log the error
        });
    }
}