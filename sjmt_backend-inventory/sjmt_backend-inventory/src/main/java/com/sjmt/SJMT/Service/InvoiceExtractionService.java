package com.sjmt.SJMT.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjmt.SJMT.DTO.ResponseDTO.InvoiceItemDTO;
import com.sjmt.SJMT.Entity.*;
import com.sjmt.SJMT.Repository.CategoryRepository;
import com.sjmt.SJMT.Repository.InventoryRepository;
import com.sjmt.SJMT.Repository.SubCategoryRepository;
import com.sjmt.SJMT.Repository.UnitOfMeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InvoiceExtractionService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceExtractionService.class);
    private static final String GEMINI_UPLOAD_URL = "https://generativelanguage.googleapis.com/upload/v1beta/files";
    private static final String EXTRACTION_PROMPT = """
            Extract information from this invoice PDF and return ONLY a valid JSON array. Each object in the array represents one line item from the BOM (Bill of Materials) table.
            
            For EACH line item in the BOM table, extract the following fields and create one JSON object:
            
            {
                "invoiceNumber": "<Invoice Number from top of invoice>",
                "gstPercentage": <GST percentage as integer (e.g., if CGST is 9% then total GST is 18, so use 18)>,
                "hsnCode": "<HSN/SAC code - MUST be exactly 4, 6, or 8 digits only>",
                "name": "steel/cement",
                "description": "<Description of Goods (same as name)>",
                "purchaseRate": <Rate as decimal number>,
                "sellingRate": null,
                "status": "ACTIVE",
                "driverName": "<Driver Name from invoice>",
                "driverNumber": "<Driver Phone Number from invoice>",
                "manufacturerName": "<Seller Company Name>",
                "unitOfMeasurementName": "<Unit from BOM (e.g., MTS, KG, PCS)>",
                "categoryName": "<Description of Goods (use as category)>",
                "subcategoryName": "<Combine Size + Grade, e.g., '8MM550D'>",
                "length": "<Length from BOM>",
                "quantity": <Quantity as decimal number>,
                "grade": "<Grade from BOM>",
                "size": "<Size from BOM>"
            }
            
            Important instructions:
            1. Return ONLY the JSON array, no additional text or explanation
            2. Create one object for EACH row in the BOM table
            3. Use proper JSON formatting with double quotes
            4. For GST percentage: Add CGST% + SGST% to get total (e.g., 9% + 9% = 18)
            5. Extract Driver Name and Driver Ph.No from the invoice
            6. Extract Seller company name for manufacturerName
            7. Combine Size and Grade WITHOUT space (e.g., "8MM" + "550D" = "8MM550D")
            8. Set sellingRate as null
            9. Set status as "ACTIVE"
            10. CRITICAL: hsnCode must be ONLY digits and EXACTLY 4, 6, or 8 digits long
                - Remove any spaces, hyphens, or other characters from HSN code
                - If HSN code has more than 8 digits, use only the first 8 digits
                - If HSN code has 5 digits, use only the first 4 digits
                - If HSN code has 7 digits, use only the first 6 digits
                - If HSN code has less than 4 digits, pad with leading zeros to make it 4 digits
                - Example: "7214 10" → "721410" (6 digits, remove space)
                - Example: "72141" → "7214" (5 digits, truncate to 4)
                - Example: "123" → "0123" (3 digits, pad to 4)
            """;

    private final WebClient webClient;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final UnitOfMeasurementRepository unitOfMeasurementRepository;

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.model:gemini-1.5-flash-002}")
    private String model;
    @Value("${gemini.retry.enabled:true}")
    private boolean retryEnabled;
    @Value("${gemini.retry.max-attempts:2}")
    private int maxRetryAttempts;
    @Value("${gemini.retry.delay-seconds:30}")
    private int retryDelaySeconds;

    public InvoiceExtractionService(WebClient.Builder webClientBuilder, InventoryRepository inventoryRepository,
                                    CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository,
                                    UnitOfMeasurementRepository unitOfMeasurementRepository) {
        this.webClient = webClientBuilder.build();
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.unitOfMeasurementRepository = unitOfMeasurementRepository;
    }

    public List<InvoiceItemDTO> extractInvoiceItems(MultipartFile pdfFile) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key not configured.");
        }
        log.info("Starting invoice extraction with model: {}", model);
        String fileUri = uploadPdf(pdfFile);
        String jsonResponse = generateContentWithRetry(fileUri);
        return parseInvoiceItems(jsonResponse);
    }

    private String uploadPdf(MultipartFile pdfFile) throws IOException {
        log.info("Uploading PDF: {}", pdfFile.getOriginalFilename());
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ByteArrayResource(pdfFile.getBytes()) {
            @Override
            public String getFilename() {
                return pdfFile.getOriginalFilename();
            }
        }).contentType(MediaType.APPLICATION_PDF);

        Map<String, Object> response = webClient.post().uri(GEMINI_UPLOAD_URL + "?key=" + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve().bodyToMono(Map.class).block();

        if (response == null || !response.containsKey("file")) {
            throw new RuntimeException("Failed to upload PDF to Gemini API.");
        }
        String fileUri = (String) ((Map<String, Object>) response.get("file")).get("uri");
        log.info("Upload complete. File URI: {}", fileUri);
        return fileUri;
    }

    private String generateContentWithRetry(String fileUri) {
        if (!retryEnabled) return generateContent(fileUri);
        int attempt = 1;
        while (attempt <= maxRetryAttempts) {
            try {
                return generateContent(fileUri);
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429 && attempt < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelaySeconds * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                    attempt++;
                    continue;
                }
                throw new RuntimeException("Gemini API error: " + e.getResponseBodyAsString(), e);
            }
        }
        throw new RuntimeException("Failed to extract invoice after " + maxRetryAttempts + " attempts");
    }

    private String generateContent(String fileUri) {
        Map<String, Object> requestBody = Map.of("contents",
                List.of(Map.of("parts", List.of(Map.of("fileData", Map.of("mimeType", "application/pdf", "fileUri", fileUri)),
                        Map.of("text", EXTRACTION_PROMPT)))));
        String generateUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        Map<String, Object> response = webClient.post().uri(generateUrl).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody).retrieve().bodyToMono(Map.class).block();
        if (response == null || !response.containsKey("candidates")) {
            throw new RuntimeException("Failed to extract invoice items from PDF via Gemini API.");
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    private List<InvoiceItemDTO> parseInvoiceItems(String jsonResponse) throws IOException {
        String cleanJson = jsonResponse.trim();
        if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
        if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
        if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        cleanJson = cleanJson.trim();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(cleanJson, new TypeReference<List<InvoiceItemDTO>>() {});
    }

    /**
     * Sanitize HSN code to meet validation requirements (4, 6, or 8 digits only)
     *
     * @param hsnCode Raw HSN code from invoice extraction
     * @return Sanitized HSN code that meets validation constraints
     */
    private String sanitizeHsnCode(String hsnCode) {
        if (hsnCode == null || hsnCode.isBlank()) {
            log.warn("HSN code is null or blank, using default '0000'");
            return "0000"; // Default 4-digit code
        }

        // Remove all non-digit characters (spaces, hyphens, etc.)
        String digitsOnly = hsnCode.replaceAll("[^0-9]", "");

        if (digitsOnly.isEmpty()) {
            log.warn("HSN code '{}' contains no digits, using default '0000'", hsnCode);
            return "0000";
        }

        int length = digitsOnly.length();

        // If already 4, 6, or 8 digits, return as-is
        if (length == 4 || length == 6 || length == 8) {
            return digitsOnly;
        }

        // If less than 4, pad with leading zeros
        if (length < 4) {
            String padded = String.format("%04d", Integer.parseInt(digitsOnly));
            log.debug("HSN code '{}' padded to '{}'", hsnCode, padded);
            return padded;
        }

        // If 5 digits, truncate to 4
        if (length == 5) {
            String truncated = digitsOnly.substring(0, 4);
            log.debug("HSN code '{}' truncated from 5 to 4 digits: '{}'", hsnCode, truncated);
            return truncated;
        }

        // If 7 digits, truncate to 6
        if (length == 7) {
            String truncated = digitsOnly.substring(0, 6);
            log.debug("HSN code '{}' truncated from 7 to 6 digits: '{}'", hsnCode, truncated);
            return truncated;
        }

        // If more than 8 digits, truncate to 8
        String truncated = digitsOnly.substring(0, 8);
        log.debug("HSN code '{}' truncated to 8 digits: '{}'", hsnCode, truncated);
        return truncated;
    }

    @Transactional
    public List<InventoryEntity> saveToInventory(List<InvoiceItemDTO> items) {
        List<InventoryEntity> saved = new ArrayList<>();
        int totalItems = items.size();
        int successCount = 0;
        int failureCount = 0;

        log.info("Starting to save {} invoice items to inventory", totalItems);

        for (InvoiceItemDTO dto : items) {
            try {
                InventoryEntity inv = new InventoryEntity();

                // UNIQUE NAME FIX: Add invoice number and size to make it unique
                String uniqueName = dto.getName() + " - " + dto.getSubcategoryName() + " (" + dto.getInvoiceNumber() + ")";
                inv.setName(uniqueName);
                inv.setDescription(dto.getDescription());
                inv.setManufacturerName(dto.getManufacturerName());

                // FIX: Sanitize HSN code before setting to ensure it meets validation constraints
                String sanitizedHsn = sanitizeHsnCode(dto.getHsnCode());
                inv.setHsnCode(sanitizedHsn);
                log.debug("Original HSN: '{}', Sanitized HSN: '{}'", dto.getHsnCode(), sanitizedHsn);

                if (dto.getPurchaseRate() != null) {
                    inv.setPurchaseRate(new BigDecimal(dto.getPurchaseRate().toString()));
                }

                if (dto.getSellingRate() != null) {
                    inv.setSellingRate(new BigDecimal(dto.getSellingRate().toString()));
                }

                if (dto.getGstPercentage() != null) {
                    inv.setGstPercentage(convertGst(dto.getGstPercentage()));
                }

                inv.setDriverName(dto.getDriverName());
                inv.setDriverNumber(dto.getDriverNumber());
                inv.setUnitOfMeasurementName(dto.getUnitOfMeasurementName());

                if (dto.getUnitOfMeasurementName() != null) {
                    unitOfMeasurementRepository.findByNameIgnoreCase(dto.getUnitOfMeasurementName())
                            .ifPresent(inv::setUnitOfMeasurement);
                }

                if (dto.getCategoryName() != null) {
                    inv.setCategory(findOrCreateCategory(dto.getCategoryName()));
                }

                if (dto.getSubcategoryName() != null && inv.getCategory() != null) {
                    inv.setSubCategory(findOrCreateSubCategory(dto.getSubcategoryName(), inv.getCategory()));
                }

                inv.setStatus(RecordStatusEnum.ACTIVE);
                saved.add(inventoryRepository.save(inv));
                successCount++;
                log.info("Successfully saved item {}/{}: {} (HSN: {})", successCount, totalItems, uniqueName, sanitizedHsn);

            } catch (Exception e) {
                failureCount++;
                log.error("Failed to save item {}/{} - Name: '{}', HSN: '{}', Error: {}",
                        (successCount + failureCount), totalItems, dto.getName(), dto.getHsnCode(), e.getMessage(), e);
                // Continue processing remaining items instead of failing the entire batch
            }
        }

        log.info("Inventory save completed. Total: {}, Success: {}, Failed: {}", totalItems, successCount, failureCount);
        return saved;
    }

    private GstPercentageEnum convertGst(Integer gst) {
        return switch (gst == null ? 18 : gst) {
            case 0 -> GstPercentageEnum.GST_0;
            case 5 -> GstPercentageEnum.GST_5;
            case 12 -> GstPercentageEnum.GST_12;
            case 18 -> GstPercentageEnum.GST_18;
            case 28 -> GstPercentageEnum.GST_28;
            default -> {
                log.warn("Unknown GST percentage: {}%, defaulting to 18%", gst);
                yield GstPercentageEnum.GST_18;
            }
        };
    }

    @Transactional
    private CategoryEntity findOrCreateCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            CategoryEntity cat = new CategoryEntity();
            cat.setName(name);
            cat.setDescription("Auto-created from invoice extraction");
            cat.setStatus(RecordStatusEnum.ACTIVE);
            CategoryEntity saved = categoryRepository.save(cat);
            log.info("Created new category: {}", name);
            return saved;
        });
    }

    @Transactional
    private SubCategoryEntity findOrCreateSubCategory(String name, CategoryEntity category) {
        return subCategoryRepository.findByNameAndCategoryId(name, category.getId()).orElseGet(() -> {
            SubCategoryEntity sub = new SubCategoryEntity();
            sub.setName(name);
            sub.setDescription("Auto-created from invoice extraction");
            sub.setCategory(category);
            sub.setStatus(RecordStatusEnum.ACTIVE);
            SubCategoryEntity saved = subCategoryRepository.save(sub);
            log.info("Created new subcategory: {} under category: {}", name, category.getName());
            return saved;
        });
    }
}