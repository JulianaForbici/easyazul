create table zona_estacionamento(
id_zonaestacionamento bigserial primary key,
nome varchar(250) not null,
tarifa numeric(8,2) not null,
descricao text not null,
tempo_maximo integer not null,
hora_inicio time not null,
hora_fim time not null);