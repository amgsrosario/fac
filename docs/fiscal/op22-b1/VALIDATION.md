# OP22-B1 — Evidência de validação

12-10-2026 · Branch codex/financial-receipts-op22-b1; base70e82c122ec477ceb804e6c837f5bb529120acd7.
PostgreSQL16.3 descartável própria tuuli-op22-b1-disposable,127.0.0.1:25442;
URLs só para bases `_test`, perfil test e safety validator activos. Nenhuma base
persistente/demo utilizada. Registos FT sintéticos são fixtures SQL; a emissão RC,
numeração, ledger, vínculos e commits são reais. Spies apenas injectam falhas;
JDBC, repositórios e SQL não são simulados. Regressão separada cobre FT emitida real.

## Estado efectivo

- Primeira versão:9 integração RC +4 safety aprovados.
- Código actual:16 FinancialReceiptIntegrationTests aprovados, com commits reais.
- TestDatabaseSafetyValidatorTests4; FinancialCoreIntegrationTests15;
  FinancialQueriesIntegrationTests7; DocumentoComercialControllerTests46;
  DocumentoComercialEmissaoIntegridadeTests25; FinancialCoreMigrationTests1;
  FinancialReceiptMigrationTests1; FiscalMigrationCompatibilityTests1;
  FlywaySchemaIntegrationTests1: **117 testes distintos aprovados**, zero skips.
- Regressão inicial75 encontrou25 erros apenas na limpeza da fixture, devido
  às FKs V20 novas. Cleanup passou a incluir as duas tabelas explicitamente,
  mantendo TestDatabaseSafetyValidator, perfil/flag/base_test, sem CASCADE.
  Todos os25 afectados passaram após correcção, com núcleo/queries/schema48/48.
- Revisão encontrou MAJOR1 (linha tardia sem vínculo após constraintsIMMEDIATE)
  e MINOR1 (observacoes alteráveis). Corrigidos/testados; revalidação23/23 de
  receipts/migrations/schema/safety aprovada e última suite RC16/16 aprovada.
- Package final `mvn -o -B -DskipTests package`: BUILD SUCCESS.
- Revisão independente op22_b1_review, exclusivamente leitura: APROVAR,
  BLOCKER0 / MAJOR0 / MINOR0 após correcções. Revisão documental final APROVAR, sem findings adicionais.
- Diff completo/checks e43 destinos Markdown locais aprovados; entrega validada para commit local.
  Sem push, PR ou CI executado nesta missão; CI posterior não dispensado.

A revisão efectivamente realizada é de agente Codex independente, não de Claude.
A governação central identifica Claude na revisão cruzada TUULI; essa composição
entre fornecedores não foi executada nesta sessão e não é declarada cumprida.
Os resultados de teste pertencem ao executor, não foram executados pelo revisor.

Comandos usados com Maven3.9.16 em cache e Java21, URLs próprias porta25442:

```sh
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25442/tuuli_op22_b1_final_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests,TestDatabaseSafetyValidatorTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25442/tuuli_op22_b1_regression_test mvn -o -B -Dtest=DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests,FinancialCoreMigrationTests,FinancialReceiptMigrationTests,FlywaySchemaIntegrationTests,FiscalMigrationCompatibilityTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25442/tuuli_op22_b1_regression_test mvn -o -B -Dtest=DocumentoComercialEmissaoIntegridadeTests,FlywaySchemaIntegrationTests,FinancialCoreIntegrationTests,FinancialQueriesIntegrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25442/tuuli_op22_b1_reviewed_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests,FinancialReceiptMigrationTests,FinancialCoreMigrationTests,FiscalMigrationCompatibilityTests,FlywaySchemaIntegrationTests,TestDatabaseSafetyValidatorTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25442/tuuli_op22_b1_reviewed_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests test
mvn -o -B -DskipTests package
```

Resultados prévios válidos foram reutilizados quando não afectados pelas correcções;
117 é contagem distinta, não soma de reexecuções. Nenhuma dependency instalada.

V20 testada a partir de schema vazio. Depois de acrescentar o gate de reversão,
foi criada nova base descartável, sem repair/alteração de checksums Flyway.
Upgrade V19→V20 e segunda inicialização aprovados: preserva RC com bruto1.000001,
desconto0.000001, ATCUD/QR, ledger original/residual100.00; vínculos novos vazios,
RC legado permanece anulável. Upgrade17→20 e15→20 preservam provas anteriores.
Não se fez repair nem se tocaram bases persistentes. Container próprio parado
após verificações; todos os outros containers e worktrees preservados.

## Rastreabilidade dos16 critérios

| Critério | Teste / limite |
| --- | --- |
| 1 | partialMultipleSuccessiveReceiptsAndDocumentaryEffect: emissão RC real, ATCUD, SETTLE, vínculo/data documental |
| 2 | mesmo teste: duas FT, parcial e liquidação sucessiva até residual0 |
| 3 | requestReplayDifferentPayloadContextAndUniqueBindings; concurrentSameRequestCreatesOneReceipt |
| 4 | payload diferente/cliente diferente/autor diferente recusados |
| 5 | concurrentDifferentSeriesCannotOverconsume: duas FT, ordens de pedido opostas, séries distintas |
| 6 | excessCentsDiscountDuplicatesAndIsolationAreRejectedWithoutNumber |
| 7 | failureAfterNumberRestoresNumeratorAndLeavesNoReceipt |
| 8/9 | failureAfterActualSettlementRollsBackDocumentLedgerAndRetry; failureDuringQrRollsBackLinesAndBalances; failureAfterBindingsAndAuditRollsBackEverything |
| 10 | trace linha/afectação, vínculo exclusivo e SQL rejeita duplicação/tampering |
| 11 | cent exacto10/10.000000, fracção0.001 recusada, sem arredondamento |
| 12 | desconto percentual ou absoluto não zero recusado |
| 13 | protectedSeriesChronologyRollsBackReservation, efeito derivado da emissão RC |
| 14 | unreconciledLegacyReceiptIsBlockedAndLegacyCancellationStillWorks + regressão dos endpoints existentes |
| 15 | soma da projecção PendenteDto isolada = totals(scope).openDebt; não se afirma que endpoints foram transferidos |
| 16 | clientes existentes diferentes, autor efectivo/replay e consultas fora do contexto |

Testes de regressão da revisão: immediateConstraintsCannotPermitLateUnlinkedLineInSameTransaction,
requestObservationsAreImmutableButPrintMarkerRemainsMutable e
financialDebtLinkDoesNotAuthoriseOtherDocumentTypes aprovados.

MissingOrigin prova ausência de importação/backfill. Cutover diagnóstico sempre
recusa activação. Guard de estorno vinculado testado sem atribuir semântica temporal.
Testes históricos por data e nova anulação não executados: partes deliberadamente
suspensas. Frontend/CI/SAF-T não alterados, nem tratados como provas de conformidade.
