package com.banco.ticketero.nonfunctional;

import com.banco.ticketero.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for non-functional tests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseNonFunctionalTest extends BaseIntegrationTest {
    
    protected static final String API_BASE_PATH = "/api/tickets";
    protected static final String ADMIN_BASE_PATH = "/api/admin";
    protected static final String HEALTH_PATH = "/actuator/health";
    
    // Performance thresholds
    protected static final int MAX_RESPONSE_TIME_MS = 2000;
    protected static final int MAX_THROUGHPUT_PER_MINUTE = 50;
    protected static final int MAX_CONCURRENT_USERS = 100;
    
    // Security test payloads
    protected static final String[] SQL_INJECTION_PAYLOADS = {
        "'; DROP TABLE tickets; --",
        "' OR '1'='1",
        "'; SELECT * FROM tickets; --",
        "' UNION SELECT * FROM tickets --"
    };
    
    protected static final String[] XSS_PAYLOADS = {
        "<script>alert('XSS')</script>",
        "javascript:alert('XSS')",
        "<img src=x onerror=alert('XSS')>",
        "';alert('XSS');//"
    };
    
    @BeforeEach
    void setUpNonFunctionalTest() {
        resetMetrics();
    }
    
    protected void resetMetrics() {
        System.gc();
    }
    
    protected long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    protected double getCpuUsage() {
        return ((com.sun.management.OperatingSystemMXBean) 
            java.lang.management.ManagementFactory.getOperatingSystemMXBean())
            .getProcessCpuLoad() * 100;
    }
}