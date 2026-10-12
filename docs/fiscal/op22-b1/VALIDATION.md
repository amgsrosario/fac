# OP22-B1 — Evidência de validação

12-10-2026 · Branch codex/financial-receipts-op22-b1; base70e82c122ec477ceb804e6c837f5bb529120acd7.
PostgreSQL16.3 descartável própria tuuli-op22-b1-disposable,127.0.0.1:25442;
URLs só para bases `_test`, perfil test e safety validator activos. Nenhuma base
persistente/demo utilizada. Registos FT sintéticos são fixtures SQL; a emissão RC,
numeração, ledger, vínculos e commits são reais. Spies apenas injectam falhas;
JDBC, repositórios e SQL não são simulados. Regressão separada cobre FT emitida real.

## Estado validado do commit184eafc (anterior à MAJOR-001)

- Primeira versão:9 integração RC +4 safety aprovados.
- Nesse commit:16 FinancialReceiptIntegrationTests aprovados, com commits reais.
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

## Correcção MAJOR-001 — componentes financeiros após vínculo RC

A nova revisão Codex encontrou um MAJOR distinto das observações anteriores.
Reprodução efectiva em PostgreSQL16.3 descartável, schema V20: emissor interno
RC10 numa transacção externa → SET CONSTRAINTS ALL IMMEDIATE → ALL DEFERRED →
CASH_OUT1 e DISCOUNT1 → COMMIT. O teste temporário confirmou os dois componentes
persistidos e desconto documental0; teste1/1 aprovado demonstra a vulnerabilidade,
não uma validação da integridade. A fixture de reprodução não foi usada como base
validada após a correcção. Teste temporário substituído por regressão que exige rejeição.

Alternativas técnicas:

| Protecção | Benefício | Custo/risco |
| --- | --- | --- |
| Bloquear inserções após vínculo | Imediata, independente das constraints, ledger RC selado | Exige montar componentes/afectações antes do vínculo; emissor já o faz |
| Revalidar RC em cada inserção | Poderia permitir extensões compatíveis | Alarga função/dispatch e triggers diferidos, aceita mutações desnecessárias |

Escolhida a primeira: extensão de uma liquidação RC finalizada não é necessária
nesta fronteira. Migration aditiva V21 com função e dois triggers BEFORE INSERT
não diferíveis; apenas operações presentes em financial_receipt são seladas.
V18 e V20 mantêm checksums. V20 continua a validar o conteúdo antes/do vínculo;
V18 impede extensão de operações consolidadas fora da transacção criadora.
Nenhuma validação depende exclusivamente de Java. Nenhuma alteração de dados,
backfill ou reparação automática de ledger já corrompido. Sem activação/B2.

Testes permanentes acrescentados: ataque equilibrado exacto, CASH_OUT, DISCOUNT,
afectação de outra posição sem vínculo de linha, emissão válida após ambos os
modos, operação genérica sem RC com par equilibrado e violação antes do vínculo.
Modos inicial diferido, IMMEDIATE e IMMEDIATE→DEFERRED cobertos separadamente.
Falhas tardias confirmam rollback de documento, linhas, operação, afectações,
componentes, vínculos, auditoria, numerador e residual. A afectação usa outra
posição para não confundir a rejeição com a constraint de unicidade.

Revalidação da correcção em PostgreSQL16.3 próprio, porta25443, schema V21:
23 RC +15 core +7 queries +4 safety +46 controller +25 integridade +2 receipt
migrations +1 core migration +1 fiscal compatibility +1 schema = **125 cenários
distintos aprovados**, zero falhas/erros/skips. As execuções31/31,24/24 e71/71
incluem repetição do upgrade V19: não somadas como126 testes distintos.
Upgrade V19→V21 e V20→V21, assim como V17/V15→V21, aprovados; segundo arranque0.
Fixtures preservam RC legado de seis casas, ATCUD/QR e ledger anterior; triggers
V21 confirmados não diferíveis. Inicialização de schema vazio aprovada.
Nova revisão independente Codex major001_review: **APROVAR**, BLOCKER0/MAJOR0/MINOR0.
Revisor analisou SQL, ordem do emissor, concorrência, testes e documentação por
leitura; resultados executados pertencem ao executor.

Comandos adicionais (Maven em cache, sem instalar dependências):

```sh
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25443/tuuli_op22_b1_major001_fixed_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests,TestDatabaseSafetyValidatorTests,FinancialReceiptMigrationTests,FinancialCoreMigrationTests,FiscalMigrationCompatibilityTests,FlywaySchemaIntegrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25443/tuuli_op22_b1_major001_fixed_test mvn -o -B -Dtest=FinancialCoreIntegrationTests,FinancialQueriesIntegrationTests,FinancialReceiptMigrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25443/tuuli_op22_b1_major001_legacy_test mvn -o -B -Dtest=DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests test
mvn -o -B -DskipTests package
```

Package final BUILD SUCCESS; diff e37 destinos locais Markdown validados.
Logs da reprodução e das três revalidações conservados em /tmp/tuuli-major001-*.log
como evidência auxiliar local, não fonte institucional nem requisito de acesso.
Commit adicional preserva184eafc; nenhum push/PR/merge autorizado nesta correcção.
Revisão cruzada Claude permanece pendente; revisão Codex não a substitui.
