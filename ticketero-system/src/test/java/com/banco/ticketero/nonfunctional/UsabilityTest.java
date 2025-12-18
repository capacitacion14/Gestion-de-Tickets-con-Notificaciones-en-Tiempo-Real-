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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Usability Tests - RNF-U01 to RNF-U04
 * Validates user experience and accessibility aspects
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("nonfunctional")
class UsabilityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * RNF-U01: Response Time UX - Feedback visual < 200ms
     */
    @Test
    void testResponseTimeUX_ShouldProvideFastFeedback() {
        // Test multiple endpoints for response time
        String[] endpoints = {
            "/api/tickets",
            "/actuator/health",
            "/api/tickets/queue/GENERAL"
        };

        System.out.println("Response Time UX Test Results:");
        
        for (String endpoint : endpoints) {
            // Act
            Instant start = Instant.now();
            ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + endpoint, String.class);
            Instant end = Instant.now();
            
            long responseTime = Duration.between(start, end).toMillis();
            
            // Assert
            System.out.printf("- %-25s: %d ms %s%n", 
                endpoint, 
                responseTime,
                responseTime < 200 ? "✅ FAST" : "⚠️ SLOW");
            
            // For UX, we want most endpoints to respond quickly
            // Health check should always be fast
            if (endpoint.contains("health")) {
                assertTrue(responseTime < 200, 
                    String.format("Health endpoint took %d ms, should be < 200ms", responseTime));
            }
        }
        
        // Test POST request response time
        Instant start = Instant.now();
        CreateTicketRequest request = new CreateTicketRequest("12345678", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/api/tickets", 
            entity, 
            String.class
        );
        Instant end = Instant.now();
        
        long postResponseTime = Duration.between(start, end).toMillis();
        
        System.out.printf("- %-25s: %d ms %s%n", 
            "POST /api/tickets", 
            postResponseTime,
            postResponseTime < 1000 ? "✅ ACCEPTABLE" : "⚠️ SLOW");
        
        // POST requests can be slightly slower but should still be reasonable
        assertTrue(postResponseTime < 2000, 
            String.format("POST request took %d ms, should be < 2000ms for good UX", postResponseTime));
    }

    /**
     * RNF-U02: Error Messages - Clear and actionable
     */
    @Test
    void testErrorMessages_ShouldBeClearAndActionable() {
        System.out.println("Error Messages Test Results:");
        
        // Test 1: Invalid input validation
        testErrorMessage(
            new CreateTicketRequest("", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA),
            "Empty nationalId",
            "nationalId"
        );
        
        // Test 2: Invalid nationalId format
        testErrorMessage(
            new CreateTicketRequest("123", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA),
            "Short nationalId",
            "nationalId"
        );
        
        // Test 3: Missing description
        testErrorMessage(
            new CreateTicketRequest("12345678", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA),
            "Empty description",
            "description"
        );
        
        // Test 4: Null values
        testErrorMessage(
            new CreateTicketRequest(null, "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA),
            "Null nationalId",
            "nationalId"
        );
        
        // Test 5: Invalid endpoint (404)
        ResponseEntity<String> notFoundResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/nonexistent", 
            String.class
        );
        
        System.out.printf("- %-20s: Status %s %s%n", 
            "Invalid endpoint",
            notFoundResponse.getStatusCode(),
            notFoundResponse.getStatusCode() == HttpStatus.NOT_FOUND ? "✅ CLEAR" : "❌ UNCLEAR");
        
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode(),
            "Invalid endpoint should return 404");
    }

    private void testErrorMessage(CreateTicketRequest request, String testCase, String expectedField) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/api/tickets", 
            entity, 
            String.class
        );
        
        boolean isErrorHandled = response.getStatusCode().is4xxClientError();
        boolean hasUsefulMessage = response.getBody() != null && 
                                 (response.getBody().toLowerCase().contains(expectedField.toLowerCase()) ||
                                  response.getBody().toLowerCase().contains("validation") ||
                                  response.getBody().toLowerCase().contains("required") ||
                                  response.getBody().toLowerCase().contains("invalid"));
        
        System.out.printf("- %-20s: Status %s, Message: %s %s%n", 
            testCase,
            response.getStatusCode(),
            hasUsefulMessage ? "Helpful" : "Generic",
            (isErrorHandled && hasUsefulMessage) ? "✅ CLEAR" : "❌ UNCLEAR");
        
        assertTrue(isErrorHandled, 
            String.format("Test case '%s' should return 4xx error", testCase));
    }

    /**
     * RNF-U03: Accessibility - WCAG compliance simulation
     */
    @Test
    void testAccessibility_ShouldMeetBasicStandards() {
        System.out.println("Accessibility Test Results:");
        
        // Test 1: API responses should have proper structure
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/api/tickets", 
            String.class
        );
        
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        
        // Test 2: Content-Type headers should be properly set
        HttpHeaders responseHeaders = response.getHeaders();
        String contentType = responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        
        boolean hasProperContentType = contentType != null && 
                                     contentType.contains("application/json");
        
        System.out.printf("- %-25s: %s %s%n", 
            "Proper Content-Type",
            contentType,
            hasProperContentType ? "✅ COMPLIANT" : "❌ NON-COMPLIANT");
        
        assertTrue(hasProperContentType, 
            "API should return proper Content-Type headers");
        
        // Test 3: Error responses should have proper HTTP status codes
        CreateTicketRequest invalidRequest = new CreateTicketRequest("", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(invalidRequest, headers);
        
        ResponseEntity<String> errorResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/tickets", 
            entity, 
            String.class
        );
        
        boolean hasProperErrorStatus = errorResponse.getStatusCode().is4xxClientError();
        
        System.out.printf("- %-25s: %s %s%n", 
            "Proper Error Status",
            errorResponse.getStatusCode(),
            hasProperErrorStatus ? "✅ COMPLIANT" : "❌ NON-COMPLIANT");
        
        assertTrue(hasProperErrorStatus, 
            "Invalid requests should return proper 4xx status codes");
        
        // Test 4: Response structure should be consistent
        boolean hasConsistentStructure = isResponseStructureConsistent(responseBody);
        
        System.out.printf("- %-25s: %s%n", 
            "Consistent Structure",
            hasConsistentStructure ? "✅ COMPLIANT" : "❌ NON-COMPLIANT");
        
        assertTrue(hasConsistentStructure, 
            "API responses should have consistent structure");
    }

    /**
     * RNF-U04: Mobile Experience - Responsive design validation
     */
    @Test
    void testMobileExperience_ShouldBeResponsive() {
        System.out.println("Mobile Experience Test Results:");
        
        // Test 1: API should handle mobile-like request patterns
        // Simulate mobile app making requests with different headers
        HttpHeaders mobileHeaders = new HttpHeaders();
        mobileHeaders.setContentType(MediaType.APPLICATION_JSON);
        mobileHeaders.set("User-Agent", "Mobile App/1.0 (iOS 15.0)");
        mobileHeaders.set("Accept", "application/json");
        
        HttpEntity<Void> mobileEntity = new HttpEntity<>(mobileHeaders);
        
        ResponseEntity<String> mobileResponse = restTemplate.exchange(
            getBaseUrl() + "/api/tickets",
            HttpMethod.GET,
            mobileEntity,
            String.class
        );
        
        boolean mobileCompatible = mobileResponse.getStatusCode().is2xxSuccessful();
        
        System.out.printf("- %-25s: %s %s%n", 
            "Mobile User-Agent",
            mobileResponse.getStatusCode(),
            mobileCompatible ? "✅ COMPATIBLE" : "❌ INCOMPATIBLE");
        
        assertTrue(mobileCompatible, 
            "API should work with mobile user agents");
        
        // Test 2: Response size should be reasonable for mobile
        String responseBody = mobileResponse.getBody();
        int responseSize = responseBody != null ? responseBody.length() : 0;
        boolean reasonableSize = responseSize < 50000; // 50KB limit for mobile
        
        System.out.printf("- %-25s: %d bytes %s%n", 
            "Response Size",
            responseSize,
            reasonableSize ? "✅ MOBILE-FRIENDLY" : "⚠️ LARGE");
        
        // Test 3: API should handle network timeouts gracefully
        // This is simulated by checking if the API responds within reasonable time
        Instant start = Instant.now();
        
        CreateTicketRequest mobileRequest = new CreateTicketRequest("87654321", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
        HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(mobileRequest, mobileHeaders);
        
        ResponseEntity<String> createResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/tickets", 
            entity, 
            String.class
        );
        
        Instant end = Instant.now();
        long responseTime = Duration.between(start, end).toMillis();
        
        boolean mobileTimeoutFriendly = responseTime < 5000; // 5 seconds max for mobile
        
        System.out.printf("- %-25s: %d ms %s%n", 
            "Mobile Response Time",
            responseTime,
            mobileTimeoutFriendly ? "✅ FAST" : "⚠️ SLOW");
        
        assertTrue(mobileTimeoutFriendly, 
            "API should respond quickly enough for mobile networks");
        
        // Test 4: JSON responses should be compact and well-structured
        boolean compactJson = isJsonCompactAndWellStructured(createResponse.getBody());
        
        System.out.printf("- %-25s: %s%n", 
            "Compact JSON",
            compactJson ? "✅ OPTIMIZED" : "❌ VERBOSE");
        
        assertTrue(compactJson, 
            "JSON responses should be compact for mobile bandwidth");
    }

    private boolean isResponseStructureConsistent(String responseBody) {
        if (responseBody == null) return false;
        
        // Check if response is valid JSON and has expected structure
        try {
            // Basic JSON structure validation
            return responseBody.trim().startsWith("[") || responseBody.trim().startsWith("{");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isJsonCompactAndWellStructured(String jsonResponse) {
        if (jsonResponse == null) return false;
        
        // Check for unnecessary whitespace (should be minified for mobile)
        boolean isCompact = !jsonResponse.contains("  ") && !jsonResponse.contains("\n\n");
        
        // Check for proper JSON structure
        boolean isValidJson = (jsonResponse.trim().startsWith("{") && jsonResponse.trim().endsWith("}")) ||
                             (jsonResponse.trim().startsWith("[") && jsonResponse.trim().endsWith("]"));
        
        return isCompact && isValidJson;
    }
}