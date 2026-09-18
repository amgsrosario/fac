import { useEffect, useMemo, useRef, useState } from "react";
import { apiFetch } from "./api";
import PostalCodeLookup from "./PostalCodeLookup";

type Page<T> = { content: T[]; number: number; size: number; totalElements: number; totalPages: number };
type Row = Record<string, unknown>;
type Values = Record<string, string | boolean>;
type Option = { value: string; label: string };
type Feedback = { kind: "success" | "error"; text: string };
type TableKey = "tipos-documento" | "series" | "riva" | "codpostal" | "freguesias" | "armazens";
type Field = {
  key: string;
  label: string;
  type?: "text" | "number" | "date" | "checkbox" | "password" | "select";
  maxLength?: number;
  required?: boolean;
  createOnly?: boolean;
  optionalOnUpdate?: boolean;
  options?: string;
};
type Config = {
  key: TableKey;
  label: string;
  endpoint: string;
  fields: Field[];
  columns: { key: string; label: string }[];
  rowId: (row: Row) => string;
  itemUrl: (row: Row) => string;
};

const field = (key: string, label: string, options: Partial<Field> = {}): Field => ({ key, label, ...options });

const configs: Record<TableKey, Config> = {
  "tipos-documento": {
    key: "tipos-documento", label: "Tipos de documento", endpoint: "/api/tipos-documento",
    fields: [field("id", "Código", { required: true, maxLength: 3, createOnly: true }), field("descricao", "Descrição", { required: true, maxLength: 50 }), field("codigoFiscal", "Código fiscal", { type: "select", maxLength: 2, options: "codigosFiscais" }), field("modeloEmissao1", "Modelo de emissão 1", { maxLength: 25 }), field("modeloEmissao2", "Modelo de emissão 2", { maxLength: 25 }), field("modeloEmissao3", "Modelo de emissão 3", { maxLength: 25 }), field("modeloEmissao4", "Modelo de emissão 4", { maxLength: 25 }), field("areaGestao", "Área de gestão", { type: "select", required: true, options: "areasGestao" }), field("entidade", "Entidade", { type: "select", required: true, options: "entidadesDocumento" }), field("sinalContabilistico", "Sinal contabilístico", { type: "select", required: true, options: "sinais" }), field("liquidacaoImediata", "Liquidação imediata", { type: "checkbox" })],
    columns: [{ key: "id", label: "Código" }, { key: "codigoFiscal", label: "Fiscal" }, { key: "descricao", label: "Descrição" }, { key: "areaGestao", label: "Área" }, { key: "liquidacaoImediata", label: "Liquidação imediata" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/tipos-documento/${encodeURIComponent(String(row.id))}`
  },
  series: {
    key: "series", label: "Séries", endpoint: "/api/series",
    fields: [field("tipoDocumentoId", "Tipo de documento", { type: "select", required: true, createOnly: true, options: "tiposDocumento" }), field("serie", "Série", { required: true, maxLength: 10, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 50 }), field("codigoAt", "Código AT", { maxLength: 100 }), field("dataCodigoAt", "Data do código AT", { type: "date" })],
    columns: [{ key: "tipoDocumentoId", label: "Documento" }, { key: "serie", label: "Série" }, { key: "nome", label: "Nome" }, { key: "numerador", label: "Numerador" }],
    rowId: (row) => `${row.tipoDocumentoId}-${row.serie}`, itemUrl: (row) => `/api/series/${encodeURIComponent(String(row.tipoDocumentoId))}/${encodeURIComponent(String(row.serie))}`
  },
  riva: {
    key: "riva", label: "Regimes de IVA", endpoint: "/api/riva",
    fields: [field("id", "Código", { required: true, maxLength: 3, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 30 })],
    columns: [{ key: "id", label: "Código" }, { key: "nome", label: "Nome" }, { key: "taxas", label: "Taxas" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/riva/${encodeURIComponent(String(row.id))}`
  },
  codpostal: {
    key: "codpostal", label: "Códigos postais", endpoint: "/api/codpostal",
    fields: [field("id", "Código postal", { required: true, maxLength: 20, createOnly: true }), field("nome", "Localidade", { required: true, maxLength: 50 })],
    columns: [{ key: "id", label: "Código" }, { key: "nome", label: "Localidade" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/codpostal/${encodeURIComponent(String(row.id))}`
  },
  freguesias: {
    key: "freguesias", label: "Freguesias", endpoint: "/api/freguesias",
    fields: [field("codigo", "Código", { required: true, maxLength: 6, createOnly: true }), field("codigoDistrito", "Código distrito", { required: true, maxLength: 2, createOnly: true }), field("codigoConcelho", "Código concelho", { required: true, maxLength: 2, createOnly: true }), field("codigoFreguesia", "Código freguesia", { required: true, maxLength: 2, createOnly: true }), field("concelho", "Concelho", { required: true, maxLength: 50 }), field("nome", "Freguesia", { required: true, maxLength: 80 }), field("extinta", "Extinta", { type: "checkbox" })],
    columns: [{ key: "codigo", label: "Código" }, { key: "concelho", label: "Concelho" }, { key: "nome", label: "Freguesia" }, { key: "extinta", label: "Estado" }],
    rowId: (row) => String(row.codigo), itemUrl: (row) => `/api/freguesias/${encodeURIComponent(String(row.codigo))}`
  },
  armazens: {
    key: "armazens", label: "Armazéns", endpoint: "/api/armazens",
    fields: [field("id", "Código", { required: true, maxLength: 3, createOnly: true }), field("nome", "Nome", { required: true, maxLength: 100 }), field("morada", "Morada", { required: true, maxLength: 60 }), field("morada1", "Morada complementar", { maxLength: 60 }), field("codPostalId", "Código postal", { required: true }), field("localidade", "Localidade", { required: true, maxLength: 50 }), field("paisId", "País", { type: "select", required: true, options: "paises" }), field("freguesiaId", "Freguesia")],
    columns: [{ key: "id", label: "Código" }, { key: "nome", label: "Nome" }, { key: "localidade", label: "Localidade" }, { key: "paisId", label: "País" }],
    rowId: (row) => String(row.id), itemUrl: (row) => `/api/armazens/${encodeURIComponent(String(row.id))}`
  }
};

export const specificTables: { key: TableKey; label: string; group: string }[] = [
  { key: "tipos-documento", label: "Tipos de documento", group: "Documentos" }, { key: "series", label: "Séries", group: "Documentos" },
  { key: "riva", label: "Regimes de IVA", group: "Fiscalidade" }, { key: "codpostal", label: "Códigos postais", group: "Localização" },
  { key: "freguesias", label: "Freguesias", group: "Localização" }, { key: "armazens", label: "Armazéns", group: "Sistema" }
];

export default function TabelasEspecificasView({ tableKey, onBack, startNew = false }: { tableKey: TableKey; onBack: () => void; startNew?: boolean }) {
  const config = configs[tableKey];
  const [rows, setRows] = useState<Row[]>([]);
  const [values, setValues] = useState<Values>({});
  const [editing, setEditing] = useState<Row | null>(null);
  const [options, setOptions] = useState<Record<string, Option[]>>({
    sinais: [{ value: "1", label: "1 - Débito" }, { value: "2", label: "2 - Crédito" }],
    areasGestao: [
      { value: "1", label: "1 - Documento comercial (área 1)" },
      { value: "2", label: "2 - Documento comercial / faturação" },
      { value: "3", label: "3 - Documento financeiro / tesouraria" }
    ],
    entidadesDocumento: [{ value: "1", label: "1 - Cliente" }],
    codigosFiscais: [
      { value: "FT", label: "FT - Fatura" },
      { value: "FS", label: "FS - Fatura simplificada" },
      { value: "FR", label: "FR - Fatura-recibo" },
      { value: "ND", label: "ND - Nota de débito" },
      { value: "NC", label: "NC - Nota de crédito" },
      { value: "VD", label: "VD - Venda a dinheiro" },
      { value: "TV", label: "TV - Talão de venda" },
      { value: "TD", label: "TD - Talão de devolução" },
      { value: "AA", label: "AA - Alienação de ativos" },
      { value: "DA", label: "DA - Devolução de ativos" },
      { value: "RP", label: "RP - Recibo de prémio" },
      { value: "RE", label: "RE - Estorno ou recibo de estorno" },
      { value: "CS", label: "CS - Imputação a co-seguradoras" },
      { value: "LD", label: "LD - Imputação a co-seguradora líder" },
      { value: "RA", label: "RA - Resseguro aceite" },
      { value: "CM", label: "CM - Consulta de mesa" },
      { value: "CC", label: "CC - Crédito de consignação" },
      { value: "FC", label: "FC - Fatura de consignação" },
      { value: "GR", label: "GR - Guia de remessa" },
      { value: "GT", label: "GT - Guia de transporte" },
      { value: "GA", label: "GA - Guia de movimentação de ativos próprios" },
      { value: "GC", label: "GC - Guia de consignação" },
      { value: "GD", label: "GD - Guia ou nota de devolução" },
      { value: "RC", label: "RC - Recibo" },
      { value: "RG", label: "RG - Outros recibos emitidos" }
    ]
  });
  const [rates, setRates] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const isRiva = tableKey === "riva";
  const isPostal = tableKey === "codpostal";
  const isParish = tableKey === "freguesias";
  const isWarehouse = tableKey === "armazens";
  const [postalSearch, setPostalSearch] = useState("");
  const [postalDebouncedSearch, setPostalDebouncedSearch] = useState("");
  const [postalPage, setPostalPage] = useState(0);
  const [postalPageSize, setPostalPageSize] = useState(25);
  const [postalSortField, setPostalSortField] = useState<"id" | "nome">("id");
  const [postalSortDirection, setPostalSortDirection] = useState<"asc" | "desc">("asc");
  const [postalTotalElements, setPostalTotalElements] = useState(0);
  const [postalTotalPages, setPostalTotalPages] = useState(0);
  const [parishSearch, setParishSearch] = useState("");
  const [parishDebouncedSearch, setParishDebouncedSearch] = useState("");
  const [parishPage, setParishPage] = useState(0);
  const [parishPageSize, setParishPageSize] = useState(25);
  const [parishSortField, setParishSortField] = useState<"codigo" | "concelho" | "nome" | "extinta">("codigo");
  const [parishSortDirection, setParishSortDirection] = useState<"asc" | "desc">("asc");
  const [parishTotalElements, setParishTotalElements] = useState(0);
  const [parishTotalPages, setParishTotalPages] = useState(0);
  const [warehousePage, setWarehousePage] = useState(0);
  const [warehousePageSize, setWarehousePageSize] = useState(25);
  const [warehouseTotalElements, setWarehouseTotalElements] = useState(0);
  const [warehouseTotalPages, setWarehouseTotalPages] = useState(0);
  const rowsRequestRef = useRef(0);

  useEffect(() => { if (startNew) reset(); }, [startNew, tableKey]);

  useEffect(() => { if (!isPostal && !isParish && !isWarehouse) load(); }, [tableKey]);

  useEffect(() => {
    if (!isPostal) return;
    const timeout = window.setTimeout(() => {
      setPostalPage(0);
      setPostalDebouncedSearch(postalSearch.trim());
    }, 300);
    return () => window.clearTimeout(timeout);
  }, [isPostal, postalSearch]);

  useEffect(() => {
    if (isPostal) loadPostal();
  }, [isPostal, postalDebouncedSearch, postalPage, postalPageSize, postalSortField, postalSortDirection]);

  useEffect(() => {
    if (!isParish) return;
    const timeout = window.setTimeout(() => {
      setParishPage(0);
      setParishDebouncedSearch(parishSearch.trim());
    }, 300);
    return () => window.clearTimeout(timeout);
  }, [isParish, parishSearch]);

  useEffect(() => {
    if (isParish) loadParishes();
  }, [isParish, parishDebouncedSearch, parishPage, parishPageSize, parishSortField, parishSortDirection]);

  useEffect(() => {
    if (isWarehouse) loadWarehouses();
  }, [isWarehouse, warehousePage, warehousePageSize]);

  async function load() {
    const requestId = ++rowsRequestRef.current;
    setLoading(true); setFeedback(null);
    try {
      const sortField = tableKey === "freguesias" ? "codigo" : tableKey === "series" ? "serie" : "id";
      const [page, support] = await Promise.all([get<Page<Row>>(`${config.endpoint}?size=1000&sort=${sortField},asc`), loadOptions()]);
      if (requestId !== rowsRequestRef.current) return;
      setRows(page.content); setOptions((current) => ({ ...current, ...support })); reset();
    } catch (error) {
      if (requestId === rowsRequestRef.current) setFeedback({ kind: "error", text: errorMessage(error) });
    } finally {
      if (requestId === rowsRequestRef.current) setLoading(false);
    }
  }

  async function loadPostal() {
    const requestId = ++rowsRequestRef.current;
    setLoading(true); setFeedback(null);
    const params = new URLSearchParams({
      page: String(postalPage),
      size: String(postalPageSize),
      sort: `${postalSortField},${postalSortDirection}`
    });
    if (postalDebouncedSearch) params.set("search", postalDebouncedSearch);
    try {
      const result = await get<Page<Row>>(`${config.endpoint}?${params}`);
      if (requestId !== rowsRequestRef.current) return;
      setRows(result.content);
      setPostalPage(result.number);
      setPostalPageSize(result.size);
      setPostalTotalElements(result.totalElements);
      setPostalTotalPages(result.totalPages);
    } catch (error) {
      if (requestId === rowsRequestRef.current) setFeedback({ kind: "error", text: errorMessage(error) });
    } finally {
      if (requestId === rowsRequestRef.current) setLoading(false);
    }
  }

  async function loadParishes() {
    const requestId = ++rowsRequestRef.current;
    setLoading(true); setFeedback(null);
    const params = new URLSearchParams({
      page: String(parishPage),
      size: String(parishPageSize),
      sort: `${parishSortField},${parishSortDirection}`
    });
    if (parishDebouncedSearch) params.set("search", parishDebouncedSearch);
    try {
      const result = await get<Page<Row>>(`${config.endpoint}?${params}`);
      if (requestId !== rowsRequestRef.current) return;
      setRows(result.content);
      setParishPage(result.number);
      setParishPageSize(result.size);
      setParishTotalElements(result.totalElements);
      setParishTotalPages(result.totalPages);
    } catch (error) {
      if (requestId === rowsRequestRef.current) setFeedback({ kind: "error", text: errorMessage(error) });
    } finally {
      if (requestId === rowsRequestRef.current) setLoading(false);
    }
  }

  async function loadWarehouses() {
    const requestId = ++rowsRequestRef.current;
    setLoading(true); setFeedback(null);
    const params = new URLSearchParams({ page: String(warehousePage), size: String(warehousePageSize), sort: "id,asc" });
    try {
      const [result, support] = await Promise.all([get<Page<Row>>(`${config.endpoint}?${params}`), loadOptions()]);
      if (requestId !== rowsRequestRef.current) return;
      setRows(result.content);
      setWarehousePage(result.number);
      setWarehousePageSize(result.size);
      setWarehouseTotalElements(result.totalElements);
      setWarehouseTotalPages(result.totalPages);
      setOptions((current) => ({ ...current, ...support }));
      reset();
    } catch (error) {
      if (requestId === rowsRequestRef.current) setFeedback({ kind: "error", text: errorMessage(error) });
    } finally {
      if (requestId === rowsRequestRef.current) setLoading(false);
    }
  }

  async function loadOptions() {
    const required = new Set(config.fields.map((item) => item.options).filter(Boolean));
    if (isRiva) required.add("tiposTaxa");
    const result: Record<string, Option[]> = {};
    const staticOptions = new Set(["sinais", "areasGestao", "entidadesDocumento", "codigosFiscais"]);
    await Promise.all([...required].filter((key) => !staticOptions.has(key!)).map(async (key) => {
      const definitions: Record<string, [string, (row: Row) => Option]> = {
        tiposDocumento: ["/api/tipos-documento?size=500&sort=id,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.descricao}` })],
        paises: ["/api/paises?size=500&sort=nome,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.nome}` })],
        tiposTaxa: ["/api/tipos-taxa-iva?size=100&sort=id,asc", (row) => ({ value: String(row.id), label: `${row.id} - ${row.descricao}` })]
      };
      const definition = definitions[key!];
      if (definition) result[key!] = (await get<Page<Row>>(definition[0])).content.map(definition[1]);
    }));
    return result;
  }

  function reset() {
    setEditing(null); setValues(Object.fromEntries(config.fields.map((item) => [item.key, item.type === "checkbox" ? false : ""]))); setRates({});
  }

  function edit(row: Row) {
    setEditing(row); setFeedback(null);
    setValues(Object.fromEntries(config.fields.map((item) => [item.key, item.type === "checkbox" ? Boolean(row[item.key]) : item.key === "password" ? "" : String(row[item.key] ?? "")])));
    if (isRiva) setRates(Object.fromEntries(((row.taxas as Row[]) ?? []).map((taxa) => [String(taxa.tipoTaxaIvaId), String(taxa.valor)])));
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function save() {
    for (const item of config.fields) {
      if (item.required && !(editing && (item.createOnly || item.optionalOnUpdate)) && String(values[item.key] ?? "").trim() === "") {
        setFeedback({ kind: "error", text: `${item.label} é obrigatório.` }); return;
      }
    }
    const taxas = Object.entries(rates).filter(([, value]) => value !== "").map(([tipoTaxaIvaId, valor]) => ({ tipoTaxaIvaId, valor: Number(valor) }));
    if (isRiva && taxas.length === 0) { setFeedback({ kind: "error", text: "O regime de IVA deve ter pelo menos uma taxa." }); return; }
    setLoading(true); setFeedback(null);
    try {
      const payload: Record<string, unknown> = Object.fromEntries(config.fields.filter((item) => !(editing && item.createOnly) && !(editing && item.optionalOnUpdate && !values[item.key])).map((item) => [item.key, item.type === "number" ? Number(values[item.key]) : item.type === "checkbox" ? Boolean(values[item.key]) : blankToNull(String(values[item.key] ?? ""))]));
      if (isRiva) payload.taxas = taxas;
      const response = await apiFetch(editing ? config.itemUrl(editing) : config.endpoint, { method: editing ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
      if (!response.ok) throw new Error(await responseError(response));
      if (isPostal) await loadPostal(); else if (isParish) await loadParishes(); else if (isWarehouse) await loadWarehouses(); else await load();
      setFeedback({ kind: "success", text: editing ? "Registo atualizado com sucesso." : "Registo criado com sucesso." });
    } catch (error) { setFeedback({ kind: "error", text: errorMessage(error) }); setLoading(false); }
  }

  async function remove(row: Row) {
    if (!window.confirm(`Eliminar ${config.label}: ${config.rowId(row)}?\n\nSe estiver em utilização, a aplicação recusará a operação.`)) return;
    setLoading(true); setFeedback(null);
    try {
      const response = await apiFetch(config.itemUrl(row), { method: "DELETE" });
      if (!response.ok) throw new Error(await responseError(response));
      if (isPostal) await loadPostal(); else if (isParish) await loadParishes(); else if (isWarehouse) await loadWarehouses(); else await load();
      setFeedback({ kind: "success", text: "Registo eliminado com sucesso." });
    } catch (error) { setFeedback({ kind: "error", text: `O registo não foi eliminado e permanece na tabela. ${errorMessage(error)}` }); setLoading(false); }
  }

  const rateOptions = useMemo(() => options.tiposTaxa ?? [], [options]);

  function changePostalSort(fieldName: "id" | "nome") {
    setPostalPage(0);
    if (postalSortField === fieldName) setPostalSortDirection((current) => current === "asc" ? "desc" : "asc");
    else {
      setPostalSortField(fieldName);
      setPostalSortDirection("asc");
    }
  }

  function changeParishSort(fieldName: "codigo" | "concelho" | "nome" | "extinta") {
    setParishPage(0);
    if (parishSortField === fieldName) setParishSortDirection((current) => current === "asc" ? "desc" : "asc");
    else {
      setParishSortField(fieldName);
      setParishSortDirection("asc");
    }
  }

  const isPagedCatalog = isPostal || isParish;
  const isPagedTable = isPagedCatalog || isWarehouse;
  const catalogSearch = isPostal ? postalSearch : parishSearch;
  const catalogDebouncedSearch = isPostal ? postalDebouncedSearch : parishDebouncedSearch;
  const catalogPage = isPostal ? postalPage : isParish ? parishPage : warehousePage;
  const catalogPageSize = isPostal ? postalPageSize : isParish ? parishPageSize : warehousePageSize;
  const catalogTotalElements = isPostal ? postalTotalElements : isParish ? parishTotalElements : warehouseTotalElements;
  const catalogTotalPages = isPostal ? postalTotalPages : isParish ? parishTotalPages : warehouseTotalPages;

  return <section className="fac-panel">
    <div className="fac-panel-header"><div><p className="fac-eyebrow">Tabela</p><h2>{config.label}</h2></div><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={onBack} type="button">Voltar</button><button className="fac-primary-button" onClick={reset} type="button">Novo registo</button></div></div>
    {feedback && <p className={`fac-editor-message fac-editor-message-${feedback.kind}`} role={feedback.kind === "error" ? "alert" : "status"}>{feedback.text}</p>}
    <div className="fac-table-editor"><div className="fac-form-grid">{config.fields.map((item) => tableKey === "armazens" && item.key === "codPostalId"
      ? <PostalCodeLookup key={item.key} required={item.required} value={String(values[item.key] ?? "")} onChange={(value) => setValues((current) => ({ ...current, [item.key]: value }))} />
      : tableKey === "armazens" && item.key === "freguesiaId"
      ? <ReferenceLookup field={item} key={item.key} value={String(values[item.key] ?? "")} onChange={(value) => setValues((current) => ({ ...current, [item.key]: value }))} />
      : <EditorField field={item} key={item.key} options={options[item.options ?? ""] ?? []} editing={Boolean(editing)} value={values[item.key]} onChange={(value) => setValues((current) => ({ ...current, [item.key]: value }))} />)}</div>
      {isRiva && <div className="fac-rate-grid"><p className="fac-muted">Taxas do regime</p>{rateOptions.map((option) => <label className="fac-field" key={option.value}><span>{option.label}</span><input min="0" onChange={(event) => setRates((current) => ({ ...current, [option.value]: event.target.value }))} step="0.01" type="number" value={rates[option.value] ?? ""}/></label>)}</div>}
      <div className="fac-form-footer"><span className="fac-muted">{editing ? `A editar ${config.rowId(editing)}` : "Novo registo"}</span><button className="fac-primary-button" disabled={loading} onClick={save} type="button">{loading ? "A guardar..." : "Guardar"}</button></div>
    </div>
    <p className={`fac-muted${isPagedCatalog ? " fac-catalog-maintenance-note" : ""}`}>A eliminação só é aceite para registos nunca utilizados.</p>
    {isPagedCatalog && <div className="fac-postal-toolbar" aria-label={isPostal ? "Pesquisa de códigos postais" : "Pesquisa de freguesias"}>
      <input aria-label={isPostal ? "Pesquisar códigos postais" : "Pesquisar freguesias"} onChange={(event) => isPostal ? setPostalSearch(event.target.value) : setParishSearch(event.target.value)} placeholder={isPostal ? "Pesquisar código ou localidade..." : "Pesquisar código, freguesia ou concelho..."} type="search" value={catalogSearch} />
      <span aria-live="polite">{loading ? "A carregar..." : recordCountLabel(catalogTotalElements)}</span>
    </div>}
    <div className={isPagedCatalog ? "fac-postal-table-wrap" : undefined}><table className={`fac-table${isPagedCatalog ? " fac-postal-table" : ""}`}><thead><tr>{config.columns.map((column) => <th key={column.key}>{isPostal && (column.key === "id" || column.key === "nome") ? <button aria-label={`Ordenar por ${column.label}`} className="fac-postal-sort" onClick={() => changePostalSort(column.key as "id" | "nome")} type="button">{column.label}<span aria-hidden="true">{postalSortField === column.key ? (postalSortDirection === "asc" ? " ↑" : " ↓") : ""}</span></button> : isParish ? <button aria-label={`Ordenar por ${column.label}`} className="fac-postal-sort" onClick={() => changeParishSort(column.key as "codigo" | "concelho" | "nome" | "extinta")} type="button">{column.label}<span aria-hidden="true">{parishSortField === column.key ? (parishSortDirection === "asc" ? " ↑" : " ↓") : ""}</span></button> : column.label}</th>)}<th>Ações</th></tr></thead><tbody>{rows.map((row) => <tr key={config.rowId(row)}>{config.columns.map((column) => <td key={column.key}>{display(column.key, row[column.key])}</td>)}<td><div className="fac-inline-actions"><button className="fac-ghost-button" onClick={() => edit(row)} type="button">Editar</button><button className="fac-link-danger" disabled={loading} onClick={() => remove(row)} type="button">Eliminar</button></div></td></tr>)}{!loading && rows.length === 0 && <tr><td colSpan={config.columns.length + 1}>{isPagedCatalog && catalogDebouncedSearch ? `Sem resultados para “${catalogDebouncedSearch}”.` : "Sem registos."}</td></tr>}</tbody></table></div>
    {isPagedTable && <div className="fac-postal-pagination">
      <span>{recordCountLabel(catalogTotalElements)}</span>
      <div className="fac-postal-page-controls">
        <button className="fac-ghost-button" disabled={loading || catalogPage === 0} onClick={() => isPostal ? setPostalPage((current) => current - 1) : isParish ? setParishPage((current) => current - 1) : setWarehousePage((current) => current - 1)} type="button">Anterior</button>
        <span>Página {catalogTotalPages === 0 ? 0 : catalogPage + 1} de {catalogTotalPages.toLocaleString("pt-PT")}</span>
        <button className="fac-ghost-button" disabled={loading || catalogPage + 1 >= catalogTotalPages} onClick={() => isPostal ? setPostalPage((current) => current + 1) : isParish ? setParishPage((current) => current + 1) : setWarehousePage((current) => current + 1)} type="button">Seguinte</button>
      </div>
      <label><span>Por página</span><select onChange={(event) => { const size = Number(event.target.value); if (isPostal) { setPostalPage(0); setPostalPageSize(size); } else if (isParish) { setParishPage(0); setParishPageSize(size); } else { setWarehousePage(0); setWarehousePageSize(size); } }} value={catalogPageSize}><option value="25">25</option><option value="50">50</option></select></label>
    </div>}
  </section>;
}

function EditorField({ field: item, value, editing, options, onChange }: { field: Field; value: string | boolean; editing: boolean; options: Option[]; onChange: (value: string | boolean) => void }) {
  if (item.type === "checkbox") return <label className="fac-check-field"><input checked={Boolean(value)} onChange={(event) => onChange(event.target.checked)} type="checkbox"/><span>{item.label}</span></label>;
  return <label className="fac-field"><span>{item.label}{item.optionalOnUpdate && editing ? " (deixar vazio para manter)" : ""}</span>{item.type === "select" ? <select disabled={editing && item.createOnly} onChange={(event) => onChange(event.target.value)} value={String(value ?? "")}><option value="">Selecionar</option>{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select> : <input disabled={editing && item.createOnly} maxLength={item.maxLength} min={item.type === "number" ? 0 : undefined} onChange={(event) => onChange(event.target.value)} step={item.type === "number" ? "1" : undefined} type={item.type ?? "text"} value={String(value ?? "")}/>}</label>;
}

function ReferenceLookup({ field, value, onChange }: { field: Field; value: string; onChange: (value: string) => void }) {
  const postal = field.key === "codPostalId";
  const endpoint = postal ? "/api/codpostal" : "/api/freguesias";
  const target = postal ? "codpostal" : "freguesias";
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Option[]>([]);
  const [open, setOpen] = useState(false);
  const requestRef = useRef(0);
  const selectedRef = useRef("");

  useEffect(() => {
    const requestId = ++requestRef.current;
    if (!value) {
      if (selectedRef.current) setQuery("");
      selectedRef.current = "";
      return;
    }
    selectedRef.current = value;
    get<Row>(`${endpoint}/${encodeURIComponent(value)}`).then((row) => {
      if (requestId === requestRef.current) setQuery(postal ? `${row.id} · ${row.nome}` : `${row.codigo} · ${row.nome}`);
    }).catch(() => { if (requestId === requestRef.current) setQuery(value); });
  }, [value, endpoint, postal]);

  useEffect(() => {
    if (!open || !query.trim()) { setResults([]); return; }
    const requestId = ++requestRef.current;
    const timeout = window.setTimeout(async () => {
      const params = new URLSearchParams({ search: query.trim(), page: "0", size: "10", sort: postal ? "id,asc" : "codigo,asc" });
      try {
        const page = await get<Page<Row>>(`${endpoint}?${params}`);
        if (requestId === requestRef.current) setResults(page.content.map((row) => ({
          value: String(postal ? row.id : row.codigo), label: `${postal ? row.id : row.codigo} · ${row.nome}`
        })));
      } catch { if (requestId === requestRef.current) setResults([]); }
    }, 300);
    return () => window.clearTimeout(timeout);
  }, [query, open, endpoint, postal]);

  return <div className="fac-field fac-reference-lookup">
    <span>{field.label}</span>
    <div className="fac-reference-lookup-row">
      <input aria-label={field.label} autoComplete="off" onBlur={() => window.setTimeout(() => setOpen(false), 150)} onChange={(event) => { selectedRef.current = ""; setQuery(event.target.value); onChange(""); setOpen(true); }} onFocus={() => setOpen(true)} placeholder={postal ? "Pesquisar código postal ou localidade..." : "Pesquisar código, freguesia ou concelho..."} type="search" value={query} />
      <a aria-label={postal ? "Criar código postal" : "Criar freguesia"} href={`/configuracao/tabelas/${target}?mode=new`} rel="noopener noreferrer" target="_blank" title={postal ? "Criar código postal" : "Criar freguesia"}><i aria-hidden="true" className="pi pi-external-link" /></a>
    </div>
    {open && results.length > 0 && <div className="fac-reference-lookup-results">{results.map((result) => <button key={result.value} onMouseDown={(event) => event.preventDefault()} onClick={() => { onChange(result.value); setQuery(result.label); setOpen(false); }} type="button">{result.label}</button>)}</div>}
  </div>;
}

function display(key: string, value: unknown) { if (key === "taxas" && Array.isArray(value)) return value.map((taxa: Row) => `${taxa.tipoTaxaIvaId}: ${taxa.valor}%`).join(" | "); if (typeof value === "boolean") return key === "inativo" || key === "extinta" ? (value ? "Inativo" : "Ativo") : value ? "Sim" : "Não"; return value == null || value === "" ? "-" : String(value); }
function recordCountLabel(count: number) { return `${count.toLocaleString("pt-PT")} ${count === 1 ? "registo" : "registos"}`; }
function blankToNull(value: string) { const trimmed = value.trim(); return trimmed || null; }
async function get<T>(url: string): Promise<T> { const response = await apiFetch(url); if (!response.ok) throw new Error(await responseError(response)); return response.json(); }
async function responseError(response: Response) { try { const payload = await response.json(); return payload.message || payload.error || `Erro HTTP ${response.status}`; } catch { return `Erro HTTP ${response.status}`; } }
function errorMessage(error: unknown) { return error instanceof Error ? error.message : "Não foi possível concluir a operação."; }
