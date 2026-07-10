ALTER TABLE ONLY public.cliente
    DROP CONSTRAINT cliente_id_transporte_fkey;

ALTER TABLE ONLY public.documento_comercial
    DROP CONSTRAINT documento_comercial_id_transporte_fkey;

ALTER TABLE ONLY public.parametros_cliente
    DROP CONSTRAINT parametros_cliente_id_transporte_fkey;

ALTER TABLE ONLY public.transporte
    DROP CONSTRAINT transporte_pkey;

ALTER TABLE public.cliente
    ALTER COLUMN id_transporte TYPE character varying(3)
    USING CASE
        WHEN id_transporte = 2 THEN 'T02'
        WHEN id_transporte = 3 THEN 'T03'
        WHEN id_transporte = 4 THEN 'T04'
        WHEN id_transporte = 1001 THEN '001'
        ELSE lpad(id_transporte::text, 3, '0')
    END;

ALTER TABLE public.documento_comercial
    ALTER COLUMN id_transporte TYPE character varying(3)
    USING CASE
        WHEN id_transporte = 2 THEN 'T02'
        WHEN id_transporte = 3 THEN 'T03'
        WHEN id_transporte = 4 THEN 'T04'
        WHEN id_transporte = 1001 THEN '001'
        ELSE lpad(id_transporte::text, 3, '0')
    END;

ALTER TABLE public.parametros_cliente
    ALTER COLUMN id_transporte TYPE character varying(3)
    USING CASE
        WHEN id_transporte IS NULL THEN NULL
        WHEN id_transporte = 2 THEN 'T02'
        WHEN id_transporte = 3 THEN 'T03'
        WHEN id_transporte = 4 THEN 'T04'
        WHEN id_transporte = 1001 THEN '001'
        ELSE lpad(id_transporte::text, 3, '0')
    END;

ALTER TABLE public.transporte
    ALTER COLUMN id DROP IDENTITY IF EXISTS;

ALTER TABLE public.transporte
    ALTER COLUMN id TYPE character varying(3)
    USING CASE
        WHEN id = 2 THEN 'T02'
        WHEN id = 3 THEN 'T03'
        WHEN id = 4 THEN 'T04'
        WHEN id = 1001 THEN '001'
        ELSE lpad(id::text, 3, '0')
    END;

ALTER TABLE ONLY public.transporte
    ADD CONSTRAINT transporte_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.transporte
    ADD CONSTRAINT ck_transporte_id_codigo CHECK ((id)::text ~ '^[A-Z0-9]{3}$'::text);

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_id_transporte_fkey FOREIGN KEY (id_transporte) REFERENCES public.transporte(id);

ALTER TABLE ONLY public.documento_comercial
    ADD CONSTRAINT documento_comercial_id_transporte_fkey FOREIGN KEY (id_transporte) REFERENCES public.transporte(id);

ALTER TABLE ONLY public.parametros_cliente
    ADD CONSTRAINT parametros_cliente_id_transporte_fkey FOREIGN KEY (id_transporte) REFERENCES public.transporte(id);

DROP SEQUENCE IF EXISTS public.transporte_id_seq;
