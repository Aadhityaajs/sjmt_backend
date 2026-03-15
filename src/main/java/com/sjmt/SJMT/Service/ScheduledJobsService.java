package com.sjmt.SJMT.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled jobs that run automatically on a cron schedule.
 *
 * - Daily at 01:00 IST: expire quotations past their validUntil date
 * - Daily at 01:00 IST: flag issued/partially-paid bills past their dueDate as OVERDUE
 */
@Service
public class ScheduledJobsService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobsService.class);

    private final QuotationService quotationService;
    private final CustomerBillService customerBillService;

    public ScheduledJobsService(QuotationService quotationService,
                                CustomerBillService customerBillService) {
        this.quotationService = quotationService;
        this.customerBillService = customerBillService;
    }

    /**
     * Runs daily at 01:00 AM IST.
     * Marks DRAFT/SENT quotations whose validUntil < today as EXPIRED.
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Kolkata")
    public void expireOutdatedQuotations() {
        log.info("[Scheduler] Running expireOutdatedQuotations job");
        quotationService.expireOutdatedQuotations();
    }

    /**
     * Runs daily at 01:05 AM IST.
     * Marks ISSUED/PARTIALLY_PAID bills whose dueDate < today as OVERDUE.
     */
    @Scheduled(cron = "0 5 1 * * ?", zone = "Asia/Kolkata")
    public void flagOverdueBills() {
        log.info("[Scheduler] Running flagOverdueBills job");
        customerBillService.flagOverdueBills();
    }
}
