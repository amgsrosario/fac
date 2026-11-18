-- Inactive FR correction. Original operations and fiscal identity remain immutable.

CREATE OR REPLACE FUNCTION financial_immediate_invoice_operation_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.kind='REVERSE' AND EXISTS(SELECT 1 FROM financial_immediate_invoice f
   JOIN financial_position p ON p.id=f.position_id
   WHERE NEW.reverse_of IN(f.operation_id,p.creation_id)
   AND NOT EXISTS (SELECT 1 FROM documento_comercial d WHERE d.id=f.document_id
     AND d.projection_external_type='FR' AND d.estado='EMITIDO'
     AND length(btrim(NEW.reason)) BETWEEN 5 AND 500
     AND ((NEW.reverse_of=f.operation_id AND NEW.request_key='FR_VOID_RECEIPT:'||d.id)
       OR (NEW.reverse_of=p.creation_id AND NEW.request_key='FR_VOID_INVOICE:'||d.id
         AND EXISTS (SELECT 1 FROM financial_operation r WHERE r.reverse_of=f.operation_id
           AND r.request_key='FR_VOID_RECEIPT:'||d.id)))))
 THEN RAISE EXCEPTION 'Immediate invoice reversal requires an approved documentary act'; END IF;
 RETURN NEW;
END $$;

CREATE OR REPLACE FUNCTION financial_check_operation() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE op financial_operation; original_op financial_operation; debt numeric; credit numeric; cash_in numeric; cash_out numeric; discount numeric;
BEGIN
 IF TG_TABLE_NAME='financial_operation' THEN op:=NEW;
 ELSE SELECT * INTO op FROM financial_operation WHERE id=NEW.operation_id; END IF;
 IF op.kind='SETTLE' THEN
 IF EXISTS(SELECT 1 FROM financial_allocation WHERE operation_id=op.id AND effect<>'CONSUME') OR EXISTS(SELECT 1 FROM financial_component WHERE operation_id=op.id AND reversed) THEN RAISE EXCEPTION 'Invalid settlement effects'; END IF;
 SELECT coalesce(sum(a.amount) FILTER (WHERE p.nature='DEBT'),0),coalesce(sum(a.amount) FILTER (WHERE p.nature='CREDIT'),0)
 INTO debt,credit FROM financial_allocation a JOIN financial_position p ON p.id=a.position_id WHERE a.operation_id=op.id;
 SELECT coalesce(sum(amount) FILTER (WHERE kind='CASH_IN'),0),coalesce(sum(amount) FILTER (WHERE kind='CASH_OUT'),0),coalesce(sum(amount) FILTER (WHERE kind='DISCOUNT'),0)
 INTO cash_in,cash_out,discount FROM financial_component WHERE operation_id=op.id;
 IF debt+cash_out<>credit+cash_in+discount OR discount>debt OR NOT EXISTS(SELECT 1 FROM financial_allocation WHERE operation_id=op.id)
 THEN RAISE EXCEPTION 'Unbalanced financial operation'; END IF;
 END IF;
 IF op.kind='CREATE' AND (NOT EXISTS(SELECT 1 FROM financial_position WHERE creation_id=op.id) OR EXISTS(SELECT 1 FROM financial_allocation WHERE operation_id=op.id) OR EXISTS(SELECT 1 FROM financial_component WHERE operation_id=op.id))
 THEN RAISE EXCEPTION 'Creation without position'; END IF;
 IF op.kind='REVERSE' THEN
 SELECT * INTO original_op FROM financial_operation WHERE id=op.reverse_of;
 IF original_op.kind='REVERSE' OR (op.empresa_id,op.cliente_id,op.moeda_id) IS DISTINCT FROM (original_op.empresa_id,original_op.cliente_id,original_op.moeda_id)
 THEN RAISE EXCEPTION 'Invalid reversal scope'; END IF;
 IF original_op.kind='CREATE' THEN
 IF NOT EXISTS(SELECT 1 FROM financial_position WHERE creation_id=original_op.id AND cancelled)
 OR EXISTS(SELECT 1 FROM financial_allocation WHERE operation_id=op.id)
 OR EXISTS(SELECT 1 FROM financial_component WHERE operation_id=op.id)
 THEN RAISE EXCEPTION 'Invalid creation reversal'; END IF;
 ELSE
 IF EXISTS(SELECT 1 FROM financial_allocation a WHERE a.operation_id=original_op.id AND NOT EXISTS(SELECT 1 FROM financial_allocation b WHERE b.operation_id=op.id AND b.reverse_of=a.id AND b.position_id=a.position_id AND b.amount=a.amount AND b.effect='RESTORE'))
 OR EXISTS(SELECT 1 FROM financial_allocation b WHERE b.operation_id=op.id AND NOT EXISTS(SELECT 1 FROM financial_allocation a WHERE a.id=b.reverse_of AND a.operation_id=original_op.id AND a.amount=b.amount AND a.position_id=b.position_id AND b.effect='RESTORE'))
 THEN RAISE EXCEPTION 'Invalid allocation reversal'; END IF;
 IF EXISTS (SELECT 1 FROM financial_immediate_invoice f WHERE f.operation_id=op.reverse_of
   AND op.request_key='FR_VOID_RECEIPT:'||f.document_id) THEN
  IF EXISTS (SELECT 1 FROM financial_component WHERE operation_id=op.id)
  THEN RAISE EXCEPTION 'FR receipt correction cannot create a cash movement'; END IF;
 ELSIF EXISTS(SELECT 1 FROM financial_component a WHERE a.operation_id=original_op.id AND NOT EXISTS(SELECT 1 FROM financial_component b WHERE b.operation_id=op.id AND b.reverse_of=a.id AND b.amount=a.amount AND b.reversed AND b.kind=CASE a.kind WHEN 'CASH_IN' THEN 'CASH_OUT' WHEN 'CASH_OUT' THEN 'CASH_IN' ELSE 'DISCOUNT' END))
 OR EXISTS(SELECT 1 FROM financial_component b WHERE b.operation_id=op.id AND NOT EXISTS(SELECT 1 FROM financial_component a WHERE a.id=b.reverse_of AND a.operation_id=original_op.id AND a.amount=b.amount AND b.reversed AND b.kind=CASE a.kind WHEN 'CASH_IN' THEN 'CASH_OUT' WHEN 'CASH_OUT' THEN 'CASH_IN' ELSE 'DISCOUNT' END))
 THEN RAISE EXCEPTION 'Invalid monetary reversal'; END IF;
 END IF;
 END IF;
 RETURN NULL;
END $$;

CREATE OR REPLACE FUNCTION financial_immediate_invoice_entry_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE operation_id=NEW.operation_id)
 THEN RAISE EXCEPTION 'Immediate invoice settlement is sealed'; END IF;
 IF TG_TABLE_NAME='financial_allocation' THEN
   IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE position_id=NEW.position_id
      AND NOT (NEW.effect='RESTORE' AND EXISTS (
        SELECT 1 FROM financial_operation r WHERE r.id=NEW.operation_id
          AND r.reverse_of=financial_immediate_invoice.operation_id
          AND r.request_key='FR_VOID_RECEIPT:'||financial_immediate_invoice.document_id)))
   THEN RAISE EXCEPTION 'Immediate invoice position is sealed'; END IF;
 END IF;
 RETURN NEW;
END $$;

CREATE OR REPLACE FUNCTION financial_immediate_invoice_position_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE position_id=OLD.id)
 AND NEW IS DISTINCT FROM OLD
 AND NOT EXISTS (SELECT 1 FROM financial_immediate_invoice f JOIN financial_operation r
   ON r.request_key=CASE WHEN NEW.cancelled THEN 'FR_VOID_INVOICE:' ELSE 'FR_VOID_RECEIPT:' END||f.document_id
   AND r.reverse_of=CASE WHEN NEW.cancelled THEN OLD.creation_id ELSE f.operation_id END
   AND r.created_tx=txid_current()
   WHERE f.position_id=OLD.id AND
     (to_jsonb(NEW)-ARRAY['available','cancelled'])=(to_jsonb(OLD)-ARRAY['available','cancelled'])) THEN RAISE EXCEPTION 'Immediate invoice position is immutable'; END IF;
 RETURN NEW;
END $$;

CREATE OR REPLACE FUNCTION financial_immediate_invoice_document_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE document_id=OLD.id) THEN
   IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Immediate invoice document is immutable'; END IF;
   IF OLD.projection_external_type='FR' AND OLD.estado='EMITIDO' AND NEW.estado='ANULADO'
     AND NEW.anulado AND EXISTS (
       SELECT 1 FROM financial_immediate_invoice f JOIN financial_position p ON p.id=f.position_id
       JOIN financial_operation r ON r.reverse_of=p.creation_id AND r.request_key='FR_VOID_INVOICE:'||f.document_id
       WHERE f.document_id=OLD.id AND r.created_tx=txid_current() AND p.cancelled
         AND r.actor=NEW.anulado_por_utilizador_id AND r.reason=NEW.motivo_anulacao
         AND r.created_at=NEW.data_hora_anulacao)
     AND (to_jsonb(NEW)-ARRAY['estado','anulado','motivo_anulacao','data_hora_anulacao','anulado_por_utilizador_id','anulado_por_nome'])
       =(to_jsonb(OLD)-ARRAY['estado','anulado','motivo_anulacao','data_hora_anulacao','anulado_por_utilizador_id','anulado_por_nome'])
   THEN RETURN NEW; END IF;
   -- Reprinting may mark the document printed; monetary, fiscal and quittance identity stay fixed.
   IF (to_jsonb(NEW)-'impresso') IS DISTINCT FROM (to_jsonb(OLD)-'impresso')
   THEN RAISE EXCEPTION 'Immediate invoice identity is immutable'; END IF;
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 RETURN NEW;
END $$;


CREATE FUNCTION financial_fr_void_document_complete() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.kind='REVERSE' AND EXISTS (
  SELECT 1 FROM financial_immediate_invoice f JOIN financial_position p ON p.id=f.position_id
  JOIN documento_comercial d ON d.id=f.document_id
  WHERE NEW.reverse_of=p.creation_id AND d.estado<>'ANULADO')
 THEN RAISE EXCEPTION 'FR invoice reversal requires documentary cancellation'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER financial_fr_void_document_complete AFTER INSERT ON financial_operation
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_fr_void_document_complete();


CREATE OR REPLACE FUNCTION financial_circuit_identity_lock() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE debt_id bigint; immediate_document bigint; immediate_position bigint;
BEGIN
 -- Only the new explicit immediate act can replace the V22 shared pending identity.
 -- Its completion is mandatory at commit (see deferred checks below).
 IF TG_TABLE_NAME='financial_document_origin' THEN
   immediate_document:=NEW.document_id; immediate_position:=NEW.position_id;
 ELSIF TG_TABLE_NAME='financial_allocation' THEN
   SELECT document_id INTO immediate_document FROM financial_document_origin WHERE position_id=NEW.position_id;
   immediate_position:=NEW.position_id;
 END IF;
 IF EXISTS(SELECT 1 FROM documento_comercial d JOIN tipodocumento t ON t.id=d.id_tipo_documento
           WHERE d.id=immediate_document AND t.codigo_fiscal='FT' AND t.liquidacao_imediata) THEN
   PERFORM id FROM documento_comercial WHERE id=immediate_document FOR UPDATE;
   PERFORM id FROM financial_position WHERE id=immediate_position FOR UPDATE;
   IF TG_TABLE_NAME='financial_allocation' THEN
     IF NEW.effect='RESTORE' AND EXISTS (
       SELECT 1 FROM financial_immediate_invoice f JOIN financial_operation r
         ON r.id=NEW.operation_id AND r.reverse_of=f.operation_id
       WHERE f.position_id=immediate_position AND f.document_id=immediate_document
         AND r.request_key='FR_VOID_RECEIPT:'||f.document_id)
     THEN RETURN NEW; END IF;
   END IF;
   IF NOT EXISTS(SELECT 1 FROM financial_position p JOIN financial_operation op ON op.id=p.creation_id
                 WHERE p.id=immediate_position AND op.created_tx=txid_current())
   THEN RAISE EXCEPTION 'Immediate invoice must be originated and settled atomically'; END IF;
   UPDATE financial_position SET available=available WHERE id=immediate_position;
   RETURN NEW;
 END IF;
 IF TG_TABLE_NAME='linha_documento_financeiro' THEN
   IF TG_OP='INSERT' THEN
     PERFORM financial_circuit_touch(NEW.id_pendente);
   ELSE
     FOR debt_id IN SELECT id FROM pendente WHERE id IN (OLD.id_pendente,NEW.id_pendente) ORDER BY id
     LOOP PERFORM financial_circuit_touch(debt_id); END LOOP;
   END IF;
 ELSIF TG_TABLE_NAME='financial_allocation' THEN
   -- Serialize discovery against a concurrent documentary association.
   PERFORM id FROM financial_position WHERE id=NEW.position_id FOR UPDATE;
   IF EXISTS(SELECT 1 FROM financial_document_origin WHERE position_id=NEW.position_id)
   AND NOT EXISTS(SELECT 1 FROM financial_document_origin o JOIN pendente p ON p.id_documento_comercial=o.document_id WHERE o.position_id=NEW.position_id)
   THEN RAISE EXCEPTION 'Shared documentary debt identity is required'; END IF;
   FOR debt_id IN SELECT p.id FROM pendente p JOIN financial_document_origin o ON o.document_id=p.id_documento_comercial
     WHERE o.position_id=NEW.position_id ORDER BY p.id
   LOOP PERFORM financial_circuit_touch(debt_id); END LOOP;
 ELSIF TG_TABLE_NAME='financial_document_origin' THEN
   PERFORM id FROM financial_position WHERE id=NEW.position_id FOR UPDATE;
   -- A row lock alone leaves an old RR snapshot usable after an origin-first
   -- commit. Publish a row version without changing the financial amount.
   UPDATE financial_position SET available=available WHERE id=NEW.position_id;
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
