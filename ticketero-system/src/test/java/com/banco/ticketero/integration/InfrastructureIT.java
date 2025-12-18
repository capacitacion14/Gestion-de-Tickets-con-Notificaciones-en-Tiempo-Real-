package com.banco.ticketero.integration;

import com.banco.ticketero.model.entity.Advisor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Infrastructure Integration Tests")
class InfrastructureIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Should start all containers and application context successfully")
    void shouldStartInfrastructure() {
        // Verify WireMock server is running
        assertTrue(wireMockServer.isRunning(), "WireMock server should be running");
        
        // Verify application health endpoint
        given()
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("Should connect to PostgreSQL and execute queries")
    void shouldConnectToDatabase() {
        // Verify database connection by counting tickets
        int initialCount = ticketRepository.findAll().size();
        assertEquals(0, initialCount, "Database should be empty initially");
        
        // Verify advisors were created in setup
        int advisorCount = advisorRepository.findAll().size();
        assertEquals(4, advisorCount, "Should have 4 test advisors");
    }

    @Test
    @DisplayName("Should mock Telegram API successfully")
    void shouldMockTelegramAPI() {
        // Verify WireMock is responding to Telegram API calls
        String wireMockUrl = "http://localhost:" + wireMockServer.port();
        
        given()
            .contentType("application/json")
            .body("{\"chat_id\":\"123\",\"text\":\"Test message\"}")
        .when()
            .post(wireMockUrl + "/bot123456789:TEST/sendMessage")
        .then()
            .statusCode(200)
            .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Should clean database between tests")
    void shouldCleanDatabaseBetweenTests() {
        // Verify that we start with clean data (only the 4 setup advisors)
        long initialCount = advisorRepository.count();
        assertEquals(4, initialCount, "Should have exactly 4 setup advisors");
        
        // Create additional test data
        Advisor testAdvisor = Advisor.builder()
                .name("Temporary Test Advisor")
                .email("temp.test." + System.nanoTime() + "@test.com")
                .status(Advisor.AdvisorStatus.AVAILABLE)
                .moduleNumber(99)
                .supportedQueues("CAJA")
                .assignedTicketsCount(0)
                .build();
        
        advisorRepository.save(testAdvisor);
        
        // Verify the additional advisor was created
        long afterCreateCount = advisorRepository.count();
        assertEquals(5, afterCreateCount, "Should have 5 advisors after creation");
        
        // After this test completes, tearDown() should clean the database
        // The next test should not see this data
    }

    @Test
    @DisplayName("Should have clean database after previous test")
    void shouldHaveCleanDatabaseAfterPreviousTest() {
        // Verify the test advisor from previous test was cleaned up
        assertFalse(advisorRepository.existsById(999L), "Test advisor should be cleaned up");
        
        // Should only have the 4 default advisors
        assertEquals(4, advisorRepository.count(), "Should only have default advisors");
    }
}