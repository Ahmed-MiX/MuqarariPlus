-- ═══════════════════════════════════════════════════════════════════════
-- Muqarari+ Expert Verification Workflow — MySQL Migration Script
-- ═══════════════════════════════════════════════════════════════════════
-- This script updates the `experts` table to support the full 
-- verification lifecycle: NONE → PENDING → APPROVED/REJECTED
-- with file storage and cooldown timer support.
--
-- NOTE: Since spring.jpa.hibernate.ddl-auto=update, Hibernate will
-- auto-create new columns. This script is provided for documentation
-- and manual execution if needed.
-- ═══════════════════════════════════════════════════════════════════════

-- Add verification status column (replaces boolean is_verified)
ALTER TABLE experts 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'NONE';

-- Add CV file path for local storage
ALTER TABLE experts 
ADD COLUMN IF NOT EXISTS cv_file_path VARCHAR(500);

-- Add timestamp for rejection cooldown tracking
ALTER TABLE experts 
ADD COLUMN IF NOT EXISTS last_submission_time DATETIME;

-- Migrate existing data: convert is_verified boolean to status enum
UPDATE experts SET status = 'APPROVED' WHERE is_verified = 1;
UPDATE experts SET status = 'NONE' WHERE is_verified = 0 OR is_verified IS NULL;

-- Drop the old boolean column (optional — keep if you want backward compat)
-- ALTER TABLE experts DROP COLUMN IF EXISTS is_verified;

-- Create index for efficient status-based queries
CREATE INDEX IF NOT EXISTS idx_experts_status ON experts(status);

-- Verify the changes
DESCRIBE experts;
SELECT user_id, status, cv_file_path, cv_url, linkedin_url, last_submission_time FROM experts;
