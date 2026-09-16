\set ON_ERROR_STOP on
BEGIN;

CREATE TEMP TABLE postal_reference (id varchar(20) PRIMARY KEY, nome varchar(50) NOT NULL) ON COMMIT DROP;
CREATE TEMP TABLE freguesia_reference (
  codigo varchar(6) PRIMARY KEY,
  codigo_distrito varchar(2) NOT NULL,
  codigo_concelho varchar(2) NOT NULL,
  codigo_freguesia varchar(2) NOT NULL,
  concelho varchar(50) NOT NULL,
  nome varchar(80) NOT NULL,
  extinta boolean NOT NULL
) ON COMMIT DROP;

\copy postal_reference (id,nome) FROM '/tmp/tuuli-codigos-postais.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')
\copy freguesia_reference (codigo,codigo_distrito,codigo_concelho,codigo_freguesia,concelho,nome,extinta) FROM '/tmp/tuuli-freguesias.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')

DO $$
BEGIN
  IF (SELECT count(*) FROM postal_reference) <> 197772 THEN
    RAISE EXCEPTION 'Catálogo postal incompleto: esperado 197772';
  END IF;
  IF (SELECT count(*) FROM freguesia_reference) <> 5188 THEN
    RAISE EXCEPTION 'Catálogo de freguesias incompleto: esperado 5188';
  END IF;
  IF (SELECT count(*) FROM freguesia_reference WHERE extinta) <> 2095 THEN
    RAISE EXCEPTION 'Contagem de freguesias extintas incorreta: esperado 2095';
  END IF;
  IF EXISTS (SELECT 1 FROM freguesia_reference
             WHERE codigo <> codigo_distrito || codigo_concelho || codigo_freguesia) THEN
    RAISE EXCEPTION 'Código de freguesia inconsistente';
  END IF;
  IF EXISTS (SELECT 1 FROM postal_reference WHERE id = '' OR nome = '') OR
     EXISTS (SELECT 1 FROM freguesia_reference WHERE codigo = '' OR nome = '' OR concelho = '') THEN
    RAISE EXCEPTION 'Dataset de referência contém campos vazios';
  END IF;
END $$;

INSERT INTO codpostal (id,nome)
SELECT id,nome FROM postal_reference ON CONFLICT (id) DO NOTHING;

INSERT INTO freguesia (codigo,codigo_distrito,codigo_concelho,codigo_freguesia,concelho,nome,extinta)
SELECT codigo,codigo_distrito,codigo_concelho,codigo_freguesia,concelho,nome,extinta
FROM freguesia_reference ON CONFLICT (codigo) DO NOTHING;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM postal_reference p LEFT JOIN codpostal c USING (id)
             WHERE c.id IS NULL OR c.nome <> p.nome) THEN
    RAISE EXCEPTION 'Códigos postais existentes divergem do dataset; carga revertida';
  END IF;
  IF EXISTS (SELECT 1 FROM freguesia_reference r LEFT JOIN freguesia f USING (codigo)
             WHERE f.codigo IS NULL OR f.codigo_distrito <> r.codigo_distrito
                OR f.codigo_concelho <> r.codigo_concelho OR f.codigo_freguesia <> r.codigo_freguesia
                OR f.concelho <> r.concelho OR f.nome <> r.nome OR f.extinta <> r.extinta) THEN
    RAISE EXCEPTION 'Freguesias existentes divergem do dataset; carga revertida';
  END IF;
END $$;

COMMIT;
