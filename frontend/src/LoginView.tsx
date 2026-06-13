import { useState } from "react";
import { AuthSession, saveAuthSession } from "./api";

type Props = { onAuthenticated: (session: AuthSession) => void };

export default function LoginView({ onAuthenticated }: Props) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  async function login(event: React.FormEvent) {
    event.preventDefault();
    if (!username.trim() || !password) {
      setMessage("Indica o utilizador e a password.");
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const response = await fetch("/api/auth/login", {
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
      setMessage(error instanceof Error ? error.message : "Nao foi possivel iniciar sessao.");
    } finally {
      setLoading(false);
    }
  }

  return <main className="fac-login-shell"><section className="fac-login-card">
    <div className="fac-login-brand"><div className="fac-brand-mark">FAC</div><div><strong>Workspace UI</strong><span>Faturacao simples e operacional</span></div></div>
    <div className="fac-login-intro"><p className="fac-eyebrow">Entrada segura</p><h1>Bem-vindo ao FAC</h1><p>Identifica-te para aceder aos clientes, documentos e recebimentos.</p></div>
    {message && <p className="fac-message" role="alert">{message}</p>}
    <form className="fac-login-form" onSubmit={login}>
      <label className="fac-field"><span>Utilizador ou email</span><input autoComplete="username" autoFocus onChange={(event) => setUsername(event.target.value)} value={username} /></label>
      <label className="fac-field"><span>Password</span><input autoComplete="current-password" onChange={(event) => setPassword(event.target.value)} type="password" value={password} /></label>
      <button className="fac-primary-button" disabled={loading} type="submit">{loading ? "A validar..." : "Entrar"}</button>
    </form>
  </section></main>;
}
