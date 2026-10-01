import { test, expect, Page } from '@playwright/test';

const clients = Array.from({ length: 81 }, (_, i) => ({ id: i + 1, nome: `Cliente ${String(i + 1).padStart(3, '0')}`, nif: `509000${String(i + 1).padStart(3, '0')}`, inativo: false, moedaId: 'EUR', rivaId: 'CON', mPagamentoId: 'TRF', pPagamentoId: 'P30', transporteId: '1' }));
const articles = Array.from({ length: 81 }, (_, i) => ({ codigo: `ART${String(i + 1).padStart(3, '0')}`, descricao: `Artigo ${i + 1}`, familiaId: i === 80 ? 12 : 1, tipoArtigo: 'ARTIGO', unidade: 'UN', pvp: 12.5, ivaVendaId: 'NORMAL', inativo: false }));
const pageOf = (rows: unknown[], page = 0, size = 20) => ({ content: rows.slice(page * size, (page + 1) * size), totalElements: rows.length, totalPages: Math.ceil(rows.length / size), number: page });

async function setup(page: Page) {
  page.on('pageerror', (error) => console.error('Browser error:', error.message));
  const calls: URL[] = [];
  let failNext = false;
  await page.addInitScript(() => {
    if (location.protocol === 'http:' || location.protocol === 'https:') {
      localStorage.setItem('fac.auth.session', JSON.stringify({ token: 'synthetic', type: 'Bearer', codigo: 'TESTE', nome: 'Teste', papel: 'ADMINISTRADOR', permissoes: ['DOCUMENTO_CRIAR', 'DOCUMENTO_EDITAR_RASCUNHO', 'DOCUMENTO_EMITIR', 'DOCUMENTO_OBTER_PDF'] }));
    }
  });
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url()); calls.push(url);
    const path = url.pathname.replace('/api/', '');
    const send = (body: unknown, status = 200) => route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });
    if (path === 'dashboard/comercial') return send({ documentosVencidos: { quantidade: 0, valor: 0 }, vendas: 0, recebimentos: 0, valorEmAberto: 0, moedaId: 'EUR' });
    if (path.endsWith('/lookup')) {
      if (failNext) { failNext = false; return send({ message: 'Falha sintética' }, 503); }
      const search = url.searchParams.get('search') ?? '';
      let rows: any[] = path.startsWith('clientes') ? clients : articles;
      const ids = url.searchParams.getAll('ids');
      if (ids.length) rows = rows.filter((row) => ids.includes(String(row.id ?? row.codigo)));
      else if (search === 'slow') { await new Promise((resolve) => setTimeout(resolve, 900)); rows = [clients[0]]; }
      else if (search.startsWith('nif:=')) rows = rows.filter((row) => row.nif === search.slice(5));
      else if (search.startsWith('familia:=')) rows = rows.filter((row) => row.familiaId === Number(search.slice(9)));
      else if (search) rows = rows.filter((row) => JSON.stringify(row).toLowerCase().includes(search.toLowerCase()));
      return send(pageOf(rows, Number(url.searchParams.get('page') ?? 0), Number(url.searchParams.get('size') ?? 20)));
    }
    const catalogues: Record<string, unknown[]> = {
      'tipos-documento': [{ id: 'FT', descricao: 'Fatura', areaGestao: 2 }],
      series: [{ serie: 'A', tipoDocumentoId: 'FT', nome: 'Série A' }],
      armazens: [{ id: '1', nome: 'Sede' }], moedas: [{ id: 'EUR', nome: 'Euro' }],
      riva: [{ id: 'CON', nome: 'Continente', taxas: [{ tipoTaxaIvaId: 'NORMAL', valor: 23 }] }],
      mpagamentos: [{ id: 'TRF', nome: 'Transferência' }], 'p-pagamentos': [{ id: 'P30', nome: '30 dias' }],
      transportes: [{ id: '1', nome: 'Transporte' }], 'tipos-taxa-iva': [{ id: 'NORMAL', descricao: '23%', inativo: false }]
    };
    if (catalogues[path]) return send(pageOf(catalogues[path]));
    if (path === 'parametros-documento-comercial') return send({ tipoDocumentoId: 'FT', serie: 'A', armazemCargaId: '1' });
    if (path.startsWith('listagens/pendentes')) return send({ linhas: [], totais: { valor: 0, liquidado: 0, pendente: 0 }, totalElements: 0, totalPages: 0 });
    if (/^documentos-comerciais\/9/.test(path)) {
      if (path.endsWith('/linhas')) return send([{ id: 1, numeroLinha: 1, tipoLinha: 'COMERCIAL', artigoId: 'ART081', descricao: 'Artigo 81', quantidade: 2, precoUnitario: 12.5, tipoTaxaIvaId: 'NORMAL', unidade: 'UN', tipoDesconto: 'VALOR', desconto: 0 }]);
      if (path.endsWith('/diagnostico')) return send({ podeEmitir: true, podeAnular: false, alertas: [], bloqueios: [] });
      if (path.endsWith('/impressao')) return send({ documento: {}, linhas: [] });
      return send({ id: 9, tipoDocumentoId: 'FT', serie: 'A', estado: 'RASCUNHO', dataEmissao: '2026-09-30', clienteId: 81, armazemCargaId: '1', moedaId: 'EUR', rivaId: 'CON' });
    }
    return send(pageOf([]));
  });
  return { calls, fail: () => { failNext = true; } };
}

function assertNoPreload(calls: URL[]) {
  // The legacy shell can load a normal, single page for its own customer
  // screen. The lookup flows must never enumerate main-list pages.
  expect(calls.filter((url) => ['/api/clientes', '/api/artigos'].includes(url.pathname) && Number(url.searchParams.get('page') ?? '0') > 0)).toEqual([]);
  for (const url of calls.filter((url) => url.pathname.endsWith('/lookup') && !url.searchParams.has('ids'))) expect(url.searchParams.get('size')).toBe('20');
}

test('editor: NIF e família fora da primeira página, defaults, seleção e teclado', async ({ page }) => {
  const { calls } = await setup(page);
  await page.goto('/documentos/novo');
  const client = page.getByRole('textbox', { name: 'Cliente', exact: true });
  await client.fill('nif:=509000081');
  await expect(page.getByRole('option', { name: /Cliente 081/ })).toBeVisible();
  await client.press('Enter');
  await expect(client).toHaveAttribute('placeholder', /Cliente 081/);
  await client.fill('semresultado');
  await expect(page.getByText('Sem clientes para selecionar.', { exact: true })).toBeVisible();
  await client.press('Escape');
  await expect(client).toHaveAttribute('placeholder', /Cliente 081/);
  await page.getByRole('button', { name: 'Continuar para linhas' }).click();
  const article = page.getByRole('textbox', { name: 'Pesquisar artigo ou serviço', exact: true });
  await article.fill('familia:=12');
  await expect(page.getByRole('option', { name: /ART081/ })).toBeVisible();
  await article.press('Enter');
  await expect(page.locator('tr.active-line').getByRole('textbox', { name: 'Descrição da linha' })).toHaveValue('Artigo 81');
  await expect(page.locator('tr.active-line').getByRole('textbox', { name: 'Unidade', exact: true })).toHaveValue('UN');
  await expect(page.locator('tr.active-line').getByRole('combobox', { name: 'IVA', exact: true })).toHaveValue('NORMAL');
  await expect(page.locator('tr.active-line').getByRole('spinbutton').nth(1)).toHaveValue('12,5');
  assertNoPreload(calls);
});

test('debounce, respostas antigas, erro e retry', async ({ page }) => {
  const { calls, fail } = await setup(page);
  await page.goto('/documentos/novo');
  const client = page.getByRole('textbox', { name: 'Cliente', exact: true });
  await client.fill('slow');
  await expect.poll(() => calls.some((url) => url.searchParams.get('search') === 'slow')).toBe(true);
  await client.fill('Cliente 081');
  await expect(page.getByRole('option', { name: /Cliente 081/ })).toBeVisible();
  await page.waitForTimeout(1000);
  await expect(page.getByRole('option', { name: /Cliente 001/ })).toHaveCount(0);
  const before = calls.length;
  await client.fill('a'); await client.fill('ab'); await client.fill('abc');
  await expect.poll(() => calls.length).toBeGreaterThan(before);
  expect(calls.slice(before).filter((url) => ['a', 'ab'].includes(url.searchParams.get('search') ?? ''))).toHaveLength(0);
  fail(); await client.fill('retry');
  await expect(page.getByRole('button', { name: 'Tentar novamente' })).toBeVisible();
  await page.getByRole('button', { name: 'Tentar novamente' }).click();
  await expect(page.getByText('Sem clientes para selecionar.', { exact: true })).toBeVisible();
});

test('Listagens: paginação remota e seleção estável', async ({ page }) => {
  const { calls } = await setup(page);
  await page.goto('/listagens/pendentes');
  await page.getByRole('button', { name: 'Todos os clientes', exact: true }).click();
  await expect(page.getByRole('checkbox', { name: /Cliente 001/ })).toBeVisible();
  await page.getByRole('button', { name: 'Seguinte', exact: true }).click();
  await expect(page.getByRole('checkbox', { name: /Cliente 021/ })).toBeVisible();
  await page.getByRole('checkbox', { name: /Cliente 021/ }).check();
  await page.getByRole('searchbox', { name: 'Pesquisar cliente', exact: true }).fill('Cliente 081');
  await expect(page.getByRole('checkbox', { name: /Cliente 081/ })).toBeVisible();
  await page.getByRole('checkbox', { name: /Cliente 081/ }).check();
  await page.getByRole('button', { name: 'Concluir', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Cliente 021 x' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Cliente 081 x' })).toBeVisible();
  assertNoPreload(calls);
});

test('GlobalSearch pesquisa remota e preserva deep links', async ({ page }) => {
  const { calls } = await setup(page);
  await page.goto('/documentos');
  const search = page.getByRole('searchbox', { name: 'Pesquisa global' });
  await search.fill('clientes:Cliente 081');
  await expect(page.getByRole('option', { name: /Cliente 081/ })).toBeVisible();
  assertNoPreload(calls);
  await search.press('Enter');
  await expect(page).toHaveURL(/\/clientes\?cliente=81/);
});

test('edição hidrata apenas seleções existentes fora da primeira página', async ({ page }) => {
  const { calls } = await setup(page);
  await page.goto('/documentos/9');
  await expect(page.getByRole('textbox', { name: 'Cliente', exact: true })).toHaveAttribute('placeholder', /Cliente 081/);
  const hydration = calls.filter((url) => url.pathname.endsWith('/lookup'));
  expect(hydration.length).toBeLessThanOrEqual(4);
  expect([...new Set(hydration.flatMap((url) => url.searchParams.getAll('ids')))]).toEqual(['81', 'ART081']);
  assertNoPreload(calls);
});
