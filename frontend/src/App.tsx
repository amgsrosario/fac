import { useEffect, useMemo, useState } from "react";
import ArtigosView from "./ArtigosView";
import DocumentosView from "./DocumentosView";
import PendentesView from "./PendentesView";
import ParametrosDocumentoView from "./ParametrosDocumentoView";
import TabelasView from "./TabelasView";
import ListagensView from "./ListagensView";
import AuditoriaView from "./AuditoriaView";
import { apiFetch, AuthSession } from "./api";

type Page<T> = {
  content: T[];
  totalElements: number;
};

type ViewKey = "Dashboard" | "Clientes" | "Documentos" | "Artigos" | "Tesouraria" | "Listagens" | "Auditoria" | "Configuracao";

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

type ParametrosCliente = {
  id: number;
  paisId?: string;
  moedaId?: string;
  rivaId?: string;
  mPagamentoId?: number;
  pPagamentoId?: string;
  transporteId?: number;
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
  modosPagamento: CatalogoNumero[];
  prazosPagamento: CatalogoString[];
  transportes: CatalogoNumero[];
};

type ClienteColumnKey = "id" | "nome" | "nif" | "email" | "tel" | "localidade" | "paisId" | "moedaId" | "rivaId" | "estado";

type ClienteColumn = {
  key: ClienteColumnKey;
  label: string;
  visible: boolean;
};

const CLIENT_COLUMNS_STORAGE = "fac.clientes.colunas";
const DEFAULT_CLIENT_COLUMNS: ClienteColumn[] = [
  { key: "id", label: "Codigo", visible: true },
  { key: "nome", label: "Nome", visible: true },
  { key: "nif", label: "NIF", visible: true },
  { key: "email", label: "Email", visible: true },
  { key: "tel", label: "Telefone", visible: false },
  { key: "localidade", label: "Localidade", visible: false },
  { key: "paisId", label: "Pais", visible: false },
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

const menu: { label: ViewKey; hint: string }[] = [
  { label: "Dashboard", hint: "Visao geral" },
  { label: "Clientes", hint: "Conta corrente" },
  { label: "Documentos", hint: "Faturacao" },
  { label: "Artigos", hint: "Catalogo" },
  { label: "Tesouraria", hint: "Recebimentos" },
  { label: "Listagens", hint: "Consulta e analise" },
  { label: "Auditoria", hint: "Rastreabilidade" },
  { label: "Configuracao", hint: "Base FAC" }
];

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

type AppProps = { currentUser: AuthSession; onLogout: () => void };

function App({ currentUser, onLogout }: AppProps) {
  const [activeView, setActiveView] = useState<ViewKey>("Dashboard");
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
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar dados.");
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
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar clientes.");
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
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar a conta corrente.");
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
      fetchPage<CatalogoNumero>("/api/mpagamentos?size=100&sort=nome,asc"),
      fetchPage<CatalogoString>("/api/p-pagamentos?size=100&sort=nome,asc"),
      fetchPage<CatalogoNumero>("/api/transportes?size=100&sort=nome,asc")
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
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar os catalogos de cliente.");
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
      setError(err instanceof Error ? err.message : "Nao foi possivel abrir o cliente para edicao.");
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
        ? "A Matriz 0 ainda nao foi configurada."
        : "Nao foi possivel aplicar a Matriz 0.");
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
      setClienteNotice(`Cliente ${created.nome} criado com o codigo ${created.id}.`);
    } catch (err) {
      setEditorMessage(err instanceof Error ? err.message : "Nao foi possivel criar o cliente.");
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
      setEditorMessage(err instanceof Error ? err.message : "Nao foi possivel atualizar o cliente.");
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
      setConfigMessage(err instanceof Error ? err.message : "Nao foi possivel carregar a Matriz 0.");
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
      setConfigMessage("Matriz 0 guardada. Sera aplicada apenas quando pedida no novo cliente.");
    } catch (err) {
      setConfigMessage(err instanceof Error ? err.message : "Nao foi possivel guardar a Matriz 0.");
    } finally {
      setConfigLoading(false);
    }
  }

  async function refreshActiveView() {
    if (activeView === "Clientes") {
      await loadClientes();
      if (selectedClienteId) {
        await loadContaCorrente(selectedClienteId);
      }
      return;
    }
    if (activeView === "Configuracao") {
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
    if (selectedClienteId) {
      loadContaCorrente(selectedClienteId);
    }
  }, [selectedClienteId]);

  useEffect(() => {
    if (activeView === "Configuracao") {
      loadParametrosCliente();
    }
  }, [activeView]);

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
            <span>{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "Demo Partner Edition" : "Aplicacao de faturacao"}</span>
          </div>
        </div>

        <nav className="fac-menu" aria-label="Navegacao principal">
          {menu.filter((item) => {
            if (item.label === "Auditoria") return currentUser.permissoes?.includes("AUDITORIA_CONSULTAR");
            if (item.label === "Configuracao") return currentUser.permissoes?.includes("CONFIGURACAO_GERIR");
            return true;
          }).map((item) => (
            <button
              className={activeView === item.label ? "active" : ""}
              key={item.label}
              onClick={() => setActiveView(item.label)}
              type="button"
            >
              <span>{item.label}</span>
              <small>{item.hint}</small>
            </button>
          ))}
        </nav>
      </aside>

      <section className="fac-workspace">
        <header className="fac-topbar">
          <div>
            <p className="fac-eyebrow">{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "FAC Demo Partner Edition · Ambiente de demonstração" : "FAC · Aplicação de faturação"}</p>
            <h1>{viewTitle(activeView)}</h1>
          </div>
          <div className="fac-topbar-actions">
            <div className="fac-current-user"><span>{currentUser.nome}</span><small>{currentUser.papel} · {currentUser.codigo}</small></div>
            <input
              onChange={(event) => setClienteSearch(event.target.value)}
              disabled={activeView === "Configuracao" || activeView === "Listagens"}
              placeholder={activeView === "Clientes" ? "Pesquisar cliente, NIF ou email" : activeView === "Configuracao" ? "Configuracao da aplicacao" : activeView === "Listagens" ? "Pesquisa disponivel dentro da listagem" : "Pesquisar documento, cliente ou artigo"}
              type="search"
              value={activeView === "Clientes" ? clienteSearch : ""}
            />
            {activeView !== "Listagens" && <button onClick={refreshActiveView} type="button">Atualizar</button>}
            <button className="fac-ghost-button" onClick={onLogout} type="button">Sair</button>
          </div>
        </header>

        {activeView === "Clientes" ? (
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
        ) : activeView === "Documentos" ? (
          <DocumentosView />
        ) : activeView === "Artigos" ? (
          <ArtigosView />
        ) : activeView === "Tesouraria" ? (
          <PendentesView />
        ) : activeView === "Listagens" ? (
          <ListagensView />
        ) : activeView === "Auditoria" ? (
          <AuditoriaView />
        ) : activeView === "Configuracao" ? (
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
            onNavigate={setActiveView}
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
          <h2>Faturar, receber e conferir num único percurso</h2>
          <p>
            A interface utiliza dados reais do backend e apresenta o circuito operacional do FAC.
          </p>
        </div>
        <div className="fac-hero-card">
          <span>Estado do backend</span>
          <strong>{loading ? "A carregar..." : error ? "Com erro" : "Ligado"}</strong>
          <small>{error ?? "Serviços operacionais disponíveis"}</small>
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
            <p className="fac-eyebrow">Operacao diaria</p>
            <h2>Continuar o trabalho</h2>
          </div>
          <span className="fac-muted">As consultas detalhadas estao concentradas em Listagens.</span>
        </div>

        <div className="fac-dashboard-action-grid">
          <button onClick={() => onNavigate("Documentos")} type="button">
            <strong>Documentos</strong>
            <span>Criar e acompanhar faturacao</span>
          </button>
          <button onClick={() => onNavigate("Tesouraria")} type="button">
            <strong>Tesouraria</strong>
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
  const [columns, setColumns] = useState<ClienteColumn[]>(loadClientColumns);

  useEffect(() => {
    window.localStorage.setItem(CLIENT_COLUMNS_STORAGE, JSON.stringify(columns));
  }, [columns]);

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
      <section className={`fac-hero ${editorOpen ? "fac-hidden" : ""}`}>
        <div>
          <p className="fac-eyebrow">Clientes</p>
          <h2>Consulta simples com conta corrente integrada</h2>
          <p>
            Este e o primeiro ecran real apos o dashboard: lista clientes do backend e mostra a
            respetiva conta corrente sem criar ainda formularios complexos.
          </p>
        </div>
        <div className="fac-hero-card">
          <span>Cliente selecionado</span>
          <strong>{selectedCliente?.nome ?? (loading ? "A carregar..." : "Sem cliente")}</strong>
          <small>{selectedCliente ? `NIF ${selectedCliente.nif}` : "Escolhe um cliente na lista"}</small>
        </div>
      </section>

      <section className={`fac-metrics ${editorOpen ? "fac-hidden" : ""}`} aria-label="Indicadores de cliente">
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

      <section className={`fac-content-grid ${editorOpen ? "fac-hidden" : ""}`}>
        <article className="fac-panel fac-panel-main">
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
              <div><strong>Colunas da listagem</strong><span>Marca os campos visiveis e define a respetiva ordem.</span></div>
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

        <aside className="fac-panel fac-detail">
          <p className="fac-eyebrow">Ficha resumida</p>
          <h2>{selectedCliente?.nome ?? "Sem cliente"}</h2>
          <dl>
            <div><dt>Codigo</dt><dd>{selectedCliente?.id ?? "-"}</dd></div>
            <div><dt>NIF</dt><dd>{selectedCliente?.nif ?? "-"}</dd></div>
            <div><dt>Morada</dt><dd>{selectedCliente?.morada ?? "-"}</dd></div>
            <div><dt>Localidade</dt><dd>{selectedCliente?.localidade ?? "-"}</dd></div>
            <div><dt>Codigo postal</dt><dd>{selectedCliente?.codPostalId ?? "-"}</dd></div>
            <div><dt>Pais</dt><dd>{selectedCliente?.paisId ?? "-"}</dd></div>
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

          <div className="fac-form-grid">
            <Field label="Nome"><input maxLength={80} onChange={(event) => changeField("nome", event.target.value)} value={form.nome} /></Field>
            <Field label="NIF"><input maxLength={9} onChange={(event) => changeField("nif", event.target.value)} value={form.nif} /></Field>
            <Field label="Email"><input maxLength={120} onChange={(event) => changeField("email", event.target.value)} type="email" value={form.email} /></Field>
            <Field label="Segundo email"><input maxLength={120} onChange={(event) => changeField("email1", event.target.value)} type="email" value={form.email1} /></Field>
            <Field label="Telefone"><input maxLength={20} onChange={(event) => changeField("tel", event.target.value)} value={form.tel} /></Field>
            <Field label="Telemovel"><input maxLength={20} onChange={(event) => changeField("tm", event.target.value)} value={form.tm} /></Field>
            <Field label="Morada"><input maxLength={60} onChange={(event) => changeField("morada", event.target.value)} value={form.morada} /></Field>
            <Field label="Morada complementar"><input maxLength={60} onChange={(event) => changeField("morada1", event.target.value)} value={form.morada1} /></Field>
            <Field label="Codigo postal"><input onChange={(event) => changeField("codPostalId", event.target.value)} value={form.codPostalId} /></Field>
            <Field label="Localidade"><input maxLength={50} onChange={(event) => changeField("localidade", event.target.value)} value={form.localidade} /></Field>
            <Field label="Pais">
              <select onChange={(event) => changeField("paisId", event.target.value)} value={form.paisId}>
                <option value="">Sem valor</option>
                {catalogos?.paises.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
              </select>
            </Field>
            <Field label="Moeda">
              <select onChange={(event) => changeField("moedaId", event.target.value)} value={form.moedaId}>
                <option value="">Sem valor</option>
                {catalogos?.moedas.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
              </select>
            </Field>
            <Field label="Regime de IVA">
              <select onChange={(event) => changeField("rivaId", event.target.value)} value={form.rivaId}>
                <option value="">Sem valor</option>
                {catalogos?.regimesIva.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
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
            <Field label="Transporte">
              <select onChange={(event) => changeField("transporteId", event.target.value)} value={form.transporteId}>
                <option value="">Sem valor</option>
                {catalogos?.transportes.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
              </select>
            </Field>
            <Field label="TSPIVA"><input maxLength={20} onChange={(event) => changeField("tspiva", event.target.value)} value={form.tspiva} /></Field>
            <Field label="IBAN"><input maxLength={34} onChange={(event) => changeField("iban", event.target.value)} value={form.iban} /></Field>
            <label className="fac-check-field">
              <input checked={form.retencao} onChange={(event) => changeField("retencao", event.target.checked)} type="checkbox" />
              <span>Cliente sujeito a retencao</span>
            </label>
            <label className="fac-check-field">
              <input checked={form.inativo} onChange={(event) => changeField("inativo", event.target.checked)} type="checkbox" />
              <span>Cliente inativo</span>
            </label>
            <Field label="Observacoes"><textarea maxLength={300} onChange={(event) => changeField("observacoes", event.target.value)} value={form.observacoes} /></Field>
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
  const [area, setArea] = useState<"EMPRESA" | "PARAMETROS" | "TABELAS">("PARAMETROS");

  function changeField<K extends keyof ParametrosClienteForm>(field: K, value: ParametrosClienteForm[K]) {
    onChangeForm({ ...form, [field]: value });
  }

  return (
    <>
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Configuracao</p>
          <h2>Base de funcionamento do FAC</h2>
          <p>Dados da empresa, valores sugeridos e tabelas de apoio, separados das operacoes diarias.</p>
        </div>
        <div className="fac-hero-card">
          <span>Area atual</span>
          <strong>{area === "EMPRESA" ? "Empresa" : area === "PARAMETROS" ? "Parametros" : "Tabelas"}</strong>
          <small>Configuracao simples, explicita e centralizada</small>
        </div>
      </section>

      <nav aria-label="Areas de configuracao" className="fac-config-nav">
        <button className={area === "EMPRESA" ? "active" : ""} onClick={() => setArea("EMPRESA")} type="button"><strong>Empresa</strong><span>Identificacao e dados fiscais</span></button>
        <button className={area === "PARAMETROS" ? "active" : ""} onClick={() => setArea("PARAMETROS")} type="button"><strong>Parametros</strong><span>Valores sugeridos da aplicacao</span></button>
        <button className={area === "TABELAS" ? "active" : ""} onClick={() => setArea("TABELAS")} type="button"><strong>Tabelas</strong><span>Catalogos de apoio</span></button>
      </nav>

      {area === "EMPRESA" && <section className="fac-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Empresa proprietaria</p><h2>Identificacao da entidade emissora</h2></div>
          <span className="fac-status">Proximo editor</span>
        </div>
        <p className="fac-muted">Esta area concentrara os dados legais, fiscais e de contacto usados nos documentos. O editor sera ligado diretamente a ficha unica da empresa.</p>
      </section>}

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
          <Field label="Pais">
            <select onChange={(event) => changeField("paisId", event.target.value)} value={form.paisId}>
              <option value="">Nao sugerir</option>
              {catalogos?.paises.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Moeda">
            <select onChange={(event) => changeField("moedaId", event.target.value)} value={form.moedaId}>
              <option value="">Nao sugerir</option>
              {catalogos?.moedas.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Regime de IVA">
            <select onChange={(event) => changeField("rivaId", event.target.value)} value={form.rivaId}>
              <option value="">Nao sugerir</option>
              {catalogos?.regimesIva.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Modo de pagamento">
            <select onChange={(event) => changeField("mPagamentoId", event.target.value)} value={form.mPagamentoId}>
              <option value="">Nao sugerir</option>
              {catalogos?.modosPagamento.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Prazo de pagamento">
            <select onChange={(event) => changeField("pPagamentoId", event.target.value)} value={form.pPagamentoId}>
              <option value="">Nao sugerir</option>
              {catalogos?.prazosPagamento.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Transporte">
            <select onChange={(event) => changeField("transporteId", event.target.value)} value={form.transporteId}>
              <option value="">Nao sugerir</option>
              {catalogos?.transportes.map((item) => <option key={item.id} value={item.id}>{item.nome}</option>)}
            </select>
          </Field>
          <Field label="Retencao">
            <select onChange={(event) => changeField("retencao", event.target.value as ParametrosClienteForm["retencao"])} value={form.retencao}>
              <option value="">Nao sugerir</option>
              <option value="false">Nao</option>
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
  if (!form.nome.trim()) return "O nome e obrigatorio.";
  if (!/^\d{9}$/.test(form.nif.trim())) return "O NIF deve ter exatamente 9 algarismos.";
  if (!form.email.trim() || !/^\S+@\S+\.\S+$/.test(form.email.trim())) return "Indica um email valido.";
  if (!form.morada.trim()) return "A morada e obrigatoria.";
  if (!form.codPostalId.trim()) return "O codigo postal e obrigatorio.";
  if (!form.paisId) return "O pais e obrigatorio.";
  if (!form.moedaId) return "A moeda e obrigatoria.";
  if (!form.transporteId) return "O transporte e obrigatorio.";
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
    mPagamentoId: numberOrNull(form.mPagamentoId),
    pPagamentoId: blankToNull(form.pPagamentoId),
    transporteId: numberOrNull(form.transporteId),
    retencao: form.retencao === "" ? null : form.retencao === "true"
  };
}

function viewTitle(view: ViewKey) {
  if (view === "Clientes") return "Clientes e conta corrente";
  if (view === "Listagens") return "Listagens e analise operacional";
  if (view === "Auditoria") return "Auditoria fiscal";
  if (view === "Configuracao") return "Configuracao simples e explicita";
  return "Faturacao simples, clara e operacional";
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
    mPagamentoId: numberOrNull(form.mPagamentoId),
    pPagamentoId: blankToNull(form.pPagamentoId),
    rivaId: blankToNull(form.rivaId),
    transporteId: numberOrNull(form.transporteId)
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

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export default App;
