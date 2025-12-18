package com.banco.ticketero.integration.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Slf4j
public class WireMockConfig {

    private WireMockServer wireMockServer;
    private static final int WIREMOCK_PORT = 8089;

    public void startWireMock() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            wireMockServer = new WireMockServer(options().port(WIREMOCK_PORT));
            wireMockServer.start();
            WireMock.configureFor("localhost", WIREMOCK_PORT);
            
            setupTelegramMocks();
            log.info("WireMock started on port {}", WIREMOCK_PORT);
        }
    }

    public void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            log.info("WireMock stopped");
        }
    }

    public void resetWireMock() {
        if (wireMockServer != null) {
            wireMockServer.resetAll();
            setupTelegramMocks();
        }
    }

    private void setupTelegramMocks() {
        // Mock exitoso para sendMessage
        wireMockServer.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "ok": true,
                        "result": {
                            "message_id": 123,
                            "date": 1640995200,
                            "chat": {
                                "id": 123456789,
                                "type": "private"
                            },
                            "text": "Mensaje enviado correctamente"
                        }
                    }
                    """)));

        // Mock para getMe (verificar bot)
        wireMockServer.stubFor(get(urlPathMatching("/bot.*/getMe"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "ok": true,
                        "result": {
                            "id": 123456789,
                            "is_bot": true,
                            "first_name": "Test Bot",
                            "username": "test_bot"
                        }
                    }
                    """)));

        log.info("Telegram API mocks configured");
    }

    // Métodos para verificar llamadas en tests
    public void verifyTicketCreatedNotification() {
        wireMockServer.verify(
            postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
                .withRequestBody(containing("Ticket Creado"))
        );
    }

    public void verifyNextTurnNotification() {
        wireMockServer.verify(
            postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
                .withRequestBody(containing("Próximo Turno"))
        );
    }

    public void verifyYourTurnNotification() {
        wireMockServer.verify(
            postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
                .withRequestBody(containing("Es tu turno"))
        );
    }

    public void verifyNoTelegramCalls() {
        wireMockServer.verify(0, postRequestedFor(urlPathMatching("/bot.*/sendMessage")));
    }

    public int getTelegramCallCount() {
        return wireMockServer.countRequestsMatching(
            postRequestedFor(urlPathMatching("/bot.*/sendMessage")).build()
        ).getCount();
    }

    // Simular fallo de Telegram
    public void simulateTelegramFailure() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")));
    }

    // Simular timeout de Telegram
    public void simulateTelegramTimeout() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlPathMatching("/bot.*/sendMessage"))
            .willReturn(aResponse()
                .withFixedDelay(30000) // 30 segundos
                .withStatus(200)));
    }

    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }
}