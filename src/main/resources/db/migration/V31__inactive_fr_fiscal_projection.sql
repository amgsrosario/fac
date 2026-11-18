-- Inactive schema-v2 FR projection. No definitions, pointers or historical backfill.
DO $$ DECLARE c text; BEGIN
 SELECT conname INTO c FROM pg_constraint WHERE conrelid='fiscal_catalog_revision'::regclass
   AND contype='c' AND pg_get_constraintdef(oid) LIKE '%schemaVersion%';
 EXECUTE format('ALTER TABLE fiscal_catalog_revision DROP CONSTRAINT %I',c);
END $$;
ALTER TABLE fiscal_catalog_revision ADD CONSTRAINT fiscal_catalog_definition_version CHECK (
 jsonb_typeof(definition)='object' AND definition ?& ARRAY['schemaVersion','fiscalCode','revision','semantics']
 AND jsonb_typeof(definition->'schemaVersion')='number' AND definition->>'schemaVersion' IN ('1','2')
 AND jsonb_typeof(definition->'fiscalCode')='string' AND definition->>'fiscalCode'=fiscal_code
 AND jsonb_typeof(definition->'revision')='number' AND definition->>'revision'=revision::text
 AND jsonb_typeof(definition->'semantics')='object');

ALTER FUNCTION fiscal_ft_definition_valid(jsonb) RENAME TO fiscal_ft_deferred_definition_valid;
CREATE FUNCTION fiscal_fr_definition_valid(d jsonb) RETURNS boolean LANGUAGE plpgsql IMMUTABLE STRICT AS $$
DECLARE c jsonb;
BEGIN
 IF jsonb_typeof(d)<>'object' OR (SELECT count(*) FROM jsonb_object_keys(d))<>4
 OR NOT d ?& ARRAY['schemaVersion','fiscalCode','revision','semantics']
 OR d->>'schemaVersion'<>'2' OR d->>'fiscalCode'<>'FT'
 OR jsonb_typeof(d->'revision')<>'number' OR (d->>'revision')!~'^[1-9][0-9]*$'
 OR length(d->>'revision')>19 THEN RETURN false; END IF;
 IF (d->>'revision')::numeric>9223372036854775807 THEN RETURN false; END IF;
 IF jsonb_typeof(d->'semantics')<>'object' THEN RETURN false; END IF;
 IF (SELECT count(*) FROM jsonb_object_keys(d->'semantics'))<>4
 OR NOT (d->'semantics') ?& ARRAY['specialization','externalType','documentLabel','allowedConfigurations']
 OR d#>>'{semantics,specialization}'<>'FR' OR d#>>'{semantics,externalType}'<>'FR'
 OR d#>>'{semantics,documentLabel}'<>'Fatura-recibo'
 OR jsonb_typeof(d#>'{semantics,allowedConfigurations}')<>'array' THEN RETURN false; END IF;
 IF jsonb_array_length(d#>'{semantics,allowedConfigurations}')=0 THEN RETURN false; END IF;
 IF (SELECT count(*) FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}'))<>
 (SELECT count(DISTINCT value) FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}')) THEN RETURN false; END IF;
 FOR c IN SELECT value FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}') LOOP
  IF jsonb_typeof(c)<>'object' THEN RETURN false; END IF;
  IF (SELECT count(*) FROM jsonb_object_keys(c))<>5
  OR NOT c ?& ARRAY['codigoFiscal','areaGestao','entidade','sinalContabilistico','liquidacaoImediata']
  OR c->>'codigoFiscal'<>'FT' OR jsonb_typeof(c->'codigoFiscal')<>'string'
  OR jsonb_typeof(c->'liquidacaoImediata')<>'boolean' OR c->>'liquidacaoImediata'<>'true'
  OR jsonb_typeof(c->'areaGestao')<>'number' OR (c->>'areaGestao')!~'^[1-9][0-9]*$'
  OR jsonb_typeof(c->'entidade')<>'number' OR (c->>'entidade')!~'^[1-9][0-9]*$'
  OR jsonb_typeof(c->'sinalContabilistico')<>'number' OR (c->>'sinalContabilistico')!~'^[1-9][0-9]*$' THEN RETURN false; END IF;
  IF (c->>'areaGestao')::numeric NOT BETWEEN 1 AND 99 OR (c->>'entidade')::numeric NOT BETWEEN 1 AND 9
  OR (c->>'sinalContabilistico')::numeric NOT BETWEEN 1 AND 2 THEN RETURN false; END IF;
 END LOOP;
 RETURN true;
END $$;
CREATE FUNCTION fiscal_ft_definition_valid(d jsonb) RETURNS boolean LANGUAGE sql IMMUTABLE STRICT AS $$
 SELECT CASE WHEN d->>'schemaVersion'='2' THEN fiscal_fr_definition_valid(d) ELSE fiscal_ft_deferred_definition_valid(d) END
$$;
CREATE OR REPLACE FUNCTION fiscal_ft_configuration_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE d jsonb; changed boolean := true;
BEGIN
    IF TG_OP = 'UPDATE' THEN
        changed := ROW(NEW.codigo_fiscal,NEW.area_gestao,NEW.entidade,NEW.sinal_contabilistico,NEW.liquidacao_imediata)
            IS DISTINCT FROM ROW(OLD.codigo_fiscal,OLD.area_gestao,OLD.entidade,OLD.sinal_contabilistico,OLD.liquidacao_imediata);
        IF OLD.fiscal_catalog_code IS NOT NULL AND NEW.id IS DISTINCT FROM OLD.id THEN
            RAISE EXCEPTION 'Governed configuration identity is immutable' USING ERRCODE = '23514';
        END IF;
    END IF;
    IF NEW.fiscal_catalog_code IS NOT NULL AND NEW.fiscal_catalog_revision IS NOT NULL THEN
        -- Other governed typologies have not been specified/homologated by this slice.
        IF NEW.fiscal_catalog_code <> 'FT' THEN
            RAISE EXCEPTION 'Unsupported governed configuration in this release' USING ERRCODE = '23514';
        END IF;
        SELECT definition INTO d FROM fiscal_catalog_revision
         WHERE fiscal_code=NEW.fiscal_catalog_code AND revision=NEW.fiscal_catalog_revision;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Fiscal catalog revision does not exist' USING ERRCODE='23503';
        END IF;
        IF fiscal_ft_definition_valid(d) IS NOT TRUE OR NEW.codigo_fiscal IS DISTINCT FROM 'FT'
           OR NEW.liquidacao_imediata IS DISTINCT FROM (d->>'schemaVersion'='2') OR NOT EXISTS (
             SELECT 1 FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}') c
              WHERE (c->>'areaGestao')::integer=NEW.area_gestao
                AND (c->>'entidade')::integer=NEW.entidade
                AND (c->>'sinalContabilistico')::integer=NEW.sinal_contabilistico
           ) THEN
            RAISE EXCEPTION 'Configuration is incompatible with governed FT revision' USING ERRCODE='23514';
        END IF;
    ELSIF (TG_OP='INSERT' OR changed) AND upper(btrim(NEW.codigo_fiscal))='FT' AND NOT NEW.liquidacao_imediata THEN
        RAISE EXCEPTION 'New or reclassified FT deferred configuration requires a governed binding' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;

ALTER TABLE documento_comercial
 ADD COLUMN projection_external_type varchar(2), ADD COLUMN projection_catalog_code varchar(2),
 ADD COLUMN projection_revision bigint, ADD COLUMN projection_label varchar(50),
 ADD CONSTRAINT document_projection_pair CHECK(
  (num_nonnulls(projection_external_type,projection_catalog_code,projection_revision,projection_label)=0)
  OR (num_nonnulls(projection_external_type,projection_catalog_code,projection_revision,projection_label)=4 AND projection_external_type='FR' AND projection_catalog_code='FT' AND projection_revision IS NOT NULL AND projection_label='Fatura-recibo')),
 ADD CONSTRAINT document_projection_revision FOREIGN KEY(projection_catalog_code,projection_revision) REFERENCES fiscal_catalog_revision(fiscal_code,revision);

-- Tombstones outlive configuration removal. This is classification history, not another financial ledger.
CREATE TABLE fiscal_type_identity(internal_code varchar(3) PRIMARY KEY,external_type varchar(2) NOT NULL CHECK(length(external_type)=2));
CREATE TRIGGER fiscal_type_identity_immutable BEFORE UPDATE OR DELETE ON fiscal_type_identity FOR EACH ROW EXECUTE FUNCTION financial_immutable();
CREATE TRIGGER fiscal_type_identity_no_truncate BEFORE TRUNCATE ON fiscal_type_identity FOR EACH STATEMENT EXECUTE FUNCTION financial_immutable();

-- Versioned lock identities force stale REPEATABLE READ writers to abort, not validate old history.
CREATE TABLE fiscal_identity_mutex(resource text PRIMARY KEY,generation bigint NOT NULL);
CREATE FUNCTION fiscal_identity_touch(key text) RETURNS void LANGUAGE sql AS $$
 INSERT INTO fiscal_identity_mutex VALUES(key,1) ON CONFLICT(resource) DO UPDATE SET generation=fiscal_identity_mutex.generation+1
$$;

CREATE FUNCTION fiscal_claim_identity(code text,external_code text,strict_history boolean) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('FISCAL_CODE:'||code,0));
 PERFORM fiscal_identity_touch('CODE:'||code);
 IF EXISTS(SELECT 1 FROM fiscal_type_identity WHERE internal_code=code AND external_type<>external_code)
 OR EXISTS(SELECT 1 FROM documento_comercial d WHERE coalesce(d.tipo_documento_codigo,d.id_tipo_documento)=code
   AND (d.estado IN('EMITIDO','ANULADO') OR d.momento_emissao IS NOT NULL)
   AND coalesce(d.projection_external_type,d.tipo_documento_codigo_fiscal) IS NOT NULL
   AND coalesce(d.projection_external_type,d.tipo_documento_codigo_fiscal)<>external_code)
 THEN RAISE EXCEPTION 'Internal code already identifies a different external fiscal type'; END IF;
 IF strict_history AND (
  EXISTS(SELECT 1 FROM documento_comercial d WHERE coalesce(d.tipo_documento_codigo,d.id_tipo_documento)=code
   AND (d.estado IN('EMITIDO','ANULADO') OR d.momento_emissao IS NOT NULL)
   AND coalesce(d.projection_external_type,d.tipo_documento_codigo_fiscal) IS NULL)
  OR EXISTS(SELECT 1 FROM documento_financeiro WHERE id_tipo_documento=code))
 THEN RAISE EXCEPTION 'Internal code has historical identity not proven for a new FR projection'; END IF;
 INSERT INTO fiscal_type_identity VALUES(code,external_code) ON CONFLICT(internal_code) DO NOTHING;
END $$;

CREATE FUNCTION fiscal_governed_fr_type_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE d jsonb; external_code text;
BEGIN
 IF NEW.fiscal_catalog_code IS NOT NULL THEN
  SELECT definition INTO d FROM fiscal_catalog_revision WHERE fiscal_code=NEW.fiscal_catalog_code AND revision=NEW.fiscal_catalog_revision;
 END IF;
 external_code:=CASE WHEN d->>'schemaVersion'='2' THEN 'FR' ELSE NEW.codigo_fiscal END;
 IF d->>'schemaVersion'='2' THEN PERFORM fiscal_claim_identity(NEW.id,external_code,true);
 ELSIF external_code IS NOT NULL AND EXISTS(SELECT 1 FROM fiscal_type_identity WHERE internal_code=NEW.id) THEN
  PERFORM fiscal_claim_identity(NEW.id,external_code,false);
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER fiscal_governed_fr_type_guard BEFORE INSERT OR UPDATE ON tipodocumento FOR EACH ROW EXECUTE FUNCTION fiscal_governed_fr_type_guard();

CREATE FUNCTION fiscal_document_projection_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE chosen_revision fiscal_catalog_revision; t tipodocumento; external_code text;
BEGIN
 SELECT * INTO t FROM tipodocumento WHERE id=NEW.id_tipo_documento;
 SELECT * INTO chosen_revision FROM fiscal_catalog_revision r WHERE r.fiscal_code=t.fiscal_catalog_code AND r.revision=t.fiscal_catalog_revision;
 IF TG_OP='UPDATE' AND OLD.projection_external_type IS NOT NULL AND
  (NEW.projection_external_type,NEW.projection_catalog_code,NEW.projection_revision,NEW.projection_label)
  IS DISTINCT FROM (OLD.projection_external_type,OLD.projection_catalog_code,OLD.projection_revision,OLD.projection_label)
 THEN RAISE EXCEPTION 'Document projection is immutable'; END IF;
 IF NEW.projection_external_type IS NOT NULL THEN
  IF fiscal_fr_definition_valid(chosen_revision.definition) IS NOT TRUE OR NOT t.liquidacao_imediata
  OR NEW.projection_external_type<>'FR' OR NEW.projection_catalog_code IS DISTINCT FROM t.fiscal_catalog_code
  OR NEW.projection_revision IS DISTINCT FROM t.fiscal_catalog_revision
  OR NEW.projection_label IS DISTINCT FROM chosen_revision.definition#>>'{semantics,documentLabel}'
  OR NOT NEW.liquidado THEN RAISE EXCEPTION 'Invalid governed document FR projection'; END IF;
 END IF;
 IF NEW.estado IN('EMITIDO','ANULADO') OR NEW.momento_emissao IS NOT NULL THEN
  IF chosen_revision.definition->>'schemaVersion'='2' AND NEW.projection_external_type IS NULL THEN RAISE EXCEPTION 'Governed FR requires explicit projection and quittance'; END IF;
  external_code:=coalesce(NEW.projection_external_type,NEW.tipo_documento_codigo_fiscal);
  IF external_code IS NOT NULL THEN PERFORM fiscal_claim_identity(coalesce(NEW.tipo_documento_codigo,NEW.id_tipo_documento),external_code,NEW.projection_external_type IS NOT NULL); END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER fiscal_document_projection_guard BEFORE INSERT OR UPDATE ON documento_comercial FOR EACH ROW EXECUTE FUNCTION fiscal_document_projection_guard();
CREATE FUNCTION fiscal_fr_projection_complete() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM documento_comercial WHERE id=NEW.id AND projection_external_type='FR')
 AND NOT EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE document_id=NEW.id)
 THEN RAISE EXCEPTION 'Projected FR requires complete V30 quittance at commit'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER fiscal_fr_projection_complete AFTER INSERT OR UPDATE ON documento_comercial
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION fiscal_fr_projection_complete();
CREATE FUNCTION fiscal_financial_identity_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE fiscal text;
BEGIN
 SELECT codigo_fiscal INTO fiscal FROM tipodocumento WHERE id=NEW.id_tipo_documento;
 IF fiscal IS NOT NULL THEN PERFORM fiscal_claim_identity(NEW.id_tipo_documento,fiscal,false); END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER fiscal_financial_identity_guard BEFORE INSERT ON documento_financeiro FOR EACH ROW EXECUTE FUNCTION fiscal_financial_identity_guard();

-- A new FR series has a canonical external identity independent of aliases.
CREATE TABLE fiscal_fr_series (
 internal_code varchar(3) NOT NULL,serie varchar(10) NOT NULL,external_type varchar(2) NOT NULL CHECK(external_type='FR'),
 validation_code varchar(100),PRIMARY KEY(internal_code,serie),UNIQUE(external_type,serie),UNIQUE(validation_code));
CREATE FUNCTION fiscal_fr_series_record_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Fiscal series identity cannot be removed'; END IF;
 IF TG_OP='UPDATE' AND ((NEW.internal_code,NEW.serie,NEW.external_type) IS DISTINCT FROM (OLD.internal_code,OLD.serie,OLD.external_type)
   OR OLD.validation_code IS NOT NULL OR NEW.validation_code IS NULL) THEN RAISE EXCEPTION 'Fiscal series identity is immutable'; END IF;
 IF NOT EXISTS(SELECT 1 FROM serie s JOIN tipodocumento t ON t.id=s.id_tipo_documento
  JOIN fiscal_catalog_revision r ON r.fiscal_code=t.fiscal_catalog_code AND r.revision=t.fiscal_catalog_revision
  WHERE s.id_tipo_documento=NEW.internal_code AND s.serie=NEW.serie AND r.definition->>'schemaVersion'='2'
   AND NEW.validation_code IS NOT DISTINCT FROM upper(btrim(s.codigo_at))) THEN RAISE EXCEPTION 'Series identity must match its governed source'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER fiscal_fr_series_record_guard BEFORE INSERT OR UPDATE OR DELETE ON fiscal_fr_series FOR EACH ROW EXECUTE FUNCTION fiscal_fr_series_record_guard();
CREATE TRIGGER fiscal_fr_series_no_truncate BEFORE TRUNCATE ON fiscal_fr_series FOR EACH STATEMENT EXECUTE FUNCTION financial_immutable();
CREATE FUNCTION fiscal_fr_series_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE governed boolean; existing fiscal_fr_series;
BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('FISCAL_SERIES:'||NEW.serie,0));
 PERFORM fiscal_identity_touch('SERIES:'||NEW.serie);
 IF NEW.codigo_at IS NOT NULL THEN
  PERFORM pg_advisory_xact_lock(hashtextextended('FISCAL_AT:'||upper(btrim(NEW.codigo_at)),0));
  PERFORM fiscal_identity_touch('AT:'||upper(btrim(NEW.codigo_at)));
 END IF;
 SELECT EXISTS(SELECT 1 FROM tipodocumento t JOIN fiscal_catalog_revision r ON r.fiscal_code=t.fiscal_catalog_code AND r.revision=t.fiscal_catalog_revision
  WHERE t.id=NEW.id_tipo_documento AND r.definition->>'schemaVersion'='2') INTO governed;
 IF NOT governed THEN
  IF EXISTS(SELECT 1 FROM fiscal_fr_series WHERE serie=NEW.serie OR (validation_code IS NOT NULL AND validation_code=upper(btrim(NEW.codigo_at))))
  THEN RAISE EXCEPTION 'Series or AT code collides with a reserved FR fiscal identity'; END IF;
  RETURN NEW;
 END IF;
 IF TG_OP='UPDATE' AND (NEW.id_tipo_documento,NEW.serie) IS DISTINCT FROM (OLD.id_tipo_documento,OLD.serie) THEN RAISE EXCEPTION 'Governed FR series key is immutable'; END IF;
 IF EXISTS(SELECT 1 FROM serie s WHERE (s.id_tipo_documento,s.serie)<>(NEW.id_tipo_documento,NEW.serie)
  AND (s.serie=NEW.serie OR (s.codigo_at IS NOT NULL AND upper(btrim(s.codigo_at))=upper(btrim(NEW.codigo_at)))))
 THEN RAISE EXCEPTION 'New FR series collides with an existing documentary series'; END IF;
 IF NEW.codigo_at IS NOT NULL AND length(btrim(NEW.codigo_at))<8 THEN RAISE EXCEPTION 'FR validation code must have at least eight characters'; END IF;
 SELECT * INTO existing FROM fiscal_fr_series WHERE internal_code=NEW.id_tipo_documento AND serie=NEW.serie;
 IF FOUND THEN
  IF TG_OP='INSERT' THEN RAISE EXCEPTION 'Fiscal series cannot be recreated'; END IF;
  IF existing.validation_code IS DISTINCT FROM upper(btrim(NEW.codigo_at)) THEN
    UPDATE fiscal_fr_series SET validation_code=upper(btrim(NEW.codigo_at)) WHERE internal_code=NEW.id_tipo_documento AND serie=NEW.serie;
  END IF;
 ELSE INSERT INTO fiscal_fr_series VALUES(NEW.id_tipo_documento,NEW.serie,'FR',upper(btrim(NEW.codigo_at)));
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER fiscal_fr_series_guard AFTER INSERT OR UPDATE ON serie FOR EACH ROW EXECUTE FUNCTION fiscal_fr_series_guard();
