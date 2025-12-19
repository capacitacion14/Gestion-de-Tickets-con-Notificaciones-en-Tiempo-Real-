package com.banco.ticketero.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TicketStatus - Unit Tests")
class TicketStatusTest {

    @Test
    void isActivo_withActiveStatuses_shouldReturnTrue() {
        assertThat(TicketStatus.EN_ESPERA.isActivo()).isTrue();
        assertThat(TicketStatus.PROXIMO.isActivo()).isTrue();
        assertThat(TicketStatus.ATENDIENDO.isActivo()).isTrue();
    }

    @Test
    void isActivo_withInactiveStatuses_shouldReturnFalse() {
        assertThat(TicketStatus.COMPLETADO.isActivo()).isFalse();
        assertThat(TicketStatus.CANCELADO.isActivo()).isFalse();
        assertThat(TicketStatus.VENCIDO.isActivo()).isFalse();
    }

    @Test
    void getDescripcion_shouldReturnDescriptiveText() {
        assertThat(TicketStatus.EN_ESPERA.getDescripcion())
            .isEqualTo("Esperando asignación");
        assertThat(TicketStatus.COMPLETADO.getDescripcion())
            .isEqualTo("Atención finalizada");
    }

    @Test
    void getEstadosActivos_shouldReturnOnlyActiveStatuses() {
        TicketStatus[] activos = TicketStatus.getEstadosActivos();
        
        assertThat(activos)
            .hasSize(3)
            .containsExactly(
                TicketStatus.EN_ESPERA,
                TicketStatus.PROXIMO,
                TicketStatus.ATENDIENDO
            );
    }
}
