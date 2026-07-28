import { ReactNode, useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import ArtigosView from "./ArtigosView";
import DocumentosView from "./DocumentosView";
import PendentesView from "./PendentesView";
import ParametrosDocumentoView from "./ParametrosDocumentoView";
import TabelasView from "./TabelasView";
import ListagensView from "./ListagensView";
import AuditoriaView from "./AuditoriaView";
import EmpresaAdminView from "./EmpresaAdminView";
import AdminUtilizadoresView from "./AdminUtilizadoresView";
import ImportExportView from "./ImportExportView";
import { GlobalSearch } from "./GlobalSearch";
import { apiFetch, AuthSession } from "./api";

type Page<T> = {
  content: T[];
  totalElements: number;
};

type ViewKey = "Dashboard" | "Clientes" | "Documentos" | "Artigos" | "Tesouraria" | "Listagens" | "ImportExport" | "Auditoria" | "Configuracao";

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
  mPagamentoId?: string;
  pPagamentoId?: string;
  rivaId?: string;
  transporteId?: string;
};

type CatalogoString = {
  id: string;
  nome: string;
};

type CatalogoNumero = {
  id: number;
  nome: string;
};

type ParametrosCliente = {
  id: number;
  paisId?: string;
  moedaId?: string;
  rivaId?: string;
  mPagamentoId?: string;
  pPagamentoId?: string;
  transporteId?: string;
  retencao?: boolean;
};

type ParametrosClienteForm = {
  paisId: string;
  moedaId: string;
  rivaId: string;
  mPagamentoId: string;
  pPagamentoId: string;
  transporteId: string;
  retencao: "" | "true" | "false";
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

type ClienteCatalogos = {
  paises: CatalogoString[];
  moedas: CatalogoString[];
  regimesIva: CatalogoString[];
  modosPagamento: CatalogoString[];
  prazosPagamento: CatalogoString[];
  transportes: CatalogoString[];
};

type ClienteColumnKey = "id" | "nome" | "nif" | "email" | "tel" | "localidade" | "paisId" | "moedaId" | "rivaId" | "estado";

type ClienteColumn = {
  key: ClienteColumnKey;
  label: string;
  visible: boolean;
};

const CLIENT_COLUMNS_STORAGE = "fac.clientes.colunas";
const DEFAULT_CLIENT_COLUMNS: ClienteColumn[] = [
  { key: "id", label: "Código", visible: true },
  { key: "nome", label: "Nome", visible: true },
  { key: "nif", label: "NIF", visible: true },
  { key: "email", label: "Email", visible: true },
  { key: "tel", label: "Telefone", visible: false },
  { key: "localidade", label: "Localidade", visible: false },
  { key: "paisId", label: "País", visible: false },
  { key: "moedaId", label: "Moeda", visible: false },
  { key: "rivaId", label: "Regime IVA", visible: false },
  { key: "estado", label: "Estado", visible: true }
];

type DocumentoComercial = {
  id: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento: number | null;
  estado: string;
  clienteNome?: string;
  clienteId: number;
  dataEmissao: string;
  valorTotal: number;
  moedaId: string;
};

type Pendente = {
  id: number;
  documentoComercialId: number;
  tipoDocumentoId: string;
  serieDocumento: string;
  numeroDocumento: number;
  clienteId: number;
  dataDocumento: string;
  dataVencimento: string;
  valorDocumento: number;
  valorPendente: number;
  moedaId: string;
};

type DocumentoFinanceiro = {
  id: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento: number;
  dataEmissao: string;
  valorPagamentoLiquido: number;
  moedaId: string;
  anulado: boolean;
};

type ContaCorrenteResumo = {
  moedaId: string;
  documentos: number;
  vencidos: number;
  valorDocumento: number;
  valorRecebidoAtivo: number;
  valorRecebidoAnulado: number;
  valorPendente: number;
};

type ContaCorrenteDocumento = {
  pendenteId: number;
  documentoComercialId: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento: number;
  dataDocumento: string;
  dataVencimento: string;
  estado: string;
  moedaId: string;
  valorDocumento: number;
  valorRecebidoAtivo: number;
  valorRecebidoAnulado: number;
  valorPendente: number;
};

type ContaCorrenteDiagnostico = {
  clienteId: number;
  clienteNome: string;
  totais: ContaCorrenteResumo[];
  documentos: ContaCorrenteDocumento[];
  alertas: string[];
};

type DashboardData = {
  comerciais: Page<DocumentoComercial>;
  pendentes: Page<Pendente>;
  financeiros: Page<DocumentoFinanceiro>;
};

type MenuItem = { label: ViewKey; hint: string };
type MenuGroup = { title: string; items: MenuItem[] };

const menuGroups: MenuGroup[] = [
  { title: "Visão geral", items: [{ label: "Dashboard", hint: "Visão geral" }] },
  { title: "Vendas", items: [{ label: "Documentos", hint: "Faturação" }] },
  {
    title: "Dados comerciais",
    items: [
      { label: "Clientes", hint: "Conta corrente" },
      { label: "Artigos", hint: "Catálogo" }
    ]
  },
  { title: "Tesouraria", items: [{ label: "Tesouraria", hint: "Recebimentos" }] },
  { title: "Análise", items: [{ label: "Listagens", hint: "Consulta e análise" }] }
];

const adminMenuItems: MenuItem[] = [
  { label: "Configuracao", hint: "Base FAC" },
  { label: "Auditoria", hint: "Rastreabilidade" },
  { label: "ImportExport", hint: "Dados mestres" }
];

const navigationItems: MenuItem[] = [...menuGroups.flatMap((group) => group.items), ...adminMenuItems];

function menuLabel(label: ViewKey) {
  if (label === "Dashboard") return "Visão geral";
  if (label === "Tesouraria") return "Recebimentos";
  if (label === "Configuracao") return "Configuração";
  if (label === "ImportExport") return "Importar/Exportar";
  return label;
}

const emptyClienteForm: ClienteForm = {
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

const emptyParametrosClienteForm: ParametrosClienteForm = {
  paisId: "",
  moedaId: "",
  rivaId: "",
  mPagamentoId: "",
  pPagamentoId: "",
  transporteId: "",
  retencao: ""
};

type AppProps = {
  currentUser: AuthSession;
  embeddedContent?: ReactNode;
  initialView?: ViewKey;
  onLogout: () => void;
};

function App({ currentUser, embeddedContent, initialView = "Dashboard", onLogout }: AppProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const adminAccessRef = useRef<HTMLDivElement>(null);
  const navigationView = viewFromNavigationState(location.state);
  const [activeView, setActiveView] = useState<ViewKey>(navigationView ?? initialView);
  const [adminMenuOpen, setAdminMenuOpen] = useState(false);
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [clientes, setClientes] = useState<Page<Cliente> | null>(null);
  const [selectedClienteId, setSelectedClienteId] = useState<number | null>(null);
  const [contaCorrente, setContaCorrente] = useState<ContaCorrenteDiagnostico | null>(null);
  const [loading, setLoading] = useState(true);
  const [clientesLoading, setClientesLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [clienteSearch, setClienteSearch] = useState("");
  const [clienteEditorOpen, setClienteEditorOpen] = useState(false);
  const [editingClienteId, setEditingClienteId] = useState<number | null>(null);
  const [clienteForm, setClienteForm] = useState<ClienteForm>(emptyClienteForm);
  const [clienteCatalogos, setClienteCatalogos] = useState<ClienteCatalogos | null>(null);
  const [editorMessage, setEditorMessage] = useState<string | null>(null);
  const [clienteNotice, setClienteNotice] = useState<string | null>(null);
  const [parametrosClienteForm, setParametrosClienteForm] = useState<ParametrosClienteForm>(emptyParametrosClienteForm);
  const [parametrosClienteExists, setParametrosClienteExists] = useState(false);
  const [configLoading, setConfigLoading] = useState(false);
  const [configMessage, setConfigMessage] = useState<string | null>(null);

  async function loadDashboard() {
    setLoading(true);
    setError(null);
    try {
      const [comerciais, pendentes, financeiros] = await Promise.all([
        fetchPage<DocumentoComercial>("/api/documentos-comerciais?size=100&sort=id,desc"),
        fetchPage<Pendente>("/api/pendentes?size=100&sort=id,desc"),
        fetchPage<DocumentoFinanceiro>("/api/documentos-financeiros?size=100&sort=id,desc")
      ]);
      setDashboardData({ comerciais, pendentes, financeiros });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar dados.");
    } finally {
      setLoading(false);
    }
  }

  async function loadClientes() {
    setClientesLoading(true);
    setError(null);
    try {
      const page = await fetchPage<Cliente>("/api/clientes?size=100&sort=nome,asc");
      setClientes(page);
      const firstClienteId = page.content[0]?.id ?? null;
      setSelectedClienteId((current) => current ?? firstClienteId);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar clientes.");
    } finally {
      setClientesLoading(false);
    }
  }

  async function loadContaCorrente(clienteId: number) {
    setClientesLoading(true);
    setError(null);
    try {
      const diagnostico = await fetchJson<ContaCorrenteDiagnostico>(`/api/pendentes/conta-corrente/clientes/${clienteId}/diagnostico`);
      setContaCorrente(diagnostico);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar a conta corrente.");
      setContaCorrente(null);
    } finally {
      setClientesLoading(false);
    }
  }

  async function loadClienteCatalogos() {
    if (clienteCatalogos) return;
    const [paises, moedas, regimesIva, modosPagamento, prazosPagamento, transportes] = await Promise.all([
      fetchPage<CatalogoString>("/api/paises?size=300&sort=nome,asc"),
      fetchPage<CatalogoString>("/api/moedas?size=100&sort=nome,asc"),
      fetchPage<CatalogoString>("/api/riva?size=100&sort=nome,asc"),
      fetchPage<CatalogoString>("/api/mpagamentos?size=100&sort=nome,asc"),
      fetchPage<CatalogoString>("/api/p-pagamentos?size=100&sort=nome,asc"),
      fetchPage<CatalogoString>("/api/transportes?size=100&sort=nome,asc")
    ]);
    setClienteCatalogos({
      paises: paises.content,
      moedas: moedas.content,
      regimesIva: regimesIva.content,
      modosPagamento: modosPagamento.content,
      prazosPagamento: prazosPagamento.content,
      transportes: transportes.content
    });
  }

  async function openClienteEditor() {
    setClienteEditorOpen(true);
    setEditingClienteId(null);
    setClienteForm(emptyClienteForm);
    setEditorMessage(null);
    setClienteNotice(null);
    setClientesLoading(true);
    try {
      await loadClienteCatalogos();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar os catálogos de cliente.");
    } finally {
      setClientesLoading(false);
    }
  }

  async function openClienteEditEditor(clienteId: number) {
    setClientesLoading(true);
    setEditorMessage(null);
    setClienteNotice(null);
    try {
      const [cliente] = await Promise.all([
        fetchJson<Cliente>(`/api/clientes/${clienteId}`),
        loadClienteCatalogos()
      ]);
      setEditingClienteId(cliente.id);
      setClienteForm(clienteToForm(cliente));
      setClienteEditorOpen(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível abrir o cliente para edição.");
    } finally {
      setClientesLoading(false);
    }
  }

  async function applyMatrizZero() {
    setClientesLoading(true);
    setEditorMessage(null);
    try {
      const matriz = await fetchJson<ParametrosCliente>("/api/parametros-cliente");
      setClienteForm((current) => ({
        ...current,
        paisId: matriz.paisId ?? current.paisId,
        moedaId: matriz.moedaId ?? current.moedaId,
        rivaId: matriz.rivaId ?? current.rivaId,
        mPagamentoId: matriz.mPagamentoId != null ? String(matriz.mPagamentoId) : current.mPagamentoId,
        pPagamentoId: matriz.pPagamentoId ?? current.pPagamentoId,
        transporteId: matriz.transporteId != null ? String(matriz.transporteId) : current.transporteId,
        retencao: matriz.retencao ?? current.retencao
      }));
      setEditorMessage("Matriz 0 aplicada aos campos configurados.");
    } catch (err) {
      setEditorMessage(err instanceof Error && err.message.includes("404")
        ? "A Matriz 0 ainda não foi configurada."
        : "Não foi possível aplicar a Matriz 0.");
    } finally {
      setClientesLoading(false);
    }
  }

  async function createCliente() {
    const validationMessage = validateClienteForm(clienteForm);
    if (validationMessage) {
      setEditorMessage(validationMessage);
      return;
    }

    setClientesLoading(true);
    setEditorMessage(null);
    try {
      const created = await sendJson<Cliente>("/api/clientes", clientePayload(clienteForm));

      const page = await fetchPage<Cliente>("/api/clientes?size=100&sort=nome,asc");
      setClientes(page);
      setSelectedClienteId(created.id);
      setClienteEditorOpen(false);
      setClienteForm(emptyClienteForm);
      setClienteNotice(`Cliente ${created.nome} criado com o código ${created.id}.`);
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Não foi possível criar o cliente.");
    } finally {
      setClientesLoading(false);
    }
  }

  async function updateCliente() {
    if (!editingClienteId) return;
    const validationMessage = validateClienteForm(clienteForm);
    if (validationMessage) {
      setEditorMessage(validationMessage);
      return;
    }

    setClientesLoading(true);
    setEditorMessage(null);
    try {
      await putJson(`/api/clientes/${editingClienteId}`, clientePayload(clienteForm));
      const page = await fetchPage<Cliente>("/api/clientes?size=100&sort=nome,asc");
      setClientes(page);
      setSelectedClienteId(editingClienteId);
      setClienteEditorOpen(false);
      setEditingClienteId(null);
      setClienteNotice(`Cliente ${clienteForm.nome.trim()} atualizado.`);
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Não foi possível atualizar o cliente.");
    } finally {
      setClientesLoading(false);
    }
  }

  async function loadParametrosCliente() {
    setConfigLoading(true);
    setConfigMessage(null);
    try {
      await loadClienteCatalogos();
      const parametros = await fetchOptionalJson<ParametrosCliente>("/api/parametros-cliente");
      setParametrosClienteExists(parametros != null);
      setParametrosClienteForm(parametros ? parametrosToForm(parametros) : emptyParametrosClienteForm);
    } catch (err) {
      setConfigMessage(err instanceof Error ? err.message : "Não foi possível carregar a Matriz 0.");
    } finally {
      setConfigLoading(false);
    }
  }

  async function saveParametrosCliente() {
    setConfigLoading(true);
    setConfigMessage(null);
    try {
      const payload = parametrosClientePayload(parametrosClienteForm);
      if (parametrosClienteExists) {
        await putJson("/api/parametros-cliente", payload);
      } else {
        await sendJson<ParametrosCliente>("/api/parametros-cliente", payload);
        setParametrosClienteExists(true);
      }
      setConfigMessage("Matriz 0 guardada. Será aplicada apenas quando pedida no novo cliente.");
    } catch (err) {
      setConfigMessage(err instanceof Error ? err.message : "Não foi possível guardar a Matriz 0.");
    } finally {
      setConfigLoading(false);
    }
  }

  const shellView = embeddedContent ? initialView : activeView;

  async function refreshActiveView() {
    if (shellView === "Clientes") {
      await loadClientes();
      if (selectedClienteId) {
        await loadContaCorrente(selectedClienteId);
      }
      return;
    }
    if (shellView === "Configuracao") {
      await loadParametrosCliente();
      return;
    }
    await loadDashboard();
  }

  useEffect(() => {
    loadDashboard();
    loadClientes();
  }, []);

  useEffect(() => {
    if (!embeddedContent) {
      setActiveView(navigationView ?? initialView);
    }
  }, [embeddedContent, initialView, navigationView]);

  useEffect(() => {
    if (selectedClienteId) {
      loadContaCorrente(selectedClienteId);
    }
  }, [selectedClienteId]);

  useEffect(() => {
    if (shellView !== "Clientes" || !clientes?.content.length) return;
    const clienteId = Number(new URLSearchParams(location.search).get("cliente"));
    if (!Number.isFinite(clienteId)) return;
    if (clientes.content.some((cliente) => cliente.id === clienteId)) {
      setSelectedClienteId(clienteId);
    }
  }, [clientes, location.search, shellView]);

  useEffect(() => {
    if (shellView === "Configuracao") {
      loadParametrosCliente();
    }
  }, [shellView]);

  useEffect(() => {
    setAdminMenuOpen(false);
  }, [shellView]);

  useEffect(() => {
    if (!adminMenuOpen) return;

    function closeOnOutsideClick(event: MouseEvent) {
      if (!adminAccessRef.current?.contains(event.target as Node)) {
        setAdminMenuOpen(false);
      }
    }

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setAdminMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", closeOnOutsideClick);
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.removeEventListener("mousedown", closeOnOutsideClick);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, [adminMenuOpen]);

  function selectView(view: ViewKey) {
    setActiveView(view);
    if (view === "Dashboard") {
      navigate("/");
    } else if (view === "Documentos") {
      navigate("/documentos");
    } else {
      navigate("/", { state: { activeView: view } });
    }
  }

  function selectAdminView(view: ViewKey) {
    selectView(view);
    setAdminMenuOpen(false);
  }

  const saldoPendente = useMemo(
    () => dashboardData?.pendentes.content.reduce((total, pendente) => total + Number(pendente.valorPendente || 0), 0) ?? 0,
    [dashboardData]
  );

  const documentosVencidos = useMemo(
    () => dashboardData?.pendentes.content.filter((pendente) => pendente.valorPendente > 0 && pendente.dataVencimento < todayIso()).length ?? 0,
    [dashboardData]
  );

  const recebidoAtivo = useMemo(
    () => dashboardData?.financeiros.content
      .filter((documento) => !documento.anulado)
      .reduce((total, documento) => total + Number(documento.valorPagamentoLiquido || 0), 0) ?? 0,
    [dashboardData]
  );

  const filteredClientes = useMemo(() => {
    const term = clienteSearch.trim().toLowerCase();
    if (!term) {
      return clientes?.content ?? [];
    }
    return (clientes?.content ?? []).filter((cliente) =>
      [cliente.nome, cliente.nif, String(cliente.id), cliente.email]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(term))
    );
  }, [clientes, clienteSearch]);

  const selectedCliente = clientes?.content.find((cliente) => cliente.id === selectedClienteId) ?? null;
  const contaResumo = contaCorrente?.totais[0] ?? null;
  const visibleMenuGroups = menuGroups
    .map((group) => ({ ...group, items: group.items.filter(canShowMenuItem) }))
    .filter((group) => group.items.length > 0);
  const visibleAdminMenuItems = adminMenuItems.filter(canShowMenuItem);
  const isAdminView = visibleAdminMenuItems.some((item) => item.label === shellView);

  function canShowMenuItem(item: MenuItem) {
    if (item.label === "Auditoria") return currentUser.permissoes?.includes("AUDITORIA_CONSULTAR");
    if (item.label === "Configuracao") return currentUser.permissoes?.includes("CONFIGURACAO_GERIR");
    if (item.label === "ImportExport") return currentUser.permissoes?.includes("DADOS_MESTRES_IMPORTAR") || currentUser.permissoes?.includes("DADOS_MESTRES_EXPORTAR");
    return true;
  }

  const metrics = [
    { label: "Saldo pendente", value: `${money(saldoPendente)} EUR`, tone: "client" },
    { label: "Documentos vencidos", value: String(documentosVencidos), tone: "document" },
    { label: "Recebido ativo", value: `${money(recebidoAtivo)} EUR`, tone: "treasury" },
    { label: "Documentos comerciais", value: String(dashboardData?.comerciais.totalElements ?? 0), tone: "product" }
  ];

  return (
    <main className="fac-shell">
      <aside className="fac-sidebar">
        <div className="fac-brand">
          <div className="fac-brand-mark">FAC</div>
          <div>
            <strong>FAC</strong>
            <span>{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "Ambiente de demonstração" : "Gestão comercial e faturação"}</span>
          </div>
        </div>

        <nav className="fac-menu" aria-label="Navegação principal">
          {visibleMenuGroups.map((group) => (
            <section className="fac-menu-section" key={group.title}>
              <p>{group.title}</p>
              {group.items.map((item) => (
                <button
                  className={shellView === item.label ? "active" : ""}
                  key={item.label}
                  onClick={() => selectView(item.label)}
                  type="button"
                >
                  <span>{menuLabel(item.label)}</span>
                  <small>{item.hint}</small>
                </button>
              ))}
            </section>
          ))}
        </nav>
      </aside>

      <section className="fac-workspace">
        <header className="fac-topbar">
          <div>
            <p className="fac-eyebrow">{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "FAC · Ambiente de demonstração" : "FAC · Gestão comercial e faturação"}</p>
            <h1>{viewTitle(shellView)}</h1>
          </div>
          <div className="fac-topbar-actions">
            {visibleAdminMenuItems.length > 0 && (
              <div className="fac-admin-access" ref={adminAccessRef}>
                <button
                  aria-expanded={adminMenuOpen}
                  aria-haspopup="menu"
                  aria-label="Administração e configuração"
                  className={`fac-admin-trigger${isAdminView ? " active" : ""}`}
                  onClick={() => setAdminMenuOpen((open) => !open)}
                  title="Administração e configuração"
                  type="button"
                >
                  <i aria-hidden="true" className="pi pi-shield" />
                </button>
                {adminMenuOpen && (
                  <div className="fac-admin-menu" role="menu">
                    <p>Administração</p>
                    {visibleAdminMenuItems.map((item) => (
                      <button
                        className={shellView === item.label ? "active" : ""}
                        key={item.label}
                        onClick={() => selectAdminView(item.label)}
                        role="menuitem"
                        type="button"
                      >
                        <span>{menuLabel(item.label)}</span>
                        <small>{item.hint}</small>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
            <div className="fac-current-user"><span>{currentUser.nome}</span><small>{currentUser.papel} · {currentUser.codigo}</small></div>
            <GlobalSearch />
            <button className="fac-ghost-button" onClick={onLogout} type="button">Sair</button>
          </div>
        </header>

        {embeddedContent ? embeddedContent : shellView === "Clientes" ? (
          <ClientesView
            catalogos={clienteCatalogos}
            clientes={filteredClientes}
            notice={clienteNotice}
            editorMessage={editorMessage}
            form={clienteForm}
            editorOpen={clienteEditorOpen}
            editingClienteId={editingClienteId}
            contaCorrente={contaCorrente}
            contaResumo={contaResumo}
            loading={clientesLoading}
            canManage={currentUser.permissoes?.includes("MESTRES_GERIR") ?? false}
            selectedCliente={selectedCliente}
            selectedClienteId={selectedClienteId}
            onApplyMatrizZero={applyMatrizZero}
            onChangeForm={setClienteForm}
            onCloseEditor={() => { setClienteEditorOpen(false); setEditingClienteId(null); }}
            onEditCliente={openClienteEditEditor}
            onOpenEditor={openClienteEditor}
            onSaveCliente={editingClienteId ? updateCliente : createCliente}
            onSelectCliente={setSelectedClienteId}
          />
        ) : shellView === "Documentos" ? (
          <DocumentosView />
        ) : shellView === "Artigos" ? (
          <ArtigosView />
        ) : shellView === "Tesouraria" ? (
          <PendentesView />
        ) : shellView === "Listagens" ? (
          <ListagensView />
        ) : shellView === "ImportExport" ? (
          <ImportExportView />
        ) : shellView === "Auditoria" ? (
          <AuditoriaView />
        ) : shellView === "Configuracao" ? (
          <ConfiguracaoView
            catalogos={clienteCatalogos}
            exists={parametrosClienteExists}
            form={parametrosClienteForm}
            loading={configLoading}
            message={configMessage}
            onChangeForm={setParametrosClienteForm}
            onSave={saveParametrosCliente}
          />
        ) : (
          <DashboardView
            error={error}
            loading={loading}
            metrics={metrics}
            onNavigate={selectView}
          />
        )}

        {error && <p className="fac-message">{error}</p>}
      </section>
    </main>
  );
}

type DashboardViewProps = {
  error: string | null;
  loading: boolean;
  metrics: { label: string; value: string; tone: string }[];
  onNavigate: (view: ViewKey) => void;
};

function DashboardView({
  error,
  loading,
  metrics,
  onNavigate
}: DashboardViewProps) {
  return (
    <>
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "Alentejo Sabores, Lda. · Demonstração" : "Ambiente de trabalho"}</p>
          <h2>Visão geral da atividade</h2>
          <p>
            Acompanha documentos, clientes e recebimentos num único espaço de trabalho.
          </p>
        </div>
        <div className="fac-hero-card">
          <span>Estado do sistema</span>
          <strong>{loading ? "A carregar..." : error ? "Com erro" : "Operacional"}</strong>
          <small>{error ?? "Serviços disponíveis"}</small>
        </div>
      </section>

      <section className="fac-metrics" aria-label="Indicadores">
        {metrics.map((metric) => (
          <article className={`fac-metric ${metric.tone}`} key={metric.label}>
            <span>{metric.label}</span>
            <strong>{loading ? "-" : metric.value}</strong>
          </article>
        ))}
      </section>

      <section className="fac-panel fac-dashboard-actions">
        <div className="fac-panel-header">
          <div>
            <p className="fac-eyebrow">Operação diária</p>
            <h2>Continuar o trabalho</h2>
          </div>
          <span className="fac-muted">Consulta a informação detalhada na área Listagens.</span>
        </div>

        <div className="fac-dashboard-action-grid">
          <button onClick={() => onNavigate("Documentos")} type="button">
            <strong>Documentos</strong>
            <span>Criar e acompanhar faturação</span>
          </button>
          <button onClick={() => onNavigate("Tesouraria")} type="button">
            <strong>Recebimentos</strong>
            <span>Receber e consultar pendentes</span>
          </button>
          <button onClick={() => onNavigate("Listagens")} type="button">
            <strong>Listagens</strong>
            <span>Analisar documentos, linhas e recebimentos</span>
          </button>
        </div>
      </section>
    </>
  );
}

type ClientesViewProps = {
  catalogos: ClienteCatalogos | null;
  clientes: Cliente[];
  notice: string | null;
  editorMessage: string | null;
  form: ClienteForm;
  editorOpen: boolean;
  editingClienteId: number | null;
  contaCorrente: ContaCorrenteDiagnostico | null;
  contaResumo: ContaCorrenteResumo | null;
  loading: boolean;
  canManage: boolean;
  selectedCliente: Cliente | null;
  selectedClienteId: number | null;
  onApplyMatrizZero: () => void;
  onChangeForm: (form: ClienteForm) => void;
  onCloseEditor: () => void;
  onEditCliente: (clienteId: number) => void;
  onOpenEditor: () => void;
  onSaveCliente: () => void;
  onSelectCliente: (clienteId: number) => void;
};

function ClientesView({
  catalogos,
  clientes,
  notice,
  editorMessage,
  form,
  editorOpen,
  editingClienteId,
  contaCorrente,
  contaResumo,
  loading,
  canManage,
  selectedCliente,
  selectedClienteId,
  onApplyMatrizZero,
  onChangeForm,
  onCloseEditor,
  onEditCliente,
  onOpenEditor,
  onSaveCliente,
  onSelectCliente
}: ClientesViewProps) {
  const [columnEditorOpen, setColumnEditorOpen] = useState(false);
  const [moreOptionsOpen, setMoreOptionsOpen] = useState(false);
  const [columns, setColumns] = useState<ClienteColumn[]>(loadClientColumns);

  useEffect(() => {
    window.localStorage.setItem(CLIENT_COLUMNS_STORAGE, JSON.stringify(columns));
  }, [columns]);

  useEffect(() => {
    if (!editorOpen) return;
    setMoreOptionsOpen(Boolean(editingClienteId && (form.transporteId || form.observacoes.trim() || form.inativo)));
  }, [editorOpen, editingClienteId]);

  function changeField<K extends keyof ClienteForm>(field: K, value: ClienteForm[K]) {
    onChangeForm({ ...form, [field]: value });
  }

  function toggleColumn(key: ClienteColumnKey) {
    setColumns((current) => {
      const target = current.find((column) => column.key === key);
      if (target?.visible && current.filter((column) => column.visible).length === 1) return current;
      return current.map((column) => column.key === key ? { ...column, visible: !column.visible } : column);
    });
  }

  function moveColumn(index: number, direction: -1 | 1) {
    setColumns((current) => {
      const destination = index + direction;
      if (destination < 0 || destination >= current.length) return current;
      const next = [...current];
      [next[index], next[destination]] = [next[destination], next[index]];
      return next;
    });
  }

  const visibleColumns = columns.filter((column) => column.visible);

  return (
    <>
      {notice && !editorOpen && <p className="fac-editor-message">{notice}</p>}
      <section className={`fac-clients-header-grid ${editorOpen ? "fac-hidden" : ""}`}>
        <div className="fac-clients-header-main">
          <section className="fac-hero fac-clients-hero">
            <div>
              <p className="fac-eyebrow">Clientes</p>
              <h2>Consulta simples com conta corrente integrada</h2>
              <p>
                Consulta clientes e acompanha a respetiva conta corrente num único local.
              </p>
            </div>
          </section>

          <section className="fac-metrics fac-header-metrics" aria-label="Indicadores de cliente">
            <article className="fac-metric client">
              <span>Saldo pendente</span>
              <strong>{contaResumo ? `${money(contaResumo.valorPendente)} ${contaResumo.moedaId}` : "-"}</strong>
            </article>
            <article className="fac-metric document">
              <span>Documentos</span>
              <strong>{contaResumo?.documentos ?? 0}</strong>
            </article>
            <article className="fac-metric treasury">
              <span>Recebido ativo</span>
              <strong>{contaResumo ? `${money(contaResumo.valorRecebidoAtivo)} ${contaResumo.moedaId}` : "-"}</strong>
            </article>
            <article className="fac-metric product">
              <span>Vencidos</span>
              <strong>{contaResumo?.vencidos ?? 0}</strong>
            </article>
          </section>
        </div>

        <aside className="fac-panel fac-detail fac-clients-detail-top">
          <p className="fac-eyebrow">Ficha resumida</p>
          <h2>{selectedCliente?.nome ?? "Sem cliente"}</h2>
          <dl>
            <div><dt>Código</dt><dd>{selectedCliente?.id ?? "-"}</dd></div>
            <div><dt>NIF</dt><dd>{selectedCliente?.nif ?? "-"}</dd></div>
            <div><dt>Morada</dt><dd>{selectedCliente?.morada ?? "-"}</dd></div>
            <div><dt>Localidade</dt><dd>{selectedCliente?.localidade ?? "-"}</dd></div>
            <div><dt>Código postal</dt><dd>{selectedCliente?.codPostalId ?? "-"}</dd></div>
            <div><dt>País</dt><dd>{selectedCliente?.paisId ?? "-"}</dd></div>
            <div><dt>Moeda</dt><dd>{selectedCliente?.moedaId ?? "-"}</dd></div>
            <div><dt>Regime IVA</dt><dd>{selectedCliente?.rivaId ?? "-"}</dd></div>
          </dl>
          {canManage && <button
            className="fac-primary-button"
            disabled={!selectedCliente || loading}
            onClick={() => selectedCliente && onEditCliente(selectedCliente.id)}
            type="button"
          >
            Editar cliente
          </button>}
        </aside>
      </section>

      <section className={`fac-content-grid fac-clients-content-grid ${editorOpen ? "fac-hidden" : ""}`}>
        <article className="fac-panel fac-panel-main fac-clients-table-panel">
          <div className="fac-panel-header">
            <div>
              <p className="fac-eyebrow">Consulta</p>
              <h2>Clientes</h2>
            </div>
            <div className="fac-inline-actions">
              <button className="fac-ghost-button" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({visibleColumns.length})</button>
              {canManage && <button className="fac-soft-button" onClick={onOpenEditor} type="button">Novo cliente</button>}
            </div>
          </div>

          {columnEditorOpen && <div className="fac-column-editor">
            <div className="fac-column-editor-header">
              <div><strong>Colunas da listagem</strong><span>Marca os campos visíveis e define a respetiva ordem.</span></div>
              <button className="fac-ghost-button" onClick={() => setColumns(DEFAULT_CLIENT_COLUMNS)} type="button">Repor base</button>
            </div>
            <div className="fac-column-list">
              {columns.map((column, index) => <div className="fac-column-item" key={column.key}>
                <label><input checked={column.visible} onChange={() => toggleColumn(column.key)} type="checkbox" />{column.label}</label>
                <div className="fac-column-order">
                  <button aria-label={`Subir ${column.label}`} disabled={index === 0} onClick={() => moveColumn(index, -1)} type="button">↑</button>
                  <button aria-label={`Descer ${column.label}`} disabled={index === columns.length - 1} onClick={() => moveColumn(index, 1)} type="button">↓</button>
                </div>
              </div>)}
            </div>
          </div>}

          <table className="fac-table">
            <thead>
              <tr>
                {visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}
              </tr>
            </thead>
            <tbody>
              {clientes.map((cliente) => (
                <tr
                  className={cliente.id === selectedClienteId ? "fac-row-selected" : ""}
                  key={cliente.id}
                  onClick={() => onSelectCliente(cliente.id)}
                >
                  {visibleColumns.map((column) => <td key={column.key}>{clientColumnValue(cliente, column.key)}</td>)}
                </tr>
              ))}
              {!loading && clientes.length === 0 && (
                <tr>
                  <td colSpan={visibleColumns.length}>Sem clientes para mostrar.</td>
                </tr>
              )}
            </tbody>
          </table>
        </article>

        <aside className="fac-panel fac-detail fac-clients-detail-card">
          <p className="fac-eyebrow">Ficha resumida</p>
          <h2>{selectedCliente?.nome ?? "Sem cliente"}</h2>
          <dl>
            <div><dt>Código</dt><dd>{selectedCliente?.id ?? "-"}</dd></div>
            <div><dt>NIF</dt><dd>{selectedCliente?.nif ?? "-"}</dd></div>
            <div><dt>Morada</dt><dd>{selectedCliente?.morada ?? "-"}</dd></div>
            <div><dt>Localidade</dt><dd>{selectedCliente?.localidade ?? "-"}</dd></div>
            <div><dt>Código postal</dt><dd>{selectedCliente?.codPostalId ?? "-"}</dd></div>
            <div><dt>País</dt><dd>{selectedCliente?.paisId ?? "-"}</dd></div>
            <div><dt>Moeda</dt><dd>{selectedCliente?.moedaId ?? "-"}</dd></div>
            <div><dt>Regime IVA</dt><dd>{selectedCliente?.rivaId ?? "-"}</dd></div>
          </dl>
          {canManage && <button
            className="fac-primary-button"
            disabled={!selectedCliente || loading}
            onClick={() => selectedCliente && onEditCliente(selectedCliente.id)}
            type="button"
          >
            Editar cliente
          </button>}
        </aside>
      </section>

      {editorOpen && (
        <section className="fac-panel fac-section-panel">
          <div className="fac-panel-header">
            <div>
              <p className="fac-eyebrow">Editor</p>
              <h2>{editingClienteId ? `Editar cliente ${editingClienteId}` : "Novo cliente"}</h2>
            </div>
            <div className="fac-inline-actions">
              {!editingClienteId && (
                <button className="fac-soft-button" disabled={loading} onClick={onApplyMatrizZero} type="button">
                  Aplicar Matriz 0
                </button>
              )}
              <button className="fac-ghost-button" onClick={onCloseEditor} type="button">Voltar a lista</button>
            </div>
          </div>

          {editorMessage && <p className="fac-editor-message">{editorMessage}</p>}

          <div className="fac-client-form-sections">
            <FormSection title="Identificação">
              <Field label="Código"><input disabled value={editingClienteId ? String(editingClienteId) : "Automático"} /></Field>
              <Field label="Nome"><input maxLength={80} onChange={(event) => changeField("nome", event.target.value)} value={form.nome} /></Field>
              <Field label="NIF"><input maxLength={9} onChange={(event) => changeField("nif", event.target.value)} value={form.nif} /></Field>
            </FormSection>

            <FormSection title="Contactos">
              <Field label="Email"><input maxLength={120} onChange={(event) => changeField("email", event.target.value)} type="email" value={form.email} /></Field>
              <Field label="Telefone"><input maxLength={20} onChange={(event) => changeField("tel", event.target.value)} value={form.tel} /></Field>
              <Field label="Telemóvel"><input maxLength={20} onChange={(event) => changeField("tm", event.target.value)} value={form.tm} /></Field>
            </FormSection>

            <FormSection title="Morada">
              <Field label="Morada"><input maxLength={60} onChange={(event) => changeField("morada", event.target.value)} value={form.morada} /></Field>
              <Field label="Morada complementar"><input maxLength={60} onChange={(event) => changeField("morada1", event.target.value)} value={form.morada1} /></Field>
              <Field label="Código postal"><input onChange={(event) => changeField("codPostalId", event.target.value)} value={form.codPostalId} /></Field>
              <Field label="Localidade"><input maxLength={50} onChange={(event) => changeField("localidade", event.target.value)} value={form.localidade} /></Field>
              <Field label="País">
                <select onChange={(event) => changeField("paisId", event.target.value)} value={form.paisId}>
                  <option value="">Sem valor</option>
                  {catalogos?.paises.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
                </select>
              </Field>
            </FormSection>

            <FormSection title="Condições comerciais e financeiras">
              <Field label="Moeda">
                <select onChange={(event) => changeField("moedaId", event.target.value)} value={form.moedaId}>
                  <option value="">Sem valor</option>
                  {catalogos?.moedas.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
                </select>
              </Field>
              <Field label="Modo de pagamento">
                <select onChange={(event) => changeField("mPagamentoId", event.target.value)} value={form.mPagamentoId}>
                  <option value="">Sem valor</option>
                  {catalogos?.modosPagamento.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
                </select>
              </Field>
              <Field label="Prazo de pagamento">
                <select onChange={(event) => changeField("pPagamentoId", event.target.value)} value={form.pPagamentoId}>
                  <option value="">Sem valor</option>
                  {catalogos?.prazosPagamento.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
                </select>
              </Field>
              <Field label="Regime de IVA">
                <select onChange={(event) => changeField("rivaId", event.target.value)} value={form.rivaId}>
                  <option value="">Sem valor</option>
                  {catalogos?.regimesIva.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
                </select>
              </Field>
              <Field label="IBAN"><input maxLength={34} onChange={(event) => changeField("iban", event.target.value)} value={form.iban} /></Field>
              <label className="fac-check-field">
                <input checked={form.retencao} onChange={(event) => changeField("retencao", event.target.checked)} type="checkbox" />
                <span>Cliente sujeito a retenção</span>
              </label>
            </FormSection>

            <section className={`fac-more-options ${moreOptionsOpen ? "open" : ""}`}>
              <button
                aria-controls="fac-client-more-options"
                aria-expanded={moreOptionsOpen}
                className="fac-more-options-trigger"
                onClick={() => setMoreOptionsOpen((open) => !open)}
                type="button"
              >
                <span>Mais opções</span>
                {(form.transporteId || form.observacoes.trim() || form.inativo) && <small>Com valores</small>}
              </button>
              <div className="fac-form-grid" hidden={!moreOptionsOpen} id="fac-client-more-options">
                <Field label="Transporte">
                  <select onChange={(event) => changeField("transporteId", event.target.value)} value={form.transporteId}>
                    <option value="">Sem valor</option>
                    {catalogos?.transportes.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
                  </select>
                </Field>
                <Field label="Observações"><textarea maxLength={300} onChange={(event) => changeField("observacoes", event.target.value)} value={form.observacoes} /></Field>
                <label className="fac-check-field">
                  <input checked={form.inativo} onChange={(event) => changeField("inativo", event.target.checked)} type="checkbox" />
                  <span>Cliente inativo</span>
                </label>
              </div>
            </section>
          </div>

          <div className="fac-form-footer">
            <span className="fac-muted">
              {editingClienteId ? "As alteracoes substituem os dados atuais do cliente." : "A Matriz 0 preenche apenas os valores configurados."}
            </span>
            <button className="fac-primary-button" disabled={loading} onClick={onSaveCliente} type="button">
              {loading ? "A gravar..." : editingClienteId ? "Guardar alteracoes" : "Gravar cliente"}
            </button>
          </div>
        </section>
      )}

      <section className={`fac-panel fac-section-panel ${editorOpen ? "fac-hidden" : ""}`}>
        <div className="fac-panel-header">
          <div>
            <p className="fac-eyebrow">Conta corrente</p>
            <h2>{contaCorrente?.clienteNome ?? selectedCliente?.nome ?? "Sem cliente"}</h2>
          </div>
          <span className="fac-muted">{loading ? "A carregar..." : `${contaCorrente?.documentos.length ?? 0} documentos`}</span>
        </div>

        <table className="fac-table">
          <thead>
            <tr>
              <th>Documento</th>
              <th>Estado</th>
              <th>Data</th>
              <th>Vencimento</th>
              <th>Total</th>
              <th>Recebido</th>
              <th>Pendente</th>
            </tr>
          </thead>
          <tbody>
            {(contaCorrente?.documentos ?? []).map((documento) => (
              <tr key={documento.pendenteId}>
                <td>{referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento)}</td>
                <td><span className="fac-status">{documento.estado}</span></td>
                <td>{datePt(documento.dataDocumento)}</td>
                <td>{datePt(documento.dataVencimento)}</td>
                <td>{money(documento.valorDocumento)} {documento.moedaId}</td>
                <td>{money(documento.valorRecebidoAtivo)} {documento.moedaId}</td>
                <td>{money(documento.valorPendente)} {documento.moedaId}</td>
              </tr>
            ))}
            {!loading && (contaCorrente?.documentos.length ?? 0) === 0 && (
              <tr>
                <td colSpan={7}>Sem documentos na conta corrente.</td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </>
  );
}

function loadClientColumns(): ClienteColumn[] {
  try {
    const stored = JSON.parse(window.localStorage.getItem(CLIENT_COLUMNS_STORAGE) ?? "null") as ClienteColumn[] | null;
    if (!Array.isArray(stored)) return DEFAULT_CLIENT_COLUMNS;
    const known = new Map(DEFAULT_CLIENT_COLUMNS.map((column) => [column.key, column]));
    const valid = stored.filter((column) => known.has(column.key)).map((column) => ({ ...known.get(column.key)!, visible: Boolean(column.visible) }));
    for (const column of DEFAULT_CLIENT_COLUMNS) if (!valid.some((item) => item.key === column.key)) valid.push(column);
    return valid.some((column) => column.visible) ? valid : DEFAULT_CLIENT_COLUMNS;
  } catch {
    return DEFAULT_CLIENT_COLUMNS;
  }
}

function clientColumnValue(cliente: Cliente, key: ClienteColumnKey) {
  switch (key) {
    case "id": return cliente.id;
    case "nome": return cliente.nome;
    case "nif": return cliente.nif;
    case "email": return cliente.email ?? "-";
    case "tel": return cliente.tel ?? cliente.tm ?? "-";
    case "localidade": return cliente.localidade ?? "-";
    case "paisId": return cliente.paisId ?? "-";
    case "moedaId": return cliente.moedaId ?? "-";
    case "rivaId": return cliente.rivaId ?? "-";
    case "estado": return <span className="fac-status">{cliente.inativo ? "Inativo" : "Ativo"}</span>;
  }
}

type ConfiguracaoViewProps = {
  catalogos: ClienteCatalogos | null;
  exists: boolean;
  form: ParametrosClienteForm;
  loading: boolean;
  message: string | null;
  onChangeForm: (form: ParametrosClienteForm) => void;
  onSave: () => void;
};

function ConfiguracaoView({ catalogos, exists, form, loading, message, onChangeForm, onSave }: ConfiguracaoViewProps) {
  const [area, setArea] = useState<"EMPRESA" | "UTILIZADORES" | "PARAMETROS" | "TABELAS">("PARAMETROS");

  function changeField<K extends keyof ParametrosClienteForm>(field: K, value: ParametrosClienteForm[K]) {
    onChangeForm({ ...form, [field]: value });
  }

  return (
    <>
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Configuração</p>
          <h2>Base de funcionamento do FAC</h2>
          <p>Dados da empresa, valores sugeridos e tabelas de apoio, separados das operações diárias.</p>
        </div>
        <div className="fac-hero-card">
          <span>Area atual</span>
          <strong>{area === "EMPRESA" ? "Empresa" : area === "UTILIZADORES" ? "Utilizadores" : area === "PARAMETROS" ? "Parâmetros" : "Tabelas"}</strong>
          <small>Configuração simples, explícita e centralizada</small>
        </div>
      </section>

      <nav aria-label="Áreas de configuração" className="fac-config-nav">
        <button className={area === "EMPRESA" ? "active" : ""} onClick={() => setArea("EMPRESA")} type="button"><strong>Empresa</strong><span>Identificação e dados fiscais</span></button>
        <button className={area === "UTILIZADORES" ? "active" : ""} onClick={() => setArea("UTILIZADORES")} type="button"><strong>Utilizadores</strong><span>Perfis, estado e palavra-passe</span></button>
        <button className={area === "PARAMETROS" ? "active" : ""} onClick={() => setArea("PARAMETROS")} type="button"><strong>Parâmetros</strong><span>Valores sugeridos da aplicação</span></button>
        <button className={area === "TABELAS" ? "active" : ""} onClick={() => setArea("TABELAS")} type="button"><strong>Tabelas</strong><span>Catálogos de apoio</span></button>
      </nav>

      {area === "EMPRESA" && <EmpresaAdminView />}

      {area === "UTILIZADORES" && <AdminUtilizadoresView />}

      {area === "PARAMETROS" && <>
      <section className="fac-panel">
        <div className="fac-panel-header">
          <div>
            <p className="fac-eyebrow">Valores base</p>
            <h2>Valores base para novos clientes</h2>
          </div>
          <span className="fac-muted">{loading ? "A carregar..." : exists ? "Configurada" : "Ainda vazia"}</span>
        </div>

        {message && <p className="fac-editor-message">{message}</p>}

        <div className="fac-form-grid">
          <Field label="País">
            <select onChange={(event) => changeField("paisId", event.target.value)} value={form.paisId}>
              <option value="">Não sugerir</option>
              {catalogos?.paises.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Moeda">
            <select onChange={(event) => changeField("moedaId", event.target.value)} value={form.moedaId}>
              <option value="">Não sugerir</option>
              {catalogos?.moedas.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Regime de IVA">
            <select onChange={(event) => changeField("rivaId", event.target.value)} value={form.rivaId}>
              <option value="">Não sugerir</option>
              {catalogos?.regimesIva.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Modo de pagamento">
            <select onChange={(event) => changeField("mPagamentoId", event.target.value)} value={form.mPagamentoId}>
              <option value="">Não sugerir</option>
              {catalogos?.modosPagamento.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Prazo de pagamento">
            <select onChange={(event) => changeField("pPagamentoId", event.target.value)} value={form.pPagamentoId}>
              <option value="">Não sugerir</option>
              {catalogos?.prazosPagamento.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Transporte">
            <select onChange={(event) => changeField("transporteId", event.target.value)} value={form.transporteId}>
              <option value="">Não sugerir</option>
              {catalogos?.transportes.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Retencao">
            <select onChange={(event) => changeField("retencao", event.target.value as ParametrosClienteForm["retencao"])} value={form.retencao}>
              <option value="">Não sugerir</option>
              <option value="false">Não</option>
              <option value="true">Sim</option>
            </select>
          </Field>
        </div>

        <div className="fac-form-footer">
          <span className="fac-muted">A Matriz 0 aplica estes valores base apenas quando solicitada no novo cliente.</span>
          <button className="fac-primary-button" disabled={loading} onClick={onSave} type="button">
            {loading ? "A guardar..." : "Guardar Matriz 0"}
          </button>
        </div>
      </section>

      <ParametrosDocumentoView />
      </>}

      {area === "TABELAS" && <TabelasView />}
    </>
  );
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return (
    <label className="fac-field">
      <span>{label}</span>
      {children}
    </label>
  );
}

function FormSection({ children, title }: { children: React.ReactNode; title: string }) {
  return (
    <fieldset className="fac-client-form-section">
      <legend>{title}</legend>
      <div className="fac-form-grid">{children}</div>
    </fieldset>
  );
}

async function fetchPage<T>(url: string): Promise<Page<T>> {
  return fetchJson<Page<T>>(url);
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await apiFetch(url);
  if (!response.ok) {
    throw new Error(await responseError(response));
  }
  return response.json();
}

async function fetchOptionalJson<T>(url: string): Promise<T | null> {
  const response = await apiFetch(url);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function sendJson<T>(url: string, body: unknown): Promise<T> {
  const response = await apiFetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    throw new Error(await responseError(response));
  }
  return response.json();
}

async function putJson(url: string, body: unknown): Promise<void> {
  const response = await apiFetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    throw new Error(await responseError(response));
  }
}

async function responseError(response: Response) {
  try {
    const payload = await response.json();
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}

function validateClienteForm(form: ClienteForm) {
  if (!form.nome.trim()) return "O nome é obrigatório.";
  if (!/^\d{9}$/.test(form.nif.trim())) return "O NIF deve ter exatamente 9 algarismos.";
  if (!form.email.trim() || !/^\S+@\S+\.\S+$/.test(form.email.trim())) return "Indica um email valido.";
  if (!form.morada.trim()) return "A morada é obrigatória.";
  if (!form.codPostalId.trim()) return "O código postal é obrigatório.";
  if (!form.paisId) return "O país é obrigatório.";
  if (!form.moedaId) return "A moeda e obrigatoria.";
  if (!form.transporteId) return "O transporte é obrigatório.";
  return null;
}

function parametrosToForm(parametros: ParametrosCliente): ParametrosClienteForm {
  return {
    paisId: parametros.paisId ?? "",
    moedaId: parametros.moedaId ?? "",
    rivaId: parametros.rivaId ?? "",
    mPagamentoId: parametros.mPagamentoId != null ? String(parametros.mPagamentoId) : "",
    pPagamentoId: parametros.pPagamentoId ?? "",
    transporteId: parametros.transporteId != null ? String(parametros.transporteId) : "",
    retencao: parametros.retencao == null ? "" : String(parametros.retencao) as "true" | "false"
  };
}

function parametrosClientePayload(form: ParametrosClienteForm) {
  return {
    paisId: blankToNull(form.paisId),
    moedaId: blankToNull(form.moedaId),
    rivaId: blankToNull(form.rivaId),
    mPagamentoId: form.mPagamentoId || null,
    pPagamentoId: blankToNull(form.pPagamentoId),
    transporteId: form.transporteId || null,
    retencao: form.retencao === "" ? null : form.retencao === "true"
  };
}

function viewTitle(view: ViewKey) {
  if (view === "Clientes") return "Clientes e conta corrente";
  if (view === "Listagens") return "Listagens e análise";
  if (view === "ImportExport") return "Importação e exportação de dados mestres";
  if (view === "Auditoria") return "Auditoria fiscal";
  if (view === "Configuracao") return "Configuração simples e explícita";
  return "Visão geral";
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

function clientePayload(form: ClienteForm) {
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
    mPagamentoId: form.mPagamentoId || null,
    pPagamentoId: blankToNull(form.pPagamentoId),
    rivaId: blankToNull(form.rivaId),
    transporteId: form.transporteId || null
  };
}

function blankToNull(value: string) {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}

function numberOrNull(value: string) {
  return value ? Number(value) : null;
}

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function referencia(tipo: string, serie: string, numero: number | null) {
  return `${tipo} ${serie}/${numero ?? "rascunho"}`;
}

function datePt(value: string) {
  if (!value) {
    return "-";
  }
  return value.split("-").reverse().join("/");
}

function viewFromNavigationState(state: unknown): ViewKey | null {
  if (!state || typeof state !== "object" || !("activeView" in state)) return null;
  const view = (state as { activeView?: unknown }).activeView;
  return typeof view === "string" && navigationItems.some((item) => item.label === view) ? view as ViewKey : null;
}

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export default App;
