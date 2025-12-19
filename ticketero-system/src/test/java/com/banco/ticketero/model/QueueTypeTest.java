package com.banco.ticketero.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QueueType - Unit Tests")
class QueueTypeTest {

    @Test
    void calculateEstimatedTime_withPosition1_shouldReturnAverageTime() {
        // Given
        QueueType queueType = QueueType.CAJA;
        int position = 1;

        // When
        int estimatedTime = queueType.calculateEstimatedTime(position);

        // Then
        assertThat(estimatedTime).isEqualTo(5);
    }

    @Test
    void calculateEstimatedTime_withPosition5_shouldMultiplyCorrectly() {
        // Given
        QueueType queueType = QueueType.PERSONAL_BANKER;
        int position = 5;

        // When
        int estimatedTime = queueType.calculateEstimatedTime(position);

        // Then
        assertThat(estimatedTime).isEqualTo(75); // 5 * 15
    }

    @Test
    void getPrefijo_shouldReturnCorrectPrefixPerQueue() {
        assertThat(QueueType.CAJA.getPrefijo()).isEqualTo("C");
        assertThat(QueueType.PERSONAL_BANKER.getPrefijo()).isEqualTo("P");
        assertThat(QueueType.EMPRESAS.getPrefijo()).isEqualTo("E");
        assertThat(QueueType.GERENCIA.getPrefijo()).isEqualTo("G");
    }

    @Test
    void getPrioridad_shouldReturnCorrectOrder() {
        assertThat(QueueType.CAJA.getPrioridad()).isEqualTo(1);
        assertThat(QueueType.GERENCIA.getPrioridad()).isEqualTo(4);
    }

    @Test
    void getVigenciaMinutos_shouldReturnValidityTime() {
        assertThat(QueueType.CAJA.getVigenciaMinutos()).isEqualTo(60);
        assertThat(QueueType.GERENCIA.getVigenciaMinutos()).isEqualTo(240);
    }

    @Test
    void getTiempoPromedioMinutos_shouldReturnAverageAttentionTime() {
        assertThat(QueueType.CAJA.getTiempoPromedioMinutos()).isEqualTo(5);
        assertThat(QueueType.EMPRESAS.getTiempoPromedioMinutos()).isEqualTo(20);
    }
}
