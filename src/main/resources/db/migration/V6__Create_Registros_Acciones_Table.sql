-- Flyway Migration V6: Crear tabla de registros de acciones (audit trail)

CREATE TABLE IF NOT EXISTS registros_acciones (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(100) NOT NULL,
    detalles TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tablero_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (tablero_id) REFERENCES tableros(id) ON DELETE CASCADE
);

CREATE INDEX idx_registros_tablero ON registros_acciones(tablero_id);
CREATE INDEX idx_registros_tipo ON registros_acciones(tipo);
CREATE INDEX idx_registros_fecha ON registros_acciones(fecha);
