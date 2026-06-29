# FAC UI - Validacao visual da fundacao comercial

Data: 2026-06-29  
Branch: `feature/ui-commercial`  
Commit de partida observado: `fc1649b V60-ui-poc`  
Tag preservada: `V60-ui-poc`

## 1. Ambiente

Validacao executada localmente em Windows, no projeto:

```text
C:\Projeto_faturação\fac
```

Ambito respeitado:

- alteracoes apenas em `frontend/src/ui/` e `docs/ui/`;
- sem alteracoes em backend, base de dados, Flyway, API, regras fiscais, permissoes, `App.tsx` ou vistas funcionais legacy;
- sem migracao funcional de ecras reais.

## 2. Comandos executados

Servidor comercial temporario:

```cmd
cd /d C:\Projeto_faturação\fac\frontend && set VITE_FAC_UI_MODE=commercial&& .\node_modules\.bin\vite.cmd --host 127.0.0.1 --port 5174
```

Equivalente em PowerShell:

```powershell
$env:VITE_FAC_UI_MODE="commercial"
.\node_modules\.bin\vite.cmd --host 127.0.0.1 --port 5174
```

Build por npm:

```text
npm run build
```

Resultado: falhou por shim global do npm quebrado.

```text
Cannot find module 'C:\Users\amgsr\AppData\Roaming\npm\node_modules\npm\bin\npm-cli.js'
```

Build por binarios locais:

```powershell
& '.\node_modules\.bin\tsc.cmd' -b
& '.\node_modules\.bin\vite.cmd' build
```

Resultado: passou.

## 3. Larguras testadas

Capturas e metricas de overflow foram verificadas em:

- 375 px;
- 430 px;
- 768 px;
- 1024 px;
- 1440 px.

Resultado objetivo: `documentElement.scrollWidth` e `body.scrollWidth` ficaram iguais a `window.innerWidth` nos cinco breakpoints. Nao foi detetado overflow horizontal.

## 4. Componentes validados

No laboratorio `/ui-lab` foram validados:

- shell desktop;
- shell mobile;
- badge de estado;
- botoes primario, secundario, ghost, texto e destrutivo;
- input ativo;
- input desativado;
- select com overlay;
- dialog controlado;
- toast;
- mensagens informativa, sucesso, aviso e erro;
- loading state;
- empty state;
- error state;
- composicao desktop;
- composicao mobile.

## 5. Problemas encontrados

Foram encontrados e corrigidos estes problemas visuais:

- classe `.fac-message` colidia semanticamente com estilos legacy; foi criada `.fac-ui-message`;
- select PrimeReact unstyled abria com lista sem reset e posicao inadequada; overlay passou a ser local ao controlo;
- toast PrimeReact unstyled nao preservava a classe wrapper esperada; os estilos passaram a usar atributos `data-pc-name="toast"` e `data-pc-section`;
- dialog tinha botao de fechar visualmente cru; o close button recebeu foco, dimensao e cor FAC;
- mobile 375 px tinha risco de corte lateral por min-width de paineis/acoes; foram adicionados limites de largura, `min-width: 0` e `overflow-x: hidden` nos contentores comerciais;
- grid desktop esticava paineis verticalmente; `align-items: start` foi aplicado ao grid do laboratorio;
- demo de toast duplicava em desenvolvimento por `StrictMode`; o disparo tecnico foi protegido com `useRef`.

## 6. Alteracoes efetuadas

Ficheiros UI alterados:

- `frontend/src/ui/fac/theme/fac-tokens.css`;
- `frontend/src/ui/fac/theme/fac-prime.css`;
- `frontend/src/ui/fac/components/FacButton.tsx`;
- `frontend/src/ui/fac/components/FacSelect.tsx`;
- `frontend/src/ui/fac/components/FacStatusBadge.tsx`;
- `frontend/src/ui/fac/feedback/FacStates.tsx`;
- `frontend/src/ui/fac/layout/DesktopShell.tsx`;
- `frontend/src/ui/fac/layout/MobileShell.tsx`;
- `frontend/src/ui/fac/responsive/device.tsx`;
- `frontend/src/ui/commercial/UiFoundationLab.tsx`.

Alteracoes principais:

- tokens FAC ampliados para controlar a base comercial;
- novos tons e variantes visuais;
- `FacMessage` introduzido para mensagens comerciais isoladas;
- suporte tecnico a query params de demo para capturas;
- estilos de overlay PrimeReact ajustados ao modo `unstyled`;
- shells preparados para desktop, tablet e mobile sem overflow horizontal.

## 7. Capturas guardadas

Pasta:

```text
docs/ui/commercial-foundation/
```

Ficheiros:

- `desktop-1440.png`;
- `desktop-1024.png`;
- `tablet-768.png`;
- `mobile-430.png`;
- `mobile-375.png`;
- `dialog-open.png`;
- `select-open.png`;
- `toast-visible.png`;
- `states.png`;
- `comparison-mobile.png`;
- `legacy-default-login.png`.

## 8. Acessibilidade

Validacao feita:

- foco visual em botoes, select e close de dialog;
- dialog com semantica PrimeReact;
- mensagens com `role="status"` e erros com `role="alert"`;
- botoes PrimeReact mantem `aria-label`;
- select mantem semantica de listbox do PrimeReact.

Limite: nao foi feita auditoria WCAG completa nem teste com leitor de ecra. A fundacao esta melhor preparada, mas ainda nao deve ser declarada conforme WCAG.

## 9. Isolamento CSS

A UI comercial fica ancorada em `.fac-ui` e classes FAC dedicadas.

Excecoes intencionais:

- overlays PrimeReact podem ser renderizados fora da arvore visual imediata;
- toast e dialog usam seletores `data-pc-*` para capturar DOM real do PrimeReact em modo `unstyled`;
- `styles.css` legacy continua importado e a UI legacy continua dependente dele.

Foi evitada a migracao ou alteracao de vistas funcionais existentes.

## 10. Diferencas desktop/mobile

Desktop:

- usa `DesktopShell`;
- sidebar fixa;
- composicao mais horizontal;
- grid com paineis lado a lado;
- conteudo limitado por `--fac-content-max-width`.

Mobile:

- usa `MobileShell`;
- header simples;
- paineis em coluna;
- botoes com largura total e altura de toque maior;
- acao primaria demonstrada com area sticky;
- sem overflow horizontal nos 375 px e 430 px testados.

## 11. Limitacoes

- A validacao cobre o laboratorio comercial, nao ecras funcionais reais.
- A validacao legacy confirmou a entrada/login default, mas nao dashboard, listagens ou fluxos autenticados por falta de sessao/backend autenticado nesta execucao.
- O browser integrado do Codex ficou instavel em algumas navegacoes; as evidencias foram capturadas com Chrome headless local.
- `npm run build` continua bloqueado pelo shim global do npm; o build valido foi feito pelos binarios locais.
- Nao foram criados testes automatizados de UI.

## 12. Decisoes pendentes

- API final de `FacDataTable`;
- politica global de confirmacoes;
- politica global de toasts;
- navegacao mobile definitiva;
- matriz de capacidades por edicao;
- campos que ficarao ocultos na edicao simples;
- valores predefinidos por ecran para campos ocultos;
- criterios de aceitacao WCAG por nivel.

## 13. Primeiro ecran real recomendado

O primeiro ecran funcional recomendado continua a ser `Artigos`.

Razoes:

- CRUD claro;
- menor risco fiscal que documentos comerciais;
- permite validar formularios, listagens, estados vazios e permissoes simples;
- expande naturalmente a futura API de `FacDataTable`;
- permite testar capacidades de produto sem tocar ainda em faturacao, recibos ou SAF-T.

A fundacao visual esta pronta para iniciar esse primeiro ecran real, desde que a migracao continue ecran a ecran e sem alterar contratos de backend.
