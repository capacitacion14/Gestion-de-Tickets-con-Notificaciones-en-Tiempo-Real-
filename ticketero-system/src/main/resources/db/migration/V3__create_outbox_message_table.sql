CREATE TABLE outbox_message (
    id BIGSERIAL PRIMARY KEY,
    ticket_id UUID NOT NULL,
    plantilla VARCHAR(50) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL,
    fecha_programada TIMESTAMP NOT NULL,
    fecha_envio TIMESTAMP,
    telegram_message_id VARCHAR(50),
    intentos INTEGER DEFAULT 0,
    chat_id VARCHAR(50),
    CONSTRAINT fk_outbox_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(codigo_referencia)
);

CREATE INDEX idx_outbox_estado_envio ON outbox_message(estado_envio);
CREATE INDEX idx_outbox_fecha_programada ON outbox_message(fecha_programada);
CREATE INDEX idx_outbox_ticket_id ON outbox_message(ticket_id);
