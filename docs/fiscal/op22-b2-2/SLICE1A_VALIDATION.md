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
- V23 aditiva sem backfill: novos RC exigem data, limite superior inicialmente por CHECK (substituído por trigger exclusivamente RC no complemento abaixo);
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

## Validação inicial de 1540714 — PostgreSQL 16.3 exclusivamente descartável

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

## Correcções após revisão Claude — mesma branch, base 1540714

Parecer comunicado pelo PO: REQUEST CHANGES, BLOCKER 0 / MAJOR 1 / MINOR 2.
Não confundir com aprovação desta correcção, cuja revisão final está pendente.

- MAJOR-001: ReceiptValueDate.validate e persistência só quando codigo_fiscal=RC.
  Documentos não-RC conservam regras legadas, inclusive liquidação de pendentes
  credores; ignoram Data Valor enviada no pedido. UI usa codigoFiscal para não
  impor regras, checkbox ou detalhe Data Valor a não-RC; PDF não-RC conserva
  apresentação anterior, sem rótulo Data Valor. V23 retira CHECK global e restringe
  limite superior, inferior diferido, locks adicionais e guards de origem/natureza
  aos RC. RC emitidos continuam imutáveis, NULL históricos preservados.
  Teste novo cria não-RC, liquida credor, verifica INSERT/UPDATE/linhas/cronologia
  SQL sem rejeições novas; testes RC anteriores continuam eficazes.
- MINOR-001: samePayload remove apenas o campo Data Valor para compatibilidade
  de payloads antigos e compara estritamente os restantes caracteres JSON.
  Montantes nunca são reserializados por representação de precisão inferior.
  Payload novo continua com igualdade textual estrita; parser numérico exacto
  não altera os restantes campos. Testes incluem números de 19 dígitos e
  representações 30.0/30.00 distintas, além do replay por omissão/default.
- MINOR-002: browser testa mensagem abaixo da emissão de origem e captura o
  request POST real com checkbox desmarcado (emissão) e marcado (data explícita).
  Não é apenas teste visual: postDataJSON verifica datas e afectação enviada.
- ADR-002 §4.1 agora enuncia E1 sem reserva: dívida FT nasce financeiramente
  na emissão; E2 cria obrigação de débito elegível na emissão, vencimento separado.

### Validação complementar executada

PostgreSQL 16.3: tuuli_value_date_claude_fix_test, criada vazia no container
exclusivamente descartável desta missão. V23 não consta da referência integrada
local origin/feature/visual-redesign consultada; apenas V23 não publicada foi
corrigida. Nenhuma migration integrada alterada, nenhum repair ou base persistente.

- FinancialReceiptIntegrationTests: 60 aprovados (RC/não-RC, SQL, concorrência,
  idempotência, preservação financeira e erro temporal antes de numeração).
- FinancialReceiptMigrationTests: 4 aprovados, upgrade/validação/segundo arranque.
- ReceiptPayloadCompatibilityTests: 4 aprovados, incluindo precisão/representação.
- TestDatabaseSafetyValidatorTests: 4 aprovados.
- DocumentoFinanceiroPdfServiceTests: 3 aprovados, RC histórico/Data Valor e
  ausência do novo rótulo em documento não-RC.
- Total dirigido complementar: 75 testes distintos, sem duplicar os 168 iniciais.
- Playwright: 6 cenários com API simulada, incluindo os três pedidos na revisão.
- TypeScript/build frontend e package backend: aprovados.

Não se declara repetição integral de todas as suites originais. Falha inicial
não-RC foi fixture sem código AT de série; corrigida sem enfraquecer regra AT.
Os novos browser tiveram selector de teste corrigido para o nome acessível do
combobox, sem alteração de produto. Logs locais:
/tmp/tuuli-value-date-claude-fix-final.log, /tmp/tuuli-value-date-fix-ui-corrected.log,
/tmp/tuuli-value-date-fix-build-final.log, /tmp/tuuli-value-date-fix-package-final.log,
/tmp/tuuli-value-date-fix-precision-pdf.log e /tmp/tuuli-value-date-fix-browser-final.log.
Guardas V20/V21/V22 preservadas; emissor novo inactivo, F2/T1 e consumidores
operacionais intocados. Sem push/PR/merge; nova revisão independente necessária.

## Fecho técnico MINOR-N1 — base a00e0a9, 2026-10-15

Parecer Claude comunicado pelo PO: APPROVE WITH CONDITIONS, BLOCKER 0 / MAJOR 0 /
MINOR 1. Esta correcção fica preparada para verificação independente final;
não se declara essa revisão realizada pelo implementador.

V23, ainda não integrada, estende receipt_value_date_type_guard: uma mudança
real de codigo_fiscal de ou para RC é rejeitada por SQL quando existe documento
financeiro desse tipo com data_valor não NULL. Mantém o guard anterior da natureza
da dívida e não modifica V18/V20/V21/V22 nem os serviços operacionais.

Três testes adicionais demonstram:

- RC emitido impede RC → XX; a classificação permanece RC, a Data Valor não
  pode ser apagada e a cronologia da origem continua protegida.
- Não-RC com Data Valor persistida impede XX → RC; XX → YY continua permitido.
  O fixture usa emissão não-RC legítima, que grava NULL, seguida de escrita SQL
  de Data Valor permitida para não-RC pela V23. Não inventa um RC inválido nem
  contorna constraints para produzir o estado testado.
- Tipo sem documentos com Data Valor pode mudar XX → RC → XX.

### Evidência desta execução com V23 final

PostgreSQL 16.3, container exclusivamente descartável
`tuuli-value-date-slice1a-test`. Bases novas, sem repair ou utilização das bases
anteriores com outro checksum V23:

- `tuuli_value_date_final_n1_test`: FinancialReceiptIntegrationTests,
  63 executados/aprovados, 0 falhas, 0 erros, 0 ignorados. Inclui os três novos
  testes SQL, compatibilidade RC/não-RC, imutabilidade, limites temporais,
  constraints antecipadas/diferidas e regressão financeira dirigida.
- FinancialReceiptMigrationTests: 4 executados/aprovados, 0 falhas/erros/ignorados.
  Upgrade V19/V20/V21/V22 → V23, validação e segundo arranque sem migrations;
  preserva valores históricos, precisão e ledger, sem backfill de Data Valor.
- TestDatabaseSafetyValidatorTests: 4 executados/aprovados, 0 falhas/erros/ignorados.
- Total dirigido desta execução: 71 testes, BUILD SUCCESS.

Log: `/tmp/tuuli-value-date-n1-directed.log`.

Regressão comercial na base separada e vazia
`tuuli_value_date_final_n1_commercial_test`, também migrada até V23 final:

| Grupo | Executados | Aprovados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: | ---: |
| DocumentoComercialControllerTests | 46 | 46 | 0 | 0 | 0 |
| DocumentoComercialEmissaoIntegridadeTests | 25 | 25 | 0 | 0 | 0 |
| Total comercial | 71 | 71 | 0 | 0 | 0 |

BUILD SUCCESS; log `/tmp/tuuli-value-date-n1-commercial.log`. Separação de bases
impede interferência entre fixtures comerciais de limpeza e ledger imutável.
Total desta validação final: 142 testes distintos aprovados, sem somar novamente
execuções anteriores. Não se apresenta este conjunto como suite integral.

Diff final limitado a V23, testes de integração RC e este documento;
`git diff --check` aprovado. Sem alterações a saldos, extractos, serviços,
FinancialCore, frontend ou migrations integradas. Não foi necessário repetir
browser/build frontend nesta correcção exclusivamente SQL/testes/documentação.
Não houve migrations em bases persistentes, alteração da demo ou activação
F2/T1. Publicação e revisão independente final permanecem gates posteriores;
nenhum push, PR ou merge foi executado nesta missão.
