import { useEffect, useMemo, useState } from "react";
import { apiFetch } from "./api";

type Papel = "ADMINISTRADOR" | "OPERADOR" | "CONSULTA";

type Utilizador = {
  codigo: string;
  nome: string;
  email: string;
  papel: Papel;
  ativo: boolean;
  inativo: boolean;
  criadoEm?: string;
  atualizadoEm?: string;
  ultimoLoginEm?: string;
  criadoPor?: string;
  atualizadoPor?: string;
};

type Page<T> = { content: T[]; totalElements: number };

type FormState = {
  codigo: string;
  nome: string;
  email: string;
  papel: Papel;
  password: string;
};

const emptyForm: FormState = { codigo: "", nome: "", email: "", papel: "OPERADOR", password: "" };

export default function AdminUtilizadoresView() {
  const [users, setUsers] = useState<Utilizador[]>([]);
  const [total, setTotal] = useState(0);
  const [query, setQuery] = useState("");
  const [papel, setPapel] = useState<"" | Papel>("");
  const [ativo, setAtivo] = useState<"" | "true" | "false">("");
  const [form, setForm] = useState<FormState>(emptyForm);
  const [editing, setEditing] = useState<Utilizador | null>(null);
  const [resetTarget, setResetTarget] = useState<Utilizador | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const params = useMemo(() => {
    const p = new URLSearchParams({ size: "50", sort: "codigo,asc" });
    if (query.trim()) p.set("q", query.trim());
    if (papel) p.set("papel", papel);
    if (ativo) p.set("ativo", ativo);
    return p.toString();
  }, [query, papel, ativo]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    apiGet<Page<Utilizador>>(`/api/utilizadores?${params}`)
      .then((page) => {
        if (!active) return;
        setUsers(page.content);
        setTotal(page.totalElements);
      })
      .catch((error) => { if (active) setMessage(error.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [params]);

  function select(user: Utilizador) {
    setEditing(user);
    setResetTarget(null);
    setForm({ codigo: user.codigo, nome: user.nome, email: user.email, papel: user.papel, password: "" });
  }

  function clear() {
    setEditing(null);
    setResetTarget(null);
    setNewPassword("");
    setForm(emptyForm);
  }

  async function save() {
    setMessage(null);
    try {
      if (editing) {
        await apiSend<Utilizador>(`/api/utilizadores/${encodeURIComponent(editing.codigo)}`, "PUT", {
          nome: form.nome,
          email: form.email
        });
        if (form.papel !== editing.papel) {
          await apiSend<Utilizador>(`/api/utilizadores/${encodeURIComponent(editing.codigo)}/perfil`, "PATCH", { papel: form.papel });
        }
        setMessage("Utilizador atualizado.");
      } else {
        await apiSend<Utilizador>("/api/utilizadores", "POST", form);
        setMessage("Utilizador criado.");
      }
      clear();
      await reload(params, setUsers, setTotal);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível guardar o utilizador.");
    }
  }

  async function toggle(user: Utilizador) {
    setMessage(null);
    try {
      await apiSend<Utilizador>(`/api/utilizadores/${encodeURIComponent(user.codigo)}/estado`, "PATCH", { ativo: !user.ativo });
      await reload(params, setUsers, setTotal);
      setMessage(user.ativo ? "Utilizador desativado." : "Utilizador reativado.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível alterar o estado.");
    }
  }

  async function resetPassword() {
    if (!resetTarget) return;
    setMessage(null);
    try {
      const response = await apiFetch(`/api/utilizadores/${encodeURIComponent(resetTarget.codigo)}/redefinir-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ novaPassword: newPassword })
      });
      if (!response.ok) throw new Error(await responseError(response));
      setResetTarget(null);
      setNewPassword("");
      setMessage("Password redefinida.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Não foi possível redefinir a password.");
    }
  }

  return (
    <section className="fac-panel fac-users-v2">
      <div className="fac-panel-header fac-users-header">
        <div>
          <p className="fac-eyebrow">Administração</p>
          <h2>Utilizadores e perfis funcionais</h2>
        </div>
        <span className="fac-users-count">{loading ? "A carregar..." : `${total} ${total === 1 ? "utilizador" : "utilizadores"}`}</span>
      </div>

      {message && <p className="fac-editor-message">{message}</p>}

      <div className="fac-toolbar fac-users-filters">
        <label className="fac-users-filter fac-users-filter-search">
          <span>Pesquisar</span>
          <input placeholder="Código, nome ou email" type="search" value={query} onChange={(e) => setQuery(e.target.value)} />
        </label>
        <label className="fac-users-filter">
          <span>Perfil</span>
          <select value={papel} onChange={(e) => setPapel(e.target.value as "" | Papel)}>
            <option value="">Todos os perfis</option>
            <option value="ADMINISTRADOR">Administrador</option>
            <option value="OPERADOR">Operador</option>
            <option value="CONSULTA">Consulta</option>
          </select>
        </label>
        <label className="fac-users-filter">
          <span>Estado</span>
          <select value={ativo} onChange={(e) => setAtivo(e.target.value as "" | "true" | "false")}>
            <option value="">Todos os estados</option>
            <option value="true">Ativos</option>
            <option value="false">Inativos</option>
          </select>
        </label>
      </div>

      <div className="fac-grid-two fac-users-workspace">
        <div className="fac-table-wrapper">
          <table className="fac-table fac-admin-users-table">
            <thead>
              <tr><th>Código</th><th>Nome</th><th>Perfil</th><th>Estado</th><th>Ações</th></tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.codigo}>
                  <td><strong className="fac-users-identity">{user.codigo}</strong><small>{user.email}</small></td>
                  <td><span className="fac-users-name">{user.nome}</span><small>{user.ultimoLoginEm ? `Último login ${formatDate(user.ultimoLoginEm)}` : "Sem login registado"}</small></td>
                  <td><span className="fac-users-role">{labelPapel(user.papel)}</span></td>
                  <td><span className={`fac-status${user.ativo ? "" : " danger"}`}>{user.ativo ? "Ativo" : "Inativo"}</span></td>
                  <td className="fac-actions fac-users-actions">
                    <button aria-label={`Editar ${user.codigo}`} className="fac-users-action" onClick={() => select(user)} title="Editar utilizador" type="button"><i aria-hidden="true" className="pi pi-pencil" /></button>
                    <button aria-label={`${user.ativo ? "Desativar" : "Ativar"} ${user.codigo}`} className="fac-users-action" onClick={() => toggle(user)} title={user.ativo ? "Desativar utilizador" : "Ativar utilizador"} type="button"><i aria-hidden="true" className={`pi ${user.ativo ? "pi-ban" : "pi-check"}`} /></button>
                    <button aria-label={`Redefinir password de ${user.codigo}`} className="fac-users-action" onClick={() => { setResetTarget(user); setNewPassword(""); }} title="Redefinir password" type="button"><i aria-hidden="true" className="pi pi-key" /></button>
                  </td>
                </tr>
              ))}
              {!users.length && <tr><td colSpan={5}>Sem utilizadores para os filtros atuais.</td></tr>}
            </tbody>
          </table>
        </div>

        <div className="fac-editor-card fac-users-editor">
          <div className="fac-panel-header">
            <div><p className="fac-eyebrow">{editing ? "Editar" : "Novo"}</p><h3>{editing ? editing.codigo : "Utilizador"}</h3></div>
            {editing && <button className="fac-ghost-button" onClick={clear} type="button">Novo</button>}
          </div>
          <Field label="Código">
            <input autoComplete="off" disabled={Boolean(editing)} value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} />
          </Field>
          <Field label="Nome">
            <input autoComplete="off" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
          </Field>
          <Field label="Email">
            <input autoComplete="off" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </Field>
          <Field label="Perfil">
            <select value={form.papel} onChange={(e) => setForm({ ...form, papel: e.target.value as Papel })}>
              <option value="ADMINISTRADOR">Administrador</option>
              <option value="OPERADOR">Operador</option>
              <option value="CONSULTA">Consulta</option>
            </select>
          </Field>
          {!editing && <Field label="Password inicial">
            <input autoComplete="new-password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </Field>}
          {!editing && <small className="fac-muted">8 a 72 caracteres, com maiúscula, minúscula, número e símbolo. Não pode conter o código do utilizador.</small>}
          <div className="fac-form-footer">
            <span className="fac-muted">O código é imutável depois da criação.</span>
            <button className="fac-primary-button" onClick={save} type="button">{editing ? "Guardar" : "Criar"}</button>
          </div>
        </div>
      </div>

      {resetTarget && <div className="fac-panel fac-compact-panel fac-users-password">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Reset de password</p><h3>{resetTarget.codigo}</h3></div>
          <button className="fac-ghost-button" onClick={() => setResetTarget(null)} type="button">Fechar</button>
        </div>
        <div className="fac-form-grid">
          <Field label="Nova password">
            <input autoComplete="new-password" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
          </Field>
        </div>
        <div className="fac-form-footer">
          <span className="fac-muted">Tokens JWT já emitidos não são revogados por este reset.</span>
          <button className="fac-primary-button" onClick={resetPassword} type="button">Redefinir password</button>
        </div>
      </div>}
    </section>
  );
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return <label className="fac-field"><span>{label}</span>{children}</label>;
}

function labelPapel(papel: Papel) {
  if (papel === "ADMINISTRADOR") return "Administrador";
  if (papel === "OPERADOR") return "Operador";
  return "Consulta";
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("pt-PT");
}

async function reload(params: string, setUsers: (users: Utilizador[]) => void, setTotal: (total: number) => void) {
  const page = await apiGet<Page<Utilizador>>(`/api/utilizadores?${params}`);
  setUsers(page.content);
  setTotal(page.totalElements);
}

async function apiGet<T>(url: string): Promise<T> {
  const response = await apiFetch(url);
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function apiSend<T>(url: string, method: "POST" | "PUT" | "PATCH", body: unknown): Promise<T> {
  const response = await apiFetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!response.ok) throw new Error(await responseError(response));
  return response.json();
}

async function responseError(response: Response) {
  try {
    const payload = await response.json() as {
      error?: string;
      fieldErrors?: Array<{ field?: string; message?: string }>;
      message?: string;
    };
    const fieldMessages = payload.fieldErrors
      ?.map((item) => item.message?.trim())
      .filter((message): message is string => Boolean(message));
    if (fieldMessages?.length) return fieldMessages.join(" ");
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}
