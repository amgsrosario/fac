import { useEffect, useMemo, useState } from "react";

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
type Cliente = { id: number; nome: string; nif: string; inativo: boolean };
type TipoDocumento = { id: string; descricao: string; areaGestao: number };
type Serie = { serie: string; tipoDocumentoId: string; nome: string };
type MPagamento = { id: number; nome: string };
type Utilizador = { codigo: string; nome: string; inativo: boolean };
type DocumentoFinanceiro = {
  id: number;
  clienteId: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento: number;
  dataEmissao: string;
  valorPagamentoLiquido: number;
  moedaId: string;
  mPagamentoId: number;
  emissorId: string;
  anulado: boolean;
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

export default function PendentesView() {
  const [pendentes, setPendentes] = useState<Pendente[]>([]);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [financeiros, setFinanceiros] = useState<DocumentoFinanceiro[]>([]);
  const [tipos, setTipos] = useState<TipoDocumento[]>([]);
  const [series, setSeries] = useState<Serie[]>([]);
  const [modos, setModos] = useState<MPagamento[]>([]);
  const [utilizadores, setUtilizadores] = useState<Utilizador[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [receiptOpen, setReceiptOpen] = useState(false);
  const [form, setForm] = useState<ReceiptForm>(emptyReceiptForm());
  const [allocations, setAllocations] = useState<Allocations>({});

  useEffect(() => { loadTesouraria(); }, []);

  async function loadTesouraria() {
    setLoading(true);
    setMessage(null);
    try {
      const [pendentesPage, clientesPage, financeirosPage] = await Promise.all([
        fetchJson<Page<Pendente>>("/api/pendentes?size=500&sort=id,desc"),
        fetchJson<Page<Cliente>>("/api/clientes?size=500&sort=nome,asc"),
        fetchJson<Page<DocumentoFinanceiro>>("/api/documentos-financeiros?size=300&sort=id,desc")
      ]);
      setPendentes(pendentesPage.content);
      setClientes(clientesPage.content);
      setFinanceiros(financeirosPage.content);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel carregar a tesouraria.");
    } finally {
      setLoading(false);
    }
  }

  async function openReceipt() {
    setLoading(true);
    setMessage(null);
    setNotice(null);
    try {
      const [tiposPage, seriesPage, modosPage, utilizadoresPage] = await Promise.all([
        fetchJson<Page<TipoDocumento>>("/api/tipos-documento?size=100&sort=descricao,asc"),
        fetchJson<Page<Serie>>("/api/series?size=200&sort=serie,asc"),
        fetchJson<Page<MPagamento>>("/api/mpagamentos?size=100&sort=nome,asc"),
        fetchJson<Page<Utilizador>>("/api/utilizadores?size=100&sort=nome,asc")
      ]);
      const financeirosTipos = tiposPage.content.filter((tipo) => tipo.areaGestao === 3);
      const seriesFinanceiras = seriesPage.content.filter((serie) => financeirosTipos.some((tipo) => tipo.id === serie.tipoDocumentoId));
      const ativos = utilizadoresPage.content.filter((utilizador) => !utilizador.inativo);
      const tipoInicial = financeirosTipos[0]?.id ?? "";
      setTipos(financeirosTipos);
      setSeries(seriesFinanceiras);
      setModos(modosPage.content);
      setUtilizadores(ativos);
      setForm({
        ...emptyReceiptForm(),
        tipoDocumentoId: tipoInicial,
        serie: seriesFinanceiras.find((serie) => serie.tipoDocumentoId === tipoInicial)?.serie ?? "",
        emissorId: ativos.length === 1 ? ativos[0].codigo : ""
      });
      setAllocations({});
      setReceiptOpen(true);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel preparar o recebimento.");
    } finally {
      setLoading(false);
    }
  }

  function selectClient(clienteId: string) {
    const moedas = openPendentesForClient(pendentes, Number(clienteId)).map((item) => item.moedaId);
    const moedasUnicas = [...new Set(moedas)];
    setForm((current) => ({ ...current, clienteId, moedaId: moedasUnicas.length === 1 ? moedasUnicas[0] : "", valorRecebido: "" }));
    setAllocations({});
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
    setMessage(remaining > 0 ? `Ficam ${money(remaining)} ${form.moedaId} por distribuir porque o valor recebido excede os pendentes disponíveis.` : null);
  }

  function changeAllocation(pendente: Pendente, value: string) {
    if (value === "") {
      setAllocations((current) => ({ ...current, [pendente.id]: "" }));
      return;
    }
    const amount = Math.max(0, Math.min(Number(value), Number(pendente.valorPendente)));
    setAllocations((current) => ({ ...current, [pendente.id]: String(round6(amount)) }));
  }

  async function issueReceipt() {
    const validation = validateReceipt(form, receiptPendentes, allocations);
    if (validation) {
      setMessage(validation);
      return;
    }
    const cliente = clientes.find((item) => item.id === Number(form.clienteId));
    if (!window.confirm(`Emitir recebimento de ${money(receiptTarget)} ${form.moedaId} para ${cliente?.nome ?? form.clienteId}, distribuido por ${allocatedLines.length} pendentes?`)) return;
    setLoading(true);
    setMessage(null);
    try {
      const created = await sendJson<DocumentoFinanceiro>("/api/documentos-financeiros", {
        tipoDocumentoId: form.tipoDocumentoId,
        serie: form.serie,
        dataEmissao: form.dataEmissao,
        clienteId: Number(form.clienteId),
        moedaId: form.moedaId,
        mPagamentoId: Number(form.mPagamentoId),
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
      setReceiptOpen(false);
      setNotice(`${created.tipoDocumentoId} ${created.serie}/${created.numeroDocumento} emitido por ${money(created.valorPagamentoLiquido)} ${created.moedaId}. Pendentes atualizados.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel emitir o recebimento.");
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
        setMessage(diagnostico.bloqueios.join(" ") || "Este documento financeiro nao pode ser anulado.");
        return;
      }
      if (!window.confirm(`Anular ${diagnostico.referencia}? Os valores recebidos serao repostos nos respetivos pendentes.`)) return;
      await sendJson<DocumentoFinanceiro>(`/api/documentos-financeiros/${documento.id}/anular`, null);
      await loadTesouraria();
      setNotice(`${diagnostico.referencia} anulado. Os pendentes foram repostos.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel anular o documento financeiro.");
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

  return <>
    {notice && <p className="fac-editor-message">{notice}</p>}
    {message && <p className="fac-message">{message}</p>}

    <section className="fac-hero">
      <div><p className="fac-eyebrow">Tesouraria</p><h2>Recebimentos por cliente</h2><p>Cria um recibo e distribui o valor recebido por um ou vários pendentes, incluindo liquidações parciais.</p></div>
      <div className="fac-hero-card"><span>Saldo em aberto</span><strong>{money(sum(abertas.map((item) => item.valorPendente)))} EUR</strong><small>{abertas.length} pendentes ativos</small></div>
    </section>

    <section className="fac-list-toolbar">
      <input onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar pendente, cliente ou estado" type="search" value={search}/>
      <div className="fac-inline-actions"><button className="fac-soft-button" disabled={loading} onClick={loadTesouraria} type="button">Atualizar</button><button className="fac-primary-button" disabled={loading || clientesComPendentes.length === 0} onClick={openReceipt} type="button">Novo recebimento</button></div>
    </section>

    {receiptOpen && <section className="fac-panel fac-section-panel fac-emission-panel">
      <div className="fac-panel-header"><div><p className="fac-eyebrow">Novo documento financeiro</p><h2>Distribuir recebimento</h2></div><button className="fac-ghost-button" onClick={() => setReceiptOpen(false)} type="button">Cancelar</button></div>
      <div className="fac-form-grid">
        <Field label="Cliente"><select onChange={(event) => selectClient(event.target.value)} value={form.clienteId}><option value="">Selecionar cliente</option>{clientesComPendentes.map((cliente) => <option key={cliente.id} value={cliente.id}>{cliente.nome} - {cliente.nif}</option>)}</select></Field>
        <Field label="Moeda"><select disabled={!form.clienteId || clientCurrencies.length <= 1} onChange={(event) => { setForm((current) => ({ ...current, moedaId: event.target.value, valorRecebido: "" })); setAllocations({}); }} value={form.moedaId}><option value="">Selecionar moeda</option>{clientCurrencies.map((moeda) => <option key={moeda} value={moeda}>{moeda}</option>)}</select></Field>
        <Field label="Valor recebido"><input disabled={!form.moedaId} min="0.000001" onChange={(event) => { setForm((current) => ({ ...current, valorRecebido: event.target.value })); setAllocations({}); }} step="0.000001" type="number" value={form.valorRecebido}/></Field>
        <Field label="Modo de pagamento"><select onChange={(event) => setForm((current) => ({ ...current, mPagamentoId: event.target.value }))} value={form.mPagamentoId}><option value="">Confirmar modo</option>{modos.map((modo) => <option key={modo.id} value={modo.id}>{modo.nome}</option>)}</select></Field>
        <Field label="Tipo de documento"><select onChange={(event) => { const tipoDocumentoId = event.target.value; setForm((current) => ({ ...current, tipoDocumentoId, serie: series.find((item) => item.tipoDocumentoId === tipoDocumentoId)?.serie ?? "" })); }} value={form.tipoDocumentoId}><option value="">Selecionar</option>{tipos.map((tipo) => <option key={tipo.id} value={tipo.id}>{tipo.id} - {tipo.descricao}</option>)}</select></Field>
        <Field label="Serie"><select onChange={(event) => setForm((current) => ({ ...current, serie: event.target.value }))} value={form.serie}><option value="">Selecionar</option>{availableSeries.map((serie) => <option key={`${serie.tipoDocumentoId}-${serie.serie}`} value={serie.serie}>{serie.serie} - {serie.nome}</option>)}</select></Field>
        <Field label="Data de emissao"><input onChange={(event) => setForm((current) => ({ ...current, dataEmissao: event.target.value }))} type="date" value={form.dataEmissao}/></Field>
        <Field label="Emissor"><select onChange={(event) => setForm((current) => ({ ...current, emissorId: event.target.value }))} value={form.emissorId}><option value="">Selecionar</option>{utilizadores.map((utilizador) => <option key={utilizador.codigo} value={utilizador.codigo}>{utilizador.nome} ({utilizador.codigo})</option>)}</select></Field>
      </div>

      <div className="fac-receipt-totals"><div><span>Valor recebido</span><strong>{money(receiptTarget)} {form.moedaId}</strong></div><div><span>Distribuido</span><strong>{money(allocatedTotal)} {form.moedaId}</strong></div><div className={difference === 0 && receiptTarget > 0 ? "balanced" : "unbalanced"}><span>Diferenca</span><strong>{money(difference)} {form.moedaId}</strong></div></div>
      <div className="fac-inline-actions"><button className="fac-soft-button" disabled={!form.valorRecebido || !form.moedaId} onClick={distributeReceipt} type="button">Distribuir por antiguidade</button><button className="fac-ghost-button" disabled={allocatedTotal === 0} onClick={() => setAllocations({})} type="button">Limpar distribuicao</button></div>

      <table className="fac-table fac-allocation-table"><thead><tr><th>Documento</th><th>Emissao</th><th>Vencimento</th><th>Valor original</th><th>Pendente antes</th><th>Valor a liquidar</th><th>Novo pendente</th></tr></thead><tbody>
        {receiptPendentes.map((pendente) => { const amount = round6(Number(allocations[pendente.id] || 0)); return <tr key={pendente.id}><td>{referencia(pendente)}</td><td>{datePt(pendente.dataDocumento)}</td><td>{datePt(pendente.dataVencimento)}</td><td>{money(pendente.valorDocumento)} {pendente.moedaId}</td><td>{money(pendente.valorPendente)} {pendente.moedaId}</td><td><input className="fac-table-input" max={pendente.valorPendente} min="0" onChange={(event) => changeAllocation(pendente, event.target.value)} step="0.000001" type="number" value={allocations[pendente.id] ?? ""}/></td><td>{money(round6(pendente.valorPendente - amount))} {pendente.moedaId}</td></tr>; })}
        {form.clienteId && form.moedaId && receiptPendentes.length === 0 && <tr><td colSpan={7}>Este cliente nao tem pendentes em aberto nesta moeda.</td></tr>}
      </tbody></table>

      <div className="fac-form-grid"><Field label="Observacoes"><textarea maxLength={250} onChange={(event) => setForm((current) => ({ ...current, observacoes: event.target.value }))} value={form.observacoes}/></Field></div>
      <div className="fac-form-footer"><span className="fac-muted">O recibo só pode ser emitido quando o valor recebido estiver totalmente distribuido pelos pendentes.</span><button className="fac-gold-button" disabled={loading || receiptTarget <= 0 || difference !== 0 || allocatedLines.length === 0} onClick={issueReceipt} type="button">Emitir recebimento</button></div>
    </section>}

    <section className="fac-panel fac-section-panel"><div className="fac-panel-header"><div><p className="fac-eyebrow">Pendentes</p><h2>Conta corrente em aberto e liquidada</h2></div><span className="fac-muted">{filteredPendentes.length} registos</span></div><table className="fac-table"><thead><tr><th>Documento</th><th>Cliente</th><th>Estado</th><th>Vencimento</th><th>Original</th><th>Pendente</th></tr></thead><tbody>{filteredPendentes.map((item) => <tr key={item.id}><td>{referencia(item)}</td><td>{item.clienteId}</td><td><span className="fac-status">{estado(item)}</span></td><td>{datePt(item.dataVencimento)}</td><td>{money(item.valorDocumento)} {item.moedaId}</td><td>{money(item.valorPendente)} {item.moedaId}</td></tr>)}{!loading && filteredPendentes.length === 0 && <tr><td colSpan={6}>Sem pendentes para mostrar.</td></tr>}</tbody></table></section>

    <section className="fac-panel fac-section-panel"><div className="fac-panel-header"><div><p className="fac-eyebrow">Documentos financeiros</p><h2>Recebimentos emitidos</h2></div><span className="fac-muted">{financeiros.length} documentos</span></div><table className="fac-table"><thead><tr><th>Documento</th><th>Cliente</th><th>Data</th><th>Modo</th><th>Liquido</th><th>Estado</th><th>Acoes</th></tr></thead><tbody>{financeiros.map((documento) => <tr key={documento.id}><td>{documento.tipoDocumentoId} {documento.serie}/{documento.numeroDocumento}</td><td>{documento.clienteId}</td><td>{datePt(documento.dataEmissao)}</td><td>{documento.mPagamentoId}</td><td>{money(documento.valorPagamentoLiquido)} {documento.moedaId}</td><td><span className={`fac-status ${documento.anulado ? "danger" : ""}`}>{documento.anulado ? "ANULADO" : "EMITIDO"}</span></td><td><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => window.open(`/api/documentos-financeiros/${documento.id}/diagnostico/html`, "_blank", "noopener,noreferrer")} type="button">Diagnostico</button>{!documento.anulado && <button className="fac-link-danger" disabled={loading} onClick={() => annulFinancial(documento)} type="button">Anular</button>}</div></td></tr>)}{!loading && financeiros.length === 0 && <tr><td colSpan={7}>Sem documentos financeiros para mostrar.</td></tr>}</tbody></table></section>
  </>;
}

function emptyReceiptForm(): ReceiptForm { return { clienteId: "", moedaId: "", tipoDocumentoId: "", serie: "", dataEmissao: todayIso(), valorRecebido: "", mPagamentoId: "", emissorId: "", observacoes: "" }; }
function openPendentesForClient(pendentes: Pendente[], clienteId: number) { return clienteId ? pendentes.filter((item) => item.clienteId === clienteId && Number(item.valorPendente) > 0) : []; }
function validateReceipt(form: ReceiptForm, pendentes: Pendente[], allocations: Allocations) { if (!form.clienteId) return "Seleciona o cliente."; if (!form.moedaId) return "Seleciona a moeda."; if (!form.tipoDocumentoId) return "Seleciona o tipo de documento financeiro."; if (!form.serie) return "Seleciona a serie."; if (!form.dataEmissao) return "A data de emissao e obrigatoria."; if (!form.mPagamentoId) return "Confirma o modo de pagamento."; if (!form.emissorId) return "Seleciona o emissor."; const target = round6(Number(form.valorRecebido)); if (!Number.isFinite(target) || target <= 0) return "O valor recebido deve ser positivo."; const total = round6(sum(pendentes.map((item) => Number(allocations[item.id] || 0)))); if (total <= 0) return "Distribui o recebimento por pelo menos um pendente."; if (round6(target - total) !== 0) return "O valor recebido e a distribuicao pelos pendentes nao coincidem."; return null; }
function estado(item: Pendente) { if (Number(item.valorPendente) <= 0) return "LIQUIDADO"; if (item.dataVencimento < todayIso()) return "VENCIDO"; if (Number(item.valorPendente) < Number(item.valorDocumento)) return "PARCIAL"; return "ABERTO"; }
function referencia(item: Pendente) { return `${item.tipoDocumentoId} ${item.serieDocumento}/${item.numeroDocumento}`; }
function datePt(value: string) { return value ? value.split("-").reverse().join("/") : "-"; }
function money(value: number) { return Number(value || 0).toLocaleString("pt-PT", { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
function sum(values: number[]) { return values.reduce((total, value) => total + Number(value || 0), 0); }
function round6(value: number) { return Math.round((value + Number.EPSILON) * 1_000_000) / 1_000_000; }
function todayIso() { const now = new Date(); const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000); return local.toISOString().slice(0, 10); }
function blankToNull(value: string) { return value.trim() || null; }
function Field({ children, label }: { children: React.ReactNode; label: string }) { return <label className="fac-field"><span>{label}</span>{children}</label>; }
async function fetchJson<T>(url: string): Promise<T> { const response = await fetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function sendJson<T>(url: string, body: unknown): Promise<T> { const response = await fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: body == null ? undefined : JSON.stringify(body) }); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
