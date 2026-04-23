-- Flyway Migration V7: Crear tabla de prerequisitos de listas

CREATE TABLE IF NOT EXISTS lista_prerequisitos (
    lista_id VARCHAR(36) NOT NULL,
    lista_prerequisito_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (lista_id, lista_prerequisito_id),
    FOREIGN KEY (lista_id) REFERENCES listas(id) ON DELETE CASCADE
);

CREATE INDEX idx_lista_prerequisitos_lista ON lista_prerequisitos(lista_id);
CREATE INDEX idx_lista_prerequisitos_previa ON lista_prerequisitos(lista_prerequisito_id);