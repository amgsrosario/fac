# Lookups remotos de Clientes e Artigos

Os endpoints `/api/clientes/lookup` e `/api/artigos/lookup` são separados das listagens principais. Aceitam `search`, `page` (0), `size` (20, máximo 50), `sort` repetível, `inativo`, `filter.<campo>` e `context`. `ids` repetível é reservado à hidratação dos IDs efetivamente selecionados (máximo 50 por pedido); não percorre catálogos.

## Matriz da linguagem existente

| Sintaxe | Entidade/contexto | Campo frontend | Expressão backend | Semântica |
| --- | --- | --- | --- | --- |
| texto, `*texto` | editor | campos de pesquisa abaixo | OR entre os mesmos campos | contém |
| `^texto`, `=texto`, `!texto`, `$texto` | editor/global | campos de pesquisa | predicados parametrizados | começa, igual, não contém, termina |
| `nome:`, `nif:`, `email:`, `localidade:`, `id:` | Cliente/editor | campo homónimo | `cliente.<campo>` | qualificado; aceita os cinco operadores |
| `telefone:`, `telemovel:`, `telemóvel:` | Cliente/editor | tel e tm | `cliente.tel`, `cliente.tm` | OR entre ambos |
| `codigo:`, `id:` | Artigo/editor | codigo | `artigo.codigo` | aliases do mesmo campo |
| `descricao:` (também descrição) | Artigo/editor | descricao | `artigo.descricao` | qualificado |
| `familia:` (também família) | Artigo/editor | familiaId | `artigo.id_familia` | inclui `familia:=12` |
| `unidade:`, `pvp:` | Artigo/editor | unidade, pvp | unidade, número sem zeros decimais finais | qualificado |
| `iva:` | Artigo/editor | rótulo calculado por ivaCompactLabel | CASE parametrizado a partir do catálogo IVA ativo | percentagem extraída da descrição ou abreviatura do ID; ID completo se sem taxa ativa |
| `filter.<campo>=expressão` | diálogo editor | coluna visível | whitelist da entidade | filtros combinados por AND; valor bruto da coluna, não o rótulo renderizado |
| `clientes:`, `artigos:` | GlobalSearch | grupo | interpretado no frontend | escolhe o endpoint; sem qualificadores internos de campo |
| texto global de Cliente | GlobalSearch | id, nome, nif, localidade, email | mesmos campos | operadores; sem adicionar telefone/email1 |
| texto global de Artigo | GlobalSearch | codigo, descricao, familiaId, unidade | mesmos campos | operadores; sem adicionar abreviatura |
| texto literal | Listagens | searchText concatenado | mesmos campos e separadores | contains minúsculo, sem gramática nem remoção de acentos |

Qualificadores reconhecidos são combinados por AND. As restantes palavras compõem um único termo global; nesse termo, qualquer campo pode corresponder. `!` mantém o OR entre campos da implementação original, não é convertido em NOT de toda a pesquisa. Qualificadores desconhecidos e símbolos não reconhecidos continuam texto literal. Não existem comparadores numéricos `<`/`>` nesta linguagem.

Cliente/editor pesquisa nome, NIF, localidade, email, tel/tm e ID. Artigo/editor pesquisa código, descrição, família, unidade, PVP e rótulo IVA. Tipo e estado não eram qualificadores: estado é o parâmetro `inativo`; tipo é devolvido para preservar defaults do editor.

Normalização do editor/global: NFD, remoção das marcas U+0300–U+036F, trim e minúsculas. `%`, `_` e barras são literais, escapados nos parâmetros SQL. Campos de filtro, contexto, direção/campo de ordenação e paginação inválidos devolvem 400. Nunca são interpoladas propriedades fornecidas pelo utilizador.

Filtros por coluna permitidos correspondem aos campos explícitos das projeções, incluindo nome/NIF/localidade/telefone/código postal/país no Cliente e código/descrição/família/unidade/PVP/IVA/retenção/estado/observações no Artigo. Só filtros das colunas visíveis são enviados, como no componente original.

## Paginação e seleção

O componente pede uma página de 20 linhas após 300 ms. Mudar pesquisa/filtros/ordenação repõe a página zero; respostas obsoletas são abortadas e ignoradas. O diálogo pagina no servidor, sem filtrar ou ordenar uma página parcial. A seleção fica separada das linhas da página. Os defaults provêm do objeto selecionado; a edição hidrata apenas os IDs do documento em lotes.

A migration V13 cria exclusivamente `lookup_pt_natural` (ICU, `pt-u-kn-true`) para a ordenação explícita do diálogo, equivalente à ordenação numérica do `Intl.Collator('pt-PT', { numeric: true })`. Não altera tabelas, índices, dados ou a ordenação das listagens principais. Requer PostgreSQL com ICU (validado no PostgreSQL 16.3 descartável).

Cada página usa duas consultas escalares (contagem e conteúdo). Artigos/editor lê adicionalmente o pequeno catálogo de IVA numa consulta, nunca por artigo. A normalização pode requerer scan no servidor; não há benchmark nem alegação de pesquisa indexada. A resposta e o carregamento de entidades permanecem limitados à página.

## Inventário dos preloads

- Removidos: Clientes/Artigos do DraftDocumentEditor, filtros de Listagens e GlobalSearch.
- Mantido: preload de documentos do GlobalSearch, fora do âmbito Clientes/Artigos.
- Mantido: os preloads do formulário sem percurso alcançável em DocumentsView.tsx e do editor antigo em DocumentosView.tsx; configuração geral também fica fora do âmbito.
- Endpoints fiscais/documentais, cálculo de extratos/pendentes, permissões, exportações e Visual V2 não foram alterados.

## Verificação

Backend: `mvn '-Dtest=DocumentoComercialControllerTests#lookupsRemotos*,TestDatabaseSafetyValidatorTests' test`, exclusivamente com FAC_TEST_DATASOURCE_* apontado para base descartável.
Frontend: `npm run test:lookups` (Playwright, API simulada, 81 clientes e 81 artigos), seguido de `npm run build`. O browser configurado por omissão é Edge; `PLAYWRIGHT_CHANNEL` permite usar outro canal instalado.
