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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel guardar o utilizador.");
    }
  }

  async function toggle(user: Utilizador) {
    setMessage(null);
    try {
      await apiSend<Utilizador>(`/api/utilizadores/${encodeURIComponent(user.codigo)}/estado`, "PATCH", { ativo: !user.ativo });
      await reload(params, setUsers, setTotal);
      setMessage(user.ativo ? "Utilizador desativado." : "Utilizador reativado.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Nao foi possivel alterar o estado.");
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel redefinir a password.");
    }
  }

  return (
    <section className="fac-panel">
      <div className="fac-panel-header">
        <div>
          <p className="fac-eyebrow">Administracao</p>
          <h2>Utilizadores e perfis funcionais</h2>
        </div>
        <span className="fac-muted">{loading ? "A carregar..." : `${total} utilizador(es)`}</span>
      </div>

      {message && <p className="fac-editor-message">{message}</p>}

      <div className="fac-toolbar">
        <input placeholder="Pesquisar por codigo, nome ou email" value={query} onChange={(e) => setQuery(e.target.value)} />
        <select value={papel} onChange={(e) => setPapel(e.target.value as "" | Papel)}>
          <option value="">Todos os perfis</option>
          <option value="ADMINISTRADOR">Administrador</option>
          <option value="OPERADOR">Operador</option>
          <option value="CONSULTA">Consulta</option>
        </select>
        <select value={ativo} onChange={(e) => setAtivo(e.target.value as "" | "true" | "false")}>
          <option value="">Todos os estados</option>
          <option value="true">Ativos</option>
          <option value="false">Inativos</option>
        </select>
      </div>

      <div className="fac-grid-two">
        <div className="fac-table-wrapper">
          <table className="fac-table">
            <thead>
              <tr><th>Codigo</th><th>Nome</th><th>Perfil</th><th>Estado</th><th>Acoes</th></tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.codigo}>
                  <td>{user.codigo}<br /><small>{user.email}</small></td>
                  <td>{user.nome}<br /><small>{user.ultimoLoginEm ? `Ultimo login ${formatDate(user.ultimoLoginEm)}` : "Sem login registado"}</small></td>
                  <td>{labelPapel(user.papel)}</td>
                  <td><span className="fac-status">{user.ativo ? "Ativo" : "Inativo"}</span></td>
                  <td className="fac-actions">
                    <button onClick={() => select(user)} type="button">Editar</button>
                    <button onClick={() => toggle(user)} type="button">{user.ativo ? "Desativar" : "Ativar"}</button>
                    <button onClick={() => { setResetTarget(user); setNewPassword(""); }} type="button">Password</button>
                  </td>
                </tr>
              ))}
              {!users.length && <tr><td colSpan={5}>Sem utilizadores para os filtros atuais.</td></tr>}
            </tbody>
          </table>
        </div>

        <div className="fac-editor-card">
          <div className="fac-panel-header">
            <div><p className="fac-eyebrow">{editing ? "Editar" : "Novo"}</p><h3>{editing ? editing.codigo : "Utilizador"}</h3></div>
            {editing && <button onClick={clear} type="button">Novo</button>}
          </div>
          <Field label="Codigo">
            <input disabled={Boolean(editing)} value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} />
          </Field>
          <Field label="Nome">
            <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
          </Field>
          <Field label="Email">
            <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </Field>
          <Field label="Perfil">
            <select value={form.papel} onChange={(e) => setForm({ ...form, papel: e.target.value as Papel })}>
              <option value="ADMINISTRADOR">Administrador</option>
              <option value="OPERADOR">Operador</option>
              <option value="CONSULTA">Consulta</option>
            </select>
          </Field>
          {!editing && <Field label="Password inicial">
            <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </Field>}
          <div className="fac-form-footer">
            <span className="fac-muted">O codigo e imutavel depois da criacao.</span>
            <button className="fac-primary-button" onClick={save} type="button">{editing ? "Guardar" : "Criar"}</button>
          </div>
        </div>
      </div>

      {resetTarget && <div className="fac-panel fac-compact-panel">
        <div className="fac-panel-header">
          <div><p className="fac-eyebrow">Reset de password</p><h3>{resetTarget.codigo}</h3></div>
          <button onClick={() => setResetTarget(null)} type="button">Fechar</button>
        </div>
        <div className="fac-form-grid">
          <Field label="Nova password">
            <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
          </Field>
        </div>
        <div className="fac-form-footer">
          <span className="fac-muted">Tokens JWT ja emitidos nao sao revogados por este reset.</span>
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
    const payload = await response.json();
    return payload.message || payload.error || `Erro HTTP ${response.status}`;
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}
