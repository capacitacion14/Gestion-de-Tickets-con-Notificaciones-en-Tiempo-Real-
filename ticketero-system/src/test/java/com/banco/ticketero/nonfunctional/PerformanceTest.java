package com.banco.ticketero.nonfunctional;

import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance Tests - RNF-P01 to RNF-P04
 * Validates system performance under load conditions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("nonfunctional")
class PerformanceTest {

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
     * RNF-P01: Throughput Test - ≥50 tickets/minute
     */
    @Test
    void testThroughput_ShouldHandle50TicketsPerMinute() throws Exception {
        // Arrange
        int targetTickets = 50;
        Duration testDuration = Duration.ofMinutes(1);
        
        // Act
        Instant start = Instant.now();
        List<CompletableFuture<ResponseEntity<TicketResponse>>> futures = new ArrayList<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < targetTickets; i++) {
            final int ticketNumber = i;
            CompletableFuture<ResponseEntity<TicketResponse>> future = 
                CompletableFuture.supplyAsync(() -> createTicket("12345678" + ticketNumber), executor);
            futures.add(future);
            
            // Distribute requests over the minute
            Thread.sleep(testDuration.toMillis() / targetTickets);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(90, TimeUnit.SECONDS);
        
        Instant end = Instant.now();
        Duration actualDuration = Duration.between(start, end);
        
        // Assert
        long successfulRequests = futures.stream()
            .mapToLong(future -> {
                try {
                    ResponseEntity<TicketResponse> response = future.get();
                    return response.getStatusCode().is2xxSuccessful() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        
        double throughputPerMinute = (successfulRequests * 60.0) / actualDuration.getSeconds();
        
        System.out.printf("Throughput Test Results:%n");
        System.out.printf("- Successful requests: %d/%d%n", successfulRequests, targetTickets);
        System.out.printf("- Duration: %.2f seconds%n", actualDuration.getSeconds());
        System.out.printf("- Throughput: %.2f tickets/minute%n", throughputPerMinute);
        
        assertTrue(throughputPerMinute >= 50, 
            String.format("Throughput %.2f tickets/min is below required 50 tickets/min", throughputPerMinute));
        
        executor.shutdown();
    }

    /**
     * RNF-P02: Latency Test - P95 < 2 seconds
     */
    @Test
    void testLatency_P95ShouldBeLessThan2Seconds() throws Exception {
        // Arrange
        int requestCount = 100;
        List<Long> latencies = new ArrayList<>();
        
        // Act
        for (int i = 0; i < requestCount; i++) {
            Instant start = Instant.now();
            ResponseEntity<TicketResponse> response = createTicket("87654321" + i);
            Instant end = Instant.now();
            
            if (response.getStatusCode().is2xxSuccessful()) {
                latencies.add(Duration.between(start, end).toMillis());
            }
            
            Thread.sleep(50); // Small delay between requests
        }
        
        // Calculate P95
        latencies.sort(Long::compareTo);
        int p95Index = (int) Math.ceil(0.95 * latencies.size()) - 1;
        long p95Latency = latencies.get(p95Index);
        
        // Calculate other percentiles for reporting
        long p50Latency = latencies.get((int) Math.ceil(0.50 * latencies.size()) - 1);
        long p99Latency = latencies.get((int) Math.ceil(0.99 * latencies.size()) - 1);
        
        // Assert
        System.out.printf("Latency Test Results:%n");
        System.out.printf("- P50: %d ms%n", p50Latency);
        System.out.printf("- P95: %d ms%n", p95Latency);
        System.out.printf("- P99: %d ms%n", p99Latency);
        System.out.printf("- Successful requests: %d/%d%n", latencies.size(), requestCount);
        
        assertTrue(p95Latency < 2000, 
            String.format("P95 latency %d ms exceeds 2000 ms threshold", p95Latency));
    }

    /**
     * RNF-P03: Concurrency Test - 100 simultaneous users
     */
    @Test
    void testConcurrency_ShouldHandle100SimultaneousUsers() throws Exception {
        // Arrange
        int concurrentUsers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        // Act
        Instant start = Instant.now();
        List<CompletableFuture<ResponseEntity<TicketResponse>>> futures = new ArrayList<>();
        
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            CompletableFuture<ResponseEntity<TicketResponse>> future = 
                CompletableFuture.supplyAsync(() -> createTicket("11111111" + userId), executor);
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        
        Instant end = Instant.now();
        Duration totalDuration = Duration.between(start, end);
        
        // Assert
        long successfulRequests = futures.stream()
            .mapToLong(future -> {
                try {
                    ResponseEntity<TicketResponse> response = future.get();
                    return response.getStatusCode().is2xxSuccessful() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        
        double successRate = (successfulRequests * 100.0) / concurrentUsers;
        
        System.out.printf("Concurrency Test Results:%n");
        System.out.printf("- Concurrent users: %d%n", concurrentUsers);
        System.out.printf("- Successful requests: %d/%d%n", successfulRequests, concurrentUsers);
        System.out.printf("- Success rate: %.2f%%%n", successRate);
        System.out.printf("- Total duration: %.2f seconds%n", totalDuration.getSeconds());
        
        assertTrue(successRate >= 95, 
            String.format("Success rate %.2f%% is below 95%% threshold", successRate));
        
        executor.shutdown();
    }

    /**
     * RNF-P04: Memory Stability Test - No memory leaks
     */
    @Test
    void testMemoryStability_ShouldNotHaveMemoryLeaks() throws Exception {
        // Arrange
        Runtime runtime = Runtime.getRuntime();
        int iterations = 1000;
        
        // Force garbage collection and get baseline
        System.gc();
        Thread.sleep(1000);
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Act - Create many tickets to stress memory
        for (int i = 0; i < iterations; i++) {
            createTicket("99999999" + i);
            
            if (i % 100 == 0) {
                System.gc(); // Periodic garbage collection
                Thread.sleep(10);
            }
        }
        
        // Force final garbage collection
        System.gc();
        Thread.sleep(2000);
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Assert
        long memoryIncrease = finalMemory - initialMemory;
        double memoryIncreasePercent = (memoryIncrease * 100.0) / initialMemory;
        
        System.out.printf("Memory Stability Test Results:%n");
        System.out.printf("- Initial memory: %.2f MB%n", initialMemory / 1024.0 / 1024.0);
        System.out.printf("- Final memory: %.2f MB%n", finalMemory / 1024.0 / 1024.0);
        System.out.printf("- Memory increase: %.2f MB (%.2f%%)%n", 
            memoryIncrease / 1024.0 / 1024.0, memoryIncreasePercent);
        System.out.printf("- Iterations completed: %d%n", iterations);
        
        // Memory increase should be reasonable (less than 50% increase)
        assertTrue(memoryIncreasePercent < 50, 
            String.format("Memory increase %.2f%% suggests potential memory leak", memoryIncreasePercent));
    }

    private ResponseEntity<TicketResponse> createTicket(String nationalId) {
        try {
            CreateTicketRequest request = new CreateTicketRequest(
                nationalId, 
                "+56912345678", 
                "Sucursal Centro", 
                com.banco.ticketero.model.QueueType.CAJA
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<CreateTicketRequest> entity = new HttpEntity<>(request, headers);
            
            return restTemplate.postForEntity(
                getBaseUrl() + "/api/tickets", 
                entity, 
                TicketResponse.class
            );
        } catch (Exception e) {
            System.err.println("Error creating ticket: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}