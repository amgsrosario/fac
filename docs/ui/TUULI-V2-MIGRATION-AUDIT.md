# TUULI Visual v2 - auditoria de migração

Este documento é um mapa de trabalho futuro. Não autoriza migração automática nem substitui validação funcional por módulo.

## Global

- `styles.css` ainda concentra regras antigas de painéis, tabelas, toolbars, pesquisa, radius, sombras e pesos `500/600/700`.
- `fac-prime.css` e componentes partilhados têm estados, sombras e pesos próprios que devem ser confrontados com as primitivas TUULI.
- Existem cores hardcoded, radius grandes e sombras locais em CSS global e comercial.
- A formatação numérica está repetida através de `toLocaleString`, `Intl.NumberFormat`, `toFixed` e concatenação de moeda.
- O bloco histórico `.fac-documents-v2` está inativo e deve ser removido numa tarefa isolada após aprovação da extração.

## Clientes

Gramática validada: **Entidade + listagem**.

- Segunda implementação oficial da Visual v2.
- Toolbar, pesquisa, tabela, seleção, contexto, detalhe, estados e paginação usam as primitivas comuns.
- A Conta Corrente integra a mesma linguagem tabular e os helpers numéricos comuns.
- O CSS legado remanescente pode ser revisto apenas numa limpeza futura, isolada e verificável.

## Artigos

Gramática provável: **Entidade + listagem**.

- Rever toolbar, pesquisa, tabela, seleção, ficha comercial e estado Ativo/Inativo.
- `articles.css` contém pesos `700` e badges de radius elevado.
- Confirmar unidade, preço e IVA antes de aplicar regras numéricas comuns.

## Recebimentos

Gramática provável: **Listagem simples** com contexto transacional.

- Rever filtros, toolbar, tabela de pendentes, totais e ações de recebimento.
- `PendentesView.tsx` mantém helper monetário local e concatenação direta de moedas.
- Estados Aberto/Liquidado/Vencido exigem semântica funcional própria.

## Dashboard

Gramática: **Analítico**.

- Rever métricas, grelhas, painéis, sombras, radius e pesos fortes sem converter painéis analíticos em tabelas.
- Preservar hierarquia e comparação de dados; não aplicar literalmente a composição de Documentos.
- Consolidar números apenas depois de confirmar unidades e escalas de cada indicador.

## Listagens

Gramática provável: **Listagem simples** ou **Analítico**, conforme o relatório.

- Rever filtros, exportações, toolbars, tabelas e paginação por tipo de listagem.
- Existem estilos locais de tabela e formatação monetária repetida.
- Distinguir controlos de consulta de ações operacionais antes da migração.

## Login

- Não pertence às quatro gramáticas operacionais.
- Rever apenas tokens de superfície, controlo, focus, radius e sombra numa tarefa própria.
- Preservar simplicidade, identidade e fluxo de autenticação.
