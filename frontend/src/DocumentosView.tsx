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

const emptyDraftForm: DraftForm = {
  tipoDocumentoId: "",
  serie: "",
  dataEmissao: todayIso(),
  clienteId: "",
  armazemCargaId: "",
  observacoes: ""
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

  async function openDraftEditor() {
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const [clientesPage, tiposPage, seriesPage, armazensPage, parametros] = await Promise.all([
        fetchJson<Page<Cliente>>("/api/clientes?size=300&sort=nome,asc"),
        fetchJson<Page<TipoDocumento>>("/api/tipos-documento?size=100&sort=descricao,asc"),
        fetchJson<Page<Serie>>("/api/series?size=200&sort=serie,asc"),
        fetchJson<Page<Armazem>>("/api/armazens?size=100&sort=nome,asc"),
        fetchOptionalJson<ParametrosDocumento>("/api/parametros-documento-comercial")
      ]);
      const comerciais = tiposPage.content.filter((tipo) => tipo.areaGestao === 1 || tipo.areaGestao === 2);
      setClientes(clientesPage.content);
      setTiposDocumento(comerciais);
      setSeries(seriesPage.content.filter((serie) => comerciais.some((tipo) => tipo.id === serie.tipoDocumentoId)));
      setArmazens(armazensPage.content);
      setDraftForm({
        ...emptyDraftForm,
        tipoDocumentoId: parametros?.tipoDocumentoId ?? "",
        serie: parametros?.serie ?? "",
        armazemCargaId: parametros?.armazemCargaId != null ? String(parametros.armazemCargaId) : ""
      });
      setEditorOpen(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel preparar o novo rascunho.");
    } finally {
      setLoading(false);
    }
  }

  async function createDraft() {
    const validation = validateDraft(draftForm);
    if (validation) {
      setMessage(validation);
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const created = await requestJson<DocumentoComercial>("/api/documentos-comerciais", "POST", {
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
      });
      const page = await fetchJson<Page<DocumentoComercial>>("/api/documentos-comerciais?size=200&sort=id,desc");
      setDocumentos(page.content);
      setSelectedId(created.id);
      setEditorOpen(false);
      setNotice(`Rascunho ${created.tipoDocumentoId} criado com o identificador interno ${created.id}.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel criar o rascunho.");
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

  function changeDraft<K extends keyof DraftForm>(field: K, value: DraftForm[K]) {
    setDraftForm((current) => ({ ...current, [field]: value }));
  }

  if (editorOpen) {
    return (
      <section className="fac-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Documento comercial</p><h2>Novo rascunho</h2></div>
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
            <select onChange={(event) => changeDraft("clienteId", event.target.value)} value={draftForm.clienteId}>
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

        <div className="fac-form-footer">
          <span className="fac-muted">O rascunho ainda nao recebe numero definitivo nem altera o numerador da serie.</span>
          <button className="fac-primary-button" disabled={loading} onClick={createDraft} type="button">{loading ? "A criar..." : "Criar rascunho"}</button>
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
          <button className="fac-primary-button" disabled={loading} onClick={openDraftEditor} type="button">Novo rascunho</button>
        </div>
      </section>

      <section className="fac-content-grid">
        <article className="fac-panel fac-panel-main">
          <table className="fac-table">
            <thead><tr><th>Documento</th><th>Cliente</th><th>Data</th><th>Estado</th><th>Total</th></tr></thead>
            <tbody>
              {filtered.map((documento) => (
                <tr className={documento.id === selectedId ? "fac-row-selected" : ""} key={documento.id} onClick={() => setSelectedId(documento.id)}>
                  <td>{reference(documento)}</td>
                  <td>{documento.clienteNome}</td>
                  <td>{datePt(documento.dataEmissao)}</td>
                  <td><span className={`fac-status ${documento.anulado ? "danger" : ""}`}>{documentState(documento)}</span></td>
                  <td>{money(documento.valorTotal)} {documento.moedaId}</td>
                </tr>
              ))}
              {!loading && filtered.length === 0 && <tr><td colSpan={5}>Sem documentos para mostrar.</td></tr>}
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
        </aside>
      </section>

      <section className="fac-panel fac-section-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Linhas</p><h2>{selected ? reference(selected) : "Sem documento"}</h2></div>
          <span className="fac-muted">{linesLoading ? "A carregar..." : `${linhas.length} linhas`}</span>
        </div>
        <table className="fac-table">
          <thead><tr><th>Linha</th><th>Artigo</th><th>Descricao</th><th>Quantidade</th><th>Preco</th><th>IVA</th><th>Valor</th></tr></thead>
          <tbody>
            {linhas.map((linha) => (
              <tr key={linha.id}>
                <td>{linha.numeroLinha}</td><td>{linha.artigoId}</td><td>{linha.descricao}</td>
                <td>{decimal(linha.quantidade)}</td><td>{money(linha.precoUnitario)}</td>
                <td>{decimal(linha.percentagemIva)}%</td><td>{money(linha.valorLinha)}</td>
              </tr>
            ))}
            {!linesLoading && linhas.length === 0 && <tr><td colSpan={7}>Documento sem linhas.</td></tr>}
          </tbody>
        </table>
      </section>
    </>
  );
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function fetchOptionalJson<T>(url: string): Promise<T | null> {
  const response = await fetch(url);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function requestJson<T>(url: string, method: "POST" | "PUT", body: unknown): Promise<T> {
  const response = await fetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
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
