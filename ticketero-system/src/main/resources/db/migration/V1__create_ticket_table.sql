CREATE TABLE ticket (
    codigo_referencia UUID PRIMARY KEY,
    numero VARCHAR(10) NOT NULL UNIQUE,
    national_id VARCHAR(50) NOT NULL,
    telefono VARCHAR(20),
    branch_office VARCHAR(100) NOT NULL,
    queue_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_in_queue INTEGER,
    estimated_wait_minutes INTEGER,
    vigencia_minutos INTEGER NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_advisor_id BIGINT,
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
