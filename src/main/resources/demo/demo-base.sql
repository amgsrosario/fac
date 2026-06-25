-- Dados minimos de demonstracao do FAC.
--
-- Objetivo:
-- preparar a base para simular um ciclo real por API:
-- Cliente -> Artigo -> Documento Comercial -> Linha -> Emissao -> Pendente.
--
-- Este ficheiro e intencionalmente pequeno.
-- Nao cria documentos diretamente, porque isso deve passar pelos Services
-- para validar regras de negocio, numeracao, totais e pendentes.

-- Catalogos base
insert into pais (id, nome)
values ('PT', 'Portugal')
on conflict (id) do update set nome = excluded.nome;

insert into codpostal (id, nome)
values ('3750-001', 'Agueda')
on conflict (id) do update set nome = excluded.nome;

insert into moeda (id, nome, vcompra, vvenda, simbolo, ndecimais, ciso)
values ('EUR', 'Euro', 1, 1, 'EUR', 2, '978')
on conflict (id) do update set
    nome = excluded.nome,
    vcompra = excluded.vcompra,
    vvenda = excluded.vvenda,
    simbolo = excluded.simbolo,
    ndecimais = excluded.ndecimais,
    ciso = excluded.ciso;

insert into tipotaxaiva (id, descricao, inativo)
values
    ('ISENTA', 'Isenta', false),
    ('REDUZIDA', 'Reduzida', false),
    ('INTERMEDIA', 'Intermedia', false),
    ('NORMAL', 'Normal', false)
on conflict (id) do update set
    descricao = excluded.descricao,
    inativo = excluded.inativo;

insert into riva (id, nome)
values ('CON', 'Continente')
on conflict (id) do update set nome = excluded.nome;

insert into riva_taxa (id_riva, id_tipo_taxa_iva, valor)
values
    ('CON', 'ISENTA', 0.00),
    ('CON', 'REDUZIDA', 6.00),
    ('CON', 'INTERMEDIA', 13.00),
    ('CON', 'NORMAL', 23.00)
on conflict (id_riva, id_tipo_taxa_iva) do update set valor = excluded.valor;

insert into ppagamento (id, nome, dias)
values ('P30', '30 dias', 30)
on conflict (id) do update set
    nome = excluded.nome,
    dias = excluded.dias;

insert into mpagamento (id, nome)
values (1001, 'Transferencia bancaria')
on conflict (id) do update set nome = excluded.nome;

insert into transporte (id, nome)
values (1001, 'Sem transporte')
on conflict (id) do update set nome = excluded.nome;

-- Empresa proprietaria
insert into empresa (
    id,
    nome,
    nif,
    morada,
    morada1,
    id_codpostal,
    localidade,
    id_pais,
    id_freguesia,
    capital_social,
    matricula_registo_comercial,
    cae,
    descricao_cae,
    email,
    web,
    nome_comercial,
    telefone,
    iban,
    bic_swift,
    texto_rodape,
    observacoes_legais,
    observacoes_comerciais_default,
    logo,
    logo_media_type,
    logo_nome_interno
)
values (
    1,
    'Alentejo Sabores, Lda.',
    '599000007',
    'Avenida das Oliveiras, 14',
    null,
    '3750-001',
    'Evora Demo',
    'PT',
    null,
    0.00,
    'Conservatoria do Registo Comercial de Agueda',
    '62010',
    'Comercio de produtos alimentares e bebidas',
    'geral@alentejo-sabores.demo',
    'https://alentejo-sabores.demo',
    'Alentejo Sabores',
    '+351 266 000 001',
    'PT50000201231234567890154',
    'CGDIPTPL',
    'Documento emitido por sistema de faturacao FAC Demo Partner Edition.',
    'Empresa ficticia para demonstracao comercial.',
    'Obrigado pela preferencia.',
    decode('FFD8FFE000104A46494600010101006000600000FFDB0043000302020302020303030304030304050805050404050A070706080C0A0C0C0B0A0B0B0D0E12100D0E110E0B0B1016101113141515150C0F171816141812141514FFDB00430103040405040509050509140D0B0D1414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414FFC00011080010002003012200021101031101FFC4001F0000010501010101010100000000000000000102030405060708090A0BFFC400B5100002010303020403050504040000017D01020300041105122131410613516107227114328191A1082342B1C11552D1F02433627282090A161718191A25262728292A3435363738393A434445464748494A535455565758595A636465666768696A737475767778797A838485868788898A92939495969798999AA2A3A4A5A6A7A8A9AAB2B3B4B5B6B7B8B9BAC2C3C4C5C6C7C8C9CAD2D3D4D5D6D7D8D9DAE1E2E3E4E5E6E7E8E9EAF1F2F3F4F5F6F7F8F9FAFFC4001F0100030101010101010101010000000000000102030405060708090A0BFFC400B51100020102040403040705040400010277000102031104052131061241510761711322328108144291A1B1C109233352F0156272D10A162434E125F11718191A262728292A35363738393A434445464748494A535455565758595A636465666768696A737475767778797A82838485868788898A92939495969798999AA2A3A4A5A6A7A8A9AAB2B3B4B5B6B7B8B9BAC2C3C4C5C6C7C8C9CAD2D3D4D5D6D7D8D9DAE2E3E4E5E6E7E8E9EAF2F3F4F5F6F7F8F9FAFFDA000C03010002110311003F00D7FD933F64CF087C78F873A8EBFAFEA3ADD9DE5B6AB258A269B3C2919458617048789CEECC8DDF18038AF6BFF8770FC35FFA0E78AFFF0002EDBFF91EBC53F64CFDACFC21F01FE1CEA3A06BFA76B7797973AAC97C8FA6C10BC611A1850025E543BB31B76C608E6BDAFF00E1E3DF0D7FE807E2BFFC04B6FF00E48AF39DEFA1F3984FECDF610F6B6E6B6A1FF0EE1F86BFF41CF15FFE05DB7FF23D78A7ED67FB267843E03FC39D3B5FD0351D6EF2F2E7558EC5D3529E178C2343339202448776635EF8C13C57B5FF00C3C7BE1AFF00D00FC57FF8096DFF00C915E29FB59FED67E10F8F1F0E74ED0340D3B5BB3BCB6D563BE77D4A0852328B0CC840292B9DD9917B6300F349735C317FD9BEC27EC6DCD6D0FFD9','hex'),
    'image/jpeg',
    'alentejo-sabores-demo.jpg'
)
on conflict (id) do update set
    nome = excluded.nome,
    nif = excluded.nif,
    morada = excluded.morada,
    morada1 = excluded.morada1,
    id_codpostal = excluded.id_codpostal,
    localidade = excluded.localidade,
    id_pais = excluded.id_pais,
    id_freguesia = excluded.id_freguesia,
    capital_social = excluded.capital_social,
    matricula_registo_comercial = excluded.matricula_registo_comercial,
    cae = excluded.cae,
    descricao_cae = excluded.descricao_cae,
    email = excluded.email,
    web = excluded.web,
    nome_comercial = excluded.nome_comercial,
    telefone = excluded.telefone,
    iban = excluded.iban,
    bic_swift = excluded.bic_swift,
    texto_rodape = excluded.texto_rodape,
    observacoes_legais = excluded.observacoes_legais,
    observacoes_comerciais_default = excluded.observacoes_comerciais_default,
    logo = excluded.logo,
    logo_media_type = excluded.logo_media_type,
    logo_nome_interno = excluded.logo_nome_interno;

insert into parametros_aplicacao (id, atraso_carga_minutos, decimais_quantidade, decimais_valor)
values (1, 10, 3, 2)
on conflict (id) do update set
    atraso_carga_minutos = excluded.atraso_carga_minutos,
    decimais_quantidade = excluded.decimais_quantidade,
    decimais_valor = excluded.decimais_valor;

-- Documentos e series
insert into tipodocumento (
    id,
    descricao,
    modelo_emissao1,
    modelo_emissao2,
    modelo_emissao3,
    modelo_emissao4,
    area_gestao,
    entidade,
    sinal_contabilistico,
    liquidacao_imediata
)
values
    ('FT', 'Fatura', null, null, null, null, 2, 1, 1, false),
    ('RC', 'Recibo', null, null, null, null, 3, 1, 2, true)
on conflict (id) do update set
    descricao = excluded.descricao,
    modelo_emissao1 = excluded.modelo_emissao1,
    modelo_emissao2 = excluded.modelo_emissao2,
    modelo_emissao3 = excluded.modelo_emissao3,
    modelo_emissao4 = excluded.modelo_emissao4,
    area_gestao = excluded.area_gestao,
    entidade = excluded.entidade,
    sinal_contabilistico = excluded.sinal_contabilistico,
    liquidacao_imediata = excluded.liquidacao_imediata;

insert into serie (id_tipo_documento, serie, nome, codigo_at, data_codigo_at, numerador)
values
    ('FT', 'DEMO26', 'Serie demonstracao FT 2026', 'DEMOFT26', '2026-01-01', 0),
    ('RC', 'DEMO26', 'Serie demonstracao RC 2026', 'DEMORC26', '2026-01-01', 0)
on conflict (id_tipo_documento, serie) do update set
    nome = excluded.nome,
    codigo_at = excluded.codigo_at,
    data_codigo_at = excluded.data_codigo_at;

-- Utilizador temporario para emissao enquanto a seguranca esta desativada.
-- Password meramente demonstrativa; nao usar em producao.
insert into utilizador (codigo, nome, email, password_hash, inativo)
values (
    'DEMO',
    'Utilizador Demo',
    'utilizador@fac.demo',
    '$2a$10$Ig5Tm6ex2e27iy2z2InRqOqIa0pQmTF0ZIQySWuR5CmMiYP3LxkEy',
    false
)
on conflict (codigo) do update set
    nome = excluded.nome,
    email = excluded.email,
    password_hash = excluded.password_hash,
    inativo = excluded.inativo;

-- Estrutura comercial minima
insert into familia (id, descricao)
values (1001, 'Servicos')
on conflict (id) do update set descricao = excluded.descricao;

insert into artigo (
    codigo,
    abreviatura,
    codigo_identificacao,
    descricao,
    unidade,
    id_familia,
    peso,
    id_iva_compra,
    id_iva_venda,
    pvp,
    inativo,
    retencao,
    observacoes
)
values (
    'SERVHORA',
    'HORA',
    null,
    'Servico tecnico por hora',
    'H',
    1001,
    0.000,
    'NORMAL',
    'NORMAL',
    50.000000,
    false,
    false,
    'Artigo demo para simular faturacao de servicos'
)
on conflict (codigo) do update set
    abreviatura = excluded.abreviatura,
    codigo_identificacao = excluded.codigo_identificacao,
    descricao = excluded.descricao,
    unidade = excluded.unidade,
    id_familia = excluded.id_familia,
    peso = excluded.peso,
    id_iva_compra = excluded.id_iva_compra,
    id_iva_venda = excluded.id_iva_venda,
    pvp = excluded.pvp,
    inativo = excluded.inativo,
    retencao = excluded.retencao,
    observacoes = excluded.observacoes;

insert into armazem (
    id,
    nome,
    morada,
    morada1,
    id_codpostal,
    localidade,
    id_pais,
    id_freguesia
)
values (
    1001,
    'Sede',
    'Rua da Empresa, 1',
    null,
    '3750-001',
    'Agueda',
    'PT',
    null
)
on conflict (id) do update set
    nome = excluded.nome,
    morada = excluded.morada,
    morada1 = excluded.morada1,
    id_codpostal = excluded.id_codpostal,
    localidade = excluded.localidade,
    id_pais = excluded.id_pais,
    id_freguesia = excluded.id_freguesia;

insert into cliente (
    id,
    nome,
    morada,
    morada1,
    localidade,
    id_codpostal,
    id_pais,
    nif,
    id_moeda,
    tel,
    tm,
    email,
    email1,
    id_mpagamento,
    id_ppagamento,
    id_riva,
    tspiva,
    iban,
    retencao,
    id_transporte,
    inativo,
    observacoes
)
values (
    1001,
    'Cliente Demonstracao Lda',
    'Rua do Cliente, 10',
    null,
    'Agueda',
    '3750-001',
    'PT',
    '509999990',
    'EUR',
    null,
    null,
    'cliente@fac.demo',
    null,
    1001,
    'P30',
    'CON',
    null,
    null,
    false,
    1001,
    false,
    'Cliente demo para simular documentos comerciais'
)
on conflict (id) do update set
    nome = excluded.nome,
    morada = excluded.morada,
    morada1 = excluded.morada1,
    localidade = excluded.localidade,
    id_codpostal = excluded.id_codpostal,
    id_pais = excluded.id_pais,
    nif = excluded.nif,
    id_moeda = excluded.id_moeda,
    tel = excluded.tel,
    tm = excluded.tm,
    email = excluded.email,
    email1 = excluded.email1,
    id_mpagamento = excluded.id_mpagamento,
    id_ppagamento = excluded.id_ppagamento,
    id_riva = excluded.id_riva,
    tspiva = excluded.tspiva,
    iban = excluded.iban,
    retencao = excluded.retencao,
    id_transporte = excluded.id_transporte,
    inativo = excluded.inativo,
    observacoes = excluded.observacoes;

insert into pais (id, nome) values ('ES', 'Espanha') on conflict (id) do update set nome=excluded.nome;
insert into codpostal (id, nome) values ('7000-001','Evora Demo'),('28000','Madrid Demo') on conflict (id) do update set nome=excluded.nome;
update armazem set nome='Armazem Alentejo Sabores', morada='Avenida das Oliveiras, 14', id_codpostal='7000-001', localidade='Evora Demo' where id=1001;

insert into familia (id, descricao) values (1002,'Azeites'),(1003,'Vinhos'),(1004,'Cabazes e servicos') on conflict (id) do update set descricao=excluded.descricao;
insert into artigo (codigo,abreviatura,codigo_identificacao,descricao,unidade,id_familia,peso,id_iva_compra,id_iva_venda,pvp,inativo,retencao,observacoes) values
('AZ075','AZ075',null,'Azeite Virgem Extra 0,75 L','UN',1002,0.750,'INTERMEDIA','INTERMEDIA',12.500000,false,false,'Produto ficticio de demonstracao'),
('AZ5L','AZ5L',null,'Azeite Virgem Extra 5 L','UN',1002,5.000,'INTERMEDIA','INTERMEDIA',54.900000,false,false,'Produto ficticio de demonstracao'),
('VTRES','VTRES',null,'Vinho Tinto Reserva','UN',1003,0.750,'NORMAL','NORMAL',18.900000,false,false,'Produto ficticio de demonstracao'),
('VBREG','VBREG',null,'Vinho Branco Regional','UN',1003,0.750,'NORMAL','NORMAL',11.900000,false,false,'Produto ficticio de demonstracao'),
('CABAZ','CABAZ',null,'Caixa Presente Alentejana','UN',1004,2.000,'NORMAL','NORMAL',39.900000,false,false,'Produto ficticio de demonstracao'),
('PREPCAB','PREP',null,'Servico de Preparacao de Cabaz','UN',1004,0.000,'NORMAL','NORMAL',7.500000,false,false,'Servico ficticio de demonstracao'),
('TRNAC','TRNAC',null,'Transporte Nacional','UN',1004,0.000,'NORMAL','NORMAL',15.000000,false,false,'Servico ficticio de demonstracao'),
('TRINT','TRINT',null,'Transporte Intracomunitario','UN',1004,0.000,'NORMAL','NORMAL',35.000000,false,false,'Tratado com a regra IVA atualmente suportada pelo FAC')
on conflict (codigo) do update set descricao=excluded.descricao,pvp=excluded.pvp,id_iva_venda=excluded.id_iva_venda,inativo=false;

insert into cliente (id,nome,morada,morada1,localidade,id_codpostal,id_pais,nif,id_moeda,tel,tm,email,email1,id_mpagamento,id_ppagamento,id_riva,tspiva,iban,retencao,id_transporte,inativo,observacoes) values
(1101,'Mercearia Campo Dourado, Lda.','Rua do Mercado Demo, 8',null,'Evora Demo','7000-001','PT','599100011','EUR',null,null,'compras@campo-dourado.demo',null,1001,'P30','CON',null,null,false,1001,false,'Cliente empresarial nacional ficticio'),
(1102,'Sabores de Madrid SL','Calle Olivo Demo, 21',null,'Madrid Demo','28000','ES','ESB9900001','EUR',null,null,'compras@sabores-madrid.demo',null,1001,'P30','CON',null,null,false,1001,false,'Cliente espanhol ficticio; fiscalidade limitada ao modelo atual'),
(1103,'Consumidor Final Demo','Praca Central Demo, 3',null,'Evora Demo','7000-001','PT','999999990','EUR',null,null,null,null,1001,'P30','CON',null,null,false,1001,false,'Consumidor final ficticio'),
(1104,'Casa dos Sabores do Sul, Lda.','Rua das Adegas Demo, 17',null,'Evora Demo','7000-001','PT','599100029','EUR',null,null,'financeiro@casa-sabores.demo',null,1001,'P30','CON',null,null,false,1001,false,'Cliente com varios movimentos para extrato'),
(1105,'Cliente Documento Anulado, Lda.','Travessa do Montado Demo, 5',null,'Evora Demo','7000-001','PT','599100037','EUR',null,null,'arquivo@cliente-anulado.demo',null,1001,'P30','CON',null,null,false,1001,false,'Cliente reservado ao documento anulado')
on conflict (id) do update set nome=excluded.nome,morada=excluded.morada,id_codpostal=excluded.id_codpostal,id_pais=excluded.id_pais,nif=excluded.nif,email=excluded.email,inativo=false;

-- O cenário comercial consolidado usa exatamente os cinco clientes e oito artigos acima.
delete from cliente where id = 1001;
delete from artigo where codigo = 'SERVHORA';

-- IDs uteis para a simulacao por API:
--
-- Empresa: 1
-- Utilizador emissor: DEMO
-- Cliente: 1001
-- Artigo: SERVHORA
-- Armazem: 1001
-- Tipo documento comercial: FT
-- Tipo documento financeiro: RC
-- Serie: 2026
-- Moeda: EUR
-- Regime IVA: CON
-- Prazo pagamento: P30
-- Modo pagamento: 1001
-- Transporte: 1001
