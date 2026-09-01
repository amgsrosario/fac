import { ReactNode, useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Paginator } from "primereact/paginator";
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
import { EntityDetailOverlay } from "./EntityContext";
import { integer as tuuliInteger, money as tuuliMoney } from "./ui/tuuli/format";
import { apiFetch, AuthSession, responseError } from "./api";

type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
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
  periodo: { dataInicio: string; dataFim: string };
  moedaId: string;
  vendas: number;
  recebimentos: number;
  valorEmAberto: number;
  documentosVencidos: { quantidade: number; valor: number };
  evolucao: { periodo: string; vendas: number; recebimentos: number }[];
  clientesComMaiorSaldo: { clienteId: number; clienteNome: string; saldo: number; documentosPendentes: number; vencimentoMaisAntigo?: string }[];
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
  { label: "Configuracao", hint: "Parâmetros gerais" },
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
  const mobileMenuButtonRef = useRef<HTMLButtonElement>(null);
  const mobileDrawerRef = useRef<HTMLElement>(null);
  const navigationView = viewFromNavigationState(location.state);
  const [activeView, setActiveView] = useState<ViewKey>(navigationView ?? initialView);
  const [adminMenuOpen, setAdminMenuOpen] = useState(false);
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [dashboardStart, setDashboardStart] = useState(() => monthStartIso());
  const [dashboardEnd, setDashboardEnd] = useState(() => todayIso());
  const dashboardRequestRef = useRef(0);
  const [clientes, setClientes] = useState<Page<Cliente> | null>(null);
  const [selectedClienteId, setSelectedClienteId] = useState<number | null>(null);
  const [contaCorrente, setContaCorrente] = useState<ContaCorrenteDiagnostico | null>(null);
  const [loading, setLoading] = useState(true);
  const [clientesLoading, setClientesLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [clienteSearch, setClienteSearch] = useState("");
  const [clientePage, setClientePage] = useState(0);
  const [clientePageSize, setClientePageSize] = useState(20);
  const [clienteMostrarInativos, setClienteMostrarInativos] = useState(false);
  const clienteRequestRef = useRef(0);
  const clienteSelectionRequestRef = useRef(0);
  const contaCorrenteRequestRef = useRef(0);
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

  async function loadDashboard(dataInicio = dashboardStart, dataFim = dashboardEnd) {
    if (!dataInicio || !dataFim || dataInicio > dataFim) {
      setError("A data inicial não pode ser posterior à data final.");
      return;
    }
    const requestId = ++dashboardRequestRef.current;
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({ dataInicio, dataFim });
      const response = await fetchJson<DashboardData>(`/api/dashboard/comercial?${params}`);
      if (requestId === dashboardRequestRef.current) setDashboardData(response);
    } catch (err) {
      if (requestId === dashboardRequestRef.current) {
        setDashboardData(null);
        setError(err instanceof Error ? err.message : "Não foi possível carregar dados.");
      }
    } finally {
      if (requestId === dashboardRequestRef.current) setLoading(false);
    }
  }

  async function loadClientes(pageNumber = clientePage, search = clienteSearch, pageSize = clientePageSize) {
    const requestId = ++clienteRequestRef.current;
    setClientesLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams({ page: String(pageNumber), size: String(pageSize), sort: "nome,asc" });
      if (search.trim()) params.set("search", search.trim());
      if (!clienteMostrarInativos) params.set("inativo", "false");
      const page = await fetchPage<Cliente>(`/api/clientes?${params}`);
      if (requestId !== clienteRequestRef.current) return;
      setClientes(page);
      setClientePage(page.number);
      const firstClienteId = page.content[0]?.id ?? null;
      setSelectedClienteId((current) => page.content.some((cliente) => cliente.id === current) ? current : firstClienteId);
    } catch (err) {
      if (requestId !== clienteRequestRef.current) return;
      setError(err instanceof Error ? err.message : "Não foi possível carregar clientes.");
    } finally {
      if (requestId === clienteRequestRef.current) setClientesLoading(false);
    }
  }

  async function loadContaCorrente(clienteId: number) {
    const requestId = ++contaCorrenteRequestRef.current;
    setClientesLoading(true);
    setError(null);
    try {
      const diagnostico = await fetchJson<ContaCorrenteDiagnostico>(`/api/pendentes/conta-corrente/clientes/${clienteId}/diagnostico`);
      if (requestId === contaCorrenteRequestRef.current) setContaCorrente(diagnostico);
    } catch (err) {
      if (requestId !== contaCorrenteRequestRef.current) return;
      setError(err instanceof Error ? err.message : "Não foi possível carregar a conta corrente.");
      setContaCorrente(null);
    } finally {
      if (requestId === contaCorrenteRequestRef.current) setClientesLoading(false);
    }
  }

  async function selectDashboardCliente(clienteId: number) {
    selectView("Clientes");
    const requestId = ++clienteSelectionRequestRef.current;
    clienteRequestRef.current += 1;
    setClientesLoading(true);
    setError(null);
    try {
      const cliente = await fetchJson<Cliente>(`/api/clientes/${clienteId}`);
      if (requestId !== clienteSelectionRequestRef.current) return;
      setClientes((current) => {
        const currentContent = current?.content ?? [];
        const existingIndex = currentContent.findIndex((item) => item.id === cliente.id);
        const content = existingIndex >= 0
          ? currentContent.map((item) => item.id === cliente.id ? cliente : item)
          : [...currentContent, cliente].sort((left, right) =>
              left.nome.localeCompare(right.nome, "pt", { sensitivity: "base" }) || left.id - right.id
            );
        return {
          content,
          totalElements: current?.totalElements ?? 1,
          totalPages: current?.totalPages ?? 1,
          number: current?.number ?? 0,
          size: current?.size ?? clientePageSize
        };
      });
      setSelectedClienteId(cliente.id);
    } catch (err) {
      if (requestId !== clienteSelectionRequestRef.current) return;
      setError(err instanceof Error ? err.message : "Não foi possível abrir o cliente.");
    } finally {
      if (requestId === clienteSelectionRequestRef.current) setClientesLoading(false);
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

      await loadClientes(0, clienteSearch, clientePageSize);
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
      await loadClientes(clientePage, clienteSearch, clientePageSize);
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

  const requestedShellView = embeddedContent ? initialView : activeView;
  const shellView = canShowMenuItem({ label: requestedShellView, hint: "" }) ? requestedShellView : "Dashboard";

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
    loadClientes(0, "", clientePageSize);
  }, []);

  useEffect(() => {
    if (shellView !== "Clientes") return;
    const timeout = window.setTimeout(() => loadClientes(0, clienteSearch, clientePageSize), 300);
    return () => window.clearTimeout(timeout);
  }, [clienteSearch, clienteMostrarInativos]);

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
    setMobileDrawerOpen(false);
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

  useEffect(() => {
    if (!mobileDrawerOpen) return;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const focusableSelector = "button:not(:disabled), [href], input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex='-1'])";
    const firstFocusable = mobileDrawerRef.current?.querySelector<HTMLElement>(focusableSelector);
    window.requestAnimationFrame(() => firstFocusable?.focus());

    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        event.preventDefault();
        closeMobileDrawer();
        return;
      }
      if (event.key !== "Tab" || !mobileDrawerRef.current) return;
      const focusable = Array.from(mobileDrawerRef.current.querySelectorAll<HTMLElement>(focusableSelector))
        .filter((element) => element.offsetParent !== null);
      if (focusable.length === 0) {
        event.preventDefault();
        return;
      }
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    }

    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [mobileDrawerOpen]);

  function selectView(view: ViewKey) {
    if (view === "Clientes") {
      clienteSelectionRequestRef.current += 1;
      contaCorrenteRequestRef.current += 1;
      setSelectedClienteId(null);
      setContaCorrente(null);
    }
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
    setMobileDrawerOpen(false);
  }

  function closeMobileDrawer(returnFocus = true) {
    setMobileDrawerOpen(false);
    if (returnFocus) {
      window.requestAnimationFrame(() => mobileMenuButtonRef.current?.focus());
    }
  }

  function selectMobileView(view: ViewKey) {
    selectView(view);
    closeMobileDrawer(false);
  }

  function renderMenu(groups: MenuGroup[], onSelect: (view: ViewKey) => void) {
    return groups.map((group) => (
      <section className="fac-menu-section" key={group.title}>
        <p>{group.title}</p>
        {group.items.map((item) => (
          <button
            aria-current={shellView === item.label ? "page" : undefined}
            className={shellView === item.label ? "active" : ""}
            key={item.label}
            onClick={() => onSelect(item.label)}
            type="button"
          >
            <span>{menuLabel(item.label)}</span>
          </button>
        ))}
      </section>
    ));
  }

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

  const metrics: Array<{ detail?: string; label: string; value: string }> = [
    { label: "Vendas no período", value: `${dashboardMoney(dashboardData?.vendas ?? 0)} ${dashboardData?.moedaId ?? "EUR"}` },
    { label: "Recebimentos no período", value: `${dashboardMoney(dashboardData?.recebimentos ?? 0)} ${dashboardData?.moedaId ?? "EUR"}` },
    { label: "Valores em aberto", value: `${dashboardMoney(dashboardData?.valorEmAberto ?? 0)} ${dashboardData?.moedaId ?? "EUR"}` },
    {
      detail: documentsLabel(dashboardData?.documentosVencidos.quantidade ?? 0),
      label: "Documentos vencidos",
      value: `${dashboardMoney(dashboardData?.documentosVencidos.valor ?? 0)} ${dashboardData?.moedaId ?? "EUR"}`
    }
  ];

  return (
    <main className={`fac-shell${shellView === "Documentos" ? " fac-shell-documents-v2" : ""}${shellView === "Documentos" || shellView === "Clientes" ? " fac-shell-tuuli-v2" : ""}`}>
      <header className="fac-mobile-topbar">
        <button
          aria-controls="fac-mobile-drawer"
          aria-expanded={mobileDrawerOpen}
          aria-label="Abrir menu"
          className="fac-mobile-menu-button"
          onClick={() => setMobileDrawerOpen(true)}
          ref={mobileMenuButtonRef}
          type="button"
        >
          <i aria-hidden="true" className="pi pi-bars" />
        </button>
        <div className="fac-mobile-title">
          <span className="fac-mobile-logo"><img alt="TUULI AIR" src="/tuuli-air-logo-compact.png" /></span>
          <span>{viewTitle(shellView)}</span>
        </div>
        <div className="fac-mobile-actions">
          {visibleAdminMenuItems.length > 0 && (
            <button
              aria-label="Administração e configuração"
              className={`fac-admin-trigger${isAdminView ? " active" : ""}`}
              onClick={() => setMobileDrawerOpen(true)}
              title="Administração e configuração"
              type="button"
            >
              <i aria-hidden="true" className="pi pi-shield" />
            </button>
          )}
          <button className="fac-mobile-logout" onClick={onLogout} type="button">Sair</button>
        </div>
      </header>

      {mobileDrawerOpen && <button aria-label="Fechar menu" className="fac-mobile-drawer-overlay" onClick={() => closeMobileDrawer()} type="button" />}
      {mobileDrawerOpen && (
        <aside
          aria-label="Menu principal"
          aria-modal="true"
          className="fac-mobile-drawer open"
          id="fac-mobile-drawer"
          ref={mobileDrawerRef}
          role="dialog"
        >
          <div className="fac-mobile-drawer-header">
            <div className="fac-brand">
              <span className="fac-brand-logo"><img alt="TUULI AIR" src="/tuuli-air-logo-compact.png" /></span>
              {import.meta.env.VITE_FAC_DEMO_MODE === "true" && <span className="fac-demo-label">Ambiente de demonstração</span>}
            </div>
            <button aria-label="Fechar menu" className="fac-mobile-drawer-close" onClick={() => closeMobileDrawer()} type="button">
              <i aria-hidden="true" className="pi pi-times" />
            </button>
          </div>
          <nav className="fac-menu fac-mobile-drawer-nav" aria-label="Navegação principal">
            {renderMenu(visibleMenuGroups, selectMobileView)}
            {visibleAdminMenuItems.length > 0 && (
              <section className="fac-menu-section">
                <p>Administração</p>
                {visibleAdminMenuItems.map((item) => (
                  <button
                    aria-current={shellView === item.label ? "page" : undefined}
                    className={shellView === item.label ? "active" : ""}
                    key={item.label}
                    onClick={() => selectMobileView(item.label)}
                    type="button"
                  >
                    <span>{menuLabel(item.label)}</span>
                    <small>{item.hint}</small>
                  </button>
                ))}
              </section>
            )}
          </nav>
          <div className="fac-mobile-drawer-footer">
            <div className="fac-current-user"><span>{currentUser.nome}</span><small>{currentUser.papel} · {currentUser.codigo}</small></div>
            <button className="fac-ghost-button" onClick={onLogout} type="button">Sair</button>
          </div>
        </aside>
      )}

      <aside className="fac-sidebar">
        <div className="fac-brand">
          <span className="fac-brand-logo"><img alt="TUULI AIR" src="/tuuli-air-logo-compact.png" /></span>
          {import.meta.env.VITE_FAC_DEMO_MODE === "true" && <span className="fac-demo-label">Ambiente de demonstração</span>}
        </div>

        <nav className="fac-menu" aria-label="Navegação principal">
          {renderMenu(visibleMenuGroups, selectView)}
        </nav>
      </aside>

      <section className={`fac-workspace${shellView === "Documentos" ? " fac-workspace-documents-v2" : ""}${shellView === "Documentos" || shellView === "Clientes" ? " fac-workspace-tuuli-v2" : ""}${shellView === "Clientes" ? " fac-workspace-tuuli-entity-list" : ""}`}>
        <header className="fac-topbar">
          <div>
            {import.meta.env.VITE_FAC_DEMO_MODE === "true" && <p className="fac-eyebrow">Ambiente de demonstração</p>}
            <h1 className={shellView === "Documentos" || shellView === "Clientes" ? "tuuli-page-title" : undefined}>{viewTitle(shellView)}</h1>
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
            clientes={clientes?.content ?? []}
            page={clientePage}
            pageSize={clientePageSize}
            search={clienteSearch}
            mostrarInativos={clienteMostrarInativos}
            totalElements={clientes?.totalElements ?? 0}
            notice={clienteNotice}
            editorMessage={editorMessage}
            form={clienteForm}
            editorOpen={clienteEditorOpen}
            editingClienteId={editingClienteId}
            contaCorrente={contaCorrente}
            contaResumo={contaResumo}
            error={error}
            loading={clientesLoading}
            canManage={currentUser.permissoes?.includes("MESTRES_GERIR") ?? false}
            selectedCliente={selectedCliente}
            selectedClienteId={selectedClienteId}
            onApplyMatrizZero={applyMatrizZero}
            onChangeForm={setClienteForm}
            onCloseEditor={() => { setClienteEditorOpen(false); setEditingClienteId(null); }}
            onEditCliente={openClienteEditEditor}
            onOpenEditor={openClienteEditor}
            onPageChange={(page, size) => { setClientePageSize(size); loadClientes(page, clienteSearch, size); }}
            onSearch={setClienteSearch}
            onMostrarInativos={setClienteMostrarInativos}
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
            data={dashboardData}
            dataFim={dashboardEnd}
            dataInicio={dashboardStart}
            error={error}
            loading={loading}
            metrics={metrics}
            onDataFim={setDashboardEnd}
            onDataInicio={setDashboardStart}
            onNavigate={selectView}
            onSelectCliente={selectDashboardCliente}
            onRefresh={() => loadDashboard()}
          />
        )}

        {error && <p className="fac-message">{error}</p>}
      </section>
    </main>
  );
}

type DashboardViewProps = {
  data: DashboardData | null;
  dataFim: string;
  dataInicio: string;
  error: string | null;
  loading: boolean;
  metrics: { detail?: string; label: string; value: string }[];
  onDataFim: (value: string) => void;
  onDataInicio: (value: string) => void;
  onNavigate: (view: ViewKey) => void;
  onSelectCliente: (clienteId: number) => void;
  onRefresh: () => void;
};

function DashboardView({
  data,
  dataFim,
  dataInicio,
  error,
  loading,
  metrics,
  onDataFim,
  onDataInicio,
  onNavigate,
  onSelectCliente,
  onRefresh
}: DashboardViewProps) {
  const maxEvolution = Math.max(1, ...(data?.evolucao.flatMap((point) => [Number(point.vendas), Number(point.recebimentos)]) ?? [0]));
  return (
    <>
      <section className="fac-dashboard-period" aria-label="Período do dashboard">
        <label><span>Data inicial</span><input max={dataFim} onChange={(event) => onDataInicio(event.target.value)} type="date" value={dataInicio}/></label>
        <label><span>Data final</span><input min={dataInicio} onChange={(event) => onDataFim(event.target.value)} type="date" value={dataFim}/></label>
        <button className="fac-soft-button" disabled={loading || !dataInicio || !dataFim || dataInicio > dataFim} onClick={onRefresh} type="button">Atualizar</button>
      </section>

      <section className="fac-dashboard-metrics" aria-label="Indicadores">
        {metrics.map((metric) => (
          <article className="fac-dashboard-metric" key={metric.label}>
            <span>{metric.label}</span>
            <strong>{loading || error ? "-" : metric.value}</strong>
            {metric.detail && <small>{loading || error ? "" : metric.detail}</small>}
          </article>
        ))}
      </section>

      {error && <section className="fac-panel fac-dashboard-error" role="alert"><strong>Não foi possível atualizar o dashboard.</strong><span>{error}</span><button className="fac-soft-button" onClick={onRefresh} type="button">Tentar novamente</button></section>}

      {!error && <div className="fac-dashboard-grid">
        <section className="fac-panel fac-dashboard-evolution" aria-labelledby="dashboard-evolution-title">
          <div className="fac-panel-header"><div><p className="fac-eyebrow">Fluxos do período</p><h2 id="dashboard-evolution-title">Evolução de vendas e recebimentos</h2></div></div>
          {loading ? <p className="fac-empty-state">A carregar evolução...</p> : data?.evolucao.length ? <div className="fac-dashboard-chart" role="img" aria-label="Evolução temporal de vendas e recebimentos">
            {data.evolucao.map((point) => <div className="fac-dashboard-chart-row" key={point.periodo}>
              <span>{dashboardPeriodLabel(point.periodo)}</span>
              <div className="fac-dashboard-series"><div><i className="fac-dashboard-bar sales" style={{ width: `${Math.max(0, Number(point.vendas)) / maxEvolution * 100}%` }}/></div><strong>{dashboardMoney(point.vendas)}</strong></div>
              <div className="fac-dashboard-series"><div><i className="fac-dashboard-bar receipts" style={{ width: `${Math.max(0, Number(point.recebimentos)) / maxEvolution * 100}%` }}/></div><strong>{dashboardMoney(point.recebimentos)}</strong></div>
            </div>)}
            <footer className="fac-dashboard-legend"><span><i className="sales"/>Vendas</span><span><i className="receipts"/>Recebimentos</span></footer>
          </div> : <p className="fac-empty-state">Sem vendas ou recebimentos no período selecionado.</p>}
        </section>

        <section className="fac-panel fac-dashboard-clients" aria-labelledby="dashboard-clients-title">
          <div className="fac-panel-header"><div><p className="fac-eyebrow">Posição atual</p><h2 id="dashboard-clients-title">Clientes com maior saldo</h2></div></div>
          {loading ? <p className="fac-empty-state">A carregar saldos...</p> : data?.clientesComMaiorSaldo.length ? <div className="fac-dashboard-client-list">
            {data.clientesComMaiorSaldo.map((cliente) => <article className="fac-dashboard-client-row" key={cliente.clienteId}>
              <button className="fac-table-link" onClick={() => onSelectCliente(cliente.clienteId)} title={cliente.clienteNome} type="button"><span>{cliente.clienteNome}</span></button>
              <div className="fac-dashboard-client-meta"><span>#{cliente.clienteId}</span><span>mais antigo {datePt(cliente.vencimentoMaisAntigo)}</span><span className="fac-dashboard-client-count">{documentsLabel(cliente.documentosPendentes)}</span><strong>{dashboardMoney(cliente.saldo)} {data.moedaId}</strong></div>
            </article>)}
          </div> : <p className="fac-empty-state">Não existem clientes com valores em aberto.</p>}
        </section>
      </div>}

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
  page: number;
  pageSize: number;
  search: string;
  mostrarInativos: boolean;
  totalElements: number;
  notice: string | null;
  editorMessage: string | null;
  error: string | null;
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
  onPageChange: (page: number, size: number) => void;
  onSearch: (value: string) => void;
  onMostrarInativos: (value: boolean) => void;
  onSaveCliente: () => void;
  onSelectCliente: (clienteId: number) => void;
};

function ClientesView({
  catalogos,
  clientes,
  page,
  pageSize,
  search,
  mostrarInativos,
  totalElements,
  notice,
  editorMessage,
  error,
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
  onPageChange,
  onSearch,
  onMostrarInativos,
  onSaveCliente,
  onSelectCliente
}: ClientesViewProps) {
  const [columnEditorOpen, setColumnEditorOpen] = useState(false);
  const [moreOptionsOpen, setMoreOptionsOpen] = useState(false);
  const [columns, setColumns] = useState<ClienteColumn[]>(loadClientColumns);
  const [contaPage, setContaPage] = useState(0);
  const [contaPageSize, setContaPageSize] = useState(10);
  const [contaApenasNaoLiquidados, setContaApenasNaoLiquidados] = useState(false);
  const [exportingClientes, setExportingClientes] = useState<"pdf" | "xlsx" | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const detailTriggerRef = useRef<HTMLButtonElement>(null);

  async function exportarClientes(formato: "pdf" | "xlsx") {
    setExportingClientes(formato);
    try {
      const params = new URLSearchParams({ formato });
      if (!mostrarInativos) params.set("ativos", "true");
      const response = await apiFetch(`/api/exportacoes/clientes?${params}`);
      if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filenameFromDisposition(response.headers.get("Content-Disposition")) ?? `clientes.${formato}`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (exportError) {
      window.alert(exportError instanceof Error ? exportError.message : "Não foi possível exportar os clientes.");
    } finally {
      setExportingClientes(null);
    }
  }

  const contaDocumentos = contaCorrente?.documentos ?? [];
  const contaDocumentosFiltrados = useMemo(() => contaApenasNaoLiquidados
    ? contaDocumentos.filter((documento) => Number(documento.valorPendente) > 0)
    : contaDocumentos, [contaApenasNaoLiquidados, contaCorrente]);
  const contaDocumentosPagina = contaDocumentosFiltrados.slice(contaPage * contaPageSize, (contaPage + 1) * contaPageSize);
  const contaTotais = useMemo(() => {
    const porMoeda = new Map<string, { total: number; recebido: number; pendente: number }>();
    contaDocumentosFiltrados.forEach((documento) => {
      const total = porMoeda.get(documento.moedaId) ?? { total: 0, recebido: 0, pendente: 0 };
      total.total += Number(documento.valorDocumento);
      total.recebido += Number(documento.valorRecebidoAtivo);
      total.pendente += Number(documento.valorPendente);
      porMoeda.set(documento.moedaId, total);
    });
    return Array.from(porMoeda.entries());
  }, [contaDocumentosFiltrados]);

  useEffect(() => {
    setContaPage(0);
    setDetailOpen(false);
  }, [selectedClienteId, contaApenasNaoLiquidados]);

  useEffect(() => {
    const lastPage = Math.max(0, Math.ceil(contaDocumentosFiltrados.length / contaPageSize) - 1);
    if (contaPage > lastPage) setContaPage(lastPage);
  }, [contaDocumentosFiltrados.length, contaPage, contaPageSize]);

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
    <div className="tuuli-v2-page tuuli-grammar-entity-list">
      {notice && !editorOpen && <p className="fac-editor-message">{notice}</p>}
      <section className={`fac-entity-context fac-client-context tuuli-entity-context ${editorOpen ? "fac-hidden" : ""}`} aria-label="Cliente selecionado">
        {selectedCliente ? <>
          <div className="fac-entity-context-main tuuli-entity-primary">
            <strong>{selectedCliente.nome}</strong>
            <span>Código {selectedCliente.id} · NIF {selectedCliente.nif}{selectedCliente.localidade ? ` · ${selectedCliente.localidade}` : ""}</span>
          </div>
          <div className="fac-client-context-metrics tuuli-metric-group tuuli-entity-metrics" aria-label="Indicadores do cliente">
            <div className="fac-entity-context-value tuuli-metric">
              <span>Saldo pendente</span>
              <strong>{contaResumo ? `${tuuliMoney(contaResumo.valorPendente)} ${contaResumo.moedaId}` : "-"}</strong>
            </div>
            <div className="fac-entity-context-value tuuli-metric">
              <span>Total de negócios</span>
              <strong>{contaResumo ? `${tuuliMoney(contaResumo.valorDocumento)} ${contaResumo.moedaId}` : "-"}</strong>
            </div>
            <div className="fac-entity-context-value tuuli-metric">
              <span>Documentos</span>
              <strong>{tuuliInteger(contaResumo?.documentos ?? 0)}</strong>
            </div>
            <div className="fac-entity-context-value tuuli-metric">
              <span>Vencidos</span>
              <strong>{tuuliInteger(contaResumo?.vencidos ?? 0)}</strong>
            </div>
          </div>
          <div className="fac-entity-context-actions tuuli-context-actions">
            <button aria-controls="fac-client-detail" aria-expanded={detailOpen} className="fac-ghost-button tuuli-tool-action" onClick={() => setDetailOpen(true)} ref={detailTriggerRef} type="button">Ver detalhe</button>
            {canManage && <button className="fac-primary-button tuuli-primary-action" disabled={loading} onClick={() => onEditCliente(selectedCliente.id)} type="button">Editar</button>}
          </div>
        </> : <span className="fac-muted">Selecione um cliente para consultar o respetivo contexto.</span>}
      </section>

      <section className={`fac-list-toolbar fac-clients-toolbar tuuli-toolbar ${editorOpen ? "fac-hidden" : ""}`}>
        <label className="tuuli-search"><i aria-hidden="true" className="pi pi-search" /><input aria-label="Pesquisar clientes" onChange={(event) => onSearch(event.target.value)} onKeyDown={(event) => { if (event.key === "Escape") onSearch(""); }} placeholder="Pesquisar por código, nome, NIF ou email" type="search" value={search} /></label>
        <div className="fac-inline-actions">
          <label className="fac-listing-checkbox"><input checked={mostrarInativos} onChange={(event) => onMostrarInativos(event.target.checked)} type="checkbox"/><span>Mostrar inativos</span></label>
          <button className="fac-soft-button tuuli-tool-action" disabled={exportingClientes !== null} onClick={() => exportarClientes("pdf")} type="button">{exportingClientes === "pdf" ? "A gerar PDF..." : "Exportar PDF"}</button>
          <button className="fac-soft-button tuuli-tool-action" disabled={exportingClientes !== null} onClick={() => exportarClientes("xlsx")} type="button">{exportingClientes === "xlsx" ? "A gerar Excel..." : "Exportar Excel"}</button>
          <button className="fac-ghost-button tuuli-tool-action" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({visibleColumns.length})</button>
          {canManage && <button className="fac-primary-button tuuli-primary-action" onClick={onOpenEditor} type="button">Novo cliente</button>}
        </div>
      </section>

      <section className={`fac-content-grid fac-clients-content-grid ${editorOpen ? "fac-hidden" : ""}`}>
        <article className="fac-panel fac-panel-main fac-clients-table-panel tuuli-table-surface">

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

          <table className="fac-table tuuli-table">
            <thead>
              <tr>
                {visibleColumns.map((column) => <th className={clientColumnClass(column.key)} key={column.key}>{column.label}</th>)}
              </tr>
            </thead>
            <tbody>
              {clientes.map((cliente) => (
                <tr
                  className={cliente.id === selectedClienteId ? "fac-row-selected tuuli-table-row-selected" : ""}
                  key={cliente.id}
                  onClick={() => onSelectCliente(cliente.id)}
                >
                  {visibleColumns.map((column) => <td className={clientColumnClass(column.key)} key={column.key}>{clientColumnValue(cliente, column.key)}</td>)}
                </tr>
              ))}
              {!loading && !error && clientes.length === 0 && (
                <tr>
                  <td colSpan={visibleColumns.length}>{search ? "Nenhum cliente corresponde à pesquisa." : "Sem clientes para mostrar."}</td>
                </tr>
              )}
            </tbody>
          </table>
          <div className="fac-list-pagination tuuli-pagination">
            <span>{search ? `${clientes.length} de ${totalElements}` : totalElements} {totalElements === 1 ? "cliente" : "clientes"}</span>
            <Paginator first={page * pageSize} onPageChange={(event) => onPageChange(event.page, event.rows)} rows={pageSize} rowsPerPageOptions={[10, 20, 50]} totalRecords={totalElements} />
          </div>
        </article>

      </section>

      <EntityDetailOverlay labelledBy="fac-client-detail-title" onClose={() => setDetailOpen(false)} open={detailOpen && Boolean(selectedCliente)} returnFocusRef={detailTriggerRef}>
        {selectedCliente && <div id="fac-client-detail">
          <p className="fac-eyebrow">Cliente</p>
          <h2 id="fac-client-detail-title">{selectedCliente.nome}</h2>
          <dl className="fac-entity-detail-rows">
            <div><dt>Código</dt><dd>{selectedCliente.id}</dd></div>
            <div><dt>NIF</dt><dd>{selectedCliente.nif}</dd></div>
            <div><dt>Morada</dt><dd>{selectedCliente.morada ?? "-"}</dd></div>
            <div><dt>Morada adicional</dt><dd>{selectedCliente.morada1 ?? "-"}</dd></div>
            <div><dt>Código postal</dt><dd>{selectedCliente.codPostalId ?? "-"}</dd></div>
            <div><dt>Localidade</dt><dd>{selectedCliente.localidade ?? "-"}</dd></div>
            <div><dt>País</dt><dd>{selectedCliente.paisId ?? "-"}</dd></div>
            <div><dt>Moeda</dt><dd>{selectedCliente.moedaId ?? "-"}</dd></div>
            <div><dt>Regime IVA</dt><dd>{selectedCliente.rivaId ?? "-"}</dd></div>
            <div><dt>Email</dt><dd>{selectedCliente.email ?? "-"}</dd></div>
            <div><dt>Telefone</dt><dd>{selectedCliente.tel ?? selectedCliente.tm ?? "-"}</dd></div>
            <div><dt>Estado</dt><dd>{selectedCliente.inativo ? "Inativo" : "Ativo"}</dd></div>
          </dl>
        </div>}
      </EntityDetailOverlay>

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

      <section className={`fac-panel fac-section-panel tuuli-section ${editorOpen ? "fac-hidden" : ""}`}>
        <div className="fac-panel-header tuuli-section-header">
          <div>
            <p className="fac-eyebrow tuuli-section-kicker">Conta corrente</p>
            <h2 className="tuuli-section-title">{contaCorrente?.clienteNome ?? selectedCliente?.nome ?? "Sem cliente"}</h2>
          </div>
          <div className="fac-current-account-actions">
            <label className="tuuli-inline-control"><input checked={contaApenasNaoLiquidados} onChange={(event) => setContaApenasNaoLiquidados(event.target.checked)} type="checkbox" /><span>Apenas não liquidados</span></label>
            <span className="fac-muted tuuli-meta">{loading ? "A carregar..." : contaApenasNaoLiquidados ? `${contaDocumentosFiltrados.length} de ${contaDocumentos.length} documentos` : `${contaDocumentos.length} documentos`}</span>
          </div>
        </div>

        <div className="fac-table-scroll tuuli-table-surface"><table className="fac-table fac-current-account-table tuuli-table">
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
            {contaDocumentosPagina.map((documento) => (
              <tr key={documento.pendenteId}>
                <td className="tuuli-cell-primary">{referencia(documento.tipoDocumentoId, documento.serie, documento.numeroDocumento)}</td>
                <td><span className={`fac-status tuuli-status tuuli-status-${documento.estado.toLocaleLowerCase("pt-PT")}`}>{statusLabel(documento.estado)}</span></td>
                <td className="tuuli-cell-secondary">{datePt(documento.dataDocumento)}</td>
                <td className="tuuli-cell-secondary">{datePt(documento.dataVencimento)}</td>
                <td className="tuuli-cell-primary tuuli-cell-numeric">{tuuliMoney(documento.valorDocumento)} {documento.moedaId}</td>
                <td className="tuuli-cell-secondary tuuli-cell-numeric">{tuuliMoney(documento.valorRecebidoAtivo)} {documento.moedaId}</td>
                <td className="tuuli-cell-primary tuuli-cell-numeric">{tuuliMoney(documento.valorPendente)} {documento.moedaId}</td>
              </tr>
            ))}
            {!loading && contaDocumentosFiltrados.length === 0 && (
              <tr>
                <td colSpan={7}>{contaApenasNaoLiquidados ? "Sem documentos por liquidar." : "Sem documentos na conta corrente."}</td>
              </tr>
            )}
          </tbody>
          {contaTotais.length > 0 && <tfoot className="tuuli-table-footer">{contaTotais.map(([moedaId, totais]) => <tr key={moedaId}><th colSpan={4}>Totais ({moedaId})</th><td className="tuuli-cell-primary tuuli-cell-numeric">{tuuliMoney(totais.total)} {moedaId}</td><td className="tuuli-cell-primary tuuli-cell-numeric">{tuuliMoney(totais.recebido)} {moedaId}</td><td className="tuuli-cell-primary tuuli-cell-numeric">{tuuliMoney(totais.pendente)} {moedaId}</td></tr>)}</tfoot>}
        </table></div>
        {contaDocumentosFiltrados.length > 0 && <div className="fac-list-pagination fac-current-account-pagination tuuli-pagination">
          <span>{contaDocumentosFiltrados.length} {contaDocumentosFiltrados.length === 1 ? "documento" : "documentos"}</span>
          <Paginator first={contaPage * contaPageSize} onPageChange={(event) => { setContaPage(event.page); setContaPageSize(event.rows); }} rows={contaPageSize} rowsPerPageOptions={[10, 20, 50]} totalRecords={contaDocumentosFiltrados.length} />
        </div>}
      </section>
    </div>
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
  if (view === "Documentos") return "Documentos";
  if (view === "Artigos") return "Artigos e serviços";
  if (view === "Tesouraria") return "Recebimentos";
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

function filenameFromDisposition(disposition: string | null) {
  const match = disposition?.match(/filename="?([^";]+)"?/i);
  return match?.[1] ?? null;
}

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function clientColumnClass(key: ClienteColumnKey) {
  if (key === "nome") return "tuuli-cell-primary";
  if (key === "estado") return "tuuli-cell-secondary tuuli-status";
  return "tuuli-cell-secondary";
}

function dashboardMoney(value: number) {
  const [whole, decimal] = money(value).split(",");
  return `${whole.replace(/\B(?=(\d{3})+(?!\d))/g, " ")},${decimal}`;
}

function integer(value: number) {
  return Math.round(Number(value || 0)).toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

function documentsLabel(value: number) {
  return `${integer(value)} ${Number(value) === 1 ? "documento" : "documentos"}`;
}

function referencia(tipo: string, serie: string, numero: number | null) {
  return `${tipo} ${serie}/${numero ?? "rascunho"}`;
}

function datePt(value?: string) {
  if (!value) {
    return "-";
  }
  return value.split("-").reverse().join("/");
}

function statusLabel(value: string) {
  const normalized = value.toLocaleLowerCase("pt-PT");
  return normalized.charAt(0).toLocaleUpperCase("pt-PT") + normalized.slice(1);
}

function viewFromNavigationState(state: unknown): ViewKey | null {
  if (!state || typeof state !== "object" || !("activeView" in state)) return null;
  const view = (state as { activeView?: unknown }).activeView;
  return typeof view === "string" && navigationItems.some((item) => item.label === view) ? view as ViewKey : null;
}

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function monthStartIso() {
  return `${todayIso().slice(0, 7)}-01`;
}

function dashboardPeriodLabel(value: string) {
  if (value.length !== 7) return datePt(value);
  const [year, month] = value.split("-").map(Number);
  return new Intl.DateTimeFormat("pt-PT", { month: "short", year: "numeric" })
    .format(new Date(Date.UTC(year, month - 1, 1)));
}

export default App;
