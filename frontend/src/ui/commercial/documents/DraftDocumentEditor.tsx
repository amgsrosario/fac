import { KeyboardEvent, useEffect, useMemo, useState } from "react";
import { InputNumber, InputNumberValueChangeEvent } from "primereact/inputnumber";
import { useNavigate, useParams } from "react-router-dom";
import { apiFetch, AuthSession } from "../../../api";
import { DesktopShell, EntityLookupColumn, EntityLookupField, EntityLookupSearchField, FacButton, FacInputText, FacMessage, FacSelect, MobileShell, ResponsiveSlot, useFacToast } from "../../fac";
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
type Artigo = { codigo: string; abreviatura?: string | null; codigoIdentificacao?: string | null; descricao: string; unidade: string; familiaId?: number | null; peso?: string | number | null; ivaCompraId?: string | null; pvp: number; ivaVendaId: string; inativo: boolean; retencao?: boolean; observacoes?: string | null };
type CatalogoString = { id: string; nome: string };
type CatalogoNumero = { id: number; nome: string };
type TipoTaxaIva = { id: string; descricao: string; inativo: boolean };
type Armazem = { id: string; nome: string };

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

const emptyLine = (tipoLinha: TipoLinha = "COMERCIAL"): EditorLine => ({
  uid: crypto.randomUUID(),
  tipoLinha,
  numeroLinha: 0,
  artigoId: "",
  descricao: "",
  quantidade: tipoLinha === "COMERCIAL" ? "1" : "",
  unidade: "",
  precoUnitario: tipoLinha === "COMERCIAL" ? "0" : "",
  tipoDesconto: "VALOR",
  desconto: "0",
  tipoTaxaIvaId: "",
  dirty: true
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
  const [activeLine, setActiveLine] = useState<EditorLine>(() => emptyLine("COMERCIAL"));
  const [removedLineIds, setRemovedLineIds] = useState<number[]>([]);
  const [selectedLineUid, setSelectedLineUid] = useState<string | null>(null);
  const [originalHeaderKey, setOriginalHeaderKey] = useState("");
  const [originalOrderKey, setOriginalOrderKey] = useState("");
  const [dirty, setDirty] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
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
  const totals = useMemo(() => calculateTotals(lines, catalogos), [catalogos, lines]);
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
          setNotice("Documento aberto em modo consulta. A edicao fica bloqueada fora de RASCUNHO.");
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
        setActiveLine(emptyLine("COMERCIAL"));
        setRemovedLineIds([]);
        setDirty(false);
      } else {
        const nextHeader = initialiseHeader(loadedCatalogos);
        setDocumento(null);
        setHeader(nextHeader);
        setLines([]);
        setOriginalHeaderKey(headerKey(nextHeader));
        setOriginalOrderKey("");
        setDiagnostico(null);
        setImpressao(null);
        setActiveLine(emptyLine("COMERCIAL"));
        setRemovedLineIds([]);
        setDirty(false);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel carregar o editor de rascunho.");
    } finally {
      setLoading(false);
    }
  }

  function updateHeader(patch: Partial<HeaderState>) {
    if (!canEditCurrent) return;
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
    setLines((current) => current.map((line) => line.uid === uid ? { ...line, ...patch, dirty: true } : line));
    setDirty(true);
  }

  function chooseArticle(uid: string, artigoId: string | null, draft = false) {
    if (!canEditCurrent) return;
    const artigo = catalogos.artigos.find((item) => item.codigo === artigoId);
    const patch: Partial<EditorLine> = {
      artigoId: artigo?.codigo ?? "",
      descricao: artigo?.descricao ?? "",
      unidade: artigo?.unidade ?? "",
      precoUnitario: artigo ? String(artigo.pvp) : "",
      tipoTaxaIvaId: artigo?.ivaVendaId ?? ""
    };
    if (draft) {
      setActiveLine((current) => ({ ...current, ...patch }));
      setDirty(true);
      return;
    }
    updateLine(uid, patch);
  }

  function setActivePatch(patch: Partial<EditorLine>) {
    if (!canEditCurrent) return;
    setActiveLine((current) => ({ ...current, ...patch }));
    setDirty(true);
  }

  function commitActiveLine(tipoLinha = activeLine.tipoLinha) {
    if (!canEditCurrent) return false;
    const line = { ...activeLine, tipoLinha };
    if (!isLineFilled(line)) {
      setNotice("A linha ativa continua local e nao foi adicionada.");
      return false;
    }
    const validation = validateLine(line);
    if (validation) {
      setError(validation);
      return false;
    }
    const committed = { ...line, uid: crypto.randomUUID(), dirty: true };
    setLines((current) => resequenceLines([...current, committed]));
    setActiveLine(emptyLine(tipoLinha === "TEXTO" ? "TEXTO" : "COMERCIAL"));
    setSelectedLineUid(committed.uid);
    setDirty(true);
    setNotice("Linha adicionada localmente.");
    return true;
  }

  function addBlankLine(tipoLinha: TipoLinha, afterUid?: string) {
    if (!canEditCurrent) return;
    const nextLine = emptyLine(tipoLinha);
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
      commitActiveLine("TEXTO");
      return;
    }
    if (event.key === "Enter") {
      const target = event.target as HTMLElement;
      if (target.matches("textarea")) return;
      if (target.hasAttribute("data-active-line") || target.closest("[data-active-line]")) {
        event.preventDefault();
        commitActiveLine("COMERCIAL");
      }
    }
  }

  function goBack() {
    if (!confirmLeave(dirty)) return;
    navigate("/documentos");
  }

  async function saveDraft() {
    if (saving || !canEditCurrent) return;
    setError(null);
    setNotice(null);
    const draftLines = isLineFilled(activeLine) ? resequenceLines([...lines, { ...activeLine, uid: crypto.randomUUID(), dirty: true }]) : lines;
    const localLines = resequenceLines(draftLines.filter((line) => !isEmptyCommercialLine(line)));
    const validation = validateHeader(header) ?? validateHasCommercialLine(localLines) ?? localLines.map(validateLine).find(Boolean) ?? null;
    if (validation) {
      setError(validation);
      return;
    }

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
        setDocumento(created);
      } else if (headerKey(header) !== originalHeaderKey) {
        await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${currentId}`, headerPayload(header), "PUT");
      }

      if (!currentId) throw new Error("Nao foi possivel obter o identificador do documento.");

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

      const [freshDoc, freshLines] = await Promise.all([
        requestJson<DocumentoComercial>(`/api/documentos-comerciais/${currentId}`),
        requestJson<LinhaDocumento[]>(`/api/documentos-comerciais/${currentId}/linhas`)
      ]);
      const freshEditorLines = freshLines.slice().sort((left, right) => left.numeroLinha - right.numeroLinha).map(lineFromDto);
      assertOrder(freshEditorLines, orderIds);
      const nextHeader = headerFromDocument(freshDoc);
      setDocumento(freshDoc);
      setHeader(nextHeader);
      setLines(resequenceLines(freshEditorLines.map((line) => ({ ...line, dirty: false }))));
      setActiveLine(emptyLine("COMERCIAL"));
      setRemovedLineIds([]);
      setOriginalHeaderKey(headerKey(nextHeader));
      setOriginalOrderKey(orderKey(freshEditorLines));
      await refreshDocumentMeta(currentId);
      setDirty(false);
      setNotice("Rascunho guardado.");
      showToast({ detail: "Rascunho guardado.", severity: "success", summary: "Documentos" });
      if (!documentId) navigate(`/documentos/${currentId}`, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel guardar o rascunho. As alteracoes locais foram mantidas.");
    } finally {
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
    setSaving(true);
    setError(null);
    try {
      const emitted = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${documento.id}/emitir`, { emissorId: currentUser.codigo }, "POST");
      await refreshAfterStateChange(emitted.id, "Documento emitido.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel emitir o documento.");
    } finally {
      setSaving(false);
    }
  }

  async function voidDocument() {
    if (!documento || saving || !canVoid) return;
    if (motivoAnulacao.trim().length < 5) {
      setError("Indica um motivo de anulacao com pelo menos 5 caracteres.");
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
      setError(err instanceof Error ? err.message : "Nao foi possivel anular o documento.");
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
      setError(err instanceof Error ? err.message : "Nao foi possivel eliminar o rascunho.");
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
    setActiveLine(emptyLine("COMERCIAL"));
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
      setError(err instanceof Error ? err.message : "Nao foi possivel abrir o PDF.");
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
          {dirty && <span className="fac-draft-dirty">Alteracoes por guardar</span>}
          <FacButton icon="pi pi-arrow-left" label="Lista" onClick={goBack} variant="ghost" />
          {documento && documento.estado === "RASCUNHO" && canDeleteDraft && <FacButton disabled={saving || loading} icon="pi pi-trash" label="Eliminar rascunho" onClick={() => setDeleteOpen(true)} variant="destructive" />}
          {canOpenPdfCurrent && <FacButton disabled={saving} icon="pi pi-file-pdf" label="PDF" onClick={openPdf} variant="secondary" />}
          {canVoidCurrent && <FacButton disabled={saving} icon="pi pi-ban" label="Anular documento" onClick={() => setAnularOpen(true)} variant="destructive" />}
          {canEmit && isDraft && <FacButton disabled={saving || loading || !canEmitCurrent} icon="pi pi-check" label="Conferir e emitir" onClick={emitDocument} variant="secondary" />}
          {isDraft && <FacButton disabled={saving || loading || !canEditCurrent} icon="pi pi-save" label={saving ? "A guardar..." : "Guardar rascunho"} onClick={saveDraft} variant="primary" />}
        </div>
      </header>
      {error && <FacMessage tone="error" title="Erro">{error}</FacMessage>}
      {notice && <FacMessage tone="success" title="Estado">{notice}</FacMessage>}
      {loading ? (
        <div className="fac-draft-loading">A carregar rascunho.</div>
      ) : (
        <>
          <nav className="fac-draft-steps" aria-label="Fases do rascunho">
            <button className={step === "header" ? "active" : ""} onClick={() => setStep("header")} type="button">1. Cabecalho e condicoes</button>
            <button className={step === "lines" ? "active" : ""} onClick={() => setStep("lines")} type="button">2. Linhas e totais</button>
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
              onChooseActiveArticle={(value) => chooseArticle(activeLine.uid, value, true)}
              onChooseArticle={chooseArticle}
              onCommitActiveLine={commitActiveLine}
              onDuplicateLine={duplicateLine}
              onMoveLine={moveLine}
              onRemoveLine={removeLine}
              onSelectLine={setSelectedLineUid}
              onUpdateActiveLine={setActivePatch}
              onUpdateLine={updateLine}
              readOnly={!canEditCurrent}
              selectedLineUid={selectedLineUid}
              totals={totals}
            />
          )}
        </>
      )}
      {anularOpen && (
        <div className="fac-draft-void-panel" role="dialog" aria-modal="true" aria-label="Anular documento">
          <div>
            <strong>Anular documento</strong>
            <span>A anulacao preserva o documento e a auditoria.</span>
          </div>
          <textarea maxLength={500} minLength={5} onChange={(event) => setMotivoAnulacao(event.target.value)} placeholder="Motivo da anulacao" value={motivoAnulacao} />
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
            <span>O documento e todas as respetivas linhas serao eliminados. Esta acao nao pode ser revertida.</span>
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
        <FacSelect disabled={readOnly} label="Serie" onChange={(value) => onUpdate({ serie: value ?? "" })} options={series.map((serie) => ({ label: `${serie.serie} - ${serie.nome}`, value: serie.serie }))} value={header.serie} />
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
        <FacSelect disabled={readOnly} label="Armazem de carga" onChange={(value) => onUpdate({ armazemCargaId: value ?? "" })} options={catalogos.armazens.map((armazem) => ({ label: `${armazem.id} - ${armazem.nome}`, value: armazem.id }))} value={header.armazemCargaId} />
        <FacSelect disabled={readOnly} label="Moeda" onChange={(value) => onUpdate({ moedaId: value ?? "" })} options={catalogos.moedas.map((moeda) => ({ label: moeda.nome, value: moeda.id }))} value={header.moedaId} />
        <FacSelect disabled={readOnly} label="Regime IVA" onChange={(value) => onUpdate({ rivaId: value ?? "" })} options={catalogos.regimesIva.map((regime) => ({ label: regime.nome, value: regime.id }))} value={header.rivaId} />
        <FacSelect disabled={readOnly} label="Modo de pagamento" onChange={(value) => onUpdate({ mPagamentoId: value ?? "" })} options={catalogos.modosPagamento.map((modo) => ({ label: modo.nome, value: String(modo.id) }))} value={header.mPagamentoId} />
        <FacSelect disabled={readOnly} label="Prazo" onChange={(value) => onUpdate({ pPagamentoId: value ?? "" })} options={catalogos.prazosPagamento.map((prazo) => ({ label: prazo.nome, value: prazo.id }))} value={header.pPagamentoId} />
        <FacSelect disabled={readOnly} label="Transporte" onChange={(value) => onUpdate({ transporteId: value ?? "" })} options={catalogos.transportes.map((transporte) => ({ label: transporte.nome, value: String(transporte.id) }))} value={header.transporteId} />
        <label className="fac-draft-textarea"><span>Observacoes</span><textarea disabled={readOnly} maxLength={250} onChange={(event) => onUpdate({ observacoes: event.target.value })} value={header.observacoes} /></label>
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
  onChooseActiveArticle: (articleId: string | null) => void;
  onChooseArticle: (uid: string, articleId: string | null) => void;
  onCommitActiveLine: (tipoLinha?: TipoLinha) => boolean;
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
  return (
    <section className="fac-draft-lines-phase">
      <div className="fac-draft-lines-toolbar">
        <FacButton disabled={props.readOnly} icon="pi pi-align-left" label="Inserir texto" onClick={() => props.onAddBlankLine("TEXTO", props.selectedLineUid ?? undefined)} variant="secondary" />
        <FacButton disabled={props.readOnly} icon="pi pi-plus" label="Inserir comercial" onClick={() => props.onAddBlankLine("COMERCIAL", props.selectedLineUid ?? undefined)} variant="secondary" />
      </div>
      <div className="fac-draft-lines-wrap">
        <table className="fac-draft-lines-table">
          <colgroup>
            <col className="fac-draft-col-number" />
            <col className="fac-draft-col-type" />
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
          <thead><tr><th>#</th><th>Tipo</th><th>Artigo</th><th>Descricao</th><th>Qtd.</th><th>Un.</th><th>Preco</th><th>Desc.</th><th>IVA</th><th>Total</th><th></th></tr></thead>
          <tbody>
            {props.lines.map((line, index) => (
              <DraftLineRow index={index} key={line.uid} line={line} {...props} />
            ))}
            {!props.readOnly && <DraftLineRow active index={props.lines.length} line={props.activeLine} {...props} />}
          </tbody>
        </table>
      </div>
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
  const isText = line.tipoLinha === "TEXTO";
  const selected = props.selectedLineUid === line.uid || active;
  const disabled = props.readOnly && !active;
  const update = (patch: Partial<EditorLine>) => active ? props.onUpdateActiveLine(patch) : props.onUpdateLine(line.uid, patch);
  const selectedArticle = catalogos.artigos.find((artigo) => artigo.codigo === line.artigoId) ?? null;
  return (
    <tr className={`${selected ? "selected" : ""} ${isText ? "text-line" : ""} ${active ? "active-line" : ""}`} onFocus={() => !active && props.onSelectLine(line.uid)} onMouseDown={() => !active && props.onSelectLine(line.uid)}>
      <td>{index + 1}</td>
      <td>
        {active ? (
          <select className="fac-draft-cell" data-active-line disabled={props.readOnly} onChange={(event) => update({ tipoLinha: event.target.value as TipoLinha })} value={line.tipoLinha}>
            <option value="COMERCIAL">Comercial</option>
            <option value="TEXTO">Texto</option>
          </select>
        ) : <span>{isText ? "Texto" : "Comercial"}</span>}
      </td>
      {isText ? (
        <>
          <td colSpan={8}><textarea className="fac-draft-line-text" data-active-line={active || undefined} disabled={disabled} maxLength={80} onChange={(event) => update({ descricao: event.target.value })} placeholder="Texto documental" value={line.descricao} /></td>
          <td className="fac-draft-row-actions">{active ? <FacButton label="OK" onClick={() => props.onCommitActiveLine("TEXTO")} variant="secondary" /> : <RowActions {...props} line={line} />}</td>
        </>
      ) : (
        <>
          <td>
            <EntityLookupField<Artigo>
              clearable
              columns={artigoLookupColumns(catalogos.tiposIva)}
              dataKey="codigo"
              disabled={disabled}
              emptyMessage="Sem artigos para selecionar."
              loading={catalogos.artigos.length === 0}
              optionLabel={artigoLookupLabel}
              optionMeta={(artigo) => [artigo.unidade, artigo.familiaId ? `Familia ${artigo.familiaId}` : null, money(Number(artigo.pvp))].filter(Boolean).join(" · ")}
              onClear={() => active ? props.onChooseActiveArticle(null) : props.onChooseArticle(line.uid, null)}
              onSelect={(artigo) => active ? props.onChooseActiveArticle(artigo.codigo) : props.onChooseArticle(line.uid, artigo.codigo)}
              placeholder="Artigo"
              preferenceKey="fac.lookup.draft.artigos"
              searchFields={artigoSearchFields(catalogos.tiposIva)}
              selection={selectedArticle}
              title="Selecionar artigo"
              value={catalogos.artigos.filter((artigo) => !artigo.inativo)}
              valueLabel={selectedArticle ? selectedArticle.codigo : undefined}
            />
          </td>
          <td><input className="fac-draft-cell" data-active-line={active || undefined} disabled={disabled} maxLength={80} onChange={(event) => update({ descricao: event.target.value })} value={line.descricao} /></td>
          <td><DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ quantidade: value })} value={line.quantidade} /></td>
          <td><input className="fac-draft-cell" data-active-line={active || undefined} disabled={disabled} onChange={(event) => update({ unidade: event.target.value })} value={line.unidade} /></td>
          <td><DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ precoUnitario: value })} value={line.precoUnitario} /></td>
          <td><DecimalInput active={active} disabled={disabled} min={0} onChange={(value) => update({ desconto: value })} value={line.desconto} /></td>
          <td><select className="fac-draft-cell" data-active-line={active || undefined} disabled={disabled} onChange={(event) => update({ tipoTaxaIvaId: event.target.value })} title={catalogos.tiposIva.find((iva) => iva.id === line.tipoTaxaIvaId)?.descricao} value={line.tipoTaxaIvaId}><option value="">IVA</option>{catalogos.tiposIva.map((iva) => <option key={iva.id} value={iva.id}>{ivaCompactLabel(iva)}</option>)}</select></td>
          <td className="fac-draft-money">{money(lineTotal(line, catalogos))}</td>
          <td className="fac-draft-row-actions">{active ? <FacButton label="OK" onClick={() => props.onCommitActiveLine("COMERCIAL")} variant="secondary" /> : <RowActions {...props} line={line} />}</td>
        </>
      )}
    </tr>
  );
}

function RowActions(props: Parameters<typeof DraftLines>[0] & { line: EditorLine }) {
  if (props.readOnly) return null;
  return (
    <>
      <button aria-label="Subir linha" onClick={() => props.onMoveLine(props.line.uid, -1)} type="button">↑</button>
      <button aria-label="Descer linha" onClick={() => props.onMoveLine(props.line.uid, 1)} type="button">↓</button>
      <button aria-label="Duplicar linha" onClick={() => props.onDuplicateLine(props.line.uid)} type="button">Duplicar</button>
      <button aria-label="Remover linha" onClick={() => props.onRemoveLine(props.line.uid)} type="button">X</button>
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
  { field: "paisId", header: "Pais", sortable: true, width: "7rem" },
  { field: "codPostalId", header: "Codigo postal", sortable: true, width: "9rem" },
  { body: (cliente) => cliente.inativo ? "Sim" : "Nao", field: "inativo", header: "Inativo", sortable: true, width: "7rem" }
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
    { defaultVisible: true, field: "codigo", filterable: true, globalSearch: true, header: "Codigo", required: true, sortable: true, width: "8rem" },
    { defaultVisible: true, field: "descricao", filterable: true, globalSearch: true, header: "Descricao", sortable: true },
    { body: (artigo) => artigo.familiaId ?? "-", defaultVisible: true, field: "familiaId", filterable: true, globalSearch: true, header: "Familia", sortable: true, width: "8rem" },
    { defaultVisible: true, field: "unidade", filterable: true, globalSearch: true, header: "Unidade", sortable: true, width: "7rem" },
    { body: (artigo) => money(Number(artigo.pvp)), defaultVisible: true, field: "pvp", header: "PVP", sortable: true, width: "8rem" },
    { body: (artigo) => ivaLookupLabel(artigo.ivaVendaId, tiposIva), defaultVisible: true, field: "ivaVendaId", filterable: true, header: "IVA venda", sortable: true, width: "8rem" },
    { body: (artigo) => artigo.retencao ? "Sim" : "Nao", field: "retencao", header: "Retencao", sortable: true, width: "8rem" },
    { body: (artigo) => artigo.inativo ? "Sim" : "Nao", field: "inativo", header: "Inativo", sortable: true, width: "7rem" },
    { field: "observacoes", header: "Observacoes", sortable: true }
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

function initialiseHeader(catalogos: Catalogos): HeaderState {
  const tipoDocumentoId = catalogos.tiposDocumento[0]?.id ?? "";
  return {
    ...emptyHeader,
    tipoDocumentoId,
    serie: firstSerie(catalogos.series, tipoDocumentoId),
    armazemCargaId: catalogos.armazens[0] ? String(catalogos.armazens[0].id) : "",
    moedaId: catalogos.moedas[0]?.id ?? "",
    rivaId: catalogos.regimesIva[0]?.id ?? ""
  };
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
  const tipoLinha = line.tipoLinha ?? "COMERCIAL";
  return {
    uid: crypto.randomUUID(),
    id: line.id,
    tipoLinha,
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
  };
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
  if (line.tipoLinha === "TEXTO") return { tipoLinha: "TEXTO", descricao: nullable(line.descricao) };
  return { ...lineUpdatePayload(line), tipoLinha: "COMERCIAL" };
}

function lineUpdatePayload(line: EditorLine) {
  if (line.tipoLinha === "TEXTO") return { descricao: nullable(line.descricao) };
  return {
    artigoId: line.artigoId,
    descricao: nullable(line.descricao),
    quantidade: line.quantidade,
    precoUnitario: line.precoUnitario,
    tipoDesconto: line.tipoDesconto,
    desconto: line.desconto || "0",
    tipoTaxaIvaId: nullable(line.tipoTaxaIvaId)
  };
}

function validateHeader(header: HeaderState) {
  if (!header.tipoDocumentoId) return "Seleciona o tipo de documento.";
  if (!header.serie) return "Seleciona a serie.";
  if (!header.dataEmissao) return "Indica a data de emissao.";
  if (!header.clienteId) return "Seleciona o cliente.";
  if (!header.armazemCargaId) return "Seleciona o armazem de carga.";
  return null;
}

function validateHasCommercialLine(lines: EditorLine[]) {
  return lines.some(isValidCommercialLine)
    ? null
    : "Crie pelo menos uma linha comercial valida antes de guardar.";
}

function validateLine(line: EditorLine) {
  if (line.tipoLinha === "TEXTO") return line.descricao.trim() ? null : "A linha de texto precisa de descricao.";
  if (isEmptyCommercialLine(line)) return null;
  if (!line.artigoId) return "Cada linha comercial precisa de artigo.";
  if (!line.descricao.trim()) return "Cada linha comercial precisa de descricao.";
  if (Number(line.quantidade) <= 0) return "A quantidade deve ser maior que zero.";
  if (Number(line.precoUnitario) < 0) return "O preco nao pode ser negativo.";
  if (Number(line.desconto || 0) < 0) return "O desconto nao pode ser negativo.";
  return null;
}

function isLineFilled(line: EditorLine) {
  if (line.tipoLinha === "TEXTO") return Boolean(line.descricao.trim());
  return !isEmptyCommercialLine(line);
}

function isValidCommercialLine(line: EditorLine) {
  return line.tipoLinha === "COMERCIAL" && !isEmptyCommercialLine(line) && validateLine(line) === null;
}

function isEmptyCommercialLine(line: EditorLine) {
  if (line.tipoLinha !== "COMERCIAL") return false;
  return !line.artigoId
    && !line.descricao.trim()
    && !hasRelevantQuantity(line.quantidade)
    && !hasRelevantNumber(line.precoUnitario)
    && !hasRelevantNumber(line.desconto)
    && !line.tipoTaxaIvaId;
}

function hasRelevantQuantity(value: string) {
  if (!value.trim()) return false;
  return Number(value) !== 1;
}

function hasRelevantNumber(value: string) {
  if (!value.trim()) return false;
  return Number(value) !== 0;
}

function calculateTotals(lines: EditorLine[], catalogos: Catalogos): Totals {
  return lines.reduce<Totals>((acc, line) => {
    if (line.tipoLinha === "TEXTO" || isEmptyCommercialLine(line)) return acc;
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
  if (line.tipoLinha === "TEXTO" || isEmptyCommercialLine(line)) return 0;
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

function assertOrder(lines: EditorLine[], expectedIds: number[]) {
  const actual = lines.map((line) => line.id);
  if (expectedIds.length && expectedIds.some((id, index) => actual[index] !== id)) {
    throw new Error("O rascunho foi gravado, mas a ordem devolvida pelo servidor nao coincide com a ordem editada.");
  }
}

function firstSerie(series: Serie[], tipoDocumentoId: string) {
  return series.find((serie) => serie.tipoDocumentoId === tipoDocumentoId)?.serie ?? "";
}

function nullable(value: string) {
  return value.trim() ? value.trim() : null;
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
