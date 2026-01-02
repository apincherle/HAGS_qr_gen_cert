package com.hags.qrcert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hags.qrcert.entity.CardCertificate;
import com.hags.qrcert.repository.CardCertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QrCertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardCertificateRepository certificateRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String testCustomerId;  // UUID as String
    private String testSubmissionId;  // UUID as String
    private String testItemId;  // UUID as String

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        certificateRepository.deleteAll();
        
        // Create test data: customer, submission, and item
        // customer_id, submission_id, and item_id are all UUIDs
        
        // Check required columns for customers table
        List<String> requiredCustomerColumns = jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns " +
            "WHERE table_name = 'customers' AND is_nullable = 'NO' AND column_default IS NULL",
            String.class);
        
        // Check required columns for submissions table
        List<String> requiredSubmissionColumns = jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns " +
            "WHERE table_name = 'submissions' AND is_nullable = 'NO' AND column_default IS NULL",
            String.class);
        
        // Check required columns for submission_items table
        List<String> requiredItemColumns = jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns " +
            "WHERE table_name = 'submission_items' AND is_nullable = 'NO' AND column_default IS NULL",
            String.class);
        
        // Create a test customer (UUID) - include all required fields
        // Use CURRENT_TIMESTAMP in SQL to handle timestamp properly
        testCustomerId = jdbcTemplate.queryForObject(
            "INSERT INTO customers (customer_id, full_name, email, marketing_opt_in, status, created_at, updated_at) " +
            "VALUES (gen_random_uuid(), ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING customer_id::text",
            String.class, "Test Customer", "test@example.com", false, "ACTIVE");
        
        // Create a test submission linked to the customer
        // submission_id is a UUID primary key, so we need to generate it
        // service_level, status, and updated_at are required (NOT NULL constraints)
        // Try common service level values - if this fails, we may need to query the constraint
        testSubmissionId = jdbcTemplate.queryForObject(
            "INSERT INTO submissions (submission_id, customer_id, service_level, status, created_at, updated_at) " +
            "VALUES (gen_random_uuid(), ?::uuid, 'BRONZE', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING submission_id::text",
            String.class, testCustomerId);
        
        // Create a test submission item linked to the submission
        // item_id is a UUID primary key, so we need to generate it
        // enrichment_status, free_text_line, game, line_number, and requested_photo_slots are required (NOT NULL constraints)
        // Try to find a valid game value by querying the constraint or using a common value
        String gameValue = null;
        try {
            // Query the constraint definition to extract allowed values
            String constraintDef = jdbcTemplate.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint " +
                "WHERE conname = 'submission_items_game_check' AND conrelid = 'submission_items'::regclass",
                String.class);
            // Parse common constraint patterns like: game IN ('POKEMON', 'MTG', ...) or game = ANY(ARRAY[...])
            if (constraintDef != null) {
                // Try to extract values from IN clause
                if (constraintDef.contains("IN")) {
                    // Extract first value from IN clause as fallback
                    int inStart = constraintDef.indexOf("IN (");
                    if (inStart > 0) {
                        int valueStart = constraintDef.indexOf("'", inStart) + 1;
                        int valueEnd = constraintDef.indexOf("'", valueStart);
                        if (valueEnd > valueStart) {
                            gameValue = constraintDef.substring(valueStart, valueEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore and try other methods
        }
        
        // If we couldn't get from constraint, try existing data
        if (gameValue == null) {
            try {
                List<String> existingGames = jdbcTemplate.queryForList(
                    "SELECT DISTINCT game FROM submission_items WHERE game IS NOT NULL LIMIT 1",
                    String.class);
                if (!existingGames.isEmpty()) {
                    gameValue = existingGames.get(0);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Final fallback - try common values
        if (gameValue == null) {
            // Try POKEMON in uppercase (common for enum-like constraints)
            gameValue = "POKEMON";
        }
        
        testItemId = jdbcTemplate.queryForObject(
            "INSERT INTO submission_items (item_id, submission_id, enrichment_status, free_text_line, game, line_number, requested_photo_slots, created_at) " +
            "VALUES (gen_random_uuid(), ?::uuid, 'PENDING', 'Test Item', ?, 1, 0, CURRENT_TIMESTAMP) RETURNING item_id::text",
            String.class, testSubmissionId, gameValue);
    }

    @Test
    void testCreateAndRetrieveCertificate() throws Exception {
        // Prepare certificate creation request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("submissionId", testSubmissionId);
        requestBody.put("customerId", testCustomerId);
        requestBody.put("itemId", testItemId);
        requestBody.put("cardName", "Charizard");
        requestBody.put("setName", "Base Set");
        requestBody.put("year", 1999);
        requestBody.put("cardNumber", "4");
        requestBody.put("variant", "Holo");
        requestBody.put("grade", 9.5);
        requestBody.put("graderVersion", "v1.0");
        requestBody.put("gradedAt", LocalDateTime.now().toString());
        requestBody.put("notesPublic", "Excellent condition");
        requestBody.put("status", "VERIFIED");

        // Create certificate
        String responseJson = mockMvc.perform(post("/api/qr-certificate/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.publicId", notNullValue()))
            .andExpect(jsonPath("$.serialNumber", notNullValue()))
            .andExpect(jsonPath("$.submissionId", is(testSubmissionId)))
            .andExpect(jsonPath("$.customerId", is(testCustomerId)))
            .andExpect(jsonPath("$.itemId", is(testItemId)))
            .andExpect(jsonPath("$.cardName", is("Charizard")))
            .andExpect(jsonPath("$.setName", is("Base Set")))
            .andExpect(jsonPath("$.year", is(1999)))
            .andExpect(jsonPath("$.cardNumber", is("4")))
            .andExpect(jsonPath("$.variant", is("Holo")))
            .andExpect(jsonPath("$.grade", is(9.5)))
            .andExpect(jsonPath("$.status", is("VERIFIED")))
            .andExpect(jsonPath("$.certificateUrl", notNullValue()))
            .andExpect(jsonPath("$.qrImageUrl", notNullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Extract publicId from response
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        String publicId = (String) response.get("publicId");

        // Retrieve certificate by publicId
        mockMvc.perform(get("/api/qr-certificate/{publicId}", publicId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.publicId", is(publicId)))
            .andExpect(jsonPath("$.submissionId", is(testSubmissionId)))
            .andExpect(jsonPath("$.customerId", is(testCustomerId)))
            .andExpect(jsonPath("$.itemId", is(testItemId)))
            .andExpect(jsonPath("$.cardName", is("Charizard")))
            .andExpect(jsonPath("$.setName", is("Base Set")))
            .andExpect(jsonPath("$.year", is(1999)))
            .andExpect(jsonPath("$.cardNumber", is("4")))
            .andExpect(jsonPath("$.variant", is("Holo")))
            .andExpect(jsonPath("$.grade", is(9.5)))
            .andExpect(jsonPath("$.status", is("VERIFIED")))
            .andExpect(jsonPath("$.certificateUrl", notNullValue()))
            .andExpect(jsonPath("$.qrImageUrl", notNullValue()));

        // Verify certificate exists in database
        CardCertificate certificate = certificateRepository.findByPublicId(publicId)
            .orElseThrow(() -> new AssertionError("Certificate not found in database"));
        
        assert certificate.getSubmissionId().equals(testSubmissionId);
        assert certificate.getCustomerId().equals(testCustomerId);
        assert certificate.getItemId().equals(testItemId);
        assert certificate.getCardName().equals("Charizard");
        assert certificate.getGrade().equals(9.5);
    }

    @Test
    void testGetQrCodeImage() throws Exception {
        // First create a certificate
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("submissionId", testSubmissionId);
        requestBody.put("customerId", testCustomerId);
        requestBody.put("itemId", testItemId);
        requestBody.put("cardName", "Pikachu");
        requestBody.put("setName", "Base Set");
        requestBody.put("year", 1999);
        requestBody.put("cardNumber", "58");
        requestBody.put("grade", 10.0);
        requestBody.put("status", "VERIFIED");

        String responseJson = mockMvc.perform(post("/api/qr-certificate/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        String publicId = (String) response.get("publicId");

        // Retrieve QR code image
        mockMvc.perform(get("/api/qr-certificate/qr/{publicId}", publicId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void testGetCertificateNotFound() throws Exception {
        mockMvc.perform(get("/api/qr-certificate/{publicId}", "NONEXISTENT"))
            .andExpect(status().isNotFound());
    }
}

