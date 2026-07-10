ALTER TABLE ONLY public.cliente
    DROP CONSTRAINT cliente_id_mpagamento_fkey;

ALTER TABLE ONLY public.documento_comercial
    DROP CONSTRAINT documento_comercial_id_mpagamento_fkey;

ALTER TABLE ONLY public.documento_financeiro
    DROP CONSTRAINT documento_financeiro_id_mpagamento_fkey;

ALTER TABLE ONLY public.parametros_cliente
    DROP CONSTRAINT parametros_cliente_id_mpagamento_fkey;

ALTER TABLE ONLY public.mpagamento
    DROP CONSTRAINT mpagamento_pkey;

ALTER TABLE public.cliente
    ALTER COLUMN id_mpagamento TYPE character varying(3)
    USING CASE
        WHEN id_mpagamento IS NULL THEN NULL
        WHEN id_mpagamento = 2 THEN 'CHQ'
        WHEN id_mpagamento = 3 THEN 'NUM'
        WHEN id_mpagamento = 4 THEN 'TRF'
        WHEN id_mpagamento = 1001 THEN 'TFB'
        ELSE lpad(id_mpagamento::text, 3, '0')
    END;

ALTER TABLE public.documento_comercial
    ALTER COLUMN id_mpagamento TYPE character varying(3)
    USING CASE
        WHEN id_mpagamento IS NULL THEN NULL
        WHEN id_mpagamento = 2 THEN 'CHQ'
        WHEN id_mpagamento = 3 THEN 'NUM'
        WHEN id_mpagamento = 4 THEN 'TRF'
        WHEN id_mpagamento = 1001 THEN 'TFB'
        ELSE lpad(id_mpagamento::text, 3, '0')
    END;

ALTER TABLE public.documento_financeiro
    ALTER COLUMN id_mpagamento TYPE character varying(3)
    USING CASE
        WHEN id_mpagamento = 2 THEN 'CHQ'
        WHEN id_mpagamento = 3 THEN 'NUM'
        WHEN id_mpagamento = 4 THEN 'TRF'
        WHEN id_mpagamento = 1001 THEN 'TFB'
        ELSE lpad(id_mpagamento::text, 3, '0')
    END;

ALTER TABLE public.parametros_cliente
    ALTER COLUMN id_mpagamento TYPE character varying(3)
    USING CASE
        WHEN id_mpagamento IS NULL THEN NULL
        WHEN id_mpagamento = 2 THEN 'CHQ'
        WHEN id_mpagamento = 3 THEN 'NUM'
        WHEN id_mpagamento = 4 THEN 'TRF'
        WHEN id_mpagamento = 1001 THEN 'TFB'
        ELSE lpad(id_mpagamento::text, 3, '0')
    END;

ALTER TABLE public.mpagamento
    ALTER COLUMN id DROP IDENTITY IF EXISTS;

ALTER TABLE public.mpagamento
    ALTER COLUMN id TYPE character varying(3)
    USING CASE
        WHEN id = 2 THEN 'CHQ'
        WHEN id = 3 THEN 'NUM'
        WHEN id = 4 THEN 'TRF'
        WHEN id = 1001 THEN 'TFB'
        ELSE lpad(id::text, 3, '0')
    END;

ALTER TABLE ONLY public.mpagamento
    ADD CONSTRAINT mpagamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.mpagamento
    ADD CONSTRAINT ck_mpagamento_id_codigo CHECK ((id)::text ~ '^[A-Z0-9]{3}$'::text);

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_id_mpagamento_fkey FOREIGN KEY (id_mpagamento) REFERENCES public.mpagamento(id);

ALTER TABLE ONLY public.documento_comercial
    ADD CONSTRAINT documento_comercial_id_mpagamento_fkey FOREIGN KEY (id_mpagamento) REFERENCES public.mpagamento(id);

ALTER TABLE ONLY public.documento_financeiro
    ADD CONSTRAINT documento_financeiro_id_mpagamento_fkey FOREIGN KEY (id_mpagamento) REFERENCES public.mpagamento(id);

ALTER TABLE ONLY public.parametros_cliente
    ADD CONSTRAINT parametros_cliente_id_mpagamento_fkey FOREIGN KEY (id_mpagamento) REFERENCES public.mpagamento(id);

DROP SEQUENCE IF EXISTS public.mpagamento_id_seq;
