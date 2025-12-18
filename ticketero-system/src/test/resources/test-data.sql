-- Insertar datos de test adicionales para testing
-- Las tablas ya existen por Flyway migrations

-- Limpiar datos existentes
DELETE FROM outbox_message;
DELETE FROM ticket;
DELETE FROM advisor;

-- Insertar asesores de prueba
INSERT INTO advisor (name, email, status, module_number, supported_queues, assigned_tickets_count) VALUES 
('Ana García Test', 'ana.garcia.test@banco.cl', 'AVAILABLE', 1, ARRAY['CAJA', 'PERSONAL_BANKER'], 0),
('Carlos López Test', 'carlos.lopez.test@banco.cl', 'AVAILABLE', 2, ARRAY['PERSONAL_BANKER', 'EMPRESAS'], 0),
('María Rodríguez Test', 'maria.rodriguez.test@banco.cl', 'AVAILABLE', 3, ARRAY['EMPRESAS', 'GERENCIA'], 0),
('Juan Pérez Test', 'juan.perez.test@banco.cl', 'AVAILABLE', 4, ARRAY['CAJA'], 0);