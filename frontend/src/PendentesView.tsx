import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch, getAuthSession, hasPermission } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";

type Page<T> = { content: T[]; totalElements: number };
type Pendente = {
  id: number;
  documentoComercialId: number;
  clienteId: number;
  tipoDocumentoId: string;
  numeroDocumento: number;
  serieDocumento: string;
  valorDocumento: number;
  valorPendente: number;
  dataDocumento: string;
  dataVencimento: string;
  moedaId: string;
};
type Cliente = { id: number; nome: string; nif: string; inativo: boolean; moedaId?: string | null; mPagamentoId?: string | null; pPagamentoId?: string | null };
type TipoDocumento = { id: string; descricao: string; areaGestao: number };
type Serie = { serie: string; tipoDocumentoId: string; nome: string };
type MPagamento = { id: string; nome: string };
type DocumentoFinanceiro = {
  id: number;
  clienteId: number;
  tipoDocumentoId: string;
  tipoDocumentoCodigoFiscal?: string | null;
  serie: string;
  numeroDocumento: number;
  atcud?: string | null;
  temQrFiscal?: boolean;
  qrPayload?: string | null;
  dataEmissao: string;
  valorPagamentoBruto?: number;
  valorDescontoFinanceiro?: number;
  valorPagamentoLiquido: number;
  moedaId: string;
  mPagamentoId: string;
  dataHoraOperacao?: string | null;
  emissorId: string;
  momentoEmissao?: string | null;
  observacoes?: string | null;
  anulado: boolean;
  impresso?: boolean;
  linhas?: LinhaFinanceira[];
};
type LinhaFinanceira = {
  id: number;
  numeroLinha: number;
  pendenteId: number;
  dataDocumento: string;
  dataVencimento: string;
  tipoDocumentoId: string;
  numeroDocumento: number;
  serieDocumento: string;
  valorDocumento: number;
  valorPendenteAntes: number;
  valorALiquidar: number;
  descontoPercentual: number;
  descontoValor: number;
  valorPagamentoLiquido: number;
  novoValorPendente: number;
  moedaId: string;
};
type DiagnosticoFinanceiro = { referencia: string; podeAnular: boolean; bloqueios: string[] };
type ReceiptForm = {
  clienteId: string;
  moedaId: string;
  tipoDocumentoId: string;
  serie: string;
  dataEmissao: string;
  valorRecebido: string;
  mPagamentoId: string;
  emissorId: string;
  observacoes: string;
};
type Allocations = Record<number, string>;

const PENDENTE_COLUMNS: ConfigurableColumn[] = [
  { key: "documento", label: "Documento", visible: true }, { key: "cliente", label: "Cliente", visible: true },
  { key: "emissao", label: "Emissão", visible: false }, { key: "vencimento", label: "Vencimento", visible: true },
  { key: "moeda", label: "Moeda", visible: false }, { key: "original", label: "Original", visible: true },
  { key: "pendente", label: "Pendente", visible: true }, { key: "estado", label: "Estado", visible: true }
];
const FINANCEIRO_COLUMNS: ConfigurableColumn[] = [
  { key: "documento", label: "Documento", visible: true }, { key: "cliente", label: "Cliente", visible: true },
  { key: "data", label: "Data", visible: true }, { key: "modo", label: "Modo", visible: true },
  { key: "moeda", label: "Moeda", visible: false }, { key: "liquido", label: "Líquido", visible: true },
  { key: "emissor", label: "Emissor", visible: false }, { key: "estado", label: "Estado", visible: true }
];

export default function PendentesView() {
  const navigate = useNavigate();
  const canManageTreasury = hasPermission("TESOURARIA_GERIR");
  const canAnnul = hasPermission("DOCUMENTO_ANULAR");
  const receiptEditorRef = useRef<HTMLElement | null>(null);
  const clientSelectRef = useRef<HTMLSelectElement | null>(null);
  const newReceiptButtonRef = useRef<HTMLButtonElement | null>(null);
  const [pendentes, setPendentes] = useState<Pendente[]>([]);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [financeiros, setFinanceiros] = useState<DocumentoFinanceiro[]>([]);
  const [tipos, setTipos] = useState<TipoDocumento[]>([]);
  const [series, setSeries] = useState<Serie[]>([]);
  const [modos, setModos] = useState<MPagamento[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [receiptOpen, setReceiptOpen] = useState(false);
  const [form, setForm] = useState<ReceiptForm>(emptyReceiptForm());
  const [allocations, setAllocations] = useState<Allocations>({});
  const [manualReceiptValue, setManualReceiptValue] = useState(false);
  const [pendenteColumnsOpen, setPendenteColumnsOpen] = useState(false);
  const [financeiroColumnsOpen, setFinanceiroColumnsOpen] = useState(false);
  const [selectedFinanceiroId, setSelectedFinanceiroId] = useState<number | null>(null);
  const [selectedFinanceiro, setSelectedFinanceiro] = useState<DocumentoFinanceiro | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [createdReceipt, setCreatedReceipt] = useState<DocumentoFinanceiro | null>(null);
  const pendenteColumns = useConfiguredColumns("fac.pendentes.colunas", PENDENTE_COLUMNS);
  const financeiroColumns = useConfiguredColumns("fac.recebimentos.colunas", FINANCEIRO_COLUMNS);

  useEffect(() => { loadTesouraria(); }, []);

  useEffect(() => {
    if (!receiptOpen) return;
    requestAnimationFrame(() => clientSelectRef.current?.focus());
  }, [receiptOpen]);

  async function loadTesouraria() {
    setLoading(true);
    setMessage(null);
    try {
      const [pendentesPage, clientesPage, financeirosPage, modosPage] = await Promise.all([
        fetchJson<Page<Pendente>>("/api/pendentes?size=500&sort=id,desc"),
        fetchJson<Page<Cliente>>("/api/clientes?size=500&sort=nome,asc"),
        fetchJson<Page<DocumentoFinanceiro>>("/api/documentos-financeiros?size=300&sort=id,desc"),
        fetchJson<Page<MPagamento>>("/api/mpagamentos?size=100&sort=nome,asc")
      ]);
      setPendentes(pendentesPage.content);
      setClientes(clientesPage.content);
      setFinanceiros(financeirosPage.content);
      setModos(modosPage.content);
      setSelectedFinanceiro((current) => current ? financeirosPage.content.find((item) => item.id === current.id) ?? current : current);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar a tesouraria.");
    } finally {
      setLoading(false);
    }
  }

  async function openReceipt() {
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const [tiposPage, seriesPage, modosPage] = await Promise.all([
        fetchJson<Page<TipoDocumento>>("/api/tipos-documento?size=100&sort=descricao,asc"),
        fetchJson<Page<Serie>>("/api/series?size=200&sort=serie,asc"),
        fetchJson<Page<MPagamento>>("/api/mpagamentos?size=100&sort=nome,asc")
      ]);
      const financeirosTipos = tiposPage.content.filter((tipo) => tipo.areaGestao === 3);
      const seriesFinanceiras = seriesPage.content.filter((serie) => financeirosTipos.some((tipo) => tipo.id === serie.tipoDocumentoId));
      const tipoInicial = financeirosTipos[0]?.id ?? "";
      setTipos(financeirosTipos);
      setSeries(seriesFinanceiras);
      setModos(modosPage.content);
      setForm({
        ...emptyReceiptForm(),
        tipoDocumentoId: tipoInicial,
        serie: seriesFinanceiras.find((serie) => serie.tipoDocumentoId === tipoInicial)?.serie ?? "",
        emissorId: getAuthSession()?.codigo ?? ""
      });
      setAllocations({});
      setManualReceiptValue(false);
      setReceiptOpen(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível preparar o recebimento.");
    } finally {
      setLoading(false);
    }
  }

  function closeReceipt() {
    setReceiptOpen(false);
    requestAnimationFrame(() => newReceiptButtonRef.current?.focus());
  }

  async function openFinancialDetail(documento: DocumentoFinanceiro) {
    setSelectedFinanceiroId(documento.id);
    setDetailLoading(true);
    setMessage(null);
    try {
      const detail = await fetchJson<DocumentoFinanceiro>(`/api/documentos-financeiros/${documento.id}`);
      setSelectedFinanceiro(detail);
      requestAnimationFrame(() => document.getElementById("financial-detail-heading")?.focus());
    } catch (error) {
      setSelectedFinanceiro(null);
      setMessage(error instanceof Error ? error.message : "Não foi possível abrir o detalhe do recebimento.");
    } finally {
      setDetailLoading(false);
    }
  }

  function closeFinancialDetail() {
    setSelectedFinanceiroId(null);
    setSelectedFinanceiro(null);
  }

  function selectClient(clienteId: string) {
    const cliente = clientes.find((item) => item.id === Number(clienteId));
    const moedas = openPendentesForClient(pendentes, Number(clienteId)).map((item) => item.moedaId);
    const moedasUnicas = [...new Set(moedas)];
    const moedaCliente = cliente?.moedaId ?? "";
    const moedaId = moedaCliente && moedasUnicas.includes(moedaCliente)
      ? moedaCliente
      : moedasUnicas.length === 1 ? moedasUnicas[0] : "";
    setForm((current) => ({
      ...current,
      clienteId,
      moedaId,
      mPagamentoId: validPaymentMode(modos, cliente?.mPagamentoId),
      valorRecebido: ""
    }));
    setAllocations({});
    setManualReceiptValue(false);
    requestAnimationFrame(() => {
      const selector = moedasUnicas.length === 1 ? "[data-receipt-value]" : "[data-receipt-currency]";
      receiptEditorRef.current?.querySelector<HTMLElement>(selector)?.focus();
    });
  }

  function distributeReceipt() {
    const target = round6(Number(form.valorRecebido));
    if (!Number.isFinite(target) || target <= 0) {
      setMessage("Indica primeiro um valor recebido positivo.");
      return;
    }
    let remaining = target;
    const next: Allocations = {};
    receiptPendentes.forEach((pendente) => {
      if (remaining <= 0) return;
      const allocated = Math.min(round6(pendente.valorPendente), remaining);
      next[pendente.id] = String(round6(allocated));
      remaining = round6(remaining - allocated);
    });
    setAllocations(next);
    requestAnimationFrame(() => {
      const firstAllocation = receiptEditorRef.current?.querySelector<HTMLInputElement>(".fac-table-input");
      firstAllocation?.focus();
      firstAllocation?.select();
    });
    setMessage(remaining > 0 ? `Ficam ${money(remaining)} ${form.moedaId} por distribuir porque o valor recebido excede os pendentes disponíveis.` : null);
  }

  function handleReceiptKeyDown(event: React.KeyboardEvent<HTMLElement>) {
    if (event.key === "Escape") {
      event.preventDefault();
      closeReceipt();
      return;
    }
    if (event.key === "Enter" && (event.target as HTMLElement).hasAttribute("data-receipt-value")) {
      event.preventDefault();
      distributeReceipt();
      return;
    }
    if (event.key !== "Tab") return;

    const focusable = receiptEditorRef.current
      ? Array.from(receiptEditorRef.current.querySelectorAll<HTMLElement>(
          'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
        )).filter((element) => element.offsetParent !== null)
      : [];
    if (focusable.length === 0) return;

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

  function changeAllocation(pendente: Pendente, value: string) {
    const amount = value === ""
      ? ""
      : String(round6(Math.max(0, Math.min(Number(value), Number(pendente.valorPendente)))));
    const next = { ...allocations, [pendente.id]: amount };
    const total = round6(sum(Object.values(next).map((item) => Number(item || 0))));
    setAllocations(next);
    if (!manualReceiptValue) {
      setForm((current) => ({ ...current, valorRecebido: total > 0 ? String(total) : "" }));
    }
  }

  function clearAllocations() {
    setAllocations({});
    if (!manualReceiptValue) {
      setForm((current) => ({ ...current, valorRecebido: "" }));
    }
  }

  function toggleAllocation(pendente: Pendente) {
    const currentAmount = round6(Number(allocations[pendente.id] || 0));
    if (currentAmount > 0) {
      changeAllocation(pendente, "");
      return;
    }

    const allocatedElsewhere = round6(sum(
      Object.entries(allocations)
        .filter(([id]) => Number(id) !== pendente.id)
        .map(([, value]) => Number(value || 0))
    ));
    const available = manualReceiptValue
      ? round6(Math.max(0, Number(form.valorRecebido || 0) - allocatedElsewhere))
      : Number(pendente.valorPendente);

    if (available <= 0) {
      setMessage("O valor recebido já está totalmente distribuído.");
      return;
    }

    setMessage(null);
    changeAllocation(pendente, String(Math.min(Number(pendente.valorPendente), available)));
  }

  function handleAllocationInputKeyDown(event: React.KeyboardEvent<HTMLInputElement>, pendente: Pendente) {
    if (event.key !== "Enter") return;
    event.preventDefault();
    toggleAllocation(pendente);
  }

  async function issueReceipt() {
    const validation = validateReceipt(form, receiptPendentes, allocations);
    if (validation) {
      setMessage(validation);
      return;
    }
    const cliente = clientes.find((item) => item.id === Number(form.clienteId));
    if (!window.confirm(`Emitir recebimento de ${money(receiptTarget)} ${form.moedaId} para ${cliente?.nome ?? form.clienteId}, distribuído por ${allocatedLines.length} pendentes?`)) return;
    setLoading(true);
    setMessage(null);
    try {
      const created = await sendJson<DocumentoFinanceiro>("/api/documentos-financeiros", {
        tipoDocumentoId: form.tipoDocumentoId,
        serie: form.serie,
        dataEmissao: form.dataEmissao,
        clienteId: Number(form.clienteId),
        moedaId: form.moedaId,
        mPagamentoId: form.mPagamentoId,
        dataHoraOperacao: null,
        emissorId: form.emissorId,
        observacoes: blankToNull(form.observacoes),
        linhas: allocatedLines.map(({ pendente, amount }) => ({
          pendenteId: pendente.id,
          valorALiquidar: amount,
          descontoPercentual: 0,
          descontoValor: 0
        }))
      });
      await loadTesouraria();
      closeReceipt();
      setCreatedReceipt(created);
      setNotice(`${financialReference(created)} emitido por ${money(created.valorPagamentoLiquido)} ${created.moedaId}. Pendentes atualizados.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível emitir o recebimento.");
    } finally {
      setLoading(false);
    }
  }

  async function annulFinancial(documento: DocumentoFinanceiro) {
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const diagnostico = await fetchJson<DiagnosticoFinanceiro>(`/api/documentos-financeiros/${documento.id}/diagnostico`);
      if (!diagnostico.podeAnular) {
        setMessage(diagnostico.bloqueios.join(" ") || "Este documento financeiro não pode ser anulado.");
        return;
      }
      if (!window.confirm(`Anular ${diagnostico.referencia}? Os valores recebidos serão repostos nos respetivos pendentes.`)) return;
      await sendJson<DocumentoFinanceiro>(`/api/documentos-financeiros/${documento.id}/anular`, null);
      await loadTesouraria();
      const updated = await fetchJson<DocumentoFinanceiro>(`/api/documentos-financeiros/${documento.id}`);
      setSelectedFinanceiro(updated);
      setNotice(`${diagnostico.referencia} anulado. Os pendentes foram repostos.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível anular o documento financeiro.");
    } finally {
      setLoading(false);
    }
  }

  async function openFinancialPdf(documento: DocumentoFinanceiro) {
    setLoading(true);
    setMessage(null);
    try {
      const response = await apiFetch(`/api/documentos-financeiros/${documento.id}/pdf`);
      if (!response.ok) throw new Error(await responseError(response));
      const url = URL.createObjectURL(await response.blob());
      window.open(url, "_blank", "noopener,noreferrer");
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
      setFinanceiros((current) => current.map((item) => item.id === documento.id ? { ...item, impresso: true } : item));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível gerar o PDF do recibo.");
    } finally {
      setLoading(false);
    }
  }

  const abertas = pendentes.filter((item) => Number(item.valorPendente) > 0);
  const clientesComPendentes = clientes.filter((cliente) => !cliente.inativo && abertas.some((item) => item.clienteId === cliente.id));
  const receiptPendentes = openPendentesForClient(pendentes, Number(form.clienteId))
    .filter((item) => !form.moedaId || item.moedaId === form.moedaId)
    .sort((a, b) => a.dataVencimento.localeCompare(b.dataVencimento) || a.numeroDocumento - b.numeroDocumento);
  const clientCurrencies = [...new Set(openPendentesForClient(pendentes, Number(form.clienteId)).map((item) => item.moedaId))];
  const receiptTarget = round6(Number(form.valorRecebido || 0));
  const allocatedLines = receiptPendentes
    .map((pendente) => ({ pendente, amount: round6(Number(allocations[pendente.id] || 0)) }))
    .filter((item) => item.amount > 0);
  const allocatedTotal = round6(sum(allocatedLines.map((item) => item.amount)));
  const difference = round6(receiptTarget - allocatedTotal);
  const availableSeries = series.filter((serie) => serie.tipoDocumentoId === form.tipoDocumentoId);
  const filteredPendentes = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return pendentes;
    return pendentes.filter((item) => [referencia(item), String(item.clienteId), String(item.id), estado(item)].some((value) => value.toLowerCase().includes(term)));
  }, [pendentes, search]);
  const selectedCliente = selectedFinanceiro ? clientes.find((cliente) => cliente.id === selectedFinanceiro.clienteId) ?? null : null;
  const selectedModo = selectedFinanceiro ? modos.find((modo) => modo.id === selectedFinanceiro.mPagamentoId) ?? null : null;
  const pendenteById = useMemo(() => new Map(pendentes.map((pendente) => [pendente.id, pendente])), [pendentes]);

  return <>
    {notice && <div className="fac-editor-message"><p>{notice}</p>{createdReceipt && <div className="fac-inline-actions"><button className="fac-gold-button" disabled={loading} onClick={() => openFinancialPdf(createdReceipt)} type="button">Abrir PDF</button><button className="fac-soft-button" disabled={loading} onClick={() => openFinancialDetail(createdReceipt)} type="button">Ver detalhe</button></div>}</div>}
    {message && <p className="fac-message">{message}</p>}

    <section className="fac-hero">
      <div><p className="fac-eyebrow">Tesouraria</p><h2>Recebimentos por cliente</h2><p>Consulta recebimentos emitidos, abre o detalhe de cada recibo e cria novos recebimentos a partir dos pendentes.</p></div>
      <div className="fac-hero-card"><span>Saldo em aberto</span><strong>{money(sum(abertas.map((item) => item.valorPendente)))} EUR</strong><small>{abertas.length} pendentes ativos</small></div>
    </section>

    {!receiptOpen && !selectedFinanceiroId && <section className="fac-panel fac-section-panel fac-receipt-action-panel"><div className="fac-panel-header"><div><p className="fac-eyebrow">Novo recebimento</p><h2>Liquidar pendentes</h2><p className="fac-muted">Cria um recibo a partir dos documentos em aberto selecionados.</p></div><div className="fac-inline-actions"><button className="fac-soft-button" disabled={loading} onClick={loadTesouraria} type="button">Atualizar</button>{canManageTreasury && <button className="fac-primary-button" disabled={loading || clientesComPendentes.length === 0} onClick={openReceipt} ref={newReceiptButtonRef} type="button">Novo recebimento</button>}</div></div></section>}

    {!receiptOpen && !selectedFinanceiroId && <section className="fac-panel fac-section-panel"><div className="fac-panel-header"><div><p className="fac-eyebrow">Documentos financeiros</p><h2>Recebimentos emitidos</h2></div><div className="fac-inline-actions"><span className="fac-muted">{financeiros.length} documentos</span><button className="fac-ghost-button" onClick={() => setFinanceiroColumnsOpen((current) => !current)} type="button">Colunas ({financeiroColumns.visibleColumns.length})</button></div></div><ColumnSelector columns={financeiroColumns.columns} open={financeiroColumnsOpen} onMove={financeiroColumns.moveColumn} onReset={financeiroColumns.resetColumns} onToggle={financeiroColumns.toggleColumn}/><table className="fac-table"><thead><tr>{financeiroColumns.visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>{financeiros.map((documento) => <tr key={documento.id}>{financeiroColumns.visibleColumns.map((column) => <td key={column.key}>{financeiroColumnValue(documento, column.key, openFinancialDetail)}</td>)}</tr>)}{!loading && financeiros.length === 0 && <tr><td colSpan={financeiroColumns.visibleColumns.length}>Sem documentos financeiros para mostrar.</td></tr>}</tbody></table></section>}

    {!receiptOpen && selectedFinanceiroId && <section className="fac-panel fac-section-panel fac-financial-detail"><div className="fac-panel-header"><div><p className="fac-eyebrow">Recibo</p><h2 id="financial-detail-heading" tabIndex={-1}>{selectedFinanceiro ? financialReference(selectedFinanceiro) : "A carregar recebimento..."}</h2>{selectedFinanceiro && <p className="fac-muted">Emitido em {datePt(selectedFinanceiro.dataEmissao)}</p>}</div><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={closeFinancialDetail} type="button">Voltar à lista</button>{selectedFinanceiro && hasPermission("DOCUMENTO_OBTER_PDF") && <button className="fac-gold-button" disabled={loading || detailLoading} onClick={() => openFinancialPdf(selectedFinanceiro)} type="button">Abrir PDF</button>}{selectedFinanceiro && canAnnul && !selectedFinanceiro.anulado && <button className="fac-link-danger" disabled={loading || detailLoading} onClick={() => annulFinancial(selectedFinanceiro)} type="button">Anular recebimento</button>}</div></div>{detailLoading && <p className="fac-muted">A carregar detalhe...</p>}{selectedFinanceiro && <><div className="fac-detail-grid"><section><h3>Identificação</h3><dl><div><dt>Documento</dt><dd>{financialReference(selectedFinanceiro)}</dd></div><div><dt>Estado</dt><dd><span className={`fac-status ${selectedFinanceiro.anulado ? "danger" : ""}`}>{selectedFinanceiro.anulado ? "ANULADO" : "EMITIDO"}</span></dd></div><div><dt>Data</dt><dd>{datePt(selectedFinanceiro.dataEmissao)}</dd></div><div><dt>Cliente</dt><dd>{selectedCliente ? `${selectedCliente.nome} - ${selectedCliente.nif}` : selectedFinanceiro.clienteId}</dd></div><div><dt>Moeda</dt><dd>{selectedFinanceiro.moedaId}</dd></div><div><dt>Modo de pagamento</dt><dd>{selectedModo?.nome ?? selectedFinanceiro.mPagamentoId}</dd></div></dl></section><section><h3>Valores</h3><dl><div><dt>Total recebido</dt><dd>{money(selectedFinanceiro.valorPagamentoLiquido)} {selectedFinanceiro.moedaId}</dd></div><div><dt>Valor bruto</dt><dd>{money(selectedFinanceiro.valorPagamentoBruto ?? selectedFinanceiro.valorPagamentoLiquido)} {selectedFinanceiro.moedaId}</dd></div><div><dt>Desconto financeiro</dt><dd>{money(selectedFinanceiro.valorDescontoFinanceiro ?? 0)} {selectedFinanceiro.moedaId}</dd></div><div><dt>Valor aplicado</dt><dd>{money(sum((selectedFinanceiro.linhas ?? []).map((linha) => linha.valorALiquidar)))} {selectedFinanceiro.moedaId}</dd></div></dl></section><section><h3>Auditoria funcional</h3><dl><div><dt>Emitido por</dt><dd>{selectedFinanceiro.emissorId}</dd></div><div><dt>Emitido em</dt><dd>{dateTimePt(selectedFinanceiro.momentoEmissao)}</dd></div><div><dt>Operação</dt><dd>{dateTimePt(selectedFinanceiro.dataHoraOperacao)}</dd></div><div><dt>Anulação</dt><dd>{selectedFinanceiro.anulado ? "Anulado" : "Não anulado"}</dd></div><div><dt>Observações</dt><dd>{selectedFinanceiro.observacoes || "-"}</dd></div></dl></section></div><section className="fac-financial-lines"><div className="fac-panel-header compact"><div><p className="fac-eyebrow">Liquidações</p><h3>Documentos comerciais associados</h3></div><span className="fac-muted">{selectedFinanceiro.linhas?.length ?? 0} linhas</span></div><table className="fac-table"><thead><tr><th>Documento</th><th>Vencimento</th><th>Valor documento</th><th>Saldo anterior</th><th>Liquidado</th><th>Saldo posterior</th></tr></thead><tbody>{(selectedFinanceiro.linhas ?? []).map((linha) => { const documentoComercialId = pendenteById.get(linha.pendenteId)?.documentoComercialId; return <tr key={linha.id}><td>{documentoComercialId ? <button className="fac-table-link" onClick={() => navigate(`/documentos/${documentoComercialId}`)} onKeyDown={(event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); navigate(`/documentos/${documentoComercialId}`); } }} type="button">{lineReference(linha)}</button> : lineReference(linha)}</td><td>{datePt(linha.dataVencimento)}</td><td>{money(linha.valorDocumento)} {linha.moedaId}</td><td>{money(linha.valorPendenteAntes)} {linha.moedaId}</td><td>{money(linha.valorALiquidar)} {linha.moedaId}</td><td>{money(linha.novoValorPendente)} {linha.moedaId}</td></tr>; })}{(!selectedFinanceiro.linhas || selectedFinanceiro.linhas.length === 0) && <tr><td colSpan={6}>Sem liquidações associadas.</td></tr>}</tbody></table></section></>}</section>}

    {receiptOpen && <section aria-label="Novo recebimento" className="fac-panel fac-section-panel fac-emission-panel" onKeyDown={handleReceiptKeyDown} ref={receiptEditorRef}>
      <div className="fac-panel-header"><div><p className="fac-eyebrow">Novo documento financeiro</p><h2>Distribuir recebimento</h2></div><button className="fac-ghost-button" onClick={closeReceipt} type="button">Cancelar</button></div>
      <div className="fac-form-grid">
        <Field label="Cliente"><select onChange={(event) => selectClient(event.target.value)} ref={clientSelectRef} value={form.clienteId}><option value="">Selecionar cliente</option>{clientesComPendentes.map((cliente) => <option key={cliente.id} value={cliente.id}>{cliente.nome} - {cliente.nif}</option>)}</select></Field>
        <Field label="Moeda"><select data-receipt-currency disabled={!form.clienteId || clientCurrencies.length <= 1} onChange={(event) => { setForm((current) => ({ ...current, moedaId: event.target.value, valorRecebido: "" })); setAllocations({}); setManualReceiptValue(false); requestAnimationFrame(() => receiptEditorRef.current?.querySelector<HTMLElement>("[data-receipt-value]")?.focus()); }} value={form.moedaId}><option value="">Selecionar moeda</option>{clientCurrencies.map((moeda) => <option key={moeda} value={moeda}>{moeda}</option>)}</select></Field>
        <Field label="Valor recebido"><input data-receipt-value disabled={!form.moedaId} min="0.000001" onChange={(event) => { const value = event.target.value; setForm((current) => ({ ...current, valorRecebido: value })); setAllocations({}); setManualReceiptValue(value !== ""); }} step="0.000001" type="number" value={form.valorRecebido}/></Field>
        <Field label="Modo de pagamento"><select onChange={(event) => setForm((current) => ({ ...current, mPagamentoId: event.target.value }))} value={form.mPagamentoId}><option value="">Confirmar modo</option>{modos.map((modo) => <option key={modo.id} value={modo.id}>{modo.nome}</option>)}</select></Field>
        <Field label="Tipo de documento"><select onChange={(event) => { const tipoDocumentoId = event.target.value; setForm((current) => ({ ...current, tipoDocumentoId, serie: series.find((item) => item.tipoDocumentoId === tipoDocumentoId)?.serie ?? "" })); }} value={form.tipoDocumentoId}><option value="">Selecionar</option>{tipos.map((tipo) => <option key={tipo.id} value={tipo.id}>{tipo.id} - {tipo.descricao}</option>)}</select></Field>
        <Field label="Série"><select onChange={(event) => setForm((current) => ({ ...current, serie: event.target.value }))} value={form.serie}><option value="">Selecionar</option>{availableSeries.map((serie) => <option key={`${serie.tipoDocumentoId}-${serie.serie}`} value={serie.serie}>{serie.serie} - {serie.nome}</option>)}</select></Field>
        <Field label="Data de emissão"><input onChange={(event) => setForm((current) => ({ ...current, dataEmissao: event.target.value }))} type="date" value={form.dataEmissao}/></Field>
        <Field label="Emissor"><input disabled value={getAuthSession()?.nome ?? "Utilizador autenticado"}/></Field>
      </div>

      <div className="fac-receipt-totals"><div><span>Valor recebido</span><strong>{money(receiptTarget)} {form.moedaId}</strong></div><div><span>Distribuído</span><strong>{money(allocatedTotal)} {form.moedaId}</strong></div><div className={difference === 0 && receiptTarget > 0 ? "balanced" : "unbalanced"}><span>Diferença</span><strong>{money(difference)} {form.moedaId}</strong></div></div>
      <div className="fac-inline-actions"><button className="fac-soft-button" disabled={!form.valorRecebido || !form.moedaId} onClick={distributeReceipt} type="button">Distribuir por antiguidade</button><button className="fac-ghost-button" disabled={allocatedTotal === 0} onClick={clearAllocations} type="button">Limpar distribuição</button></div>

      <table className="fac-table fac-allocation-table"><thead><tr><th>Documento</th><th>Emissão</th><th>Vencimento</th><th>Valor original</th><th>Pendente antes</th><th>Valor a liquidar</th><th>Novo pendente</th></tr></thead><tbody>
        {receiptPendentes.map((pendente) => { const amount = round6(Number(allocations[pendente.id] || 0)); return <tr aria-label={`${referencia(pendente)}: clicar para atribuir ou limpar o valor a liquidar`} className={`fac-allocation-row ${amount > 0 ? "allocated" : ""}`} key={pendente.id} onClick={(event) => { if (!(event.target as HTMLElement).closest("input, button, select, textarea")) toggleAllocation(pendente); }}><td>{referencia(pendente)}</td><td>{datePt(pendente.dataDocumento)}</td><td>{datePt(pendente.dataVencimento)}</td><td>{money(pendente.valorDocumento)} {pendente.moedaId}</td><td>{money(pendente.valorPendente)} {pendente.moedaId}</td><td><input aria-label={`Valor a liquidar de ${referencia(pendente)}`} className="fac-table-input" max={pendente.valorPendente} min="0" onChange={(event) => changeAllocation(pendente, event.target.value)} onKeyDown={(event) => handleAllocationInputKeyDown(event, pendente)} step="0.000001" type="number" value={allocations[pendente.id] ?? ""}/></td><td>{money(round6(pendente.valorPendente - amount))} {pendente.moedaId}</td></tr>; })}
        {form.clienteId && form.moedaId && receiptPendentes.length === 0 && <tr><td colSpan={7}>Este cliente não tem pendentes em aberto nesta moeda.</td></tr>}
      </tbody></table>

      <div className="fac-form-grid"><Field label="Observações"><textarea maxLength={250} onChange={(event) => setForm((current) => ({ ...current, observacoes: event.target.value }))} value={form.observacoes}/></Field></div>
      <div className="fac-form-footer"><span className="fac-muted">O recibo só pode ser emitido quando o valor recebido estiver totalmente distribuído pelos pendentes.</span><button className="fac-gold-button" disabled={loading || receiptTarget <= 0 || difference !== 0 || allocatedLines.length === 0} onClick={issueReceipt} type="button">Emitir recebimento</button></div>
    </section>}

    {!receiptOpen && !selectedFinanceiroId && <>
    <section className="fac-panel fac-section-panel"><div className="fac-panel-header"><div><p className="fac-eyebrow">Pendentes</p><h2>Conta corrente em aberto e liquidada</h2></div><div className="fac-inline-actions"><input className="fac-list-search" onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar pendente, cliente ou estado" type="search" value={search}/><span className="fac-muted">{filteredPendentes.length} registos</span><button className="fac-ghost-button" onClick={() => setPendenteColumnsOpen((current) => !current)} type="button">Colunas ({pendenteColumns.visibleColumns.length})</button></div></div><ColumnSelector columns={pendenteColumns.columns} open={pendenteColumnsOpen} onMove={pendenteColumns.moveColumn} onReset={pendenteColumns.resetColumns} onToggle={pendenteColumns.toggleColumn}/><table className="fac-table"><thead><tr>{pendenteColumns.visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>{filteredPendentes.map((item) => <tr key={item.id}>{pendenteColumns.visibleColumns.map((column) => <td key={column.key}>{pendenteColumnValue(item, column.key)}</td>)}</tr>)}{!loading && filteredPendentes.length === 0 && <tr><td colSpan={pendenteColumns.visibleColumns.length}>Sem pendentes para mostrar.</td></tr>}</tbody></table></section>
    </>}
  </>;
}

function emptyReceiptForm(): ReceiptForm { return { clienteId: "", moedaId: "", tipoDocumentoId: "", serie: "", dataEmissao: todayIso(), valorRecebido: "", mPagamentoId: "", emissorId: getAuthSession()?.codigo ?? "", observacoes: "" }; }
function openPendentesForClient(pendentes: Pendente[], clienteId: number) { return clienteId ? pendentes.filter((item) => item.clienteId === clienteId && Number(item.valorPendente) > 0) : []; }
function validPaymentMode(modos: MPagamento[], mPagamentoId?: string | null) { return mPagamentoId && modos.some((modo) => modo.id === mPagamentoId) ? mPagamentoId : ""; }
function validateReceipt(form: ReceiptForm, pendentes: Pendente[], allocations: Allocations) { if (!form.clienteId) return "Seleciona o cliente."; if (!form.moedaId) return "Seleciona a moeda."; if (!form.tipoDocumentoId) return "Seleciona o tipo de documento financeiro."; if (!form.serie) return "Seleciona a série."; if (!form.dataEmissao) return "A data de emissão é obrigatória."; if (!form.mPagamentoId) return "Confirma o modo de pagamento."; const target = round6(Number(form.valorRecebido)); if (!Number.isFinite(target) || target <= 0) return "O valor recebido deve ser positivo."; const total = round6(sum(pendentes.map((item) => Number(allocations[item.id] || 0)))); if (total <= 0) return "Distribui o recebimento por pelo menos um pendente."; if (round6(target - total) !== 0) return "O valor recebido e a distribuição pelos pendentes não coincidem."; return null; }
function estado(item: Pendente) { if (Number(item.valorPendente) <= 0) return "LIQUIDADO"; if (item.dataVencimento < todayIso()) return "VENCIDO"; if (Number(item.valorPendente) < Number(item.valorDocumento)) return "PARCIAL"; return "ABERTO"; }
function referencia(item: Pendente) { return `${item.tipoDocumentoId} ${item.serieDocumento}/${item.numeroDocumento}`; }
function pendenteColumnValue(item: Pendente, key: string) { switch (key) { case "documento": return referencia(item); case "cliente": return item.clienteId; case "emissao": return datePt(item.dataDocumento); case "vencimento": return datePt(item.dataVencimento); case "moeda": return item.moedaId; case "original": return `${money(item.valorDocumento)} ${item.moedaId}`; case "pendente": return `${money(item.valorPendente)} ${item.moedaId}`; case "estado": return <span className="fac-status">{estado(item)}</span>; default: return "-"; } }
function financeiroColumnValue(documento: DocumentoFinanceiro, key: string, onOpen: (documento: DocumentoFinanceiro) => void) { switch (key) { case "documento": return <button className="fac-table-link" onClick={() => onOpen(documento)} type="button">{financialReference(documento)}</button>; case "cliente": return documento.clienteId; case "data": return datePt(documento.dataEmissao); case "modo": return documento.mPagamentoId; case "moeda": return documento.moedaId; case "liquido": return `${money(documento.valorPagamentoLiquido)} ${documento.moedaId}`; case "emissor": return documento.emissorId; case "estado": return <span className={`fac-status ${documento.anulado ? "danger" : ""}`}>{documento.anulado ? "ANULADO" : "EMITIDO"}</span>; default: return "-"; } }
function financialReference(documento: DocumentoFinanceiro) { return `${documento.tipoDocumentoId} ${documento.serie}/${documento.numeroDocumento}`; }
function lineReference(linha: LinhaFinanceira) { return `${linha.tipoDocumentoId} ${linha.serieDocumento}/${linha.numeroDocumento}`; }
function datePt(value: string) { return value ? value.split("-").reverse().join("/") : "-"; }
function dateTimePt(value?: string | null) { return value ? new Date(value).toLocaleString("pt-PT") : "-"; }
function money(value: number) { return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function sum(values: number[]) { return values.reduce((total, value) => total + Number(value || 0), 0); }
function round6(value: number) { return Math.round((value + Number.EPSILON) * 1_000_000) / 1_000_000; }
function todayIso() { const now = new Date(); const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000); return local.toISOString().slice(0, 10); }
function blankToNull(value: string) { return value.trim() || null; }
function Field({ children, label }: { children: React.ReactNode; label: string }) { return <label className="fac-field"><span>{label}</span>{children}</label>; }
async function fetchJson<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function sendJson<T>(url: string, body: unknown): Promise<T> { const response = await apiFetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: body == null ? undefined : JSON.stringify(body) }); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
