import { useEffect, useMemo, useState } from "react";

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
  inativo: boolean;
  codPostalId?: string;
  paisId?: string;
  moedaId?: string;
  mPagamentoId?: number;
  pPagamentoId?: string;
  rivaId?: string;
  transporteId?: number;
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

  async function refreshActiveView() {
    if (activeView === "Clientes") {
      await loadClientes();
      if (selectedClienteId) {
        await loadContaCorrente(selectedClienteId);
      }
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
            <h1>{activeView === "Clientes" ? "Clientes e conta corrente" : "Faturacao simples, clara e operacional"}</h1>
          </div>
          <div className="fac-topbar-actions">
            <input
              onChange={(event) => setClienteSearch(event.target.value)}
              placeholder={activeView === "Clientes" ? "Pesquisar cliente, NIF ou email" : "Pesquisar documento, cliente ou artigo"}
              type="search"
              value={activeView === "Clientes" ? clienteSearch : ""}
            />
            <button onClick={refreshActiveView} type="button">Atualizar</button>
          </div>
        </header>

        {activeView === "Clientes" ? (
          <ClientesView
            clientes={filteredClientes}
            contaCorrente={contaCorrente}
            contaResumo={contaResumo}
            loading={clientesLoading}
            selectedCliente={selectedCliente}
            selectedClienteId={selectedClienteId}
            onSelectCliente={setSelectedClienteId}
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
  clientes: Cliente[];
  contaCorrente: ContaCorrenteDiagnostico | null;
  contaResumo: ContaCorrenteResumo | null;
  loading: boolean;
  selectedCliente: Cliente | null;
  selectedClienteId: number | null;
  onSelectCliente: (clienteId: number) => void;
};

function ClientesView({
  clientes,
  contaCorrente,
  contaResumo,
  loading,
  selectedCliente,
  selectedClienteId,
  onSelectCliente
}: ClientesViewProps) {
  return (
    <>
      <section className="fac-hero">
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

      <section className="fac-metrics" aria-label="Indicadores de cliente">
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

      <section className="fac-content-grid">
        <article className="fac-panel fac-panel-main">
          <div className="fac-panel-header">
            <div>
              <p className="fac-eyebrow">Consulta</p>
              <h2>Clientes</h2>
            </div>
            <button className="fac-soft-button" disabled type="button">Novo cliente em breve</button>
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
          <button className="fac-primary-button" disabled type="button">Editar cliente em breve</button>
        </aside>
      </section>

      <section className="fac-panel fac-section-panel">
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

async function fetchPage<T>(url: string): Promise<Page<T>> {
  return fetchJson<Page<T>>(url);
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Erro HTTP ${response.status}`);
  }
  return response.json();
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
