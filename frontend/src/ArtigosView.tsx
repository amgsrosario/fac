import { useEffect, useMemo, useRef, useState } from "react";
import { Paginator } from "primereact/paginator";
import { useLocation } from "react-router-dom";
import { apiFetch, hasPermission } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";
import { EntityDetailOverlay } from "./EntityContext";
import { decimal, integer, money } from "./ui/tuuli/format";

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
  const [detailOpen, setDetailOpen] = useState(false);
  const detailTriggerRef = useRef<HTMLButtonElement>(null);
  const [search, setSearch] = useState("");
  const [showInactive, setShowInactive] = useState(false);
  const [exportingFormat, setExportingFormat] = useState<"pdf" | "xlsx" | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
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
    return artigos.filter((artigo) => {
      if (!showInactive && artigo.inativo) return false;
      if (!term) return true;
      return [artigo.codigo, artigo.descricao, artigo.abreviatura, artigo.codigoIdentificacao]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term));
    });
  }, [artigos, showInactive, search]);

  const pagedArtigos = filtered.slice(page * pageSize, (page + 1) * pageSize);

  useEffect(() => {
    setPage(0);
  }, [showInactive, search]);

  async function exportArtigos(formato: "pdf" | "xlsx") {
    setExportingFormat(formato);
    try {
      const params = new URLSearchParams({ formato });
      if (!showInactive) params.set("ativos", "true");
      const response = await apiFetch(`/api/exportacoes/artigos?${params}`);
      if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filenameFromDisposition(response.headers.get("Content-Disposition")) ?? `artigos.${formato}`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      window.alert(error instanceof Error ? error.message : "Não foi possível exportar os artigos.");
    } finally {
      setExportingFormat(null);
    }
  }

  useEffect(() => {
    const lastPage = Math.max(0, Math.ceil(filtered.length / pageSize) - 1);
    if (page > lastPage) setPage(lastPage);
  }, [filtered.length, page, pageSize]);

  const selected = artigos.find((artigo) => artigo.codigo === selectedCodigo) ?? null;
  const familiaNome = familias.find((familia) => familia.id === selected?.familiaId)?.descricao ?? "-";

  useEffect(() => setDetailOpen(false), [selectedCodigo]);

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
    <div className="tuuli-v2-page tuuli-grammar-entity-list">
      {notice && <p className="fac-editor-message">{notice}</p>}
      {message && <p className="fac-message">{message}</p>}
      <section aria-label="Artigo selecionado" className="fac-entity-context fac-article-context tuuli-entity-context">
        {selected ? <>
          <div className="fac-entity-context-main tuuli-entity-primary">
            <strong>{selected.descricao}</strong>
            <span>Código {selected.codigo} · IVA {selected.ivaVendaId} · {selected.inativo ? "Inativo" : "Ativo"}</span>
          </div>
          <div className="fac-article-context-metrics tuuli-metric-group tuuli-entity-metrics tuuli-entity-metrics-three" aria-label="Indicadores do catálogo">
            <div className="fac-entity-context-value tuuli-metric">
              <span>Preço</span>
              <strong>{money(selected.pvp)} EUR</strong>
            </div>
            <div className="fac-entity-context-value tuuli-metric">
              <span>Catálogo</span>
              <strong>{integer(artigos.length)} artigos</strong>
            </div>
            <div className="fac-entity-context-value tuuli-metric">
              <span>Inativos</span>
              <strong>{integer(artigos.filter((artigo) => artigo.inativo).length)}</strong>
            </div>
          </div>
          <div className="fac-entity-context-actions tuuli-context-actions">
            <button aria-controls="fac-article-detail" aria-expanded={detailOpen} className="fac-ghost-button tuuli-tool-action" onClick={() => setDetailOpen(true)} ref={detailTriggerRef} type="button">Ver detalhe</button>
            {canManage && <button className="fac-primary-button tuuli-primary-action" onClick={() => openEdit(selected)} type="button">Editar</button>}
          </div>
        </> : <span className="fac-muted">Selecione um artigo para consultar o respetivo contexto.</span>}
      </section>

      <section className="fac-list-toolbar fac-articles-toolbar tuuli-toolbar">
        <label className="tuuli-search"><i aria-hidden="true" className="pi pi-search" /><input aria-label="Pesquisar artigos" onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar código, descrição ou identificação" type="search" value={search} /></label>
        <div className="fac-inline-actions">
          <label className="fac-listing-checkbox"><input checked={showInactive} onChange={(event) => setShowInactive(event.target.checked)} type="checkbox" /><span>Mostrar inativos</span></label>
          <button className="fac-soft-button tuuli-tool-action" disabled={exportingFormat !== null} onClick={() => exportArtigos("pdf")} type="button">{exportingFormat === "pdf" ? "A gerar PDF..." : "Exportar PDF"}</button>
          <button className="fac-soft-button tuuli-tool-action" disabled={exportingFormat !== null} onClick={() => exportArtigos("xlsx")} type="button">{exportingFormat === "xlsx" ? "A gerar Excel..." : "Exportar Excel"}</button>
          <button className="fac-ghost-button tuuli-tool-action" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({artigoColumns.visibleColumns.length})</button>
          {canManage && <button className="fac-primary-button tuuli-primary-action" onClick={openNew} type="button">Novo artigo</button>}
        </div>
      </section>

      <section className="fac-content-grid fac-articles-content-grid">
        <article className="fac-panel fac-panel-main fac-articles-table-panel tuuli-table-surface">
          <ColumnSelector columns={artigoColumns.columns} open={columnEditorOpen} onMove={artigoColumns.moveColumn} onReset={artigoColumns.resetColumns} onToggle={artigoColumns.toggleColumn} />
          <table className="fac-table fac-articles-table tuuli-table">
            <thead><tr>{artigoColumns.visibleColumns.map((column) => <th className={artigoColumnClass(column.key)} key={column.key}>{column.label}</th>)}</tr></thead>
            <tbody>
              {pagedArtigos.map((artigo) => (
                <tr className={artigo.codigo === selectedCodigo ? "fac-row-selected tuuli-table-row-selected" : ""} key={artigo.codigo} onClick={() => setSelectedCodigo(artigo.codigo)}>
                  {artigoColumns.visibleColumns.map((column) => <td className={artigoColumnClass(column.key)} key={column.key}>{artigoColumnValue(artigo, column.key, familias)}</td>)}
                </tr>
              ))}
              {!loading && filtered.length === 0 && <tr><td colSpan={artigoColumns.visibleColumns.length}>Sem artigos para mostrar.</td></tr>}
            </tbody>
          </table>
          {filtered.length > 0 && <div className="fac-list-pagination fac-articles-pagination tuuli-pagination">
            <span>{filtered.length} {filtered.length === 1 ? "artigo" : "artigos"}{!showInactive ? ` de ${artigos.length}` : ""}</span>
            <Paginator first={page * pageSize} onPageChange={(event) => { setPage(event.page); setPageSize(event.rows); }} rows={pageSize} rowsPerPageOptions={[10, 20, 50]} totalRecords={filtered.length} />
          </div>}
        </article>

      </section>

      <EntityDetailOverlay labelledBy="fac-article-detail-title" onClose={() => setDetailOpen(false)} open={detailOpen && Boolean(selected)} returnFocusRef={detailTriggerRef}>
        {selected && <div id="fac-article-detail">
          <p className="fac-eyebrow">Artigo</p>
          <h2 id="fac-article-detail-title">{selected.descricao}</h2>
          <dl className="fac-entity-detail-rows">
            <div><dt>Código</dt><dd>{selected.codigo}</dd></div>
            <div><dt>Tipo</dt><dd>{tipoArtigoLabel(selected.tipoArtigo)}</dd></div>
            <div><dt>Família</dt><dd>{familiaNome}</dd></div>
            <div><dt>Unidade</dt><dd>{selected.unidade}</dd></div>
            <div><dt>IVA venda</dt><dd>{selected.ivaVendaId}</dd></div>
            <div><dt>PVP</dt><dd className="tuuli-cell-numeric">{money(selected.pvp)} EUR</dd></div>
            <div><dt>Retenção</dt><dd>{selected.retencao ? "Sim" : "Não"}</dd></div>
            <div><dt>Estado</dt><dd>{selected.inativo ? "Inativo" : "Ativo"}</dd></div>
            {selected.observacoes && <div><dt>Observações</dt><dd>{selected.observacoes}</dd></div>}
          </dl>
        </div>}
      </EntityDetailOverlay>
    </div>
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
    case "pvp": return `${money(artigo.pvp)} EUR`;
    case "peso": return decimal(artigo.peso ?? 0);
    case "retencao": return artigo.retencao ? "Sim" : "Não";
    case "estado": return <span className={`fac-status tuuli-status tuuli-status-${artigo.inativo ? "inativo" : "ativo"}`}>{artigo.inativo ? "Inativo" : "Ativo"}</span>;
    default: return "-";
  }
}

function artigoColumnClass(key: string) {
  if (key === "descricao") return "tuuli-cell-primary";
  if (key === "pvp") return "fac-article-number tuuli-cell-primary tuuli-cell-numeric";
  if (key === "peso") return "fac-article-number tuuli-cell-secondary tuuli-cell-numeric";
  return "tuuli-cell-secondary";
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

function filenameFromDisposition(disposition: string | null) {
  const match = disposition?.match(/filename="?([^";]+)"?/i);
  return match?.[1] ?? null;
}

function firstActiveIva(tiposIva: TipoTaxaIva[]) {
  return tiposIva.find((tipo) => !tipo.inativo) ?? tiposIva[0] ?? null;
}

function tipoArtigoLabel(tipo: TipoArtigo) {
  return tipo === "ARTIGO" ? "Artigo" : "Serviço";
}
