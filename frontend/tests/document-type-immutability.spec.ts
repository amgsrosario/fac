import { test, expect, Page } from '@playwright/test';

async function editor(page: Page, locked: boolean, unusual = false) {
  const row = { id: 'TST', descricao: 'Tipo teste', codigoFiscal: 'ND', areaGestao: unusual ? 99 : 3,
    entidade: unusual ? 2 : 1, sinalContabilistico: 1, liquidacaoImediata: false, configuracaoBloqueada: locked,
    modeloEmissao1: 'Modelo original' };
  await page.addInitScript(() => localStorage.setItem('fac.auth.session', JSON.stringify({ token: 'synthetic',
    type: 'Bearer', codigo: 'TESTE', nome: 'Teste', papel: 'ADMINISTRADOR', permissoes: ['CONFIGURACAO_GERIR', 'DOCUMENTO_CONSULTAR'] })));
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname.replace('/api/', '');
    const send = (body: unknown, status = 200) => route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });
    if (path === 'dashboard/comercial') return send({ documentosVencidos: { quantidade: 0, valor: 0 }, vendas: 0, recebimentos: 0, valorEmAberto: 0, moedaId: 'EUR' });
    if (path === 'tipos-documento/TST' && route.request().method() === 'PUT') return route.fulfill({ status: 204 });
    if (path === 'tipos-documento') return send({ content: [row], totalElements: 1, totalPages: 1, number: 0 });
    return send({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  });
  page.on("pageerror", e => console.error(e.message));
  await page.goto('/configuracao/tabelas/tipos-documento');
  await page.getByRole('button', { name: 'Editar', exact: true }).click();
}

test('issued type keeps protected values visible and only other fields editable', async ({ page }) => {
  await editor(page, true);
  await expect(page.getByRole('status')).toContainText('já existir emissão documental');
  for (const name of ['Código fiscal', 'Área de gestão', 'Entidade', 'Sinal contabilístico']) {
    await expect(page.getByRole('combobox', { name, exact: true })).toBeDisabled();
  }
  await expect(page.getByRole('checkbox', { name: 'Liquidação imediata' })).toBeDisabled();
  await expect(page.getByLabel('Descrição', { exact: true })).toBeEditable();
  await page.getByLabel('Descrição', { exact: true }).fill('Descrição nova');
  const posted = page.waitForRequest(r => r.method() === 'PUT');
  await page.getByRole('button', { name: 'Guardar', exact: true }).click();
  expect((await posted).postDataJSON()).toMatchObject({ descricao: 'Descrição nova', codigoFiscal: 'ND',
    areaGestao: '3', entidade: '1', sinalContabilistico: '1', liquidacaoImediata: false });
});
test('unused type remains editable and new form does not inherit the lock', async ({ page }) => {
  await editor(page, false);
  await expect(page.getByRole('combobox', { name: 'Código fiscal', exact: true })).toBeEnabled();
  await expect(page.getByRole('checkbox', { name: 'Liquidação imediata' })).toBeEnabled();
  await page.getByRole('button', { name: 'Novo registo' }).click();
  await expect(page.getByRole('combobox', { name: 'Código fiscal', exact: true })).toBeEnabled();
});
test('locked values outside the current UI catalogue are still shown', async ({ page }) => {
  await editor(page, true, true);
  await expect(page.getByRole('combobox', { name: 'Área de gestão', exact: true })).toHaveValue('99');
  await expect(page.getByRole('combobox', { name: 'Entidade', exact: true })).toHaveValue('2');
});
