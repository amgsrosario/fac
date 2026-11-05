import { test, expect, Page } from '@playwright/test';
async function setup(page: Page, consumer: string, allowed: boolean, warningOnSave = false, options: {missing?: boolean; failSave?: boolean; forbidden?: boolean; delayed?: boolean} = {}) {
  let diagCalls = 0;
  const posts: unknown[] = [];
  const session = { token:'synthetic',type:'Bearer',codigo:'TESTE',nome:'Teste',papel:'ADMINISTRADOR',
    permissoes:['DOCUMENTO_CONSULTAR','DOCUMENTO_EDITAR_RASCUNHO','DOCUMENTO_EMITIR','DOCUMENTO_CRIAR','DOCUMENTO_OBTER_PDF'] };
  await page.addInitScript(value => localStorage.setItem('fac.auth.session',JSON.stringify(value)),session);
  const doc = { id:9,tipoDocumentoId:'FT',tipoDocumentoDescricao:'Fatura',serie:'A',estado:'RASCUNHO',
    dataEmissao:'2026-11-05',clienteId:1,clienteNome:'Synthetic customer',clienteNif:'509123456',
    armazemCargaId:'1',moedaId:'EUR',rivaId:'CON',valorBruto:10,valorDesconto:0,valorIvaTotal:2.3,valorTotal:12.3,
    resultadoFiscal:{recalculoAviso:false} };
  const fiscal={valorBruto:10,valorDesconto:0,baseTributavel:10,taxaAplicavel:23,ivaCalculado:2.3,ivaLiquidado:2.3,totalLinha:12.3,tratamentoLiquidacao:'NORMAL'};
  const line={id:1,numeroLinha:1,tipoLinha:'COMERCIAL',artigoId:'A1',descricao:'Synthetic article',quantidade:'1',precoUnitario:'10',tipoTaxaIvaId:'NORMAL',tipoDesconto:'VALOR',desconto:'0',resultadoFiscal:fiscal};
  await page.route('**/api/**',async route => {
    const path=new URL(route.request().url()).pathname.replace('/api/','');
    const send=(value: unknown,status=200)=>route.fulfill({status,contentType:'application/json',body:JSON.stringify(value)});
    if(path.endsWith('/diagnostico')) {
      diagCalls++;
      if (options.delayed && diagCalls > 1) await new Promise(resolve => setTimeout(resolve, 250));
      if (options.missing) return send({message:'Diagnóstico sintético indisponível'},503);
      return send({referencia:'FT A/9',podeEmitir:allowed,podeAnular:false,alertas:[],bloqueios:[],
        pendente:{existe:false},totais:{coerente:true,cabecalhoValorTotal:12.3,linhasValorTotal:12.3},
        avaliacaoEmissao:{confirmacaoRecalculoNecessaria:doc.resultadoFiscal.recalculoAviso,totalApresentado:12.3,totalProjectado:15,
          impedimentos:allowed?[]:[{codigo:'CODIGO_AT_AUSENTE',mensagem:'A série não possui código AT.'}],
          avisos:doc.resultadoFiscal.recalculoAviso?[{codigo:'RECALCULO_POR_CONFIRMAR',mensagem:'Confirme os valores recalculados.'}]:[]}});
    }
    if(path.endsWith('/emitir')) {posts.push(route.request().postDataJSON());if(options.forbidden)return send({message:'Sem permissão sintética'},403);doc.estado='EMITIDO';return send(doc);}
    if(path.endsWith('/recalcular-fiscal')) return send(doc);
    if(path==='documentos-comerciais/preview-fiscal') return send({linhas:[fiscal],totais:{valorBruto:10,valorDesconto:0,valorIvaTotal:2.3,valorTotal:12.3}});
    if(path.endsWith('/linhas/1') && route.request().method()==='PUT') {if(options.failSave)return send({message:'Falha sintética ao guardar'},400);doc.resultadoFiscal.recalculoAviso=warningOnSave; return send(line);}
    if(path.endsWith('/linhas')) return send([line]);
    if(path.endsWith('/impressao')) return send({documento:doc,linhas:[line]});
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
  await page.goto('/tests/emission-harness.html?consumer='+consumer);
  return {posts,diagnostics:()=>diagCalls};
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
