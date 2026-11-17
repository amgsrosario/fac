# Fontes e rastreabilidade — FR/EUR identidade

17/11/2026. Leitura dirigida; sem testes, SQL ou Docker. Referência integrada e hashes dos ficheiros consultados: SOURCE_REGISTER.json. IDs abaixo são locais ao dossier, não novas decisões administrativas.

## Decisões e evidências Git

| ID | Fonte directamente consultada | Alcance / limite |
| --- | --- | --- |
| D00 | [Ordem original de preparação](AUTHORIZATION.md), recebida neste chat | Apenas preparação, revisão independente e registo de merge; implementação fiscal e anulação fora deste mandato |
| D01 | docs/governance/INSTITUTIONAL_MEMORY.md, D-CAT-2A, §§Facturação e limites; ADR-003; fonte original H1.1 em docs/governance/sources/2026-10-23_D-CAT-2-H1.1_ordem-recebida.md | FT/FR partilham tipologia interna FT; FR quita na emissão. Híbrido e revisões imutáveis aprovados; não homologa projecção externa por cópia do código interno |
| D02 | docs/fiscal/catalog/H1_1_FT_SPEC.md §§1–2; D-CAT-2_MATRIX.md | Exclusão de FR na primeira fatia governada, não cancelamento do produto. A matriz de 22/10 é um registo histórico; reservas entretanto superadas não são reabertas |
| D03 | docs/planning/fr-eur-inactive/AUTHORIZATION.md, MANDATE_CLARIFICATION_2026-11-17.md, REVIEW_CLOSEOUT.md; original Claude no arquivo | Aprovação do corte económico interno 0/0/0. Original/sidecar SHA-256 46c8904275d9bb17b908a1ee3dfd80f3f1ad4054a8c209a031910a59eff8a9a1; identidade/projecção externa FR e anulação reservadas. Este novo plano ainda não foi revisto |
| D04 | Git e API GitHub do PR #58, HEAD remoto de feature/visual-redesign | merged=true; merge e6973910e1652dea85acdc9e0848c7683c34e6d5, 17/11/2026 19:16:47 UTC; head b8b0c0f. Merge tree idêntica à entrega. O registo local 4ad1edf é posterior e não integrado |
| D05 | docs/fiscal/h2-06/PROPOSAL.md:47–57,90–107; contrato SAF-T integrado | Âmbito série/tipo e datas/cadeia de assinatura, sem provedor produtivo. Record Signature não tem keyVersion. Não reabrir D-SIGN nem implementar o exportador |
| D06 | Documentos M4-12/R1 locais em 0337ff127380207a2e9ecd81b1dfa74ac6bcf8f1, registados no JSON | UnitPrice derivado, Settlement futuro, P12-01/02/03 reservados. Não integrados; não se atribui homologação/decisão a essas propostas |

## Código e prova disponível na base e697391

| ID | Caminho / localização | Facto observado |
| --- | --- | --- |
| C01 | src/main/java/com/ar2lda/fac/fiscal/catalog/FiscalCatalog.java:24–30,49–96,103–120; FtDeferredSemantics.java:12–41; resources/fiscal/catalog.json | Manifesto vazio, ponteiro ftDeferred e semântica FT só não imediata; ainda não representa a projecção FR |
| C02 | TipoDocumentoService.java:45–61,82–105; resources/db/migration/V28__governed_ft_configuration_contract.sql | Novas FT diferidas vinculadas; guardas de revisão/configuração excluem imediato; FT imediato sintético actual não fica vinculado. Java e SQL têm de evoluir coerentemente |
| C03 | financial/FinancialFrIssuer.java:48–111; V30__inactive_immediate_invoice.sql; JdbcFinancialQueries.java:120–121 | Acto único e data documental já implementados; não é configuração/projecção fiscal FR homologada |
| C04 | service/DocumentoComercialService.java:356–405,645–647; model/DocumentoComercial.java:486–525; mapper/DocumentoComercialMapper.java:14–22 | Ordem de numeração/snapshot/assinatura e número com prefixo interno; snapshot hoje copia codigoFiscal; mapper privilegia snapshot |
| C05 | model/Serie.java e SerieId.java; service/SerieService.java:103–123; AtcudService.java:10–26 | Chave interna, bloqueio/contador e composição actual. No percurso SerieService consultado, código AT é dado fornecido por CRUD autorizado; não foi demonstrada comunicação operacional à AT |
| C06 | service/QrFiscalPayloadBuilder.java:40–48,160; DocumentoComercialPdfService.java:156–165,195–205,299–321 | D usa código actual/fallback; G/H persistidos; título/identificador do PDF precisam de contrato FR explícito. O espaço de ATCUD: continua reservado |
| C07 | service/FiscalDocumentSignatureSource.java:17–22 | Assinatura integral/certificado ligados ao documento; falta keyVersion na interface. Não confundir Q/R com Hash/HashControl |
| T01 | fiscal/catalog/FinancialFrIntegrationTests.java:149–284; financial/FinancialFrIsolationTests.java; fr-eur-inactive/VALIDATION.md | Prova económica e inactividade já revista; não prova projecção FR externa |
| T02 | fiscal/catalog/FtIdentityJourneyTests.java:111 em diante; testinfra/PdfQrInspection.java; m4-04-ft-identity/VALIDATION.md | Técnica de comparação identidade persistida/PDF/QR reutilizável. Oráculo e aprovação FT não são automaticamente FR |
| T03 | m4-09-qr-amounts/VALIDATION.md e testes QR/PDF/Series/catalog existentes | Seleccionar apenas cobertura afectada em futuro mandato; não repetir nesta preparação |

Caminhos C02–C07 abreviam src/main/java/com/ar2lda/fac/ excepto os resources explicitados; caminhos T01/T02 abreviam src/test/java/com/ar2lda/fac/. O JSON dá o caminho completo e hash por commit, incluindo ficheiros locais não integrados. Referências por método permitem localizar eventuais deslocações de linha.

## Fontes oficiais reutilizadas e conferência actual

- **N01:** [Portaria 302/2016](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_302_2016.pdf), Anexo I §§4.1.4.1, .8 e .20.6. InvoiceType admite FR; InvoiceNo é uma identificação distinta. O grupo de pagamento da factura tem fonte própria.
- **N02:** [AT, especificações QR v1.1, Outubro 2020](https://info.portaldasfinancas.gov.pt/pt/apoio_ao_contribuinte/Negocios/Faturacao/Regras_mecanismos_comunicacao/Comunicacao_de_series_ATCUD/Codigo_barras_bidimensional/Documents/Especificacoes_Tecnicas_Codigo_QR.pdf), tabela D/G/H, páginas físicas PDF 7–8. Não confundir o exemplo FT com uma regra de preenchimento universal.
- **N03:** [Portaria 195/2020](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_195_2020.pdf), arts.2–4, série/tipo, sequencial e ATCUD. Nenhuma dispensa transitória é usada como alternativa ao valor real.

As três URLs foram consultadas por browser em 17/11/2026. Foram reutilizados os três PDFs originais M04, com SHA-256 conferido, em vez de repetir a recolha normativa completa. OFFICIAL_SOURCES.json fixa URL, versão/localização, origem, dimensão e hash. Uma tentativa de novo download directo do primeiro PDF falhou com ConnectionResetError antes de gravar o ficheiro; não se afirma um novo hash do servidor nem igualdade byte a byte da resposta HTTP corrente. As cópias reutilizadas são a evidência preservada; a leitura online reconfirmou as passagens usadas. Isto não é parecer fiscal de aplicabilidade integral ao emitente/certificação.

## Pacote de entrega

Cópias de fontes oficiais, MERGE.json e documentos deste plano serão preservados em `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Identidade/Preparacao-2026-11-17`, com manifesto SHA-256 conferido e identificado no relatório de entrega. O código permanece consultável pelo commit base fixado; o plano não copia nem altera a implementação FR. Nenhuma fonte de sessão privada foi consultada.
