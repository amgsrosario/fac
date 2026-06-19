type Option = { value: number; label: string };

type Props = {
  allLabel: string;
  options: Option[];
  selectedValues: number[];
  onChange: (values: number[]) => void;
};

export function MultiSelectFilter({ allLabel, options, selectedValues, onChange }: Props) {
  const selected = new Set(selectedValues);
  const summary = selectedValues.length === 0
    ? allLabel
    : selectedValues.length === 1
      ? options.find((option) => option.value === selectedValues[0])?.label ?? "1 selecionado"
      : `${selectedValues.length} clientes selecionados`;

  function toggle(value: number) {
    onChange(selected.has(value)
      ? selectedValues.filter((item) => item !== value)
      : [...selectedValues, value]);
  }

  return <details className="fac-multi-select">
    <summary title={summary}>{summary}</summary>
    <div className="fac-multi-select-menu">
      <button className="fac-multi-select-all" onClick={() => onChange([])} type="button">{allLabel}</button>
      <div className="fac-multi-select-options" role="group" aria-label="Selecionar clientes">
        {options.map((option) => <label key={option.value}>
          <input checked={selected.has(option.value)} onChange={() => toggle(option.value)} type="checkbox"/>
          <span>{option.label}</span>
        </label>)}
      </div>
    </div>
  </details>;
}
