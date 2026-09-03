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

Gramática validada: **Entidade + listagem**.

- Terceira implementação oficial da Visual v2, migrada, validada e aprovada como baseline.
- Contexto, pesquisa, toolbar, tabela, seleção, detalhe, estados e paginação reutilizam aproximadamente 95% do sistema comum.
- Preço, peso e indicadores usam os helpers numéricos comuns.
- A variante genérica `tuuli-entity-metrics-three` suporta contextos com três indicadores.
- O único CSS específico relevante é a largura mínima da tabela, necessária ao domínio/layout de Artigos.
- A validação visual e responsiva abrangeu desktop, 1100 px, 700 px e 393 px.
- Os smoke tests de Documentos e Clientes foram aprovados sem regressões observadas.
- A mudança efetiva para a página 2 não foi exercitada porque o conjunto atual contém apenas oito artigos; a implementação e os controlos de paginação foram preservados.

## Recebimentos

Composição validada: **Híbrida transacional**.

- Quarta implementação oficial da Visual v2, abrangendo listagem, detalhe documental e workspace de distribuição.
- Duas coleções relacionadas coexistem sem cards exteriores; a Conta Corrente usa duas linhas funcionais deliberadas para filtros e ferramentas.
- Tabelas, paginação, estados e formatação monetária reutilizam as primitivas e helpers comuns.
- O detalhe reutiliza a linguagem documental existente; o workspace organiza a complexidade da distribuição sem ser promovido a gramática global.
- Estados Aberto/Liquidado/Vencido mantêm semântica funcional própria.

## Dashboard

Gramática validada: **Analítico**.

- Quinta implementação oficial da Visual v2, com período, métricas, evolução, ranking de clientes e atalhos operacionais.
- Métricas e áreas analíticas usam espaço, títulos e divisores em vez de widgets pesados.
- A baixa densidade de dados é preservada sem decoração compensatória.
- O mobile mantém a hierarquia e reduz altura desnecessária; os atalhos empilham sem cards.
- A numeração usa os helpers comuns e mantém alinhamento e leitura imediata.

## Listagens

Gramática provável: **Listagem simples** ou **Analítico**, conforme o relatório.

- Rever filtros, exportações, toolbars, tabelas e paginação por tipo de listagem.
- Existem estilos locais de tabela e formatação monetária repetida.
- Distinguir controlos de consulta de ações operacionais antes da migração.

## Login

- Não pertence às quatro gramáticas operacionais.
- Rever apenas tokens de superfície, controlo, focus, radius e sombra numa tarefa própria.
- Preservar simplicidade, identidade e fluxo de autenticação.
