import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { apiFetch, AuthSession } from "../../../api";
import {
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
  useDeviceClass,
  useFacToast
} from "../../fac";
import "./customers.css";

type Page<T> = {
  content: T[];
  totalElements: number;
};

type Cliente = {
  id: number;
  nome: string;
  morada?: string;
  morada1?: string;
  localidade?: string;
  nif: string;
  tel?: string;
  tm?: string;
  email?: string;
  email1?: string;
  tspiva?: string;
  iban?: string;
  retencao: boolean;
  inativo: boolean;
  observacoes?: string;
  codPostalId?: string;
  paisId?: string;
  moedaId?: string;
  mPagamentoId?: number;
  pPagamentoId?: string;
  rivaId?: string;
  transporteId?: number;
};

type CatalogoString = {
  id: string;
  nome: string;
};

type CatalogoNumero = {
  id: number;
  nome: string;
};

type ClienteCatalogos = {
  codPostais: CatalogoString[];
  paises: CatalogoString[];
  moedas: CatalogoString[];
  regimesIva: CatalogoString[];
  modosPagamento: CatalogoNumero[];
  prazosPagamento: CatalogoString[];
  transportes: CatalogoNumero[];
};

type ClienteForm = {
  nome: string;
  nif: string;
  email: string;
  email1: string;
  tel: string;
  tm: string;
  morada: string;
  morada1: string;
  codPostalId: string;
  localidade: string;
  paisId: string;
  moedaId: string;
  rivaId: string;
  mPagamentoId: string;
  pPagamentoId: string;
  transporteId: string;
  tspiva: string;
  iban: string;
  retencao: boolean;
  inativo: boolean;
  observacoes: string;
};

type EditorMode = "create" | "edit";
type MobileScreen = "list" | "detail" | "form";
type StateFilter = "all" | "active" | "inactive";

const emptyForm: ClienteForm = {
  nome: "",
  nif: "",
  email: "",
  email1: "",
  tel: "",
  tm: "",
  morada: "",
  morada1: "",
  codPostalId: "",
  localidade: "",
  paisId: "",
  moedaId: "",
  rivaId: "",
  mPagamentoId: "",
  pPagamentoId: "",
  transporteId: "",
  tspiva: "",
  iban: "",
  retencao: false,
  inativo: false,
  observacoes: ""
};

export default function CustomersView({ currentUser, onLogout }: { currentUser: AuthSession; onLogout: () => void }) {
  const canManage = currentUser.permissoes?.includes("MESTRES_GERIR") ?? false;
  const { deviceClass, isMobile } = useDeviceClass();
  const { showToast } = useFacToast();
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [catalogos, setCatalogos] = useState<ClienteCatalogos>({
    codPostais: [],
    paises: [],
    moedas: [],
    regimesIva: [],
    modosPagamento: [],
    prazosPagamento: [],
    transportes: []
  });
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [search, setSearch] = useState("");
  const [stateFilter, setStateFilter] = useState<StateFilter>("all");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editorMode, setEditorMode] = useState<EditorMode>("create");
  const [form, setForm] = useState<ClienteForm>(emptyForm);
  const [editorMessage, setEditorMessage] = useState<string | null>(null);
  const [mobileScreen, setMobileScreen] = useState<MobileScreen>("list");

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const [clientesPage, codPostaisPage, paisesPage, moedasPage, regimesIvaPage, modosPagamentoPage, prazosPagamentoPage, transportesPage] = await Promise.all([
        fetchPage<Cliente>("/api/clientes?size=300&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/codpostal?size=300&sort=id,asc"),
        fetchPage<CatalogoString>("/api/paises?size=300&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/moedas?size=100&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/riva?size=100&sort=nome,asc"),
        fetchPage<CatalogoNumero>("/api/mpagamentos?size=100&sort=nome,asc"),
        fetchPage<CatalogoString>("/api/p-pagamentos?size=100&sort=nome,asc"),
        fetchPage<CatalogoNumero>("/api/transportes?size=100&sort=nome,asc")
      ]);
      setClientes(clientesPage.content);
      setCatalogos({
        codPostais: codPostaisPage.content,
        paises: paisesPage.content,
        moedas: moedasPage.content,
        regimesIva: regimesIvaPage.content,
        modosPagamento: modosPagamentoPage.content,
        prazosPagamento: prazosPagamentoPage.content,
        transportes: transportesPage.content
      });
      setSelectedId((current) => current ?? clientesPage.content[0]?.id ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar os clientes.");
    } finally {
      setLoading(false);
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    return clientes.filter((cliente) => {
      const matchesSearch = !term || [
        String(cliente.id),
        cliente.nome,
        cliente.nif,
        cliente.email,
        cliente.email1,
        cliente.tel,
        cliente.tm,
        cliente.localidade
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term));
      const matchesState = stateFilter === "all"
        || (stateFilter === "active" && !cliente.inativo)
        || (stateFilter === "inactive" && cliente.inativo);
      return matchesSearch && matchesState;
    });
  }, [clientes, search, stateFilter]);

  const selected = clientes.find((cliente) => cliente.id === selectedId) ?? null;
  const activeCount = clientes.filter((cliente) => !cliente.inativo).length;

  function openNew() {
    if (!canManage) return;
    setEditorMode("create");
    setForm(emptyForm);
    setEditorMessage(null);
    setEditorOpen(true);
    setMobileScreen("form");
  }

  function openEdit(cliente: Cliente) {
    if (!canManage) return;
    setEditorMode("edit");
    setForm(clienteToForm(cliente));
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
    const validation = validate(form);
    if (validation) {
      setEditorMessage(validation);
      scrollCustomerFormTop();
      return;
    }

    setSaving(true);
    setEditorMessage(null);
    setError(null);
    try {
      const payload = toPayload(form);
      let nextSelectedId = selected?.id ?? selectedId;
      if (editorMode === "edit" && selected) {
        await request(`/api/clientes/${selected.id}`, "PUT", payload);
        nextSelectedId = selected.id;
      } else {
        const created = await requestJson<Cliente>("/api/clientes", "POST", payload);
        nextSelectedId = created.id;
      }
      const page = await fetchPage<Cliente>("/api/clientes?size=300&sort=nome,asc");
      setClientes(page.content);
      setSelectedId(nextSelectedId);
      setEditorOpen(false);
      setNotice(`Cliente ${form.nome.trim()} ${editorMode === "edit" ? "atualizado" : "criado"}.`);
      showToast({ detail: `Cliente ${form.nome.trim()} guardado.`, severity: "success", summary: "Guardado" });
      if (isMobile) setMobileScreen("detail");
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Nao foi possivel guardar o cliente.");
      scrollCustomerFormTop();
    } finally {
      setSaving(false);
    }
  }

  function selectCliente(id: number) {
    setSelectedId(id);
    if (isMobile) setMobileScreen("detail");
  }

  const sidebar = <CommercialSidebar active="customers" currentUser={currentUser} onLogout={onLogout} />;

  const content = (
    <CustomersContent
      activeCount={activeCount}
      canManage={canManage}
      catalogos={catalogos}
      clientes={clientes}
      deviceClass={deviceClass}
      editorMessage={editorMessage}
      editorMode={editorMode}
      editorOpen={editorOpen}
      error={error}
      filtered={filtered}
      form={form}
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
      onSelect={selectCliente}
      onStateFilter={setStateFilter}
      saving={saving}
      search={search}
      selected={selected}
      stateFilter={stateFilter}
    />
  );

  return (
    <ResponsiveSlot
      desktop={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
      mobile={<MobileShell title="FAC Clientes">{content}</MobileShell>}
      tablet={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
    />
  );
}

function CustomersContent(props: {
  activeCount: number;
  canManage: boolean;
  catalogos: ClienteCatalogos;
  clientes: Cliente[];
  deviceClass: string;
  editorMessage: string | null;
  editorMode: EditorMode;
  editorOpen: boolean;
  error: string | null;
  filtered: Cliente[];
  form: ClienteForm;
  loading: boolean;
  mobileScreen: MobileScreen;
  notice: string | null;
  onBackToList: () => void;
  onChangeForm: (form: ClienteForm) => void;
  onCloseEditor: () => void;
  onEdit: (cliente: Cliente) => void;
  onNew: () => void;
  onSave: (event?: FormEvent) => void;
  onSearch: (value: string) => void;
  onSelect: (id: number) => void;
  onStateFilter: (value: StateFilter) => void;
  saving: boolean;
  search: string;
  selected: Cliente | null;
  stateFilter: StateFilter;
}) {
  const isMobile = props.deviceClass === "mobile";

  if (isMobile) return <MobileCustomersContent {...props} />;

  return (
    <>
      <CustomersHeader {...props} />
      <CustomersToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operacao concluida">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <section aria-label="Catalogo de clientes" className="fac-customers-layout">
        <article className="fac-customers-panel fac-customers-list-panel">
          <CustomersList {...props} />
        </article>
        <aside className="fac-customers-panel fac-customers-detail-panel">
          <CustomerDetail {...props} />
        </aside>
      </section>
      <CustomerEditorDialog {...props} />
    </>
  );
}

function MobileCustomersContent(props: Parameters<typeof CustomersContent>[0]) {
  if (props.editorOpen || props.mobileScreen === "form") {
    return (
      <section className="fac-customers-mobile-page">
        <MobilePageHeader
          action={<FacButton icon="pi pi-times" label="Fechar" onClick={props.onCloseEditor} variant="ghost" />}
          eyebrow={props.editorMode === "edit" ? "Editar cliente" : "Novo cliente"}
          title={props.editorMode === "edit" && props.selected ? String(props.selected.id) : "Novo"}
        />
        <CustomerFormFields {...props} />
      </section>
    );
  }

  if (props.mobileScreen === "detail" && props.selected) {
    return (
      <section className="fac-customers-mobile-page">
        <MobilePageHeader
          action={<FacButton icon="pi pi-arrow-left" label="Lista" onClick={props.onBackToList} variant="ghost" />}
          eyebrow="Cliente"
          title={props.selected.nome}
        />
        <CustomerDetail {...props} />
      </section>
    );
  }

  return (
    <>
      <CustomersHeader {...props} compact />
      <CustomersToolbar {...props} />
      {props.notice && <FacMessage tone="success" title="Operacao concluida">{props.notice}</FacMessage>}
      {props.error && <FacMessage tone="error" title="Erro">{props.error}</FacMessage>}
      <CustomersList {...props} />
    </>
  );
}

function CustomersHeader({
  activeCount,
  canManage,
  clientes,
  compact = false,
  loading,
  onNew
}: Parameters<typeof CustomersContent>[0] & { compact?: boolean }) {
  return (
    <header className={`fac-customers-header ${compact ? "fac-customers-header-compact" : ""}`}>
      <div>
        <p className="fac-eyebrow">Clientes</p>
        <h1>Clientes</h1>
        {!compact && <p className="fac-muted">Gerir clientes utilizados nos documentos.</p>}
      </div>
      <div className="fac-customers-header-actions">
        <div className="fac-customers-summary" aria-label="Resumo de clientes">
          <span>{loading ? "A carregar" : `${clientes.length} clientes`}</span>
          <strong>{activeCount} ativos</strong>
        </div>
        {canManage && <FacButton icon="pi pi-plus" label={compact ? "Novo" : "Novo cliente"} onClick={onNew} variant="primary" />}
      </div>
    </header>
  );
}

function CustomersToolbar({ deviceClass, onSearch, onStateFilter, search, stateFilter }: Parameters<typeof CustomersContent>[0]) {
  const placeholder = deviceClass === "mobile" ? "Pesquisar clientes" : "Pesquisar por codigo, nome, NIF ou email";

  return (
    <section className="fac-customers-toolbar" aria-label="Pesquisa e filtros">
      <FacInputText
        aria-label="Pesquisar clientes"
        onChange={(event) => onSearch(event.target.value)}
        placeholder={placeholder}
        type="search"
        value={search}
      />
      <FacSelect
        onChange={(value) => onStateFilter((value as StateFilter) ?? "all")}
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

function CustomersList({ filtered, loading, onSelect, search, selected, clientes, stateFilter }: Parameters<typeof CustomersContent>[0]) {
  if (loading) return <FacLoadingState description="A carregar clientes." />;
  if (clientes.length === 0) return <FacEmptyState description="Ainda nao existem clientes." />;
  if (filtered.length === 0) return <FacEmptyState description="Sem resultados para a pesquisa e filtros atuais." />;

  return (
    <>
      <div className="fac-customers-list-meta">
        <span>{filtered.length} resultados</span>
      </div>
      <div className="fac-customers-table-wrap" data-filter={stateFilter} data-search={search ? "active" : "empty"}>
        <table className="fac-customers-table">
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Nome</th>
              <th>NIF</th>
              <th>Localidade</th>
              <th>Contacto</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((cliente) => (
              <tr
                aria-selected={cliente.id === selected?.id}
                className={cliente.id === selected?.id ? "is-selected" : ""}
                key={cliente.id}
                onClick={() => onSelect(cliente.id)}
                tabIndex={0}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") onSelect(cliente.id);
                }}
              >
                <td>{cliente.id}</td>
                <td>{cliente.nome}</td>
                <td>{cliente.nif}</td>
                <td>{cliente.localidade || "-"}</td>
                <td>{primaryContact(cliente)}</td>
                <td><StateBadge inactive={cliente.inativo} /></td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="fac-customers-mobile-list">
          {filtered.map((cliente) => (
            <button className="fac-customer-card" key={cliente.id} onClick={() => onSelect(cliente.id)} type="button">
              <strong>{cliente.nome}</strong>
              <span>{cliente.id} - NIF {cliente.nif}</span>
              <span>{cliente.localidade || cliente.email || primaryContact(cliente)}</span>
              <StateBadge inactive={cliente.inativo} />
            </button>
          ))}
        </div>
      </div>
    </>
  );
}

function CustomerDetail({ canManage, catalogos, onEdit, selected }: Parameters<typeof CustomersContent>[0]) {
  if (!selected) return <FacEmptyState description="Seleciona um cliente para ver o detalhe." />;

  return (
    <section className="fac-customers-detail">
      <div className="fac-customers-detail-title">
        <div>
          <p className="fac-eyebrow">Detalhe do cliente</p>
          <h2>{selected.nome}</h2>
        </div>
        <StateBadge inactive={selected.inativo} />
      </div>
      <dl>
        <div><dt>Codigo</dt><dd>{selected.id}</dd></div>
        <div><dt>NIF</dt><dd>{selected.nif || "-"}</dd></div>
        <div><dt>Morada</dt><dd>{addressLine(selected)}</dd></div>
        <div><dt>Codigo postal</dt><dd>{selected.codPostalId || "-"}</dd></div>
        <div><dt>Localidade</dt><dd>{selected.localidade || "-"}</dd></div>
        <div><dt>Pais</dt><dd>{catalogName(catalogos.paises, selected.paisId)}</dd></div>
        <div><dt>Email</dt><dd>{selected.email || "-"}</dd></div>
        <div><dt>Telefone</dt><dd>{selected.tel || selected.tm || "-"}</dd></div>
        <div><dt>Condicao</dt><dd>{catalogName(catalogos.prazosPagamento, selected.pPagamentoId)}</dd></div>
        <div><dt>Moeda</dt><dd>{catalogName(catalogos.moedas, selected.moedaId)}</dd></div>
        <div><dt>Cliente ativo</dt><dd>{selected.inativo ? "Nao" : "Sim"}</dd></div>
      </dl>
      {canManage
        ? <FacButton icon="pi pi-pencil" label="Editar cliente" onClick={() => onEdit(selected)} variant="primary" />
        : <FacMessage title="Consulta">Sem permissao para criar ou editar clientes.</FacMessage>}
    </section>
  );
}

function CustomerEditorDialog(props: Parameters<typeof CustomersContent>[0]) {
  const formId = "fac-customers-editor-form";

  return (
    <FacDialog
      className="fac-customers-dialog"
      footer={<CustomerFormFooter formId={formId} onCloseEditor={props.onCloseEditor} saving={props.saving} />}
      header={props.editorMode === "edit" ? "Editar cliente" : "Novo cliente"}
      onHide={props.onCloseEditor}
      visible={props.editorOpen}
    >
      <CustomerFormFields {...props} formId={formId} inlineFooter={false} />
    </FacDialog>
  );
}

function CustomerFormFields({ catalogos, editorMessage, editorMode, form, formId, inlineFooter = true, onChangeForm, onCloseEditor, onSave, saving, selected }: Parameters<typeof CustomersContent>[0] & { formId?: string; inlineFooter?: boolean }) {
  return (
    <form className="fac-customers-form" id={formId} onSubmit={onSave}>
      {editorMessage && <FacMessage tone="error" title="Validacao">{editorMessage}</FacMessage>}

      <FormSection title="Identificacao">
        {editorMode === "edit" && <FacInputText disabled label="Codigo" value={selected ? String(selected.id) : ""} />}
        <FacInputText label="Nome" maxLength={80} onChange={(event) => onChangeForm({ ...form, nome: event.target.value })} required value={form.nome} />
        <FacInputText label="NIF" maxLength={9} onChange={(event) => onChangeForm({ ...form, nif: event.target.value })} required value={form.nif} />
        <label className="fac-customers-check">
          <input checked={!form.inativo} onChange={(event) => onChangeForm({ ...form, inativo: !event.target.checked })} type="checkbox" />
          <span>Cliente ativo</span>
        </label>
      </FormSection>

      <FormSection title="Contactos">
        <FacInputText label="Email" maxLength={120} onChange={(event) => onChangeForm({ ...form, email: event.target.value })} required type="email" value={form.email} />
        <FacInputText label="Segundo email" maxLength={120} onChange={(event) => onChangeForm({ ...form, email1: event.target.value })} type="email" value={form.email1} />
        <FacInputText label="Telefone" maxLength={20} onChange={(event) => onChangeForm({ ...form, tel: event.target.value })} value={form.tel} />
        <FacInputText label="Telemovel" maxLength={20} onChange={(event) => onChangeForm({ ...form, tm: event.target.value })} value={form.tm} />
      </FormSection>

      <FormSection title="Morada">
        <FacInputText label="Morada" maxLength={60} onChange={(event) => onChangeForm({ ...form, morada: event.target.value })} required value={form.morada} />
        <FacInputText label="Morada complementar" maxLength={60} onChange={(event) => onChangeForm({ ...form, morada1: event.target.value })} value={form.morada1} />
        <FacSelect label="Codigo postal" onChange={(value) => onChangeForm({ ...form, codPostalId: value ?? "" })} options={catalogOptions(catalogos.codPostais)} value={form.codPostalId} />
        <FacInputText label="Localidade" maxLength={50} onChange={(event) => onChangeForm({ ...form, localidade: event.target.value })} value={form.localidade} />
        <FacSelect label="Pais" onChange={(value) => onChangeForm({ ...form, paisId: value ?? "" })} options={catalogOptions(catalogos.paises)} value={form.paisId} />
      </FormSection>

      <FormSection title="Condicoes comerciais">
        <FacSelect label="Moeda" onChange={(value) => onChangeForm({ ...form, moedaId: value ?? "" })} options={catalogOptions(catalogos.moedas)} value={form.moedaId} />
        <FacSelect label="Regime de IVA" onChange={(value) => onChangeForm({ ...form, rivaId: value ?? "" })} options={catalogOptions(catalogos.regimesIva)} value={form.rivaId} />
        <FacSelect label="Modo de pagamento" onChange={(value) => onChangeForm({ ...form, mPagamentoId: value ?? "" })} options={catalogOptions(catalogos.modosPagamento)} value={form.mPagamentoId} />
        <FacSelect label="Prazo de pagamento" onChange={(value) => onChangeForm({ ...form, pPagamentoId: value ?? "" })} options={catalogOptions(catalogos.prazosPagamento)} value={form.pPagamentoId} />
        <FacSelect label="Transporte" onChange={(value) => onChangeForm({ ...form, transporteId: value ?? "" })} options={catalogOptions(catalogos.transportes)} value={form.transporteId} />
        <label className="fac-customers-check">
          <input checked={form.retencao} onChange={(event) => onChangeForm({ ...form, retencao: event.target.checked })} type="checkbox" />
          <span>Sujeito a retencao</span>
        </label>
      </FormSection>

      <FormSection title="Notas">
        <FacInputText label="TSPIVA" maxLength={20} onChange={(event) => onChangeForm({ ...form, tspiva: event.target.value })} value={form.tspiva} />
        <FacInputText label="IBAN" maxLength={34} onChange={(event) => onChangeForm({ ...form, iban: event.target.value })} value={form.iban} />
        <label className="fac-customers-textarea">
          <span>Observacoes</span>
          <textarea maxLength={300} onChange={(event) => onChangeForm({ ...form, observacoes: event.target.value })} value={form.observacoes} />
        </label>
      </FormSection>

      {inlineFooter && <CustomerFormFooter onCloseEditor={onCloseEditor} saving={saving} />}
    </form>
  );
}

function CustomerFormFooter({ formId, onCloseEditor, saving }: { formId?: string; onCloseEditor: () => void; saving: boolean }) {
  return (
    <div className="fac-customers-form-footer">
      <FacButton label="Cancelar" onClick={onCloseEditor} type="button" variant="ghost" />
      <FacButton disabled={saving} form={formId} icon="pi pi-save" label={saving ? "A guardar..." : "Guardar"} type="submit" variant="primary" />
    </div>
  );
}

function scrollCustomerFormTop() {
  window.setTimeout(() => {
    document.querySelector<HTMLElement>(".fac-customers-dialog [data-pc-section='content'], .fac-customers-dialog .p-dialog-content")?.scrollTo({ top: 0, behavior: "smooth" });
    document.querySelector<HTMLElement>(".fac-customers-mobile-page")?.scrollIntoView({ block: "start" });
  }, 0);
}

function FormSection({ children, title }: { children: ReactNode; title: string }) {
  return (
    <fieldset className="fac-customers-form-section">
      <legend>{title}</legend>
      <div className="fac-customers-form-grid">{children}</div>
    </fieldset>
  );
}

function CommercialSidebar({ active, currentUser, onLogout }: { active: "articles" | "customers"; currentUser: AuthSession; onLogout: () => void }) {
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
        <small>{currentUser.papel} - {currentUser.codigo}</small>
        <FacButton label="Sair" onClick={onLogout} variant="text" />
      </div>
    </div>
  );
}

function MobilePageHeader({ action, eyebrow, title }: { action: ReactNode; eyebrow: string; title: string }) {
  return (
    <header className="fac-customers-mobile-header">
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

async function request(url: string, method: "PUT", body: unknown) {
  const response = await apiFetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
}

async function requestJson<T>(url: string, method: "POST", body: unknown): Promise<T> {
  const response = await apiFetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function responseError(response: Response) {
  try {
    const payload = await response.json();
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}

function validate(form: ClienteForm) {
  if (!form.nome.trim()) return "O nome e obrigatorio.";
  if (form.nif.trim().length !== 9) return "O NIF deve ter 9 caracteres.";
  if (!form.email.trim() || !/^\S+@\S+\.\S+$/.test(form.email.trim())) return "Indica um email valido.";
  if (form.email1.trim() && !/^\S+@\S+\.\S+$/.test(form.email1.trim())) return "Indica um segundo email valido.";
  if (!form.morada.trim()) return "A morada e obrigatoria.";
  if (!form.codPostalId.trim()) return "O codigo postal e obrigatorio.";
  if (!form.paisId) return "O pais e obrigatorio.";
  if (!form.moedaId) return "A moeda e obrigatoria.";
  if (!form.transporteId) return "O transporte e obrigatorio.";
  return null;
}

function clienteToForm(cliente: Cliente): ClienteForm {
  return {
    nome: cliente.nome ?? "",
    nif: cliente.nif ?? "",
    email: cliente.email ?? "",
    email1: cliente.email1 ?? "",
    tel: cliente.tel ?? "",
    tm: cliente.tm ?? "",
    morada: cliente.morada ?? "",
    morada1: cliente.morada1 ?? "",
    codPostalId: cliente.codPostalId ?? "",
    localidade: cliente.localidade ?? "",
    paisId: cliente.paisId ?? "",
    moedaId: cliente.moedaId ?? "",
    rivaId: cliente.rivaId ?? "",
    mPagamentoId: cliente.mPagamentoId != null ? String(cliente.mPagamentoId) : "",
    pPagamentoId: cliente.pPagamentoId ?? "",
    transporteId: cliente.transporteId != null ? String(cliente.transporteId) : "",
    tspiva: cliente.tspiva ?? "",
    iban: cliente.iban ?? "",
    retencao: cliente.retencao ?? false,
    inativo: cliente.inativo ?? false,
    observacoes: cliente.observacoes ?? ""
  };
}

function toPayload(form: ClienteForm) {
  return {
    nome: form.nome.trim(),
    morada: form.morada.trim(),
    morada1: blankToNull(form.morada1),
    localidade: blankToNull(form.localidade),
    nif: form.nif.trim(),
    tel: blankToNull(form.tel),
    tm: blankToNull(form.tm),
    email: form.email.trim(),
    email1: blankToNull(form.email1),
    tspiva: blankToNull(form.tspiva),
    iban: blankToNull(form.iban),
    retencao: form.retencao,
    inativo: form.inativo,
    observacoes: blankToNull(form.observacoes),
    codPostalId: form.codPostalId.trim(),
    paisId: form.paisId,
    moedaId: form.moedaId,
    mPagamentoId: numberOrNull(form.mPagamentoId),
    pPagamentoId: blankToNull(form.pPagamentoId),
    rivaId: blankToNull(form.rivaId),
    transporteId: numberOrNull(form.transporteId)
  };
}

function catalogOptions<T extends { id: string | number; nome: string }>(items: T[]) {
  return items.map((item) => ({ label: `${item.id} - ${item.nome}`, value: String(item.id) }));
}

function catalogName<T extends { id: string | number; nome: string }>(items: T[], value?: string | number | null) {
  if (value == null || value === "") return "-";
  const item = items.find((candidate) => String(candidate.id) === String(value));
  return item ? item.nome : String(value);
}

function primaryContact(cliente: Cliente) {
  return cliente.email || cliente.tel || cliente.tm || "-";
}

function addressLine(cliente: Cliente) {
  return [cliente.morada, cliente.morada1].filter(Boolean).join(", ") || "-";
}

function blankToNull(value: string) {
  const trimmed = value.trim();
  return trimmed || null;
}

function numberOrNull(value: string) {
  return value ? Number(value) : null;
}
