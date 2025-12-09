# Diseño Detallado de Componentes - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Arquitecto:** Arquitecto de Software Senior  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 1. TicketService

### Responsabilidad
Gestionar el ciclo de vida completo de tickets: creación, consulta, cálculo de posición y tiempo estimado.

### Métodos Públicos

```java
public interface TicketService {
    TicketResponse create(CreateTicketRequest request);
    Optional<TicketResponse> findByCodigoReferencia(UUID codigoReferencia);
    Optional<TicketResponse> findByNumero(String numero);
    PositionResponse calculatePosition(UUID codigoReferencia);
}
```

### Reglas de Negocio Implementadas
- RN-001: Validar unicidad de ticket activo
- RN-005: Generar número de ticket con formato correcto
- RN-006: Aplicar prefijo según tipo de cola
- RN-010: Calcular tiempo estimado

---

## 2. AssignmentService

### Responsabilidad
Asignar automáticamente tickets a ejecutivos disponibles aplicando prioridades y balanceo de carga.

### Métodos Públicos

```java
public interface AssignmentService {
    void assignNextTicket();
    void completeTicket(UUID ticketId, Long advisorId);
    void cancelTicket(UUID ticketId, String reason);
}
```

### Reglas de Negocio Implementadas
- RN-002: Prioridad de colas
- RN-003: Orden FIFO dentro de cola
- RN-004: Balanceo de carga entre asesores
- RN-013: Estados de asesor

---

## 3. TelegramService

### Responsabilidad
Enviar notificaciones vía Telegram con reintentos automáticos y backoff exponencial.

### Métodos Públicos

```java
public interface TelegramService {
    void scheduleMessages(Ticket ticket);
    void sendPendingMessages();
    void retryFailedMessages();
}
```

### Reglas de Negocio Implementadas
- RN-007: Reintentos automáticos (3 intentos)
- RN-008: Backoff exponencial (30s, 60s, 120s)
- RN-012: Enviar pre-aviso cuando posición ≤ 3

---

## 4. QueueService

### Responsabilidad
Gestionar información de colas, métricas y estadísticas.

### Métodos Públicos

```java
public interface QueueService {
    List<QueueSummary> getAllQueues();
    QueueDetail getQueueDetail(QueueType type);
    QueueStats getQueueStats(QueueType type);
}
```

### Reglas de Negocio Implementadas
- RN-002: Prioridad de colas
- RN-010: Tiempo promedio por cola

---

## 5. AuditService

### Responsabilidad
Registrar todos los eventos del sistema para trazabilidad completa.

### Métodos Públicos

```java
public interface AuditService {
    void logEvent(AuditEvent event);
    List<AuditLog> findByEntityId(String entityId);
    List<AuditLog> findByEventType(String eventType);
}
```

### Reglas de Negocio Implementadas
- RN-011: Auditoría obligatoria de eventos críticos

---

## 6. DTOs (Request/Response)

### CreateTicketRequest
```java
public record CreateTicketRequest(
    @NotBlank String nationalId,
    String telefono,
    @NotBlank String branchOffice,
    @NotNull QueueType queueType
) {}
```

### TicketResponse
```java
public record TicketResponse(
    UUID codigoReferencia,
    String numero,
    QueueType queueType,
    TicketStatus status,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    String assignedAdvisor,
    Integer assignedModuleNumber,
    LocalDateTime createdAt
) {}
```

### PositionResponse
```java
public record PositionResponse(
    String numero,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    TicketStatus status,
    LocalDateTime calculatedAt
) {}
```

---

## 7. Enums

### QueueType
```java
public enum QueueType {
    CAJA(1, "C", 5),
    PERSONAL_BANKER(2, "P", 15),
    EMPRESAS(3, "E", 20),
    GERENCIA(4, "G", 30);
    
    private final int priority;
    private final String prefix;
    private final int averageTimeMinutes;
}
```

### TicketStatus
```java
public enum TicketStatus {
    EN_ESPERA, PROXIMO, ATENDIENDO, 
    COMPLETADO, CANCELADO, NO_ATENDIDO
}
```

### AdvisorStatus
```java
public enum AdvisorStatus {
    AVAILABLE, BUSY, OFFLINE
}
```

---

**Fin del Documento de Diseño de Componentes**
