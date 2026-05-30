ALTER TABLE veiculo ALTER COLUMN status SET DEFAULT 'ATIVO';

UPDATE veiculo SET status = 'ATIVO'   WHERE lower(status) = 'ativo';
UPDATE veiculo SET status = 'INATIVO' WHERE lower(status) = 'inativo';


ALTER TABLE veiculo ADD CONSTRAINT chk_status_veiculo CHECK (status IN ('ATIVO','INATIVO'));
