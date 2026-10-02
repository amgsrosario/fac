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
- Nenhuma alteração funcional adicional no fecho. Fundação de Permissões: **NÃO INICIADA**.

## Sequência estratégica vigente

1. JWT / Gestão e Invalidação de Sessões — **CONCLUÍDA**.
2. Fundação de Permissões — **PRÓXIMA MISSÃO; NÃO INICIADA**.
3. Security Gate.
4. Fecho Fiscal Sistemático.
5. Auditoria Funcional Final.
6. Preparação e Certificação AT.
7. Piloto Controlado.

Esta sequência é a orientação vigente do Executivo. A conclusão JWT não antecipa os gates seguintes nem declara certificação ou prontidão global de segurança.

## Resíduos e limites

- Não foi criado um fluxo de alteração de password pelo próprio utilizador; o caminho implementado nesta missão é o reset administrativo existente.
- Logout permanece client-side; não existe revogação individual de JWT introduzida nesta missão.
- A Fundação de Permissões e o capítulo global Segurança continuam pendentes.
- A publicação documental segue o fluxo de branch/PR contra `feature/visual-redesign`; o merge é uma decisão humana de António.
