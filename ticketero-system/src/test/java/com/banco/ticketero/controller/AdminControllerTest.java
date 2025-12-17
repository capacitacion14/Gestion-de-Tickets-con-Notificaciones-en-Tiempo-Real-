package com.banco.ticketero.controller;

import com.banco.ticketero.service.TicketLifecycleManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController - Unit Tests")
class AdminControllerTest {

    @Mock
    private TicketLifecycleManager lifecycleManager;

    @InjectMocks
    private AdminController adminController;

    @Nested
    @DisplayName("getSchedulerStatus()")
    class GetSchedulerStatus {

        @Test
        void execute_debeRetornarStats() {
            // Given
            var expectedStats = new TicketLifecycleManager.SchedulerStats(
                10, 2, LocalDateTime.now()
            );
            when(lifecycleManager.getStats()).thenReturn(expectedStats);

            // When
            ResponseEntity<TicketLifecycleManager.SchedulerStats> response = 
                adminController.getSchedulerStatus();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedStats);
            verify(lifecycleManager).getStats();
        }
    }

    @Nested
    @DisplayName("runSchedulerManually()")
    class RunSchedulerManually {

        @Test
        void execute_debeEjecutarAmbosSchedulers() {
            // When
            ResponseEntity<Map<String, Object>> response = 
                adminController.runSchedulerManually();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(lifecycleManager).cancelExpiredTickets();
            verify(lifecycleManager).processNotifications();
        }

        @Test
        void execute_debeRetornarMensajeExito() {
            // When
            ResponseEntity<Map<String, Object>> response = 
                adminController.runSchedulerManually();

            // Then
            assertThat(response.getBody())
                .containsEntry("success", true)
                .containsKey("message")
                .containsKey("timestamp");
        }

        @Test
        void execute_conError_debeRetornar500() {
            // Given
            doThrow(new RuntimeException("Test error"))
                .when(lifecycleManager).cancelExpiredTickets();

            // When
            ResponseEntity<Map<String, Object>> response = 
                adminController.runSchedulerManually();

            // Then
            assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody())
                .containsEntry("success", false);
        }
    }

    @Nested
    @DisplayName("getDashboard()")
    class GetDashboard {

        @Test
        void execute_debeRetornarDashboardCompleto() {
            // Given
            var stats = new TicketLifecycleManager.SchedulerStats(
                5, 1, LocalDateTime.now()
            );
            when(lifecycleManager.getStats()).thenReturn(stats);

            // When
            ResponseEntity<Map<String, Object>> response = 
                adminController.getDashboard();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                .containsKeys("ticketsActivos", "ticketsVencidos", 
                             "schedulerStats", "lastUpdated");
        }

        @Test
        void execute_debeIncluirSchedulerStats() {
            // Given
            var stats = new TicketLifecycleManager.SchedulerStats(
                10, 3, LocalDateTime.now()
            );
            when(lifecycleManager.getStats()).thenReturn(stats);

            // When
            ResponseEntity<Map<String, Object>> response = 
                adminController.getDashboard();

            // Then
            assertThat(response.getBody().get("schedulerStats"))
                .isEqualTo(stats);
        }
    }
}
