import { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { apiFetch, hasPermission } from "./api";
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
  tipoArtigo: TipoArtigo;
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

type TipoArtigo = "ARTIGO" | "SERVICO";

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
  tipoArtigo: TipoArtigo;
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
  tipoArtigo: "SERVICO",
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
  { key: "codigo", label: "Código", visible: true },
  { key: "descricao", label: "Descrição", visible: true },
  { key: "abreviatura", label: "Abreviatura", visible: false },
  { key: "codigoIdentificacao", label: "Identificação", visible: false },
  { key: "familia", label: "Família", visible: false },
  { key: "unidade", label: "Unidade", visible: true },
  { key: "ivaVenda", label: "IVA venda", visible: false },
  { key: "pvp", label: "PVP", visible: true },
  { key: "peso", label: "Peso", visible: false },
  { key: "retencao", label: "Retenção", visible: false },
  { key: "estado", label: "Estado", visible: true }
];

export default function ArtigosView() {
  const location = useLocation();
  const canManage = hasPermission("MESTRES_GERIR");
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
  const [moreOptionsOpen, setMoreOptionsOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (artigos.length === 0) return;
    const artigoId = new URLSearchParams(location.search).get("artigo");
    if (!artigoId) return;
    const decoded = decodeURIComponent(artigoId);
    if (artigos.some((artigo) => artigo.codigo === decoded)) {
      setSelectedCodigo(decoded);
    }
  }, [artigos, location.search]);

  useEffect(() => {
    if (!editorOpen) return;
    setMoreOptionsOpen(Boolean(editingCodigo && (form.familiaId || form.observacoes.trim() || form.retencao || form.inativo)));
  }, [editorOpen, editingCodigo]);

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
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar artigos.");
    } finally {
      setLoading(false);
    }
  }

  function openNew() {
    const defaultIva = firstActiveIva(tiposIva)?.id ?? "";
    setEditingCodigo(null);
    setForm({ ...emptyForm, ivaCompraId: defaultIva, ivaVendaId: defaultIva });
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
      setMessage(error instanceof Error ? error.message : "Não foi possível guardar o artigo.");
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

        <div className="fac-article-form-sections">
          <FormSection title="Identificação">
            <Field label="Código">
              <input disabled={editingCodigo != null} maxLength={50} onChange={(event) => change("codigo", normalizeCode(event.target.value))} value={form.codigo} />
            </Field>
            <Field label="Descrição"><input maxLength={80} onChange={(event) => change("descricao", event.target.value)} value={form.descricao} /></Field>
            <Field label="Tipo Artigo/Serviço">
              <select onChange={(event) => change("tipoArtigo", event.target.value as TipoArtigo)} value={form.tipoArtigo}>
                <option value="SERVICO">Serviço</option>
                <option value="ARTIGO">Artigo</option>
              </select>
            </Field>
            <Field label="Unidade"><input maxLength={3} onChange={(event) => change("unidade", event.target.value.toUpperCase())} value={form.unidade} /></Field>
          </FormSection>

          <FormSection title="Preço e fiscalidade">
            <Field label="Preço de venda"><input min="0" onChange={(event) => change("pvp", event.target.value)} step="0.000001" type="number" value={form.pvp} /></Field>
            <Field label="IVA de venda">
              <select onChange={(event) => change("ivaVendaId", event.target.value)} value={form.ivaVendaId}>
                <option value="">Selecionar</option>
                {tiposIva.map((tipo) => <option disabled={tipo.inativo && tipo.id !== form.ivaVendaId} key={tipo.id} value={tipo.id}>{tipo.descricao}{tipo.inativo ? " (inativo)" : ""}</option>)}
              </select>
            </Field>
          </FormSection>

          <section className={`fac-article-more-options ${moreOptionsOpen ? "open" : ""}`}>
            <button
              aria-controls="fac-article-more-options"
              aria-expanded={moreOptionsOpen}
              className="fac-article-more-options-trigger"
              onClick={() => setMoreOptionsOpen((open) => !open)}
              type="button"
            >
              <span>Mais opções</span>
              {(form.familiaId || form.observacoes.trim() || form.retencao || form.inativo) && <small>Com valores</small>}
            </button>
            <div className="fac-form-grid" hidden={!moreOptionsOpen} id="fac-article-more-options">
              <Field label="Família">
                <select onChange={(event) => change("familiaId", event.target.value)} value={form.familiaId}>
                  <option value="">Selecionar</option>
                  {familias.map((familia) => <option key={familia.id} value={familia.id}>{familia.descricao}</option>)}
                </select>
              </Field>
              <Field label="Observações"><textarea maxLength={250} onChange={(event) => change("observacoes", event.target.value)} value={form.observacoes} /></Field>
              <label className="fac-check-field"><input checked={form.retencao} onChange={(event) => change("retencao", event.target.checked)} type="checkbox" /><span>Sujeito a retenção</span></label>
              <label className="fac-check-field"><input checked={form.inativo} onChange={(event) => change("inativo", event.target.checked)} type="checkbox" /><span>Artigo inativo</span></label>
            </div>
          </section>
        </div>

        <div className="fac-form-footer">
          <span className="fac-muted">O código é definitivo depois de criar o artigo.</span>
          <button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar artigo"}</button>
        </div>
      </section>
    );
  }

  return (
    <>
      {notice && <p className="fac-editor-message">{notice}</p>}
      {message && <p className="fac-message">{message}</p>}
      <section className="fac-articles-header-grid">
        <section className="fac-hero fac-articles-hero">
        <div>
          <p className="fac-eyebrow">Artigos</p>
          <h2>Catálogo de artigos</h2>
          <p>Artigos e serviços.</p>
        </div>
        <div className="fac-hero-card">
          <span>Catálogo</span>
          <strong>{loading ? "A carregar..." : `${artigos.length} artigos`}</strong>
          <small>{artigos.filter((artigo) => artigo.inativo).length} inativos</small>
        </div>
      </section>

      <aside className="fac-panel fac-detail fac-articles-detail-top">
        <p className="fac-eyebrow">Ficha comercial</p>
        <h2>{selected?.codigo ?? "Sem artigo"}</h2>
        <dl>
          <div><dt>Descrição</dt><dd>{selected?.descricao ?? "-"}</dd></div>
          <div><dt>Tipo</dt><dd>{selected ? tipoArtigoLabel(selected.tipoArtigo) : "-"}</dd></div>
          <div><dt>Família</dt><dd>{familiaNome}</dd></div>
          <div><dt>Unidade</dt><dd>{selected?.unidade ?? "-"}</dd></div>
          <div><dt>IVA venda</dt><dd>{selected?.ivaVendaId ?? "-"}</dd></div>
          <div><dt>PVP</dt><dd>{selected ? money(selected.pvp) : "-"}</dd></div>
          <div><dt>Retenção</dt><dd>{selected?.retencao ? "Sim" : "Não"}</dd></div>
        </dl>
        {!selected && <p className="fac-muted">Selecione um artigo para consultar os respetivos dados.</p>}
        {canManage && <button className="fac-primary-button" disabled={!selected} onClick={() => selected && openEdit(selected)} type="button">Editar artigo</button>}
        </aside>
      </section>

      <section className="fac-list-toolbar fac-articles-toolbar">
        <input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar código, descrição ou identificação" type="search" value={search} />
        <div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({artigoColumns.visibleColumns.length})</button>{canManage && <button className="fac-primary-button" onClick={openNew} type="button">Novo artigo</button>}</div>
      </section>

      <section className="fac-content-grid fac-articles-content-grid">
        <article className="fac-panel fac-panel-main fac-articles-table-panel">
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

        <aside className="fac-panel fac-detail fac-articles-detail-card">
          <p className="fac-eyebrow">Ficha comercial</p>
          <h2>{selected?.codigo ?? "Sem artigo"}</h2>
          <dl>
            <div><dt>Descrição</dt><dd>{selected?.descricao ?? "-"}</dd></div>
            <div><dt>Tipo</dt><dd>{selected ? tipoArtigoLabel(selected.tipoArtigo) : "-"}</dd></div>
            <div><dt>Família</dt><dd>{familiaNome}</dd></div>
            <div><dt>Unidade</dt><dd>{selected?.unidade ?? "-"}</dd></div>
            <div><dt>IVA venda</dt><dd>{selected?.ivaVendaId ?? "-"}</dd></div>
            <div><dt>PVP</dt><dd>{selected ? money(selected.pvp) : "-"}</dd></div>
            <div><dt>Retenção</dt><dd>{selected?.retencao ? "Sim" : "Não"}</dd></div>
          </dl>
          {canManage && <button className="fac-primary-button" disabled={!selected} onClick={() => selected && openEdit(selected)} type="button">Editar artigo</button>}
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
    case "ivaVenda": return artigo.ivaVendaId;
    case "pvp": return money(artigo.pvp);
    case "peso": return artigo.peso ?? 0;
    case "retencao": return artigo.retencao ? "Sim" : "Não";
    case "estado": return <span className="fac-status">{artigo.inativo ? "Inativo" : "Ativo"}</span>;
    default: return "-";
  }
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}

function FormSection({ children, title }: { children: React.ReactNode; title: string }) {
  return (
    <fieldset className="fac-article-form-section">
      <legend>{title}</legend>
      <div className="fac-form-grid">{children}</div>
    </fieldset>
  );
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
  if (!editing && !/^[A-Z0-9]{1,50}$/.test(form.codigo)) return "O código deve conter apenas letras maiúsculas e números.";
  if (!form.descricao.trim()) return "A descrição é obrigatória.";
  if (!form.tipoArtigo) return "Tipo Artigo/Serviço é obrigatório.";
  if (!form.unidade.trim()) return "A unidade é obrigatória.";
  if (!form.familiaId) return "A família é obrigatória.";
  if (!form.ivaVendaId) return "O IVA de venda é obrigatório.";
  if (form.pvp === "" || Number(form.pvp) < 0) return "O preço de venda deve ser igual ou superior a zero.";
  if (form.peso !== "" && Number(form.peso) < 0) return "O peso não pode ser negativo.";
  return null;
}

function toForm(artigo: Artigo): ArtigoForm {
  return {
    codigo: artigo.codigo,
    abreviatura: artigo.abreviatura ?? "",
    codigoIdentificacao: artigo.codigoIdentificacao ?? "",
    descricao: artigo.descricao,
    tipoArtigo: artigo.tipoArtigo ?? "SERVICO",
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
    tipoArtigo: form.tipoArtigo,
    unidade: form.unidade.trim().toUpperCase(),
    familiaId: Number(form.familiaId),
    peso: form.peso === "" ? null : Number(form.peso),
    ivaCompraId: creating ? form.ivaVendaId : form.ivaCompraId || form.ivaVendaId,
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

function firstActiveIva(tiposIva: TipoTaxaIva[]) {
  return tiposIva.find((tipo) => !tipo.inativo) ?? tiposIva[0] ?? null;
}

function tipoArtigoLabel(tipo: TipoArtigo) {
  return tipo === "ARTIGO" ? "Artigo" : "Serviço";
}

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 6 });
}
