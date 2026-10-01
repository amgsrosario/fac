import { KeyboardEvent, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "./api";
import "./GlobalSearch.css";
import { useRemoteLookup } from "./lookups/useRemoteLookup";

type Page<T> = {
  content: T[];
  totalPages?: number;
};

type SearchType = "document" | "customer" | "article";

type DocumentoComercial = {
  id: number;
  tipoDocumentoId: string;
  serie: string;
  numeroDocumento?: number | null;
  numeroDocumentoCompleto?: string | null;
  estado: string;
  clienteNome?: string | null;
  clienteNif?: string | null;
  dataEmissao?: string | null;
};

type Cliente = {
  id: number;
  nome: string;
  nif: string;
  localidade?: string | null;
  email?: string | null;
};

type Artigo = {
  codigo: string;
  descricao: string;
  tipoArtigo?: "ARTIGO" | "SERVICO";
  familiaId?: number | null;
  unidade?: string | null;
};

type GlobalSearchResult = {
  id: number | string;
  route: string;
  subtitle?: string;
  title: string;
  type: SearchType;
};

type GlobalSearchData = {
  articles: Artigo[];
  customers: Cliente[];
  documents: DocumentoComercial[];
};

const GROUPS: Array<{ key: SearchType; label: string }> = [
  { key: "document", label: "Documentos" },
  { key: "customer", label: "Clientes" },
  { key: "article", label: "Artigos" }
];

const QUALIFIERS: Record<string, SearchType> = {
  artigo: "article",
  artigos: "article",
  cliente: "customer",
  clientes: "customer",
  documento: "document",
  documentos: "document"
};

let cachedDocuments: DocumentoComercial[] | null = null;
let cachedDocumentsPromise: Promise<DocumentoComercial[]> | null = null;

export function GlobalSearch({ className = "" }: { className?: string }) {
  const navigate = useNavigate();
  const rootRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const [query, setQuery] = useState("");
  const [data, setData] = useState<GlobalSearchData | null>(null);
  const [documentsLoading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);
  const [documentsError, setError] = useState<string | null>(null);
  const parsed = useMemo(() => parseQuery(query), [query]);
  const ready = isSearchReady(parsed.term);
  const customers = useRemoteLookup<Cliente>({ endpoint: "/api/clientes/lookup", context: "global" }, { search: parsed.term, sort: ["nome,asc"] }, ready && (!parsed.type || parsed.type === "customer"));
  const articles = useRemoteLookup<Artigo>({ endpoint: "/api/artigos/lookup", context: "global" }, { search: parsed.term, sort: ["codigo,asc"] }, ready && (!parsed.type || parsed.type === "article"));
  const loading = documentsLoading || customers.loading || articles.loading;
  const error = documentsError || customers.error || articles.error;
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    if (!open) return;
    const closeOnOutside = (event: MouseEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", closeOnOutside);
    return () => document.removeEventListener("mousedown", closeOnOutside);
  }, [open]);

  useEffect(() => {
    setActiveIndex(0);
    setError(null);
    setLoading(false);
    if (!ready || (parsed.type && parsed.type !== "document")) return;
    let current = true;
    setLoading(true);
    const timer = window.setTimeout(() => {
      loadGlobalSearchDocuments()
        .then((documents) => { if (current) setData({ documents, customers: [], articles: [] }); })
        .catch((err) => { if (current) setError(err instanceof Error ? err.message : "Não foi possível pesquisar."); })
        .finally(() => { if (current) setLoading(false); });
    }, 300);
    return () => { current = false; window.clearTimeout(timer); };
  }, [ready, parsed.term, parsed.type, retry]);

  const grouped = useMemo(() => groupResults({ documents: data?.documents ?? [], customers: customers.rows, articles: articles.rows }, parsed), [data, customers.rows, articles.rows, parsed]);
  const flatResults = useMemo(() => GROUPS.flatMap((group) => grouped[group.key]), [grouped]);
  const hasResults = flatResults.length > 0;

  function choose(result: GlobalSearchResult) {
    setOpen(false);
    setQuery("");
    navigate(result.route);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Escape") {
      setOpen(false);
      return;
    }
    if (!open && ["ArrowDown", "Enter"].includes(event.key)) {
      setOpen(true);
    }
    if (event.key === "ArrowDown") {
      event.preventDefault();
      setActiveIndex((current) => Math.min(current + 1, Math.max(flatResults.length - 1, 0)));
      return;
    }
    if (event.key === "ArrowUp") {
      event.preventDefault();
      setActiveIndex((current) => Math.max(current - 1, 0));
      return;
    }
    if (event.key === "Enter" && !loading && !error && flatResults[activeIndex]) {
      event.preventDefault();
      choose(flatResults[activeIndex]);
    }
  }

  return (
    <div className={`fac-global-search ${className}`.trim()} ref={rootRef}>
      <span className="fac-global-search-field">
        <i aria-hidden="true" className="pi pi-search" />
        <input
          aria-autocomplete="list"
          aria-expanded={open}
          aria-label="Pesquisa global"
          onChange={(event) => {
            setQuery(event.target.value);
            setOpen(true);
          }}
          onFocus={() => query && setOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder="Pesquisar documentos, clientes ou artigos"
          ref={inputRef}
          type="search"
          value={query}
        />
        {query && (
          <button aria-label="Limpar pesquisa" onClick={() => { setQuery(""); setOpen(false); inputRef.current?.focus(); }} type="button">
            <i aria-hidden="true" className="pi pi-times" />
          </button>
        )}
      </span>
      {open && query.trim() && (
        <div className="fac-global-search-panel" role="listbox">
          {!ready && <p className="fac-global-search-state">Introduza pelo menos 2 caracteres.</p>}
          {ready && loading && <p className="fac-global-search-state">A pesquisar.</p>}
          {ready && error && <p className="fac-global-search-state">{error} <button type="button" onClick={() => { customers.retry(); articles.retry(); setRetry((n) => n + 1); }}>Tentar novamente</button></p>}
          {ready && !loading && !error && !hasResults && <p className="fac-global-search-state">Não foram encontrados resultados.</p>}
          {ready && !loading && !error && hasResults && GROUPS.map((group) => (
            <section className="fac-global-search-group" key={group.key}>
              <p>{group.label}</p>
              {grouped[group.key].length === 0 ? (
                <span className="fac-global-search-empty">Sem resultados.</span>
              ) : grouped[group.key].map((result) => {
                const index = flatResults.indexOf(result);
                return (
                  <button
                    className={index === activeIndex ? "active" : ""}
                    key={`${result.type}-${result.id}`}
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => choose(result)}
                    role="option"
                    type="button"
                  >
                    <strong>{result.title}</strong>
                    {result.subtitle && <small>{result.subtitle}</small>}
                  </button>
                );
              })}
            </section>
          ))}
        </div>
      )}
    </div>
  );
}

async function loadGlobalSearchDocuments() {
  if (cachedDocuments) return cachedDocuments;
  if (!cachedDocumentsPromise) {
    cachedDocumentsPromise = fetchAllPages<DocumentoComercial>("/api/documentos-comerciais", "dataEmissao,desc&sort=id,desc")
      .then((documents) => { cachedDocuments = documents; return documents; })
      .catch((error) => { cachedDocumentsPromise = null; throw error; });
  }
  return cachedDocumentsPromise;
}

async function fetchAllPages<T>(baseUrl: string, sort: string) {
  const size = 200;
  const rows: T[] = [];
  let page = 0;
  let totalPages = 1;
  do {
    const separator = baseUrl.includes("?") ? "&" : "?";
    const response = await apiFetch(`${baseUrl}${separator}page=${page}&size=${size}&sort=${sort}`);
    if (!response.ok) throw new Error(await response.text());
    const payload = await response.json() as Page<T>;
    rows.push(...payload.content);
    totalPages = payload.totalPages ?? 1;
    page += 1;
  } while (page < totalPages);
  return rows;
}

function groupResults(data: GlobalSearchData | null, parsed: ReturnType<typeof parseQuery>) {
  const initial: Record<SearchType, GlobalSearchResult[]> = { article: [], customer: [], document: [] };
  if (!data || !isSearchReady(parsed.term)) return initial;
  if (!parsed.type || parsed.type === "document") {
    initial.document = data.documents
      .filter((documento) => matchValues(documentValues(documento), parsed.term))
      .slice(0, 5)
      .map((documento) => ({
        id: documento.id,
        route: `/documentos/${documento.id}`,
        subtitle: [documento.clienteNome, formatDate(documento.dataEmissao), estadoLabel(documento.estado)].filter(Boolean).join(" · "),
        title: documentRef(documento),
        type: "document"
      }));
  }
  if (!parsed.type || parsed.type === "customer") {
    initial.customer = data.customers
      .slice(0, 5)
      .map((cliente) => ({
        id: cliente.id,
        route: `/clientes?cliente=${cliente.id}`,
        subtitle: [`NIF ${cliente.nif}`, cliente.localidade].filter(Boolean).join(" · "),
        title: cliente.nome,
        type: "customer"
      }));
  }
  if (!parsed.type || parsed.type === "article") {
    initial.article = data.articles
      .slice(0, 5)
      .map((artigo) => ({
        id: artigo.codigo,
        route: `/artigos?artigo=${encodeURIComponent(artigo.codigo)}`,
        subtitle: [artigo.descricao, artigo.unidade].filter(Boolean).join(" · "),
        title: artigo.codigo,
        type: "article"
      }));
  }
  return initial;
}

function parseQuery(rawQuery: string) {
  const trimmed = rawQuery.trim();
  const qualifier = trimmed.match(/^([a-zA-ZÀ-ÿ]+):(.*)$/);
  if (!qualifier) return { term: trimmed, type: undefined as SearchType | undefined };
  const type = QUALIFIERS[normalizeText(qualifier[1])];
  return type ? { term: qualifier[2].trim(), type } : { term: trimmed, type: undefined };
}

function isSearchReady(term: string) {
  const trimmed = term.trim();
  if (trimmed.length >= 2) return true;
  return /^[=^*$!]\S+$/.test(trimmed) || /^\d+$/.test(trimmed);
}

function matchValues(values: unknown[], query: string) {
  return values.some((value) => matchOperator(value, query));
}

function matchOperator(value: unknown, rawQuery: string) {
  const query = rawQuery.trim();
  const operator = ["^", "*", "=", "!", "$"].includes(query[0]) ? query[0] : "*";
  const needle = normalizeText(operator === "*" && query.startsWith("*") ? query.slice(1) : operator === "*" ? query : query.slice(1));
  const haystack = normalizeText(value);
  if (!needle) return true;
  if (operator === "^") return haystack.startsWith(needle);
  if (operator === "=") return haystack === needle;
  if (operator === "!") return !haystack.includes(needle);
  if (operator === "$") return haystack.endsWith(needle);
  return haystack.includes(needle);
}

function documentValues(documento: DocumentoComercial) {
  return [
    documento.id,
    documento.tipoDocumentoId,
    documento.serie,
    documento.numeroDocumento,
    documento.numeroDocumentoCompleto,
    documentRef(documento),
    documento.clienteNome,
    documento.clienteNif,
    documento.dataEmissao,
    documento.estado
  ];
}

function documentRef(documento: DocumentoComercial) {
  if (documento.numeroDocumentoCompleto) return documento.numeroDocumentoCompleto;
  const numero = documento.numeroDocumento ? `/${documento.numeroDocumento}` : "";
  return `${documento.tipoDocumentoId} ${documento.serie}${numero}`;
}

function estadoLabel(estado: string) {
  if (estado === "RASCUNHO") return "Rascunho";
  if (estado === "EMITIDO") return "Emitido";
  if (estado === "ANULADO") return "Anulado";
  return estado;
}

function formatDate(value?: string | null) {
  if (!value) return "";
  return value.slice(0, 10);
}

function normalizeText(value: unknown) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .toLowerCase();
}
