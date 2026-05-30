alter table veiculo add column tipo_veiculo varchar(20);

update veiculo set tipo_veiculo = 'CARRO' where tipo_veiculo is null;

alter table veiculo alter column tipo_veiculo set not null;