-- V25 freezes the master's fiscal attributes after emission. Keep that historical
-- authority attached to the financial document: switching its FK would bypass V25.
-- Financial documents have no draft state; every persisted header is issued.
-- No snapshot, backfill or change of historical reader semantics is introduced.
CREATE FUNCTION financial_document_classification_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Classificação do documento financeiro emitido é imutável'
        USING ERRCODE = '23514';
END $$;
CREATE TRIGGER financial_document_classification_guard
BEFORE UPDATE OF id_tipo_documento ON documento_financeiro
FOR EACH ROW WHEN (NEW.id_tipo_documento IS DISTINCT FROM OLD.id_tipo_documento)
EXECUTE FUNCTION financial_document_classification_guard();
