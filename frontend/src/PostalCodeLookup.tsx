import { useEffect, useRef, useState } from "react";
import { apiFetch } from "./api";

type PostalCode = {
  id: string;
  nome: string;
};

type Page<T> = {
  content: T[];
};

type PostalCodeLookupProps = {
  value: string;
  onChange: (value: string, postalCode?: PostalCode) => void;
  required?: boolean;
  disabled?: boolean;
  readOnly?: boolean;
  id?: string;
  name?: string;
};

export default function PostalCodeLookup({
  value,
  onChange,
  required = false,
  disabled = false,
  readOnly = false,
  id,
  name
}: PostalCodeLookupProps) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<PostalCode[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const requestRef = useRef(0);
  const selectedRef = useRef("");

  useEffect(() => {
    const requestId = ++requestRef.current;
    if (!value) {
      if (selectedRef.current) setQuery("");
      selectedRef.current = "";
      return;
    }
    if (selectedRef.current === value && query.startsWith(`${value} · `)) return;
    selectedRef.current = value;
    void getPostalCode(value).then((postalCode) => {
      if (requestId === requestRef.current) setQuery(formatPostalCode(postalCode));
    }).catch(() => {
      if (requestId === requestRef.current) setQuery(value);
    });
  }, [value]);

  useEffect(() => {
    const search = query.trim();
    if (!open || disabled || readOnly || !search || (selectedRef.current && search.startsWith(`${selectedRef.current} · `))) {
      setResults([]);
      setLoading(false);
      return;
    }
    const requestId = ++requestRef.current;
    const timeout = window.setTimeout(async () => {
      setLoading(true);
      const params = new URLSearchParams({ search, page: "0", size: "10", sort: "id,asc" });
      try {
        const response = await apiFetch(`/api/codpostal?${params}`);
        if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);
        const page = await response.json() as Page<PostalCode>;
        if (requestId === requestRef.current) setResults(page.content);
      } catch {
        if (requestId === requestRef.current) setResults([]);
      } finally {
        if (requestId === requestRef.current) setLoading(false);
      }
    }, 300);
    return () => window.clearTimeout(timeout);
  }, [query, open, disabled, readOnly]);

  const choose = (postalCode: PostalCode) => {
    selectedRef.current = postalCode.id;
    setQuery(formatPostalCode(postalCode));
    setOpen(false);
    onChange(postalCode.id, postalCode);
  };

  return <label className="fac-field fac-reference-lookup">
    <span>Código postal</span>
    <div className="fac-reference-lookup-row">
      <input
        aria-label="Código postal"
        autoComplete="off"
        disabled={disabled}
        id={id}
        name={name}
        onBlur={() => window.setTimeout(() => setOpen(false), 150)}
        onChange={(event) => {
          selectedRef.current = "";
          setQuery(event.target.value);
          onChange("");
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        placeholder="Pesquisar código postal ou localidade..."
        readOnly={readOnly}
        required={required}
        type="search"
        value={query}
      />
      <a aria-label="Gerir códigos postais" href="/configuracao/tabelas/codpostal?mode=new" rel="noopener noreferrer" target="_blank" title="Gerir códigos postais"><i aria-hidden="true" className="pi pi-external-link" /></a>
    </div>
    {open && !disabled && !readOnly && <div className="fac-reference-lookup-results" role="listbox">
      {loading && <span className="fac-reference-lookup-status">A pesquisar...</span>}
      {!loading && query.trim() && results.length === 0 && <span className="fac-reference-lookup-status">Sem resultados.</span>}
      {!loading && results.map((postalCode) => <button key={postalCode.id} onMouseDown={(event) => event.preventDefault()} onClick={() => choose(postalCode)} role="option" type="button">{formatPostalCode(postalCode)}</button>)}
    </div>}
  </label>;
}

async function getPostalCode(value: string) {
  const response = await apiFetch(`/api/codpostal/${encodeURIComponent(value)}`);
  if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);
  return response.json() as Promise<PostalCode>;
}

function formatPostalCode(postalCode: PostalCode) {
  return `${postalCode.id} · ${postalCode.nome}`;
}
