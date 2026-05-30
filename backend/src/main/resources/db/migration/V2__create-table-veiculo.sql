create table veiculo(
id bigserial primary key,
placa varchar(10) not null unique,
id_usuario bigint not null,
foreign key (id_usuario) references usuario(id_usuario));