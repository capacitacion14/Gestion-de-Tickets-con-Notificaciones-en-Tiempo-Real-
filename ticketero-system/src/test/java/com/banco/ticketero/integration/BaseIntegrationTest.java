package com.banco.ticketero.integration;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.Advisor.AdvisorStatus;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.OutboxMessage.MessageStatus;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    protected static WireMockServer wireMockServer;

    @LocalServerPort
    protected int port;

    @Autowired
    protected TicketRepository ticketRepository;

    @Autowired
    protected AdvisorRepository advisorRepository;

    @Autowired
    protected OutboxMessageRepository outboxMessageRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL (docker-compose-test.yml)
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5433/ticketero_test");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");

        // RabbitMQ (docker-compose-test.yml)
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5673");
        registry.add("spring.rabbitmq.username", () -> "test");
        registry.add("spring.rabbitmq.password", () -> "test");

        // Telegram API (WireMock) - use dynamic port
        registry.add("telegram.bot.api-url", () -> {
            if (wireMockServer != null && wireMockServer.isRunning()) {
                return "http://localhost:" + wireMockServer.port();
            }
            return "http://localhost:8089"; // fallback
        });
    }

    @BeforeAll
    static void setupWireMock() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            wireMockServer = new WireMockServer(0); // Use dynamic port
            wireMockServer.start();
        }
        
        // Mock Telegram sendMessage endpoint
        wireMockServer.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"result\":{\"message_id\":1}}")));
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Reset WireMock
        wireMockServer.resetAll();
        setupTelegramMocks();
        
        // Clean database BEFORE setup to avoid conflicts
        cleanDatabase();
        
        // Setup test data
        setupTestAdvisors();
    }

    @AfterEach
    void tearDown() {
        // Clean database after each test
        cleanDatabase();
        
        // Reset WireMock
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.resetAll();
        }
    }
    
    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }
    
    private void cleanDatabase() {
        try {
            // Clean in reverse order of dependencies
            outboxMessageRepository.deleteAll();
            ticketRepository.deleteAll();
            advisorRepository.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup errors to avoid masking test failures
            System.err.println("Warning: Database cleanup failed: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    protected String createTicketRequest(String nationalId, QueueType queueType) {
        return createTicketRequest(nationalId, queueType, "+56912345678", "Sucursal Centro");
    }

    protected String createTicketRequest(String nationalId, QueueType queueType, String telefono, String branchOffice) {
        return """
            {
                "nationalId": "%s",
                "telefono": "%s",
                "branchOffice": "%s",
                "queueType": "%s"
            }
            """.formatted(nationalId, telefono, branchOffice, queueType.name());
    }

    protected int countTicketsInStatus(TicketStatus status) {
        return (int) ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getStatus() == status)
                .count();
    }

    protected int countOutboxMessages(MessageStatus status) {
        return (int) outboxMessageRepository.findAll().stream()
                .filter(msg -> msg.getEstadoEnvio() == status)
                .count();
    }

    protected void waitForTicketProcessing(int expectedCompleted, int timeoutSeconds) {
        await()
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> countTicketsInStatus(TicketStatus.COMPLETADO) >= expectedCompleted);
    }

    protected void waitForOutboxProcessing(int expectedProcessed, int timeoutSeconds) {
        await()
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> countOutboxMessages(MessageStatus.SENT) >= expectedProcessed);
    }

    protected Ticket createTestTicket(String nationalId, QueueType queueType, TicketStatus status) {
        // Calcular posición en cola
        int position = (int) ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA) + 1;
        
        Ticket ticket = Ticket.builder()
                .numero(generateTicketNumber(queueType))
                .nationalId(nationalId)
                .telefono("+56912345678")
                .branchOffice("Sucursal Centro")
                .queueType(queueType)
                .status(status)
                .positionInQueue(position)
                .estimatedWaitMinutes(queueType.calculateEstimatedTime(position))
                .vigenciaMinutos(queueType.getVigenciaMinutos())
                .expiresAt(LocalDateTime.now().plusMinutes(queueType.getVigenciaMinutos()))
                .createdAt(LocalDateTime.now())
                .build();
        
        return ticketRepository.save(ticket);
    }

    protected Advisor createTestAdvisor(String name, AdvisorStatus status, Integer moduleNumber, String... supportedQueues) {
        // Generate unique email to avoid constraint violations
        String uniqueEmail = name.toLowerCase().replace(" ", ".") + "." + System.nanoTime() + "@test.com";
        
        Advisor advisor = Advisor.builder()
                .name(name)
                .email(uniqueEmail)
                .status(status)
                .moduleNumber(moduleNumber)
                .supportedQueues(String.join(",", supportedQueues))
                .build();
        
        return advisorRepository.save(advisor);
    }

    private void setupTestAdvisors() {
        // Create available advisors for each queue type with unique emails
        long timestamp = System.currentTimeMillis();
        createTestAdvisor("Test Asesor Caja " + timestamp, AdvisorStatus.AVAILABLE, 1, "CAJA");
        createTestAdvisor("Test Asesor Personal " + timestamp, AdvisorStatus.AVAILABLE, 2, "PERSONAL_BANKER");
        createTestAdvisor("Test Asesor Empresas " + timestamp, AdvisorStatus.AVAILABLE, 3, "EMPRESAS");
        createTestAdvisor("Test Asesor Gerencia " + timestamp, AdvisorStatus.AVAILABLE, 4, "GERENCIA");
    }

    private void setupTelegramMocks() {
        // Mock successful sendMessage
        wireMockServer.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"result\":{\"message_id\":1}}")));
    }

    private String generateTicketNumber(QueueType queueType) {
        String prefix = switch (queueType) {
            case CAJA -> "C";
            case PERSONAL_BANKER -> "P";
            case EMPRESAS -> "E";
            case GERENCIA -> "G";
        };
        
        long count = ticketRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    // ==================== ASSERTION HELPERS ====================

    protected void assertTicketCreated(String responseBody, String expectedNationalId, QueueType expectedQueueType) {
        try {
            var response = objectMapper.readTree(responseBody);
            
            assert response.get("nationalId").asText().equals(expectedNationalId);
            assert response.get("queueType").asText().equals(expectedQueueType.name());
            assert response.get("status").asText().equals("EN_ESPERA");
            assert response.has("codigoReferencia");
            assert response.has("numero");
            assert response.get("positionInQueue").asInt() > 0;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + responseBody, e);
        }
    }

    protected void assertTelegramMessageSent(String expectedContent) {
        wireMockServer.verify(
                postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
                        .withRequestBody(containing(expectedContent))
        );
    }
}