# Ensaio funcional da experiencia unificada compacta

Data: 2026-07-02

Ambito: laboratorio `/documentos-lab`, experiencia `Experiencia unificada`, fase 2 compacta. Nao foram alterados backend, DTOs, entidades, migrations, base de dados, contratos REST ou o modulo real `/documentos`.

## Resultado geral

O ensaio confirmou que a fase 2 compacta continua funcional com a sidebar presente, totais compactos e sem overflow horizontal nos viewports testados. Foram encontrados e corrigidos problemas funcionais de foco/navegacao:

- ao entrar na fase 2, o foco ficava no `BODY`; passou a ir para o primeiro campo da linha ativa;
- os campos compactos de unidade, armazem e IVA da linha ativa passaram a integrar a sequencia de grelha/draft;
- `Enter` no ultimo campo `data-draft-input` conclui a linha ativa e cria nova linha ativa focada;
- o popover dos campos compactos deixou de ser cortado pela area da grelha.

## Evidencias

- `20-linhas-suave.png`: 20 linhas geradas em densidade Suave.
- `20-linhas-densa.png`: as mesmas 20 linhas em densidade Densa.
- `50-linhas.png`: 50 linhas geradas em Densa.
- `linha-ativa-fundo-50.png`: linha ativa e totais visiveis no fundo depois de scroll.
- `linhas-texto-intercaladas.png`: linhas comerciais e texto intercaladas.
- `texto-multilinha.png`: linha de texto com tres linhas, altura controlada.
- `detalhe-unidade.png`, `detalhe-armazem.png`, `detalhe-iva.png`: popovers compactos visiveis.
- `reordenacao-botoes.png`: reordenacao por botoes.
- `reordenacao-teclado.png`: reordenacao por `Alt+ArrowDown`.
- `1366x768.png`, `1024x768.png`, `mobile.png`: viewports principais.

## Cenarios

### Entrada na fase 2 e foco

Resultado inicial: a linha ativa existia, mas o foco ficava no `BODY`.

Correcao aplicada: `changeWizardStep("lines")`, quando em modo unificado, agenda `focusDraftLine(draftLine.id)` depois da renderizacao.

Resultado final: ao entrar em Linhas e totais, o foco fica no `SELECT` de artigo da linha ativa (`activeDraft: true`). Evidencia: `linha-ativa-fundo.png`.

### 20 linhas

Resultado: 21 linhas no DOM, incluindo 18 comerciais, 2 linhas de texto geradas e 1 linha ativa. Sem overflow horizontal. Totais: `Subtotal 974,50 EUR`, `Desc. 15,00 EUR`, `IVA 166,55 EUR`, `Total 1126,05 EUR`.

Observacao ergonomica: com geracao em massa, a grelha segue scroll de pagina coerente. A linha ativa fica no fim do documento e requer scroll quando ha muitas linhas.

### 50 linhas

Resultado: 51 linhas no DOM, incluindo 44 comerciais, 6 linhas de texto geradas e 1 linha ativa. Sem overflow horizontal. Totais: `Subtotal 2092,20 EUR`, `Desc. 35,00 EUR`, `IVA 368,76 EUR`, `Total 2425,96 EUR`.

Scroll: a pagina usa scroll vertical principal, nao scroll interno da grelha. Nao foi observada coexistencia confusa entre dois scrolls verticais. Evidencia: `50-linhas.png` e `linha-ativa-fundo-50.png`.

### Linhas de texto

Resultado: as linhas de texto tem apenas uma `textarea` editavel. Os restantes campos comerciais nao recebem foco nessas linhas. As linhas de texto permanecem intercaladas e visualmente distinguiveis.

Multilinha: texto com tres linhas manteve altura controlada, com primeira linha de texto a cerca de 54 px. Evidencia: `texto-multilinha.png`.

Totais: as linhas de texto nao alteraram os totais.

### Reordenacao

Por botoes: a linha movida trocou de posicao atraves do seu `data-line-id`, mantendo identidade local e renumeracao visual. Totais inalterados.

Por teclado: `Alt+ArrowDown` moveu a linha selecionada, preservou o `data-line-id` ativo e manteve totais inalterados.

Limite: drag and drop nao foi validado de forma conclusiva no browser integrado; a validacao robusta ficou feita por botoes e teclado.

### Campos compactos

Unidade, armazem e IVA abriram popover visivel, sem clipping no viewport. Os detalhes apareceram com codigo, descricao e, quando aplicavel, detalhe de taxa. Evidencias: `detalhe-unidade.png`, `detalhe-armazem.png`, `detalhe-iva.png`.

### Densidade Suave versus Densa

Suave: leitura mais confortavel, mas ocupa mais altura.

Densa: reduz ligeiramente a altura total e manteve texto legivel, foco visivel, botoes e campos compactos operaveis. Nao foram observadas sobreposicoes.

### Viewports

- `1366x768`: sem overflow horizontal; fase 2 funcional; massa de 20/50 linhas validada.
- `1024x768`: sem overflow horizontal (`scrollWidth` abaixo do `clientWidth`); linha ativa nao estava visivel no ponto de scroll capturado com 50 linhas, mas continua acessivel por scroll de pagina.
- `1440x900`: sem overflow horizontal com 50 linhas.
- `1920x1080`: sem overflow horizontal com 50 linhas.
- `430x932`: sem overflow horizontal; botoes principais continuam disponiveis, incluindo voltar ao cabecalho, linha de texto, guardar rascunho, emitir e controlos de linha.

## Limitacoes restantes

- A automacao prolongada do browser integrado foi instavel em loops longos de introducao por teclado, chegando a reiniciar a sessao. Por isso, o ensaio intensivo de 20 linhas 100% por teclado foi substituido por validacao de foco/Enter em passos curtos e por volume gerado pelos controlos do laboratorio.
- A selecao nativa do artigo por `selectOption` foi instavel no browser integrado. O foco e a sequencia de campos foram confirmados, mas a metrica completa de tempo/cliques/teclas para 20 linhas manuais nao ficou fiavel.
- Drag and drop visual nao ficou validado conclusivamente; recomenda-se novo ensaio manual antes de promover essa interacao como principal.

## Validacao tecnica

- `tsc -b`: OK.
- `vite build`: OK.
- `docker-compose --env-file .env.demo -f compose.demo.yaml up -d --build frontend`: OK.
- `git diff --check`: executar no fecho da tarefa.
- `git status`: executar no fecho da tarefa.
