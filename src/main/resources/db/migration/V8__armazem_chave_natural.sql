ALTER TABLE ONLY public.documento_comercial
    DROP CONSTRAINT documento_comercial_id_armazem_carga_fkey;

ALTER TABLE ONLY public.parametros_documento_comercial
    DROP CONSTRAINT parametros_documento_comercial_id_armazem_carga_fkey;

ALTER TABLE ONLY public.armazem
    DROP CONSTRAINT armazem_pkey;

ALTER TABLE public.documento_comercial
    ALTER COLUMN id_armazem_carga TYPE character varying(3)
    USING CASE
        WHEN id_armazem_carga = 1001 THEN '001'
        ELSE lpad(id_armazem_carga::text, 3, '0')
    END;

ALTER TABLE public.parametros_documento_comercial
    ALTER COLUMN id_armazem_carga TYPE character varying(3)
    USING CASE
        WHEN id_armazem_carga IS NULL THEN NULL
        WHEN id_armazem_carga = 1001 THEN '001'
        ELSE lpad(id_armazem_carga::text, 3, '0')
    END;

ALTER TABLE public.armazem
    ALTER COLUMN id DROP IDENTITY IF EXISTS;

ALTER TABLE public.armazem
    ALTER COLUMN id TYPE character varying(3)
    USING CASE
        WHEN id = 1001 THEN '001'
        ELSE lpad(id::text, 3, '0')
    END;

ALTER TABLE ONLY public.armazem
    ADD CONSTRAINT armazem_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.armazem
    ADD CONSTRAINT ck_armazem_id_codigo CHECK ((id)::text ~ '^[A-Z0-9]{3}$'::text);

ALTER TABLE ONLY public.documento_comercial
    ADD CONSTRAINT documento_comercial_id_armazem_carga_fkey FOREIGN KEY (id_armazem_carga) REFERENCES public.armazem(id);

ALTER TABLE ONLY public.parametros_documento_comercial
    ADD CONSTRAINT parametros_documento_comercial_id_armazem_carga_fkey FOREIGN KEY (id_armazem_carga) REFERENCES public.armazem(id);

DROP SEQUENCE IF EXISTS public.armazem_id_seq;
