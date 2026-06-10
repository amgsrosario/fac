import { useEffect, useMemo, useState } from "react";

type Page<T> = {
  content: T[];
  totalElements: number;
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

type DashboardData = {
  comerciais: Page<DocumentoComercial>;
  pendentes: Page<Pendente>;
  financeiros: Page<DocumentoFinanceiro>;
};

const menu = [
  { label: "Dashboard", hint: "Visao geral" },
  { label: "Clientes", hint: "Conta corrente" },
  { label: "Documentos", hint: "Faturacao" },
  { label: "Artigos", hint: "Catalogo" },
  { label: "Tesouraria", hint: "Recebimentos" },
  { label: "Configuracao", hint: "Base FAC" }
];

function App() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadDashboard() {
    setLoading(true);
    setError(null);
    try {
      const [comerciais, pendentes, financeiros] = await Promise.all([
        fetchPage<DocumentoComercial>("/api/documentos-comerciais?size=100&sort=id,desc"),
        fetchPage<Pendente>("/api/pendentes?size=100&sort=id,desc"),
        fetchPage<DocumentoFinanceiro>("/api/documentos-financeiros?size=100&sort=id,desc")
      ]);
      setData({ comerciais, pendentes, financeiros });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar dados.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  const saldoPendente = useMemo(
    () => data?.pendentes.content.reduce((total, pendente) => total + Number(pendente.valorPendente || 0), 0) ?? 0,
    [data]
  );

  const documentosVencidos = useMemo(
    () => data?.pendentes.content.filter((pendente) => pendente.valorPendente > 0 && pendente.dataVencimento < todayIso()).length ?? 0,
    [data]
  );

  const recebidoAtivo = useMemo(
    () => data?.financeiros.content
      .filter((documento) => !documento.anulado)
      .reduce((total, documento) => total + Number(documento.valorPagamentoLiquido || 0), 0) ?? 0,
    [data]
  );

  const selectedPendente = data?.pendentes.content[0] ?? null;
  const selectedDocumento = selectedPendente
    ? data?.comerciais.content.find((documento) => documento.id === selectedPendente.documentoComercialId)
    : null;

  const metrics = [
    { label: "Saldo pendente", value: `${money(saldoPendente)} EUR`, tone: "client" },
    { label: "Documentos vencidos", value: String(documentosVencidos), tone: "document" },
    { label: "Recebido ativo", value: `${money(recebidoAtivo)} EUR`, tone: "treasury" },
    { label: "Documentos comerciais", value: String(data?.comerciais.totalElements ?? 0), tone: "product" }
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
          {menu.map((item, index) => (
            <button className={index === 0 ? "active" : ""} key={item.label}>
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
            <h1>Faturacao simples, clara e operacional</h1>
          </div>
          <div className="fac-topbar-actions">
            <input type="search" placeholder="Pesquisar documento, cliente ou artigo" />
            <button onClick={loadDashboard} type="button">Atualizar</button>
          </div>
        </header>

        <section className="fac-hero">
          <div>
            <p className="fac-eyebrow">Ambiente de trabalho</p>
            <h2>Uma interface calma para faturar, receber e conferir</h2>
            <p>
              Esta prova de conceito ja le dados reais do backend para validar o ritmo visual antes
              de avançarmos para formularios e fluxos de emissao.
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

        {error && <p className="fac-message">{error}</p>}

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
      </section>
    </main>
  );
}

async function fetchPage<T>(url: string): Promise<Page<T>> {
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
