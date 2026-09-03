import { useEffect, useMemo, useRef, useState } from "react";
import { Paginator } from "primereact/paginator";
import { useNavigate } from "react-router-dom";
import { apiFetch, getAuthSession, hasPermission } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";
import { EntityLookupField, type EntityLookupColumn, type EntityLookupSearchField } from "./ui/fac/components/EntityLookup";
import { money as formatMoney } from "./ui/tuuli/format";

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
type Cliente = { id: number; nome: string; nif: string; email?: string | null; localidade?: string | null; inativo: boolean; moedaId?: string | null; mPagamentoId?: string | null; pPagamentoId?: string | null };
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
type ReceiptPostAction = "DETAIL" | "PDF";

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
const CLIENT_LOOKUP_COLUMNS: EntityLookupColumn<Cliente>[] = [
  { defaultVisible: true, field: "id", filterable: true, globalSearch: true, header: "Código", required: true, sortable: true, width: "7rem" },
  { defaultVisible: true, field: "nome", filterable: true, globalSearch: true, header: "Nome", required: true, sortable: true },
  { defaultVisible: true, field: "nif", filterable: true, globalSearch: true, header: "NIF", sortable: true, width: "9rem" },
  { defaultVisible: true, field: "email", filterable: true, globalSearch: true, header: "Email", sortable: true },
  { body: (cliente) => cliente.inativo ? "Sim" : "Não", field: "inativo", header: "Inativo", sortable: true, width: "7rem" }
];
const CLIENT_SEARCH_FIELDS: EntityLookupSearchField<Cliente>[] = [
  { aliases: ["codigo", "código"], fields: ["id"], key: "id" },
  { fields: ["nome"], key: "nome" },
  { fields: ["nif"], key: "nif" },
  { fields: ["email"], key: "email" },
  { fields: ["localidade"], key: "localidade" }
];

export default function PendentesView() {
  const navigate = useNavigate();
  const canManageTreasury = hasPermission("TESOURARIA_GERIR");
  const canAnnul = hasPermission("DOCUMENTO_ANULAR");
  const receiptEditorRef = useRef<HTMLElement | null>(null);
  const newReceiptButtonRef = useRef<HTMLButtonElement | null>(null);
  const receiptSubmittingRef = useRef(false);
  const [pendentes, setPendentes] = useState<Pendente[]>([]);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [financeiros, setFinanceiros] = useState<DocumentoFinanceiro[]>([]);
  const [tipos, setTipos] = useState<TipoDocumento[]>([]);
  const [series, setSeries] = useState<Serie[]>([]);
  const [modos, setModos] = useState<MPagamento[]>([]);
  const [search, setSearch] = useState("");
  const [financeiroDateFrom, setFinanceiroDateFrom] = useState(currentYearStartIso);
  const [financeiroDateTo, setFinanceiroDateTo] = useState(todayIso);
  const [showAnnulledFinanceiros, setShowAnnulledFinanceiros] = useState(false);
  const [financeirosPage, setFinanceirosPage] = useState(0);
  const [financeirosPageSize, setFinanceirosPageSize] = useState(10);
  const [dueFilter, setDueFilter] = useState<"all" | "overdue" | "not-overdue">("all");
  const [excludeSettled, setExcludeSettled] = useState(false);
  const [pendentesPage, setPendentesPage] = useState(0);
  const [pendentesPageSize, setPendentesPageSize] = useState(10);
  const [exportingPendentes, setExportingPendentes] = useState<"pdf" | "xlsx" | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [receiptOpen, setReceiptOpen] = useState(false);
  const [form, setForm] = useState<ReceiptForm>(emptyReceiptForm());
  const [initialReceiptFormKey, setInitialReceiptFormKey] = useState("");
  const [receiptSubmitting, setReceiptSubmitting] = useState(false);
  const [allocations, setAllocations] = useState<Allocations>({});
  const [manualReceiptValue, setManualReceiptValue] = useState(false);
  const [clientFocusRequest, setClientFocusRequest] = useState(0);
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
    setClientFocusRequest((current) => current + 1);
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
      const nextForm = {
        ...emptyReceiptForm(),
        tipoDocumentoId: tipoInicial,
        serie: seriesFinanceiras.find((serie) => serie.tipoDocumentoId === tipoInicial)?.serie ?? "",
        emissorId: getAuthSession()?.codigo ?? ""
      };
      setForm(nextForm);
      setInitialReceiptFormKey(receiptFormKey(nextForm));
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
    setInitialReceiptFormKey("");
    requestAnimationFrame(() => newReceiptButtonRef.current?.focus());
  }

  async function exportPendentes(format: "pdf" | "xlsx") {
    setExportingPendentes(format);
    setMessage(null);
    try {
      const params = new URLSearchParams({ apenasVencidos: String(dueFilter === "overdue") });
      const response = await apiFetch(`/api/listagens/pendentes/exportar/${format}?${params}`);
      if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filenameFromDisposition(response.headers.get("Content-Disposition")) ?? `pendentes.${format}`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível exportar os pendentes.");
    } finally {
      setExportingPendentes(null);
    }
  }

  function hasReceiptChanges() {
    return Boolean(
      receiptFormKey(form) !== initialReceiptFormKey
      || Object.values(allocations).some((value) => Number(value || 0) > 0)
      || manualReceiptValue
    );
  }

  function backToReceiptList() {
    if (hasReceiptChanges()
        && !window.confirm("Existem dados de um recebimento ainda não emitido. Pretende sair e perder estas alterações?")) {
      return;
    }
    closeReceipt();
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
    if (!clienteId) {
      setForm((current) => ({
        ...current,
        clienteId: "",
        moedaId: "",
        mPagamentoId: "",
        valorRecebido: ""
      }));
      setAllocations({});
      setManualReceiptValue(false);
      return;
    }
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
    setMessage(remaining > 0 ? `Ficam ${formatMoney(remaining)} ${form.moedaId} por distribuir porque o valor recebido excede os pendentes disponíveis.` : null);
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

  async function issueReceipt(postAction: ReceiptPostAction = "DETAIL") {
    if (receiptSubmittingRef.current) return;
    const validation = validateReceipt(form, receiptPendentes, allocations);
    if (validation) {
      setMessage(validation);
      return;
    }
    const cliente = clientes.find((item) => item.id === Number(form.clienteId));
    const pdfMessage = postAction === "PDF" ? " O PDF será aberto após a emissão." : "";
    if (!window.confirm(`Confirmar a emissão deste recibo de ${formatMoney(receiptTarget)} ${form.moedaId} para ${cliente?.nome ?? form.clienteId}? Após a emissão, o documento deixa de poder ser editado.${pdfMessage}`)) return;
    receiptSubmittingRef.current = true;
    setReceiptSubmitting(true);
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
      closeReceipt();
      setCreatedReceipt(created);
      setNotice(`${financialReference(created)} emitido por ${formatMoney(created.valorPagamentoLiquido)} ${created.moedaId}. Pendentes atualizados.`);
      await loadTesouraria();
      await openFinancialDetail(created);
      if (postAction === "PDF") {
        await openFinancialPdf(created);
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível emitir o recebimento.");
    } finally {
      receiptSubmittingRef.current = false;
      setReceiptSubmitting(false);
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
      if (!window.confirm(`Anular este recibo? Os valores liquidados em ${diagnostico.referencia} voltarão a ficar pendentes nos documentos respetivos.`)) return;
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
  const receiptCliente = clientesComPendentes.find((cliente) => cliente.id === Number(form.clienteId)) ?? null;
  const receiptPendentes = openPendentesForClient(pendentes, Number(form.clienteId))
    .filter((item) => !form.moedaId || item.moedaId === form.moedaId)
    .sort((a, b) => a.dataVencimento.localeCompare(b.dataVencimento) || a.numeroDocumento - b.numeroDocumento);
  const clientCurrencies = [...new Set(openPendentesForClient(pendentes, Number(form.clienteId)).map((item) => item.moedaId))];
  const receiptTarget = round6(Number(form.valorRecebido || 0));
  const allocatedLines = receiptPendentes
    .map((pendente) => ({ pendente, amount: round6(Number(allocations[pendente.id] || 0)) }))
    .filter((item) => item.amount > 0);
  const allocatedTotal = round6(sum(allocatedLines.map((item) => item.amount)));
  const totalPendenteCliente = round6(sum(receiptPendentes.map((pendente) => Number(pendente.valorPendente) || 0)));
  const difference = round6(receiptTarget - allocatedTotal);
  const availableSeries = series.filter((serie) => serie.tipoDocumentoId === form.tipoDocumentoId);
  const canIssueReceipt = !loading && !receiptSubmitting && !validateReceipt(form, receiptPendentes, allocations);
  const issueButtonLabel = receiptSubmitting ? "A emitir..." : "Emitir recebimento";
  const issuePdfButtonLabel = receiptSubmitting ? "A emitir..." : "Emitir e abrir PDF";
  const filteredPendentes = useMemo(() => {
    const term = search.trim().toLowerCase();
    return pendentes.filter((item) => {
      const itemEstado = estado(item);
      if (excludeSettled && itemEstado === "LIQUIDADO") return false;
      if (dueFilter === "overdue" && itemEstado !== "VENCIDO") return false;
      if (dueFilter === "not-overdue" && itemEstado === "VENCIDO") return false;
      return !term || [referencia(item), String(item.clienteId), String(item.id), itemEstado].some((value) => value.toLowerCase().includes(term));
    });
  }, [dueFilter, excludeSettled, pendentes, search]);
  const pagedPendentes = filteredPendentes.slice(pendentesPage * pendentesPageSize, (pendentesPage + 1) * pendentesPageSize);
  const subtotalPendentes = summarizePendentes(pagedPendentes);
  const totalFilteredPendentes = summarizePendentes(filteredPendentes);
  const filteredFinanceiros = useMemo(() => financeiros.filter((documento) => {
    if (!showAnnulledFinanceiros && documento.anulado) return false;
    if (financeiroDateFrom && documento.dataEmissao < financeiroDateFrom) return false;
    if (financeiroDateTo && documento.dataEmissao > financeiroDateTo) return false;
    return true;
  }), [financeiroDateFrom, financeiroDateTo, financeiros, showAnnulledFinanceiros]);
  const pagedFinanceiros = filteredFinanceiros.slice(financeirosPage * financeirosPageSize, (financeirosPage + 1) * financeirosPageSize);

  useEffect(() => {
    setPendentesPage(0);
  }, [dueFilter, excludeSettled, search]);

  useEffect(() => {
    const lastPage = Math.max(0, Math.ceil(filteredPendentes.length / pendentesPageSize) - 1);
    if (pendentesPage > lastPage) setPendentesPage(lastPage);
  }, [filteredPendentes.length, pendentesPage, pendentesPageSize]);

  useEffect(() => {
    setFinanceirosPage(0);
  }, [financeiroDateFrom, financeiroDateTo, showAnnulledFinanceiros]);

  useEffect(() => {
    const lastPage = Math.max(0, Math.ceil(filteredFinanceiros.length / financeirosPageSize) - 1);
    if (financeirosPage > lastPage) setFinanceirosPage(lastPage);
  }, [filteredFinanceiros.length, financeirosPage, financeirosPageSize]);
  const selectedCliente = selectedFinanceiro ? clientes.find((cliente) => cliente.id === selectedFinanceiro.clienteId) ?? null : null;
  const selectedModo = selectedFinanceiro ? modos.find((modo) => modo.id === selectedFinanceiro.mPagamentoId) ?? null : null;
  const selectedAppliedTotal = selectedFinanceiro ? round6(sum((selectedFinanceiro.linhas ?? []).map((linha) => linha.valorALiquidar))) : 0;
  const selectedReceiptDifference = selectedFinanceiro ? round6(Number(selectedFinanceiro.valorPagamentoLiquido || 0) - selectedAppliedTotal) : 0;
  const selectedReceiptStatus = selectedFinanceiro ? receiptStatusLabel(selectedFinanceiro) : "";
  const pendenteById = useMemo(() => new Map(pendentes.map((pendente) => [pendente.id, pendente])), [pendentes]);

  return <>
    {notice && <div className="fac-editor-message"><p>{notice}</p>{createdReceipt && <div className="fac-inline-actions"><button className="fac-gold-button" disabled={loading} onClick={() => openFinancialPdf(createdReceipt)} type="button">Abrir PDF</button><button className="fac-soft-button" disabled={loading} onClick={() => openFinancialDetail(createdReceipt)} type="button">Ver detalhe</button></div>}</div>}
    {message && <p className="fac-message">{message}</p>}

    {!receiptOpen && !selectedFinanceiroId && <div className="tuuli-v2-page tuuli-receipts-page">
      <section className="tuuli-receipts-context" aria-label="Contexto de recebimentos">
        <div className="tuuli-metric">
          <span>Saldo em aberto</span>
          <strong>{formatMoney(sum(abertas.map((item) => item.valorPendente)))} EUR</strong>
          <small>{abertas.length} pendentes ativos</small>
        </div>
        <div className="tuuli-receipts-page-actions">
          <button className="fac-ghost-button tuuli-tool-action" disabled={loading} onClick={loadTesouraria} type="button">Atualizar</button>
          {canManageTreasury && <button className="fac-primary-button tuuli-primary-action" disabled={loading || clientesComPendentes.length === 0} onClick={openReceipt} ref={newReceiptButtonRef} type="button">Novo recebimento</button>}
        </div>
      </section>

      <section className="tuuli-receipts-section">
        <h2>Recebimentos emitidos</h2>
        <div className="tuuli-toolbar tuuli-receipts-toolbar tuuli-receipts-issued-toolbar">
          <div className="tuuli-receipts-date-filters">
            <label><span>Data inicial</span><input max={financeiroDateTo || undefined} onChange={(event) => setFinanceiroDateFrom(event.target.value)} type="date" value={financeiroDateFrom}/></label>
            <label><span>Data final</span><input min={financeiroDateFrom || undefined} onChange={(event) => setFinanceiroDateTo(event.target.value)} type="date" value={financeiroDateTo}/></label>
            <label className="tuuli-inline-control"><input checked={showAnnulledFinanceiros} onChange={(event) => setShowAnnulledFinanceiros(event.target.checked)} type="checkbox"/><span>Mostrar anulados</span></label>
          </div>
          <div className="tuuli-receipts-toolbar-actions"><span className="tuuli-meta">{filteredFinanceiros.length} documentos</span><button className="fac-ghost-button tuuli-tool-action" onClick={() => setFinanceiroColumnsOpen((current) => !current)} type="button">Colunas ({financeiroColumns.visibleColumns.length})</button></div>
        </div>
        <ColumnSelector columns={financeiroColumns.columns} open={financeiroColumnsOpen} onMove={financeiroColumns.moveColumn} onReset={financeiroColumns.resetColumns} onToggle={financeiroColumns.toggleColumn}/>
        <div className="tuuli-table-surface tuuli-receipts-table-surface"><table className="tuuli-table"><thead><tr>{financeiroColumns.visibleColumns.map((column) => <th className={receiptColumnClass(column.key)} key={column.key}>{column.label}</th>)}</tr></thead><tbody>{pagedFinanceiros.map((documento) => <tr key={documento.id}>{financeiroColumns.visibleColumns.map((column) => <td className={receiptColumnClass(column.key)} key={column.key}>{financeiroColumnValue(documento, column.key, openFinancialDetail)}</td>)}</tr>)}{!loading && filteredFinanceiros.length === 0 && <tr><td colSpan={financeiroColumns.visibleColumns.length}>Sem documentos financeiros para mostrar neste período.</td></tr>}</tbody></table></div>
        {filteredFinanceiros.length > 0 && <div className="tuuli-pagination"><span>{filteredFinanceiros.length} {filteredFinanceiros.length === 1 ? "documento" : "documentos"}</span><Paginator first={financeirosPage * financeirosPageSize} onPageChange={(event) => { setFinanceirosPage(event.page); setFinanceirosPageSize(event.rows); }} rows={financeirosPageSize} rowsPerPageOptions={[10, 20, 50]} totalRecords={filteredFinanceiros.length}/></div>}
      </section>

      <section className="tuuli-receipts-section tuuli-receipts-current-account">
        <h2>Conta corrente em aberto e liquidada</h2>
        <div className="tuuli-toolbar tuuli-receipts-toolbar tuuli-receipts-current-toolbar">
          <div className="tuuli-receipts-filter-group">
            <label className="tuuli-search"><i aria-hidden="true" className="pi pi-search"/><input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar pendente, cliente ou estado" type="search" value={search}/></label>
            <select aria-label="Filtrar por vencimento" className="tuuli-receipts-select" onChange={(event) => setDueFilter(event.target.value as "all" | "overdue" | "not-overdue")} value={dueFilter}><option value="all">Vencidos e não vencidos</option><option value="overdue">Vencidos</option><option value="not-overdue">Não vencidos</option></select>
            <label className="tuuli-inline-control"><input checked={excludeSettled} onChange={(event) => setExcludeSettled(event.target.checked)} type="checkbox"/><span>Excluir totalmente liquidados</span></label>
          </div>
          <div className="tuuli-receipts-toolbar-actions">
            <div className="tuuli-receipts-export-actions"><button className="fac-ghost-button tuuli-tool-action" disabled={exportingPendentes !== null} onClick={() => exportPendentes("pdf")} type="button">{exportingPendentes === "pdf" ? "A gerar PDF..." : "Exportar PDF"}</button><button className="fac-ghost-button tuuli-tool-action" disabled={exportingPendentes !== null} onClick={() => exportPendentes("xlsx")} type="button">{exportingPendentes === "xlsx" ? "A gerar Excel..." : "Exportar Excel"}</button></div>
            <div className="tuuli-receipts-meta-actions"><span className="tuuli-meta">{filteredPendentes.length} registos</span><button className="fac-ghost-button tuuli-tool-action" onClick={() => setPendenteColumnsOpen((current) => !current)} type="button">Colunas ({pendenteColumns.visibleColumns.length})</button></div>
          </div>
        </div>
        <ColumnSelector columns={pendenteColumns.columns} open={pendenteColumnsOpen} onMove={pendenteColumns.moveColumn} onReset={pendenteColumns.resetColumns} onToggle={pendenteColumns.toggleColumn}/>
        <div className="tuuli-table-surface tuuli-receipts-table-surface"><table className="tuuli-table"><thead><tr>{pendenteColumns.visibleColumns.map((column) => <th className={pendingColumnClass(column.key)} key={column.key}>{column.label}</th>)}</tr></thead><tbody>{pagedPendentes.map((item) => <tr key={item.id}>{pendenteColumns.visibleColumns.map((column) => <td className={pendingColumnClass(column.key)} key={column.key}>{pendenteColumnValue(item, column.key)}</td>)}</tr>)}{!loading && filteredPendentes.length === 0 && <tr><td colSpan={pendenteColumns.visibleColumns.length}>Sem pendentes para mostrar.</td></tr>}</tbody></table></div>
        {filteredPendentes.length > 0 && <div className="tuuli-receipts-summary"><PendingSummary label="Subtotal da página" values={subtotalPendentes}/><PendingSummary label="Total filtrado" values={totalFilteredPendentes}/></div>}
        {filteredPendentes.length > 0 && <div className="tuuli-pagination"><span>{filteredPendentes.length} {filteredPendentes.length === 1 ? "registo" : "registos"}</span><Paginator first={pendentesPage * pendentesPageSize} onPageChange={(event) => { setPendentesPage(event.page); setPendentesPageSize(event.rows); }} rows={pendentesPageSize} rowsPerPageOptions={[10, 20, 50]} totalRecords={filteredPendentes.length}/></div>}
      </section>
    </div>}

    {!receiptOpen && selectedFinanceiroId && <section className="tuuli-v2-page tuuli-receipt-detail">
      <div className="tuuli-receipt-detail-heading">
        <div className="tuuli-receipt-detail-title">
          <div>
            <p className="tuuli-receipt-detail-kicker">Recibo</p>
            <h2 id="financial-detail-heading" tabIndex={-1}>{selectedFinanceiro ? financialReference(selectedFinanceiro) : "A carregar recebimento..."}</h2>
            {selectedFinanceiro && <p>Emitido em {datePt(selectedFinanceiro.dataEmissao)}</p>}
          </div>
          {selectedFinanceiro && <span aria-label={`Estado do recibo: ${selectedReceiptStatus}`} className={`tuuli-status ${selectedFinanceiro.anulado ? "tuuli-status-anulado" : "tuuli-status-ativo"}`}>{selectedReceiptStatus}</span>}
        </div>
        <div className="tuuli-receipt-detail-actions">
          <button className="fac-ghost-button tuuli-tool-action" onClick={closeFinancialDetail} type="button">Voltar à listagem</button>
          {selectedFinanceiro && hasPermission("DOCUMENTO_OBTER_PDF") && <button className="fac-ghost-button tuuli-tool-action" disabled={loading || detailLoading} onClick={() => openFinancialPdf(selectedFinanceiro)} type="button">Abrir PDF</button>}
          {selectedFinanceiro && canAnnul && !selectedFinanceiro.anulado && <button className="fac-link-danger tuuli-receipt-danger-action" disabled={loading || detailLoading} onClick={() => annulFinancial(selectedFinanceiro)} type="button">Anular recibo</button>}
        </div>
      </div>
      {detailLoading && <p className="fac-muted">A carregar detalhe do recibo...</p>}
      {selectedFinanceiro && <>
        <section className="tuuli-receipt-detail-section">
          <h3>Identificação</h3>
          <dl className="tuuli-receipt-identification">
            <div><dt>Cliente</dt><dd>{selectedCliente ? selectedCliente.nome : selectedFinanceiro.clienteId}</dd></div>
            {selectedCliente?.nif && <div><dt>NIF</dt><dd>{selectedCliente.nif}</dd></div>}
            <div><dt>Tipo</dt><dd>{selectedFinanceiro.tipoDocumentoId}</dd></div>
            <div><dt>Série</dt><dd>{selectedFinanceiro.serie}</dd></div>
            <div><dt>Data</dt><dd>{datePt(selectedFinanceiro.dataEmissao)}</dd></div>
            <div><dt>Moeda</dt><dd>{selectedFinanceiro.moedaId}</dd></div>
            <div><dt>Modo de pagamento</dt><dd>{selectedModo?.nome ?? selectedFinanceiro.mPagamentoId}</dd></div>
            <div><dt>Emissor</dt><dd>{selectedFinanceiro.emissorId}</dd></div>
            <div><dt>Operação</dt><dd>{dateTimePt(selectedFinanceiro.dataHoraOperacao)}</dd></div>
          </dl>
        </section>
        <section className="tuuli-receipt-detail-section">
          <h3>Resumo financeiro</h3>
          <div className="tuuli-receipt-financial-summary" aria-label="Resumo financeiro do recibo">
            <div><span>Valor recebido</span><strong>{formatMoney(selectedFinanceiro.valorPagamentoLiquido)} {selectedFinanceiro.moedaId}</strong></div>
            <div><span>Distribuído</span><strong>{formatMoney(selectedAppliedTotal)} {selectedFinanceiro.moedaId}</strong></div>
            <div><span>Diferença</span><strong>{formatMoney(selectedReceiptDifference)} {selectedFinanceiro.moedaId}</strong></div>
            <div><span>Estado</span><strong className={selectedFinanceiro.anulado ? "tuuli-status-anulado" : "tuuli-status-ativo"}>{selectedReceiptStatus}</strong></div>
          </div>
        </section>
        <section className="tuuli-receipt-detail-section tuuli-receipt-lines">
          <div className="tuuli-receipt-lines-heading"><div><p>Documentos liquidados</p><h3>Documentos comerciais associados</h3></div><span className="tuuli-meta">{selectedFinanceiro.linhas?.length ?? 0} linhas</span></div>
          <div className="tuuli-table-surface tuuli-receipt-lines-table">
            <table className="tuuli-table"><thead><tr><th className="tuuli-cell-primary">Documento</th><th className="tuuli-cell-secondary">Emissão</th><th className="tuuli-cell-secondary">Vencimento</th><th className="tuuli-cell-numeric tuuli-cell-secondary">Valor original</th><th className="tuuli-cell-numeric tuuli-cell-secondary">Saldo anterior</th><th className="tuuli-cell-numeric tuuli-cell-primary">Valor liquidado</th><th className="tuuli-cell-numeric tuuli-cell-primary">Saldo posterior</th></tr></thead><tbody>{(selectedFinanceiro.linhas ?? []).map((linha) => { const documentoComercialId = pendenteById.get(linha.pendenteId)?.documentoComercialId; return <tr key={linha.id}><td className="tuuli-cell-primary">{documentoComercialId ? <button className="fac-table-link tuuli-receipt-link" onClick={() => navigate(`/documentos/${documentoComercialId}`)} onKeyDown={(event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); navigate(`/documentos/${documentoComercialId}`); } }} type="button">{lineReference(linha)}</button> : lineReference(linha)}</td><td className="tuuli-cell-secondary">{datePt(linha.dataDocumento)}</td><td className="tuuli-cell-secondary">{datePt(linha.dataVencimento)}</td><td className="tuuli-cell-numeric tuuli-cell-secondary">{formatMoney(linha.valorDocumento)} {linha.moedaId}</td><td className="tuuli-cell-numeric tuuli-cell-secondary">{formatMoney(linha.valorPendenteAntes)} {linha.moedaId}</td><td className="tuuli-cell-numeric tuuli-cell-primary">{formatMoney(linha.valorALiquidar)} {linha.moedaId}</td><td className="tuuli-cell-numeric tuuli-cell-primary">{formatMoney(linha.novoValorPendente)} {linha.moedaId}</td></tr>; })}{(!selectedFinanceiro.linhas || selectedFinanceiro.linhas.length === 0) && <tr><td colSpan={7}>Sem documentos liquidados.</td></tr>}</tbody></table>
          </div>
        </section>
        {selectedFinanceiro.observacoes && <section className="tuuli-receipt-detail-section"><h3>Observações</h3><p className="tuuli-receipt-observations">{selectedFinanceiro.observacoes}</p></section>}
      </>}
    </section>}

    {receiptOpen && <section aria-label="Novo recebimento" className="tuuli-v2-page tuuli-receipt-workspace" onKeyDown={handleReceiptKeyDown} ref={receiptEditorRef}>
      <div className="tuuli-receipt-workspace-heading"><div><p>Novo documento financeiro</p><h2>Distribuir recebimento</h2></div><div className="tuuli-receipt-workspace-actions"><button className="fac-ghost-button tuuli-tool-action" disabled={loading || receiptSubmitting} onClick={backToReceiptList} type="button">Voltar à listagem</button><button className="fac-primary-button tuuli-primary-action" disabled={!canIssueReceipt} onClick={() => issueReceipt("DETAIL")} type="button">{issueButtonLabel}</button><button className="fac-ghost-button tuuli-tool-action" disabled={!canIssueReceipt} onClick={() => issueReceipt("PDF")} type="button">{issuePdfButtonLabel}</button></div></div>
      <div className="fac-form-grid tuuli-receipt-workspace-fields">
        <EntityLookupField<Cliente>
          autoFocusRequest={clientFocusRequest}
          columns={CLIENT_LOOKUP_COLUMNS}
          dataKey="id"
          emptyMessage="Sem clientes com documentos pendentes para selecionar."
          label="Cliente"
          loading={loading}
          onClear={() => selectClient("")}
          onSelect={(cliente) => selectClient(String(cliente.id))}
          openDialogOnF2
          optionLabel={(cliente) => `${cliente.id} · ${cliente.nome}`}
          optionMeta={(cliente) => [cliente.nif && `NIF ${cliente.nif}`, cliente.email, cliente.inativo ? "Inativo" : null].filter(Boolean).join(" · ")}
          placeholder="Pesquisar cliente"
          preferenceKey="fac.lookup.recebimentos.clientes"
          searchFields={CLIENT_SEARCH_FIELDS}
          selection={receiptCliente}
          title="Selecionar cliente"
          value={clientesComPendentes}
          valueLabel={receiptCliente ? `${receiptCliente.id} · ${receiptCliente.nome}` : undefined}
        />
        <Field label="Moeda"><select data-receipt-currency disabled={!form.clienteId || clientCurrencies.length <= 1} onChange={(event) => { setForm((current) => ({ ...current, moedaId: event.target.value, valorRecebido: "" })); setAllocations({}); setManualReceiptValue(false); requestAnimationFrame(() => receiptEditorRef.current?.querySelector<HTMLElement>("[data-receipt-value]")?.focus()); }} value={form.moedaId}><option value="">Selecionar moeda</option>{clientCurrencies.map((moeda) => <option key={moeda} value={moeda}>{moeda}</option>)}</select></Field>
        <Field label="Valor recebido"><input data-receipt-value disabled={!form.moedaId} min="0.000001" onChange={(event) => { const value = event.target.value; setForm((current) => ({ ...current, valorRecebido: value })); setAllocations({}); setManualReceiptValue(value !== ""); }} step="0.000001" type="number" value={form.valorRecebido}/></Field>
        <Field label="Modo de pagamento"><select onChange={(event) => setForm((current) => ({ ...current, mPagamentoId: event.target.value }))} value={form.mPagamentoId}><option value="">Confirmar modo</option>{modos.map((modo) => <option key={modo.id} value={modo.id}>{modo.nome}</option>)}</select></Field>
        <Field label="Tipo de documento"><select onChange={(event) => { const tipoDocumentoId = event.target.value; setForm((current) => ({ ...current, tipoDocumentoId, serie: series.find((item) => item.tipoDocumentoId === tipoDocumentoId)?.serie ?? "" })); }} value={form.tipoDocumentoId}><option value="">Selecionar</option>{tipos.map((tipo) => <option key={tipo.id} value={tipo.id}>{tipo.id} - {tipo.descricao}</option>)}</select></Field>
        <Field label="Série"><select onChange={(event) => setForm((current) => ({ ...current, serie: event.target.value }))} value={form.serie}><option value="">Selecionar</option>{availableSeries.map((serie) => <option key={`${serie.tipoDocumentoId}-${serie.serie}`} value={serie.serie}>{serie.serie} - {serie.nome}</option>)}</select></Field>
        <Field label="Data de emissão"><input onChange={(event) => setForm((current) => ({ ...current, dataEmissao: event.target.value }))} type="date" value={form.dataEmissao}/></Field>
        <Field label="Emissor"><input disabled value={getAuthSession()?.nome ?? "Utilizador autenticado"}/></Field>
      </div>

      <div className="tuuli-receipt-workspace-summary"><div><span>Total pendente do cliente</span><strong>{formatMoney(totalPendenteCliente)} {form.moedaId || "EUR"}</strong></div><div><span>Valor recebido</span><strong>{formatMoney(receiptTarget)} {form.moedaId}</strong></div><div><span>Distribuído</span><strong>{formatMoney(allocatedTotal)} {form.moedaId}</strong></div><div className={difference === 0 ? "balanced" : "unbalanced"}><span>Diferença</span><strong>{formatMoney(difference)} {form.moedaId}</strong></div></div>
      <div className="tuuli-receipt-distribution-heading"><div><p>Documentos pendentes</p><h3>Distribuição do recebimento</h3></div><div className="tuuli-receipt-distribution-tools"><button className="fac-ghost-button tuuli-tool-action" disabled={!form.valorRecebido || !form.moedaId} onClick={distributeReceipt} type="button">Distribuir por antiguidade</button><button className="fac-ghost-button tuuli-tool-action" disabled={allocatedTotal === 0} onClick={clearAllocations} type="button">Limpar distribuição</button></div></div>

      <div className="tuuli-table-surface tuuli-receipt-allocation-surface"><table className="tuuli-table fac-allocation-table"><thead><tr><th>Documento</th><th>Emissão</th><th>Vencimento</th><th>Valor original</th><th>Pendente antes</th><th>Valor a liquidar</th><th>Novo pendente</th></tr></thead><tbody>
        {receiptPendentes.map((pendente) => { const amount = round6(Number(allocations[pendente.id] || 0)); return <tr aria-label={`${referencia(pendente)}: clicar para atribuir ou limpar o valor a liquidar`} className={`fac-allocation-row ${amount > 0 ? "allocated" : ""}`} key={pendente.id} onClick={(event) => { if (!(event.target as HTMLElement).closest("input, button, select, textarea")) toggleAllocation(pendente); }}><td className="tuuli-cell-primary">{referencia(pendente)}</td><td className="tuuli-cell-secondary">{datePt(pendente.dataDocumento)}</td><td className="tuuli-cell-secondary">{datePt(pendente.dataVencimento)}</td><td className="tuuli-cell-numeric tuuli-cell-secondary">{formatMoney(pendente.valorDocumento)} {pendente.moedaId}</td><td className="tuuli-cell-numeric tuuli-cell-secondary">{formatMoney(pendente.valorPendente)} {pendente.moedaId}</td><td className="tuuli-cell-numeric tuuli-cell-primary"><input aria-label={`Valor a liquidar de ${referencia(pendente)}`} className="tuuli-allocation-input" max={pendente.valorPendente} min="0" onChange={(event) => changeAllocation(pendente, event.target.value)} onKeyDown={(event) => handleAllocationInputKeyDown(event, pendente)} step="0.000001" type="number" value={allocations[pendente.id] ?? ""}/></td><td className="tuuli-cell-numeric tuuli-cell-primary">{formatMoney(round6(pendente.valorPendente - amount))} {pendente.moedaId}</td></tr>; })}
        {form.clienteId && form.moedaId && receiptPendentes.length === 0 && <tr><td colSpan={7}>Este cliente não tem pendentes em aberto nesta moeda.</td></tr>}
      </tbody></table></div>

      <div className="tuuli-receipt-workspace-notes"><Field label="Observações"><textarea maxLength={250} onChange={(event) => setForm((current) => ({ ...current, observacoes: event.target.value }))} value={form.observacoes}/></Field></div>
      <p className="tuuli-receipt-workspace-guidance">O recibo só pode ser emitido quando o valor recebido estiver totalmente distribuído pelos pendentes.</p>
    </section>}

  </>;
}

function emptyReceiptForm(): ReceiptForm { return { clienteId: "", moedaId: "", tipoDocumentoId: "", serie: "", dataEmissao: todayIso(), valorRecebido: "", mPagamentoId: "", emissorId: getAuthSession()?.codigo ?? "", observacoes: "" }; }
function receiptFormKey(form: ReceiptForm) { return JSON.stringify({ clienteId: form.clienteId, moedaId: form.moedaId, tipoDocumentoId: form.tipoDocumentoId, serie: form.serie, dataEmissao: form.dataEmissao, valorRecebido: form.valorRecebido, mPagamentoId: form.mPagamentoId, observacoes: form.observacoes.trim() }); }
function openPendentesForClient(pendentes: Pendente[], clienteId: number) { return clienteId ? pendentes.filter((item) => item.clienteId === clienteId && Number(item.valorPendente) > 0) : []; }
function summarizePendentes(items: Pendente[]) { const byCurrency = new Map<string, { count: number; original: number; pending: number }>(); items.forEach((item) => { const current = byCurrency.get(item.moedaId) ?? { count: 0, original: 0, pending: 0 }; current.count += 1; current.original += Number(item.valorDocumento); current.pending += Number(item.valorPendente); byCurrency.set(item.moedaId, current); }); return Array.from(byCurrency.entries()); }
function PendingSummary({ label, values }: { label: string; values: Array<[string, { count: number; original: number; pending: number }]> }) { return <section><h3>{label}</h3>{values.map(([currency, totals]) => <div className="tuuli-receipts-summary-row" key={currency}><span>{totals.count} {totals.count === 1 ? "registo" : "registos"} · {currency}</span><dl><div><dt>Original</dt><dd>{formatMoney(totals.original)} {currency}</dd></div><div><dt>Pendente</dt><dd>{formatMoney(totals.pending)} {currency}</dd></div></dl></div>)}</section>; }
function validPaymentMode(modos: MPagamento[], mPagamentoId?: string | null) { return mPagamentoId && modos.some((modo) => modo.id === mPagamentoId) ? mPagamentoId : ""; }
function validateReceipt(form: ReceiptForm, pendentes: Pendente[], allocations: Allocations) { if (!form.clienteId) return "Seleciona o cliente."; if (!form.moedaId) return "Seleciona a moeda."; if (!form.tipoDocumentoId) return "Seleciona o tipo de documento financeiro."; if (!form.serie) return "Seleciona a série."; if (!form.dataEmissao) return "A data de emissão é obrigatória."; if (!form.mPagamentoId) return "Confirma o modo de pagamento."; const target = round6(Number(form.valorRecebido)); if (!Number.isFinite(target) || target <= 0) return "O valor recebido deve ser positivo."; const total = round6(sum(pendentes.map((item) => Number(allocations[item.id] || 0)))); if (total <= 0) return "Distribui o recebimento por pelo menos um pendente."; if (round6(target - total) !== 0) return "O valor recebido e a distribuição pelos pendentes não coincidem."; return null; }
function estado(item: Pendente) { if (Number(item.valorPendente) <= 0) return "LIQUIDADO"; if (item.dataVencimento < todayIso()) return "VENCIDO"; if (Number(item.valorPendente) < Number(item.valorDocumento)) return "PARCIAL"; return "ABERTO"; }
function referencia(item: Pendente) { return `${item.tipoDocumentoId} ${item.serieDocumento}/${item.numeroDocumento}`; }
function pendenteColumnValue(item: Pendente, key: string) { switch (key) { case "documento": return referencia(item); case "cliente": return item.clienteId; case "emissao": return datePt(item.dataDocumento); case "vencimento": return datePt(item.dataVencimento); case "moeda": return item.moedaId; case "original": return `${formatMoney(item.valorDocumento)} ${item.moedaId}`; case "pendente": return `${formatMoney(item.valorPendente)} ${item.moedaId}`; case "estado": return <span className={`fac-status tuuli-status-${estado(item).toLowerCase()}`}>{estado(item)}</span>; default: return "-"; } }
function financeiroColumnValue(documento: DocumentoFinanceiro, key: string, onOpen: (documento: DocumentoFinanceiro) => void) { switch (key) { case "documento": return <button className="fac-table-link tuuli-receipt-link" onClick={() => onOpen(documento)} type="button">{financialReference(documento)}</button>; case "cliente": return documento.clienteId; case "data": return datePt(documento.dataEmissao); case "modo": return documento.mPagamentoId; case "moeda": return documento.moedaId; case "liquido": return `${formatMoney(documento.valorPagamentoLiquido)} ${documento.moedaId}`; case "emissor": return documento.emissorId; case "estado": return <span className={`fac-status ${documento.anulado ? "tuuli-status-anulado" : "tuuli-status-ativo"}`}>{documento.anulado ? "ANULADO" : "EMITIDO"}</span>; default: return "-"; } }
function receiptColumnClass(key: string) { return [key === "documento" || key === "liquido" ? "tuuli-cell-primary" : "tuuli-cell-secondary", key === "liquido" ? "tuuli-cell-numeric" : ""].filter(Boolean).join(" "); }
function pendingColumnClass(key: string) { return [key === "documento" || key === "original" || key === "pendente" ? "tuuli-cell-primary" : "tuuli-cell-secondary", key === "original" || key === "pendente" ? "tuuli-cell-numeric" : ""].filter(Boolean).join(" "); }
function financialReference(documento: DocumentoFinanceiro) { return `${documento.tipoDocumentoId} ${documento.serie}/${documento.numeroDocumento}`; }
function receiptStatusLabel(documento: DocumentoFinanceiro) { return documento.anulado ? "Anulado" : "Emitido"; }
function lineReference(linha: LinhaFinanceira) { return `${linha.tipoDocumentoId} ${linha.serieDocumento}/${linha.numeroDocumento}`; }
function datePt(value: string) { return value ? value.split("-").reverse().join("/") : "-"; }
function dateTimePt(value?: string | null) { return value ? new Date(value).toLocaleString("pt-PT") : "-"; }
function sum(values: number[]) { return values.reduce((total, value) => total + Number(value || 0), 0); }
function round6(value: number) { return Math.round((value + Number.EPSILON) * 1_000_000) / 1_000_000; }
function todayIso() { const now = new Date(); const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000); return local.toISOString().slice(0, 10); }
function currentYearStartIso() { return `${todayIso().slice(0, 4)}-01-01`; }
function blankToNull(value: string) { return value.trim() || null; }
function filenameFromDisposition(disposition: string | null) { return disposition?.match(/filename="?([^";]+)"?/i)?.[1] ?? null; }
function Field({ children, label }: { children: React.ReactNode; label: string }) { return <label className="fac-field"><span>{label}</span>{children}</label>; }
async function fetchJson<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function sendJson<T>(url: string, body: unknown): Promise<T> { const response = await apiFetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: body == null ? undefined : JSON.stringify(body) }); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
