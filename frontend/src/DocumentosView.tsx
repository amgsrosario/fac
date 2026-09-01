import { useEffect, useMemo, useRef, useState } from "react";
import { Paginator } from "primereact/paginator";
import { useNavigate } from "react-router-dom";
import { apiFetch, getAuthSession, hasPermission } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";
import { EntityDetailOverlay } from "./EntityContext";
import { decimal, integer, money } from "./ui/tuuli/format";

type Page<T> = {
  content: T[];
  totalElements: number;
};

const DOCUMENTO_COLUMNS: ConfigurableColumn[] = [
  { key: "documento", label: "Documento", visible: true },
  { key: "cliente", label: "Cliente", visible: true },
  { key: "nif", label: "NIF", visible: false },
  { key: "emissao", label: "Emissão", visible: true },
  { key: "vencimento", label: "Vencimento", visible: true },
  { key: "moeda", label: "Moeda", visible: false },
  { key: "bruto", label: "Bruto", visible: false },
  { key: "desconto", label: "Desconto", visible: false },
  { key: "iva", label: "IVA", visible: false },
  { key: "total", label: "Total", visible: true },
  { key: "estado", label: "Estado", visible: true },
  { key: "impresso", label: "Impresso", visible: false },
  { key: "liquidado", label: "Liquidado", visible: true }
];

type DocumentoComercial = {
  id: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento: number | null;
  estado: string;
  dataEmissao: string;
  dataVencimento?: string;
  clienteId: number;
  clienteNome: string;
  clienteNif: string;
  moedaId: string;
  valorBruto: number;
  valorDesconto: number;
  valorIvaTotal: number;
  valorRetencao: number;
  valorTotal: number;
  observacoes?: string;
  emissorId?: string;
  anulado: boolean;
  motivoAnulacao?: string;
  dataHoraAnulacao?: string;
  anuladoPorUtilizadorId?: string;
  anuladoPorNome?: string;
  impresso: boolean;
  liquidado: boolean;
};

type LinhaDocumento = {
  id: number;
  numeroLinha: number;
  artigoId: string;
  descricao: string;
  quantidade: number;
  precoUnitario: number;
  valorBruto: number;
  valorDesconto: number;
  valorLinha: number;
  tipoTaxaIvaId: string;
  percentagemIva: number;
};

type Cliente = {
  id: number;
  nome: string;
  nif: string;
};

type TipoDocumento = {
  id: string;
  descricao: string;
  areaGestao: number;
};

type Serie = {
  serie: string;
  tipoDocumentoId: string;
  nome: string;
};

type Armazem = {
  id: string;
  nome: string;
};

type Artigo = {
  codigo: string;
  descricao: string;
  pvp: number;
  inativo: boolean;
};

type DiagnosticoDocumento = {
  referencia: string;
  podeEmitir: boolean;
  alertas: string[];
  bloqueios: string[];
  pendente: {
    existe: boolean;
    id?: number;
    valorDocumento?: number;
    valorPendente?: number;
  };
  totais: {
    cabecalhoValorTotal: number;
    linhasValorTotal: number;
    coerente: boolean;
  };
};

type DraftForm = {
  tipoDocumentoId: string;
  serie: string;
  dataEmissao: string;
  clienteId: string;
  armazemCargaId: string;
  observacoes: string;
};

type ParametrosDocumento = {
  tipoDocumentoId?: string;
  serie?: string;
  armazemCargaId?: string;
};

type LineForm = {
  artigoId: string;
  descricao: string;
  quantidade: string;
  precoUnitario: string;
  tipoDesconto: "PERCENTAGEM" | "VALOR";
  desconto: string;
};

const emptyDraftForm: DraftForm = {
  tipoDocumentoId: "",
  serie: "",
  dataEmissao: todayIso(),
  clienteId: "",
  armazemCargaId: "",
  observacoes: ""
};

const emptyLineForm: LineForm = {
  artigoId: "",
  descricao: "",
  quantidade: "1",
  precoUnitario: "0",
  tipoDesconto: "PERCENTAGEM",
  desconto: "0"
};

export default function DocumentosView() {
  const navigate = useNavigate();
  const [documentos, setDocumentos] = useState<DocumentoComercial[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const detailTriggerRef = useRef<HTMLButtonElement>(null);
  const [linhas, setLinhas] = useState<LinhaDocumento[]>([]);
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [linesLoading, setLinesLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [draftForm, setDraftForm] = useState<DraftForm>(emptyDraftForm);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [tiposDocumento, setTiposDocumento] = useState<TipoDocumento[]>([]);
  const [series, setSeries] = useState<Serie[]>([]);
  const [armazens, setArmazens] = useState<Armazem[]>([]);
  const [artigos, setArtigos] = useState<Artigo[]>([]);
  const [lineEditorOpen, setLineEditorOpen] = useState(false);
  const [lineForm, setLineForm] = useState<LineForm>(emptyLineForm);
  const [emissionOpen, setEmissionOpen] = useState(false);
  const [diagnostico, setDiagnostico] = useState<DiagnosticoDocumento | null>(null);
  const [annulOpen, setAnnulOpen] = useState(false);
  const [annulReason, setAnnulReason] = useState("");
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [columnEditorOpen, setColumnEditorOpen] = useState(false);
  const documentoColumns = useConfiguredColumns("fac.documentos.colunas", DOCUMENTO_COLUMNS);
  const newDocumentClientRef = useRef<HTMLSelectElement>(null);
  const lineArticleRef = useRef<HTMLSelectElement>(null);
  const emissionPanelRef = useRef<HTMLElement>(null);

  useEffect(() => {
    loadDocumentos();
  }, []);

  useEffect(() => {
    setLinhas([]);
    setLineEditorOpen(false);
  }, [selectedId]);

  useEffect(() => {
    if (editorOpen) window.setTimeout(() => newDocumentClientRef.current?.focus(), 0);
    else if (lineEditorOpen) window.setTimeout(() => lineArticleRef.current?.focus(), 0);
  }, [editorOpen, lineEditorOpen]);

  useEffect(() => {
    if (emissionOpen) {
      window.setTimeout(() => emissionPanelRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 0);
    }
  }, [emissionOpen]);

  async function loadDocumentos() {
    setLoading(true);
    setMessage(null);
    try {
      const page = await fetchJson<Page<DocumentoComercial>>("/api/documentos-comerciais?size=200&sort=id,desc");
      setDocumentos(page.content);
      setSelectedId((current) => current ?? page.content[0]?.id ?? null);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar documentos.");
    } finally {
      setLoading(false);
    }
  }

  async function loadLinhas(documentoId: number) {
    setLinesLoading(true);
    setMessage(null);
    try {
      setLinhas(await fetchJson<LinhaDocumento[]>(`/api/documentos-comerciais/${documentoId}/linhas`));
    } catch (error) {
      setLinhas([]);
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar as linhas.");
    } finally {
      setLinesLoading(false);
    }
  }

  async function openLineEditor() {
    if (!selected || selected.estado !== "RASCUNHO") return;
    setMessage(null);
    try {
      if (artigos.length === 0) {
        const page = await fetchJson<Page<Artigo>>("/api/artigos?size=500&sort=descricao,asc");
        setArtigos(page.content.filter((artigo) => !artigo.inativo));
      }
      setLineForm(emptyLineForm);
      setLineEditorOpen(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível carregar os artigos.");
    }
  }

  async function createLine() {
    if (!selected) return;
    const validation = validateLine(lineForm);
    if (validation) {
      setMessage(validation);
      return;
    }
    setLinesLoading(true);
    setMessage(null);
    try {
      await requestJson<LinhaDocumento>(`/api/documentos-comerciais/${selected.id}/linhas`, "POST", {
        artigoId: lineForm.artigoId,
        descricao: blankToNull(lineForm.descricao),
        quantidade: Number(lineForm.quantidade),
        precoUnitario: Number(lineForm.precoUnitario),
        tipoDesconto: lineForm.tipoDesconto,
        desconto: Number(lineForm.desconto),
        tipoTaxaIvaId: null,
        peso: null
      });
      await refreshSelectedDocument(selected.id);
      setLineEditorOpen(false);
      setNotice("Linha adicionada e totais do documento recalculados.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível adicionar a linha.");
    } finally {
      setLinesLoading(false);
    }
  }

  async function deleteLine(lineId: number) {
    if (!selected || !window.confirm("Remover esta linha do rascunho?")) return;
    setLinesLoading(true);
    setMessage(null);
    try {
      const response = await apiFetch(`/api/documentos-comerciais/${selected.id}/linhas/${lineId}`, { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      await refreshSelectedDocument(selected.id);
      setNotice("Linha removida e totais do documento recalculados.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível remover a linha.");
    } finally {
      setLinesLoading(false);
    }
  }

  async function refreshSelectedDocument(documentoId: number) {
    const [documento, linhasAtualizadas] = await Promise.all([
      fetchJson<DocumentoComercial>(`/api/documentos-comerciais/${documentoId}`),
      fetchJson<LinhaDocumento[]>(`/api/documentos-comerciais/${documentoId}/linhas`)
    ]);
    setDocumentos((current) => current.map((item) => item.id === documentoId ? documento : item));
    setLinhas(linhasAtualizadas);
  }

  async function openEmission() {
    if (!selected || selected.estado !== "RASCUNHO") return;
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const diagnosticoAtual = await fetchJson<DiagnosticoDocumento>(`/api/documentos-comerciais/${selected.id}/diagnostico`);
      setDiagnostico(diagnosticoAtual);
      setEmissionOpen(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível preparar a emissão.");
    } finally {
      setLoading(false);
    }
  }

  async function emitDocument() {
    if (!selected || !diagnostico?.podeEmitir) return;
    if (!window.confirm(`Emitir definitivamente ${diagnostico.referencia}? Depois de emitido, o documento fica imutável.`)) return;
    setLoading(true);
    setMessage(null);
    try {
      const emitted = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${selected.id}/emitir`, "POST", { emissorId: getAuthSession()?.codigo });
      const diagnosticoEmitido = await fetchJson<DiagnosticoDocumento>(`/api/documentos-comerciais/${selected.id}/diagnostico`);
      setDocumentos((current) => current.map((item) => item.id === emitted.id ? emitted : item));
      setEmissionOpen(false);
      setDiagnostico(null);
      setLineEditorOpen(false);
      setNotice(diagnosticoEmitido.pendente.existe
        ? `Documento ${reference(emitted)} emitido. Pendente ${diagnosticoEmitido.pendente.id} criado com ${money(diagnosticoEmitido.pendente.valorPendente ?? 0)} ${emitted.moedaId}.`
        : `Documento ${reference(emitted)} emitido sem pendente por ser de liquidação imediata.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível emitir o documento.");
    } finally {
      setLoading(false);
    }
  }

  async function openPdf(id: number) {
    setLoading(true);
    setMessage(null);
    try {
      const response = await apiFetch(`/api/documentos-comerciais/${id}/pdf`);
      if (!response.ok) throw new Error(await responseError(response));
      const url = URL.createObjectURL(await response.blob());
      window.open(url, "_blank", "noopener,noreferrer");
      window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
      setDocumentos((current) => current.map((item) => item.id === id ? { ...item, impresso: true } : item));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível gerar o PDF.");
    } finally {
      setLoading(false);
    }
  }

  async function annulDocument() {
    if (!selected || selected.estado !== "EMITIDO" || selected.anulado) return;
    const reason = annulReason.trim();
    if (reason.length < 5 || reason.length > 500) {
      setMessage("O motivo deve ter entre 5 e 500 caracteres.");
      return;
    }
    if (!window.confirm(`Confirmar a anulação definitiva de ${reference(selected)}?`)) return;
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const annulled = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${selected.id}/anular`, "POST", { motivo: reason });
      setDocumentos((current) => current.map((item) => item.id === annulled.id ? annulled : item));
      setAnnulOpen(false);
      setAnnulReason("");
      setNotice(`${reference(annulled)} anulado.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível anular o documento.");
    } finally {
      setLoading(false);
    }
  }

  async function deleteDraft() {
    if (!selected || selected.estado !== "RASCUNHO") return;
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const response = await apiFetch(`/api/documentos-comerciais/${selected.id}`, { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      const page = await fetchJson<Page<DocumentoComercial>>("/api/documentos-comerciais?size=200&sort=id,desc");
      setDocumentos(page.content);
      setSelectedId(page.content[0]?.id ?? null);
      setDeleteOpen(false);
      setNotice("Rascunho eliminado com sucesso.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível eliminar o rascunho.");
    } finally {
      setLoading(false);
    }
  }

  function openDraftEditor() {
    navigate("/documentos/novo");
  }

  async function createDraft() {
    const validation = validateDraft(draftForm) ?? validateLine(lineForm);
    if (validation) {
      setMessage(validation);
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const created = await requestJson<DocumentoComercial>("/api/documentos-comerciais", "POST", {
        documento: {
          tipoDocumentoId: draftForm.tipoDocumentoId,
          serie: draftForm.serie,
          dataEmissao: draftForm.dataEmissao,
          clienteId: Number(draftForm.clienteId),
          moradaEnvioId: null,
          armazemCargaId: draftForm.armazemCargaId,
          moedaId: null,
          rivaId: null,
          mPagamentoId: null,
          pPagamentoId: null,
          transporteId: null,
          dataCarga: null,
          horaCarga: null,
          matricula: null,
          dataDescarga: null,
          horaDescarga: null,
          peso: null,
          observacoes: blankToNull(draftForm.observacoes)
        },
        linha: {
          artigoId: lineForm.artigoId,
          descricao: blankToNull(lineForm.descricao),
          quantidade: Number(lineForm.quantidade),
          precoUnitario: Number(lineForm.precoUnitario),
          tipoDesconto: lineForm.tipoDesconto,
          desconto: Number(lineForm.desconto),
          tipoTaxaIvaId: null,
          peso: null
        }
      });
      const page = await fetchJson<Page<DocumentoComercial>>("/api/documentos-comerciais?size=200&sort=id,desc");
      setDocumentos(page.content);
      setSelectedId(created.id);
      setEditorOpen(false);
      setLineForm(emptyLineForm);
      setLineEditorOpen(true);
      setNotice(`Documento ${created.tipoDocumentoId} iniciado com a primeira linha. Podes continuar a introduzir linhas.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível criar o documento com a primeira linha.");
    } finally {
      setLoading(false);
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return documentos;
    return documentos.filter((documento) =>
      [reference(documento), documento.clienteNome, documento.clienteNif, documento.estado, String(documento.id)]
        .some((value) => value.toLowerCase().includes(term))
    );
  }, [documentos, search]);
  const pagedDocumentos = filtered.slice(page * pageSize, (page + 1) * pageSize);

  useEffect(() => {
    setPage(0);
  }, [search]);

  useEffect(() => setDetailOpen(false), [selectedId]);

  useEffect(() => {
    const lastPage = Math.max(0, Math.ceil(filtered.length / pageSize) - 1);
    if (page > lastPage) setPage(lastPage);
  }, [filtered.length, page, pageSize]);

  const selected = documentos.find((documento) => documento.id === selectedId) ?? null;
  const emitted = documentos.filter((documento) => documento.estado === "EMITIDO" && !documento.anulado).length;
  const drafts = documentos.filter((documento) => documento.estado === "RASCUNHO").length;
  const annulled = documentos.filter((documento) => documento.anulado).length;
  const availableSeries = series.filter((serie) => serie.tipoDocumentoId === draftForm.tipoDocumentoId);
  const selectedIsDraft = selected?.estado === "RASCUNHO";
  const canCreate = hasPermission("DOCUMENTO_CRIAR");
  const canEdit = hasPermission("DOCUMENTO_EDITAR_RASCUNHO");
  const canDeleteDraft = hasPermission("DOCUMENTO_ELIMINAR_RASCUNHO");
  const canEmit = hasPermission("DOCUMENTO_EMITIR");
  const canAnnul = hasPermission("DOCUMENTO_ANULAR");
  const canPdf = hasPermission("DOCUMENTO_OBTER_PDF");

  function changeDraft<K extends keyof DraftForm>(field: K, value: DraftForm[K]) {
    setDraftForm((current) => ({ ...current, [field]: value }));
  }

  if (editorOpen) {
    return (
      <section className="fac-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Documento comercial</p><h2>Novo documento</h2></div>
          <button className="fac-ghost-button" onClick={() => setEditorOpen(false)} type="button">Voltar a lista</button>
        </div>

        {message && <p className="fac-message">{message}</p>}

        <div className="fac-form-grid">
          <Field label="Tipo de documento">
            <select onChange={(event) => setDraftForm((current) => ({ ...current, tipoDocumentoId: event.target.value, serie: "" }))} value={draftForm.tipoDocumentoId}>
              <option value="">Selecionar</option>
              {tiposDocumento.map((tipo) => <option key={tipo.id} value={tipo.id}>{tipo.id} - {tipo.descricao}</option>)}
            </select>
          </Field>
          <Field label="Série">
            <select disabled={!draftForm.tipoDocumentoId} onChange={(event) => changeDraft("serie", event.target.value)} value={draftForm.serie}>
              <option value="">Selecionar</option>
              {availableSeries.map((serie) => <option key={`${serie.tipoDocumentoId}-${serie.serie}`} value={serie.serie}>{serie.serie} - {serie.nome}</option>)}
            </select>
          </Field>
          <Field label="Data de emissão"><input onChange={(event) => changeDraft("dataEmissao", event.target.value)} type="date" value={draftForm.dataEmissao} /></Field>
          <Field label="Cliente">
            <select ref={newDocumentClientRef} onChange={(event) => changeDraft("clienteId", event.target.value)} value={draftForm.clienteId}>
              <option value="">Selecionar</option>
              {clientes.map((cliente) => <option key={cliente.id} value={cliente.id}>{cliente.nome} - {cliente.nif}</option>)}
            </select>
          </Field>
          <Field label="Armazém de carga">
            <select onChange={(event) => changeDraft("armazemCargaId", event.target.value)} value={draftForm.armazemCargaId}>
              <option value="">Selecionar</option>
              {armazens.map((armazem) => <option key={armazem.id} value={armazem.id}>{armazem.id} - {armazem.nome}</option>)}
            </select>
          </Field>
          <Field label="Observações"><textarea maxLength={250} onChange={(event) => changeDraft("observacoes", event.target.value)} value={draftForm.observacoes} /></Field>
        </div>

        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Primeira linha</p><h2>Conteudo do documento</h2></div>
          <span className="fac-muted">O documento so e gravado quando esta linha for valida.</span>
        </div>
        <div className="fac-form-grid">
          <Field label="Artigo">
            <select onChange={(event) => selectArticle(event.target.value, artigos, setLineForm)} value={lineForm.artigoId}>
              <option value="">Selecionar</option>
              {artigos.map((artigo) => <option key={artigo.codigo} value={artigo.codigo}>{artigo.codigo} - {artigo.descricao}</option>)}
            </select>
          </Field>
          <Field label="Descrição"><input maxLength={80} onChange={(event) => setLineForm((current) => ({ ...current, descricao: event.target.value }))} placeholder="Usa a descrição do artigo" value={lineForm.descricao} /></Field>
          <Field label="Quantidade"><input min="0.000001" onChange={(event) => setLineForm((current) => ({ ...current, quantidade: event.target.value }))} step="0.000001" type="number" value={lineForm.quantidade} /></Field>
          <Field label="Preço unitário"><input min="0" onChange={(event) => setLineForm((current) => ({ ...current, precoUnitario: event.target.value }))} step="0.000001" type="number" value={lineForm.precoUnitario} /></Field>
          <Field label="Tipo de desconto">
            <select onChange={(event) => setLineForm((current) => ({ ...current, tipoDesconto: event.target.value as LineForm["tipoDesconto"] }))} value={lineForm.tipoDesconto}>
              <option value="PERCENTAGEM">Percentagem</option><option value="VALOR">Valor</option>
            </select>
          </Field>
          <Field label={lineForm.tipoDesconto === "PERCENTAGEM" ? "Desconto (%)" : "Desconto (valor)"}><input min="0" onChange={(event) => setLineForm((current) => ({ ...current, desconto: event.target.value }))} step="0.000001" type="number" value={lineForm.desconto} /></Field>
        </div>

        <div className="fac-form-footer">
          <span className="fac-muted">O documento fica em rascunho, sem número definitivo, mas nunca sem linhas.</span>
          <button className="fac-primary-button" disabled={loading} onClick={createDraft} type="button">{loading ? "A criar..." : "Criar documento e guardar primeira linha"}</button>
        </div>
      </section>
    );
  }

  return (
    <div className="tuuli-v2-page tuuli-grammar-document-collection">
      {notice && <p className="fac-editor-message">{notice}</p>}
      {message && <p className="fac-message">{message}</p>}

      <section aria-label="Indicadores da listagem" className="fac-collection-context tuuli-metric-group">
        <div className="fac-collection-metric tuuli-metric"><span>Emitidos ativos</span><strong>{integer(emitted)}</strong></div>
        <div className="fac-collection-metric tuuli-metric"><span>Rascunhos</span><strong>{integer(drafts)}</strong></div>
        <div className="fac-collection-metric tuuli-metric"><span>Anulados</span><strong>{integer(annulled)}</strong></div>
      </section>

      <section className="fac-list-toolbar tuuli-toolbar">
        <label className="fac-documents-search tuuli-search">
          <i aria-hidden="true" className="pi pi-search" />
          <input aria-label="Pesquisar documentos" onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar documento, cliente, NIF ou estado" type="search" value={search} />
        </label>
        <div className="fac-inline-actions">
          <button className="fac-soft-button tuuli-tool-action" disabled={loading} onClick={loadDocumentos} type="button">Atualizar lista</button>
          <div className="fac-inline-actions"><button className="fac-ghost-button tuuli-tool-action" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({documentoColumns.visibleColumns.length})</button>{canCreate && <button className="fac-primary-button tuuli-primary-action" disabled={loading} onClick={openDraftEditor} type="button">Novo documento</button>}</div>
        </div>
      </section>

      {selected && <section aria-label={`Documento selecionado: ${reference(selected)}`} className="fac-document-selection-bar tuuli-context-bar">
        <div className="fac-document-selection-summary tuuli-context-summary">
          <strong>{reference(selected)}</strong>
          <span className="fac-document-selection-client">{selected.clienteNome}</span>
          <span className="fac-document-selection-value">{money(selected.valorTotal)} {selected.moedaId}</span>
          <span>{selected.dataVencimento ? `Venc. ${datePt(selected.dataVencimento)}` : "Sem vencimento"}</span>
          <span className={`fac-document-state ${selected.estado.toLowerCase()}`}>
            {selected.estado === "RASCUNHO" ? "Rasc." : selected.estado === "ANULADO" ? "Anulado" : "Emitido"}
          </span>
        </div>
        <div className="fac-document-selection-actions tuuli-context-actions">
          {!selectedIsDraft && <button className="fac-context-action" disabled={loading} onClick={() => navigate(`/documentos/${selected.id}`)} type="button">Consultar</button>}
          {selectedIsDraft && canEdit && <button className="fac-context-action" disabled={loading} onClick={() => navigate(`/documentos/${selected.id}`)} type="button">Editar</button>}
          {(selected.estado === "EMITIDO" || selected.estado === "ANULADO") && canPdf && <button aria-label={`Abrir PDF de ${reference(selected)}`} className="fac-context-action" disabled={loading} onClick={() => openPdf(selected.id)} title="Abrir PDF" type="button">PDF</button>}
          <button className="fac-context-action" disabled={loading} onClick={() => setDetailOpen(true)} ref={detailTriggerRef} type="button">Detalhe</button>
          {((selectedIsDraft && (canEmit || canDeleteDraft)) || (selected.estado === "EMITIDO" && canAnnul)) && <details className="fac-context-menu">
            <summary aria-haspopup="menu" aria-label={`Mais ações para ${reference(selected)}`} title="Mais ações"><span>Mais</span><i aria-hidden="true" className="pi pi-chevron-down" /></summary>
            <div className="fac-context-menu-items">
              {selectedIsDraft && canEmit && <button disabled={loading} onClick={openEmission} type="button">Conferir e emitir</button>}
              {selectedIsDraft && canDeleteDraft && <button className="danger" disabled={loading} onClick={() => setDeleteOpen(true)} type="button">Eliminar rascunho</button>}
              {selected.estado === "EMITIDO" && canAnnul && <button className="danger" disabled={loading} onClick={() => { setAnnulReason(""); setAnnulOpen(true); }} type="button">Anular documento</button>}
            </div>
          </details>}
        </div>
      </section>}

      <section className="fac-content-grid fac-documents-content-grid">
        <article className="fac-panel fac-panel-main fac-documents-table-panel tuuli-table-surface">
          <ColumnSelector columns={documentoColumns.columns} open={columnEditorOpen} onMove={documentoColumns.moveColumn} onReset={documentoColumns.resetColumns} onToggle={documentoColumns.toggleColumn} />
          <table className="fac-table tuuli-table">
            <thead><tr>{documentoColumns.visibleColumns.map((column) => <th className={documentoColumnClass(column.key)} key={column.key}>{column.label}</th>)}</tr></thead>
            <tbody>
              {pagedDocumentos.map((documento) => (
                <tr className={documento.id === selectedId ? "fac-row-selected tuuli-table-row-selected" : ""} key={documento.id} onClick={() => setSelectedId(documento.id)}>
                  {documentoColumns.visibleColumns.map((column) => <td className={documentoColumnClass(column.key)} key={column.key}>{documentoColumnValue(documento, column.key)}</td>)}
                </tr>
              ))}
              {!loading && filtered.length === 0 && <tr><td colSpan={documentoColumns.visibleColumns.length}>Sem documentos para mostrar.</td></tr>}
            </tbody>
          </table>
          {filtered.length > 0 && <div className="fac-list-pagination tuuli-pagination"><span>{filtered.length} {filtered.length === 1 ? "documento" : "documentos"}</span><Paginator first={page * pageSize} onPageChange={(event) => { setPage(event.page); setPageSize(event.rows); }} rows={pageSize} rowsPerPageOptions={[10, 20, 50]} totalRecords={filtered.length}/></div>}
        </article>

      </section>

      <EntityDetailOverlay labelledBy="fac-document-detail-title" onClose={() => setDetailOpen(false)} open={detailOpen && Boolean(selected)} returnFocusRef={detailTriggerRef}>
        {selected && <div id="fac-document-detail">
          <p className="fac-eyebrow">Documento</p>
          <h2 id="fac-document-detail-title">{reference(selected)}</h2>
          <dl className="fac-entity-detail-rows">
            <div><dt>Cliente</dt><dd>{selected.clienteNome}</dd></div>
            <div><dt>NIF</dt><dd>{selected.clienteNif}</dd></div>
            <div><dt>Estado</dt><dd>{documentState(selected)}</dd></div>
            <div><dt>Emissão</dt><dd>{datePt(selected.dataEmissao)}</dd></div>
            <div><dt>Vencimento</dt><dd>{selected.dataVencimento ? datePt(selected.dataVencimento) : "-"}</dd></div>
            <div><dt>Moeda</dt><dd>{selected.moedaId}</dd></div>
            <div><dt>Bruto</dt><dd>{money(selected.valorBruto)} {selected.moedaId}</dd></div>
            <div><dt>Desconto</dt><dd>{money(selected.valorDesconto)} {selected.moedaId}</dd></div>
            <div><dt>IVA</dt><dd>{money(selected.valorIvaTotal)} {selected.moedaId}</dd></div>
            <div><dt>Total</dt><dd>{money(selected.valorTotal)} {selected.moedaId}</dd></div>
            <div><dt>Liquidado</dt><dd>{selected.liquidado ? "Sim" : "Não"}</dd></div>
            <div><dt>Impresso</dt><dd>{selected.impresso ? "Sim" : "Não"}</dd></div>
            {selected.estado === "ANULADO" && <><div><dt>Motivo da anulação</dt><dd>{selected.motivoAnulacao ?? "-"}</dd></div><div><dt>Anulado em</dt><dd>{selected.dataHoraAnulacao ? new Date(selected.dataHoraAnulacao).toLocaleString("pt-PT") : "-"}</dd></div><div><dt>Anulado por</dt><dd>{selected.anuladoPorNome ?? selected.anuladoPorUtilizadorId ?? "-"}</dd></div></>}
          </dl>
          <div className="fac-entity-detail-actions">
            {!selectedIsDraft && <button className="fac-soft-button" disabled={loading} onClick={() => navigate(`/documentos/${selected.id}`)} type="button">Consultar documento</button>}
            {selectedIsDraft && canEdit && <button className="fac-soft-button" disabled={loading} onClick={() => navigate(`/documentos/${selected.id}`)} type="button">Editar rascunho</button>}
            {selectedIsDraft && canEmit && <button className="fac-gold-button" disabled={loading} onClick={openEmission} type="button">Conferir e emitir</button>}
            {(selected.estado === "EMITIDO" || selected.estado === "ANULADO") && canPdf && <button className="fac-soft-button" disabled={loading} onClick={() => openPdf(selected.id)} type="button">Abrir PDF</button>}
            {selectedIsDraft && canDeleteDraft && <button className="fac-link-danger" disabled={loading} onClick={() => setDeleteOpen(true)} type="button">Eliminar rascunho</button>}
            {selected.estado === "EMITIDO" && canAnnul && <button className="fac-link-danger" disabled={loading} onClick={() => { setAnnulReason(""); setAnnulOpen(true); }} type="button">Anular documento</button>}
          </div>
        </div>}
      </EntityDetailOverlay>

      {annulOpen && selected && <div className="fac-dialog-backdrop" role="presentation"><div aria-labelledby="annul-title" aria-modal="true" className="fac-dialog" role="dialog"><h2 id="annul-title">Anular {reference(selected)}</h2><p>O documento e os dados fiscais originais serão preservados. Esta operação é definitiva.</p><label className="fac-field"><span>Motivo da anulação</span><textarea autoFocus maxLength={500} onChange={(event) => setAnnulReason(event.target.value)} value={annulReason} /></label><small>{annulReason.trim().length}/500 (mínimo 5)</small><div className="fac-inline-actions"><button className="fac-ghost-button" disabled={loading} onClick={() => setAnnulOpen(false)} type="button">Cancelar</button><button className="fac-link-danger" disabled={loading || annulReason.trim().length < 5} onClick={annulDocument} type="button">{loading ? "A anular..." : "Confirmar anulação"}</button></div></div></div>}

      {deleteOpen && selectedIsDraft && selected && <div className="fac-dialog-backdrop" role="presentation"><div aria-labelledby="delete-title" aria-modal="true" className="fac-dialog" role="dialog"><h2 id="delete-title">Eliminar {reference(selected)}?</h2><p>O rascunho e todas as respetivas linhas serão eliminados. Esta ação não pode ser revertida.</p><div className="fac-inline-actions"><button className="fac-ghost-button" disabled={loading} onClick={() => setDeleteOpen(false)} type="button">Cancelar</button><button className="fac-link-danger" disabled={loading} onClick={deleteDraft} type="button">{loading ? "A eliminar..." : "Eliminar rascunho"}</button></div></div></div>}

      {emissionOpen && selectedIsDraft && diagnostico && (
        <section className="fac-panel fac-section-panel fac-emission-panel" ref={emissionPanelRef}>
          <div className="fac-panel-header">
            <div><p className="fac-eyebrow">Emissão definitiva</p><h2>{diagnostico.referencia}</h2></div>
            <button className="fac-ghost-button" onClick={() => setEmissionOpen(false)} type="button">Fechar conferencia</button>
          </div>

          <div className="fac-emission-summary">
            <div><span>Total do cabeçalho</span><strong>{money(diagnostico.totais.cabecalhoValorTotal)} {selected.moedaId}</strong></div>
            <div><span>Total calculado pelas linhas</span><strong>{money(diagnostico.totais.linhasValorTotal)} {selected.moedaId}</strong></div>
            <div><span>Coerência dos totais</span><strong>{diagnostico.totais.coerente ? "Confirmada" : "Com diferenças"}</strong></div>
          </div>

          {diagnostico.bloqueios.length > 0 && <div className="fac-check-list danger"><strong>Emissão bloqueada</strong>{diagnostico.bloqueios.map((item) => <p key={item}>{item}</p>)}</div>}
          {diagnostico.alertas.length > 0 && <div className="fac-check-list warning"><strong>Alertas</strong>{diagnostico.alertas.map((item) => <p key={item}>{item}</p>)}</div>}
          {diagnostico.podeEmitir && diagnostico.alertas.length === 0 && <p className="fac-check-ok">O documento está coerente e pode ser emitido.</p>}

          <div className="fac-form-footer">
            <span className="fac-muted">Emissor: {getAuthSession()?.nome}</span>
            <button className="fac-gold-button" disabled={loading || !diagnostico.podeEmitir} onClick={emitDocument} type="button">Emitir documento</button>
          </div>
          <p className="fac-muted">A emissão atribui o número definitivo, avança o numerador da série e torna o documento imutável.</p>
        </section>
      )}

    </div>
  );
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function fetchOptionalJson<T>(url: string): Promise<T | null> {
  const response = await apiFetch(url);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function requestJson<T>(url: string, method: "POST" | "PUT", body: unknown): Promise<T> {
  const response = await apiFetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function responseError(response: Response) {
  try {
    const payload = await response.json();
    const prefix = response.status === 403 ? "Sem permissão: " : response.status === 409 ? "Conflito: " : response.status === 400 ? "Validação: " : "";
    return prefix + (payload.message || payload.error || `Erro HTTP ${response.status}`);
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}

function openHtml(id: number) {
  window.open(`/api/documentos-comerciais/${id}/diagnostico/html`, "_blank", "noopener,noreferrer");
}

function openJson(id: number) {
  window.open(`/api/documentos-comerciais/${id}/diagnostico`, "_blank", "noopener,noreferrer");
}

function reference(documento: DocumentoComercial) {
  return `${documento.tipoDocumentoId} ${documento.serie}/${documento.numeroDocumento ?? "rascunho"}`;
}

function documentoColumnValue(documento: DocumentoComercial, key: string) {
  switch (key) {
    case "documento": return reference(documento);
    case "cliente": return documento.clienteNome;
    case "nif": return documento.clienteNif;
    case "emissao": return datePt(documento.dataEmissao);
    case "vencimento": return documento.dataVencimento ? datePt(documento.dataVencimento) : "-";
    case "moeda": return documento.moedaId;
    case "bruto": return money(documento.valorBruto);
    case "desconto": return money(documento.valorDesconto);
    case "iva": return money(documento.valorIvaTotal);
    case "total": return `${money(documento.valorTotal)} ${documento.moedaId}`;
    case "estado": return <span className={`fac-status ${documento.anulado ? "danger" : ""}`}>{documentTableState(documento)}</span>;
    case "impresso": return documento.impresso ? "Sim" : "Não";
    case "liquidado": return documento.liquidado ? "Sim" : "Não";
    default: return "-";
  }
}

function documentoColumnClass(key: string) {
  const tone = ["documento", "bruto", "desconto", "iva", "total"].includes(key) ? "tuuli-cell-primary" : "tuuli-cell-secondary";
  const numeric = ["bruto", "desconto", "iva", "total"].includes(key) ? " fac-numeric tuuli-cell-numeric" : "";
  const compact = ["estado", "impresso", "liquidado", "moeda"].includes(key) ? " fac-document-col-compact" : "";
  const status = key === "estado" ? " tuuli-status" : "";
  return `${tone}${numeric}${compact}${status} fac-document-col-${key}`;
}

function documentTableState(documento: DocumentoComercial) {
  if (documento.anulado) return "Anulado";
  return documento.estado === "RASCUNHO" ? "Rasc." : "Emitido";
}

function documentState(documento: DocumentoComercial) {
  if (documento.anulado) return "ANULADO";
  return documento.estado;
}

function datePt(value: string) {
  return value ? value.split("-").reverse().join("/") : "-";
}

function validateDraft(form: DraftForm) {
  if (!form.tipoDocumentoId) return "Seleciona o tipo de documento.";
  if (!form.serie) return "Seleciona a série.";
  if (!form.dataEmissao) return "A data de emissão é obrigatória.";
  if (!form.clienteId) return "Seleciona o cliente.";
  if (!form.armazemCargaId) return "Seleciona o armazém de carga.";
  return null;
}

function validateLine(form: LineForm) {
  if (!form.artigoId) return "Seleciona o artigo.";
  if (!form.quantidade || Number(form.quantidade) <= 0) return "A quantidade deve ser superior a zero.";
  if (form.precoUnitario === "" || Number(form.precoUnitario) < 0) return "O preço unitário não pode ser negativo.";
  if (form.desconto === "" || Number(form.desconto) < 0) return "O desconto não pode ser negativo.";
  if (form.tipoDesconto === "PERCENTAGEM" && Number(form.desconto) > 100) return "O desconto percentual não pode exceder 100%.";
  return null;
}

function selectArticle(codigo: string, artigos: Artigo[], setForm: React.Dispatch<React.SetStateAction<LineForm>>) {
  const artigo = artigos.find((item) => item.codigo === codigo);
  setForm((current) => ({
    ...current,
    artigoId: codigo,
    descricao: "",
    precoUnitario: artigo ? String(artigo.pvp ?? 0) : "0"
  }));
}

function blankToNull(value: string) {
  const trimmed = value.trim();
  return trimmed || null;
}

function todayIso() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}
