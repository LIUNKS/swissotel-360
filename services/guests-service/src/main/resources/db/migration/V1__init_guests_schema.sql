-- =====================================================================
-- Swissotel 360 - Guests Microservice Schema (schema: guests)
-- =====================================================================

CREATE TABLE IF NOT EXISTS document_types (
  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code              VARCHAR(20) NOT NULL UNIQUE,
  name              VARCHAR(60) NOT NULL,
  has_check_digit   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS guests (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  user_id             BIGINT UNIQUE,
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

CREATE INDEX IF NOT EXISTS idx_guests_email ON guests(email);

INSERT INTO document_types (code, name, has_check_digit) VALUES
  ('DNI',      'Documento Nacional de Identidad', TRUE),
  ('PASSPORT', 'Pasaporte',                       FALSE),
  ('CE',       'Carné de Extranjería',            FALSE)
ON CONFLICT (code) DO NOTHING;
