import { useEffect, useMemo, useState } from "react";
import ArtigosView from "./ArtigosView";
import DocumentosView from "./DocumentosView";
import PendentesView from "./PendentesView";
import ParametrosDocumentoView from "./ParametrosDocumentoView";

type Page<T> = {
  content: T[];
  totalElements: number;
};

type ViewKey = "Dashboard" | "Clientes" | "Documentos" | "Artigos" | "Tesouraria" | "Configuracao";

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

function App() {
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

  const selectedPendente = dashboardData?.pendentes.content[0] ?? null;
  const selectedDocumento = selectedPendente
    ? dashboardData?.comerciais.content.find((documento) => documento.id === selectedPendente.documentoComercialId)
    : null;

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
            <strong>Workspace UI</strong>
            <span>React POC</span>
          </div>
        </div>

        <nav className="fac-menu" aria-label="Navegacao principal">
          {menu.map((item) => (
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
            <p className="fac-eyebrow">FAC Workspace UI</p>
            <h1>{viewTitle(activeView)}</h1>
          </div>
          <div className="fac-topbar-actions">
            <input
              onChange={(event) => setClienteSearch(event.target.value)}
              disabled={activeView === "Configuracao"}
              placeholder={activeView === "Clientes" ? "Pesquisar cliente, NIF ou email" : activeView === "Configuracao" ? "Configuracao da aplicacao" : "Pesquisar documento, cliente ou artigo"}
              type="search"
              value={activeView === "Clientes" ? clienteSearch : ""}
            />
            <button onClick={refreshActiveView} type="button">Atualizar</button>
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
            data={dashboardData}
            error={error}
            loading={loading}
            metrics={metrics}
            recebidoAtivo={recebidoAtivo}
            saldoPendente={saldoPendente}
            selectedDocumento={selectedDocumento}
            selectedPendente={selectedPendente}
          />
        )}

        {error && <p className="fac-message">{error}</p>}
      </section>
    </main>
  );
}

type DashboardViewProps = {
  data: DashboardData | null;
  error: string | null;
  loading: boolean;
  metrics: { label: string; value: string; tone: string }[];
  recebidoAtivo: number;
  saldoPendente: number;
  selectedDocumento?: DocumentoComercial | null;
  selectedPendente?: Pendente | null;
};

function DashboardView({
  data,
  error,
  loading,
  metrics,
  recebidoAtivo,
  saldoPendente,
  selectedDocumento,
  selectedPendente
}: DashboardViewProps) {
  return (
    <>
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Ambiente de trabalho</p>
          <h2>Uma interface calma para faturar, receber e conferir</h2>
          <p>
            Esta prova de conceito ja le dados reais do backend para validar o ritmo visual antes
            de avancarmos para formularios e fluxos de emissao.
          </p>
        </div>
        <div className="fac-hero-card">
          <span>Estado do backend</span>
          <strong>{loading ? "A carregar..." : error ? "Com erro" : "Ligado"}</strong>
          <small>{error ?? "Spring Boot em localhost:8080 via proxy /api"}</small>
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

      <section className="fac-content-grid">
        <article className="fac-panel fac-panel-main">
          <div className="fac-panel-header">
            <div>
              <p className="fac-eyebrow">Conta corrente</p>
              <h2>{selectedPendente ? `Cliente ${selectedPendente.clienteId}` : "Sem pendentes"}</h2>
            </div>
            <button className="fac-soft-button" type="button">Diagnostico backend</button>
          </div>

          <div className="fac-balance-strip">
            <div>
              <span>Original</span>
              <strong>{money(sum(data?.pendentes.content.map((pendente) => pendente.valorDocumento) ?? []))} EUR</strong>
            </div>
            <div>
              <span>Recebido ativo</span>
              <strong>{money(recebidoAtivo)} EUR</strong>
            </div>
            <div>
              <span>Documentos</span>
              <strong>{data?.pendentes.totalElements ?? 0}</strong>
            </div>
            <div>
              <span>Saldo</span>
              <strong>{money(saldoPendente)} EUR</strong>
            </div>
          </div>

          <table className="fac-table">
            <thead>
              <tr>
                <th>Documento</th>
                <th>Cliente</th>
                <th>Estado</th>
                <th>Data</th>
                <th>Valor</th>
                <th>Pendente</th>
              </tr>
            </thead>
            <tbody>
              {(data?.pendentes.content ?? []).map((pendente) => (
                <tr key={pendente.id}>
                  <td>{referencia(pendente.tipoDocumentoId, pendente.serieDocumento, pendente.numeroDocumento)}</td>
                  <td>{pendente.clienteId}</td>
                  <td><span className="fac-status">{estadoPendente(pendente)}</span></td>
                  <td>{datePt(pendente.dataDocumento)}</td>
                  <td>{money(pendente.valorDocumento)} {pendente.moedaId}</td>
                  <td>{money(pendente.valorPendente)} {pendente.moedaId}</td>
                </tr>
              ))}
              {!loading && (data?.pendentes.content.length ?? 0) === 0 && (
                <tr>
                  <td colSpan={6}>Sem pendentes para mostrar.</td>
                </tr>
              )}
            </tbody>
          </table>
        </article>

        <aside className="fac-panel fac-detail">
          <p className="fac-eyebrow">Conferencia</p>
          <h2>{selectedPendente ? referencia(selectedPendente.tipoDocumentoId, selectedPendente.serieDocumento, selectedPendente.numeroDocumento) : "Sem documento"}</h2>
          <dl>
            <div><dt>Estado</dt><dd>{selectedPendente ? estadoPendente(selectedPendente) : "-"}</dd></div>
            <div><dt>Vencimento</dt><dd>{selectedPendente ? datePt(selectedPendente.dataVencimento) : "-"}</dd></div>
            <div><dt>Documento origem</dt><dd>{selectedDocumento?.id ?? "-"}</dd></div>
            <div><dt>Total</dt><dd>{selectedPendente ? `${money(selectedPendente.valorDocumento)} ${selectedPendente.moedaId}` : "-"}</dd></div>
            <div><dt>Pendente</dt><dd>{selectedPendente ? `${money(selectedPendente.valorPendente)} ${selectedPendente.moedaId}` : "-"}</dd></div>
          </dl>
          <button className="fac-primary-button" type="button">Preparar recebimento</button>
          <button className="fac-ghost-button" type="button">Abrir diagnostico</button>
        </aside>
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
  function changeField<K extends keyof ClienteForm>(field: K, value: ClienteForm[K]) {
    onChangeForm({ ...form, [field]: value });
  }

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
            <button className="fac-soft-button" onClick={onOpenEditor} type="button">Novo cliente</button>
          </div>

          <table className="fac-table">
            <thead>
              <tr>
                <th>Codigo</th>
                <th>Nome</th>
                <th>NIF</th>
                <th>Email</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {clientes.map((cliente) => (
                <tr
                  className={cliente.id === selectedClienteId ? "fac-row-selected" : ""}
                  key={cliente.id}
                  onClick={() => onSelectCliente(cliente.id)}
                >
                  <td>{cliente.id}</td>
                  <td>{cliente.nome}</td>
                  <td>{cliente.nif}</td>
                  <td>{cliente.email ?? "-"}</td>
                  <td><span className="fac-status">{cliente.inativo ? "Inativo" : "Ativo"}</span></td>
                </tr>
              ))}
              {!loading && clientes.length === 0 && (
                <tr>
                  <td colSpan={5}>Sem clientes para mostrar.</td>
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
          <button
            className="fac-primary-button"
            disabled={!selectedCliente || loading}
            onClick={() => selectedCliente && onEditCliente(selectedCliente.id)}
            type="button"
          >
            Editar cliente
          </button>
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
  function changeField<K extends keyof ParametrosClienteForm>(field: K, value: ParametrosClienteForm[K]) {
    onChangeForm({ ...form, [field]: value });
  }

  return (
    <>
      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Configuracao</p>
          <h2>Matriz 0 para novos clientes</h2>
          <p>
            Define apenas os valores que devem ser sugeridos na criacao de clientes. Campos vazios
            continuam vazios e nunca alteram clientes existentes.
          </p>
        </div>
        <div className="fac-hero-card">
          <span>Estado da matriz</span>
          <strong>{loading ? "A carregar..." : exists ? "Configurada" : "Ainda vazia"}</strong>
          <small>Registo tecnico unico, separado da tabela de clientes</small>
        </div>
      </section>

      <section className="fac-panel">
        <div className="fac-panel-header">
          <div>
            <p className="fac-eyebrow">Valores sugeridos</p>
            <h2>Matriz 0</h2>
          </div>
          <span className="fac-muted">Todos os campos sao opcionais</span>
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
          <span className="fac-muted">A matriz so e aplicada quando se carrega em Aplicar Matriz 0 no novo cliente.</span>
          <button className="fac-primary-button" disabled={loading} onClick={onSave} type="button">
            {loading ? "A guardar..." : "Guardar Matriz 0"}
          </button>
        </div>
      </section>

      <ParametrosDocumentoView />
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
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(await responseError(response));
  }
  return response.json();
}

async function fetchOptionalJson<T>(url: string): Promise<T | null> {
  const response = await fetch(url);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function sendJson<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
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
  const response = await fetch(url, {
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

function sum(values: number[]) {
  return values.reduce((total, value) => total + Number(value || 0), 0);
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

function estadoPendente(pendente: Pendente) {
  if (Number(pendente.valorPendente || 0) <= 0) {
    return "Liquidado";
  }
  if (pendente.dataVencimento < todayIso()) {
    return "Vencido";
  }
  if (Number(pendente.valorPendente || 0) < Number(pendente.valorDocumento || 0)) {
    return "Parcial";
  }
  return "Aberto";
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
