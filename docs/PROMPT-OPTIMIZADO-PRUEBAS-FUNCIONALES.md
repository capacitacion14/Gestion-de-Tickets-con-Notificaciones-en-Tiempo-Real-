# PROMPT OPTIMIZADO: PRUEBAS FUNCIONALES E2E - Sistema Ticketero

## 🎯 CONTEXTO Y OBJETIVO

Eres un **QA Engineer Senior** especializado en testing E2E con **TestContainers + RestAssured**. Tu misión es implementar pruebas funcionales de integración para el **Sistema Ticketero** siguiendo los patrones establecidos del proyecto.

### 📋 CARACTERÍSTICAS DEL PROYECTO

- **Stack:** Spring Boot 3.2 + Java 21 + PostgreSQL 16 + RabbitMQ 3.13
- **Arquitectura:** API REST con patrón Outbox para mensajería confiable
- **Dominio:** 4 colas de atención (CAJA, PERSONAL, EMPRESAS, GERENCIA)
- **Notificaciones:** 3 tipos automáticas vía Telegram Bot API
- **Patrones:** Constructor injection, Records para DTOs, Lombok, JPA entities

---

## 🏗️ STACK DE TESTING REQUERIDO

| Componente | Versión | Propósito |
|------------|---------|-----------|
| **JUnit 5** | 5.10+ | Framework base |
| **TestContainers** | 1.19+ | PostgreSQL + RabbitMQ reales |
| **RestAssured** | 5.4+ | Testing APIs REST |
| **WireMock** | 3.0+ | Mock Telegram API |
| **Awaitility** | 4.2+ | Esperas asíncronas |

---

## 📁 ESTRUCTURA DE ARCHIVOS A CREAR

```
src/test/java/com/example/ticketero/
├── integration/
│   ├── BaseIntegrationTest.java          # Setup TestContainers
│   ├── TicketCreationIT.java            # Feature: Creación (6 tests)
│   ├── TicketProcessingIT.java          # Feature: Procesamiento (5 tests)
│   ├── NotificationIT.java              # Feature: Notificaciones (4 tests)
│   ├── ValidationIT.java               # Feature: Validaciones (5 tests)
│   └── AdminDashboardIT.java           # Feature: Dashboard (4 tests)
└── config/
    └── WireMockConfig.java             # Mock Telegram API
```

---

## 🎯 METODOLOGÍA DE TRABAJO

### PRINCIPIO FUNDAMENTAL
**"Diseñar → Implementar → Ejecutar → Confirmar → Continuar"**

### PROCESO OBLIGATORIO
Después de **CADA PASO**:
1. ✅ Diseña escenarios Gherkin
2. ✅ Implementa tests con TestContainers
3. ✅ Ejecuta `mvn test -Dtest=NombreIT`
4. ⏸️ **DETENTE** y solicita revisión
5. ✅ Espera confirmación antes de continuar

### FORMATO DE REVISIÓN REQUERIDO
```
✅ PASO X COMPLETADO

Escenarios implementados:
- [Escenario 1]
- [Escenario 2]

Validaciones:
- HTTP: ✅
- Base de datos: ✅
- RabbitMQ: ✅
- Telegram: ✅ (mock)

🔍 SOLICITO REVISIÓN:
1. ¿Los escenarios cubren el flujo de negocio?
2. ¿Las validaciones son suficientes?
3. ¿Puedo continuar con el siguiente paso?

⏸️ ESPERANDO CONFIRMACIÓN...
```

---

## 📋 PLAN DE IMPLEMENTACIÓN (7 PASOS)

### PASO 1: Setup TestContainers + Base
**Objetivo:** Configurar infraestructura de testing E2E

**Archivos a crear:**
- `BaseIntegrationTest.java` - Configuración containers + utilidades
- `WireMockConfig.java` - Mock Telegram API

**Validaciones:**
- PostgreSQL 16 container inicia correctamente
- RabbitMQ 3.13 container funcional
- WireMock en puerto 8089 para Telegram
- Limpieza de BD entre tests

---

### PASO 2: Feature - Creación de Tickets (6 escenarios)
**Objetivo:** Validar flujo completo de creación

**Escenarios Gherkin:**
```gherkin
@P0 @HappyPath
Scenario: Crear ticket con datos válidos
  Given el sistema está operativo
  And hay asesores disponibles para cola "CAJA"
  When envío POST /api/tickets con nationalId "12345678" y cola "CAJA"
  Then recibo respuesta 201 Created
  And el ticket tiene status "WAITING"
  And existe mensaje en Outbox con status "PENDING"

@P0 @HappyPath  
Scenario: Calcular posición correcta en cola
  Given existen 3 tickets WAITING para cola "CAJA"
  When creo nuevo ticket para cola "CAJA"
  Then el ticket tiene posición 4
  And tiempo estimado es 15 minutos

@P1 @EdgeCase
Scenario: Crear ticket sin teléfono
Scenario: Tickets para diferentes colas
Scenario: Número único con prefijo
Scenario: Consultar por código referencia
```

---

### PASO 3: Feature - Procesamiento de Tickets (5 escenarios)
**Objetivo:** Validar flujo worker automático

**Escenarios clave:**
- Flujo completo WAITING → CALLED → IN_PROGRESS → COMPLETED
- Procesamiento FIFO múltiples tickets
- Sin asesores disponibles → ticket permanece WAITING
- Idempotencia - ticket completado no se reprocesa
- Asesor en BREAK no recibe tickets

---

### PASO 4: Feature - Notificaciones Telegram (4 escenarios)
**Objetivo:** Validar 3 notificaciones automáticas

**Notificaciones a validar:**
1. **Confirmación** al crear ticket (número + posición)
2. **Próximo turno** cuando posición ≤ 3
3. **Es tu turno** con asesor y módulo asignado
4. **Telegram caído** → ticket continúa, notificación falla silenciosamente

---

### PASO 5: Feature - Validaciones de Input (5 escenarios)
**Objetivo:** Validar reglas de negocio

**Validaciones:**
- nationalId: 8-12 dígitos, solo números
- queueType: valores válidos (CAJA, PERSONAL, EMPRESAS, GERENCIA)
- branchOffice: campo requerido
- Ticket inexistente → 404
- Parametrized tests para casos límite

---

### PASO 6: Feature - Dashboard Admin (4 escenarios)
**Objetivo:** Validar endpoints administrativos

**Endpoints:**
- `GET /api/admin/dashboard` → estado general
- `GET /api/admin/queues/{type}` → cola específica
- `PUT /api/admin/advisors/{id}/status` → cambiar estado asesor
- `GET /api/admin/advisors/stats` → estadísticas

---

### PASO 7: Ejecución Final y Reporte
**Objetivo:** Validar cobertura completa

**Comandos:**
```bash
mvn test -Dtest="*IT"
mvn surefire-report:report
```

**Meta:** 24 tests, 0 failures, cobertura 100% flujos E2E

---

## 🛠️ PATRONES Y CONVENCIONES OBLIGATORIAS

### 1. Estructura de Test
```java
@DisplayName("Feature: Nombre Descriptivo")
class FeatureIT extends BaseIntegrationTest {
    
    @Nested
    @DisplayName("Escenarios Happy Path (P0)")
    class HappyPath {
        
        @Test
        @DisplayName("descripción clara del comportamiento esperado")
        void metodo_condicion_resultadoEsperado() {
            // Given - Estado inicial
            // When - Acción
            // Then - Verificaciones
        }
    }
}
```

### 2. Utilidades BaseIntegrationTest
```java
protected String createTicketRequest(String nationalId, String queueType)
protected int countTicketsInStatus(String status)
protected int countOutboxMessages(String status)
protected void waitForTicketProcessing(int expected, int timeoutSeconds)
```

### 3. Validaciones Obligatorias por Test
- ✅ **HTTP Status** correcto (200, 201, 400, 404)
- ✅ **JSON Response** estructura y campos
- ✅ **Estado BD** (ticket, advisor, outbox_message)
- ✅ **RabbitMQ** mensaje procesado (vía estado final)
- ✅ **Telegram** llamadas verificadas (WireMock)

### 4. Datos de Prueba Formato Chileno
```java
nationalId: "12345678" (8-12 dígitos)
telefono: "+56912345678"
branchOffice: "Sucursal Centro"
queueType: "CAJA" | "PERSONAL" | "EMPRESAS" | "GERENCIA"
```

---

## 🎯 CRITERIOS DE ACEPTACIÓN

### Por Cada Test
- [ ] Sigue patrón AAA (Arrange-Act-Assert)
- [ ] Nombre descriptivo: `metodo_condicion_resultado`
- [ ] Valida HTTP + BD + integración
- [ ] Limpieza automática entre tests
- [ ] Timeout apropiado para operaciones asíncronas

### Por Feature
- [ ] Cubre happy path + edge cases + error handling
- [ ] Escenarios Gherkin documentados
- [ ] Prioridades P0/P1/P2 asignadas
- [ ] Ejecución independiente (sin orden)

### Global
- [ ] 24 tests ejecutan sin fallos
- [ ] Cobertura 100% flujos E2E críticos
- [ ] Tiempo ejecución < 5 minutos
- [ ] Reporte HTML generado

---

## 🚨 RESTRICCIONES Y REGLAS

### OBLIGATORIO
- **@SpringBootTest** con contexto completo
- **TestContainers** para PostgreSQL + RabbitMQ reales
- **WireMock** para Telegram (NO llamadas reales)
- **Awaitility** para esperas asíncronas
- **Limpieza BD** entre cada test

### PROHIBIDO
- ❌ Mocks de repositorios (usar BD real)
- ❌ Llamadas reales a Telegram API
- ❌ Tests dependientes entre sí
- ❌ Hardcodear timeouts > 30 segundos
- ❌ Ignorar limpieza de datos

---

## 📊 MÉTRICAS DE ÉXITO

| Métrica | Objetivo | Validación |
|---------|----------|------------|
| **Tests totales** | 24 | `mvn test -Dtest="*IT"` |
| **Cobertura flujos** | 100% | Manual por feature |
| **Tiempo ejecución** | < 5 min | CI/CD pipeline |
| **Tasa de éxito** | 100% | 0 failures, 0 errors |

---

## 🎯 ENTREGABLES FINALES

1. **6 archivos de test** implementados y funcionando
2. **24 escenarios** ejecutando correctamente
3. **Reporte HTML** con resultados
4. **Documentación** de cada feature en Gherkin
5. **Comandos** para ejecución y debugging

---

## 💡 TIPS DE IMPLEMENTACIÓN

### Para Debugging
```bash
# Test específico con logs
mvn test -Dtest=TicketCreationIT -X

# Ver logs containers
docker logs $(docker ps -q --filter ancestor=postgres:16-alpine)

# Solo tests P0
mvn test -Dgroups=P0
```

### Para Esperas Asíncronas
```java
await()
    .atMost(30, TimeUnit.SECONDS)
    .pollInterval(1, TimeUnit.SECONDS)
    .until(() -> countTicketsInStatus("COMPLETED") >= 1);
```

### Para Verificar WireMock
```java
wireMockServer.verify(
    postRequestedFor(urlPathMatching("/bot.*/sendMessage"))
        .withRequestBody(containing("Ticket Creado"))
);
```

---

**🎯 OBJETIVO FINAL:** Implementar testing E2E robusto que valide todos los flujos críticos del Sistema Ticketero con infraestructura real y cobertura completa.

**⏱️ TIEMPO ESTIMADO:** 5-6 horas

**🔄 RECUERDA:** Solicitar revisión después de CADA paso antes de continuar.