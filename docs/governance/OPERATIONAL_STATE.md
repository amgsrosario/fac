# TUULI AIR — Estado Operacional

Fonte canónica do estado corrente. Atualização: 2026-10-09, Europe/Lisbon. Substituir informação obsoleta quando o estado mudar; o histórico detalhado permanece no Git. Consultar também [INSTITUTIONAL_MEMORY.md](INSTITUTIONAL_MEMORY.md).

## Base produtiva confirmada

- Linha produtiva: `feature/visual-redesign`.
- HEAD remoto confirmado por fetch neste fecho pós-merge documental: `8877b9281f43212c479d5a96e5efb9a985a2d03a` (merge do PR #10, que integra `eac635e4504cdc95e3cdb4c4884a4075459b96d5`).
- Este identificador é a referência confirmada neste fecho, não o hash do commit que contém este ficheiro. Registar o próprio HEAD tornaria a atualização autorreferencial; não encadear commits apenas para acompanhar esta correção documental. Para obter o HEAD remoto corrente, atualizar as referências Git e consultar `origin/feature/visual-redesign`.

## JWT / Gestão e Invalidação de Sessões

**CONCLUÍDA E INTEGRADA. Segurança JWT: FECHADA. Capítulo global Segurança: FORMALMENTE ENCERRADO por decisão posterior do Executivo.**

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

**CONCLUÍDA E INTEGRADA. PR #12 merged; gate humano de merge concluído por António. Não existe trabalho funcional pendente desta missão.**

- Base da missão confirmada por fetch: origin/feature/visual-redesign em da5c75fbcf5744611a7de39d3e1dfa15b62b86e7. A referência documental anterior na secção de base produtiva conserva o significado histórico do respetivo fecho.
- HEAD produtivo observado e confirmado no fecho pós-merge: 967a14743ccc8a162811ac614f4be049863fb7f6 (merge do PR #12). A ancestralidade dos commits 2a319954b7d8de7c5a5c5a16b67d1a5b086491c4 e c67b0673c578d36a5b306c4cf8a56d29e7c90170 foi confirmada. Este é um registo do fecho, não um HEAD eternamente atual; consultar Git para o estado corrente.
- Branch feature/permissions-foundation removida localmente e em origin; worktree /home/arosario/.codex/worktrees/permissions-foundation/fac removida, com arquivo recuperável.
- Três perfis e mapa exato das 16 capacidades preservados. Contrato backend tipado e catálogo/helper frontend partilhados. PDF financeiro alinhado com DOCUMENTO_OBTER_PDF; acesso dos três perfis preservado.
- Mudanças efetivas de perfil incrementam token_version; tokens anteriores passam a 401. Pedidos com o mesmo perfil conservam a sessão. Novo login recebe as novas autoridades; último administrador, autoelevação e optimistic locking preservados.
- Nenhuma migration criada ou alterada. O teste de schema, anteriormente preso a V12, foi alinhado com V15 já existente e reforçado com verificação de token_version/row_version bigint não nulas, com default zero.
- Suite integral: 236 testes aprovados, zero falhas/erros/omitidos, na PostgreSQL 16.3 descartável. Inclui Segurança 23, matriz HTTP com login real 6, Utilizadores 7, Safety 4 e schema V15. A matriz usa operações e PDFs reais, sem mocks dos serviços de negócio.
- Frontend: TypeScript e build Vite aprovados; dois testes do helper aprovados com Playwright existente. Runtime Node Linux temporário utilizado para evitar limitações UNC do Node Windows, sem alterações globais ou ao CI.
- Package final: BUILD SUCCESS. Revisão independente por review_permissions_final: APROVADA, BLOCKER 0 / MAJOR 0 / MINOR 0. Revisão estática do diff completo e ficheiros novos; o revisor não repetiu os testes executados pelo Executor.
- PR #12 integrado em feature/visual-redesign, confirmado pelo histórico Git após fetch. O estado do CI não foi consultado diretamente pelo Executor; a confirmação do merge não é uma declaração autónoma de CI verde.
- Evidência local preservada em /home/arosario/Projetos/fac/target/permissions-foundation-validation (logs e revisão, ignorados pelo Git; não arquivo institucional permanente).
- Segurança formalmente encerrada por decisão do Executivo. Security Gate integrado; manutenção npm futura não bloqueante, sem reabertura da Fundação de Permissões.
- Alterações preexistentes do checkout principal em docker-compose.yml e .worktrees/ preservadas e excluídas desta missão.

## Security Gate

**CONCLUÍDO E INTEGRADO. PR #14 merged; HUMAN MERGE concluído por António. Não existe trabalho funcional pendente desta missão. Segurança formalmente encerrada pelo Executivo; manutenção npm futura não bloqueante.**

- Base confirmada: `origin/feature/visual-redesign`, observada em `b32720a8b7060314f1d7c3b8f79181bd41df3ca3`.
- HEAD produtivo observado e confirmado no fecho pós-merge: `f2e8dfe1c11fb61378bc01684b96eaf871343021`. Os commits `feaa5c16bd1e6af81e75f2fdbf7d01bcf2489d0e` e `b293e796eba8651417d31f65ce3e41636dc4b31c` estão integrados. Esta referência descreve o fecho; o HEAD corrente deve ser consultado no Git.
- Branch `feature/security-gate` removida localmente e em `origin`; worktree `/home/arosario/Projetos/fac/.worktrees/security-gate` removida. Evidência local preservada em `target/security-gate-validation/b293e79` (ignorada pelo Git; não arquivo institucional permanente).
- F1 — RESOLVIDO E INTEGRADO: bypass operacional recusado no arranque; excepção confinada ao contexto de testes e ausente do JAR operacional.
- F2 — RESOLVIDO E INTEGRADO: secret operacional obrigatório/validado; chave aleatória apenas em desenvolvimento explicitamente activado/teste autorizado; duração JWT 1–1440 minutos, default 60.
- F3 — RESOLVIDO E INTEGRADO: limiter próprio da aplicação por identificador/origem/global, limitado em memória por instância; resposta 429, recuperação automática, logs agregados sem credenciais; BCrypt nos caminhos de credenciais inválidas.
- F4 — RESOLVIDO E INTEGRADO: limites precoces de CSV, admissão XLSX com expansão/estrutura limitadas incluindo parts OPC não canónicos, e formato/dimensões de imagens antes de decode. Limites funcionais e protecções de exportação preservados.
- F5 — RESOLVIDO E INTEGRADO: claims essenciais e tipos validados antes de consulta de identidade; issuer `fac`, expiração obrigatória, versão persistida e invalidação existentes preservadas; sem audience nova.
- F6 — RESOLVIDO E INTEGRADO: herança Nginx corrigida e CSP simples; headers HTTP efectivos verificados em páginas, health e erro proxy. Frontend real renderizado sob CSP em navegador, sem violações observadas no smoke de login.
- Suite integral: 259 testes em 47 classes, zero falhas/erros/omitidos, com PostgreSQL 16.3 descartável e Flyway V15. Backend package e frontend TypeScript/Vite aprovados. Após endurecer o fallback de perfil dev implícito, 46 testes afectados e package repetidos antes da entrega. A prova adicional dos dois budgets de expansão XLSX passou em 6 testes de admissão/package, sem alteração funcional.
- Revisão independente: MAJOR sobre worksheet não canónica corrigido e testado; revisão final da implementação/documentação APROVADA, BLOCKER 0 / MAJOR 0 / MINOR 0 (revisão estática; testes executados pelo Executor). [PR #14](https://github.com/amgsrosario/fac/pull/14) merged em `feature/visual-redesign`; gate humano concluído. CI final de `b293e796eba8651417d31f65ce3e41636dc4b31c` confirmado verde na [execução 37504527078](https://github.com/amgsrosario/fac/actions/runs/37504527078).
- Instalação pelo lockfile frontend reportou 7 vulnerabilidades (1 moderada, 6 altas). O registo descreve a instalação observada no Security Gate. A OP fiscal confirma a decisão posterior: Segurança formalmente encerrada; manutenção de dependências npm é futura e não bloqueante, sem reabrir o Security Gate. Não foram actualizadas dependências nesta missão fiscal.
- Resíduos aceites: localStorage, logout local, ausência de refresh tokens/sessões individuais, auditoria selectiva de recusas. Limiter volátil por instância: restart/múltiplas instâncias alteram orçamento; origem pode ser partilhada atrás de proxy. PDF conserva materialização final e layout.
- Produção pública exige HTTPS. TLS/redireccionamento/certificados/renovação/HSTS no edge, firewall e gestão/rotação de secrets são responsabilidades do deployment. Contrato completo em [SECURITY_GATE.md](../security/SECURITY_GATE.md).
- Checkout principal e respectivas alterações preexistentes preservados. No fecho do Security Gate não houve migration, nova permissão ou alteração de CI. A missão fiscal posterior encontra-se abaixo.

## Fundação fiscal RIVA + motor monetário

**CONCLUÍDA E INTEGRADA. PR #16 merged em `feature/visual-redesign`; gate humano de merge concluído por António. Não existe trabalho funcional pendente desta missão.**

- Branch local `codex/fiscal-riva-foundation` e worktree `.worktrees/fiscal-riva-foundation` removidas no fecho após confirmação da integração e working tree limpa. Branch remota preservada. Base remota observada na abertura: `95dc3de6895abe55b9ec0b1a574250a232d54ee6`, `feature/visual-redesign`; consultar Git para HEAD corrente.
- V16 acrescenta enquadramento RIVA, fundamento próprio do artigo, resultados fiscais e snapshot V3, postal opcional estrangeiro e taxas territoriais. V17 referencia 33 códigos Mxx oficiais AT V4.0 / 18-06-2026, preservando códigos/designações existentes.
- IVA calculado/liquidado separados; monetário HALF_UP por linha a cêntimos; RIVA recalcula todas as linhas; legado rascunho avisa diferenças; históricos V1/V2 não são recalculados.
- Combinações habilitadas: tributação normal, isenção inerente M07, não liquidação M10 nacional ou M16 intracomunitária para bens, com fundamento explícito. O enquadramento configurado não verifica elegibilidade documental/VIES. Outras projeções normativas exigem evolução própria; catálogo completo não implica habilitação geral.
- Emissão não EUR e retenção documental não zero são recusadas enquanto não existir conversão/distribuição fiscal demonstrada. Estruturas monetárias e dados legacy preservados. Taxas territoriais são referência atual, não motor de legislação histórica.
- Validação em PostgreSQL 16.3 descartável: regressão final integral `mvn -o test`: 283/283, zero falhas/erros/skips; upgrade V15→V17 1/1 preserva catálogos personalizados e emitido V2, valida e reinicializa sem migrations. As correções de revisão estão incluídas nesta regressão; 30 testes dirigidos também passaram. Package `mvn -o -DskipTests package`: BUILD SUCCESS.
- Frontend: TypeScript/build aprovados; 11 Playwright aprovados, incluindo apresentação fiscal com API simulada. Não confundir mocks com prova de enquadramento legal; testes backend verificam emissão/PDF/QR reais.
- Revisão independente final por `fiscal_final_review` e `fiscal_ui_review`: APROVADA, BLOCKER 0 / MAJOR 0. Quatro MAJOR anteriores corrigidos e testados: precisão preview/gravação, M16 em serviços, QR não EUR e retenção. Mock frontend alinhado com M16; aviso descoberto ao guardar exige nova confirmação. Revisões estáticas; testes executados pelos executores.
- Entrega integrada: commits `2e1c51d31e56d8b24cc6f0432a0d7cbc10ab4d16` e `c707a04eb0c56d2474472c08e8b3179d4d0ec126` confirmados como ancestrais da base produtiva. [PR #16](https://github.com/amgsrosario/fac/pull/16) merged por António em 08-10-2026, com destino `feature/visual-redesign`. CI do HEAD final da missão confirmado verde na [execução 37656846468](https://github.com/amgsrosario/fac/actions/runs/37656846468) (safety tests e package). HUMAN MERGE CONCLUÍDO; missão encerrada operacionalmente. O HEAD Git corrente deve ser consultado no Git, sem actualizações documentais autorreferenciais.
- Próximo bloco fiscal: NC/ND e RC/NL, sujeitos a nova OP; implementação ainda NÃO iniciada neste fecho; SAF-T, assinatura/certificação e fiscalidade não portuguesa fora desta fundação.
- Alterações preexistentes do checkout principal preservadas; nenhuma base persistente/demo usada em testes; CI/dependências não alterados.

## Sequência estratégica vigente

1. JWT / Gestão e Invalidação de Sessões — **CONCLUÍDA**.
2. Fundação de Permissões — **CONCLUÍDA E INTEGRADA; GATE HUMANO CONCLUÍDO**.
3. Security Gate — **INTEGRADO; HUMAN MERGE CONCLUÍDO; SEGURANÇA FORMALMENTE ENCERRADA**.
4. Fecho Fiscal Sistemático — **FUNDAÇÃO FISCAL RIVA + MOTOR MONETÁRIO INTEGRADA; PRÓXIMO BLOCO NC/ND E RC/NL AINDA NÃO INICIADO**.
5. Auditoria Funcional Final.
6. Preparação e Certificação AT.
7. Piloto Controlado.

Esta sequência é a orientação vigente do Executivo. A conclusão JWT não antecipa os gates seguintes nem declara certificação ou prontidão global de segurança.

## Resíduos e limites

- Não foi criado um fluxo de alteração de password pelo próprio utilizador; o caminho implementado nesta missão é o reset administrativo existente.
- Logout permanece client-side; não existe revogação individual de JWT introduzida nesta missão.
- Fundação de Permissões integrada, sem trabalho funcional pendente. Segurança formalmente encerrada; Security Gate integrado; manutenção npm futura não bloqueante.
- A publicação documental segue o fluxo de branch/PR contra `feature/visual-redesign`; o merge é uma decisão humana de António.
