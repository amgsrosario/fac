import { ReactNode, useEffect, useMemo, useState } from "react";
import { FilterMatchMode } from "primereact/api";
import { Column, ColumnBodyOptions } from "primereact/column";
import { DataTable, DataTableFilterMeta, DataTableFilterMetaData, DataTableProps, DataTableSelectionSingleChangeEvent } from "primereact/datatable";
import { FacEmptyState } from "../feedback";
import "./fac-data-table.css";

type FacFilterMatchMode = DataTableFilterMetaData["matchMode"];
type FacDataTableField<T extends object> = Extract<keyof T, string>;

const DEFAULT_FILTER_MATCH_MODE: FacFilterMatchMode = "contains";

export type FacDataTableColumn<T extends object> = {
  body?: (row: T, options: ColumnBodyOptions) => ReactNode;
  className?: string;
  dataType?: "text" | "numeric" | "date" | "boolean";
  field: FacDataTableField<T>;
  filter?: boolean;
  filterElement?: (options: FacDataTableFilterElementOptions) => ReactNode;
  filterMatchMode?: FacFilterMatchMode;
  filterPlaceholder?: string;
  header: ReactNode;
  sortable?: boolean;
  style?: React.CSSProperties;
};

export type FacDataTableFilterElementOptions = {
  filterApplyCallback: (value: unknown) => void;
  value: unknown;
};

export type FacDataTableProps<T extends object> = {
  ariaLabel: string;
  className?: string;
  columns: FacDataTableColumn<T>[];
  dataKey: string;
  emptyMessage?: string;
  globalFilter?: string;
  globalFilterFields?: Array<FacDataTableField<T>>;
  loading?: boolean;
  onLazyPage?: DataTableProps<T[]>["onPage"];
  onLazySort?: DataTableProps<T[]>["onSort"];
  onLazyFilter?: DataTableProps<T[]>["onFilter"];
  onPage?: DataTableProps<T[]>["onPage"];
  onRowSelect?: (row: T) => void;
  onSelectionChange?: (row: T | null) => void;
  paginator?: boolean;
  rows?: number;
  rowsPerPageOptions?: number[];
  selection?: T | null;
  totalRecords?: number;
  value: T[];
} & Pick<DataTableProps<T[]>, "first" | "lazy" | "sortField" | "sortOrder">;

export function FacDataTable<T extends object>({
  ariaLabel,
  className = "",
  columns,
  dataKey,
  emptyMessage = "Sem resultados.",
  globalFilter = "",
  globalFilterFields = [],
  loading = false,
  onLazyFilter,
  onLazyPage,
  onLazySort,
  onPage,
  onRowSelect,
  onSelectionChange,
  paginator = true,
  rows = 10,
  rowsPerPageOptions = [10, 20, 50],
  selection = null,
  totalRecords,
  value,
  ...tableProps
}: FacDataTableProps<T>) {
  const baseFilters = useMemo(() => createFilters(columns, globalFilterFields), [columns, globalFilterFields]);
  const [filters, setFilters] = useState<DataTableFilterMeta>(baseFilters);

  useEffect(() => {
    setFilters((current) => ({
      ...current,
      global: { value: globalFilter || null, matchMode: DEFAULT_FILTER_MATCH_MODE }
    }));
  }, [globalFilter]);

  return (
    <DataTable
      aria-label={ariaLabel}
      className={`fac-data-table ${className}`.trim()}
      dataKey={dataKey}
      emptyMessage={<FacEmptyState description={emptyMessage} />}
      filterDisplay="row"
      filters={filters}
      globalFilterFields={[...globalFilterFields]}
      loading={loading}
      onFilter={(event) => {
        setFilters(event.filters);
        onLazyFilter?.(event);
      }}
      onPage={onLazyPage ?? onPage}
      onRowClick={(event) => {
        onRowSelect?.(event.data as T);
        onSelectionChange?.(event.data as T);
      }}
      onSelectionChange={(event: DataTableSelectionSingleChangeEvent<T[]>) => onSelectionChange?.((event.value as T | null) ?? null)}
      onSort={onLazySort}
      paginator={paginator}
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown CurrentPageReport"
      currentPageReportTemplate="{first}-{last} de {totalRecords}"
      removableSort
      rows={rows}
      rowsPerPageOptions={rowsPerPageOptions}
      selection={selection}
      selectionMode="single"
      tabIndex={0}
      totalRecords={totalRecords}
      value={value}
      {...tableProps}
    >
      {columns.map((column) => (
        <Column
          body={column.body}
          className={column.className}
          dataType={column.dataType}
          field={column.field}
          filter={column.filter}
          filterElement={column.filterElement}
          filterMatchMode={column.filterMatchMode ?? FilterMatchMode.CONTAINS}
          filterPlaceholder={column.filterPlaceholder}
          showFilterMenu={false}
          header={column.header}
          key={column.field}
          sortable={column.sortable}
          style={column.style}
        />
      ))}
    </DataTable>
  );
}

function createFilters<T extends object>(
  columns: FacDataTableColumn<T>[],
  globalFilterFields: Array<FacDataTableField<T>>
): DataTableFilterMeta {
  const filters: DataTableFilterMeta = {};
  if (globalFilterFields.length > 0) {
    filters.global = { value: null, matchMode: DEFAULT_FILTER_MATCH_MODE };
  }
  columns.forEach((column) => {
    if (!column.filter) return;
    filters[column.field] = { value: null, matchMode: column.filterMatchMode ?? DEFAULT_FILTER_MATCH_MODE };
  });
  return filters;
}
