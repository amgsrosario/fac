import { useState } from "react";
import { apiFetch, responseError } from "../../../api";

type Entry = { number: string; status: string; net: number; vat: number; total: number };
type Receipt = { number: string; invoice: string; amount: number };
type ExportResult = { aviso: string; documentos: number; totalCredit: number; reconciliacao: Entry[]; recebimentos: Receipt[]; xmlBase64: string };

export default function DemoSaftView() {
  const [inicio,setInicio]=useState("");
  const [fim,setFim]=useState("");
  const [result,setResult]=useState<ExportResult | null>(null);
  const [error,setError]=useState("");
  const [busy,setBusy]=useState(false);
  async function generate() {
    setResult(null); setError(""); setBusy(true);
    try {
      const response=await apiFetch(`/api/saft-demo/exportar?inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`);
      if (!response.ok) throw new Error(await responseError(response));
      const value=await response.json() as ExportResult;
      const bytes=Uint8Array.from(atob(value.xmlBase64),character=>character.charCodeAt(0));
      const url=URL.createObjectURL(new Blob([bytes],{type:"application/xml"}));
      const link=document.createElement("a"); link.href=url; link.download=`SAFT-DEMO-${inicio}-${fim}.xml`; link.click();
      window.setTimeout(()=>URL.revokeObjectURL(url),1000);
      setResult(value);
    } catch (exception) { setError(exception instanceof Error ? exception.message : "Não foi possível gerar o ficheiro."); }
    finally { setBusy(false); }
  }
  const money=(value:number)=>new Intl.NumberFormat("pt-PT",{style:"currency",currency:"EUR"}).format(value);
  return <section className="fac-card" aria-label="Exportação SAF-T demo">
    <h2>SAF-T (PT) — demonstração FT/EUR e recibo integral</h2>
    <p>Ficheiro sintético para conferência, sem validade fiscal. O período é recusado quando contém documentos ou fontes fora da cobertura.</p>
    <label>Data inicial <input type="date" value={inicio} onChange={event=>setInicio(event.target.value)} /></label>{" "}
    <label>Data final <input type="date" value={fim} onChange={event=>setFim(event.target.value)} /></label>{" "}
    <button type="button" disabled={busy || !inicio || !fim || inicio>fim} onClick={generate}>{busy ? "A verificar…" : "Gerar e descarregar XML"}</button>
    {error && <p role="alert">Exportação bloqueada: {error}</p>}
    {result && <div><p role="status">{result.aviso} · {result.documentos} documentos · TotalCredit {money(result.totalCredit)}</p>
      <p>Os documentos anulados são apresentados, mas não entram no TotalCredit.</p>
      <table><thead><tr><th>Documento</th><th>Estado</th><th>Base</th><th>IVA</th><th>Total</th></tr></thead>
        <tbody>{result.reconciliacao.map(item=><tr key={item.number}><td>{item.number}</td><td>{item.status}</td><td>{money(item.net)}</td><td>{money(item.vat)}</td><td>{money(item.total)}</td></tr>)}</tbody>
      </table>
      {result.recebimentos.length>0 && <><p>Recebimentos (totalizados separadamente das vendas)</p>
        <table><thead><tr><th>Recibo</th><th>Factura</th><th>Recebido</th></tr></thead>
          <tbody>{result.recebimentos.map(item=><tr key={`${item.number}:${item.invoice}`}><td>{item.number}</td><td>{item.invoice}</td><td>{money(item.amount)}</td></tr>)}</tbody></table></>}
      </div>}
  </section>;
}
