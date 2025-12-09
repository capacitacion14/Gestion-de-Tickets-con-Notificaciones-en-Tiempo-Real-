# Diseño de Base de Datos - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 1. Scripts DDL

### Tabla: ticket
```sql
CREATE TABLE ticket (
    codigo_referencia UUID PRIMARY KEY,
    numero VARCHAR(10) NOT NULL,
    national_id VARCHAR(50) NOT NULL,
    telefono VARCHAR(20),
    branch_office VARCHAR(100) NOT NULL,
    queue_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_in_queue INTEGER,
    estimated_wait_minutes INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_advisor_id BIGINT REFERENCES advisor(id),
    assigned_module_number INTEGER,
    completed_at TIMESTAMP
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_queue_type ON ticket(queue_type);
CREATE INDEX idx_ticket_national_id ON ticket(national_id);
CREATE INDEX idx_ticket_created_at ON ticket(created_at);
CREATE UNIQUE INDEX idx_ticket_numero ON ticket(numero);
```

### Tabla: advisor
```sql
CREATE TABLE advisor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    module_number INTEGER NOT NULL,
    supported_queues TEXT[] NOT NULL,
    assigned_tickets_count INTEGER DEFAULT 0,
    last_assignment_at TIMESTAMP
);

CREATE INDEX idx_advisor_status ON advisor(status);
CREATE INDEX idx_advisor_module_number ON advisor(module_number);
```

### Tabla: message
```sql
CREATE TABLE message (
    id BIGSERIAL PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES ticket(codigo_referencia),
    plantilla VARCHAR(50) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL,
    fecha_programada TIMESTAMP NOT NULL,
    fecha_envio TIMESTAMP,
    telegram_message_id VARCHAR(50),
    intentos INTEGER DEFAULT 0
);

CREATE INDEX idx_message_estado_envio ON message(estado_envio);
CREATE INDEX idx_message_fecha_programada ON message(fecha_programada);
CREATE INDEX idx_message_ticket_id ON message(ticket_id);
```

### Tabla: audit_log
```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type VARCHAR(50) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    previous_state JSONB,
    new_state JSONB NOT NULL,
    metadata JSONB
);

CREATE INDEX idx_audit_entity_id ON audit_log(entity_id);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_event_type ON audit_log(event_type);
```

---

## 2. Queries Críticas

### Query 1: Calcular posición en cola
```sql
SELECT COUNT(*) + 1 AS position
FROM ticket
WHERE queue_type = :queueType
  AND status IN ('EN_ESPERA', 'PROXIMO')
  AND created_at < :ticketCreatedAt;
```

### Query 2: Obtener siguiente ticket para asignar
```sql
SELECT *
FROM ticket
WHERE status IN ('EN_ESPERA', 'PROXIMO')
ORDER BY 
    CASE queue_type
        WHEN 'GERENCIA' THEN 4
        WHEN 'EMPRESAS' THEN 3
        WHEN 'PERSONAL_BANKER' THEN 2
        WHEN 'CAJA' THEN 1
    END DESC,
    created_at ASC
LIMIT 1;
```

### Query 3: Obtener asesor disponible con menor carga
```sql
SELECT *
FROM advisor
WHERE status = 'AVAILABLE'
  AND :queueType = ANY(supported_queues)
ORDER BY assigned_tickets_count ASC, last_assignment_at ASC
LIMIT 1;
```

### Query 4: Mensajes pendientes de envío
```sql
SELECT *
FROM message
WHERE estado_envio = 'PENDIENTE'
  AND fecha_programada <= CURRENT_TIMESTAMP
ORDER BY fecha_programada ASC;
```

---

**Fin del Documento de Diseño de Base de Datos**
