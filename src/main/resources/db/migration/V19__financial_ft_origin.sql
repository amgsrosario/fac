-- Additive documentary association; no historical positions are created.
CREATE TABLE financial_document_origin (
 document_id bigint PRIMARY KEY REFERENCES documento_comercial(id),
 position_id bigint NOT NULL UNIQUE REFERENCES financial_position(id)
);
CREATE TRIGGER financial_document_origin_immutable
 BEFORE UPDATE OR DELETE ON financial_document_origin
 FOR EACH ROW EXECUTE FUNCTION financial_immutable();
CREATE FUNCTION financial_document_origin_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM financial_position p JOIN documento_comercial d ON d.id=NEW.document_id
 WHERE p.id=NEW.position_id AND p.empresa_id=1 AND p.cliente_id=d.id_cliente
 AND p.moeda_id=d.id_moeda AND p.nature='DEBT' AND p.original=d.valor_total
 AND p.origin='DOCUMENTO_COMERCIAL:' || d.id::text AND d.estado='EMITIDO')
 THEN RAISE EXCEPTION 'Invalid financial documentary origin'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER financial_document_origin_scope BEFORE INSERT ON financial_document_origin
 FOR EACH ROW EXECUTE FUNCTION financial_document_origin_guard();
