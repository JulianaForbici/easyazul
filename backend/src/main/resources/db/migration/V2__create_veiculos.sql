CREATE TABLE veiculos (
    id BIGSERIAL PRIMARY KEY,

    placa VARCHAR(20) NOT NULL,

    modelo VARCHAR(80) NOT NULL,

    marca VARCHAR(80) NOT NULL,

    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_veiculo_placa
        UNIQUE (placa)
);

