create table pagamento(
id_pagamento bigserial primary key,
id_usuario bigint not null,
tipo_referencia varchar(20) not null,
valor numeric(8,2) not null,
forma_pagamento varchar(30) not null,
data_pagamento timestamp not null default now(),
foreign key (id_usuario) references usuario(id_usuario));