export type AuthSession = {
  token: string;
  type: string;
  expiresIn: number;
  codigo: string;
  nome: string;
  papel: "ADMINISTRADOR" | "OPERADOR" | "CONSULTA";
  permissoes: string[];
};

export function hasPermission(permission: string) {
  return getAuthSession()?.permissoes?.includes(permission) ?? false;
}

const SESSION_KEY = "fac.auth.session";

export function getAuthSession(): AuthSession | null {
  const stored = window.localStorage.getItem(SESSION_KEY);
  if (!stored) return null;
  try {
    const session = JSON.parse(stored) as Partial<AuthSession>;
    if (!session.token || !session.type || !session.codigo || !session.nome
        || !session.papel || !Array.isArray(session.permissoes)) {
      window.localStorage.removeItem(SESSION_KEY);
      return null;
    }
    return session as AuthSession;
  } catch {
    window.localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function saveAuthSession(session: AuthSession) {
  window.localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearAuthSession() {
  window.localStorage.removeItem(SESSION_KEY);
}

export async function apiFetch(input: RequestInfo | URL, init: RequestInit = {}) {
  const session = getAuthSession();
  const headers = new Headers(init.headers);
  if (session?.token) headers.set("Authorization", `${session.type} ${session.token}`);
  const response = await fetch(input, { ...init, headers });
  if (response.status === 401 && !String(input).includes("/auth/login")) {
    clearAuthSession();
    window.dispatchEvent(new Event("fac:unauthorized"));
  }
  return response;
}
