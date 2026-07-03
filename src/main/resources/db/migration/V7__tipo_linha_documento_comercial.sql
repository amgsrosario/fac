ALTER TABLE public.linha_documento_comercial
    ADD COLUMN tipo_linha character varying(20) DEFAULT 'COMERCIAL' NOT NULL;

ALTER TABLE public.linha_documento_comercial
    ALTER COLUMN id_artigo DROP NOT NULL,
    ALTER COLUMN quantidade DROP NOT NULL,
    ALTER COLUMN preco_unitario DROP NOT NULL,
    ALTER COLUMN valor_bruto DROP NOT NULL,
    ALTER COLUMN tipo_desconto DROP NOT NULL,
    ALTER COLUMN desconto DROP NOT NULL,
    ALTER COLUMN valor_desconto DROP NOT NULL,
    ALTER COLUMN valor_linha DROP NOT NULL,
    ALTER COLUMN id_tipo_taxa_iva DROP NOT NULL,
    ALTER COLUMN percentagem_iva DROP NOT NULL;

ALTER TABLE public.linha_documento_comercial
    DROP CONSTRAINT IF EXISTS ck_linha_documento_comercial_desconto,
    DROP CONSTRAINT IF EXISTS ck_linha_documento_comercial_preco,
    DROP CONSTRAINT IF EXISTS ck_linha_documento_comercial_quantidade,
    DROP CONSTRAINT IF EXISTS ck_linha_documento_comercial_tipo_desconto,
    DROP CONSTRAINT IF EXISTS ck_linha_documento_comercial_valor;

ALTER TABLE public.linha_documento_comercial
    ADD CONSTRAINT ck_linha_documento_comercial_tipo_linha
        CHECK ((tipo_linha)::text = ANY ((ARRAY['COMERCIAL'::character varying, 'TEXTO'::character varying])::text[])),
    ADD CONSTRAINT ck_linha_documento_comercial_comercial
        CHECK (
            (tipo_linha = 'COMERCIAL'
                AND id_artigo IS NOT NULL
                AND quantidade IS NOT NULL AND quantidade > 0
                AND preco_unitario IS NOT NULL AND preco_unitario >= 0
                AND valor_bruto IS NOT NULL
                AND tipo_desconto IS NOT NULL
                AND desconto IS NOT NULL AND desconto >= 0
                AND valor_desconto IS NOT NULL AND valor_desconto >= 0 AND valor_desconto <= valor_bruto
                AND valor_linha IS NOT NULL AND valor_linha = valor_bruto - valor_desconto
                AND id_tipo_taxa_iva IS NOT NULL
                AND percentagem_iva IS NOT NULL
                AND (tipo_desconto)::text = ANY ((ARRAY['PERCENTAGEM'::character varying, 'VALOR'::character varying])::text[]))
            OR tipo_linha = 'TEXTO'
        ),
    ADD CONSTRAINT ck_linha_documento_comercial_texto
        CHECK (
            tipo_linha = 'COMERCIAL'
            OR (tipo_linha = 'TEXTO'
                AND id_artigo IS NULL
                AND quantidade IS NULL
                AND preco_unitario IS NULL
                AND valor_bruto IS NULL
                AND tipo_desconto IS NULL
                AND desconto IS NULL
                AND valor_desconto IS NULL
                AND valor_linha IS NULL
                AND id_tipo_taxa_iva IS NULL
                AND percentagem_iva IS NULL
                AND peso IS NULL)
        );
