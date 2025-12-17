# Gestión del Ciclo de Vida de Tickets

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 1. Ciclo de Vida Completo

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CICLO DE VIDA DEL TICKET                         │
└─────────────────────────────────────────────────────────────────────┘

CREACIÓN
┌──────────────────┐
│ Cliente crea     │ ──► vigenciaMinutos = config[queueType]
│ ticket           │ ──► expiresAt = createdAt + vigencia
└────────┬─────────┘ ──► status = EN_ESPERA
         │
         ▼
┌──────────────────┐
│   EN_ESPERA      │ ◄── Esperando asignación
└────────┬─────────┘     Notificaciones: 15min, 5min, 3pos
         │
         ├─── Tiempo ──► VENCIDO (scheduler)
         │
         ▼ posición ≤ 3
┌──────────────────┐
│    PROXIMO       │ ◄── Pre-aviso enviado
└────────┬─────────┘     Cliente debe acercarse
         │
         ├─── Tiempo ──► VENCIDO (scheduler)
         │
         ▼ Ejecutivo disponible
┌──────────────────┐
│   ATENDIENDO     │ ◄── Asignado a ejecutivo
└────────┬─────────┘     En módulo específico
         │
         ├─── Tiempo ──► Continúa (no se cancela)
         │
         ▼ Atención finalizada
┌──────────────────┐
│   COMPLETADO     │ ◄── Estado final exitoso
└──────────────────┘

Estados Alternativos:
┌──────────────────┐
│    CANCELADO     │ ◄── Cliente cancela manualmente
└──────────────────┘

┌──────────────────┐
│   NO_ATENDIDO    │ ◄── Cliente no se presentó
└──────────────────┘

┌──────────────────┐
│     VENCIDO      │ ◄── Expirado por tiempo (automático)
└──────────────────┘
```

---

## 2. Configuración de Vigencia

### Por Tipo de Cola

| QueueType | Vigencia | Razón | Tiempo Promedio |
|-----------|----------|-------|-----------------|
| CAJA | 60 min | Transacciones rápidas | 5 min |
| PERSONAL_BANKER | 120 min | Consultas complejas | 15 min |
| EMPRESAS | 180 min | Procesos corporativos | 20 min |
| GERENCIA | 240 min | Casos especiales | 30 min |

### Cálculo Automático

```sql
-- Al crear ticket
vigenciaMinutos = SELECT vigencia_minutos FROM queue_config WHERE queue_type = :queueType
expiresAt = createdAt + (vigenciaMinutos || ' minutes')::INTERVAL
```

---

## 3. Proceso de Cancelación Automática

### Scheduler Configuration

```java
@Scheduled(fixedDelay = 60000) // Cada 60 segundos
public void cancelExpiredTickets() {
    // Proceso automático
}
```

### Algoritmo de Cancelación

```
1. QUERY: Obtener tickets vencidos
   SELECT * FROM ticket 
   WHERE status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO')
     AND expires_at < CURRENT_TIMESTAMP

2. PARA CADA TICKET VENCIDO:
   a. Si status = 'ATENDIENDO' → Solo registrar, NO cancelar
   b. Si status IN ('EN_ESPERA', 'PROXIMO'):
      - Cambiar status = 'VENCIDO'
      - Establecer cancelled_at = NOW()
      - Establecer cancel_reason = 'EXPIRED'
      - Programar mensaje totem_ticket_vencido
      - Registrar auditoría

3. RECALCULAR POSICIONES:
   - Actualizar position_in_queue de tickets restantes
   - Actualizar estimated_wait_minutes
   - Disparar notificaciones si cruzan umbrales

4. MÉTRICAS:
   - Incrementar contador tickets_vencidos_hoy
   - Actualizar dashboard
```

### Estados y Transiciones

```
REGLAS DE CANCELACIÓN POR ESTADO:

EN_ESPERA + vencido    → VENCIDO ✅
PROXIMO + vencido      → VENCIDO ✅  
ATENDIENDO + vencido   → Continúa ❌ (no se cancela)
COMPLETADO + vencido   → N/A (ya finalizado)
CANCELADO + vencido    → N/A (ya cancelado)
VENCIDO + vencido      → N/A (ya vencido)
```

---

## 4. Sistema de Notificaciones Progresivas

### Triggers de Notificación

| Condición | Plantilla | Momento |
|-----------|-----------|---------|
| Ticket creado | totem_ticket_creado | Inmediato |
| estimatedWaitMinutes ≤ 15 | totem_faltan_15_min | Al recalcular |
| estimatedWaitMinutes ≤ 5 | totem_faltan_5_min | Al recalcular |
| positionInQueue ≤ 3 | totem_proximo_turno | Al recalcular |
| Asignado a ejecutivo | totem_es_tu_turno | Al asignar |
| Ticket vencido | totem_ticket_vencido | Al vencer |

### Algoritmo de Notificaciones

```
@Scheduled(fixedDelay = 30000) // Cada 30 segundos
public void checkNotificationTriggers() {
    
    // 1. Recalcular posiciones y tiempos
    recalculateQueuePositions();
    
    // 2. Verificar umbrales de tiempo
    List<Ticket> tickets15min = findTicketsWithEstimatedTime(15);
    List<Ticket> tickets5min = findTicketsWithEstimatedTime(5);
    
    // 3. Enviar notificaciones no enviadas
    for (Ticket ticket : tickets15min) {
        if (!hasNotificationSent(ticket, "totem_faltan_15_min")) {
            scheduleMessage(ticket, "totem_faltan_15_min");
        }
    }
    
    for (Ticket ticket : tickets5min) {
        if (!hasNotificationSent(ticket, "totem_faltan_5_min")) {
            scheduleMessage(ticket, "totem_faltan_5_min");
        }
    }
}
```

---

## 5. Métricas y Monitoreo

### Dashboard de Vigencia

```json
{
  "tickets_activos": 45,
  "tickets_proximos_vencer": 8,
  "tickets_vencidos_hoy": 12,
  "tasa_vencimiento": "3.2%",
  "tiempo_promedio_vida": "85 minutos",
  "vigencia_por_cola": {
    "CAJA": {"activos": 15, "vencidos_hoy": 2},
    "PERSONAL_BANKER": {"activos": 20, "vencidos_hoy": 5},
    "EMPRESAS": {"activos": 8, "vencidos_hoy": 3},
    "GERENCIA": {"activos": 2, "vencidos_hoy": 2}
  }
}
```

### Alertas Automáticas

| Condición | Severidad | Acción |
|-----------|-----------|--------|
| Tasa vencimiento > 10% | ALTA | Alerta supervisor |
| Tickets próximos vencer > 20 | MEDIA | Revisar capacidad |
| Scheduler no ejecuta > 2min | CRÍTICA | Alerta técnica |

---

## 6. Queries de Administración

### Consultar tickets próximos a vencer

```sql
SELECT 
    numero,
    queue_type,
    expires_at,
    EXTRACT(EPOCH FROM (expires_at - CURRENT_TIMESTAMP))/60 AS minutos_restantes
FROM ticket 
WHERE status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO')
  AND expires_at BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '30 minutes'
ORDER BY expires_at ASC;
```

### Estadísticas de vencimiento por cola

```sql
SELECT 
    queue_type,
    COUNT(*) FILTER (WHERE status = 'VENCIDO' AND DATE(cancelled_at) = CURRENT_DATE) as vencidos_hoy,
    COUNT(*) FILTER (WHERE status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO')) as activos,
    ROUND(AVG(EXTRACT(EPOCH FROM (COALESCE(cancelled_at, completed_at, CURRENT_TIMESTAMP) - created_at))/60), 1) as tiempo_promedio_vida_min
FROM ticket 
WHERE DATE(created_at) = CURRENT_DATE
GROUP BY queue_type;
```

### Rendimiento del scheduler

```sql
CREATE TABLE scheduler_metrics (
    id BIGSERIAL PRIMARY KEY,
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tickets_processed INTEGER,
    tickets_expired INTEGER,
    execution_duration_ms INTEGER,
    success BOOLEAN
);
```

---

## 7. Configuración Técnica

### Application Properties

```yaml
ticketero:
  scheduler:
    cancel-expired:
      enabled: true
      fixed-delay: 60000  # 60 segundos
    notifications:
      enabled: true
      fixed-delay: 30000  # 30 segundos
  queue-config:
    caja:
      vigencia-minutos: 60
    personal-banker:
      vigencia-minutos: 120
    empresas:
      vigencia-minutos: 180
    gerencia:
      vigencia-minutos: 240
```

### Componentes Spring

```java
@Component
@EnableScheduling
public class TicketLifecycleManager {
    
    @Scheduled(fixedDelayString = "${ticketero.scheduler.cancel-expired.fixed-delay}")
    public void cancelExpiredTickets() { }
    
    @Scheduled(fixedDelayString = "${ticketero.scheduler.notifications.fixed-delay}")
    public void processNotifications() { }
}
```

---

**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Estado:** Documentación Completa