import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import LoginView from "../../LoginView";
import PendentesView from "../../PendentesView";
import ListagensView from "../../ListagensView";
import {
  AuthSession,
  clearAuthSession,
  getAuthSession,
} from "../../api";
import { FacToastProvider } from "../fac";
import ArticlesView from "./articles/ArticlesView";
import CustomersView from "./customers/CustomersView";
import DocumentsLabView from "./documents-lab/DocumentsLabView";
import DraftDocumentEditor from "./documents/DraftDocumentEditor";
import DocumentsView from "./documents/DocumentsView";
import { UiFoundationLabContent } from "./UiFoundationLab";
import "./shared/commercial-shared.css";

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
  const documentsView = (
    <DocumentsView
      currentUser={session}
      onLogout={() => {
        clearAuthSession();
        setSession(null);
      }}
    />
  );
  const draftDocumentsView = (
    <DraftDocumentEditor
      currentUser={session}
      onLogout={() => {
        clearAuthSession();
        setSession(null);
      }}
    />
  );
  const documentsLabView = (
    <DocumentsLabView
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
        <Route element={documentsView} path="/documentos" />
        <Route element={<PendentesView />} path="/recebimentos" />
        <Route element={<ListagensView />} path="/listagens/pendentes-a-data" />
        <Route element={<ListagensView />} path="/listagens/pendentes" />
        <Route element={draftDocumentsView} path="/documentos/novo" />
        <Route element={draftDocumentsView} path="/documentos/:id" />
        <Route element={documentsLabView} path="/documentos-lab" />
        <Route element={<UiFoundationLabContent />} path="/ui-lab" />
        <Route element={<Navigate replace to="/artigos" />} path="*" />
      </Routes>
    </FacToastProvider>
  );
}
