CREATE TABLE reservas (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT NOT NULL,

    veiculo_id BIGINT NOT NULL,

    zona_id BIGINT NOT NULL,

    inicio TIMESTAMP NOT NULL,

    fim TIMESTAMP NOT NULL,

    status VARCHAR(30) NOT NULL,

    codigo_qr VARCHAR(100),

    qr_utilizado BOOLEAN NOT NULL DEFAULT FALSE,

    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reserva_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_reserva_veiculo
        FOREIGN KEY (veiculo_id)
        REFERENCES veiculos(id),

    CONSTRAINT fk_reserva_zona
        FOREIGN KEY (zona_id)
        REFERENCES zonas(id),

    CONSTRAINT ck_reserva_periodo
        CHECK (fim > inicio)
);

CREATE INDEX idx_reserva_veiculo_periodo
ON reservas(veiculo_id, inicio, fim);
