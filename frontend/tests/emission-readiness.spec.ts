import { test, expect, Page } from '@playwright/test';
async function setup(page: Page, consumer: string, allowed: boolean, warningOnSave = false, options: {missing?: boolean; failSave?: boolean; forbidden?: boolean; delayed?: boolean; newDocument?: boolean; stale?: boolean} = {}) {
  let diagCalls = 0;
  const posts: unknown[] = [];
  const savedDates: (string | null)[] = [];
  const session = { token:'synthetic',type:'Bearer',codigo:'TESTE',nome:'Teste',papel:'ADMINISTRADOR',
    permissoes:['DOCUMENTO_CONSULTAR','DOCUMENTO_EDITAR_RASCUNHO','DOCUMENTO_EMITIR','DOCUMENTO_CRIAR','DOCUMENTO_OBTER_PDF'] };
  await page.addInitScript(value => localStorage.setItem('fac.auth.session',JSON.stringify(value)),session);
  const doc = { id:9,tipoDocumentoId:'FT',tipoDocumentoDescricao:'Fatura',serie:'A',estado:'RASCUNHO',
    dataPrestacao:null as string | null,dataEmissao:'2026-11-05',clienteId:1,clienteNome:'Synthetic customer',clienteNif:'509123456',
    armazemCargaId:'1',moedaId:'EUR',rivaId:'CON',valorBruto:10,valorDesconto:0,valorIvaTotal:2.3,valorTotal:12.3,
    resultadoFiscal:{recalculoAviso:false} };
  const fiscal={valorBruto:10,valorDesconto:0,baseTributavel:10,taxaAplicavel:23,ivaCalculado:2.3,ivaLiquidado:2.3,totalLinha:12.3,tratamentoLiquidacao:'NORMAL'};
  const line={id:1,numeroLinha:1,tipoLinha:'COMERCIAL',artigoId:'A1',descricao:'Synthetic article',quantidade:'1',precoUnitario:'10',tipoTaxaIvaId:'NORMAL',tipoDesconto:'VALOR',desconto:'0',resultadoFiscal:fiscal};
  await page.route('**/api/**',async route => {
    const path=new URL(route.request().url()).pathname.replace('/api/','');
    const send=(value: unknown,status=200)=>route.fulfill({status,contentType:'application/json',body:JSON.stringify(value)});
    if(path.endsWith('/diagnostico')) {
      diagCalls++;
      if(options.stale && path.includes('/9/')) await new Promise(resolve=>setTimeout(resolve,700));
      if (options.delayed && diagCalls > 1) await new Promise(resolve => setTimeout(resolve, 250));
      if (options.missing) return send({message:'Diagnóstico sintético indisponível'},503);
      return send({referencia:'FT A/9',podeEmitir:allowed,podeAnular:false,alertas:[],bloqueios:[],
        pendente:{existe:false},totais:{coerente:true,cabecalhoValorTotal:12.3,linhasValorTotal:12.3},
        avaliacaoEmissao:{confirmacaoRecalculoNecessaria:doc.resultadoFiscal.recalculoAviso,totalApresentado:12.3,totalProjectado:15,
          impedimentos:allowed?[]:[{codigo:'CODIGO_AT_AUSENTE',mensagem:options.stale?(path.includes('/10/')?'Novo contexto sintético':'Antigo contexto sintético'):'A série não possui código AT.'}],
          avisos:doc.resultadoFiscal.recalculoAviso?[{codigo:'RECALCULO_POR_CONFIRMAR',mensagem:'Confirme os valores recalculados.'}]:[]}});
    }
    if(path==='parametros-documento-comercial')return send({tipoDocumentoId:'FT',serie:'A',armazemCargaId:'1'});
    if(path==='documentos-comerciais' && route.request().method()==='POST')return send(doc,201);
    if(path.startsWith('documentos-comerciais/10') && !path.endsWith('/linhas') && !path.endsWith('/impressao'))return send({...doc,id:10});
    if(path.endsWith('/emitir')) {posts.push(route.request().postDataJSON());if(options.forbidden)return send({message:'Sem permissão sintética'},403);doc.estado='EMITIDO';return send(doc);}
    if(path.endsWith('/recalcular-fiscal')) return send(doc);
    if(path==='documentos-comerciais/preview-fiscal') return send({linhas:[fiscal],totais:{valorBruto:10,valorDesconto:0,valorIvaTotal:2.3,valorTotal:12.3}});
    if(path.endsWith('/linhas/1') && route.request().method()==='PUT') {if(options.failSave)return send({message:'Falha sintética ao guardar'},400);doc.resultadoFiscal.recalculoAviso=warningOnSave; return send(line);}
    if(path.endsWith('/linhas')) return send([line]);
    if(path.endsWith('/impressao')) return send({documento:doc,linhas:[line]});
    if(path==='documentos-comerciais/9' && route.request().method()==='PUT') {
      doc.dataPrestacao=route.request().postDataJSON().dataPrestacao;
      savedDates.push(doc.dataPrestacao);
      return route.fulfill({status:204});
    }
    if(path.startsWith('documentos-comerciais/9')) return send(doc);
    if(path==='documentos-comerciais' || path==='documentos-comerciais/resumos') return send({content:[doc],totalElements:1,totalPages:1,number:0});
    const catalogs: Record<string,unknown[]>={
      'tipos-documento':[{id:'FT',descricao:'Fatura',areaGestao:2}],series:[{serie:'A',tipoDocumentoId:'FT',nome:'A'}],
      armazens:[{id:'1',nome:'Warehouse'}],moedas:[{id:'EUR',nome:'Euro'}],riva:[{id:'CON',nome:'Continente'}],
      'tipos-taxa-iva':[{id:'NORMAL',descricao:'Normal',inativo:false}],
      'clientes/lookup':[{id:1,nome:'Synthetic customer',nif:'509123456',inativo:false}],
      'artigos/lookup':[{codigo:'A1',descricao:'Synthetic article',pvp:10,ivaVendaId:'NORMAL',inativo:false}]};
    return send({content:catalogs[path]??[],totalElements:(catalogs[path]??[]).length,totalPages:1,number:0});
  });
  await page.goto('/tests/emission-harness.html?consumer='+consumer+(options.newDocument?'&new=1':''));
  return {posts,savedDates,diagnostics:()=>diagCalls, setServiceDate: (value: string | null) => { doc.dataPrestacao = value; }};
}
for(const consumer of ['legacy','list','editor']) {
  test(consumer+' shows known blocker and prevents issuance',async({page})=>{
    const state=await setup(page,consumer,false);
    if(consumer==='legacy') {
      await page.getByRole('button',{name:'Detalhe',exact:true}).click();
      await page.getByRole('button',{name:'Conferir e emitir',exact:true}).last().click();
    }
    if(consumer==='editor') await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
    await expect(page.getByText('A série não possui código AT.',{exact:true}).first()).toBeVisible();
    await expect(page.getByRole('button',{name:consumer==='list'?'Conferir e emitir':'Emitir documento',exact:true}).last()).toBeDisabled();
    expect(state.posts).toHaveLength(0);
  });
  test(consumer+' reevaluates before post and displays projected difference',async({page})=>{
    const state=await setup(page,consumer,true);
    const dialogs: string[]=[];
    page.on('dialog',async dialog=>{dialogs.push(dialog.message());await dialog.accept();});
    if(consumer==='legacy') {
      await page.getByRole('button',{name:'Detalhe',exact:true}).click();
      await page.getByRole('button',{name:'Conferir e emitir',exact:true}).last().click();
    }
    if(consumer==='editor') await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
    const baseline=state.diagnostics();
    await page.getByRole('button',{name:consumer==='list'?'Conferir e emitir':'Emitir documento',exact:true}).last().click();
    await expect.poll(()=>state.posts.length).toBe(1);
    expect(state.diagnostics()).toBeGreaterThan(baseline);
    expect(dialogs[0]).toContain('Total apresentado: 12.3. Total projectado: 15.');
    expect(state.posts[0]).toMatchObject({recalculoConfirmado:false});
  });
}
test('dirty editor saves then reevaluates and confirms the new persisted warning',async({page})=>{
  const state=await setup(page,'editor',true,true);
  await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
  await page.locator('tr:not(.active-line)').getByRole('textbox',{name:'Descrição da linha'}).fill('Updated synthetic article');
  const messages:string[]=[];
  page.on('dialog',async dialog=>{messages.push(dialog.message());await dialog.accept();});
  await page.getByRole('button',{name:'Guardar e conferir',exact:true}).click();
  await expect(page.getByText('O rascunho foi guardado e recalculado. Revê os valores apresentados e confirma novamente a emissão.',{exact:true})).toBeVisible();
  expect(state.posts).toHaveLength(0);
  expect(messages).toHaveLength(0);
  await expect(page.locator('.fac-draft-total-final strong')).toHaveText(/12,30/);
  await page.getByRole('button',{name:'Emitir documento',exact:true}).click();
  await expect.poll(()=>state.posts.length).toBe(1);
  expect(state.posts[0]).toMatchObject({recalculoConfirmado:true});
  expect(messages[0]).toContain('Confirme os valores recalculados.');
});

async function openConsumer(page: Page, consumer: string) {
  if(consumer==='legacy') {
    await page.getByRole('button',{name:'Detalhe',exact:true}).click();
    await page.getByRole('button',{name:'Conferir e emitir',exact:true}).last().click();
  }
  if(consumer==='editor') await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
}
for(const consumer of ['legacy','list','editor']) {
  test(consumer+' cancellation never posts issuance',async({page})=>{
    const state=await setup(page,consumer,true);
    await openConsumer(page,consumer);
    let dialogs=0;
    page.on('dialog',async dialog=>{dialogs++;await dialog.dismiss();});
    await page.getByRole('button',{name:consumer==='list'?'Conferir e emitir':'Emitir documento',exact:true}).last().click();
    await expect.poll(()=>dialogs).toBe(1);
    expect(state.posts).toHaveLength(0);
  });
  test(consumer+' missing diagnostic never authorizes issuance',async({page})=>{
    const state=await setup(page,consumer,true,false,{missing:true});
    await openConsumer(page,consumer);
    await expect(page.getByText('Diagnóstico sintético indisponível',{exact:true}).first()).toBeVisible();
    expect(state.posts).toHaveLength(0);
    const button=page.getByRole('button',{name:consumer==='list'?'Conferir e emitir':'Emitir documento',exact:true}).last();
    if(await button.count()) await expect(button).toBeDisabled();
  });
  test(consumer+' POST 403 remains an error without success',async({page})=>{
    const state=await setup(page,consumer,true,false,{forbidden:true});
    await openConsumer(page,consumer);
    page.on('dialog',dialog=>dialog.accept());
    await page.getByRole('button',{name:consumer==='list'?'Conferir e emitir':'Emitir documento',exact:true}).last().click();
    await expect.poll(()=>state.posts.length).toBe(1);
    await expect(page.getByText(/Sem permissão sintética/).first()).toBeVisible();
  });
  test(consumer+' waits for delayed fresh diagnostic before confirmation',async({page})=>{
    const state=await setup(page,consumer,true,false,{delayed:true});
    await openConsumer(page,consumer);
    let dialogs=0;
    page.on('dialog',async dialog=>{dialogs++;await dialog.dismiss();});
    await page.getByRole('button',{name:consumer==='list'?'Conferir e emitir':'Emitir documento',exact:true}).last().click();
    expect(state.posts).toHaveLength(0);
    expect(dialogs).toBe(0);
    await expect.poll(()=>dialogs).toBe(1);
    expect(state.posts).toHaveLength(0);
  });
}
test('editor save failure does not request issuance',async({page})=>{
  const state=await setup(page,'editor',true,false,{failSave:true});
  await openConsumer(page,'editor');
  await page.locator('tr:not(.active-line)').getByRole('textbox',{name:'Descrição da linha'}).fill('Synthetic changed');
  await page.getByRole('button',{name:'Guardar e conferir',exact:true}).click();
  await expect(page.getByText(/Falha sintética ao guardar/).first()).toBeVisible();
  expect(state.posts).toHaveLength(0);
});

test('new editor saves before diagnosing and emitting',async({page})=>{
  const state=await setup(page,'editor',true,false,{newDocument:true});
  const client=page.getByRole('textbox',{name:'Cliente',exact:true});
  await client.fill('Synthetic');
  await page.getByRole('option',{name:/Synthetic customer/}).click();
  await page.getByRole('button',{name:'Continuar para linhas',exact:true}).click();
  const article=page.getByRole('textbox',{name:'Pesquisar artigo ou serviço',exact:true});
  await article.fill('Synthetic');
  await page.getByRole('option',{name:/Synthetic article/}).click();
  page.on('dialog',dialog=>dialog.accept());
  await page.getByRole('button',{name:'Guardar e conferir',exact:true}).click();
  await expect.poll(()=>state.posts.length).toBe(1);
  expect(state.posts[0]).toMatchObject({recalculoConfirmado:false});
});
test('editor discards a delayed diagnostic from the previous document',async({page})=>{
  const state=await setup(page,'editor',false,false,{stale:true});
  await expect.poll(()=>state.diagnostics()).toBe(1);
  await page.getByRole('link',{name:'Outro documento sintético',exact:true}).click();
  await expect(page.getByText('Novo contexto sintético',{exact:true})).toBeVisible();
  await page.waitForTimeout(900);
  await expect(page.getByText('Antigo contexto sintético',{exact:true})).toHaveCount(0);
  expect(state.posts).toHaveLength(0);
});


test('draft service date survives reload and clears as explicit null', async ({page}) => {
  const state = await setup(page, 'editor', true);
  const date = page.getByLabel('Data de prestação', {exact:true});
  const save = page.getByRole('button', {name:'Guardar alterações', exact:true});
  await date.fill('2026-11-07');
  await save.click();
  await expect.poll(() => state.savedDates).toEqual(['2026-11-07']);
  await page.reload();
  await expect(date).toHaveValue('2026-11-07');
  await date.fill('');
  await save.click();
  await expect.poll(() => state.savedDates).toEqual(['2026-11-07', null]);
  await page.reload();
  await expect(date).toHaveValue('');
});


const afterIssue = 'A data de prestação é posterior à data de emissão.';
const lateIssue = 'A data de emissão excede o 5.º dia útil após a data de prestação';
const immutableDate = 'A data de prestação não pode ser alterada depois de emitida.';

test('service date working-day calendar uses Gregorian holiday boundaries', async ({page}) => {
  await page.goto('/tests/emission-harness.html');
  const warnings = await page.evaluate(async () => {
    const moduleUrl = '/src/serviceDateWarning.ts';
    const {serviceDateWarning: warning} = await import(moduleUrl);
    return [
      warning('2026-11-28', '2026-11-27'),
      warning('2026-11-27', '2026-12-07'),
      warning('2026-11-27', '2026-12-08'),
      warning('2026-11-27', '2026-12-09'),
      warning('2026-04-02', '2026-04-10'),
      warning('2026-04-02', '2026-04-11'),
      warning('2026-04-05', '2026-04-10'),
      warning('2026-05-29', '2026-06-08'),
      warning('2026-05-29', '2026-06-09'),
      warning('', '2026-11-27'),
      warning('2026-11-27', '2026-11-27')
    ];
  });
  expect(warnings).toEqual([
    'after-issue', null, 'late-issue', 'late-issue',
    null, 'late-issue', null, null, 'late-issue', null, null
  ]);
});

test('service date warning is shown during document creation and responds to either date', async ({page}, info) => {
  await setup(page, 'editor', true, false, {newDocument:true});
  const service = page.getByLabel('Data de prestação', {exact:true});
  const issue = page.getByLabel('Data', {exact:true});
  await issue.fill('2026-12-07');
  await service.fill('2026-11-27');
  await expect(page.getByText(lateIssue, {exact:false})).toHaveCount(0);
  await issue.fill('2026-12-09');
  await expect(page.getByText(lateIssue, {exact:false})).toBeVisible();
  const screenshot = info.outputPath('01-criacao.png');
  await page.screenshot({path:screenshot});
  await info.attach('Criação', {path:screenshot, contentType:'image/png'});
  await service.fill('2026-12-09');
  await expect(page.getByText(lateIssue, {exact:false})).toHaveCount(0);
});

test('fresh service date requests renewed confirmation and does not change the issuance payload', async ({page}, info) => {
  const baseline = await setup(page, 'editor', true);
  const messages: string[] = [];
  page.on('dialog', async dialog => { messages.push(dialog.message()); await dialog.accept(); });
  await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
  await page.getByRole('button',{name:'Emitir documento',exact:true}).click();
  await expect.poll(() => baseline.posts.length).toBe(1);
  expect(messages[0]).not.toContain(immutableDate);

  const state = await setup(page, 'editor', true);
  const service = page.getByLabel('Data de prestação', {exact:true});
  await service.fill('2026-11-07');
  await expect(page.getByText(afterIssue, {exact:false})).toBeVisible();
  const editorShot = info.outputPath('02-editor.png');
  await page.screenshot({path:editorShot});
  await info.attach('Editor', {path:editorShot, contentType:'image/png'});
  await page.getByRole('button',{name:'Guardar alterações',exact:true}).click();
  await expect.poll(() => state.savedDates).toEqual(['2026-11-07']);
  const emit = page.getByRole('button',{name:'Emitir documento',exact:true});
  await expect(emit).toBeEnabled();
  state.setServiceDate('2026-10-20');
  await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
  await emit.click();
  const notice = page.getByText(/Datas actualizadas.*Prestação: 2026-10-20.*Reveja as datas/);
  await expect(notice).toContainText(lateIssue);
  expect(messages).toHaveLength(1);
  expect(state.posts).toHaveLength(0);
  await page.getByRole('button',{name:'Cabeçalho e condições',exact:true}).click();
  await expect(service).toHaveValue('2026-10-20');
  await page.getByRole('button',{name:'Linhas e totais',exact:true}).click();
  await emit.click();
  await expect.poll(() => state.posts.length).toBe(1);
  expect(messages).toHaveLength(2);
  expect(messages[1]).toContain(lateIssue);
  expect(messages[1]).toContain(immutableDate);
  expect(state.posts[0]).toEqual(baseline.posts[0]);
  await expect(page.getByText('Documento emitido.', {exact:true}).first()).toBeVisible();

  // Playwright does not include the native confirmation dialog in page screenshots.
  // Preserve its actual message and make a labelled visual transcript.
  await info.attach('Mensagem nativa da confirmação', {body:messages[1], contentType:'text/plain'});
  const transcript = await page.context().newPage();
  await transcript.setContent('<h1>Transcrição da confirmação nativa</h1><pre style="white-space: pre-wrap"></pre>');
  await transcript.locator('pre').evaluate((element, message) => { element.textContent = message; }, messages[1]);
  const dialogShot = info.outputPath('03-confirmacao-transcricao.png');
  await transcript.screenshot({path:dialogShot});
  await info.attach('Transcrição da confirmação', {path:dialogShot, contentType:'image/png'});
  await transcript.close();
});

test('legacy creation form shows the service date warning', async ({page}) => {
  await setup(page, 'legacy', true);
  await page.goto('/tests/emission-harness.html?consumer=legacy&legacyCreate=1');
  await expect(page.getByRole('heading', {name:'Novo documento'})).toBeVisible();
  await page.getByLabel('Data de emissão', {exact:true}).fill('2026-11-23');
  const service = page.getByLabel('Data de prestação', {exact:true});
  await service.fill('2026-11-24');
  await expect(page.getByText(afterIssue, {exact:false})).toBeVisible();
  await service.fill('');
  await expect(page.getByText(afterIssue, {exact:false})).toHaveCount(0);
});

for (const consumer of ['legacy','list']) {
  test(consumer+' confirms once with the baseline message or the fresh service date warning', async ({page}) => {
    const baselineMessage = 'Total apresentado: 12.3. Total projectado: 15.\nEmitir definitivamente FT A/9? Confirma os valores apresentados.';
    const messages: string[] = [];
    page.on('dialog', async dialog => { messages.push(dialog.message()); await dialog.accept(); });
    const emit = page.getByRole('button', {name:consumer==='list'?'Conferir e emitir':'Emitir documento', exact:true}).last();
    const baseline = await setup(page, consumer, true);
    await openConsumer(page, consumer);
    await emit.click();
    await expect.poll(() => baseline.posts.length).toBe(1);
    expect(messages).toEqual([baselineMessage]);
    expect(baseline.posts).toEqual([{emissorId:'TESTE', recalculoConfirmado:false}]);

    const state = await setup(page, consumer, true);
    await openConsumer(page, consumer);
    await expect(emit).toBeEnabled();
    state.setServiceDate('2026-11-07');
    await emit.click();
    await expect.poll(() => state.posts.length).toBe(1);
    expect(messages).toHaveLength(2);
    expect(messages[1]).toContain('Emissão: 2026-11-05. Prestação: 2026-11-07.');
    expect(messages[1]).toContain(afterIssue);
    expect(messages[1]).toContain(immutableDate);
    expect(messages[1]).toContain(baselineMessage);
    expect(state.posts).toEqual(baseline.posts);
    await expect(page.getByRole('cell', {name:'Emitido', exact:true}).first()).toBeVisible();
  });
}

test('service date working-day calendar handles other years and non-business start dates', async ({page}) => {
  await setup(page, 'editor', true);
  const results = await page.evaluate(async () => {
    const moduleUrl = '/src/serviceDateWarning.ts';
    const {serviceDateWarning: warning} = await import(moduleUrl);
    return [
      ['2025-04-16','2025-04-24','2025-04-25'],
      ['2025-06-18','2025-06-26','2025-06-27'],
      ['2027-03-25','2027-04-02','2027-04-03'],
      ['2027-05-26','2027-06-03','2027-06-04'],
      ['2026-11-28','2026-12-07','2026-12-09'],
      ['2026-12-01','2026-12-09','2026-12-10'],
      ['2026-12-31','2027-01-08','2027-01-11']
    ].map(([service,deadline,late]) => [warning(service,deadline),warning(service,late)]);
  });
  expect(results).toEqual(Array.from({length:7}, () => [null,'late-issue']));
});
