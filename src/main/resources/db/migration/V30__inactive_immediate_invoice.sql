-- Inactive FR/EUR adapter. No configuration, historical backfill or operational caller.
-- This is a documentary binding into V18, NOT a second cash ledger or fiscal receipt.
CREATE TABLE financial_immediate_invoice (
 document_id bigint PRIMARY KEY REFERENCES documento_comercial(id),
 position_id bigint NOT NULL UNIQUE REFERENCES financial_position(id),
 operation_id bigint NOT NULL UNIQUE,
 empresa_id bigint NOT NULL CHECK(empresa_id=1),
 cliente_id bigint NOT NULL,
 moeda_id varchar(3) NOT NULL CHECK(moeda_id='EUR'),
 request_key varchar(120) NOT NULL CHECK(length(trim(request_key))>0),
 received numeric(19,2) NOT NULL CHECK(received>0),
 payment_method varchar(3) NOT NULL REFERENCES mpagamento(id),
 payment_reference varchar(200) NOT NULL CHECK(length(trim(payment_reference))>0),
 actor varchar(20) NOT NULL REFERENCES utilizador(codigo),
 recalculation_confirmed boolean NOT NULL,
 UNIQUE(empresa_id,request_key),
 FOREIGN KEY(operation_id,empresa_id,cliente_id,moeda_id)
   REFERENCES financial_operation(id,empresa_id,cliente_id,moeda_id)
);

CREATE FUNCTION financial_immediate_invoice_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS (
   SELECT 1 FROM documento_comercial d JOIN tipodocumento t ON t.id=d.id_tipo_documento
   JOIN financial_document_origin o ON o.document_id=d.id AND o.position_id=NEW.position_id
   JOIN financial_position p ON p.id=o.position_id
   JOIN financial_operation creation ON creation.id=p.creation_id
   JOIN financial_operation settlement ON settlement.id=NEW.operation_id
   WHERE d.id=NEW.document_id AND d.estado='EMITIDO' AND NOT d.anulado AND d.liquidado
     AND t.codigo_fiscal='FT' AND t.liquidacao_imediata
     AND d.id_moeda='EUR' AND d.id_cliente=NEW.cliente_id
     AND d.id_emissor=NEW.actor AND d.id_mpagamento=NEW.payment_method
     AND d.momento_emissao IS NOT NULL AND d.data_emissao IS NOT NULL
     AND d.xmin::text::bigint=(txid_current()%4294967296)
     AND d.valor_total=NEW.received AND p.original=NEW.received AND p.available=0 AND NOT p.cancelled
     AND creation.kind='CREATE' AND creation.created_tx=txid_current()
     AND settlement.kind='SETTLE' AND settlement.created_tx=txid_current()
     AND (p.empresa_id,p.cliente_id,p.moeda_id)=(NEW.empresa_id,NEW.cliente_id,NEW.moeda_id)
     AND creation.actor=NEW.actor AND settlement.actor=NEW.actor
 ) THEN RAISE EXCEPTION 'Invalid immediate invoice binding'; END IF;
 IF (SELECT count(*) FROM financial_allocation WHERE operation_id=NEW.operation_id)<>1
 OR NOT EXISTS(SELECT 1 FROM financial_allocation WHERE operation_id=NEW.operation_id
     AND position_id=NEW.position_id AND amount=NEW.received AND effect='CONSUME')
 OR (SELECT count(*) FROM financial_component WHERE operation_id=NEW.operation_id)<>1
 OR NOT EXISTS(SELECT 1 FROM financial_component WHERE operation_id=NEW.operation_id
     AND kind='CASH_IN' AND amount=NEW.received AND NOT reversed)
 OR EXISTS(SELECT 1 FROM financial_receipt WHERE operation_id=NEW.operation_id)
 OR (SELECT count(*) FROM financial_allocation WHERE position_id=NEW.position_id)<>1
 OR EXISTS(SELECT 1 FROM financial_operation op JOIN financial_position p ON p.id=NEW.position_id
     WHERE op.reverse_of IN(NEW.operation_id,p.creation_id))
 OR EXISTS(SELECT 1 FROM pendente WHERE id_documento_comercial=NEW.document_id)
 THEN RAISE EXCEPTION 'Immediate invoice requires exactly one full cash settlement without RC or pending'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_immediate_invoice_guard BEFORE INSERT ON financial_immediate_invoice
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_guard();
CREATE TRIGGER financial_immediate_invoice_immutable BEFORE UPDATE OR DELETE ON financial_immediate_invoice
 FOR EACH ROW EXECUTE FUNCTION financial_immutable();
CREATE TRIGGER financial_immediate_invoice_no_truncate BEFORE TRUNCATE ON financial_immediate_invoice
 FOR EACH STATEMENT EXECUTE FUNCTION financial_immutable();

-- No reversals are implicitly authorised by the issuance contract. A future FR
-- cancellation requires its own atomic documentary/financial act and temporal contract.
CREATE FUNCTION financial_immediate_invoice_operation_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.kind='REVERSE' AND EXISTS(SELECT 1 FROM financial_immediate_invoice f
   JOIN financial_position p ON p.id=f.position_id
   WHERE NEW.reverse_of IN(f.operation_id,p.creation_id))
 THEN RAISE EXCEPTION 'Immediate invoice reversal requires an approved documentary act'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_immediate_invoice_operation_guard BEFORE INSERT ON financial_operation
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_operation_guard();

CREATE FUNCTION financial_immediate_invoice_entry_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE operation_id=NEW.operation_id)
 THEN RAISE EXCEPTION 'Immediate invoice settlement is sealed'; END IF;
 IF TG_TABLE_NAME='financial_allocation' THEN
   IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE position_id=NEW.position_id)
   THEN RAISE EXCEPTION 'Immediate invoice position is sealed'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_immediate_invoice_allocation_guard BEFORE INSERT ON financial_allocation
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_entry_guard();
CREATE TRIGGER financial_immediate_invoice_component_guard BEFORE INSERT ON financial_component
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_entry_guard();
CREATE TRIGGER financial_immediate_invoice_receipt_guard BEFORE INSERT ON financial_receipt
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_entry_guard();

CREATE FUNCTION financial_immediate_invoice_document_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE document_id=OLD.id) THEN
   IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Immediate invoice document is immutable'; END IF;
   -- Reprinting may mark the document printed; monetary, fiscal and quittance identity stay fixed.
   IF (to_jsonb(NEW)-'impresso') IS DISTINCT FROM (to_jsonb(OLD)-'impresso')
   THEN RAISE EXCEPTION 'Immediate invoice identity is immutable'; END IF;
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_immediate_invoice_document_guard BEFORE UPDATE OR DELETE ON documento_comercial
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_document_guard();
CREATE FUNCTION financial_immediate_invoice_position_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE position_id=OLD.id)
 AND NEW IS DISTINCT FROM OLD THEN RAISE EXCEPTION 'Immediate invoice position is immutable'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_immediate_invoice_position_guard BEFORE UPDATE ON financial_position
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_position_guard();
CREATE FUNCTION financial_immediate_invoice_pending_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_immediate_invoice WHERE document_id=NEW.id_documento_comercial)
 THEN RAISE EXCEPTION 'Immediate invoice cannot acquire a legacy pending'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_immediate_invoice_pending_guard BEFORE INSERT OR UPDATE ON pendente
 FOR EACH ROW EXECUTE FUNCTION financial_immediate_invoice_pending_guard();

-- One financial effect date: the single invoice/quittance's issue date. The
-- technical operation instant is distinct. Existing RC and unknown generic rules stay unchanged.
CREATE OR REPLACE VIEW financial_operation_effect AS
 SELECT op.id,
   CASE WHEN op.kind='CREATE' THEN origin.data_emissao
        WHEN op.kind='SETTLE' AND fr.document_id IS NOT NULL THEN immediate.data_emissao
        WHEN op.kind='SETTLE' THEN coalesce(receipt.data_valor,receipt.data_emissao)
        WHEN op.kind='REVERSE' AND act.operation_id IS NOT NULL THEN original.data_valor
   END AS effect_date,
   CASE WHEN op.kind='SETTLE' AND fr.document_id IS NOT NULL THEN immediate.momento_emissao
        WHEN op.kind='SETTLE' AND r.document_id IS NOT NULL THEN receipt.momento_emissao
        WHEN op.kind='REVERSE' AND act.operation_id IS NOT NULL THEN act.occurred_at
        ELSE op.created_at END AS operation_at
 FROM financial_operation op
 LEFT JOIN financial_position p ON p.creation_id=op.id
 LEFT JOIN financial_document_origin link ON link.position_id=p.id
 LEFT JOIN documento_comercial origin ON origin.id=link.document_id
 LEFT JOIN financial_receipt r ON r.operation_id=op.id
 LEFT JOIN documento_financeiro receipt ON receipt.id=r.document_id
 LEFT JOIN financial_receipt_reversal act ON act.operation_id=op.id
 LEFT JOIN documento_financeiro original ON original.id=act.document_id
 LEFT JOIN financial_immediate_invoice fr ON fr.operation_id=op.id
 LEFT JOIN documento_comercial immediate ON immediate.id=fr.document_id;
