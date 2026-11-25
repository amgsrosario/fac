ALTER TABLE demo_saft_signature_evidence ADD COLUMN envelope bytea;
ALTER TABLE demo_saft_signature_evidence ADD COLUMN envelope_signature bytea;

CREATE FUNCTION demo_saft_evidence_at_commit() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.envelope IS NULL OR NEW.envelope_signature IS NULL OR NOT EXISTS (SELECT 1 FROM documento_comercial d
                   WHERE d.id=NEW.documento_id AND d.estado='EMITIDO'
                     AND d.numero_documento_completo=NEW.numero_documento
                     AND d.qr_payload_version='TUULI-DEMO-1'
                     AND (substring(d.qr_payload from position(chr(10) in d.qr_payload)+1)::jsonb ->> 'signature')
                         = replace(encode(NEW.signature,'base64'), E'\n', '')) THEN
        RAISE EXCEPTION 'Demonstration evidence requires the matching issued document and signature';
    END IF;
    RETURN NULL;
END $$;

CREATE CONSTRAINT TRIGGER demo_saft_evidence_commit_guard
    AFTER INSERT ON demo_saft_signature_evidence DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION demo_saft_evidence_at_commit();
