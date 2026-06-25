-- MD 28: gestao administrativa da instalacao e parametrizacao da empresa.
-- Migracao exclusivamente aditiva; nao cria utilizadores nem empresas ficticias.

ALTER TABLE public.utilizador
    ADD COLUMN criado_em timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN atualizado_em timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN ultimo_login_em timestamp with time zone,
    ADD COLUMN criado_por character varying(20),
    ADD COLUMN atualizado_por character varying(20);

ALTER TABLE public.utilizador DROP CONSTRAINT IF EXISTS utilizador_email_key;
CREATE UNIQUE INDEX ux_utilizador_email_lower ON public.utilizador (lower(email));
CREATE INDEX ix_utilizador_papel_ativo ON public.utilizador (papel, inativo);
CREATE INDEX ix_utilizador_nome_lower ON public.utilizador (lower(nome));

ALTER TABLE public.empresa
    ADD COLUMN nome_comercial character varying(100),
    ADD COLUMN telefone character varying(30),
    ADD COLUMN iban character varying(34),
    ADD COLUMN bic_swift character varying(11),
    ADD COLUMN observacoes_legais character varying(1000),
    ADD COLUMN texto_rodape character varying(500),
    ADD COLUMN observacoes_comerciais_default character varying(1000),
    ADD COLUMN logo bytea,
    ADD COLUMN logo_media_type character varying(20),
    ADD COLUMN logo_nome_interno character varying(80),
    ADD COLUMN atualizado_em timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN atualizado_por character varying(20),
    ADD CONSTRAINT ck_empresa_logo_consistente CHECK (
        (logo IS NULL AND logo_media_type IS NULL AND logo_nome_interno IS NULL)
        OR (logo IS NOT NULL AND logo_media_type IN ('image/png', 'image/jpeg') AND logo_nome_interno IS NOT NULL)
    );

ALTER TABLE public.documento_comercial
    ADD COLUMN emitente_nome_comercial character varying(100),
    ADD COLUMN emitente_telefone character varying(30),
    ADD COLUMN emitente_iban character varying(34),
    ADD COLUMN emitente_bic_swift character varying(11),
    ADD COLUMN emitente_observacoes_legais character varying(1000),
    ADD COLUMN emitente_texto_rodape character varying(500),
    ADD COLUMN emitente_logo bytea,
    ADD COLUMN emitente_logo_media_type character varying(20);

ALTER TABLE public.documento_comercial DROP CONSTRAINT ck_documento_fiscal_snapshot_version;
ALTER TABLE public.documento_comercial
    ADD CONSTRAINT ck_documento_fiscal_snapshot_version
        CHECK (fiscal_snapshot_version IS NULL OR fiscal_snapshot_version IN (1, 2));

COMMENT ON COLUMN public.utilizador.codigo IS 'Username e identificador estavel do utilizador';
COMMENT ON COLUMN public.empresa.logo IS 'Logotipo interno PNG/JPEG, no maximo 1 MiB validado pela aplicacao';
COMMENT ON COLUMN public.documento_comercial.emitente_logo IS 'Snapshot imutavel do logotipo no momento da emissao';
