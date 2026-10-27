-- H2-03 infrastructure only. No fiscal revision, creation pointer, type or historical backfill.
-- Existing V25/V26/V27 remain unchanged. Only explicit FT/deferred enters this perimeter.
CREATE FUNCTION fiscal_ft_definition_valid(d jsonb) RETURNS boolean
LANGUAGE plpgsql IMMUTABLE STRICT AS $$
DECLARE c jsonb;
BEGIN
    IF jsonb_typeof(d) <> 'object' THEN RETURN false; END IF;
    IF (SELECT count(*) FROM jsonb_object_keys(d)) <> 4
       OR NOT d ?& ARRAY['schemaVersion','fiscalCode','revision','semantics']
       OR jsonb_typeof(d->'schemaVersion') <> 'number' OR d->>'schemaVersion' <> '1'
       OR jsonb_typeof(d->'fiscalCode') <> 'string' OR d->>'fiscalCode' <> 'FT'
       OR jsonb_typeof(d->'revision') <> 'number' OR (d->>'revision') !~ '^[1-9][0-9]*$'
       OR length(d->>'revision') > 19 THEN RETURN false; END IF;
    IF (d->>'revision')::numeric > 9223372036854775807 THEN RETURN false; END IF;
    IF jsonb_typeof(d->'semantics') <> 'object' THEN RETURN false; END IF;
    IF (SELECT count(*) FROM jsonb_object_keys(d->'semantics')) <> 1
       OR NOT (d->'semantics') ? 'allowedConfigurations'
       OR jsonb_typeof(d#>'{semantics,allowedConfigurations}') <> 'array' THEN RETURN false; END IF;
    IF jsonb_array_length(d#>'{semantics,allowedConfigurations}') = 0 THEN RETURN false; END IF;
    IF (SELECT count(*) FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}')) <>
       (SELECT count(DISTINCT value) FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}')) THEN RETURN false; END IF;
    FOR c IN SELECT value FROM jsonb_array_elements(d#>'{semantics,allowedConfigurations}') LOOP
        IF jsonb_typeof(c) <> 'object' THEN RETURN false; END IF;
        IF (SELECT count(*) FROM jsonb_object_keys(c)) <> 5
           OR NOT c ?& ARRAY['codigoFiscal','areaGestao','entidade','sinalContabilistico','liquidacaoImediata']
           OR jsonb_typeof(c->'codigoFiscal') <> 'string' OR c->>'codigoFiscal' <> 'FT'
           OR jsonb_typeof(c->'liquidacaoImediata') <> 'boolean' OR c->>'liquidacaoImediata' <> 'false'
           OR jsonb_typeof(c->'areaGestao') <> 'number' OR (c->>'areaGestao') !~ '^[1-9][0-9]*$'
           OR jsonb_typeof(c->'entidade') <> 'number' OR (c->>'entidade') !~ '^[1-9][0-9]*$'
           OR jsonb_typeof(c->'sinalContabilistico') <> 'number' OR (c->>'sinalContabilistico') !~ '^[1-9][0-9]*$'
           THEN RETURN false; END IF;
        IF (c->>'areaGestao')::numeric NOT BETWEEN 1 AND 99
           OR (c->>'entidade')::numeric NOT BETWEEN 1 AND 9
           OR (c->>'sinalContabilistico')::numeric NOT BETWEEN 1 AND 2 THEN RETURN false; END IF;
    END LOOP;
    RETURN true;
END $$;

CREATE FUNCTION fiscal_ft_revision_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.fiscal_code = 'FT' AND fiscal_ft_definition_valid(NEW.definition) IS NOT TRUE THEN
        RAISE EXCEPTION 'Invalid governed FT deferred definition' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER fiscal_ft_revision_guard BEFORE INSERT ON fiscal_catalog_revision
FOR EACH ROW EXECUTE FUNCTION fiscal_ft_revision_guard();

CREATE FUNCTION fiscal_ft_configuration_guard() RETURNS trigger LANGUAGE plpgsql AS $$
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
           OR NEW.liquidacao_imediata OR NOT EXISTS (
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
CREATE TRIGGER fiscal_ft_configuration_guard BEFORE INSERT OR UPDATE ON tipodocumento
FOR EACH ROW EXECUTE FUNCTION fiscal_ft_configuration_guard();
