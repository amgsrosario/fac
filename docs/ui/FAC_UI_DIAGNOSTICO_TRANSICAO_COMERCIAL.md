# FAC - Diagnostico tecnico e funcional da UI antes da transicao comercial

Data do diagnostico: 2026-06-27  
Repositorio analisado: `C:\Projeto_faturacao\fac`  
Branch analisada: `feature/ui-commercial`  
Ambito: frontend React/Vite da FAC, sem alteracoes funcionais.

## 1. Resumo executivo

A UI atual da FAC ja tem valor de produto: cobre o circuito principal de clientes, artigos, documentos comerciais, recebimentos, listagens, importacao/exportacao, configuracao, utilizadores e auditoria. O desenho visual e sobrio, orientado a trabalho diario, com linguagem consistente em varios ecras: sidebar fixa, topbar, blocos hero, paineis, metricas, tabelas, detalhes laterais, formularios e mensagens operacionais.

O principal risco para a transicao comercial nao esta na falta de funcionalidade, mas na forma como a UI foi crescendo como POC. O frontend e pequeno em numero de ficheiros, mas tem ficheiros grandes e muita logica local nos componentes. `App.tsx` concentra shell, dashboard, clientes, configuracao e helpers; `styles.css` concentra todo o sistema visual global; varios ecras repetem `Field`, `fetchJson`, `responseError`, validacoes, formatadores e padroes de formulario/tabela.

A estrategia recomendada e a Estrategia B - Consolidacao progressiva. Preserva a imagem e os fluxos atuais, que ja agradam ao proprietario do produto, mas cria padroes comuns antes de evoluir ecras especificos. Uma reestruturacao ampla nesta fase teria risco alto de regressao e retrabalho; uma evolucao minima resolveria a aparencia imediata, mas deixaria a divida tecnica a crescer.

## 2. Estado Git verificado

Comandos executados antes da analise:

```text
git status --short
git branch --show-current
git log -1 --oneline
git tag --points-at HEAD
```

Resultado:

| Verificacao | Resultado |
| --- | --- |
| Branch atual | `feature/ui-commercial` |
| Working tree | Sem alteracoes locais reportadas por `git status --short` |
| Commit atual | `d415c06 V59` |
| Tag em HEAD | `V60-ui-poc` |
| Tag de preservacao | Confirmada em HEAD |

Nao foram detetadas alteracoes locais antes da analise. A tarefa nao executou build, migracoes, instalacao de dependencias, alteracoes de backend, base de dados, Docker ou scripts.

## 3. Inventario tecnico do frontend

### Estrutura resumida

```text
frontend/
  index.html                         Entrada HTML Vite
  package.json                       React 18, Vite 6, TypeScript 5
  vite.config.ts                     Configuracao Vite
  tsconfig*.json                     Configuracao TypeScript
  src/
    main.tsx                         Root React, sessao e alternancia Login/App
    App.tsx                          App shell, dashboard, clientes e configuracao
    api.ts                           Sessao, permissoes e cliente fetch autenticado
    styles.css                       Sistema visual global
    LoginView.tsx                    Login
    DocumentosView.tsx               Documentos comerciais
    PendentesView.tsx                Tesouraria, pendentes e recebimentos
    ArtigosView.tsx                  Catalogo de artigos
    ListagensView.tsx                Listagens e extratos
    ImportExportView.tsx             Importacao/exportacao de dados mestres
    EmpresaAdminView.tsx             Dados da empresa e logotipo
    AdminUtilizadoresView.tsx        Utilizadores, perfis, estado e password
    AuditoriaView.tsx                Consulta de eventos de auditoria
    TabelasView.tsx                  Catalogos genericos
    TabelasEspecificasView.tsx       Tabelas com regras especificas
    ParametrosDocumentoView.tsx      Valores base para documentos
    ColumnSelector.tsx               Hook/componente reutilizavel de colunas
    MultiSelectFilter.tsx            Filtro multi-selecao para extratos
    dateFilters.ts                   Periodo do ano corrente
```

### Ficheiros de maior dimensao

| Ficheiro | Linhas | Observacao |
| --- | ---: | --- |
| `src/App.tsx` | 1286 | Concentra varias responsabilidades: shell, dashboard, clientes, configuracao, fetch helpers e formatadores. |
| `src/styles.css` | 1158 | CSS global unico, com tokens, layout e estilos especificos por ecran. |
| `src/DocumentosView.tsx` | 759 | Fluxo comercial robusto, mas com muita logica local. |
| `src/PendentesView.tsx` | 391 | Fluxo de tesouraria funcionalmente forte e denso. |
| `src/ArtigosView.tsx` | 361 | CRUD de artigos com padroes repetidos. |
| `src/ListagensView.tsx` | 342 | Muitas fontes de dados e colunas configuraveis num unico componente. |

### Dependencias

O frontend usa apenas:

- `react`
- `react-dom`
- `vite`
- `typescript`
- `@vitejs/plugin-react`
- tipos React

Nao ha biblioteca visual, router, form library, data-fetching library, tabela especializada, modal/toast library, icones externos ou framework CSS. Isto reduz superficie de dependencia, mas aumenta codigo proprio para padroes comuns.

## 4. Routing, contexto global, autenticacao e permissoes

Nao ha `react-router`. A navegacao principal e feita por estado local em `App.tsx` atraves de `activeView`, com o tipo:

```text
Dashboard | Clientes | Documentos | Artigos | Tesouraria | Listagens | ImportExport | Auditoria | Configuracao
```

`main.tsx` decide entre `LoginView` e `App` com base em `getAuthSession()`. A sessao fica em `localStorage` na chave `fac.auth.session`. `apiFetch` injeta `Authorization` e, em HTTP 401 fora do login, limpa a sessao e dispara o evento global `fac:unauthorized`.

Permissoes sao arrays na sessao (`permissoes: string[]`). Existem dois niveis de aplicacao:

- menu: `Auditoria`, `Configuracao` e `ImportExport` sao filtrados no sidebar;
- acoes: documentos, artigos, tesouraria e import/export escondem ou desativam botoes por permissao.

Permissoes observadas:

| Permissao | Uso UI |
| --- | --- |
| `AUDITORIA_CONSULTAR` | Entrada de menu Auditoria |
| `CONFIGURACAO_GERIR` | Entrada de menu Configuracao |
| `DADOS_MESTRES_IMPORTAR` | Importar dados mestres |
| `DADOS_MESTRES_EXPORTAR` | Exportar modelos/dados |
| `MESTRES_GERIR` | Criar/editar artigos e gerir clientes a partir de `App.tsx` |
| `DOCUMENTO_CRIAR` | Novo documento comercial |
| `DOCUMENTO_EDITAR_RASCUNHO` | Adicionar/remover linhas de rascunho |
| `DOCUMENTO_EMITIR` | Conferir e emitir documento |
| `DOCUMENTO_ANULAR` | Anular documento comercial/financeiro |
| `DOCUMENTO_OBTER_PDF` | Abrir PDF comercial |
| `TESOURARIA_GERIR` | Novo recebimento |

Risco: as regras de permissao estao espalhadas pelos componentes. Para a fase comercial, convem consolidar nomes, descricoes e politica de visibilidade/desativacao.

## 5. Mapa funcional da UI

### 5.1 Autenticacao e login

- Rota logica: sem rota URL; renderizado quando nao ha sessao.
- Ficheiro: `LoginView.tsx`.
- Backend: `POST /api/auth/login`.
- Acoes: login por utilizador/email e password.
- Visual: card central, marca FAC, mensagem de erro com `role="alert"`.
- Maturidade: media/alta para POC; simples e claro.
- Problemas: texto apresenta sinais de encoding em alguns ficheiros lidos (`FaturaÃ§Ã£o`), login nao apresenta recuperacao de password nem estado de sessao expirada distinto.
- Preservar: simplicidade e mensagem segura de credenciais invalidas.
- Rever: encoding, feedback de sessao expirada, politicas de seguranca visuais.

### 5.2 Estrutura global da aplicacao

- Ficheiro: `App.tsx`.
- Componentes: `fac-shell`, `fac-sidebar`, `fac-workspace`, `fac-topbar`.
- Acoes: menu lateral, pesquisa global visualmente presente, atualizar, logout.
- Backend: varia conforme vista ativa.
- Visual: sidebar fixa, topbar com utilizador, hero/metricas/paineis.
- Maturidade: visualmente forte; tecnicamente ainda POC.
- Problemas: sem router, sem breadcrumbs, sem estado colapsado do menu, responsividade baseada em CSS global.
- Preservar: estrutura sidebar + workspace, identidade FAC discreta, topbar com utilizador.
- Rever: navegacao, hierarquia de titulos, comportamento mobile e pesquisa global.

### 5.3 Pagina inicial / Dashboard

- Ficheiro: `App.tsx`, componente `DashboardView`.
- Dados: `/api/documentos-comerciais`, `/api/pendentes`, `/api/documentos-financeiros`.
- Acoes: indicadores e atalhos para areas principais.
- Visual: hero, metricas, acoes em grelha, paineis de resumo.
- Maturidade: boa para demonstracao.
- Problemas: depende de listas carregadas com `size=100`; pode nao representar volumes reais.
- Preservar: indicadores de saldo, vencidos, recebido e documentos.
- Rever: definicao funcional dos KPI comerciais e ordenacao por perfil.

### 5.4 Clientes

- Ficheiro: `App.tsx`, componente `ClientesView`.
- Dados: `/api/clientes`, `/api/pendentes/conta-corrente/clientes/{id}/diagnostico`, catalogos de paises/moedas/regimes/modos/prazos/transportes.
- Acoes: pesquisar, selecionar, configurar colunas, criar/editar cliente, aplicar Matriz 0, consultar conta corrente.
- Permissao: `MESTRES_GERIR` para gestao.
- Visual: lista + detalhe/conta corrente, painel editor, metricas.
- Maturidade: funcionalmente boa.
- Problemas tecnicos: esta dentro de `App.tsx`; colunas de cliente usam implementacao propria em vez de `ColumnSelector`; muitos tipos e helpers locais.
- UX: formulario grande sem seccoes explicitas; conta corrente e ficha de cliente competem por atencao.
- Preservar: Matriz 0, conta corrente integrada, colunas configuraveis.
- Rever: separar componente, formulario por secoes e consistencia com `ColumnSelector`.

### 5.5 Artigos

- Ficheiro: `ArtigosView.tsx`.
- Dados: `/api/artigos`, `/api/familias`, `/api/tipos-taxa-iva`.
- Acoes: pesquisar, configurar colunas, criar, editar.
- Permissao: `MESTRES_GERIR`.
- Visual: hero, toolbar, tabela + painel de detalhe, editor.
- Maturidade: media/alta.
- Problemas: helpers HTTP e `Field` duplicados; validacao local simples.
- Preservar: lista + detalhe, codigo imutavel, normalizacao de codigo.
- Rever: tratamento de familias/IVA quando catalogos crescem.

### 5.6 Documentos comerciais

- Ficheiro: `DocumentosView.tsx`.
- Dados: documentos, linhas, clientes, tipos, series, armazens, artigos, parametros, diagnostico, PDF.
- Endpoints principais: `/api/documentos-comerciais`, `/linhas`, `/emitir`, `/anular`, `/pdf`, `/diagnostico`.
- Acoes: novo rascunho com primeira linha, adicionar/remover linhas, diagnostico HTML/JSON, emitir, anular, abrir PDF, configurar colunas.
- Permissoes: `DOCUMENTO_CRIAR`, `DOCUMENTO_EDITAR_RASCUNHO`, `DOCUMENTO_EMITIR`, `DOCUMENTO_ANULAR`, `DOCUMENTO_OBTER_PDF`.
- Visual: muito completo, com detalhe lateral e painel de conferencia.
- Maturidade: funcionalmente alta; tecnicamente media.
- Problemas: componente grande, confirmacoes via `window.confirm`, muitas chamadas HTTP dentro do componente, dialogs misturados com pagina.
- UX: bom cuidado em bloquear emissao por diagnostico, mas fluxo denso para utilizadores novos.
- Preservar: conferencia antes de emissao, diagnostico backend, estado rascunho/emitido/anulado, primeira linha obrigatoria.
- Rever: dialogos, copy fiscal, fluxo guiado de documento.

### 5.7 Series documentais

- Ficheiro: `TabelasEspecificasView.tsx`, tabela `series`.
- Dados: `/api/series`, `/api/tipos-documento`.
- Acoes: criar, editar, eliminar se nao usado.
- Visual: tabela especifica dentro de Configuracao > Tabelas.
- Maturidade: media.
- Problemas: gestao de series esta escondida numa area generica, apesar de ser critica fiscalmente.
- Preservar: validacao por backend, numerador visivel.
- Rever: destaque funcional e explicacao de AT/codigo AT.

### 5.8 Recebimentos e documentos financeiros

- Ficheiro: `PendentesView.tsx`.
- Dados: `/api/pendentes`, `/api/documentos-financeiros`, `/api/tipos-documento`, `/api/series`, `/api/mpagamentos`.
- Acoes: novo recebimento, distribuir por antiguidade, alocar manualmente, emitir recibo, abrir PDF, diagnostico, anular.
- Permissoes: `TESOURARIA_GERIR`, `DOCUMENTO_ANULAR`.
- Visual: hero de saldo em aberto, editor de recebimento, tabelas de pendentes e financeiros.
- Maturidade: alta funcional.
- Problemas: forte densidade visual; algum JSX em linhas muito longas; confirmacoes nativas.
- Preservar: distribuicao por antiguidade, diferenca recebida/distribuida, reposicao de pendentes na anulacao.
- Rever: identificacao por nome de cliente em vez de apenas ID em algumas colunas.

### 5.9 Extratos e listagens

- Ficheiro: `ListagensView.tsx`.
- Fontes: comerciais, linhas comerciais, financeiros, linhas financeiras, relacao comercial, relacao financeira, extrato historico.
- Dados: documentos comerciais/financeiros, linhas por documento, `/api/extratos/clientes`, exportacao PDF/XLSX.
- Acoes: escolher fonte, pesquisar, configurar colunas, consultar extrato, exportar PDF/Excel.
- Visual: grelha de fontes, painel de resultados, tabelas rolaveis.
- Maturidade: boa para consulta e demonstracao.
- Problemas: para linhas comerciais carrega detalhes documento a documento; pode escalar mal. Muitas regras de celula num unico ficheiro.
- Preservar: fontes configuraveis, extrato por cliente/moeda, colunas configuraveis.
- Rever: paginacao, performance e exportacao das restantes listagens, se necessario.

### 5.10 Importacao e exportacao

- Ficheiro: `ImportExportView.tsx`.
- Dados: `/api/importacoes/{tipo}/validar`, `/confirmar`, modelos, exportacoes.
- Tipos: clientes e artigos.
- Formatos: CSV e XLSX.
- Permissoes: `DADOS_MESTRES_IMPORTAR`, `DADOS_MESTRES_EXPORTAR`.
- Acoes: descarregar modelo, exportar todos/ativos, validar ficheiro, confirmar ou cancelar importacao.
- Visual: fluxo de pre-validacao com resumo, erros, avisos e amostra JSON.
- Maturidade: funcionalmente boa.
- Problemas: amostra em `pre` JSON tem aspeto tecnico; confirmacao com `window.confirm`.
- Preservar: validacao antes de gravar, resumo e separacao erros/avisos.
- Rever: apresentacao comercial da amostra e nomenclatura "pre-validacao".

### 5.11 Parametrizacao da empresa

- Ficheiro: `EmpresaAdminView.tsx`.
- Dados: `/api/empresa`, `/api/empresa/logotipo`.
- Acoes: editar dados legais/comerciais, upload/remocao de logotipo.
- Visual: formulario grande, bloco de logotipo.
- Maturidade: media.
- Problemas: muitos campos sem agrupamento funcional forte; alguns campos de catalogo sao texto livre (`paisId`, `codPostalId`, `freguesiaId`).
- Preservar: nota de snapshot fiscal, logotipo com preview.
- Rever: agrupamento por identidade, contactos, morada, dados fiscais, PDFs.

### 5.12 Utilizadores

- Ficheiro: `AdminUtilizadoresView.tsx`.
- Dados: `/api/utilizadores`, perfil, estado, reset password.
- Acoes: filtrar, criar, editar nome/email/perfil, ativar/desativar, redefinir password.
- Visual: tabela + editor lateral.
- Maturidade: media.
- Problemas: UI informa que tokens JWT ja emitidos nao sao revogados; isto e importante mas tambem risco operacional.
- Preservar: filtros por perfil/estado e codigo imutavel.
- Rever: validacao visual de password, confirmacoes para desativar/reset.

### 5.13 Perfis e permissoes

- Ficheiro: `api.ts`, `AdminUtilizadoresView.tsx`, regras espalhadas por vistas.
- Perfis: `ADMINISTRADOR`, `OPERADOR`, `CONSULTA`.
- Acoes: alterar perfil de utilizador.
- Maturidade: media.
- Problemas: nao existe ecran dedicado para matriz de permissoes; o utilizador ve papeis mas nao ve capacidades resultantes.
- Preservar: modelo simples de perfis.
- Rever: explicacao funcional dos perfis antes de venda/distribuicao.

### 5.14 Backup, restauro e operacoes tecnicas

- Nao foi encontrado ecran frontend especifico.
- Existem docs operacionais no repositorio, mas a UI analisada nao expoe backup/restauro.
- Maturidade UI: inexistente.
- Risco: se esta operacao for vendida como parte da experiencia do produto, precisa decisao de produto sobre ficar fora da UI, em area administrativa, ou em ferramenta separada.

### 5.15 Auditoria

- Ficheiro: `AuditoriaView.tsx`.
- Dados: `/api/auditoria`.
- Acoes: filtrar por evento, referencia, utilizador e datas.
- Permissao/menu: `AUDITORIA_CONSULTAR`.
- Visual: hero, filtros, tabela.
- Maturidade: media.
- Problemas: lista de eventos e hardcoded e curta; erros sao tratados de modo especifico para 403.
- Preservar: leitura administrativa simples.
- Rever: exportacao, detalhe de evento, lista dinamica de tipos.

### 5.16 Outras areas

- Configuracao > Parametros: Matriz 0 de clientes e valores base de documentos.
- Configuracao > Tabelas: familias, modos/prazos de pagamento, transportes, paises, moedas, taxas IVA, IVA SAF-T, motivos de isencao, tipos de documento, series, regimes IVA, codigos postais, freguesias, armazens.

## 6. Estrutura global da aplicacao

### App shell

Pontos fortes:

- Sidebar com marca FAC, menu principal e descricoes curtas.
- Topbar com titulo da area ativa, utilizador, papel/codigo e logout.
- Conteudo organizado em workspace com paineis e metricas.
- Ambiente demo tem textos condicionados por `VITE_FAC_DEMO_MODE`.

Problemas:

- Sidebar nao tem recolha/expansao.
- Sem rotas URL, deep links ou historico do browser.
- Sem breadcrumbs; a hierarquia depende do titulo e da area ativa.
- A pesquisa global aparece no topbar, mas nao foi encontrado comportamento funcional transversal.
- Mobile existe em CSS, mas a shell fixa e tabelas largas exigem validacao visual real antes da transicao comercial.

### Hierarquia visual

Padroes fortes:

- `fac-eyebrow` para contexto curto.
- `h1/h2` para titulo de area/painel.
- `fac-hero` + `fac-hero-card` para resumo.
- `fac-metrics` para indicadores.
- `fac-panel` + `fac-panel-header` para secoes.
- `fac-list-toolbar`, `fac-inline-actions`, `fac-form-footer` para acoes.
- `fac-table`, `fac-status`, `fac-muted`, `fac-message`.

Inconsistencias:

- Alguns botoes usam classes (`fac-primary-button`, `fac-ghost-button`, `fac-soft-button`, `fac-gold-button`), outros sao `button` simples em `fac-actions`.
- Confirmacoes alternam entre modal proprio (`fac-dialog`) e `window.confirm`.
- `Field` existe repetido em muitos ficheiros.
- Algumas areas usam "hero"; outras, como auditoria, usam hero mais simples.
- Ha textos com encoding corrompido em alguns ficheiros lidos, o que deve ser auditado no ambiente real.

### Estados da aplicacao

| Estado | Implementacao atual |
| --- | --- |
| Carregamento | Texto em botoes, `fac-muted`, mensagens como "A carregar..." |
| Erro | `fac-message` ou `fac-editor-message-error`; tratamento por componente |
| Sucesso | `fac-editor-message`, `notice`, feedback por ecran |
| Vazio | Linhas de tabela "Sem registos..." e `fac-empty-state` no extrato |
| Sem resultados | Mensagens em tabelas por colSpan |
| Acesso negado | Menu filtrado; 403 em auditoria/documentos tratado parcialmente |
| Sessao expirada | 401 limpa sessao e volta ao login, sem mensagem especifica |
| Confirmacao | `window.confirm` e alguns dialogs proprios |
| Cancelamento | Botoes locais; sem padrao unico |
| Validacao formulario | Funcoes locais retornam strings |
| Gravacao em curso | `loading`, labels "A guardar...", "A validar...", "A anular..." |

## 7. Componentes e padroes reutilizaveis

| Padrao | Existe? | Onde | Estado | Recomendacao | Risco |
| --- | --- | --- | --- | --- | --- |
| `AppShell` | Informal | `App.tsx` | Forte visualmente, acoplado | Consolidar | Medio |
| `PageHeader` | Informal | `fac-hero`, topbar | Repetido por markup | Consolidar | Baixo |
| `PageActions` | Informal | `fac-inline-actions`, `fac-actions` | Inconsistente | Consolidar | Baixo |
| `SectionCard` | Informal | `fac-panel` | Muito usado | Preservar com ajustes | Baixo |
| `FormField` | Informal | `Field` repetido | Duplicado | Consolidar | Baixo |
| `FormSection` | Parcial | `fac-form-grid`, `fac-form-footer` | Sem componente | Consolidar | Baixo |
| `DataTable` | Informal | `fac-table` | CSS comum, logica duplicada | Consolidar progressivamente | Medio |
| `FilterBar` | Informal | toolbars/filtros | Varias formas | Consolidar | Medio |
| `SearchField` | Informal | inputs search | Simples | Preservar com ajustes | Baixo |
| `StatusBadge` | Informal | `fac-status` | Consistente visualmente | Consolidar | Baixo |
| `EmptyState` | Parcial | `fac-empty-state`, linhas vazias | Inconsistente | Consolidar | Baixo |
| `LoadingState` | Parcial | texto local | Sem padrao | Consolidar | Baixo |
| `ErrorState` | Parcial | `fac-message` | Sem severidade padrao | Consolidar | Baixo |
| `ConfirmDialog` | Parcial | `fac-dialog`, `window.confirm` | Inconsistente | Substituir nativo gradualmente | Medio |
| `Modal` | Parcial | anulacao/eliminacao | Limitado | Consolidar | Medio |
| `Toast` | Nao formal | notices inline | Nao existe | Decidir | Baixo |
| `Pagination` | Nao | fetch com `size` fixo | Ausente | Criar quando houver volume | Medio |
| `Tabs` | Informal | `fac-config-nav`, fontes de listagem | Sem componente | Consolidar | Baixo |

## 8. CSS e sistema visual

### Organizacao

Todo o CSS esta em `src/styles.css`. O ficheiro combina:

- tokens CSS em `:root`;
- reset/base;
- app shell;
- login;
- heroes, metricas, paineis;
- tabelas;
- extratos;
- editor de colunas;
- dialogs;
- documentos/tesouraria;
- configuracao/tabelas;
- formularios;
- responsividade.

### Sistema visual implicito

Tokens principais:

```text
--fac-bg: #F7F7F5
--fac-surface: #FFFFFF
--fac-sidebar: #FAFAFA
--fac-border: #E8E8E8
--fac-text: #4B5563
--fac-text-muted: #8A8F98
--fac-petrol: #44515D
--fac-gold: #BA963C
--fac-client-soft: #F3F6F1
--fac-document-soft: #F7F3EA
--fac-product-soft: #F1F5F7
--fac-treasury-soft: #F6F2EC
```

Leitura objetiva:

- Paleta neutra quente, com petrolio/cinza como cor principal e dourado como acento.
- Fundos suaves por dominio funcional.
- Bordas claras, sombras discretas, cantos arredondados moderados.
- Tipografia sem framework, baseada no sistema operativo.
- Controlo visual bom para aplicacao de gestao, sem excesso decorativo.

Riscos CSS:

- CSS global grande favorece efeitos colaterais.
- Classes `fac-*` reduzem colisao, mas nao ha isolamento por componente.
- Alguns seletores agrupam muitos elementos e areas diferentes.
- Valores fixos de larguras/grelhas podem exigir ensaio em mobile e tabelas densas.
- Estados focus existem em campos, mas a acessibilidade completa de todos os botoes/dialogs deve ser testada.

## 9. Qualidade tecnica

Pontos fortes:

- TypeScript usado em todos os componentes.
- Sem `any` evidente nas pesquisas feitas; ha uso de `unknown` em tabelas dinamicas.
- `apiFetch` centraliza token e 401.
- `ColumnSelector` e `useConfiguredColumns` sao reutilizaveis e ja aplicados em varias listagens.
- Fluxos fiscais criticos pedem diagnostico backend antes de emitir/anular.
- Validacoes locais impedem erros basicos antes de chamar o backend.

Divida tecnica:

- Componentes grandes com UI, estado, validacao, HTTP, formatacao e regras de negocio no mesmo ficheiro.
- Repeticao de helpers: `fetchJson`, `responseError`, `Field`, `money`, `datePt`, `blankToNull`, `todayIso`.
- Sem router, o crescimento de areas vai aumentar complexidade em `App.tsx`.
- Sem camada de servicos por dominio; endpoints estao espalhados nos componentes.
- Sem testes frontend encontrados no inventario.
- Sem biblioteca de formularios/tabelas; pode ser aceitavel, mas aumenta custo de manter consistencia.
- Paginacao ausente; muitos endpoints usam `size=100`, `200`, `500`, `1000`.
- Confirmacoes nativas reduzem controlo visual e acessivel.
- Algumas strings mostram encoding corrompido nos ficheiros lidos, a confirmar no editor/browser.

## 10. Avaliacao comercial

### Qualidade funcional

Boa. A UI cobre fluxos reais e coerentes: criar cliente/artigo, criar rascunho, adicionar linhas, emitir, gerar PDF, anular, receber valores, consultar listagens/extratos, importar/exportar dados e administrar parametros.

### Qualidade tecnica

Media. A base e compreensivel e typed, mas ainda tem estrutura POC por concentracao de responsabilidades e duplicacao.

### Consistencia visual

Media/alta. O sistema `fac-*` cria uma identidade reconhecivel. Inconsistencias aparecem em botoes, dialogs, formularios longos e areas administrativas.

### Maturidade comercial

Promissora, ainda nao pronta para ser declarada como UI comercial final sem trabalho adicional. A experiencia transmite confianca nos fluxos fiscais principais, mas algumas areas parecem ferramenta interna: amostras JSON, tabelas tecnicas, confirmacoes nativas, textos de debug/diagnostico visiveis e organizacao de configuracao muito densa.

### Adequacao a microempresas e PME

Boa para utilizadores com rotina administrativa/fiscal. Para utilizadores menos tecnicos, a densidade de documentos, tabelas e parametros precisa hierarquia mais guiada.

### Adequacao a uso diario

Boa nos fluxos principais; precisa validacao de performance, paginacao e ergonomia com dados reais volumosos.

### Aspetos que parecem produto final

- Shell visual consistente.
- Documentos com diagnostico antes de emissao.
- Recebimentos com distribuicao e diferenca.
- Extrato por cliente e moeda.
- Importacao com pre-validacao.
- Configuracao de colunas persistida.

### Aspetos com aparencia de POC

- Sem rotas e breadcrumbs.
- Ficheiros/componentes muito grandes.
- Confirmacoes `window.confirm`.
- Helpers duplicados.
- Mensagens e amostras tecnicas expostas.
- Administracao de tabelas muito generica para areas fiscalmente sensiveis.

## 11. Matriz de diagnostico

| Area | Estado atual | Pontos fortes | Problemas | Risco | Reutilizacao | Prioridade |
| --- | --- | --- | --- | --- | --- | --- |
| App shell | Visualmente consistente | Sidebar/topbar bons | Sem router, sem collapse, pesquisa global incerta | medio | preservar com ajustes | alta |
| Login | Simples e funcional | Mensagens seguras | Sem sessao expirada distinta | baixo | preservar com ajustes | media |
| Dashboard | Bom para demo | KPI claros | Pode nao escalar volumes | medio | preservar com ajustes | alta |
| Clientes | Funcional e integrado | Conta corrente, Matriz 0 | Dentro de `App.tsx`, formulario longo | medio | consolidar | alta |
| Artigos | CRUD util | Lista+detalhe, colunas | Duplicacao de padroes | medio | consolidar | media |
| Documentos comerciais | Fluxo forte | Diagnostico, emissao, PDF, anulacao | Componente grande, dialogs mistos | alto | preservar com ajustes | alta |
| Series documentais | Funcional | Numerador e codigo AT | Escondido em tabelas | alto | preservar com ajustes | alta |
| Tesouraria | Muito funcional | Distribuicao por antiguidade | Densidade e confirmacoes nativas | alto | preservar com ajustes | alta |
| Listagens/extratos | Rico | Fontes, colunas, exportacao extrato | Performance sem paginacao | medio | consolidar | media |
| Import/export | Bem desenhado funcionalmente | Validar antes de gravar | Amostra JSON tecnica | medio | preservar com ajustes | media |
| Empresa | Cobertura ampla | Logotipo e dados fiscais | Campos pouco agrupados | medio | consolidar | alta |
| Utilizadores | Funcional | Perfil, estado, reset | Sem matriz visivel de permissoes | medio | consolidar | media |
| Perfis/permissoes | Simples | Integrado na sessao | Regras espalhadas | alto | consolidar | alta |
| Tabelas | Abrangente | CRUD generico, delete protegido | Area densa e tecnica | alto | consolidar | alta |
| Auditoria | Consulta basica | Filtros essenciais | Tipos hardcoded, sem detalhe/export | medio | preservar com ajustes | media |
| CSS | Identidade clara | Tokens e classes consistentes | Ficheiro global grande | alto | consolidar | alta |
| Componentes comuns | Emergentes | `ColumnSelector` funciona | Poucos componentes formais | alto | consolidar | imediata |

## 12. Riscos principais

1. Regressao visual por alteracoes em `styles.css` global.
2. Regressao funcional se `App.tsx` for alterado sem separar clientes/configuracao gradualmente.
3. Inconsistencia de permissoes por regras espalhadas.
4. Escalabilidade limitada por fetches com tamanhos fixos e sem paginacao UI.
5. Experiencia comercial prejudicada por elementos tecnicos visiveis em areas de diagnostico/importacao/tabelas.
6. Acessibilidade incompleta em dialogs, confirmacoes nativas, tabelas clicaveis e foco.
7. Dificuldade de teste por acoplamento entre UI, chamadas HTTP e regras locais.

## 13. Estrategias possiveis

### Estrategia A - Evolucao minima

Ambito: ajustes pontuais de copy, espacamentos, botoes, dialogs mais visiveis e pequenos alinhamentos por ecran.

Vantagens:

- Rapida.
- Menor risco imediato.
- Preserva quase tudo.
- Boa para uma demonstracao proxima.

Riscos:

- Mantem a divida tecnica.
- CSS global continua fragil.
- A cada novo ecran a inconsistencia aumenta.

Esforco relativo: baixo.  
Risco de regressao: baixo/medio.  
Adequacao ao estado atual: adequada apenas se houver urgencia de apresentacao.  
Impacto no parceiro: melhora percecao, mas nao resolve sustentabilidade.  
Impacto futuro na distribuicao: limitado.

### Estrategia B - Consolidacao progressiva

Ambito: criar/consolidar padroes comuns e migrar ecras por prioridade, preservando fluxos e identidade atuais.

Vantagens:

- Preserva a UI que agrada ao proprietario.
- Reduz duplicacao sem reescrever tudo.
- Permite discutir secao por secao.
- Baixa risco de retrabalho antes de decidir identidade final.

Riscos:

- Exige disciplina para nao virar refactor amplo.
- Durante a transicao podem coexistir padroes antigos e novos.

Esforco relativo: medio.  
Risco de regressao: medio, controlavel por migracao incremental.  
Adequacao ao estado atual: alta.  
Impacto no parceiro: melhora maturidade sem quebrar demonstracao.  
Impacto futuro na distribuicao: bom, cria base sustentavel.

### Estrategia C - Reestruturacao ampla

Ambito: reorganizar frontend com router, servicos, componentes formais, possivelmente novo design system e reescrita de varios ecras.

Vantagens:

- Resolve arquitetura de fundo.
- Pode criar fundacao comercial muito limpa.
- Facilita teste e crescimento futuro.

Riscos:

- Alto risco de regressao em fluxos fiscais.
- Maior tempo antes de nova demonstracao.
- Pode perder caracteristicas atuais que agradam ao proprietario.

Esforco relativo: alto.  
Risco de regressao: alto.  
Adequacao ao estado atual: baixa/media nesta fase.  
Impacto no parceiro: pode atrasar apresentacao.  
Impacto futuro na distribuicao: alto se bem executada, mas caro.

Recomendacao: Estrategia B.

## 14. Ordem recomendada para discussao da UI

1. Fundacoes globais: shell, navegacao, topbar, identidade FAC, estados globais, responsividade.
2. Sistema visual atual: cores, tipografia, paineis, tabelas, formularios, botoes, mensagens.
3. Componentes comuns: PageHeader, SectionPanel, Field, DataTable, FilterBar, StatusBadge, Dialog.
4. Permissoes e perfis: visibilidade de menus, acoes, mensagens de acesso negado.
5. Dashboard: KPI que devem representar valor comercial.
6. Clientes: ficha, Matriz 0, conta corrente e formulario.
7. Artigos: catalogo, campos essenciais e criacao rapida.
8. Documentos comerciais: rascunho, linhas, conferencia, emissao, anulacao e PDF.
9. Tesouraria: pendentes, recebimentos, distribuicao e anulacao.
10. Listagens e extratos: fontes, filtros, colunas, exportacoes e performance.
11. Importacao/exportacao: linguagem comercial, erros/avisos e amostras.
12. Empresa e parametrizacao: separacao entre configuracao simples e fiscalmente sensivel.
13. Series, tipos de documento e tabelas fiscais: tratamento como area critica.
14. Utilizadores e auditoria: administracao, rastreabilidade e operacao segura.
15. Backup/restauro: decisao sobre ficar fora da UI ou entrar em area tecnica.

## 15. Recomendacao final

Avancar com uma consolidacao progressiva. A UI atual deve ser preservada como base, especialmente nos fluxos de documentos, tesouraria, clientes, listagens e importacao/exportacao. Antes de alterar ecras especificos, convem estabilizar os padroes globais e decidir com o proprietario do produto quais elementos sao identidade a manter e quais sao marcas de POC a remover.

A primeira frente de trabalho futura deveria ser apenas de fundacoes: componentes comuns, regras de layout, estados, dialogos, botoes, tabelas e formularios. Depois disso, a migracao por secao tera menos risco e cada discussao funcional podera concentrar-se no valor do ecran, nao em inconsistencias de base.

## 16. Decisoes dependentes do proprietario do produto

- Manter sidebar fixa ou introduzir menu recolhivel?
- A pesquisa global deve existir funcionalmente ou ser removida?
- Quais KPI do dashboard sao comercialmente obrigatorios?
- A Matriz 0 deve continuar com esse nome para utilizadores finais?
- Series documentais devem sair de "Tabelas" para uma area propria?
- Diagnosticos HTML/JSON devem estar visiveis ao utilizador final, ao administrador, ou apenas em modo tecnico?
- Importacao deve mostrar amostra JSON ou tabela amigavel?
- Backup/restauro deve ter UI propria?
- Perfis simples bastam ou e necessaria uma matriz visivel de permissoes?
- O produto comercial deve suportar deep links/URLs por area?
- Confirmacoes criticas devem ser dialogs FAC com copy fiscal controlada?
- Que areas sao essenciais para a primeira demonstracao ao parceiro?

## 17. Ficheiros consultados

Principais ficheiros consultados:

- `frontend/package.json`
- `frontend/src/main.tsx`
- `frontend/src/App.tsx`
- `frontend/src/api.ts`
- `frontend/src/styles.css`
- `frontend/src/LoginView.tsx`
- `frontend/src/DocumentosView.tsx`
- `frontend/src/PendentesView.tsx`
- `frontend/src/ArtigosView.tsx`
- `frontend/src/ListagensView.tsx`
- `frontend/src/ImportExportView.tsx`
- `frontend/src/EmpresaAdminView.tsx`
- `frontend/src/AdminUtilizadoresView.tsx`
- `frontend/src/AuditoriaView.tsx`
- `frontend/src/TabelasView.tsx`
- `frontend/src/TabelasEspecificasView.tsx`
- `frontend/src/ParametrosDocumentoView.tsx`
- `frontend/src/ColumnSelector.tsx`
- `frontend/src/MultiSelectFilter.tsx`
- `frontend/src/dateFilters.ts`
- inventario documental de `docs/`

## 18. Confirmacao de nao alteracao funcional

Este diagnostico foi produzido por inspecao estatica do frontend e verificacoes Git. Nao foram implementadas melhorias, nao foi alterado codigo, nao foram alteradas dependencias, nao foram executadas migracoes, nao foi alterada base de dados, nao foi criada commit e nao foi feito push.

Ficheiro criado para o diagnostico:

- `docs/ui/FAC_UI_DIAGNOSTICO_TRANSICAO_COMERCIAL.md`

Resultado final esperado de `git status --short` apos criar este relatorio:

```text
?? docs/ui/
```

Esse estado representa apenas a criacao documental deste diagnostico.
