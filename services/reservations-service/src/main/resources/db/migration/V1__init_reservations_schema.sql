-- =====================================================================
-- Swissotel 360 - Reservations Microservice Schema (schema: reservations)
-- =====================================================================

CREATE TABLE IF NOT EXISTS reservations (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  reservation_code    VARCHAR(20) NOT NULL UNIQUE,
  guest_id            BIGINT NOT NULL,
  room_type_id        BIGINT NOT NULL,
  check_in_date       DATE NOT NULL,
  check_out_date      DATE NOT NULL,
  status              VARCHAR(20) NOT NULL DEFAULT 'pending'
      CHECK (status IN ('pending', 'confirmed', 'cancelled', 'completed')),
  payment_type        VARCHAR(20) NOT NULL CHECK (payment_type IN ('immediate', 'deferred')),
  total_amount        NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
  created_by          BIGINT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
  cancelled_at        TIMESTAMPTZ,
  cancellation_reason VARCHAR(20),
  cancellation_note   VARCHAR(200),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),

  CHECK (check_out_date > check_in_date),
  CONSTRAINT ck_reservations_cancellation_reason
      CHECK (cancellation_reason IS NULL OR cancellation_reason IN ('guest_request', 'staff', 'payment_timeout')),
  CONSTRAINT ck_reservations_cancellation
      CHECK ((status = 'cancelled') = (cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_reservations_guest           ON reservations(guest_id);
CREATE INDEX IF NOT EXISTS idx_reservations_type_dates      ON reservations(room_type_id, check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_reservations_status          ON reservations(status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_reservations_active_duplicate
    ON reservations (guest_id, room_type_id, check_in_date, check_out_date)
    WHERE status IN ('pending', 'confirmed');
CREATE INDEX IF NOT EXISTS idx_reservations_pending_created
    ON reservations (created_at) WHERE status = 'pending';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = reservations, extensions
AS $$
BEGIN
  NEW.updated_at = clock_timestamp();
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_reservations_updated_at ON reservations;
CREATE TRIGGER trg_reservations_updated_at
  BEFORE UPDATE ON reservations
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();
