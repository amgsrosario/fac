# TUULI AIR — Estado Operacional

Fonte canónica do estado corrente. Atualização: 2026-10-02, Europe/Lisbon. Substituir informação obsoleta quando o estado mudar; o histórico detalhado permanece no Git. Consultar também [INSTITUTIONAL_MEMORY.md](INSTITUTIONAL_MEMORY.md).

## Base produtiva confirmada

- Linha produtiva: `feature/visual-redesign`.
- HEAD remoto confirmado por fetch neste fecho pós-merge documental: `8877b9281f43212c479d5a96e5efb9a985a2d03a` (merge do PR #10, que integra `eac635e4504cdc95e3cdb4c4884a4075459b96d5`).
- Este identificador é a referência confirmada neste fecho, não o hash do commit que contém este ficheiro. Registar o próprio HEAD tornaria a atualização autorreferencial; não encadear commits apenas para acompanhar esta correção documental. Para obter o HEAD remoto corrente, atualizar as referências Git e consultar `origin/feature/visual-redesign`.

## JWT / Gestão e Invalidação de Sessões

**CONCLUÍDA E INTEGRADA. Segurança JWT: FECHADA. Capítulo global Segurança: AINDA NÃO FECHADO.**

| Entrega | Estado confirmado |
| --- | --- |
| PR principal #8 | Integrado; merge `acf74763a6b3a56000d9e7fc3c11a3a4958c0c59` |
| Implementação principal | `dc82d397ba9bdc7ec2672484c1ae92225c5d6f4a`, integrada |
| PR complementar #9 | Integrado; merge `b042b3bea660b8b5b65b87acc08ea331f75cc6f0` |
| Validação estrita e testes complementares | `ebb71bc8f91b8e60bb4a81b61436caadddf9ab32`, integrado |

A ancestralidade dos dois commits na base remota foi confirmada. Não existe trabalho funcional JWT por integrar.

### Validação final da missão

| Verificação | Resultado |
| --- | --- |
| SecurityIntegrationTests | 11 aprovados |
| UtilizadorControllerTests | 7 aprovados |
| TestDatabaseSafetyValidatorTests | 4 aprovados |
| Total dirigido | 22 aprovados, sem falhas ou erros |
| PostgreSQL de validação | 16.3 descartável |
| Flyway | 15 migrations validadas; schema V15 em duas inicializações |
| Package | `mvn -o -DskipTests package`: BUILD SUCCESS |
| Diff | `git diff --check`: aprovado |
| Revisão independente final | BLOCKER 0 / MAJOR 0 / MINOR 0; gate aprovado |
| CI do PR #9 | Verde, confirmado pelo Executivo na instrução de institucionalização |

A revisão independente foi estática, baseada no código anteriormente inspecionado e no diff complementar fornecido, sem reinspeção do commit final nem execução própria dos testes pelo revisor. Os testes e package foram executados pelo Executor. O CI do PR #9 não foi consultado diretamente nesta sessão, por ausência de cliente/conector GitHub disponível; a confirmação acima provém do Executivo.

V14 acrescenta `token_version` e V15 acrescenta `row_version`, ambas com valor inicial 0 e sem remoção de dados. A cobertura dirigida inclui login e acesso normal, reset/token antigo/password antiga e nova, desativação, reativação sem ressuscitar tokens, tokens expirados/adulterados/utilizador inexistente, claims inválidas e conflito otimista de entidade desatualizada.

### Fecho Git e evidência local

- Worktree `/home/arosario/Projetos/fac/.worktrees/jwt-session-invalidation`: removida.
- Branch `feature/jwt-session-invalidation`: removida localmente e em `origin` após confirmação da integração.
- Registos preservados: `/home/arosario/Projetos/fac/target/jwt-session-invalidation-validation` (logs de testes/package e três relatórios Surefire; ficheiros locais ignorados pelo Git, não arquivo institucional permanente).
- Checkout principal: alterações preexistentes em `docker-compose.yml` e `.worktrees/` preservadas e excluídas da institucionalização documental. A branch local principal estava atrás da referência remota; não foi atualizada nem usada como prova da base produtiva.
- Nenhuma alteração funcional adicional no fecho JWT. O estado da Fundação de Permissões encontra-se abaixo.

## Fundação de Permissões

**IMPLEMENTADA E VALIDADA LOCALMENTE; AINDA NÃO INTEGRADA. Merge reservado ao António.**

- Base da missão confirmada por fetch: origin/feature/visual-redesign em da5c75fbcf5744611a7de39d3e1dfa15b62b86e7. A referência documental anterior na secção de base produtiva conserva o significado histórico do respetivo fecho.
- Branch: feature/permissions-foundation. Worktree isolada: /home/arosario/.codex/worktrees/permissions-foundation/fac.
- Três perfis e mapa exato das 16 capacidades preservados. Contrato backend tipado e catálogo/helper frontend partilhados. PDF financeiro alinhado com DOCUMENTO_OBTER_PDF; acesso dos três perfis preservado.
- Mudanças efetivas de perfil incrementam token_version; tokens anteriores passam a 401. Pedidos com o mesmo perfil conservam a sessão. Novo login recebe as novas autoridades; último administrador, autoelevação e optimistic locking preservados.
- Nenhuma migration criada ou alterada. O teste de schema, anteriormente preso a V12, foi alinhado com V15 já existente e reforçado com verificação de token_version/row_version bigint não nulas, com default zero.
- Suite integral: 236 testes aprovados, zero falhas/erros/omitidos, na PostgreSQL 16.3 descartável. Inclui Segurança 23, matriz HTTP com login real 6, Utilizadores 7, Safety 4 e schema V15. A matriz usa operações e PDFs reais, sem mocks dos serviços de negócio.
- Frontend: TypeScript e build Vite aprovados; dois testes do helper aprovados com Playwright existente. Runtime Node Linux temporário utilizado para evitar limitações UNC do Node Windows, sem alterações globais ou ao CI.
- Package final: BUILD SUCCESS. Revisão independente por review_permissions_final: APROVADA, BLOCKER 0 / MAJOR 0 / MINOR 0. Revisão estática do diff completo e ficheiros novos; o revisor não repetiu os testes executados pelo Executor.
- Branch feature/permissions-foundation publicada em origin, com push normal confirmado. PR ainda não criado e CI não consultado nesta sessão; integração pendente.
- Não existe gh/gh.exe nem conector GitHub disponível nesta sessão para criar/consultar PR e CI. A OP admite entrega do link para criação manual do PR contra feature/visual-redesign após o push da branch; CI e conflitos no GitHub devem ser confirmados antes do gate humano. Não declarar PR verde sem evidência.
- Alterações preexistentes do checkout principal em docker-compose.yml e .worktrees/ preservadas e excluídas desta missão.

## Sequência estratégica vigente

1. JWT / Gestão e Invalidação de Sessões — **CONCLUÍDA**.
2. Fundação de Permissões — **IMPLEMENTADA; VALIDAÇÃO LOCAL APROVADA; ENTREGA/GATE HUMANO PENDENTES**.
3. Security Gate.
4. Fecho Fiscal Sistemático.
5. Auditoria Funcional Final.
6. Preparação e Certificação AT.
7. Piloto Controlado.

Esta sequência é a orientação vigente do Executivo. A conclusão JWT não antecipa os gates seguintes nem declara certificação ou prontidão global de segurança.

## Resíduos e limites

- Não foi criado um fluxo de alteração de password pelo próprio utilizador; o caminho implementado nesta missão é o reset administrativo existente.
- Logout permanece client-side; não existe revogação individual de JWT introduzida nesta missão.
- A integração da Fundação de Permissões e o capítulo global Segurança continuam pendentes. Esta missão não antecipa o Security Gate.
- A publicação documental segue o fluxo de branch/PR contra `feature/visual-redesign`; o merge é uma decisão humana de António.
