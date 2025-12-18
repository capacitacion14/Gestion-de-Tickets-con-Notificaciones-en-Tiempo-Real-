CREATE TABLE advisor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    module_number INTEGER NOT NULL,
    supported_queues VARCHAR(500),
    assigned_tickets_count INTEGER DEFAULT 0,
    last_assignment_at TIMESTAMP
);

CREATE INDEX idx_advisor_status ON advisor(status);
CREATE INDEX idx_advisor_module_number ON advisor(module_number);

ALTER TABLE ticket 
ADD CONSTRAINT fk_ticket_advisor 
FOREIGN KEY (assigned_advisor_id) REFERENCES advisor(id);
