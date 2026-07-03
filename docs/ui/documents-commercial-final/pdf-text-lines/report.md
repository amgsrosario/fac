# FAC - Etapa 3 - PDF e impressao com linhas TEXTO

Data: 2026-07-03
Branch: feature/ui-commercial

## Inventario inicial

- Estado inicial do working tree: continha as alteracoes acumuladas das Etapas 1 e 2, ainda sem commit.
- Servico de PDF comercial: `DocumentoComercialPdfService`.
- DTO/modelo de impressao: `DocumentoComercialImpressaoDto`.
- Servico de impressao/leitura/diagnostico: `DocumentoComercialService`.
- Snapshot fiscal comercial: construido em `DocumentoComercialService.buildImpressao` e usado por `FiscalQrService`.
- Biblioteca PDF: OpenHTMLToPDF com PDFBox (`openhtmltopdf-pdfbox`).
- Paginacao atual: HTML/CSS com `@page`, cabecalho de continuacao por elemento running, tabela com `-fs-table-paginate: paginate`, `thead` repetido e blocos finais com `page-break-inside: avoid`.
- Leitura ordenada das linhas: `findByDocumentoComercialIdOrderByNumeroLinha`.
- Diagnostico HTML: gerado por `DocumentoComercialService.getDiagnosticoHtml`.
- Diferenca entre superficies:
  - `/impressao` devolve DTO JSON ordenado para consumo/preview;
  - `/pdf` renderiza PDF fiscal a partir do DTO de impressao;
  - `/diagnostico` devolve JSON de conferencia;
  - `/diagnostico/html` devolve HTML tecnico de conferencia.

## Alteracoes

- Linhas `TEXTO` no PDF passaram a usar uma linha propria:
  - numero de linha discreto;
  - descricao em celula unica com `colspan`;
  - sem artigo, quantidade, preco, desconto, IVA ou total;
  - sem celulas comerciais vazias pesadas;
  - quebra de texto compatibilizada com OpenHTMLToPDF.
- Diagnostico HTML passou a representar linhas `TEXTO` como descricao de largura total, sem simular artigo.
- Mantida a ordem por `numeroLinha`.
- Mantidos os totais, QR, snapshot fiscal e regras de emissao sem alteracao funcional.

## Testes acrescentados/reforcados

- Linha `TEXTO` sem valores comerciais no PDF.
- Linhas `TEXTO` no inicio, consecutivas, entre comerciais e no fim.
- Paginacao com muitas linhas `TEXTO` e comerciais alternadas.
- Artefacto visual adicional em `target/pdf-validation/fac-documento-linhas-texto.pdf`.

## Limitacoes

- A descricao continua limitada ao campo atual da base de dados: `VARCHAR(80)`.
- A validacao visual automatica usa extracao de texto PDFBox; nao substitui revisao visual manual do PDF quando houver alteracoes de layout mais profundas.
- Nao foi alterada a UI real `/documentos`.
- Nao foi criada nova migration nesta etapa.
