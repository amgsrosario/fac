import { KeyboardEvent, ReactNode, useEffect, useMemo, useRef, useState } from "react";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable, DataTableSelectionSingleChangeEvent } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { InputText } from "primereact/inputtext";
import { MultiSelect } from "primereact/multiselect";
import "./EntityLookup.css";

export type EntityLookupColumn<T extends object> = {
  body?: (row: T) => ReactNode;
  defaultVisible?: boolean;
  field: Extract<keyof T, string> | string;
  filterable?: boolean;
  globalSearch?: boolean;
  header: string;
  required?: boolean;
  sortable?: boolean;
  style?: React.CSSProperties;
  width?: string;
};

export type EntityLookupSearchField<T extends object> = {
  aliases?: string[];
  fields?: Array<Extract<keyof T, string> | string>;
  getValue?: (row: T) => unknown;
  key: string;
};

type EntityLookupDialogProps<T extends object> = {
  columns: EntityLookupColumn<T>[];
  dataKey: Extract<keyof T, string>;
  emptyMessage: string;
  globalFilterFields?: Array<Extract<keyof T, string> | string>;
  initialQuery?: string;
  loading?: boolean;
  onHide: () => void;
  onSelect: (row: T) => void;
  preferenceKey?: string;
  searchFields?: EntityLookupSearchField<T>[];
  selection?: T | null;
  title: string;
  value: T[];
  visible: boolean;
};

type EntityLookupFieldProps<T extends object> = Omit<EntityLookupDialogProps<T>, "onHide" | "onSelect" | "selection" | "visible"> & {
  clearable?: boolean;
  disabled?: boolean;
  label?: string;
  optionLabel: (row: T) => ReactNode;
  optionMeta?: (row: T) => ReactNode;
  onClear?: () => void;
  onSelect: (row: T) => void;
  placeholder: string;
  selection?: T | null;
  suggestionLimit?: number;
  valueLabel?: string;
};

const SEARCH_HELP = "^ começa por · * contém · = igual a · ! não contém · $ termina em";

export function EntityLookupField<T extends object>({
  clearable = true,
  disabled = false,
  label,
  onClear,
  optionLabel,
  optionMeta,
  placeholder,
  selection = null,
  suggestionLimit = 10,
  valueLabel,
  ...dialogProps
}: EntityLookupFieldProps<T>) {
  const [visible, setVisible] = useState(false);
  const [query, setQuery] = useState("");
  const [suggestionsOpen, setSuggestionsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const rootRef = useRef<HTMLDivElement>(null);
  const text = valueLabel || "";
  const searchFields = useMemo(
    () => dialogProps.searchFields ?? searchFieldsFromColumns(dialogProps.columns, dialogProps.globalFilterFields),
    [dialogProps.columns, dialogProps.globalFilterFields, dialogProps.searchFields]
  );
  const suggestions = useMemo(() => {
    const trimmed = query.trim();
    if (!trimmed) return [];
    return dialogProps.value.filter((row) => matchEntityQuery(row, trimmed, searchFields)).slice(0, suggestionLimit);
  }, [dialogProps.value, query, searchFields, suggestionLimit]);

  useEffect(() => {
    if (selection) {
      setQuery("");
      setSuggestionsOpen(false);
    }
  }, [selection]);

  useEffect(() => {
    function onPointerDown(event: PointerEvent) {
      if (!rootRef.current?.contains(event.target as Node)) setSuggestionsOpen(false);
    }
    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, []);

  function hide() {
    setVisible(false);
    window.setTimeout(() => inputRef.current?.focus(), 0);
  }

  function selectRow(row: T) {
    dialogProps.onSelect(row);
    setQuery("");
    setSuggestionsOpen(false);
  }

  function openDialog() {
    setSuggestionsOpen(false);
    setVisible(true);
  }

  function clearSelection() {
    setQuery("");
    setSuggestionsOpen(false);
    onClear?.();
    window.setTimeout(() => inputRef.current?.focus(), 0);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "ArrowDown" && suggestions.length > 0) {
      event.preventDefault();
      setSuggestionsOpen(true);
      setActiveIndex((current) => Math.min(current + 1, suggestions.length - 1));
      return;
    }
    if (event.key === "ArrowUp" && suggestions.length > 0) {
      event.preventDefault();
      setActiveIndex((current) => Math.max(current - 1, 0));
      return;
    }
    if (event.key === "Enter") {
      if (suggestionsOpen && suggestions[activeIndex]) {
        event.preventDefault();
        selectRow(suggestions[activeIndex]);
      } else if (query.trim()) {
        event.preventDefault();
        openDialog();
      }
      return;
    }
    if (event.key === "Escape") {
      setSuggestionsOpen(false);
    }
  }

  const control = (
    <div className="fac-lookup-wrap" ref={rootRef}>
      <div className={`fac-lookup-field ${disabled ? "disabled" : ""}`}>
        <InputText
          aria-label={label ?? placeholder}
          className="fac-lookup-input"
          disabled={disabled}
          onChange={(event) => {
            setQuery(event.target.value);
            setActiveIndex(0);
            setSuggestionsOpen(Boolean(event.target.value.trim()));
          }}
          onFocus={() => query.trim() && setSuggestionsOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder={text || placeholder}
          ref={inputRef}
          title={SEARCH_HELP}
          value={query}
        />
        <button aria-label="Ajuda da pesquisa" className="fac-lookup-icon" disabled={disabled} title={`${SEARCH_HELP}\nExemplos: ^fin · nif:=516281950 · descricao:*mensal`} type="button">
          <i className="pi pi-info-circle" aria-hidden="true" />
        </button>
        <button aria-label="Pesquisa avançada" className="fac-lookup-icon" disabled={disabled} onClick={openDialog} title="Pesquisa avançada" type="button">
          <i className="pi pi-search" aria-hidden="true" />
        </button>
        {clearable && (selection || query) && (
          <button aria-label="Limpar" className="fac-lookup-icon" disabled={disabled} onClick={clearSelection} type="button">
            <i className="pi pi-times" aria-hidden="true" />
          </button>
        )}
      </div>
      {suggestionsOpen && suggestions.length > 0 && (
        <div className="fac-lookup-suggestions" role="listbox">
          {suggestions.map((row, index) => (
            <button
              aria-selected={index === activeIndex}
              className={index === activeIndex ? "active" : ""}
              key={entityKey(row, dialogProps.dataKey)}
              onMouseEnter={() => setActiveIndex(index)}
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => selectRow(row)}
              role="option"
              type="button"
            >
              <span>{optionLabel(row)}</span>
              {optionMeta && <small>{optionMeta(row)}</small>}
            </button>
          ))}
        </div>
      )}
    </div>
  );

  return (
    <>
      {label ? <label className="fac-field-stack"><span>{label}</span>{control}</label> : control}
      <EntityLookupDialog
        {...dialogProps}
        onHide={hide}
        initialQuery={query}
        onSelect={(row) => {
          selectRow(row);
          hide();
        }}
        searchFields={searchFields}
        selection={selection}
        visible={visible}
      />
    </>
  );
}

export function EntityLookupDialog<T extends object>({
  columns,
  dataKey,
  emptyMessage,
  globalFilterFields,
  loading = false,
  onHide,
  onSelect,
  preferenceKey,
  searchFields: providedSearchFields,
  selection = null,
  title,
  value,
  visible,
  initialQuery = ""
}: EntityLookupDialogProps<T>) {
  const defaultVisibleFields = useMemo(
    () => columns.filter((column) => column.defaultVisible || column.required).map((column) => column.field),
    [columns]
  );
  const requiredFields = useMemo(() => columns.filter((column) => column.required).map((column) => column.field), [columns]);
  const searchFields = useMemo(
    () => providedSearchFields ?? searchFieldsFromColumns(columns, globalFilterFields),
    [columns, globalFilterFields, providedSearchFields]
  );
  const [selected, setSelected] = useState<T | null>(selection);
  const [globalFilter, setGlobalFilter] = useState("");
  const [visibleFields, setVisibleFields] = useState<string[]>(() => readVisibleFields(preferenceKey, defaultVisibleFields, columns));
  const [columnFilters, setColumnFilters] = useState<Record<string, string>>({});
  const visibleColumns = columns.filter((column) => visibleFields.includes(column.field));
  const filteredValue = useMemo(
    () => value.filter((row) => matchEntityQuery(row, globalFilter, searchFields) && visibleColumns.every((column) => matchColumnFilter(row, column, columnFilters[column.field]))),
    [columnFilters, globalFilter, searchFields, value, visibleColumns]
  );
  const searchRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!visible) return;
    setSelected(selection);
    setGlobalFilter(initialQuery);
    window.setTimeout(() => searchRef.current?.focus(), 120);
  }, [initialQuery, selection, visible]);

  useEffect(() => {
    if (!preferenceKey) return;
    window.localStorage.setItem(preferenceKey, JSON.stringify(visibleFields));
  }, [preferenceKey, visibleFields]);

  function changeColumns(nextFields: string[]) {
    const required = requiredFields.filter((field) => !nextFields.includes(field));
    const merged = Array.from(new Set([...required, ...nextFields]));
    setVisibleFields(merged.length > 0 ? merged : defaultVisibleFields);
  }

  function confirmSelection(row = selected) {
    if (row) onSelect(row);
  }

  const footer = (
    <div className="fac-lookup-footer">
      <Button className="fac-button fac-button-ghost" label="Cancelar" onClick={onHide} type="button" />
      <Button className="fac-button fac-button-primary" disabled={!selected} icon="pi pi-check" label="Selecionar" onClick={() => confirmSelection()} type="button" />
    </div>
  );

  return (
    <Dialog
      className="fac-lookup-dialog"
      footer={footer}
      header={title}
      onHide={onHide}
      visible={visible}
      modal
      style={{ width: "min(980px, calc(100vw - 32px))" }}
    >
      <div className="fac-lookup-toolbar">
        <span className="p-input-icon-left fac-lookup-search">
          <i className="pi pi-search" aria-hidden="true" />
          <InputText onChange={(event) => setGlobalFilter(event.target.value)} placeholder="Pesquisar" ref={searchRef} title={SEARCH_HELP} value={globalFilter} />
        </span>
        <span className="fac-lookup-help" title={`${SEARCH_HELP}\nExemplos: ^fin · nif:=516281950 · descricao:*mensal`}>
          <i className="pi pi-info-circle" aria-hidden="true" />
        </span>
        <MultiSelect
          className="fac-lookup-columns"
          display="chip"
          onChange={(event) => changeColumns(event.value)}
          optionDisabled={(option) => requiredFields.includes(option.value)}
          optionLabel="label"
          optionValue="value"
          options={columns.map((column) => ({ label: column.header, value: column.field }))}
          placeholder="Colunas"
          value={visibleFields}
        />
      </div>
      <DataTable
        className="fac-lookup-table"
        dataKey={dataKey}
        emptyMessage={emptyMessage}
        filterDisplay="row"
        loading={loading}
        onKeyDown={(event) => {
          if (event.key === "Enter") {
            event.preventDefault();
            confirmSelection();
          }
        }}
        onRowClick={(event) => setSelected(event.data as T)}
        onRowDoubleClick={(event) => confirmSelection(event.data as T)}
        onSelectionChange={(event: DataTableSelectionSingleChangeEvent<T[]>) => setSelected((event.value as T | null) ?? null)}
        removableSort
        scrollable
        scrollHeight="420px"
        selection={selected}
        selectionMode="single"
        sortMode="multiple"
        tabIndex={0}
        value={filteredValue}
      >
        {visibleColumns.map((column) => (
          <Column
            body={column.body ? (row) => column.body?.(row as T) : undefined}
            field={column.field}
            filter={column.filterable}
            filterElement={() => (
              <InputText
                className="fac-lookup-column-filter"
                onChange={(event) => setColumnFilters((current) => ({ ...current, [column.field]: event.target.value }))}
                placeholder={column.header}
                title={SEARCH_HELP}
                value={columnFilters[column.field] ?? ""}
              />
            )}
            header={column.header}
            key={column.field}
            showFilterMenu={false}
            sortable={column.sortable}
            style={{ width: column.width, ...column.style }}
          />
        ))}
      </DataTable>
    </Dialog>
  );
}

function readVisibleFields<T extends object>(preferenceKey: string | undefined, fallback: string[], columns: EntityLookupColumn<T>[]) {
  if (!preferenceKey) return fallback;
  try {
    const stored = JSON.parse(window.localStorage.getItem(preferenceKey) || "[]");
    const allowed = new Set(columns.map((column) => column.field));
    const fields = Array.isArray(stored) ? stored.filter((field) => allowed.has(field)) : [];
    return fields.length > 0 ? fields : fallback;
  } catch {
    return fallback;
  }
}

function searchFieldsFromColumns<T extends object>(columns: EntityLookupColumn<T>[], globalFilterFields?: Array<Extract<keyof T, string> | string>) {
  const fields = globalFilterFields ?? columns.filter((column) => column.globalSearch).map((column) => column.field);
  return fields.map((field) => ({ fields: [field], key: String(field) }));
}

function entityKey<T extends object>(row: T, dataKey: Extract<keyof T, string>) {
  return String(row[dataKey] ?? "");
}

function matchColumnFilter<T extends object>(row: T, column: EntityLookupColumn<T>, query?: string) {
  if (!query?.trim()) return true;
  return matchOperator(getFieldValue(row, column.field), query);
}

export function matchEntityQuery<T extends object>(row: T, query: string, fields: EntityLookupSearchField<T>[]) {
  const trimmed = query.trim();
  if (!trimmed) return true;
  const { global, qualified } = parseEntityQuery(trimmed, fields);
  return qualified.every(({ field, term }) => matchSearchField(row, term, field))
    && (!global || fields.some((field) => matchSearchField(row, global, field)));
}

function parseEntityQuery<T extends object>(query: string, fields: EntityLookupSearchField<T>[]) {
  const aliases = new Map<string, EntityLookupSearchField<T>>();
  fields.forEach((field) => [field.key, ...(field.aliases ?? [])].forEach((alias) => aliases.set(normalizeText(alias), field)));
  const tokens = query.split(/\s+/).filter(Boolean);
  const qualified: Array<{ field: EntityLookupSearchField<T>; term: string }> = [];
  const globalParts: string[] = [];
  tokens.forEach((token) => {
    const index = token.indexOf(":");
    if (index > 0) {
      const maybeField = aliases.get(normalizeText(token.slice(0, index)));
      if (maybeField) {
        qualified.push({ field: maybeField, term: token.slice(index + 1) });
        return;
      }
    }
    globalParts.push(token);
  });
  return { global: globalParts.join(" "), qualified };
}

function matchSearchField<T extends object>(row: T, query: string, field: EntityLookupSearchField<T>) {
  const values = field.getValue ? [field.getValue(row)] : (field.fields ?? [field.key]).map((key) => getFieldValue(row, key));
  return values.some((value) => matchOperator(value, query));
}

function matchOperator(value: unknown, rawQuery: string) {
  const query = rawQuery.trim();
  if (!query) return true;
  const operator = ["^", "*", "=", "!", "$"].includes(query[0]) ? query[0] : "*";
  const needle = normalizeText(operator === "*" && rawQuery.trim().startsWith("*") ? query.slice(1) : operator === "*" ? query : query.slice(1));
  const haystack = normalizeText(value);
  if (!needle) return true;
  if (operator === "^") return haystack.startsWith(needle);
  if (operator === "=") return haystack === needle;
  if (operator === "!") return !haystack.includes(needle);
  if (operator === "$") return haystack.endsWith(needle);
  return haystack.includes(needle);
}

function getFieldValue<T extends object>(row: T, field: Extract<keyof T, string> | string) {
  return (row as Record<string, unknown>)[String(field)];
}

function normalizeText(value: unknown) {
  return String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .toLowerCase();
}
