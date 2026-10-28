# H2-03 / issue #39 — validação da implementação

Data: 27-10-2026. Executor: Codex. Base:
`c17a9d8af9cf2d197c8c8c21f1f780a38efee0d9` (PR #37).
Branch: `codex/h2-03-document-type-contract`.
Contrato e autoridade: [H2_03_IMPLEMENTATION.md](H2_03_IMPLEMENTATION.md).

## Ambiente e evidência

PostgreSQL **16.3 descartável**, contentor `tuuli-h203-issue39-158511`, cluster
`7701408400379076653`, porta loopback 64859, tmpfs e rede exclusivos da missão.
Cada fase Maven usa uma base nova com sufixo `_test`; os testes de migration
criam ainda bases filhas. O runner confirma a identidade do cluster antes de uso.
Não foi executado qualquer comando na base histórica `fac/facdb`, nem na
instalação isolada da issue #38. Credenciais aleatórias fora do arquivo de evidência.

Evidência local reservada: `tuuli-backups/issue39/2026-10-27-h203` no directório
do utilizador executor. Contém logs originais, XML/texto Surefire copiados por
fase, resumos JSON, ferramentas do ensaio de restauro e manifesto SHA-256.
Não depende da conservação de `target/`. Os dumps são exclusivamente sintéticos.

Comando base efectivo, substituindo `<classes>` pelos nomes registados em cada
`<fase>.json`:

```sh
mvn -o -B -Dspring.profiles.active=test -Dspring.jpa.show-sql=false -Dtest=<classes> test
```

JDK 21 e Maven 3.9.16 existentes; modo offline, sem instalar dependências.
As variáveis FAC_TEST_DATASOURCE apontaram apenas para a instância descartável.

## Fecho incremental dos três MINOR — 28-10-2026

Parecer original Claude: `2026-10-28_TUULI_Issue39_H2-03_revisao-independente_Claude.md`,
10711 bytes, SHA-256
`b2475e9a09ebeda4c6170f6f57958648770297f5b018a2cd42d9d397ed3db74c`.
Arquivo privado AR2-Executive-Reports, commit
`b3234d47243707935b4c92cbb7d793951b17dc15`, pasta `TUULI/2026/10/`.
Veredicto APPROVE WITH CONDITIONS, 0 BLOCKER / 0 MAJOR / 3 MINOR.

**MINOR-001:** executadas novamente as mesmas 15 classes contra o commit exacto
`6c32e283c474512014b075c0196ccf34c583999f`. Antes e depois de cada classe:
HEAD confirmado, worktree limpa e hashes SHA-256 de todos os ficheiros versionados
idênticos. Nenhum ficheiro do repositório foi alterado durante a execução.

Primeira tentativa conjunta numa base partilhada: 318 executados, 316 aprovados,
2 falhas, zero erros/ignorados. As duas falhas comerciais foram contagens globais
contaminadas por dados de outras suites: lookup esperava 2 e obteve 108;
extractos esperavam 10 e obtiveram 15. A tentativa original foi preservada em
`tuuli-backups/issue39/2026-10-28-h203-minors`. Nenhuma asserção, fixture ou código
foi alterado para a fazer passar.

Consolidação final: uma base nova por classe no mesmo cluster PostgreSQL 16.3
descartável, **318 executados/aprovados, zero falhas, erros ou ignorados**. As 15
invocações e respectivos XML estão em `commands.json`, `summary.json` e pastas
por classe em `tuuli-backups/issue39/2026-10-28-h203-minors-isolated`.
É a mesma selecção de 15 classes da tabela abaixo, sem soma de execuções repetidas.
`git-before.json` e `git-after.json` registam o código exacto; o runner verificou
também HEAD/limpeza/hashes antes e depois de cada classe.

Manifesto desta execução isolada: `MANIFEST.sha256`, SHA-256
`a7171f1a09c86196c27b171f8e70b37b2e4edb84dcf41852697543b871647815`.
Evidência preparada para arquivo no GitHub privado, com manifestos e resumos
para revisão dirigida. Não se executaram novos testes de
restauro, package, frontend ou CI nesta correcção; os resultados anteriores
continuam identificados na sua própria execução.

**MINOR-002:** RC/RL/guias sem identidade fiscal explícita nas áreas inventariadas
mantêm `configurationReady()` false como pendência de correspondência/classificação,
não como erro nem bloqueio universal de criação. Não atribuir um código presumido
para fechar o gate. [Contrato documentado](H2_03_IMPLEMENTATION.md).

**MINOR-003:** documentada a recusa 409 intencional de edições apenas descritivas
de tipos vinculados quando o catálogo da aplicação está incoerente. Mantém-se a
falha em fechado e a ausência de fallback; não se alterou o serviço nem o SQL.
A emissão continua a utilizar as garantias persistentes existentes.

A revisão incremental dos três achados permanece pendente. Os registos de
27-10-2026 abaixo são evidência histórica da implementação, substituídos pela
reexecução acima para a condição específica de correspondência ao commit final.

## Resultados da validação inicial de 27-10-2026 por classe

| Classe | Executados/aprovados | Evidência final |
| --- | ---: | --- |
| FiscalCatalogInitializationTests | 5 | directed-final |
| FtDeferredSemanticsTests | 16 | directed-final |
| FiscalCatalogTests (contrato V27, target explícito 27) | 26 | directed-final |
| GovernedFtPersistenceTests | 34 | persistent-final |
| GovernedFtControllerTests | 19 | regression-financial-final |
| TipoDocumentoControllerTests | 3 | regression-financial-final |
| DocumentoComercialControllerTests | 46 | regression-commercial |
| DocumentoComercialEmissaoIntegridadeTests | 25 | regression-commercial |
| DocumentTypeImmutabilityTests | 17 | regression-commercial |
| FinancialDocumentClassificationTests | 14 | regression-financial-final |
| FinancialReceiptIntegrationTests | 80 | regression-financial-final |
| FinancialReconciliationIntegrationTests | 22 | regression-financial-final |
| FinancialCoreMigrationTests | 1 | regression-financial-final |
| FinancialReceiptMigrationTests | 6 | regression-financial-final |
| ReceiptPayloadCompatibilityTests | 4 | regression-financial-final |

**318 testes distintos**, nas classes acima, aprovados na respectiva execução
final; zero falhas, erros ou ignorados nessas execuções seleccionadas. Não é a
suite integral do projecto. Fases sobrepostas não são somadas como testes únicos.
Em particular, directed-final executou 100 e regression-financial-final 149,
com 19 testes comuns; a regressão comercial teve 105, com classes repetidas
nas fases finais e o incidente de expectativa descrito abaixo.

Package: `mvn -o -B -DskipTests package`, BUILD SUCCESS. Esta operação não executa
testes. Não houve CI da branch local, build de frontend ou execução Windows.

## Cobertura e rastreabilidade T01–T15

| Contrato | Evidência obtida |
| --- | --- |
| T01/T02 | POST/GET com vínculo real, flush antes de 201; modalidade FT explícita; tuplo inválido e revisão enviada pelo cliente recusados; RC/RL/guias sem código mantidos |
| T03/T06 | Cinco atributos antes da emissão, revisão estável, alteração compatível, SQL directo/forma de escrita antiga, descrição/marker V25, conversões e P16 recusados quando abrangidos |
| T04 | UPSERT, INSERT SELECT, multi-row e COPY PostgreSQL reais; rollback sem linhas parciais |
| T05/T08 | V27 mantém par/FK/vínculo/revisão imutáveis; nova revisão e mudança explícita de pointer não reinterpretam os tipos anteriores |
| T07 | Duas sessões READ COMMITTED e REPEATABLE READ para publicação/visibilidade e mesma PK; regressão de primeira emissão/edição V25 e atomicidade RC |
| T09 | V27→V28, validate, segundo arranque sem migrations; upgrades financeiros/comerciais existentes até V28, preservando documentos e V25/V26 |
| T10 | Parser estrito, precisão numérica V27, tipos/tuplos/duplicados/incoerências, recusas equivalentes Java/SQL para definições malformadas |
| T11 | CRUD, 71 testes comerciais de emissão, 17 V25, 14 V26, 80 recibos, 22 reconciliação e migrations financeiras |
| T12 | Cinco verificações Linux de restauro/upgrade/integridade abaixo; nenhuma restauração de ambiente real ou execução de helper Windows |
| T13 | Arranque legado com manifesto inactivo/ausente/inválido quando não utilizado; uso governado falha em fechado, sem fallback |
| T14 | 401/403 existentes, leitura persistida, inventário agregado ordenado de número fixo de consultas; sem novo endpoint |
| T15 | Inventário sintético inclui tipo ID FT com código NULL sem lhe inventar identidade, bloqueia prontidão e nunca permite activação |

## Restauros descartáveis efectivamente executados

`restore-scenarios/summary.json` regista cinco verificações aprovadas:

1. Dump completo V27, restauro, upgrade V28, preservação exacta dos tipos e
   prontidão fechada para configurações legadas incompatíveis.
2. Data-only de tipos FT sem vínculo introduzidos numa V28: recusa atómica,
   sem linhas parcialmente importadas.
3. Script legado P16 de atribuição FT por ID, dentro de transacção controlada:
   recusa, rollback e código NULL preservado.
4. Dump completo V28, restauro, validate/segundo arranque, tipos preservados e
   nova escrita inválida recusada pelos triggers restaurados.
5. Inventário pós-restauro não declara prontidão/activação com manifesto vazio.

Não se restauraram GOLDEN, demo ou desenvolvimento reais. Não se executaram
helpers Windows nem um binário antigo completo. As escritas SQL equivalentes
demonstram a protecção persistente, mas não provam compatibilidade funcional
integral de clientes externos/binários não disponibilizados. Estas limitações
devem ser revistas antes de um rollout nos respectivos ambientes.

## Incidentes durante a validação e correcções

- persistent-first: 34 testes, uma expectativa textual errada no caso que excede
  varchar(2); o PostgreSQL já recusava o valor. Corrigida a asserção para integridade.
- regression-commercial: 105 testes, uma falha na contagem antiga de migrations
  da classe V26; expectativa actualizada para V28. Os restantes 91 passaram;
  os 14 dessa classe passaram posteriormente em regression-financial-final.
- regression-financial: 149 testes, dez falhas dos novos testes de controller
  por manifesto de fixture que omitia a revisão sintética de outras suites.
  O contrato recusou correctamente a incoerência. A fixture passou a representar
  todas as revisões de teste existentes e a verificar ausência de novas escritas
  em relação à contagem inicial, sem exigir uma base globalmente vazia.
  regression-financial-final: 149 aprovados, zero falhas/erros/ignorados.
- A criação JPA passou a INSERT-only com persist/flush: não usa merge para ID
  atribuído, evitando actualizar uma linha criada entretanto por outra transacção.

Os logs de tentativas com falha foram preservados. Não são apresentados como
execuções aprovadas nem como defeitos fiscais resolvidos por alteração de regras.

## Gates, limites e revisão independente

V1–V27 e o manifesto operacional não foram alterados; V28 não publica qualquer
revisão ou combinação. Tuplos 98/9/2 e 97/8/1 testam apenas o mecanismo; a fixture
de regressão com 1/1/1 e 2/1/1 é exclusivamente de teste, não homologação fiscal.
Sem seeds reais, backfill, limpeza, alteração QR/PDF ou dados operacionais.
FinancialCore/RC/RL não activados; sem cutover; Fatia 1e suspensa.

Revisão Claude da implementação recebida: APPROVE WITH CONDITIONS, 0 BLOCKER /
0 MAJOR / 3 MINOR. Reexecução e correcções documentais preparadas para revisão
incremental independente; não declarar aprovação final antes do parecer dirigido.
A aprovação da especificação e o R2 da issue #38 não substituem esta revisão.
Os ensaios não substituem homologação 2/1/1, QR/PDF ou AT.
