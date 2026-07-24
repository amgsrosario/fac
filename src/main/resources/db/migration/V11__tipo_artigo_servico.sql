ALTER TABLE public.artigo
    ADD COLUMN tipo_artigo varchar(20);

UPDATE public.artigo
SET tipo_artigo = 'SERVICO'
WHERE tipo_artigo IS NULL;

UPDATE public.artigo
SET tipo_artigo = 'ARTIGO'
WHERE codigo IN ('DISCO1TB', 'AZ075', 'AZ5L', 'VTRES', 'VBREG', 'CABAZ');

ALTER TABLE public.artigo
    ALTER COLUMN tipo_artigo SET NOT NULL;

ALTER TABLE public.artigo
    ADD CONSTRAINT ck_artigo_tipo_artigo CHECK ((tipo_artigo)::text IN ('ARTIGO', 'SERVICO'));
