ALTER TABLE zona_estacionamento
ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'ATIVA';

ALTER TABLE zona_estacionamento
ADD CONSTRAINT chk_status_zona
CHECK (status IN ('ATIVA','INATIVA'));