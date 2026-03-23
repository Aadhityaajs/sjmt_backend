package com.sjmt.SJMT.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


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
    public void sendPasswordResetEmail(String toEmail, String tempPassword, String username) {
        try {
            String subject = "Password Reset Request - " + appName;
                        
            String body = "Hello " + username + ",\n\n" +
                    "We received a request to reset your password for your " + appName + " account.\n\n" +
                    "Here is your temporary password to login:\n\n" +
                    "Username: " + username + "\n" +
                    "Temporary Password: " + tempPassword + "\n\n" +
                    "IMPORTANT: For security reasons, you will be required to change this password after login.\n\n" +
                    "Please login at: " + baseUrl + "\n\n" +
                    "Best regards,\n" +
                    appName + " Team";
            
            sendEmail(toEmail, subject, body);
            logger.info("Temporary Password has been sent for reseting password to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending tempory password email for reseting password: {}", e.getMessage());
            throw new RuntimeException("Failed to send  temporary password email for reseting password");
        }
    }
    
    // /**
    //  * Send password set link for new staff members
    //  */
    // public void sendSetPasswordEmail(String toEmail, String token, String username) {
    //     try {
    //         String subject = "Set Your Password - " + appName;
    //         String setPasswordUrl = baseUrl + "/api/auth/verify-email?token=" + token;
            
    //         String body = "Hello " + username + ",\n\n" +
    //                 "Your account has been created in " + appName + ".\n\n" +
    //                 "Please click the link below to verify your email and set your password:\n" +
    //                 setPasswordUrl + "\n\n" +
    //                 "This link will expire in 24 hours.\n\n" +
    //                 "If you have any questions, please contact your administrator.\n\n" +
    //                 "Best regards,\n" +
    //                 appName + " Team";
            
    //         sendEmail(toEmail, subject, body);
    //         logger.info("Set password email sent to: {}", toEmail);
    //     } catch (Exception e) {
    //         logger.error("Error sending set password email: {}", e.getMessage());
    //         throw new RuntimeException("Failed to send set password email");
    //     }
    // }

    /**
     * Send temporary password email after email verification
     */
    public void sendTemporaryPasswordEmail(String toEmail, String tempPassword, String username) {
        try {
            String subject = "Your Temporary Password - " + appName;
            
            String body = "Hello " + username + ",\n\n" +
                    "Your email has been verified successfully!\n\n" +
                    "Here is your temporary password to login:\n\n" +
                    "Username: " + username + "\n" +
                    "Temporary Password: " + tempPassword + "\n\n" +
                    "IMPORTANT: For security reasons, you will be required to change this password on your first login.\n\n" +
                    "Please login at: " + baseUrl + "\n\n" +
                    "Best regards,\n" +
                    appName + " Team";
            
            sendEmail(toEmail, subject, body);
            logger.info("Temporary password email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending temporary password email: {}", e.getMessage());
            throw new RuntimeException("Failed to send temporary password email");
        }
    }
    
    /**
     * Notify the customer that their quotation has expired (MEDIUM-2)
     */
    public void sendQuotationExpiredEmail(String toEmail, String customerName,
                                          String quotationNumber, String validUntil) {
        try {
            String subject = "Your Quotation Has Expired - " + appName;
            String body = "Dear " + customerName + ",\n\n"
                    + "We wanted to let you know that your quotation " + quotationNumber
                    + " (valid until " + validUntil + ") has expired.\n\n"
                    + "If you are still interested, please contact us to receive a fresh quotation.\n\n"
                    + "Best regards,\n"
                    + appName + " Team";
            sendEmail(toEmail, subject, body);
            logger.info("Quotation expired email sent to {} for quotation {}", toEmail, quotationNumber);
        } catch (Exception e) {
            logger.error("Failed to send quotation expired email for {}: {}", quotationNumber, e.getMessage());
        }
    }

    /**
     * Remind the customer that their quotation is expiring soon (MEDIUM-3)
     */
    public void sendQuotationExpiryReminderEmail(String toEmail, String customerName,
                                                  String quotationNumber, String expiryDate,
                                                  int daysLeft) {
        try {
            String subject = "Quotation Expiring Soon - " + appName;
            String body = "Dear " + customerName + ",\n\n"
                    + "This is a reminder that your quotation " + quotationNumber
                    + " will expire in " + daysLeft + " day(s) on " + expiryDate + ".\n\n"
                    + "Please accept or contact us before the expiry date to avoid losing your quote.\n\n"
                    + "Best regards,\n"
                    + appName + " Team";
            sendEmail(toEmail, subject, body);
            logger.info("Quotation expiry reminder sent to {} for quotation {} ({} days left)",
                    toEmail, quotationNumber, daysLeft);
        } catch (Exception e) {
            logger.error("Failed to send quotation expiry reminder for {}: {}", quotationNumber, e.getMessage());
        }
    }

    /**
     * Send a plain alert to the admin inbox (used by scheduled jobs on failure)
     */
    public void sendAdminAlert(String subject, String body) {
        try {
            sendEmail(fromEmail, "[SJMT ALERT] " + subject, body);
            logger.info("Admin alert sent: {}", subject);
        } catch (Exception e) {
            // Only log — never let alert failure break the caller
            logger.error("Failed to send admin alert '{}': {}", subject, e.getMessage());
        }
    }

    /**
     * Generic method to send email
     */
    private void sendEmail(String to, String subject, String body) {
       SimpleMailMessage message = new SimpleMailMessage();
       message.setFrom(fromEmail);
       message.setTo(to);
       message.setSubject(subject);
       message.setText(body);

       mailSender.send(message);
    }
}