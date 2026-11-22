-- =====================================================================
-- Swissôtel 360 - Harden Supabase Security (Flyway Migration V6)
-- Move btree_gist extension out of public into extensions schema.
-- Note: RLS + REVOKE on public.flyway_schema_history cannot run inside
-- a Flyway migration because Flyway locks its own history table during
-- migrate(); apply that DDL outside Flyway's migration transaction.
-- =====================================================================

CREATE SCHEMA IF NOT EXISTS extensions;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM pg_extension e
    JOIN pg_namespace n ON n.oid = e.extnamespace
    WHERE e.extname = 'btree_gist'
      AND n.nspname = 'public'
  ) THEN
    ALTER EXTENSION btree_gist SET SCHEMA extensions;
  END IF;
END $$;
