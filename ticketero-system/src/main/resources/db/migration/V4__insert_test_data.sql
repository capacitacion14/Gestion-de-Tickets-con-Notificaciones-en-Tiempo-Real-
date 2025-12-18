-- Insert test advisors
INSERT INTO advisor (name, email, status, module_number, supported_queues, assigned_tickets_count) VALUES
('María González', 'maria.gonzalez@banco.cl', 'AVAILABLE', 1, ARRAY['CAJA', 'PERSONAL_BANKER'], 0),
('Juan Pérez', 'juan.perez@banco.cl', 'AVAILABLE', 2, ARRAY['CAJA'], 0),
('Ana Silva', 'ana.silva@banco.cl', 'AVAILABLE', 3, ARRAY['EMPRESAS', 'GERENCIA'], 0),
('Carlos Rojas', 'carlos.rojas@banco.cl', 'OFFLINE', 4, ARRAY['PERSONAL_BANKER'], 0);
