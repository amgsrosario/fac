import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { PrimeReactProvider } from "primereact/api";
import App from "./App";
import LoginView from "./LoginView";
import { AuthSession, clearAuthSession, getAuthSession } from "./api";
import { CommercialApp } from "./ui/commercial";
import DraftDocumentEditor from "./ui/commercial/documents/DraftDocumentEditor";
import { FacToastProvider } from "./ui/fac";
import { facPrimeReactConfig } from "./ui/fac/theme/primeReactConfig";
import "primeicons/primeicons.css";
import "./ui/fac/theme/fac-tokens.css";
import "./ui/fac/theme/fac-prime.css";
import "./styles.css";

const uiMode = import.meta.env.VITE_FAC_UI_MODE === "commercial" ? "commercial" : "legacy";

function Root() {
  const [session, setSession] = useState<AuthSession | null>(() => getAuthSession());
  const logout = () => {
    clearAuthSession();
    setSession(null);
  };

  useEffect(() => {
    const unauthorized = () => setSession(null);
    window.addEventListener("fac:unauthorized", unauthorized);
    return () => window.removeEventListener("fac:unauthorized", unauthorized);
  }, []);

  if (!session) return <LoginView onAuthenticated={setSession} />;
  return (
    <FacToastProvider>
      <Routes>
        <Route element={<App currentUser={session} initialView="Dashboard" onLogout={logout} />} path="/" />
        <Route element={<App currentUser={session} initialView="Documentos" onLogout={logout} />} path="/documentos" />
        <Route element={<App currentUser={session} embeddedContent={<DraftDocumentEditor currentUser={session} embedded onLogout={logout} />} initialView="Documentos" onLogout={logout} />} path="/documentos/novo" />
        <Route element={<App currentUser={session} embeddedContent={<DraftDocumentEditor currentUser={session} embedded onLogout={logout} />} initialView="Documentos" onLogout={logout} />} path="/documentos/:id" />
        <Route element={<App currentUser={session} onLogout={logout} />} path="*" />
      </Routes>
    </FacToastProvider>
  );
}

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <PrimeReactProvider value={facPrimeReactConfig}>
      <BrowserRouter>
        {uiMode === "commercial" ? <CommercialApp /> : <Root />}
      </BrowserRouter>
    </PrimeReactProvider>
  </React.StrictMode>
);
