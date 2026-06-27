# FAC UI - Arquitetura comercial

Data: 2026-06-27  
Branch: `feature/ui-commercial`  
Baseline preservada: `V60-ui-poc` em `d415c06 V59`

## 1. Decisoes tomadas

A evolucao comercial passa a ter uma solucao mista:

- PrimeReact fornece comportamento, acessibilidade, overlays, dialogs, inputs, selecao, mensagens e futura base para tabelas/paginacao.
- A identidade FAC continua responsavel por shell, paleta, espacamento, hierarquia, composicao e linguagem.
- O modo `legacy` permanece predefinido.
- O modo `commercial` abre apenas um laboratorio tecnico isolado.
- Nenhum ecran funcional foi migrado para PrimeReact nesta tarefa.
- Nenhum endpoint, regra fiscal, permissao, PDF, backend, base de dados ou migracao Flyway foi alterado.

## 2. Arquitetura PrimeReact + FAC

PrimeReact foi configurado em `frontend/src/ui/fac/theme/primeReactConfig.ts`.

A decisao de styling foi usar PrimeReact em modo `unstyled`, sem tema generico global. Isto evita que a UI atual adquira aspeto de template PrimeReact e permite que a camada FAC controle a identidade visual.

Imports globais em `main.tsx`:

- `primeicons/primeicons.css`
- `ui/fac/theme/fac-tokens.css`
- `ui/fac/theme/fac-prime.css`
- `styles.css`

`styles.css` continua presente e a UI legacy continua a usa-lo. Os novos estilos FAC comerciais ficam escopados principalmente em `.fac-ui` e classes especificas da fundacao.

## 3. Dependencias instaladas

| Dependencia | Versao instalada | Razao |
| --- | --- | --- |
| `primereact` | `10.9.8` | Versao estavel consultada no npm, compativel com React 18, 19 e 17 segundo peer dependencies. |
| `primeicons` | `7.0.0` | Icones oficiais PrimeReact. |
| `react-router-dom` | `7.18.0` | Routing real, compativel com React >=18. |

Alternativas recusadas nesta tarefa:

- PrimeFlex: recusado para evitar framework utilitario adicional.
- Tailwind: recusado para nao trocar a fundacao visual.
- Bootstrap: recusado para evitar identidade visual generica.
- templates administrativos PrimeReact: recusados por risco de descaracterizar a FAC.
- bibliotecas de estado/formularios/tabelas alternativas: fora do ambito.

## 4. Provider e configuracao

`main.tsx` envolve a aplicacao com:

- `PrimeReactProvider`
- `BrowserRouter`

Configuracao FAC:

- `locale: "pt-FAC"`
- `ripple: false`
- `inputStyle: "outlined"`
- `appendTo: "self"`
- `hideOverlaysOnDocumentScrolling: false`
- `unstyled: true`

A localizacao portuguesa inicial e registada via `addLocale`. Esta configuracao podera ser expandida quando Calendar, DataTable e filtros PrimeReact forem adotados.

## 5. Tokens FAC

Ficheiro:

```text
frontend/src/ui/fac/theme/fac-tokens.css
```

Tokens preservados:

- `--fac-bg`
- `--fac-surface`
- `--fac-sidebar`
- `--fac-border`
- `--fac-text`
- `--fac-text-muted`
- `--fac-petrol`
- `--fac-gold`
- `--fac-client-soft`
- `--fac-document-soft`
- `--fac-product-soft`
- `--fac-treasury-soft`

Tokens acrescentados:

- espacamentos;
- raios;
- altura de controlos;
- foco;
- sombra suave;
- estados erro/sucesso;
- breakpoints;
- z-index.

A paleta nao foi redesenhada.

## 6. Estrutura de diretorios

```text
frontend/src/ui/
  fac/
    capabilities/
    components/
    data/
    feedback/
    forms/
    layout/
    navigation/
    responsive/
    theme/
  legacy/
  commercial/
```

As pastas `data`, `forms` e `navigation` ficaram preparadas para evolucao posterior. Os ficheiros legacy existentes nao foram movidos.

## 7. Componentes FAC criados

Componentes iniciais:

- `FacButton`
- `FacInputText`
- `FacSelect`
- `FacDialog`
- `FacConfirmDialogHost`
- `confirmFacDialog`
- `FacToastProvider`
- `useFacToast`
- `FacStatusBadge`
- `FacLoadingState`
- `FacErrorState`
- `FacEmptyState`

Estes componentes encapsulam PrimeReact quando aplicavel, aceitam `className` quando util e nao contêm regras de negocio.

Nao foi criado `FacDataTable` completo porque paginacao, filtros, lazy loading e contratos de backend exigem decisao propria.

## 8. Estrategia de routing

`react-router-dom` foi introduzido com `BrowserRouter`.

No modo `legacy`, a aplicacao atual continua a funcionar atraves de `Root`, `LoginView` e `App`, sem migracao de vistas.

No modo `commercial`, o router serve o laboratorio tecnico:

```text
/ui-lab
```

Qualquer outra rota em modo comercial redireciona para `/ui-lab`.

Esta abordagem prepara URLs reais sem reescrever `App.tsx` nem alterar permissoes.

## 9. Estrategia desktop/mobile

Ficheiros:

- `frontend/src/ui/fac/responsive/device.tsx`
- `frontend/src/ui/fac/layout/DesktopShell.tsx`
- `frontend/src/ui/fac/layout/MobileShell.tsx`

Criado:

- `useDeviceClass()`
- `DesktopOnly`
- `MobileOnly`
- `ResponsiveSlot`
- `DesktopShell`
- `MobileShell`

Classes:

- `mobile`: ate 767 px;
- `tablet`: 768 a 1023 px;
- `desktop`: 1024 px ou superior.

A deteccao reage a `resize`, `orientationchange` e `matchMedia`. Nao depende de user agent nem de leitura unica de `window.innerWidth`.

## 10. Estrategia de capacidades

Ficheiro:

```text
frontend/src/ui/fac/capabilities/capabilities.ts
```

Tipos:

- `ProductProfile`
- `Capabilities`
- `FeatureKey`

Perfis preparados:

- `SERVICES_SIMPLE`
- `FULL`

Helper:

```tsx
hasCapability("ADVANCED_ARTICLE_FIELDS")
```

Capacidades nao substituem permissoes:

- permissoes controlam quem pode fazer;
- capacidades controlam o que uma edicao disponibiliza;
- backend devera validar capacidades numa fase posterior;
- a base de dados sera a mesma;
- valores predefinidos serao decididos ecran a ecran.

Nesta tarefa nenhum campo real foi ocultado.

## 11. Modo legacy/commercial

Variavel:

```text
VITE_FAC_UI_MODE
```

Valores:

- `legacy`: modo predefinido, usa a UI atual;
- `commercial`: abre a fundacao isolada em `/ui-lab`.

O seletor nao fica visivel para o utilizador final e nao usa `localStorage`.

## 12. Como arrancar

Legacy predefinido:

```bash
cd frontend
npm run dev
```

Legacy explicito:

```bash
cd frontend
set VITE_FAC_UI_MODE=legacy
npm run dev
```

Commercial lab:

```bash
cd frontend
set VITE_FAC_UI_MODE=commercial
npm run dev
```

Abrir:

```text
http://localhost:5173/ui-lab
```

Em PowerShell:

```powershell
$env:VITE_FAC_UI_MODE="commercial"
npm run dev
```

## 13. Regras de preservacao da base de dados

A arquitetura preparada assume:

- a base de dados nao muda por troca de edicao;
- upgrades nao recriam nem substituem a base;
- registos historicos continuam validos;
- campos ocultos no futuro receberao valores predefinidos, neutros ou genericos definidos por ecran;
- permissoes e regras fiscais continuam independentes da camada visual.

## 14. Pagina tecnica isolada

Ficheiro:

```text
frontend/src/ui/commercial/UiFoundationLab.tsx
```

Demonstra:

- botao primario;
- botao secundario;
- botao destrutivo;
- input;
- select;
- toast;
- dialog;
- loading;
- empty state;
- error state;
- status badge;
- desktop container;
- mobile/tablet/desktop responsive slot.

Nao usa dados reais e nao chama endpoints.

## 15. Validacao executada

`npm view` foi usado para confirmar versoes compativeis.

Instalacao:

```text
npm install primereact@10.9.8 primeicons@7.0.0 react-router-dom@7.18.0
```

O comando `npm run build` falhou no ambiente local por problema no shim global do npm:

```text
Cannot find module ... npm-cli.js
```

Foi executado o equivalente com binarios locais:

```powershell
& '.\node_modules\.bin\tsc.cmd' -b
& '.\node_modules\.bin\vite.cmd' build
```

Resultado: TypeScript e Vite build passaram.

Bundle observado:

- JS principal: cerca de 352.54 kB;
- CSS principal: cerca de 37.10 kB;
- PrimeIcons adiciona fontes e SVG aos assets.

## 16. Riscos

- Bundle aumentou por PrimeReact, React Router e PrimeIcons.
- `BrowserRouter` foi introduzido globalmente; embora a UI legacy nao use rotas, deve ser validada manualmente em dev.
- PrimeReact em modo `unstyled` exige manutencao propria dos estilos FAC para cada componente adotado.
- O laboratorio ainda nao valida DataTable, paginacao, filtros nem overlays complexos.
- Capturas automaticas da baseline nao foram feitas nesta tarefa.

## 17. Limitacoes

- Nao ha menu mobile definitivo.
- Nao ha migracao real de ecras.
- Nao ha `FacDataTable`.
- Nao ha regras de capacidades aplicadas a campos reais.
- Nao ha validacao manual completa em browser registada nesta execucao.
- Nao ha testes frontend existentes alem do build TypeScript/Vite.

## 18. Plano de migracao ecran a ecran

Ordem recomendada:

1. Componentes comuns e tokens.
2. Shell comercial desktop/mobile.
3. Login e estados globais.
4. Dashboard.
5. Artigos, por ser CRUD mais simples que documentos.
6. Clientes.
7. Documentos comerciais.
8. Tesouraria.
9. Listagens/extratos.
10. Empresa/configuracao.
11. Tabelas tecnicas e fiscais.
12. Utilizadores/auditoria.

O primeiro ecran recomendado para migracao funcional e `Artigos`, porque tem CRUD claro, permissoes simples e baixo risco fiscal comparado com documentos e tesouraria.

## 19. Proximos passos

- Validar manualmente `legacy` em browser com backend ativo.
- Validar `/ui-lab` em modo `commercial`.
- Recolher capturas da baseline.
- Definir API final de `FacDataTable`.
- Definir politica de dialogs/toasts antes de migrar ecras.
- Definir matriz inicial de capacidades por edicao com o proprietario.
- Definir quais campos de servicos simples ficam ocultos e quais valores predefinidos recebem.
