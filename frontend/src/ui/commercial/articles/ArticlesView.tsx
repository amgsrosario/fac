import { FormEvent, ReactNode, useEffect, useMemo, useRef, useState } from "react";
import { FilterMatchMode } from "primereact/api";
import { Paginator } from "primereact/paginator";
import { useLocation } from "react-router-dom";
import { GlobalSearch } from "../../../GlobalSearch";
import { apiFetch, AuthSession } from "../../../api";
import {
  DEFAULT_PRODUCT_PROFILE,
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
  getCapabilities,
  hasCapability,
  useDeviceClass,
  useFacToast
} from "../../fac";
import { CommercialSidebar, ModuleHeader } from "../shared";
import "./articles.css";

type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
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

type ServiceForm = {
  codigo: string;
  descricao: string;
  tipoArtigo: TipoArtigo;
  familiaId: string;
  unidade: string;
  pvp: string;
  ivaVendaId: string;
  inativo: boolean;
  retencao: boolean;
  observacoes: string;
};

type HiddenDefaults = {
  ivaCompraId: string;
  peso: string;
  abreviatura: string;
  codigoIdentificacao: string;
};

type EditorMode = "create" | "edit";
type MobileScreen = "list" | "detail" | "form";

const emptyForm: ServiceForm = {
  codigo: "",
  descricao: "",
  tipoArtigo: "SERVICO",
  familiaId: "",
  unidade: "UN",
  pvp: "0",
  ivaVendaId: "",
  inativo: false,
  retencao: false,
  observacoes: ""
};

const servicesCapabilities = getCapabilities(DEFAULT_PRODUCT_PROFILE);
const advancedArticleFieldsEnabled = hasCapability("ADVANCED_ARTICLE_FIELDS", servicesCapabilities);

const serviceUnitOptions = [
  { label: "UN - Unidade", value: "UN" },
  { label: "H - Hora", value: "H" },
  { label: "DIA - Dia", value: "DIA" },
  { label: "MES - Mes", value: "MES" },
  { label: "SES - Sessao", value: "SES" },
  { label: "SER - Servico", value: "SER" },
  { label: "AVN - Avenca", value: "AVN" }
];

export default function ArticlesView({
  currentUser,
  onLogout
}: {
  currentUser: AuthSession;
  onLogout: () => void;
}) {
  const location = useLocation();
  const canManage = currentUser.permissoes?.includes("MESTRES_GERIR") ?? false;
  const { deviceClass, isMobile } = useDeviceClass();
  const { showToast } = useFacToast();
  const [services, setServices] = useState<Artigo[]>([]);
  const [familias, setFamilias] = useState<Familia[]>([]);
  const [tiposIva, setTiposIva] = useState<TipoTaxaIva[]>([]);
  const [selectedCodigo, setSelectedCodigo] = useState<string | null>(null);
  const [selectedOverride, setSelectedOverride] = useState<Artigo | null>(null);
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [stateFilter, setStateFilter] = useState<"all" | "active" | "inactive">("all");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortField, setSortField] = useState("codigo");
  const [sortOrder, setSortOrder] = useState<1 | -1>(1);
  const [columnFilters, setColumnFilters] = useState({ codigo: "", descricao: "", unidade: "", ivaVendaId: "", inativo: "" });
  const requestRef = useRef(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editorMode, setEditorMode] = useState<EditorMode>("create");
  const [form, setForm] = useState<ServiceForm>(emptyForm);
  const [editorMessage, setEditorMessage] = useState<string | null>(null);
  const [mobileScreen, setMobileScreen] = useState<MobileScreen>("list");

  useEffect(() => {
    loadCatalogs();
  }, []);

  useEffect(() => {
    const timeout = window.setTimeout(() => { setPage(0); setDebouncedSearch(search.trim()); }, 300);
    return () => window.clearTimeout(timeout);
  }, [search]);

  useEffect(() => {
    void loadServices();
  }, [page, pageSize, debouncedSearch, stateFilter, sortField, sortOrder, columnFilters]);

  useEffect(() => {
    const artigoId = new URLSearchParams(location.search).get("artigo");
    if (!artigoId) return;
    const decoded = decodeURIComponent(artigoId);
    const current = services.find((service) => service.codigo === decoded);
    if (current) {
      setSelectedCodigo(decoded);
      setSelectedOverride(null);
      setMobileScreen("detail");
      return;
    }
    requestJson<Artigo>(`/api/artigos/${encodeURIComponent(decoded)}`)
      .then((service) => { setSelectedOverride(service); setSelectedCodigo(decoded); setMobileScreen("detail"); })
      .catch(() => undefined);
  }, [location.search, services]);

  async function loadCatalogs() {
    setError(null);
    try {
      const [familiasPage, tiposIvaPage] = await Promise.all([
        fetchPage<Familia>("/api/familias?size=200&sort=descricao,asc"),
        fetchPage<TipoTaxaIva>("/api/tipos-taxa-iva?size=100&sort=descricao,asc")
      ]);
      setFamilias(familiasPage.content);
      setTiposIva(tiposIvaPage.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar os artigos.");
    }
  }

  async function loadServices(preferredCodigo?: string): Promise<boolean> {
    const requestId = ++requestRef.current;
    setLoading(true);
    setError(null);
    const params = new URLSearchParams({ page: String(page), size: String(pageSize) });
    if (debouncedSearch) params.set("search", debouncedSearch);
    const effectiveInactive = columnFilters.inativo || (stateFilter === "active" ? "false" : stateFilter === "inactive" ? "true" : "");
    if (effectiveInactive) params.set("inativo", effectiveInactive);
    if (columnFilters.codigo) params.set("codigo", columnFilters.codigo);
    if (columnFilters.descricao) params.set("descricao", columnFilters.descricao);
    if (columnFilters.unidade) params.set("unidade", columnFilters.unidade);
    if (columnFilters.ivaVendaId) params.set("ivaVendaId", columnFilters.ivaVendaId);
    params.append("sort", `${sortField},${sortOrder === 1 ? "asc" : "desc"}`);
    if (sortField !== "codigo") params.append("sort", "codigo,asc");
    try {
      const result = await fetchPage<Artigo>(`/api/artigos?${params}`);
      if (requestId !== requestRef.current) return false;
      setServices(result.content);
      setTotalElements(result.totalElements);
      setTotalPages(result.totalPages ?? 0);
      const preferredVisible = preferredCodigo != null && result.content.some((service) => service.codigo === preferredCodigo);
      setSelectedCodigo((current) => {
        const wanted = preferredCodigo ?? current;
        return wanted && result.content.some((service) => service.codigo === wanted) ? wanted : result.content[0]?.codigo ?? null;
      });
      if (preferredVisible || !preferredCodigo) setSelectedOverride(null);
      return preferredVisible;
    } catch (err) {
      if (requestId === requestRef.current) setError(err instanceof Error ? err.message : "Não foi possível carregar os artigos.");
      return false;
    } finally {
      if (requestId === requestRef.current) setLoading(false);
    }
  }

  const filtered = services;
  const selected = services.find((service) => service.codigo === selectedCodigo)
    ?? (selectedOverride?.codigo === selectedCodigo ? selectedOverride : null);
  const selectedIva = tiposIva.find((tipo) => tipo.id === selected?.ivaVendaId)?.descricao ?? selected?.ivaVendaId ?? "-";
  const hiddenDefaultSource = editorOpen && editorMode === "edit" ? selected : null;
  const defaults = useMemo(() => resolveHiddenDefaults(tiposIva, hiddenDefaultSource), [hiddenDefaultSource, tiposIva]);
  const activeCount = stateFilter === "active" ? totalElements : services.filter((service) => !service.inativo).length;

  function openNew() {
    if (!canManage) return;
    setEditorMode("create");
    setForm({ ...emptyForm, ivaVendaId: firstActiveIva(tiposIva)?.id ?? "" });
    setEditorMessage(null);
    setEditorOpen(true);
    setMobileScreen("form");
  }

  function openEdit(service: Artigo) {
    if (!canManage) return;
    setEditorMode("edit");
    setForm({
      codigo: service.codigo,
      descricao: service.descricao,
      tipoArtigo: service.tipoArtigo ?? "SERVICO",
      familiaId: String(service.familiaId),
      unidade: service.unidade,
      pvp: String(service.pvp),
      ivaVendaId: service.ivaVendaId,
      inativo: service.inativo,
      retencao: service.retencao,
      observacoes: service.observacoes ?? ""
    });
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
    const validation = validate(form, editorMode === "edit", defaults);
    if (validation) {
      setEditorMessage(validation);
      return;
    }

    setSaving(true);
    setEditorMessage(null);
    setError(null);
    try {
      const payload = toPayload(form, defaults, editorMode === "create");
      if (editorMode === "edit") {
        await request(`/api/artigos/${encodeURIComponent(form.codigo)}`, "PUT", payload);
      } else {
        await request("/api/artigos", "POST", payload);
      }
      const visible = await loadServices(form.codigo);
      if (!visible) {
        const saved = await requestJson<Artigo>(`/api/artigos/${encodeURIComponent(form.codigo)}`);
        setSelectedOverride(saved);
        setSelectedCodigo(saved.codigo);
      }
      setEditorOpen(false);
      setNotice(`Artigo ${form.codigo} ${editorMode === "edit" ? "atualizado" : "criado"}.`);
      showToast({ detail: `Artigo ${form.codigo} guardado.`, severity: "success", summary: "Guardado" });
      if (isMobile) setMobileScreen("detail");
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Não foi possível guardar o artigo.");
    } finally {
      setSaving(false);
    }
  }

  function selectService(codigo: string) {
    setSelectedOverride(null);
    setSelectedCodigo(codigo);
    if (isMobile) setMobileScreen("detail");
  }

  const sidebar = (
    <CommercialSidebar
      active="articles"
      currentUser={currentUser}
      onLogout={onLogout}
    />
  );

  const content = (
    <ServicesContent
      activeCount={activeCount}
      canManage={canManage}
      defaults={defaults}
      deviceClass={deviceClass}
      editorMessage={editorMessage}
      editorMode={editorMode}
      editorOpen={editorOpen}
      error={error}
      filtered={filtered}
      form={form}
      familias={familias}
      loading={loading}
      mobileScreen={mobileScreen}
      notice={notice}
      onBackToList={() => setMobileScreen("list")}
      onChangeForm={setForm}
      onCloseEditor={closeEditor}
      onEdit={openEdit}
      onNew={openNew}
      onSave={save}
      onSearch={setSearch}
      onColumnFilters={(filters) => { setPage(0); setColumnFilters(filters); }}
      onPageChange={(nextPage, nextSize) => { setPage(nextSize === pageSize ? nextPage : 0); setPageSize(nextSize); }}
      onSortChange={(field, order) => { setPage(0); setSortField(field); setSortOrder(order); }}
      onSelect={selectService}
      onStateFilter={(value) => { setPage(0); setStateFilter(value); }}
      page={page}
      pageSize={pageSize}
      saving={saving}
      search={search}
      selected={selected}
      selectedIva={selectedIva}
      services={services}
      stateFilter={stateFilter}
      sortField={sortField}
      sortOrder={sortOrder}
      totalElements={totalElements}
      totalPages={totalPages}
      tiposIva={tiposIva}
    />
  );

  return (
    <ResponsiveSlot
      desktop={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
      mobile={<MobileShell title="Artigos">{content}</MobileShell>}
      tablet={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
    />
  );
}

function ServicesContent(props: {
  activeCount: number;
  canManage: boolean;
  defaults: HiddenDefaults;
  deviceClass: string;
  editorMessage: string | null;
  editorMode: EditorMode;
  editorOpen: boolean;
  error: string | null;
  familias: Familia[];
  filtered: Artigo[];
  form: ServiceForm;
  loading: boolean;
  mobileScreen: MobileScreen;
  notice: string | null;
  onBackToList: () => void;
  onChangeForm: (form: ServiceForm) => void;
  onCloseEditor: () => void;
  onEdit: (service: Artigo) => void;
  onNew: () => void;
  onSave: (event?: FormEvent) => void;
  onSearch: (value: string) => void;
  onColumnFilters: (filters: { codigo: string; descricao: string; unidade: string; ivaVendaId: string; inativo: string }) => void;
  onPageChange: (page: number, size: number) => void;
  onSortChange: (field: string, order: 1 | -1) => void;
  onSelect: (codigo: string) => void;
  onStateFilter: (value: "all" | "active" | "inactive") => void;
  saving: boolean;
  search: string;
  page: number;
  pageSize: number;
  selected: Artigo | null;
  selectedIva: string;
  services: Artigo[];
  stateFilter: "all" | "active" | "inactive";
  sortField: string;
  sortOrder: 1 | -1;
  totalElements: number;
  totalPages: number;
  tiposIva: TipoTaxaIva[];
}) {
  const isMobile = props.deviceClass === "mobile";

  if (isMobile) {
    return (
      <MobileServicesContent {...props} />
    );
  }

  return (
    <>
      <section className="fac-services-header-grid">
        <ServicesHeader {...props} />
        <article className="fac-services-panel fac-services-detail-top">
          <ServiceDetail {...props} />
        </article>
      </section>
      <ServicesToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operação concluída">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <section
        aria-label="Catálogo de artigos"
        className="fac-services-layout"
        data-advanced-article-fields={advancedArticleFieldsEnabled ? "on" : "off"}
        data-product-profile={DEFAULT_PRODUCT_PROFILE}
      >
        <article className="fac-services-panel fac-services-list-panel">
          <ServicesList {...props} />
        </article>
        <aside className="fac-services-panel fac-services-detail-panel">
          <ServiceDetail {...props} />
        </aside>
      </section>
      <ServiceEditorDialog {...props} />
    </>
  );
}

function MobileServicesContent(props: Parameters<typeof ServicesContent>[0]) {
  if (props.editorOpen || props.mobileScreen === "form") {
    return (
      <section className="fac-services-mobile-page">
        <MobilePageHeader
          action={<FacButton icon="pi pi-times" label="Fechar" onClick={props.onCloseEditor} variant="ghost" />}
          eyebrow={props.editorMode === "edit" ? "Editar artigo" : "Novo artigo"}
          title={props.editorMode === "edit" ? props.form.codigo : "Novo"}
        />
        <ServiceFormFields {...props} />
      </section>
    );
  }

  if (props.mobileScreen === "detail" && props.selected) {
    return (
      <section className="fac-services-mobile-page">
        <MobilePageHeader
          action={<FacButton icon="pi pi-arrow-left" label="Lista" onClick={props.onBackToList} variant="ghost" />}
          eyebrow="Artigo"
          title={props.selected.descricao}
        />
        <ServiceDetail {...props} />
      </section>
    );
  }

  return (
    <>
      <ServicesHeader {...props} compact />
      <ServicesToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operação concluída">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <ServicesList {...props} />
    </>
  );
}

function ServicesHeader({
  activeCount,
  canManage,
  compact = false,
  loading,
  onNew,
  stateFilter,
  totalElements
}: Parameters<typeof ServicesContent>[0] & { compact?: boolean }) {
  return (
    <ModuleHeader
      action={
        <>
          <GlobalSearch className="fac-commercial-global-search" />
        </>
      }
      className="fac-services-header"
      compact={compact}
      eyebrow="Catálogo"
      subtitle="Gerir produtos e serviços utilizados nos documentos."
      summary={
        <div className="fac-module-summary" aria-label="Resumo de artigos">
          <span>{loading ? "A carregar" : `${totalElements} artigos`}</span>
          <strong>{activeCount} {stateFilter === "active" ? "ativos" : "ativos nesta página"}</strong>
        </div>
      }
      title="Artigos"
    />
  );
}

function ServicesToolbar({
  canManage,
  deviceClass,
  onNew,
  onSearch,
  onStateFilter,
  search,
  stateFilter
}: Parameters<typeof ServicesContent>[0]) {
  const placeholder = deviceClass === "mobile" ? "Pesquisar artigos" : "Pesquisar por código, descrição ou identificação";

  return (
    <section className="fac-services-toolbar" aria-label="Pesquisa e filtros">
      <FacInputText
        aria-label="Pesquisar artigos"
        onChange={(event) => onSearch(event.target.value)}
        placeholder={placeholder}
        type="search"
        value={search}
      />
      <FacSelect
        onChange={(value) => onStateFilter((value as "all" | "active" | "inactive") ?? "all")}
        options={[
          { label: "Todos", value: "all" },
          { label: "Ativos", value: "active" },
          { label: "Inativos", value: "inactive" }
        ]}
        value={stateFilter}
      />
      {canManage && <FacButton icon="pi pi-plus" label={deviceClass === "mobile" ? "Novo" : "Novo artigo"} onClick={onNew} variant="primary" />}
    </section>
  );
}

function ServicesList({
  deviceClass,
  filtered,
  loading,
  onColumnFilters,
  onPageChange,
  onSelect,
  onSortChange,
  page,
  pageSize,
  search,
  selected,
  services,
  sortField,
  sortOrder,
  stateFilter,
  tiposIva,
  totalElements,
  totalPages
}: Parameters<typeof ServicesContent>[0]) {
  if (loading) return <FacLoadingState description="A carregar artigos." />;
  if (services.length === 0) return <FacEmptyState description="Ainda não existem artigos no catálogo." />;
  if (filtered.length === 0) return <FacEmptyState description="Sem resultados para a pesquisa e filtros atuais." />;

  if (deviceClass !== "mobile") {
    const columns: FacDataTableColumn<Artigo>[] = [
      { field: "codigo", filter: true, filterPlaceholder: "Código", header: "Código", sortable: true, style: { width: "8rem" } },
      { field: "descricao", filter: true, filterPlaceholder: "Descrição", header: "Descrição", sortable: true },
      {
        field: "unidade",
        filter: true,
        filterElement: (options) => <ColumnSelectFilter onChange={options.filterApplyCallback} options={serviceUnitOptions.map((option) => option.value)} value={options.value} />,
        filterMatchMode: FilterMatchMode.EQUALS,
        header: "Unidade",
        sortable: true,
        style: { width: "7rem" }
      },
      { body: (service) => `${money(service.pvp)} EUR`, dataType: "numeric", field: "pvp", header: "PVP", sortable: true, style: { width: "8rem" } },
      {
        field: "ivaVendaId",
        filter: true,
        filterElement: (options) => <ColumnSelectFilter onChange={options.filterApplyCallback} options={tiposIva.map((tipo) => tipo.id).sort()} value={options.value} />,
        filterMatchMode: FilterMatchMode.EQUALS,
        header: "IVA",
        sortable: true,
        style: { width: "7rem" }
      },
      {
        body: (service) => <StateBadge inactive={service.inativo} />,
        field: "inativo",
        filter: true,
        filterElement: (options) => <StateColumnFilter onChange={options.filterApplyCallback} value={options.value} />,
        filterMatchMode: FilterMatchMode.EQUALS,
        header: "Estado",
        sortable: true,
        style: { width: "7rem" }
      }
    ];

    return (
      <FacDataTable
        ariaLabel="Tabela de artigos"
        className="fac-services-data-table"
        columns={columns}
        dataKey="codigo"
        emptyMessage="Sem resultados para a pesquisa e filtros atuais."
        globalFilter={search}
        globalFilterFields={["codigo", "descricao", "abreviatura", "codigoIdentificacao"]}
        first={page * pageSize}
        lazy
        loading={loading}
        onLazyFilter={(event) => onColumnFilters({
          codigo: filterValue(event.filters, "codigo"),
          descricao: filterValue(event.filters, "descricao"),
          unidade: filterValue(event.filters, "unidade"),
          ivaVendaId: filterValue(event.filters, "ivaVendaId"),
          inativo: filterValue(event.filters, "inativo")
        })}
        onLazyPage={(event) => onPageChange(event.page ?? 0, event.rows)}
        onLazySort={(event) => onSortChange(String(event.sortField ?? "codigo"), event.sortOrder === 1 ? 1 : -1)}
        onSelectionChange={(service) => service && onSelect(service.codigo)}
        paginator={totalPages > 1}
        rows={pageSize}
        rowsPerPageOptions={[20, 50]}
        selection={selected}
        sortField={sortField}
        sortOrder={sortOrder}
        totalRecords={totalElements}
        value={services}
      />
    );
  }

  return (
    <>
      <div className="fac-services-list-meta">
        <span>{totalElements} resultados</span>
      </div>
      <div className="fac-services-table-wrap" data-filter={stateFilter} data-search={search ? "active" : "empty"}>
        <table className="fac-services-table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Descrição</th>
              <th>Unidade</th>
              <th>PVP</th>
              <th>IVA</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((service) => (
              <tr
                aria-selected={service.codigo === selected?.codigo}
                className={service.codigo === selected?.codigo ? "is-selected" : ""}
                key={service.codigo}
                onClick={() => onSelect(service.codigo)}
                tabIndex={0}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") onSelect(service.codigo);
                }}
              >
                <td>{service.codigo}</td>
                <td>{service.descricao}</td>
                <td>{service.unidade}</td>
                <td>{money(service.pvp)} EUR</td>
                <td>{service.ivaVendaId}</td>
                <td><StateBadge inactive={service.inativo} /></td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="fac-services-mobile-list">
          {filtered.map((service) => (
            <button className="fac-service-card" key={service.codigo} onClick={() => onSelect(service.codigo)} type="button">
              <strong>{service.descricao}</strong>
              <span>{service.codigo} · {service.unidade}</span>
              <span>{money(service.pvp)} EUR + IVA</span>
              <StateBadge inactive={service.inativo} />
            </button>
          ))}
        </div>
      </div>
      {totalPages > 1 && <Paginator first={page * pageSize} onPageChange={(event) => onPageChange(event.page, event.rows)} rows={pageSize} rowsPerPageOptions={[20, 50]} totalRecords={totalElements} />}
    </>
  );
}

function filterValue(filters: Record<string, unknown>, field: string) {
  const filter = filters[field] as { value?: unknown } | undefined;
  if (typeof filter?.value === "boolean") return String(filter.value);
  return typeof filter?.value === "string" ? filter.value.trim() : "";
}

function ServiceDetail({
  canManage,
  onEdit,
  selected,
  selectedIva
}: Parameters<typeof ServicesContent>[0]) {
  if (!selected) return <FacEmptyState description="Seleciona um artigo para ver o detalhe." />;

  return (
    <section className="fac-services-detail">
      <div className="fac-services-detail-title">
        <div>
          <p className="fac-eyebrow">Detalhe do artigo</p>
          <h2>{selected.descricao}</h2>
        </div>
        <StateBadge inactive={selected.inativo} />
      </div>
      <dl>
        <div><dt>Código</dt><dd>{selected.codigo}</dd></div>
        <div><dt>Tipo</dt><dd>{tipoArtigoLabel(selected.tipoArtigo)}</dd></div>
        <div><dt>Unidade</dt><dd>{selected.unidade}</dd></div>
        <div><dt>Preço sem IVA</dt><dd>{money(selected.pvp)} EUR</dd></div>
        <div><dt>Taxa de IVA</dt><dd>{selectedIva}</dd></div>
        <div><dt>Artigo ativo</dt><dd>{selected.inativo ? "Não" : "Sim"}</dd></div>
      </dl>
      {canManage
        ? <FacButton icon="pi pi-pencil" label="Editar artigo" onClick={() => onEdit(selected)} variant="primary" />
        : <FacMessage title="Consulta">Sem permissao para criar ou editar artigos.</FacMessage>}
    </section>
  );
}

function ServiceEditorDialog(props: Parameters<typeof ServicesContent>[0]) {
  return (
    <FacDialog
      className="fac-services-dialog"
      header={props.editorMode === "edit" ? "Editar artigo" : "Novo artigo"}
      onHide={props.onCloseEditor}
      visible={props.editorOpen}
    >
      <ServiceFormFields {...props} />
    </FacDialog>
  );
}

function FormSection({ children, title }: { children: ReactNode; title: string }) {
  return (
    <fieldset className="fac-services-form-section">
      <legend>{title}</legend>
      <div className="fac-services-form-grid">{children}</div>
    </fieldset>
  );
}

function ServiceFormFields({
  defaults,
  editorMessage,
  editorMode,
  familias,
  form,
  onChangeForm,
  onCloseEditor,
  onSave,
  saving,
  tiposIva
}: Parameters<typeof ServicesContent>[0]) {
  const [moreOptionsOpen, setMoreOptionsOpen] = useState(false);
  const ivaOptions = tiposIva.map((tipo) => ({
    label: `${tipo.descricao}${tipo.inativo && tipo.id !== form.ivaVendaId ? " (inativo)" : ""}`,
    value: tipo.id
  }));
  const familiaOptions = familias.map((familia) => ({
    label: familia.descricao,
    value: String(familia.id)
  }));

  useEffect(() => {
    setMoreOptionsOpen(editorMode === "edit" && Boolean(form.familiaId || form.observacoes.trim() || form.retencao || form.inativo));
  }, [editorMode, form.codigo]);

  useEffect(() => {
    if (editorMessage) setMoreOptionsOpen(true);
  }, [editorMessage]);

  return (
    <form className="fac-services-form" onSubmit={onSave}>
      {editorMessage && <FacMessage tone="error" title="Validação">{editorMessage}</FacMessage>}
      <div className="fac-services-form-sections">
        <FormSection title="Identificação">
          <FacInputText
            disabled={editorMode === "edit"}
            label="Código"
            maxLength={50}
            onChange={(event) => onChangeForm({ ...form, codigo: normalizeCode(event.target.value) })}
            required
            value={form.codigo}
          />
          <FacInputText
            label="Descrição"
            maxLength={80}
            onChange={(event) => onChangeForm({ ...form, descricao: event.target.value })}
            required
            value={form.descricao}
          />
          <FacSelect
            label="Tipo Artigo/Serviço"
            onChange={(value) => onChangeForm({ ...form, tipoArtigo: (value as TipoArtigo | null) ?? "SERVICO" })}
            options={[
              { label: "Serviço", value: "SERVICO" },
              { label: "Artigo", value: "ARTIGO" }
            ]}
            value={form.tipoArtigo}
          />
          <FacSelect
            label="Unidade"
            onChange={(value) => onChangeForm({ ...form, unidade: value ?? "" })}
            options={serviceUnitOptions}
            value={form.unidade}
          />
        </FormSection>

        <FormSection title="Preço e fiscalidade">
          <FacInputText
            label="Preço de venda"
            min="0"
            onChange={(event) => onChangeForm({ ...form, pvp: event.target.value })}
            required
            step="0.000001"
            type="number"
            value={form.pvp}
          />
          <FacSelect
            label="IVA de venda"
            onChange={(value) => onChangeForm({ ...form, ivaVendaId: value ?? "" })}
            options={ivaOptions}
            value={form.ivaVendaId}
          />
        </FormSection>

        <section className={`fac-services-more-options${moreOptionsOpen ? " open" : ""}`}>
          <button
            aria-controls="fac-service-more-options"
            aria-expanded={moreOptionsOpen}
            className="fac-services-more-trigger"
            onClick={() => setMoreOptionsOpen((open) => !open)}
            type="button"
          >
            <span>Mais opções</span>
            {(form.familiaId || form.observacoes.trim() || form.retencao || form.inativo) && <small>Com valores</small>}
          </button>
          <div className="fac-services-form-grid" hidden={!moreOptionsOpen} id="fac-service-more-options">
            <FacSelect
              label="Família"
              onChange={(value) => onChangeForm({ ...form, familiaId: value ?? "" })}
              options={familiaOptions}
              value={form.familiaId}
            />
            <label className="fac-services-textarea">
              <span>Observações</span>
              <textarea maxLength={250} onChange={(event) => onChangeForm({ ...form, observacoes: event.target.value })} value={form.observacoes} />
            </label>
            <label className="fac-services-check">
              <input checked={form.retencao} onChange={(event) => onChangeForm({ ...form, retencao: event.target.checked })} type="checkbox" />
              <span>Sujeito a retenção</span>
            </label>
            <label className="fac-services-check">
              <input checked={form.inativo} onChange={(event) => onChangeForm({ ...form, inativo: event.target.checked })} type="checkbox" />
              <span>Artigo inativo</span>
            </label>
          </div>
        </section>
      </div>
      <div className="fac-services-form-footer">
        <FacButton label="Cancelar" onClick={onCloseEditor} type="button" variant="ghost" />
        <FacButton disabled={saving} icon="pi pi-save" label={saving ? "A guardar..." : "Guardar"} type="submit" variant="primary" />
      </div>
    </form>
  );
}

function LegacyCommercialSidebar({
  active,
  currentUser,
  onLogout
}: {
  active: "articles" | "customers";
  currentUser: AuthSession;
  onLogout: () => void;
}) {
  return (
    <div className="fac-commercial-nav">
      <div className="fac-commercial-brand">
        <span>TUULI</span>
        <div>
          <strong>TUULI</strong>
          <small>AIR</small>
        </div>
      </div>
      <nav aria-label="Navegação comercial">
        <a className={active === "articles" ? "active" : ""} href="/artigos">
          <strong>Artigos</strong>
          <small>Catálogo</small>
        </a>
        <a className={active === "customers" ? "active" : ""} href="/clientes">
          <strong>Clientes</strong>
          <small>Documentos</small>
        </a>
        <a href="/ui-lab">
          <strong>UI Lab</strong>
          <small>Fundacao</small>
        </a>
      </nav>
      <div className="fac-commercial-user">
        <span>{currentUser.nome}</span>
        <small>{currentUser.papel} · {currentUser.codigo}</small>
        <FacButton label="Sair" onClick={onLogout} variant="text" />
      </div>
    </div>
  );
}

function MobilePageHeader({
  action,
  eyebrow,
  title
}: {
  action: ReactNode;
  eyebrow: string;
  title: string;
}) {
  return (
    <header className="fac-services-mobile-header">
      <div>
        <p className="fac-eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
      </div>
      {action}
    </header>
  );
}

function StateBadge({ inactive }: { inactive: boolean }) {
  return <FacStatusBadge tone={inactive ? "warning" : "success"}>{inactive ? "Inativo" : "Ativo"}</FacStatusBadge>;
}

function ColumnSelectFilter({ onChange, options, value }: { onChange: (value: unknown) => void; options: string[]; value: unknown }) {
  return (
    <select className="fac-data-table-filter-select" onChange={(event) => onChange(event.target.value || null)} value={typeof value === "string" ? value : ""}>
      <option value="">Todos</option>
      {options.map((option) => <option key={option} value={option}>{option}</option>)}
    </select>
  );
}

function StateColumnFilter({ onChange, value }: { onChange: (value: unknown) => void; value: unknown }) {
  const current = typeof value === "boolean" ? String(value) : "";
  return (
    <select className="fac-data-table-filter-select" onChange={(event) => onChange(event.target.value === "" ? null : event.target.value === "true")} value={current}>
      <option value="">Todos</option>
      <option value="false">Ativo</option>
      <option value="true">Inativo</option>
    </select>
  );
}

async function fetchPage<T>(url: string): Promise<Page<T>> {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function requestJson<T>(url: string): Promise<T> {
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

function validate(form: ServiceForm, editing: boolean, defaults: HiddenDefaults) {
  if (!editing && !/^[A-Z0-9]{1,50}$/.test(form.codigo)) return "O código deve conter apenas letras maiúsculas e números.";
  if (!form.descricao.trim()) return "A descrição é obrigatória.";
  if (!form.tipoArtigo) return "Tipo Artigo/Serviço é obrigatório.";
  if (!form.familiaId) return "A família é obrigatória.";
  if (!form.unidade.trim()) return "A unidade é obrigatória.";
  if (!form.ivaVendaId) return "O IVA de venda é obrigatório.";
  if (!defaults.ivaCompraId) return "Não existe uma taxa de IVA segura nos catálogos atuais.";
  if (form.pvp === "" || Number(form.pvp) < 0) return "O preço de venda deve ser igual ou superior a zero.";
  return null;
}

function toPayload(form: ServiceForm, defaults: HiddenDefaults, creating: boolean) {
  return {
    ...(creating ? { codigo: form.codigo } : {}),
    abreviatura: blankToNull(defaults.abreviatura),
    codigoIdentificacao: blankToNull(defaults.codigoIdentificacao),
    descricao: form.descricao.trim(),
    tipoArtigo: form.tipoArtigo,
    unidade: form.unidade.trim().toUpperCase(),
    familiaId: Number(form.familiaId),
    peso: Number(defaults.peso),
    ivaCompraId: defaults.ivaCompraId,
    ivaVendaId: form.ivaVendaId,
    pvp: Number(form.pvp),
    inativo: form.inativo,
    retencao: form.retencao,
    observacoes: blankToNull(form.observacoes)
  };
}

function resolveHiddenDefaults(tiposIva: TipoTaxaIva[], selected: Artigo | null): HiddenDefaults {
  const ivaCompra = selected?.ivaCompraId
    ? tiposIva.find((tipo) => tipo.id === selected.ivaCompraId) ?? firstActiveIva(tiposIva)
    : firstActiveIva(tiposIva);
  return {
    ivaCompraId: ivaCompra?.id ?? "",
    peso: String(selected?.peso ?? 0),
    abreviatura: selected?.abreviatura ?? "",
    codigoIdentificacao: selected?.codigoIdentificacao ?? ""
  };
}

function firstActiveIva(tiposIva: TipoTaxaIva[]) {
  return tiposIva.find((tipo) => !tipo.inativo) ?? tiposIva[0] ?? null;
}

function normalizeCode(value: string) {
  return value.toUpperCase().replace(/[^A-Z0-9]/g, "");
}

function blankToNull(value: string) {
  const trimmed = value.trim();
  return trimmed || null;
}

function tipoArtigoLabel(tipo: TipoArtigo) {
  return tipo === "ARTIGO" ? "Artigo" : "Serviço";
}

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6
  });
}
