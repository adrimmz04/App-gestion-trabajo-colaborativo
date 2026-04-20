-- Flyway Migration V2: Crear tabla de usuarios compartidos

CREATE TABLE IF NOT EXISTS tableros_usuarios_compartidos (
    tablero_id VARCHAR(36) NOT NULL,
    usuarios_compartidos VARCHAR(255) NOT NULL,
    PRIMARY KEY (tablero_id, usuarios_compartidos),
    FOREIGN KEY (tablero_id) REFERENCES tableros(id) ON DELETE CASCADE
);

CREATE INDEX idx_tableros_usuarios_shared ON tableros_usuarios_compartidos(usuarios_compartidos);
