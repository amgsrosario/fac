import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { PrimeReactProvider } from "primereact/api";
import App from "./App";
import LoginView from "./LoginView";
import { AuthSession, clearAuthSession, getAuthSession } from "./api";
import { UiFoundationLab } from "./ui/commercial";
import { facPrimeReactConfig } from "./ui/fac/theme/primeReactConfig";
import "primeicons/primeicons.css";
import "./ui/fac/theme/fac-tokens.css";
import "./ui/fac/theme/fac-prime.css";
import "./styles.css";

const uiMode = import.meta.env.VITE_FAC_UI_MODE === "commercial" ? "commercial" : "legacy";

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
    <PrimeReactProvider value={facPrimeReactConfig}>
      <BrowserRouter>
        {uiMode === "commercial" ? <UiFoundationLab /> : <Root />}
      </BrowserRouter>
    </PrimeReactProvider>
  </React.StrictMode>
);
