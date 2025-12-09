# Diagrama de Arquitectura Técnica - Sistema Ticketero

## Arquitectura de 3 Capas

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA TÉCNICA COMPLETA                    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     CAPA DE PRESENTACIÓN                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │ TicketController │  │ AdminController  │  │ QueueController │  │
│  │                  │  │                  │  │                 │  │
│  │ @RestController  │  │ @RestController  │  │ @RestController │  │
│  │ @RequestMapping  │  │ @RequestMapping  │  │ @RequestMapping │  │
│  │ ("/api/tickets") │  │ ("/api/admin")   │  │ ("/api/queues") │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬────────┘  │
│           │                     │                      │           │
│           │ DTOs (Records)      │                      │           │
│           │ - CreateTicketReq   │                      │           │
│           │ - TicketResponse    │                      │           │
│           │ - PositionResponse  │                      │           │
│           │                     │                      │           │
└───────────┼─────────────────────┼──────────────────────┼───────────┘
            │                     │                      │
            ▼                     ▼                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CAPA DE NEGOCIO                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐   │
│  │ TicketService   │  │ AssignmentService│  │ TelegramService │   │
│  │                 │  │                  │  │                 │   │
│  │ @Service        │  │ @Service         │  │ @Service        │   │
│  │ @Transactional  │  │ @Transactional   │  │ @Async          │   │
│  │                 │  │                  │  │                 │   │
│  │ • create()      │  │ • assignNext()   │  │ • schedule()    │   │
│  │ • findById()    │  │ • complete()     │  │ • sendPending() │   │
│  │ • calcPosition()│  │ • cancel()       │  │ • retry()       │   │
│  └────────┬────────┘  └────────┬─────────┘  └────────┬────────┘   │
│           │                    │                     │            │
│  ┌─────────────────┐  ┌──────────────────┐                        │
│  │ QueueService    │  │ AuditService     │                        │
│  │                 │  │                  │                        │
│  │ @Service        │  │ @Service         │                        │
│  │                 │  │                  │                        │
│  │ • getAllQueues()│  │ • logEvent()     │                        │
│  │ • getStats()    │  │ • findByEntity() │                        │
│  └────────┬────────┘  └────────┬─────────┘                        │
│           │                    │                                  │
└───────────┼────────────────────┼──────────────────────────────────┘
            │                    │
            ▼                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CAPA DE DATOS                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │ TicketRepository │  │ AdvisorRepository│  │ MessageRepository│ │
│  │                  │  │                  │  │                 │  │
│  │ @Repository      │  │ @Repository      │  │ @Repository     │  │
│  │ extends JPA      │  │ extends JPA      │  │ extends JPA     │  │
│  │                  │  │                  │  │                 │  │
│  │ • save()         │  │ • findAvailable()│  │ • findPending() │  │
│  │ • findById()     │  │ • updateStatus() │  │ • save()        │  │
│  │ • findByStatus() │  │                  │  │                 │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬────────┘  │
│           │                     │                      │           │
│  ┌──────────────────┐                                              │
│  │ AuditRepository  │                                              │
│  │                  │                                              │
│  │ @Repository      │                                              │
│  │ extends JPA      │                                              │
│  │                  │                                              │
│  │ • save()         │                                              │
│  │ • findByEntity() │                                              │
│  └────────┬─────────┘                                              │
│           │                                                        │
└───────────┼────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      BASE DE DATOS                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│                        PostgreSQL 15                                │
│                                                                     │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐            │
│  │ ticket  │  │ advisor │  │ message │  │ audit_log │            │
│  └─────────┘  └─────────┘  └─────────┘  └───────────┘            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                    SERVICIOS EXTERNOS                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────┐          │
│  │            Telegram Bot API                          │          │
│  │                                                      │          │
│  │  • sendMessage()                                     │          │
│  │  • Webhook para respuestas                           │          │
│  │  • Rate limit: 30 msg/segundo                        │          │
│  └──────────────────────────────────────────────────────┘          │
│                            ▲                                        │
│                            │                                        │
│                            │ HTTPS                                  │
│                            │                                        │
│                   ┌────────┴────────┐                               │
│                   │ TelegramService │                               │
│                   └─────────────────┘                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Stack Tecnológico Detallado

```
┌─────────────────────────────────────────────────────────────────────┐
│                    STACK TECNOLÓGICO                                │
└─────────────────────────────────────────────────────────────────────┘

BACKEND
├─ Java 21 LTS
├─ Spring Boot 3.2.x
│  ├─ Spring Web (REST)
│  ├─ Spring Data JPA
│  ├─ Spring Validation
│  ├─ Spring Scheduling
│  └─ Spring Security (JWT)
│
├─ Lombok
│  ├─ @RequiredArgsConstructor
│  ├─ @Slf4j
│  └─ @Data
│
└─ Records (Java 21)
   └─ DTOs inmutables

BASE DE DATOS
├─ PostgreSQL 15
├─ Flyway (migraciones)
└─ HikariCP (connection pool)

MENSAJERÍA
├─ Telegram Bot API
├─ RestTemplate / WebClient
├─ Retry con backoff exponencial
└─ Circuit Breaker (Resilience4j)

COLAS (Evolutivo)
├─ MVP: PostgreSQL + Scheduler
├─ Expansión: Redis + Pub/Sub
└─ Nacional: RabbitMQ

TESTING
├─ JUnit 5
├─ Mockito
├─ TestContainers (PostgreSQL)
└─ REST Assured

DOCUMENTACIÓN
├─ OpenAPI 3.0
├─ Swagger UI
└─ JavaDoc

BUILD & DEPLOY
├─ Maven 3.9.x
├─ Docker
├─ Docker Compose
└─ GitHub Actions (CI/CD)

MONITOREO
├─ Spring Actuator
├─ Micrometer (métricas)
├─ Prometheus + Grafana
└─ Logs estructurados (JSON)

RESILIENCIA
├─ Circuit Breaker (Resilience4j)
├─ Retry automático
├─ Rate Limiting
└─ Bulkhead (aislamiento)
```

---

## Flujo de Datos Técnico

```
┌─────────────────────────────────────────────────────────────────────┐
│              FLUJO TÉCNICO: CREAR TICKET                            │
└─────────────────────────────────────────────────────────────────────┘

1. REQUEST HTTP
   POST /api/tickets
   Content-Type: application/json
   {
     "nationalId": "12345678-9",
     "telefono": "+56912345678",
     "branchOffice": "Sucursal Centro",
     "queueType": "CAJA"
   }
   │
   ▼
2. CONTROLLER
   @PostMapping
   @Valid CreateTicketRequest
   │
   ├─ Validación automática (@Valid)
   │  └─ @NotBlank, @NotNull
   │
   ▼
3. SERVICE
   @Transactional
   TicketService.create()
   │
   ├─ Validar RN-001 (ticket activo único)
   ├─ Generar UUID
   ├─ Generar número (RN-005, RN-006)
   ├─ Calcular posición (RN-010)
   ├─ Calcular tiempo estimado
   │
   ▼
4. REPOSITORY
   TicketRepository.save()
   │
   ├─ JPA persist
   │
   ▼
5. DATABASE
   INSERT INTO ticket (...)
   │
   ├─ Commit transacción
   │
   ▼
6. SERVICE (continuación)
   TelegramService.scheduleMessages()
   │
   ├─ Crear 3 registros en tabla message
   │  • Mensaje 1: PENDIENTE (inmediato)
   │  • Mensaje 2: PENDIENTE (cuando posición ≤ 3)
   │  • Mensaje 3: PENDIENTE (cuando asignado)
   │
   ▼
7. AUDIT
   AuditService.logEvent()
   │
   ├─ INSERT INTO audit_log
   │  eventType: "TICKET_CREADO"
   │
   ▼
8. RESPONSE HTTP
   201 Created
   {
     "codigoReferencia": "uuid-123",
     "numero": "C01",
     "positionInQueue": 5,
     "estimatedWaitMinutes": 25
   }


┌─────────────────────────────────────────────────────────────────────┐
│         PROCESO ASÍNCRONO: ENVÍO DE MENSAJES                        │
└─────────────────────────────────────────────────────────────────────┘

@Scheduled(fixedDelay = 30000) // Cada 30 segundos
TelegramService.sendPendingMessages()
│
├─ Query: SELECT * FROM message 
│         WHERE estado_envio = 'PENDIENTE'
│         AND fecha_programada <= NOW()
│
├─ Para cada mensaje:
│  │
│  ├─ RestTemplate.postForEntity(
│  │    "https://api.telegram.org/bot{token}/sendMessage",
│  │    body
│  │  )
│  │
│  ├─ Si éxito (200 OK):
│  │  └─ UPDATE message SET estado_envio = 'ENVIADO'
│  │
│  └─ Si fallo:
│     ├─ intentos++
│     ├─ Si intentos < 4:
│     │  └─ Programar reintento (backoff exponencial)
│     └─ Si intentos >= 4:
│        └─ UPDATE message SET estado_envio = 'FALLIDO'
│
└─ AuditService.logEvent("MENSAJE_ENVIADO" | "MENSAJE_FALLIDO")
```

---

## Patrones de Diseño Aplicados

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PATRONES DE DISEÑO                               │
└─────────────────────────────────────────────────────────────────────┘

1. REPOSITORY PATTERN
   ┌──────────────┐
   │   Service    │
   └──────┬───────┘
          │ usa
          ▼
   ┌──────────────┐
   │  Repository  │ ◄─── Abstracción de persistencia
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │      DB      │
   └──────────────┘

2. SERVICE LAYER PATTERN
   ┌──────────────┐
   │  Controller  │ ◄─── Solo HTTP
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │   Service    │ ◄─── Lógica de negocio
   └──────────────┘

3. DTO PATTERN
   ┌──────────────┐
   │   Entity     │ ◄─── Modelo de dominio
   └──────┬───────┘
          │ mapea
          ▼
   ┌──────────────┐
   │     DTO      │ ◄─── Transferencia de datos
   └──────────────┘

4. STRATEGY PATTERN
   QueueType.CAJA.calculateEstimatedTime()
   QueueType.GERENCIA.calculateEstimatedTime()
   ▲
   └─── Cada tipo tiene su estrategia

5. TEMPLATE METHOD
   MessageTemplate.totem_ticket_creado
   MessageTemplate.totem_proximo_turno
   MessageTemplate.totem_es_tu_turno
   ▲
   └─── Plantillas reutilizables
```

---

## Seguridad

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CAPAS DE SEGURIDAD                               │
└─────────────────────────────────────────────────────────────────────┘

NIVEL 1: TRANSPORTE
├─ HTTPS obligatorio
├─ TLS 1.3
└─ Certificados SSL

NIVEL 2: AUTENTICACIÓN
├─ JWT para endpoints /api/admin/*
├─ Roles: SUPERVISOR, ADVISOR
└─ Endpoints públicos: /api/tickets (sin auth)

NIVEL 3: VALIDACIÓN
├─ @Valid en todos los @RequestBody
├─ Sanitización de inputs
├─ Rate limiting (100 req/min por IP)
└─ Validación de formato RUT

NIVEL 4: DATOS
├─ Encriptación de national_id en DB
├─ Encriptación de telefono en DB
├─ Passwords con BCrypt
└─ Secrets en variables de entorno

NIVEL 5: AUDITORÍA
├─ Todos los accesos registrados
├─ Logs de seguridad separados
├─ Retención: 1 año
└─ Alertas de intentos sospechosos
```

---

**Versión:** 1.0  
**Fecha:** Diciembre 2025
