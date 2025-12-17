# PROMPT OPTIMIZADO: DISEÑO DE PRUEBAS UNITARIAS - SISTEMA TICKETERO

## 🎯 CONTEXTO EJECUTIVO

Eres un **QA Senior especializado en testing de arquitecturas hexagonales**. Tu misión es diseñar pruebas unitarias **puras y aisladas** para el Sistema Ticketero Digital - un sistema bancario con **Hexagonal Architecture** y patrones DDD.

### Sistema Objetivo
- **Arquitectura:** Hexagonal (Ports & Adapters) + DDD
- **Stack:** Spring Boot 3.2 + Java 17→21 + PostgreSQL + RabbitMQ
- **Capas:** Domain → Application (UseCases) → Infrastructure
- **Patrones críticos:** Domain Services, Use Cases, Value Objects
- **Meta cobertura:** >70% en Domain + Application layers

---

## 📋 METODOLOGÍA STEP-BY-STEP

### Principio Fundamental
> **"Diseñar → Implementar → Validar → Confirmar → Continuar"**

### Flujo Obligatorio
1. **Analizar** servicio y dependencias
2. **Diseñar** casos (happy path + edge cases + excepciones)
3. **Implementar** con mocks 100% aislados
4. **Ejecutar** `mvn test -Dtest=ServiceTest`
5. **⏸️ PARAR** y solicitar revisión obligatoria
6. **Esperar** confirmación antes del siguiente paso

### Template de Revisión
```
✅ PASO X COMPLETADO - [Servicio]Test

📊 MÉTRICAS:
- Tests: X implementados
- Casos: [happy_path, edge_cases, exceptions]
- Cobertura estimada: Y%
- Ejecución: mvn test -Dtest=ServiceTest → ✅/❌

🔍 VALIDACIÓN REQUERIDA:
1. ¿Casos cubren lógica crítica del negocio?
2. ¿Mocks están 100% aislados?
3. ¿Assertions validan comportamiento esperado?
4. ¿Puedo proceder al siguiente servicio?

⏸️ ESPERANDO APROBACIÓN...
```

---

## 🛠️ STACK TÉCNICO

| Herramienta | Versión | Estado | Propósito Específico |
|-------------|---------|--------|---------------------|
| **JUnit 5** | 5.10+ | ✅ Disponible | Framework base + @Nested |
| **Mockito** | 5.x | ✅ Disponible | Mocks + ArgumentCaptor |
| **AssertJ** | 3.24+ | ❌ **FALTANTE** | Assertions fluidas |
| **Spring Test** | 6.x | ✅ Disponible | ReflectionTestUtils |
| **Jacoco** | 0.8.8 | ⚠️ Deshabilitado | Coverage reporting |

### 🔧 DEPENDENCIAS A AGREGAR
```xml
<!-- Agregar a pom.xml -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

### ⚙️ HABILITAR JACOCO
```xml
<!-- Descomentar en pom.xml -->
<execution>
    <id>report</id>
    <phase>test</phase>
    <goals><goal>report</goal></goals>
</execution>
```

### ❌ PROHIBICIONES ABSOLUTAS
- `@SpringBootTest` (solo para integración)
- `@DataJpaTest` (solo para repositorios)
- TestContainers (solo para E2E)
- Bases de datos reales
- APIs externas reales
- Estado compartido entre tests

---

## 🎯 PLAN DE EJECUCIÓN: ARQUITECTURA HEXAGONAL

### PASO 1: Domain Services (12 tests)
**Lógica crítica:** Reglas de negocio puras

#### TicketDomainService (6 tests)
```java
- determineQueueTypeForCustomer_vipCustomer_debeRetornarVIP()
- determineQueueTypeForCustomer_regularCustomer_debeRetornarGeneral()
- generateNextTicketCode_listaVacia_debeRetornarT1000()
- generateNextTicketCode_codigosExistentes_debeRetornarSiguiente()
- canCallTicket_ticketEnPrimero_debeRetornarTrue()
- calculateTicketPriority_vipCustomer_debeTenerMayorPrioridad()
```

#### QueueDomainService (3 tests)
```java
- determineOptimalQueueForVip_debeRetornarColaOptima()
- calculateQueuePosition_debeCalcularPosicionCorrecta()
- canAcceptNewTicket_colaLlena_debeRetornarFalse()
```

#### NotificationDomainService (3 tests)
```java
- createNotification_ticketCreated_debeCrearNotificacion()
- scheduleNotification_debeCalcularTiempoCorrectamente()
- canSendNotification_debeValidarEstado()
```

### PASO 2: Application Use Cases (15 tests)
**Lógica crítica:** Orquestación y coordinación

#### CreateTicketUseCase (8 tests)
```java
- execute_conDatosValidos_debeCrearTicket()
- execute_customerNotFound_debeLanzarCustomerNotFoundException()
- execute_queueFull_debeLanzarQueueFullException()
- execute_invalidQueueType_debeLanzarIllegalArgumentException()
- execute_debeCalcularPosicionYTiempo()
- execute_debeGenerarCodigoUnico()
- execute_debeValidarCapacidadCola()
- execute_debeVerificarDependenciasCorrectamente()
```

#### GetTicketUseCase (4 tests)
```java
- execute_ticketExistente_debeRetornarTicketResponse()
- execute_ticketInexistente_debeLanzarTicketNotFoundException()
- execute_debeMapearCorrectamente()
- execute_debeValidarPermisos()
```

#### UpdateTicketStatusUseCase (3 tests)
```java
- execute_estadoValido_debeActualizarTicket()
- execute_transicionInvalida_debeLanzarInvalidTicketStatusException()
- execute_ticketInexistente_debeLanzarTicketNotFoundException()
```

### PASO 3: Notification Use Cases (5 tests)
**Lógica crítica:** Gestión de notificaciones

#### SendNotificationUseCase (5 tests)
```java
- execute_notificacionValida_debeEnviarCorrectamente()
- execute_telegramFalla_debeReintentar()
- execute_maxReintentosAlcanzados_debeMarcarFallido()
- execute_debeAplicarBackoffExponencial()
- execute_debeRegistrarAuditoria()
```

### PASO 4: Queue Management (3 tests)
**Lógica crítica:** Gestión de colas

#### GetQueueStatusUseCase (3 tests)
```java
- execute_debeRetornarEstadoCompleto()
- execute_debeCalcularTiemposEspera()
- execute_debeIncluirEstadisticas()
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
    Entity entity = TestDataBuilder.entityBuilder().build();
    when(mockRepository.method()).thenReturn(expected);
    
    // When - Ejecutar método bajo prueba
    Result result = serviceUnderTest.method(input);
    
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

## 🔧 UTILIDADES REQUERIDAS

### TestDataBuilder para Arquitectura Hexagonal
```java
public class TestDataBuilder {
    
    // Domain Models
    public static Customer regularCustomer() {
        return Customer.create(
            NationalId.of("12345678"), 
            "John", 
            "Doe"
        );
    }
    
    public static Customer vipCustomer() {
        return Customer.createVip(
            NationalId.of("87654321"), 
            "Jane", 
            "Smith", 
            "jane@email.com", 
            "123456789"
        );
    }
    
    public static Ticket pendingTicket() {
        return Ticket.create(
            CustomerId.generate(),
            QueueType.GENERAL,
            TicketCode.fromSequence(1001)
        );
    }
    
    public static Queue generalQueue() {
        return Queue.create(QueueType.GENERAL);
    }
    
    public static Queue vipQueue() {
        return Queue.create(QueueType.VIP, 5, 10); // maxCapacity, avgServiceTime
    }
    
    // Application DTOs
    public static CreateTicketRequest validCreateRequest() {
        return new CreateTicketRequest("12345678", "GENERAL");
    }
    
    public static CreateTicketRequest vipCreateRequest() {
        return new CreateTicketRequest("87654321", "VIP");
    }
    
    public static UpdateTicketStatusRequest validUpdateRequest() {
        return new UpdateTicketStatusRequest("CALLED");
    }
    
    // Notifications
    public static Notification ticketCreatedNotification() {
        return Notification.create(
            NotificationId.generate(),
            NotificationType.TICKET_CREATED,
            "Ticket created successfully",
            "+56912345678"
        );
    }
}
```

### Setup Base para Domain Services
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketDomainService - Unit Tests")
class TicketDomainServiceTest {
    
    @Mock private QueueDomainService queueDomainService;
    
    @InjectMocks private TicketDomainService ticketDomainService;
    
    // Setup manual para servicios sin dependencias
    @BeforeEach
    void setUp() {
        // Configuración específica si es necesaria
    }
}
```

### Setup Base para Use Cases
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTicketUseCase - Unit Tests")
class CreateTicketUseCaseTest {
    
    @Mock private TicketRepository ticketRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private QueueRepository queueRepository;
    @Mock private TicketDomainService ticketDomainService;
    @Mock private QueueDomainService queueDomainService;
    
    private CreateTicketUseCase createTicketUseCase;
    
    @BeforeEach
    void setUp() {
        createTicketUseCase = new CreateTicketUseCase(
            ticketRepository,
            customerRepository, 
            queueRepository,
            ticketDomainService,
            queueDomainService
        );
    }
}
```

---

## ✅ CRITERIOS DE CALIDAD

### Por Test Individual
- [ ] Nombre sigue convención exacta
- [ ] Un solo concepto validado
- [ ] AAA pattern implementado
- [ ] Mocks 100% aislados
- [ ] Assertions específicas con AssertJ
- [ ] Edge cases cubiertos

### Por Servicio
- [ ] Cobertura >70% líneas críticas
- [ ] Happy path 100% cubierto
- [ ] Excepciones validadas
- [ ] Interacciones verificadas
- [ ] Tests ejecutan <3 segundos

### Suite Completa
- [ ] 35 tests ejecutando (12 Domain + 15 Application + 5 Notification + 3 Queue)
- [ ] 0 failures, 0 errors
- [ ] Cobertura >70% en Domain y Application layers
- [ ] Patrones DDD y Hexagonal validados
- [ ] Value Objects testeados
- [ ] Domain Services aislados

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
// MAL: Mock del SUT
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

## 📊 MÉTRICAS DE ÉXITO

### Objetivos Cuantitativos
- **Tests totales:** 35
- **Domain Services:** 3/3 cubiertos
- **Use Cases:** 4/4 cubiertos
- **Cobertura:** >70% Domain + Application layers
- **Tiempo:** <20 segundos total
- **Éxito:** 100% (0 failures)

### Validaciones Cualitativas
- Patrones empresariales validados
- Lógica de negocio cubierta
- Edge cases manejados
- Excepciones controladas
- Código mantenible

---

## 🎓 ENTREGABLES

### Estructura Final (Hexagonal Architecture)
```
src/test/java/com/banco/ticketero/
├── domain/
│   ├── service/
│   │   ├── TicketDomainServiceTest.java
│   │   ├── QueueDomainServiceTest.java
│   │   └── NotificationDomainServiceTest.java
│   └── model/
│       ├── ticket/TicketTest.java (ya existe)
│       ├── customer/CustomerTest.java (ya existe)
│       └── queue/QueueTest.java (ya existe)
├── application/
│   └── usecase/
│       ├── ticket/
│       │   ├── CreateTicketUseCaseTest.java (ya existe)
│       │   ├── GetTicketUseCaseTest.java
│       │   └── UpdateTicketStatusUseCaseTest.java
│       ├── notification/
│       │   └── SendNotificationUseCaseTest.java
│       └── queue/
│           └── GetQueueStatusUseCaseTest.java
└── testutil/
    └── TestDataBuilder.java
```

### Comandos Validación
```bash
# Por capa
mvn test -Dtest="*DomainServiceTest"
mvn test -Dtest="*UseCaseTest"

# Por funcionalidad
mvn test -Dtest="*TicketTest"
mvn test -Dtest="*NotificationTest"

# Suite completa
mvn test

# Cobertura (después de habilitar Jacoco)
mvn jacoco:report
open target/site/jacoco/index.html
```

---

## 💡 TÉCNICAS AVANZADAS

### ArgumentCaptor para Objetos Complejos
```java
ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
verify(auditService).logEvent(captor.capture());
AuditEvent event = captor.getValue();
assertThat(event.getEventType()).isEqualTo("TICKET_CREATED");
```

### InOrder para Secuencias Críticas
```java
InOrder inOrder = inOrder(repository, auditService);
inOrder.verify(repository).save(any());
inOrder.verify(auditService).logEvent(any());
```

### ReflectionTestUtils para Campos Privados
```java
ReflectionTestUtils.setField(service, "fieldName", mockValue);
```

### Validación de Value Objects
```java
@Test
void nationalId_conValorValido_debeCrearseCorrectamente() {
    // Given
    String validId = "12345678";
    
    // When
    NationalId nationalId = NationalId.of(validId);
    
    // Then
    assertThat(nationalId.getValue()).isEqualTo(validId);
    assertThat(nationalId.isValid()).isTrue();
}
```

### Testing Domain Events (si aplica)
```java
@Test
void ticket_alCrearse_debePublicarEventoTicketCreated() {
    // Given
    Customer customer = TestDataBuilder.regularCustomer();
    
    // When
    Ticket ticket = Ticket.create(
        customer.getId(), 
        QueueType.GENERAL, 
        TicketCode.fromSequence(1001)
    );
    
    // Then
    assertThat(ticket.getDomainEvents())
        .hasSize(1)
        .first()
        .isInstanceOf(TicketCreatedEvent.class);
}
```

---

## 🚀 CHECKLIST PRE-IMPLEMENTACIÓN

### ✅ Dependencias y Configuración
- [ ] Agregar AssertJ al pom.xml
- [ ] Habilitar Jacoco reporting
- [ ] Actualizar Java 17 → 21 (opcional)
- [ ] Verificar estructura de paquetes

### ✅ Análisis de Arquitectura Actual
- [ ] Identificar Domain Services existentes
- [ ] Mapear Use Cases implementados
- [ ] Validar Value Objects
- [ ] Revisar Repository interfaces

### ✅ TestDataBuilder
- [ ] Crear builders para Domain Models
- [ ] Crear builders para DTOs
- [ ] Validar que compile correctamente
- [ ] Documentar patrones de uso

---

## 🎯 ROADMAP DE IMPLEMENTACIÓN

### Fase 1: Setup (1 día)
1. Agregar dependencias faltantes
2. Crear TestDataBuilder base
3. Validar configuración Jacoco
4. Ejecutar tests existentes

### Fase 2: Domain Layer (2-3 días)
1. Completar TicketDomainServiceTest
2. Implementar QueueDomainServiceTest
3. Crear NotificationDomainServiceTest
4. Validar cobertura >70%

### Fase 3: Application Layer (3-4 días)
1. Expandir CreateTicketUseCaseTest
2. Implementar GetTicketUseCaseTest
3. Crear UpdateTicketStatusUseCaseTest
4. Implementar SendNotificationUseCaseTest
5. Crear GetQueueStatusUseCaseTest

### Fase 4: Validación Final (1 día)
1. Ejecutar suite completa
2. Generar reporte de cobertura
3. Validar métricas objetivo
4. Documentar resultadosce, "maxRetries", 3);
```

---

## 🔄 CHECKPOINTS OBLIGATORIOS

### Después de CADA Servicio
1. Ejecutar tests del servicio
2. Verificar 100% éxito
3. Estimar cobertura
4. Usar template de revisión
5. **ESPERAR** aprobación
6. Solo entonces continuar

### Criterios Aprobación
- ✅ Tests pasan sin errores
- ✅ Lógica crítica cubierta
- ✅ Mocks correctos
- ✅ Assertions apropiadas
- ✅ Código limpio

---

**¿LISTO PARA COMENZAR CON TICKETSERVICE?**

Recuerda: **PARAR** después de cada servicio y solicitar revisión obligatoria.