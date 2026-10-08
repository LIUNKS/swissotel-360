-- =====================================================================
-- Swissôtel 360 - Seed Initial Super Administrator (Flyway Migration V2)
-- Default Credentials:
--   Email: admin@swissotel.com
--   Password: Admin123!
-- =====================================================================

INSERT INTO users (role_id, full_name, email, password_hash, status)
SELECT r.id,
       'System Administrator',
       'admin@swissotel.com',
       '$2a$10$btuTrWGbWAkconeK0b0IiuION/uazfoD4XMGP74Qxc0.rjXlnjR/W',
       'active'
FROM roles r
WHERE r.code = 'ROLE_SUPER_ADMIN'
ON CONFLICT (email) DO NOTHING;
