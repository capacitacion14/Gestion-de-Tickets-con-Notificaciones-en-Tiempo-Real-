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
    vigencia_minutos INTEGER NOT NULL DEFAULT 120,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_advisor_id BIGINT REFERENCES advisor(id),
    assigned_module_number INTEGER,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(50)
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_queue_type ON ticket(queue_type);
CREATE INDEX idx_ticket_national_id ON ticket(national_id);
CREATE INDEX idx_ticket_created_at ON ticket(created_at);
CREATE INDEX idx_ticket_expires_at ON ticket(expires_at);
CREATE INDEX idx_ticket_status_expires ON ticket(status, expires_at);
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

### Configuración de Vigencia por Cola
```sql
CREATE TABLE queue_config (
    queue_type VARCHAR(20) PRIMARY KEY,
    vigencia_minutos INTEGER NOT NULL,
    tiempo_promedio_minutos INTEGER NOT NULL,
    prioridad INTEGER NOT NULL
);

INSERT INTO queue_config VALUES
('CAJA', 60, 5, 1),
('PERSONAL_BANKER', 120, 15, 2),
('EMPRESAS', 180, 20, 3),
('GERENCIA', 240, 30, 4);
```

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

### Query 5: Tickets vencidos para cancelación automática
```sql
SELECT codigo_referencia, numero, status, expires_at
FROM ticket
WHERE status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO')
  AND expires_at < CURRENT_TIMESTAMP
ORDER BY expires_at ASC;
```

### Query 6: Tickets próximos a notificaciones por tiempo
```sql
SELECT t.codigo_referencia, t.numero, t.estimated_wait_minutes
FROM ticket t
WHERE t.status IN ('EN_ESPERA', 'PROXIMO')
  AND t.estimated_wait_minutes IN (15, 5)
  AND NOT EXISTS (
    SELECT 1 FROM message m 
    WHERE m.ticket_id = t.codigo_referencia 
      AND m.plantilla IN ('totem_faltan_15_min', 'totem_faltan_5_min')
      AND m.estado_envio = 'ENVIADO'
  );
```

### Query 7: Configuración de vigencia por cola
```sql
CREATE TABLE queue_config (
    queue_type VARCHAR(20) PRIMARY KEY,
    vigencia_minutos INTEGER NOT NULL,
    tiempo_promedio_minutos INTEGER NOT NULL,
    prioridad INTEGER NOT NULL
);

INSERT INTO queue_config VALUES
('CAJA', 60, 5, 1),
('PERSONAL_BANKER', 120, 15, 2),
('EMPRESAS', 180, 20, 3),
('GERENCIA', 240, 30, 4);
```

---

## 3. Triggers y Funciones

### Trigger: Calcular expires_at automáticamente
```sql
CREATE OR REPLACE FUNCTION calculate_expires_at()
RETURNS TRIGGER AS $$
BEGIN
    SELECT vigencia_minutos INTO NEW.vigencia_minutos
    FROM queue_config 
    WHERE queue_type = NEW.queue_type;
    
    NEW.expires_at := NEW.created_at + (NEW.vigencia_minutos || ' minutes')::INTERVAL;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_expires_at
    BEFORE INSERT ON ticket
    FOR EACH ROW
    EXECUTE FUNCTION calculate_expires_at();
```

### Función: Cancelar tickets vencidos
```sql
CREATE OR REPLACE FUNCTION cancel_expired_tickets()
RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    UPDATE ticket 
    SET status = 'VENCIDO',
        cancelled_at = CURRENT_TIMESTAMP,
        cancel_reason = 'EXPIRED'
    WHERE status IN ('EN_ESPERA', 'PROXIMO', 'ATENDIENDO')
      AND expires_at < CURRENT_TIMESTAMP;
    
    GET DIAGNOSTICS expired_count = ROW_COUNT;
    
    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;
```

---

**Fin del Documento de Diseño de Base de Datos**
