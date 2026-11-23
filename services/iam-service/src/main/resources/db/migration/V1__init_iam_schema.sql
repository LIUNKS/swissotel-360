-- =====================================================================
-- Swissotel 360 - IAM Microservice Schema (schema: iam)
-- =====================================================================

CREATE TABLE IF NOT EXISTS roles (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id     UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  code          VARCHAR(40) NOT NULL UNIQUE,
  name          VARCHAR(80) NOT NULL,
  description   TEXT,
  is_system     BOOLEAN NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE IF NOT EXISTS permissions (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code          VARCHAR(60) NOT NULL UNIQUE,
  module        VARCHAR(30) NOT NULL,
  description   VARCHAR(150) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS users (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  role_id         BIGINT NOT NULL REFERENCES roles(id),
  full_name       VARCHAR(120) NOT NULL,
  email           VARCHAR(150) NOT NULL UNIQUE CHECK (email = lower(email)),
  password_hash   VARCHAR(255) NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'active'
      CHECK (status IN ('active', 'inactive', 'suspended')),
  last_login_at   TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role_id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = iam, extensions
AS $$
BEGIN
  NEW.updated_at = clock_timestamp();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

INSERT INTO roles (code, name, description, is_system) VALUES
  ('ROLE_SUPER_ADMIN',  'Super Administrator', 'Full system access',           TRUE),
  ('ROLE_MANAGER',      'Hotel Manager',       'Managerial access',            TRUE),
  ('ROLE_RECEPTIONIST', 'Receptionist',        'Front desk operations',        TRUE),
  ('ROLE_HOUSEKEEPING', 'Housekeeping',        'Room and service attendance',  TRUE),
  ('ROLE_GUEST',        'Guest',               'Hotel guest access',           TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (code, module, description) VALUES
  ('users.manage',             'security',         'Create, edit and suspend staff users'),
  ('roles.manage',             'security',         'Manage roles and their permissions'),
  ('rooms.view',               'rooms',            'View rooms and availability'),
  ('rooms.manage',             'rooms',            'Create and edit rooms and room types'),
  ('rooms.update_status',      'rooms',            'Update room status after operational authorization'),
  ('rates.manage',             'rooms',            'Create and edit rates'),
  ('guests.view',              'guests',           'View guest records'),
  ('guests.manage',            'guests',           'Create and edit guest records'),
  ('reservations.view',        'reservations',     'View reservations'),
  ('reservations.create',      'reservations',     'Create reservations'),
  ('reservations.cancel',      'reservations',     'Cancel reservations'),
  ('stays.checkin',            'stays',            'Perform check-in'),
  ('stays.checkout',           'stays',            'Perform check-out'),
  ('keys.manage',              'stays',            'Activate and revoke digital keys'),
  ('service_requests.view',    'service_requests', 'View service requests'),
  ('service_requests.create',  'service_requests', 'Create service requests'),
  ('service_requests.handle',  'service_requests', 'Attend and complete service requests'),
  ('billing.view',             'billing',          'View folios and charges'),
  ('billing.charge',           'billing',          'Add charges to a folio'),
  ('payments.process',         'billing',          'Process payments'),
  ('invoices.issue',           'billing',          'Issue receipts and invoices'),
  ('api_clients.manage',       'integrations',     'Manage B2B API clients'),
  ('consents.manage',          'integrations',     'Manage data sharing consents')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_MANAGER' AND p.code <> 'roles.manage'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_RECEPTIONIST' AND p.code IN (
  'rooms.view', 'rooms.update_status', 'guests.view', 'guests.manage',
  'reservations.view', 'reservations.create', 'reservations.cancel',
  'stays.checkin', 'stays.checkout', 'keys.manage',
  'service_requests.view', 'service_requests.create',
  'billing.view', 'billing.charge', 'payments.process', 'invoices.issue'
)
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_HOUSEKEEPING' AND p.code IN (
  'rooms.view', 'rooms.update_status', 'service_requests.view', 'service_requests.handle'
)
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_GUEST' AND p.code IN (
  'reservations.view', 'reservations.create', 'reservations.cancel',
  'service_requests.view', 'service_requests.create',
  'consents.manage'
)
ON CONFLICT DO NOTHING;

INSERT INTO users (role_id, full_name, email, password_hash, status)
SELECT r.id,
       'System Administrator',
       'admin@swissotel.com',
       '$2a$10$btuTrWGbWAkconeK0b0IiuION/uazfoD4XMGP74Qxc0.rjXlnjR/W',
       'active'
FROM roles r
WHERE r.code = 'ROLE_SUPER_ADMIN'
ON CONFLICT (email) DO NOTHING;
