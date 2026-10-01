import { useEffect, useId, useLayoutEffect, useRef, useState } from "react";
import type { CSSProperties } from "react";
import { LookupOptions, useRemoteLookup } from "./lookups/useRemoteLookup";

export type MultiSelectOption<Value extends string | number = number> = {
  value: Value;
  label: string;
  searchText?: string;
};

type Props<Value extends string | number = number, Row extends object = object> = {
  remoteLookup?: LookupOptions;
  optionFromRow?: (row: Row) => MultiSelectOption<Value>;
  onRememberRow?: (row: Row) => void;
  label?: string;
  allLabel: string;
  searchPlaceholder?: string;
  options: MultiSelectOption<Value>[];
  selectedValues: Value[];
  onChange: (values: Value[]) => void;
  formatSummary?: (selectedCount: number, allLabel: string) => string;
  disabled?: boolean;
  loading?: boolean;
  emptyMessage?: string;
};

export function MultiSelectFilter<Value extends string | number = number, Row extends object = object>({
  remoteLookup, optionFromRow, onRememberRow,
  label = "cliente",
  allLabel,
  searchPlaceholder,
  options,
  selectedValues,
  onChange,
  formatSummary,
  disabled = false,
  loading = false,
  emptyMessage
}: Props<Value, Row>) {
  const id = useId();
  const containerRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [menuStyle, setMenuStyle] = useState<CSSProperties>();
  const selected = new Set(selectedValues);
  const [page, setPage] = useState(0);
  const remote = useRemoteLookup<Row>(remoteLookup, { search, page }, open);
  const normalizedSearch = search.trim().toLowerCase();
  const filteredOptions = remoteLookup && optionFromRow ? remote.rows.map(optionFromRow) : options.filter((option) => (option.searchText ?? option.label).toLowerCase().includes(normalizedSearch));
  const selectedCountLabel = selectedValues.length === 1 ? `1 ${label} selecionado` : `${selectedValues.length} ${label}s selecionados`;
  const summary = formatSummary?.(selectedValues.length, allLabel) ?? (selectedValues.length === 0 ? allLabel : selectedCountLabel);
  const searchLabel = searchPlaceholder ?? `Pesquisar ${label}`;
  const noResults = emptyMessage ?? `Sem ${label} encontrados.`;

  useEffect(() => {
    if (!open) return;
    function handlePointerDown(event: PointerEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpen(false);
        triggerRef.current?.focus();
      }
    }
    document.addEventListener("pointerdown", handlePointerDown);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("pointerdown", handlePointerDown);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [open]);

  useLayoutEffect(() => {
    if (!open) return;
    function updateMenuPosition() {
      const trigger = triggerRef.current;
      if (!trigger) return;
      const margin = 12;
      const gap = 6;
      const rect = trigger.getBoundingClientRect();
      const width = Math.min(Math.max(rect.width, 430), 560, window.innerWidth - margin * 2);
      const left = Math.min(Math.max(rect.left, margin), window.innerWidth - width - margin);
      const spaceBelow = window.innerHeight - rect.bottom - gap - margin;
      const spaceAbove = rect.top - gap - margin;
      const openAbove = spaceBelow < 260 && spaceAbove > spaceBelow;
      const availableHeight = openAbove ? spaceAbove : spaceBelow;
      const maxHeight = Math.max(180, Math.min(420, availableHeight));
      setMenuStyle({
        left,
        maxHeight,
        top: openAbove ? rect.top - gap - maxHeight : rect.bottom + gap,
        width
      });
    }
    updateMenuPosition();
    window.addEventListener("resize", updateMenuPosition);
    window.addEventListener("scroll", updateMenuPosition, true);
    return () => {
      window.removeEventListener("resize", updateMenuPosition);
      window.removeEventListener("scroll", updateMenuPosition, true);
    };
  }, [open]);

  function toggle(value: Value) {
    const row = remote.rows.find((item) => optionFromRow?.(item).value === value);
    if (row && !selected.has(value)) onRememberRow?.(row);
    onChange(selected.has(value)
      ? selectedValues.filter((item) => item !== value)
      : [...selectedValues, value]);
  }

  function clearSelection() {
    onChange([]);
  }

  function closeMenu() {
    setOpen(false);
    triggerRef.current?.focus();
  }

  return <div className="fac-multi-select" ref={containerRef}>
    <button aria-expanded={open} aria-haspopup="listbox" className="fac-multi-select-trigger" disabled={disabled || (!remoteLookup && loading)} onClick={() => setOpen((current) => !current)} ref={triggerRef} title={summary} type="button">
      <span>{summary}</span>
    </button>
    {open && <div className="fac-multi-select-menu" style={menuStyle}>
      <div className="fac-multi-select-header">
        <input aria-label={searchLabel} className="fac-multi-select-search" onChange={(event) => { setSearch(event.target.value); setPage(0); }} placeholder={searchLabel} type="search" value={search}/>
        <button aria-pressed={selectedValues.length === 0} className="fac-multi-select-all" onClick={clearSelection} type="button">{allLabel}</button>
      </div>
      <div className="fac-multi-select-options" role="group" aria-label={`Selecionar ${label}`}>
        {remote.loading && <span role="status">A pesquisar...</span>}
        {remote.error && <span role="alert">{remote.error} <button type="button" onClick={remote.retry}>Tentar novamente</button></span>}
        {filteredOptions.map((option) => <label key={option.value} title={option.label}>
          <input checked={selected.has(option.value)} onChange={() => toggle(option.value)} type="checkbox" id={`${id}-${option.value}`}/>
          <span>{option.label}</span>
        </label>)}
        {filteredOptions.length === 0 && <span className="fac-multi-select-empty">{loading || remote.loading ? "A carregar..." : noResults}</span>}
      </div>
      {remoteLookup && remote.pages > 1 && <div className="fac-multi-select-footer">
        <button type="button" disabled={page === 0 || remote.loading} onClick={() => setPage((n) => n - 1)}>Anterior</button>
        <span>{page + 1} / {remote.pages}</span>
        <button type="button" disabled={page + 1 >= remote.pages || remote.loading} onClick={() => setPage((n) => n + 1)}>Seguinte</button>
      </div>}
      <div className="fac-multi-select-footer">
        <span>{selectedCountLabel}</span>
        <div>
          <button className="fac-ghost-button" onClick={clearSelection} type="button">Limpar</button>
          <button className="fac-soft-button" onClick={closeMenu} type="button">Concluir</button>
        </div>
      </div>
    </div>}
  </div>;
}
