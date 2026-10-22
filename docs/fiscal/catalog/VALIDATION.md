# Catálogo fiscal — fundação inactiva: validação e revisão

22-10-2026. Base integrada PR #34, 3bc2f70c473b0cd6bb5011ba46eb4b0dc382bc7f.
Branch codex/fiscal-catalog-dcat1. Contratos documentais d2ebd0b e 79766bc preservados.
Ordem de implementação posterior autorizou apenas esta fundação inactiva.

## Implementação

- V27__inactive_fiscal_catalog.sql: tabela de revisões com identidade composta
  código/revisão, JSON com identidade/schema coerentes e revisões positivas.
  Qualquer revisão inserida é imutável: UPDATE, DELETE e TRUNCATE recusados.
  Não existe estado draft/publicação editável nem editor de semântica.
- tipodocumento recebe par opcional de colunas de vínculo, FK à revisão e
  identidade fiscal coerente. Vínculo apenas no INSERT; qualquer mudança
  posterior, incluindo NULL→revisão ou remoção, é recusada. Colunas existentes,
  V25/V26 e migrations integradas não alteradas. Sem atribuições históricas.
- fiscal/catalog.json é a definição governada da release, com revisions vazio.
  Futuras definições exigem matriz homologada e migration de materialização
  controlada; esta entrega não cria valores fiscais ou correspondência RL.
- FiscalCatalog, serviço interno: verifica conjunto completo e conteúdo materializado
  em uma query, comparação JSON com decimais exactos; rejeita materialização
  diferente/incompleta antes de devolver revisão. Identidades duplicadas ou manifesto
  inválido recusados; definições devolvidas são cópias defensivas. Não há procura
  da revisão mais recente, reinterpretação de mestres ou classificação de fallback.
- forType devolve ausência explícita para tipo sem vínculo, sem alterar o DTO
  operacional. Não é nova política de «classificação desconhecida» dos leitores
  existentes. Sem chamadores operacionais, endpoint, hook de arranque ou alterações
  no CRUD; ausência de catálogo não impede arranque/uso do legado.

O conteúdo JSON será regra de negócio apenas depois de aprovado. Os mecanismos
de integridade não homologam combinações de área, Entidade, sinal ou efeitos.
O vínculo novo foi exercitado por SQL em fixtures; não é exposto ao utilizador.
A protecção de DDL contra administrador/proprietário que remova triggers está
fora do contrato; não se alteraram roles/secrets. Inserções governadas são feitas
por migrations, não por uma API Java de escrita. SQL estranho ao manifesto
é detectado ao usar/verificar o catálogo, não por uma nova fiscalização de arranque.

## Testes efectivamente executados

PostgreSQL 16.3, contentor exclusivo tuuli-dcat1-test, tmpfs e porta local 25437;
base fac_catalog_test e bases auxiliares com sufixo _test. Contentor removido ao
concluir; nenhuma base persistente ou demo utilizada.

| Classe | Aprovados |
| --- | --- |
| FiscalCatalogTests | 25 |
| DocumentTypeImmutabilityTests | 17 |
| FinancialDocumentClassificationTests | 14 |
| FinancialCoreMigrationTests | 1 |
| FinancialReceiptMigrationTests | 6 |
| ReceiptPayloadCompatibilityTests | 4 |
| FinancialQueriesIntegrationTests | 8 |
| FinancialReconciliationIntegrationTests | 22 |
| TestDatabaseSafetyValidatorTests | 4 |
| FinancialReceiptIntegrationTests | 80 |
| DocumentoFinanceiroPdfServiceTests | 3 |
| **Total distinto** | **184** |

Todas as classes: zero falhas/erros/ignorados nas execuções finais. Fases:
regressão 76 + package final 108 = 184, conjuntos disjuntos.
A fase dirigida intermédia de 24 testes sobrepõe-se aos 25 finais; não somar.
Execução inicial: 22 testes, quatro falhas de expectativa de espera na FK.
A investigação demonstrou recusa imediata da referência a pai não confirmado.
Os testes finais verificam SQLSTATE 23503, ausência de configuração parcial,
commit/rollback e retry depois de publicação confirmada, em READ COMMITTED e
REPEATABLE READ. Não foi declarada serialização por espera onde PostgreSQL recusa.
Nenhum enfraquecimento de invariante para obter verde.

Novos casos: revisão inválida/duplicada, SQL directo/UPSERT/TRUNCATE, vínculo
estável mesmo após nova revisão, proibição de backfill, identidade/FK/par
incompletos, diferenças de materialização com precisão decimal elevada, rollback
integral, uso READ ONLY e cópias defensivas. Configuração vinculada emitida e
anulada conserva V25/V26. Fixtures Z9/JSON fixtureOnly são sintéticos e nunca
foram adicionados ao manifesto de produção ou à demo.

Upgrade próprio V26→V27 compara tipo preexistente excluindo apenas as duas
novas colunas, verifica catálogo vazio, validate e segundo arranque sem migration.
Regressão de upgrades V17 e V19…V25 até V27 conserva valores/documentos históricos;
apenas contagens esperadas de schema foram actualizadas em quatro testes antigos.
V25/V26 não foram editadas nem dispensadas. Testes de leitura histórica, paginação
500/501, isolamento e Data Valor do diagnóstico continuam aprovados.

Comandos (credenciais apenas do contentor descartável):

```bash
export FAC_TEST_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:25437/fac_catalog_test
export FAC_TEST_DATASOURCE_USERNAME=postgres
export FAC_TEST_DATASOURCE_PASSWORD=postgres
mvn -o -B -Dtest=DocumentTypeImmutabilityTests,FinancialDocumentClassificationTests,FinancialReceiptMigrationTests,FinancialCoreMigrationTests,ReceiptPayloadCompatibilityTests,FinancialQueriesIntegrationTests,FinancialReconciliationIntegrationTests,TestDatabaseSafetyValidatorTests test
mvn -o -B -Dtest=FiscalCatalogTests,FinancialReceiptIntegrationTests,DocumentoFinanceiroPdfServiceTests package
```

Package: BUILD SUCCESS. Não é suite integral, prova de CI ou certificação fiscal.
Não houve alteração frontend, pelo que não se executou build/browser.

## Evidência preservada

Arquivo externo reservado Relatorios-AR2/TUULI/Arquivo-Reservado/OP22-Catalogo-Fatia1:
26 artefactos, 847.533 bytes (quatro logs, 11 XML e 11 TXT), copiados byte a byte
sem alterar originais; manifesto adicional com origem/tamanho/SHA-256.
MANIFEST_SHA256.tsv SHA-256:
6d6574bb3f6307a9278f7b1818878ce200627e55744da81d8e0e2da4dca4a6fc.
Conserva a falha inicial e as execuções finais, sem reconstruir evidência.

## Revisão cruzada — pendente

Claude não disponível: command -v claude não encontra executável e não existe
ferramenta Claude exposta. Nenhuma revisão independente ou aprovação declarada.
Pedido para Claude, exclusivamente leitura:

1. Ler AGENTS, canónicos, ADR-003, FIRST_SLICE e matriz D-CAT-2.
2. Rever git diff 79766bc..codex/fiscal-catalog-dcat1; contexto documental desde
   d2ebd0b disponível, sem reabrir decisões do PO.
3. Verificar CHECK/FK/triggers V27, vínculo apenas na criação, imutabilidade de
   revisão, semântica do manifesto e precisão, concorrência/rollback e ausência
   de efeitos em V25/V26, leitores ou emissores existentes.
4. Confrontar os XML/logs arquivados; distinguir mecanismo sintético de matriz
   fiscal homologada e ensaios do Executor de testes próprios do Revisor.
5. Emitir APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES e contagem
   BLOCKER/MAJOR/MINOR. Não corrigir ficheiros nem migrar bases persistentes.

Impedimentos à integração: revisão cruzada e verificações de publicação ainda
pendentes; não existe CI novo nesta missão. D-CAT-2 bloqueia uma futura validação
operacional de tipologias, não é falsamente declarada aprovada para esta fundação.
Sem push, PR, merge ou activação. RC/RL, FinancialCore operacional e 1e inactivos;
legado única autoridade operacional.
