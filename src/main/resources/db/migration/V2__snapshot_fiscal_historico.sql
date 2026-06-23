ALTER TABLE documento_comercial
    ADD COLUMN fiscal_snapshot_version integer,
    ADD COLUMN emitente_nome varchar(100),
    ADD COLUMN emitente_nif varchar(20),
    ADD COLUMN emitente_morada varchar(60),
    ADD COLUMN emitente_morada1 varchar(60),
    ADD COLUMN emitente_cod_postal varchar(20),
    ADD COLUMN emitente_localidade varchar(50),
    ADD COLUMN emitente_pais varchar(3),
    ADD COLUMN emitente_email varchar(120),
    ADD COLUMN emitente_web varchar(120),
    ADD COLUMN emitente_capital_social numeric(19,2),
    ADD COLUMN emitente_matricula_registo varchar(100),
    ADD COLUMN emitente_cae varchar(10),
    ADD COLUMN emitente_descricao_cae varchar(100),
    ADD COLUMN tipo_documento_codigo varchar(3),
    ADD COLUMN tipo_documento_codigo_fiscal varchar(2),
    ADD COLUMN tipo_documento_descricao varchar(50),
    ADD COLUMN serie_descricao varchar(50),
    ADD COLUMN moeda_codigo varchar(3),
    ADD COLUMN moeda_simbolo varchar(5),
    ADD COLUMN moeda_casas_decimais integer,
    ADD COLUMN taxa_cambio numeric(20,10),
    ADD COLUMN regime_iva_codigo varchar(3),
    ADD CONSTRAINT ck_documento_fiscal_snapshot_version
        CHECK (fiscal_snapshot_version IS NULL OR fiscal_snapshot_version = 1),
    ADD CONSTRAINT ck_documento_moeda_casas_decimais
        CHECK (moeda_casas_decimais IS NULL OR moeda_casas_decimais BETWEEN 0 AND 10);

ALTER TABLE linha_documento_comercial
    ADD COLUMN artigo_codigo varchar(50),
    ADD COLUMN unidade varchar(3),
    ADD COLUMN tipo_taxa_iva_codigo varchar(20),
    ADD COLUMN tipo_taxa_iva_descricao varchar(50),
    ADD COLUMN base_tributavel numeric(19,6),
    ADD COLUMN valor_imposto numeric(19,6),
    ADD COLUMN total_linha numeric(19,6),
    ADD CONSTRAINT ck_linha_snapshot_valores
        CHECK (
            (base_tributavel IS NULL AND valor_imposto IS NULL AND total_linha IS NULL)
            OR (base_tributavel >= 0 AND valor_imposto >= 0 AND total_linha = base_tributavel + valor_imposto)
        );

CREATE INDEX ix_documento_comercial_snapshot_version
    ON documento_comercial (fiscal_snapshot_version);

CREATE INDEX ix_documento_comercial_numero_completo
    ON documento_comercial (numero_documento_completo)
    WHERE numero_documento_completo IS NOT NULL;
