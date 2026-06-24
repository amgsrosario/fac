# 15 - PDF fiscal final e robustez multipágina

## Fecho da pendência de cabeçalho nas páginas de continuação

### Causa da limitação anterior

O documento comercial é produzido por `DocumentoComercialPdfService` através de HTML/CSS e OpenHTMLtoPDF/PDFBox. O cabeçalho documental completo estava no fluxo normal do HTML e, por isso, era desenhado apenas uma vez. A tabela de linhas já utilizava `-fs-table-paginate: paginate` e `display: table-header-group`, repetindo as colunas, mas não existia um elemento CSS `running` para identificar o documento nas páginas seguintes.

O número total de páginas não é calculado previamente pela aplicação. É determinado pelo renderer e disponibilizado pelos contadores CSS `counter(page)` e `counter(pages)`.

### Solução aplicada

Foi preservado o cabeçalho completo da primeira página. Foi acrescentado um cabeçalho compacto de continuação, colocado na margem superior das páginas a partir da segunda através de `position: running(...)` e `@top-center`.

O cabeçalho de continuação inclui:

- emitente e NIF;
- indicação `CONTINUAÇÃO`;
- tipo, série e número do documento;
- data do documento;
- cliente e NIF;
- página atual e total de páginas.

As páginas de continuação usam uma margem superior própria de 29 mm; a primeira mantém 16 mm. O cabeçalho das colunas continua a ser repetido pelo mecanismo nativo da tabela. Foi ainda aplicado `page-break-inside: avoid` às linhas para impedir que descrições longas dividam uma linha entre duas páginas.

Totais, resumo de IVA, observações, ATCUD, QR Code e rodapé permanecem no bloco final existente e não foram alteradas regras fiscais, cálculos, modelo de dados ou payload QR.

### Ficheiros alterados

- `src/main/java/com/ar2lda/fac/service/DocumentoComercialPdfService.java`
- `src/test/java/com/ar2lda/fac/DocumentoComercialPdfServiceTests.java`
- `docs/15 - PDF fiscal final e robustez multipágina.md`

### Testes automáticos

Os novos testes validam:

- PDF de uma página sem cabeçalho de continuação;
- documento de 35 linhas com exatamente duas páginas;
- documento com descrições longas e cinco páginas;
- presença do número, emitente e cliente em todas as páginas;
- `CONTINUAÇÃO` e paginação apenas a partir da segunda página;
- repetição das colunas em páginas com linhas;
- totais e ATCUD apenas no bloco final;
- imagem QR na última página;
- ausência de página final vazia;
- geração com número de linhas próximo do limite, cliente longo, observações, taxas de IVA diferentes e documento anulado.

### Validação visual

Foram produzidos e renderizados a 120 DPI:

| Artefacto | Linhas | Páginas |
|---|---:|---:|
| `fac-documento-1-pagina.pdf` | 1 | 1 |
| `fac-documento-2-paginas.pdf` | 35 | 2 |
| `fac-documento-3-ou-mais-paginas.pdf` | 90, com descrições longas | 5 |

Foram inspecionadas todas as oito páginas. Confirmaram-se margens A4, alinhamento, espaçamento, cabeçalhos, continuidade das linhas, totais, ATCUD, QR Code, rodapé, ausência de cortes, sobreposições e páginas vazias.

### Limitações remanescentes

A validação estrutural não substitui inspeção visual quando forem alteradas fontes, dimensões, colunas ou conteúdo variável de dimensão excecional. Por esse motivo, os três artefactos de referência são mantidos em `output/pdf/` para regressão visual. Não permanece nenhuma limitação conhecida relativa ao cabeçalho multipágina desta pendência.
