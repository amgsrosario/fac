-- No backfill or ownership inferred merely from an FT origin.
-- Immediate triggers serialize writers only; deferred checks classify completed receipts.
CREATE FUNCTION financial_circuit_check(pending_id bigint) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM linha_documento_financeiro l
   JOIN documento_financeiro d ON d.id=l.id_documento_financeiro
   LEFT JOIN financial_receipt r ON r.document_id=d.id
   WHERE l.id_pendente=pending_id AND NOT d.anulado AND r.document_id IS NULL)
 AND EXISTS(SELECT 1 FROM pendente p
   JOIN financial_document_origin o ON o.document_id=p.id_documento_comercial
   JOIN financial_allocation a ON a.position_id=o.position_id
   WHERE p.id=pending_id AND a.effect='CONSUME'
   AND NOT EXISTS(SELECT 1 FROM financial_allocation reversal WHERE reversal.reverse_of=a.id))
 THEN RAISE EXCEPTION 'Incompatible liquidation circuits for debt %',pending_id; END IF;
END $$;

CREATE FUNCTION financial_circuit_touch(pending_id bigint) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
 -- Keep values unchanged but produce a row version: stale repeatable-read writers
 -- must fail serialization instead of validating an old cross-circuit snapshot.
 UPDATE pendente SET id=id WHERE id=pending_id;
 IF NOT FOUND THEN RAISE EXCEPTION 'Shared documentary debt identity is required'; END IF;
END $$;
CREATE FUNCTION financial_circuit_identity_lock() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE debt_id bigint;
BEGIN
 IF TG_TABLE_NAME='linha_documento_financeiro' THEN
   IF TG_OP='INSERT' THEN
     PERFORM financial_circuit_touch(NEW.id_pendente);
   ELSE
     FOR debt_id IN SELECT id FROM pendente WHERE id IN (OLD.id_pendente,NEW.id_pendente) ORDER BY id
     LOOP PERFORM financial_circuit_touch(debt_id); END LOOP;
   END IF;
 ELSIF TG_TABLE_NAME='financial_allocation' THEN
   IF EXISTS(SELECT 1 FROM financial_document_origin WHERE position_id=NEW.position_id)
   AND NOT EXISTS(SELECT 1 FROM financial_document_origin o JOIN pendente p ON p.id_documento_comercial=o.document_id WHERE o.position_id=NEW.position_id)
   THEN RAISE EXCEPTION 'Shared documentary debt identity is required'; END IF;
   FOR debt_id IN SELECT p.id FROM pendente p JOIN financial_document_origin o ON o.document_id=p.id_documento_comercial
     WHERE o.position_id=NEW.position_id ORDER BY p.id
   LOOP PERFORM financial_circuit_touch(debt_id); END LOOP;
 ELSIF TG_TABLE_NAME='financial_document_origin' THEN
   IF NOT EXISTS(SELECT 1 FROM pendente WHERE id_documento_comercial=NEW.document_id)
   THEN RAISE EXCEPTION 'Shared documentary debt identity is required'; END IF;
   FOR debt_id IN SELECT id FROM pendente WHERE id_documento_comercial=NEW.document_id ORDER BY id
   LOOP PERFORM financial_circuit_touch(debt_id); END LOOP;
 ELSE
   FOR debt_id IN SELECT DISTINCT id_pendente FROM linha_documento_financeiro
     WHERE id_documento_financeiro=NEW.id ORDER BY id_pendente
   LOOP PERFORM financial_circuit_touch(debt_id); END LOOP;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_circuit_line_lock BEFORE INSERT OR UPDATE ON linha_documento_financeiro
 FOR EACH ROW EXECUTE FUNCTION financial_circuit_identity_lock();
CREATE TRIGGER financial_circuit_allocation_lock BEFORE INSERT ON financial_allocation
 FOR EACH ROW EXECUTE FUNCTION financial_circuit_identity_lock();
CREATE TRIGGER financial_circuit_origin_lock BEFORE INSERT ON financial_document_origin
 FOR EACH ROW EXECUTE FUNCTION financial_circuit_identity_lock();

CREATE TRIGGER financial_circuit_document_lock BEFORE UPDATE OF anulado ON documento_financeiro
 FOR EACH ROW WHEN (OLD.anulado IS DISTINCT FROM NEW.anulado) EXECUTE FUNCTION financial_circuit_identity_lock();

-- A recreated documentary identity must observe the current mapped position,
-- including when its transaction began before a previously committed consumption.
CREATE FUNCTION financial_circuit_pending_lock() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM p.id FROM financial_position p JOIN financial_document_origin o ON o.position_id=p.id
   WHERE o.document_id=NEW.id_documento_comercial ORDER BY p.id FOR UPDATE OF p;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_circuit_pending_insert_lock BEFORE INSERT ON pendente
 FOR EACH ROW EXECUTE FUNCTION financial_circuit_pending_lock();
CREATE TRIGGER financial_circuit_pending_move_lock BEFORE UPDATE OF id_documento_comercial ON pendente
 FOR EACH ROW WHEN (OLD.id_documento_comercial IS DISTINCT FROM NEW.id_documento_comercial)
 EXECUTE FUNCTION financial_circuit_pending_lock();

CREATE FUNCTION financial_circuit_validate() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE debt_id bigint;
BEGIN
 IF TG_TABLE_NAME='linha_documento_financeiro' THEN
   IF TG_OP='UPDATE' THEN PERFORM financial_circuit_check(OLD.id_pendente); END IF;
   PERFORM financial_circuit_check(NEW.id_pendente);
 ELSIF TG_TABLE_NAME='financial_allocation' THEN
   FOR debt_id IN SELECT p.id FROM pendente p JOIN financial_document_origin o
     ON o.document_id=p.id_documento_comercial WHERE o.position_id=NEW.position_id
   LOOP PERFORM financial_circuit_check(debt_id); END LOOP;
 ELSIF TG_TABLE_NAME='financial_document_origin' THEN
   FOR debt_id IN SELECT id FROM pendente WHERE id_documento_comercial=NEW.document_id
   LOOP PERFORM financial_circuit_check(debt_id); END LOOP;
 ELSIF TG_TABLE_NAME='pendente' THEN
   PERFORM financial_circuit_check(NEW.id);
 ELSE
   FOR debt_id IN SELECT DISTINCT id_pendente FROM linha_documento_financeiro
     WHERE id_documento_financeiro=NEW.id ORDER BY id_pendente
   LOOP
     PERFORM id FROM pendente WHERE id=debt_id FOR UPDATE;
     PERFORM financial_circuit_check(debt_id);
   END LOOP;
 END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER financial_circuit_line_check AFTER INSERT OR UPDATE ON linha_documento_financeiro
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_circuit_validate();
CREATE CONSTRAINT TRIGGER financial_circuit_allocation_check AFTER INSERT ON financial_allocation
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_circuit_validate();
CREATE CONSTRAINT TRIGGER financial_circuit_origin_check AFTER INSERT ON financial_document_origin
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_circuit_validate();
CREATE CONSTRAINT TRIGGER financial_circuit_pending_check AFTER INSERT OR UPDATE ON pendente
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_circuit_validate();
CREATE CONSTRAINT TRIGGER financial_circuit_document_check AFTER UPDATE ON documento_financeiro
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW WHEN (OLD.anulado IS DISTINCT FROM NEW.anulado) EXECUTE FUNCTION financial_circuit_validate();
