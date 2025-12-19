-- Insert test advisors (ejecutivos listos para tomar tickets)
INSERT INTO advisor (name, email, status, module_number, supported_queues, assigned_tickets_count) VALUES
-- Ejecutivos de Caja (disponibles)
('María González', 'maria.gonzalez@banco.cl', 'AVAILABLE', 1, 'CAJA,PERSONAL_BANKER', 0),
('Juan Pérez', 'juan.perez@banco.cl', 'AVAILABLE', 2, 'CAJA', 0),
('Carmen López', 'carmen.lopez@banco.cl', 'AVAILABLE', 3, 'CAJA', 0),

-- Ejecutivos Personal Banker (disponibles)
('Ana Silva', 'ana.silva@banco.cl', 'AVAILABLE', 4, 'PERSONAL_BANKER,EMPRESAS', 0),
('Roberto Díaz', 'roberto.diaz@banco.cl', 'AVAILABLE', 5, 'PERSONAL_BANKER', 0),

-- Ejecutivos Empresas (disponibles)
('Patricia Morales', 'patricia.morales@banco.cl', 'AVAILABLE', 6, 'EMPRESAS,GERENCIA', 0),
('Luis Herrera', 'luis.herrera@banco.cl', 'AVAILABLE', 7, 'EMPRESAS', 0),

-- Ejecutivos Gerencia (disponibles)
('Sandra Vega', 'sandra.vega@banco.cl', 'AVAILABLE', 8, 'GERENCIA', 0),

-- Algunos offline para testing
('Carlos Rojas', 'carlos.rojas@banco.cl', 'OFFLINE', 9, 'CAJA', 0),
('Elena Torres', 'elena.torres@banco.cl', 'BREAK', 10, 'PERSONAL_BANKER', 0);
