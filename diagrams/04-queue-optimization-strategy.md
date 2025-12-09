# Estrategia de Optimización de Colas - Sistema Ticketero

**Arquitecto:** Arquitecto de Software Senior  
**Versión:** 2.0 (Optimizada)  
**Fecha:** Diciembre 2025

---

## 🎯 Decisión de Arquitectura: Enfoque Híbrido

### Recomendación Final

**Para MVP (Fase Piloto: 500-800 tickets/día):**
```
✅ USAR: PostgreSQL como Cola + Scheduler
❌ NO USAR: RabbitMQ/Kafka (over-engineering)
```

**Para Expansión (2,500-3,000 tickets/día):**
```
✅ MIGRAR A: Redis + Spring Events
```

**Para Nacional (25,000+ tickets/día):**
```
✅ MIGRAR A: RabbitMQ/Kafka
```

---

## 📊 Comparación de Opciones

```
┌─────────────────────────────────────────────────────────────────────┐
│              MATRIZ DE DECISIÓN DE COLAS                            │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┬──────────────┬──────────────┬──────────────┬─────────┐
│   Opción     │ Complejidad  │ Performance  │ Escalabilidad│ Costo   │
├──────────────┼──────────────┼──────────────┼──────────────┼─────────┤
│ PostgreSQL   │ ⭐ Baja      │ ⭐⭐ Media   │ ⭐⭐ Media   │ ⭐⭐⭐  │
│ + Scheduler  │              │ (< 1K/día)   │ (< 3K/día)   │ Bajo    │
├──────────────┼──────────────┼──────────────┼──────────────┼─────────┤
│ Redis        │ ⭐⭐ Media   │ ⭐⭐⭐ Alta  │ ⭐⭐⭐ Alta  │ ⭐⭐    │
│ + Pub/Sub    │              │ (< 10K/día)  │ (< 10K/día)  │ Medio   │
├──────────────┼──────────────┼──────────────┼──────────────┼─────────┤
│ RabbitMQ     │ ⭐⭐⭐ Alta  │ ⭐⭐⭐ Alta  │ ⭐⭐⭐⭐    │ ⭐     │
│              │              │ (< 50K/día)  │ (ilimitada)  │ Alto    │
├──────────────┼──────────────┼──────────────┼──────────────┼─────────┤
│ Kafka        │ ⭐⭐⭐⭐    │ ⭐⭐⭐⭐    │ ⭐⭐⭐⭐⭐  │ ⭐     │
│              │ Muy Alta     │ Muy Alta     │ Muy Alta     │ Muy Alto│
└──────────────┴──────────────┴──────────────┴──────────────┴─────────┘

RECOMENDACIÓN POR FASE:
├─ MVP (500-800/día):     PostgreSQL + Scheduler ✅
├─ Expansión (3K/día):    Redis + Spring Events ✅
└─ Nacional (25K+/día):   RabbitMQ ✅
```

---

## 🏗️ SOLUCIÓN OPTIMIZADA: Arquitectura Híbrida Evolutiva

### Fase 1: MVP - PostgreSQL como Cola (ACTUAL)

```
┌─────────────────────────────────────────────────────────────────────┐
│         ARQUITECTURA MVP: PostgreSQL + Scheduler                    │
└─────────────────────────────────────────────────────────────────────┘

VENTAJAS:
✅ Sin infraestructura adicional
✅ ACID garantizado
✅ Simplicidad operacional
✅ Fácil debugging (SQL queries)
✅ Transacciones nativas
✅ Suficiente para 500-800 tickets/día

DESVENTAJAS:
⚠️ No es tiempo real puro (polling cada 30s)
⚠️ Carga adicional en DB
⚠️ Escalabilidad limitada (< 3K/día)

CUÁNDO USAR:
├─ MVP y Fase Piloto
├─ < 1,000 tickets/día
├─ Equipo pequeño (2-3 devs)
└─ Budget limitado


┌──────────────────────────────────────────────────────────────┐
│                    FLUJO ACTUAL                              │
└──────────────────────────────────────────────────────────────┘

1. Ticket Creado
   │
   ▼
2. INSERT INTO message (estado_envio = 'PENDIENTE')
   │
   ▼
3. @Scheduled(fixedDelay = 30000)
   │
   ├─ SELECT * FROM message WHERE estado_envio = 'PENDIENTE'
   │
   ├─ Para cada mensaje:
   │  └─ Enviar a Telegram API
   │
   └─ UPDATE message SET estado_envio = 'ENVIADO'


OPTIMIZACIONES APLICADAS:
├─ Índice en (estado_envio, fecha_programada)
├─ LIMIT 50 (procesamiento en lotes)
├─ Connection pooling (HikariCP)
└─ @Async para no bloquear scheduler
```

---

### Fase 2: Expansión - Redis + Spring Events (RECOMENDADO)

```
┌─────────────────────────────────────────────────────────────────────┐
│      ARQUITECTURA EXPANSIÓN: Redis + Spring Events                 │
└─────────────────────────────────────────────────────────────────────┘

VENTAJAS:
✅ Tiempo real (< 100ms latencia)
✅ Alta performance (10K+ ops/segundo)
✅ Pub/Sub nativo
✅ Persistencia opcional (AOF/RDB)
✅ Fácil integración con Spring
✅ Menor carga en PostgreSQL

DESVENTAJAS:
⚠️ Infraestructura adicional (Redis)
⚠️ Requiere monitoreo adicional
⚠️ Persistencia no garantizada (por defecto)

CUÁNDO MIGRAR:
├─ > 1,000 tickets/día
├─ Necesidad de tiempo real
├─ Múltiples sucursales
└─ Equipo con experiencia en Redis


┌──────────────────────────────────────────────────────────────┐
│              ARQUITECTURA CON REDIS                          │
└──────────────────────────────────────────────────────────────┘

┌─────────────────┐
│ TicketService   │
│ .create()       │
└────────┬────────┘
         │
         ├─ 1. Save ticket to PostgreSQL
         │
         ├─ 2. Publish event to Redis
         │    └─ PUBLISH ticket:created {ticketId, phone}
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Redis Pub/Sub                            │
│                                                             │
│  Channel: ticket:created                                    │
│  Channel: ticket:assigned                                   │
│  Channel: ticket:position-changed                           │
└────────┬────────────────────────────────────────────────────┘
         │
         │ SUBSCRIBE
         │
         ▼
┌─────────────────┐
│ TelegramService │
│ @RedisListener  │
└────────┬────────┘
         │
         ├─ 1. Recibe evento en tiempo real
         │
         ├─ 2. Construye mensaje
         │
         ├─ 3. Envía a Telegram API
         │
         └─ 4. Guarda en PostgreSQL (auditoría)


IMPLEMENTACIÓN:

// 1. Configuración Redis
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
    
    @Bean
    public RedisMessageListenerContainer container() {
        RedisMessageListenerContainer container = 
            new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(
            messageListener(), 
            new PatternTopic("ticket:*")
        );
        return container;
    }
}

// 2. Publisher (TicketService)
@Service
@RequiredArgsConstructor
public class TicketService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        // 1. Guardar en PostgreSQL
        Ticket ticket = ticketRepository.save(newTicket);
        
        // 2. Publicar evento a Redis
        TicketCreatedEvent event = new TicketCreatedEvent(
            ticket.getCodigoReferencia(),
            ticket.getNumero(),
            ticket.getTelefono()
        );
        redisTemplate.convertAndSend("ticket:created", event);
        
        return toResponse(ticket);
    }
}

// 3. Subscriber (TelegramService)
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService implements MessageListener {
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        
        if ("ticket:created".equals(channel)) {
            TicketCreatedEvent event = deserialize(message.getBody());
            sendConfirmationMessage(event);
        }
    }
    
    private void sendConfirmationMessage(TicketCreatedEvent event) {
        // Envío inmediato (< 100ms)
        telegramClient.sendMessage(event.getTelefono(), buildMessage(event));
        
        // Auditoría en PostgreSQL (async)
        auditService.logEvent("MENSAJE_ENVIADO", event);
    }
}


VENTAJAS DE ESTE ENFOQUE:
├─ Latencia < 100ms (vs 30s con scheduler)
├─ PostgreSQL solo para persistencia
├─ Redis maneja eventos en tiempo real
├─ Fácil escalabilidad horizontal
└─ Menor carga en DB principal
```

---

### Fase 3: Nacional - RabbitMQ (FUTURO)

```
┌─────────────────────────────────────────────────────────────────────┐
│         ARQUITECTURA NACIONAL: RabbitMQ                             │
└─────────────────────────────────────────────────────────────────────┘

VENTAJAS:
✅ Garantías de entrega (ACK/NACK)
✅ Dead Letter Queues (DLQ)
✅ Reintentos automáticos
✅ Routing avanzado
✅ Escalabilidad ilimitada
✅ Monitoreo robusto (Management UI)

DESVENTAJAS:
⚠️ Complejidad operacional alta
⚠️ Requiere equipo DevOps
⚠️ Costo de infraestructura
⚠️ Curva de aprendizaje

CUÁNDO MIGRAR:
├─ > 10,000 tickets/día
├─ Múltiples regiones
├─ Necesidad de garantías de entrega
└─ Equipo DevOps dedicado


┌──────────────────────────────────────────────────────────────┐
│              ARQUITECTURA CON RABBITMQ                       │
└──────────────────────────────────────────────────────────────┘

┌─────────────────┐
│ TicketService   │
└────────┬────────┘
         │
         ├─ 1. Save to PostgreSQL
         │
         ├─ 2. Publish to Exchange
         │    └─ rabbitTemplate.convertAndSend(
         │         "ticket.exchange", 
         │         "ticket.created", 
         │         event
         │       )
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│                    RabbitMQ                                 │
│                                                             │
│  Exchange: ticket.exchange (Topic)                          │
│     │                                                       │
│     ├─ Queue: ticket.created.queue                          │
│     │  └─ Binding: ticket.created                           │
│     │                                                       │
│     ├─ Queue: ticket.assigned.queue                         │
│     │  └─ Binding: ticket.assigned                          │
│     │                                                       │
│     └─ Queue: ticket.dlq (Dead Letter Queue)                │
│        └─ Mensajes fallidos después de 3 reintentos         │
└─────────────────────────────────────────────────────────────┘
         │
         │ CONSUME
         │
         ▼
┌─────────────────┐
│ TelegramService │
│ @RabbitListener │
└────────┬────────┘
         │
         ├─ 1. Consume mensaje
         │
         ├─ 2. Envía a Telegram
         │
         ├─ 3. ACK si éxito
         │
         └─ 4. NACK si fallo (reintento automático)


CONFIGURACIÓN:

@Configuration
public class RabbitMQConfig {
    
    @Bean
    public TopicExchange ticketExchange() {
        return new TopicExchange("ticket.exchange");
    }
    
    @Bean
    public Queue ticketCreatedQueue() {
        return QueueBuilder.durable("ticket.created.queue")
            .withArgument("x-dead-letter-exchange", "ticket.dlx")
            .withArgument("x-message-ttl", 300000) // 5 min
            .build();
    }
    
    @Bean
    public Binding ticketCreatedBinding() {
        return BindingBuilder
            .bind(ticketCreatedQueue())
            .to(ticketExchange())
            .with("ticket.created");
    }
}

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {
    
    @RabbitListener(queues = "ticket.created.queue")
    public void handleTicketCreated(TicketCreatedEvent event) {
        try {
            sendConfirmationMessage(event);
            // ACK automático si no hay excepción
        } catch (Exception e) {
            log.error("Error sending message", e);
            // NACK automático, RabbitMQ reintenta
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}


VENTAJAS DE RABBITMQ:
├─ Garantía de entrega (at-least-once)
├─ Reintentos automáticos con backoff
├─ DLQ para mensajes fallidos
├─ Monitoreo visual (Management UI)
├─ Escalabilidad horizontal (clustering)
└─ Routing flexible (topic, fanout, direct)
```

---

## 🎯 Recomendación Final: Plan de Migración

```
┌─────────────────────────────────────────────────────────────────────┐
│              PLAN DE MIGRACIÓN EVOLUTIVA                            │
└─────────────────────────────────────────────────────────────────────┘

FASE 1: MVP (MES 1-3)
┌──────────────────────────────────────────────────────────────┐
│ PostgreSQL + Scheduler                                       │
│                                                              │
│ ✅ Implementar AHORA                                         │
│ ✅ Sin cambios de arquitectura                               │
│ ✅ Optimizar con índices                                     │
│ ✅ Monitorear performance                                    │
│                                                              │
│ KPIs:                                                        │
│ ├─ Latencia promedio: < 30s                                 │
│ ├─ Throughput: 500-800 tickets/día                          │
│ └─ Tasa de éxito: > 99%                                     │
└──────────────────────────────────────────────────────────────┘

FASE 2: EXPANSIÓN (MES 4-6)
┌──────────────────────────────────────────────────────────────┐
│ Migrar a Redis + Spring Events                              │
│                                                              │
│ ✅ Agregar Redis a infraestructura                           │
│ ✅ Implementar Pub/Sub                                       │
│ ✅ Mantener PostgreSQL para auditoría                        │
│ ✅ A/B testing (50% Redis, 50% Scheduler)                    │
│                                                              │
│ KPIs:                                                        │
│ ├─ Latencia promedio: < 1s                                  │
│ ├─ Throughput: 2,500-3,000 tickets/día                      │
│ └─ Tasa de éxito: > 99.5%                                   │
│                                                              │
│ ESFUERZO: 2 semanas dev + 1 semana testing                  │
└──────────────────────────────────────────────────────────────┘

FASE 3: NACIONAL (MES 7+)
┌──────────────────────────────────────────────────────────────┐
│ Migrar a RabbitMQ                                            │
│                                                              │
│ ✅ Implementar RabbitMQ cluster                              │
│ ✅ Configurar DLQ y reintentos                               │
│ ✅ Monitoreo con Prometheus + Grafana                        │
│ ✅ Migración gradual por sucursal                            │
│                                                              │
│ KPIs:                                                        │
│ ├─ Latencia promedio: < 500ms                               │
│ ├─ Throughput: 25,000+ tickets/día                          │
│ └─ Tasa de éxito: > 99.9%                                   │
│                                                              │
│ ESFUERZO: 4 semanas dev + 2 semanas testing                 │
└──────────────────────────────────────────────────────────────┘


CRITERIOS DE MIGRACIÓN:
┌──────────────────────────────────────────────────────────────┐
│ Migrar a Redis SI:                                           │
│ ├─ Tickets/día > 1,000                                       │
│ ├─ Latencia actual > 20s promedio                           │
│ ├─ Múltiples sucursales (> 3)                                │
│ └─ Quejas de clientes por demora                             │
│                                                              │
│ Migrar a RabbitMQ SI:                                        │
│ ├─ Tickets/día > 10,000                                      │
│ ├─ Necesidad de garantías de entrega                         │
│ ├─ Múltiples regiones geográficas                            │
│ └─ Equipo DevOps disponible                                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔧 Optimizaciones Inmediatas (Sin Cambiar Arquitectura)

```
┌─────────────────────────────────────────────────────────────────────┐
│         OPTIMIZACIONES PARA ARQUITECTURA ACTUAL                     │
└─────────────────────────────────────────────────────────────────────┘

1. ÍNDICES COMPUESTOS
   CREATE INDEX idx_message_pending_scheduled 
   ON message(estado_envio, fecha_programada)
   WHERE estado_envio = 'PENDIENTE';
   
   Beneficio: Query 10x más rápida

2. PARTICIONAMIENTO POR FECHA
   CREATE TABLE message_2025_12 PARTITION OF message
   FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');
   
   Beneficio: Queries más rápidas, mantenimiento más fácil

3. PROCESAMIENTO EN LOTES OPTIMIZADO
   @Scheduled(fixedDelay = 10000) // Reducir a 10s
   public void sendPendingMessages() {
       List<Message> batch = messageRepository
           .findPendingMessages(PageRequest.of(0, 100));
       
       // Procesar en paralelo
       batch.parallelStream()
           .forEach(this::sendMessage);
   }
   
   Beneficio: Latencia reducida de 30s a 10s

4. CONNECTION POOLING OPTIMIZADO
   spring.datasource.hikari.maximum-pool-size=20
   spring.datasource.hikari.minimum-idle=10
   spring.datasource.hikari.connection-timeout=30000
   
   Beneficio: Mejor manejo de concurrencia

5. CACHE DE PLANTILLAS
   @Cacheable("message-templates")
   public String getTemplate(MessageTemplate template) {
       return templateRepository.findByName(template);
   }
   
   Beneficio: Reduce queries a DB

6. ASYNC PROCESSING
   @Async("telegramExecutor")
   public CompletableFuture<Void> sendMessage(Message message) {
       // Envío no bloqueante
   }
   
   Beneficio: No bloquea scheduler principal

7. CIRCUIT BREAKER
   @CircuitBreaker(name = "telegram", fallbackMethod = "fallback")
   public void sendToTelegram(String phone, String text) {
       telegramClient.sendMessage(phone, text);
   }
   
   Beneficio: Evita saturación si Telegram falla

8. MONITORING
   @Timed("telegram.send.duration")
   @Counted("telegram.send.attempts")
   public void sendMessage(Message message) {
       // Métricas automáticas
   }
   
   Beneficio: Visibilidad de performance


IMPACTO ESPERADO:
├─ Latencia: 30s → 10s (67% mejora)
├─ Throughput: +50%
├─ Carga en DB: -30%
└─ Tasa de éxito: 99% → 99.5%

ESFUERZO: 3-5 días de desarrollo
```

---

## 📊 Comparación de Costos

```
┌─────────────────────────────────────────────────────────────────────┐
│              ANÁLISIS DE COSTOS (MENSUAL)                           │
└─────────────────────────────────────────────────────────────────────┘

OPCIÓN 1: PostgreSQL + Scheduler (ACTUAL)
├─ Infraestructura: $50/mes (PostgreSQL RDS)
├─ Desarrollo: $0 (ya implementado)
├─ Operación: $0 (sin componentes adicionales)
└─ TOTAL: $50/mes

OPCIÓN 2: Redis + Spring Events
├─ Infraestructura: $50 (PostgreSQL) + $30 (Redis) = $80/mes
├─ Desarrollo: $2,000 (2 semanas dev)
├─ Operación: $100/mes (monitoreo adicional)
└─ TOTAL: $180/mes + $2,000 one-time

OPCIÓN 3: RabbitMQ
├─ Infraestructura: $50 (PostgreSQL) + $100 (RabbitMQ cluster) = $150/mes
├─ Desarrollo: $4,000 (4 semanas dev)
├─ Operación: $200/mes (DevOps + monitoreo)
└─ TOTAL: $350/mes + $4,000 one-time

OPCIÓN 4: Kafka
├─ Infraestructura: $50 (PostgreSQL) + $300 (Kafka cluster) = $350/mes
├─ Desarrollo: $6,000 (6 semanas dev)
├─ Operación: $400/mes (DevOps especializado)
└─ TOTAL: $750/mes + $6,000 one-time


ROI ANALYSIS:
├─ MVP (< 1K/día): PostgreSQL es suficiente ✅
├─ Expansión (3K/día): Redis se paga en 3 meses ✅
└─ Nacional (25K+/día): RabbitMQ se paga en 6 meses ✅
```

---

## ✅ Decisión Final y Acción Inmediata

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DECISIÓN ARQUITECTÓNICA                          │
└─────────────────────────────────────────────────────────────────────┘

PARA MVP (AHORA):
✅ MANTENER: PostgreSQL + Scheduler
✅ APLICAR: 8 optimizaciones inmediatas
✅ MONITOREAR: KPIs de performance
✅ PREPARAR: Migración a Redis (código desacoplado)

CÓDIGO PREPARADO PARA MIGRACIÓN:
// Interface que permite cambiar implementación
public interface MessageQueue {
    void publish(MessageEvent event);
}

// Implementación actual (PostgreSQL)
@Service
@Profile("postgres")
public class PostgresMessageQueue implements MessageQueue {
    public void publish(MessageEvent event) {
        messageRepository.save(toEntity(event));
    }
}

// Implementación futura (Redis)
@Service
@Profile("redis")
public class RedisMessageQueue implements MessageQueue {
    public void publish(MessageEvent event) {
        redisTemplate.convertAndSend("messages", event);
    }
}

// Service usa interface (no implementación)
@Service
@RequiredArgsConstructor
public class TicketService {
    private final MessageQueue messageQueue; // ✅ Desacoplado
    
    public TicketResponse create(CreateTicketRequest request) {
        Ticket ticket = save(request);
        messageQueue.publish(new TicketCreatedEvent(ticket));
        return toResponse(ticket);
    }
}


VENTAJAS DE ESTE ENFOQUE:
├─ Migración sin reescribir código
├─ A/B testing fácil (profiles)
├─ Rollback inmediato si hay problemas
└─ Código limpio y mantenible
```

---

**Versión:** 2.0  
**Fecha:** Diciembre 2025  
**Estado:** ✅ Estrategia Optimizada Aprobada
