import { useEffect, useState } from "react";
import { apiFetch } from "./api";
import TabelasEspecificasView, { specificTables } from "./TabelasEspecificasView";

type Page<T> = { content: T[] };
type Row = Record<string, string | number | boolean | null>;
type Values = Record<string, string | boolean>;
type Feedback = { kind: "info" | "success" | "error"; text: string };
type Field = { key: string; label: string; type?: "text" | "number" | "checkbox"; required?: boolean; maxLength?: number; createOnly?: boolean };
type Config = { key: string; label: string; group: string; endpoint: string; fields: Field[]; columns: Field[] };
type TableEntry = { key: string; label: string; description: string; kind: "generic" | "specific" };
type TableCategory = { key: string; title: string; description: string; items: TableEntry[] };

const text = (key: string, label: string, maxLength: number, required = false, createOnly = false): Field => ({ key, label, maxLength, required, createOnly });
const simpleNumber = (key: string, label: string, group: string, endpoint: string, field = "nome"): Config => ({ key, label, group, endpoint, fields: [text(field, field === "nome" ? "Nome" : "Descrição", 30, true)], columns: [text("id", "ID", 0), text(field, field === "nome" ? "Nome" : "Descrição", 0)] });
const simpleCode = (key: string, label: string, group: string, endpoint: string, maxName: number): Config => ({ key, label, group, endpoint, fields: [text("id", "Código", 3, true, true), text("nome", "Nome", maxName, true)], columns: [text("id", "Código", 0), text("nome", "Nome", 0)] });

const configs: Config[] = [
  simpleNumber("familias", "Famílias", "Comercial", "/api/familias", "descricao"),
  simpleCode("mpagamentos", "Modos de pagamento", "Comercial", "/api/mpagamentos", 30),
  { key: "ppagamentos", label: "Prazos de pagamento", group: "Comercial", endpoint: "/api/p-pagamentos", fields: [text("id", "Código", 3, true, true), text("nome", "Nome", 30, true), { key: "dias", label: "Dias", type: "number", required: true }], columns: [text("id", "Código", 0), text("nome", "Nome", 0), text("dias", "Dias", 0)] },
  simpleCode("transportes", "Transportes", "Comercial", "/api/transportes", 30),
  simpleCode("paises", "Países", "Localização", "/api/paises", 50),
  { key: "moedas", label: "Moedas", group: "Sistema", endpoint: "/api/moedas", fields: [text("id", "Código ISO", 3, true, true), text("nome", "Nome", 30, true), text("simbolo", "Símbolo", 5, true), { key: "ndecimais", label: "Decimais", type: "number", required: true }, text("ciso", "Código ISO adicional", 10), { key: "vcompra", label: "Câmbio compra", type: "number" }, { key: "vvenda", label: "Câmbio venda", type: "number" }], columns: [text("id", "Código", 0), text("nome", "Nome", 0), text("simbolo", "Símbolo", 0), text("ndecimais", "Decimais", 0)] },
  { key: "taxas", label: "Taxas de IVA", group: "Fiscalidade", endpoint: "/api/tipos-taxa-iva", fields: [text("id", "Código", 20, true, true), text("descricao", "Descrição", 50, true), { key: "inativo", label: "Inativo", type: "checkbox" }], columns: [text("id", "Código", 0), text("descricao", "Descrição", 0), text("inativo", "Estado", 0)] },
  simpleCode("iva-saft", "IVA SAF-T", "Fiscalidade", "/api/iva-saft", 50),
  { key: "isencoes", label: "Motivos de isenção", group: "Fiscalidade", endpoint: "/api/motivos-isencao", fields: [text("id", "Código", 3, true, true), text("nome", "Nome", 60, true), text("ivaSaftId", "IVA SAF-T", 3, true)], columns: [text("id", "Código", 0), text("nome", "Nome", 0), text("ivaSaftId", "IVA SAF-T", 0)] }
];

const tableCategories: TableCategory[] = [
  {
    key: "comercial",
    title: "Configuração comercial",
    description: "Elementos usados na operação comercial e na emissão de documentos.",
    items: [
      { key: "series", label: "Séries", description: "Séries documentais e numeradores", kind: "specific" },
      { key: "mpagamentos", label: "Modos de pagamento", description: "Formas de liquidação", kind: "generic" },
      { key: "ppagamentos", label: "Prazos de pagamento", description: "Condições e dias de vencimento", kind: "generic" },
      { key: "transportes", label: "Transportes", description: "Meios de expedição", kind: "generic" },
      { key: "armazens", label: "Armazéns", description: "Locais de carga e stock", kind: "specific" },
      { key: "familias", label: "Famílias", description: "Agrupamento do catálogo", kind: "generic" }
    ]
  },
  {
    key: "fiscal",
    title: "Configuração fiscal",
    description: "Tabelas fiscais utilizadas na faturação e no cumprimento declarativo.",
    items: [
      { key: "riva", label: "Regimes de IVA", description: "Regimes e taxas associadas", kind: "specific" },
      { key: "isencoes", label: "Motivos de isenção", description: "Justificações fiscais de isenção", kind: "generic" },
      { key: "taxas", label: "Taxas de IVA", description: "Tipos de taxa fiscal", kind: "generic" },
      { key: "iva-saft", label: "IVA SAF-T", description: "Códigos declarativos", kind: "generic" }
    ]
  },
  {
    key: "avancada",
    title: "Configuração avançada",
    description: "Dados estruturais e tabelas de manutenção menos frequente.",
    items: [
      { key: "paises", label: "Países", description: "Códigos de país", kind: "generic" },
      { key: "moedas", label: "Moedas", description: "Moedas e casas decimais", kind: "generic" },
      { key: "codpostal", label: "Códigos postais", description: "Códigos e localidades", kind: "specific" },
      { key: "freguesias", label: "Freguesias", description: "Divisões administrativas", kind: "specific" },
      { key: "tipos-documento", label: "Tipos de documento", description: "Tipos fiscais e área de gestão", kind: "specific" }
    ]
  }
];

export default function TabelasView() {
  const [active, setActive] = useState<Config | null>(null);
  const [specificActive, setSpecificActive] = useState<(typeof specificTables)[number]["key"] | null>(null);
  const [rows, setRows] = useState<Row[]>([]);
  const [values, setValues] = useState<Values>({});
  const [editingId, setEditingId] = useState<string | number | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<Feedback | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Row | null>(null);

  useEffect(() => { if (active) load(active); }, [active?.key]);

  const activeKey = active?.key ?? specificActive;

  async function load(config: Config) {
    setLoading(true); setMessage(null);
    try {
      const page = await get<Page<Row>>(`${config.endpoint}?size=500&sort=id,asc`);
      setRows(page.content); reset(config);
    } catch (error) { setMessage({ kind: "error", text: errorMessage(error) }); }
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
    for (const field of active.fields) if (field.required && !(field.createOnly && editingId != null) && String(values[field.key] ?? "").trim() === "") { setMessage({ kind: "error", text: `${field.label} é obrigatório.` }); return; }
    setLoading(true); setMessage(null);
    try {
      const editing = editingId != null;
      const response = await apiFetch(editing ? `${active.endpoint}/${encodeURIComponent(String(editingId))}` : active.endpoint, { method: editing ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(toPayload(active, values, editing)) });
      if (!response.ok) throw new Error(await responseError(response));
      await load(active); setMessage({ kind: "success", text: editing ? "Registo atualizado com sucesso." : "Registo criado com sucesso." });
    } catch (error) { setMessage({ kind: "error", text: errorMessage(error) }); setLoading(false); }
  }

  async function remove(row: Row) {
    if (!active) return;
    setPendingDelete(row);
  }

  function selectTable(item: TableEntry) {
    setPendingDelete(null);
    setMessage(null);
    if (item.kind === "specific") {
      setActive(null);
      setSpecificActive(item.key as (typeof specificTables)[number]["key"]);
      return;
    }
    const config = configs.find((current) => current.key === item.key) ?? null;
    setSpecificActive(null);
    setActive(config);
  }

  async function confirmRemove() {
    if (!active || !pendingDelete) return;
    const row = pendingDelete;
    const reference = rowReference(active, row);
    setPendingDelete(null);
    setLoading(true); setMessage({ kind: "info", text: `A verificar se ${reference} pode ser eliminado...` });
    try {
      const response = await apiFetch(`${active.endpoint}/${encodeURIComponent(String(row.id))}`, { method: "DELETE" });
      if (!response.ok) {
        const detail = await responseError(response);
        if (response.status === 409) {
          setMessage({ kind: "error", text: `${reference} não foi eliminado. O registo está em utilização e permanece nesta tabela. ${detail}` });
          setLoading(false);
          return;
        }
        throw new Error(detail);
      }
      await load(active); setMessage({ kind: "success", text: `${reference} foi eliminado com sucesso.` });
    } catch (error) {
      setMessage({ kind: "error", text: `${reference} não foi eliminado e permanece nesta tabela. ${errorMessage(error)}` });
      setLoading(false);
    }
  }

  if (specificActive) return <>
    <TableDirectory activeKey={activeKey} compact onSelect={selectTable} />
    <TabelasEspecificasView onBack={() => setSpecificActive(null)} tableKey={specificActive} />
  </>;

  if (!active) return <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabelas</p><h2>Escolha uma área de configuração</h2></div><span className="fac-muted">Catálogos agrupados por função</span></div>
    <TableDirectory activeKey={activeKey} onSelect={selectTable} />
  </section>;

  return <>
  <TableDirectory activeKey={activeKey} compact onSelect={selectTable} />
  <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabela</p><h2>{active.label}</h2></div><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => setActive(null)} type="button">Voltar</button><button className="fac-primary-button" onClick={() => reset()} type="button">Novo registo</button></div></div>
    {message && <p className={`fac-editor-message fac-editor-message-${message.kind}`} role={message.kind === "error" ? "alert" : "status"}>{message.text}</p>}
    <div className="fac-table-editor"><div className="fac-form-grid">{active.fields.map((field) => field.type === "checkbox" ? <label className="fac-check-field" key={field.key}><input checked={Boolean(values[field.key])} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.checked }))} type="checkbox"/><span>{field.label}</span></label> : <label className="fac-field" key={field.key}><span>{field.label}</span><input disabled={field.createOnly && editingId != null} maxLength={field.maxLength} min={field.type === "number" ? 0 : undefined} onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))} step={field.type === "number" ? "0.000001" : undefined} type={field.type ?? "text"} value={String(values[field.key] ?? "")}/></label>)}</div><div className="fac-form-footer"><span className="fac-muted">{editingId == null ? "Novo registo" : `A editar ${editingId}`}</span><button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar"}</button></div></div>
    <p className="fac-muted">A eliminação só é aceite para registos nunca utilizados.</p>
    <table className="fac-table"><thead><tr>{active.columns.map((column) => <th key={column.key}>{column.label}</th>)}<th>Ações</th></tr></thead><tbody>{rows.map((row) => <tr key={String(row.id)}>{active.columns.map((column) => <td key={column.key}>{display(column.key, row[column.key])}</td>)}<td><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => edit(row)} type="button">Editar</button><button className="fac-link-danger" disabled={loading} onClick={() => remove(row)} type="button">Eliminar</button></div></td></tr>)}{!loading && rows.length === 0 && <tr><td colSpan={active.columns.length + 1}>Sem registos.</td></tr>}</tbody></table>
    {pendingDelete && <div className="fac-dialog-backdrop" role="presentation">
      <div aria-labelledby="fac-delete-title" aria-modal="true" className="fac-dialog" role="dialog">
        <p className="fac-eyebrow">Confirmar eliminação</p>
        <h3 id="fac-delete-title">{rowReference(active, pendingDelete)}</h3>
        <p>O FAC vai verificar se este registo ja foi utilizado em clientes, documentos, na empresa ou noutras tabelas relacionadas.</p>
        <p><strong>Se estiver em utilização, não será eliminado e permanecerá nesta tabela.</strong></p>
        <div className="fac-dialog-actions">
          <button className="fac-ghost-button" onClick={() => setPendingDelete(null)} type="button">Cancelar</button>
          <button className="fac-danger-button" onClick={confirmRemove} type="button">Verificar e eliminar</button>
        </div>
      </div>
    </div>}
  </section>
  </>;
}

function TableDirectory({ activeKey, compact = false, onSelect }: { activeKey: string | null; compact?: boolean; onSelect: (item: TableEntry) => void }) {
  return (
    <div className={compact ? "fac-table-groups fac-table-groups-compact" : "fac-table-groups"}>
      {tableCategories.map((category) => (
        <article className="fac-table-group" key={category.key}>
          <p className="fac-eyebrow">{category.title}</p>
          {!compact && <small>{category.description}</small>}
          <div className="fac-table-group-links">
            {category.items.map((item) => (
              <button
                className={`fac-table-link${activeKey === item.key ? " active" : ""}`}
                key={item.key}
                onClick={() => onSelect(item)}
                type="button"
              >
                <span>{item.label}</span>
                <small>{item.description}</small>
              </button>
            ))}
          </div>
        </article>
      ))}
    </div>
  );
}

function toPayload(config: Config, values: Values, editing: boolean) { return Object.fromEntries(config.fields.filter((field) => !(editing && field.createOnly)).map((field) => [field.key, field.type === "number" ? (values[field.key] === "" ? null : Number(values[field.key])) : field.type === "checkbox" ? Boolean(values[field.key]) : String(values[field.key] ?? "").trim()])); }
function display(key: string, value: Row[string]) { if (typeof value === "boolean") return key === "inativo" ? (value ? "Inativo" : "Ativo") : value ? "Sim" : "Não"; return value ?? "-"; }
function rowReference(config: Config, row: Row) { const description = config.columns.find((column) => column.key !== "id" && row[column.key] != null); const detail = description ? ` - ${String(row[description.key])}` : ""; return `${config.label} ${String(row.id)}${detail}`; }
async function get<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function errorMessage(error: unknown) { return error instanceof Error ? error.message : "Não foi possível concluir a operação."; }
