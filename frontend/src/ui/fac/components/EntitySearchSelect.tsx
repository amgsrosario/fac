import { useEffect, useRef } from "react";
import { Dropdown, DropdownChangeEvent } from "primereact/dropdown";

export type EntitySearchOption = {
  label: string;
  value: string;
  secondary?: string;
  compactLabel?: string;
  meta?: string;
};

type EntitySearchSelectProps = {
  className?: string;
  disabled?: boolean;
  invalid?: boolean;
  label?: string;
  loading?: boolean;
  onChange: (value: string | null) => void;
  openOnMount?: boolean;
  options: EntitySearchOption[];
  placeholder?: string;
  value: string | null;
};

export function EntitySearchSelect({
  className = "",
  disabled = false,
  invalid = false,
  label,
  loading = false,
  onChange,
  openOnMount = false,
  options,
  placeholder = "Pesquisar",
  value
}: EntitySearchSelectProps) {
  const dropdownRef = useRef<Dropdown>(null);

  useEffect(() => {
    if (!openOnMount || disabled) return;
    const timeoutId = window.setTimeout(() => dropdownRef.current?.show(), 250);
    return () => window.clearTimeout(timeoutId);
  }, [disabled, openOnMount]);

  const selectedOption = options.find((option) => option.value === value);
  const select = (
    <Dropdown
      appendTo="self"
      className={`fac-entity-search ${disabled ? "fac-disabled" : ""} ${invalid ? "p-invalid" : ""} ${className}`.trim()}
      disabled={disabled}
      emptyFilterMessage="Sem resultados"
      emptyMessage="Sem resultados"
      filter
      filterBy="label,secondary,meta,compactLabel"
      inputId={label ? `entity-search-${label.toLowerCase().replace(/\s+/g, "-")}` : undefined}
      itemTemplate={(option: EntitySearchOption) => (
        <div className="fac-entity-search-item">
          <strong>{option.label}</strong>
          {option.secondary && <span>{option.secondary}</span>}
          {option.meta && <small>{option.meta}</small>}
        </div>
      )}
      loading={loading}
      onChange={(event: DropdownChangeEvent) => onChange(event.value ?? null)}
      optionLabel="label"
      optionValue="value"
      options={options}
      panelClassName="fac-entity-search-panel"
      placeholder={placeholder}
      ref={dropdownRef}
      showClear
      value={value}
      valueTemplate={() => selectedOption?.compactLabel ?? selectedOption?.label ?? placeholder}
    />
  );

  if (!label) return select;
  return <label className="fac-field-stack"><span>{label}</span>{select}</label>;
}
