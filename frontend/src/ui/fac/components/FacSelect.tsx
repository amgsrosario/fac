import { Dropdown, DropdownChangeEvent } from "primereact/dropdown";

export type FacSelectOption = {
  label: string;
  value: string;
};

export type FacSelectProps = {
  className?: string;
  disabled?: boolean;
  label?: string;
  onChange: (value: string | null) => void;
  options: FacSelectOption[];
  placeholder?: string;
  value: string | null;
};

export function FacSelect({
  className = "",
  disabled = false,
  label,
  onChange,
  options,
  placeholder = "Selecionar",
  value
}: FacSelectProps) {
  const select = (
    <Dropdown
      className={`fac-select ${disabled ? "fac-disabled" : ""} ${className}`.trim()}
      disabled={disabled}
      onChange={(event: DropdownChangeEvent) => onChange(event.value ?? null)}
      optionLabel="label"
      optionValue="value"
      options={options}
      panelClassName="fac-select-panel"
      itemTemplate={(option: FacSelectOption) => <div className="fac-select-item">{option.label}</div>}
      placeholder={placeholder}
      value={value}
    />
  );
  if (!label) return select;
  return <label className="fac-field-stack"><span>{label}</span>{select}</label>;
}
