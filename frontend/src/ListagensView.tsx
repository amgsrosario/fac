import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";

type Page<T> = { content: T[]; totalElements: number };
type SourceKey = "comerciais" | "linhasComerciais" | "financeiros" | "linhasFinanceiras" | "relacaoComercial" | "relacaoFinanceira" | "extratoCliente";

type DocumentoComercial = {
  id: number; tipoDocumentoId: string; serie: string; numeroDocumento: number | null; estado: string;
  dataEmissao: string; dataVencimento?: string; clienteId: number; clienteNome: string; clienteNif: string;
  moedaId: string; valorBruto: number; valorDesconto: number; valorIvaTotal: number; valorRetencao: number;
  valorTotal: number; emissorId?: string; momentoEmissao?: string; anulado: boolean; impresso: boolean; liquidado: boolean;
};
type LinhaComercial = {
  id: number; documentoComercialId: number; numeroLinha: number; artigoId: string; descricao: string;
  quantidade: number; precoUnitario: number; valorBruto: number; valorDesconto: number; valorLinha: number;
  tipoTaxaIvaId: string; percentagemIva: number; peso: number;
};
type LinhaFinanceira = {
  id: number; numeroLinha: number; pendenteId: number; dataDocumento: string; dataVencimento: string;
  tipoDocumentoId: string; numeroDocumento: number; serieDocumento: string; valorDocumento: number;
  valorPendenteAntes: number; valorALiquidar: number; descontoPercentual: number; descontoValor: number;
  valorPagamentoLiquido: number; novoValorPendente: number; moedaId: string;
};
type DocumentoFinanceiro = {
  id: number; clienteId: number; tipoDocumentoId: string; serie: string; numeroDocumento: number;
  dataEmissao: string; moedaId: string; valorPagamentoBruto: number; valorDescontoFinanceiro: number;
  valorPagamentoLiquido: number; mPagamentoId: number; emissorId: string; anulado: boolean; impresso: boolean;
  momentoEmissao?: string; linhas: LinhaFinanceira[];
};
type LinhaComercialListagem = LinhaComercial & { documento: DocumentoComercial };
type LinhaFinanceiraListagem = LinhaFinanceira & { documento: DocumentoFinanceiro };
type ClienteOption = { id: number; nome: string; nif: string };
type ExtratoTotais = { debito: number; credito: number; saldo: number };
type ExtratoMovimento = {
  id: number; origem: "COMERCIAL" | "FINANCEIRO"; data: string; momento?: string;
  tipoDocumentoId: string; serie: string; numeroDocumento: number; descricao: string;
  dataVencimento?: string; debito: number; credito: number; saldoAcumulado: number;
};
type ExtratoMoeda = {
  moedaId: string; anterior: ExtratoTotais; movimentos: ExtratoMovimento[];
  totalPeriodo: ExtratoTotais; totalFinal: ExtratoTotais;
};
type ExtratoCliente = {
  clienteId: number; clienteNome: string; clienteNif: string; dataInicial: string; dataFinal: string;
  geradoEm: string; moedas: ExtratoMoeda[];
};

const SOURCES: { key?: SourceKey; label: string; description: string }[] = [
  { key: "comerciais", label: "Documentos comerciais", description: "Uma linha por cabecalho comercial" },
  { key: "linhasComerciais", label: "Linhas comerciais", description: "Cabecalho e detalhe de artigos" },
  { key: "financeiros", label: "Documentos financeiros", description: "Uma linha por recebimento" },
  { key: "linhasFinanceiras", label: "Linhas financeiras", description: "Documentos liquidados por recebimento" },
  { key: "relacaoComercial", label: "Listagem comercial", description: "Cabecalho e linhas na mesma consulta" },
  { key: "relacaoFinanceira", label: "Listagem financeira", description: "Recebimento e liquidacoes na mesma consulta" },
  { key: "extratoCliente", label: "Extrato historico de cliente", description: "Faturas, recibos e saldo acumulado" }
];

const COLUMNS: Record<SourceKey, ConfigurableColumn[]> = {
  comerciais: [
    c("documento", "Documento", true), c("cliente", "Cliente", true), c("nif", "NIF"), c("emissao", "Emissao", true),
    c("vencimento", "Vencimento"), c("moeda", "Moeda"), c("bruto", "Bruto"), c("desconto", "Desconto"),
    c("iva", "IVA"), c("retencao", "Retencao"), c("total", "Total", true), c("estado", "Estado", true),
    c("impresso", "Impresso"), c("liquidado", "Liquidado"), c("emissor", "Emissor")
  ],
  linhasComerciais: [
    c("documento", "Documento", true), c("emissao", "Emissao"), c("cliente", "Cliente", true), c("nif", "NIF"),
    c("linha", "Linha", true), c("artigo", "Artigo", true), c("descricao", "Descricao", true), c("quantidade", "Quantidade", true),
    c("preco", "Preco unitario", true), c("bruto", "Bruto"), c("desconto", "Desconto"), c("liquido", "Valor linha", true),
    c("tipoIva", "Tipo IVA"), c("taxaIva", "Taxa IVA"), c("peso", "Peso"), c("moeda", "Moeda")
  ],
  financeiros: [
    c("documento", "Documento", true), c("cliente", "Cliente", true), c("data", "Data", true), c("moeda", "Moeda"),
    c("modo", "Modo pagamento"), c("bruto", "Valor aplicado"), c("desconto", "Desconto"), c("liquido", "Recebido", true),
    c("emissor", "Emissor"), c("estado", "Estado", true), c("impresso", "Impresso")
  ],
  linhasFinanceiras: [
    c("recibo", "Recebimento", true), c("cliente", "Cliente"), c("dataRecibo", "Data recebimento"),
    c("documento", "Documento liquidado", true), c("emissao", "Emissao"), c("vencimento", "Vencimento"),
    c("valorDocumento", "Valor documento"), c("pendenteAntes", "Pendente antes", true), c("liquidado", "Valor liquidado", true),
    c("desconto", "Desconto"), c("recebido", "Recebido", true), c("novoPendente", "Novo pendente", true), c("moeda", "Moeda")
  ],
  relacaoComercial: [
    c("documento", "Documento", true), c("estado", "Estado", true), c("emissao", "Emissao", true),
    c("vencimento", "Vencimento"), c("cliente", "Cliente", true), c("nif", "NIF"), c("moeda", "Moeda"),
    c("totalDocumento", "Total documento", true), c("liquidadoDocumento", "Liquidado"), c("linha", "Linha", true),
    c("artigo", "Artigo", true), c("descricao", "Descricao", true), c("quantidade", "Quantidade", true),
    c("preco", "Preco unitario", true), c("brutoLinha", "Bruto linha"), c("descontoLinha", "Desconto linha"),
    c("valorLinha", "Valor linha", true), c("tipoIva", "Tipo IVA"), c("taxaIva", "Taxa IVA"), c("peso", "Peso")
  ],
  relacaoFinanceira: [
    c("recibo", "Recebimento", true), c("estado", "Estado", true), c("dataRecibo", "Data recebimento", true),
    c("cliente", "Cliente", true), c("modo", "Modo pagamento"), c("totalRecebido", "Total recebido", true),
    c("documento", "Documento liquidado", true), c("emissao", "Emissao"), c("vencimento", "Vencimento"),
    c("valorDocumento", "Valor documento"), c("pendenteAntes", "Pendente antes", true),
    c("valorLiquidado", "Valor liquidado", true), c("descontoLinha", "Desconto"),
    c("recebidoLinha", "Recebido", true), c("novoPendente", "Novo pendente", true), c("moeda", "Moeda")
  ],
  extratoCliente: [
    c("data", "Data", true), c("documento", "Documento", true), c("descricao", "Descricao", true),
    c("vencimento", "Vencimento"), c("debito", "Debito", true), c("credito", "Credito", true),
    c("saldo", "Saldo", true), c("moeda", "Moeda", true)
  ]
};

export default function ListagensView() {
  const [source, setSource] = useState<SourceKey>("comerciais");
  const [comerciais, setComerciais] = useState<DocumentoComercial[]>([]);
  const [financeiros, setFinanceiros] = useState<DocumentoFinanceiro[]>([]);
  const [linhasComerciais, setLinhasComerciais] = useState<LinhaComercialListagem[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [columnsOpen, setColumnsOpen] = useState(false);
  const [clientesExtrato, setClientesExtrato] = useState<ClienteOption[]>([]);
  const [extratoClienteId, setExtratoClienteId] = useState("");
  const [extratoDataInicial, setExtratoDataInicial] = useState("");
  const [extratoDataFinal, setExtratoDataFinal] = useState("");
  const [extrato, setExtrato] = useState<ExtratoCliente | null>(null);
  const [exportingPdf, setExportingPdf] = useState(false);
  const [exportingExcel, setExportingExcel] = useState(false);
  const configured = useConfiguredColumns(`fac.listagens.${source}.colunas`, COLUMNS[source]);

  useEffect(() => { loadSource(source); }, [source]);

  async function loadSource(target: SourceKey) {
    setLoading(true);
    setMessage(null);
    try {
      if (target === "extratoCliente" && clientesExtrato.length === 0) {
        setClientesExtrato((await fetchPage<ClienteOption>("/api/clientes?size=500&sort=nome,asc")).content);
      }
      if (target === "comerciais" && comerciais.length === 0) {
        setComerciais((await fetchPage<DocumentoComercial>("/api/documentos-comerciais?size=500&sort=id,desc")).content);
      }
      if ((target === "linhasComerciais" || target === "relacaoComercial") && linhasComerciais.length === 0) {
        const docs = comerciais.length > 0 ? comerciais : (await fetchPage<DocumentoComercial>("/api/documentos-comerciais?size=500&sort=id,desc")).content;
        setComerciais(docs);
        const details = await Promise.all(docs.map(async (documento) =>
          (await fetchJson<LinhaComercial[]>(`/api/documentos-comerciais/${documento.id}/linhas`))
            .map((linha) => ({ ...linha, documento }))));
        setLinhasComerciais(details.flat());
      }
      if ((target === "financeiros" || target === "linhasFinanceiras" || target === "relacaoFinanceira") && financeiros.length === 0) {
        setFinanceiros((await fetchPage<DocumentoFinanceiro>("/api/documentos-financeiros?size=500&sort=id,desc")).content);
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar a listagem.");
    } finally {
      setLoading(false);
    }
  }

  async function consultarExtrato() {
    setMessage(null);
    setExtrato(null);
    if (!extratoClienteId || !extratoDataInicial || !extratoDataFinal) {
      setMessage("Seleciona o cliente, a data inicial e a data final.");
      return;
    }
    if (extratoDataInicial > extratoDataFinal) {
      setMessage("A data inicial nao pode ser posterior a data final.");
      return;
    }
    setLoading(true);
    try {
      const params = new URLSearchParams({ dataInicial: extratoDataInicial, dataFinal: extratoDataFinal });
      setExtrato(await fetchJson<ExtratoCliente>(`/api/extratos/clientes/${extratoClienteId}?${params}`));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar o extrato.");
    } finally {
      setLoading(false);
    }
  }

  async function exportarExtrato(format: "pdf" | "xlsx") {
    setMessage(null);
    if (!extratoClienteId || !extratoDataInicial || !extratoDataFinal) {
      setMessage("Seleciona o cliente, a data inicial e a data final.");
      return;
    }
    if (extratoDataInicial > extratoDataFinal) {
      setMessage("A data inicial nao pode ser posterior a data final.");
      return;
    }
    const setExporting = format === "pdf" ? setExportingPdf : setExportingExcel;
    setExporting(true);
    try {
      const params = new URLSearchParams({ dataInicial: extratoDataInicial, dataFinal: extratoDataFinal });
      const response = await apiFetch(`/api/extratos/clientes/${extratoClienteId}/exportar/${format}?${params}`);
      if (!response.ok) throw new Error(await responseError(response));
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = downloadFilename(response.headers.get("Content-Disposition"), `extrato-cliente.${format}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel exportar o extrato.");
    } finally {
      setExporting(false);
    }
  }

  const rows = useMemo(() => {
    const base: unknown[] = source === "extratoCliente" ? []
      : source === "comerciais" ? comerciais
      : source === "linhasComerciais" || source === "relacaoComercial" ? linhasComerciais
      : source === "financeiros" ? financeiros
      : financeiros.flatMap((documento) => (documento.linhas ?? []).map((linha) => ({ ...linha, documento })));
    const term = search.trim().toLowerCase();
    if (!term) return base;
    return base.filter((row) => searchText(source, row).includes(term));
  }, [source, comerciais, linhasComerciais, financeiros, search]);

  return <>
    <section className="fac-hero">
      <div><p className="fac-eyebrow">Listagens</p><h2>Consulta transversal dos dados do FAC</h2><p>Escolhe uma fonte, define as colunas necessarias e consulta cabecalhos ou linhas sem interferir com a operacao diaria.</p></div>
      <div className="fac-hero-card"><span>Fonte atual</span><strong>{SOURCES.find((item) => item.key === source)?.label}</strong><small>{loading ? "A carregar..." : source === "extratoCliente" ? extrato ? `${extrato.moedas.reduce((total, moeda) => total + moeda.movimentos.length, 0)} movimentos` : "A aguardar consulta" : `${rows.length} registos`}</small></div>
    </section>

    <section className="fac-report-source-grid">
      {SOURCES.map((item, index) => <button className={item.key === source ? "active" : ""} disabled={!item.key} key={`${item.label}-${index}`} onClick={() => item.key && setSource(item.key)} type="button"><strong>{item.label}</strong><span>{item.description}</span></button>)}
    </section>

    <section className="fac-panel fac-section-panel">
      <div className="fac-panel-header"><div><p className="fac-eyebrow">{SOURCES.find((item) => item.key === source)?.label}</p><h2>Dados disponiveis</h2></div><div className="fac-inline-actions">{source !== "extratoCliente" && <input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar nesta listagem" type="search" value={search}/>}<button className="fac-ghost-button" onClick={() => setColumnsOpen((current) => !current)} type="button">Colunas ({configured.visibleColumns.length})</button><button className="fac-soft-button" disabled={loading} onClick={() => source === "extratoCliente" ? consultarExtrato() : loadSource(source)} type="button">Atualizar</button></div></div>
      {message && <p className="fac-message">{message}</p>}
      {source === "extratoCliente" && <p className="fac-muted">Extrato oficial calculado pelo backend. Os documentos anulados nao integram os movimentos contabilisticos e cada moeda e apresentada separadamente.</p>}
      {source === "extratoCliente" && <div className="fac-extrato-filters">
        <label><span>Cliente</span><select onChange={(event) => { setExtratoClienteId(event.target.value); setExtrato(null); }} value={extratoClienteId}><option value="">Selecionar cliente</option>{clientesExtrato.map((cliente) => <option key={cliente.id} value={cliente.id}>{cliente.id} - {cliente.nome}</option>)}</select></label>
        <label><span>Data inicial</span><input onChange={(event) => setExtratoDataInicial(event.target.value)} type="date" value={extratoDataInicial}/></label>
        <label><span>Data final</span><input onChange={(event) => setExtratoDataFinal(event.target.value)} type="date" value={extratoDataFinal}/></label>
        <button className="fac-primary-button" disabled={loading} onClick={consultarExtrato} type="button">Consultar extrato</button>
        <button className="fac-soft-button" disabled={exportingPdf || exportingExcel} onClick={() => exportarExtrato("pdf")} type="button">{exportingPdf ? "A gerar PDF..." : "Exportar PDF"}</button>
        <button className="fac-soft-button" disabled={exportingPdf || exportingExcel} onClick={() => exportarExtrato("xlsx")} type="button">{exportingExcel ? "A gerar Excel..." : "Exportar Excel"}</button>
        <button className="fac-ghost-button" onClick={() => { setExtratoClienteId(""); setExtratoDataInicial(""); setExtratoDataFinal(""); setExtrato(null); setMessage(null); }} type="button">Limpar</button>
      </div>}
      <ColumnSelector columns={configured.columns} open={columnsOpen} onMove={configured.moveColumn} onReset={configured.resetColumns} onToggle={configured.toggleColumn}/>
      {source === "extratoCliente" && <ExtratoTable extrato={extrato} loading={loading} columns={configured.visibleColumns}/>}
      {source !== "extratoCliente" &&
      <div className="fac-table-scroll"><table className="fac-table"><thead><tr>{configured.visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>
        {rows.map((row, index) => <tr key={rowKey(source, row, index)}>{configured.visibleColumns.map((column) => <td key={column.key}>{cellValue(source, row, column.key)}</td>)}</tr>)}
        {!loading && rows.length === 0 && <tr><td colSpan={configured.visibleColumns.length}>Sem registos para mostrar.</td></tr>}
      </tbody></table></div>}
    </section>
  </>;
}

function ExtratoTable({ extrato, loading, columns }: { extrato: ExtratoCliente | null; loading: boolean; columns: ConfigurableColumn[] }) {
  if (loading) return <p className="fac-empty-state">A calcular o extrato...</p>;
  if (!extrato) return <p className="fac-empty-state">Seleciona um cliente e um intervalo de datas para consultar o extrato.</p>;

  return <div className="fac-extrato-result">
    <header className="fac-extrato-header">
      <div><span>Cliente {extrato.clienteId}</span><strong>{extrato.clienteNome}</strong><small>NIF {extrato.clienteNif || "-"}</small></div>
      <div><span>Periodo</span><strong>{datePt(extrato.dataInicial)} a {datePt(extrato.dataFinal)}</strong><small>Gerado em {dateTimePt(extrato.geradoEm)}</small></div>
    </header>
    {extrato.moedas.map((moeda) => <section className="fac-extrato-moeda" key={moeda.moedaId}>
      <div className="fac-extrato-moeda-title"><strong>{moeda.moedaId}</strong><span>{moeda.movimentos.length} movimentos no periodo</span></div>
      <div className="fac-table-scroll"><table className="fac-table"><thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>
        <ExtratoTotalRow className="fac-extrato-anterior" label="Anterior" moeda={moeda.moedaId} totals={moeda.anterior} columns={columns}/>
        {moeda.movimentos.map((movimento) => <tr key={`${movimento.origem}-${movimento.id}`}>{columns.map((column) => <td key={column.key}>{extratoMovementCell(movimento, moeda.moedaId, column.key)}</td>)}</tr>)}
        {moeda.movimentos.length === 0 && <tr><td className="fac-extrato-empty" colSpan={columns.length}>Sem movimentos no periodo selecionado.</td></tr>}
        <ExtratoTotalRow className="fac-extrato-periodo" label="Total do periodo" moeda={moeda.moedaId} totals={moeda.totalPeriodo} columns={columns}/>
        <ExtratoTotalRow className="fac-extrato-total" label="Total final" moeda={moeda.moedaId} totals={moeda.totalFinal} columns={columns}/>
      </tbody></table></div>
    </section>)}
  </div>;
}

function ExtratoTotalRow({ className, label, moeda, totals, columns }: { className: string; label: string; moeda: string; totals: ExtratoTotais; columns: ConfigurableColumn[] }) {
  return <tr className={className}>{columns.map((column, index) => <td key={column.key}>{extratoTotalValue(column.key, index, label, moeda, totals)}</td>)}</tr>;
}

function extratoMovementCell(movimento: ExtratoMovimento, moedaId: string, key: string) {
  const values: Record<string, React.ReactNode> = {
    data: datePt(movimento.data),
    documento: reference(movimento.tipoDocumentoId, movimento.serie, movimento.numeroDocumento),
    descricao: movimento.descricao,
    vencimento: datePt(movimento.dataVencimento),
    debito: money(movimento.debito),
    credito: money(movimento.credito),
    saldo: <strong>{money(movimento.saldoAcumulado)}</strong>,
    moeda: moedaId
  };
  return values[key] ?? "-";
}

function extratoTotalValue(key: string, columnIndex: number, label: string, moeda: string, totals: ExtratoTotais) {
  if (key === "debito") return <strong>{money(totals.debito)}</strong>;
  if (key === "credito") return <strong>{money(totals.credito)}</strong>;
  if (key === "saldo") return <strong>{money(totals.saldo)}</strong>;
  if (key === "moeda") return moeda;
  if (columnIndex === 0) return <strong>{label}</strong>;
  return "";
}

function c(key: string, label: string, visible = false): ConfigurableColumn { return { key, label, visible }; }
function reference(tipo: string, serie: string, numero: number | null) { return `${tipo} ${serie}/${numero ?? "rascunho"}`; }
function datePt(value?: string) { return value ? value.split("-").reverse().join("/") : "-"; }
function dateTimePt(value?: string) { return value ? new Date(value).toLocaleString("pt-PT") : "-"; }
function money(value: number) { return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function decimal(value: number) { return Number(value || 0).toLocaleString("pt-PT", { maximumFractionDigits: 6 }); }
function yesNo(value: boolean) { return value ? "Sim" : "Nao"; }
function statusComercial(d: DocumentoComercial) { return d.anulado ? "ANULADO" : d.estado; }

function cellValue(source: SourceKey, raw: unknown, key: string) {
  if (source === "comerciais") {
    const d = raw as DocumentoComercial;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), cliente: d.clienteNome, nif: d.clienteNif, emissao: datePt(d.dataEmissao), vencimento: datePt(d.dataVencimento), moeda: d.moedaId, bruto: money(d.valorBruto), desconto: money(d.valorDesconto), iva: money(d.valorIvaTotal), retencao: money(d.valorRetencao), total: `${money(d.valorTotal)} ${d.moedaId}`, estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{statusComercial(d)}</span>, impresso: yesNo(d.impresso), liquidado: yesNo(d.liquidado), emissor: d.emissorId ?? "-" };
    return values[key] ?? "-";
  }
  if (source === "linhasComerciais") {
    const l = raw as LinhaComercialListagem; const d = l.documento;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), emissao: datePt(d.dataEmissao), cliente: d.clienteNome, nif: d.clienteNif, linha: l.numeroLinha, artigo: l.artigoId, descricao: l.descricao, quantidade: decimal(l.quantidade), preco: money(l.precoUnitario), bruto: money(l.valorBruto), desconto: money(l.valorDesconto), liquido: money(l.valorLinha), tipoIva: l.tipoTaxaIvaId, taxaIva: `${decimal(l.percentagemIva)}%`, peso: decimal(l.peso), moeda: d.moedaId };
    return values[key] ?? "-";
  }
  if (source === "relacaoComercial") {
    const l = raw as LinhaComercialListagem; const d = l.documento;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{statusComercial(d)}</span>, emissao: datePt(d.dataEmissao), vencimento: datePt(d.dataVencimento), cliente: d.clienteNome, nif: d.clienteNif, moeda: d.moedaId, totalDocumento: money(d.valorTotal), liquidadoDocumento: yesNo(d.liquidado), linha: l.numeroLinha, artigo: l.artigoId, descricao: l.descricao, quantidade: decimal(l.quantidade), preco: money(l.precoUnitario), brutoLinha: money(l.valorBruto), descontoLinha: money(l.valorDesconto), valorLinha: money(l.valorLinha), tipoIva: l.tipoTaxaIvaId, taxaIva: `${decimal(l.percentagemIva)}%`, peso: decimal(l.peso) };
    return values[key] ?? "-";
  }
  if (source === "financeiros") {
    const d = raw as DocumentoFinanceiro;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), cliente: d.clienteId, data: datePt(d.dataEmissao), moeda: d.moedaId, modo: d.mPagamentoId, bruto: money(d.valorPagamentoBruto), desconto: money(d.valorDescontoFinanceiro), liquido: `${money(d.valorPagamentoLiquido)} ${d.moedaId}`, emissor: d.emissorId, estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{d.anulado ? "ANULADO" : "EMITIDO"}</span>, impresso: yesNo(d.impresso) };
    return values[key] ?? "-";
  }
  const l = raw as LinhaFinanceiraListagem; const d = l.documento;
  const values: Record<string, React.ReactNode> = source === "relacaoFinanceira"
    ? { recibo: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{d.anulado ? "ANULADO" : "EMITIDO"}</span>, dataRecibo: datePt(d.dataEmissao), cliente: d.clienteId, modo: d.mPagamentoId, totalRecebido: money(d.valorPagamentoLiquido), documento: reference(l.tipoDocumentoId, l.serieDocumento, l.numeroDocumento), emissao: datePt(l.dataDocumento), vencimento: datePt(l.dataVencimento), valorDocumento: money(l.valorDocumento), pendenteAntes: money(l.valorPendenteAntes), valorLiquidado: money(l.valorALiquidar), descontoLinha: money(l.descontoValor), recebidoLinha: money(l.valorPagamentoLiquido), novoPendente: money(l.novoValorPendente), moeda: l.moedaId }
    : { recibo: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), cliente: d.clienteId, dataRecibo: datePt(d.dataEmissao), documento: reference(l.tipoDocumentoId, l.serieDocumento, l.numeroDocumento), emissao: datePt(l.dataDocumento), vencimento: datePt(l.dataVencimento), valorDocumento: money(l.valorDocumento), pendenteAntes: money(l.valorPendenteAntes), liquidado: money(l.valorALiquidar), desconto: money(l.descontoValor), recebido: money(l.valorPagamentoLiquido), novoPendente: money(l.novoValorPendente), moeda: l.moedaId };
  return values[key] ?? "-";
}

function searchText(source: SourceKey, row: unknown) {
  if (source === "comerciais") { const d = row as DocumentoComercial; return `${reference(d.tipoDocumentoId, d.serie, d.numeroDocumento)} ${d.clienteNome} ${d.clienteNif} ${statusComercial(d)}`.toLowerCase(); }
  if (source === "linhasComerciais" || source === "relacaoComercial") { const l = row as LinhaComercialListagem; return `${reference(l.documento.tipoDocumentoId, l.documento.serie, l.documento.numeroDocumento)} ${l.documento.clienteNome} ${l.documento.clienteNif} ${l.artigoId} ${l.descricao}`.toLowerCase(); }
  if (source === "financeiros") { const d = row as DocumentoFinanceiro; return `${reference(d.tipoDocumentoId, d.serie, d.numeroDocumento)} ${d.clienteId} ${d.emissorId}`.toLowerCase(); }
  const l = row as LinhaFinanceiraListagem; return `${reference(l.documento.tipoDocumentoId, l.documento.serie, l.documento.numeroDocumento)} ${reference(l.tipoDocumentoId, l.serieDocumento, l.numeroDocumento)} ${l.documento.clienteId}`.toLowerCase();
}

function rowKey(source: SourceKey, row: unknown, index: number) {
  if (source === "comerciais" || source === "financeiros") return (row as { id: number }).id;
  return `${source}-${(row as { id: number }).id}-${index}`;
}

async function fetchPage<T>(url: string): Promise<Page<T>> { return fetchJson<Page<T>>(url); }
async function fetchJson<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function downloadFilename(contentDisposition: string | null, fallback: string) {
  if (!contentDisposition) return fallback;
  const encoded = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
  if (encoded) return decodeURIComponent(encoded.replace(/^"|"$/g, ""));
  return contentDisposition.match(/filename="?([^";]+)"?/i)?.[1] ?? fallback;
}
