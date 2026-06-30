import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { apiFetch, AuthSession } from "../../../api";
import {
  DEFAULT_PRODUCT_PROFILE,
  DesktopShell,
  FacButton,
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
import "./articles.css";

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

type ServiceForm = {
  codigo: string;
  descricao: string;
  familiaId: string;
  unidade: string;
  pvp: string;
  ivaVendaId: string;
  inativo: boolean;
};

type HiddenDefaults = {
  ivaCompraId: string;
  peso: string;
  retencao: boolean;
  abreviatura: string;
  codigoIdentificacao: string;
  observacoes: string;
};

type EditorMode = "create" | "edit";
type MobileScreen = "list" | "detail" | "form";

const emptyForm: ServiceForm = {
  codigo: "",
  descricao: "",
  familiaId: "",
  unidade: "UN",
  pvp: "0",
  ivaVendaId: "",
  inativo: false
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
  const canManage = currentUser.permissoes?.includes("MESTRES_GERIR") ?? false;
  const { deviceClass, isMobile } = useDeviceClass();
  const { showToast } = useFacToast();
  const [services, setServices] = useState<Artigo[]>([]);
  const [familias, setFamilias] = useState<Familia[]>([]);
  const [tiposIva, setTiposIva] = useState<TipoTaxaIva[]>([]);
  const [selectedCodigo, setSelectedCodigo] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [stateFilter, setStateFilter] = useState<"all" | "active" | "inactive">("all");
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
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const [artigosPage, familiasPage, tiposIvaPage] = await Promise.all([
        fetchPage<Artigo>("/api/artigos?size=200&sort=codigo,asc"),
        fetchPage<Familia>("/api/familias?size=200&sort=descricao,asc"),
        fetchPage<TipoTaxaIva>("/api/tipos-taxa-iva?size=100&sort=descricao,asc")
      ]);
      setServices(artigosPage.content);
      setFamilias(familiasPage.content);
      setTiposIva(tiposIvaPage.content);
      setSelectedCodigo((current) => current ?? artigosPage.content[0]?.codigo ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar os artigos.");
    } finally {
      setLoading(false);
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    return services.filter((service) => {
      const matchesSearch = !term || [service.codigo, service.descricao, service.abreviatura, service.codigoIdentificacao]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term));
      const matchesState = stateFilter === "all"
        || (stateFilter === "active" && !service.inativo)
        || (stateFilter === "inactive" && service.inativo);
      return matchesSearch && matchesState;
    });
  }, [search, services, stateFilter]);

  const selected = services.find((service) => service.codigo === selectedCodigo) ?? null;
  const selectedIva = tiposIva.find((tipo) => tipo.id === selected?.ivaVendaId)?.descricao ?? selected?.ivaVendaId ?? "-";
  const hiddenDefaultSource = editorOpen && editorMode === "edit" ? selected : null;
  const defaults = useMemo(() => resolveHiddenDefaults(tiposIva, hiddenDefaultSource), [hiddenDefaultSource, tiposIva]);
  const activeCount = services.filter((service) => !service.inativo).length;

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
      familiaId: String(service.familiaId),
      unidade: service.unidade,
      pvp: String(service.pvp),
      ivaVendaId: service.ivaVendaId,
      inativo: service.inativo
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
      const page = await fetchPage<Artigo>("/api/artigos?size=200&sort=codigo,asc");
      setServices(page.content);
      setSelectedCodigo(form.codigo);
      setEditorOpen(false);
      setNotice(`Artigo ${form.codigo} ${editorMode === "edit" ? "atualizado" : "criado"}.`);
      showToast({ detail: `Artigo ${form.codigo} guardado.`, severity: "success", summary: "Guardado" });
      if (isMobile) setMobileScreen("detail");
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Nao foi possivel guardar o artigo.");
    } finally {
      setSaving(false);
    }
  }

  function selectService(codigo: string) {
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
      onSelect={selectService}
      onStateFilter={setStateFilter}
      saving={saving}
      search={search}
      selected={selected}
      selectedIva={selectedIva}
      services={services}
      stateFilter={stateFilter}
      tiposIva={tiposIva}
    />
  );

  return (
    <ResponsiveSlot
      desktop={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
      mobile={<MobileShell title="FAC Artigos">{content}</MobileShell>}
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
  onSelect: (codigo: string) => void;
  onStateFilter: (value: "all" | "active" | "inactive") => void;
  saving: boolean;
  search: string;
  selected: Artigo | null;
  selectedIva: string;
  services: Artigo[];
  stateFilter: "all" | "active" | "inactive";
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
      <ServicesHeader {...props} />
      <ServicesToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operacao concluida">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <section
        aria-label="Catalogo de artigos"
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
      {props.notice && <FacMessage tone="success" title="Operacao concluida">{props.notice}</FacMessage>}
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
  services
}: Parameters<typeof ServicesContent>[0] & { compact?: boolean }) {
  return (
    <header className={`fac-services-header ${compact ? "fac-services-header-compact" : ""}`}>
      <div>
        <p className="fac-eyebrow">Catalogo</p>
        <h1>Artigos</h1>
        {!compact && <p className="fac-muted">Gerir produtos e servicos utilizados nos documentos.</p>}
      </div>
      <div className="fac-services-header-actions">
        <div className="fac-services-summary" aria-label="Resumo de artigos">
          <span>{loading ? "A carregar" : `${services.length} artigos`}</span>
          <strong>{activeCount} ativos</strong>
        </div>
        {canManage && <FacButton icon="pi pi-plus" label={compact ? "Novo" : "Novo artigo"} onClick={onNew} variant="primary" />}
      </div>
    </header>
  );
}

function ServicesToolbar({
  deviceClass,
  onSearch,
  onStateFilter,
  search,
  stateFilter
}: Parameters<typeof ServicesContent>[0]) {
  const placeholder = deviceClass === "mobile" ? "Pesquisar artigos" : "Pesquisar por codigo, descricao ou identificacao";

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
    </section>
  );
}

function ServicesList({
  filtered,
  loading,
  onSelect,
  search,
  selected,
  services,
  stateFilter
}: Parameters<typeof ServicesContent>[0]) {
  if (loading) return <FacLoadingState description="A carregar artigos." />;
  if (services.length === 0) return <FacEmptyState description="Ainda nao existem artigos no catalogo." />;
  if (filtered.length === 0) return <FacEmptyState description="Sem resultados para a pesquisa e filtros atuais." />;

  return (
    <>
      <div className="fac-services-list-meta">
        <span>{filtered.length} resultados</span>
      </div>
      <div className="fac-services-table-wrap" data-filter={stateFilter} data-search={search ? "active" : "empty"}>
        <table className="fac-services-table">
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Descricao</th>
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
    </>
  );
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
        <div><dt>Codigo</dt><dd>{selected.codigo}</dd></div>
        <div><dt>Unidade</dt><dd>{selected.unidade}</dd></div>
        <div><dt>Preco sem IVA</dt><dd>{money(selected.pvp)} EUR</dd></div>
        <div><dt>Taxa de IVA</dt><dd>{selectedIva}</dd></div>
        <div><dt>Artigo ativo</dt><dd>{selected.inativo ? "Nao" : "Sim"}</dd></div>
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
  const ivaOptions = tiposIva.map((tipo) => ({
    label: `${tipo.descricao}${tipo.inativo && tipo.id !== form.ivaVendaId ? " (inativo)" : ""}`,
    value: tipo.id
  }));
  const familiaOptions = familias.map((familia) => ({
    label: familia.descricao,
    value: String(familia.id)
  }));

  return (
    <form className="fac-services-form" onSubmit={onSave}>
      {editorMessage && <FacMessage tone="error" title="Validacao">{editorMessage}</FacMessage>}
      <div className="fac-services-form-grid">
        <FacInputText
          disabled={editorMode === "edit"}
          label="Codigo"
          maxLength={50}
          onChange={(event) => onChangeForm({ ...form, codigo: normalizeCode(event.target.value) })}
          required
          value={form.codigo}
        />
        <FacInputText
          label="Descricao"
          maxLength={80}
          onChange={(event) => onChangeForm({ ...form, descricao: event.target.value })}
          required
          value={form.descricao}
        />
        <FacSelect
          label="Familia"
          onChange={(value) => onChangeForm({ ...form, familiaId: value ?? "" })}
          options={familiaOptions}
          value={form.familiaId}
        />
        <FacSelect
          label="Unidade"
          onChange={(value) => onChangeForm({ ...form, unidade: value ?? "" })}
          options={serviceUnitOptions}
          value={form.unidade}
        />
        <FacInputText
          label="Preco sem IVA"
          min="0"
          onChange={(event) => onChangeForm({ ...form, pvp: event.target.value })}
          required
          step="0.000001"
          type="number"
          value={form.pvp}
        />
        <FacSelect
          label="Taxa de IVA"
          onChange={(value) => onChangeForm({ ...form, ivaVendaId: value ?? "" })}
          options={ivaOptions}
          value={form.ivaVendaId}
        />
        <label className="fac-services-check">
          <input
            checked={!form.inativo}
            onChange={(event) => onChangeForm({ ...form, inativo: !event.target.checked })}
            type="checkbox"
          />
          <span>Artigo ativo</span>
        </label>
      </div>
      <div className="fac-services-form-footer">
        <FacButton label="Cancelar" onClick={onCloseEditor} type="button" variant="ghost" />
        <FacButton disabled={saving} icon="pi pi-save" label={saving ? "A guardar..." : "Guardar"} type="submit" variant="primary" />
      </div>
    </form>
  );
}

function CommercialSidebar({
  active,
  currentUser,
  onLogout
}: {
  active: "articles";
  currentUser: AuthSession;
  onLogout: () => void;
}) {
  return (
    <div className="fac-commercial-nav">
      <div className="fac-commercial-brand">
        <span>FAC</span>
        <div>
          <strong>FAC</strong>
          <small>Comercial</small>
        </div>
      </div>
      <nav aria-label="Navegacao comercial">
        <a className={active === "articles" ? "active" : ""} href="/artigos">
          <strong>Artigos</strong>
          <small>Catalogo</small>
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

function validate(form: ServiceForm, editing: boolean, defaults: HiddenDefaults) {
  if (!editing && !/^[A-Z0-9]{1,50}$/.test(form.codigo)) return "O codigo deve conter apenas letras maiusculas e numeros.";
  if (!form.descricao.trim()) return "A descricao e obrigatoria.";
  if (!form.familiaId) return "A familia e obrigatoria.";
  if (!form.unidade.trim()) return "A unidade e obrigatoria.";
  if (!form.ivaVendaId) return "A taxa de IVA e obrigatoria.";
  if (!defaults.ivaCompraId) return "O IVA de compra obrigatorio nao tem valor seguro nos catalogos atuais.";
  if (form.pvp === "" || Number(form.pvp) < 0) return "O preco deve ser igual ou superior a zero.";
  return null;
}

function toPayload(form: ServiceForm, defaults: HiddenDefaults, creating: boolean) {
  return {
    ...(creating ? { codigo: form.codigo } : {}),
    abreviatura: blankToNull(defaults.abreviatura),
    codigoIdentificacao: blankToNull(defaults.codigoIdentificacao),
    descricao: form.descricao.trim(),
    unidade: form.unidade.trim().toUpperCase(),
    familiaId: Number(form.familiaId),
    peso: Number(defaults.peso),
    ivaCompraId: defaults.ivaCompraId,
    ivaVendaId: form.ivaVendaId,
    pvp: Number(form.pvp),
    inativo: form.inativo,
    retencao: defaults.retencao,
    observacoes: blankToNull(defaults.observacoes)
  };
}

function resolveHiddenDefaults(tiposIva: TipoTaxaIva[], selected: Artigo | null): HiddenDefaults {
  const ivaCompra = selected?.ivaCompraId
    ? tiposIva.find((tipo) => tipo.id === selected.ivaCompraId) ?? firstActiveIva(tiposIva)
    : firstActiveIva(tiposIva);
  return {
    ivaCompraId: ivaCompra?.id ?? "",
    peso: String(selected?.peso ?? 0),
    retencao: selected?.retencao ?? false,
    abreviatura: selected?.abreviatura ?? "",
    codigoIdentificacao: selected?.codigoIdentificacao ?? "",
    observacoes: selected?.observacoes ?? ""
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

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6
  });
}
