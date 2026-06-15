alter table documento_comercial
    add column if not exists codigo_validacao_at varchar(100),
    add column if not exists atcud varchar(150);

alter table documento_financeiro
    add column if not exists codigo_validacao_at varchar(100),
    add column if not exists atcud varchar(150);
