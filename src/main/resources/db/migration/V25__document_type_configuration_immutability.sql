-- Preserve the current classification, not a reconstruction of historical classifications.
-- This marker survives cancellation and deletion of source documents.
-- Prevent an emission racing with discovery of existing history during the upgrade.
LOCK TABLE documento_comercial, documento_financeiro IN SHARE ROW EXCLUSIVE MODE;
ALTER TABLE tipodocumento ADD COLUMN configuracao_bloqueada boolean NOT NULL DEFAULT false;
UPDATE tipodocumento t SET configuracao_bloqueada = true
WHERE EXISTS (SELECT 1 FROM documento_comercial d WHERE d.id_tipo_documento = t.id
              AND (d.estado IN ('EMITIDO', 'ANULADO') OR d.momento_emissao IS NOT NULL))
   OR EXISTS (SELECT 1 FROM documento_financeiro d WHERE d.id_tipo_documento = t.id);

CREATE FUNCTION document_type_configuration_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.configuracao_bloqueada THEN
        IF TG_OP = 'DELETE' THEN
            RAISE EXCEPTION 'Tipo de documento com emissão histórica não pode ser eliminado' USING ERRCODE = '23514';
        END IF;
        IF NOT NEW.configuracao_bloqueada OR
           ROW(NEW.id, NEW.codigo_fiscal, NEW.area_gestao, NEW.entidade,
               NEW.sinal_contabilistico, NEW.liquidacao_imediata)
           IS DISTINCT FROM
           ROW(OLD.id, OLD.codigo_fiscal, OLD.area_gestao, OLD.entidade,
               OLD.sinal_contabilistico, OLD.liquidacao_imediata) THEN
            RAISE EXCEPTION 'Configuração fiscal e financeira bloqueada: já existe emissão documental deste tipo'
                USING ERRCODE = '23514';
        END IF;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER document_type_configuration_guard BEFORE UPDATE OR DELETE ON tipodocumento
FOR EACH ROW EXECUTE FUNCTION document_type_configuration_guard();

CREATE FUNCTION document_type_record_emission() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_TABLE_NAME = 'documento_financeiro' THEN
        IF TG_OP = 'UPDATE' AND NEW.id_tipo_documento IS NOT DISTINCT FROM OLD.id_tipo_documento THEN
            RETURN NEW;
        END IF;
    ELSE
        IF NEW.estado NOT IN ('EMITIDO', 'ANULADO') AND NEW.momento_emissao IS NULL THEN RETURN NEW; END IF;
        IF TG_OP = 'UPDATE' AND OLD.estado IN ('EMITIDO', 'ANULADO')
           AND NEW.id_tipo_documento IS NOT DISTINCT FROM OLD.id_tipo_documento THEN RETURN NEW; END IF;
    END IF;
    -- A real row write serializes SQL writers as well; stale repeatable-read writers abort.
    UPDATE tipodocumento SET configuracao_bloqueada = true WHERE id = NEW.id_tipo_documento;
    RETURN NEW;
END $$;
CREATE TRIGGER document_type_record_emission BEFORE INSERT OR UPDATE ON documento_comercial
FOR EACH ROW EXECUTE FUNCTION document_type_record_emission();
CREATE TRIGGER document_type_record_emission BEFORE INSERT OR UPDATE ON documento_financeiro
FOR EACH ROW EXECUTE FUNCTION document_type_record_emission();
