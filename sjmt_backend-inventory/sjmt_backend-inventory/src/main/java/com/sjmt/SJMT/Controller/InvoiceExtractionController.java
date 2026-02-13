package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.ResponseDTO.InvoiceItemDTO;
import com.sjmt.SJMT.Entity.InventoryEntity;
import com.sjmt.SJMT.Service.InvoiceExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Invoice Extraction Controller
 * @author SJMT Team
 * @version 1.0
 */
@RestController
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
    @PostMapping(
            value = "/extract",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ResponseEntity<?> extractInvoiceDetails(
            @Parameter(description = "Invoice PDF file to extract details from", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            logger.info("Invoice extraction request received. File: {}", file.getOriginalFilename());

            // Validate file
            if (file.isEmpty()) {
                logger.warn("Empty file uploaded");
                Map<String, String> error = new HashMap<>();
                error.put("error", "No file uploaded");
                error.put("message", "Please upload a PDF file");
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

            // Step 2: Save to inventory database
            List<InventoryEntity> savedItems = invoiceExtractionService.saveToInventory(invoiceItems);
            logger.info("Successfully saved {} items to inventory", savedItems.size());

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invoice items extracted and saved to inventory successfully");
            response.put("extractedCount", invoiceItems.size());
            response.put("savedCount", savedItems.size());
            response.put("inventoryIds", savedItems.stream()
                    .map(InventoryEntity::getId)
                    .collect(Collectors.toList()));
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
    @PostMapping(
            value = "/extract-only",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ResponseEntity<?> extractOnly(
            @Parameter(description = "Invoice PDF file", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            logger.info("Extract-only request received. File: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No file uploaded"));
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
}