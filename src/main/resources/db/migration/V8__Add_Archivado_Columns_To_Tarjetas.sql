-- Flyway Migration V8: Añadir archivado de tarjetas

ALTER TABLE tarjetas
    ADD COLUMN IF NOT EXISTS archivada BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tarjetas
    ADD COLUMN IF NOT EXISTS fecha_archivado TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_tarjetas_archivada ON tarjetas(archivada);