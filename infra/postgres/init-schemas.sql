-- Schema-per-service initialization for local PostgreSQL 17
-- Executed automatically on first container startup via /docker-entrypoint-initdb.d

CREATE SCHEMA IF NOT EXISTS extensions;
CREATE SCHEMA IF NOT EXISTS iam;
CREATE SCHEMA IF NOT EXISTS guests;
CREATE SCHEMA IF NOT EXISTS rooms;
CREATE SCHEMA IF NOT EXISTS reservations;
CREATE SCHEMA IF NOT EXISTS operations;
CREATE SCHEMA IF NOT EXISTS billing;

CREATE EXTENSION IF NOT EXISTS pgcrypto SCHEMA extensions;
CREATE EXTENSION IF NOT EXISTS btree_gist SCHEMA extensions;

ALTER DATABASE swissotel360 SET search_path TO public, extensions;
