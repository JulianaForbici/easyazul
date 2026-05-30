create table usuario(
id_usuario bigserial primary key,
nome varchar(250) not null,
email varchar(150) not null unique,
tipo varchar(50) not null,
senha varchar(150) not null,
cpf varchar(14) unique,
cnpj varchar(18) unique,
telefone varchar(20) not null unique,
data_nascimento date,
razao_social varchar(250),
status varchar(10) not null default 'ATIVO');