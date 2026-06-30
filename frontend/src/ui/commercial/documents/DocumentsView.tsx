import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { FilterMatchMode } from "primereact/api";
import { apiFetch, AuthSession } from "../../../api";
import {
  DesktopShell,
  FacButton,
  FacDataTable,
  FacDataTableColumn,
  FacDialog,
  FacEmptyState,
  FacInputText,
  FacLoadingState,
  FacMessage,
  FacSelect,
  FacStatusBadge,
  MobileShell,
  ResponsiveSlot,
  useDeviceClass,
  useFacToast
} from "../../fac";
import { CommercialSidebar, ModuleHeader } from "../shared";
import "./documents.css";

type Page<T> = {
  content: T[];
  totalElements: number;
};

type EstadoDocumento = "RASCUNHO" | "EMITIDO" | "ANULADO";

type DocumentoComercial = {
  id: number;
  tipoDocumentoId: string;
  tipoDocumentoCodigoFiscal?: string;
  serie: string;
  numeroDocumento?: number | null;
  numeroDocumentoCompleto?: string | null;
  atcud?: string | null;
  fiscalmenteConsolidado: boolean;
  temQrFiscal: boolean;
  qrPayload?: string | null;
  estado: EstadoDocumento;
  dataEmissao: string;
  dataVencimento?: string | null;
  clienteId: number;
  moedaId?: string | null;
  rivaId?: string | null;
  mPagamentoId?: number | null;
  pPagamentoId?: string | null;
  transporteId?: number | null;
  clienteNome: string;
  clienteNif: string;
  clienteMorada?: string | null;
  clienteMorada1?: string | null;
  clienteCodPostal?: string | null;
  clienteLocalidade?: string | null;
  clientePais?: string | null;
  valorBruto?: string | number | null;
  valorDesconto?: string | number | null;
  valorIvaTotal?: string | number | null;
  valorRetencao?: string | number | null;
  valorTotal?: string | number | null;
  observacoes?: string | null;
  momentoEmissao?: string | null;
  emissorId?: string | null;
  anulado: boolean;
  motivoAnulacao?: string | null;
  dataHoraAnulacao?: string | null;
  anuladoPorNome?: string | null;
  impresso: boolean;
  liquidado: boolean;
  tipoDocumentoDescricao?: string | null;
  serieDescricao?: string | null;
  moedaCodigo?: string | null;
  moedaSimbolo?: string | null;
  moedaCasasDecimais?: number | null;
};

type LinhaDocumento = {
  id: number;
  documentoComercialId: number;
  numeroLinha: number;
  artigoId: string;
  descricao: string;
  quantidade: string | number;
  precoUnitario: string | number;
  valorBruto: string | number;
  tipoDesconto?: "VALOR" | "PERCENTAGEM" | null;
  desconto?: string | number | null;
  valorDesconto?: string | number | null;
  valorLinha?: string | number | null;
  tipoTaxaIvaId?: string | null;
  percentagemIva?: string | number | null;
  unidade?: string | null;
  baseTributavel?: string | number | null;
  valorImposto?: string | number | null;
  totalLinha?: string | number | null;
};

type DocumentoImpressao = {
  documento: DocumentoComercial;
  linhas: LinhaDocumento[];
};

type Diagnostico = {
  podeEmitir: boolean;
  podeAnular: boolean;
  alertas: string[];
  bloqueios: string[];
};

type TipoDocumento = {
  id: string;
  descricao: string;
  areaGestao: number;
};

type Serie = {
  serie: string;
  tipoDocumentoId: string;
  nome: string;
  temCodigoAt: boolean;
};

type Cliente = {
  id: number;
  nome: string;
  nif: string;
  inativo: boolean;
  moedaId?: string | null;
  rivaId?: string | null;
  mPagamentoId?: number | null;
  pPagamentoId?: string | null;
  transporteId?: number | null;
};

type Artigo = {
  codigo: string;
  descricao: string;
  unidade: string;
  pvp: number;
  ivaVendaId: string;
  inativo: boolean;
};

type CatalogoString = {
  id: string;
  nome: string;
};

type TipoTaxaIva = {
  id: string;
  descricao: string;
  inativo: boolean;
};

type CatalogoNumero = {
  id: number;
  nome: string;
};

type Armazem = {
  id: number;
  nome: string;
};

type Catalogos = {
  artigos: Artigo[];
  armazens: Armazem[];
  clientes: Cliente[];
  moedas: CatalogoString[];
  regimesIva: CatalogoString[];
  series: Serie[];
  tiposDocumento: TipoDocumento[];
  tiposIva: TipoTaxaIva[];
  transportes: CatalogoNumero[];
  modosPagamento: CatalogoNumero[];
  prazosPagamento: CatalogoString[];
};

type DocumentForm = {
  tipoDocumentoId: string;
  serie: string;
  dataEmissao: string;
  clienteId: string;
  armazemCargaId: string;
  moedaId: string;
  rivaId: string;
  mPagamentoId: string;
  pPagamentoId: string;
  transporteId: string;
  observacoes: string;
  artigoId: string;
  descricao: string;
  quantidade: string;
  precoUnitario: string;
  tipoDesconto: "VALOR" | "PERCENTAGEM";
  desconto: string;
  tipoTaxaIvaId: string;
};

type MobileScreen = "list" | "detail" | "form";
type StateFilter = "all" | EstadoDocumento;

const today = () => new Date().toISOString().slice(0, 10);

const emptyForm: DocumentForm = {
  tipoDocumentoId: "",
  serie: "",
  dataEmissao: today(),
  clienteId: "",
  armazemCargaId: "",
  moedaId: "",
  rivaId: "",
  mPagamentoId: "",
  pPagamentoId: "",
  transporteId: "",
  observacoes: "",
  artigoId: "",
  descricao: "",
  quantidade: "1",
  precoUnitario: "0",
  tipoDesconto: "VALOR",
  desconto: "0",
  tipoTaxaIvaId: ""
};

export default function DocumentsView({ currentUser, onLogout }: { currentUser: AuthSession; onLogout: () => void }) {
  const { deviceClass, isMobile } = useDeviceClass();
  const { showToast } = useFacToast();
  const [documentos, setDocumentos] = useState<DocumentoComercial[]>([]);
  const [linhas, setLinhas] = useState<LinhaDocumento[]>([]);
  const [diagnostico, setDiagnostico] = useState<Diagnostico | null>(null);
  const [catalogos, setCatalogos] = useState<Catalogos>({
    artigos: [],
    armazens: [],
    clientes: [],
    moedas: [],
    regimesIva: [],
    series: [],
    tiposDocumento: [],
    tiposIva: [],
    transportes: [],
    modosPagamento: [],
    prazosPagamento: []
  });
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [search, setSearch] = useState("");
  const [stateFilter, setStateFilter] = useState<StateFilter>("all");
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [form, setForm] = useState<DocumentForm>(emptyForm);
  const [editorMessage, setEditorMessage] = useState<string | null>(null);
  const [anularOpen, setAnularOpen] = useState(false);
  const [motivoAnulacao, setMotivoAnulacao] = useState("");
  const [mobileScreen, setMobileScreen] = useState<MobileScreen>("list");

  const canCreate = currentUser.permissoes.includes("DOCUMENTO_CRIAR");
  const canEditDraft = currentUser.permissoes.includes("DOCUMENTO_EDITAR_RASCUNHO");
  const canEmit = currentUser.permissoes.includes("DOCUMENTO_EMITIR");
  const canVoid = currentUser.permissoes.includes("DOCUMENTO_ANULAR");
  const canPdf = currentUser.permissoes.includes("DOCUMENTO_OBTER_PDF");

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (selectedId) loadDetail(selectedId);
  }, [selectedId]);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const [docsPage, tiposPage, seriesPage, clientesPage, artigosPage, armazensPage, moedasPage, regimesPage, modosPage, prazosPage, transportesPage, tiposIvaPage] = await Promise.all([
        fetchPage<DocumentoComercial>("/api/documentos-comerciais?size=300&sort=dataEmissao,desc&sort=id,desc"),
        fetchPage<TipoDocumento>("/api/tipos-documento?size=100&sort=id,asc"),
        fetchPage<Serie>("/api/series?size=100&sort=tipoDocumento.id,asc&sort=serie,asc"),
        fetchPage<Cliente>("/api/clientes?size=300&sort=nome,asc"),
        fetchPage<Artigo>("/api/artigos?size=300&sort=descricao,asc"),
        fetchPage<Armazem>("/api/armazens?size=100&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/moedas?size=100&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/riva?size=100&sort=nome,asc"),
        fetchPage<CatalogoNumero>("/api/mpagamentos?size=100&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/p-pagamentos?size=100&sort=nome,asc"),
        fetchPage<CatalogoNumero>("/api/transportes?size=100&sort=nome,asc"),
        fetchPage<TipoTaxaIva>("/api/tipos-taxa-iva?size=100&sort=descricao,asc")
      ]);
      const commercialTypes = tiposPage.content.filter((tipo) => tipo.areaGestao === 2);
      setDocumentos(docsPage.content);
      setCatalogos({
        artigos: artigosPage.content.filter((artigo) => !artigo.inativo),
        armazens: armazensPage.content,
        clientes: clientesPage.content,
        moedas: moedasPage.content,
        regimesIva: regimesPage.content,
        series: seriesPage.content,
        tiposDocumento: commercialTypes,
        tiposIva: tiposIvaPage.content.filter((iva) => !iva.inativo),
        transportes: transportesPage.content,
        modosPagamento: modosPage.content,
        prazosPagamento: prazosPage.content
      });
      setSelectedId((current) => current ?? docsPage.content[0]?.id ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar documentos.");
    } finally {
      setLoading(false);
    }
  }

  async function loadDetail(id: number) {
    setDetailLoading(true);
    try {
      const [impressao, diag] = await Promise.all([
        requestJson<DocumentoImpressao>(`/api/documentos-comerciais/${id}/impressao`),
        requestJson<Diagnostico>(`/api/documentos-comerciais/${id}/diagnostico`)
      ]);
      setLinhas(impressao.linhas);
      setDiagnostico(diag);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar detalhe do documento.");
      setLinhas([]);
      setDiagnostico(null);
    } finally {
      setDetailLoading(false);
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    return documentos.filter((doc) => {
      const matchesSearch = !term || [
        doc.id,
        doc.tipoDocumentoId,
        doc.tipoDocumentoDescricao,
        doc.serie,
        doc.numeroDocumento,
        doc.numeroDocumentoCompleto,
        doc.clienteNome,
        doc.clienteNif,
        doc.estado,
        doc.moedaCodigo ?? doc.moedaId
      ].filter(Boolean).some((value) => String(value).toLowerCase().includes(term));
      const matchesState = stateFilter === "all" || doc.estado === stateFilter;
      return matchesSearch && matchesState;
    });
  }, [documentos, search, stateFilter]);

  const selected = documentos.find((doc) => doc.id === selectedId) ?? null;
  const activeCount = documentos.filter((doc) => doc.estado === "RASCUNHO").length;

  function openNew() {
    if (!canCreate) return;
    const next = initialiseForm(catalogos);
    setForm(next);
    setEditorMessage(null);
    setEditorOpen(true);
    setMobileScreen("form");
  }

  function closeEditor() {
    setEditorOpen(false);
    setEditorMessage(null);
    if (isMobile) setMobileScreen(selected ? "detail" : "list");
  }

  async function save(event?: FormEvent) {
    event?.preventDefault();
    if (saving) return;
    const validation = validateForm(form);
    if (validation) {
      setEditorMessage(validation);
      return;
    }
    setSaving(true);
    setEditorMessage(null);
    setError(null);
    try {
      const created = await requestJson<DocumentoComercial>("/api/documentos-comerciais", {
        documento: {
          tipoDocumentoId: form.tipoDocumentoId,
          serie: form.serie,
          dataEmissao: form.dataEmissao,
          clienteId: Number(form.clienteId),
          armazemCargaId: Number(form.armazemCargaId),
          moedaId: nullable(form.moedaId),
          rivaId: nullable(form.rivaId),
          mPagamentoId: nullableNumber(form.mPagamentoId),
          pPagamentoId: nullable(form.pPagamentoId),
          transporteId: nullableNumber(form.transporteId),
          observacoes: nullable(form.observacoes)
        },
        linha: {
          artigoId: form.artigoId,
          descricao: nullable(form.descricao),
          quantidade: form.quantidade,
          precoUnitario: form.precoUnitario,
          tipoDesconto: form.tipoDesconto,
          desconto: form.desconto || "0",
          tipoTaxaIvaId: nullable(form.tipoTaxaIvaId)
        }
      }, "POST");
      await loadData();
      setSelectedId(created.id);
      setEditorOpen(false);
      setNotice(`Documento ${documentRef(created)} criado como rascunho.`);
      showToast({ detail: "Documento guardado como rascunho.", severity: "success", summary: "Documento" });
      if (isMobile) setMobileScreen("detail");
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Nao foi possivel guardar o documento.");
    } finally {
      setSaving(false);
    }
  }

  async function emitir() {
    if (!selected || saving) return;
    setSaving(true);
    try {
      const updated = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${selected.id}/emitir`, { emissorId: currentUser.codigo }, "POST");
      await refreshAfterAction(updated.id, "Documento emitido.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel emitir o documento.");
    } finally {
      setSaving(false);
    }
  }

  async function anular() {
    if (!selected || saving) return;
    if (motivoAnulacao.trim().length < 5) {
      setError("Indica um motivo de anulacao com pelo menos 5 caracteres.");
      return;
    }
    setSaving(true);
    try {
      const updated = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${selected.id}/anular`, { motivo: motivoAnulacao.trim() }, "POST");
      setAnularOpen(false);
      setMotivoAnulacao("");
      await refreshAfterAction(updated.id, "Documento anulado.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel anular o documento.");
    } finally {
      setSaving(false);
    }
  }

  async function openPdf() {
    if (!selected) return;
    try {
      const response = await apiFetch(`/api/documentos-comerciais/${selected.id}/pdf`);
      if (!response.ok) throw new Error(await responseError(response));
      const blob = await response.blob();
      window.open(URL.createObjectURL(blob), "_blank", "noopener,noreferrer");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel abrir o PDF.");
    }
  }

  async function refreshAfterAction(id: number, message: string) {
    const page = await fetchPage<DocumentoComercial>("/api/documentos-comerciais?size=300&sort=dataEmissao,desc&sort=id,desc");
    setDocumentos(page.content);
    setSelectedId(id);
    await loadDetail(id);
    setNotice(message);
    showToast({ detail: message, severity: "success", summary: "Documento" });
  }

  function selectDocument(id: number) {
    setSelectedId(id);
    if (isMobile) setMobileScreen("detail");
  }

  const sidebar = <CommercialSidebar active="documents" currentUser={currentUser} onLogout={onLogout} />;
  const content = (
    <DocumentsContent
      activeCount={activeCount}
      canCreate={canCreate}
      canEditDraft={canEditDraft}
      canEmit={canEmit}
      canPdf={canPdf}
      canVoid={canVoid}
      catalogos={catalogos}
      detailLoading={detailLoading}
      deviceClass={deviceClass}
      diagnostico={diagnostico}
      documentos={documentos}
      editorMessage={editorMessage}
      editorOpen={editorOpen}
      error={error}
      filtered={filtered}
      form={form}
      linhas={linhas}
      loading={loading}
      mobileScreen={mobileScreen}
      motivoAnulacao={motivoAnulacao}
      notice={notice}
      onAnular={anular}
      onBackToList={() => setMobileScreen("list")}
      onChangeForm={setForm}
      onChangeMotivo={setMotivoAnulacao}
      onCloseAnular={() => setAnularOpen(false)}
      onCloseEditor={closeEditor}
      onEmitir={emitir}
      onNew={openNew}
      onOpenAnular={() => { setMotivoAnulacao(""); setAnularOpen(true); }}
      onOpenPdf={openPdf}
      onSave={save}
      onSearch={setSearch}
      onSelect={selectDocument}
      onStateFilter={setStateFilter}
      saving={saving}
      search={search}
      selected={selected}
      stateFilter={stateFilter}
      voidOpen={anularOpen}
    />
  );

  return (
    <ResponsiveSlot
      desktop={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
      mobile={<MobileShell title="FAC Documentos">{content}</MobileShell>}
      tablet={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
    />
  );
}

function DocumentsContent(props: {
  activeCount: number;
  canCreate: boolean;
  canEditDraft: boolean;
  canEmit: boolean;
  canPdf: boolean;
  canVoid: boolean;
  catalogos: Catalogos;
  detailLoading: boolean;
  deviceClass: string;
  diagnostico: Diagnostico | null;
  documentos: DocumentoComercial[];
  editorMessage: string | null;
  editorOpen: boolean;
  error: string | null;
  filtered: DocumentoComercial[];
  form: DocumentForm;
  linhas: LinhaDocumento[];
  loading: boolean;
  mobileScreen: MobileScreen;
  motivoAnulacao: string;
  notice: string | null;
  onAnular: () => void;
  onBackToList: () => void;
  onChangeForm: (form: DocumentForm) => void;
  onChangeMotivo: (value: string) => void;
  onCloseAnular: () => void;
  onCloseEditor: () => void;
  onEmitir: () => void;
  onNew: () => void;
  onOpenAnular: () => void;
  onOpenPdf: () => void;
  onSave: (event?: FormEvent) => void;
  onSearch: (value: string) => void;
  onSelect: (id: number) => void;
  onStateFilter: (value: StateFilter) => void;
  saving: boolean;
  search: string;
  selected: DocumentoComercial | null;
  stateFilter: StateFilter;
  voidOpen: boolean;
}) {
  if (props.deviceClass === "mobile") return <MobileDocumentsContent {...props} />;

  return (
    <>
      <DocumentsHeader {...props} />
      <DocumentsToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operacao concluida">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <section aria-label="Documentos comerciais" className="fac-documents-layout">
        <article className="fac-documents-panel fac-documents-list-panel">
          <DocumentsList {...props} />
        </article>
        <aside className="fac-documents-panel fac-documents-detail-panel">
          <DocumentDetail {...props} />
        </aside>
      </section>
      <DocumentEditorDialog {...props} />
      <VoidDialog {...props} />
    </>
  );
}

function MobileDocumentsContent(props: Parameters<typeof DocumentsContent>[0]) {
  if (props.editorOpen || props.mobileScreen === "form") {
    return (
      <section className="fac-documents-mobile-page">
        <MobilePageHeader action={<FacButton icon="pi pi-times" label="Fechar" onClick={props.onCloseEditor} variant="ghost" />} eyebrow="Novo documento" title="Rascunho" />
        <DocumentFormFields {...props} inlineFooter />
      </section>
    );
  }

  if (props.mobileScreen === "detail" && props.selected) {
    return (
      <section className="fac-documents-mobile-page">
        <MobilePageHeader action={<FacButton icon="pi pi-arrow-left" label="Lista" onClick={props.onBackToList} variant="ghost" />} eyebrow="Documento" title={documentRef(props.selected)} />
        <DocumentDetail {...props} />
        <VoidDialog {...props} />
      </section>
    );
  }

  return (
    <>
      <DocumentsHeader {...props} compact />
      <DocumentsToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operacao concluida">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <DocumentsList {...props} />
    </>
  );
}

function DocumentsHeader({ activeCount, canCreate, compact = false, documentos, loading, onNew }: Parameters<typeof DocumentsContent>[0] & { compact?: boolean }) {
  return (
    <ModuleHeader
      action={canCreate && <FacButton icon="pi pi-plus" label={compact ? "Novo" : "Novo documento"} onClick={onNew} variant="primary" />}
      compact={compact}
      eyebrow="Documentos"
      subtitle="Consultar e preparar documentos comerciais."
      summary={<div className="fac-module-summary" aria-label="Resumo de documentos"><span>{loading ? "A carregar" : `${documentos.length} documentos`}</span><strong>{activeCount} rascunhos</strong></div>}
      title="Documentos"
    />
  );
}

function DocumentsToolbar({ deviceClass, onSearch, onStateFilter, search, stateFilter }: Parameters<typeof DocumentsContent>[0]) {
  return (
    <section className="fac-documents-toolbar" aria-label="Pesquisa e filtros">
      <FacInputText aria-label="Pesquisar documentos" onChange={(event) => onSearch(event.target.value)} placeholder={deviceClass === "mobile" ? "Pesquisar documentos" : "Pesquisar por numero, cliente, NIF, serie ou estado"} type="search" value={search} />
      <FacSelect
        onChange={(value) => onStateFilter((value as StateFilter) ?? "all")}
        options={[
          { label: "Todos", value: "all" },
          { label: "Rascunhos", value: "RASCUNHO" },
          { label: "Emitidos", value: "EMITIDO" },
          { label: "Anulados", value: "ANULADO" }
        ]}
        value={stateFilter}
      />
    </section>
  );
}

function DocumentsList({ deviceClass, documentos, filtered, loading, onSelect, search, selected, stateFilter }: Parameters<typeof DocumentsContent>[0]) {
  if (loading) return <FacLoadingState description="A carregar documentos." />;
  if (documentos.length === 0) return <FacEmptyState description="Ainda nao existem documentos comerciais." />;
  if (filtered.length === 0) return <FacEmptyState description="Sem resultados para a pesquisa e filtros atuais." />;

  if (deviceClass !== "mobile") {
    const tableValue = documentos.filter((doc) => stateFilter === "all" || doc.estado === stateFilter);
    const columns: FacDataTableColumn<DocumentoComercial>[] = [
      { body: (doc) => date(doc.dataEmissao), dataType: "date", field: "dataEmissao", filter: true, filterPlaceholder: "Data", header: "Data", sortable: true, style: { width: "7rem" } },
      { body: (doc) => documentRef(doc), field: "numeroDocumentoCompleto", filter: true, filterPlaceholder: "Documento", header: "Documento", sortable: true, style: { width: "10rem" } },
      { field: "clienteNome", filter: true, filterPlaceholder: "Cliente", header: "Cliente", sortable: true },
      { body: (doc) => money(doc.valorTotal, doc), dataType: "numeric", field: "valorTotal", header: "Total", sortable: true, style: { width: "8rem" } },
      {
        body: (doc) => <DocumentStatusBadge estado={doc.estado} />,
        field: "estado",
        filter: true,
        filterElement: (options) => <DocumentStateFilter onChange={options.filterApplyCallback} value={options.value} />,
        filterMatchMode: FilterMatchMode.EQUALS,
        header: "Estado",
        sortable: true,
        style: { width: "8rem" }
      }
    ];
    return (
      <FacDataTable
        ariaLabel="Tabela de documentos"
        className="fac-documents-data-table"
        columns={columns}
        dataKey="id"
        emptyMessage="Sem documentos para os filtros atuais."
        globalFilter={search}
        globalFilterFields={["id", "tipoDocumentoId", "serie", "numeroDocumentoCompleto", "clienteNome", "clienteNif", "estado", "moedaCodigo"]}
        loading={loading}
        onSelectionChange={(doc) => doc && onSelect(doc.id)}
        selection={selected}
        value={tableValue}
      />
    );
  }

  return (
    <div className="fac-documents-mobile-list">
      {filtered.map((doc) => (
        <button className="fac-document-card" key={doc.id} onClick={() => onSelect(doc.id)} type="button">
          <strong>{documentRef(doc)}</strong>
          <span>{date(doc.dataEmissao)} - {doc.clienteNome}</span>
          <span>{money(doc.valorTotal, doc)}</span>
          <DocumentStatusBadge estado={doc.estado} />
        </button>
      ))}
    </div>
  );
}

function DocumentDetail(props: Parameters<typeof DocumentsContent>[0]) {
  const { canEmit, canPdf, canVoid, detailLoading, diagnostico, linhas, onEmitir, onOpenAnular, onOpenPdf, saving, selected } = props;
  if (!selected) return <FacEmptyState description="Seleciona um documento para ver o detalhe." />;
  const canEmitNow = canEmit && selected.estado === "RASCUNHO" && (diagnostico?.podeEmitir ?? linhas.length > 0);
  const canVoidNow = canVoid && selected.estado === "EMITIDO" && (diagnostico?.podeAnular ?? !selected.anulado);

  return (
    <section className="fac-documents-detail">
      <div className="fac-documents-detail-title">
        <div>
          <p className="fac-eyebrow">Detalhe do documento</p>
          <h2>{documentRef(selected)}</h2>
        </div>
        <DocumentStatusBadge estado={selected.estado} />
      </div>
      <dl className="fac-documents-definition">
        <div><dt>Tipo</dt><dd>{selected.tipoDocumentoDescricao ?? selected.tipoDocumentoId}</dd></div>
        <div><dt>Serie</dt><dd>{selected.serieDescricao ?? selected.serie}</dd></div>
        <div><dt>Data</dt><dd>{date(selected.dataEmissao)}</dd></div>
        <div><dt>Cliente</dt><dd>{selected.clienteNome}</dd></div>
        <div><dt>NIF</dt><dd>{selected.clienteNif}</dd></div>
        <div><dt>Moeda</dt><dd>{selected.moedaCodigo ?? selected.moedaId ?? "-"}</dd></div>
        {selected.atcud && <div><dt>ATCUD</dt><dd>{selected.atcud}</dd></div>}
        {selected.temQrFiscal && <div><dt>QR fiscal</dt><dd>Disponivel</dd></div>}
        {selected.motivoAnulacao && <div><dt>Motivo anulacao</dt><dd>{selected.motivoAnulacao}</dd></div>}
      </dl>
      {detailLoading ? <FacLoadingState description="A carregar linhas." /> : <DocumentLines linhas={linhas} documento={selected} />}
      <DocumentTotals documento={selected} />
      {diagnostico?.bloqueios?.length ? <FacMessage title="Bloqueios" tone="warning">{diagnostico.bloqueios.join(" ")}</FacMessage> : null}
      <div className="fac-documents-actions">
        {canPdf && <FacButton icon="pi pi-file-pdf" label="Ver PDF" onClick={onOpenPdf} variant="secondary" />}
        {canEmitNow && <FacButton disabled={saving} icon="pi pi-check" label="Emitir" onClick={onEmitir} variant="primary" />}
        {canVoidNow && <FacButton disabled={saving} icon="pi pi-ban" label="Anular" onClick={onOpenAnular} variant="destructive" />}
      </div>
    </section>
  );
}

function DocumentLines({ documento, linhas }: { documento: DocumentoComercial; linhas: LinhaDocumento[] }) {
  if (linhas.length === 0) return <FacEmptyState description="Documento sem linhas." />;
  return (
    <section className="fac-documents-lines">
      <h3>Linhas</h3>
      {linhas.map((linha) => (
        <article className="fac-document-line" key={linha.id}>
          <div><strong>{linha.descricao}</strong><span>{linha.artigoId} - {linha.unidade ?? "-"}</span></div>
          <div><span>Qtd. {formatNumber(linha.quantidade)}</span><span>{money(linha.precoUnitario, documento)}</span></div>
          <div><span>IVA {linha.tipoTaxaIvaId ?? "-"} {formatNumber(linha.percentagemIva)}%</span><strong>{money(linha.totalLinha, documento)}</strong></div>
        </article>
      ))}
    </section>
  );
}

function DocumentTotals({ documento }: { documento: DocumentoComercial }) {
  return (
    <section className="fac-documents-totals" aria-label="Totais do documento">
      <div><span>Subtotal</span><strong>{money(documento.valorBruto, documento)}</strong></div>
      <div><span>Descontos</span><strong>{money(documento.valorDesconto, documento)}</strong></div>
      <div><span>IVA</span><strong>{money(documento.valorIvaTotal, documento)}</strong></div>
      <div><span>Retencao</span><strong>{money(documento.valorRetencao, documento)}</strong></div>
      <div className="fac-documents-total-final"><span>Total</span><strong>{money(documento.valorTotal, documento)}</strong></div>
    </section>
  );
}

function DocumentEditorDialog(props: Parameters<typeof DocumentsContent>[0]) {
  return (
    <FacDialog className="fac-documents-dialog" header="Novo documento" onHide={props.onCloseEditor} visible={props.editorOpen}>
      <DocumentFormFields {...props} />
    </FacDialog>
  );
}

function DocumentFormFields({ catalogos, editorMessage, form, inlineFooter = false, onChangeForm, onCloseEditor, onSave, saving }: Parameters<typeof DocumentsContent>[0] & { inlineFooter?: boolean }) {
  const series = catalogos.series.filter((serie) => serie.tipoDocumentoId === form.tipoDocumentoId);
  const selectedArticle = catalogos.artigos.find((artigo) => artigo.codigo === form.artigoId);
  return (
    <form className="fac-documents-form" onSubmit={onSave}>
      {editorMessage && <FacMessage tone="error" title="Validacao">{editorMessage}</FacMessage>}
      <section className="fac-documents-form-section">
        <h3>Cabecalho</h3>
        <div className="fac-documents-form-grid">
          <FacSelect label="Tipo" onChange={(value) => onChangeForm({ ...form, tipoDocumentoId: value ?? "", serie: firstSerie(catalogos.series, value ?? "") })} options={catalogos.tiposDocumento.map((tipo) => ({ label: `${tipo.id} - ${tipo.descricao}`, value: tipo.id }))} value={form.tipoDocumentoId} />
          <FacSelect label="Serie" onChange={(value) => onChangeForm({ ...form, serie: value ?? "" })} options={series.map((serie) => ({ label: `${serie.serie} - ${serie.nome}`, value: serie.serie }))} value={form.serie} />
          <FacInputText label="Data" onChange={(event) => onChangeForm({ ...form, dataEmissao: event.target.value })} required type="date" value={form.dataEmissao} />
          <FacSelect label="Cliente" onChange={(value) => onChangeForm(applyClientDefaults({ ...form, clienteId: value ?? "" }, catalogos))} options={catalogos.clientes.filter((cliente) => !cliente.inativo).map((cliente) => ({ label: `${cliente.nome} - ${cliente.nif}`, value: String(cliente.id) }))} value={form.clienteId} />
          <FacSelect label="Armazem carga" onChange={(value) => onChangeForm({ ...form, armazemCargaId: value ?? "" })} options={catalogos.armazens.map((armazem) => ({ label: armazem.nome, value: String(armazem.id) }))} value={form.armazemCargaId} />
          <FacSelect label="Moeda" onChange={(value) => onChangeForm({ ...form, moedaId: value ?? "" })} options={catalogos.moedas.map((moeda) => ({ label: moeda.nome, value: moeda.id }))} value={form.moedaId} />
        </div>
      </section>
      <section className="fac-documents-form-section">
        <h3>Primeira linha</h3>
        <div className="fac-documents-form-grid">
          <FacSelect label="Artigo" onChange={(value) => onChangeForm(applyArticleDefaults({ ...form, artigoId: value ?? "" }, catalogos))} options={catalogos.artigos.map((artigo) => ({ label: `${artigo.codigo} - ${artigo.descricao}`, value: artigo.codigo }))} value={form.artigoId} />
          <FacInputText label="Descricao" maxLength={80} onChange={(event) => onChangeForm({ ...form, descricao: event.target.value })} value={form.descricao || selectedArticle?.descricao || ""} />
          <FacInputText label="Quantidade" min="0.000001" onChange={(event) => onChangeForm({ ...form, quantidade: event.target.value })} required step="0.000001" type="number" value={form.quantidade} />
          <FacInputText label="Preco unitario" min="0" onChange={(event) => onChangeForm({ ...form, precoUnitario: event.target.value })} required step="0.000001" type="number" value={form.precoUnitario} />
          <FacSelect label="IVA" onChange={(value) => onChangeForm({ ...form, tipoTaxaIvaId: value ?? "" })} options={catalogos.tiposIva.map((iva) => ({ label: iva.descricao, value: iva.id }))} value={form.tipoTaxaIvaId} />
          <FacInputText label="Desconto" min="0" onChange={(event) => onChangeForm({ ...form, desconto: event.target.value })} step="0.000001" type="number" value={form.desconto} />
        </div>
      </section>
      <label className="fac-documents-textarea"><span>Observacoes</span><textarea maxLength={250} onChange={(event) => onChangeForm({ ...form, observacoes: event.target.value })} value={form.observacoes} /></label>
      {inlineFooter && <DocumentFormFooter onCloseEditor={onCloseEditor} saving={saving} />}
      {!inlineFooter && <DocumentFormFooter onCloseEditor={onCloseEditor} saving={saving} />}
    </form>
  );
}

function DocumentFormFooter({ onCloseEditor, saving }: { onCloseEditor: () => void; saving: boolean }) {
  return (
    <div className="fac-documents-form-footer">
      <FacButton label="Cancelar" onClick={onCloseEditor} type="button" variant="ghost" />
      <FacButton disabled={saving} icon="pi pi-save" label={saving ? "A guardar..." : "Guardar rascunho"} type="submit" variant="primary" />
    </div>
  );
}

function VoidDialog(props: Parameters<typeof DocumentsContent>[0]) {
  return (
    <FacDialog
      className="fac-documents-void-dialog"
      footer={<div className="fac-documents-form-footer"><FacButton label="Cancelar" onClick={props.onCloseAnular} variant="ghost" /><FacButton disabled={props.saving} icon="pi pi-ban" label="Anular" onClick={props.onAnular} variant="destructive" /></div>}
      header="Anular documento"
      onHide={props.onCloseAnular}
      visible={props.voidOpen}
    >
      <FacMessage tone="warning" title="Anulacao fiscal">A anulacao preserva o documento e a auditoria. Nao elimina fisicamente o registo.</FacMessage>
      <label className="fac-documents-textarea"><span>Motivo</span><textarea maxLength={500} minLength={5} onChange={(event) => props.onChangeMotivo(event.target.value)} value={props.motivoAnulacao} /></label>
    </FacDialog>
  );
}

function MobilePageHeader({ action, eyebrow, title }: { action: ReactNode; eyebrow: string; title: string }) {
  return (
    <header className="fac-documents-mobile-header">
      <div><p className="fac-eyebrow">{eyebrow}</p><h1>{title}</h1></div>
      {action}
    </header>
  );
}

function DocumentStatusBadge({ estado }: { estado: EstadoDocumento }) {
  const tone = estado === "EMITIDO" ? "success" : estado === "ANULADO" ? "warning" : "info";
  const label = estado === "RASCUNHO" ? "Rascunho" : estado === "EMITIDO" ? "Emitido" : "Anulado";
  return <FacStatusBadge tone={tone}>{label}</FacStatusBadge>;
}

function DocumentStateFilter({ onChange, value }: { onChange: (value: unknown) => void; value: unknown }) {
  return (
    <select className="fac-data-table-filter-select" onChange={(event) => onChange(event.target.value || null)} value={typeof value === "string" ? value : ""}>
      <option value="">Todos</option>
      <option value="RASCUNHO">Rascunho</option>
      <option value="EMITIDO">Emitido</option>
      <option value="ANULADO">Anulado</option>
    </select>
  );
}

async function fetchPage<T>(url: string): Promise<Page<T>> {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function requestJson<T>(url: string, body?: unknown, method: "GET" | "POST" = "GET"): Promise<T> {
  const response = await apiFetch(url, body === undefined ? undefined : { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function responseError(response: Response) {
  try {
    const data = await response.json();
    return data.message ?? data.error ?? `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}

function initialiseForm(catalogos: Catalogos): DocumentForm {
  const tipoDocumentoId = catalogos.tiposDocumento[0]?.id ?? "";
  const serie = firstSerie(catalogos.series, tipoDocumentoId);
  const form = {
    ...emptyForm,
    tipoDocumentoId,
    serie,
    armazemCargaId: catalogos.armazens[0] ? String(catalogos.armazens[0].id) : "",
    moedaId: catalogos.moedas[0]?.id ?? "",
    rivaId: catalogos.regimesIva[0]?.id ?? "",
    artigoId: catalogos.artigos[0]?.codigo ?? "",
    tipoTaxaIvaId: catalogos.artigos[0]?.ivaVendaId ?? catalogos.tiposIva[0]?.id ?? "",
    precoUnitario: catalogos.artigos[0] ? String(catalogos.artigos[0].pvp) : "0",
    descricao: catalogos.artigos[0]?.descricao ?? "",
    dataEmissao: today()
  };
  return form;
}

function firstSerie(series: Serie[], tipoDocumentoId: string) {
  return series.find((serie) => serie.tipoDocumentoId === tipoDocumentoId)?.serie ?? "";
}

function applyClientDefaults(form: DocumentForm, catalogos: Catalogos) {
  const cliente = catalogos.clientes.find((item) => String(item.id) === form.clienteId);
  if (!cliente) return form;
  return {
    ...form,
    moedaId: cliente.moedaId ?? form.moedaId,
    rivaId: cliente.rivaId ?? form.rivaId,
    mPagamentoId: cliente.mPagamentoId ? String(cliente.mPagamentoId) : form.mPagamentoId,
    pPagamentoId: cliente.pPagamentoId ?? form.pPagamentoId,
    transporteId: cliente.transporteId ? String(cliente.transporteId) : form.transporteId
  };
}

function applyArticleDefaults(form: DocumentForm, catalogos: Catalogos) {
  const artigo = catalogos.artigos.find((item) => item.codigo === form.artigoId);
  if (!artigo) return form;
  return {
    ...form,
    descricao: artigo.descricao,
    precoUnitario: String(artigo.pvp),
    tipoTaxaIvaId: artigo.ivaVendaId
  };
}

function validateForm(form: DocumentForm) {
  if (!form.tipoDocumentoId) return "Seleciona o tipo de documento.";
  if (!form.serie) return "Seleciona a serie.";
  if (!form.dataEmissao) return "Indica a data de emissao.";
  if (!form.clienteId) return "Seleciona o cliente.";
  if (!form.armazemCargaId) return "Seleciona o armazem de carga.";
  if (!form.artigoId) return "Seleciona o artigo.";
  if (Number(form.quantidade) <= 0) return "A quantidade deve ser maior que zero.";
  if (Number(form.precoUnitario) < 0) return "O preco nao pode ser negativo.";
  if (Number(form.desconto || 0) < 0) return "O desconto nao pode ser negativo.";
  return null;
}

function nullable(value: string) {
  return value.trim() ? value.trim() : null;
}

function nullableNumber(value: string) {
  return value ? Number(value) : null;
}

function documentRef(doc: DocumentoComercial) {
  return doc.numeroDocumentoCompleto || `${doc.tipoDocumentoId} ${doc.serie}/${doc.numeroDocumento ?? doc.id}`;
}

function date(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("pt-PT").format(new Date(`${value}T00:00:00`));
}

function money(value: string | number | null | undefined, doc: DocumentoComercial) {
  const amount = Number(value ?? 0);
  const currency = doc.moedaCodigo || doc.moedaId || "EUR";
  return new Intl.NumberFormat("pt-PT", { currency, maximumFractionDigits: doc.moedaCasasDecimais ?? 2, minimumFractionDigits: 2, style: "currency" }).format(amount);
}

function formatNumber(value: string | number | null | undefined) {
  return new Intl.NumberFormat("pt-PT", { maximumFractionDigits: 6 }).format(Number(value ?? 0));
}
