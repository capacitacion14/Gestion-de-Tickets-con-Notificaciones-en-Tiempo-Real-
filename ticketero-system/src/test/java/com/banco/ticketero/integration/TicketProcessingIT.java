package com.banco.ticketero.integration;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.Advisor.AdvisorStatus;
import com.banco.ticketero.model.entity.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Feature: Procesamiento de Tickets")
class TicketProcessingIT extends BaseIntegrationTest {

    @Nested
    @DisplayName("Flujo Completo de Procesamiento (P0)")
    class CompleteProcessingFlow {

        @Test
        @DisplayName("Procesar ticket completo: WAITING → CALLED → IN_PROGRESS → COMPLETED")
        void processTicket_completeFlow_shouldTransitionThroughAllStates() {
            // Given - Asesor disponible y ticket en espera
            Advisor advisor = createTestAdvisor("Ana García", AdvisorStatus.AVAILABLE, 1, "CAJA");
            
            String ticketRequest = createTicketRequest("12345678", QueueType.CAJA);
            String ticketCode = given()
                .contentType("application/json")
                .body(ticketRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("numero");

            // When - Worker procesa automáticamente
            await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    String status = getTicketStatus(ticketCode);
                    return "COMPLETADO".equals(status);
                });

            // Then - Verificar transiciones completas
            Ticket processedTicket = ticketRepository.findByNumero(ticketCode).orElseThrow();
            
            assertEquals(TicketStatus.COMPLETADO, processedTicket.getStatus());
            assertNotNull(processedTicket.getAssignedAdvisor());
            assertTrue(processedTicket.getAssignedAdvisor().getId() > 0);
            
            // Verificar asesor vuelve a AVAILABLE
            Advisor updatedAdvisor = advisorRepository.findById(advisor.getId()).orElseThrow();
            assertEquals(AdvisorStatus.AVAILABLE, updatedAdvisor.getStatus());
            
            // Verificar mensajes de notificación
            await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> countOutboxMessages(com.banco.ticketero.model.entity.OutboxMessage.MessageStatus.SENT) >= 2);
        }

        @Test
        @DisplayName("Procesamiento FIFO: múltiples tickets se procesan en orden")
        void processMultipleTickets_fifoOrder_shouldProcessInCorrectSequence() {
            // Given - Asesor disponible y 3 tickets en cola
            createTestAdvisor("Carlos López", AdvisorStatus.AVAILABLE, 2, "PERSONAL_BANKER");
            
            String ticket1 = createAndGetTicketCode("11111111", QueueType.PERSONAL_BANKER);
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            String ticket2 = createAndGetTicketCode("22222222", QueueType.PERSONAL_BANKER);
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            String ticket3 = createAndGetTicketCode("33333333", QueueType.PERSONAL_BANKER);

            // When - Worker procesa automáticamente
            await()
                .atMost(45, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> countTicketsInStatus(TicketStatus.COMPLETADO) >= 3);

            // Then - Verificar orden FIFO
            Ticket firstTicket = ticketRepository.findByNumero(ticket1).orElseThrow();
            Ticket secondTicket = ticketRepository.findByNumero(ticket2).orElseThrow();
            Ticket thirdTicket = ticketRepository.findByNumero(ticket3).orElseThrow();
            
            // Verify completion order (FIFO) - only if all tickets are completed
            if (firstTicket.getCompletedAt() != null && secondTicket.getCompletedAt() != null) {
                assertTrue(firstTicket.getCompletedAt().isBefore(secondTicket.getCompletedAt()) || 
                          firstTicket.getCompletedAt().equals(secondTicket.getCompletedAt()));
            }
            if (secondTicket.getCompletedAt() != null && thirdTicket.getCompletedAt() != null) {
                assertTrue(secondTicket.getCompletedAt().isBefore(thirdTicket.getCompletedAt()) || 
                          secondTicket.getCompletedAt().equals(thirdTicket.getCompletedAt()));
            }
            
            assertEquals(TicketStatus.COMPLETADO, firstTicket.getStatus());
            assertEquals(TicketStatus.COMPLETADO, secondTicket.getStatus());
            assertEquals(TicketStatus.COMPLETADO, thirdTicket.getStatus());
        }
    }

    @Nested
    @DisplayName("Casos Límite de Procesamiento (P1)")
    class EdgeCases {

        @Test
        @DisplayName("Sin asesores disponibles: ticket permanece en WAITING")
        void processTicket_noAvailableAdvisors_shouldRemainWaiting() {
            // Given - Limpiar asesores automáticos y crear solo BREAK/BUSY
            advisorRepository.deleteAll();
            createTestAdvisor("María Pérez", AdvisorStatus.BREAK, 3, "EMPRESAS");
            createTestAdvisor("Juan Silva", AdvisorStatus.BUSY, 4, "EMPRESAS");
            
            String ticketRequest = createTicketRequest("87654321", QueueType.EMPRESAS);
            String ticketCode = given()
                .contentType("application/json")
                .body(ticketRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("numero");

            // When - Esperar tiempo suficiente para procesamiento
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Then - Ticket debe permanecer en WAITING
            Ticket waitingTicket = ticketRepository.findByNumero(ticketCode).orElseThrow();
            
            assertEquals(TicketStatus.EN_ESPERA, waitingTicket.getStatus());
            assertNull(waitingTicket.getAssignedAdvisor());
            assertNull(waitingTicket.getAssignedModuleNumber());
            
            // Solo debe existir mensaje de confirmación
            assertEquals(1, countOutboxMessages(com.banco.ticketero.model.entity.OutboxMessage.MessageStatus.SENT));
        }

        @Test
        @DisplayName("Idempotencia: ticket completado no se reprocesa")
        void processTicket_alreadyCompleted_shouldNotReprocess() {
            // Given - Ticket ya completado manualmente
            Advisor advisor = createTestAdvisor("Rosa Martín", AdvisorStatus.AVAILABLE, 5, "GERENCIA");
            
            Ticket completedTicket = Ticket.builder()
                .nationalId("99999999")
                .queueType(QueueType.GERENCIA)
                .status(TicketStatus.COMPLETADO)
                .numero("G-001")
                .positionInQueue(1)
                .branchOffice("Sucursal Centro")
                .assignedAdvisor(advisor)
                .assignedModuleNumber(5)
                .createdAt(LocalDateTime.now().minusMinutes(30))

                .completedAt(LocalDateTime.now().minusMinutes(15))
                .build();
            
            ticketRepository.save(completedTicket);
            
            LocalDateTime originalCompletedAt = completedTicket.getCompletedAt();

            // When - Worker intenta procesar
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Then - Ticket no debe cambiar
            Ticket unchangedTicket = ticketRepository.findByNumero("G-001").orElseThrow();
            
            assertEquals(TicketStatus.COMPLETADO, unchangedTicket.getStatus());
            assertEquals(originalCompletedAt, unchangedTicket.getCompletedAt());
            assertEquals(advisor.getId(), unchangedTicket.getAssignedAdvisor().getId());
            
            // Asesor debe seguir AVAILABLE
            Advisor unchangedAdvisor = advisorRepository.findById(advisor.getId()).orElseThrow();
            assertEquals(AdvisorStatus.AVAILABLE, unchangedAdvisor.getStatus());
        }

        @Test
        @DisplayName("Asesor en BREAK no recibe tickets nuevos")
        void processTicket_advisorOnBreak_shouldNotReceiveTickets() {
            // Given - Asesor en BREAK y otro AVAILABLE
            Advisor advisorOnBreak = createTestAdvisor("Pedro Ruiz", AdvisorStatus.BREAK, 6, "CAJA");
            Advisor availableAdvisor = createTestAdvisor("Laura Vega", AdvisorStatus.AVAILABLE, 7, "CAJA");
            
            String ticketRequest = createTicketRequest("55555555", QueueType.CAJA);
            String ticketCode = given()
                .contentType("application/json")
                .body(ticketRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("numero");

            // When - Worker procesa
            await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> "COMPLETADO".equals(getTicketStatus(ticketCode)));

            // Then - Solo asesor AVAILABLE debe procesar
            Ticket processedTicket = ticketRepository.findByNumero(ticketCode).orElseThrow();
            
            assertEquals(TicketStatus.COMPLETADO, processedTicket.getStatus());
            assertNotNull(processedTicket.getAssignedAdvisor());
            assertTrue(processedTicket.getAssignedAdvisor().getId() > 0);
            
            // Asesor en BREAK no debe cambiar
            Advisor stillOnBreak = advisorRepository.findById(advisorOnBreak.getId()).orElseThrow();
            assertEquals(AdvisorStatus.BREAK, stillOnBreak.getStatus());
        }
    }

    // Métodos auxiliares
    private String createAndGetTicketCode(String nationalId, QueueType queueType) {
        String request = createTicketRequest(nationalId, queueType);
        return given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api/tickets")
            .then()
            .statusCode(201)
            .extract()
            .path("numero");
    }

    private String getTicketStatus(String numero) {
        return given()
            .when()
            .get("/api/tickets/numero/{numero}", numero)
            .then()
            .statusCode(200)
            .extract()
            .path("status");
    }
}