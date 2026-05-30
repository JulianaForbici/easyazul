alter table pagamento add column status varchar(20) not null default 'PENDENTE';

update pagamento set status = 'PENDENTE' where status is null;

alter table pagamento alter column status set not null;