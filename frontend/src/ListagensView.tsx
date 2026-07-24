import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { apiFetch } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";
import { currentYearDateRange } from "./dateFilters";
import { MultiSelectFilter, MultiSelectOption } from "./MultiSelectFilter";

type Page<T> = { content: T[]; totalElements: number; totalPages?: number };
type SourceKey = "pendentesAData" | "pendentes" | "comerciais" | "linhasComerciais" | "financeiros" | "linhasFinanceiras" | "relacaoComercial" | "relacaoFinanceira" | "extratoCliente";

type DocumentoComercial = {
  id: number; tipoDocumentoId: string; serie: string; numeroDocumento: number | null; estado: string;
  dataEmissao: string; dataVencimento?: string; clienteId: number; clienteNome: string; clienteNif: string;
  moedaId: string; valorBruto: number; valorDesconto: number; valorIvaTotal: number; valorRetencao: number;
  valorTotal: number; emissorId?: string; momentoEmissao?: string; anulado: boolean; impresso: boolean; liquidado: boolean;
  valorLiquido?: number;
};
type LinhaComercial = {
  id: number; documentoComercialId: number; numeroLinha: number; tipoLinha: string; artigoId?: string | null; descricao: string;
  quantidade: number; precoUnitario: number; valorBruto: number; valorDesconto: number; valorLinha: number;
  tipoTaxaIvaId?: string | null; percentagemIva: number; peso: number; unidade?: string | null; valorImposto?: number; totalLinha?: number;
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
  valorPagamentoLiquido: number; mPagamentoId: string; emissorId: string; anulado: boolean; impresso: boolean;
  momentoEmissao?: string; linhas: LinhaFinanceira[];
};
type LinhaComercialListagem = LinhaComercial & { documento: DocumentoComercial };
type LinhaFinanceiraListagem = LinhaFinanceira & { documento: DocumentoFinanceiro };
type ClienteOption = { id: number; nome: string; nif: string; inativo?: boolean; localidade?: string | null; tel?: string | null; tm?: string | null; email?: string | null; morada?: string | null; codPostalId?: string | null; paisId?: string | null; moedaId?: string | null; mPagamentoId?: string | null; pPagamentoId?: string | null; rivaId?: string | null };
type ArtigoOption = { codigo: string; descricao: string; tipoArtigo?: "ARTIGO" | "SERVICO"; unidade?: string | null; familiaId?: number | null; pvp?: number; ivaVendaId?: string | null; retencao?: boolean; inativo?: boolean; observacoes?: string | null };
type DocumentoComercialResponse = { documento: DocumentoComercial; valorLiquido: number };
type LinhaComercialResponse = { documento: DocumentoComercial; linha: LinhaComercial };
type LinhaFinanceiraResponse = { documento: DocumentoFinanceiro; linha: LinhaFinanceira };
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
type PendenteListagem = {
  documentoId: number; documento: string; data: string; vencimento: string; clienteId: number;
  clienteCodigo: string; clienteNome: string; moedaId: string; total: number; recebido: number; pendente: number;
};
type PendentesResponse = {
  linhas: PendenteListagem[];
  totais: { total: number; recebido: number; pendente: number };
};

const SOURCES: { key?: SourceKey; label: string; description: string }[] = [
  { key: "comerciais", label: "Documentos comerciais", description: "Uma linha por cabeçalho comercial" },
  { key: "linhasComerciais", label: "Detalhe dos documentos comerciais", description: "Cabeçalho e detalhe de artigos" },
  { key: "financeiros", label: "Documentos financeiros", description: "Uma linha por recebimento" },
  { key: "linhasFinanceiras", label: "Detalhe dos documentos financeiros", description: "Documentos liquidados por recebimento" },
  { key: "extratoCliente", label: "Extrato histórico de cliente", description: "Faturas, recibos e saldo acumulado" },
  { key: "pendentes", label: "Todos os pendentes", description: "Documentos por receber, vencidos e nao vencidos" },
  { key: "pendentesAData", label: "Valores pendentes numa data", description: "Situação dos valores por receber" }
];

const COLUMNS: Record<SourceKey, ConfigurableColumn[]> = {
  pendentesAData: [
    c("cliente", "Cliente", true), c("documento", "Documento", true), c("data", "Data", true),
    c("vencimento", "Vencimento", true), c("total", "Total", true), c("recebido", "Recebido", true),
    c("pendente", "Pendente", true)
  ],
  pendentes: [
    c("cliente", "Cliente", true), c("documento", "Documento", true), c("data", "Data", true),
    c("vencimento", "Vencimento", true), c("total", "Total", true), c("recebido", "Recebido", true),
    c("pendente", "Pendente", true)
  ],
  comerciais: [
    c("emissao", "Data", true), c("documento", "Documento", true), c("cliente", "Cliente", true), c("nif", "NIF"),
    c("estado", "Estado", true), c("liquido", "Valor líquido", true), c("iva", "IVA", true), c("total", "Total", true),
    c("vencimento", "Vencimento"), c("moeda", "Moeda"), c("bruto", "Bruto"), c("desconto", "Desconto"), c("retencao", "Retenção"),
    c("impresso", "Impresso"), c("liquidado", "Liquidado"), c("emissor", "Emissor")
  ],
  linhasComerciais: [
    c("documento", "Documento", true), c("emissao", "Emissão"), c("cliente", "Cliente", true), c("nif", "NIF"),
    c("linha", "Linha", true), c("artigo", "Artigo", true), c("descricao", "Descrição", true), c("quantidade", "Quantidade", true),
    c("preco", "Preço unitário", true), c("bruto", "Bruto"), c("desconto", "Desconto"), c("liquido", "Valor linha", true),
    c("tipoIva", "Tipo IVA"), c("taxaIva", "Taxa IVA"), c("peso", "Peso"), c("moeda", "Moeda")
  ],
  financeiros: [
    c("documento", "Documento", true), c("cliente", "Cliente", true), c("data", "Data", true), c("moeda", "Moeda"),
    c("modo", "Modo pagamento"), c("bruto", "Valor aplicado"), c("desconto", "Desconto"), c("liquido", "Recebido", true),
    c("emissor", "Emissor"), c("estado", "Estado", true), c("impresso", "Impresso")
  ],
  linhasFinanceiras: [
    c("recibo", "Recebimento", true), c("cliente", "Cliente"), c("dataRecibo", "Data recebimento"),
    c("documento", "Documento liquidado", true), c("emissao", "Emissão"), c("vencimento", "Vencimento"),
    c("valorDocumento", "Valor documento"), c("pendenteAntes", "Pendente antes", true), c("liquidado", "Valor liquidado", true),
    c("desconto", "Desconto"), c("recebido", "Recebido", true), c("novoPendente", "Novo pendente", true), c("moeda", "Moeda")
  ],
  relacaoComercial: [
    c("documento", "Documento", true), c("estado", "Estado", true), c("emissao", "Emissão", true),
    c("vencimento", "Vencimento"), c("cliente", "Cliente", true), c("nif", "NIF"), c("moeda", "Moeda"),
    c("totalDocumento", "Total documento", true), c("liquidadoDocumento", "Liquidado"), c("linha", "Linha", true),
    c("artigo", "Artigo", true), c("descricao", "Descrição", true), c("quantidade", "Quantidade", true),
    c("preco", "Preço unitário", true), c("brutoLinha", "Bruto linha"), c("descontoLinha", "Desconto linha"),
    c("valorLinha", "Valor linha", true), c("tipoIva", "Tipo IVA"), c("taxaIva", "Taxa IVA"), c("peso", "Peso")
  ],
  relacaoFinanceira: [
    c("recibo", "Recebimento", true), c("estado", "Estado", true), c("dataRecibo", "Data recebimento", true),
    c("cliente", "Cliente", true), c("modo", "Modo pagamento"), c("totalRecebido", "Total recebido", true),
    c("documento", "Documento liquidado", true), c("emissao", "Emissão"), c("vencimento", "Vencimento"),
    c("valorDocumento", "Valor documento"), c("pendenteAntes", "Pendente antes", true),
    c("valorLiquidado", "Valor liquidado", true), c("descontoLinha", "Desconto"),
    c("recebidoLinha", "Recebido", true), c("novoPendente", "Novo pendente", true), c("moeda", "Moeda")
  ],
  extratoCliente: [
    c("data", "Data", true), c("documento", "Documento", true), c("descricao", "Descrição", true),
    c("vencimento", "Vencimento"), c("debito", "Débito", true), c("credito", "Crédito", true),
    c("saldo", "Saldo", true), c("moeda", "Moeda", true)
  ]
};

export default function ListagensView() {
  const location = useLocation();
  const navigate = useNavigate();
  const defaultPeriod = currentYearDateRange();
  const [source, setSource] = useState<SourceKey>(() => location.pathname.includes("/listagens/pendentes-a-data") ? "pendentesAData" : location.pathname.includes("/listagens/pendentes") ? "pendentes" : "comerciais");
  const [pendentes, setPendentes] = useState<PendenteListagem[]>([]);
  const [pendentesTotais, setPendentesTotais] = useState<PendentesResponse["totais"]>({ total: 0, recebido: 0, pendente: 0 });
  const [pendentesClienteIds, setPendentesClienteIds] = useState<number[]>([]);
  const [pendentesDataReferencia, setPendentesDataReferencia] = useState(todayIso);
  const [pendentesApenasVencidos, setPendentesApenasVencidos] = useState(false);
  const [comerciais, setComerciais] = useState<DocumentoComercial[]>([]);
  const [financeiros, setFinanceiros] = useState<DocumentoFinanceiro[]>([]);
  const [linhasComerciais, setLinhasComerciais] = useState<LinhaComercialListagem[]>([]);
  const [linhasFinanceiras, setLinhasFinanceiras] = useState<LinhaFinanceiraListagem[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [columnsOpen, setColumnsOpen] = useState(false);
  const [clientes, setClientes] = useState<ClienteOption[]>([]);
  const [artigos, setArtigos] = useState<ArtigoOption[]>([]);
  const [dataInicial, setDataInicial] = useState(defaultPeriod.dataInicial);
  const [dataFinal, setDataFinal] = useState(defaultPeriod.dataFinal);
  const [clienteIds, setClienteIds] = useState<number[]>([]);
  const [artigoIds, setArtigoIds] = useState<string[]>([]);
  const [clientesExtrato, setClientesExtrato] = useState<ClienteOption[]>([]);
  const [extratoClienteIds, setExtratoClienteIds] = useState<number[]>([]);
  const [extratoDataInicial, setExtratoDataInicial] = useState(() => currentYearDateRange().dataInicial);
  const [extratoDataFinal, setExtratoDataFinal] = useState(() => currentYearDateRange().dataFinal);
  const [extratos, setExtratos] = useState<ExtratoCliente[] | null>(null);
  const [exportingPdf, setExportingPdf] = useState(false);
  const [exportingExcel, setExportingExcel] = useState(false);
  const [exportingPendentesFormat, setExportingPendentesFormat] = useState<"pdf" | "xlsx" | null>(null);
  const configured = useConfiguredColumns(`fac.listagens.${source}.colunas`, COLUMNS[source]);

  useEffect(() => { loadFilterOptions(); }, []);
  useEffect(() => {
    if (source !== "extratoCliente") {
      loadSource(source);
    }
  }, [source, dataInicial, dataFinal, clienteIds, artigoIds, pendentesClienteIds, pendentesDataReferencia, pendentesApenasVencidos]);

  async function loadFilterOptions() {
    try {
      const [clientesRows, artigosRows] = await Promise.all([
        fetchAllPages<ClienteOption>("/api/clientes", "nome,asc"),
        fetchAllPages<ArtigoOption>("/api/artigos", "descricao,asc")
      ]);
      setClientes(clientesRows.filter((cliente) => !cliente.inativo));
      setArtigos(artigosRows);
      setClientesExtrato(clientesRows);
    } catch {
      // As listagens continuam utilizaveis; apenas os seletores ficam sem opcoes.
    }
  }

  async function loadSource(target: SourceKey) {
    if (target !== "pendentes" && target !== "pendentesAData" && (!dataInicial || !dataFinal)) {
      setMessage("Indica a data inicial e a data final.");
      return;
    }
    if (target === "pendentesAData" && !pendentesDataReferencia) {
      setMessage("Indica a data de referencia.");
      return;
    }
    if (target !== "pendentes" && target !== "pendentesAData" && dataInicial > dataFinal) {
      setMessage("A data inicial não pode ser posterior à data final.");
      clearSourceRows(target);
      return;
    }
    setLoading(true);
    setMessage(null);
    clearSourceRows(target);
    try {
      if (target === "extratoCliente" && clientesExtrato.length === 0) {
        setClientesExtrato((await fetchPage<ClienteOption>("/api/clientes?size=500&sort=nome,asc")).content);
      }
      if (target === "pendentes" || target === "pendentesAData") {
        const params = new URLSearchParams();
        if (target === "pendentesAData") params.set("dataReferencia", pendentesDataReferencia);
        params.set("apenasVencidos", String(pendentesApenasVencidos));
        pendentesClienteIds.forEach((id) => params.append("clienteIds", String(id)));
        const endpoint = target === "pendentesAData" ? "/api/listagens/pendentes-a-data" : "/api/listagens/pendentes";
        const response = await fetchJson<PendentesResponse>(`${endpoint}${params.toString() ? `?${params}` : ""}`);
        setPendentes(response.linhas);
        setPendentesTotais(response.totais);
      }
      if (target === "comerciais") {
        const page = await fetchPage<DocumentoComercialResponse>(`${listagemUrl("/api/listagens/documentos-comerciais")}&sort=dataEmissao,desc&sort=id,desc`);
        setComerciais(page.content.map((item) => ({ ...item.documento, valorLiquido: item.valorLiquido })));
      }
      if (target === "linhasComerciais" || target === "relacaoComercial") {
        const page = await fetchPage<LinhaComercialResponse>(`${listagemUrl("/api/listagens/linhas-comerciais", target === "linhasComerciais")}&sort=documentoComercial.dataEmissao,desc&sort=documentoComercial.id,desc&sort=numeroLinha,asc`);
        setLinhasComerciais(page.content.map((item) => ({ ...item.linha, documento: item.documento })));
      }
      if (target === "financeiros") {
        setFinanceiros((await fetchPage<DocumentoFinanceiro>(`${listagemUrl("/api/listagens/documentos-financeiros")}&sort=dataEmissao,desc&sort=id,desc`)).content);
      }
      if (target === "linhasFinanceiras" || target === "relacaoFinanceira") {
        const page = await fetchPage<LinhaFinanceiraResponse>(`${listagemUrl("/api/listagens/linhas-financeiras")}&sort=documentoFinanceiro.dataEmissao,desc&sort=documentoFinanceiro.id,desc&sort=numeroLinha,asc`);
        setLinhasFinanceiras(page.content.map((item) => ({ ...item.linha, documento: item.documento })));
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar a listagem.");
    } finally {
      setLoading(false);
    }
  }

  async function consultarExtrato() {
    setMessage(null);
    setExtratos(null);
    if (!extratoDataInicial || !extratoDataFinal) {
      setMessage("Indica a data inicial e a data final.");
      return;
    }
    if (extratoDataInicial > extratoDataFinal) {
      setMessage("A data inicial não pode ser posterior à data final.");
      return;
    }
    setLoading(true);
    try {
      const params = extratoParams(extratoClienteIds, extratoDataInicial, extratoDataFinal);
      setExtratos(await fetchJson<ExtratoCliente[]>(`/api/extratos/clientes?${params}`));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar o extrato.");
    } finally {
      setLoading(false);
    }
  }

  async function exportarExtrato(format: "pdf" | "xlsx") {
    setMessage(null);
    if (!extratoDataInicial || !extratoDataFinal) {
      setMessage("Indica a data inicial e a data final.");
      return;
    }
    if (extratoDataInicial > extratoDataFinal) {
      setMessage("A data inicial não pode ser posterior à data final.");
      return;
    }
    const setExporting = format === "pdf" ? setExportingPdf : setExportingExcel;
    setExporting(true);
    try {
      const params = extratoParams(extratoClienteIds, extratoDataInicial, extratoDataFinal);
      const response = await apiFetch(`/api/extratos/clientes/exportar/${format}?${params}`);
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
      setMessage(error instanceof Error ? error.message : "Não foi possível exportar o extrato.");
    } finally {
      setExporting(false);
    }
  }

  async function exportarPendentes(format: "pdf" | "xlsx") {
    setMessage(null);
    if (source === "pendentesAData" && !pendentesDataReferencia) {
      setMessage("Indica a data de referencia.");
      return;
    }
    setExportingPendentesFormat(format);
    try {
      const params = new URLSearchParams();
      if (source === "pendentesAData") params.set("dataReferencia", pendentesDataReferencia);
      params.set("apenasVencidos", String(pendentesApenasVencidos));
      pendentesClienteIds.forEach((id) => params.append("clienteIds", String(id)));
      const endpoint = source === "pendentesAData" ? "/api/listagens/pendentes-a-data/exportar" : "/api/listagens/pendentes/exportar";
      const response = await apiFetch(`${endpoint}/${format}${params.toString() ? `?${params}` : ""}`);
      if (!response.ok) throw new Error(await responseError(response));
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = downloadFilename(response.headers.get("Content-Disposition"), `${source === "pendentesAData" ? "pendentes-a-data" : "todos-pendentes"}.${format}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel exportar a listagem.");
    } finally {
      setExportingPendentesFormat(null);
    }
  }

  function clearSourceRows(target: SourceKey) {
    if (isPendentesSource(target)) {
      setPendentes([]);
      setPendentesTotais({ total: 0, recebido: 0, pendente: 0 });
    }
    if (target === "comerciais") setComerciais([]);
    if (target === "financeiros") setFinanceiros([]);
    if (target === "linhasComerciais" || target === "relacaoComercial") setLinhasComerciais([]);
    if (target === "linhasFinanceiras" || target === "relacaoFinanceira") setLinhasFinanceiras([]);
  }

  function listagemUrl(path: string, includeArtigos = false) {
    const params = new URLSearchParams({
      dataInicial,
      dataFinal,
      size: "500"
    });
    clienteIds.forEach((id) => params.append("clienteIds", String(id)));
    if (includeArtigos) artigoIds.forEach((id) => params.append("artigoIds", id));
    return `${path}?${params}`;
  }

  function changeSource(next: SourceKey) {
    setSource(next);
    setSearch("");
    if (next !== "linhasComerciais") {
      setArtigoIds([]);
    }
  }

  const rows = useMemo(() => {
    const base: unknown[] = source === "extratoCliente" ? []
      : isPendentesSource(source) ? pendentes
      : source === "comerciais" ? comerciais
      : source === "linhasComerciais" || source === "relacaoComercial" ? linhasComerciais
      : source === "financeiros" ? financeiros
      : linhasFinanceiras;
    const term = search.trim().toLowerCase();
    if (!term) return base;
    return base.filter((row) => searchText(source, row).includes(term));
  }, [source, pendentes, comerciais, linhasComerciais, financeiros, linhasFinanceiras, search]);

  return <>
    <section className="fac-hero">
      <div><p className="fac-eyebrow">Listagens</p><h2>Consulta transversal dos dados do FAC</h2><p>Escolhe uma fonte, define as colunas necessárias e consulta cabeçalhos ou linhas sem interferir com a operação diária.</p></div>
      <div className="fac-hero-card"><span>Fonte atual</span><strong>{SOURCES.find((item) => item.key === source)?.label}</strong><small>{loading ? "A carregar..." : source === "extratoCliente" ? extratos ? `${extratos.reduce((total, extrato) => total + extrato.moedas.reduce((subtotal, moeda) => subtotal + moeda.movimentos.length, 0), 0)} movimentos` : "A aguardar consulta" : `${rows.length} registos`}</small></div>
    </section>

    <section className="fac-report-source-grid">
      {SOURCES.map((item, index) => <button className={item.key === source ? "active" : ""} disabled={!item.key} key={`${item.label}-${index}`} onClick={() => item.key && changeSource(item.key)} type="button"><strong>{item.label}</strong><span>{item.description}</span></button>)}
    </section>

    <section className="fac-panel fac-section-panel">
      <div className="fac-panel-header"><div><p className="fac-eyebrow">{SOURCES.find((item) => item.key === source)?.label}</p><h2>{source === "pendentesAData" ? "Situação dos valores por receber" : "Dados disponíveis"}</h2>{source === "pendentesAData" && <p className="fac-muted">Consulta os valores que se encontravam pendentes na data selecionada, incluindo documentos vencidos e nao vencidos.</p>}</div><div className="fac-inline-actions">{source !== "extratoCliente" && <input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar nesta listagem" type="search" value={search}/>}<button className="fac-ghost-button" onClick={() => setColumnsOpen((current) => !current)} type="button">Colunas ({configured.visibleColumns.length})</button><button className="fac-soft-button" disabled={loading} onClick={() => source === "extratoCliente" ? consultarExtrato() : loadSource(source)} type="button">Atualizar</button></div></div>
      {message && <p className="fac-message">{message}</p>}
      {isPendentesSource(source) && <PendentesFilters apenasVencidos={pendentesApenasVencidos} clientes={clientes} dataReferencia={source === "pendentesAData" ? pendentesDataReferencia : undefined} onApenasVencidos={setPendentesApenasVencidos} onChange={setPendentesClienteIds} onDataReferencia={setPendentesDataReferencia} selectedValues={pendentesClienteIds}/>}
      {isPendentesSource(source) && <div className="fac-pendentes-actions">
        <button className="fac-soft-button" disabled={exportingPendentesFormat !== null} onClick={() => exportarPendentes("pdf")} type="button">{exportingPendentesFormat === "pdf" ? "A gerar PDF..." : "Exportar PDF"}</button>
        <button className="fac-soft-button" disabled={exportingPendentesFormat !== null} onClick={() => exportarPendentes("xlsx")} type="button">{exportingPendentesFormat === "xlsx" ? "A gerar Excel..." : "Exportar Excel"}</button>
      </div>}
      {isPendentesSource(source) && <PendentesTotals totais={pendentesTotais}/>}
      {source !== "extratoCliente" && !isPendentesSource(source) && <ListingFilters artigos={artigos} clientes={clientes} dataFinal={dataFinal} dataInicial={dataInicial} onArtigos={setArtigoIds} onClientes={setClienteIds} onDataFinal={setDataFinal} onDataInicial={setDataInicial} selectedArtigoIds={artigoIds} selectedClienteIds={clienteIds} showArtigo={source === "linhasComerciais"} />}
      {source === "extratoCliente" && <p className="fac-muted">Extrato calculado a partir dos documentos emitidos. Os documentos anulados não integram os movimentos contabilísticos e cada moeda é apresentada separadamente.</p>}
      {source === "extratoCliente" && <div className="fac-extrato-filters">
        <div className="fac-filter-field"><span>Clientes</span><MultiSelectFilter allLabel="Todos os clientes" options={clientesExtrato.map((cliente) => ({ value: cliente.id, label: `${cliente.id} - ${cliente.nome}` }))} selectedValues={extratoClienteIds} onChange={(values) => { setExtratoClienteIds(values); setExtratos(null); }}/></div>
        <label><span>Data inicial</span><input onChange={(event) => setExtratoDataInicial(event.target.value)} type="date" value={extratoDataInicial}/></label>
        <label><span>Data final</span><input onChange={(event) => setExtratoDataFinal(event.target.value)} type="date" value={extratoDataFinal}/></label>
        <button className="fac-primary-button" disabled={loading} onClick={consultarExtrato} type="button">Consultar extrato</button>
        <button className="fac-soft-button" disabled={exportingPdf || exportingExcel} onClick={() => exportarExtrato("pdf")} type="button">{exportingPdf ? "A gerar PDF..." : "Exportar PDF"}</button>
        <button className="fac-soft-button" disabled={exportingPdf || exportingExcel} onClick={() => exportarExtrato("xlsx")} type="button">{exportingExcel ? "A gerar Excel..." : "Exportar Excel"}</button>
        <button className="fac-ghost-button" onClick={() => { const period = currentYearDateRange(); setExtratoClienteIds([]); setExtratoDataInicial(period.dataInicial); setExtratoDataFinal(period.dataFinal); setExtratos(null); setMessage(null); }} type="button">Limpar</button>
      </div>}
      <ColumnSelector columns={configured.columns} open={columnsOpen} onMove={configured.moveColumn} onReset={configured.resetColumns} onToggle={configured.toggleColumn}/>
      {source === "extratoCliente" && <ExtratoTable extratos={extratos} loading={loading} columns={configured.visibleColumns}/>}
      {source !== "extratoCliente" &&
      <div className="fac-table-scroll"><table className="fac-table"><thead><tr>{configured.visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>
        {rows.map((row, index) => <tr key={rowKey(source, row, index)}>{configured.visibleColumns.map((column) => <td key={column.key}>{cellValue(source, row, column.key, navigate)}</td>)}</tr>)}
        {!loading && rows.length === 0 && <tr><td colSpan={configured.visibleColumns.length}>{emptyMessage(source, pendentesClienteIds.length > 0)}</td></tr>}
      </tbody></table></div>}
    </section>
  </>;
}

function PendentesFilters({
  apenasVencidos,
  clientes,
  dataReferencia,
  onApenasVencidos,
  onChange,
  onDataReferencia,
  selectedValues
}: {
  apenasVencidos: boolean;
  clientes: ClienteOption[];
  dataReferencia?: string;
  onApenasVencidos: (value: boolean) => void;
  onChange: (values: number[]) => void;
  onDataReferencia?: (value: string) => void;
  selectedValues: number[];
}) {
  const selected = clientes.filter((cliente) => selectedValues.includes(cliente.id));
  return <div className="fac-pendentes-filters">
    {dataReferencia !== undefined && <label>
      <span>Data de referência</span>
      <input onChange={(event) => onDataReferencia?.(event.target.value)} type="date" value={dataReferencia}/>
    </label>}
    <div className="fac-filter-field">
      <span>Clientes</span>
      <MultiSelectFilter allLabel="Todos os clientes" options={clientes.filter((cliente) => !cliente.inativo).map((cliente) => ({ value: cliente.id, label: `${cliente.id} - ${cliente.nome}${cliente.nif ? ` - NIF ${cliente.nif}` : ""}` }))} selectedValues={selectedValues} onChange={onChange}/>
    </div>
    <label className="fac-pendentes-checkbox">
      <input checked={apenasVencidos} onChange={(event) => onApenasVencidos(event.target.checked)} type="checkbox"/>
      <span>Mostrar apenas vencidos</span>
    </label>
    {selected.length > 0 && <div className="fac-selected-chips" aria-label="Clientes selecionados">
      <span>Clientes selecionados: {selected.length}</span>
      {selected.map((cliente) => <button key={cliente.id} onClick={() => onChange(selectedValues.filter((id) => id !== cliente.id))} type="button">{cliente.nome} x</button>)}
      <button className="fac-ghost-button" onClick={() => onChange([])} type="button">Limpar</button>
    </div>}
  </div>;
}

function PendentesTotals({ totais }: { totais: PendentesResponse["totais"] }) {
  return <div className="fac-pendentes-totals">
    <div><span>Total faturado</span><strong>{money(totais.total)}</strong></div>
    <div><span>Total recebido</span><strong>{money(totais.recebido)}</strong></div>
    <div className="highlight"><span>Total pendente</span><strong>{money(totais.pendente)}</strong></div>
  </div>;
}

function ListingFilters({
  artigos,
  clientes,
  dataFinal,
  dataInicial,
  onArtigos,
  onClientes,
  onDataFinal,
  onDataInicial,
  selectedArtigoIds,
  selectedClienteIds,
  showArtigo
}: {
  artigos: ArtigoOption[];
  clientes: ClienteOption[];
  dataFinal: string;
  dataInicial: string;
  onArtigos: (values: string[]) => void;
  onClientes: (values: number[]) => void;
  onDataFinal: (value: string) => void;
  onDataInicial: (value: string) => void;
  selectedArtigoIds: string[];
  selectedClienteIds: number[];
  showArtigo: boolean;
}) {
  const activeClientes = clientes.filter((cliente) => !cliente.inativo);
  const activeArtigos = artigos.filter((artigo) => !artigo.inativo);
  const selectedClientes = activeClientes.filter((cliente) => selectedClienteIds.includes(cliente.id));
  const selectedArtigos = activeArtigos.filter((artigo) => selectedArtigoIds.includes(artigo.codigo));
  return (
    <div className="fac-listing-filters">
      <label><span>Data inicial</span><input onChange={(event) => onDataInicial(event.target.value)} type="date" value={dataInicial} /></label>
      <label><span>Data final</span><input onChange={(event) => onDataFinal(event.target.value)} type="date" value={dataFinal} /></label>
      <div className="fac-filter-field">
        <span>Clientes</span>
        <MultiSelectFilter allLabel="Todos os clientes" emptyMessage="Sem clientes encontrados." label="cliente" onChange={onClientes} options={clienteOptions(activeClientes)} searchPlaceholder="Pesquisar clientes" selectedValues={selectedClienteIds}/>
      </div>
      {showArtigo && <div className="fac-filter-field">
        <span>Artigos</span>
        <MultiSelectFilter<string> allLabel="Todos os artigos" emptyMessage="Sem artigos encontrados." label="artigo" onChange={onArtigos} options={artigoOptions(activeArtigos)} searchPlaceholder="Pesquisar artigos" selectedValues={selectedArtigoIds}/>
      </div>}
      {(selectedClientes.length > 0 || selectedArtigos.length > 0) && <div className="fac-selected-chips" aria-label="Filtros selecionados">
        {selectedClientes.map((cliente) => <button key={cliente.id} onClick={() => onClientes(selectedClienteIds.filter((id) => id !== cliente.id))} type="button">{cliente.nome} x</button>)}
        {selectedArtigos.map((artigo) => <button key={artigo.codigo} onClick={() => onArtigos(selectedArtigoIds.filter((id) => id !== artigo.codigo))} type="button">{artigo.descricao} x</button>)}
        <button className="fac-ghost-button" onClick={() => { onClientes([]); onArtigos([]); }} type="button">Limpar</button>
      </div>}
    </div>
  );
}
function ExtratoTable({ extratos, loading, columns }: { extratos: ExtratoCliente[] | null; loading: boolean; columns: ConfigurableColumn[] }) {
  if (loading) return <p className="fac-empty-state">A calcular o extrato...</p>;
  if (!extratos) return <p className="fac-empty-state">Sem seleção de clientes serão considerados todos.</p>;
  if (extratos.length === 0) return <p className="fac-empty-state">Sem clientes para mostrar.</p>;

  return <div className="fac-extrato-result">
    {extratos.map((extrato) => <section className="fac-extrato-cliente-result" key={extrato.clienteId}>
    <header className="fac-extrato-header">
      <div><span>Cliente {extrato.clienteId}</span><strong>{extrato.clienteNome}</strong><small>NIF {extrato.clienteNif || "-"}</small></div>
      <div><span>Período</span><strong>{datePt(extrato.dataInicial)} a {datePt(extrato.dataFinal)}</strong><small>Gerado em {dateTimePt(extrato.geradoEm)}</small></div>
    </header>
    {extrato.moedas.map((moeda) => <section className="fac-extrato-moeda" key={moeda.moedaId}>
      <div className="fac-extrato-moeda-title"><strong>{moeda.moedaId}</strong><span>{moeda.movimentos.length} movimentos no periodo</span></div>
      <div className="fac-table-scroll"><table className="fac-table"><thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>
        <ExtratoTotalRow className="fac-extrato-anterior" label="Anterior" moeda={moeda.moedaId} totals={moeda.anterior} columns={columns}/>
        {moeda.movimentos.map((movimento) => <tr key={`${movimento.origem}-${movimento.id}`}>{columns.map((column) => <td key={column.key}>{extratoMovementCell(movimento, moeda.moedaId, column.key)}</td>)}</tr>)}
        {moeda.movimentos.length === 0 && <tr><td className="fac-extrato-empty" colSpan={columns.length}>Sem movimentos no periodo selecionado.</td></tr>}
        <ExtratoTotalRow className="fac-extrato-periodo" label="Total do período" moeda={moeda.moedaId} totals={moeda.totalPeriodo} columns={columns}/>
        <ExtratoTotalRow className="fac-extrato-total" label="Total final" moeda={moeda.moedaId} totals={moeda.totalFinal} columns={columns}/>
      </tbody></table></div>
    </section>)}
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

function isPendentesSource(source: SourceKey) {
  return source === "pendentes" || source === "pendentesAData";
}

function emptyMessage(source: SourceKey, hasClientes: boolean) {
  if (source === "pendentesAData") {
    return hasClientes
      ? "Nao existem valores pendentes na data selecionada para os clientes escolhidos."
      : "Nao existem valores pendentes na data selecionada.";
  }
  if (source === "pendentes") {
    return hasClientes
      ? "Nao existem documentos pendentes para os clientes selecionados."
      : "Nao existem documentos pendentes.";
  }
  return "Sem registos para mostrar.";
}

function c(key: string, label: string, visible = false): ConfigurableColumn { return { key, label, visible }; }
function reference(tipo: string, serie: string, numero: number | null) { return `${tipo} ${serie}/${numero ?? "rascunho"}`; }
function todayIso() { return new Date().toLocaleDateString("sv-SE"); }
function datePt(value?: string) { return value ? value.split("-").reverse().join("/") : "-"; }
function dateTimePt(value?: string) { return value ? new Date(value).toLocaleString("pt-PT") : "-"; }
function extratoParams(clienteIds: number[], dataInicial: string, dataFinal: string) { const params = new URLSearchParams({ dataInicial, dataFinal }); clienteIds.forEach((id) => params.append("clienteIds", String(id))); return params; }
function clienteOptions(clientes: ClienteOption[]): MultiSelectOption<number>[] {
  return clientes.map((cliente) => ({
    value: cliente.id,
    label: `${cliente.id} - ${cliente.nome}${cliente.nif ? ` - NIF ${cliente.nif}` : ""}`,
    searchText: [cliente.id, cliente.nome, cliente.nif, cliente.localidade, cliente.email, cliente.tel, cliente.tm].filter(Boolean).join(" ")
  }));
}

function artigoOptions(artigos: ArtigoOption[]): MultiSelectOption<string>[] {
  return artigos.map((artigo) => ({
    value: artigo.codigo,
    label: `${artigo.codigo} - ${artigo.descricao}`,
    searchText: [artigo.codigo, artigo.descricao, artigo.unidade, artigo.familiaId, artigo.ivaVendaId].filter(Boolean).join(" ")
  }));
}
function money(value: number) { return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function moneyCell(value: number, moeda?: string) { return <span className="fac-money">{money(value)}{moeda ? ` ${moeda}` : ""}</span>; }
function decimal(value: number) { return Number(value || 0).toLocaleString("pt-PT", { maximumFractionDigits: 6 }); }
function yesNo(value: boolean) { return value ? "Sim" : "Não"; }
function statusComercial(d: DocumentoComercial) { return d.anulado ? "ANULADO" : d.estado; }

function cellValue(source: SourceKey, raw: unknown, key: string, navigate?: (path: string) => void) {
  if (isPendentesSource(source)) {
    const p = raw as PendenteListagem;
    const values: Record<string, React.ReactNode> = {
      cliente: <span><strong>{p.clienteNome}</strong><small className="fac-cell-note">NIF {p.clienteCodigo || "-"}</small></span>,
      documento: <button className="fac-link-button" onClick={() => navigate?.(`/documentos/${p.documentoId}`)} type="button">{p.documento}</button>,
      data: datePt(p.data),
      vencimento: datePt(p.vencimento),
      total: moneyCell(p.total, p.moedaId),
      recebido: moneyCell(p.recebido, p.moedaId),
      pendente: <strong>{moneyCell(p.pendente, p.moedaId)}</strong>
    };
    return values[key] ?? "-";
  }
  if (source === "comerciais") {
    const d = raw as DocumentoComercial;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), cliente: d.clienteNome, nif: d.clienteNif, emissao: datePt(d.dataEmissao), vencimento: datePt(d.dataVencimento), moeda: d.moedaId, bruto: moneyCell(d.valorBruto), desconto: moneyCell(d.valorDesconto), liquido: moneyCell(d.valorLiquido ?? 0), iva: moneyCell(d.valorIvaTotal), retencao: moneyCell(d.valorRetencao), total: moneyCell(d.valorTotal, d.moedaId), estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{statusComercial(d)}</span>, impresso: yesNo(d.impresso), liquidado: yesNo(d.liquidado), emissor: d.emissorId ?? "-" };
    return values[key] ?? "-";
  }
  if (source === "linhasComerciais") {
    const l = raw as LinhaComercialListagem; const d = l.documento;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), emissao: datePt(d.dataEmissao), cliente: d.clienteNome, nif: d.clienteNif, linha: l.numeroLinha, artigo: l.artigoId ?? "-", descricao: l.descricao, quantidade: decimal(l.quantidade), preco: moneyCell(l.precoUnitario), bruto: moneyCell(l.valorBruto), desconto: moneyCell(l.valorDesconto), liquido: moneyCell(l.valorLinha), tipoIva: l.tipoTaxaIvaId ?? "-", taxaIva: `${decimal(l.percentagemIva)}%`, peso: decimal(l.peso), moeda: d.moedaId };
    return values[key] ?? "-";
  }
  if (source === "relacaoComercial") {
    const l = raw as LinhaComercialListagem; const d = l.documento;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{statusComercial(d)}</span>, emissao: datePt(d.dataEmissao), vencimento: datePt(d.dataVencimento), cliente: d.clienteNome, nif: d.clienteNif, moeda: d.moedaId, totalDocumento: moneyCell(d.valorTotal), liquidadoDocumento: yesNo(d.liquidado), linha: l.numeroLinha, artigo: l.artigoId ?? "-", descricao: l.descricao, quantidade: decimal(l.quantidade), preco: moneyCell(l.precoUnitario), brutoLinha: moneyCell(l.valorBruto), descontoLinha: moneyCell(l.valorDesconto), valorLinha: moneyCell(l.valorLinha), tipoIva: l.tipoTaxaIvaId ?? "-", taxaIva: `${decimal(l.percentagemIva)}%`, peso: decimal(l.peso) };
    return values[key] ?? "-";
  }
  if (source === "financeiros") {
    const d = raw as DocumentoFinanceiro;
    const values: Record<string, React.ReactNode> = { documento: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), cliente: d.clienteId, data: datePt(d.dataEmissao), moeda: d.moedaId, modo: d.mPagamentoId, bruto: moneyCell(d.valorPagamentoBruto), desconto: moneyCell(d.valorDescontoFinanceiro), liquido: moneyCell(d.valorPagamentoLiquido, d.moedaId), emissor: d.emissorId, estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{d.anulado ? "ANULADO" : "EMITIDO"}</span>, impresso: yesNo(d.impresso) };
    return values[key] ?? "-";
  }
  const l = raw as LinhaFinanceiraListagem; const d = l.documento;
  const values: Record<string, React.ReactNode> = source === "relacaoFinanceira"
    ? { recibo: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), estado: <span className={`fac-status ${d.anulado ? "danger" : ""}`}>{d.anulado ? "ANULADO" : "EMITIDO"}</span>, dataRecibo: datePt(d.dataEmissao), cliente: d.clienteId, modo: d.mPagamentoId, totalRecebido: moneyCell(d.valorPagamentoLiquido), documento: reference(l.tipoDocumentoId, l.serieDocumento, l.numeroDocumento), emissao: datePt(l.dataDocumento), vencimento: datePt(l.dataVencimento), valorDocumento: moneyCell(l.valorDocumento), pendenteAntes: moneyCell(l.valorPendenteAntes), valorLiquidado: moneyCell(l.valorALiquidar), descontoLinha: moneyCell(l.descontoValor), recebidoLinha: moneyCell(l.valorPagamentoLiquido), novoPendente: moneyCell(l.novoValorPendente), moeda: l.moedaId }
    : { recibo: reference(d.tipoDocumentoId, d.serie, d.numeroDocumento), cliente: d.clienteId, dataRecibo: datePt(d.dataEmissao), documento: reference(l.tipoDocumentoId, l.serieDocumento, l.numeroDocumento), emissao: datePt(l.dataDocumento), vencimento: datePt(l.dataVencimento), valorDocumento: moneyCell(l.valorDocumento), pendenteAntes: moneyCell(l.valorPendenteAntes), liquidado: moneyCell(l.valorALiquidar), desconto: moneyCell(l.descontoValor), recebido: moneyCell(l.valorPagamentoLiquido), novoPendente: moneyCell(l.novoValorPendente), moeda: l.moedaId };
  return values[key] ?? "-";
}

function searchText(source: SourceKey, row: unknown) {
  if (isPendentesSource(source)) { const p = row as PendenteListagem; return `${p.clienteNome} ${p.clienteCodigo} ${p.documento}`.toLowerCase(); }
  if (source === "comerciais") { const d = row as DocumentoComercial; return `${reference(d.tipoDocumentoId, d.serie, d.numeroDocumento)} ${d.clienteNome} ${d.clienteNif} ${statusComercial(d)}`.toLowerCase(); }
  if (source === "linhasComerciais" || source === "relacaoComercial") { const l = row as LinhaComercialListagem; return `${reference(l.documento.tipoDocumentoId, l.documento.serie, l.documento.numeroDocumento)} ${l.documento.clienteNome} ${l.documento.clienteNif} ${l.artigoId} ${l.descricao}`.toLowerCase(); }
  if (source === "financeiros") { const d = row as DocumentoFinanceiro; return `${reference(d.tipoDocumentoId, d.serie, d.numeroDocumento)} ${d.clienteId} ${d.emissorId}`.toLowerCase(); }
  const l = row as LinhaFinanceiraListagem; return `${reference(l.documento.tipoDocumentoId, l.documento.serie, l.documento.numeroDocumento)} ${reference(l.tipoDocumentoId, l.serieDocumento, l.numeroDocumento)} ${l.documento.clienteId}`.toLowerCase();
}

function rowKey(source: SourceKey, row: unknown, index: number) {
  if (isPendentesSource(source)) return (row as PendenteListagem).documentoId;
  if (source === "comerciais" || source === "financeiros") return (row as { id: number }).id;
  return `${source}-${(row as { id: number }).id}-${index}`;
}

async function fetchPage<T>(url: string): Promise<Page<T>> { return fetchJson<Page<T>>(url); }
async function fetchAllPages<T>(path: string, sort: string, pageSize = 500): Promise<T[]> {
  const rows: T[] = [];
  for (let pageNumber = 0; ; pageNumber += 1) {
    const page = await fetchPage<T>(`${path}?page=${pageNumber}&size=${pageSize}&sort=${sort}`);
    rows.push(...page.content);
    if (page.totalPages !== undefined) {
      if (pageNumber + 1 >= page.totalPages) return rows;
    } else if (page.content.length < pageSize) {
      return rows;
    }
  }
}
async function fetchJson<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function downloadFilename(contentDisposition: string | null, fallback: string) {
  if (!contentDisposition) return fallback;
  const encoded = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
  if (encoded) return decodeURIComponent(encoded.replace(/^"|"$/g, ""));
  return contentDisposition.match(/filename="?([^";]+)"?/i)?.[1] ?? fallback;
}
