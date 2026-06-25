import { useState } from "react";
import { apiFetch, hasPermission } from "./api";

type Tipo = "clientes" | "artigos";
type Formato = "csv" | "xlsx";

type Issue = { linha: number; coluna: string; valor: string; codigo: string; mensagem: string };
type Resumo = {
  totalLinhas: number;
  linhasValidas: number;
  linhasComErro: number;
  linhasComAviso: number;
  registosNovos: number;
  duplicados: number;
  linhasIgnoradas: number;
};
type Validacao = {
  id: string;
  tipo: "CLIENTES" | "ARTIGOS";
  nomeFicheiro: string;
  formato: string;
  expiraEm: string;
  resumo: Resumo;
  erros: Issue[];
  avisos: Issue[];
  amostra: Record<string, string>[];
};
type Resultado = {
  estado: string;
  resumo: Resumo;
  criados: number;
  rejeitados: number;
  ignorados: number;
  erros: Issue[];
  avisos: Issue[];
};

export default function ImportExportView() {
  const [tipo, setTipo] = useState<Tipo>("clientes");
  const [formato, setFormato] = useState<Formato>("csv");
  const [file, setFile] = useState<File | null>(null);
  const [validation, setValidation] = useState<Validacao | null>(null);
  const [result, setResult] = useState<Resultado | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const canImport = hasPermission("DADOS_MESTRES_IMPORTAR");
  const canExport = hasPermission("DADOS_MESTRES_EXPORTAR");

  async function validate() {
    if (!file) {
      setMessage("Seleciona um ficheiro CSV ou XLSX.");
      return;
    }
    setLoading(true);
    setMessage(null);
    setResult(null);
    const form = new FormData();
    form.append("file", file);
    try {
      const response = await apiFetch(`/api/importacoes/${tipo}/validar`, { method: "POST", body: form });
      if (!response.ok) throw new Error(await responseError(response));
      setValidation(await response.json());
    } catch (error) {
      setValidation(null);
      setMessage(error instanceof Error ? error.message : "Nao foi possivel validar o ficheiro.");
    } finally {
      setLoading(false);
    }
  }

  async function confirm() {
    if (!validation || validation.resumo.linhasValidas === 0) return;
    if (!window.confirm(`Confirmar importacao de ${validation.resumo.linhasValidas} registo(s)?`)) return;
    setLoading(true);
    setMessage(null);
    try {
      const response = await apiFetch(`/api/importacoes/${tipo}/${validation.id}/confirmar`, { method: "POST" });
      if (!response.ok) throw new Error(await responseError(response));
      setResult(await response.json());
      setValidation(null);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel confirmar a importacao.");
    } finally {
      setLoading(false);
    }
  }

  async function cancel() {
    if (!validation) return;
    setLoading(true);
    setMessage(null);
    try {
      const response = await apiFetch(`/api/importacoes/${tipo}/${validation.id}`, { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      setValidation(null);
      setMessage("Importacao cancelada.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel cancelar a importacao.");
    } finally {
      setLoading(false);
    }
  }

  async function download(path: string) {
    setMessage(null);
    try {
      const response = await apiFetch(path);
      if (!response.ok) throw new Error(await responseError(response));
      const blob = await response.blob();
      const disposition = response.headers.get("Content-Disposition") ?? "";
      const match = disposition.match(/filename="([^"]+)"/);
      const filename = match?.[1] ?? "fac-dados-mestres";
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel descarregar o ficheiro.");
    }
  }

  return (
    <>
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Dados mestres</p>
          <h2>Importacao e exportacao controlada</h2>
          <p>Valida ficheiros antes de gravar, confirma apenas o que e seguro e exporta clientes ou artigos.</p>
        </div>
        <div className="fac-hero-card">
          <span>Formatos</span>
          <strong>CSV e XLSX</strong>
          <small>Limite 10 MB / 10 000 linhas</small>
        </div>
      </section>

      {message && <p className="fac-editor-message">{message}</p>}

      <section className="fac-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Ficheiro</p><h2>Validar antes de importar</h2></div>
          <span className="fac-muted">{canImport ? "Importacao disponivel" : "Sem permissao de importacao"}</span>
        </div>
        <div className="fac-form-grid">
          <Field label="Tipo">
            <select value={tipo} onChange={(event) => { setTipo(event.target.value as Tipo); setValidation(null); setResult(null); }}>
              <option value="clientes">Clientes</option>
              <option value="artigos">Artigos</option>
            </select>
          </Field>
          <Field label="Formato">
            <select value={formato} onChange={(event) => setFormato(event.target.value as Formato)}>
              <option value="csv">CSV</option>
              <option value="xlsx">XLSX</option>
            </select>
          </Field>
          <Field label="Ficheiro">
            <input accept=".csv,.xlsx,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" disabled={!canImport} onChange={(event) => setFile(event.target.files?.[0] ?? null)} type="file" />
          </Field>
        </div>
        <div className="fac-form-footer">
          <div className="fac-actions">
            <button disabled={!canExport} onClick={() => download(`/api/importacoes/${tipo}/modelo?formato=${formato}`)} type="button">Descarregar modelo</button>
            <button disabled={!canExport} onClick={() => download(`/api/exportacoes/${tipo}?formato=${formato}`)} type="button">Exportar todos</button>
            <button disabled={!canExport} onClick={() => download(`/api/exportacoes/${tipo}?formato=${formato}&ativos=true`)} type="button">Exportar ativos</button>
          </div>
          <button className="fac-primary-button" disabled={!canImport || !file || loading} onClick={validate} type="button">
            {loading ? "A validar..." : "Validar ficheiro"}
          </button>
        </div>
      </section>

      {validation && <ValidationPanel validation={validation} loading={loading} onCancel={cancel} onConfirm={confirm} />}
      {result && <ResultPanel result={result} />}
    </>
  );
}

function ValidationPanel({ validation, loading, onCancel, onConfirm }: { validation: Validacao; loading: boolean; onCancel: () => void; onConfirm: () => void }) {
  return (
    <section className="fac-panel">
      <div className="fac-panel-header">
        <div><p className="fac-eyebrow">Pre-validacao</p><h2>{validation.nomeFicheiro}</h2></div>
        <span className="fac-muted">Expira {new Date(validation.expiraEm).toLocaleString("pt-PT")}</span>
      </div>
      <ResumoGrid resumo={validation.resumo} />
      <Issues title="Erros" issues={validation.erros} />
      <Issues title="Avisos" issues={validation.avisos} />
      {validation.amostra.length > 0 && <pre className="fac-code-preview">{JSON.stringify(validation.amostra.slice(0, 5), null, 2)}</pre>}
      <div className="fac-form-footer">
        <span className="fac-muted">Apenas linhas validas serao importadas. A confirmacao revalida duplicados na base.</span>
        <div className="fac-actions">
          <button disabled={loading} onClick={onCancel} type="button">Cancelar</button>
          <button className="fac-primary-button" disabled={loading || validation.resumo.linhasValidas === 0} onClick={onConfirm} type="button">Confirmar importacao</button>
        </div>
      </div>
    </section>
  );
}

function ResultPanel({ result }: { result: Resultado }) {
  return (
    <section className="fac-panel">
      <div className="fac-panel-header">
        <div><p className="fac-eyebrow">Resultado</p><h2>{result.estado}</h2></div>
        <span className="fac-status">{result.criados} criado(s)</span>
      </div>
      <ResumoGrid resumo={result.resumo} />
      <Issues title="Erros finais" issues={result.erros} />
      <Issues title="Avisos finais" issues={result.avisos} />
    </section>
  );
}

function ResumoGrid({ resumo }: { resumo: Resumo }) {
  const items = [
    ["Linhas", resumo.totalLinhas],
    ["Validas", resumo.linhasValidas],
    ["Erros", resumo.linhasComErro],
    ["Avisos", resumo.linhasComAviso],
    ["Novos", resumo.registosNovos],
    ["Duplicados", resumo.duplicados],
    ["Ignoradas", resumo.linhasIgnoradas]
  ];
  return <div className="fac-metrics">{items.map(([label, value]) => <div className="fac-metric" key={label}><span>{label}</span><strong>{value}</strong></div>)}</div>;
}

function Issues({ title, issues }: { title: string; issues: Issue[] }) {
  if (!issues.length) return null;
  return (
    <div className="fac-table-wrapper">
      <h3>{title}</h3>
      <table className="fac-table">
        <thead><tr><th>Linha</th><th>Coluna</th><th>Codigo</th><th>Mensagem</th><th>Valor</th></tr></thead>
        <tbody>{issues.slice(0, 50).map((issue, index) => (
          <tr key={`${issue.linha}-${issue.codigo}-${index}`}>
            <td>{issue.linha}</td><td>{issue.coluna}</td><td>{issue.codigo}</td><td>{issue.mensagem}</td><td>{issue.valor}</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}

async function responseError(response: Response) {
  try {
    const payload = await response.json();
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}
