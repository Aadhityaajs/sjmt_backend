-- ================================================================
-- SJMT Backend - Initial Admin User Setup
-- ================================================================
-- This script creates the initial ADMIN user with UPDATE privileges
-- 
-- Default Credentials:
-- Username: admin
-- Password: Admin@123
-- Email: admin@sjmt.com
-- 
-- IMPORTANT: Change the password immediately after first login!
-- ================================================================

-- Insert initial admin user
INSERT INTO users (
    username, 
    email, 
    password, 
    full_name, 
    phone_number, 
    email_verified, 
    role, 
    status, 
    privileges, 
    created_at
) 
VALUES (
    'admin', 
    'admin@sjmt.com', 
    '$2a$10$X5wFLXKzHjYVdDqcYQqL5OZ8QGQsEiJZuMQKzWzqHLKFZQzJQXQGe', -- BCrypt hash for "Admin@123"
    'System Administrator', 
    '9876543210', 
    true, 
    'ADMIN', 
    'ACTIVE', 
    'UPDATE', 
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

-- Verify insertion
SELECT 
    user_id,
    username,
    email,
    full_name,
    role,
    privileges,
    status,
    email_verified,
    created_at
FROM users 
WHERE username = 'admin';

-- ================================================================
-- Password Hashes for Different Test Passwords (for reference)
-- ================================================================
-- Admin@123    -> $2a$10$X5wFLXKzHjYVdDqcYQqL5OZ8QGQsEiJZuMQKzWzqHLKFZQzJQXQGe
-- Password@123 -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- Test@1234    -> $2a$10$slYQmyNdGzTn7ZfYnT6YPe/mM5L9Ur7Y8yzNqJpKPEmXkYYkPvgMO
-- ================================================================

-- ================================================================
-- How to Generate New BCrypt Password Hash:
-- ================================================================
-- Option 1: Use Online BCrypt Generator
-- Visit: https://bcrypt-generator.com/
-- Enter your password and copy the hash
--
-- Option 2: Use Java Code
-- PasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hash = encoder.encode("YourPassword");
-- System.out.println(hash);
--
-- Option 3: Use Python
-- pip install bcrypt
-- import bcrypt
-- password = b"YourPassword"
-- hash = bcrypt.hashpw(password, bcrypt.gensalt())
-- print(hash.decode())
-- ================================================================