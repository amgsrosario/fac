# OP22-B2.1 — Evidência

13-10-2026. PostgreSQL 16.3 descartável tuuli-op22-b2-1-disposable,
porta 127.0.0.1:25444, bases próprias _test; nenhum volume/demo existente usado.
Java 21/Maven 3.9.16 em cache, sem instalar dependências. Perfil test/safety activo.

## Execuções efectivas e estado final

A versão final foi validada em bases novas _validated_test, sem repair/checksum
alterado em schema existente. Execuções finais: 68/68 (núcleo/RC/schema/migrations),
71/71 (regressão documental/legado), 1/1 (identidade recriada/snapshot obsoleto).
**140 cenários distintos aprovados**, zero falhas, erros ou skips:
37 RC +15 core +7 queries +4 safety +46 controller +25 integridade +3 receipt
upgrades +1 core upgrade +1 fiscal compatibility +1 schema.
As 37 provas RC combinam 36 da suite final com 1 teste acrescentado posteriormente;
não se apresenta uma execução única inexistente. Execuções anteriores 62/33/1/66/71
foram revalidações de desenvolvimento e não somadas à contagem distinta.

Upgrades V19/V20/V21→V22 e segunda inicialização: 0 migrations aprovados;
V17/V15→V22 preservam fixtures anteriores. Inicialização vazia/schema V22 aprovada.
V18–V21 não modificadas. Não houve repair ou execução em bases persistentes.
Package `mvn -o -B -DskipTests package`: BUILD SUCCESS. `git diff --check`
aprovado; referências documentais locais validadas. Nenhum CI desta OP executado.

Comandos finais com Maven 3.9.16 em cache (binário /mnt/c/Tools/apache-maven-3.9.16/bin/mvn):

```sh
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25444/tuuli_op22_b2_1_validated_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests,FinancialCoreIntegrationTests,FinancialQueriesIntegrationTests,TestDatabaseSafetyValidatorTests,FinancialReceiptMigrationTests,FinancialCoreMigrationTests,FiscalMigrationCompatibilityTests,FlywaySchemaIntegrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25444/tuuli_op22_b2_1_legacy_validated_test mvn -o -B -Dtest=DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25444/tuuli_op22_b2_1_validated_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests#staleRecreatedIdentityMustObserveMappedPosition test
mvn -o -B -DskipTests package
```

Logs auxiliares locais: /tmp/tuuli-b2-1-validated.log,
/tmp/tuuli-b2-1-legacy-validated.log, /tmp/tuuli-b2-1-recreated.log e
/tmp/tuuli-b2-1-package.log. Não substituem esta fonte institucional.

## Rastreabilidade adicional à OP22-B1

| Cenário | Teste |
| --- | --- |
| Núcleo→legado, pré-numeração, replay | coreThenLegacyIsRejectedBeforeNumberAndReplaySurvives |
| Legado→núcleo, origem não é consumo | legacyThenCoreIsRejectedBeforeNumberWithoutInferringOwnershipFromOrigin |
| Corrida real | crossCircuitConcurrentReceiptsCommitExactlyOne |
| Multi-FT em ordens opostas | oppositeDebtOrdersAcrossCircuitsCannotCommitBoth |
| SQL legado, modos de constraints | rawLegacyInsertionCannotBypassEitherConstraintMode |
| Núcleo genérico, rollback | rawCoreConsumptionAfterLegacyRollsBackAllLedgerWrites |
| Alteração da dívida de linha legada | rawLegacyLineReassignmentIsRejected |
| SQL núcleo, modos/rollback integral | directSqlCoreConsumptionCannotBypassDeferredOrImmediateChecks |
| Snapshot anterior a commit concorrente | staleRepeatableReadCannotIssueCoreReceiptAfterLegacyCommit |
| Origem associada depois de consumos | lateOriginCannotHideExistingMixedConsumption |
| Mudança directa de estado legado | directLegacyReactivationCannotBypassCircuitExclusion |
| Origem sem identidade | documentaryOriginWithoutSharedIdentityIsRejected |
| Consumo vinculado sem identidade | mappedConsumptionWithoutSharedIdentityIsRejected |
| Identidade recriada com snapshot anterior | staleRecreatedIdentityMustObserveMappedPosition |

RC legítimos, precisão, parcial/múltiplo, replay/concorrência, V21 e falhas
injectadas anteriores continuam cobertos pelos 23 testes B1. Testes fazem commits
PostgreSQL reais; spies apenas injectam falhas específicas, não simulam ledger.
CI B1 cobriu safety/package; não é apresentado como execução destes testes SQL.

Revisão independente Claude B2.1: PENDENTE. Nenhum push/PR/merge nesta etapa.
