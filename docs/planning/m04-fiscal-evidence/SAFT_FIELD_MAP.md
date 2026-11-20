# M4-12 — Mapeamento documental SAF-T (PT)

13-11-2026. **Proposta para revisão; contrato NÃO fechado.** Base integrada f15388d/PR #57; partida local 1e79e878ef459958bc7ec8d5c9b3cf9c94e379c6. Nenhum exportador, XML, teste, SQL ou alteração funcional produzido.

## Convenções e proveniência

[Dossier de fontes](SOURCES.md) N05/N07; [proveniência desta leitura](M4_12_SOURCES.json). XSD 1.04_01 arquivado em 04-11-2026, hash 292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350, namespace PT_1.04_01, vc:minVersion 1.1. Leitura estrutural de elementos/choices, não validação de XML. Obrigatoriedades abaixo dependem conjuntamente do XSD, das notas N05 e do perfil aprovado; minOccurs0 não autoriza omitir informação fiscal existente.

**D:** campo existente no modelo/persistência, não garantia de preenchimento/legitimidade em qualquer base. **C:** correspondência candidata ou derivação a validar. **L:** lacuna na superfície consultada. **A:** dependência de assinatura/certificação. **P:** parâmetro/configuração de extracção ou decisão ainda por aprovar. Ter D não significa que o contrato do campo esteja homologado.

Fontes de código (referências à versão de partida; hash dos ficheiros no JSON):

- **E:** [Empresa](../../../src/main/java/com/ar2lda/fac/model/Empresa.java), linhas25–40/45–65/78–118: singleton id1, dados fiscais, morada e contactos.
- **C:** [Cliente](../../../src/main/java/com/ar2lda/fac/model/Cliente.java), linhas18–61/63–83/95–103: id/NIF/dados mestres; não é histórico fiscal por si.
- **P:** [Artigo](../../../src/main/java/com/ar2lda/fac/model/Artigo.java), linhas24–59/65–85; [TipoArtigo](../../../src/main/java/com/ar2lda/fac/model/TipoArtigo.java): ARTIGO/SERVICO.
- **D:** [DocumentoComercial](../../../src/main/java/com/ar2lda/fac/model/DocumentoComercial.java), linhas31–37/42–165/169–245/249–330/334–417/485–531: identidade, snapshots, datas, totais e anulação.
- **L:** [LinhaDocumentoComercial](../../../src/main/java/com/ar2lda/fac/model/LinhaDocumentoComercial.java), linhas30–35/49–127/134–145: descrição, quantidades, preços, descontos, snapshots e imposto.
- **T:** [RIvaTaxa](../../../src/main/java/com/ar2lda/fac/model/RIvaTaxa.java), [TaxaIvaTerritorial](../../../src/main/java/com/ar2lda/fac/model/TaxaIvaTerritorial.java), [TipoTaxaIva](../../../src/main/java/com/ar2lda/fac/model/TipoTaxaIva.java), [IvaSaft](../../../src/main/java/com/ar2lda/fac/model/IvaSaft.java). Existência de IvaSaft CRUD não é mapeamento de imposto por documento.
- **S:** [FiscalDocumentSignatureSource](../../../src/main/java/com/ar2lda/fac/service/FiscalDocumentSignatureSource.java), linhas17–18; [FiscalQrService](../../../src/main/java/com/ar2lda/fac/service/FiscalQrService.java), linhas25–39/67–71; [FiscalQrIdentity](../../../src/main/java/com/ar2lda/fac/service/FiscalQrIdentity.java), linhas16–27.

## 1. Header — N05 tabela1; XSD Header

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| AuditFileVersion | P: esquema fixado1.04_01 | Constante da versão autorizada, não da release Java |
| CompanyID | D/C: E.matriculaRegistoComercial, E.nif | O texto livre não separa conservatória/número. Validar estrutura e aplicabilidade; não usar Empresa.id=1. Excepção normativa de ausência de registo não é fallback genérico |
| TaxRegistrationNumber | D: E.nif | Validar NIF legítimo do emitente; não ProductCompanyTaxID |
| TaxAccountingBasis | P: não existe campo dedicado | Decisão de perfil, ver SAFT_PROFILE_DECISIONS; não inferir do ledger |
| CompanyName / BusinessName | D: E.nome / E.nomeComercial | Nome fiscal vs comercial distintos; snapshot/coerência com emitente do lote |
| CompanyAddress/AddressDetail | D/C: E.morada, morada1 | Composição sem truncagem silenciosa; o endereço livre não contém decomposição obrigatória presumida |
| CompanyAddress/City / PostalCode / Country | D/C: E.localidade, codPostal.id, pais.id | Validar formato/país; Pais.id admite3 caracteres, não prova ISO alpha-2 |
| CompanyAddress/BuildingNumber / StreetName / Region | L/C: sem campos separados | Opcionais condicionados ao que exista; não inventar parsing de números/rua/região |
| FiscalYear / StartDate / EndDate | P: âmbito temporal da extracção | Exercício/período fiscal e regras de fracção; não uma pesquisa livre de datas sem contrato |
| CurrencyCode | P/C: EUR no perímetro proposto | Moeda do ficheiro não é moeda preferida de Cliente; não implementar multimoeda |
| DateCreated | P: relógio da futura extracção | Data de geração do ficheiro, não data de factura ou snapshot |
| TaxEntity | P/L: sem identidade de estabelecimento no modelo E | N05 §1.13: facturação por estabelecimento usa identificação aplicável; sem estabelecimento específico, Global; contabilidade/integrada, Sede. P12-03 confirma o caso real; não mapear FinancialCore.empresa_id nem E.id |
| ProductCompanyTaxID | L/P: titular/produtor do software autorizado | Não copiar E.nif por conveniência; produtor e emitente podem ser entidades distintas |
| SoftwareCertificateNumber | A/L: S.Signature.softwareCertificateNumber, sem configuração produtiva legítima demonstrada | Certificado da release/instalação; valor R do QR não é fonte canónica histórica suficiente. Nenhum1234 demo |
| ProductID / ProductVersion | L/P: identidade de produto autorizada e release; pom.xml tem nome fac/version0.0.1-SNAPSHOT | Nome/versão build não demonstram identidade certificada; exigir metadados de release verificáveis |
| HeaderComment | P/C: relatório/configuração da extracção | Só informação legítima aplicável ao perfil, sem credenciais ou notas inventadas |
| Telephone / Email / Website | D: E.telefone/email/web | Dados disponíveis, formato/condição de exportação a verificar |
| Fax | L: sem campo identificado | Não preencher texto fictício; opcional conforme contrato |

Header é da instalação/emitente aprovado. Documentos guardam emitenteNif/nome/moradas/etc.; um lote com identidade conflitante não pode ser atribuído ao singleton actual por suposição. Determinar coerência e diagnosticar, sem migrar históricos.

## 2. MasterFiles/Customer — N05 tabela2.2

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| CustomerID | D/C: C.id; D.cliente.id | Proposta técnica: representação textual estável do id e mesma referência em Invoice; não derivar chave de NIF mutável |
| AccountID | L/C: não existe conta contabilística no C | N05 §2.2.2 permite Desconhecido quando a conta não esteja definida. Uso condicionado à situação real, não criação de conta fictícia |
| CustomerTaxID / CompanyName | D: C.nif/nome; D.clienteNif/clienteNome consolidados | Política de coerência mestres/snapshots pendente; não substituir NIF histórico cegamente por mestre actual |
| BillingAddress/AddressDetail / City / PostalCode / Country | D/C: C.morada/morada1/localidade/codPostal/pais; snapshots D.cliente* | Não inferir país/NIF. Normalizar só mediante contrato e sem reparar dados; incoerências identificadas |
| BillingAddress/BuildingNumber / StreetName / Region | L: não estruturados no C | Não extrair arbitrariamente de texto livre |
| ShipToAddress | D/C: C.moradas; D.envio* | Mestre actual não prova todas as moradas históricas; usar origem/contexto aprovado |
| Contact | L/C: C.nome não identifica necessariamente pessoa de contacto | Não repetir nome como contacto sem fundamento |
| Telephone / Email | D/C: C.tel/tm/email/email1 | Política de selecção/composição, sem truncagem ou colisão |
| Fax / Website | L | Sem origem dedicada identificada |
| SelfBillingIndicator | L/P: sem indicador fiscal explícito no C | Confirmar configuração real; ausência não prova0 |

Consumidor final exige a condição normativa própria; não converter qualquer NIF ausente num cliente genérico. Um CustomerID não pode ser duplicado para acomodar conflitos. NIF/país divergentes entre documentos do mesmo id devem ser diagnosticados antes da entrega; não decidir silenciosamente primeiro/último registo. Diferenças legítimas de morada/nome requerem política de data de referência, não reclassificação de documentos.

## 3. MasterFiles/Product — N05 tabela2.4

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| ProductType | D/C: P.tipoArtigo ARTIGO/SERVICO | Candidato SERVICO→S e mercadoria ARTIGO→P; classificação exige correspondência demonstrada. Tipo actual não é snapshot da categoria na emissão; não inventar O/I/E |
| ProductCode | D: P.codigo; L.artigoCodigo consolidado | Chave estável e igual à da linha, não substituir pelo id interno da linha |
| ProductGroup | D/C: P.familia.descricao | Opcional; não há snapshot de família na L; não atribuir historicidade ao valor actual |
| ProductDescription | D/C: P.descricao; L.descricao | L.descricao é descrição da linha; não é um snapshot separado da descrição do mestre. Fixar política de mestre à data de extracção e coerência com a linha |
| ProductNumberCode | D/C: P.codigoIdentificacao | Não está demonstrado como EAN validado. Na ausência real de EAN, N05 §2.4.5 admite ProductCode; não tratar qualquer texto como EAN |
| CustomsDetails/CNCode / UNNumber | L: sem campos dedicados identificados | Classificação aduaneira fora da FT/EUR simples; não inventar |

Categoria fiscal do produto, EAN e família não são integralmente congelados por L.artigoCodigo/unidade. Isto é lacuna/limite de origem, não autorização para alterar snapshots nesta missão. Sem backfill ou reconstrução de história.

## 4. MasterFiles/TaxTable — N05 tabela2.5

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| TaxType | C: L.fiscalTratamentoLiquidacao/fiscalProjecaoQr; T | Candidata IVA para tributação normal no perímetro; não deduzir IS/NS pelo valor0 |
| TaxCountryRegion | D/C: D.fiscalJurisdicao/fiscalTerritorioFiscal; TaxaIvaTerritorial.jurisdicao/territorio | Mapeamento explícito de território para código fiscal; não copiar enum interno. Restantes territórios/regimes continuam reservados |
| TaxCode | D/C: L.tipoTaxaIvaCodigo, tipoTaxaIvaDescricao; IvaSaft.id/nome | NORMAL interno não é NOR por identidade textual; tabela governada de correspondência ainda por validar, incluindo casos especiais |
| Description | D/C: L.tipoTaxaIvaDescricao ou T.descricao | Não é a descrição do artigo; origem à data aplicável |
| TaxExpirationDate | D/C: TaxaIvaTerritorial.vigenteAte | Condicional; tabela actual não prova a origem histórica de qualquer linha |
| TaxPercentage | D: L.percentagemIva; RIvaTaxa.valor; TaxaIvaTerritorial.valor | Preferir taxa efectivamente consolidada da linha, sem reprecificar por taxas actuais. Chave composta deve distinguir percentagem/território/tipo |
| TaxAmount, alternativa à percentagem | L/C | Apenas se imposto fixo aplicável e origem explícita; não usar IVA total como imposto fixo |

A chave de imposto precisa de unicidade/referência consistente entre TaxTable e linhas; IvaSaft não fornece essa ligação completa. Não se cria tabela fiscal por esta documentação. Percentagens e tratamentos requerem significado, não apenas XSD válido.

## 5. SourceDocuments/SalesInvoices/Invoice — N05 tabela4.1

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| InvoiceNo | D: D.numeroDocumentoCompleto | Valor consolidado, código interno+série/sequência já confirmado M4-04; não recompor de tipo fiscal ou D.id |
| ATCUD | D: D.atcud, codigoValidacaoAt, numeroDocumento | Valor sem rótulo; a questão do espaço gráfico PDF não altera este campo |
| InvoiceDate | D: D.dataEmissao | Não data de vencimento, geração do ficheiro ou Data Valor |
| InvoiceType | D/C: D.tipoDocumentoCodigoFiscal consolidado | FT no perímetro aprovado; não copiar GFT ou a tipologia interna partilhada FT/FR |
| DocumentStatus/InvoiceStatus | D/C: D.estado/anulado | Candidatos EMITIDO→N e ANULADO→A; excluir RASCUNHO. Outros estados não representados, não inferir F/S/R |
| DocumentStatus/InvoiceStatusDate | D/C: momentoEmissao para emissão; dataHoraAnulacao para anulação | Timestamp da mudança correspondente, não momento da exportação. Histórico incompleto é diagnóstico, não data inventada |
| DocumentStatus/Reason | D: motivoAnulacao | Campo condicional; preservar causa efectiva, não texto padrão |
| DocumentStatus/SourceID | D/C: anuladoPorUtilizadorId ou emissor.codigo conforme estado | Autor da mudança; não confundir com autor da geração do ficheiro |
| DocumentStatus/SourceBilling | L/C: percurso interno conhecido, sem indicador de proveniência dedicado em D | P apenas quando origem interna demonstrada; não marcar toda base como P se houver integrações/importados |
| Hash | A/L: S.Signature.fullSignature existe na interface, não no modelo D | Falta fonte autêntica durável para releitura/exportação; Q são quatro caracteres, não assinatura integral |
| HashControl | A/L: nenhum keyVersion na record S nem em D | Versão da chave usada no documento; R é certificado e não HashControl. Não usar chave activa ou supor versão1 |
| Period | P/C: dataEmissao e início do exercício aprovado | Mês no exercício fiscal, não necessariamente mês civil |
| SpecialRegimes/SelfBillingIndicator | L/P | Enquadramento real explícito; não default0 por ausência |
| SpecialRegimes/CashVATSchemeIndicator | L/P: riva não comprova adesão ao IVA de caixa | Gate de regime; não inferir de RC/Data Valor |
| SpecialRegimes/ThirdPartiesBillingIndicator | L/P | Modelo de operação aprovado; não default0 por omissão |
| SourceID | D/C: D.emissor.codigo | Utilizador que gerou o documento, não autor da anulação |
| EACCode | D/C: D.emitenteCae | CAE da actividade relevante; CAE geral da empresa não garante correspondência de cada operação |
| SystemEntryDate | D/C: D.momentoEmissao | Candidato ao instante de gravação/assinatura, não timestamp de rascunho. Fonte produtiva deve confirmar correspondência com assinatura. Converter zona/precisão sem alterar o instante de origem |
| TransactionID | L/P: FinancialCore tem identidade própria, sem GeneralLedgerEntries estatutário demonstrado | Não exportar UUID do ledger como id contabilístico SAF-T. Obrigatoriedade depende da natureza da aplicação, não só da letra escolhida para Header |
| CustomerID | D/C: D.cliente.id | Referência à entrada Customer única e consistente no mesmo ficheiro |
| ShipTo/DeliveryID / WarehouseID / DeliveryDate / Address | D/C/L: D.envio*/descarga*, dataDescarga/horaDescarga | Endereço e data disponíveis, mas entrega/guia/warehouse de destino sem identidade própria demonstrada. Não atribuir ids fictícios |
| ShipFrom/DeliveryID / WarehouseID / DeliveryDate / Address | D/C/L: D.armazemCarga.id, carga*, dataCarga/horaCarga | Não usar local de carga como prova de guia/entrega inexistente |
| MovementStartTime / MovementEndTime | D/C: dataCarga/horaCarga; dataDescarga/horaDescarga | Condicionais; data sem hora não autoriza meia-noite inventada |

### 5.1 Line

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| LineNumber | D: L.numeroLinha | Ordenação estável; se se omitirem linhas sem relevância fiscal, preservar referência à numeração original |
| OrderReferences/OriginatingON / OrderDate | L | Sem relação de encomenda dedicada nos modelos consultados; não substituir por id da factura |
| ProductCode | D: L.artigoCodigo | Referência coerente a Product; não reler artigo actual para reclassificar |
| ProductDescription / Description | D/C: L.descricao | Mesmo campo disponível, mas significados distintos; avaliar correspondência. Não declarar dois snapshots independentes |
| Quantity | D: L.quantidade | Decimal persistido; não inteiro truncado |
| UnitOfMeasure | D: L.unidade consolidada | Não unidade actual de P após alteração |
| UnitPrice | **C: preço líquido unitário derivado**, não L.precoUnitario bruto | N05 §4.1.4.19.7: deduzir descontos efectivos de linha e cabeçalho, excluir impostos; 0.00 quando TaxBase obrigatório. Precisão, arredondamento, origem líquida e exemplos em [R1](M4_12_R1_CORRECTIONS.md). Não descontar novamente totais já líquidos |
| TaxBase | C/L: L.baseTributavel não demonstra por si condição deste campo específico | Não confundir base monetária da linha com obrigação de preencher TaxBase em qualquer IVA |
| TaxPointDate | L/C: não há data autónoma de prestação/fornecimento por linha | D.dataCarga é candidata só se prova de fornecimento aplicável; **não usar dataEmissao como fallback universal**. Lacuna funcional para prestação em data diferente, sem autorização de a corrigir aqui |
| References/Reference / Reason | L | Sem relação estruturada com facturas rectificadas nestes modelos; NC/ND fora do perímetro |
| ProductSerialNumber/SerialNumber | L | Não confundir número de série de produto com D.serie |
| DebitAmount ou CreditAmount | D/C: L.baseTributavel/valorLinha, após descontos | Regra fiscal usa sentido na conta de vendas, não sinal do saldo do cliente. Candidato FT venda positiva→CreditAmount; validar oráculo; nunca escolher pelo sinal1 de TipoDocumento isoladamente |
| Tax/TaxType / TaxCountryRegion / TaxCode | C: conforme TaxTable e snapshots D/L | Mesma chave e semântica de imposto da secção4; sem mapping por taxa percentual isolada |
| Tax/TaxPercentage | D: L.percentagemIva | Exacta e histórica; não taxa de RIva actual |
| Tax/TaxAmount, alternativa | L/C | Imposto fixo só com origem e enquadramento explícitos |
| TaxExemptionReason / TaxExemptionCode | D/C: L.fiscalFundamento/fiscalMIsencaoCodigo | Só se aplicáveis e fundamentados; existência de campos não homologa isenção/não sujeição |
| SettlementAmount | D/C: L.valorDesconto, com afectação de cabeçalho se efectivamente existente e demonstrada | N05 §4.1.4.19.18: descontos efectivos de linha e parcela global aplicável; não deduzir novamente de CreditAmount/NetTotal. Agregado D.valorDesconto não prova parcela adicional |
| CustomsInformation | L | Documentos/quantidades aduaneiros sem origem dedicada; não inferir |

> **Supersessão TAXPOINT-01.** A deliberação D1 da Administração de 20/11/2026 na [issue #46](https://github.com/amgsrosario/fac/issues/46) aplica-se apenas à TaxPointDate da FT/EUR. Usar a data de prestação do documento e, na sua ausência, data_emissao, conforme [SAFT_F_FT_EUR.md, §5.1](SAFT_F_FT_EUR.md). Para os outros campos e eventos obrigatórios mantém-se a regra «diagnóstico, não fallback».


### 5.2 DocumentTotals, retenção e controlo

| Campo(s) | Origem efectiva / estado | Regra proposta, lacuna ou dependência |
| --- | --- | --- |
| DocumentTotals/TaxPayable | D/C: D.valorIvaTotal; L.valorImposto/ivaLiquidado | Conciliar com tratamento fiscal consolidado; prova M4-09 limitada a IVA normal/EUR |
| NetTotal | D/C: soma das bases consolidadas das linhas fiscais; cabeçalho separa bruto/desconto | Não usar D.valorBruto como líquido. Descontos já aplicados não se subtraem duas vezes |
| GrossTotal | D: D.valorTotal | Conciliar com líquido/imposto/retenções conforme perfil; não saldo pendente ou recebido |
| Currency/CurrencyCode / CurrencyAmount / ExchangeRate | D/C: D.moedaCodigo, taxaCambio e valorTotal | Fora do primeiro perímetro não-EUR; não inferir moeda funcional/conversão de taxaCambio sozinho |
| Settlement/SettlementDiscount / SettlementAmount | **L: acordo de desconto futuro sem origem própria demonstrada** | N05 §§4.1.4.20.5.1/.2: não altera GrossTotal presente. Não copiar D.valorDesconto nem L.valorDesconto, já aplicados e reflectidos nos líquidos |
| Settlement/SettlementDate | L: data acordada para pagamento com desconto futuro não demonstrada | N05 §4.1.4.20.5.3; dataVencimento não prova esse acordo |
| Settlement/PaymentTerms | D/C: pPagamento e dataVencimento, mediante conteúdo legítimo das condições | N05 §4.1.4.20.5.4: termos/prazo de pagamento; pode existir sem desconto futuro. ID isolado não é texto do acordo nem prova de recebimento |
| Payment/PaymentMechanism / PaymentAmount / PaymentDate | L/C: mPagamento é método, não recebimento | Não criar pagamento da FT diferida na emissão. Futuras afectações/RC exigem contrato próprio para montantes/datas reais |
| WithholdingTax/WithholdingTaxType / Description / Amount | D/C/L: D.valorRetencao existe; tipo/fundamento específicos não demonstrados | Retenção continua reservada; valor isolado não autoriza código de imposto ou generalização |
| SalesInvoices/NumberOfEntries | C: contar documentos do conjunto fiscal seleccionado, incluindo anulados conforme norma | Não número de páginas, linhas ou apenas documentos activos |
| TotalDebit / TotalCredit | C: agregação DebitAmount/CreditAmount com regra de estado da tabela4.1 | Excluir A/F dos somatórios de controlo, não da contagem; não somar GrossTotal nem saldos FinancialCore |

## 6. Selecção e consistência — componentes existentes, não exportador pronto

[DocumentoComercialRepository](../../../src/main/java/com/ar2lda/fac/repository/DocumentoComercialRepository.java):67–83 tem findAnaliticos por data, exclui rascunhos e permite incluir anulados. É candidato parcial; não é consulta SAF-T: filtros por cliente/mostrarAnulados não podem cortar arbitrariamente um ficheiro fiscal completo. [LinhaDocumentoComercialRepository](../../../src/main/java/com/ar2lda/fac/repository/LinhaDocumentoComercialRepository.java):27–47 exclui sempre anulados e permite linhas texto: **não reutilizar findAnaliticas cegamente**. A leitura ordenada por documento existe em:18–19; uma chamada por factura criaria N+1, não é desenho recomendado de exportação.

[ClienteRepository](../../../src/main/java/com/ar2lda/fac/repository/ClienteRepository.java) e [ArtigoRepository](../../../src/main/java/com/ar2lda/fac/repository/ArtigoRepository.java) têm findExportPage, usados como candidatos de leitura, não selecção fiscal completa/histórica. Propor extracção read-only consistente, ordenação por chave estável e leitura em lotes, sem escrever marcadores nos documentos. Recusar lote incoerente com diagnóstico por campo/referência em vez de reparar dados ou emitir resultado incompleto como válido.

Âmbito actual: Header/Customer/Product/TaxTable/SalesInvoices FT diferida/EUR. Supplier, GeneralLedgerAccounts/Entries, MovementOfGoods, WorkingDocuments e Payments não receberam mapping exaustivo nesta fatia; a decisão de perfil deve indicar quais têm de existir. Não foi procurado todo o repositório para declarar que outras origens não existem. Sem exportar a demo/históricos por inferência.

## 7. Lacunas materiais para a implementação futura

- **Assinatura:** fullSignature e versão de chave por documento, fonte durável/autêntica e vínculo; a interface não fecha HashControl e QR não reconstrói Hash.
- **Identidade fiscal da instalação/release:** produtor, certificado, ProductID/version e CompanyID não estão todos demonstrados como inputs conformes.
- **Eventos e regime:** TaxPointDate e indicadores de regimes especiais não possuem correspondência completa. Condicionar perímetro/inputs, não escolher datas/zeros por conveniência.
- **Mestres e referências:** coerência de identidade histórica, classificação/EAN do produto e chave/mapeamento de impostos exigem contrato e validação. Ausência de dado opcional só é legítima quando não exista/aplique.

[Decisões de perfil/perímetro](SAFT_PROFILE_DECISIONS.md), [aceitação futura](SAFT_ACCEPTANCE.md) e [pedido Claude](M4_12_REVIEW_HANDOFF.md). **Questão ATCUD: e melhoria bruto/IVA/total no diálogo permanecem abertas.**
