import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "./api";
import { FacInputText } from "./ui/fac";

type Page<T> = { content: T[] };
type Row = Record<string, unknown>;
type Values = Record<string, string | boolean>;
type Option = { value: string; label: string };
type Feedback = { kind: "success" | "error"; text: string };
type TableKey = "tipos-documento" | "series" | "riva" | "codpostal" | "freguesias" | "armazens";
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
    fields: [field("id", "Código", { required: true, maxLength: 3, createOnly: true }), field("descricao", "Descrição", { required: true, maxLength: 50 }), field("codigoFiscal", "Código fiscal", { type: "select", maxLength: 2, options: "codigosFiscais" }), field("modeloEmissao1", "Modelo de emissão 1", { maxLength: 25 }), field("modeloEmissao2", "Modelo de emissão 2", { maxLength: 25 }), field("modeloEmissao3", "Modelo de emissão 3", { maxLength: 25 }), field("modeloEmissao4", "Modelo de emissão 4", { maxLength: 25 }), field("areaGestao", "Área de gestão", { type: "select", required: true, options: "areasGestao" }), field("entidade", "Entidade", { type: "select", required: true, options: "entidadesDocumento" }), field("sinalContabilistico", "Sinal contabilístico", { type: "select", required: true, options: "sinais" }), field("liquidacaoImediata", "Liquidação imediata", { type: "checkbox" })],
    columns: [{ key: "id", label: "Código" }, { key: "codigoFiscal", label: "Fiscal" }, { key: "descricao", label: "Descrição" }, { key: "areaGestao", label: "Área" }, { key: "liquidacaoImediata", label: "Liquidação imediata" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/tipos-documento/${encodeURIComponent(String(row.id))}`
  },
  series: {
    key: "series", label: "Séries", endpoint: "/api/series",
    fields: [field("tipoDocumentoId", "Tipo de documento", { type: "select", required: true, createOnly: true, options: "tiposDocumento" }), field("serie", "Série", { required: true, maxLength: 10, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 50 }), field("codigoAt", "Código AT", { maxLength: 100 }), field("dataCodigoAt", "Data do código AT", { type: "date" })],
    columns: [{ key: "tipoDocumentoId", label: "Documento" }, { key: "serie", label: "Série" }, { key: "nome", label: "Nome" }, { key: "numerador", label: "Numerador" }],
    rowId: (row) => `${row.tipoDocumentoId}-${row.serie}`, itemUrl: (row) => `/api/series/${encodeURIComponent(String(row.tipoDocumentoId))}/${encodeURIComponent(String(row.serie))}`
  },
  riva: {
    key: "riva", label: "Regimes de IVA", endpoint: "/api/riva",
    fields: [field("id", "Código", { required: true, maxLength: 3, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 30 })],
    columns: [{ key: "id", label: "Código" }, { key: "nome", label: "Nome" }, { key: "taxas", label: "Taxas" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/riva/${encodeURIComponent(String(row.id))}`
  },
  codpostal: {
    key: "codpostal", label: "Códigos postais", endpoint: "/api/codpostal",
    fields: [field("id", "Código postal", { required: true, maxLength: 20, createOnly: true }), field("nome", "Localidade", { required: true, maxLength: 50 })],
    columns: [{ key: "id", label: "Código" }, { key: "nome", label: "Localidade" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/codpostal/${encodeURIComponent(String(row.id))}`
  },
  freguesias: {
    key: "freguesias", label: "Freguesias", endpoint: "/api/freguesias",
    fields: [field("codigo", "Código", { required: true, maxLength: 6, createOnly: true }), field("codigoDistrito", "Código distrito", { required: true, maxLength: 2, createOnly: true }), field("codigoConcelho", "Código concelho", { required: true, maxLength: 2, createOnly: true }), field("codigoFreguesia", "Código freguesia", { required: true, maxLength: 2, createOnly: true }), field("concelho", "Concelho", { required: true, maxLength: 50 }), field("nome", "Freguesia", { required: true, maxLength: 80 }), field("extinta", "Extinta", { type: "checkbox" })],
    columns: [{ key: "codigo", label: "Código" }, { key: "concelho", label: "Concelho" }, { key: "nome", label: "Freguesia" }, { key: "extinta", label: "Estado" }],
    rowId: (row) => String(row.codigo), itemUrl: (row) => `/api/freguesias/${encodeURIComponent(String(row.codigo))}`
  },
  armazens: {
    key: "armazens", label: "Armazéns", endpoint: "/api/armazens",
    fields: [field("id", "Código", { required: true, maxLength: 3, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 100 }), field("morada", "Morada", { required: true, maxLength: 60 }), field("morada1", "Morada complementar", { maxLength: 60 }), field("codPostalId", "Código postal", { type: "select", required: true, options: "codigosPostais" }), field("localidade", "Localidade", { required: true, maxLength: 50 }), field("paisId", "País", { type: "select", required: true, options: "paises" }), field("freguesiaId", "Freguesia", { type: "select", options: "freguesias" })],
    columns: [{ key: "id", label: "Código" }, { key: "nome", label: "Nome" }, { key: "localidade", label: "Localidade" }, { key: "paisId", label: "País" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/armazens/${encodeURIComponent(String(row.id))}`
  }
};

export const specificTables: { key: TableKey; label: string; group: string }[] = [
  { key: "tipos-documento", label: "Tipos de documento", group: "Documentos" }, { key: "series", label: "Séries", group: "Documentos" },
  { key: "riva", label: "Regimes de IVA", group: "Fiscalidade" }, { key: "codpostal", label: "Códigos postais", group: "Localização" },
  { key: "freguesias", label: "Freguesias", group: "Localização" }, { key: "armazens", label: "Armazéns", group: "Sistema" }
];

export default function TabelasEspecificasView({ tableKey, onBack }: { tableKey: TableKey; onBack: () => void }) {
  const config = configs[tableKey];
  const [rows, setRows] = useState<Row[]>([]);
  const [values, setValues] = useState<Values>({});
  const [editing, setEditing] = useState<Row | null>(null);
  const [options, setOptions] = useState<Record<string, Option[]>>({
    sinais: [{ value: "1", label: "1 - Débito" }, { value: "2", label: "2 - Crédito" }],
    areasGestao: [
      { value: "1", label: "1 - Documento comercial (área 1)" },
      { value: "2", label: "2 - Documento comercial / faturação" },
      { value: "3", label: "3 - Documento financeiro / tesouraria" }
    ],
    entidadesDocumento: [{ value: "1", label: "1 - Cliente" }],
    codigosFiscais: [
      { value: "FT", label: "FT - Fatura" },
      { value: "FS", label: "FS - Fatura simplificada" },
      { value: "FR", label: "FR - Fatura-recibo" },
      { value: "ND", label: "ND - Nota de débito" },
      { value: "NC", label: "NC - Nota de crédito" },
      { value: "VD", label: "VD - Venda a dinheiro" },
      { value: "TV", label: "TV - Talão de venda" },
      { value: "TD", label: "TD - Talão de devolução" },
      { value: "AA", label: "AA - Alienação de ativos" },
      { value: "DA", label: "DA - Devolução de ativos" },
      { value: "RP", label: "RP - Recibo de prémio" },
      { value: "RE", label: "RE - Estorno ou recibo de estorno" },
      { value: "CS", label: "CS - Imputação a co-seguradoras" },
      { value: "LD", label: "LD - Imputação a co-seguradora líder" },
      { value: "RA", label: "RA - Resseguro aceite" },
      { value: "CM", label: "CM - Consulta de mesa" },
      { value: "CC", label: "CC - Crédito de consignação" },
      { value: "FC", label: "FC - Fatura de consignação" },
      { value: "GR", label: "GR - Guia de remessa" },
      { value: "GT", label: "GT - Guia de transporte" },
      { value: "GA", label: "GA - Guia de movimentação de ativos próprios" },
      { value: "GC", label: "GC - Guia de consignação" },
      { value: "GD", label: "GD - Guia ou nota de devolução" },
      { value: "RC", label: "RC - Recibo" },
      { value: "RG", label: "RG - Outros recibos emitidos" }
    ]
  });
  const [rates, setRates] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [tableSearch, setTableSearch] = useState("");
  const isRiva = tableKey === "riva";

  useEffect(() => { load(); }, [tableKey]);

  async function load() {
    setLoading(true); setFeedback(null);
    try {
      const sortField = tableKey === "freguesias" ? "codigo" : tableKey === "series" ? "serie" : "id";
      const params = new URLSearchParams({ page: "0", size: tableKey === "codpostal" ? "50" : "100", sort: `${sortField},asc` });
      if (tableKey === "codpostal" && tableSearch.trim()) params.set("search", tableSearch.trim());
      const [page, support] = await Promise.all([get<Page<Row>>(`${config.endpoint}?${params}`), loadOptions()]);
      setRows(page.content); setOptions((current) => ({ ...current, ...support })); reset();
    } catch (error) { setFeedback({ kind: "error", text: errorMessage(error) }); }
    finally { setLoading(false); }
  }

  async function loadOptions() {
    const required = new Set(config.fields.map((item) => item.options).filter(Boolean));
    if (isRiva) required.add("tiposTaxa");
    const result: Record<string, Option[]> = {};
    const staticOptions = new Set(["sinais", "areasGestao", "entidadesDocumento", "codigosFiscais"]);
    await Promise.all([...required].filter((key) => !staticOptions.has(key!) && key !== "codigosPostais").map(async (key) => {
      const definitions: Record<string, [string, (row: Row) => Option]> = {
        tiposDocumento: ["/api/tipos-documento?size=500&sort=id,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.descricao}` })],
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
        setFeedback({ kind: "error", text: `${item.label} é obrigatório.` }); return;
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
    if (!window.confirm(`Eliminar ${config.label}: ${config.rowId(row)}?\n\nSe estiver em utilização, o FAC recusará a operação.`)) return;
    setLoading(true); setFeedback(null);
    try {
      const response = await apiFetch(config.itemUrl(row), { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      await load(); setFeedback({ kind: "success", text: "Registo eliminado com sucesso." });
    } catch (error) { setFeedback({ kind: "error", text: `O registo não foi eliminado e permanece na tabela. ${errorMessage(error)}` }); setLoading(false); }
  }

  const rateOptions = useMemo(() => options.tiposTaxa ?? [], [options]);

  return <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabela</p><h2>{config.label}</h2></div><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={onBack} type="button">Voltar</button><button className="fac-primary-button" onClick={reset} type="button">Novo registo</button></div></div>
    {feedback && <p className={`fac-editor-message fac-editor-message-${feedback.kind}`} role={feedback.kind === "error" ? "alert" : "status"}>{feedback.text}</p>}
    <div className="fac-table-editor"><div className="fac-form-grid">{config.fields.map((item) => <EditorField field={item} key={item.key} options={options[item.options ?? ""] ?? []} editing={Boolean(editing)} value={values[item.key]} onChange={(value) => setValues((current) => ({ ...current, [item.key]: value }))} />)}</div>
      {isRiva && <div className="fac-rate-grid"><p className="fac-muted">Taxas do regime</p>{rateOptions.map((option) => <label className="fac-field" key={option.value}><span>{option.label}</span><input min="0" onChange={(event) => setRates((current) => ({ ...current, [option.value]: event.target.value }))} step="0.01" type="number" value={rates[option.value] ?? ""}/></label>)}</div>}
      <div className="fac-form-footer"><span className="fac-muted">{editing ? `A editar ${config.rowId(editing)}` : "Novo registo"}</span><button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar"}</button></div>
    </div>
    {tableKey === "codpostal" && <div className="fac-inline-actions"><FacInputText aria-label="Pesquisar códigos postais" onChange={(event) => setTableSearch(event.target.value)} placeholder="Pesquisar código ou localidade" value={tableSearch} /><button className="fac-ghost-button" onClick={load} type="button">Pesquisar</button></div>}
    <p className="fac-muted">A eliminação só é aceite para registos nunca utilizados.</p>
    <table className="fac-table"><thead><tr>{config.columns.map((column) => <th key={column.key}>{column.label}</th>)}<th>Ações</th></tr></thead><tbody>{rows.map((row) => <tr key={config.rowId(row)}>{config.columns.map((column) => <td key={column.key}>{display(column.key, row[column.key])}</td>)}<td><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => edit(row)} type="button">Editar</button><button className="fac-link-danger" disabled={loading} onClick={() => remove(row)} type="button">Eliminar</button></div></td></tr>)}{!loading && rows.length === 0 && <tr><td colSpan={config.columns.length + 1}>Sem registos.</td></tr>}</tbody></table>
  </section>;
}

function EditorField({ field: item, value, editing, options, onChange }: { field: Field; value: string | boolean; editing: boolean; options: Option[]; onChange: (value: string | boolean) => void }) {
  if (item.type === "checkbox") return <label className="fac-check-field"><input checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)} type="checkbox"/><span>{item.label}</span></label>;
  if (item.options === "codigosPostais") return <PostalCodeAdminField value={String(value ?? "")} onChange={onChange} disabled={Boolean(editing && item.createOnly)} />;
  return <label className="fac-field"><span>{item.label}{item.optionalOnUpdate && editing ? " (deixar vazio para manter)" : ""}</span>{item.type === "select" ? <select disabled={editing && item.createOnly} onChange={(event) => onChange(event.target.value)} value={String(value ?? "")}><option value="">Selecionar</option>{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select> : <input disabled={editing && item.createOnly} maxLength={item.maxLength} min={item.type === "number" ? 0 : undefined} onChange={(event) => onChange(event.target.value)} step={item.type === "number" ? "1" : undefined} type={item.type ?? "text"} value={String(value ?? "")}/>}</label>;
}

function PostalCodeAdminField({ value, onChange, disabled }: { value: string; onChange: (value: string) => void; disabled: boolean }) {
  const [query, setQuery] = useState(value);
  const [results, setResults] = useState<{ codigoPostal: string; nome: string }[]>([]);
  const [open, setOpen] = useState(false);
  useEffect(() => setQuery(value), [value]);
  useEffect(() => {
    if (query.trim().length < 2 || disabled) { setResults([]); return; }
    const timeout = window.setTimeout(async () => {
      const response = await apiFetch(`/api/codpostal/search?q=${encodeURIComponent(query.trim())}&limit=30`);
      if (!response.ok) return;
      const page = await response.json() as { content: { codigoPostal: string; nome: string }[] };
      setResults(page.content ?? []); setOpen(true);
    }, 280);
    return () => window.clearTimeout(timeout);
  }, [query, disabled]);
  return <label className="fac-field fac-postal-admin-field"><span>Código postal</span><input disabled={disabled} onBlur={() => window.setTimeout(() => setOpen(false), 150)} onChange={(event) => { setQuery(event.target.value); onChange(event.target.value); }} onFocus={() => results.length > 0 && setOpen(true)} value={query}/>{open && <div className="fac-postal-admin-results">{results.map((result) => <button key={result.codigoPostal} onMouseDown={(event) => event.preventDefault()} onClick={() => { setQuery(result.codigoPostal); onChange(result.codigoPostal); setOpen(false); }} type="button">{result.codigoPostal} — {result.nome}</button>)}</div>}</label>;
}

function display(key: string, value: unknown) { if (key === "taxas" && Array.isArray(value)) return value.map((taxa: Row) => `${taxa.tipoTaxaIvaId}: ${taxa.valor}%`).join(" | "); if (typeof value === "boolean") return key === "inativo" || key === "extinta" ? (value ? "Inativo" : "Ativo") : value ? "Sim" : "Não"; return value == null || value === "" ? "-" : String(value); }
function blankToNull(value: string) { const trimmed = value.trim(); return trimmed || null; }
async function get<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function errorMessage(error: unknown) { return error instanceof Error ? error.message : "Não foi possível concluir a operação."; }
