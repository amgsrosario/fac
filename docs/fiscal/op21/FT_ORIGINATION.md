# OP21 — Originação de posições financeiras por novas FT

12-10-2026 · Implementação local sobre91ef3c42f668875d0aa8468f3886e91fb7f358d6,
merge confirmado do PR19/OP20. Publicação pelo Product Owner, ainda não efetuada.

**Originação FT integrada no código local; liquidação RC ainda não integrada.**
Pendente permanece fonte operacional; a conta corrente/extrato e interface não
leem financial_position. Não certifica o produto nem altera fiscalidade.

## Implementação e fronteira transacional

FinancialFtOrigin é adaptador interno, sem endpoint. originateNewEmission só é
chamado durante a transição de rascunho para EMITIDO, depois de Pendente, na
transação existente de DocumentoComercialService.emitir. Exige transação existente
(MANDATORY) e flush JPA antes de associar IDs através de JDBC. FinancialCore
participa nessa transação. Falha em posição ou associação faz rollback da FT,
Pendente, numerador, snapshots e operações financeiras. Não recalcula o total.

A classificação usa codigoFiscal FT configurado, nunca descrição/nome ou sinal
contabilístico editável. Exige EUR e ausência de liquidação imediata. Não gera
posição por leitura/edição/migration ou por anulação de histórico. Documento em
rascunho, FR, NC, ND, RC/NL e moedas fora do âmbito não são integrados. O montante
final positivo emitido usa o motor monetário existente; não há cálculo fiscal
adicional no adaptador.

Empresa vem de Empresa.EMPRESA_ID; cliente e moeda do documento validado; autor
é Utilizador resolvido pelo CurrentUserService no circuito existente. Quando
segurança ativa, a identidade autenticada prevalece sobre fallback do payload.
Não há nova permissão, bypass, Scope público ou alteração ao circuito de login.

Origem DOCUMENTO_COMERCIAL:<id>, chave FT_ORIGIN:<id> e referência documental
emitida são determinísticas. Natureza DEBT é explícita. V19 acrescenta somente
financial_document_origin com FKs e unicidade documento/posição, vínculo
imutável e guard SQL que verifica identidade, âmbito, montante, natureza e
estado de emissão. V18 permanece inalterada. Não há importação histórica.

A emissão conserva bloqueio do documento e numeração existentes. Um segundo
pedido de emissão de emitido continua409, sem nova posição. Retry interno da
originação retorna a posição idempotente; incompatibilidade com vínculo existente
falha em vez de ocultar duplicação.

## Anulação e reversibilidade

DocumentoComercialService.anular executa os mesmos gates comerciais, permissões,
motivo e restrições de recebimentos. Só depois FinancialFtOrigin procura vínculo
persistido e pede reverse da criação, com FT_CANCEL:<id> e motivo/autor reais.
Não reclassifica o tipo atual para decidir se desfaz um vínculo histórico.
Sem vínculo, não cria/backfill de posição. Documento e auditoria seguem o fluxo
anterior; novo erro financeiro faz rollback da anulação e Pendente.

Original, posição e vínculo não são apagados; reverseCREATE deixa posição
CANCELLED/residual0. Repetição da anulação comercial permanece recusada, sem
segunda reversão. Consumos reais do núcleo bloqueiam reverse segundo OP20;
nenhum circuito público faz esses consumos nesta fase. Não há cascade novo,
reabertura, compensação automática ou nova forma de anular FT.

## Coexistência deliberada

FT12,30 cria Pendente12,30 e posição original/residual12,30. RC legado integral
reduz apenas Pendente a0; posição permanece12,30, sem afectação financeira nova.
Essa diferença é conhecida e não deve ser sincronizada silenciosamente nem
apresentada como saldo corrente. Depois de anular RC pelo fluxo existente,
a anulação da FT segue os gates antigos e estorna a originação associada.

Snapshots fiscais, QR, ATCUD e valores emitidos não são modificados pelo vínculo
ou estorno. Nenhum editor, endpoint, calculador RIVA, SAF-T ou leitura operacional
foi substituído. Integração RC/NC/ND e migração histórica exigem OP posterior.

## Rastreabilidade e evidência

| Requisito OP21 | Prova dirigida |
| --- | --- |
| 1/2/3 | emissaoRecalculaTotaisEPersisteIdentidadeAtcudQr: posição única, DEBT12,30, Pendente12,30, autor e cliente |
| 4 | falhaNoAtcud/falhaNoPayloadQr: numerador/documento/posição não subsistem |
| 5 | financialOriginFailureRollsBackInvoicePendingNumberAndCreatedHistory: erro após criar posição, rollback real |
| 6/7 | repeticaoSequencial/duasEmissoesConcorrentes: uma emissão e um vínculo |
| 8/9 | cancellationReversesOriginOnceAndPreservesSnapshot, reversalFailureRollsBackCommercialAndFinancialCancellation |
| 10 | excludedDocumentTypesDoNotOriginateAndDraftRemainsUnlinked, historicalEmissionWithoutOriginCanBeCancelledWithoutBackfill, emissaoNaoEurRecusaQrSemConversao |
| 11/12 | ftOriginKeepsPendingAuthoritativeAfterLegacyReceipt; asserts de ausência de SETTLE em controller |
| 13 | regressões de snapshot/PDF; QR preservado após anulação |
| 14 | PermissionMatrixIntegrationTests e regras de Scope da OP20 |

Integração exclusivamente em PostgreSQL16.3 descartável, com listener/validator
de segurança. As fixtures não transacionais de emissão removem sua história
apenas por TRUNCATE das tabelas do núcleo na base `_test` autorizada pelo perfil
real e flag destrutiva. Não desativam triggers ou proteções na aplicação; não
executar esse cleanup contra base persistente. Tests transacionais rollbackam
normalmente; o flush explícito em fixture simula a escrita antes da leitura SQL.

Schema/compatibilidade passam a validarV19, mantendo assertions históricas.
[Verificações finais e CI](VALIDATION.md). Casos futuros OP19 de NC/compensação,
descontos e certificação não se tornam documentalmente habilitados por esta OP.
