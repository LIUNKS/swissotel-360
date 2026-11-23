-- =====================================================================
-- Swissotel 360 - Billing Microservice Schema (schema: billing)
-- =====================================================================

CREATE TABLE IF NOT EXISTS folios (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id     UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  stay_id       BIGINT NOT NULL UNIQUE,
  status        VARCHAR(20) NOT NULL DEFAULT 'open'
      CHECK (status IN ('open', 'closed')),
  total_amount  NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0)
);

CREATE TABLE IF NOT EXISTS charges (
  id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  public_id           UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  folio_id            BIGINT NOT NULL REFERENCES folios(id),
  service_request_id  BIGINT,
  concept             VARCHAR(120) NOT NULL,
  amount              NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
  charged_at          TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE IF NOT EXISTS payment_methods (
  id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code    VARCHAR(30) NOT NULL UNIQUE,
  name    VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
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

CREATE TABLE IF NOT EXISTS invoices (
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

CREATE TABLE IF NOT EXISTS processed_integration_events (
  event_key     VARCHAR(120) PRIMARY KEY,
  event_type    VARCHAR(80) NOT NULL,
  processed_at  TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_charges_folio   ON charges(folio_id);
CREATE INDEX IF NOT EXISTS idx_charges_request ON charges(service_request_id);

CREATE OR REPLACE FUNCTION fn_block_closed_folio()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = billing, extensions
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

DROP TRIGGER IF EXISTS trg_charges_block_closed_folio ON charges;
CREATE TRIGGER trg_charges_block_closed_folio
  BEFORE INSERT OR UPDATE ON charges
  FOR EACH ROW
  EXECUTE FUNCTION fn_block_closed_folio();

CREATE OR REPLACE FUNCTION fn_sync_folio_total()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = billing, extensions
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

DROP TRIGGER IF EXISTS trg_charges_sync_folio_total ON charges;
CREATE TRIGGER trg_charges_sync_folio_total
  AFTER INSERT OR UPDATE OR DELETE ON charges
  FOR EACH ROW
  EXECUTE FUNCTION fn_sync_folio_total();

INSERT INTO payment_methods (code, name) VALUES
  ('credit_card',   'Tarjeta de Crédito'),
  ('debit_card',    'Tarjeta de Débito'),
  ('cash',          'Efectivo'),
  ('bank_transfer', 'Transferencia Bancaria')
ON CONFLICT (code) DO NOTHING;
