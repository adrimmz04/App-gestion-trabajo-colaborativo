-- Flyway Migration V1: Crear tabla de tableros

CREATE TABLE IF NOT EXISTS tableros (
    id VARCHAR(36) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    propietario_email VARCHAR(255) NOT NULL,
    bloqueado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_desbloqueo TIMESTAMP,
    CONSTRAINT check_email CHECK (propietario_email ~ '^[A-Za-z0-9+_.-]+@(.+)$')
);

-- Crear índices para optimizar búsquedas
CREATE INDEX idx_tableros_propietario ON tableros(propietario_email);
CREATE INDEX idx_tableros_bloqueado ON tableros(bloqueado);
CREATE INDEX idx_tableros_fecha_creacion ON tableros(fecha_creacion);
