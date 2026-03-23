package com.sjmt.SJMT.Service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sends an HTML error-alert email to all configured developer addresses
 * whenever a critical error occurs (backend crash or frontend unhandled error).
 *
 * <p><b>Rate limiting:</b> the same error fingerprint (first 120 chars of the
 * message) will not trigger another email for 10 minutes, preventing inbox
 * floods when a bug fires repeatedly.
 *
 * <p>Only active when {@code app.error-notification.enabled=true}.
 */
@Service
@ConditionalOnProperty(name = "app.error-notification.enabled", havingValue = "true")
public class ErrorNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorNotificationService.class);

    /** Minimum gap (ms) before the same error fingerprint can send again. */
    private static final long RATE_LIMIT_MS = 10 * 60 * 1_000L; // 10 minutes

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.error-notification.recipients}")
    private String recipientsRaw;

    /** fingerprint → last-sent epoch-ms */
    private final ConcurrentHashMap<String, Long> lastSentAt = new ConcurrentHashMap<>();

    public ErrorNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Send an error alert asynchronously so the HTTP response is never delayed.
     *
     * @param source  where the error came from (e.g. "Backend", "Frontend – ErrorBoundary")
     * @param message short error message
     * @param stack   full stack trace (may be null)
     * @param url     request/page URL where the error occurred (may be null)
     */
    @Async
    public void sendErrorAlert(String source, String message, String stack, String url) {
        if (isRateLimited(message)) {
            return;
        }

        List<String> recipients = Arrays.stream(recipientsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (recipients.isEmpty()) {
            logger.warn("ErrorNotificationService: no recipients configured — skipping email.");
            return;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("[SJMT ERROR] " + source + ": " + truncate(message, 80));
            helper.setText(buildHtml(source, message, stack, url), true);

            mailSender.send(mime);
            logger.info("Error notification sent to {} recipient(s) for: {}", recipients.size(), truncate(message, 60));

        } catch (Exception e) {
            // Never let email failure cause a secondary crash
            logger.warn("ErrorNotificationService: failed to send email — {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isRateLimited(String message) {
        String fingerprint = truncate(message, 120);
        long now = System.currentTimeMillis();
        Long last = lastSentAt.get(fingerprint);
        if (last != null && (now - last) < RATE_LIMIT_MS) {
            return true;
        }
        lastSentAt.put(fingerprint, now);
        return false;
    }

    private String truncate(String s, int max) {
        if (s == null) return "(null)";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String buildHtml(String source, String message, String stack, String url) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String stackHtml = stack != null
                ? "<pre style='background:#1e1e1e;color:#d4d4d4;padding:12px;border-radius:6px;"
                  + "font-size:12px;overflow:auto;white-space:pre-wrap'>"
                  + escapeHtml(stack) + "</pre>"
                : "<p style='color:#6b7280'>No stack trace available.</p>";

        String urlHtml = url != null
                ? "<p><b>URL:</b> <a href='" + escapeHtml(url) + "'>" + escapeHtml(url) + "</a></p>"
                : "";

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/></head>
            <body style="font-family:system-ui,sans-serif;margin:0;padding:24px;background:#f3f4f6">
              <div style="max-width:700px;margin:auto;background:#fff;border-radius:8px;
                          border-left:5px solid #ef4444;padding:24px;box-shadow:0 1px 3px rgba(0,0,0,.1)">

                <h2 style="margin:0 0 4px;color:#ef4444">⚠ SJMT Application Error</h2>
                <p style="color:#6b7280;margin:0 0 20px;font-size:13px">%s</p>

                <table style="width:100%;border-collapse:collapse;margin-bottom:20px;font-size:14px">
                  <tr style="background:#f9fafb">
                    <td style="padding:8px 12px;font-weight:600;width:100px">Source</td>
                    <td style="padding:8px 12px">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:8px 12px;font-weight:600">Message</td>
                    <td style="padding:8px 12px;color:#dc2626">%s</td>
                  </tr>
                </table>

                %s

                <h3 style="margin:20px 0 8px;font-size:14px">Stack Trace</h3>
                %s

                <hr style="border:none;border-top:1px solid #e5e7eb;margin:20px 0"/>
                <p style="color:#9ca3af;font-size:12px;margin:0">
                  This alert was sent automatically by SJMT. Identical errors are suppressed for 10 minutes.
                </p>
              </div>
            </body>
            </html>
            """.formatted(timestamp, escapeHtml(source), escapeHtml(message), urlHtml, stackHtml);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
