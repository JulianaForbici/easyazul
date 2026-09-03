ALTER TABLE ticket_estacionamento
ADD COLUMN vence_em TIMESTAMP;

UPDATE ticket_estacionamento ticket
SET vence_em = ticket.inicio_ticket + (zona.tempo_maximo * INTERVAL '1 minute')
FROM zona_estacionamento zona
WHERE ticket.id_zonaestacionamento = zona.id_zonaestacionamento
  AND ticket.status = 'ATIVO'
  AND ticket.inicio_ticket IS NOT NULL
  AND ticket.vence_em IS NULL;
