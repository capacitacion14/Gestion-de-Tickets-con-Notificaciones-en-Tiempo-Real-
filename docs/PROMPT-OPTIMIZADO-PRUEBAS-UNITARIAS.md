# PROMPT OPTIMIZADO: PRUEBAS UNITARIAS - SISTEMA TICKETERO

## 🎯 CONTEXTO EJECUTIVO

Eres un **QA Senior especializado en testing de Spring Boot**. Tu misión es diseñar e implementar pruebas unitarias **puras y aisladas** para el Sistema Ticketero Digital - un sistema bancario con **arquitectura en capas** (Controller → Service → Repository).

### Sistema Objetivo

- **Arquitectura:** Controller → Service → Repository (Spring Boot 3 capas)
- **Stack:** Spring Boot 3.2 + Java 17 + PostgreSQL + Telegram API
- **Capas:** Controller → Service → Repository → Database
- **Patrones:** Service Layer, Repository Pattern, DTOs
- **Meta cobertura:** >85% en Services y Controllers

---

## 📦 ESTRUCTURA REAL DEL PROYECTO

### Arquitectura Actual

```
src/main/java/com/banco/ticketero/
├── controller/
│   ├── AdminController.java          ✅ EXISTE
│   └── HealthController.java         ✅ EXISTE
├── service/
│   └── TicketLifecycleManager.java   ✅ EXISTE
├── model/
│   ├── QueueType.java                ✅ EXISTE
│   └── TicketStatus.java             ✅ EXISTE
├── SimpleTicketBotInMemory.java
├── SimpleTicketeroApplication.java
└── TelegramConfig.java
```

### Clases a Implementar (según component-design.md)

**Services (5):**
1. TicketService
2. AssignmentService
3. TelegramService
4. QueueService
5. AuditService

**Repositories (4):**
1. TicketRepository
2. AdvisorRepository
3. MessageRepository
4. AuditLogRepository

**DTOs (5):**
1. CreateTicketRequest
2. TicketResponse
3. PositionResponse
4. QueueSummary
5. AuditEvent

**Entities (4):**
1. Ticket
2. Advisor
3. Message
4. AuditLog

---

## 🛠️ STACK TÉCNICO

| Herramienta     | Versión | Estado           | Propósito                |
| --------------- | ------- | ---------------- | ------------------------ |
| **JUnit 5**     | 5.10+   | ✅ Disponible    | Framework base + @Nested |
| **Mockito**     | 5.x     | ✅ Disponible    | Mocks + ArgumentCaptor   |
| **AssertJ**     | 3.24+   | ❌ **FALTANTE**  | Assertions fluidas       |
| **Spring Test** | 6.x     | ✅ Disponible    | ReflectionTestUtils      |
| **Jacoco**      | 0.8.11  | ⚠️ Deshabilitado | Coverage reporting       |

### 🔧 DEPENDENCIAS A AGREGAR

```xml
<!-- Agregar a pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

### ⚙️ HABILITAR JACOCO

```xml
<!-- Agregar a pom.xml en <build><plugins> -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### ❌ PROHIBICIONES

- `@SpringBootTest` (solo para integración)
- `@DataJpaTest` (solo para repositorios)
- TestContainers (solo para E2E)
- Bases de datos reales
- APIs externas reales
- Estado compartido entre tests

---

## 🎯 PLAN DE IMPLEMENTACIÓN DE TESTS

### FASE 1: Setup y Dependencias (5 min)

**Objetivo:** Configurar entorno de testing

**Tareas:**
1. Actualizar pom.xml con dependencias
2. Crear estructura src/test/java/
3. Crear TestDataBuilder base

---

### FASE 2: Tests de Enums (10 tests - 10 min)

**Objetivo:** Validar lógica de negocio en enums

#### QueueTypeTest (6 tests)

```java
package com.banco.ticketero.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QueueType - Unit Tests")
class QueueTypeTest {

    @Test
    void calculateEstimatedTime_conPosicion1_debeRetornarTiempoPromedio() {
        // Given
        QueueType queueType = QueueType.CAJA;
        int position = 1;

        // When
        int estimatedTime = queueType.calculateEstimatedTime(position);

        // Then
        assertThat(estimatedTime).isEqualTo(5);
    }

    @Test
    void calculateEstimatedTime_conPosicion5_debeMultiplicarCorrectamente() {
        // Given
        QueueType queueType = QueueType.PERSONAL_BANKER;
        int position = 5;

        // When
        int estimatedTime = queueType.calculateEstimatedTime(position);

        // Then
        assertThat(estimatedTime).isEqualTo(75); // 5 * 15
    }

    @Test
    void getPrefijo_debeRetornarPrefijoCorrectoPorCola() {
        assertThat(QueueType.CAJA.getPrefijo()).isEqualTo("C");
        assertThat(QueueType.PERSONAL_BANKER.getPrefijo()).isEqualTo("P");
        assertThat(QueueType.EMPRESAS.getPrefijo()).isEqualTo("E");
        assertThat(QueueType.GERENCIA.getPrefijo()).isEqualTo("G");
    }

    @Test
    void getPrioridad_debeRetornarOrdenCorrecto() {
        assertThat(QueueType.CAJA.getPrioridad()).isEqualTo(1);
        assertThat(QueueType.GERENCIA.getPrioridad()).isEqualTo(4);
    }

    @Test
    void getVigenciaMinutos_debeRetornarTiempoVigencia() {
        assertThat(QueueType.CAJA.getVigenciaMinutos()).isEqualTo(60);
        assertThat(QueueType.GERENCIA.getVigenciaMinutos()).isEqualTo(240);
    }

    @Test
    void getTiempoPromedioMinutos_debeRetornarTiempoAtencion() {
        assertThat(QueueType.CAJA.getTiempoPromedioMinutos()).isEqualTo(5);
        assertThat(QueueType.EMPRESAS.getTiempoPromedioMinutos()).isEqualTo(20);
    }
}
```

#### TicketStatusTest (4 tests)

```java
package com.banco.ticketero.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TicketStatus - Unit Tests")
class TicketStatusTest {

    @Test
    void isActivo_conEstadosActivos_debeRetornarTrue() {
        assertThat(TicketStatus.EN_ESPERA.isActivo()).isTrue();
        assertThat(TicketStatus.PROXIMO.isActivo()).isTrue();
        assertThat(TicketStatus.ATENDIENDO.isActivo()).isTrue();
    }

    @Test
    void isActivo_conEstadosInactivos_debeRetornarFalse() {
        assertThat(TicketStatus.COMPLETADO.isActivo()).isFalse();
        assertThat(TicketStatus.CANCELADO.isActivo()).isFalse();
        assertThat(TicketStatus.VENCIDO.isActivo()).isFalse();
    }

    @Test
    void getDescripcion_debeRetornarTextoDescriptivo() {
        assertThat(TicketStatus.EN_ESPERA.getDescripcion())
            .isEqualTo("Esperando asignación");
        assertThat(TicketStatus.COMPLETADO.getDescripcion())
            .isEqualTo("Atención finalizada");
    }

    @Test
    void getEstadosActivos_debeRetornarSoloActivos() {
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
```

---

### FASE 3: Tests de TicketLifecycleManager (8 tests - 15 min)

**Objetivo:** Validar lógica de schedulers y estadísticas

```java
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
```

---

### FASE 4: Tests de AdminController (6 tests - 10 min)

**Objetivo:** Validar endpoints administrativos

```java
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
```

---

### FASE 5: TestDataBuilder (Utilidad Central)

**Objetivo:** Builder pattern para crear objetos de prueba

```java
package com.banco.ticketero.testutil;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.service.TicketLifecycleManager;

import java.time.LocalDateTime;

public class TestDataBuilder {

    // ========== ENUMS ==========
    
    public static QueueType defaultQueueType() {
        return QueueType.CAJA;
    }

    public static QueueType vipQueueType() {
        return QueueType.GERENCIA;
    }

    public static TicketStatus activeStatus() {
        return TicketStatus.EN_ESPERA;
    }

    public static TicketStatus completedStatus() {
        return TicketStatus.COMPLETADO;
    }

    // ========== SCHEDULER STATS ==========

    public static TicketLifecycleManager.SchedulerStats defaultStats() {
        return new TicketLifecycleManager.SchedulerStats(
            10,
            2,
            LocalDateTime.now()
        );
    }

    // ========== DTOs (cuando se implementen) ==========
    
    // TODO: Agregar builders cuando se implementen las clases:
    // - CreateTicketRequest
    // - TicketResponse
    // - PositionResponse
    
    // ========== ENTITIES (cuando se implementen) ==========
    
    // TODO: Agregar builders cuando se implementen las clases:
    // - Ticket
    // - Advisor
    // - Message
    // - AuditLog
}
```

---

## 📐 CONVENCIONES OBLIGATORIAS

### Naming Pattern

```java
// Formato: methodName_condition_expectedBehavior()
create_conDatosValidos_debeRetornarTicketResponse()
assignNextTicket_sinAdvisorsDisponibles_debeLanzarNoAdvisorException()
sendMessage_telegramFalla_debeIncrementarReintentos()
```

### Estructura AAA

```java
@Test
@DisplayName("descripción clara del comportamiento")
void methodName_condition_expectedBehavior() {
    // Given - Setup datos y mocks
    var input = TestDataBuilder.defaultInput();
    when(mockRepository.method()).thenReturn(expected);

    // When - Ejecutar método bajo prueba
    var result = serviceUnderTest.method(input);

    // Then - Verificar resultado y comportamiento
    assertThat(result).isNotNull();
    verify(mockRepository).save(any());
}
```

### Organización @Nested

```java
@Nested
@DisplayName("methodName()")
class MethodName {
    // Agrupar todos los tests del método
}
```

---

## 🚨 ANTI-PATTERNS CRÍTICOS

### ❌ Tests Frágiles

```java
// MAL: Dependiente de tiempo
assertThat(ticket.getCreatedAt()).isEqualTo(LocalDateTime.now());

// BIEN: Verificar existencia
assertThat(ticket.getCreatedAt()).isNotNull();
```

### ❌ Mocks Incorrectos

```java
// MAL: Mock del SUT (System Under Test)
@Mock private TicketService ticketService; // ¡Es lo que testeas!

// BIEN: Mock de dependencias
@Mock private TicketRepository ticketRepository;
```

### ❌ Assertions Vagas

```java
// MAL: Assertion genérica
assertThat(result).isNotNull();

// BIEN: Assertion específica
assertThat(result.getStatus()).isEqualTo(TicketStatus.EN_ESPERA);
```

---

## 📊 ESTRUCTURA FINAL DE TESTS

```
src/test/java/com/banco/ticketero/
├── controller/
│   ├── AdminControllerTest.java (6 tests) ✅
│   └── HealthControllerTest.java (pendiente)
├── service/
│   ├── TicketLifecycleManagerTest.java (8 tests) ✅
│   ├── TicketServiceTest.java (pendiente - 12 tests)
│   ├── AssignmentServiceTest.java (pendiente - 8 tests)
│   ├── TelegramServiceTest.java (pendiente - 10 tests)
│   ├── QueueServiceTest.java (pendiente - 6 tests)
│   └── AuditServiceTest.java (pendiente - 5 tests)
├── model/
│   ├── QueueTypeTest.java (6 tests) ✅
│   └── TicketStatusTest.java (4 tests) ✅
└── testutil/
    └── TestDataBuilder.java ✅
```

**Total Implementado:** 24 tests  
**Total Pendiente:** 41 tests (requieren implementar services primero)  
**Total Objetivo:** 65 tests para 85%+ cobertura

---

## ✅ CRITERIOS DE ACEPTACIÓN

### Tests Implementados (Fase Actual)

- [x] QueueTypeTest: 6/6 tests
- [x] TicketStatusTest: 4/4 tests
- [x] TicketLifecycleManagerTest: 8/8 tests
- [x] AdminControllerTest: 6/6 tests
- [x] TestDataBuilder creado

### Cobertura Esperada (Post-Implementación)

- [ ] Services: >85% cobertura
- [ ] Controllers: >90% cobertura
- [ ] Models/Enums: 100% cobertura
- [ ] Suite completa: <30 segundos

---

## 🚀 COMANDOS DE EJECUCIÓN

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests por capa
mvn test -Dtest="*ControllerTest"
mvn test -Dtest="*ServiceTest"
mvn test -Dtest="*Test" -Dgroups="model"

# Ejecutar test específico
mvn test -Dtest="QueueTypeTest"

# Generar reporte de cobertura
mvn clean test jacoco:report
open target/site/jacoco/index.html

# Ejecutar con logs detallados
mvn test -X
```

---

## 📋 CHECKLIST FINAL

### Pre-Implementación

- [ ] pom.xml actualizado con dependencias
- [ ] Estructura src/test/java/ creada
- [ ] TestDataBuilder implementado
- [ ] Jacoco configurado

### Post-Implementación

- [ ] 24 tests ejecutando correctamente
- [ ] 0 failures, 0 errors
- [ ] Cobertura >80% en clases existentes
- [ ] Tiempo ejecución <10 segundos
- [ ] Reporte Jacoco generado

### Próximos Pasos

- [ ] Implementar TicketService + tests
- [ ] Implementar AssignmentService + tests
- [ ] Implementar TelegramService + tests
- [ ] Implementar QueueService + tests
- [ ] Implementar AuditService + tests
- [ ] Alcanzar 85%+ cobertura total

---

## 💡 NOTAS IMPORTANTES

1. **Prioridad:** Implementar services antes de sus tests
2. **Cobertura:** Foco en lógica de negocio, no getters/setters
3. **Mocks:** Solo para dependencias externas (repositories, APIs)
4. **Assertions:** Usar AssertJ para legibilidad
5. **Naming:** Seguir convención `method_condition_expected`
6. **AAA Pattern:** Siempre Given/When/Then
7. **@Nested:** Agrupar tests por método
8. **@DisplayName:** Descripciones en español

---

**Versión:** 3.0 (Limpia - Solo Arquitectura Real)  
**Última actualización:** Diciembre 2024  
**Estado:** ✅ Documento Completo y Coherente - Listo para Implementación
