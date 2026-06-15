import { useEffect, useMemo, useRef, useState } from "react";
import { apiFetch, getAuthSession } from "./api";
import { ColumnSelector, ConfigurableColumn, useConfiguredColumns } from "./ColumnSelector";

type Page<T> = {
  content: T[];
  totalElements: number;
};

const DOCUMENTO_COLUMNS: ConfigurableColumn[] = [
  { key: "documento", label: "Documento", visible: true },
  { key: "cliente", label: "Cliente", visible: true },
  { key: "nif", label: "NIF", visible: false },
  { key: "emissao", label: "Emissao", visible: true },
  { key: "vencimento", label: "Vencimento", visible: false },
  { key: "moeda", label: "Moeda", visible: false },
  { key: "bruto", label: "Bruto", visible: false },
  { key: "desconto", label: "Desconto", visible: false },
  { key: "iva", label: "IVA", visible: false },
  { key: "total", label: "Total", visible: true },
  { key: "estado", label: "Estado", visible: true },
  { key: "impresso", label: "Impresso", visible: false },
  { key: "liquidado", label: "Liquidado", visible: false }
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
  id: number;
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
  armazemCargaId?: number;
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
  const [documentos, setDocumentos] = useState<DocumentoComercial[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [linhas, setLinhas] = useState<LinhaDocumento[]>([]);
  const [search, setSearch] = useState("");
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
  const [columnEditorOpen, setColumnEditorOpen] = useState(false);
  const documentoColumns = useConfiguredColumns("fac.documentos.colunas", DOCUMENTO_COLUMNS);
  const newDocumentClientRef = useRef<HTMLSelectElement>(null);
  const lineArticleRef = useRef<HTMLSelectElement>(null);

  useEffect(() => {
    loadDocumentos();
  }, []);

  useEffect(() => {
    if (selectedId == null) {
      setLinhas([]);
      return;
    }
    loadLinhas(selectedId);
  }, [selectedId]);

  useEffect(() => {
    if (editorOpen) window.setTimeout(() => newDocumentClientRef.current?.focus(), 0);
    else if (lineEditorOpen) window.setTimeout(() => lineArticleRef.current?.focus(), 0);
  }, [editorOpen, lineEditorOpen]);

  async function loadDocumentos() {
    setLoading(true);
    setMessage(null);
    try {
      const page = await fetchJson<Page<DocumentoComercial>>("/api/documentos-comerciais?size=200&sort=id,desc");
      setDocumentos(page.content);
      setSelectedId((current) => current ?? page.content[0]?.id ?? null);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar documentos.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar as linhas.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar os artigos.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel adicionar a linha.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel remover a linha.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel preparar a emissao.");
    } finally {
      setLoading(false);
    }
  }

  async function emitDocument() {
    if (!selected || !diagnostico?.podeEmitir) return;
    if (!window.confirm(`Emitir definitivamente ${diagnostico.referencia}? Depois de emitido, o documento fica imutavel.`)) return;
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
        : `Documento ${reference(emitted)} emitido sem pendente por ser de liquidacao imediata.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel emitir o documento.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel gerar o PDF.");
    } finally {
      setLoading(false);
    }
  }

  async function annulDocument() {
    if (!selected || selected.estado !== "EMITIDO" || selected.anulado) return;
    if (!window.confirm(`Anular ${reference(selected)}? Esta operacao so e permitida quando nao existem recebimentos ativos.`)) return;
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const annulled = await requestJson<DocumentoComercial>(`/api/documentos-comerciais/${selected.id}/anular`, "POST", null);
      setDocumentos((current) => current.map((item) => item.id === annulled.id ? annulled : item));
      setNotice(`${reference(annulled)} anulado.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel anular o documento.");
    } finally {
      setLoading(false);
    }
  }

  async function openDraftEditor() {
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const [clientesPage, tiposPage, seriesPage, armazensPage, artigosPage, parametros] = await Promise.all([
        fetchJson<Page<Cliente>>("/api/clientes?size=300&sort=nome,asc"),
        fetchJson<Page<TipoDocumento>>("/api/tipos-documento?size=100&sort=descricao,asc"),
        fetchJson<Page<Serie>>("/api/series?size=200&sort=serie,asc"),
        fetchJson<Page<Armazem>>("/api/armazens?size=100&sort=nome,asc"),
        fetchJson<Page<Artigo>>("/api/artigos?size=500&sort=descricao,asc"),
        fetchOptionalJson<ParametrosDocumento>("/api/parametros-documento-comercial")
      ]);
      const comerciais = tiposPage.content.filter((tipo) => tipo.areaGestao === 1 || tipo.areaGestao === 2);
      setClientes(clientesPage.content);
      setTiposDocumento(comerciais);
      setSeries(seriesPage.content.filter((serie) => comerciais.some((tipo) => tipo.id === serie.tipoDocumentoId)));
      setArmazens(armazensPage.content);
      setArtigos(artigosPage.content.filter((artigo) => !artigo.inativo));
      setLineForm(emptyLineForm);
      setDraftForm({
        ...emptyDraftForm,
        tipoDocumentoId: parametros?.tipoDocumentoId ?? "",
        serie: parametros?.serie ?? "",
        armazemCargaId: parametros?.armazemCargaId != null ? String(parametros.armazemCargaId) : ""
      });
      setEditorOpen(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel preparar o novo documento.");
    } finally {
      setLoading(false);
    }
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
          armazemCargaId: Number(draftForm.armazemCargaId),
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel criar o documento com a primeira linha.");
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

  const selected = documentos.find((documento) => documento.id === selectedId) ?? null;
  const emitted = documentos.filter((documento) => documento.estado === "EMITIDO" && !documento.anulado).length;
  const drafts = documentos.filter((documento) => documento.estado === "RASCUNHO").length;
  const annulled = documentos.filter((documento) => documento.anulado).length;
  const availableSeries = series.filter((serie) => serie.tipoDocumentoId === draftForm.tipoDocumentoId);
  const selectedIsDraft = selected?.estado === "RASCUNHO";

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
          <Field label="Serie">
            <select disabled={!draftForm.tipoDocumentoId} onChange={(event) => changeDraft("serie", event.target.value)} value={draftForm.serie}>
              <option value="">Selecionar</option>
              {availableSeries.map((serie) => <option key={`${serie.tipoDocumentoId}-${serie.serie}`} value={serie.serie}>{serie.serie} - {serie.nome}</option>)}
            </select>
          </Field>
          <Field label="Data de emissao"><input onChange={(event) => changeDraft("dataEmissao", event.target.value)} type="date" value={draftForm.dataEmissao} /></Field>
          <Field label="Cliente">
            <select ref={newDocumentClientRef} onChange={(event) => changeDraft("clienteId", event.target.value)} value={draftForm.clienteId}>
              <option value="">Selecionar</option>
              {clientes.map((cliente) => <option key={cliente.id} value={cliente.id}>{cliente.nome} - {cliente.nif}</option>)}
            </select>
          </Field>
          <Field label="Armazem de carga">
            <select onChange={(event) => changeDraft("armazemCargaId", event.target.value)} value={draftForm.armazemCargaId}>
              <option value="">Selecionar</option>
              {armazens.map((armazem) => <option key={armazem.id} value={armazem.id}>{armazem.nome}</option>)}
            </select>
          </Field>
          <Field label="Observacoes"><textarea maxLength={250} onChange={(event) => changeDraft("observacoes", event.target.value)} value={draftForm.observacoes} /></Field>
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
          <Field label="Descricao"><input maxLength={80} onChange={(event) => setLineForm((current) => ({ ...current, descricao: event.target.value }))} placeholder="Usa a descricao do artigo" value={lineForm.descricao} /></Field>
          <Field label="Quantidade"><input min="0.000001" onChange={(event) => setLineForm((current) => ({ ...current, quantidade: event.target.value }))} step="0.000001" type="number" value={lineForm.quantidade} /></Field>
          <Field label="Preco unitario"><input min="0" onChange={(event) => setLineForm((current) => ({ ...current, precoUnitario: event.target.value }))} step="0.000001" type="number" value={lineForm.precoUnitario} /></Field>
          <Field label="Tipo de desconto">
            <select onChange={(event) => setLineForm((current) => ({ ...current, tipoDesconto: event.target.value as LineForm["tipoDesconto"] }))} value={lineForm.tipoDesconto}>
              <option value="PERCENTAGEM">Percentagem</option><option value="VALOR">Valor</option>
            </select>
          </Field>
          <Field label={lineForm.tipoDesconto === "PERCENTAGEM" ? "Desconto (%)" : "Desconto (valor)"}><input min="0" onChange={(event) => setLineForm((current) => ({ ...current, desconto: event.target.value }))} step="0.000001" type="number" value={lineForm.desconto} /></Field>
        </div>

        <div className="fac-form-footer">
          <span className="fac-muted">O documento fica em rascunho, sem numero definitivo, mas nunca sem linhas.</span>
          <button className="fac-primary-button" disabled={loading} onClick={createDraft} type="button">{loading ? "A criar..." : "Criar documento e guardar primeira linha"}</button>
        </div>
      </section>
    );
  }

  return (
    <>
      {notice && <p className="fac-editor-message">{notice}</p>}
      {message && <p className="fac-message">{message}</p>}

      <section className="fac-hero">
        <div>
          <p className="fac-eyebrow">Documentos comerciais</p>
          <h2>Consulta operacional e conferencia por documento</h2>
          <p>Abre o documento, confere as linhas e acede aos diagnosticos sem executar ainda acoes fiscais.</p>
        </div>
        <div className="fac-hero-card">
          <span>Documentos carregados</span>
          <strong>{loading ? "A carregar..." : documentos.length}</strong>
          <small>{emitted} emitidos, {drafts} rascunhos, {annulled} anulados</small>
        </div>
      </section>

      <section className="fac-metrics" aria-label="Indicadores de documentos">
        <article className="fac-metric document"><span>Emitidos ativos</span><strong>{emitted}</strong></article>
        <article className="fac-metric product"><span>Rascunhos</span><strong>{drafts}</strong></article>
        <article className="fac-metric treasury"><span>Anulados</span><strong>{annulled}</strong></article>
        <article className="fac-metric client"><span>Total carregado</span><strong>{documentos.length}</strong></article>
      </section>

      <section className="fac-list-toolbar">
        <input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar documento, cliente, NIF ou estado" type="search" value={search} />
        <div className="fac-inline-actions">
          <button className="fac-soft-button" disabled={loading} onClick={loadDocumentos} type="button">Atualizar</button>
          <div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => setColumnEditorOpen((current) => !current)} type="button">Colunas ({documentoColumns.visibleColumns.length})</button><button className="fac-primary-button" disabled={loading} onClick={openDraftEditor} type="button">Novo documento</button></div>
        </div>
      </section>

      <section className="fac-content-grid">
        <article className="fac-panel fac-panel-main">
          <ColumnSelector columns={documentoColumns.columns} open={columnEditorOpen} onMove={documentoColumns.moveColumn} onReset={documentoColumns.resetColumns} onToggle={documentoColumns.toggleColumn} />
          <table className="fac-table">
            <thead><tr>{documentoColumns.visibleColumns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead>
            <tbody>
              {filtered.map((documento) => (
                <tr className={documento.id === selectedId ? "fac-row-selected" : ""} key={documento.id} onClick={() => setSelectedId(documento.id)}>
                  {documentoColumns.visibleColumns.map((column) => <td key={column.key}>{documentoColumnValue(documento, column.key)}</td>)}
                </tr>
              ))}
              {!loading && filtered.length === 0 && <tr><td colSpan={documentoColumns.visibleColumns.length}>Sem documentos para mostrar.</td></tr>}
            </tbody>
          </table>
        </article>

        <aside className="fac-panel fac-detail">
          <p className="fac-eyebrow">Documento selecionado</p>
          <h2>{selected ? reference(selected) : "Sem documento"}</h2>
          <dl>
            <div><dt>Cliente</dt><dd>{selected?.clienteNome ?? "-"}</dd></div>
            <div><dt>NIF</dt><dd>{selected?.clienteNif ?? "-"}</dd></div>
            <div><dt>Estado</dt><dd>{selected ? documentState(selected) : "-"}</dd></div>
            <div><dt>Emissao</dt><dd>{selected ? datePt(selected.dataEmissao) : "-"}</dd></div>
            <div><dt>Vencimento</dt><dd>{selected?.dataVencimento ? datePt(selected.dataVencimento) : "-"}</dd></div>
            <div><dt>Total</dt><dd>{selected ? `${money(selected.valorTotal)} ${selected.moedaId}` : "-"}</dd></div>
            <div><dt>Liquidado</dt><dd>{selected?.liquidado ? "Sim" : "Nao"}</dd></div>
          </dl>
          <button className="fac-primary-button" disabled={!selected} onClick={() => selected && openHtml(selected.id)} type="button">Diagnostico HTML</button>
          <button className="fac-ghost-button" disabled={!selected} onClick={() => selected && openJson(selected.id)} type="button">Diagnostico JSON</button>
          {selected?.estado === "EMITIDO" && <button className="fac-gold-button" disabled={loading} onClick={() => openPdf(selected.id)} type="button">Abrir PDF</button>}
          {selected?.estado === "EMITIDO" && !selected.anulado && <button className="fac-link-danger" disabled={loading} onClick={annulDocument} type="button">Anular fatura</button>}
          {selectedIsDraft && <button className="fac-gold-button" disabled={loading} onClick={openEmission} type="button">Conferir e emitir</button>}
        </aside>
      </section>

      {emissionOpen && selectedIsDraft && diagnostico && (
        <section className="fac-panel fac-section-panel fac-emission-panel">
          <div className="fac-panel-header">
            <div><p className="fac-eyebrow">Emissao definitiva</p><h2>{diagnostico.referencia}</h2></div>
            <button className="fac-ghost-button" onClick={() => setEmissionOpen(false)} type="button">Fechar conferencia</button>
          </div>

          <div className="fac-emission-summary">
            <div><span>Total do cabecalho</span><strong>{money(diagnostico.totais.cabecalhoValorTotal)} {selected.moedaId}</strong></div>
            <div><span>Total calculado pelas linhas</span><strong>{money(diagnostico.totais.linhasValorTotal)} {selected.moedaId}</strong></div>
            <div><span>Coerencia dos totais</span><strong>{diagnostico.totais.coerente ? "Confirmada" : "Com diferencas"}</strong></div>
          </div>

          {diagnostico.bloqueios.length > 0 && <div className="fac-check-list danger"><strong>Emissao bloqueada</strong>{diagnostico.bloqueios.map((item) => <p key={item}>{item}</p>)}</div>}
          {diagnostico.alertas.length > 0 && <div className="fac-check-list warning"><strong>Alertas</strong>{diagnostico.alertas.map((item) => <p key={item}>{item}</p>)}</div>}
          {diagnostico.podeEmitir && diagnostico.alertas.length === 0 && <p className="fac-check-ok">O backend confirmou que o documento esta coerente e pode ser emitido.</p>}

          <div className="fac-form-footer">
            <span className="fac-muted">Emissor: {getAuthSession()?.nome}</span>
            <button className="fac-gold-button" disabled={loading || !diagnostico.podeEmitir} onClick={emitDocument} type="button">Emitir documento</button>
          </div>
          <p className="fac-muted">A emissao atribui o numero definitivo, avanca o numerador da serie e torna o documento imutavel.</p>
        </section>
      )}

      <section className="fac-panel fac-section-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Linhas</p><h2>{selected ? reference(selected) : "Sem documento"}</h2></div>
          <div className="fac-inline-actions">
            <span className="fac-muted">{linesLoading ? "A carregar..." : `${linhas.length} linhas`}</span>
            {selectedIsDraft && <button className="fac-primary-button" disabled={linesLoading} onClick={openLineEditor} type="button">Adicionar linha</button>}
          </div>
        </div>

        {lineEditorOpen && selectedIsDraft && (
          <div className="fac-line-editor">
            <div className="fac-form-grid">
              <Field label="Artigo">
                <select ref={lineArticleRef} onChange={(event) => selectArticle(event.target.value, artigos, setLineForm)} value={lineForm.artigoId}>
                  <option value="">Selecionar</option>
                  {artigos.map((artigo) => <option key={artigo.codigo} value={artigo.codigo}>{artigo.codigo} - {artigo.descricao}</option>)}
                </select>
              </Field>
              <Field label="Descricao"><input maxLength={80} onChange={(event) => setLineForm((current) => ({ ...current, descricao: event.target.value }))} placeholder="Usa a descricao do artigo" value={lineForm.descricao} /></Field>
              <Field label="Quantidade"><input min="0.000001" onChange={(event) => setLineForm((current) => ({ ...current, quantidade: event.target.value }))} step="0.000001" type="number" value={lineForm.quantidade} /></Field>
              <Field label="Preco unitario"><input min="0" onChange={(event) => setLineForm((current) => ({ ...current, precoUnitario: event.target.value }))} step="0.000001" type="number" value={lineForm.precoUnitario} /></Field>
              <Field label="Tipo de desconto">
                <select onChange={(event) => setLineForm((current) => ({ ...current, tipoDesconto: event.target.value as LineForm["tipoDesconto"] }))} value={lineForm.tipoDesconto}>
                  <option value="PERCENTAGEM">Percentagem</option><option value="VALOR">Valor</option>
                </select>
              </Field>
              <Field label={lineForm.tipoDesconto === "PERCENTAGEM" ? "Desconto (%)" : "Desconto (valor)"}><input min="0" onChange={(event) => setLineForm((current) => ({ ...current, desconto: event.target.value }))} step="0.000001" type="number" value={lineForm.desconto} /></Field>
            </div>
            <div className="fac-form-footer">
              <span className="fac-muted">IVA, peso e totais sao calculados pelo backend a partir do artigo e do regime do documento.</span>
              <div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => setLineEditorOpen(false)} type="button">Cancelar</button><button className="fac-primary-button" disabled={linesLoading} onClick={createLine} type="button">Guardar linha</button></div>
            </div>
          </div>
        )}

        <table className="fac-table">
          <thead><tr><th>Linha</th><th>Artigo</th><th>Descricao</th><th>Quantidade</th><th>Preco</th><th>IVA</th><th>Valor</th>{selectedIsDraft && <th>Acoes</th>}</tr></thead>
          <tbody>
            {linhas.map((linha) => (
              <tr key={linha.id}>
                <td>{linha.numeroLinha}</td><td>{linha.artigoId}</td><td>{linha.descricao}</td>
                <td>{decimal(linha.quantidade)}</td><td>{money(linha.precoUnitario)}</td>
                <td>{decimal(linha.percentagemIva)}%</td><td>{money(linha.valorLinha)}</td>
                {selectedIsDraft && <td><button className="fac-link-danger" disabled={linesLoading} onClick={() => deleteLine(linha.id)} type="button">Remover</button></td>}
              </tr>
            ))}
            {!linesLoading && linhas.length === 0 && <tr><td colSpan={selectedIsDraft ? 8 : 7}>Documento sem linhas.</td></tr>}
          </tbody>
        </table>
      </section>
    </>
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
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
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
    case "estado": return <span className={`fac-status ${documento.anulado ? "danger" : ""}`}>{documentState(documento)}</span>;
    case "impresso": return documento.impresso ? "Sim" : "Nao";
    case "liquidado": return documento.liquidado ? "Sim" : "Nao";
    default: return "-";
  }
}

function documentState(documento: DocumentoComercial) {
  if (documento.anulado) return "ANULADO";
  return documento.estado;
}

function datePt(value: string) {
  return value ? value.split("-").reverse().join("/") : "-";
}

function money(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function decimal(value: number) {
  return Number(value || 0).toLocaleString("pt-PT", { maximumFractionDigits: 6 });
}

function validateDraft(form: DraftForm) {
  if (!form.tipoDocumentoId) return "Seleciona o tipo de documento.";
  if (!form.serie) return "Seleciona a serie.";
  if (!form.dataEmissao) return "A data de emissao e obrigatoria.";
  if (!form.clienteId) return "Seleciona o cliente.";
  if (!form.armazemCargaId) return "Seleciona o armazem de carga.";
  return null;
}

function validateLine(form: LineForm) {
  if (!form.artigoId) return "Seleciona o artigo.";
  if (!form.quantidade || Number(form.quantidade) <= 0) return "A quantidade deve ser superior a zero.";
  if (form.precoUnitario === "" || Number(form.precoUnitario) < 0) return "O preco unitario nao pode ser negativo.";
  if (form.desconto === "" || Number(form.desconto) < 0) return "O desconto nao pode ser negativo.";
  if (form.tipoDesconto === "PERCENTAGEM" && Number(form.desconto) > 100) return "O desconto percentual nao pode exceder 100%.";
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
