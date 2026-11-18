# Plano aprovado → provas executadas

Referência: `36c6aad` TEST_PLAN.md, I01–I17. Nomes abaixo são métodos de ProjectedFrIntegrationTests salvo indicação. Resultados por execução/HEAD em VALIDATION.md; uma linha pode reutilizar mais de um método e não representa teste adicional.

| Critério | Prova e limite |
| --- | --- |
| I01 | FrProjectionContractTests: beans de criação/emissão ausentes por omissão; manifesto produtivo igual à base. Não representa ensaio de instalação comercial |
| I02/I03 | catalogueTupleAndAliasSeriesAreProtected, linkedFrDoesNotPermitLegacyPendingOrReclassification, GovernedFtPersistenceTests e FiscalCatalogTests; revisão e materialidade protegidas Java/SQL |
| I04(a) | ordinaryGovernedEmissionAndSqlIncompleteProjectionAreRefused: percurso ordinário e commit sem acto integral recusados |
| I04(b) | i04bNewUnboundImmediateFtRetainsLegacyWithoutQuittance: FT nova sem vínculo mantém D=FT e ausência de acto FR/CASH_IN. C6 ABERTO |
| I05/I08/I11 | oneDocumentFullCashZeroResidualAndHistoricalDates: POST só de teste por MockMvc, identidade antes da assinatura sintética, documento único, posição persistida e consulta histórica |
| I06/I12 | sequenceAndMultipagePdfReprintsKeepOneFinancialAct: duas emissões na série própria, PDF efectivamente gerado/QR descodificado, ATCUD multipágina e reimpressões sem novo acto |
| I07 | historicalInternalCodeCannotBeReclassifiedInJavaOrSql, financialDocumentHistoryPreventsCrossTableCodeReuse, removedConfigurationDoesNotReleaseItsFiscalCode e staleRepeatableReadSeriesWriterCannotValidateAnOldSnapshot: histórico entre tabelas, tombstone e serialização |
| I09 | missingSignatureRollsBackNumberAndAllMoney, missingAtCodeAndCashMismatchRollBack, faultsDuringSettlementAndBindingRollBackWholeAct e commit diferido: causa esperada e estado financeiro/numerador antes/depois |
| I10 | replayAndDifferentPayloadDoNotDuplicate, concurrentSameKeyReturnsSingleAct, concurrentDifferentKeysCannotIssueTwice |
| I13 | deferredFtCannotEnterImmediatePath e regressões FT/RC discriminadas por classe. A fixture negativa RC incoerente foi isolada por rollback, sem afrouxar regra da aplicação |
| I14/I15 | sqlSealingImmutabilityAndNoUndocumentedReversal, ordinaryGovernedEmissionAndSqlIncompleteProjectionAreRefused; não há anulação FR nova |
| I16 | ProjectedFrMigrationTests: V30→V31 e segundo arranque. GovernedFtPersistenceTests: V27→V28 preservado e extensão até V31, sem backfill |
| I17 | INDEPENDENT_FISCAL_WORK.md: mapeamento documental. Não executado exportador/mapper SAF-T, não existe XML. P12 e assinatura produtiva pendentes |

Não generalizar a prova para não-EUR, isenções, não sujeição, retenção, novos regimes, RC/RG, anulação FR, assinatura real ou emissão positiva pelo JAR. Identificação e pagamento sintéticos não são homologação QR/PDF/SAF-T ou certificação.
