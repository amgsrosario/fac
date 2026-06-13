import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import LoginView from "./LoginView";
import { AuthSession, clearAuthSession, getAuthSession } from "./api";
import "./styles.css";

function Root() {
  const [session, setSession] = useState<AuthSession | null>(() => getAuthSession());

  useEffect(() => {
    const unauthorized = () => setSession(null);
    window.addEventListener("fac:unauthorized", unauthorized);
    return () => window.removeEventListener("fac:unauthorized", unauthorized);
  }, []);

  if (!session) return <LoginView onAuthenticated={setSession} />;
  return <App currentUser={session} onLogout={() => { clearAuthSession(); setSession(null); }} />;
}

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>
);
