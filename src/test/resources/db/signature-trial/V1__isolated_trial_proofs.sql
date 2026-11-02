-- Separate explicit Flyway location. NEVER included in db/migration or runtime startup.
DO $$ BEGIN IF current_database() !~ '^h207_[a-f0-9]{32}_test$' THEN RAISE EXCEPTION 'Dedicated H2-07 disposable database required'; END IF; END $$;
CREATE SCHEMA signature_trial;
CREATE TABLE signature_trial.scope (
 id uuid PRIMARY KEY, label text NOT NULL CHECK(label='H2-07-NONFISCAL'),
 last_system_at timestamp(0) without time zone
);
CREATE TABLE signature_trial.series (
 scope uuid NOT NULL REFERENCES signature_trial.scope(id),
 name text NOT NULL CHECK(name ~ '^H207T[A-Z0-9]{1,24}$'),
 fiscal_type text NOT NULL DEFAULT 'FT' CHECK(fiscal_type='FT'),
 PRIMARY KEY(scope,name,fiscal_type)
);
CREATE TABLE signature_trial.proof (
 scope uuid NOT NULL, series text NOT NULL, fiscal_type text NOT NULL CHECK(fiscal_type='FT'),
 sequence bigint NOT NULL CHECK(sequence>0), document uuid NOT NULL,
 invoice_date date NOT NULL, system_at timestamp(0) without time zone NOT NULL,
 gross_total numeric(18,2) NOT NULL CHECK(gross_total>=0),
 canonical text NOT NULL, previous_signature text NOT NULL,
 signature text NOT NULL CHECK(signature ~ '^[A-Za-z0-9+/]{171}=$'),
 public_key text NOT NULL, key_version integer NOT NULL CHECK(key_version=1),
 nonfiscal text NOT NULL DEFAULT 'H2-07-NONFISCAL' CHECK(nonfiscal='H2-07-NONFISCAL'),
 PRIMARY KEY(scope,series,fiscal_type,sequence), UNIQUE(scope,document),
 FOREIGN KEY(scope,series,fiscal_type) REFERENCES signature_trial.series(scope,name,fiscal_type)
);
CREATE FUNCTION signature_trial.seal() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Trial evidence is immutable' USING ERRCODE='23514'; END $$;
CREATE TRIGGER immutable_proof BEFORE UPDATE OR DELETE ON signature_trial.proof FOR EACH ROW EXECUTE FUNCTION signature_trial.seal();
CREATE TRIGGER immutable_proof_truncate BEFORE TRUNCATE ON signature_trial.proof FOR EACH STATEMENT EXECUTE FUNCTION signature_trial.seal();
CREATE TRIGGER immutable_series BEFORE UPDATE OR DELETE ON signature_trial.series FOR EACH ROW EXECUTE FUNCTION signature_trial.seal();
CREATE TRIGGER immutable_series_truncate BEFORE TRUNCATE ON signature_trial.series FOR EACH STATEMENT EXECUTE FUNCTION signature_trial.seal();
CREATE FUNCTION signature_trial.append_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE p signature_trial.proof%ROWTYPE; clock_at timestamp; expected_text text;
BEGIN
 IF current_database() !~ '_test$' THEN RAISE EXCEPTION 'Disposable test database required'; END IF;
 SELECT last_system_at INTO clock_at FROM signature_trial.scope WHERE id=NEW.scope FOR UPDATE;
 IF NOT FOUND THEN RAISE EXCEPTION 'Unknown trial scope'; END IF;
 SELECT greatest(clock_at,max(system_at)) INTO clock_at FROM signature_trial.proof WHERE scope=NEW.scope;
 PERFORM 1 FROM signature_trial.series WHERE scope=NEW.scope AND name=NEW.series AND fiscal_type=NEW.fiscal_type FOR UPDATE;
 SELECT * INTO p FROM signature_trial.proof WHERE scope=NEW.scope AND series=NEW.series AND fiscal_type=NEW.fiscal_type ORDER BY sequence DESC LIMIT 1;
 IF NEW.sequence<>coalesce(p.sequence,0)+1 OR NEW.previous_signature<>coalesce(p.signature,'')
    OR (p.sequence IS NOT NULL AND (NEW.invoice_date<p.invoice_date OR NEW.public_key<>p.public_key))
    OR (clock_at IS NOT NULL AND NEW.system_at<clock_at) OR NEW.invoice_date>NEW.system_at::date THEN
   RAISE EXCEPTION 'Trial chain/chronology mismatch' USING ERRCODE='23514';
 END IF;
 expected_text := to_char(NEW.invoice_date,'YYYY-MM-DD') || ';' || to_char(NEW.system_at,'YYYY-MM-DD"T"HH24:MI:SS')
  || ';FT ' || NEW.series || '/' || NEW.sequence::text || ';' || NEW.gross_total::text || ';' || NEW.previous_signature;
 IF NEW.canonical<>expected_text THEN RAISE EXCEPTION 'Trial canonical mismatch' USING ERRCODE='23514'; END IF;
 UPDATE signature_trial.scope SET last_system_at=NEW.system_at WHERE id=NEW.scope;
 RETURN NEW;
END $$;
CREATE TRIGGER append_proof BEFORE INSERT ON signature_trial.proof FOR EACH ROW EXECUTE FUNCTION signature_trial.append_guard();
