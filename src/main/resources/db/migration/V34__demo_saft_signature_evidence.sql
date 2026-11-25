-- Prospectively captured demonstration evidence only; existing documents have no such proof.
CREATE TABLE demo_saft_signature_evidence (
    documento_id bigint PRIMARY KEY REFERENCES documento_comercial(id),
    numero_documento varchar(100) NOT NULL,
    key_version varchar(40) NOT NULL,
    public_key bytea NOT NULL,
    signed_bytes bytea NOT NULL,
    signature bytea NOT NULL,
    line_facts jsonb NOT NULL
);

CREATE FUNCTION demo_saft_evidence_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Demonstration signature evidence is immutable';
END $$;

CREATE TRIGGER demo_saft_evidence_no_update_delete
    BEFORE UPDATE OR DELETE ON demo_saft_signature_evidence
    FOR EACH ROW EXECUTE FUNCTION demo_saft_evidence_immutable();

CREATE FUNCTION demo_saft_evidence_during_emission() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM documento_comercial d
                   WHERE d.id=NEW.documento_id AND d.estado='RASCUNHO'
                     AND d.id_tipo_documento='FT' AND d.serie='DEMO26'
                     AND d.numero_documento_completo=NEW.numero_documento
                     AND d.numero_documento IS NOT NULL AND d.qr_payload IS NULL) THEN
        RAISE EXCEPTION 'Demonstration evidence requires the numbered FT draft during emission';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER demo_saft_evidence_insert_guard
    BEFORE INSERT ON demo_saft_signature_evidence
    FOR EACH ROW EXECUTE FUNCTION demo_saft_evidence_during_emission();
