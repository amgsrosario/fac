import { KeyboardEvent, MouseEvent, useEffect, useMemo, useRef, useState } from "react";
import { InputNumber, InputNumberValueChangeEvent } from "primereact/inputnumber";
import { useNavigate, useParams } from "react-router-dom";
import { GlobalSearch } from "../../../GlobalSearch";
import { apiFetch, AuthSession } from "../../../api";
import { DesktopShell, EntityLookupColumn, EntityLookupDialog, EntityLookupField, EntityLookupSearchField, FacButton, FacInputText, FacMessage, FacSelect, MobileShell, ResponsiveSlot, useFacToast } from "../../fac";
import { CommercialSidebar } from "../shared";

type Page<T> = { content: T[]; totalPages?: number };
type EstadoDocumento = "RASCUNHO" | "EMITIDO" | "ANULADO";
type TipoLinha = "COMERCIAL" | "TEXTO";
type Step = "header" | "lines";

type DocumentoComercial = {
  id: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento?: number | null;
  numeroDocumentoCompleto?: string | null;
  estado: EstadoDocumento;
  dataEmissao: string;
  clienteId: number;
  armazemCargaId?: string | null;
  moedaId?: string | null;
  rivaId?: string | null;
  mPagamentoId?: string | null;
  pPagamentoId?: string | null;
  transporteId?: string | null;
  observacoes?: string | null;
  clienteNome?: string | null;
  valorBruto?: string | number | null;
  valorDesconto?: string | number | null;
  valorIvaTotal?: string | number | null;
  valorTotal?: string | number | null;
  moedaCodigo?: string | null;
  moedaSimbolo?: string | null;
};

type LinhaDocumento = {
  id: number;
  documentoComercialId: number;
  numeroLinha: number;
  tipoLinha?: TipoLinha | null;
  artigoId?: string | null;
  descricao: string;
  quantidade?: string | number | null;
  precoUnitario?: string | number | null;
  tipoDesconto?: "VALOR" | "PERCENTAGEM" | null;
  desconto?: string | number | null;
  valorDesconto?: string | number | null;
  valorLinha?: string | number | null;
  tipoTaxaIvaId?: string | null;
  percentagemIva?: string | number | null;
  unidade?: string | null;
  totalLinha?: string | number | null;
};

type DiagnosticoDocumento = {
  podeEmitir: boolean;
  podeAnular: boolean;
  alertas: string[];
  bloqueios: string[];
};

type DocumentoImpressao = {
  documento: DocumentoComercial;
  linhas: LinhaDocumento[];
};

type TipoDocumento = { id: string; descricao: string; areaGestao: number };
type Serie = { serie: string; tipoDocumentoId: string; nome: string };
type Cliente = { id: number; nome: string; nif: string; inativo: boolean; localidade?: string | null; tel?: string | null; tm?: string | null; email?: string | null; morada?: string | null; codPostalId?: string | null; paisId?: string | null; moedaId?: string | null; rivaId?: string | null; mPagamentoId?: string | null; pPagamentoId?: string | null; transporteId?: string | null };
type Artigo = { codigo: string; abreviatura?: string | null; codigoIdentificacao?: string | null; descricao: string; tipoArtigo?: "ARTIGO" | "SERVICO"; unidade: string; familiaId?: number | null; peso?: string | number | null; ivaCompraId?: string | null; pvp: number; ivaVendaId: string; inativo: boolean; retencao?: boolean; observacoes?: string | null };
type CatalogoString = { id: string; nome: string };
type CatalogoNumero = { id: number; nome: string };
type TipoTaxaIva = { id: string; descricao: string; inativo: boolean };
type Armazem = { id: string; nome: string };
type ParametrosDocumentoComercial = { tipoDocumentoId?: string | null; serie?: string | null; armazemCargaId?: string | null };

type Catalogos = {
  artigos: Artigo[];
  armazens: Armazem[];
  clientes: Cliente[];
  moedas: CatalogoString[];
  regimesIva: CatalogoString[];
  series: Serie[];
  tiposDocumento: TipoDocumento[];
  tiposIva: TipoTaxaIva[];
  transportes: CatalogoString[];
  modosPagamento: CatalogoString[];
  prazosPagamento: CatalogoString[];
};

type HeaderState = {
  tipoDocumentoId: string;
  serie: string;
  dataEmissao: string;
  clienteId: string;
  armazemCargaId: string;
  moedaId: string;
  rivaId: string;
  mPagamentoId: string;
  pPagamentoId: string;
  transporteId: string;
  observacoes: string;
};

type EditorLine = {
  uid: string;
  id?: number;
  tipoLinha: TipoLinha;
  numeroLinha: number;
  artigoId: string;
  descricao: string;
  quantidade: string;
  unidade: string;
  precoUnitario: string;
  tipoDesconto: "VALOR" | "PERCENTAGEM";
  desconto: string;
  tipoTaxaIvaId: string;
  dirty: boolean;
};

type PendingLineCommitResult =
  | { status: "empty"; lines: EditorLine[] }
  | { status: "committed"; line: EditorLine; lines: EditorLine[] }
  | { status: "invalid"; message: string };

type Totals = { subtotal: number; discount: number; vat: number; total: number };

const today = () => new Date().toISOString().slice(0, 10);

const emptyHeader: HeaderState = {
  tipoDocumentoId: "",
  serie: "",
  dataEmissao: today(),
  clienteId: "",
  armazemCargaId: "",
  moedaId: "",
  rivaId: "",
  mPagamentoId: "",
  pPagamentoId: "",
  transporteId: "",
  observacoes: ""
};

const emptyLine = (tipoLinha: TipoLinha = "TEXTO"): EditorLine => ({
  uid: crypto.randomUUID(),
  tipoLinha,
  numeroLinha: 0,
  artigoId: "",
  descricao: "",
  quantidade: "",
  unidade: "",
  precoUnitario: "",
  tipoDesconto: "VALOR",
  desconto: "",
  tipoTaxaIvaId: "",
  dirty: false
});

export default function DraftDocumentEditor({ currentUser, embedded = false, onLogout }: { currentUser: AuthSession; embedded?: boolean; onLogout: () => void }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useFacToast();
  const documentId = id ? Number(id) : null;
  const [step, setStep] = useState<Step>("header");
  const [documento, setDocumento] = useState<DocumentoComercial | null>(null);
  const [header, setHeader] = useState<HeaderState>(emptyHeader);
  const [lines, setLines] = useState<EditorLine[]>([]);
  const [activeLine, setActiveLine] = useState<EditorLine>(() => emptyLine());
  const [activeArticleQuery, setActiveArticleQuery] = useState("");
  const [removedLineIds, setRemovedLineIds] = useState<number[]>([]);
  const [selectedLineUid, setSelectedLineUid] = useState<string | null>(null);
  const [originalHeaderKey, setOriginalHeaderKey] = useState("");
  const [originalOrderKey, setOriginalOrderKey] = useState("");
  const [dirty, setDirty] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const savingRef = useRef(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [diagnostico, setDiagnostico] = useState<DiagnosticoDocumento | null>(null);
  const [impressao, setImpressao] = useState<DocumentoImpressao | null>(null);
  const [anularOpen, setAnularOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [motivoAnulacao, setMotivoAnulacao] = useState("");
  const [catalogos, setCatalogos] = useState<Catalogos>({
    artigos: [],
    armazens: [],
    clientes: [],
    moedas: [],
    regimesIva: [],
    series: [],
    tiposDocumento: [],
    tiposIva: [],
    transportes: [],
    modosPagamento: [],
    prazosPagamento: []
  });

  const canEdit = currentUser.permissoes.includes("DOCUMENTO_EDITAR_RASCUNHO");
  const canDeleteDraft = currentUser.permissoes.includes("DOCUMENTO_ELIMINAR_RASCUNHO");
  const canEmit = currentUser.permissoes.includes("DOCUMENTO_EMITIR");
  const canVoid = currentUser.permissoes.includes("DOCUMENTO_ANULAR");
  const canPdf = currentUser.permissoes.includes("DOCUMENTO_OBTER_PDF");
  const isDraft = !documento || documento.estado === "RASCUNHO";
  const canEditCurrent = canEdit && isDraft;
  const canEmitCurrent = Boolean(documento && isDraft && canEmit && !dirty && (diagnostico?.podeEmitir ?? false));
  const canVoidCurrent = Boolean(documento && documento.estado === "EMITIDO" && canVoid && (diagnostico?.podeAnular ?? false));
  const canOpenPdfCurrent = Boolean(documento && documento.estado !== "RASCUNHO" && canPdf);
  const showEmitAction = Boolean(documento && isDraft && canEmit);
  const showSaveDraftAction = Boolean(isDraft && canEditCurrent && (!documento || dirty));
  const saveDraftLabel = saving ? "A guardar..." : documento ? "Guardar alterações" : "Guardar rascunho";
  const draftTotals = useMemo(() => calculateTotals(isLineFilled(activeLine) ? [...lines, activeLine] : lines, catalogos), [activeLine, catalogos, lines]);
  const sidebar = embedded ? null : <CommercialSidebar active="documents" currentUser={currentUser} onLogout={() => confirmLeave(dirty) && onLogout()} />;

  useEffect(() => {
    loadInitial();
  }, [documentId]);

  useEffect(() => {
    const onBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!dirty) return;
      event.preventDefault();
      event.returnValue = "";
    };
    window.addEventListener("beforeunload", onBeforeUnload);
    return () => window.removeEventListener("beforeunload", onBeforeUnload);
  }, [dirty]);

  async function loadInitial() {
    setLoading(true);
    setError(null);
    setNotice(null);
    try {
      const loadedCatalogos = await loadCatalogos();
      setCatalogos(loadedCatalogos);
      if (documentId) {
        const [doc, realLines, diag, printModel] = await Promise.all([
          requestJson<DocumentoComercial>(`/api/documentos-comerciais/${documentId}`),
          requestJson<LinhaDocumento[]>(`/api/documentos-comerciais/${documentId}/linhas`),
          requestJson<DiagnosticoDocumento>(`/api/documentos-comerciais/${documentId}/diagnostico`),
          requestJson<DocumentoImpressao>(`/api/documentos-comerciais/${documentId}/impressao`)
        ]);
        if (doc.estado !== "RASCUNHO") {
          setNotice("Documento aberto em modo consulta. A edição fica bloqueada fora de RASCUNHO.");
        }
        const mappedHeader = headerFromDocument(doc);
        const mappedLines = realLines
          .slice()
          .sort((left, right) => left.numeroLinha - right.numeroLinha)
          .map(lineFromDto);
        setDocumento(doc);
        setHeader(mappedHeader);
        setLines(resequenceLines(mappedLines.map((line) => ({ ...line, dirty: false }))));
        setOriginalHeaderKey(headerKey(mappedHeader));
        setOriginalOrderKey(orderKey(mappedLines));
        setDiagnostico(diag);
        setImpressao(printModel);
        setActiveLine(emptyLine());
        setActiveArticleQuery("");
        setRemovedLineIds([]);
        setDirty(false);
      } else {
        const parametros = await fetchOptional<ParametrosDocumentoComercial>("/api/parametros-documento-comercial");
        const { header: nextHeader, warnings } = initialiseHeader(loadedCatalogos, parametros);
        setDocumento(null);
        setHeader(nextHeader);
        setLines([]);
        setOriginalHeaderKey(headerKey(nextHeader));
        setOriginalOrderKey("");
        setDiagnostico(null);
        setImpressao(null);
        setNotice(warnings.length > 0 ? warnings.join(" ") : null);
        setActiveLine(emptyLine());
        setActiveArticleQuery("");
        setRemovedLineIds([]);
        setDirty(false);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível carregar o editor de rascunho.");
    } finally {
      setLoading(false);
    }
  }

  function updateHeader(patch: Partial<HeaderState>) {
    if (!canEditCurrent) return;
    if (!hasHeaderPatchChanges(header, patch)) return;
    setHeader((current) => {
      const next = { ...current, ...patch };
      setDirty(true);
      return next;
    });
  }

  function chooseClient(clienteId: string | null) {
    const cliente = catalogos.clientes.find((item) => String(item.id) === clienteId);
    updateHeader({
      clienteId: clienteId ?? "",
      moedaId: cliente?.moedaId ?? header.moedaId,
      rivaId: cliente?.rivaId ?? header.rivaId,
      mPagamentoId: cliente?.mPagamentoId ? String(cliente.mPagamentoId) : header.mPagamentoId,
      pPagamentoId: cliente?.pPagamentoId ?? header.pPagamentoId,
      transporteId: cliente?.transporteId ? String(cliente.transporteId) : header.transporteId
    });
  }

  function updateLine(uid: string, patch: Partial<EditorLine>) {
    if (!canEditCurrent) return;
    const target = lines.find((line) => line.uid === uid);
    if (!target || !hasLinePatchChanges(target, patch)) return;
    setLines((current) => current.map((line) => line.uid === uid ? normalizeLineByArticle({ ...line, ...patch, dirty: true }) : line));
    setDirty(true);
  }

  function chooseArticle(uid: string, artigoId: string | null, draft = false) {
    if (!canEditCurrent) return;
    const artigo = catalogos.artigos.find((item) => item.codigo === artigoId);
    if (draft) {
      setActiveLine((current) => applyArticleToLine(current, artigo));
      setActiveArticleQuery("");
      setDirty(true);
      return;
    }
    const target = lines.find((line) => line.uid === uid);
    if (!target) return;
    updateLine(uid, applyArticleToLine(target, artigo));
  }

  function setActivePatch(patch: Partial<EditorLine>) {
    if (!canEditCurrent) return;
    if (!hasLinePatchChanges(activeLine, patch)) return;
    setActiveLine((current) => normalizeLineByArticle({ ...current, ...patch }));
    setDirty(true);
  }

  function updateActiveArticleQuery(query: string) {
    if (query === activeArticleQuery) return;
    setActiveArticleQuery(query);
    if (query.trim()) setDirty(true);
  }

  function preparePendingLine(_tipoLinha = activeLine.tipoLinha, baseLines = lines): PendingLineCommitResult {
    const line = normalizeLineByArticle(activeLine);
    if (activeArticleQuery.trim() && !line.artigoId) {
      return { status: "invalid", message: "Conclua ou limpe a linha em edição antes de guardar o rascunho." };
    }
    if (isPendingLineEmpty(line)) {
      return { status: "empty", lines: resequenceLines(baseLines) };
    }
    if (!isLineFilled(line)) {
      return { status: "empty", lines: resequenceLines(baseLines) };
    }
    const validation = validateLine(line);
    if (validation) {
      return { status: "invalid", message: validation };
    }
    const committed = { ...line, uid: crypto.randomUUID(), dirty: true };
    return { status: "committed", line: committed, lines: resequenceLines([...baseLines, committed]) };
  }

  function commitPendingLine(tipoLinha = activeLine.tipoLinha): PendingLineCommitResult {
    if (!canEditCurrent) return { status: "invalid", message: "Não é possível editar este rascunho." };
    const result = preparePendingLine(tipoLinha);
    if (result.status === "empty") {
      setNotice("A linha ativa continua local e não foi adicionada.");
      return result;
    }
    if (result.status === "invalid") {
      setError(result.message);
      return result;
    }
    setLines(result.lines);
    setActiveLine(emptyLine());
    setActiveArticleQuery("");
    setSelectedLineUid(result.line.uid);
    setDirty(true);
    setNotice("Linha adicionada localmente.");
    return result;
  }

  function addBlankLine(tipoLinha: TipoLinha, afterUid?: string) {
    if (!canEditCurrent) return;
    const nextLine = normalizeLineByArticle(emptyLine(tipoLinha));
    setLines((current) => {
      const next = [...current];
      const index = afterUid ? next.findIndex((line) => line.uid === afterUid) : next.length - 1;
      next.splice(index >= 0 ? index + 1 : next.length, 0, nextLine);
      return resequenceLines(next);
    });
    setSelectedLineUid(nextLine.uid);
    setDirty(true);
  }

  function duplicateLine(uid: string) {
    if (!canEditCurrent) return;
    const source = lines.find((line) => line.uid === uid);
    if (!source) return;
    const copy = { ...source, id: undefined, uid: crypto.randomUUID(), dirty: true };
    setLines((current) => {
      const next = [...current];
      const index = next.findIndex((line) => line.uid === uid);
      next.splice(index + 1, 0, copy);
      return resequenceLines(next);
    });
    setSelectedLineUid(copy.uid);
    setDirty(true);
  }

  function removeLine(uid: string) {
    if (!canEditCurrent) return;
    const line = lines.find((item) => item.uid === uid);
    if (!line) return;
    if (line.id) setRemovedLineIds((current) => current.includes(line.id!) ? current : [...current, line.id!]);
    setLines((current) => resequenceLines(current.filter((item) => item.uid !== uid)));
    setSelectedLineUid(null);
    setDirty(true);
  }

  function moveLine(uid: string, direction: -1 | 1) {
    if (!canEditCurrent) return;
    setLines((current) => {
      const index = current.findIndex((line) => line.uid === uid);
      const target = index + direction;
      if (index < 0 || target < 0 || target >= current.length) return current;
      const next = [...current];
      const [line] = next.splice(index, 1);
      next.splice(target, 0, line);
      return resequenceLines(next);
    });
    setSelectedLineUid(uid);
    setDirty(true);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLElement>) {
    if (event.key === "Escape") {
      setError(null);
      setNotice(null);
      return;
    }
    if (event.altKey && (event.key === "ArrowUp" || event.key === "ArrowDown")) {
      event.preventDefault();
      if (selectedLineUid) moveLine(selectedLineUid, event.key === "ArrowUp" ? -1 : 1);
      return;
    }
    if (event.ctrlKey && event.key === "Enter") {
      event.preventDefault();
      commitPendingLine("TEXTO");
      return;
    }
    if (event.key === "Enter") {
      const target = event.target as HTMLElement;
      if (target.matches("textarea")) return;
      if (target.hasAttribute("data-active-line") || target.closest("[data-active-line]")) {
        event.preventDefault();
        commitPendingLine("COMERCIAL");
      }
    }
  }

  function goBack() {
    if (!confirmLeave(dirty)) return;
    navigate("/documentos");
  }

  function newDocument() {
    if (!confirmLeave(dirty)) return;
    setDirty(false);
    navigate("/documentos/novo");
  }

  async function saveDraft() {
    if (savingRef.current || saving || !canEditCurrent) return;
    const isNewDraft = !documento;
    setError(null);
    setNotice(null);
    const pendingLine = preparePendingLine();
    if (pendingLine.status === "invalid") {
      setError(pendingLine.message || "Conclua ou limpe a linha em edição antes de guardar o rascunho.");
      return;
    }
    const localLines = resequenceLines(pendingLine.lines);
    const validation = validateHeader(header) ?? validateHasCommercialLine(localLines) ?? localLines.map(validateLine).find(Boolean) ?? null;
    if (validation) {
      setError(validation);
      return;
    }

    savingRef.current = true;
    setSaving(true);
    try {
      let currentId = documentId;
      let persistedLines: EditorLine[] = localLines;

      if (!currentId) {
        const firstCommercial = localLines.find(isValidCommercialLine);
        if (!firstCommercial) throw new Error("Crie pelo menos uma linha comercial valida antes de guardar.");
        const created = await requestJson<DocumentoComercial>("/api/documentos-comerciais", {
          documento: headerCreatePayload(header),
          linha: lineCreatePayload(firstCommercial)
        }, "POST");
        currentId = created.id;
        persistedLines = localLines.map((line) => line.uid === firstCommercial.uid ? { ...line, id: undefined, dirty: false, uid: firstCommercial.uid } : line);
        const createdLines = await requestJson<LinhaDocumento[]>(`/api/documentos-comerciais/${currentId}/linhas`);
        const createdFirst = createdLines[0];
        persistedLines = persistedLines.map((line) => line.uid === firstCommercial.uid ? { ...line, id: createdFirst.id } : line);
      } else if (headerKey(header) !== originalHeaderKey) {
        await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${currentId}`, headerPayload(header), "PUT");
      }

      if (!currentId) throw new Error("Não foi possível obter o identificador do documento.");

      for (const lineId of removedLineIds) {
        await requestNoContent(`/api/documentos-comerciais/${currentId}/linhas/${lineId}`, "DELETE");
      }

      const savedLines: EditorLine[] = [];
      for (const line of persistedLines) {
        if (line.id) {
          if (line.dirty) {
            await requestNoContent(`/api/documentos-comerciais/${currentId}/linhas/${line.id}`, "PUT", lineUpdatePayload(line));
          }
          savedLines.push(line);
        } else {
          const createdLine = await requestJson<LinhaDocumento>(`/api/documentos-comerciais/${currentId}/linhas`, lineCreatePayload(line), "POST");
          savedLines.push({ ...line, id: createdLine.id });
        }
      }

      const orderIds = savedLines.map((line) => line.id).filter((lineId): lineId is number => Boolean(lineId));
      if (orderIds.length > 0 && orderKey(savedLines) !== originalOrderKey) {
        await requestJson<LinhaDocumento[]>(`/api/documentos-comerciais/${currentId}/linhas/ordem`, { linhaIds: orderIds }, "PUT");
      }

      const [freshDoc, freshLines, freshDiag, freshPrintModel] = await Promise.all([
        requestJson<DocumentoComercial>(`/api/documentos-comerciais/${currentId}`),
        requestJson<LinhaDocumento[]>(`/api/documentos-comerciais/${currentId}/linhas`),
        requestJson<DiagnosticoDocumento>(`/api/documentos-comerciais/${currentId}/diagnostico`),
        requestJson<DocumentoImpressao>(`/api/documentos-comerciais/${currentId}/impressao`)
      ]);
      const freshEditorLines = freshLines.slice().sort((left, right) => left.numeroLinha - right.numeroLinha).map(lineFromDto);
      assertOrder(freshEditorLines, orderIds);
      const nextHeader = headerFromDocument(freshDoc);
      setDirty(false);
      setDocumento(freshDoc);
      setHeader(nextHeader);
      setLines(resequenceLines(freshEditorLines.map((line) => ({ ...line, dirty: false }))));
      setActiveLine(emptyLine());
      setActiveArticleQuery("");
      setRemovedLineIds([]);
      setOriginalHeaderKey(headerKey(nextHeader));
      setOriginalOrderKey(orderKey(freshEditorLines));
      setDiagnostico(freshDiag);
      setImpressao(freshPrintModel);
      const savedMessage = isNewDraft ? "Rascunho guardado." : "Alterações guardadas.";
      setNotice(savedMessage);
      showToast({ detail: savedMessage, severity: "success", summary: "Documentos" });
      if (!documentId) navigate(`/documentos/${currentId}`, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível guardar o rascunho. As alterações locais foram mantidas.");
    } finally {
      savingRef.current = false;
      setSaving(false);
    }
  }

  async function refreshDocumentMeta(idToRefresh: number) {
    const [freshDoc, diag, printModel] = await Promise.all([
      requestJson<DocumentoComercial>(`/api/documentos-comerciais/${idToRefresh}`),
      requestJson<DiagnosticoDocumento>(`/api/documentos-comerciais/${idToRefresh}/diagnostico`),
      requestJson<DocumentoImpressao>(`/api/documentos-comerciais/${idToRefresh}/impressao`)
    ]);
    setDocumento(freshDoc);
    setDiagnostico(diag);
    setImpressao(printModel);
    return freshDoc;
  }

  async function emitDocument() {
    if (!documento || saving || !canEmit) return;
    if (dirty) {
      setError("Guarde o rascunho antes de emitir.");
      return;
    }
    const reference = documentRef(documento);
    if (!window.confirm(`Emitir definitivamente ${reference}? Depois de emitido, o documento fica imutável.`)) return;
    setSaving(true);
    setError(null);
    try {
      const emitted = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${documento.id}/emitir`, { emissorId: currentUser.codigo }, "POST");
      await refreshAfterStateChange(emitted.id, "Documento emitido.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível emitir o documento.");
    } finally {
      setSaving(false);
    }
  }

  async function voidDocument() {
    if (!documento || saving || !canVoid) return;
    if (motivoAnulacao.trim().length < 5) {
      setError("Indica um motivo de anulação com pelo menos 5 caracteres.");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const voided = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${documento.id}/anular`, { motivo: motivoAnulacao.trim() }, "POST");
      setAnularOpen(false);
      setMotivoAnulacao("");
      await refreshAfterStateChange(voided.id, "Documento anulado.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível anular o documento.");
    } finally {
      setSaving(false);
    }
  }

  async function deleteDraft() {
    if (!documento || documento.estado !== "RASCUNHO" || saving || !canDeleteDraft) return;
    setSaving(true);
    setError(null);
    try {
      await requestNoContent(`/api/documentos-comerciais/${documento.id}`, "DELETE");
      setDirty(false);
      showToast({ detail: "Rascunho eliminado com sucesso.", severity: "success", summary: "Documentos" });
      navigate("/documentos", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível eliminar o rascunho.");
    } finally {
      setSaving(false);
    }
  }

  async function refreshAfterStateChange(idToRefresh: number, message: string) {
    const [freshDoc, freshLines] = await Promise.all([
      refreshDocumentMeta(idToRefresh),
      requestJson<LinhaDocumento[]>(`/api/documentos-comerciais/${idToRefresh}/linhas`)
    ]);
    const nextHeader = headerFromDocument(freshDoc);
    const nextLines = freshLines.slice().sort((left, right) => left.numeroLinha - right.numeroLinha).map(lineFromDto);
    setHeader(nextHeader);
    setLines(resequenceLines(nextLines.map((line) => ({ ...line, dirty: false }))));
    setActiveLine(emptyLine());
    setOriginalHeaderKey(headerKey(nextHeader));
    setOriginalOrderKey(orderKey(nextLines));
    setRemovedLineIds([]);
    setDirty(false);
    setNotice(message);
    showToast({ detail: message, severity: "success", summary: "Documentos" });
  }

  async function openPdf() {
    if (!documento || !canPdf) return;
    try {
      const response = await apiFetch(`/api/documentos-comerciais/${documento.id}/pdf`);
      if (!response.ok) throw new Error(await responseError(response));
      const blob = await response.blob();
      window.open(URL.createObjectURL(blob), "_blank", "noopener,noreferrer");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível abrir o PDF.");
    }
  }

  const content = (
    <section className="fac-draft-editor" onKeyDown={handleKeyDown}>
      <header className="fac-draft-appbar">
        <div className="fac-draft-title">
          <strong>Documentos</strong>
          <span>{documento ? documentRef(documento) : "Novo rascunho"}</span>
          {documento && <span className={`fac-draft-status ${documento.estado.toLowerCase()}`}>{estadoLabel(documento.estado)}</span>}
        </div>
        <div className="fac-draft-actions">
          <GlobalSearch className="fac-commercial-global-search" />
          {dirty && <span className="fac-draft-dirty">Alteracoes por guardar</span>}
          <FacButton icon="pi pi-arrow-left" label="Voltar à listagem" onClick={goBack} variant="ghost" />
          {documento && <FacButton disabled={saving || loading} icon="pi pi-plus" label="Novo documento" onClick={newDocument} variant="secondary" />}
          {documento && documento.estado === "RASCUNHO" && canDeleteDraft && <FacButton disabled={saving || loading} icon="pi pi-trash" label="Eliminar rascunho" onClick={() => setDeleteOpen(true)} variant="destructive" />}
          {canOpenPdfCurrent && <FacButton disabled={saving} icon="pi pi-file-pdf" label="PDF" onClick={openPdf} variant="secondary" />}
          {canVoidCurrent && <FacButton disabled={saving} icon="pi pi-ban" label="Anular documento" onClick={() => setAnularOpen(true)} variant="destructive" />}
          {showEmitAction && <FacButton disabled={saving || loading || !canEmitCurrent} icon="pi pi-check" label="Conferir e emitir" onClick={emitDocument} variant={dirty ? "secondary" : "primary"} />}
          {showSaveDraftAction && <FacButton disabled={saving || loading || !canEditCurrent} icon="pi pi-save" label={saveDraftLabel} onClick={saveDraft} variant="primary" />}
        </div>
      </header>
      {error && <FacMessage tone="error" title="Erro">{error}</FacMessage>}
      {notice && <div className="fac-draft-message success" role="status"><span aria-hidden="true">✓</span><span>{notice}</span></div>}
      {loading ? (
        <div className="fac-draft-loading">A carregar rascunho.</div>
      ) : (
        <>
          <nav className="fac-draft-steps" aria-label="Fases do rascunho">
            <button className={step === "header" ? "active" : ""} onClick={() => setStep("header")} type="button">Cabeçalho e condições</button>
            <button className={step === "lines" ? "active" : ""} onClick={() => setStep("lines")} type="button">Linhas e totais</button>
          </nav>
          {step === "header" ? (
            <DraftHeader catalogos={catalogos} header={header} onChooseClient={chooseClient} onContinue={() => {
              const validation = validateHeader(header);
              if (validation) setError(validation);
              else { setError(null); setStep("lines"); }
            }} onUpdate={updateHeader} readOnly={!canEditCurrent} />
          ) : (
            <DraftLines
              activeLine={activeLine}
              catalogos={catalogos}
              lines={lines}
              onAddBlankLine={addBlankLine}
              onActiveArticleQueryChange={updateActiveArticleQuery}
              onChooseActiveArticle={(value) => chooseArticle(activeLine.uid, value, true)}
              onChooseArticle={chooseArticle}
              onCommitActiveLine={commitPendingLine}
              onDuplicateLine={duplicateLine}
              onMoveLine={moveLine}
              onRemoveLine={removeLine}
              onSelectLine={setSelectedLineUid}
              onUpdateActiveLine={setActivePatch}
              onUpdateLine={updateLine}
              readOnly={!canEditCurrent}
              selectedLineUid={selectedLineUid}
              totals={draftTotals}
            />
          )}
        </>
      )}
      {anularOpen && (
        <div className="fac-draft-void-panel" role="dialog" aria-modal="true" aria-label="Anular documento">
          <div>
            <strong>Anular documento</strong>
            <span>A anulação preserva o documento e a auditoria.</span>
          </div>
          <textarea maxLength={500} minLength={5} onChange={(event) => setMotivoAnulacao(event.target.value)} placeholder="Motivo da anulação" value={motivoAnulacao} />
          <div className="fac-draft-actions">
            <FacButton label="Cancelar" onClick={() => setAnularOpen(false)} variant="ghost" />
            <FacButton disabled={saving} icon="pi pi-ban" label="Anular documento" onClick={voidDocument} variant="destructive" />
          </div>
        </div>
      )}
      {deleteOpen && (
        <div className="fac-draft-void-panel" role="dialog" aria-modal="true" aria-label="Eliminar rascunho">
          <div>
            <strong>Eliminar rascunho?</strong>
            <span>O documento e todas as respetivas linhas serão eliminados. Esta ação não pode ser revertida.</span>
          </div>
          <div className="fac-draft-actions">
            <FacButton label="Cancelar" onClick={() => setDeleteOpen(false)} variant="ghost" />
            <FacButton disabled={saving} icon="pi pi-trash" label="Eliminar rascunho" onClick={deleteDraft} variant="destructive" />
          </div>
        </div>
      )}
    </section>
  );

  if (embedded) return content;

  return (
    <ResponsiveSlot
      desktop={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
      mobile={<MobileShell title="FAC Documentos">{content}</MobileShell>}
      tablet={<DesktopShell sidebar={sidebar}>{content}</DesktopShell>}
    />
  );
}

function DraftHeader({ catalogos, header, onChooseClient, onContinue, onUpdate, readOnly }: { catalogos: Catalogos; header: HeaderState; onChooseClient: (clienteId: string | null) => void; onContinue: () => void; onUpdate: (patch: Partial<HeaderState>) => void; readOnly: boolean }) {
  const series = catalogos.series.filter((serie) => serie.tipoDocumentoId === header.tipoDocumentoId);
  const selectedCliente = catalogos.clientes.find((cliente) => String(cliente.id) === header.clienteId) ?? null;
  return (
    <section className="fac-draft-header-phase">
      <div className="fac-draft-form-grid">
        <FacSelect disabled={readOnly} label="Tipo" onChange={(value) => onUpdate({ tipoDocumentoId: value ?? "", serie: firstSerie(catalogos.series, value ?? "") })} options={catalogos.tiposDocumento.map((tipo) => ({ label: `${tipo.id} - ${tipo.descricao}`, value: tipo.id }))} value={header.tipoDocumentoId} />
        <FacSelect disabled={readOnly} label="Série" onChange={(value) => onUpdate({ serie: value ?? "" })} options={series.map((serie) => ({ label: `${serie.serie} - ${serie.nome}`, value: serie.serie }))} value={header.serie} />
        <FacInputText disabled={readOnly} label="Data" onChange={(event) => onUpdate({ dataEmissao: event.target.value })} type="date" value={header.dataEmissao} />
        <EntityLookupField<Cliente>
          clearable={false}
          columns={clienteLookupColumns}
          dataKey="id"
          disabled={readOnly}
          emptyMessage="Sem clientes para selecionar."
          label="Cliente"
          loading={catalogos.clientes.length === 0}
          optionLabel={clienteLookupLabel}
          optionMeta={(cliente) => [cliente.nif && `NIF ${cliente.nif}`, cliente.localidade].filter(Boolean).join(" · ")}
          onSelect={(cliente) => onChooseClient(String(cliente.id))}
          placeholder="Selecionar cliente"
          preferenceKey="fac.lookup.draft.clientes"
          searchFields={clienteSearchFields}
          selection={selectedCliente}
          title="Selecionar cliente"
          value={catalogos.clientes.filter((cliente) => !cliente.inativo)}
          valueLabel={selectedCliente ? clienteLookupLabel(selectedCliente) : undefined}
        />
        <FacSelect disabled={readOnly} label="Armazém de carga" onChange={(value) => onUpdate({ armazemCargaId: value ?? "" })} options={catalogos.armazens.map((armazem) => ({ label: `${armazem.id} - ${armazem.nome}`, value: armazem.id }))} value={header.armazemCargaId} />
        <FacSelect disabled={readOnly} label="Moeda" onChange={(value) => onUpdate({ moedaId: value ?? "" })} options={catalogos.moedas.map((moeda) => ({ label: moeda.nome, value: moeda.id }))} value={header.moedaId} />
        <FacSelect disabled={readOnly} label="Regime IVA" onChange={(value) => onUpdate({ rivaId: value ?? "" })} options={catalogos.regimesIva.map((regime) => ({ label: regime.nome, value: regime.id }))} value={header.rivaId} />
        <FacSelect disabled={readOnly} label="Modo de pagamento" onChange={(value) => onUpdate({ mPagamentoId: value ?? "" })} options={catalogos.modosPagamento.map((modo) => ({ label: modo.nome, value: String(modo.id) }))} value={header.mPagamentoId} />
        <FacSelect disabled={readOnly} label="Prazo" onChange={(value) => onUpdate({ pPagamentoId: value ?? "" })} options={catalogos.prazosPagamento.map((prazo) => ({ label: prazo.nome, value: prazo.id }))} value={header.pPagamentoId} />
        <FacSelect disabled={readOnly} label="Transporte" onChange={(value) => onUpdate({ transporteId: value ?? "" })} options={catalogos.transportes.map((transporte) => ({ label: transporte.nome, value: String(transporte.id) }))} value={header.transporteId} />
        <label className="fac-draft-textarea"><span>Observações</span><textarea disabled={readOnly} maxLength={250} onChange={(event) => onUpdate({ observacoes: event.target.value })} value={header.observacoes} /></label>
      </div>
      <div className="fac-draft-phase-actions">
        <FacButton icon="pi pi-arrow-right" label="Continuar para linhas" onClick={onContinue} variant="primary" />
      </div>
    </section>
  );
}

function DraftLines(props: {
  activeLine: EditorLine;
  catalogos: Catalogos;
  lines: EditorLine[];
  onAddBlankLine: (tipoLinha: TipoLinha, afterUid?: string) => void;
  onActiveArticleQueryChange: (query: string) => void;
  onChooseActiveArticle: (articleId: string | null) => void;
  onChooseArticle: (uid: string, articleId: string | null) => void;
  onCommitActiveLine: (tipoLinha?: TipoLinha) => PendingLineCommitResult;
  onDuplicateLine: (uid: string) => void;
  onMoveLine: (uid: string, direction: -1 | 1) => void;
  onRemoveLine: (uid: string) => void;
  onSelectLine: (uid: string) => void;
  onUpdateActiveLine: (patch: Partial<EditorLine>) => void;
  onUpdateLine: (uid: string, patch: Partial<EditorLine>) => void;
  readOnly: boolean;
  selectedLineUid: string | null;
  totals: Totals;
}) {
  const gridRef = useRef<HTMLDivElement>(null);
  const [articleDialogLineUid, setArticleDialogLineUid] = useState<string | null>(null);
  const articleDialogLine = articleDialogLineUid
    ? [...props.lines, props.activeLine].find((line) => line.uid === articleDialogLineUid) ?? null
    : null;
  const articleDialogSelection = articleDialogLine ? props.catalogos.artigos.find((artigo) => artigo.codigo === articleDialogLine.artigoId) ?? null : null;
  function openArticleDialogFromCell(cell: HTMLElement) {
    if (cell.dataset.gridColumn !== "0") return;
    const row = Number(cell.dataset.gridRow ?? -1);
    const line = row === props.lines.length ? props.activeLine : props.lines[row];
    if (line) setArticleDialogLineUid(line.uid);
  }
  useEffect(() => {
    function onNativeGridKeyDown(event: globalThis.KeyboardEvent) {
      if (event.key !== "F2" && !(event.altKey && event.key === "ArrowDown")) return;
      const target = event.target as HTMLElement | null;
      const cell = target?.closest<HTMLElement>("[data-grid-cell]");
      if (!cell || !gridRef.current?.contains(cell)) return;
      event.preventDefault();
      openArticleDialogFromCell(cell);
    }
    document.addEventListener("keydown", onNativeGridKeyDown, true);
    return () => document.removeEventListener("keydown", onNativeGridKeyDown, true);
  }, [props.activeLine, props.lines]);
  function focusGridCell(current: HTMLElement, direction: "next" | "prev" | "up" | "down") {
    const cells = Array.from(gridRef.current?.querySelectorAll<HTMLElement>("[data-grid-cell]") ?? []).filter((cell) => cell.tabIndex >= 0);
    const index = cells.indexOf(current.closest<HTMLElement>("[data-grid-cell]") ?? current);
    if (index < 0) return;
    const column = Number(cells[index].dataset.gridColumn ?? 0);
    let target = direction === "next" ? cells[index + 1] : direction === "prev" ? cells[index - 1] : undefined;
    if (direction === "up" || direction === "down") {
      const row = Number(cells[index].dataset.gridRow ?? 0) + (direction === "up" ? -1 : 1);
      target = cells.find((cell) => Number(cell.dataset.gridRow ?? -1) === row && Number(cell.dataset.gridColumn ?? -1) === column);
    }
    target?.focus();
  }
  function handleGridKeyDown(event: KeyboardEvent<HTMLElement>) {
    const target = event.target as HTMLElement;
    const cell = target.closest<HTMLElement>("[data-grid-cell]");
    if (!cell) return;
    if (event.key === "F2" || (event.altKey && event.key === "ArrowDown")) {
      event.preventDefault();
      openArticleDialogFromCell(cell);
      return;
    }
    if (target.matches("input, textarea, select") || target.closest(".fac-lookup-wrap")) return;
    if (event.key === "Enter") {
      event.preventDefault();
      cell.querySelector<HTMLElement>("input, textarea, select, .fac-lookup-input")?.focus();
      return;
    }
    if (event.key === "ArrowRight") { event.preventDefault(); focusGridCell(cell, "next"); }
    if (event.key === "ArrowLeft") { event.preventDefault(); focusGridCell(cell, "prev"); }
    if (event.key === "ArrowDown") { event.preventDefault(); focusGridCell(cell, "down"); }
    if (event.key === "ArrowUp") { event.preventDefault(); focusGridCell(cell, "up"); }
  }
  return (
    <section className="fac-draft-lines-phase">
      <div className="fac-draft-lines-toolbar">
        <FacButton disabled={props.readOnly} icon="pi pi-plus" label="Adicionar linha" onClick={() => props.onAddBlankLine("TEXTO", props.selectedLineUid ?? undefined)} variant="secondary" />
      </div>
      <div className="fac-draft-lines-wrap" onKeyDownCapture={handleGridKeyDown} ref={gridRef}>
        <table className="fac-draft-lines-table">
          <colgroup>
            <col className="fac-draft-col-number" />
            <col className="fac-draft-col-article" />
            <col className="fac-draft-col-description" />
            <col className="fac-draft-col-qty" />
            <col className="fac-draft-col-unit" />
            <col className="fac-draft-col-price" />
            <col className="fac-draft-col-discount" />
            <col className="fac-draft-col-vat" />
            <col className="fac-draft-col-total" />
            <col className="fac-draft-col-actions" />
          </colgroup>
          <thead><tr><th>#</th><th>Artigo</th><th>Descrição</th><th>Qtd.</th><th>Un.</th><th>Preço</th><th>Desc.</th><th>IVA</th><th>Total</th><th></th></tr></thead>
          <tbody>
            {props.lines.map((line, index) => (
              <DraftLineRow index={index} key={line.uid} line={line} {...props} />
            ))}
            {!props.readOnly && <DraftLineRow active index={props.lines.length} line={props.activeLine} {...props} />}
          </tbody>
        </table>
      </div>
      <div className="fac-draft-mobile-lines">
        {props.lines.map((line, index) => (
          <DraftLineCard index={index} key={line.uid} line={line} {...props} />
        ))}
        {!props.readOnly && <DraftLineCard active index={props.lines.length} line={props.activeLine} {...props} />}
      </div>
      <EntityLookupDialog<Artigo>
        columns={artigoLookupColumns(props.catalogos.tiposIva)}
        dataKey="codigo"
        emptyMessage="Sem artigos para selecionar."
        loading={props.catalogos.artigos.length === 0}
        onHide={() => setArticleDialogLineUid(null)}
        onSelect={(artigo) => {
          if (!articleDialogLine) return;
          if (articleDialogLine.uid === props.activeLine.uid) props.onChooseActiveArticle(artigo.codigo);
          else props.onChooseArticle(articleDialogLine.uid, artigo.codigo);
          setArticleDialogLineUid(null);
          const rowIndex = articleDialogLine.uid === props.activeLine.uid ? props.lines.length : articleDialogLine.numeroLinha - 1;
          window.setTimeout(() => gridRef.current?.querySelector<HTMLElement>(`[data-grid-row="${rowIndex}"][data-grid-column="0"]`)?.focus(), 0);
        }}
        preferenceKey="fac.lookup.draft.artigos"
        searchFields={artigoSearchFields(props.catalogos.tiposIva)}
        selection={articleDialogSelection}
        title="Selecionar artigo"
        value={props.catalogos.artigos.filter((artigo) => !artigo.inativo)}
        visible={Boolean(articleDialogLine)}
      />
      <aside className="fac-draft-totals">
        <div><span>Subtotal</span><strong>{money(props.totals.subtotal)}</strong></div>
        <div><span>Descontos</span><strong>{money(props.totals.discount)}</strong></div>
        <div><span>IVA</span><strong>{money(props.totals.vat)}</strong></div>
        <div className="fac-draft-total-final"><span>Total provisorio</span><strong>{money(props.totals.total)}</strong></div>
      </aside>
    </section>
  );
}

function DraftLineRow(props: Parameters<typeof DraftLines>[0] & { active?: boolean; index: number; line: EditorLine }) {
  const { active = false, catalogos, index, line } = props;
  const hasArticle = Boolean(line.artigoId);
  const isTextLine = !hasArticle && !active;
  const selected = props.selectedLineUid === line.uid || active;
  const disabled = props.readOnly && !active;
  const update = (patch: Partial<EditorLine>) => active ? props.onUpdateActiveLine(patch) : props.onUpdateLine(line.uid, patch);
  const selectedArticle = catalogos.artigos.find((artigo) => artigo.codigo === line.artigoId) ?? null;
  const cellProps = (column: number, editable = true) => ({
    "data-grid-cell": editable ? true : undefined,
    "data-grid-column": column,
    "data-grid-row": index,
    onMouseDown: (event: MouseEvent<HTMLElement>) => {
      if (!editable || disabled) return;
      const target = event.target as HTMLElement;
      if (target.closest(".fac-draft-row-actions")) return;
      event.preventDefault();
      event.currentTarget.focus();
    },
    tabIndex: editable && !disabled ? 0 : undefined
  });
  const inactiveCell = <span className="fac-draft-empty-cell" aria-hidden="true"></span>;
  return (
    <tr className={`${selected ? "selected" : ""} ${isTextLine ? "text-line" : ""} ${isTextLine && !line.descricao.trim() ? "text-line-empty" : ""} ${active ? "active-line" : ""}`} onFocus={() => !active && props.onSelectLine(line.uid)} onMouseDown={() => !active && props.onSelectLine(line.uid)}>
      <td className="fac-draft-line-number">{active ? "+" : index + 1}</td>
      <td className={`fac-draft-article-cell ${isTextLine ? "fac-draft-article-empty" : ""}`} {...cellProps(0, hasArticle || active)}>
        {isTextLine ? (
          <span className="fac-draft-text-separator-mark" title={line.descricao.trim() ? "Linha de texto" : "Linha de texto vazia"}></span>
        ) : (
          <>
            <span className="fac-draft-cell-rest fac-draft-article-rest" title={selectedArticle?.codigo ?? ""}>{selectedArticle?.codigo || "Selecionar artigo"}</span>
            <EntityLookupField<Artigo>
              clearable
              columns={artigoLookupColumns(catalogos.tiposIva)}
              dataKey="codigo"
              disabled={disabled}
              emptyMessage="Sem artigos para selecionar."
              loading={catalogos.artigos.length === 0}
              optionLabel={artigoLookupLabel}
              optionMeta={(artigo) => [artigo.unidade, artigo.familiaId ? `Família ${artigo.familiaId}` : null, money(Number(artigo.pvp))].filter(Boolean).join(" · ")}
              onClear={() => active ? props.onChooseActiveArticle(null) : props.onChooseArticle(line.uid, null)}
              onQueryChange={active ? props.onActiveArticleQueryChange : undefined}
              onSelect={(artigo) => active ? props.onChooseActiveArticle(artigo.codigo) : props.onChooseArticle(line.uid, artigo.codigo)}
              placeholder="Pesquisar artigo ou serviço"
              preferenceKey="fac.lookup.draft.artigos"
              searchFields={artigoSearchFields(catalogos.tiposIva)}
              showAllSuggestionsOnEmptyQuery
              selection={selectedArticle}
              title="Selecionar artigo"
              value={catalogos.artigos.filter((artigo) => !artigo.inativo)}
              valueLabel={selectedArticle ? selectedArticle.codigo : undefined}
            />
          </>
        )}
      </td>
      <td className="fac-draft-description-cell" {...cellProps(1)}><input aria-label="Descrição da linha" className="fac-draft-cell fac-draft-cell-display" data-active-line={active || undefined} disabled={disabled} maxLength={80} onChange={(event) => update({ descricao: event.target.value })} title={line.descricao} value={line.descricao} /></td>
      <td {...cellProps(2, hasArticle)}>{hasArticle ? <DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ quantidade: value })} value={line.quantidade} /> : inactiveCell}</td>
      <td className="fac-draft-unit-cell" {...cellProps(3, hasArticle)}>{hasArticle ? <input aria-label="Unidade" className="fac-draft-cell fac-draft-cell-code fac-draft-cell-display" data-active-line={active || undefined} disabled={disabled} maxLength={6} onChange={(event) => update({ unidade: event.target.value })} title={line.unidade} value={line.unidade} /> : inactiveCell}</td>
      <td {...cellProps(4, hasArticle)}>{hasArticle ? <DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ precoUnitario: value })} value={line.precoUnitario} /> : inactiveCell}</td>
      <td {...cellProps(5, hasArticle)}>{hasArticle ? <DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ desconto: value })} value={line.desconto} /> : inactiveCell}</td>
      <td className="fac-draft-vat-cell fac-draft-choice-cell" {...cellProps(6, hasArticle)}>{hasArticle ? <><span className="fac-draft-cell-rest">{line.tipoTaxaIvaId ? ivaCompactLabel(catalogos.tiposIva.find((iva) => iva.id === line.tipoTaxaIvaId) ?? { descricao: line.tipoTaxaIvaId, id: line.tipoTaxaIvaId, inativo: false }) : "IVA"}</span><select aria-label="IVA" className="fac-draft-cell fac-draft-cell-code fac-draft-cell-display" data-active-line={active || undefined} disabled={disabled} onChange={(event) => update({ tipoTaxaIvaId: event.target.value })} title={catalogos.tiposIva.find((iva) => iva.id === line.tipoTaxaIvaId)?.descricao} value={line.tipoTaxaIvaId}><option value="">IVA</option>{catalogos.tiposIva.map((iva) => <option key={iva.id} value={iva.id}>{ivaCompactLabel(iva)}</option>)}</select></> : inactiveCell}</td>
      <td className={`fac-draft-money ${active ? "fac-draft-money-empty" : ""}`}>{hasArticle ? money(lineTotal(line, catalogos)) : ""}</td>
      <td className="fac-draft-row-actions">{active ? null : <RowActions {...props} index={index} line={line} />}</td>
    </tr>
  );
}
function DraftLineCard(props: Parameters<typeof DraftLines>[0] & { active?: boolean; index: number; line: EditorLine }) {
  const { active = false, catalogos, index, line } = props;
  const hasArticle = Boolean(line.artigoId);
  const isTextLine = !hasArticle && !active;
  const disabled = props.readOnly && !active;
  const selectedArticle = catalogos.artigos.find((artigo) => artigo.codigo === line.artigoId) ?? null;
  const update = (patch: Partial<EditorLine>) => active ? props.onUpdateActiveLine(patch) : props.onUpdateLine(line.uid, patch);
  return (
    <article className={`fac-draft-line-card ${props.selectedLineUid === line.uid || active ? "selected" : ""} ${isTextLine ? "text-line" : ""} ${isTextLine && !line.descricao.trim() ? "text-line-empty" : ""} ${active ? "active-line" : ""}`} onFocus={() => !active && props.onSelectLine(line.uid)} onMouseDown={() => !active && props.onSelectLine(line.uid)}>
      <div className="fac-draft-card-top"><span>{active ? "Nova linha" : `Linha ${index + 1}`}</span></div>
      {!isTextLine && (
        <EntityLookupField<Artigo> clearable columns={artigoLookupColumns(catalogos.tiposIva)} dataKey="codigo" disabled={disabled} emptyMessage="Sem artigos para selecionar." loading={catalogos.artigos.length === 0} optionLabel={artigoLookupLabel} optionMeta={(artigo) => [artigo.unidade, artigo.familiaId ? `Família ${artigo.familiaId}` : null, money(Number(artigo.pvp))].filter(Boolean).join(" · ")} onClear={() => active ? props.onChooseActiveArticle(null) : props.onChooseArticle(line.uid, null)} onQueryChange={active ? props.onActiveArticleQueryChange : undefined} onSelect={(artigo) => active ? props.onChooseActiveArticle(artigo.codigo) : props.onChooseArticle(line.uid, artigo.codigo)} placeholder="Pesquisar artigo" preferenceKey="fac.lookup.draft.artigos" searchFields={artigoSearchFields(catalogos.tiposIva)} selection={selectedArticle} title="Selecionar artigo" value={catalogos.artigos.filter((artigo) => !artigo.inativo)} valueLabel={selectedArticle ? selectedArticle.codigo : undefined} />
      )}
      <input aria-label="Descrição da linha" className="fac-draft-cell fac-draft-cell-display" disabled={disabled} maxLength={80} onChange={(event) => update({ descricao: event.target.value })} placeholder="Descrição" value={line.descricao} />
      {hasArticle && (
        <>
          <div className="fac-draft-card-grid"><DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ quantidade: value })} value={line.quantidade} /><input aria-label="Unidade" className="fac-draft-cell fac-draft-cell-code fac-draft-cell-display" disabled={disabled} maxLength={6} onChange={(event) => update({ unidade: event.target.value })} value={line.unidade} /></div>
          <div className="fac-draft-card-grid"><DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ precoUnitario: value })} value={line.precoUnitario} /><DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ desconto: value })} value={line.desconto} /><select aria-label="IVA" className="fac-draft-cell fac-draft-cell-code fac-draft-cell-display" disabled={disabled} onChange={(event) => update({ tipoTaxaIvaId: event.target.value })} value={line.tipoTaxaIvaId}><option value="">IVA</option>{catalogos.tiposIva.map((iva) => <option key={iva.id} value={iva.id}>{ivaCompactLabel(iva)}</option>)}</select></div>
          <div className="fac-draft-card-total"><span>Total</span><strong>{money(lineTotal(line, catalogos))}</strong></div>
        </>
      )}
      {!active && <div className="fac-draft-row-actions"><RowActions {...props} index={index} line={line} /></div>}
    </article>
  );
}
function RowActions(props: Parameters<typeof DraftLines>[0] & { index: number; line: EditorLine }) {
  if (props.readOnly) return null;
  const isFirst = props.index === 0;
  const isLast = props.index >= props.lines.length - 1;
  return (
    <>
      <button aria-label="Mover linha para cima" disabled={isFirst} onClick={() => props.onMoveLine(props.line.uid, -1)} title="Mover para cima" type="button"><i aria-hidden="true" className="pi pi-arrow-up" /></button>
      <button aria-label="Mover linha para baixo" disabled={isLast} onClick={() => props.onMoveLine(props.line.uid, 1)} title="Mover para baixo" type="button"><i aria-hidden="true" className="pi pi-arrow-down" /></button>
      <button aria-label="Eliminar linha" onClick={() => props.onRemoveLine(props.line.uid)} title="Eliminar" type="button"><i aria-hidden="true" className="pi pi-trash" /></button>
    </>
  );
}

function DecimalInput({ active, disabled, min, onChange, value }: { active?: boolean; disabled: boolean; min: number; onChange: (value: string) => void; value: string }) {
  return (
    <InputNumber
      className="fac-draft-number"
      data-active-line={active || undefined}
      disabled={disabled}
      inputClassName="fac-draft-cell fac-draft-cell-number"
      locale="pt-PT"
      maxFractionDigits={6}
      min={min}
      minFractionDigits={0}
      onFocus={(event) => event.target.select()}
      onValueChange={(event: InputNumberValueChangeEvent) => onChange(event.value === null || event.value === undefined ? "" : String(event.value))}
      useGrouping
      value={decimalValue(value)}
    />
  );
}

function decimalValue(value: string) {
  if (!value.trim()) return null;
  const compact = value.replace(/\s/g, "");
  const normalized = compact.includes(",") ? compact.replace(/\./g, "").replace(",", ".") : compact;
  const parsed = Number(normalized);
  return Number.isFinite(parsed) ? parsed : null;
}

const clienteLookupColumns: EntityLookupColumn<Cliente>[] = [
  { defaultVisible: true, field: "nome", filterable: true, globalSearch: true, header: "Nome", required: true, sortable: true },
  { defaultVisible: true, field: "nif", filterable: true, globalSearch: true, header: "NIF", sortable: true, width: "9rem" },
  { defaultVisible: true, field: "localidade", filterable: true, globalSearch: true, header: "Localidade", sortable: true },
  { body: (cliente) => cliente.tm || cliente.tel || "-", defaultVisible: true, field: "tel", globalSearch: true, header: "Telefone", sortable: true, width: "9rem" },
  { field: "id", header: "ID", sortable: true, width: "6rem" },
  { field: "email", globalSearch: true, header: "Email", sortable: true },
  { field: "paisId", header: "País", sortable: true, width: "7rem" },
  { field: "codPostalId", header: "Código postal", sortable: true, width: "9rem" },
  { body: (cliente) => cliente.inativo ? "Sim" : "Não", field: "inativo", header: "Inativo", sortable: true, width: "7rem" }
];

const clienteSearchFields: EntityLookupSearchField<Cliente>[] = [
  { fields: ["nome"], key: "nome" },
  { fields: ["nif"], key: "nif" },
  { fields: ["localidade"], key: "localidade" },
  { fields: ["email"], key: "email" },
  { aliases: ["telefone", "telemovel", "telemóvel"], fields: ["tel", "tm"], key: "telefone" },
  { fields: ["id"], key: "id" }
];

function artigoLookupColumns(tiposIva: TipoTaxaIva[]): EntityLookupColumn<Artigo>[] {
  return [
    { defaultVisible: true, field: "codigo", filterable: true, globalSearch: true, header: "Código", required: true, sortable: true, width: "8rem" },
    { defaultVisible: true, field: "descricao", filterable: true, globalSearch: true, header: "Descrição", sortable: true },
    { body: (artigo) => artigo.familiaId ?? "-", defaultVisible: true, field: "familiaId", filterable: true, globalSearch: true, header: "Família", sortable: true, width: "8rem" },
    { defaultVisible: true, field: "unidade", filterable: true, globalSearch: true, header: "Unidade", sortable: true, width: "7rem" },
    { body: (artigo) => money(Number(artigo.pvp)), defaultVisible: true, field: "pvp", header: "PVP", sortable: true, width: "8rem" },
    { body: (artigo) => ivaLookupLabel(artigo.ivaVendaId, tiposIva), defaultVisible: true, field: "ivaVendaId", filterable: true, header: "IVA venda", sortable: true, width: "8rem" },
    { body: (artigo) => artigo.retencao ? "Sim" : "Não", field: "retencao", header: "Retenção", sortable: true, width: "8rem" },
    { body: (artigo) => artigo.inativo ? "Sim" : "Não", field: "inativo", header: "Inativo", sortable: true, width: "7rem" },
    { field: "observacoes", header: "Observações", sortable: true }
  ];
}

function artigoSearchFields(tiposIva: TipoTaxaIva[]): EntityLookupSearchField<Artigo>[] {
  return [
    { aliases: ["id"], fields: ["codigo"], key: "codigo" },
    { fields: ["descricao"], key: "descricao" },
    { fields: ["familiaId"], key: "familia" },
    { fields: ["unidade"], key: "unidade" },
    { fields: ["pvp"], key: "pvp" },
    { getValue: (artigo) => ivaLookupLabel(artigo.ivaVendaId, tiposIva), key: "iva" }
  ];
}

function clienteLookupLabel(cliente: Cliente) {
  return `${cliente.nome}${cliente.nif ? ` - NIF ${cliente.nif}` : ""}`;
}

function artigoLookupLabel(artigo: Artigo) {
  return `${artigo.codigo} - ${artigo.descricao}`;
}

function ivaLookupLabel(ivaId: string, tiposIva: TipoTaxaIva[]) {
  const iva = tiposIva.find((item) => item.id === ivaId);
  return iva ? ivaCompactLabel(iva) : ivaId;
}

function ivaCompactLabel(iva: TipoTaxaIva) {
  const percent = iva.descricao.match(/(\d+(?:[,.]\d+)?)/)?.[1]?.replace(",", ".");
  if (percent) return `${Number(percent).toLocaleString("pt-PT", { maximumFractionDigits: 2 })}%`;
  if (iva.id.length <= 3) return iva.id;
  return iva.id.slice(0, 3).toUpperCase();
}

async function loadCatalogos(): Promise<Catalogos> {
  const [tiposPage, seriesPage, clientes, artigos, armazensPage, moedasPage, regimesPage, modosPage, prazosPage, transportesPage, tiposIvaPage] = await Promise.all([
    fetchPage<TipoDocumento>("/api/tipos-documento?size=100&sort=id,asc"),
    fetchPage<Serie>("/api/series?size=100&sort=tipoDocumento.id,asc&sort=serie,asc"),
    fetchAllPages<Cliente>("/api/clientes", "nome,asc"),
    fetchAllPages<Artigo>("/api/artigos", "descricao,asc"),
    fetchPage<Armazem>("/api/armazens?size=100&sort=nome,asc"),
    fetchPage<CatalogoString>("/api/moedas?size=100&sort=nome,asc"),
    fetchPage<CatalogoString>("/api/riva?size=100&sort=nome,asc"),
    fetchPage<CatalogoString>("/api/mpagamentos?size=100&sort=nome,asc"),
    fetchPage<CatalogoString>("/api/p-pagamentos?size=100&sort=nome,asc"),
    fetchPage<CatalogoString>("/api/transportes?size=100&sort=nome,asc"),
    fetchPage<TipoTaxaIva>("/api/tipos-taxa-iva?size=100&sort=descricao,asc")
  ]);
  return {
    artigos: artigos.filter((artigo) => !artigo.inativo),
    armazens: armazensPage.content,
    clientes,
    moedas: moedasPage.content,
    regimesIva: regimesPage.content,
    series: seriesPage.content,
    tiposDocumento: tiposPage.content.filter((tipo) => tipo.areaGestao === 2),
    tiposIva: tiposIvaPage.content.filter((iva) => !iva.inativo),
    transportes: transportesPage.content,
    modosPagamento: modosPage.content,
    prazosPagamento: prazosPage.content
  };
}

async function fetchPage<T>(url: string): Promise<Page<T>> {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

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

async function requestJson<T>(url: string, body?: unknown, method: "GET" | "POST" | "PUT" = "GET"): Promise<T> {
  const response = await apiFetch(url, body === undefined ? undefined : { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
  return response.status === 204 ? (undefined as T) : response.json();
}

async function fetchOptional<T>(url: string): Promise<T | null> {
  const response = await apiFetch(url);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function requestNoContent(url: string, method: "DELETE" | "PUT", body?: unknown) {
  const response = await apiFetch(url, { method, headers: body === undefined ? undefined : { "Content-Type": "application/json" }, body: body === undefined ? undefined : JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
}

async function responseError(response: Response) {
  try {
    const data = await response.json();
    return data.message ?? data.error ?? `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}

function initialiseHeader(catalogos: Catalogos, parametros: ParametrosDocumentoComercial | null): { header: HeaderState; warnings: string[] } {
  const warnings: string[] = [];
  const tipoDocumentoId = validTipoDocumentoId(catalogos, parametros?.tipoDocumentoId);
  const serie = tipoDocumentoId ? validSerie(catalogos, tipoDocumentoId, parametros?.serie) : "";
  if (parametros?.tipoDocumentoId && (!tipoDocumentoId || !serie)) {
    warnings.push("Os valores base de tipo/série já não são válidos e não foram aplicados.");
  }
  const armazemConfigurado = parametros?.armazemCargaId && catalogos.armazens.some((armazem) => String(armazem.id) === String(parametros.armazemCargaId))
    ? String(parametros.armazemCargaId)
    : "";
  if (parametros?.armazemCargaId && !armazemConfigurado) {
    warnings.push("O armazém base já não existe; foi aplicado o primeiro armazém disponível.");
  }
  return { header: {
    ...emptyHeader,
    tipoDocumentoId,
    serie,
    armazemCargaId: armazemConfigurado || (catalogos.armazens[0] ? String(catalogos.armazens[0].id) : ""),
    moedaId: preferredCatalogId(catalogos.moedas, "EUR"),
    rivaId: preferredCatalogId(catalogos.regimesIva, "CON")
  }, warnings };
}

function headerFromDocument(documento: DocumentoComercial): HeaderState {
  return {
    tipoDocumentoId: documento.tipoDocumentoId,
    serie: documento.serie,
    dataEmissao: documento.dataEmissao,
    clienteId: String(documento.clienteId),
    armazemCargaId: documento.armazemCargaId ? String(documento.armazemCargaId) : "",
    moedaId: documento.moedaId ?? "",
    rivaId: documento.rivaId ?? "",
    mPagamentoId: documento.mPagamentoId ? String(documento.mPagamentoId) : "",
    pPagamentoId: documento.pPagamentoId ?? "",
    transporteId: documento.transporteId ? String(documento.transporteId) : "",
    observacoes: documento.observacoes ?? ""
  };
}

function lineFromDto(line: LinhaDocumento): EditorLine {
  return normalizeLineByArticle({
    uid: crypto.randomUUID(),
    id: line.id,
    tipoLinha: line.artigoId ? "COMERCIAL" : "TEXTO",
    numeroLinha: line.numeroLinha,
    artigoId: line.artigoId ?? "",
    descricao: line.descricao ?? "",
    quantidade: value(line.quantidade),
    unidade: line.unidade ?? "",
    precoUnitario: value(line.precoUnitario),
    tipoDesconto: line.tipoDesconto ?? "VALOR",
    desconto: value(line.desconto ?? line.valorDesconto ?? 0),
    tipoTaxaIvaId: line.tipoTaxaIvaId ?? "",
    dirty: false
  });
}

function normalizeLineByArticle(line: EditorLine): EditorLine {
  if (line.artigoId) {
    return {
      ...line,
      tipoLinha: "COMERCIAL",
      quantidade: line.quantidade || "1",
      precoUnitario: line.precoUnitario || "0",
      desconto: line.desconto || "0"
    };
  }
  return {
    ...line,
    tipoLinha: "TEXTO",
    artigoId: "",
    quantidade: "",
    unidade: "",
    precoUnitario: "",
    desconto: "",
    tipoTaxaIvaId: ""
  };
}

function applyArticleToLine(line: EditorLine, artigo?: Artigo): EditorLine {
  if (!artigo) {
    return normalizeLineByArticle({
      ...line,
      artigoId: "",
      unidade: "",
      precoUnitario: "",
      desconto: "",
      tipoTaxaIvaId: ""
    });
  }
  return normalizeLineByArticle({
    ...line,
    artigoId: artigo.codigo,
    descricao: artigo.descricao,
    quantidade: line.quantidade || "1",
    unidade: artigo.unidade,
    precoUnitario: String(artigo.pvp),
    desconto: line.desconto || "0",
    tipoTaxaIvaId: artigo.ivaVendaId
  });
}

function headerPayload(header: HeaderState) {
  return {
    dataEmissao: header.dataEmissao,
    armazemCargaId: header.armazemCargaId,
    moedaId: nullable(header.moedaId),
    rivaId: nullable(header.rivaId),
    mPagamentoId: header.mPagamentoId || null,
    pPagamentoId: nullable(header.pPagamentoId),
    transporteId: header.transporteId || null,
    observacoes: nullable(header.observacoes)
  };
}

function headerCreatePayload(header: HeaderState) {
  return {
    tipoDocumentoId: header.tipoDocumentoId,
    serie: header.serie,
    clienteId: Number(header.clienteId),
    ...headerPayload(header)
  };
}

function lineCreatePayload(line: EditorLine) {
  const normalized = normalizeLineByArticle(line);
  if (!normalized.artigoId) return { tipoLinha: "TEXTO", descricao: textDescription(normalized.descricao) };
  return { ...lineUpdatePayload(normalized), tipoLinha: "COMERCIAL" };
}

function lineUpdatePayload(line: EditorLine) {
  const normalized = normalizeLineByArticle(line);
  if (!normalized.artigoId) return { descricao: textDescription(normalized.descricao), tipoLinha: "TEXTO" };
  return {
    artigoId: normalized.artigoId,
    descricao: nullable(normalized.descricao),
    quantidade: normalized.quantidade,
    precoUnitario: normalized.precoUnitario,
    tipoDesconto: normalized.tipoDesconto,
    desconto: normalized.desconto || "0",
    tipoTaxaIvaId: nullable(normalized.tipoTaxaIvaId)
  };
}

function validateHeader(header: HeaderState) {
  if (!header.tipoDocumentoId) return "Seleciona o tipo de documento.";
  if (!header.serie) return "Seleciona a série.";
  if (!header.dataEmissao) return "Indica a data de emissão.";
  if (!header.clienteId) return "Seleciona o cliente.";
  if (!header.armazemCargaId) return "Seleciona o armazém de carga.";
  return null;
}

function validateHasCommercialLine(lines: EditorLine[]) {
  return lines.some(isValidCommercialLine)
    ? null
    : "Crie pelo menos uma linha comercial valida antes de guardar.";
}

function validateLine(line: EditorLine) {
  const normalized = normalizeLineByArticle(line);
  if (!normalized.artigoId) return null;
  if (Number(normalized.quantidade) <= 0) return "A quantidade deve ser maior que zero.";
  if (Number(normalized.precoUnitario) < 0) return "O preço não pode ser negativo.";
  if (Number(normalized.desconto || 0) < 0) return "O desconto não pode ser negativo.";
  return null;
}

function isLineFilled(line: EditorLine) {
  return Boolean(line.artigoId || line.tipoLinha === "TEXTO" || line.descricao.trim());
}

function isPendingLineEmpty(line: EditorLine) {
  return !line.artigoId && !line.descricao.trim();
}

function isValidCommercialLine(line: EditorLine) {
  return Boolean(line.artigoId) && validateLine(line) === null;
}

function calculateTotals(lines: EditorLine[], catalogos: Catalogos): Totals {
  return lines.reduce<Totals>((acc, line) => {
    if (!line.artigoId) return acc;
    const values = lineCommercialValues(line, catalogos);
    return {
      subtotal: acc.subtotal + values.base,
      discount: acc.discount + values.discount,
      vat: acc.vat + values.vat,
      total: acc.total + values.total
    };
  }, { discount: 0, subtotal: 0, total: 0, vat: 0 });
}

function lineTotal(line: EditorLine, catalogos: Catalogos) {
  if (!line.artigoId) return 0;
  return lineCommercialValues(line, catalogos).total;
}

function lineCommercialValues(line: EditorLine, catalogos: Catalogos) {
  const base = Number(line.quantidade || 0) * Number(line.precoUnitario || 0);
  const rawDiscount = Number(line.desconto || 0);
  const discount = line.tipoDesconto === "PERCENTAGEM" ? base * (rawDiscount / 100) : rawDiscount;
  const taxable = Math.max(base - discount, 0);
  const iva = catalogos.tiposIva.find((item) => item.id === line.tipoTaxaIvaId)?.descricao.match(/(\d+(?:[,.]\d+)?)/)?.[1]?.replace(",", ".");
  const vat = taxable * (Number(iva ?? 0) / 100);
  return { base, discount, taxable, total: taxable + vat, vat };
}

function resequenceLines(lines: EditorLine[]) {
  return lines.map((line, index) => ({ ...line, numeroLinha: index + 1 }));
}

function orderKey(lines: EditorLine[]) {
  return lines.map((line) => line.id ?? line.uid).join("|");
}

function headerKey(header: HeaderState) {
  return JSON.stringify(header);
}

function hasHeaderPatchChanges(header: HeaderState, patch: Partial<HeaderState>) {
  return Object.entries(patch).some(([key, nextValue]) => header[key as keyof HeaderState] !== nextValue);
}

function hasLinePatchChanges(line: EditorLine, patch: Partial<EditorLine>) {
  return Object.entries(patch).some(([key, nextValue]) => {
    const field = key as keyof EditorLine;
    const currentValue = line[field];
    if (field === "quantidade" || field === "precoUnitario" || field === "desconto") {
      return decimalValue(String(currentValue ?? "")) !== decimalValue(String(nextValue ?? ""));
    }
    return currentValue !== nextValue;
  });
}

function assertOrder(lines: EditorLine[], expectedIds: number[]) {
  const actual = lines.map((line) => line.id);
  if (expectedIds.length && expectedIds.some((id, index) => actual[index] !== id)) {
    throw new Error("O rascunho foi gravado, mas a ordem devolvida pelo servidor não coincide com a ordem editada.");
  }
}

function validTipoDocumentoId(catalogos: Catalogos, tipoDocumentoId?: string | null) {
  if (!tipoDocumentoId) return "";
  return catalogos.tiposDocumento.some((tipo) => tipo.id === tipoDocumentoId) ? tipoDocumentoId : "";
}

function validSerie(catalogos: Catalogos, tipoDocumentoId: string, serie?: string | null) {
  if (!serie) return "";
  return catalogos.series.some((item) => item.tipoDocumentoId === tipoDocumentoId && item.serie === serie) ? serie : "";
}

function firstSerie(series: Serie[], tipoDocumentoId: string) {
  return series.find((serie) => serie.tipoDocumentoId === tipoDocumentoId)?.serie ?? "";
}

function preferredCatalogId(items: CatalogoString[], preferredId: string) {
  return items.some((item) => item.id === preferredId) ? preferredId : items[0]?.id ?? "";
}

function nullable(value: string) {
  return value.trim() ? value.trim() : null;
}

function textDescription(value: string) {
  return value.trim();
}

function nullableNumber(value: string) {
  return value ? Number(value) : null;
}

function value(input: string | number | null | undefined) {
  return input === null || input === undefined ? "" : String(input);
}

function money(value: number) {
  return new Intl.NumberFormat("pt-PT", { currency: "EUR", minimumFractionDigits: 2, style: "currency" }).format(value);
}

function documentRef(documento: DocumentoComercial) {
  return documento.numeroDocumentoCompleto || `${documento.tipoDocumentoId} ${documento.serie}/${documento.numeroDocumento ?? documento.id}`;
}

function estadoLabel(estado: EstadoDocumento) {
  if (estado === "EMITIDO") return "Emitido";
  if (estado === "ANULADO") return "Anulado";
  return "Rascunho";
}

function confirmLeave(dirty: boolean) {
  return !dirty || window.confirm("Existem alteracoes por guardar. Sair sem guardar?");
}
