-- Flyway Migration V4: Crear tabla de tarjetas

CREATE TABLE IF NOT EXISTS tarjetas (
    id VARCHAR(36) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(50) NOT NULL DEFAULT 'TAREA',
    completada BOOLEAN DEFAULT FALSE,
    lista_id VARCHAR(36) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_completacion TIMESTAMP,
    FOREIGN KEY (lista_id) REFERENCES listas(id) ON DELETE CASCADE,
    CONSTRAINT check_tipo CHECK (tipo IN ('TAREA', 'CHECKLIST'))
);

CREATE INDEX idx_tarjetas_lista ON tarjetas(lista_id);
CREATE INDEX idx_tarjetas_completada ON tarjetas(completada);
CREATE INDEX idx_tarjetas_tipo ON tarjetas(tipo);
