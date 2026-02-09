-- Migration to add email verification and temporary password tracking
-- Execute this SQL on your PostgreSQL database

-- Add email verification token columns
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(500),
ADD COLUMN IF NOT EXISTS email_verification_token_expiry TIMESTAMP,
ADD COLUMN IF NOT EXISTS is_temporary_password BOOLEAN DEFAULT FALSE NOT NULL;

-- Make password nullable (for staff accounts before verification)
ALTER TABLE users 
ALTER COLUMN password DROP NOT NULL;

-- Add indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_email_verification_token 
ON users(email_verification_token);

CREATE INDEX IF NOT EXISTS idx_is_temporary_password 
ON users(is_temporary_password) 
WHERE is_temporary_password = TRUE;

-- Add comments for documentation
COMMENT ON COLUMN users.email_verification_token IS 'Token for email verification';
COMMENT ON COLUMN users.email_verification_token_expiry IS 'Expiry time for verification token (24 hours)';
COMMENT ON COLUMN users.is_temporary_password IS 'Flag indicating if user is using a temporary password and should change it';

-- Optional: View to check users with temporary passwords
CREATE OR REPLACE VIEW users_with_temporary_passwords AS
SELECT 
    user_id,
    username,
    email,
    full_name,
    role,
    email_verified,
    is_temporary_password,
    created_at,
    last_login
FROM users
WHERE is_temporary_password = TRUE
ORDER BY created_at DESC;

COMMENT ON VIEW users_with_temporary_passwords IS 'Users currently using temporary passwords who should change them';

-- Optional: View for pending email verification
CREATE OR REPLACE VIEW pending_email_verification AS
SELECT 
    user_id,
    username,
    email,
    full_name,
    role,
    email_verified,
    created_at,
    email_verification_token_expiry,
    CASE 
        WHEN email_verification_token_expiry < NOW() THEN 'Expired'
        ELSE 'Active'
    END as token_status
FROM users
WHERE email_verified = FALSE
  AND email_verification_token IS NOT NULL
ORDER BY created_at DESC;

COMMENT ON VIEW pending_email_verification IS 'Users pending email verification';