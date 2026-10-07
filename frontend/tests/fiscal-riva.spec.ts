import { test, expect, Page } from '@playwright/test';

async function fiscalEditor(page: Page, { failed = false, emitted = false, warningOnSave = false } = {}) {
  const calls: string[] = [];
  page.on("pageerror", (error) => console.error("Browser error:", error.message));
  const fiscal = { valorBruto: '50.00', valorDesconto: '0.00', baseTributavel: '50.00', taxaAplicavel: '6.00', ivaCalculado: '3.00', ivaLiquidado: '0.00', totalLinha: '50.00', tratamentoLiquidacao: 'NAO_LIQUIDAR', mIsencaoCodigo: 'M16', fundamentoFiscal: 'Isento Artigo 14.º do RITI' };
  const doc = { id: 9, tipoDocumentoId: 'FT', serie: 'A', estado: emitted ? 'EMITIDO' : 'RASCUNHO', dataEmissao: '2026-10-07', clienteId: 1, armazemCargaId: '1', moedaId: 'EUR', rivaId: 'INT', valorBruto: '50.00', valorDesconto: '0.00', valorIvaTotal: '0.00', valorTotal: '50.00', resultadoFiscal: { recalculoAviso: !warningOnSave } };
  const line = { id: 1, numeroLinha: 1, artigoId: 'A1', descricao: 'Bens', quantidade: '5', precoUnitario: '10', tipoTaxaIvaId: 'REDUZIDA', unidade: 'UN', tipoDesconto: 'VALOR', desconto: '0', resultadoFiscal: fiscal };
  await page.addInitScript(() => localStorage.setItem('fac.auth.session', JSON.stringify({ token: 'synthetic', type: 'Bearer', codigo: 'TESTE', nome: 'Teste', papel: 'ADMINISTRADOR', permissoes: ['DOCUMENTO_CONSULTAR', 'DOCUMENTO_CRIAR', 'DOCUMENTO_EDITAR_RASCUNHO', 'DOCUMENTO_EMITIR', 'DOCUMENTO_OBTER_PDF'] })));
  await page.route('**/api/**', async (route) => {
    const path = new URL(route.request().url()).pathname.replace('/api/', '');
    calls.push(`${route.request().method()} ${path}`);
    const send = (body: unknown, status = 200) => route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });
    if (path === 'dashboard/comercial') return send({ documentosVencidos: { quantidade: 0, valor: 0 }, vendas: 0, recebimentos: 0, valorEmAberto: 0, moedaId: 'EUR' });
    if (path === 'documentos-comerciais/preview-fiscal') {
      expect(route.request().method()).toBe('POST');
      expect(route.request().postDataJSON()).toMatchObject({ rivaId: 'INT', linhas: [{ artigoId: 'A1', quantidade: '5', precoUnitario: '10' }] });
      return failed ? send({ message: 'Combinação fiscal sem taxa aplicável' }, 400) : send({ linhas: [fiscal], totais: { valorBruto: '50.00', valorDesconto: '0.00', valorIvaTotal: '0.00', valorTotal: '50.00' } });
    }
    if (path === 'documentos-comerciais/9/linhas/1' && route.request().method() === 'PUT') {
      if (warningOnSave) doc.resultadoFiscal.recalculoAviso = true;
      return send(line);
    }
    if (path === 'documentos-comerciais/9/emitir') {
      expect(route.request().postDataJSON().recalculoConfirmado).toBe(true);
      doc.estado = 'EMITIDO';
      return send(doc);
    }
    if (path === 'documentos-comerciais/9/linhas') return send([line]);
    if (path === 'documentos-comerciais/9/diagnostico') return send({ podeEmitir: true, podeAnular: false, alertas: [], bloqueios: [] });
    if (path === 'documentos-comerciais/9/impressao') return send({ documento: doc, linhas: [line] });
    if (path.startsWith('documentos-comerciais/9')) return send(doc);
    const catalogues: Record<string, unknown[]> = {
      'tipos-documento': [{ id: 'FT', descricao: 'Fatura', areaGestao: 2 }], series: [{ serie: 'A', tipoDocumentoId: 'FT', nome: 'Série A' }],
      armazens: [{ id: '1', nome: 'Sede' }], moedas: [{ id: 'EUR', nome: 'Euro' }],
      riva: [{ id: 'INT', nome: 'Intracomunitário', mercado: 'INTRACOMUNITARIO', taxas: [{ tipoTaxaIvaId: 'REDUZIDA', valor: 6 }] }],
      'tipos-taxa-iva': [{ id: 'REDUZIDA', descricao: 'Reduzida', inativo: false }],
      'clientes/lookup': [{ id: 1, nome: 'Cliente', nif: '509000001', inativo: false }],
      'artigos/lookup': [{ codigo: 'A1', descricao: 'Bens', tipoArtigo: 'ARTIGO', unidade: 'UN', pvp: 10, ivaVendaId: 'REDUZIDA', inativo: false }]
    };
    return send({ content: catalogues[path] ?? [], totalElements: (catalogues[path] ?? []).length, totalPages: 1, number: 0 });
  });
  await page.goto('/documentos/9');
  await page.getByRole('button', { name: 'Linhas e totais', exact: true }).click();
  return calls;
}

test('rascunho recalculado apresenta taxa própria, calculado e liquidado do backend', async ({ page }) => {
  const calls = await fiscalEditor(page);
  await expect(page.getByText('Rascunho recalculado', { exact: true })).toBeVisible();
  await expect(page.getByText(/Taxa aplicável 6%.*IVA calculado 3,00.*IVA liquidado 0,00/).first()).toBeVisible();
  await expect(page.locator('.fac-draft-total-final strong')).toHaveText(/50,00\s*€/);
  await expect(page.getByRole('button', { name: 'Emitir documento', exact: true })).toBeEnabled();
  expect(calls).toContain('POST documentos-comerciais/9/recalcular-fiscal');
});

test('erro de resolução fiscal não produz fallback zero nem permite emissão', async ({ page }) => {
  await fiscalEditor(page, { failed: true });
  await expect(page.getByText('Combinação fiscal sem taxa aplicável')).toBeVisible();
  await expect(page.locator('.fac-draft-total-final strong')).toHaveText('—');
  await expect(page.getByRole('button', { name: 'Emitir documento', exact: true })).toBeDisabled();
});

test('emitido mostra snapshot sem preview nem recálculo mutante', async ({ page }) => {
  const calls = await fiscalEditor(page, { emitted: true });
  await expect(page.getByText(/Taxa aplicável 6%.*IVA calculado 3,00.*IVA liquidado 0,00/).first()).toBeVisible();
  await expect(page.locator('.fac-draft-total-final strong')).toHaveText(/50,00\s*€/);
  expect(calls.some((call) => call.includes('recalcular-fiscal') || call.includes('preview-fiscal'))).toBe(false);
});

test('aviso surgido ao guardar exige nova revisão e confirmação antes da emissão', async ({ page }) => {
  const calls = await fiscalEditor(page, { warningOnSave: true });
  const confirmations: string[] = [];
  page.on('dialog', async (dialog) => { confirmations.push(dialog.message()); await dialog.accept(); });
  await expect(page.getByText('Rascunho recalculado', { exact: true })).toHaveCount(0);
  await page.locator('tr:not(.active-line)').getByRole('textbox', { name: 'Descrição da linha' }).fill('Bens actualizados');
  const emit = page.getByRole('button', { name: 'Emitir documento', exact: true });
  await expect(emit).toBeEnabled();
  await emit.click();
  await expect(page.getByText('Rascunho recalculado', { exact: true })).toBeVisible();
  expect(calls.filter((call) => call === 'POST documentos-comerciais/9/emitir')).toHaveLength(0);
  await expect(emit).toBeEnabled();
  await emit.click();
  await expect.poll(() => calls.filter((call) => call === 'POST documentos-comerciais/9/emitir').length).toBe(1);
  expect(confirmations).toHaveLength(2);
  expect(confirmations[0]).not.toContain('recalculado');
  expect(confirmations[1]).toContain('recalculado');
});
