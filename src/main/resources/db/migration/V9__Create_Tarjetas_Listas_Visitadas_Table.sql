-- Flyway Migration V9: Registrar por qué listas ha pasado una tarjeta

CREATE TABLE IF NOT EXISTS tarjetas_listas_visitadas (
    tarjeta_id VARCHAR(36) NOT NULL,
    listas_visitadas VARCHAR(36) NOT NULL,
    PRIMARY KEY (tarjeta_id, listas_visitadas),
    FOREIGN KEY (tarjeta_id) REFERENCES tarjetas(id) ON DELETE CASCADE
);

CREATE INDEX idx_tarjetas_listas_visitadas_lista ON tarjetas_listas_visitadas(listas_visitadas);