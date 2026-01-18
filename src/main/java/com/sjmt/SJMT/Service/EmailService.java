package com.sjmt.SJMT.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;


/**
 * Email Service for sending emails
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    /**
     * Send email verification link
     */
    public void sendEmailVerification(String toEmail, String token, String username) {
        try {
            String subject = "Email Verification - " + appName;
            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
            
            String body = "Hello " + username + ",\n\n" +
                    "Thank you for registering with " + appName + "!\n\n" +
                    "Please click the link below to verify your email address:\n" +
                    verificationUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not create an account, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    appName + " Team";
            
            sendEmail(toEmail, subject, body);
            logger.info("Email verification sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending email verification: {}", e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }
    
    /**
     * Send password reset link
     */
    public void sendPasswordResetEmail(String toEmail, String token, String username) {
        try {
            String subject = "Password Reset Request - " + appName;
            String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;
            
            String body = "Hello " + username + ",\n\n" +
                    "We received a request to reset your password for your " + appName + " account.\n\n" +
                    "Please click the link below to reset your password:\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 10 minutes.\n\n" +
                    "If you did not request a password reset, please ignore this email or contact support if you have concerns.\n\n" +
                    "Best regards,\n" +
                    appName + " Team";
            
            sendEmail(toEmail, subject, body);
            logger.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending password reset email: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }
    
    /**
     * Send password set link for new staff members
     */
    public void sendSetPasswordEmail(String toEmail, String token, String username) {
        try {
            String subject = "Set Your Password - " + appName;
            String setPasswordUrl = baseUrl + "/api/auth/verify-email?token=" + token;
            
            String body = "Hello " + username + ",\n\n" +
                    "Your account has been created in " + appName + ".\n\n" +
                    "Please click the link below to verify your email and set your password:\n" +
                    setPasswordUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you have any questions, please contact your administrator.\n\n" +
                    "Best regards,\n" +
                    appName + " Team";
            
            sendEmail(toEmail, subject, body);
            logger.info("Set password email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending set password email: {}", e.getMessage());
            throw new RuntimeException("Failed to send set password email");
        }
    }
    
    /**
     * Generic method to send email
     */
    private void sendEmail(String to, String subject, String body) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//
//        mailSender.send(message);
    }
}