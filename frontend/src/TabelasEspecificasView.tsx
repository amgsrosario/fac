import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "./api";

type Page<T> = { content: T[] };
type Row = Record<string, unknown>;
type Values = Record<string, string | boolean>;
type Option = { value: string; label: string };
type Feedback = { kind: "success" | "error"; text: string };
type TableKey = "tipos-documento" | "series" | "riva" | "codpostal" | "freguesias" | "armazens" | "utilizadores";
type Field = {
  key: string;
  label: string;
  type?: "text" | "number" | "date" | "checkbox" | "password" | "select";
  maxLength?: number;
  required?: boolean;
  createOnly?: boolean;
  optionalOnUpdate?: boolean;
  options?: string;
};
type Config = {
  key: TableKey;
  label: string;
  endpoint: string;
  fields: Field[];
  columns: { key: string; label: string }[];
  rowId: (row: Row) => string;
  itemUrl: (row: Row) => string;
};

const field = (key: string, label: string, options: Partial<Field> = {}): Field => ({ key, label, ...options });

const configs: Record<TableKey, Config> = {
  "tipos-documento": {
    key: "tipos-documento", label: "Tipos de documento", endpoint: "/api/tipos-documento",
    fields: [field("id", "Codigo", { required: true, maxLength: 3, createOnly: true }), field("descricao", "Descricao", { required: true, maxLength: 50 }), field("codigoFiscal", "Codigo fiscal", { maxLength: 2 }), field("modeloEmissao1", "Modelo de emissao 1", { maxLength: 25 }), field("modeloEmissao2", "Modelo de emissao 2", { maxLength: 25 }), field("modeloEmissao3", "Modelo de emissao 3", { maxLength: 25 }), field("modeloEmissao4", "Modelo de emissao 4", { maxLength: 25 }), field("areaGestao", "Area de gestao", { type: "number", required: true }), field("entidade", "Entidade", { type: "number", required: true }), field("sinalContabilistico", "Sinal contabilistico", { type: "select", required: true, options: "sinais" }), field("liquidacaoImediata", "Liquidacao imediata", { type: "checkbox" })],
    columns: [{ key: "id", label: "Codigo" }, { key: "codigoFiscal", label: "Fiscal" }, { key: "descricao", label: "Descricao" }, { key: "areaGestao", label: "Area" }, { key: "liquidacaoImediata", label: "Liquidacao imediata" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/tipos-documento/${encodeURIComponent(String(row.id))}`
  },
  series: {
    key: "series", label: "Series", endpoint: "/api/series",
    fields: [field("tipoDocumentoId", "Tipo de documento", { type: "select", required: true, createOnly: true, options: "tiposDocumento" }), field("serie", "Serie", { required: true, maxLength: 10, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 50 }), field("codigoAt", "Codigo AT", { maxLength: 100 }), field("dataCodigoAt", "Data do codigo AT", { type: "date" })],
    columns: [{ key: "tipoDocumentoId", label: "Documento" }, { key: "serie", label: "Serie" }, { key: "nome", label: "Nome" }, { key: "numerador", label: "Numerador" }],
    rowId: (row) => `${row.tipoDocumentoId}-${row.serie}`, itemUrl: (row) => `/api/series/${encodeURIComponent(String(row.tipoDocumentoId))}/${encodeURIComponent(String(row.serie))}`
  },
  riva: {
    key: "riva", label: "Regimes de IVA", endpoint: "/api/riva",
    fields: [field("id", "Codigo", { required: true, maxLength: 3, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 30 })],
    columns: [{ key: "id", label: "Codigo" }, { key: "nome", label: "Nome" }, { key: "taxas", label: "Taxas" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/riva/${encodeURIComponent(String(row.id))}`
  },
  codpostal: {
    key: "codpostal", label: "Codigos postais", endpoint: "/api/codpostal",
    fields: [field("id", "Codigo postal", { required: true, maxLength: 20, createOnly: true }), field("nome", "Localidade", { required: true, maxLength: 50 })],
    columns: [{ key: "id", label: "Codigo" }, { key: "nome", label: "Localidade" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/codpostal/${encodeURIComponent(String(row.id))}`
  },
  freguesias: {
    key: "freguesias", label: "Freguesias", endpoint: "/api/freguesias",
    fields: [field("codigo", "Codigo", { required: true, maxLength: 6, createOnly: true }), field("codigoDistrito", "Codigo distrito", { required: true, maxLength: 2, createOnly: true }), field("codigoConcelho", "Codigo concelho", { required: true, maxLength: 2, createOnly: true }), field("codigoFreguesia", "Codigo freguesia", { required: true, maxLength: 2, createOnly: true }), field("concelho", "Concelho", { required: true, maxLength: 50 }), field("nome", "Freguesia", { required: true, maxLength: 80 }), field("extinta", "Extinta", { type: "checkbox" })],
    columns: [{ key: "codigo", label: "Codigo" }, { key: "concelho", label: "Concelho" }, { key: "nome", label: "Freguesia" }, { key: "extinta", label: "Estado" }],
    rowId: (row) => String(row.codigo), itemUrl: (row) => `/api/freguesias/${encodeURIComponent(String(row.codigo))}`
  },
  armazens: {
    key: "armazens", label: "Armazens", endpoint: "/api/armazens",
    fields: [field("nome", "Nome", { required: true, maxLength: 100 }), field("morada", "Morada", { required: true, maxLength: 60 }), field("morada1", "Morada complementar", { maxLength: 60 }), field("codPostalId", "Codigo postal", { type: "select", required: true, options: "codigosPostais" }), field("localidade", "Localidade", { required: true, maxLength: 50 }), field("paisId", "Pais", { type: "select", required: true, options: "paises" }), field("freguesiaId", "Freguesia", { type: "select", options: "freguesias" })],
    columns: [{ key: "id", label: "ID" }, { key: "nome", label: "Nome" }, { key: "localidade", label: "Localidade" }, { key: "paisId", label: "Pais" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/armazens/${encodeURIComponent(String(row.id))}`
  },
  utilizadores: {
    key: "utilizadores", label: "Utilizadores", endpoint: "/api/utilizadores",
    fields: [field("codigo", "Codigo", { required: true, maxLength: 20, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 100 }), field("email", "Email", { required: true, maxLength: 100 }), field("password", "Password", { type: "password", required: true, optionalOnUpdate: true, maxLength: 72 }), field("inativo", "Inativo", { type: "checkbox" })],
    columns: [{ key: "codigo", label: "Codigo" }, { key: "nome", label: "Nome" }, { key: "email", label: "Email" }, { key: "inativo", label: "Estado" }],
    rowId: (row) => String(row.codigo), itemUrl: (row) => `/api/utilizadores/${encodeURIComponent(String(row.codigo))}`
  }
};

export const specificTables: { key: TableKey; label: string; group: string }[] = [
  { key: "tipos-documento", label: "Tipos de documento", group: "Documentos" }, { key: "series", label: "Series", group: "Documentos" },
  { key: "riva", label: "Regimes de IVA", group: "Fiscalidade" }, { key: "codpostal", label: "Codigos postais", group: "Localizacao" },
  { key: "freguesias", label: "Freguesias", group: "Localizacao" }, { key: "armazens", label: "Armazens", group: "Sistema" },
  { key: "utilizadores", label: "Utilizadores", group: "Sistema" }
];

export default function TabelasEspecificasView({ tableKey, onBack }: { tableKey: TableKey; onBack: () => void }) {
  const config = configs[tableKey];
  const [rows, setRows] = useState<Row[]>([]);
  const [values, setValues] = useState<Values>({});
  const [editing, setEditing] = useState<Row | null>(null);
  const [options, setOptions] = useState<Record<string, Option[]>>({ sinais: [{ value: "1", label: "1 - Debito" }, { value: "2", label: "2 - Credito" }] });
  const [rates, setRates] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const isRiva = tableKey === "riva";

  useEffect(() => { load(); }, [tableKey]);

  async function load() {
    setLoading(true); setFeedback(null);
    try {
      const sortField = tableKey === "freguesias" || tableKey === "utilizadores" ? "codigo" : tableKey === "series" ? "serie" : "id";
      const [page, support] = await Promise.all([get<Page<Row>>(`${config.endpoint}?size=1000&sort=${sortField},asc`), loadOptions()]);
      setRows(page.content); setOptions((current) => ({ ...current, ...support })); reset();
    } catch (error) { setFeedback({ kind: "error", text: errorMessage(error) }); }
    finally { setLoading(false); }
  }

  async function loadOptions() {
    const required = new Set(config.fields.map((item) => item.options).filter(Boolean));
    if (isRiva) required.add("tiposTaxa");
    const result: Record<string, Option[]> = {};
    await Promise.all([...required].filter((key) => key !== "sinais").map(async (key) => {
      const definitions: Record<string, [string, (row: Row) => Option]> = {
        tiposDocumento: ["/api/tipos-documento?size=500&sort=id,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.descricao}` })],
        codigosPostais: ["/api/codpostal?size=1000&sort=id,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.nome}` })],
        paises: ["/api/paises?size=500&sort=nome,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.nome}` })],
        freguesias: ["/api/freguesias?size=1000&sort=nome,asc", (row) => ({ value: String(row.codigo), label: `${row.codigo} - ${row.nome}` })],
        tiposTaxa: ["/api/tipos-taxa-iva?size=100&sort=id,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.descricao}` })]
      };
      const definition = definitions[key!];
      if (definition) result[key!] = (await get<Page<Row>>(definition[0])).content.map(definition[1]);
    }));
    return result;
  }

  function reset() {
    setEditing(null); setValues(Object.fromEntries(config.fields.map((item) => [item.key, item.type === "checkbox" ? false : ""]))); setRates({});
  }

  function edit(row: Row) {
    setEditing(row); setFeedback(null);
    setValues(Object.fromEntries(config.fields.map((item) => [item.key, item.type === "checkbox" ? Boolean(row[item.key]) : item.key === "password" ? "" : String(row[item.key] ?? "")])));
    if (isRiva) setRates(Object.fromEntries(((row.taxas as Row[]) ?? []).map((taxa) => [String(taxa.tipoTaxaIvaId), String(taxa.valor)])));
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function save() {
    for (const item of config.fields) {
      if (item.required && !(editing && (item.createOnly || item.optionalOnUpdate)) && String(values[item.key] ?? "").trim() === "") {
        setFeedback({ kind: "error", text: `${item.label} e obrigatorio.` }); return;
      }
    }
    const taxas = Object.entries(rates).filter(([, value]) => value !== "").map(([tipoTaxaIvaId, valor]) => ({ tipoTaxaIvaId, valor: Number(valor) }));
    if (isRiva && taxas.length === 0) { setFeedback({ kind: "error", text: "O regime de IVA deve ter pelo menos uma taxa." }); return; }
    setLoading(true); setFeedback(null);
    try {
      const payload: Record<string, unknown> = Object.fromEntries(config.fields.filter((item) => !(editing && item.createOnly) && !(editing && item.optionalOnUpdate && !values[item.key])).map((item) => [item.key, item.type === "number" ? Number(values[item.key]) : item.type === "checkbox" ? Boolean(values[item.key]) : blankToNull(String(values[item.key] ?? ""))]));
      if (isRiva) payload.taxas = taxas;
      const response = await apiFetch(editing ? config.itemUrl(editing) : config.endpoint, { method: editing ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
      if (!response.ok) throw new Error(await responseError(response));
      await load(); setFeedback({ kind: "success", text: editing ? "Registo atualizado com sucesso." : "Registo criado com sucesso." });
    } catch (error) { setFeedback({ kind: "error", text: errorMessage(error) }); setLoading(false); }
  }

  async function remove(row: Row) {
    if (!window.confirm(`Eliminar ${config.label}: ${config.rowId(row)}?\n\nSe estiver em utilizacao, o FAC recusara a operacao.`)) return;
    setLoading(true); setFeedback(null);
    try {
      const response = await apiFetch(config.itemUrl(row), { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      await load(); setFeedback({ kind: "success", text: "Registo eliminado com sucesso." });
    } catch (error) { setFeedback({ kind: "error", text: `O registo nao foi eliminado e permanece na tabela. ${errorMessage(error)}` }); setLoading(false); }
  }

  const rateOptions = useMemo(() => options.tiposTaxa ?? [], [options]);

  return <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabela</p><h2>{config.label}</h2></div><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={onBack} type="button">Voltar</button><button className="fac-primary-button" onClick={reset} type="button">Novo registo</button></div></div>
    {feedback && <p className={`fac-editor-message fac-editor-message-${feedback.kind}`} role={feedback.kind === "error" ? "alert" : "status"}>{feedback.text}</p>}
    <div className="fac-table-editor"><div className="fac-form-grid">{config.fields.map((item) => <EditorField field={item} key={item.key} options={options[item.options ?? ""] ?? []} editing={Boolean(editing)} value={values[item.key]} onChange={(value) => setValues((current) => ({ ...current, [item.key]: value }))} />)}</div>
      {isRiva && <div className="fac-rate-grid"><p className="fac-muted">Taxas do regime</p>{rateOptions.map((option) => <label className="fac-field" key={option.value}><span>{option.label}</span><input min="0" onChange={(event) => setRates((current) => ({ ...current, [option.value]: event.target.value }))} step="0.01" type="number" value={rates[option.value] ?? ""}/></label>)}</div>}
      <div className="fac-form-footer"><span className="fac-muted">{editing ? `A editar ${config.rowId(editing)}` : "Novo registo"}</span><button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar"}</button></div>
    </div>
    <p className="fac-muted">A eliminacao so e aceite para registos nunca utilizados.</p>
    <table className="fac-table"><thead><tr>{config.columns.map((column) => <th key={column.key}>{column.label}</th>)}<th>Acoes</th></tr></thead><tbody>{rows.map((row) => <tr key={config.rowId(row)}>{config.columns.map((column) => <td key={column.key}>{display(column.key, row[column.key])}</td>)}<td><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => edit(row)} type="button">Editar</button><button className="fac-link-danger" disabled={loading} onClick={() => remove(row)} type="button">Eliminar</button></div></td></tr>)}{!loading && rows.length === 0 && <tr><td colSpan={config.columns.length + 1}>Sem registos.</td></tr>}</tbody></table>
  </section>;
}

function EditorField({ field: item, value, editing, options, onChange }: { field: Field; value: string | boolean; editing: boolean; options: Option[]; onChange: (value: string | boolean) => void }) {
  if (item.type === "checkbox") return <label className="fac-check-field"><input checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)} type="checkbox"/><span>{item.label}</span></label>;
  return <label className="fac-field"><span>{item.label}{item.optionalOnUpdate && editing ? " (deixar vazio para manter)" : ""}</span>{item.type === "select" ? <select disabled={editing && item.createOnly} onChange={(event) => onChange(event.target.value)} value={String(value ?? "")}><option value="">Selecionar</option>{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select> : <input disabled={editing && item.createOnly} maxLength={item.maxLength} min={item.type === "number" ? 0 : undefined} onChange={(event) => onChange(event.target.value)} step={item.type === "number" ? "1" : undefined} type={item.type ?? "text"} value={String(value ?? "")}/>}</label>;
}

function display(key: string, value: unknown) { if (key === "taxas" && Array.isArray(value)) return value.map((taxa: Row) => `${taxa.tipoTaxaIvaId}: ${taxa.valor}%`).join(" | "); if (typeof value === "boolean") return key === "inativo" || key === "extinta" ? (value ? "Inativo" : "Ativo") : value ? "Sim" : "Nao"; return value == null || value === "" ? "-" : String(value); }
function blankToNull(value: string) { const trimmed = value.trim(); return trimmed || null; }
async function get<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function errorMessage(error: unknown) { return error instanceof Error ? error.message : "Nao foi possivel concluir a operacao."; }
