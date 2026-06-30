import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import LoginView from "../../LoginView";
import {
  AuthSession,
  clearAuthSession,
  getAuthSession,
} from "../../api";
import { FacToastProvider } from "../fac";
import ArticlesView from "./articles/ArticlesView";
import CustomersView from "./customers/CustomersView";
import { UiFoundationLabContent } from "./UiFoundationLab";

export default function CommercialApp() {
  const [session, setSession] = useState<AuthSession | null>(() =>
    getAuthSession(),
  );

  useEffect(() => {
    const unauthorized = () => setSession(null);

    window.addEventListener("fac:unauthorized", unauthorized);

    return () =>
      window.removeEventListener("fac:unauthorized", unauthorized);
  }, []);

  if (!session) {
    return <LoginView onAuthenticated={setSession} />;
  }

  const articlesView = (
    <ArticlesView
      currentUser={session}
      onLogout={() => {
        clearAuthSession();
        setSession(null);
      }}
    />
  );
  const customersView = (
    <CustomersView
      currentUser={session}
      onLogout={() => {
        clearAuthSession();
        setSession(null);
      }}
    />
  );

  return (
    <FacToastProvider>
      <Routes>
        <Route element={articlesView} path="/services" />
        <Route element={articlesView} path="/artigos" />
        <Route element={customersView} path="/clientes" />
        <Route element={<UiFoundationLabContent />} path="/ui-lab" />
        <Route element={<Navigate replace to="/artigos" />} path="*" />
      </Routes>
    </FacToastProvider>
  );
}
