package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjmt.SJMT.DTO.ResponseDTO.InvoiceItemDTO;
import com.sjmt.SJMT.DTO.ResponseDTO.InvoiceSaveResult;
import com.sjmt.SJMT.Entity.*;
import com.sjmt.SJMT.Repository.CategoryRepository;
import com.sjmt.SJMT.Repository.InventoryRepository;
import com.sjmt.SJMT.Repository.ProductMasterRepository;
import com.sjmt.SJMT.Repository.SubCategoryRepository;
import com.sjmt.SJMT.Repository.UnitOfMeasurementRepository;
import com.sjmt.SJMT.Repository.UserRepository;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

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
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import java.time.Duration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final ProductMasterRepository productMasterRepository;
    private final StockService stockService;
    private final Validator validator;
    private final UserRepository userRepository;

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

    @Value("${invoice.storage.path:C:\\Billing_Cement_Folder_Saving\\pdf_storage}")
    private String invoiceStoragePath;

    public InvoiceExtractionService(WebClient.Builder webClientBuilder,
                                    InventoryRepository inventoryRepository,
                                    CategoryRepository categoryRepository,
                                    SubCategoryRepository subCategoryRepository,
                                    UnitOfMeasurementRepository unitOfMeasurementRepository,
                                    ProductMasterRepository productMasterRepository,
                                    StockService stockService,
                                    Validator validator,
                                    UserRepository userRepository) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(60));
        this.webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.unitOfMeasurementRepository = unitOfMeasurementRepository;
        this.productMasterRepository = productMasterRepository;
        this.stockService = stockService;
        this.validator = validator;
        this.userRepository = userRepository;
    }

    private UserEntity getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
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

        Map<String, Object> response = webClient.post()
                .uri(GEMINI_UPLOAD_URL)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

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
                List.of(Map.of("parts", List.of(
                        Map.of("fileData", Map.of("mimeType", "application/pdf", "fileUri", fileUri)),
                        Map.of("text", EXTRACTION_PROMPT)))));
        String generateUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
        Map<String, Object> response = webClient.post()
                .uri(generateUrl)
                .header("x-goog-api-key", apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (response == null || !response.containsKey("candidates")) {
            if (response != null && response.containsKey("promptFeedback")) {
                Map<String, Object> feedback = (Map<String, Object>) response.get("promptFeedback");
                if ("BLOCK".equals(feedback.get("blockReason"))) {
                    throw new RuntimeException("Request was blocked by Gemini safety filters.");
                }
            }
            throw new RuntimeException("Failed to extract invoice items from PDF via Gemini API.");
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Failed to extract invoice items from PDF: Empty candidates returned from Gemini, possibly due to safety blockers.");
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            throw new RuntimeException("Content missing from Gemini response. Possibly blocked by safety settings.");
        }
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Parts missing from Gemini response.");
        }
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

    private String sanitizeHsnCode(String hsnCode) {
        if (hsnCode == null || hsnCode.isBlank()) {
            log.warn("HSN code is null or blank, using default '0000'");
            return "0000";
        }
        String digitsOnly = hsnCode.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            log.warn("HSN code '{}' contains no digits, using default '0000'", hsnCode);
            return "0000";
        }
        int length = digitsOnly.length();
        if (length == 4 || length == 6 || length == 8) return digitsOnly;
        if (length < 4) return String.format("%04d", Integer.parseInt(digitsOnly));
        if (length == 5) return digitsOnly.substring(0, 4);
        if (length == 7) return digitsOnly.substring(0, 6);
        return digitsOnly.substring(0, 8);
    }

    public String savePdfToDisk(MultipartFile pdfFile, Integer inventoryId) throws IOException {
        File storageDir = new File(invoiceStoragePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
            log.info("Created invoice storage directory: {}", invoiceStoragePath);
        }

        String originalName = pdfFile.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "invoice_" + System.currentTimeMillis();
        }
        // Fix Path Traversal by sanitizing filename
        originalName = originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        if (originalName.contains(".")) {
            originalName = originalName.substring(0, originalName.lastIndexOf('.'));
        }
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = originalName + "_" + inventoryId + "_" + date + ".pdf";

        Path targetPath = Paths.get(invoiceStoragePath, fileName);
        Files.write(targetPath, pdfFile.getBytes());
        log.info("PDF saved to: {}", targetPath.toAbsolutePath());

        return targetPath.toAbsolutePath().toString();
    }

    /**
     * Save extracted invoice items to inventory (purchase records) and update
     * the centralized ProductMaster stock for each item.
     */
    @Transactional
    public InvoiceSaveResult saveToInventory(List<InvoiceItemDTO> items, MultipartFile pdfFile) {
        List<InventoryEntity> saved = new ArrayList<>();
        List<String> failureMessages = new ArrayList<>();
        int totalItems = items.size();
        int successCount = 0;
        int failureCount = 0;

        log.info("Starting to save {} invoice items to inventory", totalItems);

        // PDF path is same for all items from one upload — save once, reuse
        String pdfPath = null;
        UserEntity currentUser = getCurrentUser();

        for (InvoiceItemDTO dto : items) {
            java.util.Set<ConstraintViolation<InvoiceItemDTO>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMsg = violations.iterator().next().getMessage();
                failureMessages.add("Validation failed for item: " + errorMsg);
                failureCount++;
                continue;
            }

            InventoryEntity inv = new InventoryEntity();

                String uniqueName = dto.getName() + " - " + dto.getSubcategoryName()
                        + " (" + dto.getInvoiceNumber() + ")";
                inv.setName(uniqueName);
                inv.setDescription(dto.getDescription());
                inv.setManufacturerName(dto.getManufacturerName());

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

                // Quantity extracted from bill — now stored
                BigDecimal quantity = dto.getQuantity();
                inv.setQuantity(quantity);

                UnitOfMeasurementEntity unit = null;
                if (dto.getUnitOfMeasurementName() != null && !dto.getUnitOfMeasurementName().isBlank()) {
                    String uomName = dto.getUnitOfMeasurementName().trim();
                    unit = unitOfMeasurementRepository.findByNameIgnoreCase(uomName)
                            .orElseGet(() -> {
                                String cleanName = uomName.substring(0, 1).toUpperCase() + uomName.substring(1).toLowerCase();
                                String abbreviation = uomName.toUpperCase();
                                if (abbreviation.length() > 5) {
                                    abbreviation = abbreviation.substring(0, 5);
                                }
                                UnitOfMeasurementEntity newUom = new UnitOfMeasurementEntity(
                                        cleanName, abbreviation, "Auto-created from invoice extraction");
                                UnitOfMeasurementEntity savedUom = unitOfMeasurementRepository.save(newUom);
                                log.info("Auto-created unit of measurement: {}", uomName);
                                return savedUom;
                            });
                    inv.setUnitOfMeasurement(unit);
                }

                CategoryEntity category = null;
                if (dto.getCategoryName() != null) {
                    category = findOrCreateCategory(dto.getCategoryName());
                    inv.setCategory(category);
                }

                SubCategoryEntity subCategory = null;
                if (dto.getSubcategoryName() != null && category != null) {
                    subCategory = findOrCreateSubCategory(dto.getSubcategoryName(), category);
                    inv.setSubCategory(subCategory);
                }

                // ── Find or create the ProductMaster (centralized stock slot) ────────
                if (category != null && subCategory != null) {
                    ProductMasterEntity productMaster = stockService.findOrCreateProductMaster(
                            category, subCategory, sanitizedHsn, unit);
                    inv.setProductMaster(productMaster);

                    // Add incoming quantity to the product master's stock
                    if (quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
                        stockService.addStock(productMaster, quantity, inv.getPurchaseRate());
                    }
                }

                inv.setStatus(RecordStatusEnum.ACTIVE);
                inv.setCreatedBy(currentUser);
                inv.setUpdatedBy(currentUser);

                // Save inventory (purchase record) first to get generated PK
                InventoryEntity savedInv = inventoryRepository.save(inv);
                successCount++;

                // Save PDF to disk on first item only, reuse path for rest
                if (pdfFile != null) {
                    try {
                        if (pdfPath == null) {
                            pdfPath = savePdfToDisk(pdfFile, savedInv.getId());
                        }
                        savedInv.setInvoicePdfPath(pdfPath);
                        inventoryRepository.save(savedInv);
                        log.info("Linked PDF path to inventory ID {}: {}", savedInv.getId(), pdfPath);
                    } catch (IOException e) {
                        log.error("Failed to save PDF for inventory ID {}: {}", savedInv.getId(), e.getMessage());
                    }
                }

                saved.add(savedInv);
                log.info("Successfully saved item {}/{}: {}", successCount, totalItems, uniqueName);
        }

        log.info("Inventory save completed. Total: {}, Success: {}, Failed: {}",
                totalItems, successCount, failureCount);

        List<Integer> savedIds = saved.stream()
                .map(InventoryEntity::getId)
                .collect(java.util.stream.Collectors.toList());
        return new InvoiceSaveResult(savedIds, successCount, failureCount, failureMessages);
    }

    public byte[] getPdfByInventoryId(Integer inventoryId) throws IOException {
        InventoryEntity inv = inventoryRepository.findByIdWithPdf(inventoryId)
                .orElseThrow(() -> new RuntimeException(
                        "No PDF found for inventory ID: " + inventoryId));

        Path baseDir = Paths.get(invoiceStoragePath).toAbsolutePath().normalize();
        Path pdfPath = Paths.get(inv.getInvoicePdfPath()).toAbsolutePath().normalize();
        
        if (!pdfPath.startsWith(baseDir)) {
            throw new SecurityException("Path traversal attempt detected");
        }
        
        if (!Files.exists(pdfPath)) {
            throw new ResourceNotFoundException("PDF file not found on disk: " + pdfPath);
        }
        log.info("Serving PDF for inventory ID {}: {}", inventoryId, pdfPath);
        return Files.readAllBytes(pdfPath);
    }

    public List<InventoryEntity> getInventoryWithPdfByMonth(int year, int month) {
        return inventoryRepository.findByInvoicePdfMonthAndYear(year, month);
    }

    public List<InventoryEntity> getInventoryWithPdfByDateRange(LocalDateTime from, LocalDateTime to) {
        return inventoryRepository.findByInvoicePdfDateRange(from, to);
    }

    private GstPercentageEnum convertGst(Integer gst) {
        return switch (gst == null ? 18 : gst) {
            case 0  -> GstPercentageEnum.GST_0;
            case 5  -> GstPercentageEnum.GST_5;
            case 12 -> GstPercentageEnum.GST_12;
            case 18 -> GstPercentageEnum.GST_18;
            case 28 -> GstPercentageEnum.GST_28;
            default -> {
                log.warn("Unknown GST percentage: {}%, defaulting to 18%", gst);
                yield GstPercentageEnum.GST_18;
            }
        };
    }

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
