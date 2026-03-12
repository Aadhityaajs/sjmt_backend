package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.ResponseDTO.InvoiceItemDTO;
import com.sjmt.SJMT.DTO.ResponseDTO.InvoiceSaveResult;
import com.sjmt.SJMT.Entity.InventoryEntity;
import com.sjmt.SJMT.Service.InvoiceExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Invoice Extraction Controller
 * @author SJMT Team
 * @version 1.0
 */
@RestController
@Validated
@RequestMapping("/api/invoice")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Invoice Extraction", description = "APIs for extracting invoice details from PDF (Admin only)")
public class InvoiceExtractionController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceExtractionController.class);

    private final InvoiceExtractionService invoiceExtractionService;

    public InvoiceExtractionController(InvoiceExtractionService invoiceExtractionService) {
        this.invoiceExtractionService = invoiceExtractionService;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXISTING APIS — UNCHANGED
    // ═══════════════════════════════════════════════════════════════════════════

    @Operation(
            summary = "Extract invoice items from PDF and save to inventory",
            description = "Upload an invoice PDF, extract all BOM items using AI, and save them to inventory table (Admin only)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully extracted and saved invoice items"),
            @ApiResponse(responseCode = "400", description = "Invalid file or bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error during extraction")
    })
    @PostMapping(value = "/extract", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> extractInvoiceDetails(
            @Parameter(description = "Invoice PDF file to extract details from", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            logger.info("Invoice extraction request received. File: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                logger.warn("Empty file uploaded");
                Map<String, String> error = new HashMap<>();
                error.put("error", "No file uploaded");
                error.put("message", "Please upload a PDF file");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (file.getSize() > 5 * 1024 * 1024) {
                logger.warn("File too large: {} bytes", file.getSize());
                Map<String, String> error = new HashMap<>();
                error.put("error", "File too large");
                error.put("message", "Maximum upload size is 5MB");
                return ResponseEntity.badRequest().body(error);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                logger.warn("Invalid file type: {}", contentType);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid file type");
                error.put("message", "Only PDF files are accepted");
                return ResponseEntity.badRequest().body(error);
            }

            // Step 1: Extract invoice items using Gemini AI
            List<InvoiceItemDTO> invoiceItems = invoiceExtractionService.extractInvoiceItems(file);
            logger.info("Successfully extracted {} invoice items", invoiceItems.size());

            // Step 2: Save to inventory database + save PDF to disk
            InvoiceSaveResult saveResult = invoiceExtractionService.saveToInventory(invoiceItems, file);
            logger.info("Saved {}/{} items to inventory, {} failed",
                    saveResult.getSuccessCount(), invoiceItems.size(), saveResult.getFailureCount());

            Map<String, Object> response = new HashMap<>();
            response.put("success", saveResult.getFailureCount() == 0);
            response.put("message", saveResult.getFailureCount() == 0
                    ? "Invoice items extracted and saved to inventory successfully"
                    : saveResult.getSuccessCount() + " items saved, " + saveResult.getFailureCount() + " failed");
            response.put("extractedCount", invoiceItems.size());
            response.put("savedCount", saveResult.getSuccessCount());
            response.put("failedCount", saveResult.getFailureCount());
            response.put("inventoryIds", saveResult.getSavedInventoryIds());
            if (!saveResult.getFailureMessages().isEmpty()) {
                response.put("failures", saveResult.getFailureMessages());
            }
            response.put("items", invoiceItems);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            logger.error("Configuration error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Configuration error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

        } catch (Exception e) {
            logger.error("Invoice extraction failed: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Extraction failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
            summary = "Extract invoice items without saving (for testing)",
            description = "Upload an invoice PDF and extract items without saving to database"
    )
    @PostMapping(value = "/extract-only", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> extractOnly(
            @Parameter(description = "Invoice PDF file", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            logger.info("Extract-only request received. File: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No file uploaded"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File too large", "message", "Maximum upload size is 5MB"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type", "message", "Only PDF files are accepted"));
            }

            List<InvoiceItemDTO> invoiceItems = invoiceExtractionService.extractInvoiceItems(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invoice items extracted (not saved to database)");
            response.put("itemCount", invoiceItems.size());
            response.put("items", invoiceItems);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Extraction failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Extraction failed", "message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NEW APIS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/invoice/pdf/{id}
     * Download/view the invoice PDF linked to a specific inventory record.
     * Frontend: when user clicks a row in inventory table, call this with the inventory ID.
     */
    @Operation(
            summary = "Download invoice PDF by inventory ID",
            description = "Returns the invoice PDF file linked to the given inventory record ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF file returned successfully"),
            @ApiResponse(responseCode = "404", description = "No PDF found for this inventory ID"),
            @ApiResponse(responseCode = "500", description = "Error reading PDF from disk")
    })
    @GetMapping("/pdf/{id}")
    public ResponseEntity<?> getPdfByInventoryId(
            @Parameter(description = "Inventory record ID", required = true)
            @PathVariable Integer id) {
        try {
            byte[] pdfBytes = invoiceExtractionService.getPdfByInventoryId(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice_" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            logger.warn("PDF not found for inventory ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "PDF not found", "message", e.getMessage()));
        } catch (IOException e) {
            logger.error("Error reading PDF for inventory ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read PDF", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/invoice/pdf/month/{year}/{month}
     * Download a ZIP file containing all invoice PDFs uploaded in the given month.
     * Example: GET /api/invoice/pdf/month/2026/3 → invoices_march_2026.zip
     */
    @Operation(
            summary = "Download all invoice PDFs for a specific month as ZIP",
            description = "Returns a ZIP file containing all invoice PDFs uploaded in the given year/month"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ZIP file returned successfully"),
            @ApiResponse(responseCode = "404", description = "No PDFs found for this month"),
            @ApiResponse(responseCode = "500", description = "Error creating ZIP")
    })
    @GetMapping("/pdf/month/{year}/{month}")
    public ResponseEntity<?> getPdfsByMonth(
            @Parameter(description = "Year (e.g. 2026)", required = true) @PathVariable @Min(2000) @Max(2100) int year,
            @Parameter(description = "Month (1-12)", required = true) @PathVariable @Min(1) @Max(12) int month) {
        try {
            List<InventoryEntity> items = invoiceExtractionService.getInventoryWithPdfByMonth(year, month);

            if (items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No PDFs found", "message",
                                "No invoice PDFs found for " + month + "/" + year));
            }

            byte[] zipBytes = buildZip(items);
            String zipName = "invoices_" + year + "_" + String.format("%02d", month) + ".zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipName + "\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipBytes);

        } catch (Exception e) {
            logger.error("Error creating monthly ZIP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create ZIP", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/invoice/pdf/range?from=2026-01-01&to=2026-03-05
     * Download a ZIP file containing all invoice PDFs uploaded between two dates.
     */
    @Operation(
            summary = "Download all invoice PDFs between two dates as ZIP",
            description = "Returns a ZIP file containing all invoice PDFs uploaded between from and to dates (inclusive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ZIP file returned successfully"),
            @ApiResponse(responseCode = "404", description = "No PDFs found for this date range"),
            @ApiResponse(responseCode = "500", description = "Error creating ZIP")
    })
    @GetMapping("/pdf/range")
    public ResponseEntity<?> getPdfsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            LocalDateTime fromDateTime = from.atStartOfDay();
            LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

            List<InventoryEntity> items = invoiceExtractionService
                    .getInventoryWithPdfByDateRange(fromDateTime, toDateTime);

            if (items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No PDFs found",
                                "message", "No invoice PDFs found between " + from + " and " + to));
            }

            byte[] zipBytes = buildZip(items);
            String zipName = "invoices_" + from + "_to_" + to + ".zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipName + "\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipBytes);

        } catch (Exception e) {
            logger.error("Error creating date range ZIP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create ZIP", "message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPER
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Builds a ZIP file in memory from the list of inventory items with PDF paths.
     * Each PDF is added as a separate entry in the ZIP using just the filename.
     */
    private byte[] buildZip(List<InventoryEntity> items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (InventoryEntity item : items) {
                String pdfPath = item.getInvoicePdfPath();
                if (pdfPath == null) continue;

                java.nio.file.Path path = Paths.get(pdfPath);
                if (!Files.exists(path)) {
                    logger.warn("PDF file missing on disk for inventory ID {}: {}", item.getId(), pdfPath);
                    continue;
                }

                // Use only the filename inside the ZIP (not full path)
                String entryName = path.getFileName().toString();
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                zos.write(Files.readAllBytes(path));
                zos.closeEntry();
                logger.info("Added to ZIP: {}", entryName);
            }
        }
        return baos.toByteArray();
    }
}