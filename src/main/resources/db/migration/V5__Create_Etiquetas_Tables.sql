-- Flyway Migration V5: Crear tabla de etiquetas de tarjetas

CREATE TABLE IF NOT EXISTS tarjetas_etiquetas_nombres (
    tarjeta_id VARCHAR(36) NOT NULL,
    etiquetas_nombres VARCHAR(255) NOT NULL,
    PRIMARY KEY (tarjeta_id, etiquetas_nombres),
    FOREIGN KEY (tarjeta_id) REFERENCES tarjetas(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tarjetas_etiquetas_colores (
    tarjeta_id VARCHAR(36) NOT NULL,
    etiquetas_colores_key VARCHAR(255) NOT NULL,
    etiquetas_colores_value VARCHAR(50),
    PRIMARY KEY (tarjeta_id, etiquetas_colores_key),
    FOREIGN KEY (tarjeta_id) REFERENCES tarjetas(id) ON DELETE CASCADE
);

CREATE INDEX idx_etiquetas_nombres ON tarjetas_etiquetas_nombres(etiquetas_nombres);
CREATE INDEX idx_etiquetas_colores_key ON tarjetas_etiquetas_colores(etiquetas_colores_key);
