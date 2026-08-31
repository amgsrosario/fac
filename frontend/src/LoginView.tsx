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
        ? "Não foi possível contactar o serviço de autenticação. Confirma se a aplicação está disponível."
        : error instanceof Error ? error.message : "Não foi possível iniciar sessão.";
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
    <div className="fac-login-brand"><span className="fac-brand-logo fac-login-logo"><img alt="TUULI AIR" src="/tuuli-air-logo-compact.png" /></span>{import.meta.env.VITE_FAC_DEMO_MODE === "true" && <span className="fac-demo-label">Ambiente de demonstração</span>}</div>
    <div className="fac-login-intro"><h1>Bem-vindo ao TUULI AIR</h1></div>
    {message && <p className="fac-message" role="alert">{message}</p>}
    <form action="" className="fac-login-form" noValidate onSubmit={login}>
      <label className="fac-field"><span>Utilizador ou email</span><input autoComplete="username" autoFocus onChange={(event) => setUsername(event.target.value)} value={username} /></label>
      <label className="fac-field"><span>Palavra-passe</span><input autoComplete="current-password" onChange={(event) => setPassword(event.target.value)} onKeyDown={submitOnPasswordEnter} type="password" value={password} /></label>
      <button className="fac-primary-button" disabled={loading} onClick={login} type="button">{loading ? "A validar..." : "Entrar"}</button>
    </form>
  </section></main>;
}
