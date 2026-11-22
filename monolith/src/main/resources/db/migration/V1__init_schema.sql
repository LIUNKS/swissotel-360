-- =====================================================================
-- Swissôtel 360 - Database Schema v2 (PostgreSQL 15+ / Flyway Migration)
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS btree_gist;

-- =====================================================================
-- 1. Security & Staff Access
-- =====================================================================

CREATE TABLE roles (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id     UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  code          VARCHAR(40) NOT NULL UNIQUE,
  name          VARCHAR(80) NOT NULL,
  description   TEXT,
  is_system     BOOLEAN NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE permissions (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code          VARCHAR(60) NOT NULL UNIQUE,
  module        VARCHAR(30) NOT NULL,
  description   VARCHAR(150) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE role_permissions (
  role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
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

-- =====================================================================
-- 2. Guests & Identity Documents
-- =====================================================================

CREATE TABLE document_types (
  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code              VARCHAR(20) NOT NULL UNIQUE,
  name              VARCHAR(60) NOT NULL,
  has_check_digit   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE guests (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  user_id             BIGINT UNIQUE REFERENCES users(id) ON DELETE SET NULL,
  document_type_id    BIGINT NOT NULL REFERENCES document_types(id),
  document_number     VARCHAR(20) NOT NULL,
  first_name          VARCHAR(100) NOT NULL,
  last_name           VARCHAR(100) NOT NULL,
  email               VARCHAR(150),
  phone               VARCHAR(30),
  country_of_origin   VARCHAR(60),
  registered_at       TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),

  CONSTRAINT uq_guests_document UNIQUE (document_type_id, document_number)
);

-- =====================================================================
-- 3. Rooms & Rates
-- =====================================================================

CREATE TABLE room_types (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id     UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  name          VARCHAR(60) NOT NULL UNIQUE,
  capacity      SMALLINT NOT NULL CHECK (capacity > 0),
  description   TEXT
);

CREATE TABLE rooms (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  room_type_id    BIGINT NOT NULL REFERENCES room_types(id),
  room_number     VARCHAR(10) NOT NULL UNIQUE,
  floor           SMALLINT NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'available'
      CHECK (status IN ('available', 'occupied', 'cleaning', 'out_of_service'))
);

CREATE TABLE rates (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  room_type_id    BIGINT NOT NULL REFERENCES room_types(id),
  price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  season          VARCHAR(30),
  valid_from      DATE NOT NULL,
  valid_to        DATE,

  CHECK (valid_to IS NULL OR valid_to >= valid_from),

  -- A room type cannot have two overlapping rates
  CONSTRAINT ex_rates_no_overlap EXCLUDE USING gist (
    room_type_id WITH =,
    daterange(valid_from, valid_to, '[]') WITH &&
  )
);

-- =====================================================================
-- 4. Reservations
-- =====================================================================

CREATE TABLE reservations (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  reservation_code    VARCHAR(20) NOT NULL UNIQUE,
  guest_id            BIGINT NOT NULL REFERENCES guests(id),
  room_type_id        BIGINT NOT NULL REFERENCES room_types(id),
  check_in_date       DATE NOT NULL,
  check_out_date      DATE NOT NULL,
  status              VARCHAR(20) NOT NULL DEFAULT 'pending'
      CHECK (status IN ('pending', 'confirmed', 'cancelled', 'completed')),
  payment_type        VARCHAR(20) NOT NULL CHECK (payment_type IN ('immediate', 'deferred')),
  total_amount        NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
  created_by          BIGINT REFERENCES users(id),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),

  CHECK (check_out_date > check_in_date)
);

-- =====================================================================
-- 5. Stays & Digital Access
-- =====================================================================

CREATE TABLE stays (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  reservation_id      BIGINT NOT NULL UNIQUE REFERENCES reservations(id),
  room_id             BIGINT NOT NULL REFERENCES rooms(id),
  guest_id            BIGINT NOT NULL REFERENCES guests(id),
  checked_in_at       TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  checked_out_at      TIMESTAMPTZ,
  status              VARCHAR(20) NOT NULL DEFAULT 'active'
      CHECK (status IN ('active', 'closed')),
  checked_in_by       BIGINT REFERENCES users(id),

  CHECK (checked_out_at IS NULL OR checked_out_at >= checked_in_at)
);

-- A physical room can only have one active stay at a time
CREATE UNIQUE INDEX uq_stays_active_room ON stays(room_id) WHERE status = 'active';

CREATE TABLE digital_keys (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  stay_id         BIGINT NOT NULL REFERENCES stays(id),
  access_code     VARCHAR(64) NOT NULL,
  activated_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  revoked_at      TIMESTAMPTZ,
  status          VARCHAR(20) NOT NULL DEFAULT 'active'
      CHECK (status IN ('active', 'revoked')),

  CHECK (revoked_at IS NULL OR revoked_at >= activated_at)
);

-- Only one active digital key per stay
CREATE UNIQUE INDEX uq_digital_keys_active_stay ON digital_keys(stay_id) WHERE status = 'active';

-- =====================================================================
-- 6. Service Requests
-- =====================================================================

CREATE TABLE service_types (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code                VARCHAR(30) NOT NULL UNIQUE,
  name                VARCHAR(80) NOT NULL,
  operational_area    VARCHAR(60) NOT NULL
);

CREATE TABLE service_requests (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  stay_id             BIGINT NOT NULL REFERENCES stays(id),
  service_type_id     BIGINT NOT NULL REFERENCES service_types(id),
  description         TEXT,
  status              VARCHAR(20) NOT NULL DEFAULT 'registered'
      CHECK (status IN ('registered', 'in_progress', 'completed')),
  requested_at        TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  completed_at        TIMESTAMPTZ,
  handled_by          BIGINT REFERENCES users(id),

  CHECK (completed_at IS NULL OR completed_at >= requested_at)
);

-- =====================================================================
-- 7. Billing & Payments
-- =====================================================================

CREATE TABLE folios (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id     UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  stay_id       BIGINT NOT NULL UNIQUE REFERENCES stays(id),
  status        VARCHAR(20) NOT NULL DEFAULT 'open'
      CHECK (status IN ('open', 'closed')),
  total_amount  NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0)
);

CREATE TABLE charges (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  folio_id            BIGINT NOT NULL REFERENCES folios(id),
  service_request_id  BIGINT REFERENCES service_requests(id),
  concept             VARCHAR(120) NOT NULL,
  amount              NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  charged_at          TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE payment_methods (
  id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code    VARCHAR(30) NOT NULL UNIQUE,
  name    VARCHAR(60) NOT NULL
);

CREATE TABLE payments (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  folio_id            BIGINT NOT NULL UNIQUE REFERENCES folios(id),
  payment_method_id   BIGINT NOT NULL REFERENCES payment_methods(id),
  amount              NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  status              VARCHAR(20) NOT NULL DEFAULT 'processed'
      CHECK (status IN ('pending', 'processed', 'rejected')),
  gateway_reference   VARCHAR(100),
  paid_at             TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE invoices (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  payment_id      BIGINT NOT NULL UNIQUE REFERENCES payments(id),
  document_type   VARCHAR(10) NOT NULL CHECK (document_type IN ('receipt', 'invoice')),
  series          VARCHAR(10) NOT NULL,
  number          VARCHAR(20) NOT NULL,
  issued_at       TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  total_amount    NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),

  CONSTRAINT uq_invoices_series_number UNIQUE (series, number)
);

-- =====================================================================
-- 8. B2B Integrations & API Keys
-- =====================================================================

CREATE TABLE api_clients (
  id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id             UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  client_name           VARCHAR(150) NOT NULL,
  api_key_hash          VARCHAR(255) NOT NULL UNIQUE,
  rate_limit_per_day    INTEGER NOT NULL DEFAULT 250 CHECK (rate_limit_per_day > 0),
  status                VARCHAR(20) NOT NULL DEFAULT 'active'
      CHECK (status IN ('active', 'suspended', 'revoked')),
  created_at            TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE data_sharing_consents (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  api_client_id   BIGINT NOT NULL REFERENCES api_clients(id),
  guest_id        BIGINT NOT NULL REFERENCES guests(id),
  otp_hash        VARCHAR(255) NOT NULL,
  expires_at      TIMESTAMPTZ NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'pending'
      CHECK (status IN ('pending', 'approved', 'expired', 'denied')),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

-- =====================================================================
-- 9. Indexes on foreign keys and frequent queries
-- =====================================================================

CREATE INDEX idx_users_role                 ON users(role_id);
CREATE INDEX idx_guests_email               ON guests(email);
CREATE INDEX idx_rooms_type                 ON rooms(room_type_id);
CREATE INDEX idx_rooms_status               ON rooms(status);
CREATE INDEX idx_reservations_guest         ON reservations(guest_id);
CREATE INDEX idx_reservations_type_dates    ON reservations(room_type_id, check_in_date, check_out_date);
CREATE INDEX idx_reservations_status        ON reservations(status);
CREATE INDEX idx_stays_guest                ON stays(guest_id);
CREATE INDEX idx_stays_room                 ON stays(room_id);
CREATE INDEX idx_service_requests_stay      ON service_requests(stay_id);
CREATE INDEX idx_service_requests_status    ON service_requests(status);
CREATE INDEX idx_service_requests_handler   ON service_requests(handled_by);
CREATE INDEX idx_charges_folio              ON charges(folio_id);
CREATE INDEX idx_charges_request            ON charges(service_request_id);
CREATE INDEX idx_consents_client            ON data_sharing_consents(api_client_id);
CREATE INDEX idx_consents_guest             ON data_sharing_consents(guest_id);

-- =====================================================================
-- 10. Triggers & Automation
-- =====================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = public
AS $$
BEGIN
  NEW.updated_at = clock_timestamp();
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE FUNCTION fn_block_closed_folio()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = public
AS $$
DECLARE
  v_status VARCHAR(20);
BEGIN
  SELECT status INTO v_status FROM folios WHERE id = NEW.folio_id FOR SHARE;
  IF v_status = 'closed' THEN
    RAISE EXCEPTION 'Folio % is closed; charges cannot be added or modified', NEW.folio_id;
  END IF;
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_charges_block_closed_folio
  BEFORE INSERT OR UPDATE ON charges
  FOR EACH ROW
  EXECUTE FUNCTION fn_block_closed_folio();

CREATE OR REPLACE FUNCTION fn_sync_folio_total()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = public
AS $$
BEGIN
  IF TG_OP IN ('INSERT', 'UPDATE') THEN
    UPDATE folios
       SET total_amount = (SELECT COALESCE(SUM(amount), 0) FROM charges WHERE folio_id = NEW.folio_id)
     WHERE id = NEW.folio_id;
  END IF;

  IF TG_OP = 'DELETE' OR (TG_OP = 'UPDATE' AND OLD.folio_id <> NEW.folio_id) THEN
    UPDATE folios
       SET total_amount = (SELECT COALESCE(SUM(amount), 0) FROM charges WHERE folio_id = OLD.folio_id)
     WHERE id = OLD.folio_id;
  END IF;

  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_charges_sync_folio_total
  AFTER INSERT OR UPDATE OR DELETE ON charges
  FOR EACH ROW
  EXECUTE FUNCTION fn_sync_folio_total();

-- =====================================================================
-- 11. Row Level Security
-- =====================================================================

DO $$
DECLARE
  t TEXT;
  tables TEXT[] := ARRAY[
    'roles', 'permissions', 'role_permissions', 'users',
    'document_types', 'guests',
    'room_types', 'rooms', 'rates',
    'reservations', 'stays', 'digital_keys',
    'service_types', 'service_requests',
    'folios', 'charges', 'payment_methods', 'payments', 'invoices',
    'api_clients', 'data_sharing_consents'
  ];
BEGIN
  FOREACH t IN ARRAY tables LOOP
    EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', t);
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
      EXECUTE format('REVOKE ALL ON public.%I FROM anon', t);
    END IF;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
      EXECUTE format('REVOKE ALL ON public.%I FROM authenticated', t);
    END IF;
  END LOOP;
END $$;

-- =====================================================================
-- 12. Seed Data
-- =====================================================================

INSERT INTO roles (code, name, description, is_system) VALUES
  ('ROLE_SUPER_ADMIN',  'Super Administrator', 'Full system access',        TRUE),
  ('ROLE_MANAGER',      'Hotel Manager',       'Managerial access',         TRUE),
  ('ROLE_RECEPTIONIST', 'Receptionist',        'Front desk operations',     TRUE),
  ('ROLE_HOUSEKEEPING', 'Housekeeping',        'Room and service attendance', TRUE),
  ('ROLE_GUEST',        'Guest',               'Hotel guest access',        TRUE);

INSERT INTO permissions (code, module, description) VALUES
  ('users.manage',             'security',         'Create, edit and suspend staff users'),
  ('roles.manage',             'security',         'Manage roles and their permissions'),
  ('rooms.view',               'rooms',            'View rooms and availability'),
  ('rooms.manage',             'rooms',            'Create and edit rooms and room types'),
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
  ('consents.manage',          'integrations',     'Manage data sharing consents');

-- Super admin: everything
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_SUPER_ADMIN';

-- Manager: everything except role management
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_MANAGER' AND p.code <> 'roles.manage';

-- Receptionist: front desk operations
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_RECEPTIONIST' AND p.code IN (
  'rooms.view', 'guests.view', 'guests.manage',
  'reservations.view', 'reservations.create', 'reservations.cancel',
  'stays.checkin', 'stays.checkout', 'keys.manage',
  'service_requests.view', 'service_requests.create',
  'billing.view', 'billing.charge', 'payments.process', 'invoices.issue'
);

-- Housekeeping: attend service requests
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_HOUSEKEEPING' AND p.code IN (
  'rooms.view', 'service_requests.view', 'service_requests.handle'
);

-- Guest: self-service (ownership is enforced by the backend)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ROLE_GUEST' AND p.code IN (
  'reservations.view', 'reservations.create',
  'service_requests.view', 'service_requests.create',
  'consents.manage'
);

INSERT INTO document_types (code, name, has_check_digit) VALUES
  ('DNI',      'Documento Nacional de Identidad', TRUE),
  ('PASSPORT', 'Pasaporte',                       FALSE),
  ('CE',       'Carné de Extranjería',            FALSE);

INSERT INTO payment_methods (code, name) VALUES
  ('credit_card',   'Tarjeta de Crédito'),
  ('debit_card',    'Tarjeta de Débito'),
  ('cash',          'Efectivo'),
  ('bank_transfer', 'Transferencia Bancaria');

INSERT INTO room_types (name, capacity, description) VALUES
  ('Standard',        2, 'Habitación estándar con cama matrimonial'),
  ('Deluxe',          3, 'Habitación de lujo con vista a la ciudad'),
  ('Executive Suite', 4, 'Suite ejecutiva con sala de estar');

INSERT INTO service_types (code, name, operational_area) VALUES
  ('room_service', 'Room Service',    'F&B'),
  ('spa',          'Spa & Wellness',  'Spa'),
  ('laundry',      'Lavandería',      'Housekeeping'),
  ('housekeeping', 'Limpieza extra',  'Housekeeping');

-- Base rates (PEN per night), open-ended
INSERT INTO rates (room_type_id, price, season, valid_from)
SELECT id,
       CASE name
         WHEN 'Standard'        THEN 250.00
         WHEN 'Deluxe'          THEN 380.00
         WHEN 'Executive Suite' THEN 650.00
       END,
       'base',
       DATE '2026-01-01'
FROM room_types;

-- Sample inventory: 5 floors x 4 rooms (floors 1-2 Standard, 3-4 Deluxe, 5 Executive Suite)
INSERT INTO rooms (room_type_id, room_number, floor)
SELECT rt.id, (f * 100 + n)::TEXT, f
FROM generate_series(1, 5) AS f
CROSS JOIN generate_series(1, 4) AS n
JOIN room_types rt
  ON rt.name = CASE
       WHEN f <= 2 THEN 'Standard'
       WHEN f <= 4 THEN 'Deluxe'
       ELSE 'Executive Suite'
     END;
