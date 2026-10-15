# OP22-B2.2 — Fatia 1c: diagnóstico interno FT/RC

## Base e autorização

Base confirmada sem fetch nesta retoma: `f06c09ddd75545cc948060dd974663195f1d1a71`,
merge PR #29, incluindo o contrato documental 033df8a e a Fatia 1b/PR #28.
Branch isolada: codex/financial-reconciliation-slice1c. Checkout principal e demais
worktrees preservados. PO autorizou a opção 1c-B da [proposta](SLICE1C_PROPOSAL.md),
sem autorização de temporalidade FT, dados/demo, activação ou publicação.

## Contrato implementado

FinancialReconciliation é um bean interno @Transactional(readOnly=true), sem
controller, endpoint ou consumidor operacional. Não chama emissores/reversores,
não executa UPDATE/INSERT/DDL, locks de escrita ou funções financeiras mutantes.
Não foi necessária migration; V1–V24 integradas permanecem intocadas.

- inspect(Scope,offset,limit): documentos comerciais emitidos/anulados do cliente
  e moeda, linha por identidade documental; máximo 500, ORDER BY documento.id.
  Usa uma única instrução SQL com CTEs, agregando linhas e afectações separadamente
  para não multiplicar valores. Página, contagens, somas e lacunas de posições
  usam a mesma fotografia. Pedidos separados podem observar estados distintos.
- Contexto empresarial: modelo documental existente pertence à empresa única 1;
  outros IDs empresariais são recusados, nunca usados para atribuir documentos
  sem coluna de empresa a outro tenant. Cliente/moeda parametrizados; pedido
  inválido recusado. Nenhuma nova política de autorização pública foi criada.
- Identifica cliente/nome, documento/referência/emissão, Pendente, posição e
  valores correntes legados/núcleo. Compara apenas FT EUR não imediata com ambos
  os vínculos e metadados coerentes. Outros tipos/moedas aparecem explicitamente
  como não suportados, sem activar elegibilidade documental/fiscal.
- Diferença = saldo Pendente − residual FinancialCore, com BigDecimal exacto,
  sem arredondar seis casas legadas para cêntimos. Não reconstitui saldos pela
  soma ingénua de recebimentos nem interpreta diferença como erro contabilístico.
- Summary.comparableLegacy/Core/Difference somam exclusivamente o subconjunto
  comparável. Se vazio, são NULL, não zero conhecido. documents/comparable/
  nonComparable, historicallyLimited e unmappedCorePositions explicam cobertura.
  fullComparisonCoverage refere cobertura numérica corrente, não história, saldos
  operacionais, reconciliação definitiva ou autorização de corte.
- Valores ausentes no contexto ficam NULL; não se expõem montantes de Pendente
  de outro cliente/moeda. Diferença fica NULL quando não comparável. Posição
  genérica sem origem é lacuna contada globalmente, não saldo silenciosamente zero.
- receipts(Scope,document,offset,limit): evidência paginada por ID da linha,
  referência/emissão/Data Valor do documento financeiro, anulado, bruto/desconto/
  líquido e IDs SETTLE/CONSUME quando vinculados. Preserva NULL e não devolve
  documentos financeiros fora do cliente/moeda. RESTORE/reverse_of são verificáveis
  pela posição e FinancialQueries.allocations já existente; não há chamada por linha.
- consumed/restored são somas de afectações imutáveis, separadas dos componentes
  monetários; ausência de afectações de uma posição conhecida significa zero de
  actividade, não materialização de uma posição ausente.
- Explanation descreve contexto observado: documentos legados que não alimentam
  o núcleo, RC internos que não actualizam Pendente, coexistência com anulados,
  ou causa não demonstrada. Não atribui causalidade financeira por aproximação.
  A contagem de linhas não é contagem de documentos/recebimentos distintos.

## Sinalizadores — factos técnicos previstos pelo contrato

Não são novos estados documentais, naturezas ou regras financeiras.

| Sinalizador | Evidência/predicado e limite |
| --- | --- |
| MISSING_PENDING | Não existe identidade Pendente utilizável no cliente/moeda da origem; saldo ausente não é zero |
| MISSING_ORIGIN | Não existe posição vinculada no âmbito; não cria origem nem infere histórico |
| UNSUPPORTED_DOCUMENT_TYPE | Tipo diferente de FT, liquidação imediata ou linhas financeiras não-RC; não presume elegibilidade |
| UNSUPPORTED_CURRENCY | Moeda diferente de EUR; valor não convertido |
| IDENTITY_MISMATCH | Metadados, original, cancelamento ou referências de linhas incoerentes; comparação recusada e sem fuga de contexto |
| ACTIVE_LEGACY_DOCUMENT | Linhas de documentos financeiros não anulados sem financial_receipt; contexto da diferença, não prova isolada de erro |
| CORE_RECEIPT | Linhas com vínculo FinancialCore, incluindo documentos anulados; não afirma consumo activo |
| HISTORICAL_VALUE_DATE_MISSING | RC com data_valor NULL; limita conhecimento, sem inventar data de pagamento |
| REVERSED_CREATION | CREATE revertido, sem semântica temporal FT aprovada |
| UNDATED_CORE_EFFECT | Afectação cuja operação não tem data de efeito aprovada na view existente |
| DIFFERENCE_OBSERVED | Diferença numérica não nula entre representações comparáveis; causa pode continuar desconhecida |

Todos os sinalizadores podem coexistir. historicalLimit inclui cobertura
incompleta, NULL ou efeitos sem semântica suportada; não substitui balanceAt nem
significa que todo RC NULL deva ser recusado automaticamente. Nenhuma regra de
recusa histórica foi alterada. Igualdade de saldos não demonstra cobertura histórica.
Report.operationalActivationPermitted é sempre false.

## Validação e limites de evidência

Testes exclusivamente em PostgreSQL 16.3 descartável, base criada vazia
`tuuli_reconciliation_slice1c_test`, container de testes já dedicado às missões.
Sem acesso a demo/bases persistentes; fixtures emitem documentos pelos serviços
existentes quando apropriado. Histórico NULL é emitido sob V22 e actualizado
por Flyway, sem desactivar guardas ou fabricar NULL proibido pela V23.

Primeira execução: 19 testes, zero falhas, um erro de fixture (moeda USD não
registada). Corrigido registando a moeda apenas na base descartável. Execução
dirigida posterior: 21 aprovados, zero falhas/erros/ignorados. Não somar repetições
como testes distintos. Log local /tmp/tuuli-slice1c-directed-final.log.

Os 15 testes específicos abrangem: FT não recebida, parcial/total legada,
RC retroactivo do núcleo, DV-09 com várias FT, CREATE revertido e efeito genérico,
origem/Pendente ausentes, posição não mapeada, seis casas e causa desconhecida,
tipos/moeda não suportados, 500/501/fora da página e repetição com empates,
isolamento/inputs, duas consultas contadas para inspect+receipts com várias FT,
transacção PostgreSQL READ ONLY e snapshots de 13 tabelas incluindo série/auditoria,
escrita concorrente com resumo/página coerentes, referência financeira fora do
cliente e coexistência de RC estornado com recebimento legado posterior.

Teste de migrations/histórico também verifica o diagnóstico do RC NULL e a Data
Valor ausente na evidência. Não se simula operação consolidada inválida proibida
pelas constraints: incompletude é representada por origem/identidade em falta,
operações sem vínculo/semântica e posições genéricas reais.

## Fronteira de segurança e pendências

Legado continua a única autoridade operacional. Diferenças esperadas durante a
transição não constituem automaticamente erros. Não há endpoint, UI, novo leitor
operacional, cutover, backfill, alteração de saldos/extractos ou anulação adicional.
FT temporal, política dos históricos, dinheiro real, fiscalidade, consumidores,
chave/permissões públicas e plano de corte/reversão continuam por decidir/autorizar.
Demo Ubuntu por verificar antes de qualquer actualização; não foi tocada.

## Solicitação de revisão independente Claude — antes de qualquer PR

Rever a branch codex/financial-reconciliation-slice1c contra
f06c09ddd75545cc948060dd974663195f1d1a71, incluindo serviço, testes e documentos.
Foco: comparabilidade/NULL, isolamento empresarial singleton, referências de
outro cliente, CTEs/totais sem multiplicação, precisão, cobertura incompleta,
classificações simultâneas, fotografia SQL, paginação, ausência de escritas e
activação. Distinguir evidência PostgreSQL local de CI e revisão estática.
Entregar veredicto e BLOCKER/MAJOR/MINOR, sem alterar ficheiros.

O executável claude não está disponível neste executor; não foi executada revisão
Claude nem substituída silenciosamente por revisão do implementador. Solicitação
preparada para o revisor independente. Publicação exige essa revisão e nova
aprovação do PO. Sem push, PR ou merge nesta missão.

## Resultado final executado — 15-10-2026

| Grupo | Executados | Aprovados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: | ---: |
| FinancialReconciliationIntegrationTests | 15 | 15 | 0 | 0 | 0 |
| FinancialReceiptIntegrationTests | 80 | 80 | 0 | 0 | 0 |
| FinancialQueriesIntegrationTests | 8 | 8 | 0 | 0 | 0 |
| FinancialCoreIntegrationTests | 15 | 15 | 0 | 0 | 0 |
| FinancialCoreMoneyTests | 2 | 2 | 0 | 0 | 0 |
| ReceiptPayloadCompatibilityTests | 4 | 4 | 0 | 0 | 0 |
| FinancialReceiptMigrationTests | 6 | 6 | 0 | 0 | 0 |
| FinancialCoreMigrationTests | 1 | 1 | 0 | 0 | 0 |
| TestDatabaseSafetyValidatorTests | 4 | 4 | 0 | 0 | 0 |
| **Total dirigido final** | **135** | **135** | **0** | **0** | **0** |

Package backend: BUILD SUCCESS. Log local (não arquivo institucional permanente):
`/tmp/tuuli-slice1c-financial-final.log`. Os 21 dirigidos anteriores estão contidos
nesta execução e não se somam. Não se declara suite integral da aplicação,
novo CI ou testes browser/frontend, que não foram afectados. V17/V19–V23 → V24,
validação e segundo arranque sem migrations passaram nos testes existentes.
Nenhuma migration nova/alterada, repair ou execução sobre bases persistentes.

Lacunas globais de posições sem documento podem ser investigadas pela paginação
FinancialQueries.positions existente, que identifica cada posição e a ausência
de commercialOrigin. O diagnóstico não apresenta esse residual como total
comparável nem consulta essa API por linha. Diferenças de precisões são mantidas,
sem aprovar política monetária nova.

Revisão final do diff e git diff --check aprovados; referências documentais
conferidas. Alterações limitadas ao serviço novo, seus testes, cobertura histórica
e documentação institucional. Sem alterações a controllers, emissores, leitores
operacionais, FinancialQueries/balanceAt, migrations, CI ou Docker. Container
exclusivamente descartável parado no fecho; fixtures preservados para revisão.

Pedido ao Claude: auditar o diff completo base f06c09d..HEAD desta branch e emitir
APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES, com BLOCKER/MAJOR/MINOR,
evidências e limitações. Não executar correcções ou operações em bases persistentes.
A implementação fica local, pronta para esse parecer; não está autorizada publicação.
