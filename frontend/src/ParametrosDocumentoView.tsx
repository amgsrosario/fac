import { useEffect, useState } from "react";

type Page<T> = { content: T[] };
type TipoDocumento = { id: string; descricao: string; areaGestao: number };
type Serie = { serie: string; tipoDocumentoId: string; nome: string };
type Armazem = { id: number; nome: string };
type Parametros = { tipoDocumentoId?: string; serie?: string; armazemCargaId?: number };

type Form = {
  tipoDocumentoId: string;
  serie: string;
  armazemCargaId: string;
};

const emptyForm: Form = { tipoDocumentoId: "", serie: "", armazemCargaId: "" };

export default function ParametrosDocumentoView() {
  const [tipos, setTipos] = useState<TipoDocumento[]>([]);
  const [series, setSeries] = useState<Serie[]>([]);
  const [armazens, setArmazens] = useState<Armazem[]>([]);
  const [form, setForm] = useState<Form>(emptyForm);
  const [exists, setExists] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setMessage(null);
    try {
      const [tiposPage, seriesPage, armazensPage, parametros] = await Promise.all([
        fetchJson<Page<TipoDocumento>>("/api/tipos-documento?size=100&sort=descricao,asc"),
        fetchJson<Page<Serie>>("/api/series?size=200&sort=serie,asc"),
        fetchJson<Page<Armazem>>("/api/armazens?size=100&sort=nome,asc"),
        fetchOptional<Parametros>("/api/parametros-documento-comercial")
      ]);
      const comerciais = tiposPage.content.filter((tipo) => tipo.areaGestao === 1 || tipo.areaGestao === 2);
      setTipos(comerciais);
      setSeries(seriesPage.content.filter((serie) => comerciais.some((tipo) => tipo.id === serie.tipoDocumentoId)));
      setArmazens(armazensPage.content);
      setExists(parametros != null);
      setForm(parametros ? {
        tipoDocumentoId: parametros.tipoDocumentoId ?? "",
        serie: parametros.serie ?? "",
        armazemCargaId: parametros.armazemCargaId != null ? String(parametros.armazemCargaId) : ""
      } : emptyForm);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar os parametros de documentos.");
    } finally {
      setLoading(false);
    }
  }

  async function save() {
    if ((form.tipoDocumentoId && !form.serie) || (!form.tipoDocumentoId && form.serie)) {
      setMessage("Tipo de documento e serie devem ser configurados em conjunto.");
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const response = await fetch("/api/parametros-documento-comercial", {
        method: exists ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          tipoDocumentoId: blankToNull(form.tipoDocumentoId),
          serie: blankToNull(form.serie),
          armazemCargaId: form.armazemCargaId ? Number(form.armazemCargaId) : null
        })
      });
      if (!response.ok) throw new Error(await responseError(response));
      setExists(true);
      setMessage("Parametros dos documentos comerciais guardados.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel guardar os parametros.");
    } finally {
      setLoading(false);
    }
  }

  const availableSeries = series.filter((serie) => serie.tipoDocumentoId === form.tipoDocumentoId);

  return (
    <section className="fac-panel fac-section-panel">
      <div className="fac-panel-header">
        <div><p className="fac-eyebrow">Documentos comerciais</p><h2>Valores base do rascunho</h2></div>
        <span className="fac-muted">Preenchimento automático, sempre editável</span>
      </div>

      {message && <p className="fac-editor-message">{message}</p>}

      <div className="fac-form-grid">
        <Field label="Tipo de documento">
          <select onChange={(event) => setForm((current) => ({ ...current, tipoDocumentoId: event.target.value, serie: "" }))} value={form.tipoDocumentoId}>
            <option value="">Nao sugerir</option>
            {tipos.map((tipo) => <option key={tipo.id} value={tipo.id}>{tipo.id} - {tipo.descricao}</option>)}
          </select>
        </Field>
        <Field label="Serie">
          <select disabled={!form.tipoDocumentoId} onChange={(event) => setForm((current) => ({ ...current, serie: event.target.value }))} value={form.serie}>
            <option value="">Nao sugerir</option>
            {availableSeries.map((serie) => <option key={`${serie.tipoDocumentoId}-${serie.serie}`} value={serie.serie}>{serie.serie} - {serie.nome}</option>)}
          </select>
        </Field>
        <Field label="Armazem de carga">
          <select onChange={(event) => setForm((current) => ({ ...current, armazemCargaId: event.target.value }))} value={form.armazemCargaId}>
            <option value="">Nao sugerir</option>
            {armazens.map((armazem) => <option key={armazem.id} value={armazem.id}>{armazem.nome}</option>)}
          </select>
        </Field>
      </div>

      <div className="fac-form-footer">
        <span className="fac-muted">A data continua a assumir o dia atual e o cliente e escolhido em cada documento.</span>
        <button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar valores base"}</button>
      </div>
    </section>
  );
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function fetchOptional<T>(url: string): Promise<T | null> {
  const response = await fetch(url);
  if (response.status === 404) return null;
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

function blankToNull(value: string) {
  const trimmed = value.trim();
  return trimmed || null;
}
