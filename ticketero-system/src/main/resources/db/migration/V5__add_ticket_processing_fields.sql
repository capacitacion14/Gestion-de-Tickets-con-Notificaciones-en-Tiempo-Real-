-- Add fields for ticket processing
ALTER TABLE ticket 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Update existing tickets to have updated_at = created_at
UPDATE ticket 
SET updated_at = created_at 
WHERE updated_at IS NULL;