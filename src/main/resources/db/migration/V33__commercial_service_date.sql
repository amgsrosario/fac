ALTER TABLE documento_comercial ADD COLUMN data_prestacao date;

CREATE FUNCTION guard_commercial_service_date() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.estado <> 'RASCUNHO' OR NEW.estado <> 'RASCUNHO'
       OR OLD.numero_documento IS NOT NULL OR NEW.numero_documento IS NOT NULL THEN
        RAISE EXCEPTION 'Data de prestação imutável após emissão' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER commercial_service_date_immutable
BEFORE UPDATE OF data_prestacao ON documento_comercial
FOR EACH ROW WHEN (OLD.data_prestacao IS DISTINCT FROM NEW.data_prestacao)
EXECUTE FUNCTION guard_commercial_service_date();
