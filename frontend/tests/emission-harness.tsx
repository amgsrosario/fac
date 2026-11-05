import React from 'react';
import { createRoot } from 'react-dom/client';
import { MemoryRouter, Routes, Route, Link } from 'react-router-dom';
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
    <MemoryRouter initialEntries={[new URLSearchParams(location.search).get('new') ? '/documentos/novo' : '/documentos/9']}><Link to="/documentos/10">Outro documento sintético</Link><Routes>
      <Route path="/documentos/:id" element={content} />
    </Routes></MemoryRouter>
  </FacToastProvider></PrimeReactProvider>);
