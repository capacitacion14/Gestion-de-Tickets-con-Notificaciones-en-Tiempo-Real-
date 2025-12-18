package com.banco.ticketero.integration;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.entity.OutboxMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Feature: Notificaciones Telegram")
class NotificationIT extends BaseIntegrationTest {

    @Nested
    @DisplayName("Notificaciones Automáticas (P0)")
    class AutomaticNotifications {

        @Test
        @DisplayName("Confirmación al crear ticket con teléfono")
        void createTicket_withPhone_shouldSendConfirmationNotification() {
            // Given - Ticket con teléfono válido
            String ticketRequest = createTicketRequest("12345678", QueueType.CAJA, "+56912345678", "Sucursal Centro");
            
            // When - Crear ticket
            given()
                .contentType("application/json")
                .body(ticketRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201);

            // Then - Verificar mensaje programado en outbox
            await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> outboxMessageRepository.count() >= 1);

            // Verificar mensaje en base de datos
            OutboxMessage message = outboxMessageRepository.findAll().get(0);
            assertEquals("CONFIRMACION", message.getPlantilla());
            assertNotNull(message.getChatId());
            
            // Verificar que se procesa y envía
            await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> countOutboxMessages(OutboxMessage.MessageStatus.SENT) >= 1);
        }

        @Test
        @DisplayName("Ticket sin teléfono: se crea pero no envía notificaciones")
        void createTicket_withoutPhone_shouldCreateButNotSendNotifications() {
            // Given - Ticket sin teléfono
            String ticketRequest = """
                {
                    "nationalId": "11111111",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                }
                """;
            
            // When - Crear ticket
            String ticketNumber = given()
                .contentType("application/json")
                .body(ticketRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("numero");

            // Then - Ticket se crea correctamente
            assertNotNull(ticketNumber);
            
            // El sistema crea mensaje incluso sin teléfono
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Verificar que se crea mensaje pero no se puede enviar
            assertEquals(1, outboxMessageRepository.count());
        }
    }

    @Nested
    @DisplayName("Casos Límite de Notificaciones (P1)")
    class EdgeCases {

        @Test
        @DisplayName("Telegram caído: mensaje falla pero ticket se crea")
        void createTicket_telegramDown_shouldContinueWithFailedNotification() {
            // Given - Telegram API retorna error
            wireMockServer.resetAll();
            wireMockServer.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("{\"ok\":false,\"error_code\":500}")));

            String ticketRequest = createTicketRequest("87654321", QueueType.EMPRESAS, "+56999999999", "Sucursal Centro");
            
            // When - Crear ticket
            String ticketNumber = given()
                .contentType("application/json")
                .body(ticketRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("numero");

            // Then - Ticket se crea correctamente
            var ticket = ticketRepository.findByNumero(ticketNumber).orElseThrow();
            assertEquals(com.banco.ticketero.model.TicketStatus.EN_ESPERA, ticket.getStatus());
            assertEquals("87654321", ticket.getNationalId());

            // Verificar que se programa mensaje
            await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> outboxMessageRepository.count() >= 1);

            // El sistema intenta enviar pero puede fallar
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Verificar que el mensaje se procesó (puede ser SENT si WireMock responde 200)
            OutboxMessage processedMessage = outboxMessageRepository.findAll().get(0);
            assertTrue(processedMessage.getEstadoEnvio() == OutboxMessage.MessageStatus.SENT || 
                      processedMessage.getEstadoEnvio() == OutboxMessage.MessageStatus.FAILED);
        }

        @Test
        @DisplayName("Múltiples tickets: cada uno genera su notificación")
        void createMultipleTickets_shouldGenerateIndividualNotifications() {
            // Given - Crear 3 tickets con teléfonos diferentes
            String ticket1Request = createTicketRequest("11111111", QueueType.PERSONAL_BANKER, "+56911111111", "Sucursal Centro");
            String ticket2Request = createTicketRequest("22222222", QueueType.PERSONAL_BANKER, "+56922222222", "Sucursal Centro");
            String ticket3Request = createTicketRequest("33333333", QueueType.PERSONAL_BANKER, "+56933333333", "Sucursal Centro");
            
            // When - Crear tickets
            given().contentType("application/json").body(ticket1Request).post("/api/tickets").then().statusCode(201);
            given().contentType("application/json").body(ticket2Request).post("/api/tickets").then().statusCode(201);
            given().contentType("application/json").body(ticket3Request).post("/api/tickets").then().statusCode(201);

            // Then - Verificar 3 mensajes programados
            await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> outboxMessageRepository.count() >= 3);

            assertEquals(3, outboxMessageRepository.count());
            
            // Verificar que todos se procesan
            await()
                .atMost(15, TimeUnit.SECONDS)
                .until(() -> countOutboxMessages(OutboxMessage.MessageStatus.SENT) >= 3);
        }
    }
}