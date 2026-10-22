-- Infrastructure only: no fiscal definitions, historical assignments or activation.
CREATE TABLE fiscal_catalog_revision (
    fiscal_code varchar(2) NOT NULL,
    revision bigint NOT NULL CHECK (revision > 0),
    definition jsonb NOT NULL,
    PRIMARY KEY (fiscal_code, revision),
    CHECK (length(fiscal_code) = 2),
    CHECK (jsonb_typeof(definition) = 'object'
       AND definition ?& ARRAY['schemaVersion','fiscalCode','revision','semantics']
       AND jsonb_typeof(definition->'schemaVersion') = 'number'
       AND jsonb_typeof(definition->'fiscalCode') = 'string'
       AND jsonb_typeof(definition->'revision') = 'number'
       AND definition->>'schemaVersion' = '1'
       AND definition->>'fiscalCode' = fiscal_code
       AND definition->>'revision' = revision::text
       AND jsonb_typeof(definition->'semantics') = 'object')
);

CREATE FUNCTION fiscal_catalog_revision_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Fiscal catalog revisions are immutable' USING ERRCODE = '23514';
END $$;
CREATE TRIGGER fiscal_catalog_revision_immutable
BEFORE UPDATE OR DELETE ON fiscal_catalog_revision
FOR EACH ROW EXECUTE FUNCTION fiscal_catalog_revision_immutable();
CREATE TRIGGER fiscal_catalog_revision_no_truncate
BEFORE TRUNCATE ON fiscal_catalog_revision
FOR EACH STATEMENT EXECUTE FUNCTION fiscal_catalog_revision_immutable();

-- NULL is intentionally preserved for every existing type. A binding can only be
-- supplied at INSERT of a new configuration; the existing CRUD does not supply it.
ALTER TABLE tipodocumento
    ADD COLUMN fiscal_catalog_code varchar(2),
    ADD COLUMN fiscal_catalog_revision bigint,
    ADD CONSTRAINT fiscal_catalog_binding_pair CHECK (
        (fiscal_catalog_code IS NULL AND fiscal_catalog_revision IS NULL)
        OR (fiscal_catalog_code IS NOT NULL AND fiscal_catalog_revision IS NOT NULL
            AND codigo_fiscal IS NOT NULL AND fiscal_catalog_code = codigo_fiscal)),
    ADD CONSTRAINT fiscal_catalog_binding_fk FOREIGN KEY (fiscal_catalog_code, fiscal_catalog_revision)
        REFERENCES fiscal_catalog_revision(fiscal_code, revision) ON UPDATE RESTRICT ON DELETE RESTRICT;

CREATE FUNCTION fiscal_catalog_binding_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF ROW(NEW.fiscal_catalog_code,NEW.fiscal_catalog_revision)
       IS DISTINCT FROM ROW(OLD.fiscal_catalog_code,OLD.fiscal_catalog_revision) THEN
        RAISE EXCEPTION 'Fiscal catalog binding is immutable; no historical reassignment'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER fiscal_catalog_binding_immutable
BEFORE UPDATE ON tipodocumento
FOR EACH ROW EXECUTE FUNCTION fiscal_catalog_binding_immutable();
