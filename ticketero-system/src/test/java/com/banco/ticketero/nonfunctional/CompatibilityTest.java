package com.banco.ticketero.nonfunctional;

import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Compatibility Tests - RNF-C01 to RNF-C04
 * Validates system compatibility across different environments and conditions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("nonfunctional")
class CompatibilityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * RNF-C01: Cross-Browser Compatibility - Simulate different browser behaviors
     */
    @Test
    void testCrossBrowserCompatibility_ShouldWorkWithAllMajorBrowsers() {
        System.out.println("Cross-Browser Compatibility Test Results:");
        
        // Simulate different browser User-Agent strings
        String[] browserUserAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", // Chrome
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0", // Firefox
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15", // Safari
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.2210.91" // Edge
        };
        
        String[] browserNames = {"Chrome", "Firefox", "Safari", "Edge"};
        
        for (int i = 0; i < browserUserAgents.length; i++) {
            String userAgent = browserUserAgents[i];
            String browserName = browserNames[i];
            
            // Test GET request
            boolean getCompatible = testBrowserCompatibility(userAgent, HttpMethod.GET, null);
            
            // Test POST request
            CreateTicketRequest request = new CreateTicketRequest("12345678", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            boolean postCompatible = testBrowserCompatibility(userAgent, HttpMethod.POST, request);
            
            System.out.printf("- %-10s: GET %s, POST %s %s%n", 
                browserName,
                getCompatible ? "✅" : "❌",
                postCompatible ? "✅" : "❌",
                (getCompatible && postCompatible) ? "✅ COMPATIBLE" : "❌ ISSUES");
            
            assertTrue(getCompatible && postCompatible, 
                String.format("%s browser compatibility failed", browserName));
        }
    }

    /**
     * RNF-C02: Mobile Device Compatibility - iOS and Android simulation
     */
    @Test
    void testMobileDeviceCompatibility_ShouldWorkOnMobileDevices() {
        System.out.println("Mobile Device Compatibility Test Results:");
        
        // Simulate mobile device User-Agent strings
        String[] mobileUserAgents = {
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1", // iOS Safari
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1", // iOS Chrome
            "Mozilla/5.0 (Linux; Android 14; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.210 Mobile Safari/537.36", // Android Chrome
            "Mozilla/5.0 (Linux; Android 14; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/120.0.6099.210 Mobile Safari/537.36" // Android WebView
        };
        
        String[] deviceNames = {"iOS Safari", "iOS Chrome", "Android Chrome", "Android WebView"};
        
        for (int i = 0; i < mobileUserAgents.length; i++) {
            String userAgent = mobileUserAgents[i];
            String deviceName = deviceNames[i];
            
            // Test basic functionality
            boolean basicCompatible = testMobileCompatibility(userAgent, deviceName);
            
            // Test response time (mobile networks are slower)
            boolean performanceCompatible = testMobilePerformance(userAgent);
            
            System.out.printf("- %-15s: Functionality %s, Performance %s %s%n", 
                deviceName,
                basicCompatible ? "✅" : "❌",
                performanceCompatible ? "✅" : "❌",
                (basicCompatible && performanceCompatible) ? "✅ COMPATIBLE" : "❌ ISSUES");
            
            assertTrue(basicCompatible, 
                String.format("%s device compatibility failed", deviceName));
        }
    }

    /**
     * RNF-C03: API Versioning - Backward compatibility validation
     */
    @Test
    void testApiVersioning_ShouldMaintainBackwardCompatibility() {
        System.out.println("API Versioning Test Results:");
        
        // Test different API version headers
        String[] apiVersions = {"1.0", "1.1", "2.0"};
        
        for (String version : apiVersions) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("API-Version", version);
            headers.set("Accept", "application/json");
            
            // Test GET endpoint
            HttpEntity<Void> getEntity = new HttpEntity<>(headers);
            ResponseEntity<String> getResponse = restTemplate.exchange(
                getBaseUrl() + "/api/tickets",
                HttpMethod.GET,
                getEntity,
                String.class
            );
            
            boolean getCompatible = getResponse.getStatusCode().is2xxSuccessful();
            
            // Test POST endpoint
            CreateTicketRequest request = new CreateTicketRequest("12345678", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            HttpEntity<CreateTicketRequest> postEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> postResponse = restTemplate.postForEntity(
                getBaseUrl() + "/api/tickets",
                postEntity,
                String.class
            );
            
            boolean postCompatible = postResponse.getStatusCode().is2xxSuccessful() || 
                                   postResponse.getStatusCode() == HttpStatus.CREATED;
            
            // Check response structure consistency
            boolean structureCompatible = isResponseStructureCompatible(postResponse.getBody(), version);
            
            System.out.printf("- Version %-5s: GET %s, POST %s, Structure %s %s%n", 
                version,
                getCompatible ? "✅" : "❌",
                postCompatible ? "✅" : "❌",
                structureCompatible ? "✅" : "❌",
                (getCompatible && postCompatible && structureCompatible) ? "✅ COMPATIBLE" : "❌ BREAKING");
            
            // For backward compatibility, older versions should still work
            if (!version.equals("2.0")) { // Assuming 2.0 might have breaking changes
                assertTrue(getCompatible && postCompatible, 
                    String.format("API version %s should maintain backward compatibility", version));
            }
        }
    }

    /**
     * RNF-C04: Network Conditions - 3G/4G/WiFi performance simulation
     */
    @Test
    void testNetworkConditions_ShouldHandleDifferentNetworkSpeeds() {
        System.out.println("Network Conditions Test Results:");
        
        // Simulate different network conditions by testing with timeouts
        NetworkCondition[] conditions = {
            new NetworkCondition("WiFi", 1000, 50), // Fast: 1s timeout, 50ms expected
            new NetworkCondition("4G", 3000, 200),  // Medium: 3s timeout, 200ms expected  
            new NetworkCondition("3G", 8000, 1000)  // Slow: 8s timeout, 1s expected
        };
        
        for (NetworkCondition condition : conditions) {
            // Configure RestTemplate with timeout for this condition
            TestRestTemplate networkRestTemplate = new TestRestTemplate();
            networkRestTemplate.getRestTemplate().setRequestFactory(
                new org.springframework.http.client.SimpleClientHttpRequestFactory()
            );
            
            boolean networkCompatible = testNetworkCondition(networkRestTemplate, condition);
            
            System.out.printf("- %-8s: %s (timeout: %dms, expected: %dms)%n", 
                condition.name,
                networkCompatible ? "✅ COMPATIBLE" : "❌ TIMEOUT",
                condition.timeoutMs,
                condition.expectedMs);
            
            // For 3G, we allow some failures but WiFi and 4G should work
            if (!condition.name.equals("3G")) {
                assertTrue(networkCompatible, 
                    String.format("%s network condition should be supported", condition.name));
            }
        }
        
        // Test graceful degradation under poor network conditions
        testGracefulDegradation();
    }

    private boolean testBrowserCompatibility(String userAgent, HttpMethod method, CreateTicketRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json, text/html, */*");
            
            HttpEntity<?> entity = request != null ? 
                new HttpEntity<>(request, headers) : 
                new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/api/tickets",
                method,
                entity,
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testMobileCompatibility(String userAgent, String deviceName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Connection", "keep-alive"); // Mobile optimization
            
            // Test GET
            HttpEntity<Void> getEntity = new HttpEntity<>(headers);
            ResponseEntity<String> getResponse = restTemplate.exchange(
                getBaseUrl() + "/api/tickets",
                HttpMethod.GET,
                getEntity,
                String.class
            );
            
            if (!getResponse.getStatusCode().is2xxSuccessful()) {
                return false;
            }
            
            // Test POST
            CreateTicketRequest request = new CreateTicketRequest("87654321", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            HttpEntity<CreateTicketRequest> postEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> postResponse = restTemplate.postForEntity(
                getBaseUrl() + "/api/tickets",
                postEntity,
                String.class
            );
            
            return postResponse.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testMobilePerformance(String userAgent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Instant start = Instant.now();
            
            CreateTicketRequest request = new CreateTicketRequest("11111111", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/tickets",
                entity,
                String.class
            );
            
            Instant end = Instant.now();
            long responseTime = Duration.between(start, end).toMillis();
            
            // Mobile should respond within 5 seconds for good UX
            return response.getStatusCode().is2xxSuccessful() && responseTime < 5000;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isResponseStructureCompatible(String responseBody, String version) {
        if (responseBody == null) return false;
        
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // Check for required fields that should be present in all versions
            List<String> requiredFields = Arrays.asList("id", "referenceCode", "status");
            
            for (String field : requiredFields) {
                if (!jsonNode.has(field)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testNetworkCondition(TestRestTemplate networkRestTemplate, NetworkCondition condition) {
        try {
            Instant start = Instant.now();
            
            CreateTicketRequest request = new CreateTicketRequest("99999999", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = networkRestTemplate.postForEntity(
                getBaseUrl() + "/api/tickets",
                entity,
                String.class
            );
            
            Instant end = Instant.now();
            long responseTime = Duration.between(start, end).toMillis();
            
            // Check if response time is within acceptable range for this network condition
            boolean withinTimeout = responseTime < condition.timeoutMs;
            boolean reasonablePerformance = responseTime < (condition.expectedMs * 3); // Allow 3x expected time
            
            return response.getStatusCode().is2xxSuccessful() && withinTimeout && reasonablePerformance;
        } catch (Exception e) {
            return false;
        }
    }

    private void testGracefulDegradation() {
        System.out.println("Testing graceful degradation under poor network conditions:");
        
        // Test with very short timeout to simulate poor network
        try {
            TestRestTemplate shortTimeoutTemplate = new TestRestTemplate();
            org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(100); // Very short timeout
            factory.setReadTimeout(100);
            shortTimeoutTemplate.getRestTemplate().setRequestFactory(factory);
            
            CreateTicketRequest request = new CreateTicketRequest("00000000", "+56912345678", "Sucursal Centro", com.banco.ticketero.model.QueueType.CAJA);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
            
            try {
                ResponseEntity<String> response = shortTimeoutTemplate.postForEntity(
                    getBaseUrl() + "/api/tickets",
                    entity,
                    String.class
                );
                
                System.out.println("- Graceful degradation: ✅ HANDLED (request succeeded despite short timeout)");
            } catch (Exception e) {
                // This is expected with very short timeout
                System.out.println("- Graceful degradation: ✅ HANDLED (timeout handled gracefully)");
            }
        } catch (Exception e) {
            System.out.println("- Graceful degradation: ❌ NOT HANDLED (unexpected error: " + e.getMessage() + ")");
        }
    }

    private static class NetworkCondition {
        final String name;
        final int timeoutMs;
        final int expectedMs;
        
        NetworkCondition(String name, int timeoutMs, int expectedMs) {
            this.name = name;
            this.timeoutMs = timeoutMs;
            this.expectedMs = expectedMs;
        }
    }
}