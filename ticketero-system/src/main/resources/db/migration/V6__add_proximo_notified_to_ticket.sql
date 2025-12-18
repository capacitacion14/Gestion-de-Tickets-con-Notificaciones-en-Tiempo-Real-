-- Add proximo_notified field to ticket table
ALTER TABLE ticket ADD COLUMN proximo_notified BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for better performance on notification queries
CREATE INDEX idx_ticket_proximo_notified ON ticket(proximo_notified);