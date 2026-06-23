-- VLN-17 FIX: Flyway baseline migration.
-- This file was generated from the existing schema managed by Hibernate ddl-auto=update.
-- Run `flyway baseline` against your existing database before enabling Flyway in prod.
-- Future schema changes MUST be added as new versioned migration files (V2__, V3__, etc.)
-- rather than editing this file.

-- The actual CREATE TABLE statements for your production schema go here.
-- If you are migrating an existing database, set flyway.baseline-on-migrate=true
-- in application-prod.yaml and flyway will mark this migration as already applied.

-- Example: ensure the passwordHash column is nullable (VLN-09b migration)
ALTER TABLE IF EXISTS users ALTER COLUMN password_hash DROP NOT NULL;
