const menu = [
  { label: "Dashboard", hint: "Visao geral" },
  { label: "Clientes", hint: "Conta corrente" },
  { label: "Documentos", hint: "Faturacao" },
  { label: "Artigos", hint: "Catalogo" },
  { label: "Tesouraria", hint: "Recebimentos" },
  { label: "Configuracao", hint: "Base FAC" }
];

const metrics = [
  { label: "Saldo pendente", value: "147,60 EUR", tone: "client" },
  { label: "Documentos vencidos", value: "2", tone: "document" },
  { label: "Recebido hoje", value: "123,00 EUR", tone: "treasury" },
  { label: "Diagnosticos", value: "OK", tone: "product" }
];

const documents = [
  { ref: "FT 2026/1", cliente: "Cliente Demonstracao Lda", estado: "Parcial", data: "08/06/2026", valor: "123,00", pendente: "23,00" },
  { ref: "FT 2026/2", cliente: "Cliente Demonstracao Lda", estado: "Aberto", data: "09/06/2026", valor: "124,60", pendente: "124,60" },
  { ref: "RC 2026/3", cliente: "Cliente Demonstracao Lda", estado: "Emitido", data: "10/06/2026", valor: "10,00", pendente: "0,00" }
];

function App() {
  return (
    <main className="fac-shell">
      <aside className="fac-sidebar">
        <div className="fac-brand">
          <div className="fac-brand-mark">FAC</div>
          <div>
            <strong>Workspace UI</strong>
            <span>Prova visual</span>
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
            <button>Atualizar</button>
          </div>
        </header>

        <section className="fac-hero">
          <div>
            <p className="fac-eyebrow">Ambiente de trabalho</p>
            <h2>Uma interface calma para faturar, receber e conferir</h2>
            <p>
              Esta prova de conceito privilegia leitura, baixa fadiga visual e foco no ciclo principal:
              documento comercial, pendente, recebimento e diagnostico.
            </p>
          </div>
          <div className="fac-hero-card">
            <span>Estado do backend</span>
            <strong>Pronto para integrar</strong>
            <small>Spring Boot em localhost:8080 via proxy /api</small>
          </div>
        </section>

        <section className="fac-metrics" aria-label="Indicadores">
          {metrics.map((metric) => (
            <article className={`fac-metric ${metric.tone}`} key={metric.label}>
              <span>{metric.label}</span>
              <strong>{metric.value}</strong>
            </article>
          ))}
        </section>

        <section className="fac-content-grid">
          <article className="fac-panel fac-panel-main">
            <div className="fac-panel-header">
              <div>
                <p className="fac-eyebrow">Conta corrente</p>
                <h2>Cliente Demonstracao Lda</h2>
              </div>
              <button className="fac-soft-button">Diagnostico backend</button>
            </div>

            <div className="fac-balance-strip">
              <div>
                <span>Original</span>
                <strong>247,60 EUR</strong>
              </div>
              <div>
                <span>Recebido ativo</span>
                <strong>100,00 EUR</strong>
              </div>
              <div>
                <span>Anulado historico</span>
                <strong>10,00 EUR</strong>
              </div>
              <div>
                <span>Saldo</span>
                <strong>147,60 EUR</strong>
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
                {documents.map((document) => (
                  <tr key={document.ref}>
                    <td>{document.ref}</td>
                    <td>{document.cliente}</td>
                    <td><span className="fac-status">{document.estado}</span></td>
                    <td>{document.data}</td>
                    <td>{document.valor}</td>
                    <td>{document.pendente}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </article>

          <aside className="fac-panel fac-detail">
            <p className="fac-eyebrow">Conferencia</p>
            <h2>FT 2026/1</h2>
            <dl>
              <div><dt>Estado</dt><dd>Parcial</dd></div>
              <div><dt>Vencimento</dt><dd>08/07/2026</dd></div>
              <div><dt>IVA</dt><dd>23,00 EUR</dd></div>
              <div><dt>Total</dt><dd>123,00 EUR</dd></div>
              <div><dt>Liquidado</dt><dd>100,00 EUR</dd></div>
            </dl>
            <button className="fac-primary-button">Preparar recebimento</button>
            <button className="fac-ghost-button">Abrir diagnostico</button>
          </aside>
        </section>
      </section>
    </main>
  );
}

export default App;
