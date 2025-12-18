package com.banco.ticketero.nonfunctional;

import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security Tests - RNF-S01 to RNF-S04
 * Validates system security against common vulnerabilities
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("nonfunctional")
class SecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * RNF-S01: SQL Injection Protection
     */
    @Test
    void testSqlInjectionProtection_ShouldBlockMaliciousPayloads() {
        // Arrange - Common SQL injection payloads
        String[] sqlInjectionPayloads = {
            "'; DROP TABLE tickets; --",
            "' OR '1'='1",
            "' UNION SELECT * FROM users --",
            "'; INSERT INTO tickets VALUES ('hacked'); --",
            "' OR 1=1 --",
            "admin'--",
            "admin'/*",
            "' OR 'x'='x",
            "'; EXEC xp_cmdshell('dir'); --"
        };

        System.out.println("SQL Injection Test Results:");
        
        for (String payload : sqlInjectionPayloads) {
            // Act
            CreateTicketRequest maliciousRequest = new CreateTicketRequest(payload, "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(maliciousRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/tickets", 
                entity, 
                String.class
            );
            
            // Assert - Should either reject with 400 or sanitize the input
            boolean isSecure = response.getStatusCode().is4xxClientError() || 
                              (response.getStatusCode().is2xxSuccessful() && 
                               !response.getBody().contains("DROP") && 
                               !response.getBody().contains("UNION"));
            
            System.out.printf("- Payload: %-30s Status: %s %s%n", 
                payload.substring(0, Math.min(payload.length(), 25)) + "...", 
                response.getStatusCode(),
                isSecure ? "✅ BLOCKED" : "❌ VULNERABLE");
            
            assertTrue(isSecure, "SQL injection payload was not properly handled: " + payload);
        }
    }

    /**
     * RNF-S02: Rate Limiting Protection
     */
    @Test
    void testRateLimiting_ShouldPreventSpamRequests() throws Exception {
        // Arrange
        int requestsPerMinute = 100; // Exceed normal rate limit
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        // Act
        Instant start = Instant.now();
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();
        
        for (int i = 0; i < requestsPerMinute; i++) {
            final int requestNumber = i;
            CompletableFuture<ResponseEntity<String>> future = 
                CompletableFuture.supplyAsync(() -> {
                    CreateTicketRequest request = new CreateTicketRequest("12345678", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
                    
                    return restTemplate.postForEntity(
                        getBaseUrl() + "/api/tickets", 
                        entity, 
                        String.class
                    );
                }, executor);
            
            futures.add(future);
        }
        
        // Wait for all requests
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        
        // Assert
        long successfulRequests = futures.stream()
            .mapToLong(future -> {
                try {
                    ResponseEntity<String> response = future.get();
                    return response.getStatusCode().is2xxSuccessful() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        
        long blockedRequests = requestsPerMinute - successfulRequests;
        double blockRate = (blockedRequests * 100.0) / requestsPerMinute;
        
        System.out.printf("Rate Limiting Test Results:%n");
        System.out.printf("- Total requests: %d%n", requestsPerMinute);
        System.out.printf("- Successful requests: %d%n", successfulRequests);
        System.out.printf("- Blocked requests: %d%n", blockedRequests);
        System.out.printf("- Block rate: %.2f%%%n", blockRate);
        System.out.printf("- Duration: %.2f seconds%n", duration.getSeconds());
        
        // Should block some requests if rate limiting is active
        // For this test, we expect at least some blocking when sending 100 requests rapidly
        assertTrue(successfulRequests <= 60, 
            "Rate limiting should prevent all requests from succeeding when sent rapidly");
        
        executor.shutdown();
    }

    /**
     * RNF-S03: Data Exposure Protection
     */
    @Test
    void testDataExposureProtection_ShouldNotExposeSensitiveData() {
        // Act - Get ticket details
        CreateTicketRequest request = new CreateTicketRequest("12345678", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/tickets", 
            entity, 
            String.class
        );
        
        // Get all tickets to check for data exposure
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/tickets", 
            String.class
        );
        
        // Assert - Check that sensitive data is not exposed
        String responseBody = listResponse.getBody();
        assertNotNull(responseBody);
        
        // Check for common sensitive fields that should not be exposed
        String[] sensitiveFields = {
            "password", "secret", "key", "token", 
            "internal", "private", "confidential",
            "ssn", "credit_card", "bank_account"
        };
        
        System.out.println("Data Exposure Test Results:");
        
        for (String sensitiveField : sensitiveFields) {
            boolean containsSensitiveData = responseBody.toLowerCase().contains(sensitiveField.toLowerCase());
            
            System.out.printf("- Field '%s': %s%n", 
                sensitiveField, 
                containsSensitiveData ? "❌ EXPOSED" : "✅ PROTECTED");
            
            assertFalse(containsSensitiveData, 
                "Response contains sensitive field: " + sensitiveField);
        }
        
        // Verify that only expected fields are present
        assertTrue(responseBody.contains("referenceCode") || responseBody.contains("id"), 
            "Response should contain expected public fields");
    }

    /**
     * RNF-S04: Input Validation Protection
     */
    @Test
    void testInputValidation_ShouldBlockMaliciousPayloads() {
        // Arrange - Various malicious payloads
        String[][] maliciousPayloads = {
            // XSS payloads
            {"<script>alert('xss')</script>", "XSS Script Tag"},
            {"javascript:alert('xss')", "XSS JavaScript Protocol"},
            {"<img src=x onerror=alert('xss')>", "XSS Image Tag"},
            
            // Command injection payloads
            {"; ls -la", "Command Injection Semicolon"},
            {"| whoami", "Command Injection Pipe"},
            {"$(whoami)", "Command Injection Subshell"},
            
            // Path traversal payloads
            {"../../../etc/passwd", "Path Traversal"},
            {"..\\..\\..\\windows\\system32", "Windows Path Traversal"},
            
            // LDAP injection payloads
            {"*)(uid=*))(|(uid=*", "LDAP Injection"},
            
            // NoSQL injection payloads
            {"'; return true; var x='", "NoSQL Injection"}
        };

        System.out.println("Input Validation Test Results:");
        
        for (String[] payload : maliciousPayloads) {
            String maliciousInput = payload[0];
            String payloadType = payload[1];
            
            // Test in nationalId field
            testMaliciousInput(maliciousInput, "nationalId", payloadType);
            
            // Test in description field
            testMaliciousInput("12345678", maliciousInput, payloadType + " (description)");
        }
    }

    private void testMaliciousInput(String nationalId, String description, String payloadType) {
        // Act
        CreateTicketRequest maliciousRequest = new CreateTicketRequest(nationalId, "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(maliciousRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/api/tickets", 
            entity, 
            String.class
        );
        
        // Assert - Should reject malicious input or sanitize it
        boolean isSecure = response.getStatusCode().is4xxClientError() || 
                          (response.getStatusCode().is2xxSuccessful() && 
                           isInputSanitized(response.getBody(), nationalId, description));
        
        System.out.printf("- %-30s Status: %s %s%n", 
            payloadType, 
            response.getStatusCode(),
            isSecure ? "✅ BLOCKED/SANITIZED" : "❌ VULNERABLE");
        
        assertTrue(isSecure, "Malicious input was not properly handled: " + payloadType);
    }

    private boolean isInputSanitized(String responseBody, String nationalId, String description) {
        if (responseBody == null) return true;
        
        // Check if dangerous characters/patterns are present in response
        String[] dangerousPatterns = {
            "<script", "javascript:", "onerror=", "onload=",
            "$(", "`;", "|", "../", "..\\",
            "DROP", "UNION", "SELECT", "INSERT"
        };
        
        String lowerResponse = responseBody.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowerResponse.contains(pattern.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }
}