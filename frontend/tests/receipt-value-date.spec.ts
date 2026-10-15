import { test, expect, Page } from '@playwright/test';
async function receipts(page: Page, value: string | null = '2026-01-10') {
  await page.addInitScript(() => localStorage.setItem('fac.auth.session', JSON.stringify({token:'synthetic',type:'Bearer',codigo:'TESTE',nome:'Teste',papel:'ADMINISTRADOR',permissoes:['TESOURARIA_GERIR','DOCUMENTO_CONSULTAR']})));
  const receipt = {id:99,clienteId:1,tipoDocumentoId:'RC',serie:'A',numeroDocumento:1,dataEmissao:'2026-02-01',dataValor:value,moedaId:'EUR',valorPagamentoLiquido:30,mPagamentoId:'TFB',emissorId:'TESTE',anulado:false,linhas:[]};
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname.replace('/api/','');
    const send=(body:unknown)=>route.fulfill({contentType:'application/json',body:JSON.stringify(body)});
    if(path==='dashboard/comercial') return send({documentosVencidos:{quantidade:0,valor:0},vendas:0,recebimentos:0,valorEmAberto:0,moedaId:'EUR',evolucao:[],clientesComMaiorSaldo:[]});
    if(path==='documentos-financeiros/99') return send(receipt);
    if(path==='documentos-financeiros/resumos') return send({content:[receipt],totalElements:1,totalPages:1,number:0});
    if(path.includes('conta-corrente')) return send({content:[],totalElements:0,totalPages:0,number:0,totais:[]});
    const catalogues:Record<string,unknown[]>={'tipos-documento':[{id:'RC',descricao:'Recibo',areaGestao:3}],series:[{serie:'A',tipoDocumentoId:'RC',nome:'Serie'}],mpagamentos:[{id:'TFB',nome:'Transferência'}]};
    return send({content:catalogues[path]??[],totalElements:(catalogues[path]??[]).length,totalPages:1,number:0});
  });
  await page.goto('/');
  await page.getByRole('button',{name:/Receber e consultar pendentes/}).click();
}
test('Data Valor option preserves explicit input',async({page})=>{
  await receipts(page);await page.getByRole('button',{name:'Novo recebimento',exact:true}).click();
  const toggle=page.getByRole('checkbox',{name:'Data valor diferente da data de emissão'});
  const value=page.getByLabel('Data Valor',{exact:true});const issue=page.getByLabel('Data de emissão',{exact:true});
  await expect(toggle).not.toBeChecked();await expect(value).toHaveCount(0);
  await issue.fill('2026-02-01');await toggle.check();await expect(value).toHaveValue('2026-02-01');
  await value.fill('2026-01-10');await issue.fill('2026-01-05');await expect(value).toHaveValue('2026-01-10');
  await expect(page.getByRole('alert')).toHaveText('Data Valor não pode ser posterior à emissão do recibo.');
  await toggle.uncheck();await expect(value).toHaveCount(0);await toggle.check();await expect(value).toHaveValue('2026-01-05');
});
for(const value of ['2026-01-10',null]) test(`issued receipt value date ${value}`,async({page})=>{
  await receipts(page,value);await page.getByText('RC A/1',{exact:true}).click();
  await expect(page.locator('dt').filter({hasText:/^Data Valor$/})).toBeVisible();
  await expect(page.getByText(value?'10/01/2026':'Não registada',{exact:true})).toBeVisible();
});
