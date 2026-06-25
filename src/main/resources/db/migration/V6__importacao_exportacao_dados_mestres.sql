create table importacao_dados_mestres (
    id uuid primary key,
    tipo varchar(20) not null,
    estado varchar(20) not null,
    nome_ficheiro varchar(255) not null,
    formato varchar(10) not null,
    payload_json text not null,
    total_linhas integer not null,
    linhas_validas integer not null,
    linhas_com_erro integer not null,
    linhas_com_aviso integer not null,
    registos_novos integer not null,
    duplicados integer not null,
    linhas_ignoradas integer not null,
    criado_em timestamptz not null default current_timestamp,
    expira_em timestamptz not null,
    confirmado_em timestamptz,
    criado_por varchar(20),
    confirmado_por varchar(20),
    versao bigint not null default 0
);

create index ix_importacao_dados_mestres_tipo_estado on importacao_dados_mestres (tipo, estado);
create index ix_importacao_dados_mestres_expira_em on importacao_dados_mestres (expira_em);

comment on table importacao_dados_mestres is 'Sessões temporárias e auditáveis de importação de dados mestres.';
