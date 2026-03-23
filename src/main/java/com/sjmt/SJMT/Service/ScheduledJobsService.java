package com.sjmt.SJMT.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled jobs that run automatically on a cron schedule.
 * Timezone is IST (Asia/Kolkata) — set JVM-wide in SjmtApplication.main().
 *
 * - Daily at 00:30 IST: send quotation expiry reminder emails (3-day and 1-day ahead)
 * - Daily at 01:00 IST: expire quotations past their validUntil date
 * - Daily at 01:05 IST: flag issued/partially-paid bills past their dueDate as OVERDUE
 */
@Service
public class ScheduledJobsService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobsService.class);

    private final QuotationService quotationService;
    private final CustomerBillService customerBillService;
    private final EmailService emailService;

    public ScheduledJobsService(QuotationService quotationService,
                                CustomerBillService customerBillService,
                                EmailService emailService) {
        this.quotationService = quotationService;
        this.customerBillService = customerBillService;
        this.emailService = emailService;
    }

    /**
     * MEDIUM-3: Runs daily at 00:30 AM IST.
     * Sends reminder emails for quotations expiring in exactly 3 days and 1 day.
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void sendQuotationExpiryReminders() {
        log.info("[Scheduler] Running sendQuotationExpiryReminders job");
        try {
            quotationService.sendExpiryReminders(3);
            quotationService.sendExpiryReminders(1);
            log.info("[Scheduler] sendQuotationExpiryReminders completed successfully");
        } catch (Exception e) {
            log.error("[Scheduler] sendQuotationExpiryReminders FAILED: {}", e.getMessage(), e);
            emailService.sendAdminAlert(
                    "Scheduled Job Failed: sendQuotationExpiryReminders",
                    "The quotation expiry reminder job failed at " + java.time.LocalDateTime.now()
                            + ".\n\nError: " + e.getMessage());
        }
    }

    /**
     * Runs daily at 01:00 AM IST.
     * Marks DRAFT/SENT/ACCEPTED quotations whose validUntil < today as EXPIRED.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void expireOutdatedQuotations() {
        log.info("[Scheduler] Running expireOutdatedQuotations job");
        try {
            quotationService.expireOutdatedQuotations();
            log.info("[Scheduler] expireOutdatedQuotations completed successfully");
        } catch (Exception e) {
            log.error("[Scheduler] expireOutdatedQuotations FAILED: {}", e.getMessage(), e);
            emailService.sendAdminAlert(
                    "Scheduled Job Failed: expireOutdatedQuotations",
                    "The daily quotation expiry job failed at " + java.time.LocalDateTime.now()
                            + ".\n\nError: " + e.getMessage()
                            + "\n\nSome quotations may not have been auto-expired. Please check and expire manually.");
        }
    }

    /**
     * Runs daily at 01:05 AM IST.
     * Marks ISSUED/PARTIALLY_PAID bills whose dueDate < today as OVERDUE.
     */
    @Scheduled(cron = "0 5 1 * * ?")
    public void flagOverdueBills() {
        log.info("[Scheduler] Running flagOverdueBills job");
        try {
            customerBillService.flagOverdueBills();
            log.info("[Scheduler] flagOverdueBills completed successfully");
        } catch (Exception e) {
            log.error("[Scheduler] flagOverdueBills FAILED: {}", e.getMessage(), e);
            emailService.sendAdminAlert(
                    "Scheduled Job Failed: flagOverdueBills",
                    "The daily overdue-bills job failed at " + java.time.LocalDateTime.now()
                            + ".\n\nError: " + e.getMessage()
                            + "\n\nSome bills may not have been flagged as OVERDUE. Please check and flag manually.");
        }
    }
}
