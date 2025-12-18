package com.banco.ticketero.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TestConfiguration
public class WireMockConfig {

    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .port(8089)
                        .usingFilesUnderDirectory("src/test/resources/wiremock")
        );

        // Setup default Telegram API mocks
        setupTelegramMocks(wireMockServer);
        
        return wireMockServer;
    }

    private void setupTelegramMocks(WireMockServer server) {
        // Mock successful sendMessage response
        server.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "ok": true,
                                "result": {
                                    "message_id": 123,
                                    "from": {
                                        "id": 123456789,
                                        "is_bot": true,
                                        "first_name": "TicketeroBot"
                                    },
                                    "chat": {
                                        "id": 987654321,
                                        "type": "private"
                                    },
                                    "date": 1640995200,
                                    "text": "Mensaje enviado correctamente"
                                }
                            }
                            """)));

        // Mock getMe endpoint
        server.stubFor(get(urlPathMatching("/bot.*/getMe"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "ok": true,
                                "result": {
                                    "id": 123456789,
                                    "is_bot": true,
                                    "first_name": "TicketeroBot",
                                    "username": "ticketero_test_bot"
                                }
                            }
                            """)));

        // Mock error scenarios
        server.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .withRequestBody(containing("ERROR_TEST"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "ok": false,
                                "error_code": 400,
                                "description": "Bad Request: chat not found"
                            }
                            """)));

        // Mock timeout scenario
        server.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .withRequestBody(containing("TIMEOUT_TEST"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000) // 5 second delay
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"result\":{\"message_id\":1}}")));
    }

    /**
     * Utility method to verify Telegram API calls in tests
     */
    public static void verifyTelegramMessageSent(WireMockServer server, String expectedText) {
        server.verify(
                postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
                        .withRequestBody(containing(expectedText))
        );
    }

    /**
     * Utility method to verify specific number of Telegram calls
     */
    public static void verifyTelegramCallCount(WireMockServer server, int expectedCount) {
        server.verify(expectedCount, 
                postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
        );
    }

    /**
     * Reset all WireMock stubs and requests
     */
    public static void resetWireMock(WireMockServer server) {
        server.resetAll();
    }
}