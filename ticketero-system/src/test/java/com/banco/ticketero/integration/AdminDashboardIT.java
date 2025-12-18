package com.banco.ticketero.integration;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Advisor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Feature: Dashboard Admin")
class AdminDashboardIT extends BaseIntegrationTest {

    @Nested
    @DisplayName("Endpoints Administrativos (P0)")
    class AdminEndpoints {

        @Test
        @DisplayName("Dashboard general: debe mostrar estado del sistema")
        void getDashboard_shouldReturnSystemStatus() {
            // Given - Crear algunos tickets y asesores
            createTestTicket("12345678", QueueType.CAJA, TicketStatus.EN_ESPERA);
            createTestTicket("87654321", QueueType.PERSONAL_BANKER, TicketStatus.COMPLETADO);
            createTestAdvisor("Juan Pérez", Advisor.AdvisorStatus.AVAILABLE, 1, "CAJA");
            createTestAdvisor("María García", Advisor.AdvisorStatus.BUSY, 2, "PERSONAL_BANKER");

            // When - Consultar dashboard
            given()
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(200)
                .body("totalTickets", greaterThanOrEqualTo(2))
                .body("waitingTickets", greaterThanOrEqualTo(1))
                .body("completedTickets", greaterThanOrEqualTo(1))
                .body("availableAdvisors", greaterThanOrEqualTo(1));
        }

        @Test
        @DisplayName("Cola específica: debe mostrar tickets de la cola")
        void getQueueDetails_shouldReturnQueueTickets() {
            // Given - Crear tickets para cola CAJA
            createTestTicket("11111111", QueueType.CAJA, TicketStatus.EN_ESPERA);
            createTestTicket("22222222", QueueType.CAJA, TicketStatus.EN_ESPERA);
            createTestTicket("33333333", QueueType.CAJA, TicketStatus.COMPLETADO);

            // When - Consultar cola específica
            given()
                .when()
                .get("/api/admin/queues/{type}", "CAJA")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("[0].queueType", equalTo("CAJA"))
                .body("[0].status", equalTo("EN_ESPERA"));
        }
    }

    @Nested
    @DisplayName("Gestión de Asesores (P1)")
    class AdvisorManagement {

        @Test
        @DisplayName("Cambiar estado asesor: debe actualizar status correctamente")
        void updateAdvisorStatus_shouldChangeStatusSuccessfully() {
            // Given - Asesor disponible
            Advisor advisor = createTestAdvisor("Carlos López", Advisor.AdvisorStatus.AVAILABLE, 3, "EMPRESAS");

            // When - Cambiar a BUSY
            String updateRequest = """
                {
                    "status": "BUSY"
                }
                """;

            given()
                .contentType("application/json")
                .body(updateRequest)
                .when()
                .put("/api/admin/advisors/{id}/status", advisor.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(advisor.getId().toString()))
                .body("status", equalTo("BUSY"));

            // Then - Verificar cambio en base de datos
            Advisor updatedAdvisor = advisorRepository.findById(advisor.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals(Advisor.AdvisorStatus.BUSY, updatedAdvisor.getStatus());
        }

        @Test
        @DisplayName("Estadísticas de asesores: debe mostrar lista de asesores")
        void getAdvisorStats_shouldReturnAdvisorsList() {
            // Given - Varios asesores con diferentes estados
            createTestAdvisor("Ana Ruiz", Advisor.AdvisorStatus.AVAILABLE, 4, "GERENCIA");
            createTestAdvisor("Pedro Silva", Advisor.AdvisorStatus.BUSY, 5, "GERENCIA");
            createTestAdvisor("Laura Vega", Advisor.AdvisorStatus.BREAK, 6, "CAJA");

            // When - Consultar estadísticas
            given()
                .when()
                .get("/api/admin/advisors/stats")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3))
                .body("[0].status", notNullValue());
        }
    }

    @Nested
    @DisplayName("Casos Límite Admin (P1)")
    class AdminEdgeCases {

        @Test
        @DisplayName("Cola inexistente: debe retornar 400")
        void getQueueDetails_nonExistentQueue_shouldReturn400() {
            given()
                .when()
                .get("/api/admin/queues/{type}", "INVALID_QUEUE")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Asesor inexistente: debe retornar 400 al cambiar status")
        void updateAdvisorStatus_nonExistentAdvisor_shouldReturn400() {
            String updateRequest = """
                {
                    "status": "AVAILABLE"
                }
                """;

            given()
                .contentType("application/json")
                .body(updateRequest)
                .when()
                .put("/api/admin/advisors/{id}/status", 99999)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Status inválido: debe retornar 400")
        void updateAdvisorStatus_invalidStatus_shouldReturn400() {
            Advisor advisor = createTestAdvisor("Test Advisor", Advisor.AdvisorStatus.AVAILABLE, 7, "CAJA");

            String invalidRequest = """
                {
                    "status": "INVALID_STATUS"
                }
                """;

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("/api/admin/advisors/{id}/status", advisor.getId())
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("Autorización Admin (P2)")
    class AdminAuthorization {

        @Test
        @DisplayName("Endpoints admin sin autenticación: deben ser accesibles en test")
        void adminEndpoints_inTestEnvironment_shouldBeAccessible() {
            // En ambiente de test, los endpoints admin son accesibles
            // En producción deberían requerir autenticación/autorización

            given()
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(200);

            given()
                .when()
                .get("/api/admin/advisors/stats")
                .then()
                .statusCode(200);
        }
    }
}