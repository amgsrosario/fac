-- Internal/inactive F2/T1. No backfill, second value date, or operational cutover.
CREATE TABLE financial_receipt_reversal (
 document_id bigint PRIMARY KEY REFERENCES financial_receipt(document_id),
 operation_id bigint NOT NULL UNIQUE REFERENCES financial_operation(id),
 replacement_document_id bigint UNIQUE REFERENCES financial_receipt(document_id),
 money_treatment varchar(24) NOT NULL CHECK(money_treatment IN ('NOT_RECEIVED','REPLACEMENT')),
 request_payload text NOT NULL,
 evidence varchar(500) NOT NULL CHECK(length(trim(evidence))>0),
 actor varchar(100) NOT NULL REFERENCES utilizador(codigo),
 occurred_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 CHECK ((money_treatment='REPLACEMENT')=(replacement_document_id IS NOT NULL)),
 CHECK(replacement_document_id IS NULL OR replacement_document_id<>document_id)
);
CREATE TRIGGER financial_receipt_reversal_immutable BEFORE UPDATE OR DELETE ON financial_receipt_reversal
 FOR EACH ROW EXECUTE FUNCTION financial_immutable();

CREATE FUNCTION financial_receipt_reversal_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS(
   SELECT 1 FROM financial_receipt r JOIN documento_financeiro d ON d.id=r.document_id
   JOIN financial_operation op ON op.id=NEW.operation_id
   WHERE r.document_id=NEW.document_id AND NOT d.anulado AND d.data_valor IS NOT NULL
     AND op.kind='REVERSE' AND op.reverse_of=r.operation_id AND op.created_tx=txid_current()
     AND (op.empresa_id,op.cliente_id,op.moeda_id)=(r.empresa_id,r.cliente_id,r.moeda_id)
     AND op.actor=NEW.actor
 ) THEN RAISE EXCEPTION 'Invalid receipt reversal act'; END IF;
 IF NEW.replacement_document_id IS NOT NULL AND NOT EXISTS(
   SELECT 1 FROM financial_receipt original JOIN financial_receipt replacement
     ON (original.empresa_id,original.cliente_id,original.moeda_id)=(replacement.empresa_id,replacement.cliente_id,replacement.moeda_id)
   JOIN financial_operation op ON op.id=replacement.operation_id
   JOIN documento_financeiro d ON d.id=replacement.document_id
   WHERE original.document_id=NEW.document_id AND replacement.document_id=NEW.replacement_document_id
     AND op.created_tx=txid_current() AND d.data_valor IS NOT NULL AND NOT d.anulado
 ) THEN RAISE EXCEPTION 'Invalid atomic receipt replacement'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_receipt_reversal_guard BEFORE INSERT ON financial_receipt_reversal
 FOR EACH ROW EXECUTE FUNCTION financial_receipt_reversal_guard();

-- The old immediate prohibition becomes a deferred requirement for a complete act.
-- Standalone core.reverse or direct SQL cannot commit an unlinked RC reversal.
CREATE OR REPLACE FUNCTION financial_receipt_reversal_gate() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.kind='REVERSE' AND EXISTS(SELECT 1 FROM financial_receipt r JOIN documento_financeiro d ON d.id=r.document_id
      WHERE r.operation_id=NEW.reverse_of AND d.data_valor IS NULL)
 THEN RAISE EXCEPTION 'Receipt reversal requires a recorded value date; no historical inference'; END IF;
 RETURN NEW;
END $$;
CREATE FUNCTION financial_receipt_reversal_check() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE receipt financial_receipt; doc_id bigint;
BEGIN
 IF TG_TABLE_NAME='financial_operation' THEN
   SELECT document_id INTO doc_id FROM financial_receipt WHERE operation_id=NEW.reverse_of;
 ELSIF TG_TABLE_NAME='documento_financeiro' THEN doc_id:=NEW.id;
 ELSE doc_id:=NEW.document_id; END IF;
 IF TG_TABLE_NAME='financial_receipt_reversal' THEN
   IF NEW.replacement_document_id IS NOT NULL AND EXISTS(SELECT 1 FROM documento_financeiro
        WHERE id=NEW.replacement_document_id AND anulado)
   THEN RAISE EXCEPTION 'Atomic replacement must leave an active substitute'; END IF;
 END IF;
 SELECT * INTO receipt FROM financial_receipt WHERE document_id=doc_id;
 IF NOT FOUND THEN RETURN NULL; END IF;
 IF EXISTS(SELECT 1 FROM financial_operation op WHERE op.reverse_of=receipt.operation_id)
    IS DISTINCT FROM EXISTS(SELECT 1 FROM financial_receipt_reversal a JOIN financial_operation op
      ON op.id=a.operation_id AND op.reverse_of=receipt.operation_id WHERE a.document_id=doc_id)
 OR (SELECT anulado FROM documento_financeiro WHERE id=doc_id)
    IS DISTINCT FROM EXISTS(SELECT 1 FROM financial_receipt_reversal WHERE document_id=doc_id)
 THEN RAISE EXCEPTION 'Receipt reversal requires atomic act, document and ledger'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER financial_receipt_reverse_operation_check AFTER INSERT ON financial_operation
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW WHEN (NEW.kind='REVERSE') EXECUTE FUNCTION financial_receipt_reversal_check();
CREATE CONSTRAINT TRIGGER financial_receipt_reverse_link_check AFTER INSERT ON financial_receipt
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_receipt_reversal_check();
CREATE CONSTRAINT TRIGGER financial_receipt_reverse_act_check AFTER INSERT ON financial_receipt_reversal
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION financial_receipt_reversal_check();
CREATE CONSTRAINT TRIGGER financial_receipt_reverse_document_check AFTER UPDATE ON documento_financeiro
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW WHEN (OLD.anulado IS DISTINCT FROM NEW.anulado) EXECUTE FUNCTION financial_receipt_reversal_check();

CREATE OR REPLACE FUNCTION financial_receipt_document_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_receipt WHERE document_id=OLD.id) THEN
   IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Linked financial receipt is immutable'; END IF;
   IF (NEW.id_cliente,NEW.id_tipo_documento,NEW.serie,NEW.numero_documento,NEW.data_emissao,
       NEW.id_moeda,NEW.valor_pagamento_bruto,NEW.valor_desconto_financeiro,NEW.valor_pagamento_liquido,
       NEW.id_mpagamento,NEW.id_emissor,NEW.momento_emissao,NEW.data_hora_operacao,NEW.observacoes,
       NEW.atcud,NEW.codigo_validacao_at,NEW.qr_payload,NEW.qr_payload_version)
     IS DISTINCT FROM
      (OLD.id_cliente,OLD.id_tipo_documento,OLD.serie,OLD.numero_documento,OLD.data_emissao,
       OLD.id_moeda,OLD.valor_pagamento_bruto,OLD.valor_desconto_financeiro,OLD.valor_pagamento_liquido,
       OLD.id_mpagamento,OLD.id_emissor,OLD.momento_emissao,OLD.data_hora_operacao,OLD.observacoes,
       OLD.atcud,OLD.codigo_validacao_at,OLD.qr_payload,OLD.qr_payload_version)
   THEN RAISE EXCEPTION 'Linked receipt identity is immutable'; END IF;
   IF NEW.anulado AND NOT OLD.anulado AND EXISTS(
      SELECT 1 FROM financial_receipt_reversal a JOIN financial_operation op ON op.id=a.operation_id
      WHERE a.replacement_document_id=OLD.id AND op.created_tx=txid_current())
   THEN RAISE EXCEPTION 'Atomic replacement must leave an active substitute'; END IF;
   IF NEW.anulado IS DISTINCT FROM OLD.anulado AND
      (OLD.anulado OR NOT EXISTS(SELECT 1 FROM financial_receipt_reversal WHERE document_id=OLD.id))
   THEN RAISE EXCEPTION 'Linked receipt cancellation requires its reversal act'; END IF;
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF;
 RETURN NEW;
END $$;

-- A reversal is sealed too, including after SET CONSTRAINTS was forced early.
CREATE OR REPLACE FUNCTION financial_receipt_entry_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM financial_receipt WHERE operation_id=NEW.operation_id)
 OR EXISTS(SELECT 1 FROM financial_receipt_reversal WHERE operation_id=NEW.operation_id)
 THEN RAISE EXCEPTION 'Cannot append to a receipt-bound financial operation'; END IF;
 RETURN NEW;
END $$;

-- Derived only; no independent editable financial date. Unknown generic reversals
-- remain undated. Historical NULL receipts retain the explicit compatibility fallback.
CREATE VIEW financial_operation_effect AS
 SELECT op.id,
   CASE WHEN op.kind='CREATE' THEN origin.data_emissao
        WHEN op.kind='SETTLE' THEN coalesce(receipt.data_valor,receipt.data_emissao)
        WHEN op.kind='REVERSE' AND act.operation_id IS NOT NULL THEN original.data_valor
   END AS effect_date,
   CASE WHEN op.kind='SETTLE' THEN receipt.momento_emissao
        WHEN op.kind='REVERSE' AND act.operation_id IS NOT NULL THEN act.occurred_at
        ELSE op.created_at END AS operation_at
 FROM financial_operation op
 LEFT JOIN financial_position p ON p.creation_id=op.id
 LEFT JOIN financial_document_origin link ON link.position_id=p.id
 LEFT JOIN documento_comercial origin ON origin.id=link.document_id
 LEFT JOIN financial_receipt r ON r.operation_id=op.id
 LEFT JOIN documento_financeiro receipt ON receipt.id=r.document_id
 LEFT JOIN financial_receipt_reversal act ON act.operation_id=op.id
 LEFT JOIN documento_financeiro original ON original.id=act.document_id;
