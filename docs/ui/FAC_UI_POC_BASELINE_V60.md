# FAC UI POC Baseline V60

Data: 2026-06-27  
Branch de origem: `feature/ui-commercial`  
Commit de referencia: `d415c06 V59`  
Tag de preservacao: `V60-ui-poc`

## Objetivo

Este documento reforca a salvaguarda da UI POC atual antes da evolucao para a UI comercial. A preservacao principal continua a ser Git, atraves da tag `V60-ui-poc`. Este documento acrescenta uma referencia tecnica e funcional para comparacao futura.

## Estado Git registado

Verificacoes efetuadas:

```text
git status
git branch --show-current
git log -1 --oneline
git tag --list
git tag --points-at HEAD
```

Resultado:

- branch: `feature/ui-commercial`
- commit: `d415c06 V59`
- tag em HEAD: `V60-ui-poc`
- working tree inicial: continha `docs/ui/` por documentacao de diagnostico criada antes desta tarefa
- tag `V60-ui-poc`: existente e nao alterada

## Estrutura geral da UI atual

A UI atual e uma aplicacao React/Vite sem router real. O modo de navegacao principal e controlado por estado local em `App.tsx`, atraves de vistas como `Dashboard`, `Clientes`, `Documentos`, `Artigos`, `Tesouraria`, `Listagens`, `ImportExport`, `Auditoria` e `Configuracao`.

A shell atual e composta por:

- sidebar fixa com marca FAC e menu;
- topbar com titulo, utilizador, perfil/codigo e logout;
- workspace central com heroes, metricas, paineis, tabelas, formularios e detalhes laterais;
- CSS global `fac-*`, com tokens proprios e identidade visual discreta.

## Ficheiros principais

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

## Dependencias atuais da baseline

Na baseline V60 original:

- React `^18.3.1`
- React DOM `^18.3.1`
- Vite `^6.0.1`
- TypeScript `^5.6.3`
- sem PrimeReact;
- sem React Router;
- sem biblioteca visual externa.

Depois desta tarefa, a UI legacy continua a ser o modo predefinido, mas o projeto passou a conter dependencias de fundacao comercial documentadas em `FAC_UI_ARQUITETURA_COMERCIAL.md`.

## Modo de arrancar

Modo legacy predefinido:

```bash
cd frontend
npm run dev
```

Equivalente explicito:

```bash
cd frontend
set VITE_FAC_UI_MODE=legacy
npm run dev
```

O servidor Vite usa a porta `5173` e proxy `/api` para `http://localhost:8080`.

## Modo de validar

Validacao frontend:

```bash
cd frontend
npm run build
```

Se o shim global do npm local falhar, usar os binarios locais:

```bash
cd frontend
powershell -Command "& '.\node_modules\.bin\tsc.cmd' -b; if ($LASTEXITCODE -eq 0) { & '.\node_modules\.bin\vite.cmd' build }"
```

## Principais ecras da baseline

- Login
- Dashboard
- Clientes e conta corrente
- Artigos
- Documentos comerciais
- Tesouraria, pendentes e recebimentos
- Listagens e extratos
- Importacao/exportacao
- Configuracao
- Empresa
- Utilizadores
- Tabelas
- Auditoria

## Elementos visuais a preservar

- Identidade FAC discreta e operacional.
- Sidebar + workspace.
- Paleta neutra com petrolio e dourado.
- Fundos suaves por dominio funcional.
- Paineis com bordas claras.
- Metricas compactas.
- Tabelas densas para desktop.
- Linguagem funcional orientada a faturacao, recebimentos e configuracao.
- Fluxos de diagnostico antes de emissao/anulacao.

## Elementos identificados como POC

- Navegacao por estado em vez de rotas reais.
- `App.tsx` e `styles.css` demasiado concentrados.
- Confirmacoes nativas `window.confirm`.
- Helpers repetidos por ecran.
- Formularios longos sem componentes comuns.
- CRUD tecnico de tabelas ainda com linguagem interna.
- Ausencia de pagina tecnica para componentes.
- Ausencia de experiencia mobile propria.

## Capturas da baseline

Pasta reservada:

```text
docs/ui/baseline-v60/
```

Nesta tarefa nao foram recolhidas capturas automaticas porque isso exigiria um ambiente funcional com backend, dados, autenticacao e servidor de desenvolvimento ativos. Nao foram instaladas ferramentas adicionais pesadas apenas para capturas.

Checklist manual recomendada:

- `login.png`
- `dashboard.png`
- `clientes.png`
- `artigos.png`
- `documentos.png`
- `tesouraria.png`
- `listagens.png`
- `configuracao.png`

Larguras recomendadas:

- 1440 px para desktop produtivo;
- 1024 px para desktop/tablet largo;
- 768 px para tablet;
- 375 px para referencia mobile.

## Processo para regressar a versao marcada

Para inspecionar a UI POC preservada:

```bash
git fetch --tags
git checkout V60-ui-poc
```

Para regressar ao trabalho atual:

```bash
git checkout feature/ui-commercial
```

Nao mover nem recriar a tag. A tag `V60-ui-poc` e a referencia imutavel da baseline.
