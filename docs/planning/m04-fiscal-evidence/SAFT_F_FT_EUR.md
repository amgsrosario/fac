# SAF-T F / FT-EUR — contrato e proposta de execução

Base PR #61, merge `a68c4d5f911cadc68acba926fa4ffcc89f3d1247`. Fonte da deliberação: ordem da Administração disponibilizada nesta conversa em 19/11/2026, sem identificador externo. A Administração confirmou facturação própria, sem contabilidade estatutária integrada nem facturação por terceiros. Fixou F como perfil de referência, sujeito a confirmação de conformidade legal, e FT/EUR como primeiro incremento do exportador. A deliberação autoriza este contrato documental. As operações efectivamente habilitadas no arranque e a autorização de implementação têm gates próprios.

**Registo integral da decisão.** Arquivo reservado TUULI, `M04-SAFT-Consolidacao/2026-11-19/2026-11-19_P12-01-P12-02_deliberacao-Administracao.md`. SHA-256 conferido com sidecar `459dc80ea41ce04ee204aa6bf57d862ce77be328f3dbc08f02922c63d156af33`. O perfil de referência e o primeiro incremento decorrem da deliberação. O mapeamento, os oráculos e o controlo de extracção são especificações do executor para revisão.

## Fontes campo a campo e critérios

O [mapa R1](SAFT_FIELD_MAP.md) §§1–5.2 é parte normativa do contrato técnico: cada linha identifica origem persistida/derivada ou lacuna e respectiva regra. [N05 e N07](M4_12_SOURCES.json) fixam conteúdo e XSD; a [R1 monetária](M4_12_R1_CORRECTIONS.md) fixa os oráculos de preço e desconto. A tabela abaixo acrescenta a prova de aceitação para as linhas do mapa, agrupadas por oráculo. Campo condicional só pode ser omitido após determinar a sua inaplicabilidade. Uma ausência nunca substitui essa determinação.

| Linhas do mapa R1 | Prova exigida e consequência de falha |
| --- | --- |
| §1 AuditFileVersion, TaxAccountingBasis, CurrencyCode | Validar exactamente 1.04_01, F e EUR contra XSD/configuração, recusar combinação incoerente. |
| §1 CompanyID, TaxRegistrationNumber, CompanyName, BusinessName e CompanyAddress completo | Confrontar Empresa e evidência autorizada, incluindo código de país, morada, registo e variações condicionais. Recusar dados obrigatórios incompletos ou não legitimados; não truncar. |
| §1 FiscalYear, StartDate, EndDate, DateCreated, TaxEntity | Testar limites do exercício e data de geração com relógio fixo; provar centralização/estabelecimento antes de fixar TaxEntity. |
| §1 ProductCompanyTaxID, SoftwareCertificateNumber, ProductID, ProductVersion e metadados restantes | Validar contra manifesto de release e documentos P12-03, incluindo conformidade de comprimento e identificação distinta do emitente. Campos de contacto/comentário seguem origem e aplicabilidade R1. |
| §2 Customer, incluindo AccountID, NIF, endereço e indicadores | Testar unicidade e referências para todas as facturas, conflitos mestre/snapshot, consumidor final e campos condicionais com fonte comprovada. Identidades incompletas ou conflituantes bloqueiam o resultado. |
| §3 Product | Testar classificação produto/serviço, união de linhas pelo código consolidado e regra R1 de ProductNumberCode quando inexiste EAN. |
| §4 TaxTable e Tax de linha | Validar a mesma chave fiscal e taxa histórica no mestre e na linha. Testar mudança posterior de taxa e regime não suportado. Tabela actual, taxa zero e `riva` não provam regime, território ou correspondência TaxCode. |
| §5 identidade, datas de documento/registo/estado, autor, proveniência e referência ao cliente | Confrontar InvoiceNo, FT, ATCUD e restantes campos com snapshots; fixtures emitida e anulada com autor, instante e motivo distintos. Rejeitar ausência de proveniência ou fontes temporais obrigatórias. |
| §5 Period, SpecialRegimes, TransactionID, EACCode e transporte | Conferir condições com configuração e documentos que as comprovem. Sem contabilidade integrada, lacuna de TransactionID estatutário é inaplicável neste perfil salvo condição demonstrada; não usar o id do FinancialCore. Facturação própria resolve o contexto, mas verificar proveniência/indicadores de cada documento. IVA de caixa não foi confirmado. |
| §5.1 identidade e montantes de linha, referências, descontos, moeda e outras condições | Validar ligações e ordem, descrição, quantidade/unidade, UnitPrice líquido sem imposto, TaxBase quando aplicável, SettlementAmount efectivamente aplicado, lado débito/crédito e subestruturas condicionais do mapa. Usar caso R1 com descontos de linha/cabeçalho e resíduo. Recusar referências/alfândega/identificadores exigíveis sem origem. |
| §5.1 TaxPointDate | TAXPOINT-01, deliberação D1 opção A de 20/11/2026 na #46. Resolver cada linha no snapshot de exportação a partir da data de prestação persistida do documento. Quando estiver vazia, incluindo em documentos anteriores à V33, usar explicitamente a data de emissão. Datas anteriores, iguais ou posteriores são válidas. A V33 preserva o valor após emissão e anulação. A data de carga fica fora desta resolução. |
| §5–5.1 Hash/HashControl | Exigir assinatura integral e versão histórica da chave ligadas à emissão. A interface de assinatura não demonstra persistência integral nem fornece versão de chave. Comparar com oráculo e chave sintética em ensaio, exercitar ausência e versão errada. Q/R, chave activa e zero não preenchem a lacuna produtiva sem regra e prova. |
| §5.2 DocumentTotals, Settlement, Payment, WithholdingTax e totais de controlo | Reconciliar por linha, documento e ficheiro. Distinguir Settlement futuro, que não altera GrossTotal, de desconto aplicado. Sem recebimento na emissão da FT diferida. Qualquer condição de pagamento/retenção sem origem legítima deve ser diagnosticada. Anulados contam e conservam os dados originais, fora das somas de controlo conforme a regra SalesInvoices. |

O [inventário P12-03](SAFT_F_FT_EUR_P12_03.md) separa a prova sintética das condições comerciais.

## Matriz de tabelas do perfil F

Fonte: cópia N05 arquivada, SHA-256 `7ef94dfc0d1e8677f5c705abe301148e1cdedfb7a4305634259be095ca37a0d0`. Anexo I, notas gerais alíneas f) e i). A enumeração e distinção relativas à extracção parcelar constam da alínea i), [pp. 5–6](https://info.portaldasfinancas.gov.pt/pt/informacao_fiscal/legislacao/diplomas_legislativos/Documents/Portaria_302_2016.pdf#page=5). A alínea f), p. 4, discrimina as tabelas por natureza e aplicabilidade. A referência à alínea i) não autoriza extracção mensal no incremento.

| Tabelas | Aplicabilidade para facturação própria |
| --- | --- |
| Header, Customer, TaxTable | Fontes e critérios no mapa R1. |
| Product, SalesInvoices | Abrangidos no incremento FT/EUR, com fontes e critérios acima. |
| Supplier | Incluir quando exigível. A origem legítima dos fornecedores ainda não está demonstrada. Verificar a condição no inventário efectivo da instalação. |
| MovementOfGoods, WorkingDocuments, Payments | Incluir quando aplicáveis ao perímetro efectivo. O incremento cobre SalesInvoices FT/EUR. |
| GeneralLedgerAccounts, GeneralLedgerEntries | Excluídos do perfil F desta instalação de facturação própria sem contabilidade estatutária integrada. |

## Controlo de completude e recusa

O futuro exportador recebe um manifesto aprovado da instalação com perfil, exercício, estabelecimento e lista efectiva de operações/tipos/famílias habilitados. A autorização de FT/EUR é âmbito do primeiro incremento e não prova que FT/EUR esgote a operação real. Cruzar o manifesto com a configuração efectiva, incluindo todos os caminhos de emissão. Um tipo habilitado sem cobertura impede declarar entrega fiscal completa, mesmo sem ocorrências.

No mesmo snapshot transaccional read-only, inventariar o universo emitido do período/estabelecimento **antes** de filtrar FT/EUR. Consultar fontes comerciais e financeiras, incluindo anulados, e cada origem adicional habilitada ou histórica que o inventário da instalação indicar. Distinguir movimentos internos de documentos: o SETTLE associado à FR não cria Payments. Classificar por identidade persistida, incluindo desconhecidos e não-EUR, e reconciliar por família, identidade e estado com o conjunto de saída. Exigir correspondência exacta entre documentos elegíveis e linhas de cabeçalho exportadas, referências de mestre resolvidas e totais de controlo recalculados.

Se Supplier for exigível e faltar uma fonte legítima, recusar a entrega fiscal e identificar Supplier, a condição de exigibilidade e a fonte em falta no diagnóstico, mesmo quando todas as FT estiverem cobertas. Testar este caso especificamente.

Um documento emitido sem cobertura (por exemplo, RC/RG ou FR no lote FT/EUR), uma origem não inventariável ou um campo obrigatório por comprovar bloqueia a entrega integral. Emitir diagnóstico com família/tipo, identidade verificável do documento, campo/condição, causa e contagens discriminadas. Não publicar ficheiro fiscal parcial. Ensaiar também um documento fora do período, para verificar o limite da recusa, e evento concorrente durante a extracção, para provar a mesma fronteira de snapshot.

## Minuta de mandato executável

- **Objectivo:** implementar uma extracção interna e read-only de F/FT diferida EUR, com selecção auditável, diagnóstico e validação. Trabalhar em branch isolada a partir da referência integrada confirmada depois da aprovação. O desenho de fontes não autoriza novas escritas fiscais.
- **Pré-condições do ensaio descartável:** contrato aprovado, mandato de implementação, base integrada confirmada e dados sintéticos.
- **Pré-condições da entrega comercial:** operações efectivamente habilitadas confirmadas, comprovativos P12-03, D-SIGN, fonte legítima para TaxPointDate e conformidade legal do perfil F confirmada. A falta destes comprovativos comerciais não bloqueia o ensaio sintético autorizado.
- **Snapshot (O-1):** executar inventário, extracção e reconciliação numa única transacção PostgreSQL `REPEATABLE READ`, só de leitura. No teste concorrente, a transacção A realiza o inventário e sinaliza a transacção B, que confirma um documento elegível antes de A iniciar a extracção. A deve excluir o documento novo de ambas as leituras e manter identidades, contagens e totais reconciliados. Uma nova transacção deve ver o documento confirmado, comprovando que a ausência em A decorre do snapshot partilhado.
- **Ambiente:** PostgreSQL descartável e identidades/assinatura exclusivamente sintéticas. Produzir somente ficheiros de ensaio rotulados. Fixar N07 por SHA-256, namespace e versão; usar validador compatível com XSD 1.1 e oráculos independentes para as regras N05. Registar versão, comandos, commit, logs e hashes, remover recursos próprios.
- **Aceitação:** reutilizar [A12-01–16](SAFT_ACCEPTANCE.md). Validar documentos positivos, XSD inválido e XML estruturalmente válido mas semanticamente incoerente. Confrontar número/ATCUD, clientes/produtos/impostos, estados, linhas e montantes persistidos e apurados independentemente. Contar anulados, excluir os estados previstos das somas e preservar elementos originais. Testar fonte obrigatória em falta, família emitida sem cobertura, tipo habilitado sem cobertura, assinatura divergente, concorrência e consistência dos totais.
- **Ausência de escritas:** transacção de extracção explicitamente read-only. Demonstrar por estado antes/depois das tabelas, ledger, auditoria documental e numeradores e por tentativa de escrita recusada. Nenhum endpoint público ou consumidor novo nesta fase. A prova de autorização e isolamento é parte da aceitação.
- **Fecho:** revisão Claude do commit exacto e do manifesto de resultados. Integração por gate humano. Release comercial, assinatura produtiva, AT, homologação e activação requerem autorizações e dados próprios.

## Cronologia e fuso da instalação

A validação de `SystemEntryDate` e `InvoiceStatusDate` compara datas civis depois de converter cada instante para o fuso da instalação. `DateCreated` representa uma data. Por isso, um registo ocorrido na mesma data local pode ser aceite mesmo que seja posterior à hora de geração. A Administração aceitou esta limitação na deliberação de 20/11/2026 na issue #46.

A comparação à granularidade do instante fica fora do mandato. Só será considerada se houver um registo fiável do instante da geração e um risco concreto que justifique essa comparação. A prova R2 usa a fronteira de meia-noite de Verão em Europe/Lisbon, quando os dois instantes ainda pertencem à mesma data em UTC.

## Evolução da fonte TaxPointDate

A resolução recebe o documento e a linha persistidos no mesmo snapshot REPEATABLE READ. Quando houver mandato para persistir a data por linha, o valor explícito dessa linha terá precedência sobre a data de prestação do documento e a data de emissão. A consulta já lê a linha e a interface de evidência fiscal, os restantes factos de linha e o contrato XML mantêm as suas assinaturas. Nesta versão a linha não contém essa coluna. A recusa TaxPointDate missing é defensiva: a data de emissão obrigatória assegura a origem nos dados admitidos.
