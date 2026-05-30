ALTER TABLE zona_estacionamento
ADD COLUMN latitude DOUBLE PRECISION,
ADD COLUMN longitude DOUBLE PRECISION,
ADD COLUMN capacidade_vagas INTEGER;


UPDATE zona_estacionamento
SET
    latitude = 0,
    longitude = 0,
    capacidade_vagas = 0
WHERE latitude IS NULL
   OR longitude IS NULL
   OR capacidade_vagas IS NULL;


ALTER TABLE zona_estacionamento
ALTER COLUMN latitude SET NOT NULL,
ALTER COLUMN longitude SET NOT NULL,
ALTER COLUMN capacidade_vagas SET NOT NULL;


ALTER TABLE zona_estacionamento
ALTER COLUMN capacidade_vagas SET DEFAULT 0;
