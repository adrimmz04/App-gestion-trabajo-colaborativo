-- Flyway Migration V3: Crear tabla de listas

CREATE TABLE IF NOT EXISTS listas (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    limite_maximo INTEGER,
    tablero_id VARCHAR(36) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tablero_id) REFERENCES tableros(id) ON DELETE CASCADE
);

CREATE INDEX idx_listas_tablero ON listas(tablero_id);
CREATE INDEX idx_listas_nombre ON listas(nombre);
