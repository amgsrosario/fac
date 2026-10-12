# OP22-A — Evidência e limites de validação

12-10-2026 · Branch codex/financial-query-contracts-op22-a.
Base12a3aed7f35085ab0f852c48530ee21a467c490c (PR #21 merged, confirmado por Git/GitHub).
Worktree isolada; checkout principal desactualizado preservado. Sem push/PR/merge.

## Evidência técnica

PostgreSQL16.3 descartável própria: tuuli-op22-a-disposable, porta127.0.0.1:25441;
bases tuuli_op22_a_test e tuuli_op22_a_docs_test. Flyway aplicado até V19.
Perfil test e safety validator activos; nenhuma base demo/persistente utilizada.

- Primeira execução: FinancialQueriesIntegrationTests7 + FinancialCoreIntegrationTests15
  + TestDatabaseSafetyValidatorTests4 =26 aprovados, zero falhas/erros/skips.
- Regressão: DocumentoComercialControllerTests46, DocumentoComercialEmissaoIntegridadeTests25,
  FinancialCoreMigrationTests1 e FlywaySchemaIntegrationTests1 =73 aprovados,
  zero falhas/erros/skips; originação, datas, RC legado e migrations V18/V19 reais.
- Após accessor temporal: FinancialQueriesIntegrationTests7 novamente aprovados.
  Total99 testes distintos aprovados (106 execuções com a repetição dirigida).
- `mvn -o -B -DskipTests package`: BUILD SUCCESS; package não substitui testes.
- `git diff --check` e validação de destinos Markdown locais: aprovados.
- Revisão independente op22_a_review, exclusivamente leitura: APROVAR;
  BLOCKER0 / MAJOR0 / MINOR0. Observação documental: substituir resultados
  pendentes por evidência efectiva antes do commit; aplicada. O revisor não
  executou testes nem certificou enquadramento fiscal.

Comandos dirigidos (URL sempre na PostgreSQL própria porta25441):

```sh
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25441/tuuli_op22_a_test mvn -o -B -Dtest=FinancialQueriesIntegrationTests,FinancialCoreIntegrationTests,TestDatabaseSafetyValidatorTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25441/tuuli_op22_a_docs_test mvn -o -B -Dtest=DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests,FinancialCoreMigrationTests,FlywaySchemaIntegrationTests test
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25441/tuuli_op22_a_test mvn -o -B -Dtest=FinancialQueriesIntegrationTests test
mvn -o -B -DskipTests package
git diff --check
```

O container descartável próprio é parado após a validação; não se tocaram
containers/bases persistentes ou de outras missões. Nenhuma dependência instalada.

## Rastreabilidade OP22-A

| Requisito | Prova real / limite |
| --- | --- |
| 1: naturezas separadas | distinctNaturesZeroNetStillOpenAndEmptyScopes |
| 2: cliente/contexto | scopeIsolationForEveryIdentityAndList, saldos vazios fora do contexto |
| 3: documento | financialQueryUsesDocumentIssueDateAndPreservesLegacyBalances, vínculo FT/V19 e residual |
| 4/5: parcial/múltiplas | partialMultipleAllocationsExplicitCompensationAndAuditReversal |
| 6: compensação explícita | mesmo teste, residual800/600 após estorno independente, sem caixa falsa |
| 7: zero líquido não encerra | distinctNaturesZeroNetStillOpenAndEmptyScopes |
| 8: estornos históricos | histórico auditável com reverse_of e componentes; sem data de efeito financeira presumida |
| 9: consulta por data | SUSPENSA por decisão da continuação; invalidInputsAndFinancialDateNeverFallbackToRecordingTimestamp prova recusa explícita, não prova saldo histórico implementado |
| 10: paginação/ordem | deterministic501PositionsWithTimestampTiesAndGlobalCounts:500+1, mesma sequência duas vezes, sem omissões/duplicações, total501 na página vazia |
| 11: contexto | scopeIsolationForEveryIdentityAndList para empresa/cliente/moeda, IDs/operações/afectações/listas |
| 12: legado | ftOriginKeepsPendingAuthoritativeAfterLegacyReceipt e regressão de emissão/anulação, sem consumidores operacionais novos |

exhaustedAndCancelledRemainIdentifiableAndCreditNetIsSigned e
moneyComponentsAreSeparateImmutableIdentities verificam estados individuais, sinais,
caixa versus desconto, IDs de reversão. Timestamps são técnicos; o teste FT usa
data documental explícita e compara recordedAt com o campo SQL persistido,
sem dependência da data de execução. Não se fizeram testes de UI/API nova,
certificação, conversão de precisão, carga de produção nem histórico financeiro
por data: fora do âmbito implementado. Não confundir evidência anterior OP21
com testes executados nesta OP.

## Modelo e riscos residuais

Zero migrations, colunas/tabelas/índices novos. Efeito de criação FT consultável
pela emissão; SETTLE sem vínculo financeiro e REVERSE sem semântica temporal.
Histórico financeiro por data adiado, sem bloquear contratos correntes.
Pendente/RC legados permanecem operacionais; saldo do núcleo pode divergir após
RC legado, deliberadamente sem sincronização. Qualquer futura activação exige
fronteira completa e validação de precisão/estornos/vínculos/autorização.
