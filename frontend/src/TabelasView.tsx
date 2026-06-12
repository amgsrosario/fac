import { useEffect, useState } from "react";

type Page<T> = { content: T[] };
type Row = Record<string, string | number | boolean | null>;
type Values = Record<string, string | boolean>;
type Field = { key: string; label: string; type?: "text" | "number" | "checkbox"; required?: boolean; maxLength?: number; createOnly?: boolean };
type Config = { key: string; label: string; group: string; endpoint: string; fields: Field[]; columns: Field[] };

const text = (key: string, label: string, maxLength: number, required = false, createOnly = false): Field => ({ key, label, maxLength, required, createOnly });
const simpleNumber = (key: string, label: string, group: string, endpoint: string, field = "nome"): Config => ({ key, label, group, endpoint, fields: [text(field, field === "nome" ? "Nome" : "Descricao", 30, true)], columns: [text("id", "ID", 0), text(field, field === "nome" ? "Nome" : "Descricao", 0)] });
const simpleCode = (key: string, label: string, group: string, endpoint: string, maxName: number): Config => ({ key, label, group, endpoint, fields: [text("id", "Codigo", 3, true, true), text("nome", "Nome", maxName, true)], columns: [text("id", "Codigo", 0), text("nome", "Nome", 0)] });

const configs: Config[] = [
  simpleNumber("familias", "Familias", "Comercial", "/api/familias", "descricao"),
  simpleNumber("mpagamentos", "Modos de pagamento", "Comercial", "/api/mpagamentos"),
  { key: "ppagamentos", label: "Prazos de pagamento", group: "Comercial", endpoint: "/api/p-pagamentos", fields: [text("id", "Codigo", 3, true, true), text("nome", "Nome", 30, true), { key: "dias", label: "Dias", type: "number", required: true }], columns: [text("id", "Codigo", 0), text("nome", "Nome", 0), text("dias", "Dias", 0)] },
  simpleNumber("transportes", "Transportes", "Comercial", "/api/transportes"),
  simpleCode("paises", "Paises", "Localizacao", "/api/paises", 50),
  { key: "moedas", label: "Moedas", group: "Sistema", endpoint: "/api/moedas", fields: [text("id", "Codigo ISO", 3, true, true), text("nome", "Nome", 30, true), text("simbolo", "Simbolo", 5, true), { key: "ndecimais", label: "Decimais", type: "number", required: true }, text("ciso", "Codigo ISO adicional", 10), { key: "vcompra", label: "Cambio compra", type: "number" }, { key: "vvenda", label: "Cambio venda", type: "number" }], columns: [text("id", "Codigo", 0), text("nome", "Nome", 0), text("simbolo", "Simbolo", 0), text("ndecimais", "Decimais", 0)] },
  { key: "taxas", label: "Taxas de IVA", group: "Fiscalidade", endpoint: "/api/tipos-taxa-iva", fields: [text("id", "Codigo", 20, true, true), text("descricao", "Descricao", 50, true), { key: "inativo", label: "Inativo", type: "checkbox" }], columns: [text("id", "Codigo", 0), text("descricao", "Descricao", 0), text("inativo", "Estado", 0)] },
  simpleCode("iva-saft", "IVA SAF-T", "Fiscalidade", "/api/iva-saft", 50),
  { key: "isencoes", label: "Motivos de isencao", group: "Fiscalidade", endpoint: "/api/motivos-isencao", fields: [text("id", "Codigo", 3, true, true), text("nome", "Nome", 60, true), text("ivaSaftId", "IVA SAF-T", 3, true)], columns: [text("id", "Codigo", 0), text("nome", "Nome", 0), text("ivaSaftId", "IVA SAF-T", 0)] }
];

const specific = [
  ["Documentos", ["Tipos de documento", "Series"]],
  ["Fiscalidade", ["Regimes de IVA"]],
  ["Localizacao", ["Codigos postais", "Freguesias"]],
  ["Sistema", ["Armazens", "Utilizadores"]]
] as const;

export default function TabelasView() {
  const [active, setActive] = useState<Config | null>(null);
  const [rows, setRows] = useState<Row[]>([]);
  const [values, setValues] = useState<Values>({});
  const [editingId, setEditingId] = useState<string | number | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => { if (active) load(active); }, [active?.key]);

  async function load(config: Config) {
    setLoading(true); setMessage(null);
    try {
      const page = await get<Page<Row>>(`${config.endpoint}?size=500&sort=id,asc`);
      setRows(page.content); reset(config);
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setLoading(false); }
  }

  function reset(config = active) {
    if (!config) return;
    setEditingId(null);
    setValues(Object.fromEntries(config.fields.map((field) => [field.key, field.type === "checkbox" ? false : ""])));
  }

  function edit(row: Row) {
    if (!active) return;
    setEditingId(row.id as string | number);
    setValues(Object.fromEntries(active.fields.map((field) => [field.key, field.type === "checkbox" ? Boolean(row[field.key]) : String(row[field.key] ?? "")])));
    setMessage(null);
  }

  async function save() {
    if (!active) return;
    for (const field of active.fields) if (field.required && !(field.createOnly && editingId != null) && String(values[field.key] ?? "").trim() === "") { setMessage(`${field.label} e obrigatorio.`); return; }
    setLoading(true); setMessage(null);
    try {
      const editing = editingId != null;
      const response = await fetch(editing ? `${active.endpoint}/${encodeURIComponent(String(editingId))}` : active.endpoint, { method: editing ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(toPayload(active, values, editing)) });
      if (!response.ok) throw new Error(await responseError(response));
      await load(active); setMessage(editing ? "Registo atualizado." : "Registo criado.");
    } catch (error) { setMessage(errorMessage(error)); setLoading(false); }
  }

  async function remove(row: Row) {
    if (!active || !window.confirm(`Eliminar ${active.label}: ${row.id}? A operacao sera recusada se o registo estiver em utilizacao.`)) return;
    setLoading(true); setMessage(null);
    try {
      const response = await fetch(`${active.endpoint}/${encodeURIComponent(String(row.id))}`, { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      await load(active); setMessage("Registo eliminado.");
    } catch (error) { setMessage(errorMessage(error)); setLoading(false); }
  }

  if (!active) return <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabelas</p><h2>Catalogos de apoio</h2></div><span className="fac-muted">Seleciona uma tabela</span></div>
    <div className="fac-table-groups">
      {[...new Set(configs.map((item) => item.group))].map((group) => <article className="fac-table-group" key={group}><p className="fac-eyebrow">{group}</p>{configs.filter((item) => item.group === group).map((item) => <button className="fac-table-link" key={item.key} onClick={() => setActive(item)} type="button"><span>{item.label}</span><small>Abrir</small></button>)}</article>)}
      {specific.map(([group, items]) => <article className="fac-table-group" key={`${group}-${items[0]}`}><p className="fac-eyebrow">{group}</p>{items.map((item) => <div key={item}><span>{item}</span><small>Editor especifico</small></div>)}</article>)}
    </div>
  </section>;

  return <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabela</p><h2>{active.label}</h2></div><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => setActive(null)} type="button">Voltar</button><button className="fac-primary-button" onClick={() => reset()} type="button">Novo registo</button></div></div>
    {message && <p className="fac-editor-message">{message}</p>}
    <div className="fac-table-editor"><div className="fac-form-grid">{active.fields.map((field) => field.type === "checkbox" ? <label className="fac-check-field" key={field.key}><input checked={Boolean(values[field.key])} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.checked }))} type="checkbox"/><span>{field.label}</span></label> : <label className="fac-field" key={field.key}><span>{field.label}</span><input disabled={field.createOnly && editingId != null} maxLength={field.maxLength} min={field.type === "number" ? 0 : undefined} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))} step={field.type === "number" ? "0.000001" : undefined} type={field.type ?? "text"} value={String(values[field.key] ?? "")}/></label>)}</div><div className="fac-form-footer"><span className="fac-muted">{editingId == null ? "Novo registo" : `A editar ${editingId}`}</span><button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar"}</button></div></div>
    <p className="fac-muted">A eliminacao so e aceite para registos nunca utilizados. As relacoes sao validadas pelo backend e pelo PostgreSQL.</p>
    <table className="fac-table"><thead><tr>{active.columns.map((column) => <th key={column.key}>{column.label}</th>)}<th>Acoes</th></tr></thead><tbody>{rows.map((row) => <tr key={String(row.id)}>{active.columns.map((column) => <td key={column.key}>{display(column.key, row[column.key])}</td>)}<td><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => edit(row)} type="button">Editar</button><button className="fac-link-danger" disabled={loading} onClick={() => remove(row)} type="button">Eliminar</button></div></td></tr>)}{!loading && rows.length === 0 && <tr><td colSpan={active.columns.length + 1}>Sem registos.</td></tr>}</tbody></table>
  </section>;
}

function toPayload(config: Config, values: Values, editing: boolean) { return Object.fromEntries(config.fields.filter((field) => !(editing && field.createOnly)).map((field) => [field.key, field.type === "number" ? (values[field.key] === "" ? null : Number(values[field.key])) : field.type === "checkbox" ? Boolean(values[field.key]) : String(values[field.key] ?? "").trim()])); }
function display(key: string, value: Row[string]) { if (typeof value === "boolean") return key === "inativo" ? (value ? "Inativo" : "Ativo") : value ? "Sim" : "Nao"; return value ?? "-"; }
async function get<T>(url: string): Promise<T> { const response = await fetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function errorMessage(error: unknown) { return error instanceof Error ? error.message : "Nao foi possivel concluir a operacao."; }
