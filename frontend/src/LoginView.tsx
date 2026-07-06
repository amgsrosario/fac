import { FormEvent, KeyboardEvent, MouseEvent, useState } from "react";
import { AuthSession, saveAuthSession } from "./api";

type Props = { onAuthenticated: (session: AuthSession) => void };
const LOGIN_URL = "/api/auth/login";

export default function LoginView({ onAuthenticated }: Props) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  async function login(event?: FormEvent<HTMLFormElement> | MouseEvent<HTMLButtonElement> | KeyboardEvent<HTMLInputElement>) {
    event?.preventDefault();
    event?.stopPropagation();
    if (!username.trim() || !password) {
      setMessage("Indica o utilizador e a password.");
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const response = await fetch(LOGIN_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: username.trim(), password })
      });
      if (!response.ok) {
        let detail = "Utilizador ou password invalidos.";
        try {
          const payload = await response.json();
          detail = payload.message || detail;
        } catch {
          // Mantem uma mensagem segura, sem expor detalhes tecnicos.
        }
        throw new Error(detail);
      }
      const session = await response.json() as AuthSession;
      saveAuthSession(session);
      onAuthenticated(session);
    } catch (error) {
      const detail = error instanceof TypeError
        ? `Nao foi possivel contactar a API de autenticacao em ${LOGIN_URL}. Confirma se o backend e o proxy do Vite estao ativos.`
        : error instanceof Error ? error.message : "Nao foi possivel iniciar sessao.";
      setMessage(detail);
    } finally {
      setLoading(false);
    }
  }

  function submitOnPasswordEnter(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter") {
      login(event);
    }
  }

  return <main className="fac-login-shell"><section className="fac-login-card">
    <div className="fac-login-brand"><div className="fac-brand-mark">FAC</div><div><strong>FAC</strong><span>{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "Demo Partner Edition" : "Faturação simples e operacional"}</span></div></div>
    <div className="fac-login-intro"><p className="fac-eyebrow">{import.meta.env.VITE_FAC_DEMO_MODE === "true" ? "Alentejo Sabores, Lda. · Demonstração" : "Entrada segura"}</p><h1>Bem-vindo ao FAC</h1><p>Identifica-te para aceder aos clientes, documentos e recebimentos.</p></div>
    {message && <p className="fac-message" role="alert">{message}</p>}
    <form action="" className="fac-login-form" noValidate onSubmit={login}>
      <label className="fac-field"><span>Utilizador ou email</span><input autoComplete="username" autoFocus onChange={(event) => setUsername(event.target.value)} value={username} /></label>
      <label className="fac-field"><span>Password</span><input autoComplete="current-password" onChange={(event) => setPassword(event.target.value)} onKeyDown={submitOnPasswordEnter} type="password" value={password} /></label>
      <button className="fac-primary-button" disabled={loading} onClick={login} type="button">{loading ? "A validar..." : "Entrar"}</button>
    </form>
  </section></main>;
}
