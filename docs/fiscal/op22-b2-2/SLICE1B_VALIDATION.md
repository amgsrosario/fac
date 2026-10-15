# OP22-B2.2 — Fatia 1b: F2/T1 no núcleo inactivo

## Base e âmbito

Base confirmada localmente, após fetch manual do PO: PR #27 integrado,
`7dccfb2ecc61c9b80a7059cf73ce25eab02061d0`, incluindo `4bd1e89` da Fatia 1a.
Nenhum fetch adicional nesta retoma. Checkout principal e outras worktrees
preservados. Implementação isolada em `codex/receipt-value-date-slice1b`.

## Contratos implementados

- M1: autoridade da Data Valor exclusivamente em documento_financeiro.data_valor.
  Não existe uma segunda data financeira editável nem backfill.
- F2: financial_operation_effect deriva SETTLE da Data Valor pelo vínculo RC.
  Operação consultável distingue emissão, Data Valor, operationAt (momento_emissao
  para RC / occurred_at do acto) e recordedAt técnico.
  dataHoraOperacao documental mantém o significado anterior; não governa efeito.
- T1: REVERSE vinculado a acto de anulação RC deriva a Data Valor do original.
  FinancialCore.reverse conserva RESTORE individuais e CASH_OUT com reversed=true
  e reverse_of do CASH_IN. É contra-lançamento, nunca reembolso efectivo.
- FinancialReceiptReversal é um bean interno sem controller, endpoint ou chamador
  legado. Acto, REVERSE, reposições, estado documental e auditoria são atómicos.
- Substituição interna conserva Data Valor original se omitida; data explícita
  equivalente dá replay. Inclui novo emissor, numeração, SETTLE e vínculo na mesma
  transacção. DV-09 admite correcção justificada: 30 − 30 + 300 = 300; outras
  afectações não são apagadas nem substituídas por snapshots anteriores.
- Cancelamento autónomo interno limitado a declaração expressa e auditável de
  que o pagamento não ocorreu (`cancelUnreceived`, NOT_RECEIVED). Evidência textual
  não constitui prova automática: o chamador autorizado futuro deve estabelecer
  o facto e os controlos de autorização. Esta entrega não implementa verificação
  bancária/caixa nem autoriza ocultar dinheiro realmente recebido. Dinheiro real
  sem substituição/tratamento válido permanece bloqueado pelo âmbito público
  ainda inexistente; reembolsos/adiantamentos/saldo não afectado não são suportados.
- Replay antes de numeração; chave com conteúdo/contexto diferente rejeitada;
  unicidade persistente de reverse_of e do acto por RC impede dupla reversão.
  Pendentes da união original/substituto e depois posições são bloqueados por ID
  crescente, antes do documento/série. Valores de Pendente não são usados para
  calcular/restaurar saldos do núcleo.

## V24 aditiva e SQL directo

V18 a V23 integradas permanecem intocadas. V24 cria financial_receipt_reversal,
com original, REVERSE, eventual substituto, tratamento declarado, payload,
evidência, actor e instante técnico imutáveis. Não cria outro ledger.

Guarda diferida exige coerência final entre reversão, acto e anulado. A alteração
de anulado de RC vinculado só é permitida quando existe o acto; demais campos e
linhas continuam imutáveis. REVERSE genérico isolado de RC não pode confirmar.
REVERSE/acto selado também não admite componentes/afectações tardios, mesmo após
SET CONSTRAINTS IMMEDIATE e retorno a DEFERRED. V18 conserva a verificação exacta
das reposições e contra-lançamentos. V22 preserva exclusão bidireccional.

Históricos NULL continuam NULL. Leitura usa emissão como compatibilidade explícita,
identificando valueDate ausente; T1 desses RC é recusado, sem inferir data real.
Não se altera o legado para permitir anular RC FinancialCore.

## Consulta histórica interna

balanceAt calcula história rectificada pela origem FT, CONSUME na Data Valor e
RESTORE na Data Valor original; não usa created_at, snapshots actuais ou o estado
actual anulado para apagar movimentos. A mesma consulta passada pode mudar após
uma rectificação T1, como aprovado no ADR-002. Não reproduz extractos antigos.

Só suporta âmbitos cujo conjunto de posições e afectações tem datas documentais
conhecidas/aprovadas. Posições genéricas sem origem, SETTLE sem RC e reversões
CREATE/genéricas sem semântica temporal aprovada provocam recusa explícita;
não se devolvem totais parciais. Residuais históricos incoerentes também são
recusados, sem corrigir dados por inferência. Não é activada nos consumidores operacionais.

## Fronteira e riscos residuais

FinancialReceiptProjections.operationalActivationPermitted continua sempre false.
Nenhum controller, serviço legado, consumidor de saldos, frontend, Docker ou CI
foi alterado. FT operacional já origina posições; recebimentos operacionais
continuam no legado e não actualizam FinancialCore. Logo os saldos internos não
podem substituir os operacionais antes de corte/reconciliação autorizados.

Gate fiscal (SAF-T, IVA de caixa, emissão tardia, formalismos) permanece obrigatório.
Demo Ubuntu não verificada e não actualizada. Chave pública, permissões do futuro
endpoint, comprovação monetária, consumidores e corte conjunto continuam pendentes.
A revisão independente Claude desta implementação ainda não foi realizada.
Esta entrega não declara CI verde nem autorização de publicação ou merge.

## Escolhas locais de implementação

A alternativa de copiar a data para o ledger criaria outra autoridade e foi
excluída por M1. A view derivada evita divergência e não altera dados existentes.
Guardar o acto apenas em texto/razão não permitiria FKs, unicidade do original
ou vínculos de substituição; V24 usa uma pequena associação aditiva no mesmo
núcleo. Não é um novo modelo de recebimento M2. Levantar genericamente a guarda
REVERSE permitiria actos incompletos; optou-se por exigir atomicidade persistente
com validação diferida e selagem imediata após vínculo. Estas são escolhas locais
para cumprir o contrato aprovado, sem alterar regras fiscais/monetárias.

Substituto não pode ser anulado antes do commit da própria substituição, inclusive
se as constraints forem verificadas antecipadamente. Reversões posteriores em
transacções próprias continuam possíveis, com novo acto auditável. O motivo e a
declaração monetária são obrigatórios; não equivalem a validação externa de factos.

## Evidência final executada — 15-10-2026

PostgreSQL 16.3 exclusivamente descartável, em container local já destinado a
testes (`tuuli-value-date-slice1a-test`, 127.0.0.1:25439). V24 final ensaiada em
bases novas, sem repair nem alterações de checksums em bases existentes:
`tuuli_slice1b_release_core_test` e `tuuli_slice1b_release_legacy_test`.

| Grupo final | Executados | Aprovados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: | ---: |
| FinancialReceiptIntegrationTests | 78 | 78 | 0 | 0 | 0 |
| FinancialCoreIntegrationTests | 15 | 15 | 0 | 0 | 0 |
| FinancialQueriesIntegrationTests | 7 | 7 | 0 | 0 | 0 |
| FinancialCoreMoneyTests | 2 | 2 | 0 | 0 | 0 |
| ReceiptPayloadCompatibilityTests | 4 | 4 | 0 | 0 | 0 |
| FinancialReceiptMigrationTests | 5 | 5 | 0 | 0 | 0 |
| FinancialCoreMigrationTests | 1 | 1 | 0 | 0 | 0 |
| TestDatabaseSafetyValidatorTests | 4 | 4 | 0 | 0 | 0 |
| DocumentoComercialControllerTests | 46 | 46 | 0 | 0 | 0 |
| DocumentoComercialEmissaoIntegridadeTests | 25 | 25 | 0 | 0 | 0 |
| DocumentoFinanceiroPdfServiceTests | 3 | 3 | 0 | 0 | 0 |
| **Total desta validação final** | **190** | **190** | **0** | **0** | **0** |

116 testes do núcleo e 74 comerciais/PDF, sem somar repetições ou evidência de
outras fases. Não se declara suite integral da aplicação. Package backend com
os 74 testes comerciais/PDF: BUILD SUCCESS. Nenhum frontend foi alterado;
browser/TypeScript/build frontend não repetidos nesta fatia backend.

Entre os 78 cenários RC, 15 foram acrescentados nesta fatia: F2 anterior/igual à
emissão, T1 com várias FT, preservação de afectação independente, timestamp de
emissão/acto distinto do efeito, replay/concorrência com chaves iguais/diferentes,
DV-09, default da Data Valor original no substituto e replay equivalente,
rollback de substituição impossível, alteração da Data Valor no substituto,
SQL directo sem acto em modo diferido/antecipado, referências/imutabilidade,
evidência/contexto, selagem após constraints e rollback, emissão concorrente,
múltiplas FT em ordem oposta e substituto activo no commit mesmo após verificações
antecipadas. O default igual à emissão e protecções RC/não-RC anteriores também
foram repetidos na mesma classe. Concorrência nova executada em READ COMMITTED;
a regressão anterior inclui as guardas cross-circuit em REPEATABLE READ.

Upgrades reais V19/V20/V21/V22/V23 → V24, validação Flyway e segundo arranque
sem migrations. Fixtures RC vinculados emitidos sob V20/V21/V22 conservam NULL;
view usa emissão apenas como compatibilidade e T1 recusa a ausência da data.
Fixture V23 é emitido com Data Valor válida, sem produzir NULL impossível.
Precisão legada e snapshots permanecem iguais. Upgrade V17 → V24 também passou.

A primeira execução revelou asserts presos à linha interna da função PL/pgSQL e
fixture V23 que precisava da Data Valor; ajustados para verificar a guarda real
e emitir de acordo com o esquema. Fixture histórico adicional recebeu timestamps
obrigatórios. Regressão comercial revelou FK nova na lista explícita de TRUNCATE:
lista adaptada apenas no teste, preservando TestDatabaseSafetyValidator e sem
CASCADE. Falhas intermédias não são apresentadas como testes aprovados; prevalecem
as duas execuções finais identificadas abaixo.

Logs locais (não arquivo institucional permanente):
`/tmp/tuuli-slice1b-release-core.log` e
`/tmp/tuuli-slice1b-release-legacy-package.log`.

Diff/whitespace revistos com `git diff --check`; referências novas conferidas.
Só contratos internos, adaptador interno, V24, testes e documentação autorizada.
Nenhuma migration integrada foi editada. SQL executado apenas em bases novas de
teste; nenhuma alteração em bases persistentes/demo. Container de teste parado
no fecho, bases descartáveis preservadas para eventual revisão local.

## Revisão independente e publicação manual

Revisão Claude desta fatia pendente; os resultados anteriores da Fatia 1a não
constituem aprovação deste diff. Rever especialmente V24 e sequências SQL,
atomicidade JPA/JDBC, tratamento declarado de dinheiro, idempotência, bloqueios,
T1, substituição DV-09, fronteira inactiva e leitura histórica suportada.
Não foi executado push, PR ou merge.

Após revisão e autorização de publicação, o PO poderá executar:

```bash
git -C /home/arosario/Projetos/fac/.worktrees/receipt-value-date-slice1b push -u origin codex/receipt-value-date-slice1b
```

PR futuro exclusivamente para `feature/visual-redesign`. Título proposto:
`feat(financial): apply receipt value dates and atomic reversals (slice 1b)`.
Não activar/deployar com esta entrega; verificar demo/Flyway e cumprir gate fiscal,
revisão independente e autorização própria do corte conjunto.

## Fecho dos MINOR Claude — base 8754ed3, 15-10-2026

Parecer comunicado pelo PO sobre a Fatia 1b: APPROVE WITH CONDITIONS,
0 BLOCKER / 0 MAJOR / 2 MINOR. Este complemento corrige apenas os dois pontos;
não representa novo parecer independente sobre o commit complementar.

### MINOR-001 — hora técnica de SETTLE

V24 ainda não integrada: operation_at mantém receipt.momento_emissao quando
existe vínculo RC; quando não existe, utiliza op.created_at. Não se utiliza
COALESCE para substituir um momento de emissão histórico ausente de um RC.
A expressão effect_date e o código de balanceAt não foram alterados.
Teste novo em FinancialQueriesIntegrationTests demonstra SETTLE genérico sem RC:
operationAt presente e igual a recordedAt; financialEffectDate permanece ausente.
A regressão RC repete a verificação de operationAt igual ao momento da emissão.

### MINOR-002 — recusas históricas explícitas

- FinancialReceiptIntegrationTests: FT com origem conhecida primeiro admite
  consulta; após anulação/reversão CREATE, a consulta é recusada, sem atribuir
  data de efeito à anulação FT nem implementar a sua semântica temporal.
- Mesma classe: FT com data conhecida primeiro admite consulta; SETTLE genérico
  sem vínculo RC conserva efeito sem data e torna balanceAt explicitamente
  indisponível para esse âmbito, sem apresentar total parcial.
- FinancialReceiptMigrationTests: RC realmente emitido sob V22, sem data_valor,
  é actualizado por Flyway até V24. NULL permanece NULL e a emissão é exposta
  apenas como referência compatível. Na situação integralmente datada suportada,
  balanceAt devolve 90 €. Uma liquidação genérica posterior sem RC/data de efeito
  torna a história insegura e a mesma consulta é recusada. NULL não é, por si só,
  motivo de recusa geral; as regras aprovadas foram preservadas.

O fixture histórico reutiliza seed explícito e mantém a posição genérica sem
origem num cliente distinto. Assim, a consulta inicial de 90 € prova que a recusa
posterior deriva do efeito novo sem semântica, e não de uma posição incidental
já fora do âmbito documental. Não se desactivaram triggers nem se fabricaram
históricos NULL sob a V23, que já exige Data Valor nas novas emissões RC.

### Validação efectiva deste complemento

PostgreSQL 16.3 exclusivamente descartável; base nova
`tuuli_slice1b_minor_final_test` no container de testes da missão. As bases
anteriores com checksum V24 diferente não foram reutilizadas nem reparadas.

| Grupo | Executados | Aprovados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: | ---: |
| FinancialReceiptIntegrationTests | 80 | 80 | 0 | 0 | 0 |
| FinancialQueriesIntegrationTests | 8 | 8 | 0 | 0 | 0 |
| FinancialReceiptMigrationTests | 6 | 6 | 0 | 0 | 0 |
| **Total dirigido** | **94** | **94** | **0** | **0** | **0** |

Inclui quatro testes novos e a regressão F2/T1/DV-09, replay, concorrência,
SQL/imutabilidade, default de Data Valor e limites da Fatia 1a. Os seis testes
Flyway abrangem V19/V20/V21/V22/V23 → V24 e o novo cenário V22 NULL, validação e
segundo arranque sem migrations. Não somar estes 94 aos 190 da fase anterior
como testes distintos; não se declara repetição integral da suite da aplicação.

Comando executado:

```bash
FAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25439/tuuli_slice1b_minor_final_test mvn -o -B -Dspring.profiles.active=test -Dtest=FinancialQueriesIntegrationTests,FinancialReceiptIntegrationTests,FinancialReceiptMigrationTests package
```

Package backend: BUILD SUCCESS. Log local:
`/tmp/tuuli-slice1b-minor-final.log`. `git diff --check` aprovado; diff limitado
à expressão técnica V24, três classes de testes e este registo. Não foram
alterados serviços, balanceAt, regras de recusa, saldos, extractos ou legado.
Container descartável parado no fecho. Nenhuma base persistente, demo ou outra
worktree alterada. Circuito RC continua inactivo. Sem push, PR ou merge.
