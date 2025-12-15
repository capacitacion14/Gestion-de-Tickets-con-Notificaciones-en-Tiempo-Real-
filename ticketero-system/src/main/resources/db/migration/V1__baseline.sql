-- Baseline: Configuración inicial de BD
CREATE SCHEMA IF NOT EXISTS ticketero;
SET search_path TO ticketero;

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Función para timestamps automáticos
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Tipos ENUM
CREATE TYPE ticket_status AS ENUM (
    'PENDING',
    'CALLED', 
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
    'NO_SHOW'
);

CREATE TYPE queue_type AS ENUM (
    'GENERAL',
    'PRIORITY',
    'BUSINESS',
    'VIP'
);

CREATE TYPE notification_type AS ENUM (
    'TICKET_CREATED',
    'TICKET_CALLED',
    'QUEUE_UPDATE'
);

CREATE TYPE notification_status AS ENUM (
    'PENDING',
    'SENT',
    'FAILED'
);