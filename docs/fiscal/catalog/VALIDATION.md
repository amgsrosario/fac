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
  inválido recusados na utilização; definições devolvidas são cópias defensivas. Não há procura
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

## Revisão cruzada — parecer inicial e confirmação pendente

Parecer inicial Claude sobre e54a967: APROVADO COM RESERVAS, lido no arquivo
2026-10-22_OP22_V27-catalogo-fiscal_revisao-Claude.md (ver complemento abaixo).
A confirmação do diff complementar ainda não foi realizada: command -v claude
e claude.exe não encontram executável, nem há ferramenta Claude exposta.
Não se declara confirmação independente inexistente.
Pedido para Claude, exclusivamente leitura:

1. Ler AGENTS, canónicos, ADR-003, FIRST_SLICE e matriz D-CAT-2.
2. Rever git diff e54a967..codex/fiscal-catalog-dcat1 para confirmação das reservas; contexto completo desde
   79766bc e documental desde
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


## Complemento — reservas da revisão Claude sobre e54a967

Parecer estático lido integralmente no arquivo externo:
2026-10-22_OP22_V27-catalogo-fiscal_revisao-Claude.md, SHA-256
0761874b997065355adf212bc6815f8a5a73ed640130d5752d3734420fa1fa9c.
Resultado original APROVADO COM RESERVAS, sem defeitos bloqueantes; uma reserva
de cobertura RES-1 e sugestões SUG-1/SUG-2. Não executou testes ou migrations.

| Observação | Resolução executada pelo Codex |
| --- | --- |
| RES-1 | Regressão adicional das três classes pedidas: 3 + 46 + 25 = 74 aprovados |
| SUG-1 | Leitura/validação do manifesto adiada para utilização explícita; construtor só guarda dependências. Sem fallback ou cache de revisão alternativa |
| SUG-2 | Comentário distingue igualdade numérica jsonb de representação textual, que pode conservar zeros; teste 123.4500 vs 123.45 confirma igualdade sem perda de precisão |

Todos os métodos de utilização (verifyCoherence, revision, forType) validam
manifesto/materialização antes de usar o catálogo. forType valida também antes
de consultar tipos sem vínculo; não converte erro do manifesto em ausência.
Revisões continuam explicitamente identificadas, sem procura da mais recente.
A leitura é feita por invocação, sem erro guardado silenciosamente nem fallback.

FiscalCatalogInitializationTests: cinco contextos Spring dirigidos, com manifesto
ausente, vazio, JSON malformado, estrutura inválida e null. A inicialização eager
do bean e consumidor legado dirigido tem sucesso; os três métodos de utilização
lançam IllegalStateException, sem consulta JDBC. É contexto mínimo de regressão
do isolamento do bean, não prova de um servidor HTTP completo com recurso ausente.
As classes comerciais abaixo exercitam o contexto completo com o manifesto normal.

### Execução adicional efectivamente realizada

| Classe | Testes aprovados |
| --- | --- |
| FiscalCatalogTests | 26 |
| FiscalCatalogInitializationTests | 5 |
| TipoDocumentoControllerTests | 3 |
| DocumentoComercialControllerTests | 46 |
| DocumentoComercialEmissaoIntegridadeTests | 25 |
| DocumentTypeImmutabilityTests | 17 |
| FinancialDocumentClassificationTests | 14 |
| FinancialReceiptMigrationTests | 6 |
| FinancialCoreMigrationTests | 1 |
| TestDatabaseSafetyValidatorTests | 4 |
| **Total distinto desta execução complementar** | **147** |

Zero falhas, erros ou ignorados. Fase dirigida 31 + regressão/package 116 = 147;
não somar aos 184 anteriores, porque há sobreposição. Base PostgreSQL 16.3
exclusivamente descartável fac_catalog_reserves_test em 127.0.0.1:25438, contentor
tuuli-dcat1-reserves-test com tmpfs, removido após validação. Migrations até V27,
validate e segundo arranque aprovados. Antes da remoção, base principal de
regressão: schema 27, zero definições e zero tipos vinculados. V27 e V1–V26 não
foram alteradas por esta correcção; manifesto de produção continua vazio.

Comandos adicionais:

```bash
export FAC_TEST_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:25438/fac_catalog_reserves_test
export FAC_TEST_DATASOURCE_USERNAME=postgres
export FAC_TEST_DATASOURCE_PASSWORD=postgres
mvn -o -B -Dtest=FiscalCatalogInitializationTests,FiscalCatalogTests test
mvn -o -B -Dtest=TipoDocumentoControllerTests,DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests,DocumentTypeImmutabilityTests,FinancialDocumentClassificationTests,FinancialReceiptMigrationTests,FinancialCoreMigrationTests,TestDatabaseSafetyValidatorTests package
```

Package BUILD SUCCESS. git diff --check e referências documentais verificados.
Evidência complementar: Relatorios-AR2/TUULI/Arquivo-Reservado/OP22-Catalogo-V27-Reservas,
23 artefactos, 782.410 bytes: dois logs, dez XML, dez TXT e cópia do parecer inicial.
Identidade origem/cópia verificada; manifesto adicional SHA-256
a5970ae6fcb25fe8f3765e9ba721f469f9bafed8fa6a9783242a2b909d3222b1.
Arquivo anterior preservado sem substituição.

### Confirmação dirigida a solicitar ao Claude

Rever diff e54a967..HEAD desta branch: confirmar RES-1 nos XML/logs, SUG-1 no
construtor e nos três métodos de uso, SUG-2 no comentário/teste numérico. Verificar
ausência de fallback, alterações de migrations, catálogo activo ou mudanças de
produto. Emitir confirmação explícita das três observações, veredicto e findings
por gravidade. Não é necessária nova auditoria arquitectónica.

Não foi possível executar/submeter a revisão nesta sessão: Claude/claude.exe
não encontrados no PATH nem /home/arosario/.local/bin/claude; sem ferramenta Claude.
Pedido preparado, não enviado a agente de outro modelo. Aguardar confirmação
independente efectiva antes de push/PR/integração. Não se invocou GitHub nem se
alterou autenticação para contornar esta condição. Nenhuma nova aprovação
independente, CI verde ou integração declarada pelo Executor.
