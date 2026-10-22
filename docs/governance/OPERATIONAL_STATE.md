# TUULI AIR — Estado Operacional

Fonte canónica do estado corrente. Atualização: 2026-10-22, Europe/Lisbon. Substituir informação obsoleta quando o estado mudar; o histórico detalhado permanece no Git. Consultar também [INSTITUTIONAL_MEMORY.md](INSTITUTIONAL_MEMORY.md).

## Base produtiva confirmada

- Linha produtiva: `feature/visual-redesign`.
- HEAD produtivo observado neste registo: `3cd75225d615ea27d90ad603e430d258f777cbe2`, merge do PR #33, confirmado na referência local `origin/feature/visual-redesign`. Sem fetch ou consulta nova ao GitHub neste registo; não é referência auto-actualizada.
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

## OP19 — Contrato financeiro e preparação da implementação

Documentação local preparada e revista pelo Executor. OP19-R identificou
MAJOR1/MINOR3; correcções documentais OP19-C aplicadas, nova verificação
independente limitada concluída por op19_independent_review: APROVAR,
BLOCKER0/MAJOR0/MINOR0. PR18 confirmado integrado em feature/visual-redesign
(merge194ffd458b7405822d4934bac5cfef5feb87486b); gate humano concluído; aprovação
exclusivamente documental, sem validação jurídica ou implementação. Produto aprovado pela OP19; arquitetura proposta.
[Entregáveis e limites](../fiscal/op19/README.md). PR16 e PR17 confirmados merged
na linha produtiva por GitHub após fetch; base observada na abertura desta OP:
c2f7e20703b6627beba670954da9c9e187ba556b (referência histórica, não HEAD perpétuo).
Não foi localizado relatório OP18 em docs versionados. Contrato,36 cenários de
aceitação futuros, dossier AT e sequência de OPs integrados documentalmente
pelo PR18. A OP19 não implementou NC/ND/RC/NL nem executou testes funcionais
ou migrations. A evidência de implementação da fundação OP20 consta abaixo.
Gates legais NL/RCzero/descontos/adiantamentos permanecem abertos. Segurança
continua formalmente encerrada. OP20 autorizada para fundação interna isolada; integração documental exige
intervenção posterior.

## OP20 — Fundação financeira

OP20 CONCLUÍDA E INTEGRADA: PR19 merged em feature/visual-redesign,
merge91ef3c42f668875d0aa8468f3886e91fb7f358d6. Base produtiva observada na
abertura194ffd458b7405822d4934bac5cfef5feb87486b. Serviço interno/V18 aditiva;
nenhum circuito documental ativado nem endpoint novo. Validação concluída:22 testes núcleo/migration/safety,84 regressão dirigida,
1 compatibilidade histórica e package aprovados. Revisão independente
op20_core_review: APROVAR, BLOCKER0/MAJOR0/MINOR0. Entrega local validada,
commit e8da26b438aa73117b24e8afeb700318933d099e integrado. CI do PR19
sem execuções/checks encontrados; não declarado verde. Núcleo integrado no
repositório, ainda sem uso como fonte de saldo operacional. [Estado de evidência](../fiscal/op20/VALIDATION.md).

## OP21 — Originação FT

**CONCLUÍDA E INTEGRADA. PR #20 merged em feature/visual-redesign; gate humano de merge concluído.**

- Commit7a3741f04f3241e5ce21fb7936bed74e93f79b8d integrado; merge2cbb9341dc8f6ffa70605a986eb9daeda93be23e confirmado por Git/GitHub.
- FinancialCore implementado (OP20); adaptador de originação/anulação FT e V19 integrados pela OP21. RC ainda processado pelo legado; Pendente continua fonte operacional. Nenhum saldo do núcleo promovido a conta corrente.
- CI aprovado na [execução38237821996](https://github.com/amgsrosario/fac/actions/runs/38237821996): TestDatabaseSafetyValidatorTests e mvn -B -DskipTests package. Não executou integração PostgreSQL, concorrência financeira ou regressão integral.
- Evidência local separada da OP21:97/97 testes dirigidos,22/22 núcleo/migration/safety, package BUILD SUCCESS; revisão independente op21_review APROVAR, BLOCKER0/MAJOR0/MINOR0. Resultados anteriores, não repetidos nesta OP documental.
- Branch/worktree OP21 permanecem existentes, preservadas. Nenhuma limpeza nesta missão.
- [Modelo](../fiscal/op21/FT_ORIGINATION.md) e [validação](../fiscal/op21/VALIDATION.md) registam a entrega local original; para o estado pós-merge corrente prevalece esta fonte canónica.

## OP22 — Preparação e decisão arquitectónica

**OP22-A INTEGRADA PELO PR #22; OP22-B1 INTEGRADA PELO PR #23; OP22-B2.1 INTEGRADA PELO PR #24, SEM ACTIVAÇÃO OPERACIONAL.**

Alternativa B aprovada pelo Product Owner como orientação futura: FinancialCore
será fonte única e Pendente perderá saldo independente. Ainda não implementado.
Primeira fronteira a preparar: FT/RC com escrita/leitura/anulação/consistência.
[ADR-001](decisions/ADR-001-financial-source.md) regista decisão e fundamentos;
[GOV-ALT-001](INSTITUTIONAL_MEMORY.md#gov-alt-001--transparência-e-avaliação-de-alternativas)
formaliza a avaliação obrigatória das alternativas.

OP22-GOV integrada pelo PR #21, merge12a3aed7f35085ab0f852c48530ee21a467c490c,
confirmado no reconhecimento Git desta missão. Base observada OP22-A; consultar
Git para HEAD corrente, sem actualizações autorreferenciais. TaxIA não alterado.

TUULI-FIN-TIME-001 emendada pelo Product Owner em 14-10-2026: contrato-alvo
RC com liquidação na Data Valor e reversão na Data Valor original (M1 + F2 + T1).
FT/NC conservam emissão; timestamp técnico independente, distinto de SystemEntryDate
SAF-T. [ADR-002](decisions/ADR-002-financial-document-time.md). Arquitectura aprovada,
à data sem implementação ou activação; efeito RC então pela emissão.
Evolução posterior F2/T1: secção Fatia 1b integrada, sem activação RC.

OP22-A implementa contratos internos de posições, saldos correntes separados,
identificação V19 e histórico auditável; FT vinculada expõe efeito derivado da
emissão. OP22-A integrada no merge70e82c122ec477ceb804e6c837f5bb529120acd7. No âmbito da OP22-A, SETTLE sem vínculo RC;
REVERSE sem efeito documental definido. Saldo/movimentos por data financeira
SUSPENSOS; timestamp técnico nunca utilizado como substituto. Nenhuma alteração
estrutural de persistência nesta missão. Política RC6/núcleo2 permanece reservada.
Pendente permanece fonte operacional; RC legado não escreve no núcleo.
[Contratos](../fiscal/op22-a/QUERY_CONTRACTS.md) e [validação](../fiscal/op22-a/VALIDATION.md).
Validação local:99 testes distintos aprovados (mais7 reexecuções dirigidas),
PostgreSQL16.3 descartável V19, package BUILD SUCCESS, diff/links aprovados.
Revisão independente op22_a_review: APROVAR, BLOCKER0/MAJOR0/MINOR0.
Entrega OP22-A publicada e PR #22 merged, confirmado por Git/GitHub. CI do HEAD
b83f418 aprovado na execução38269640856 (safety e package; não integração SQL).
À data OP22-A, consultas históricas suspensas conforme autorização expressa;
Fatia 1b posterior prepara apenas o âmbito interno documental suportado.

### OP22-B1 — infraestrutura RC inactiva

Base observada 70e82c122ec477ceb804e6c837f5bb529120acd7. Branch/worktree isolada
codex/financial-receipts-op22-b1. Emissor interno: pedido persistente antes do
número, EUR/cêntimos exactos sem descontos, parcial/múltiplo, vínculo RC/SETTLE e
linha/afectação V20. FinancialQueries consulta a emissão RC como efeito financeiro
apenas quando existe vínculo; timestamp técnico não usado como efeito.
Projecção isolada PendenteDto/totais usa saldo do núcleo; não activada nos serviços.
Diagnóstico identifica dados não reconciliados e nunca permite activação em B1.

Na entrega B1, RC legado/Pendente e seus leitores permaneceram activos;
a protecção adicional B2.1 está agora integrada, sem activar o emissor novo.
Transferência de clientes/dashboard/diagnósticos/listagens/extratos e restante
fronteira adiada à B2. À data B1, nova anulação e histórico por data suspensos. V20 bloqueava
anulação/reverse indevidos de RC internamente vinculado; RC sem vínculo mantido.
Nenhuma demo/dado histórico apagado, reconstruído ou migrado. Nenhum endpoint novo.
[Contrato/corte](../fiscal/op22-b1/RECEIPT_INTEGRATION.md) e [evidência](../fiscal/op22-b1/VALIDATION.md).
Validação local actual 125 cenários distintos aprovados em PostgreSQL 16.3
próprio/V21;23 RC,22 núcleo/queries,4 safety,71 regressão documental,5 schema/migrations.
Revisão subsequente encontrou MAJOR-001: inserção de componentes após consumir
constraints RC na mesma transacção. Reprodução V20 demonstrou commit indevido.
Correcção aditiva V21 sela componentes/afectações após vínculo RC, sem alterar
V18/V20 nem activar circuito. Upgrades V19/V20→V21 e segundo arranque aprovados.
Nova revisão independente Codex major001_review APROVAR, BLOCKER0/MAJOR0/MINOR0.
Package final BUILD SUCCESS. Entrega complementar local, sem push/PR/CI nesta
etapa de correcção MAJOR. Revisão Claude do diff completo até2e3d3ba comunicada
pelo PO em13-10-2026: APROVAR, BLOCKER 0 / MAJOR 0 / MINOR 2, sem executar testes.
Observações documentais tratadas; PR #23 integrado, gate humano concluído.
Merge observado 549e902bca8e53a9e23b47afbb2ab405fe7bcc8d; circuito novo inactivo.
CI B1 aprovado: safety e package, distinto da integração PostgreSQL local.
Risco identificado na B1: o RC legado podia liquidar FT parcialmente liquidada
pelo emissor novo porque usava saldo Pendente. A exclusão bidireccional B2.1
resolve essa mistura na linha integrada; não autoriza a activação do circuito.
AR2-Governance consultado em ace6e54a613ce25c959f31accae3ec7b129b307e, estritamente
leitura; GOV-TUULI-002 preservada, sem alteração ou cancelamento nesta missão.

### OP22-B2.1 — integrada; gate humano concluído

PR #24 integrado em feature/visual-redesign, confirmado por Git e GitHub em
13-10-2026. Merge/HEAD produtivo observado neste fecho:
`e8d86eae2672756b84d6178644eeaa63c691601b`. Commits `f716ad6` e `9e4272e`
preservados por merge normal. O Git continua a ser a fonte do HEAD corrente;
esta referência não exige novos commits para acompanhar hashes transitórios.

Protecção transaccional nos dois emissores e V22 persistente. Origem FT isolada
não constitui consumo. Write skew reproduzido e corrigido por serialização/versão
MVCC da posição; reverse genérico e anulação FT antecipam locks de Pendente,
sem alterar efeitos ou desbloquear anulação/REVERSE de RC vinculado.
150 cenários PostgreSQL 16.3 descartável aprovados pelo Codex, upgrades/segundo
arranque e package BUILD SUCCESS. [Evidência](../fiscal/op22-b2-1/VALIDATION.md).
Segunda revisão Claude: APROVAR, BLOCKER0/MAJOR0/MINOR0; auditoria Flyway
pré-publicação: PUBLICAR, sem incompatibilidades comprovadas, comunicadas pelo
Executivo na autorização. PR #24/CI aprovado na execução 38428872934:
TestDatabaseSafetyValidatorTests e package; não executou a suite PostgreSQL.
Gate humano de merge concluído. Sem correcções funcionais B2.1 pendentes conhecidas.

**Novo circuito RC permanece INACTIVO. Demo Ubuntu NÃO VERIFICADA: verificar
estado real e histórico/checksums Flyway antes de qualquer actualização.**
A auditoria pré-publicação não confirma deployment nem compatibilidade da demo.
Nenhuma migration, actualização da demo, reconciliação ou activação neste fecho.
Pendente/consumidores continuam operacionais; V22 não repara dados anteriores.
Worktrees/branches preservadas; principal desactualizado e alterações preexistentes
mantidos. Não foi executada limpeza nesta missão.

### OP22-B2.2 — arquitectura aprovada; implementação não iniciada

DV-01 a DV-09, M1 + F2 + T1, anulação autónoma e substituição vinculada aprovadas
pelo Product Owner em 14-10-2026. Data Valor própria/imutável no RC (inclusive legado),
default emissão, checkbox só apresentação e PDF com identificação própria.
FIN-HIST-01–03 aprovados: historial preservado, liquidação na Data Valor e
consumidores-alvo por efeito. Limite: máxima data de nascimento das obrigações de débito
elegíveis ≤ Data Valor ≤ emissão RC. FT usa emissão; legado admite pendentes
não-FT mas isso não prova elegibilidade. Núcleo origina FT EUR; outros tipos
aguardam confirmação. DV-10 retirada, sem restrição nova ao legado elegível.
Correcção de montante exige motivo/auditoria; substituição 30→300 reflecte 300,
não 330; sem excesso de dívida ou dinheiro sem afectação válida. Históricos sem
Data Valor conhecida mantêm ausência, não recebem datas de pagamento inventadas.
[Contrato completo e alternativas](decisions/ADR-002-financial-document-time.md).

Formalização documental INTEGRADA pelo PR #26, merge fc57177 confirmado em
15-10-2026; 486b999, 31eee0e e cb872f3 ancestrais. Gate humano de merge concluído.
À data desta formalização: preparação, sem autorização funcional ou fiscal.
Emissão e extractos legados inalterados; FinancialCore ainda não implementava
F2/T1, anulação RC vinculada bloqueada e balanceAt suspenso.
Evolução posterior: Fatia 1b integrada abaixo, ainda sem activação RC.
Os dois MINOR do parecer Codex independente APPROVE WITH CONDITIONS estão
incorporados: gate fiscal explícito e CASH_OUT reversed=true/RESTORE vinculados.
Claude reviu 486b999: APPROVE WITH CONDITIONS, BLOCKER 0 / MAJOR 0 / MINOR 5.
Complemento dos cinco pontos e fecho N1/N2 integrados pelo PR #26. Não se
inventa resultado adicional da revisão; parecer final não consultado neste fecho.
Situação monetária real antes de anulação: identificação/validação por implementar,
sem pressupor informação disponível, reembolso automático ou saldo não afectado.
[Inventário, cenários e matriz dos MINOR](../fiscal/op22-b2-2/FINANCIAL_HISTORY.md).
Demo Ubuntu por verificar; consumidores permanecem desligados até corte conjunto.
Implementação, migrations, verificação fiscal e corte dependem de missões próprias.
AR2-Governance não alterado; eventual sincronização central exige autorização específica.

### Próxima missão financeira — pendências e decisões reservadas

- Preparação interna de anulação/substituição RC integrada na Fatia 1b, abaixo,
  segundo M1 + F2 + T1. Integração pública, autorização e tratamento factual
  monetário continuam pendentes. Preservar afectações independentes.
- Consultas históricas operacionais continuam suspensas. Fatia 1b integra consulta
  interna limitada; restantes estornos conservam decisões reservadas.
- Preparar consumidores correntes (dívidas, conta corrente, clientes com saldo,
  dashboard, listagens/exportações e diagnósticos) para o núcleo, mantendo contratos,
  sem os ligar à operação antes do corte autorizado.
- Definir transporte e estabilidade da chave idempotente no circuito público.
- Verificar demo, inventariar origens/recebimentos incompatíveis e aprovar plano
  de corte. Nenhuma migração, eliminação ou reconstrução de dados está autorizada.
- Activação depende da consistência da fronteira FT/RC completa: emissão, liquidação,
  anulação, leitura financeira e dados coerentes. GOV-ALT-001 aplica-se às escolhas materiais; preparar alternativas
  para o PO antes de implementar. Nenhuma próxima missão iniciada neste fecho.

O circuito RC legado não escreve no FinancialCore. Enquanto permanecer activo,
uma posição FT pode conservar o valor original após um recebimento legado.
Transferir operacionalmente os consumidores para o núcleo antes do corte pode
produzir saldos incorrectos. A preparação antecipada é possível, mas os novos
consumidores devem permanecer desligados da operação até ao corte autorizado.
Não se presume reconciliação dos saldos existentes. A activação exige consistência
entre emissão, liquidação, anulação e leitura financeira; a semântica de anulação
ou estorno de RC foi aprovada como contrato-alvo T1 na OP22-B2.2; implementação
inactiva posterior integrada pelo PR #28, registada na Fatia 1b.

#### Sequência técnica recomendada pelo Claude

**Proposta técnica sujeita às decisões do Product Owner.**

1. Etapa de decisão RC cumprida documentalmente em 14-10-2026 pela OP22-B2.2
   (M1 + F2 + T1); decisões restantes e validação fiscal continuam pendentes.
2. Implementar a anulação de RC vinculado, mantendo o novo circuito
   operacionalmente inactivo.
3. Verificar a demo Ubuntu, inventariar eventuais incompatibilidades e preparar
   o plano de corte para aprovação.
4. Preparar os consumidores financeiros atrás da mesma fronteira de activação,
   sem ligação operacional, definindo também a chave idempotente pública em
   conjunto com o endpoint.
5. Executar a activação conjunta de emissão, leitura, anulação e consumidores
   financeiros, exclusivamente no corte autorizado.

Os passos 3 e 4 podem decorrer em paralelo após o passo 2. Os consumidores não
podem ser ligados antes do passo 5.

Esta sequência é uma recomendação técnica, não uma autorização para iniciar as
etapas, implementar contratos ou activar funcionalidades.

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

## OP22-B2.2 — fecho MINOR-N1/N2, exclusivamente documental

Decisão PO: Data Valor ≥ nascimento de cada obrigação liquidada e ≤ emissão RC;
para várias, usar máximo nascimento. Regra válida para qualquer tipo elegível.
Estado histórico N1/N2, antes de E1/E2: equivalência universal de Pendente.dataDocumento
(emissão copiada) com nascimento NÃO demonstrada; confirmar semântica/fonte por
tipo é pendência técnica bloqueante dessa validação, não impedimento documental.
N2 acrescenta consumidores comerciais, frontend e DemoScenarioService ao inventário.
Duas grafias «eligíveis» corrigidas para «elegíveis». Nova revisão Claude limitada
ao diff complementar era pendente na entrega; PR #26 posteriormente integrado.
Nenhuma activação, alteração demo ou corte autorizado.

## Preparação Data Valor pós-PR #26 — 15-10-2026

[Reconhecimento e plano da Fatia 1](../fiscal/op22-b2-2/SLICE1_PLAN.md).
Na preparação anterior, nascimento universal não estava demonstrado. E1/E2
resolveram a semântica em 15-10-2026: emissão das origens é o limite inferior.
Plano histórico da Fatia 1:
persistência/default/interface/PDF/imutabilidade, sem F2/T1 operacional.
A missão posterior Fatia 1a autoriza implementação local, não corte operacional.
Fecho documental local preparado sem push/PR; nenhuma alteração de dados/demo.

## Fatia 1a — integrada pelo PR #27

PO autorizou em 15-10-2026; base documental e861c223 sobre PR #26.
E1/E2 encerram a pendência anterior: nascimento financeiro FT e obrigações de
débito elegíveis na emissão; limite máximo das emissões dos originadores.
Vencimento não intervém. V23 aditiva, dados históricos NULL preservados.
Integrada pelo PR #27, HEAD observado 7dccfb2ecc61c9b80a7059cf73ce25eab02061d0;
CI verde e Claude APPROVE comunicados pelo PO. Emissor interno adaptado para
campo/idempotência, continua inactivo; saldos/extractos e efeito do núcleo por
emissão mantidos. Novo guard rejeita RC cronologicamente inválido/credor.
Claude do plano: APPROVE WITH CONDITIONS (0 BLOCKER, 1 MAJOR, 3 MINOR),
comunicado PO; distinto da revisão final da implementação, aprovada pelo Claude
com 0 BLOCKER/MAJOR/MINOR conforme confirmação do PO.
[Testes e limites](../fiscal/op22-b2-2/SLICE1A_VALIDATION.md).

## Fatia 1b — integrada pelo PR #28, sem activação RC

PR #28 merged contra feature/visual-redesign, merge
2a0f8b08f9d79715dd4cd93a5888b9e6e79773eb. Commits 8754ed3/d2c5a7d
integrados; V24 presente, PR #27 ancestral confirmado.
Núcleo interno consulta SETTLE na Data Valor e REVERSE RC na Data Valor original.
V24 aditiva: acto auditável imutável, estado documental e ledger atómicos,
substituição vinculada/idempotente, RESTORE e contra-lançamentos sem apagar história.
Anulação autónoma interna só por declaração expressa de pagamento não ocorrido;
validação factual monetária e permissões públicas continuam por preparar.
Históricos NULL não alterados; reversão sem Data Valor registada recusada.
balanceAt interno suporta história rectificada apenas em âmbitos integralmente
datados, recusando outras semânticas. Não é fonte dos saldos operacionais.

Novo circuito RC continua inactivo. Legado, consumidores, saldos/extractos,
frontend e demo intocados; activação conjunta e gate fiscal exigem autorização.
[Contratos, testes e riscos da entrega](../fiscal/op22-b2-2/SLICE1B_VALIDATION.md).
Revisão independente final Claude: APPROVE, zero findings pendentes, conforme
comunicação PO nesta ordem. Resultado não reconstruído a partir dos testes.
CI confirmado directamente pela API GitHub no HEAD d2c5a7d, job validation success:
[Testes de protecção da base e package](https://github.com/amgsrosario/fac/actions/runs/38769991780/job/116542973704).
CI não executa integração PostgreSQL. Evidência local anterior: fase inicial
190 testes aprovados; complemento final 94 aprovados, zero falhas/erros/ignorados
em cada fase, com package e upgrades até V24/segundo arranque. Fases sobrepostas,
não somar como testes distintos. Não repetidas neste fecho documental.

Legado é a única autoridade operacional de liquidações/saldos/extractos. Originação
FT existente não equivale a activação do novo circuito RC. Sem cutover, bases/demo
inalteradas; demo Ubuntu ainda por verificar antes de qualquer actualização.

## Fatia 1c — diagnóstico interno integrado pelo PR #30

[Proposta técnica com alternativas e critérios](../fiscal/op22-b2-2/SLICE1C_PROPOSAL.md).
Recomendação: diagnóstico interno paginado de reconciliação FT/RC, só leitura.
Alternativas: temporalidade da anulação FT primeiro, ou preparação de consumidor
inactivo. PO aprovou posteriormente 1c-B na OP de implementação; 1c-A/1c-C
não foram autorizadas. Base documental PR #29 integrada em f06c09d.
Continuam reservados: efeito temporal FT, tratamento factual de dinheiro real,
RC NULL/perímetro de dados, consumidores, chave pública/permissões, validação
fiscal e plano explícito de activação/reversão de corte. Igualdade de saldos ou
contadores a zero não autoriza activação. A integração posterior está confirmada abaixo.


FinancialReconciliation integrado pelo PR #30 em feature/visual-redesign, merge
cf0561562d74ddd3e84786c66ab2a78409811229, incluindo 279b702 e
e9bc4a943008a1652dd822656070abde61bd7096; ancestralidade confirmada no Git local.
Leitura paginada por cliente/documento, resumo apenas de observações comparáveis e evidência paginada.
Sem migration ou chamadores operacionais. Ausência de saldo não vira zero;
comparabilidade corrente não certifica história nem autoriza activação.
[Contrato, testes e limites](../fiscal/op22-b2-2/SLICE1C_VALIDATION.md).
A solicitação de revisão e o estado local descritos na evidência histórica
referem-se à entrega anterior à integração. Os pareceres Claude arquivados de
15-10-2026 foram agora lidos: revisão de 279b702, APPROVE WITH CONDITIONS
(0 BLOCKER / 0 MAJOR / 2 MINOR); verificação final de e9bc4a9, APPROVE
(0 BLOCKER / 0 MAJOR / 0 MINOR), com ambos os findings resolvidos. Revisões
estáticas, sem execução própria de testes pelo revisor; não é nova revisão Codex
nem consulta de CI. Referências externas textuais, relativas ao arquivo
`Relatorios-AR2/TUULI/` (não distribuído no repositório):

| Parecer arquivado | SHA-256 |
| --- | --- |
| `2026-10-15_OP22-B2.2_revisao-Fatia1c.txt` | `36194f0fb71f9cd90714b5aee3aa5c9f344b6c90bddc8c7421a8ba4fa361e844` |
| `2026-10-15_OP22-B2.2_verificacao-final-Fatia1c.txt` | `ca237f05fa878a15042653202ce70784828c42e074d8cf48e6391896372dee76` |

Legado permanece única autoridade operacional; consumidores, FinancialCore RC e
demo não activados.

Validação da fase original 1c: 135 testes dirigidos aprovados, zero falhas/erros/ignorados,
com PostgreSQL 16.3 descartável e package BUILD SUCCESS. Inclui 15 testes novos do
diagnóstico, regressão financeira/safety e upgrades até V24/segundo arranque.
Complemento e9bc4a9: 142 testes aprovados, incluindo 22 do diagnóstico. Fases
sobrepostas, não somar como testes distintos. Não equivale à suite integral
da aplicação ou a novo CI. O estado de revisão pendente dos relatos originais
não descreve o estado de publicação/inclusão Git actual.


## Fatia 1d — diagnóstico concluído e aceite pelo PO em 21-10-2026

**ACEITE como diagnóstico de prontidão; resultado: NÃO PRONTO PARA TRANSIÇÃO.**
A aceitação formal foi comunicada pelo Executivo II, por decisão expressa da
Administração AR2/PO, na ordem «OP22 — Fatia 1d | Registo institucional da aceitação»
de 21-10-2026. [Transcrição literal da decisão efectivamente disponibilizada](records/2026-10-21-slice1d-po-acceptance.md),
SHA-256 `5ab32f4de2708019f62b4836438b90fc4685b95020a61c4980b39d31b1433708`; este registo
preserva a proveniência da mensagem e distingue aceitação da autorização inicial.
Este registo documental foi integrado pelo PR #31, merge 1a53ddc,
incluindo d883aba e ab2e4ef; não permanece pendente de publicação.
A aceitação do diagnóstico não constitui autorização de desenvolvimento ou corte.

Base da evidência: cf05615, PR #30. Validação anterior em PostgreSQL 16.3 descartável:
**142 testes financeiros + 2 comerciais = 144**, zero falhas/erros/ignorados,
**11 cenários de diagnóstico** e package BUILD SUCCESS. O TSV original conta
apenas os 142 financeiros; os 2 comerciais constam do respectivo XML/log.
Fixtures sintéticos distinguem-se dos emissores reais, conforme o relatório.
Não é suite integral da aplicação nem novo CI; nenhum teste foi repetido neste registo.

### Fontes preservadas e reservas documentais

Arquivo externo reservado: `Relatorios-AR2/TUULI/`. Os caminhos abaixo são
referências textuais relativas a esse arquivo, não ligações do repositório nem
caminhos pessoais de uma máquina. Os ficheiros não são distribuídos com o checkout;
a localização física do arquivo é gerida pelo Product Owner.

| Referência no arquivo | SHA-256 |
| --- | --- |
| `Arquivo-Reservado/OP22-Fatia1d/REPORT.md` | `1ba82ee5668f482956cf22be7d8038de4a469a418493b3f5652b6d90b71fa590` |
| `Arquivo-Reservado/OP22-Fatia1d/MANIFEST_SHA256.tsv` | `0881ff79bea7f9f08c280fb250d4fc945067e26a0a45cb8df8c3908258601d97` |
| `2026-10-21_OP22_Fatia1d_revisao-independente_Claude.md` | `acdb549cb69ec6d6671be6c3e235edd8e4ac0cda0cad2a21c916ebf426bbfbb1` |
| `2026-10-21_OP22_Fatia1d_preparacao-fecho_Codex.md` | `9f340f378dfaf22d82199e458a55ca862f4c2a25c57400ef2347ac5431c7ae83` |

O manifesto identifica 16 artefactos, 708.855 bytes, nomes/tamanhos/SHA-256 e
identidade origem/cópia; é adicional aos 16 ficheiros, originais preservados.
O parecer Claude de 21-10-2026 é **GO COM RESERVAS para fecho institucional**,
sem executar testes nem autorizar activação. A preparação do fecho é uma fotografia
anterior à aceitação; a indicação «aceitação pendente» nesse documento é superada
pela decisão PO transcrita, sem alterar o artefacto de preparação.

RES-1: evidência preservada fora de target, com SHA-256. RES-3: total 142 + 2
explicitado, sem alterar o TSV original. RES-2: âmbito e resultado registados aqui,
mas permanece **pendência documental da referência autónoma à autorização original
1d**. Não foi identificada fonte arquivada verificável; não reconstruir a ordem
por inferência nem confundi-la com a actual ordem de aceitação. Não declarar
eliminadas todas as reservas enquanto essa referência não for regularizada.
Os hashes demonstram identidade na preservação/verificação, não prova retrospectiva
integral de imutabilidade desde a execução de 15-10-2026.

### Bloqueios de transição que permanecem abertos

| Gravidade | Gate remanescente |
| --- | --- |
| BLOCKER | Autoridade e leitores: preparar corte coordenado de escrita, leitura e anulação |
| BLOCKER | Perímetro de dados/históricos e verificação da demo Ubuntu/Flyway antes de actualizar |
| BLOCKER | Validação fiscal/documental e contrato público, incluindo permissões/chave idempotente |
| MAJOR | Cobertura histórica: temporalidade da FT anulada e limitações RC com Data Valor NULL |
| MAJOR | Cobertura funcional: dinheiro recebido na anulação, descontos, não-EUR e outros tipos |
| MAJOR | Reversibilidade operacional após novas escritas do núcleo; retorno ao legado não ensaiado |

São gates de transição, não testes falhados nem impedimentos à aceitação do diagnóstico.
O novo circuito RC permanece inactivo. O legado permanece a única autoridade operacional de liquidações/saldos/extractos;
originação FT integrada não equivale a activação do novo circuito RC. Sem cutover,
actualização de demo, migração ou alteração de bases. **Fatia 1e SUSPENSA**, sem
início autorizado. Recomendações anteriores para a próxima implementação não são
decisões de produto nem autorização de execução; novas etapas exigem ordem própria.


## OP22 / OP22-B — classificação protegida, PR #32 e PR #33 integrados

- Base integrada confirmada: PR #32, 6abf4ceda62528ed6fef91dc35383599b248ea79,
  contendo a V25; referência observada na retoma de 22-10-2026 após fetch manual do PO.
- V25 estabiliza os cinco atributos protegidos do mestre. DTO e extractos mantêm
  as consultas existentes; a classificação não passa a depender de um novo snapshot.
- PR #33 integrado, merge 3cd75225d615ea27d90ad603e430d258f777cbe2,
  incluindo 41253ae7a26e05b9824143635d15cab0b721866d e V26; ancestralidade
  confirmada na referência local actualizada pelo PO, sem novo fetch neste fecho.
- Lacuna reproduzida: reatribuição do tipo de um financeiro não-RC por SQL altera
  Código Fiscal e sinal lidos. V26 integrada bloqueia apenas a troca de id_tipo_documento
  de cabeçalhos financeiros persistidos, incluindo anulados, por SQL/JPA/upsert.
- Sem backfill, restauro inferido de classificações ou alteração de contratos de
  leitura. Históricos sem classificação original demonstrável não foram resolvidos
  por uma nova política; qualquer mudança dessa semântica continua a exigir o PO.
- Validação local: 229 testes backend distintos aprovados, zero falhas/erros/ignorados,
  PostgreSQL 16.3 descartável e package BUILD SUCCESS. Inclui 14 novos testes de
  classificação, 17 de V25, seis de upgrades e regressão comercial/financeira.
  Não é suite integral nem CI; relatório externo 2026-10-22_OP22-B_classificacao-historica_Codex.md,
  no arquivo Relatorios-AR2/TUULI. Parecer Claude de 22-10-2026 lido neste fecho:
  APPROVE, BLOCKER 0 / MAJOR 0 / MINOR 0, revisão estática sem executar testes.
  O parecer confirma 27/27 artefactos preservados, 903.104 bytes; manifesto
  SHA-256 98bdb304b4b631ca41924ab810fce7e566b40ad36d0370f24ed0f8b0243abae2.
  Referência externa: 2026-10-22_OP22-B_classificacao-historica_revisao-Claude.md.
  Não foi consultado CI novo nem repetida a suite neste fecho documental.
- OP22 (imutabilidade de tipos, PR #32/V25) e OP22-B (vínculo financeiro, PR #33/V26)
  concluídas e integradas nos respectivos âmbitos. O parecer V25 foi APPROVE com
  três MINOR não impeditivos; não confundir com o parecer V26 sem findings.
  Evidência V25 preservada no arquivo OP22-Imutabilidade-Tipos; V26 em
  OP22-B-Classificacao-Historica. A V25 pode impedir o upsert demo-base.sql se
  houver configurações protegidas divergentes; verificar antes de actualizar a demo.
  Branches/worktrees das implementações preservadas. Legado continua autoridade operacional;
  RC FinancialCore inactivo, demo inalterada e Fatia 1e suspensa.


### Continuidade após OP22-B — plano existente, sem autorização de implementação

A sequência de referência OB §10 coloca a introdução do catálogo canónico após
a preservação histórica, agora protegida para novas emissões por V25/V26.
O princípio de autoridade do Código Fiscal já foi aprovado em OA §3.A e OB §4;
a arquitectura concreta (código/persistido/híbrido), a matriz completa de efeitos e a
política de alterações continuam reservadas ao PO por OB §11, com aprovação
conceptual parcial posterior D-CAT-2A descrita abaixo. O plano não é
autorização de execução. Não foi demonstrado um incremento seguinte totalmente
decidido e desbloqueado: o próximo objectivo funcional do plano é o catálogo,
condicionado àquelas escolhas; não iniciar nova auditoria para o reconhecer.

Aceitação existente OB §4/§5: autoridade única, especializações apenas permitidas,
preservação histórica antes/junto do catálogo e ausência de classificação
paralela pelos consumidores. A matriz completa de efeitos ainda não é critério
executável aprovado. Moeda base/valorização vêm depois (OB §10.5), com validação
fiscal de entrada obrigatória (§7) e decisões cambiais reservadas. Não antecipar
multimoeda nem inventar critérios de aceitação funcionais ainda não definidos.
Fonte original externa: Relatorios-AR2/TUULI/2026-10-19_OP22_OB_ordem-original_PO.txt;
matriz de recuperação: 2026-10-21_OP22_matriz-final-decisoes-PO_Codex.md, §§6–8.
Fatia 1e suspensa; seis gates de transição preservados. Este fecho não encerra
a OP22 financeira global nem autoriza catálogo, cutover, deployment ou RC novo.


### D-CAT-2A — decisão aprovada, registada; implementação não iniciada

22-10-2026: Administração AR2/PO aprovou o modelo conceptual de tipologias e
especializações. [Decisão canónica e limites](INSTITUTIONAL_MEMORY.md#d-cat-2a--tipologias-fiscais-e-especializações).
FT e FR partilham Código Fiscal FT, distinguindo-se por liquidação imediata
não/sim: constituição de dívida versus liquidação na emissão. RC regista
recebimento/liquidação de dívida existente; RL regista pagamento/devolução da
empresa ao cliente, incluindo liquidação de crédito de NC. RC/RL seguem as suas
respectivas tipologias sem especialização adicional de liquidação imediata;
não presumir equivalência fiscal ou inventar Código Fiscal para RL.

Próxima preparação D-CAT-2 deve incorporar esta aprovação, sem reapresentar
o modelo conceptual como indeciso. Permanecem pendentes: confirmação documental
e validação da correspondência fiscal RL, valores não demonstrados da matriz
completa (área, Entidade, sinal e efeitos), D-CAT-1 e D-CAT-3. As recomendações
do relatório anterior sobre arquitectura/versionamento não foram ratificadas.

Registo exclusivamente documental. Não altera configurações existentes ou
comportamentos operacionais, não migra/reclassifica históricos, não enfraquece
V25/V26 nem autoriza implementação do catálogo. Novo RC e RL não activados;
FinancialCore RC inactivo, legado autoridade operacional, Fatia 1e suspensa.
