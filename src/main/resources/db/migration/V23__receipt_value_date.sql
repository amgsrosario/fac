-- No backfill: historical receipts retain an unknown payment date.
ALTER TABLE documento_financeiro ADD COLUMN data_valor date;
ALTER TABLE documento_financeiro ADD CONSTRAINT receipt_value_date_upper
 CHECK (data_valor IS NULL OR data_valor <= data_emissao);

CREATE FUNCTION receipt_value_date_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='INSERT' THEN
   IF EXISTS(SELECT 1 FROM tipodocumento WHERE id=NEW.id_tipo_documento AND codigo_fiscal='RC')
      AND NEW.data_valor IS NULL THEN
     RAISE EXCEPTION 'New receipt requires a value date';
   END IF;
 ELSE
   IF NEW.id_tipo_documento IS DISTINCT FROM OLD.id_tipo_documento AND NEW.data_valor IS NULL
      AND EXISTS(SELECT 1 FROM tipodocumento WHERE id=NEW.id_tipo_documento AND codigo_fiscal='RC')
   THEN RAISE EXCEPTION 'Cannot convert historical document into a new receipt without value date'; END IF;
   IF NEW.data_valor IS DISTINCT FROM OLD.data_valor
      OR (OLD.data_valor IS NOT NULL AND
          (NEW.data_emissao,NEW.id_tipo_documento) IS DISTINCT FROM (OLD.data_emissao,OLD.id_tipo_documento))
   THEN RAISE EXCEPTION 'Issued receipt value date is immutable'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER receipt_value_date_guard BEFORE INSERT OR UPDATE ON documento_financeiro
 FOR EACH ROW EXECUTE FUNCTION receipt_value_date_guard();

CREATE FUNCTION receipt_value_date_check() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE receipt_id bigint;
BEGIN
 IF TG_TABLE_NAME='documento_financeiro' THEN receipt_id:=NEW.id;
 ELSE receipt_id:=NEW.id_documento_financeiro; END IF;
 IF EXISTS(SELECT 1 FROM documento_financeiro r
   JOIN linha_documento_financeiro l ON l.id_documento_financeiro=r.id
   JOIN pendente p ON p.id=l.id_pendente
   JOIN documento_comercial d ON d.id=p.id_documento_comercial
   JOIN tipodocumento t ON t.id=d.id_tipo_documento
   WHERE r.id=receipt_id AND r.data_valor IS NOT NULL
     AND (r.data_valor<d.data_emissao OR t.sinal_contabilistico<>1
          OR (NOT r.anulado AND (d.estado<>'EMITIDO' OR d.anulado))))
 THEN RAISE EXCEPTION 'Receipt value date or debt eligibility is invalid'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER receipt_value_date_document_check AFTER INSERT OR UPDATE ON documento_financeiro
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION receipt_value_date_check();
CREATE CONSTRAINT TRIGGER receipt_value_date_line_check AFTER INSERT OR UPDATE ON linha_documento_financeiro
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION receipt_value_date_check();

-- Serialize source chronology with line writers, not only Java callers.
CREATE FUNCTION receipt_value_date_source_lock() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM d.id FROM pendente p JOIN documento_comercial d ON d.id=p.id_documento_comercial
 WHERE p.id=NEW.id_pendente FOR UPDATE OF d;
 PERFORM t.id FROM pendente p JOIN documento_comercial d ON d.id=p.id_documento_comercial
 JOIN tipodocumento t ON t.id=d.id_tipo_documento
 WHERE p.id=NEW.id_pendente FOR SHARE OF t;
 RETURN NEW;
END $$;
CREATE TRIGGER receipt_value_date_source_lock BEFORE INSERT OR UPDATE ON linha_documento_financeiro
 FOR EACH ROW EXECUTE FUNCTION receipt_value_date_source_lock();
CREATE FUNCTION receipt_value_date_origin_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME='documento_comercial' THEN
   IF (NEW.data_emissao,NEW.id_tipo_documento) IS DISTINCT FROM (OLD.data_emissao,OLD.id_tipo_documento)
      AND EXISTS(SELECT 1 FROM pendente p JOIN linha_documento_financeiro l ON l.id_pendente=p.id
       JOIN documento_financeiro r ON r.id=l.id_documento_financeiro
       WHERE p.id_documento_comercial=OLD.id AND r.data_valor IS NOT NULL)
   THEN RAISE EXCEPTION 'Receipt debt chronology is immutable'; END IF;
 ELSE
   IF NEW.id_documento_comercial IS DISTINCT FROM OLD.id_documento_comercial
      AND EXISTS(SELECT 1 FROM linha_documento_financeiro l JOIN documento_financeiro r ON r.id=l.id_documento_financeiro
       WHERE l.id_pendente=OLD.id AND r.data_valor IS NOT NULL)
   THEN RAISE EXCEPTION 'Receipt debt origin is immutable'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER receipt_value_date_origin_guard BEFORE UPDATE ON documento_comercial
 FOR EACH ROW EXECUTE FUNCTION receipt_value_date_origin_guard();
CREATE TRIGGER receipt_value_date_pending_guard BEFORE UPDATE ON pendente
 FOR EACH ROW EXECUTE FUNCTION receipt_value_date_origin_guard();

CREATE FUNCTION receipt_value_date_type_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.sinal_contabilistico IS DISTINCT FROM OLD.sinal_contabilistico AND EXISTS(
   SELECT 1 FROM documento_comercial d JOIN pendente p ON p.id_documento_comercial=d.id
   JOIN linha_documento_financeiro l ON l.id_pendente=p.id
   JOIN documento_financeiro r ON r.id=l.id_documento_financeiro
   WHERE d.id_tipo_documento=OLD.id AND r.data_valor IS NOT NULL)
 THEN RAISE EXCEPTION 'Receipt debt nature is immutable'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER receipt_value_date_type_guard BEFORE UPDATE ON tipodocumento
 FOR EACH ROW EXECUTE FUNCTION receipt_value_date_type_guard();
