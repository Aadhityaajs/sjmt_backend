-- Migration to add email verification and password set tracking fields
-- Execute this SQL on your PostgreSQL database

-- Add email verification token columns
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(500),
ADD COLUMN IF NOT EXISTS email_verification_token_expiry TIMESTAMP,
ADD COLUMN IF NOT EXISTS password_set_required BOOLEAN DEFAULT FALSE NOT NULL;

-- Make password nullable (for staff accounts created by admin)
ALTER TABLE users 
ALTER COLUMN password DROP NOT NULL;

-- Add index for faster token lookups
CREATE INDEX IF NOT EXISTS idx_email_verification_token 
ON users(email_verification_token);

-- Add comments for documentation
COMMENT ON COLUMN users.email_verification_token IS 'Token for email verification and password setup';
COMMENT ON COLUMN users.email_verification_token_expiry IS 'Expiry time for verification token (24 hours)';
COMMENT ON COLUMN users.password_set_required IS 'Flag indicating if user needs to set password after email verification';

-- Optional: View to check users pending password setup
CREATE OR REPLACE VIEW pending_password_setup AS
SELECT 
    user_id,
    username,
    email,
    full_name,
    role,
    email_verified,
    password_set_required,
    created_at,
    email_verification_token_expiry
FROM users
WHERE password_set_required = TRUE
  AND email_verified = TRUE
  AND password IS NULL
ORDER BY created_at DESC;

COMMENT ON VIEW pending_password_setup IS 'Users who have verified email but not set password yet';