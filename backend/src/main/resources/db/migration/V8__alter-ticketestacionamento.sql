alter table ticket_estacionamento add column status varchar(20);

alter table ticket_estacionamento alter column status set not null;