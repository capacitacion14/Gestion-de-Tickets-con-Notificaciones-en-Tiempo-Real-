package com.banco.ticketero.integration;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Feature: Creación de Tickets")
class TicketCreationIT extends BaseIntegrationTest {

    @Nested
    @DisplayName("Escenarios Happy Path (P0)")
    class HappyPath {

        @Test
        @DisplayName("Crear ticket con datos válidos - debe generar ticket EN_ESPERA y mensaje outbox PENDING")
        void crearTicket_datosValidos_debeGenerarTicketYMensaje() {
            // Given - el sistema está operativo y hay asesores disponibles
            // (setupTestAdvisors ya crea asesores disponibles)
            
            // When - envío POST /api/tickets con datos válidos
            String response = given()
                .contentType("application/json")
                .body(createTicketRequest("12345678", QueueType.CAJA))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .body("nationalId", equalTo("12345678"))
                .body("queueType", equalTo("CAJA"))
                .body("status", equalTo("EN_ESPERA"))
                .body("codigoReferencia", notNullValue())
                .body("numero", startsWith("C"))
                .body("positionInQueue", greaterThan(0))
                .body("estimatedWaitMinutes", greaterThan(0))
                .extract().asString();

            // Then - verificar estado en base de datos
            assertEquals(1, countTicketsInStatus(TicketStatus.EN_ESPERA));
            assertEquals(1, countOutboxMessages(OutboxMessage.MessageStatus.PENDING));
            
            // Verificar estructura del response
            assertTicketCreated(response, "12345678", QueueType.CAJA);
            
            // Verificar que se programó notificación Telegram (mensaje outbox)
            // WireMock verification se hace de forma asíncrona
            waitForOutboxProcessing(1, 10);
        }

        @Test
        @DisplayName("Calcular posición correcta en cola - debe asignar posición secuencial")
        void crearTicket_colaConTicketsExistentes_debeCalcularPosicionCorrecta() {
            // Given - existen 3 tickets EN_ESPERA para cola CAJA
            createTestTicket("11111111", QueueType.CAJA, TicketStatus.EN_ESPERA);
            createTestTicket("22222222", QueueType.CAJA, TicketStatus.EN_ESPERA);
            createTestTicket("33333333", QueueType.CAJA, TicketStatus.EN_ESPERA);
            
            // When - creo nuevo ticket para cola CAJA
            given()
                .contentType("application/json")
                .body(createTicketRequest("44444444", QueueType.CAJA))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .body("positionInQueue", equalTo(4))
                .body("estimatedWaitMinutes", equalTo(20)); // 4 * 5 min promedio
        }
    }

    @Nested
    @DisplayName("Escenarios Edge Cases (P1)")
    class EdgeCases {

        @Test
        @DisplayName("Crear ticket sin teléfono - debe usar teléfono por defecto")
        void crearTicket_sinTelefono_debeUsarTelefonoDefecto() {
            // Given - request sin campo teléfono
            String requestSinTelefono = """
                {
                    "nationalId": "87654321",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "PERSONAL_BANKER"
                }
                """;
            
            // When - envío POST sin teléfono
            given()
                .contentType("application/json")
                .body(requestSinTelefono)
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .body("telefono", anyOf(nullValue(), equalTo("")));
        }

        @Test
        @DisplayName("Tickets para diferentes colas - deben tener numeración independiente")
        void crearTickets_diferentesColas_debenTenerNumeracionIndependiente() {
            // When - crear tickets en diferentes colas
            String ticketCaja = given()
                .contentType("application/json")
                .body(createTicketRequest("11111111", QueueType.CAJA))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .body("numero", startsWith("C"))
                .extract().path("numero");

            String ticketPersonal = given()
                .contentType("application/json")
                .body(createTicketRequest("22222222", QueueType.PERSONAL_BANKER))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .body("numero", startsWith("P"))
                .extract().path("numero");

            String ticketEmpresas = given()
                .contentType("application/json")
                .body(createTicketRequest("33333333", QueueType.EMPRESAS))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .body("numero", startsWith("E"))
                .extract().path("numero");

            // Then - verificar prefijos únicos por cola
            assertNotEquals(ticketCaja, ticketPersonal);
            assertNotEquals(ticketPersonal, ticketEmpresas);
        }

        @Test
        @DisplayName("Número único con prefijo - debe generar secuencia correcta")
        void crearMultiplesTickets_mismaCola_debeGenerarSecuenciaCorrecta() {
            // When - crear múltiples tickets en misma cola
            String numero1 = given()
                .contentType("application/json")
                .body(createTicketRequest("11111111", QueueType.GERENCIA))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .extract().path("numero");

            String numero2 = given()
                .contentType("application/json")
                .body(createTicketRequest("22222222", QueueType.GERENCIA))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .extract().path("numero");

            // Then - verificar secuencia incremental
            assertTrue(numero1.startsWith("G"));
            assertTrue(numero2.startsWith("G"));
            assertNotEquals(numero1, numero2);
            
            // Extraer números y verificar secuencia
            int num1 = Integer.parseInt(numero1.substring(1));
            int num2 = Integer.parseInt(numero2.substring(1));
            assertEquals(num1 + 1, num2);
        }

        @Test
        @DisplayName("Consultar por código referencia - debe retornar ticket existente")
        void consultarTicket_codigoReferencia_debeRetornarTicketExistente() {
            // Given - crear ticket y obtener código de referencia
            String codigoReferencia = given()
                .contentType("application/json")
                .body(createTicketRequest("99999999", QueueType.CAJA))
            .when()
                .post("/api/tickets")
            .then()
                .statusCode(201)
                .extract().path("codigoReferencia");

            // When - consultar por código de referencia
            given()
                .pathParam("codigo", codigoReferencia)
            .when()
                .get("/api/tickets/{codigo}")
            .then()
                .statusCode(200)
                .body("codigoReferencia", equalTo(codigoReferencia))
                .body("nationalId", equalTo("99999999"))
                .body("queueType", equalTo("CAJA"))
                .body("status", equalTo("EN_ESPERA"));
        }
    }
}