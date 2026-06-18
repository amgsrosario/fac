alter table tipodocumento
    add column if not exists codigo_fiscal varchar(2);

update tipodocumento
set codigo_fiscal = 'FT'
where id = 'FT'
  and (codigo_fiscal is null or btrim(codigo_fiscal) = '');

update tipodocumento
set codigo_fiscal = 'FR'
where id = 'FRC'
  and (codigo_fiscal is null or btrim(codigo_fiscal) = '');

update tipodocumento
set codigo_fiscal = 'RC'
where id = 'RC'
  and (codigo_fiscal is null or btrim(codigo_fiscal) = '');

alter table documento_comercial
    add column if not exists codigo_validacao_at varchar(100),
    add column if not exists atcud varchar(150),
    add column if not exists qr_payload text,
    add column if not exists qr_payload_version varchar(20);

alter table documento_financeiro
    add column if not exists codigo_validacao_at varchar(100),
    add column if not exists atcud varchar(150),
    add column if not exists qr_payload text,
    add column if not exists qr_payload_version varchar(20);
