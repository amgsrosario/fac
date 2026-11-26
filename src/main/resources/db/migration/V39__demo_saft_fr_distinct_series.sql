-- FR's synthetic series must differ from the existing FT and RC series.
CREATE OR REPLACE FUNCTION demo_saft_evidence_during_emission() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM documento_comercial d
                   WHERE d.id=NEW.documento_id AND d.estado='RASCUNHO'
                     AND ((d.id_tipo_documento='FT' AND d.serie='DEMO26')
                       OR (d.id_tipo_documento='FRE' AND d.serie='FR26'))
                     AND d.numero_documento_completo=NEW.numero_documento
                     AND d.numero_documento IS NOT NULL AND d.qr_payload IS NULL) THEN
        RAISE EXCEPTION 'Demonstration evidence requires the numbered FT or FR draft during emission';
    END IF;
    RETURN NEW;
END $$;
