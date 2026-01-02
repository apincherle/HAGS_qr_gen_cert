-- Add missing columns to existing card_certificate table
-- Run this script on your hags_customer database

-- Add submission_id column
ALTER TABLE card_certificate 
ADD COLUMN IF NOT EXISTS submission_id BIGINT;

-- Add customer_id column (UUID to match customers.customer_id)
ALTER TABLE card_certificate 
ADD COLUMN IF NOT EXISTS customer_id UUID;

-- Add item_id column
ALTER TABLE card_certificate 
ADD COLUMN IF NOT EXISTS item_id BIGINT;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_card_certificate_submission_id ON card_certificate(submission_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_customer_id ON card_certificate(customer_id);
CREATE INDEX IF NOT EXISTS idx_card_certificate_item_id ON card_certificate(item_id);

-- If you have existing records in card_certificate, you may need to update them
-- with appropriate values. Uncomment and modify the following if needed:

-- UPDATE card_certificate 
-- SET submission_id = (SELECT id FROM submissions LIMIT 1)
-- WHERE submission_id IS NULL;

-- UPDATE card_certificate 
-- SET customer_id = (SELECT customer_id FROM customers LIMIT 1)
-- WHERE customer_id IS NULL;

-- UPDATE card_certificate 
-- SET item_id = (SELECT id FROM submission_items LIMIT 1)
-- WHERE item_id IS NULL;

-- After updating existing records, you can make the columns NOT NULL:
-- ALTER TABLE card_certificate ALTER COLUMN submission_id SET NOT NULL;
-- ALTER TABLE card_certificate ALTER COLUMN customer_id SET NOT NULL;
-- ALTER TABLE card_certificate ALTER COLUMN item_id SET NOT NULL;

