import React from 'react';
import { createRoot } from 'react-dom/client';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { FacToastProvider } from '../src/ui/fac';
import { PrimeReactProvider } from 'primereact/api';
import { facPrimeReactConfig } from '../src/ui/fac/theme/primeReactConfig';
import DocumentosView from '../src/DocumentosView';
import DocumentsView from '../src/ui/commercial/documents/DocumentsView';
import DraftDocumentEditor from '../src/ui/commercial/documents/DraftDocumentEditor';
import { getAuthSession } from '../src/api';
import '../src/styles.css';
const session = getAuthSession()!;
const consumer = new URLSearchParams(location.search).get('consumer');
const content = consumer === 'legacy' ? <DocumentosView /> : consumer === 'list'
  ? <DocumentsView currentUser={session} onLogout={() => {}} />
  : <DraftDocumentEditor currentUser={session} onLogout={() => {}} />;
createRoot(document.getElementById('root')!).render(
  <PrimeReactProvider value={facPrimeReactConfig}><FacToastProvider>
    <MemoryRouter initialEntries={['/documentos/9']}><Routes>
      <Route path="/documentos/:id" element={content} />
    </Routes></MemoryRouter>
  </FacToastProvider></PrimeReactProvider>);
