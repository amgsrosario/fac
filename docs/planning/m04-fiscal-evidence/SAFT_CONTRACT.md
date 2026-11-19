# M04 — Delimitação do contrato SAF-T (PT)

**Contrato documental proposto, NÃO fechado:** falta revisão independente e decisões aplicáveis de perfil/perímetro. Detalhe actualizado em 13-11-2026 sobre f15388d/PR #57, com partida local 1e79e87. Síntese original baseada em ff9d2ec/PR #51; fontes N05/N07 e requisitos M4-12–M4-16 da [matriz](MATRIX.md). O exportador não foi identificado nos componentes consultados; IvaSaft é catálogo CRUD e CSV/XLSX não são SAF-T. Não se afirma inexistência de fornecedor externo não disponibilizado.

## Perímetro e interfaces de destino

Contrato-alvo de exportação fiscal de faturação, condicionado ao perfil aprovado do emitente/instalação e aos documentos realmente produzidos. Não é livro contabilístico FinancialCore nem implementação de GeneralLedgerEntries. O valor de TaxAccountingBasis e a selecção exacta de tabelas dependem desse perfil, não se inferem da presença de um ledger interno.

Primeira fatia futura possível: oráculos e mapeamento de uma FT diferida/EUR em dados sintéticos. Isso não autoriza disponibilizar um exportador comercial que omita outros documentos efectivamente emitidos. FR/RC e restantes famílias exigem projecção aprovada e mandato próprio antes de suporte produtivo. Não apresentar um XML parcial como exportação completa.

Entrada proposta: identidade da instalação/emitente, perfil/tabelas autorizados, período explícito e fonte de dados consistente. Saída-alvo: XML e relatório de validação rastreável (versão/hash XSD, release, selecção, contagens e erros). Não há endpoint, DTO público ou ficheiro XML implementado/gerado nesta missão. Resposta a inconsistências: erros localizados e recusa da entrega fiscal, sem inventar valores nem corrigir dados.

## Estrutura e síntese de mapeamento, detalhada em 13-11-2026

XSD oficial obtido de N07: versão1.04_01, namespace `urn:OECD:StandardAuditFile-Tax:PT_1.04_01`, `vc:minVersion=1.1`. A futura validação deve usar motor compatível com o esquema fixado; não presumir que qualquer validator XML/XSD1.0 demonstra a conformidade. A análise actual apenas leu/parseou a estrutura, não validou XML exportado.

| Grupo/campos | Origem TUULI candidata / regra de contrato | Independência e lacuna |
| --- | --- | --- |
| AuditFile/Header | Empresa e metadados legítimos do produtor/release/perfil; AuditFileVersion, CompanyID/TaxRegistrationNumber, nomes/morada, período, CurrencyCode, DateCreated, ProductID/Version, SoftwareCertificateNumber | Estrutura independente; identidade/regime/certificado real são inputs autorizados, não defaults sintéticos. Revisar formato/codificação e condições campo a campo com N05/N07 |
| MasterFiles/Customer, Product, TaxTable | Clientes/artigos/IVA e chaves estáveis; incluir todas as referências exigidas pelo perfil, incluindo entidades implícitas apenas quando fundamentadas | Não equivale a copiar tabelas indiscriminadamente nem inferir país/NIF/taxa. Regras de país/identificação e taxas/exonerações exigem oráculos fiscais; sem dados reais em testes |
| SourceDocuments/SalesInvoices | Documento comercial e linhas/snapshots consolidados; InvoiceNo, ATCUD, InvoiceDate, InvoiceType externo aprovado, SourceID, SystemEntryDate, CustomerID, linhas/quantidades/preços/descontos/impostos e DocumentTotals | A estrutura é independente; FT/FR no exportador não se obtêm copiando cegamente a tipologia interna FT ou o ID do tipo. Sem mistura de emissão, vencimento, Data Valor e timestamp técnico |
| SalesInvoices/DocumentStatus | Estado documental actual, data/motivo/autor legítimos e SourceBilling conforme proveniência comprovada | Não copiar o E congelado no QR como estado actual. Não reutilizar ou apagar o documento quando anulado. Totais/contagens de anulados seguem a regra da tabela em N05, não a soma indiscriminada dos valores |
| SalesInvoices/Hash, HashControl | Assinatura integral autêntica e identificador/versão de chave conforme contrato produtivo, ligados ao documento/cadeia persistidos | Dependem da fase final. Q (quatro caracteres) não é Hash e R (certificado) não é HashControl. Não inserir0, dummy ou certificado demo para contornar falta de fornecedor; disposições especiais não autorizam atalhos no perfil certificado pretendido |
| Payments/Payment | Documento financeiro, vínculo a origens/liquidações e estados auditáveis; PaymentRefNo, ATCUD, TransactionDate, PaymentType, SourceID, SystemEntryDate, CustomerID, Line/SourceDocumentID/DocumentTotals e métodos segundo condições do XSD | O esquema Payments consultado não contém Hash/HashControl directos. Não generalizar exigências criptográficas da tabela Invoice a Payment; Header/certificação e QR produtivo continuam gates independentes |
| Payments/datas e classificação | TransactionDate é emissão nos termos N05 §4.4.4.5; RC/RG externos exigem enquadramento comprovado. Data Valor continua autoridade financeira aprovada no RC | Não substituir emissão por Data Valor nem declarar efeito fiscal IVA de caixa a partir de F2/T1. Anulação/reembolso/substituição dependem dos contratos e mandatos próprios |
| Totalizadores e referências | NumberOfEntries/TotalDebit/TotalCredit e totais por documento recalculados conforme regras da tabela, chaves sem duplicação/omissões, referências verificáveis | Não importar sinal de TipoDocumento ou saldo do cliente como total fiscal sem mapeamento. Utilizar decimais exactos/escala e arredondamento do contrato aprovado, sem alteração monetária neste trabalho |

MovementOfGoods e WorkingDocuments existem no XSD; isso não autoriza activar guias/outros tipos, omitir documentos emitidos nem construir regras por analogia. NC/ND, RC/NL/RL, adiantamentos e demais reservas mantêm perímetro autónomo. Não exportar auditoria antiga/demo nem criar história fiscal por inferência.

## Testes esperados, ainda não executados

| ID | Oráculo para mandato futuro | Assinatura |
| --- | --- | --- |
| ST01 | Fixar hash/namespace XSD 1.04_01, motor1.1 e codificação de saída; validar exemplos sintéticos e rejeitar enum/tipo/obrigatoriedade inválidos | Não, teste estrutural não é ficheiro fiscal real |
| ST02 | Header/masters e referências completas com clientes estrangeiros, texto/acentos, chaves repetidas/ausentes e taxas/isenções aprovadas; sem dados reais | Não para estrutura; certificado legítimo é gate de entrega |
| ST03 | FT diferida, linhas/descontos/taxas/totais e identificadores/número/ATCUD; comparação exacta com snapshots e oráculos fiscais | Componentes semânticos não; validação integral do documento sim |
| ST04 | Data emissão/SystemEntryDate/Data Valor/vencimento distintos; estado N→A e razões/datas; QR original inalterado e estado actual exportado | Não para estados/agregação, mediante fixture autorizada |
| ST05 | Payments com RC/RG homologados para o caso; múltiplas origens, parciais, referências/contagens/totais e anulados conforme N05 | Não generalizar Hash; sem activar recebimentos reais |
| ST06 | Período/estabelecimento segundo perfil aprovado, snapshot consistente perante escrita concorrente, ordenação estável sem duplicações/omissões, reprodutibilidade e prova de ausência de escritas | Não; estratégia transaccional é escolha técnica do futuro mandato |
| ST07 | Recusa de saída fiscal quando falta assinatura/certificado/referência obrigatória; igualdade do Hash/HashControl às fontes autênticas, cadeia e versão correctas | **Sim**, fase final, sem escolher D-SIGN-02..05 nesta missão |
| ST08 | Reconciliação do conjunto de documentos/estados e totais com o repositório; arquivo de release/schema/erros e autorização do perfil; não validar só por XSD | Parte independente; aceitação fiscal final depende dos gates |

Não executar estes testes nem instalar validador/exportador por esta especificação. Fixtures criptográficas poderão testar estrutura em futura ordem, mas não devem ser confundidas com conformidade produtiva ou assinatura real. O contrato estrutural é material de decisão; aprovação desta matriz não autoriza implementar o exportador.

## Critérios de entrada de uma futura implementação

Mandato próprio, perfil fiscal/tabelas/projecções e oráculos aprovados, política de período/consistência, fontes de cada campo obrigatório, testes sintéticos e validação independente. A correspondência funcional FT diferida 2/1/1,false já está aprovada e não é reaberta; perfil SAF-T e projecções restantes não são homologados pelo executor. Dependências produtivas em falta impedem entrega/uso comercial, não a preparação documental. Sem chamada operacional à AT, publicação de manifesto, chaves reais ou activação.

## M4-12 — Detalhe preparado em 13-11-2026

- [Mapeamento de estruturas/campos e origens efectivas](SAFT_FIELD_MAP.md): D=campo disponível no modelo, C=candidato/derivação, L=lacuna, A=assinatura e P=parâmetro/decisão. Não se verificou conteúdo de bases reais.
- [Opções de perfil e perímetro](SAFT_PROFILE_DECISIONS.md): F recomendado condicionalmente à natureza de facturação da instalação; I não decorre do FinancialCore. Decisões P12-01/02/03 ainda reservadas.
- [Critérios de aceitação de implementação futura](SAFT_ACCEPTANCE.md), sem os executar; complementam ST01–ST08.
- [Fontes reutilizadas e hashes do código consultado](M4_12_SOURCES.json); [pedido de revisão independente](M4_12_REVIEW_HANDOFF.md).

Lacunas de assinatura integral durável/versão da chave, TaxPointDate, regimes, identidade da instalação/release e correspondências de mestres/impostos são explícitas. Q não é Hash e R não é HashControl. Sem completar esses dados ou delimitar legitimamente o caso, não declarar entrega comercial completa. A presente missão não altera modelos/migrations nem decide uma arquitectura de assinatura.

**Estado: PREPARADO PARA REVISÃO; NÃO FECHADO.** Nenhum resultado de teste novo ou XML foi produzido. Perfil/perímetro e decisões aplicáveis carecem de aprovação; implementar o exportador exige ordem própria. Espaço após ATCUD: e melhoria bruto/IVA/total no diálogo permanecem abertos.

## R1 — Rectificação após revisão independente

[Correcções e fundamentos](M4_12_R1_CORRECTIONS.md): UnitPrice é derivado líquido de descontos efectivos de linha e cabeçalho e sem impostos; não é cópia de precoUnitario. Critérios de precisão, divisão periódica e TaxBase constam da R1, para confirmação independente. Descontos já aplicados afectam os líquidos e Line/SettlementAmount; SettlementDiscount/SettlementAmount documental refere acordos futuros, sem alterar GrossTotal actual. Não há fonte dedicada demonstrada desses acordos. PaymentTerms tem origem candidata própria, distinta de desconto/recebimento.

P12-01/02/03 continuam pendentes no alcance aplicável, incluindo certificação expressa e famílias obrigatórias. Correcções propostas não são aprovação independente nem fecho do contrato. Sem implementação ou ensaios.

## Consolidação sobre o PR #60

O [delta M4-12 a M4-16](SAFT_CONSOLIDATION.md) complementa o mapa R1 e regista as fontes e lacunas após V31/V32. [P12](SAFT_PROFILE_DECISIONS.md#consolidação-de-19112026--factos-e-escolhas-remanescentes) mantém o fecho condicionado. As provas existentes sustentam as origens, sem validar um ficheiro SAF-T.
