# OP22-B2.2 — Fatia 1a: Data Valor implementada localmente

15-10-2026. Branch codex/receipt-value-date-slice1a, base e861c223 (documentação)
cujo pai é fc57177, merge PR #26. Sem fetch nesta missão, referência confirmada
localmente. Sem push, PR, merge ou alterações à demo. Revisão Claude da
implementação pendente; entrega preparada para revisão, não autorização de corte.

## Comportamento

- Novo campo dataValor nos DTOs e DocumentoFinanceiro; omissão normaliza para
  emissão nos dois emissores. Construtores Java anteriores conservados.
- E1/E2 aprovadas: máximo emissão dos documentos origem ≤ Data Valor ≤ emissão
  RC. Vencimento não participa. Documento deve estar emitido, não anulado e com
  sinal de débito; sem whitelist FT transitória no legado. Regra antes do número.
- Checkbox só de interface, desmarcada; datas explícitas inválidas mantidas com
  mensagem. API/detalhe e PDF mostram data persistida; histórico NULL mostra
  «Não registada». dataHoraOperacao permanece independente e inalterada.
- V23 aditiva sem backfill: novos RC exigem data, limite superior CHECK;
  guards de data/identidade, verificações diferidas de linhas contra origens,
  locks e protecção da cronologia/natureza da origem contra alterações SQL.
  Imutabilidade de Data Valor aplica-se também a RC legados e NULL históricos.
- Impressão de RC anulado após anulação da origem mantém-se permitida.
- Emissor interno continua sem activação; payload inclui data efectiva.
  Payloads anteriores sem dataValor normalizam para emissão apenas na comparação
  de replay, sem alterar o registo histórico ou fabricar Data Valor persistida.
- FinancialQueries, FinancialCore e saldos/extractos não mudados. F2/T1 não
  activados. Nenhum endpoint novo/anulação nova/substituição implementada.

## Violação preexistente identificada

PendenteService.criarDeDocumento cria Pendente para qualquer documento emitido,
positivo e não imediato, sem distinguir natureza devedora. É possível configurar
um tipo credor que satisfaça esses predicados. Não se reestruturou esse circuito
transversalmente: RC agora rejeita credor como dívida, em Java e SQL. A regra
universal E2 vale para obrigações de débito elegíveis, não habilita tipos fiscais.
O emissor interno conserva a fronteira FT vinculada já existente e inactiva.

## Testes executados — PostgreSQL 16.3 exclusivamente descartável

Container criado para esta missão: tuuli-value-date-slice1a-test, porta local
25439; bases tuuli_value_date_*_test e bases efémeras dos testes de upgrade.
Nenhuma fac/fac_demo/demo Ubuntu utilizada. Versões intermédias V23 foram
ensaiadas em bases novas, nunca com Flyway repair nem alteração de bases reais.

| Grupo | Casos distintos aprovados |
| --- | ---: |
| FinancialReceiptIntegrationTests | 59 |
| ReceiptPayloadCompatibilityTests | 2 |
| FinancialCoreIntegrationTests | 15 |
| FinancialQueriesIntegrationTests | 7 |
| FinancialReceiptMigrationTests (V19/20/21/22 → V23) | 4 |
| FinancialCoreMigrationTests | 1 |
| FiscalMigrationCompatibilityTests | 1 |
| FlywaySchemaIntegrationTests | 1 |
| TestDatabaseSafetyValidatorTests | 4 |
| DocumentoComercialControllerTests | 46 |
| DocumentoComercialEmissaoIntegridadeTests | 25 |
| DocumentoFinanceiroPdfServiceTests (inclui NULL/Data Valor distinta) | 3 |
| Total dirigido, sem contar repetições | 168 |

Execuções separadas, não uma suite integral única. Fixtures comerciais fazem
reset por endpoints e não podem apagar linhas vinculadas deixadas pelos testes
ledger (V20 imutável); por isso grupos comerciais e ledger usam bases separadas.
Falhas iniciais de fixtures/contagens V23 foram corrigidas sem enfraquecer guards.
Após ajuste final para impressão de anulado, repetidos os 59 testes de recibos;
upgrade da V23 final também validado no grupo de quatro migrations.
Asserção HTTP do default Data Valor reforçada no teste de recebimento parcial;
regressão comercial repetida em base nova com a V23 final.

Cobertura: uma/várias dívidas, máximo das emissões, igualdade/default/anterior
válida, antes da origem/depois da emissão rejeitados sem número, vencimento
posterior permitido, credores recusados, débito não-FT sem restrição artificial,
NULL históricos, PDF, imutabilidade SQL, linhas SQL inválidas e rollback com
constraints imediatas/diferidas, origem/natureza, replay/concorrência e guards
V18/V20/V21/V22. Upgrade/segundo arranque sem migrations e sem backfill.

## Frontend e package

- TypeScript e Vite build: aprovados. Node Linux 22.16.0 temporário em /tmp,
  dependências do lockfile apenas na worktree. Aviso de chunk grande já existente,
  sem alterar bundling ou dependências versionadas.
- Playwright: 3/3 aprovados no Edge existente, UI Vite isolada e API simulada.
  Confirma checkbox/default/toggle, preservação da data explícita inválida com
  mensagem, detalhe persistido e NULL histórico. Não é prova de backend fiscal.
- Package final: mvn -o -B -DskipTests package, BUILD SUCCESS.
- Sem execução CI nem declaração de CI verde.

## Evidência e limites

Logs locais /tmp/tuuli-value-date-{last-tests,print-tests,confirm-tests,
commercial-final,commercial-tests,release-tests,http-final,package-final,
linux-build,windows-ui-final}.log. Logs intermédios podem conter falhas de
configuração/fixtures posteriormente corrigidas; prevalecem grupos finais acima.
Não confundir repetição nem migrações em várias bases com testes distintos.

Validação fiscal, revisão Claude, transporte público de idempotência, corte,
reconciliação de dados e activação F2/T1 permanecem fora da entrega. Emissão de
RC cronologicamente inválido é deliberadamente recusada agora. Guardas adicionais
SQL preservam origens referenciadas; rever esta fronteira na futura anulação.
Demo Ubuntu não verificada, não actualizada e sem migrations executadas.
