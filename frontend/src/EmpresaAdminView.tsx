import { useEffect, useState } from "react";
import { apiFetch } from "./api";

type Empresa = {
  nome: string;
  nomeComercial?: string;
  nif: string;
  morada?: string;
  morada1?: string;
  codPostalId?: string;
  localidade?: string;
  paisId?: string;
  freguesiaId?: string;
  capitalSocial?: number;
  matriculaRegistoComercial?: string;
  cae?: string;
  descricaoCae?: string;
  email?: string;
  web?: string;
  telefone?: string;
  iban?: string;
  bicSwift?: string;
  observacoesLegais?: string;
  textoRodape?: string;
  observacoesComerciaisDefault?: string;
  temLogotipo?: boolean;
  logotipoMediaType?: string;
  atualizadoEm?: string;
  atualizadoPor?: string;
};

const emptyEmpresa: Empresa = {
  nome: "",
  nif: "",
  morada: "",
  morada1: "",
  codPostalId: "",
  localidade: "",
  paisId: "PT",
  freguesiaId: "",
  capitalSocial: 0,
  matriculaRegistoComercial: "",
  cae: "",
  descricaoCae: "",
  email: "",
  web: "",
  nomeComercial: "",
  telefone: "",
  iban: "",
  bicSwift: "",
  observacoesLegais: "",
  textoRodape: "",
  observacoesComerciaisDefault: ""
};

export default function EmpresaAdminView() {
  const [empresa, setEmpresa] = useState<Empresa>(emptyEmpresa);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [logoVersion, setLogoVersion] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetchEmpresa()
      .then((data) => { if (active) setEmpresa({ ...emptyEmpresa, ...data }); })
      .catch((error) => { if (active) setMessage(error.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  function change<K extends keyof Empresa>(field: K, value: Empresa[K]) {
    setEmpresa((current) => ({ ...current, [field]: value }));
  }

  async function save() {
    setSaving(true);
    setMessage(null);
    try {
      const saved = await sendEmpresa(empresa);
      setEmpresa({ ...emptyEmpresa, ...saved });
      setMessage("Dados da empresa guardados.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel guardar a empresa.");
    } finally {
      setSaving(false);
    }
  }

  async function uploadLogo(file: File | null) {
    if (!file) return;
    setMessage(null);
    const form = new FormData();
    form.append("file", file);
    try {
      const response = await apiFetch("/api/empresa/logotipo", { method: "POST", body: form });
      if (!response.ok) throw new Error(await responseError(response));
      const saved = await response.json() as Empresa;
      setEmpresa({ ...emptyEmpresa, ...saved });
      setLogoVersion((value) => value + 1);
      setMessage("Logotipo atualizado.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel atualizar o logotipo.");
    }
  }

  async function removeLogo() {
    setMessage(null);
    try {
      const response = await apiFetch("/api/empresa/logotipo", { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      setEmpresa((current) => ({ ...current, temLogotipo: false, logotipoMediaType: undefined }));
      setLogoVersion((value) => value + 1);
      setMessage("Logotipo removido.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel remover o logotipo.");
    }
  }

  return (
    <section className="fac-panel">
      <div className="fac-panel-header">
        <div>
          <p className="fac-eyebrow">Empresa proprietaria</p>
          <h2>Identificacao da entidade emissora</h2>
        </div>
        <span className="fac-muted">{loading ? "A carregar..." : empresa.atualizadoPor ? `Atualizada por ${empresa.atualizadoPor}` : "Ficha unica"}</span>
      </div>

      {message && <p className="fac-editor-message">{message}</p>}

      <div className="fac-form-grid">
        <Field label="Nome legal"><input disabled={loading} value={empresa.nome ?? ""} onChange={(e) => change("nome", e.target.value)} /></Field>
        <Field label="Nome comercial"><input disabled={loading} value={empresa.nomeComercial ?? ""} onChange={(e) => change("nomeComercial", e.target.value)} /></Field>
        <Field label="NIF"><input disabled={loading} value={empresa.nif ?? ""} onChange={(e) => change("nif", e.target.value)} /></Field>
        <Field label="Email"><input disabled={loading} type="email" value={empresa.email ?? ""} onChange={(e) => change("email", e.target.value)} /></Field>
        <Field label="Telefone"><input disabled={loading} value={empresa.telefone ?? ""} onChange={(e) => change("telefone", e.target.value)} /></Field>
        <Field label="Website"><input disabled={loading} value={empresa.web ?? ""} onChange={(e) => change("web", e.target.value)} /></Field>
        <Field label="Morada"><input disabled={loading} value={empresa.morada ?? ""} onChange={(e) => change("morada", e.target.value)} /></Field>
        <Field label="Morada adicional"><input disabled={loading} value={empresa.morada1 ?? ""} onChange={(e) => change("morada1", e.target.value)} /></Field>
        <Field label="Codigo postal"><input disabled={loading} value={empresa.codPostalId ?? ""} onChange={(e) => change("codPostalId", e.target.value)} /></Field>
        <Field label="Localidade"><input disabled={loading} value={empresa.localidade ?? ""} onChange={(e) => change("localidade", e.target.value)} /></Field>
        <Field label="Pais"><input disabled={loading} value={empresa.paisId ?? ""} onChange={(e) => change("paisId", e.target.value)} /></Field>
        <Field label="Freguesia"><input disabled={loading} value={empresa.freguesiaId ?? ""} onChange={(e) => change("freguesiaId", e.target.value)} /></Field>
        <Field label="Capital social"><input disabled={loading} type="number" step="0.01" value={empresa.capitalSocial ?? 0} onChange={(e) => change("capitalSocial", Number(e.target.value))} /></Field>
        <Field label="Matricula registo comercial"><input disabled={loading} value={empresa.matriculaRegistoComercial ?? ""} onChange={(e) => change("matriculaRegistoComercial", e.target.value)} /></Field>
        <Field label="CAE"><input disabled={loading} value={empresa.cae ?? ""} onChange={(e) => change("cae", e.target.value)} /></Field>
        <Field label="Descricao CAE"><input disabled={loading} value={empresa.descricaoCae ?? ""} onChange={(e) => change("descricaoCae", e.target.value)} /></Field>
        <Field label="IBAN"><input disabled={loading} value={empresa.iban ?? ""} onChange={(e) => change("iban", e.target.value)} /></Field>
        <Field label="BIC/SWIFT"><input disabled={loading} value={empresa.bicSwift ?? ""} onChange={(e) => change("bicSwift", e.target.value)} /></Field>
      </div>

      <div className="fac-form-grid">
        <Field label="Observacoes legais"><textarea disabled={loading} rows={3} value={empresa.observacoesLegais ?? ""} onChange={(e) => change("observacoesLegais", e.target.value)} /></Field>
        <Field label="Texto de rodape"><textarea disabled={loading} rows={3} value={empresa.textoRodape ?? ""} onChange={(e) => change("textoRodape", e.target.value)} /></Field>
        <Field label="Observacoes comerciais por defeito"><textarea disabled={loading} rows={3} value={empresa.observacoesComerciaisDefault ?? ""} onChange={(e) => change("observacoesComerciaisDefault", e.target.value)} /></Field>
      </div>

      <div className="fac-panel fac-compact-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Logotipo</p><h3>PDFs e documentos comerciais</h3></div>
          <span className="fac-muted">PNG/JPEG ate 1 MiB</span>
        </div>
        {empresa.temLogotipo && <img alt="Logotipo da empresa" className="fac-logo-preview" src={`/api/empresa/logotipo?v=${logoVersion}`} />}
        <div className="fac-actions">
          <input accept="image/png,image/jpeg" disabled={loading} onChange={(e) => uploadLogo(e.target.files?.[0] ?? null)} type="file" />
          <button disabled={!empresa.temLogotipo} onClick={removeLogo} type="button">Remover logotipo</button>
        </div>
      </div>

      <div className="fac-form-footer">
        <span className="fac-muted">Estes dados ficam preservados no snapshot fiscal dos documentos emitidos.</span>
        <button className="fac-primary-button" disabled={loading || saving} onClick={save} type="button">
          {saving ? "A guardar..." : "Guardar empresa"}
        </button>
      </div>
    </section>
  );
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}

async function fetchEmpresa(): Promise<Empresa> {
  const response = await apiFetch("/api/empresa");
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function sendEmpresa(body: Empresa): Promise<Empresa> {
  const response = await apiFetch("/api/empresa", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function responseError(response: Response) {
  try {
    const payload = await response.json();
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}
