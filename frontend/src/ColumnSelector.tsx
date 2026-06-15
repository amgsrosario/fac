import { useEffect, useState } from "react";

export type ConfigurableColumn = {
  key: string;
  label: string;
  visible: boolean;
};

export function useConfiguredColumns(storageKey: string, defaults: ConfigurableColumn[]) {
  const [columns, setColumns] = useState<ConfigurableColumn[]>(() => loadColumns(storageKey, defaults));

  useEffect(() => {
    setColumns(loadColumns(storageKey, defaults));
  }, [storageKey, defaults]);

  useEffect(() => {
    window.localStorage.setItem(storageKey, JSON.stringify(columns));
  }, [columns, storageKey]);

  function toggleColumn(key: string) {
    setColumns((current) => {
      const target = current.find((column) => column.key === key);
      if (target?.visible && current.filter((column) => column.visible).length === 1) return current;
      return current.map((column) => column.key === key ? { ...column, visible: !column.visible } : column);
    });
  }

  function moveColumn(index: number, direction: -1 | 1) {
    setColumns((current) => {
      const destination = index + direction;
      if (destination < 0 || destination >= current.length) return current;
      const next = [...current];
      [next[index], next[destination]] = [next[destination], next[index]];
      return next;
    });
  }

  return {
    columns,
    visibleColumns: columns.filter((column) => column.visible),
    toggleColumn,
    moveColumn,
    resetColumns: () => setColumns(defaults)
  };
}

export function ColumnSelector({
  columns,
  open,
  onMove,
  onReset,
  onToggle
}: {
  columns: ConfigurableColumn[];
  open: boolean;
  onMove: (index: number, direction: -1 | 1) => void;
  onReset: () => void;
  onToggle: (key: string) => void;
}) {
  if (!open) return null;
  return <div className="fac-column-editor">
    <div className="fac-column-editor-header">
      <div><strong>Colunas da listagem</strong><span>Marca os campos visiveis e define a respetiva ordem.</span></div>
      <button className="fac-ghost-button" onClick={onReset} type="button">Repor base</button>
    </div>
    <div className="fac-column-list">
      {columns.map((column, index) => <div className="fac-column-item" key={column.key}>
        <label><input checked={column.visible} onChange={() => onToggle(column.key)} type="checkbox" />{column.label}</label>
        <div className="fac-column-order">
          <button aria-label={`Subir ${column.label}`} disabled={index === 0} onClick={() => onMove(index, -1)} type="button">↑</button>
          <button aria-label={`Descer ${column.label}`} disabled={index === columns.length - 1} onClick={() => onMove(index, 1)} type="button">↓</button>
        </div>
      </div>)}
    </div>
  </div>;
}

function loadColumns(storageKey: string, defaults: ConfigurableColumn[]) {
  try {
    const stored = JSON.parse(window.localStorage.getItem(storageKey) ?? "null") as ConfigurableColumn[] | null;
    if (!Array.isArray(stored)) return defaults;
    const known = new Map(defaults.map((column) => [column.key, column]));
    const valid = stored.filter((column) => known.has(column.key))
      .map((column) => ({ ...known.get(column.key)!, visible: Boolean(column.visible) }));
    for (const column of defaults) if (!valid.some((item) => item.key === column.key)) valid.push(column);
    return valid.some((column) => column.visible) ? valid : defaults;
  } catch {
    return defaults;
  }
}
