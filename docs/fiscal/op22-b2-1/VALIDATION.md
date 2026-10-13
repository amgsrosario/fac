# OP22-B2.1 — Evidência

13-10-2026. PostgreSQL 16.3 descartável tuuli-op22-b2-1-disposable,
porta 127.0.0.1:25444, bases próprias _test; nenhum volume/demo existente usado.
Java 21/Maven 3.9.16 em cache, sem instalar dependências. Perfil test/safety activo.

## Estado validado de f716ad6, anterior às correcções Claude

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

## Correcções após primeira revisão Claude

PO comunicou APROVAR / BLOCKER 0 / MAJOR 0 / MINOR 2 sobre f716ad6.
MINOR-001 reproduzido no executor Codex em PostgreSQL16.3 descartável próprio,
tuuli-op22-b2-1-claude-disposable, porta25445, base tuuli_op22_b2_1_baseline_test.
Teste temporário executou 8 experiências: READ COMMITTED/REPEATABLE READ × origem
primeiro/afectação primeiro × constraints consumidas em IMMEDIATE/permanência ou
retorno a DEFERRED. Ambas as transacções fizeram commit e o SELECT final comprovou
mistura de linhas legadas activas com consumo vinculado não estornado, nas 8.
1 teste aprovado nesse ensaio confirma a vulnerabilidade, não valida a integridade.
Teste temporário substituído por 8 casos parametrizados que exigem rejeição.

Coordenação real: primeira transacção conclui checks sem commit; segunda estabelece
snapshot e escreve. O executor consulta pg_stat_activity: se o escritor terminar
checks (V22 antiga) ou esperar num bloqueio (V22 corrigida), liberta o primeiro
commit. Não usa timeout de aplicação como mecanismo esperado de rejeição.
Depois verifica commits, origem, residual, invariante e numeração.

Correcção da própria V22 não publicada: FOR UPDATE na posição em ambos os ramos;
origem também faz UPDATE available=available para criar versão MVCC em RR.
Sem V23, repair, alteração de valores ou migração de dados. Baseline preservada;
versão corrigida aplicada a bases novas _test, sem reutilizar checksum antigo.

MINOR-002: reverse genérico antecipa locks de Pendente ordenados, mantendo replay,
validações e efeitos; anulação FT bloqueia Pendente antes de documento/núcleo.
Provas com transacção real retendo Pendente e NOWAIT no recurso seguinte confirmam
que esse recurso ainda não foi bloqueado pelo escritor à espera. Regressão confirma
restauração 100 no reverse genérico e FT anulada/posição cancelada, sem permitir
anulação/reverse de RC vinculado. Funções de efeito temporal/histórico não mudam.

Resultados finais das correcções: 47/47 RC +32/32 núcleo/queries/safety/migrations
+71/71 comercial/legado = **150 cenários distintos aprovados**, sem falhas/erros/skips.
Distribuição: 47 RC,15 core,7 queries,4 safety,46 controller,25 integridade,
3 upgrades receipt,1 upgrade core,1 compatibilidade fiscal,1 schema.
A suite RC inclui 8 casos parametrizados do write skew e 2 provas de bloqueios,
para além dos 37 cenários anteriores. O ensaio baseline 1/1 com 8 violações não é
somado como validação aprovada. Migrations V19/V20/V21→V22 e segunda inicialização
aprovadas; migrações anteriores preservadas e nenhum CI novo executado.
Package final: BUILD SUCCESS; diff/check e referências documentais aprovados.

Comandos finais (Maven em cache, porta25445, apenas bases novas):

```sh
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25445/tuuli_op22_b2_1_claude_fixed_test mvn -o -B -Dtest=FinancialReceiptIntegrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25445/tuuli_op22_b2_1_claude_fixed_test mvn -o -B -Dtest=FinancialCoreIntegrationTests,FinancialQueriesIntegrationTests,TestDatabaseSafetyValidatorTests,FinancialReceiptMigrationTests,FinancialCoreMigrationTests,FiscalMigrationCompatibilityTests,FlywaySchemaIntegrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25445/tuuli_op22_b2_1_claude_legacy_test mvn -o -B -Dtest=DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests test
mvn -o -B -DskipTests package
```

Logs auxiliares /tmp/tuuli-b2-1-claude-{baseline,fixed,regression,legacy,package}.log.
Evidência produzida pelo executor Codex, não pelo revisor Claude. As bases com
V22 antiga foram preservadas; não se usa repair nem se presume compatibilidade
de checksum. V22 não integrada foi corrigida na origem, sem V23 desnecessária.
Segunda revisão independente Claude: PENDENTE. Nenhum push/PR/merge nesta etapa.
