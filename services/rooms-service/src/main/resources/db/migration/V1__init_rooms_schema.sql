-- =====================================================================
-- Swissotel 360 - Rooms Microservice Schema (schema: rooms)
-- =====================================================================

CREATE TABLE IF NOT EXISTS room_types (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id     UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  name          VARCHAR(60) NOT NULL UNIQUE,
  capacity      SMALLINT NOT NULL CHECK (capacity > 0),
  description   TEXT
);

CREATE TABLE IF NOT EXISTS rooms (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  room_type_id    BIGINT NOT NULL REFERENCES room_types(id),
  room_number     VARCHAR(10) NOT NULL UNIQUE,
  floor           SMALLINT NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'available'
      CHECK (status IN ('available', 'occupied', 'cleaning', 'out_of_service'))
);

CREATE TABLE IF NOT EXISTS rates (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id       UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  room_type_id    BIGINT NOT NULL REFERENCES room_types(id),
  price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
  season          VARCHAR(30),
  valid_from      DATE NOT NULL,
  valid_to        DATE,

  CHECK (valid_to IS NULL OR valid_to >= valid_from),

  CONSTRAINT ex_rates_no_overlap EXCLUDE USING gist (
    room_type_id WITH =,
    daterange(valid_from, valid_to, '[]') WITH &&
  )
);

CREATE INDEX IF NOT EXISTS idx_rooms_type   ON rooms(room_type_id);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);

INSERT INTO room_types (name, capacity, description) VALUES
  ('Standard',        2, 'Habitación estándar con cama matrimonial'),
  ('Deluxe',          3, 'Habitación de lujo con vista a la ciudad'),
  ('Executive Suite', 4, 'Suite ejecutiva con sala de estar')
ON CONFLICT (name) DO NOTHING;

INSERT INTO rates (room_type_id, price, season, valid_from)
SELECT rt.id,
       CASE rt.name
         WHEN 'Standard'        THEN 250.00
         WHEN 'Deluxe'          THEN 380.00
         WHEN 'Executive Suite' THEN 650.00
       END,
       'base',
       DATE '2026-01-01'
FROM room_types rt
WHERE NOT EXISTS (SELECT 1 FROM rates r WHERE r.room_type_id = rt.id);

INSERT INTO rooms (room_type_id, room_number, floor)
SELECT rt.id, (f * 100 + n)::TEXT, f
FROM generate_series(1, 5) AS f
CROSS JOIN generate_series(1, 4) AS n
JOIN room_types rt
  ON rt.name = CASE
       WHEN f <= 2 THEN 'Standard'
       WHEN f <= 4 THEN 'Deluxe'
       ELSE 'Executive Suite'
     END
ON CONFLICT (room_number) DO NOTHING;
