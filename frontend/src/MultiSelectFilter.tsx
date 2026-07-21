import { useEffect, useId, useLayoutEffect, useRef, useState } from "react";
import type { CSSProperties } from "react";

type Option = { value: number; label: string };

type Props = {
  allLabel: string;
  options: Option[];
  selectedValues: number[];
  onChange: (values: number[]) => void;
};

export function MultiSelectFilter({ allLabel, options, selectedValues, onChange }: Props) {
  const id = useId();
  const containerRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [menuStyle, setMenuStyle] = useState<CSSProperties>();
  const selected = new Set(selectedValues);
  const filteredOptions = options.filter((option) => option.label.toLowerCase().includes(search.trim().toLowerCase()));
  const selectedCountLabel = selectedValues.length === 1 ? "1 cliente selecionado" : `${selectedValues.length} clientes selecionados`;
  const summary = selectedValues.length === 0
    ? allLabel
    : selectedValues.length === 1
      ? "1 cliente selecionado"
      : selectedCountLabel;

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

  function toggle(value: number) {
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
    <button aria-expanded={open} aria-haspopup="listbox" className="fac-multi-select-trigger" onClick={() => setOpen((current) => !current)} ref={triggerRef} title={summary} type="button">
      <span>{summary}</span>
    </button>
    {open && <div className="fac-multi-select-menu" style={menuStyle}>
      <div className="fac-multi-select-header">
        <input aria-label="Pesquisar clientes" className="fac-multi-select-search" onChange={(event) => setSearch(event.target.value)} placeholder="Pesquisar clientes" type="search" value={search}/>
        <button aria-pressed={selectedValues.length === 0} className="fac-multi-select-all" onClick={clearSelection} type="button">{allLabel}</button>
      </div>
      <div className="fac-multi-select-options" role="group" aria-label="Selecionar clientes">
        {filteredOptions.map((option) => <label key={option.value} title={option.label}>
          <input checked={selected.has(option.value)} onChange={() => toggle(option.value)} type="checkbox" id={`${id}-${option.value}`}/>
          <span>{option.label}</span>
        </label>)}
        {filteredOptions.length === 0 && <span className="fac-multi-select-empty">Sem clientes encontrados.</span>}
      </div>
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
