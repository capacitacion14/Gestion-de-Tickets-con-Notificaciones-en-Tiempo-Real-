package com.banco.ticketero.integration;

import com.banco.ticketero.model.QueueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Feature: Validaciones de Input")
class ValidationIT extends BaseIntegrationTest {

    @Nested
    @DisplayName("Validaciones de Campos Requeridos (P0)")
    class RequiredFieldValidations {

        @Test
        @DisplayName("nationalId requerido: debe fallar sin nationalId")
        void createTicket_withoutNationalId_shouldFailValidation() {
            String invalidRequest = """
                {
                    "telefono": "+56912345678",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                }
                """;

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(400)
                .body("message", containsString("Validation failed"))
                .body("errors", hasItem(containsString("nationalId")));
        }

        @Test
        @DisplayName("branchOffice requerido: debe fallar sin sucursal")
        void createTicket_withoutBranchOffice_shouldFailValidation() {
            String invalidRequest = """
                {
                    "nationalId": "12345678",
                    "telefono": "+56912345678",
                    "queueType": "CAJA"
                }
                """;

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(400)
                .body("message", containsString("Validation failed"))
                .body("errors", hasItem(containsString("branchOffice")));
        }

        @Test
        @DisplayName("queueType requerido: debe fallar sin tipo de cola")
        void createTicket_withoutQueueType_shouldFailValidation() {
            String invalidRequest = """
                {
                    "nationalId": "12345678",
                    "telefono": "+56912345678",
                    "branchOffice": "Sucursal Centro"
                }
                """;

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(400)
                .body("message", containsString("Validation failed"))
                .body("errors", hasItem(containsString("queueType")));
        }
    }

    @Nested
    @DisplayName("Validaciones de Formato (P1)")
    class FormatValidations {

        @ParameterizedTest
        @ValueSource(strings = {"1234567", "123456789012345", "abc12345", "12345abc", ""})
        @DisplayName("nationalId formato: debe validar 8-12 dígitos")
        void createTicket_invalidNationalIdFormat_shouldFailValidation(String invalidNationalId) {
            String invalidRequest = String.format("""
                {
                    "nationalId": "%s",
                    "telefono": "+56912345678",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                }
                """, invalidNationalId);

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(400)
                .body("message", containsString("Validation failed"))
                .body("errors", hasItem(containsString("nationalId")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"+5691234567", "+569123456789", "912345678", "56912345678", "+1234567890"})
        @DisplayName("telefono formato: debe validar formato chileno +56XXXXXXXXX")
        void createTicket_invalidPhoneFormat_shouldFailValidation(String invalidPhone) {
            String invalidRequest = String.format("""
                {
                    "nationalId": "12345678",
                    "telefono": "%s",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                }
                """, invalidPhone);

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(400)
                .body("message", containsString("Validation failed"))
                .body("errors", hasItem(containsString("telefono")));
        }

        @Test
        @DisplayName("queueType inválido: debe rechazar valores no permitidos")
        void createTicket_invalidQueueType_shouldFailValidation() {
            String invalidRequest = """
                {
                    "nationalId": "12345678",
                    "telefono": "+56912345678",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "INVALID_QUEUE"
                }
                """;

            given()
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(404); // Sistema retorna 404 para enum inválido
        }
    }

    @Nested
    @DisplayName("Validaciones de Casos Límite (P1)")
    class EdgeCaseValidations {

        @Test
        @DisplayName("Consultar ticket inexistente: debe retornar 404")
        void getTicket_nonExistentNumber_shouldReturn404() {
            given()
                .when()
                .get("/api/tickets/numero/{numero}", "X999")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Consultar con UUID inválido: debe retornar 404")
        void getTicket_invalidUUID_shouldReturn404() {
            given()
                .when()
                .get("/api/tickets/{uuid}", "invalid-uuid-format")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("JSON malformado: debe retornar 404")
        void createTicket_malformedJson_shouldReturn404() {
            String malformedJson = """
                {
                    "nationalId": "12345678",
                    "telefono": "+56912345678"
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                """; // Missing closing brace and comma

            given()
                .contentType("application/json")
                .body(malformedJson)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(404); // Sistema retorna 404 para JSON malformado
        }
    }

    @Nested
    @DisplayName("Validaciones de Casos Válidos (P0)")
    class ValidCases {

        @ParameterizedTest
        @ValueSource(strings = {"12345678", "123456789", "1234567890", "12345678901", "123456789012"})
        @DisplayName("nationalId válido: debe aceptar 8-12 dígitos")
        void createTicket_validNationalId_shouldSucceed(String validNationalId) {
            String validRequest = String.format("""
                {
                    "nationalId": "%s",
                    "telefono": "+56912345678",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "CAJA"
                }
                """, validNationalId);

            given()
                .contentType("application/json")
                .body(validRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .body("nationalId", equalTo(validNationalId))
                .body("status", equalTo("EN_ESPERA"));
        }

        @Test
        @DisplayName("Todos los queueTypes válidos: debe aceptar CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA")
        void createTicket_allValidQueueTypes_shouldSucceed() {
            QueueType[] validQueues = {QueueType.CAJA, QueueType.PERSONAL_BANKER, QueueType.EMPRESAS, QueueType.GERENCIA};
            
            for (int i = 0; i < validQueues.length; i++) {
                String validRequest = String.format("""
                    {
                        "nationalId": "1234567%d",
                        "telefono": "+5691234567%d",
                        "branchOffice": "Sucursal Centro",
                        "queueType": "%s"
                    }
                    """, i, i, validQueues[i].name());

                given()
                    .contentType("application/json")
                    .body(validRequest)
                    .when()
                    .post("/api/tickets")
                    .then()
                    .statusCode(201)
                    .body("queueType", equalTo(validQueues[i].name()))
                    .body("status", equalTo("EN_ESPERA"));
            }
        }

        @Test
        @DisplayName("Ticket sin teléfono: debe crear ticket exitosamente")
        void createTicket_withoutPhone_shouldSucceed() {
            String validRequest = """
                {
                    "nationalId": "87654321",
                    "branchOffice": "Sucursal Centro",
                    "queueType": "PERSONAL_BANKER"
                }
                """;

            given()
                .contentType("application/json")
                .body(validRequest)
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201)
                .body("nationalId", equalTo("87654321"))
                .body("telefono", nullValue())
                .body("queueType", equalTo("PERSONAL_BANKER"))
                .body("status", equalTo("EN_ESPERA"));
        }
    }
}