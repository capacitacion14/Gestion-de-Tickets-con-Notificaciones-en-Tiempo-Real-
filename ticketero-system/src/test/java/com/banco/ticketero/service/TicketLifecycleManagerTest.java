package com.banco.ticketero.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketLifecycleManager - Unit Tests")
class TicketLifecycleManagerTest {

    private TicketLifecycleManager lifecycleManager;

    @BeforeEach
    void setUp() {
        lifecycleManager = new TicketLifecycleManager();
    }

    @Nested
    @DisplayName("cancelExpiredTickets()")
    class CancelExpiredTickets {

        @Test
        void execute_debeIncrementarContadorProcesados() {
            // Given
            int initialCount = lifecycleManager.getStats().ticketsProcesados();

            // When
            lifecycleManager.cancelExpiredTickets();

            // Then
            int finalCount = lifecycleManager.getStats().ticketsProcesados();
            assertThat(finalCount).isEqualTo(initialCount + 1);
        }

        @Test
        void execute_debeCompletarseEnMenosDe1Segundo() {
            // Given
            long startTime = System.currentTimeMillis();

            // When
            lifecycleManager.cancelExpiredTickets();

            // Then
            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(1000);
        }

        @Test
        void execute_noDebeLanzarExcepciones() {
            assertThatCode(() -> lifecycleManager.cancelExpiredTickets())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("processNotifications()")
    class ProcessNotifications {

        @Test
        void execute_noDebeLanzarExcepciones() {
            assertThatCode(() -> lifecycleManager.processNotifications())
                .doesNotThrowAnyException();
        }

        @Test
        void execute_debeCompletarseRapidamente() {
            // Given
            long startTime = System.currentTimeMillis();

            // When
            lifecycleManager.processNotifications();

            // Then
            long duration = System.currentTimeMillis() - startTime;
            assertThat(duration).isLessThan(500);
        }
    }

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Test
        void execute_debeRetornarEstadisticasValidas() {
            // When
            var stats = lifecycleManager.getStats();

            // Then
            assertThat(stats).isNotNull();
            assertThat(stats.ticketsProcesados()).isGreaterThanOrEqualTo(0);
            assertThat(stats.ticketsVencidos()).isGreaterThanOrEqualTo(0);
            assertThat(stats.ultimaEjecucion()).isNotNull();
        }

        @Test
        void execute_despuesDeEjecucion_debeActualizarStats() {
            // Given
            lifecycleManager.cancelExpiredTickets();

            // When
            var stats = lifecycleManager.getStats();

            // Then
            assertThat(stats.ticketsProcesados()).isGreaterThan(0);
        }

        @Test
        void execute_debeRetornarTimestampReciente() {
            // When
            var stats = lifecycleManager.getStats();

            // Then
            assertThat(stats.ultimaEjecucion())
                .isAfter(LocalDateTime.now().minusSeconds(5));
        }
    }
}
