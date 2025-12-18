-- Insert test advisors
INSERT INTO advisor (name, email, status, module_number, supported_queues, assigned_tickets_count) VALUES
('María González', 'maria.gonzalez@banco.cl', 'AVAILABLE', 1, 'CAJA,PERSONAL_BANKER', 0),
('Juan Pérez', 'juan.perez@banco.cl', 'AVAILABLE', 2, 'CAJA', 0),
('Ana Silva', 'ana.silva@banco.cl', 'AVAILABLE', 3, 'EMPRESAS,GERENCIA', 0),
('Carlos Rojas', 'carlos.rojas@banco.cl', 'OFFLINE', 4, 'PERSONAL_BANKER', 0);
