-- M02: local installation tool only. No user, login role or public execution grant.
-- The DBA grants only EXECUTE on the two functions to a short-lived local operator.
CREATE TABLE public.local_admin_provisioning (
    singleton boolean PRIMARY KEY DEFAULT true CHECK (singleton),
    installation_id uuid NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    eligible boolean NOT NULL,
    consumed_at timestamptz,
    administrator_code varchar(20) REFERENCES public.utilizador(codigo),
    audit_event_id bigint UNIQUE REFERENCES public.auditoria_evento(id),
    CHECK ((consumed_at IS NULL AND administrator_code IS NULL AND audit_event_id IS NULL)
        OR (consumed_at IS NOT NULL AND administrator_code IS NOT NULL AND audit_event_id IS NOT NULL))
);
INSERT INTO public.local_admin_provisioning(singleton,eligible)
SELECT true,NOT EXISTS(SELECT 1 FROM public.utilizador);

CREATE FUNCTION public.local_admin_receipt_guard() RETURNS trigger
LANGUAGE plpgsql SET search_path=pg_catalog,pg_temp AS $$
BEGIN
    IF TG_OP <> 'UPDATE' THEN RAISE EXCEPTION 'Local provisioning receipt cannot be recreated or removed'; END IF;
    IF OLD.consumed_at IS NOT NULL OR NOT OLD.eligible
       OR NEW.installation_id IS DISTINCT FROM OLD.installation_id
       OR NEW.singleton IS DISTINCT FROM OLD.singleton
       OR NEW.eligible IS DISTINCT FROM OLD.eligible
       OR NEW.consumed_at IS NULL THEN
        RAISE EXCEPTION 'Local provisioning receipt is immutable except for first consumption';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER local_admin_receipt_immutable BEFORE INSERT OR UPDATE OR DELETE
ON public.local_admin_provisioning FOR EACH ROW EXECUTE FUNCTION public.local_admin_receipt_guard();
CREATE TRIGGER local_admin_receipt_no_truncate BEFORE TRUNCATE
ON public.local_admin_provisioning FOR EACH STATEMENT EXECUTE FUNCTION public.local_admin_receipt_guard();

CREATE FUNCTION public.local_admin_identity()
RETURNS TABLE(database_name text,cluster_id text,installation_id uuid,operator_name text)
LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog,pg_temp AS $$
    SELECT current_database()::text,p.system_identifier::text,i.installation_id,session_user::text
    FROM pg_control_system() p CROSS JOIN public.local_admin_provisioning i;
$$;
REVOKE ALL ON FUNCTION public.local_admin_identity() FROM PUBLIC;

CREATE FUNCTION public.provision_first_administrator(
    expected_database text,expected_cluster text,expected_installation uuid,expected_operator text,
    authorization_reference text,authorization_actor text,authorization_expires timestamptz,
    administrator_code text,administrator_name text,administrator_email text,encoded_password text)
RETURNS bigint LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,pg_temp SET lock_timeout='5s' AS $$
DECLARE
    receipt public.local_admin_provisioning%ROWTYPE;
    audit_id bigint;
    instant timestamptz;
BEGIN
    IF current_database() IS DISTINCT FROM expected_database
       OR (SELECT system_identifier::text FROM pg_control_system()) IS DISTINCT FROM expected_cluster
       OR session_user::text IS DISTINCT FROM expected_operator
       OR authorization_expires IS NULL OR authorization_expires <= clock_timestamp()
       OR authorization_reference IS NULL OR length(btrim(authorization_reference)) NOT BETWEEN 1 AND 100
       OR authorization_actor IS NULL OR length(btrim(authorization_actor)) NOT BETWEEN 1 AND 100 THEN
        RAISE EXCEPTION 'Local provisioning authorization does not match installation';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname=session_user
               AND (rolsuper OR rolcreaterole OR rolcreatedb OR rolreplication OR rolbypassrls))
       OR EXISTS (SELECT 1 FROM pg_auth_members WHERE member=(SELECT oid FROM pg_roles WHERE rolname=session_user))
       OR EXISTS (SELECT 1 FROM pg_database WHERE datname=current_database()
                  AND datdba=(SELECT oid FROM pg_roles WHERE rolname=session_user)) THEN
        RAISE EXCEPTION 'Use a restricted local provisioning operator, not an owner or privileged role';
    END IF;
    SELECT * INTO STRICT receipt FROM public.local_admin_provisioning WHERE singleton FOR UPDATE;
    IF receipt.installation_id IS DISTINCT FROM expected_installation OR NOT receipt.eligible
       OR receipt.consumed_at IS NOT NULL THEN
        RAISE EXCEPTION 'Local provisioning is unavailable or already consumed';
    END IF;
    -- Also serialize against ordinary CRUD/SQL inserts; an empty row set cannot be row-locked.
    LOCK TABLE public.utilizador IN EXCLUSIVE MODE;
    IF authorization_expires <= clock_timestamp() THEN RAISE EXCEPTION 'Local authorization expired while acquiring locks'; END IF;
    IF EXISTS(SELECT 1 FROM public.utilizador) THEN
        RAISE EXCEPTION 'Local provisioning requires an installation without users';
    END IF;
    IF administrator_code IS NULL OR administrator_code !~ '^[A-Z0-9]{1,20}$'
       OR administrator_name IS NULL OR length(btrim(administrator_name)) NOT BETWEEN 1 AND 100
       OR administrator_email IS NULL OR length(administrator_email) NOT BETWEEN 3 AND 100
       OR administrator_email <> lower(btrim(administrator_email))
       OR encoded_password IS NULL OR encoded_password !~ '^\$2[aby]\$10\$[./A-Za-z0-9]{53}$' THEN
        RAISE EXCEPTION 'Invalid local administrator data';
    END IF;
    instant=clock_timestamp();
    INSERT INTO public.utilizador(codigo,nome,email,password_hash,inativo,papel,
                                  criado_em,atualizado_em,criado_por,atualizado_por)
    VALUES(administrator_code,btrim(administrator_name),administrator_email,encoded_password,false,
           'ADMINISTRADOR',instant,instant,'M02_LOCAL','M02_LOCAL');
    INSERT INTO public.auditoria_evento(data_hora,tipo_evento,entidade_tipo,entidade_id,
        resultado,referencia,descricao,dados_essenciais)
    VALUES(instant,'UTILIZADOR_CRIADO','UTILIZADOR',administrator_code,'SUCESSO',authorization_reference,
           'Primeiro administrador provisionado por ferramenta local',
           json_build_object('versao',1,'origem','M02_LOCAL','installationId',expected_installation,
                             'responsavel',authorization_actor,'perfil','ADMINISTRADOR')::text)
    RETURNING id INTO audit_id;
    UPDATE public.local_admin_provisioning
    SET consumed_at=instant,administrator_code=provision_first_administrator.administrator_code,
        audit_event_id=audit_id WHERE singleton;
    RETURN audit_id;
END $$;
REVOKE ALL ON FUNCTION public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) FROM PUBLIC;
REVOKE ALL ON public.local_admin_provisioning FROM PUBLIC;
