-- Demonstration only: archive the original signed receipt snapshot, at emission.
CREATE TABLE demo_saft_receipt_evidence (
    documento_id bigint PRIMARY KEY REFERENCES documento_financeiro(id),
    public_key bytea NOT NULL,
    signed_bytes bytea NOT NULL,
    signature bytea NOT NULL
);

CREATE TRIGGER demo_saft_receipt_no_update_delete
    BEFORE UPDATE OR DELETE ON demo_saft_receipt_evidence
    FOR EACH ROW EXECUTE FUNCTION demo_saft_evidence_immutable();

CREATE FUNCTION demo_saft_receipt_during_emission() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM documento_financeiro d
                   WHERE d.id=NEW.documento_id AND d.id_tipo_documento='RC'
                     AND d.serie='DEMO26' AND d.qr_payload IS NULL AND NOT d.anulado) THEN
        RAISE EXCEPTION 'Demonstration receipt evidence requires the original receipt during emission';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER demo_saft_receipt_insert_guard
    BEFORE INSERT ON demo_saft_receipt_evidence
    FOR EACH ROW EXECUTE FUNCTION demo_saft_receipt_during_emission();

CREATE FUNCTION demo_saft_receipt_at_commit() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM documento_financeiro d WHERE d.id=NEW.documento_id
        AND d.qr_payload_version='TUULI-DEMO-SAFT-1'
        AND (substring(d.qr_payload from position(chr(10) in d.qr_payload)+1)::jsonb ->> 'signature')
             = replace(encode(NEW.signature,'base64'), E'\n', '')) THEN
        RAISE EXCEPTION 'Demonstration evidence requires the matching issued receipt';
    END IF;
    RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER demo_saft_receipt_commit_guard
    AFTER INSERT ON demo_saft_receipt_evidence DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION demo_saft_receipt_at_commit();
