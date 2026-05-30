alter table pagamento
add column id_ticket bigint,
    add constraint fk_pagamento_ticket
        foreign key (id_ticket) references ticket_estacionamento(id_ticket);