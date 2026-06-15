import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";

type Page<T> = {
  content: T[];
  totalElements: number;
};

type Artigo = {
  codigo: string;
  abreviatura?: string;
  codigoIdentificacao?: string;
  descricao: string;
  unidade: string;
  familiaId: number;
  peso?: number;
  ivaCompraId: string;
  ivaVendaId: string;
  pvp: number;
  inativo: boolean;
  retencao: boolean;
  observacoes?: string;
};

type Familia = {
  id: number;
  descricao: string;
};

type TipoTaxaIva = {
  id: string;
  descricao: string;
  inativo: boolean;
};

type ArtigoForm = {
  codigo: string;
  abreviatura: string;
  codigoIdentificacao: string;
  descricao: string;
  unidade: string;
  familiaId: string;
  peso: string;
  ivaCompraId: string;
  ivaVendaId: string;
  pvp: string;
  inativo: boolean;
  retencao: boolean;
  observacoes: string;
};

const emptyForm: ArtigoForm = {
  codigo: "",
  abreviatura: "",
  codigoIdentificacao: "",
  descricao: "",
  unidade: "UN",
  familiaId: "",
  peso: "0",
  ivaCompraId: "",
  ivaVendaId: "",
  pvp: "0",
  inativo: false,
  retencao: false,
  observacoes: ""
};

const ARTIGO_COLUMNS: ConfigurableColumn[] = [
  { key: "codigo", label: "Codigo", visible: true },
  { key: "descricao", label: "Descricao", visible: true },
  { key: "abreviatura", label: "Abreviatura", visible: false },
  { key: "codigoIdentificacao", label: "Identificacao", visible: false },
  { key: "familia", label: "Familia", visible: false },
  { key: "unidade", label: "Unidade", visible: true },
  { key: "ivaCompra", label: "IVA compra", visible: false },
  { key: "ivaVenda", label: "IVA venda", visible: false },
  { key: "pvp", label: "PVP", visible: true },
  { key: "peso", label: "Peso", visible: false },
  { key: "retencao", label: "Retencao", visible: false },
  { key: "estado", label: "Estado", visible: true }
];

export default function ArtigosView() {
  const [artigos, setArtigos] = useState<Artigo[]>([]);
  const [familias, setFamilias] = useState<Familia[]>([]);
  const [tiposIva, setTiposIva] = useState<TipoTaxaIva[]>([]);
  const [selectedCodigo, setSelectedCodigo] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingCodigo, setEditingCodigo] = useState<string | null>(null);
  const [columnEditorOpen, setColumnEditorOpen] = useState(false);
  const artigoColumns = useConfiguredColumns("fac.artigos.colunas", ARTIGO_COLUMNS);
  const [form, setForm] = useState<ArtigoForm>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setMessage(null);
    try {
      const [artigosPage, familiasPage, tiposIvaPage] = await Promise.all([
        fetchPage<Artigo>("/api/artigos?size=200&sort=codigo,asc"),
        fetchPage<Familia>("/api/familias?size=200&sort=descricao,asc"),
        fetchPage<TipoTaxaIva>("/api/tipos-taxa-iva?size=100&sort=descricao,asc")
      ]);
      setArtigos(artigosPage.content);
      setFamilias(familiasPage.content);
      setTiposIva(tiposIvaPage.content);
      setSelectedCodigo((current) => current ?? artigosPage.content[0]?.codigo ?? null);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar artigos.");
    } finally {
      setLoading(false);
    }
  }

  function openNew() {
    setEditingCodigo(null);
    setForm(emptyForm);
    setMessage(null);
    setNotice(null);
    setEditorOpen(true);
  }

  function openEdit(artigo: Artigo) {
    setEditingCodigo(artigo.codigo);
    setForm(toForm(artigo));
    setMessage(null);
    setNotice(null);
    setEditorOpen(true);
  }

  async function save() {
    const validation = validate(form, editingCodigo != null);
    if (validation) {
      setMessage(validation);
      return;
    }

    setLoading(true);
    setMessage(null);
    try {
      const payload = toPayload(form, editingCodigo == null);
      if (editingCodigo) {
        await request(`/api/artigos/${encodeURIComponent(editingCodigo)}`, "PUT", payload);
      } else {
        await request("/api/artigos", "POST", payload);
      }
      const codigo = editingCodigo ?? form.codigo;
      const page = await fetchPage<Artigo>("/api/artigos?size=200&sort=codigo,asc");
      setArtigos(page.content);
      setSelectedCodigo(codigo);
      setEditorOpen(false);
      setEditingCodigo(null);
      setNotice(`Artigo ${codigo} ${editingCodigo ? "atualizado" : "criado"}.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel guardar o artigo.");
    } finally {
      setLoading(false);
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return artigos;
    return artigos.filter((artigo) =>
      [artigo.codigo, artigo.descricao, artigo.abreviatura, artigo.codigoIdentificacao]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term))
    );
  }, [artigos, search]);

  const selected = artigos.find((artigo) => artigo.codigo === selectedCodigo) ?? null;
  const familiaNome = familias.find((familia) => familia.id === selected?.familiaId)?.descricao ?? "-";

  function change<K extends keyof ArtigoForm>(field: K, value: ArtigoForm[K]) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  if (editorOpen) {
    return (
      <section className="fac-panel">
        <div className="fac-panel-header">
          <div>
            <p className="fac-eyebrow">Editor de artigo</p>
            <h2>{editingCodigo ? `Editar ${editingCodigo}` : "Novo artigo"}</h2>
          </div>
          <button className="fac-ghost-button" onClick={() => setEditorOpen(false)} type="button">Voltar a lista</button>
        </div>

        {message && <p className="fac-message">{message}</p>}

        <div className="fac-form-grid">
          <Field label="Codigo">
            <input disabled={editingCodigo != null} maxLength={50} onChange={(event) => change("codigo", normalizeCode(event.target.value))} value={form.codigo} />
          </Field>
          <Field label="Abreviatura"><input maxLength={30} onChange={(event) => change("abreviatura", event.target.value)} value={form.abreviatura} /></Field>
          <Field label="Codigo de identificacao"><input maxLength={100} onChange={(event) => change("codigoIdentificacao", event.target.value)} value={form.codigoIdentificacao} /></Field>
          <Field label="Descricao"><input maxLength={80} onChange={(event) => change("descricao", event.target.value)} value={form.descricao} /></Field>
          <Field label="Unidade"><input maxLength={3} onChange={(event) => change("unidade", event.target.value.toUpperCase())} value={form.unidade} /></Field>
          <Field label="Familia">
            <select onChange={(event) => change("familiaId", event.target.value)} value={form.familiaId}>
              <option value="">Selecionar</option>
              {familias.map((familia) => <option key={familia.id} value={familia.id}>{familia.descricao}</option>)}
            </select>
          </Field>
          <Field label="Peso"><input min="0" onChange={(event) => change("peso", event.target.value)} step="0.001" type="number" value={form.peso} /></Field>
          <Field label="IVA na compra">
            <select onChange={(event) => change("ivaCompraId", event.target.value)} value={form.ivaCompraId}>
              <option value="">Selecionar</option>
              {tiposIva.map((tipo) => <option disabled={tipo.inativo && tipo.id !== form.ivaCompraId} key={tipo.id} value={tipo.id}>{tipo.descricao}{tipo.inativo ? " (inativo)" : ""}</option>)}
            </select>
          </Field>
          <Field label="IVA na venda">
            <select onChange={(event) => change("ivaVendaId", event.target.value)} value={form.ivaVendaId}>
              <option value="">Selecionar</option>
              {tiposIva.map((tipo) => <option disabled={tipo.inativo && tipo.id !== form.ivaVendaId} key={tipo.id} value={tipo.id}>{tipo.descricao}{tipo.inativo ? " (inativo)" : ""}</option>)}
            </select>
          </Field>
          <Field label="PVP"><input min="0" onChange={(event) => change("pvp", event.target.value)} step="0.000001" type="number" value={form.pvp} /></Field>
          <label className="fac-check-field"><input checked={form.retencao} onChange={(event) => change("retencao", event.target.checked)} type="checkbox" /><span>Sujeito a retencao</span></label>
          <label className="fac-check-field"><input checked={form.inativo} onChange={(event) => change("inativo", event.target.checked)} type="checkbox" /><span>Artigo inativo</span></label>
          <Field label="Observacoes"><textarea maxLength={250} onChange={(event) => change("observacoes", event.target.value)} value={form.observacoes} /></Field>
        </div>

        <div className="fac-form-footer">
          <span className="fac-muted">O codigo e definitivo depois de criar o artigo.</span>
          <button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar artigo"}</button>
        </div>
      </section>
    );
  }

  return (
    <>
      {notice && <p className="fac-editor-message">{notice}</p>}
      {message && <p className="fac-message">{message}</p>}
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Artigos</p>
          <h2>Catalogo comercial simples e operacional</h2>
          <p>Consulta, cria e edita apenas os dados necessários para usar artigos nas linhas dos documentos.</p>
        </div>
        <div className="fac-hero-card">
          <span>Catalogo</span>
          <strong>{loading ? "A carregar..." : `${artigos.length} artigos`}</strong>
          <small>{artigos.filter((artigo) => artigo.inativo).length} inativos</small>
        </div>
      </section>

      <section className="fac-list-toolbar">
        <input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar codigo, descricao ou identificacao" type="search" value={search} />
        <div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({artigoColumns.visibleColumns.length})</button><button className="fac-primary-button" onClick={openNew} type="button">Novo artigo</button></div>
      </section>

      <section className="fac-content-grid">
        <article className="fac-panel fac-panel-main">
          <ColumnSelector columns={artigoColumns.columns} open={columnEditorOpen} onMove={artigoColumns.moveColumn} onReset={artigoColumns.resetColumns} onToggle={artigoColumns.toggleColumn} />
          <table className="fac-table">
            <thead><tr>{artigoColumns.visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead>
            <tbody>
              {filtered.map((artigo) => (
                <tr className={artigo.codigo === selectedCodigo ? "fac-row-selected" : ""} key={artigo.codigo} onClick={() => setSelectedCodigo(artigo.codigo)}>
                  {artigoColumns.visibleColumns.map((column) => <td key={column.key}>{artigoColumnValue(artigo, column.key, familias)}</td>)}
                </tr>
              ))}
              {!loading && filtered.length === 0 && <tr><td colSpan={artigoColumns.visibleColumns.length}>Sem artigos para mostrar.</td></tr>}
            </tbody>
          </table>
        </article>

        <aside className="fac-panel fac-detail">
          <p className="fac-eyebrow">Ficha comercial</p>
          <h2>{selected?.codigo ?? "Sem artigo"}</h2>
          <dl>
            <div><dt>Descricao</dt><dd>{selected?.descricao ?? "-"}</dd></div>
            <div><dt>Familia</dt><dd>{familiaNome}</dd></div>
            <div><dt>Unidade</dt><dd>{selected?.unidade ?? "-"}</dd></div>
            <div><dt>IVA venda</dt><dd>{selected?.ivaVendaId ?? "-"}</dd></div>
            <div><dt>PVP</dt><dd>{selected ? money(selected.pvp) : "-"}</dd></div>
            <div><dt>Retencao</dt><dd>{selected?.retencao ? "Sim" : "Nao"}</dd></div>
          </dl>
          <button className="fac-primary-button" disabled={!selected} onClick={() => selected && openEdit(selected)} type="button">Editar artigo</button>
        </aside>
      </section>
    </>
  );
}

function artigoColumnValue(artigo: Artigo, key: string, familias: Familia[]) {
  switch (key) {
    case "codigo": return artigo.codigo;
    case "descricao": return artigo.descricao;
    case "abreviatura": return artigo.abreviatura ?? "-";
    case "codigoIdentificacao": return artigo.codigoIdentificacao ?? "-";
    case "familia": return familias.find((familia) => familia.id === artigo.familiaId)?.descricao ?? artigo.familiaId;
    case "unidade": return artigo.unidade;
    case "ivaCompra": return artigo.ivaCompraId;
    case "ivaVenda": return artigo.ivaVendaId;
    case "pvp": return money(artigo.pvp);
    case "peso": return artigo.peso ?? 0;
    case "retencao": return artigo.retencao ? "Sim" : "Nao";
    case "estado": return <span className="fac-status">{artigo.inativo ? "Inativo" : "Ativo"}</span>;
    default: return "-";
  }
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}

async function fetchPage<T>(url: string): Promise<Page<T>> {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function request(url: string, method: "POST" | "PUT", body: unknown) {
  const response = await apiFetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
}

async function responseError(response: Response) {
  try {
    const payload = await response.json();
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}

function validate(form: ArtigoForm, editing: boolean) {
  if (!editing && !/^[A-Z0-9]{1,50}$/.test(form.codigo)) return "O codigo deve conter apenas letras maiusculas e numeros.";
  if (!form.descricao.trim()) return "A descricao e obrigatoria.";
  if (!form.unidade.trim()) return "A unidade e obrigatoria.";
  if (!form.familiaId) return "A familia e obrigatoria.";
  if (!form.ivaCompraId || !form.ivaVendaId) return "O IVA na compra e na venda e obrigatorio.";
  if (form.pvp === "" || Number(form.pvp) < 0) return "O PVP deve ser igual ou superior a zero.";
  if (form.peso !== "" && Number(form.peso) < 0) return "O peso nao pode ser negativo.";
  return null;
}

function toForm(artigo: Artigo): ArtigoForm {
  return {
    codigo: artigo.codigo,
    abreviatura: artigo.abreviatura ?? "",
    codigoIdentificacao: artigo.codigoIdentificacao ?? "",
    descricao: artigo.descricao,
    unidade: artigo.unidade,
    familiaId: String(artigo.familiaId),
    peso: String(artigo.peso ?? 0),
    ivaCompraId: artigo.ivaCompraId,
    ivaVendaId: artigo.ivaVendaId,
    pvp: String(artigo.pvp),
    inativo: artigo.inativo,
    retencao: artigo.retencao,
    observacoes: artigo.observacoes ?? ""
  };
}

function toPayload(form: ArtigoForm, creating: boolean) {
  return {
    ...(creating ? { codigo: form.codigo } : {}),
    abreviatura: blankToNull(form.abreviatura),
    codigoIdentificacao: blankToNull(form.codigoIdentificacao),
    descricao: form.descricao.trim(),
    unidade: form.unidade.trim().toUpperCase(),
    familiaId: Number(form.familiaId),
    peso: form.peso === "" ? null : Number(form.peso),
    ivaCompraId: form.ivaCompraId,
    ivaVendaId: form.ivaVendaId,
    pvp: Number(form.pvp),
    inativo: form.inativo,
    retencao: form.retencao,
    observacoes: blankToNull(form.observacoes)
  };
}

function normalizeCode(value: string) {
  return value.toUpperCase().replace(/[^A-Z0-9]/g, "");
}

function blankToNull(value: string) {
  const trimmed = value.trim();
  return trimmed || null;
}

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 6 });
}
