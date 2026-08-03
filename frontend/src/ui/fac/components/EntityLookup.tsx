import { type CSSProperties, KeyboardEvent, MouseEvent as ReactMouseEvent, ReactNode, useEffect, useId, useMemo, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable, DataTableSelectionSingleChangeEvent } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { InputText } from "primereact/inputtext";
import { OverlayPanel } from "primereact/overlaypanel";
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
  autoFocusRequest?: number;
  clearable?: boolean;
  closeRequest?: number;
  disabled?: boolean;
  label?: string;
  optionLabel: (row: T) => ReactNode;
  optionMeta?: (row: T) => ReactNode;
  onClear?: () => void;
  onQueryChange?: (query: string) => void;
  onSelect: (row: T) => void;
  placeholder: string;
  showAllSuggestionsOnEmptyQuery?: boolean;
  selection?: T | null;
  suggestionLimit?: number;
  valueLabel?: string;
};

const SEARCH_HELP = "^ começa por · * contém · = igual a · ! não contém · $ termina em";
const SEARCH_OPERATORS = [
  ["^", "começa por"],
  ["*", "contém"],
  ["=", "igual a"],
  ["!", "não contém"],
  ["$", "termina em"]
] as const;
const SUGGESTIONS_VIEWPORT_MARGIN = 8;
const SUGGESTIONS_MAX_HEIGHT_REM = 28;

type SuggestionsStyle = CSSProperties & {
  "--fac-lookup-suggestions-max-height"?: string;
};

export function EntityLookupField<T extends object>({
  autoFocusRequest = 0,
  clearable = true,
  closeRequest = 0,
  disabled = false,
  label,
  onClear,
  onQueryChange,
  optionLabel,
  optionMeta,
  placeholder,
  showAllSuggestionsOnEmptyQuery = false,
  selection = null,
  suggestionLimit = 10,
  valueLabel,
  ...dialogProps
}: EntityLookupFieldProps<T>) {
  const [visible, setVisible] = useState(false);
  const [query, setQuery] = useState("");
  const [suggestionsOpen, setSuggestionsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);
  const [suggestionsPlacement, setSuggestionsPlacement] = useState<"below" | "above">("below");
  const [suggestionsStyle, setSuggestionsStyle] = useState<SuggestionsStyle | null>(null);
  const suggestionsId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const rootRef = useRef<HTMLDivElement>(null);
  const suggestionsRef = useRef<HTMLDivElement>(null);
  const suppressNextFocusSuggestionsRef = useRef(false);
  const canUseDocument = typeof document !== "undefined";
  const text = valueLabel || "";
  const searchFields = useMemo(
    () => dialogProps.searchFields ?? searchFieldsFromColumns(dialogProps.columns, dialogProps.globalFilterFields),
    [dialogProps.columns, dialogProps.globalFilterFields, dialogProps.searchFields]
  );
  const suggestions = useMemo(() => {
    const trimmed = query.trim();
    if (!trimmed) return showAllSuggestionsOnEmptyQuery ? dialogProps.value.slice(0, suggestionLimit) : [];
    return dialogProps.value.filter((row) => matchEntityQuery(row, trimmed, searchFields)).slice(0, suggestionLimit);
  }, [dialogProps.value, query, searchFields, showAllSuggestionsOnEmptyQuery, suggestionLimit]);

  useEffect(() => {
    if (selection) {
      setQuery("");
      setSuggestionsOpen(false);
    }
  }, [selection]);

  useEffect(() => {
    if (autoFocusRequest === 0) return;
    const frame = window.requestAnimationFrame(() => {
      suppressNextFocusSuggestionsRef.current = true;
      if (inputRef.current) HTMLElement.prototype.focus.call(inputRef.current);
    });
    return () => window.cancelAnimationFrame(frame);
  }, [autoFocusRequest]);

  useEffect(() => {
    if (closeRequest === 0) return;
    let cancelled = false;
    setQuery("");
    setSuggestionsOpen(false);
    const close = (attempts: number) => window.requestAnimationFrame(() => {
      if (cancelled) return;
      setQuery("");
      setSuggestionsOpen(false);
      if (attempts > 0) close(attempts - 1);
    });
    close(20);
    return () => { cancelled = true; };
  }, [closeRequest]);

  useEffect(() => {
    function onPointerDown(event: PointerEvent) {
      const target = event.target as Node;
      if (!rootRef.current?.contains(target) && !suggestionsRef.current?.contains(target)) setSuggestionsOpen(false);
    }
    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, []);

  useEffect(() => {
    if (!canUseDocument || !suggestionsOpen || suggestions.length === 0) {
      setSuggestionsPlacement("below");
      setSuggestionsStyle(null);
      return;
    }
    const updateSuggestionsLayout = () => {
      const rootRect = rootRef.current?.getBoundingClientRect();
      const suggestionsElement = suggestionsRef.current;
      if (!rootRect || !suggestionsElement) return;
      const rootFontSize = Number.parseFloat(window.getComputedStyle(document.documentElement).fontSize) || 16;
      const configuredMaxHeight = SUGGESTIONS_MAX_HEIGHT_REM * rootFontSize;
      const targetHeight = Math.min(suggestionsElement.scrollHeight, configuredMaxHeight);
      const belowSpace = Math.max(0, window.innerHeight - rootRect.bottom - SUGGESTIONS_VIEWPORT_MARGIN);
      const aboveSpace = Math.max(0, rootRect.top - SUGGESTIONS_VIEWPORT_MARGIN);
      const nextPlacement = belowSpace < targetHeight && aboveSpace > belowSpace ? "above" : "below";
      const availableSpace = nextPlacement === "above" ? aboveSpace : belowSpace;
      const viewportWidth = Math.max(0, window.innerWidth - SUGGESTIONS_VIEWPORT_MARGIN * 2);
      const width = Math.min(rootRect.width, viewportWidth);
      const left = Math.min(Math.max(SUGGESTIONS_VIEWPORT_MARGIN, rootRect.left), Math.max(SUGGESTIONS_VIEWPORT_MARGIN, window.innerWidth - width - SUGGESTIONS_VIEWPORT_MARGIN));
      const maxHeight = Math.max(0, Math.min(configuredMaxHeight, availableSpace));
      const renderedHeight = Math.min(targetHeight, maxHeight);
      const top = nextPlacement === "above" ? Math.max(SUGGESTIONS_VIEWPORT_MARGIN, rootRect.top - renderedHeight - 4) : Math.min(window.innerHeight - SUGGESTIONS_VIEWPORT_MARGIN, rootRect.bottom + 4);
      setSuggestionsPlacement(nextPlacement);
      setSuggestionsStyle({
        "--fac-lookup-suggestions-max-height": `${maxHeight}px`,
        left: `${left}px`,
        top: `${top}px`,
        width: `${width}px`
      });
    };
    const frame = window.requestAnimationFrame(updateSuggestionsLayout);
    window.addEventListener("resize", updateSuggestionsLayout);
    window.addEventListener("scroll", updateSuggestionsLayout, true);
    return () => {
      window.cancelAnimationFrame(frame);
      window.removeEventListener("resize", updateSuggestionsLayout);
      window.removeEventListener("scroll", updateSuggestionsLayout, true);
    };
  }, [canUseDocument, query, suggestions.length, suggestionsOpen]);

  function hide() {
    setVisible(false);
    window.setTimeout(() => inputRef.current?.focus(), 0);
  }

  function selectRow(row: T) {
    dialogProps.onSelect(row);
    setQuery("");
    onQueryChange?.("");
    setSuggestionsOpen(false);
  }

  function openDialog() {
    setSuggestionsOpen(false);
    setVisible(true);
  }

  function clearSelection() {
    setQuery("");
    onQueryChange?.("");
    setSuggestionsOpen(showAllSuggestionsOnEmptyQuery);
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

  const suggestionsList =
    canUseDocument && suggestionsOpen && suggestions.length > 0
      ? createPortal(
          <div
            className={`fac-lookup-suggestions ${suggestionsPlacement === "above" ? "above" : ""}`}
            id={suggestionsId}
            ref={suggestionsRef}
            role="listbox"
            style={suggestionsStyle ?? { left: 0, top: 0, visibility: "hidden", width: rootRef.current?.getBoundingClientRect().width ?? undefined }}
          >
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
          </div>,
          document.body
        )
      : null;

  const control = (
    <div className="fac-lookup-wrap" ref={rootRef}>
      <div className={`fac-lookup-field ${disabled ? "disabled" : ""}`}>
        <InputText
          aria-label={label ?? placeholder}
          aria-controls={suggestionsOpen && suggestions.length > 0 ? suggestionsId : undefined}
          aria-expanded={suggestionsOpen && suggestions.length > 0}
          className="fac-lookup-input"
          disabled={disabled}
          onChange={(event) => {
            const nextQuery = event.target.value;
            setQuery(nextQuery);
            onQueryChange?.(nextQuery);
            setActiveIndex(0);
            setSuggestionsOpen(showAllSuggestionsOnEmptyQuery || Boolean(nextQuery.trim()));
          }}
          onFocus={() => {
            if (suppressNextFocusSuggestionsRef.current) {
              suppressNextFocusSuggestionsRef.current = false;
              return;
            }
            if (showAllSuggestionsOnEmptyQuery || query.trim()) setSuggestionsOpen(true);
          }}
          onKeyDown={handleKeyDown}
          placeholder={text || placeholder}
          ref={inputRef}
          title={SEARCH_HELP}
          value={query}
        />
        <button aria-label="Ajuda da pesquisa" className="fac-lookup-icon" disabled={disabled} title={`${SEARCH_HELP}\nExemplos: ^fin · NIF:=516281950 · descrição:*mensal`} type="button">
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
    </div>
  );

  return (
    <>
      {label ? <label className="fac-field-stack"><span>{label}</span>{control}</label> : control}
      {suggestionsList}
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
  const [filtersVisible, setFiltersVisible] = useState(false);
  const visibleColumns = columns.filter((column) => visibleFields.includes(column.field));
  const activeFilterCount = Object.values(columnFilters).filter((value) => value.trim()).length;
  const filteredValue = useMemo(
    () => value.filter((row) => matchEntityQuery(row, globalFilter, searchFields) && visibleColumns.every((column) => matchColumnFilter(row, column, columnFilters[column.field]))),
    [columnFilters, globalFilter, searchFields, value, visibleColumns]
  );
  const searchRef = useRef<HTMLInputElement>(null);
  const helpPanelRef = useRef<OverlayPanel>(null);
  const columnsPanelRef = useRef<OverlayPanel>(null);
  const searchPlaceholder = useMemo(() => searchPlaceholderFromColumns(columns), [columns]);

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

  function toggleColumn(field: string, checked: boolean) {
    if (!checked && requiredFields.includes(field)) return;
    const nextFields = checked ? [...visibleFields, field] : visibleFields.filter((current) => current !== field);
    changeColumns(nextFields);
  }

  function resetColumns() {
    setVisibleFields(defaultVisibleFields);
  }

  function clearColumnFilters() {
    setColumnFilters({});
  }

  function toggleHelpPanel(event: ReactMouseEvent<HTMLButtonElement>) {
    helpPanelRef.current?.toggle(event);
    columnsPanelRef.current?.hide();
  }

  function toggleColumnsPanel(event: ReactMouseEvent<HTMLButtonElement>) {
    columnsPanelRef.current?.toggle(event);
    helpPanelRef.current?.hide();
  }

  function confirmSelection(row = selected) {
    if (row) onSelect(row);
  }

  const footer = (
    <div className="fac-entity-lookup-footer">
      <Button className="fac-button fac-button-ghost" label="Cancelar" onClick={onHide} type="button" />
      <Button className="fac-button fac-button-primary" disabled={!selected} icon="pi pi-check" label="Selecionar" onClick={() => confirmSelection()} type="button" />
    </div>
  );
  const appendTarget = typeof document === "undefined" ? undefined : document.body;

  return (
    <Dialog
      appendTo={appendTarget}
      blockScroll
      className="fac-entity-lookup-dialog"
      contentClassName="fac-entity-lookup-content"
      draggable={false}
      footer={footer}
      header={title}
      maskClassName="fac-entity-lookup-mask"
      onHide={onHide}
      modal
      resizable={false}
      style={{ width: "min(1180px, 94vw)" }}
      visible={visible}
    >
      <div className="fac-entity-lookup-toolbar">
        <span className="p-input-icon-left fac-entity-lookup-search">
          <i className="pi pi-search" aria-hidden="true" />
          <InputText onChange={(event) => setGlobalFilter(event.target.value)} placeholder={searchPlaceholder} ref={searchRef} title={SEARCH_HELP} value={globalFilter} />
          {globalFilter && (
            <button aria-label="Limpar pesquisa" className="fac-entity-lookup-clear" onClick={() => setGlobalFilter("")} type="button">
              <i className="pi pi-times" aria-hidden="true" />
            </button>
          )}
        </span>
        <div className="fac-entity-lookup-toolbar-actions">
          <Button className={filtersVisible ? "fac-button fac-button-secondary active" : "fac-button fac-button-secondary"} icon="pi pi-filter" label={activeFilterCount ? `Filtros (${activeFilterCount})` : "Filtros"} onClick={() => setFiltersVisible((current) => !current)} type="button" />
          {activeFilterCount > 0 && <Button aria-label="Limpar filtros" className="fac-button fac-button-ghost fac-entity-lookup-icon-button" icon="pi pi-filter-slash" onClick={clearColumnFilters} type="button" />}
          <Button className="fac-button fac-button-secondary" icon="pi pi-table" label="Colunas" onClick={toggleColumnsPanel} type="button" />
          <Button aria-label="Ajuda da pesquisa" className="fac-button fac-button-ghost fac-entity-lookup-icon-button" icon="pi pi-question-circle" onClick={toggleHelpPanel} type="button" />
        </div>
      </div>
      <OverlayPanel appendTo={appendTarget} className="fac-entity-lookup-help-panel" ref={helpPanelRef}>
        <div className="fac-entity-lookup-panel-title">Operadores de pesquisa</div>
        <dl>
          {SEARCH_OPERATORS.map(([operator, label]) => (
            <div key={operator}><dt>{operator}</dt><dd>{label}</dd></div>
          ))}
        </dl>
        <div className="fac-entity-lookup-panel-examples">
          <span>Exemplos</span>
          <code>^fin</code>
          <code>NIF:=516281950</code>
          <code>descrição:*mensal</code>
        </div>
      </OverlayPanel>
      <OverlayPanel appendTo={appendTarget} className="fac-entity-lookup-column-panel" ref={columnsPanelRef}>
        <div className="fac-entity-lookup-panel-title">Colunas visíveis</div>
        <div className="fac-entity-lookup-column-list">
          {columns.map((column) => {
            const field = String(column.field);
            const checked = visibleFields.includes(field);
            const disabled = requiredFields.includes(field);
            return (
              <label key={field}>
                <input checked={checked} disabled={disabled} onChange={(event) => toggleColumn(field, event.target.checked)} type="checkbox" />
                <span>{column.header}</span>
              </label>
            );
          })}
        </div>
        <button className="fac-entity-lookup-panel-reset" onClick={resetColumns} type="button">Restaurar predefinição</button>
      </OverlayPanel>
      <div className="fac-entity-lookup-table">
      <DataTable
        className={filtersVisible ? "fac-entity-lookup-datatable filters-visible" : "fac-entity-lookup-datatable"}
        dataKey={dataKey}
        emptyMessage={emptyMessage}
        filterDisplay={filtersVisible ? "row" : undefined}
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
        scrollHeight="520px"
        selection={selected}
        selectionMode="single"
        sortMode="multiple"
        tabIndex={0}
        tableStyle={{ minWidth: "780px" }}
        value={filteredValue}
      >
        {visibleColumns.map((column) => (
          <Column
            body={column.body ? (row) => column.body?.(row as T) : undefined}
            field={column.field}
            filter={filtersVisible && column.filterable}
            filterElement={() => (
              <InputText
                className="fac-entity-lookup-column-filter"
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
      </div>
    </Dialog>
  );
}

function searchPlaceholderFromColumns<T extends object>(columns: EntityLookupColumn<T>[]) {
  const labels = columns
    .filter((column) => column.globalSearch || column.defaultVisible)
    .slice(0, 4)
    .map((column) => column.header.toLowerCase());
  if (labels.length === 0) return "Pesquisar";
  if (labels.length === 1) return `Pesquisar por ${labels[0]}`;
  return `Pesquisar por ${labels.slice(0, -1).join(", ")} ou ${labels[labels.length - 1]}`;
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
