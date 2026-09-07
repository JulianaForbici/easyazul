CREATE TABLE zonas_favoritas (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT NOT NULL,

    zona_id BIGINT NOT NULL,

    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_zona_favorita
        UNIQUE (usuario_id, zona_id),

    CONSTRAINT fk_favorita_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorita_zona
        FOREIGN KEY (zona_id)
        REFERENCES zonas(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_favorita_usuario
ON zonas_favoritas(usuario_id);
