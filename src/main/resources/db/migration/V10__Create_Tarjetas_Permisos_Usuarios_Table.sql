-- Flyway Migration V10: Permisos explícitos por usuario sobre tarjetas

CREATE TABLE IF NOT EXISTS tarjetas_permisos_usuarios (
    tarjeta_id VARCHAR(36) NOT NULL,
    email_usuario VARCHAR(255) NOT NULL,
    permiso VARCHAR(20) NOT NULL,
    PRIMARY KEY (tarjeta_id, email_usuario),
    FOREIGN KEY (tarjeta_id) REFERENCES tarjetas(id) ON DELETE CASCADE
);

CREATE INDEX idx_tarjetas_permisos_usuarios_email ON tarjetas_permisos_usuarios(email_usuario);