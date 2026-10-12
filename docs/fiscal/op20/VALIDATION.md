# OP20 — Validação e entrega

12-10-2026. Implementação e testes locais; publicação não autorizada.

- Compilação inicial: BUILD SUCCESS.
- Núcleo inicial:18 testes aprovados (12 integração,2 monetários,4 safety).
- Revisão inicial:2 MAJOR SQL (extensão de operação consolidada e estornos não
  completamente validados), corrigidos com triggers e testes negativos.
- Regressão conjunta inicial:96/97; única falha por clientes da fixture OP20
  contaminarem contagem do lookup existente. Expectativa original preservada;
  validação final separa as bases descartáveis de núcleo e regressão.
- Núcleo após correções finais:22/22, zero falhas/erros/skips:15 integração,
  2 monetários,1 upgradeV17→V18 e4 safety, PostgreSQL16.3 descartável.
- Regressão dirigida:84/84, zero falhas/erros/skips:44 controller comercial
  (com prova de isolamento FT/RC),20 integridade de emissão,10 extrato,
  2 PDF financeiro,1 schema,3 integridade referencial e4 safety. Esta bateria
  precede o último reforço SQL de cancelamento; os circuitos não mudaram,
  e os testes do núcleo foram repetidos após esse reforço.
- Compatibilidade fiscal legacyV15→V18:1/1, preserva catálogo e emitidoV2;
  expectativa de versões alinhada com V18 sem retirar verificações históricas.
- Package final: mvn -o -DskipTests package — BUILD SUCCESS.
- Revisão independente por op20_core_review: APROVAR, BLOCKER0/MAJOR0/MINOR0.
  Revisão estática do código/migration/testes/documentação e inspeção de logs;
  o revisor não executou testes. Dois MAJOR iniciais e a equivalência do estado
  cancelado foram corrigidos e revalidados antes da aprovação.
- git diff --check, staged diff e ligações locais: aprovados.
- Entrega preparada para commit local; sem push/PR. Commit deve ser consultado
  no Git, evitando referência autorreferencial no documento.

Logs locais ignorados: /tmp/op20-core-reviewed.log, /tmp/op20-regression-final.log,
/tmp/op20-fiscal-upgrade.log e /tmp/op20-package-final.log. Artefactos não são certificação ou validação jurídica.
Sem migrations em bases persistentes, alterações de CI/frontend ou push/PR.
