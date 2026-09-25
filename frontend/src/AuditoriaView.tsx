import { useEffect, useRef, useState } from "react";
import { Paginator } from "primereact/paginator";
import { apiFetch } from "./api";

type Evento = { id: number; dataHora: string; tipoEvento: string; entidadeTipo: string; entidadeId: string;
  utilizadorId?: string; utilizadorNome?: string; utilizadorPerfil?: string; resultado: string;
  referencia?: string; descricao: string };
type Page<T> = { content: T[]; totalElements: number; totalPages: number };
type AuditFilters = { tipo: string; referencia: string; utilizador: string; desde: string; ate: string };

export default function AuditoriaView() {
  const [eventos, setEventos] = useState<Evento[]>([]);
  const [tipo, setTipo] = useState("");
  const [referencia, setReferencia] = useState("");
  const [utilizador, setUtilizador] = useState("");
  const [desde, setDesde] = useState("");
  const [ate, setAte] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [appliedFilters, setAppliedFilters] = useState<AuditFilters>({ tipo: "", referencia: "", utilizador: "", desde: "", ate: "" });
  const requestId = useRef(0);

  async function carregar(targetPage = page, targetPageSize = pageSize, filters = appliedFilters) {
    const currentRequest = ++requestId.current;
    setLoading(true); setErro(null);
    const params = new URLSearchParams({ page: String(targetPage), size: String(targetPageSize) });
    if (filters.tipo) params.set("tipoEvento", filters.tipo);
    if (filters.referencia.trim()) params.set("referencia", filters.referencia.trim());
    if (filters.utilizador.trim()) params.set("utilizadorId", filters.utilizador.trim());
    if (filters.desde) params.set("desde", new Date(`${filters.desde}T00:00:00`).toISOString());
    if (filters.ate) params.set("ate", new Date(`${filters.ate}T23:59:59.999`).toISOString());
    try {
      const response = await apiFetch(`/api/auditoria?${params}`);
      if (!response.ok) throw new Error(response.status === 403 ? "Apenas o administrador pode consultar a auditoria." : "Não foi possível consultar a auditoria.");
      const result = (await response.json()) as Page<Evento>;
      if (currentRequest !== requestId.current) return;
      if (targetPage > 0 && targetPage >= result.totalPages) {
        setPage(Math.max(0, result.totalPages - 1));
        return;
      }
      setEventos(result.content);
      setTotalElements(result.totalElements);
      setTotalPages(result.totalPages);
    } catch (error) {
      if (currentRequest === requestId.current) setErro(error instanceof Error ? error.message : "Erro ao consultar auditoria.");
    } finally {
      if (currentRequest === requestId.current) setLoading(false);
    }
  }

  useEffect(() => { void carregar(); }, [page, pageSize, appliedFilters]);

  function aplicarFiltros() {
    setPage(0);
    setAppliedFilters({ tipo, referencia, utilizador, desde, ate });
  }

  return <section className="fac-audit-v2">
    <header className="fac-audit-header">
      <p className="fac-eyebrow">Auditoria fiscal</p>
      <h2>Operações críticas e tentativas negadas</h2>
      <p>Eventos persistentes, apenas para consulta administrativa.</p>
    </header>

    <div className="fac-audit-filters">
      <label className="fac-field fac-audit-event-filter"><span>Evento</span><select value={tipo} onChange={e => setTipo(e.target.value)}><option value="">Todos</option><option>DOCUMENTO_EMITIDO</option><option>DOCUMENTO_ANULADO</option><option>TENTATIVA_ANULACAO_NEGADA</option><option>LOGIN_SUCESSO</option><option>LOGIN_FALHADO</option></select></label>
      <label className="fac-field"><span>Referência</span><input value={referencia} onChange={e => setReferencia(e.target.value)} /></label>
      <label className="fac-field"><span>Utilizador</span><input value={utilizador} onChange={e => setUtilizador(e.target.value)} /></label>
      <label className="fac-field"><span>Desde</span><input type="date" value={desde} onChange={e => setDesde(e.target.value)} /></label>
      <label className="fac-field"><span>Até</span><input type="date" value={ate} onChange={e => setAte(e.target.value)} /></label>
      <button className="fac-primary-button fac-audit-apply" disabled={loading} onClick={aplicarFiltros}>{loading ? "A consultar..." : "Aplicar filtros"}</button>
    </div>

    {erro && <p className="fac-message">{erro}</p>}

    <div className="fac-audit-results">
      <p aria-live="polite" className="fac-audit-count">{totalElements} {totalElements === 1 ? "evento encontrado" : "eventos encontrados"}</p>
      <div className="fac-table-wrapper">
        <table className="fac-table fac-audit-table">
          <thead><tr><th>Data</th><th>Evento</th><th>Referência</th><th>Utilizador</th><th>Resultado</th><th>Descrição</th></tr></thead>
          <tbody>
            {eventos.map(e => <tr key={e.id}>
              <td className="fac-audit-date">{new Date(e.dataHora).toLocaleString("pt-PT")}</td>
              <td><span className="fac-audit-event">{e.tipoEvento}</span></td>
              <td><span className="fac-audit-reference">{e.referencia ?? `${e.entidadeTipo} ${e.entidadeId}`}</span></td>
              <td><span className="fac-audit-user">{e.utilizadorNome ?? "SISTEMA"}</span><small>{e.utilizadorPerfil ?? "-"}</small></td>
              <td><span className={`fac-status fac-audit-result${e.resultado === "FALHA" ? " danger" : ""}`}>{e.resultado}</span></td>
              <td className="fac-audit-description">{e.descricao}</td>
            </tr>)}
            {!loading && eventos.length === 0 && <tr><td colSpan={6}>Sem eventos para os filtros selecionados.</td></tr>}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && <div className="tuuli-pagination"><span>{totalElements} {totalElements === 1 ? "evento" : "eventos"}</span><Paginator first={page * pageSize} onPageChange={(event) => { setPage(event.rows === pageSize ? event.page : 0); setPageSize(event.rows); }} rows={pageSize} rowsPerPageOptions={[20, 50]} totalRecords={totalElements}/></div>}
    </div>
  </section>;
}
