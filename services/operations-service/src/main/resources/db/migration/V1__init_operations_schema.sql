-- =====================================================================
-- Swissotel 360 - Operations Microservice Schema (schema: operations)
-- =====================================================================

CREATE TABLE IF NOT EXISTS stays (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  reservation_id      BIGINT NOT NULL UNIQUE,
  room_id             BIGINT NOT NULL,
  guest_id            BIGINT NOT NULL,
  checked_in_at       TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  checked_out_at      TIMESTAMPTZ,
  status              VARCHAR(20) NOT NULL DEFAULT 'active'
      CHECK (status IN ('active', 'closed')),
  checked_in_by       BIGINT,

  CHECK (checked_out_at IS NULL OR checked_out_at >= checked_in_at)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_stays_active_room ON stays(room_id) WHERE status = 'active';
CREATE INDEX IF NOT EXISTS idx_stays_guest ON stays(guest_id);
CREATE INDEX IF NOT EXISTS idx_stays_room  ON stays(room_id);

CREATE TABLE IF NOT EXISTS digital_keys (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  stay_id         BIGINT NOT NULL REFERENCES stays(id),
  access_code     VARCHAR(64) NOT NULL,
  activated_at    TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  revoked_at      TIMESTAMPTZ,
  status          VARCHAR(20) NOT NULL DEFAULT 'active'
      CHECK (status IN ('active', 'revoked')),

  CHECK (revoked_at IS NULL OR revoked_at >= activated_at)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_digital_keys_active_stay ON digital_keys(stay_id) WHERE status = 'active';

CREATE TABLE IF NOT EXISTS service_types (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code                VARCHAR(30) NOT NULL UNIQUE,
  name                VARCHAR(80) NOT NULL,
  operational_area    VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS service_requests (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  stay_id             BIGINT NOT NULL REFERENCES stays(id),
  service_type_id     BIGINT NOT NULL REFERENCES service_types(id),
  description         TEXT,
  status              VARCHAR(20) NOT NULL DEFAULT 'registered'
      CHECK (status IN ('registered', 'in_progress', 'completed')),
  requested_at        TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  completed_at        TIMESTAMPTZ,
  handled_by          BIGINT,

  CHECK (completed_at IS NULL OR completed_at >= requested_at)
);

CREATE INDEX IF NOT EXISTS idx_service_requests_stay    ON service_requests(stay_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_status  ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_service_requests_handler ON service_requests(handled_by);

INSERT INTO service_types (code, name, operational_area) VALUES
  ('room_service', 'Room Service',   'F&B'),
  ('spa',          'Spa & Wellness', 'Spa'),
  ('laundry',      'Lavandería',     'Housekeeping'),
  ('housekeeping', 'Limpieza extra', 'Housekeeping')
ON CONFLICT (code) DO NOTHING;
