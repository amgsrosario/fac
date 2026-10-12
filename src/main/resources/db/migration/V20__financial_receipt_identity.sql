-- Internal/inactive receipt adapter. No legacy documents or balances are imported.
CREATE TABLE financial_receipt (
 document_id bigint PRIMARY KEY REFERENCES documento_financeiro(id),
 operation_id bigint NOT NULL UNIQUE,
 empresa_id bigint NOT NULL,
 cliente_id bigint NOT NULL,
 moeda_id varchar(3) NOT NULL,
 request_key varchar(120) NOT NULL CHECK(length(trim(request_key))>0),
 request_payload text NOT NULL,
 UNIQUE(empresa_id,request_key),
 FOREIGN KEY(operation_id,empresa_id,cliente_id,moeda_id)
   REFERENCES financial_operation(id,empresa_id,cliente_id,moeda_id)
);
CREATE TABLE financial_receipt_line (
 line_id bigint PRIMARY KEY REFERENCES linha_documento_financeiro(id),
 allocation_id bigint NOT NULL UNIQUE REFERENCES financial_allocation(id)
);
CREATE TRIGGER financial_receipt_immutable BEFORE UPDATE OR DELETE ON financial_receipt
 FOR EACH ROW EXECUTE FUNCTION financial_immutable();
CREATE TRIGGER financial_receipt_line_immutable BEFORE UPDATE OR DELETE ON financial_receipt_line
 FOR EACH ROW EXECUTE FUNCTION financial_immutable();

CREATE FUNCTION financial_receipt_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS (
   SELECT 1 FROM financial_operation op JOIN documento_financeiro d ON d.id=NEW.document_id
   JOIN tipodocumento t ON t.id=d.id_tipo_documento
   WHERE op.id=NEW.operation_id AND op.kind='SETTLE' AND op.created_tx=txid_current()
     AND NEW.empresa_id=1 AND op.cliente_id=d.id_cliente AND op.moeda_id=d.id_moeda
     AND op.actor=d.id_emissor AND t.codigo_fiscal='RC' AND NOT d.anulado
     AND d.id_moeda='EUR' AND d.valor_desconto_financeiro=0
     AND d.valor_pagamento_bruto=d.valor_pagamento_liquido
     AND d.valor_pagamento_bruto>0 AND d.valor_pagamento_bruto=round(d.valor_pagamento_bruto,2)
 ) THEN RAISE EXCEPTION 'Invalid financial receipt association'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_guard BEFORE INSERT ON financial_receipt
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_guard();

CREATE FUNCTION financial_receipt_line_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS (
   SELECT 1 FROM linha_documento_financeiro l JOIN financial_receipt r ON r.document_id=l.id_documento_financeiro
   JOIN financial_operation op ON op.id=r.operation_id
   JOIN financial_allocation a ON a.id=NEW.allocation_id AND a.operation_id=r.operation_id
   JOIN financial_position p ON p.id=a.position_id
   JOIN financial_document_origin o ON o.position_id=p.id
   JOIN pendente legacy ON legacy.id=l.id_pendente AND legacy.id_documento_comercial=o.document_id
   WHERE l.id=NEW.line_id AND op.created_tx=txid_current() AND p.nature='DEBT'
     AND a.effect='CONSUME' AND a.amount=l.valor_pagamento_bruto
     AND l.valor_desconto_financeiro=0 AND coalesce(l.desconto_percentual,0)=0 AND l.valor_pagamento_liquido=a.amount
     AND l.valor_pendente_antes-l.novo_valor_pendente=a.amount
     AND l.valor_pendente_antes>=a.amount AND l.valor_pendente_antes<=p.original
     AND l.novo_valor_pendente=p.available AND l.novo_valor_pendente>=0
     AND l.valor_documento=p.original AND l.id_moeda=p.moeda_id
 ) THEN RAISE EXCEPTION 'Invalid receipt allocation trace'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_line_guard BEFORE INSERT ON financial_receipt_line
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_line_guard();

CREATE FUNCTION financial_receipt_check() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE doc_id bigint; receipt financial_receipt; gross numeric; allocated numeric; cash numeric;
BEGIN
 IF TG_TABLE_NAME='financial_receipt' THEN doc_id:=NEW.document_id;
 ELSE SELECT id_documento_financeiro INTO doc_id FROM linha_documento_financeiro WHERE id=NEW.line_id; END IF;
 SELECT * INTO receipt FROM financial_receipt WHERE document_id=doc_id;
 SELECT valor_pagamento_bruto INTO gross FROM documento_financeiro WHERE id=doc_id;
 SELECT coalesce(sum(amount),0) INTO allocated FROM financial_allocation WHERE operation_id=receipt.operation_id;
 SELECT coalesce(sum(amount) FILTER(WHERE kind='CASH_IN'),0) INTO cash FROM financial_component WHERE operation_id=receipt.operation_id;
 IF gross<>allocated OR gross<>cash
 OR EXISTS(SELECT 1 FROM financial_component WHERE operation_id=receipt.operation_id AND kind<>'CASH_IN')
 OR EXISTS(SELECT 1 FROM linha_documento_financeiro l LEFT JOIN financial_receipt_line link ON link.line_id=l.id
           WHERE l.id_documento_financeiro=doc_id AND link.line_id IS NULL)
 OR EXISTS(SELECT 1 FROM financial_allocation a LEFT JOIN financial_receipt_line link ON link.allocation_id=a.id
           WHERE a.operation_id=receipt.operation_id AND link.line_id IS NULL)
 THEN RAISE EXCEPTION 'Incomplete financial receipt'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER financial_receipt_check AFTER INSERT ON financial_receipt
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_receipt_check();
CREATE CONSTRAINT TRIGGER financial_receipt_line_check AFTER INSERT ON financial_receipt_line
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_receipt_check();

-- A legacy cancellation must never restore legacy snapshots for an internally linked receipt.
-- Effect semantics remain reserved: linked receipts cannot be cancelled operationally in B1.
CREATE FUNCTION financial_receipt_document_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_receipt WHERE document_id=OLD.id) THEN
   IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Linked financial receipt is immutable'; END IF;
   IF (NEW.id_cliente,NEW.id_tipo_documento,NEW.serie,NEW.numero_documento,NEW.data_emissao,
       NEW.id_moeda,NEW.valor_pagamento_bruto,NEW.valor_desconto_financeiro,NEW.valor_pagamento_liquido,
       NEW.id_mpagamento,NEW.id_emissor,NEW.momento_emissao,NEW.data_hora_operacao,NEW.anulado,NEW.observacoes,
       NEW.atcud,NEW.codigo_validacao_at,NEW.qr_payload,NEW.qr_payload_version)
     IS DISTINCT FROM
      (OLD.id_cliente,OLD.id_tipo_documento,OLD.serie,OLD.numero_documento,OLD.data_emissao,
       OLD.id_moeda,OLD.valor_pagamento_bruto,OLD.valor_desconto_financeiro,OLD.valor_pagamento_liquido,
       OLD.id_mpagamento,OLD.id_emissor,OLD.momento_emissao,OLD.data_hora_operacao,OLD.anulado,OLD.observacoes,
       OLD.atcud,OLD.codigo_validacao_at,OLD.qr_payload,OLD.qr_payload_version)
   THEN RAISE EXCEPTION 'Linked receipt changes require an approved documentary reversal'; END IF;
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_document_guard BEFORE UPDATE OR DELETE ON documento_financeiro
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_document_guard();
CREATE FUNCTION financial_receipt_document_line_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE doc_id bigint;
BEGIN
 IF TG_OP='INSERT' THEN doc_id:=NEW.id_documento_financeiro; ELSE doc_id:=OLD.id_documento_financeiro; END IF;
 IF EXISTS(SELECT 1 FROM financial_receipt WHERE document_id=doc_id)
 THEN RAISE EXCEPTION 'Linked receipt lines are immutable'; END IF;
 IF TG_OP='UPDATE' AND EXISTS(SELECT 1 FROM financial_receipt WHERE document_id=NEW.id_documento_financeiro)
 THEN RAISE EXCEPTION 'Cannot move lines into a linked receipt'; END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_document_line_guard BEFORE INSERT OR UPDATE OR DELETE ON linha_documento_financeiro
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_document_line_guard();

CREATE FUNCTION financial_receipt_reversal_gate() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.kind='REVERSE' AND EXISTS(SELECT 1 FROM financial_receipt WHERE operation_id=NEW.reverse_of)
 THEN RAISE EXCEPTION 'Receipt reversal requires approved documentary effect semantics (B2)'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_reversal_gate BEFORE INSERT ON financial_operation
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_reversal_gate();
