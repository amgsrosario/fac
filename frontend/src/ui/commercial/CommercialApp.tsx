import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import LoginView from "../../LoginView";
import { AuthSession, clearAuthSession, getAuthSession } from "../../api";
import { FacToastProvider } from "../fac";
import ServicesView from "./services/ServicesView";
import { UiFoundationLabContent } from "./UiFoundationLab";

export default function CommercialApp() {
  const [session, setSession] = useState<AuthSession | null>(() => getAuthSession());

  useEffect(() => {
    const unauthorized = () => setSession(null);
    window.addEventListener("fac:unauthorized", unauthorized);
    return () => window.removeEventListener("fac:unauthorized", unauthorized);
  }, []);

  if (!session) return <LoginView onAuthenticated={setSession} />;

  return (
    <FacToastProvider>
      <Routes>
        <Route element={<ServicesView currentUser={session} onLogout={() => { clearAuthSession(); setSession(null); }} />} path="/services" />
        <Route element={<UiFoundationLabContent />} path="/ui-lab" />
        <Route element={<Navigate replace to="/services" />} path="*" />
      </Routes>
    </FacToastProvider>
  );
}
