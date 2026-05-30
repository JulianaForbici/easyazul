create table ticket_estacionamento(
id_ticket bigserial primary key,
id_veiculo bigint not null,
id_zonaestacionamento bigint not null,
inicio_ticket timestamp not null,
fim_ticket timestamp,
valor numeric(8,2) not null,
ativo boolean not null,
foreign key (id_veiculo) references veiculo(id),
foreign key (id_zonaestacionamento) references zona_estacionamento(id_zonaestacionamento));