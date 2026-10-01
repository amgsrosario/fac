import { useEffect, useState } from "react";
import { apiFetch } from "../api";

export type LookupPage<T> = { content: T[]; totalElements: number; totalPages: number; number: number };
export type LookupOptions = { endpoint: string; context?: string; inativo?: boolean };
export type LookupCriteria = { search: string; page?: number; sort?: string[]; filters?: Record<string, string> };

export function lookupUrl(options: LookupOptions, criteria: LookupCriteria) {
  const params = new URLSearchParams({ search: criteria.search, page: String(criteria.page ?? 0), size: "20", context: options.context ?? "editor" });
  if (options.inativo !== undefined) params.set("inativo", String(options.inativo));
  criteria.sort?.forEach((sort) => params.append("sort", sort));
  Object.entries(criteria.filters ?? {}).forEach(([field, value]) => { if (value.trim()) params.set(`filter.${field}`, value); });
  return `${options.endpoint}?${params}`;
}

export function useRemoteLookup<T>(options: LookupOptions | undefined, criteria: LookupCriteria, enabled: boolean) {
  const url = options ? lookupUrl(options, criteria) : "";
  const [attempt, setAttempt] = useState(0);
  const key = `${enabled}:${attempt}:${url}`;
  const [state, setState] = useState<{ key: string; page?: LookupPage<T>; error?: string; loading: boolean }>({ key: "", loading: false });
  useEffect(() => {
    if (!enabled || !url) return;
    let current = true;
    const controller = new AbortController();
    setState({ key, loading: true });
    const timer = window.setTimeout(async () => {
      try {
        const response = await apiFetch(url, { signal: controller.signal });
        if (!response.ok) throw new Error(await response.text() || "Não foi possível pesquisar.");
        const page = await response.json() as LookupPage<T>;
        if (current) setState({ key, page, loading: false });
      } catch (error) {
        if (current) setState({ key, error: error instanceof Error ? error.message : "Não foi possível pesquisar.", loading: false });
      }
    }, 300);
    return () => { current = false; controller.abort(); window.clearTimeout(timer); };
  }, [enabled, key, url]);
  // Never expose the previous query's rows during the render before the effect runs.
  const current = state.key === key ? state : undefined;
  return { rows: current?.page?.content ?? [], total: current?.page?.totalElements ?? 0, pages: current?.page?.totalPages ?? 0,
    loading: Boolean(enabled && options && (!current || current.loading)), error: current?.error, retry: () => setAttempt((n) => n + 1) };
}

export async function lookupSelected<T>(endpoint: string, ids: Array<string | number>): Promise<T[]> {
  const unique = [...new Set(ids)];
  const rows: T[] = [];
  // Only the document's selected IDs, never a scan of the entity catalogue.
  for (let offset = 0; offset < unique.length; offset += 50) {
    const params = new URLSearchParams({ size: "50", context: "global" });
    unique.slice(offset, offset + 50).forEach((id) => params.append("ids", String(id)));
    const response = await apiFetch(`${endpoint}?${params}`);
    if (!response.ok) throw new Error(await response.text());
    rows.push(...(await response.json() as LookupPage<T>).content);
  }
  return rows;
}
