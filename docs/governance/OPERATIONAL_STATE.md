# TUULI AIR — Estado Operacional

Fonte canónica do estado corrente. Atualização: 2026-11-04, Europe/Lisbon. Substituir informação obsoleta quando o estado mudar; o histórico detalhado permanece no Git. Consultar também [INSTITUTIONAL_MEMORY.md](INSTITUTIONAL_MEMORY.md).

## Base produtiva confirmada

- Linha produtiva: `feature/visual-redesign`.
- HEAD integrado confirmado no início da M05: `d17de43f714a18c7f7bb7b1fc719f71d77b4e14f`, merge do PR #52 (M04), confirmado por HTTPS/FETCH_HEAD. Checkout principal preservado; tracking origin local antigo não usado como base. Integração não significa actualização de base, deploy ou activação.
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
PostgreSQL 16.3 descartável V19, package BUILD SUCCESS, diff/links aprovados.
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

## Sequência estratégica — prioridade executiva actual

A [ordem de 02-11-2026](https://github.com/amgsrosario/fac/issues/46#issuecomment-6584848804)
estabelece: (1) base limpa #38, sem cutover; (2) circuitos comerciais/documentais;
(3) segurança/permissões/auditoria nas pendências reais, sem reabrir os capítulos
encerrados; (4) fecho fiscal independente da assinatura; (5) piloto não produtivo.
D-SIGN-02..05 ficam reservadas para a fase final, com contributos da Administração.
[Mapa/dependências e missões propostas](../planning/reprioritization-2026-11-02/PENDING_MAP.md).
Este inventário não autoriza executar todas as missões propostas ou abrir gates.

### Trabalhos adiados e mantidos — não cancelados

A repriorização não revoga as pendências seguintes, que continuam no roteiro:

- **NC/ND e RC/NL: ADIADOS/RESERVADOS, sujeitos a nova OP**, conforme a Fundação
  fiscal e OP19 abaixo. Mecanismos RC inactivos não encerram o bloco completo.
- **Gates legais NL/RCzero/descontos/adiantamentos: ABERTOS/RESERVADOS**, exigem
  validação específica antes da activação, conforme OP19. Nenhuma homologação
  ou equivalência NL/RL é introduzida por este registo.
- **Auditoria Funcional Final: ADIADA E MANTIDA**, depois dos incrementos e
  evidências necessários, em missão própria; não cumprida por este inventário
  ou pela CI e não confundida com o Security Gate já encerrado.
- **Preparação e Certificação AT: ADIADAS E MANTIDAS**, após as fases
  preparatórias M01–M05 e dependências de assinatura D-SIGN-02..05, além dos
  gates aplicáveis. O piloto não produtivo não antecipa certificação/uso comercial.

Evidência: sequência da base `051b1fc`, Fundação fiscal/OP19 e
[RES01–RES04 / fontes E16–E17](../planning/reprioritization-2026-11-02/PENDING_MAP.md).
A [revisão PR #48](https://github.com/amgsrosario/fac/pull/48#issuecomment-6600046074)
exige esta preservação. M01–M05 continuam propostas não iniciadas, sem alteração
à ordem autorizada, abertura de gates ou cancelamento de trabalho posterior.

### Sequência anterior preservada — itens posteriores mantidos e adiados

1. JWT / Gestão e Invalidação de Sessões — **CONCLUÍDA**.
2. Fundação de Permissões — **CONCLUÍDA E INTEGRADA; GATE HUMANO CONCLUÍDO**.
3. Security Gate — **INTEGRADO; HUMAN MERGE CONCLUÍDO; SEGURANÇA FORMALMENTE ENCERRADA**.
4. Fecho Fiscal Sistemático — **FUNDAÇÃO FISCAL RIVA + MOTOR MONETÁRIO INTEGRADA; PRÓXIMO BLOCO NC/ND E RC/NL AINDA NÃO INICIADO**.
5. Auditoria Funcional Final.
6. Preparação e Certificação AT.
7. Piloto Controlado.

A lista anterior mantém as pendências posteriores; a prioridade de execução corrente é a ordem de 02-11 acima. Não constitui autorização para iniciar NC/ND, Auditoria Funcional Final ou certificação agora. Estes trabalhos foram adiados, não cancelados. A conclusão JWT não antecipa gates nem declara prontidão comercial.

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
a matriz completa de efeitos e a política de alterações continuam reservadas
ao PO por OB §11; a arquitectura foi decidida posteriormente pela D-CAT-1. Há aprovação
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
completa (área, Entidade, sinal e efeitos). D-CAT-1 e os princípios D-CAT-3
foram posteriormente aprovados conforme os registos abaixo.

Registo exclusivamente documental. Não altera configurações existentes ou
comportamentos operacionais, não migra/reclassifica históricos, não enfraquece
V25/V26 nem autoriza implementação do catálogo. Novo RC e RL não activados;
FinancialCore RC inactivo, legado autoridade operacional, Fatia 1e suspensa.


### D-CAT-1 aprovada — preparação D-CAT-2, sem implementação

Base confirmada neste trabalho: 3bc2f70c473b0cd6bb5011ba46eb4b0dc382bc7f,
merge PR #34; contém o fecho OP22/OP22-B e D-CAT-2A. Sem novo fetch nesta retoma.
Arquitectura híbrida governada aprovada em 22-10-2026 pela Administração/PO;
[decisão e alternativas](decisions/ADR-003-fiscal-catalog.md).
[Matriz concreta de evidências D-CAT-2](../fiscal/catalog/D-CAT-2_MATRIX.md)
preparada para decisão; combinações implementadas não são aprovação fiscal.
O seed demo omite codigo_fiscal e configura RC com liquidacao_imediata=true;
a UI oferece FR como código fiscal separado. A D-CAT-2A não autoriza corrigir
retroactivamente estas configurações nem dispensar V25/V26. Não foi inspeccionada
qualquer base persistente nem alterada a demo.

Pendentes: valores finais da matriz e correspondência fiscal RL. D-CAT-3
(revisões imutáveis/vínculo estável/transição sem reclassificação automática)
foi aprovada posteriormente; não permanece uma recomendação por ratificar.
À data da preparação D-CAT-1 não existia código ou migration do catálogo;
a execução posterior da fundação inactiva encontra-se abaixo. Legado mantém autoridade
operacional; novo RC/FinancialCore e RL não activados, Fatia 1e suspensa.


### Continuidade D-CAT-2 / D-CAT-3

D-CAT-3 aprovada pela Administração na ordem de continuidade de 22-10-2026;
[registo canónico](INSTITUTIONAL_MEMORY.md#d-cat-3--revisões-imutáveis-e-transição-histórica-aprovadas).
Matriz D-CAT-2 preparada com quatro estados separados: decisão funcional, código
existente, correspondência fiscal demonstrada e reserva material. Não homologada.
[Especificação da primeira fatia](../fiscal/catalog/FIRST_SLICE.md) delimita
infraestrutura de revisões sem activação e sem regras materiais não homologadas.
O catálogo operacional e a validação completa por tipologia dependem da matriz.

Revisão cruzada Claude não realizada: comando claude ausente do PATH e nenhuma
ferramenta/revisor Claude exposto nesta sessão. Não substituída por revisão Codex.
Pedido de revisão preparado na especificação para execução externa. A ordem
invoca AR2-GOV-03; não foi fornecido o seu texto integral nesta missão e este
registo não inventa cláusulas nem formaliza AR2-GOV-02.
Legado, V25/V26, históricos e fronteira de activação inalterados; Fatia 1e suspensa.


### Primeira fatia do catálogo — implementada localmente, sem activação

Branch codex/fiscal-catalog-dcat1, sequência d2ebd0b → 79766bc preservada.
V27 e leitor FiscalCatalog acrescentados; revisões e vínculos protegidos em SQL.
Manifesto vazio, nenhum backfill, nenhum chamador operacional ou endpoint novo.
Na base descartável de regressão, após testes: schema 27, zero definições e
zero tipos vinculados. Fixtures sintéticos de outra base de teste foram removidos
com essa base. Não equivale a catálogo fiscal operacional homologado.

Validação: 184 testes distintos, zero falhas/erros/ignorados, PostgreSQL 16.3
descartável e package BUILD SUCCESS; detalhes/fases em [VALIDATION](../fiscal/catalog/VALIDATION.md).
Evidência arquivada fora de target. Parecer inicial Claude APROVADO COM RESERVAS
recebido e lido; resolução complementar e confirmação pendente abaixo. Nenhum CI consultado. Sem publicação
ou merge. V25/V26, legado, saldos, extractos e demo inalterados; RC/RL e
FinancialCore operacional não activados, Fatia 1e suspensa. D-CAT-2 continua
pendente de homologação para validação material por tipologia.


### V27 — resolução local das reservas de revisão

Parecer Claude de 22-10-2026 sobre e54a967, APROVADO COM RESERVAS, lido no
arquivo TUULI. RES-1: 74 testes comerciais/CRUD adicionais aprovados. SUG-1:
manifesto deixa de ser lido no construtor; validação integral na utilização
explícita dos três métodos, falhando em fechado sem fallback. SUG-2: comentário
jsonb corrigido e igualdade numérica com escalas distintas testada.

Validação complementar: 147 testes distintos (31 dirigidos + 116 regressão/upgrade),
zero falhas/erros/ignorados e package BUILD SUCCESS, em PostgreSQL 16.3 descartável.
Não somar aos 184 anteriores por sobreposição. V27/V25/V26 inalteradas, ausência
de backfill e catálogo vazio confirmados; contentor de teste removido.
[Evidência e pedido de confirmação dirigida](../fiscal/catalog/VALIDATION.md).

Confirmação final Claude recebida e lida: APROVADO, 0 BLOCKER / 0 MAJOR / 0 MINOR,
para e54a967..cb7600194003fbefee4a19849a7b37015fd84035. Revisão estática com
confronto da evidência do Executor, sem testes próprios do Revisor.
A restrição anterior de revisão pendente foi ultrapassada. Publicação autorizada
na ordem de integração; estado de push/PR/CI deve ser confirmado no GitHub,
não presumido deste registo pré-publicação. Sem deployment, activação
RC/RL/FinancialCore/1e, alteração de matriz, histórico ou demo.


Parecer final no arquivo Relatorios-AR2/TUULI:
2026-10-22_OP22_V27-catalogo-fiscal_confirmacao-final_Claude.md, SHA-256
aab406b66e9ab40a015489860ca0717165db87f392aef75ad7af56ceb95ee431.
Base remota confirmada pelo conector GitHub na preparação: PR #34,
3bc2f70c473b0cd6bb5011ba46eb4b0dc382bc7f. Norma central consultada em
AR2-Governance/AI_DEVELOPMENT_SYSTEM.md, commit local
6452a11099113de577ee34de4a5e19d9ffff5dbd: decisão de merge reservada a António.
Nenhuma aprovação CI ou integração é declarada antes de ser comprovada.


## D-CAT-2/H1.1 — Homologação semântica FT/FR/RC

23-10-2026. Fonte: ordem executiva D-CAT-2/H1.1 expressamente disponibilizada
pela Administração AR2. FT é factura de liquidação posterior e constitui dívida
do cliente; FR é factura-recibo de liquidação imediata, liquidada na emissão;
RC liquida dívida existente, reduzindo-a ou extinguindo-a. FT/FR partilham a
tipologia interna FT, diferenciadas pela liquidação imediata; RC não possui
especialização adicional. Esta homologação é semântica: não homologa todas as
combinações técnicas, códigos SAF-T ou projecções QR/PDF.

D-CAT-2/02 e parecer incremental Claude de 22-10-2026 foram lidos integralmente
no arquivo Relatorios-AR2/TUULI. Relatório:
2026-10-22_TUULI_D-CAT-2-02_matriz-homologacao-corrigida_Codex.md,
SHA-256 1b5cacaac93d9c4ea1259099f72a02a037e538da226a2078a082fad6f0ff1447.
Parecer: 2026-10-22_TUULI_D-CAT-2-02_verificacao-incremental_Claude.md,
APROVADO, zero achados. Não é revisão da presente especificação.

A [especificação H1.1](../fiscal/catalog/H1_1_FT_SPEC.md) prepara apenas FT
não imediata; não implementada nesta missão. FR/FT imediata, RC (até prova do
enquadramento fiscal), RL/NC/ND e restantes tipos excluídos. RL sem
correspondência homologada. V25/V26/V27 preservadas, sem backfill, alteração
histórica ou activação. Legado mantém autoridade; RC novo inactivo e 1e suspensa.
As reservas de combinações e projecções dos registos anteriores mantêm-se;
a pendência anterior de homologação da semântica FT/FR/RC foi superada por esta
ordem, não a matriz integral. Revisão Claude desta alteração ainda pendente.


### H1.1-R1 — rastreabilidade e reservas da especificação

Fonte primária disponibilizada nesta conversa: [ordem H1.1 e homologação H1
nela declarada](sources/2026-10-23_D-CAT-2-H1.1_ordem-recebida.md).
SHA-256 do arquivo: `8eed0d8bfae8d06420712d6ae54fc65a3214040fccdad66830b4d472d3545051`.
Transcrição documental da mensagem do utilizador em 23-10-2026, com proveniência
no próprio arquivo; nenhum acto H1 autónomo ou assinatura foi fornecido.
Não atribuir aprovação integral da matriz, projecções ou política de entrada.

R1 especificou compatibilidade material de configurações vinculadas em Java e
SQL durante todo o ciclo de vida, inclusive antes da primeira emissão.
A posterior [H2-02](sources/2026-10-23_H2-02_deliberacao-administracao-AR2.md)
aprovou a política A no perímetro FT não imediato e rejeitou o guard de transição
como compatibilidade histórica. A antiga pendência A/B encontra-se superada;
as combinações materiais e projecções fiscais mantêm os respectivos gates.
H2-03/R1 recebeu aprovação documental incremental Claude; essa aprovação não
substitui a revisão da implementação da issue #39.

## H2-03 / issue #39 — implementação local do contrato FT

27-10-2026. Ordem/handoff AR2-Executive-Reports no commit
`4032d5742ac4a59cd3075f9d2dad85949a5a70ef`. Branch isolada
`codex/h2-03-document-type-contract`, baseada no PR #37 acima identificado.

- Implementados Java/JPA/DTO e V28 aditiva para vínculo governado das novas FT
  explicitamente identificadas e não imediatas; integridade em INSERT, UPDATE,
  UPSERT/COPY e leituras do vínculo persistido. Sem inferência por ID/nome.
- Configurações vinculadas são validadas durante todo o ciclo de vida; V25/V26/V27
  permanecem intactas. Nenhum backfill ou reclassificação histórica.
- Fora desse perímetro conservam-se as regras existentes, incluindo RC/RL/guias
  sem código fiscal; isto não homologa a sua correspondência fiscal.
- Inventário interno de prontidão, exclusivamente de leitura e sem novo endpoint,
  sinaliza configurações sem identidade e incompatibilidades antes do uso comercial.
- Manifesto operacional vazio: não há revisão/chave FT publicada. Criação
  governada bloqueada até homologação e publicação controlada. Gates 2/1/1,
  QR/PDF e AT não são resolvidos por esta implementação.
- Validação apenas em PostgreSQL 16.3 descartável; [resultados e limites](../fiscal/catalog/H2_03_VALIDATION.md).
  CI não executado nesta branch local; não confundir testes locais com CI.
- Parecer Claude de 28-10-2026: APPROVE WITH CONDITIONS, 0 BLOCKER / 0 MAJOR /
  3 MINOR. Reexecutadas as 15 classes no commit limpo `6c32e283`, com base
  descartável por classe: 318 aprovados, zero falhas/erros/ignorados na consolidação
  final; tentativa conjunta anterior com duas falhas de contagem preservada.
- Documentados os comportamentos existentes: RC/RL/guias sem identidade fiscal
  mantêm a prontidão fechada como pendência; incoerência do catálogo recusa também
  edições descritivas de tipos vinculados. Sem alteração funcional ou semântica.
- Confirmação incremental Claude recebida e lida: **APPROVE, 0/0/0**; os três
  achados estão resolvidos sobre `6c32e283` e `2dcb797c`. Original arquivado em
  AR2-Executive-Reports, commit `a9992b9c8e79d850402b3e8c37be903bf24d9de8`, SHA-256
  `bfe568045999fa4984d12839935357c1343f3f92dd918a31224c497520eff0a3`.
  Sem testes próprios do Revisor; aprovação da infraestrutura inactiva.
- Actualização H2-05: H2-03 integrada pelo PR #40 em `91da67bc5e799d30abfd1c569ec7ef22fbd5f703`; supera o bloqueio de publicação anteriormente observado. A integração não autoriza cutover ou alteração da origem histórica/demo. CI desse PR não reexecutado nesta missão.
- FinancialCore permanece inactivo, legado mantém autoridade operacional,
  RC/RL novos não activados e Fatia 1e suspensa.

[Contrato implementado e fontes](../fiscal/catalog/H2_03_IMPLEMENTATION.md).


## H2-05 / issue #41 — PDF e QR, implementação local para revisão

28-10-2026. Branch isolada `codex/h2-05-fiscal-pdf-qr`, baseada no PR #40.
ATCUD em todas as páginas comerciais e acima do QR; QR Byte/ECC M, versão ≥9,
impressão 40 mm e margem branca ≥2,5 mm. Q derivado da assinatura completa nas
posições 1/11/21/31; R do certificado aplicável, sem defaults fictícios.

**Dependência material:** não existe produtor/persistência produtiva de assinatura
fiscal no código observado. A nova fronteira exige fonte real vinculada ao
documento; não a fornece nem certifica. Sem fonte, nova emissão comercial recusa
409 com rollback da numeração. Reimpressão usa payload já persistido. Geração
financeira mantém contrato opcional. Não apresentar como emissão comercial pronta.

Validação local: 57 testes dirigidos + 174 de regressão PostgreSQL 16.3 descartável,
231 aprovados, zero falhas/erros/ignorados; PDF final descodificado a 300 dpi.
Package backend aprovado. Não é CI nem suite integral. Revisão Claude preparada,
ainda não realizada. [Dossier e reservas](../fiscal/h2-05/IMPLEMENTATION.md) e
[evidência](../fiscal/h2-05/VALIDATION.md). Sem migrations ou alterações históricas,
manifesto vazio, catálogo/FinancialCore inactivos e Fatia 1e suspensa.
Gates 2/1/1, QR/PDF, AT e FAQ 4443 permanecem abertos. Sem push/PR/merge.


### H2-05-R1 — correcção incremental da revisão Claude

Parecer original de 28-10-2026: APPROVE WITH CONDITIONS, 0 BLOCKER / 1 MAJOR /
3 MINOR; supera a indicação anterior de revisão ainda não realizada. A ordem
executiva aprovou a alternativa B. Fornecedor demo implementado separadamente,
com activação explícita, perfil demo exclusivo, base fac_demo e cluster
autorizado, séries sintéticas próprias e chave externa. Recusa antes de
Flyway/JPA em destinos incompatíveis; nenhum histórico foi convertido.
Origem no payload/versão `TUULI-DEMO-1`, Q/R deliberadamente não fiscais,
aviso permanente no PDF. ATCUD ausente deixa de imprimir rótulo vazio.

Produção mantém 409 sem fonte real, sem fallback demo. RC legado sem fonte
real pode não ter QR, mudança introduzida na H2-05 e agora explicitada; apenas
RC demo autorizado recebe QR não fiscal. Gate RC/RG preservado. ZXing 3.5.3
fixado, com smoke de JAR e regressão de emissão/permissões.
[Validação original](../fiscal/h2-05/VALIDATION.md) e
[correcções/ensaios R1](../fiscal/h2-05/R1.md); revisão incremental Claude
ainda pendente. Sem alteração de migrations, manifesto fiscal ou bases
persistentes/demo, sem activação de FinancialCore/1e ou publicação.

Validação efectiva R1: 254 testes (74 dirigidos + 180 regressão) aprovados,
package e smoke do JAR aprovados; sete cenários de arranque/seed/reset em
PostgreSQL descartável e oito PDFs emitidos/16 páginas com aviso não fiscal
verificados. Cluster `7701754504284078125` removido, bem como credenciais e
chaves efémeras. Não é CI nem teste do deploy/Compose/demo persistente.
Confirmação incremental Claude pendente; nenhum fornecedor real implementado.


### H2-05-R2 — fecho das condições de integração

Parecer Claude R1 recebido: 0 BLOCKER / 0 MAJOR / 3 MINOR A/B/C; confirmação
R1 já realizada, confirmação R2 ainda pendente. Reexecução das 15 classes em
`203276f`, tree `a370beef85c53ca025e8c17c4e4ef94dd212359c`, com worktree limpa
registada por grupo: 254 aprovados, zero falhas/erros/ignorados.
O novo commit complementar exige repetição integral fixada ao novo HEAD;
consultar os resultados pós-commit no relatório externo e artefactos `final/`.

Procedimento novo demo/GOLDEN documentado, sem operações na instalação actual.
Regressão da fixture de permissões após H2-03 e correcção R1 registadas na memória.
Inventário comercial sinaliza `DEMO_ARTIFACTS` por tipo, sem duplicar linhas,
expor payloads ou alterar contagens históricas. PDFs identificam QR demo pelo
payload persistido. Não são novos consumidores operacionais ou guards de emissão.
[Âmbito e evidência R2](../fiscal/h2-05/R2.md). Nenhum deploy, restauro ou promoção
GOLDEN executado. Preservados isolamento demo/produção, 409 sem fonte real,
manifesto vazio e gates fiscais, FinancialCore/1e/cutover.


### H2-05 integrada / H2-06 — análise de assinatura produtiva

30-10-2026. PR #42 integrado em `ac07247`; HEAD H2-05 `e1e9e63` incluído por ancestralidade. Parecer Claude final APPROVE, 0/0/0, hash e referência em [fontes H2-06](../fiscal/h2-06/SOURCES_REVIEW.md). Supera expressamente a confirmação R2 pendente acima. Evidência anterior: 286 testes locais em 17 classes; não repetidos nesta análise. Workflow `41083075726` success; cobre safety tests e package, não a suite PostgreSQL integral.

Issue #43 / H2-06: levantamento e proposta documentais preparados para revisão independente. [Contrato observado](../fiscal/h2-06/CURRENT_CONTRACT.md), [proposta](../fiscal/h2-06/PROPOSAL.md), [riscos e testes futuros](../fiscal/h2-06/RISKS_TESTS.md). Não há implementação produtiva de assinatura nesta entrega. Faltam proveniência legítima, perfil validado, prova persistente, cadeia/cronologia e integração transaccional comprovada. As recomendações de perímetro e custódia não são decisões aprovadas. Revisão Claude H2-06 pendente.

Produção comercial sem fonte real mantém 409; isolamento demo/produção preservado. Manifesto fiscal vazio, gates 2/1/1, QR/PDF, AT, FAQ 4443 e RC/RG mantidos. FinancialCore inactivo, Fatia 1e suspensa; sem deploy, cutover ou alteração à #38/demo/bases.


### H2-06-R1 — correcção documental pós-revisão

30-10-2026. Revisão H2-06: APROVADO COM RESERVAS, 0 BLOCKER / 1 MAJOR / 2 MINOR, conforme [registo executivo #43](https://github.com/amgsrosario/fac/issues/43#issuecomment-6530199084). Supera a indicação de revisão inicial pendente; confirmação incremental ainda pendente. Corrigidas a caracterização dos requisitos criptográficos do exemplar do Despacho 8632/2014 (separada da confirmação de vigência actual), a alegação não demonstrada de arquivo integral FAQ 4443 e a delimitação da cadeia série/tipo fiscal, plurianualidade e datas. Actualizados A/B e T02/T03/T04.

[Fontes e pedido incremental Claude](../fiscal/h2-06/SOURCES_REVIEW.md). D-SIGN-01 a D-SIGN-05 suspensas até confirmação; não há aprovação independente desta correcção. Parecer original não arquivado nesta R1 por não estar disponibilizado; não reconstruído a partir do resumo executivo. Nenhum código, teste executável, chave ou base alterado; sem emissão, chamadas operacionais AT, despesa, deploy, activação ou merge. Mantidos 409 e todos os gates fiscais/financeiros.


### H2-07 / issue #44 — primeiro mecanismo de assinatura, experimental e inactivo

30-10-2026. A issue #44 regista aprovação final Claude H2-06 (0/0/0 em `10483f8`) e D-SIGN-01 parcialmente aprovada para FT posterior/EUR, série nova e material de ensaio. Supera a confirmação incremental H2-06 pendente acima; D-SIGN-02..05 não foram aprovadas. A retoma autorizada criou `codex/h2-07-signature-isolated` sobre a integração `ac07247` e incorporou apenas a documentação H2-06 aprovada. Trabalho de autoria não comprovada em `h2-07-signature-inactive`/`7ce573f` preservado e não reutilizado.

[Contrato H2-07](../fiscal/h2-07/CONTRACT.md): biblioteca sem bean/endpoint/fonte produtiva, canonicalização, assinador efémero, provas experimentais imutáveis, cadeia e replay. Schema Flyway de ensaio separado; não cria V29 produtiva nem toca V25–V28. [Validação e reservas](../fiscal/h2-07/VALIDATION.md): 29 testes novos e 165 de regressão aprovados em execuções documentadas, não uma suite integral nem uma única execução sem erros. PostgreSQL 16.3 descartável; fases com erros de fixture/expectativa preservadas.

Revisão Claude H2-07 pendente. Não é emissor fiscal, prova de custódia real, certificação ou activação. SQL não verifica RSA; leitura/append recusam prova forjada. Preservados produção sem fonte HTTP 409, gates 2/1/1, QR/PDF, AT, RC/RG, FAQ 4443, catálogo vazio, FinancialCore inactivo e Fatia 1e suspensa. Origem, demo e #38 sem alterações. Sem deploy, cutover ou merge.


### H2-07-R1 — reservas Claude e isolamento em âmbito de testes

02-11-2026. Conforme ordem #44, parecer sobre `eff724f`: APROVADO COM RESERVAS, 0 BLOCKER / 0 MAJOR / 3 MINOR; supera a revisão inicial pendente acima. R1 move biblioteca e SQL experimentais para src/test, retirando-os do artefacto produtivo, e explicita ausência de âncora independente para a chave da própria prova. O desenho produtivo exige registo confiável separado para chave/keyVersion/âmbito, sem implementar custódia nesta missão.

[Procedimento R1](../fiscal/h2-07/VALIDATION.md): runner para execução única das dez classes/194 casos sobre commit fixo e limpo, HEAD/tree/status antes/depois, logs/XML/hashes e inspecção do JAR. O SHA testado e resultados efectivos são preservados no relatório externo R1 e na issue #44, sem commits autorreferenciais; os resultados de 30-10 não substituem esta execução. Confirmação incremental Claude pendente.

Worktree desconhecida/`7ce573f` preservada. Mantidos HTTP 409, D-SIGN-02..05 pendentes, gates fiscais e financeiros, ausência de chaves reais, emissão produtiva, activação, deploy ou merge.


### H2-07 integrada / H2-08 — preparação das decisões produtivas

02-11-2026. PR #45 integrado em `f7e8f8ad405a0e5714af0e9fc7264a9e581ff0de`, incluindo `e7eb142`; Git/API/ancestralidade conferidos. [Parecer H2-07-R1 registado](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580103638): APROVADO 0/0/0, por leitura e evidências, sem testes executados pelo revisor. [Integração autorizada](https://github.com/amgsrosario/fac/issues/44#issuecomment-6580136463). Supera as indicações de confirmação/integração pendentes acima. H2-07 encerrada, ensaio em src/test, sem assinatura produtiva; não reaberta nesta missão. Arquivo byte a byte do parecer original não é presumido a partir do comentário executivo.

Issue #46 autoriza análise H2-08, não aprovação D-SIGN-02..05. [Dossier de decisão](../fiscal/h2-08/DECISIONS.md), [integração/riscos/testes futuros](../fiscal/h2-08/INTEGRATION_TESTS.md), [fontes](../fiscal/h2-08/SOURCES.md) e [sucessão/pedido Claude](../fiscal/h2-08/REVIEW_HANDOFF.md). Comparadas custódia local, HSM local e serviço remoto; A/B1/B2 sem preferência até confirmação D-SIGN-04 e suporte real (R1); sem escolha/contratação. Registo de confiança independente, proveniência, perfil actual e operação futura sujeitos às decisões/validação indicadas. Revisão Claude H2-08 pendente.

Nenhuma alteração funcional, acesso a bases, chave real, teste de aplicação, serviço pago ou operação AT. Exemplares normativos públicos arquivados com hashes; não são confirmação integral de vigência/aplicabilidade ou certificação. Preservados 409, gates fiscais/financeiros, manifesto, demo, origem/#38 e worktree desconhecida `7ce573f`. Sem activação, deploy, cutover ou merge.


### H2-08-R1 — correcção documental após parecer 0/1/2

02-11-2026. [Ordem #46/6583447094](https://github.com/amgsrosario/fac/issues/46#issuecomment-6583447094): parecer Claude APROVADO COM RESERVAS sobre `882381f`, 0 BLOCKER / 1 MAJOR / 2 MINOR; supera a revisão inicial pendente, sem declarar R1 aprovada. Original local lido e hash conferido: `cfde064651675a9898bf4316ef9f188bad2b4dd0e5fce14618cbc4a0ae92fca2`, 14805 bytes. Não se presume publicação do original no arquivo privado.

R1 liga proveniência de cada keyVersion à chave pública comunicada por Modelo 24/upload e mantém âncora independente; âmbito da chave é matéria normativa do produtor/programa, não opção livre por cliente. Gate prévio de rotação e validação jurídica de custódia B2/exclusivo conhecimento explicitados. Retirada preferência B; alternativas sem preferência até perfil actual e suporte demonstrados. Acrescentados persistência/regime de cifragem, Hash/HashControl SAF-T, política de backups, contador de reposições e detecção de manipulações a N02/D-SIGN-05/T08/T11/T12/T16. Reservas de vigência/aplicabilidade e fornecedores mantidas.

[Pedido incremental](../fiscal/h2-08/REVIEW_HANDOFF.md). Confirmação Claude R1 pendente, D-SIGN-02..05 reservadas à Administração. Só documentação: H2-07 não reaberta, nenhum código/chave/base/gate alterado, PR #47 draft, sem implementação, activação, deploy ou merge.


### Repriorização executiva — levantamento de pendências em 02-11-2026

[Mandato #46/6584848804](https://github.com/amgsrosario/fac/issues/46#issuecomment-6584848804). H2-08 integrada pelo PR #47 em `051b1fc`, incluindo `2523095`; confirmação original Claude R1 APROVADO 0/0/0 lida e hash conferido (`518ef2c1c58a04a223ef2042b84ef3e6f42bc33885e8babcd6315a8226b237e7`). Supera as indicações anteriores de revisão/integração pendentes; não decide D-SIGN-02..05, agora reservadas à fase final. H2-07/H2-08 não reabertas.

[Mapa de pendências](../planning/reprioritization-2026-11-02/PENDING_MAP.md), [fontes](../planning/reprioritization-2026-11-02/EVIDENCE.md) e [missões propostas/revisão](../planning/reprioritization-2026-11-02/MISSIONS.md). #38 fechada quanto à preparação e restauro: R2 confirmou a prova V27, errata cc67e3f resolveu metadado temporal. Reserva operacional MINOR-003R persiste; H2-03 já foi integrada (V28/PR #40), não continua por implementar. Delta da base exacta/runtime actual ainda não comprovado neste inventário. Só metadata Docker lida, sem SQL ou credenciais.

Prioridade proposta de execução futura: M01 verificar/demonstrar delta da base limpa, M02 capacidade segura de primeiro provisionamento, M03 jornada comercial não fiscal, M04 evidências fiscais independentes e M05 piloto isolado. São propostas para decisão, não novas missões iniciadas. JWT/permissões/Security Gate continuam encerrados; ensaio da instalação e inputs operacionais são pendências distintas. Não presumir prontidão por issue fechada ou ausência de implementação por issue aberta.

Este levantamento é documental, sem testes de aplicação ou alteração de bases. Revisão Claude pendente. Mantidos 409, origem/demo/#38, worktree desconhecida `7ce573f`, manifesto/gates, FinancialCore inactivo e 1e suspensa; sem chaves, contratação, activação, deploy, cutover ou merge.


### PR #48-R1 — preservação das pendências posteriores

03-11-2026. [Revisão/ordem 6600046074](https://github.com/amgsrosario/fac/pull/48#issuecomment-6600046074): APROVADO COM RESERVAS 0 BLOCKER / 0 MAJOR / 1 MINOR sobre `fdacf90`; supera revisão inicial pendente, sem declarar R1 aprovada. Correcção documental preserva expressamente NC/ND e RC/NL sujeito a nova OP, gates legais NL/RCzero/descontos/adiantamentos, Auditoria Funcional Final e Preparação/Certificação AT como adiados/reservados, não cancelados. Certificação posterior às fases preparatórias e dependências de assinatura; nenhum gate ou ordem M01–M05 alterados.

O-1: autoria do R2 #38 atestada por Claude Code no parecer original PR #48, SHA-256 `e84c4fd27cda2439b2936f465d5240a2eb292aa1ddeca94a78ceaf2b69de6d1c`, conferido com sidecar. EVIDENCE.md regista a atestação e a atribuição anterior errada, sem repetir ensaios ou presumir publicação privada. O-2 apenas explicita menor privilégio/uso único/auditoria/ausência de canal remoto como critérios futuros M02. [Pedido incremental](../planning/reprioritization-2026-11-02/MISSIONS.md) pendente. PR draft, sem código, bases, gates, implementação, activação, cutover ou merge.


### M01 — Delta isolado da preparação #38, 03-11-2026

[Ordem #46/6609266825](https://github.com/amgsrosario/fac/issues/46#issuecomment-6609266825). Base integrada confirmada `472cf5847949c9c7d2ad6b75ae5dd444a60bf9f8` (PR #48); não confundida com o checkout principal antigo. [Relatório M01](../planning/m01-clean-base-readiness/REPORT.md) e [validação](../planning/m01-clean-base-readiness/VALIDATION.md). Resultado executor **M01_PASS para o delta isolado**, revisão independente Claude **pendente**.

Preparação conservada comprovada em V27 por READ ONLY, 42 controlos de tabelas iguais a R2 e antes/depois. Snapshot autorizado reutilizado apenas para clone tmpfs: V28 aplicada, validate/rearranques sem migration adicional, 84 FKs/zero órfãos, exclusões preservadas; 7 casos de leitura aprovados em três execuções finais e 1 regressão HTTP 409 sem consumir número em banco sintético separado. São oito casos distintos, não 22 testes únicos. Tentativas iniciais do novo teste (compilação e default de segurança do pom) registadas e corrigidas no teste, sem alteração da aplicação.

Arquivo restrito `tuuli-backups/m01/2026-11-03/`, 32 artefactos e manifesto SHA-256 `9800d974d4f793954c4082b8a68b0b506cee8b07e4160702b627c6b25128b0e8`. Recursos efémeros removidos com guards; segredo invalidado, nenhuma credencial arquivada. Não se repetiu a importação/restaurabilidade #38; não se actualizou a preparação, que permanece V27. Runtime demonstrado é contexto de teste, não deploy/prontidão comercial.

Sem mudança a origem, Ubuntu/GOLDEN, worktree `7ce573f`, código operacional, emissores, manifesto ou gates. Legado autoridade operacional, FinancialCore inactivo e 1e suspensa. M02–M05 não iniciadas. Emitente, provisionamento, configurações/séries legítimas e validação operacional/fiscal mantêm-se pendentes; assinatura reservada à fase final. Trabalhos adiados, auditoria final e certificação AT não cancelados. Sem cutover, activação ou merge. [Encaminhamento efectivo ao revisor](../planning/m01-clean-base-readiness/REVIEW_HANDOFF.md) depende da Coordenação se não houver runtime Claude; comentário GitHub não o activa.


### M01-R1 — Reservas de qualidade da prova, 03-11-2026

Parecer original Claude sobre `bf80a2f`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `9bd30872d2399cc941385460c8e3ec4d77ae8f326b4335e726d3a14ed533e719`, original conferido/preservado. Supera a indicação anterior de revisão M01 ainda não recebida; não implica confirmação R1.

MINOR-001 corrigido no teste: estado efectivo de `SecuritySettings.enabled()`, filtros Bearer/Authorization e resposta 401 ao GET anónimo `/clientes`. Apenas esse percurso, não prova universal de segurança/bypass. MINOR-002: ensaios originais anteriores ao commit e sem estado Git por execução explicitamente documentados; nova captura antes/depois implementada. Validação adicional: **1 caso/1 aprovado**, exclusivamente em banco sintético tmpfs, HEAD limpo `1bf2f135074b1141913afdc1dffd723516951cb9`, tree `55dd470b30b41fd77b05511e196ce2c8ce8c5ca1`, invariantes antes/depois. Sem repetir #38 ou o delta do snapshot V27→V28; sem acesso às bases conservadas. Recursos próprios/segredo efémero eliminados.

[Validação R1](../planning/m01-clean-base-readiness/VALIDATION.md), arquivo separado `tuuli-backups/m01-r1/2026-11-03/`, manifesto SHA-256 `a6c480f9eccc7fecdee7d736b3da77fba1bdaac94194d8457fcfcd4a26c1563e`; originais M01 mantidos. [Pedido incremental](../planning/m01-clean-base-readiness/REVIEW_HANDOFF.md) será encaminhado pelo PO, conforme indicação expressa; confirmação Claude **pendente**, sem presumir activação pelo GitHub. Sem M02, alterações operacionais, bases, gates, push, PR, merge, cutover ou activação.


### M02 — Primeiro administrador local, implementação isolada de 03-11-2026

[Ordem #46/6611272547](https://github.com/amgsrosario/fac/issues/46#issuecomment-6611272547). Base `ca1e802`/PR #49 confirma a M01 encerrada após Claude 0/0/0 e CI verde, superando as indicações históricas de revisão M01 pendente. M01 e o Security Gate não reabertos. Nova branch `codex/m02-local-admin-provisioning`; checkout principal e worktrees conservados.

Reconhecimento dirigido não localizou bootstrap comercial local aprovado: CRUD é autenticado e runners/seeds de demo não constituem esse mecanismo. [Desenho M02](../planning/m02-local-admin-provisioning/DESIGN.md): CLI local explícita sem Spring/endpoint, autorização 0600 e identidade base/cluster/UUID/role, passwords só por consola sem eco. V29 aditiva cria comprovativo/UUID e funções sem EXECUTE PUBLIC, sem contas/roles/grants automáticos. Operador técnico restrito só executa funções; criação de ADMINISTRADOR activo + auditoria existente + consumo único atómicos, locks ordenados e receipt imutável. Não é recuperação/reset de administradores existentes.

[Validação dirigida](../planning/m02-local-admin-provisioning/VALIDATION.md): **99 casos distintos aprovados** (22 novos M02 + 77 regressões), package e smoke do JAR aprovados, com Git limpo capturado por classe. Duas tentativas de testes com expectativas incorrectas/desactualizadas preservadas e corrigidas, sem mudança aos contratos de autenticação ou fiscais. Código funcional idêntico nos commits ensaiados `3a5f49b`, `7fc4e28`, `077a44a`; fecho posterior só documental. Não é suite integral nem CI.

Evidência restrita `tuuli-backups/m02/2026-11-03/`, 100 entradas + manifesto geral SHA-256 `4cdcbd11d90c1c69c35fc6badf130f47f8e0fc550860418d5530f0ed622f83b0`. Recursos e segredos efémeros M02 removidos. Nenhuma base conservada/histórica, Ubuntu/GOLDEN ou worktree `7ce573f` operada; V29 não instalada na preparação conservada (V27 conforme M01). Nenhum histórico fiscal reclassificado, saldo, consumidor ou manifesto alterado.

**Estado inicial M02 (superado pelo registo M03 abaixo): implementada/testada em isolamento, então com revisão independente Claude pendente** — [handoff](../planning/m02-local-admin-provisioning/REVIEW_HANDOFF.md). Autorizações/identidades comerciais, roles/HBA reais e operação numa instalação real dependem de ordem própria. Sem push, PR, merge, deploy, cutover ou activação. Legado autoridade operacional; FinancialCore inactivo, 1e suspensa, gates mantidos e M03–M05 não iniciadas.


### M02-R1 — Correcção incremental após revisão Claude, 03-11-2026

[Ordem #46/6612496711](https://github.com/amgsrosario/fac/issues/46#issuecomment-6612496711). Primeiro parecer original Claude sobre `5ae9e50`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `790a988f0b9d5af3f0e037387c02a91cd389057d26338d151cfb3aa5bd52744a` conferido com sidecar e igual à cópia recebida pela Coordenação. Supera a indicação anterior de primeira revisão ainda pendente; não é aprovação R1.

[R1](../planning/m02-local-admin-provisioning/R1.md): MINOR-001 com guarda efectiva de terminal compatível Java 21/reflexão Java 22+, recusa própria 3 e teste stdin não vazio; MINOR-002 com ownership de objectos relevantes/schema/CREATE/DML e casos não-superuser; O-1 com READ COMMITTED explícito em Java/SQL. V29 ainda local corrigida, sem outra migration. **33 casos dirigidos aprovados**, package/smoke aprovados no commit limpo `b3eb249fb1d13795af72fadc32dade62b5686896`, tree `a5462552377b318c7afef448e1d427817d61c45d`, Git antes/depois igual. Não repetidos os 99 casos, M01/#38 ou testes não afectados.

Arquivo separado `tuuli-backups/m02-r1/2026-11-03/`, 41 artefactos + manifesto de 7698 bytes, SHA-256 `56bc1818a820f8f054b74f17416a8de90865e8dff7f07015f5270035f2771f91`. Original M02: 100 entradas/manifests conferidos e intactos; parecer/sidecar preservados. Recursos R1 descartados com guards, sem segredos nos artefactos. Nenhuma base conservada/histórica, Ubuntu/GOLDEN ou worktree 7ce573f operada.

O-2 dono SECURITY DEFINER não-superuser continua limite de prova de instalação; testes de owners são de **recusa**. O-3 localhost depende de resolução para loopback, preferir 127.0.0.1. O-4 dívida de testes históricos fora do incremento; O-5 colisão V29 condicional a eventual mandato futuro sobre worktree desconhecida, sem presumir integração ou reabrir H2-07. Java nativo disponível é 21; caminhos pós-21 simulados e guarda implementada, não declarada execução nativa inexistente. O-1 mantém classificação não condicionante original.

**Estado na entrega R1: confirmação incremental então pendente, entretanto aprovada e integrada pelo PR #50 conforme registo M03 abaixo**, [handoff actualizado](../planning/m02-local-admin-provisioning/REVIEW_HANDOFF.md) para transmissão efectiva pela Administração. Sem auto-aprovação, push, PR, merge, contas reais, cutover, deploy, activação ou M03–M05. Legado autoridade operacional, FinancialCore inactivo, 1e suspensa, manifesto/gates preservados.


### M03 — Jornada comercial não fiscal, 04-11-2026

[Ordem #46/6613324580](https://github.com/amgsrosario/fac/issues/46#issuecomment-6613324580). Base integrada confirmada `e6bb33f76523a268b39978733e099c96e98d7999`, merge PR #50/M02. Final M02 `e09433b` incluído; revisão incremental Claude 0/0/0, original/sidecar conferidos, SHA-256 `3a4f9110e2d0a8492e49a5a3bd53bddcf0c70baef98354c8aaba51c15cdd94a3`. CI PR #50 safety/package verde; não representa suite PostgreSQL. Supera as indicações históricas de revisão/integração M02 pendente. Observações O2–O9 e limites de instalação continuam válidos.

[Levantamento M03](../planning/m03-commercial-journey/COVERAGE.md) reutilizou cobertura existente e limitou a lacuna à jornada HTTP autenticada sem fonte/revisão fiscal. Branch isolada `codex/m03-commercial-journey`, sem alterar principal ou outras worktrees. **27 casos distintos aprovados** (9 novos de jornada +18 seleccionados) e package no HEAD limpo `7acfc9ec71d9f5c46b61b162ec19a48c3e76c843`, tree `dfc0a646f7d348e7d687c137640669f0d9fc0746`; antes/depois iguais por classe. PG16.3 novo/tmpfs V1–V29, sem reutilizar dados conservados; não repetiu M01/#38. Duas tentativas iniciais de preparação do teste preservadas, corrigidas no teste, não somadas aos casos únicos. Nenhum defeito funcional demonstrado nem alteração de src/main/migrations.

[Validação e limites](../planning/m03-commercial-journey/VALIDATION.md): rascunhos/linhas/mestres/parâmetros, auditoria/perfis e exportações CSV/XLSX não fiscais; 409 sem assinatura/manifesto, rollback da numeração e ausência de movimentos financeiros. Fixtures não homologam configuração fiscal; não é browser, SAF-T, deploy ou prontidão comercial. Arquivo restrito `tuuli-backups/m03/2026-11-04/`, 87 ficheiros + manifesto global SHA-256 `1af9232f2bc54289fcf490806dc995f7017930520f25c0cd1e3a68cce2791cd3`, cópias conferidas e originais preservados. Recursos/segredos efémeros próprios removidos.

**Estado na entrega inicial M03: então preparada para revisão; primeira revisão recebida com duas reservas conforme R1 abaixo. Não integrada.** [Handoff](../planning/m03-commercial-journey/REVIEW_HANDOFF.md) a transmitir pelo PO/Coordenação; comentário GitHub não activa Claude. Sem push/PR/merge, contas reais, bases conservadas, demo/GOLDEN ou 7ce573f operadas. Mantidos manifesto vazio, HTTP 409 e gates fiscais; legado autoridade operacional, FinancialCore inactivo, Fatia 1e suspensa. M04/M05 não iniciadas; assinatura reservada à fase final, trabalhos fiscais adiados/auditoria final/certificação AT não cancelados.


### M03-R1 — Reservas de cobertura, 04-11-2026

[Ordem #46/6627651498](https://github.com/amgsrosario/fac/issues/46#issuecomment-6627651498). Parecer original Claude sobre `97db276`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `34d9bd8b279705b8bde4c975b5d6495718d169b58ef766bb6e048e7c739453b7`, original/sidecar conferidos e cópia inalterada preservada. Supera a indicação histórica de primeira revisão M03 pendente; não é confirmação da R1.

MINOR-001: descrição do artigo efectivamente alterada e relida; retirada alegação de propagação do mestre às linhas/totais. MINOR-002: PUT/DELETE da linha com leituras persistidas e totais 24,60→30,75→12,30, sem mudar código funcional. [Matriz](../planning/m03-commercial-journey/COVERAGE.md) e [validação R1](../planning/m03-commercial-journey/VALIDATION.md) delimitam os resultados. **Apenas nove casos CommercialJourneyTests, nove aprovados/zero falhas/erros/ignorados**, no HEAD limpo `49b1623d34c1dae4061313a5476bab98cbd08261`, tree `9b33526066b69397db29de1b2c0a2ede19f6238b`, antes/depois iguais. Não somar aos 27 como casos únicos; não repetir as 18 regressões/package, M01/#38.

Arquivo restrito separado `tuuli-backups/m03-r1/2026-11-04/`, 16 ficheiros + manifesto de 3062 bytes, SHA-256 `8c67239595d917a9ccd341b49b2cccb131f8341d190aeb9916b6a5b22d93a378`. Originais M03/87 ficheiros mantidos. PG16.3 sintético/tmpfs e recursos/segredo próprios descartados. Apenas teste/documentação, nenhuma base conservada/Ubuntu/GOLDEN/7ce573f operada. O-1–O-5 Claude e limites M02 mantidos. **Estado na entrega R1: confirmação então pendente, entretanto aprovada e integrada pelo PR #51 conforme registo M04 abaixo**, [handoff](../planning/m03-commercial-journey/REVIEW_HANDOFF.md) para transmissão pelo PO. Sem push/PR/merge, contas reais, activação, deploy, cutover ou M04/M05; gates 409, manifesto vazio, FinancialCore inactivo e 1e suspensa.


### M04 — Matriz fiscal independente da assinatura, 04-11-2026

[Ordem #46/6628280906](https://github.com/amgsrosario/fac/issues/46#issuecomment-6628280906). Base confirmada por API/Git/HTTPS `ff9d2ec92e2419cbe6fec9be22d65afa5520e84b`, merge PR #51; `41a73bb` ancestral. **M03 encerrada e integrada**, confirmação Claude 0/0/0 original/sidecar SHA-256 `186a75d90ecd53df62dea5e531a4424fc8c0086b4f2225c130d098f32d87f7de` conferidos. CI PR #51 run 41812144559 safety/package success, não suite PostgreSQL. Supera as indicações anteriores de confirmação M03 pendente. O-6: temporário removido após conferir cópia arquivada, sem alterar evidência. Checkout principal/worktrees preservados.

Branch isolada `codex/m04-fiscal-evidence`. [Matriz M04](../planning/m04-fiscal-evidence/MATRIX.md) liga decisões/fontes/provas/lacunas/assinatura/reservas/próximos passos para FT/FR/RC, QR/PDF/estados e SAF-T. [Fontes](../planning/m04-fiscal-evidence/SOURCES.md): reutilizadas H1.1/H2-03/H2-04/H2-05/M03, sem repetir testes. Hashes dos 50 artefactos H2-03 e 124 H2-05 conferidos; XML H2-05 final 17 classes/286 aprovados são prova anterior, não execução M04. Não foi executado SQL, Docker, testes de aplicação ou exportador.

**FAQ4443 recuperada integralmente** na rota oficial faqs-00084, HTTP 200 em 04-11, SHA-256 `01dccd91844986d3e86c86b68475c870b8038c400649b4e1d4cd514f913ddd47`; antigo 00879 continua404. Lacuna de obtenção da fonte suprida, sem fechar homologação QR/PDF. XSD oficial SAF-T1.04_01 obtido, SHA-256 `292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350`. [Contrato SAF-T proposto](../planning/m04-fiscal-evidence/SAFT_CONTRACT.md) separa estrutura/mestres/estados de Hash/HashControl produtivos e não generaliza esses campos a Payments; sem implementação.

Arquivo M04 restrito `tuuli-backups/m04/2026-11-04/`, 6 ficheiros + manifesto de 955 bytes, SHA-256 `1536e33418d9d9bd40d43ef7bb6cb2953663776074f946918536746d93af3468`. Fontes originais, metadados e prova de reutilização preservados, sem dados pessoais/credenciais.

**Estado da entrega inicial M04: primeira revisão entretanto recebida com duas reservas conforme R1 abaixo; não homologada/integrada.** [Handoff Claude](../planning/m04-fiscal-evidence/REVIEW_HANDOFF.md) para transmissão efectiva pelo PO; revisão não presumida.2/1/1 continua pendente de validação fiscal expressa; projecções FT/FR/RC, enquadramento RC/RG, dados/séries/AT e perfil SAF-T mantêm reservas. Assinatura D-SIGN-02..05 reservada à fase final; F01/NC/ND/RC/NL/RL nos mandatos próprios, auditoria final/certificação adiadas e mantidas. Sem código/migrations/manifesto, base conservada/demo/GOLDEN/7ce573f, emissão real, chaves, cutover, push/PR/merge, activação ou M05. Legado autoridade operacional, FinancialCore inactivo e 1e suspensa.


### M04-R1 — Rastreabilidade de HashControl e prova 2/1/1, 04-11-2026

[Ordem #46/6628777871](https://github.com/amgsrosario/fac/issues/46#issuecomment-6628777871). Parecer original Claude sobre `ddd563b`: **APROVADO COM RESERVAS 0/0/2**, SHA-256 `e09a436ba2818678a8fd2a82d94b5b42bd83f5020eb18357ad7201d7f6b888f0`, original/sidecar/cópia recebida conferidos. Supera a indicação de primeira revisão M04 pendente; não é confirmação da R1.

MINOR-001: [M4-14 e fontes N08/E06](../planning/m04-fiscal-evidence/MATRIX.md) passam a citar Portaria363/2010 original, H2-07/CONTRACT e H2-08/DECISIONS com linhas exactas. Signature não expõe versão de chave e não fornece actualmente a origem do HashControl; integração produtiva permanece por demonstrar, sem alterar contratos ou escolher D-SIGN. MINOR-002: [C211/M4-02](../planning/m04-fiscal-evidence/SOURCES.md) separa seed demonstrativo com tuplo completo, FinancialFtOrigin que não lê área/entidade/sinal, extracto que usa sinal e UI que filtra área. **2/1/1 pendente de validação fiscal expressa.**

Exemplar Portaria363 já arquivado na H2-06: 47074 bytes, SHA-256 `6bbfc8e1e4405542c0fa1533964d3ee0d235d363ba3adf0e83414edb2db2a5b3`, conferido e copiado sem alterações. Arquivo separado `tuuli-backups/m04-r1/2026-11-04/`, 4 ficheiros + manifesto 743 bytes, SHA-256 `d5063556d61cc7948388492c145fd00730f147a4c91718c5c8ff8a3aa7b4e805`. Preservados parecer/sidecar e todos os originais M04/FAQ/XSD/evidências anteriores.

**Estado na entrega M04-R1: confirmação então pendente, entretanto aprovada e integrada pelo PR #52 conforme M05 abaixo**, [handoff](../planning/m04-fiscal-evidence/REVIEW_HANDOFF.md) para transmissão efectiva pelo PO. O-1–O-4 registadas como não condicionantes, sem fecho declarado. Nenhum teste/SQL/Docker, código, migration, base, demo, manifesto, assinatura real, activação, push/PR/merge ou M05. Gates fiscais, financeiros, D-SIGN/fase final, legado autoridade operacional e FinancialCore inactivo mantidos.


### M05 — Instalação e recuperação não produtivas, 04-11-2026

[Ordem #46/6629520296](https://github.com/amgsrosario/fac/issues/46#issuecomment-6629520296). Base confirmada API/Git/HTTPS `d17de43f714a18c7f7bb7b1fc719f71d77b4e14f`, merge PR #52/M04; 8b34294 ancestral. M04 aprovada Claude 0/0/0, original/sidecar 0d4a8037fab67059a4d0dcc1f24030345c8324ba171a0b89bbd7343cd6eba658 conferidos na publicação; integração não homologa 2/1/1, projecções ou outras reservas. CI PR #52 run 41819790670 safety/package success, não suite PostgreSQL. Supera o registo anterior de confirmação M04 pendente.

Branch/worktree isolada `codex/m05-isolated-pilot`. [Runbook](../planning/m05-isolated-pilot/RUNBOOK.md), [validação](../planning/m05-isolated-pilot/VALIDATION.md) e [handoff](../planning/m05-isolated-pilot/REVIEW_HANDOFF.md). **47/47 verificações de execução aprovadas**, não casos JUnit, no HEAD limpo 244abddbfd91ee9091becd87a060ab0d81551c33/tree 1f24738f59141c21dc72aa1ff93b06bcb284a5dc, antes/depois iguais. JAR construído em c0968cd e reutilizado por hash/código funcional igual; apenas runner/documentos modificados, nada src/main/migrations/manifesto. Duas tentativas falhadas de harness preservadas, não defeitos de produto nem sucessos ocultos.

Dois clusters PG16.3 novos/tmpfs, CLI M02 local real sem eco, owner de funções não-superuser e operador restrito, conta/admin sintéticos; runtime Java 21.0.13/prod não-root/read-only, HTTP real de jornada/permissões/auditoria. Emissão bloqueada 400 por série sem código AT; criação FT governada 409 por catálogo vazio. **Não é nova prova HTTP 409 de assinatura**: reutilizada M03 sem fabricar código AT ou revisão fiscal. Gates/efeitos financeiros preservados e limite explícito.

Backup sintético 237662 bytes SHA-256 `7a58aa67f8da916a9c58a38082c34266c23d2e11cf3713e994e8924cdcc75e6a`; restauro em cluster distinto preservou 43 tabelas/15 sequences e 86 FKs sem órfãos, validou 29 migrations sem novas alterações, recusou reabrir bootstrap e permitiu login/leitura restaurados. Roles/segredos técnicos/HBA configurados separadamente, sem reset de UUID/comprovativo.

Arquivo `tuuli-backups/m05/2026-11-04/`, 54 ficheiros + manifesto 9438 bytes SHA-256 `b016d9fccaf74b2d19d2b58ca2eded52326914b4ff4d82c6c4878d49d37516b3`; JAR/logs/dump sintético/controlos/runner preservados, cópias conferidas. Recursos próprios/segredos removidos; nenhuma origem/preparação/Ubuntu/GOLDEN/7ce573f operada. **M05 preparada para revisão independente, não piloto comercial pronto**. Sem push/PR/merge, activação ou cutover. Condições reais de instalação, fiscalização/certificação, assinatura/fase final, SAF-T e F01 mantêm mandatos próprios; legado autoridade operacional, FinancialCore inactivo e 1e suspensa.

## M05 — Estado da revisão incremental

Ordem #46/6631045763, parecer original/sidecar e511c301b4894aeb5869f98776720bdc81fc0b53afc45f797837428117cb4ffb (distinto da cópia executiva citada). Parecer inicial APROVADO COM RESERVAS 0/0/3. Confirmação independente R1 realizada. Parametrização branch/Maven, digests e limpeza manual no runbook; percurso por omissão em validação descartável. Diagnóstico podeEmitir=true não equivale a prontidão fiscal: emissão semAT400; criaçãoFT catálogo vazio409. M03 só prova409 assinatura em MockMvc/test/fixtureSQL, não runtime M05. Decisão sobre suficiência/novo ensaio/adiamento reservada à Administração; mudança do diagnóstico exige mandato próprio. Observabilidade limitada e O-1–O-6 mantidas. Bases conservadas e gates intactos, sem push/PR/merge/activação.

Validação R1 concluída no HEAD limpo4d4d9e4/tree cee9b134:48/48 checks pelo comando sem argumentos com package sobre `target/` existente. Arquivo m05-r1/2026-11-04,35 ficheiros+manifesto72a931d520966f8cf9b2a86a25b1dd788af8320c2683e5ee7060a15b4a2b3ddb; nenhum recurso M05 após limpeza. Confirmação independente R1 realizada. A decisão sobre a prova de assinatura em runtime permanece pendente. A prontidão comercial continua por demonstrar.

### M05-R2 — Proveniência do ensaio e do artefacto

A execução R1 `20261104-125327-6ca8de` correu sem argumentos, com Git limpo em `4d4d9e47aa273a34e286ec364b1a4453a4c5d861` (tree `cee9b134a340a903eb0771398c240ffa59c8eabd`). O comando `mvn -o -B -DskipTests package`, resolvido pelo PATH, terminou com sucesso sobre um `target/` existente. O log regista classes actualizadas, testes omitidos e a fase de reempacotamento Spring Boot. O JAR utilizado conservou os mesmos bytes e SHA-256 `a1e73e8f67aeaf813775587b64e69ae9a0230118116fcd0bce8efcad544e8b09` do artefacto produzido em `c0968cd49fc3f76cd9b3c1232a9f36cb0dc61373` (tree `a42ec72f2b25dcceb1b35cd408e53c1433e771db`). Em `jar.json`, `git` identifica o ensaio e `reused_package=false` indica a ausência da opção `--reuse-package`. A prova cobre o package incremental e o percurso de instalação e restauro com o binário existente. Falta uma execução integral de raiz, da compilação com `target/` vazio ao restauro.

[A ordem #46/6633281690](https://github.com/amgsrosario/fac/issues/46#issuecomment-6633281690) regista a confirmação R1 com 0 BLOCKER, 0 MAJOR e 1 MINOR. Os sidecars conferem o parecer inicial vinculativo (`e511c301b4894aeb5869f98776720bdc81fc0b53afc45f797837428117cb4ffb`) e o parecer incremental (`2c66fd9c1a01b548d10cc3c9cbfbe553eb5f0566b24c753a7468c737f390d278`). Segundo a atestação do revisor, `4b54cac3171ecb84627ca0f3d2d1d7baddcdfcb059a43bb76416d62f32014cd8` identifica a primeira versão do parecer inicial, anterior a dois acertos textuais. O-1 a O-8 permanecem registadas. A decisão sobre a prova de assinatura 409 em runtime foi tomada separadamente, conforme o registo abaixo. O parecer independente R2, sobre `22fc4c6`, encerrou a reserva de proveniência e registou um novo MINOR histórico (0 BLOCKER, 0 MAJOR, 1 MINOR). Original e sidecar conferidos, SHA-256 `8fe0f69250ecda47b5a0186e29d11119fdbe51c8234797ce3cd0b954608940ae`. A R3 repõe a frase histórica H2-07-R1 e corrige o estado da confirmação R1 (O-9). Confirmação incremental R3 pendente.

### M05 — Fecho técnico do MINOR-003, 04-11-2026

A [deliberação da Administração AR2](https://github.com/amgsrosario/fac/issues/46#issuecomment-6637180973) encerra tecnicamente o MINOR-003 no cenário sintético `M5N`. O JAR em execução, no ensaio `20261104-165749-6ad7e3` sobre `1d1c63da79340c9df3b6c07dd81dcc8156d68ff7`, devolveu HTTP 409 por ausência de fonte de assinatura. O rascunho permaneceu em `RASCUNHO`, o numerador ficou a zero e os controlos das 43 tabelas e 15 sequências coincidiram antes e depois. A cobertura é específica do tipo sintético sem classificação fiscal `M5N`. A emissão de FT governada e a certificação fiscal ficam fora desta prova.

Claude Code confirmou a evidência por leitura e emitiu APROVADO, 0 BLOCKER, 0 MAJOR e 0 MINOR. O original `2026-11-04_TUULI_M05-MINOR-003_ensaio-assinatura_Claude.md` e o respectivo `.sha256` estão preservados em `Relatorios-AR2/TUULI`. O SHA-256 calculado sobre o original coincide com o sidecar, `962394e0fc752564b705d6c57ce0e4e146f4a2070205168b322e581721dd6b64`. Evidências do executor no arquivo restrito `tuuli-backups/m05-signature/2026-11-04/20261104-165749-6ad7e3/`, manifesto SHA-256 `01f64449965549e99887113ce0b6f4358f69a77b105bd5ea4131eef30a3d0389`. Conferência local concluída. Identificação preparada para registo na issue #46.

**M05 pendente de fecho global.** A prova de instalação e restauro existente usa o binário previamente produzido em `c0968cd`. Falta demonstrar, numa única execução descartável, a compilação com `target/` vazio, o uso do JAR daí produzido e o percurso até ao restauro verificado num segundo destino. Em separado, a Administração deve decidir o significado de `podeEmitir` e a sua apresentação ao operador. No ensaio original, o diagnóstico devolveu `true` com bloqueios vazios e a emissão do mesmo rascunho foi recusada com HTTP 400 por ausência de código AT. A eventual alteração desse sinal exige mandato de produto próprio.

### M05 — Prova integral de raiz, 04-11-2026

A [deliberação AR2 #46/6637438647](https://github.com/amgsrosario/fac/issues/46#issuecomment-6637438647) reconhece o fecho técnico do MINOR-003 em M5N e autoriza a prova integral descartável. A execução `20261104-173954-ef7da4` terminou com 48/48 verificações. HEAD limpo `1d1c63da79340c9df3b6c07dd81dcc8156d68ff7`, tree `54ae587c1af32f2ab91d900e5401cb0b33cb40d5`. Com `target/` inicialmente vazio, Maven 3.9.16 e JDK 21.0.12.1 executaram `mvn -o -B -DskipTests package`. O SHA-256 do novo JAR, `09d39157b41db26526984ec72f6218d828688a08b8b7526dfbc5e49a380f8446`, foi confirmado dentro dos quatro arranques em Temurin 21.0.13. O backup sintético tem SHA-256 `7096274615377b118bac2822a2375c1bc5d080fa4ef5ee1b392b74354ffb8807`. O restauro no segundo PostgreSQL 16.3 confirmou equivalência das 43 tabelas e 15 sequências, 86 FKs validadas e zero órfãos. Recursos próprios, ficheiros privados e worktree descartável removidos.

Evidência restrita em `tuuli-backups/m05-clean/2026-11-04/20261104-173954-ef7da4/`. Manifesto conferido em 37/37 entradas, SHA-256 `d295e03cfe80cc0a8d7411b2973418b8b619aa5f8a95739b2932b38e1761a77c`. O JAR está arquivado e conferido separadamente. Pedido Claude em `tuuli-backups/m05-clean/2026-11-04/PEDIDO_CLAUDE.md`, limitado à cadeia de compilação, JAR, backup, restauro e isolamento. A revisão independente confirmou a cadeia e registou o MINOR-001 documental sobre a preparação e invocação do runner. O fecho global da M05 permanece reservado à Administração.

A deliberação também definiu a regra de produto de `podeEmitir`, superando a pendência de definição registada acima. O valor deve ser verdadeiro quando todas as pré-condições obrigatórias verificáveis na consulta estiverem satisfeitas. A falta conhecida deve produzir falso com motivo compreensível. A validação transaccional continua obrigatória no acto de emitir. A proposta técnica de contrato/API, erros e regressões cabe a mandato próprio. Implementação pendente.

O relatório do executor `tuuli-backups/m05-clean/2026-11-04/2026-11-04_TUULI_M05-prova-integral_relatorio_Codex.md` regista os comandos recuperados e distingue a reconstrução para reprodução. SHA-256 `c80279b6224df9dee399b97cffe66315e256226112db4b008e4648ed03d5dee9`. Parecer original e sidecar conferidos, SHA-256 `146b14b8444a904f6f8a7f2b230288e816d61fcc1906ba9eca2953bb79495f37`. Confirmação incremental do MINOR-001 pendente.

### M05 — Fecho global, 04-11-2026

A Administração AR2 aprovou o fecho técnico global, condicionado à conferência e ao registo versionado agora concluídos. **Estado: `M05_CLOSED`.** Claude Code confirmou o fecho do MINOR-001 da prova integral com 0 BLOCKER, 0 MAJOR e 0 MINOR. O delta revisto, sobre `1d1c63da79340c9df3b6c07dd81dcc8156d68ff7`, corresponde à correcção nos três documentos. SHA-256 de `MINOR-001.delta.diff`: `d17618270d925628fa1d1dc241290d5dbbb19927197e78c3354df52d9f3f752b`. [Deliberação #46/6638075940](https://github.com/amgsrosario/fac/issues/46#issuecomment-6638075940)

Original `2026-11-04_TUULI_M05-prova-integral-MINOR-001_confirmacao-incremental_Claude.md` e sidecar `2026-11-04_TUULI_M05-prova-integral-MINOR-001_confirmacao-incremental_Claude.md.sha256` preservados em `Relatorios-AR2/TUULI`. O SHA-256 calculado sobre o original coincide com o sidecar, `ec77d8f7e7a3a56a264ed4f748eec278f9dd06ba7a560310c6802f83d4e4f1d4`. O MINOR-003 está encerrado no cenário sintético M5N. A implementação de `podeEmitir` segue em mandato próprio. O fecho refere-se à validação técnica da M05 em isolamento.


### M4-02 — Prova MockMvc positiva, 05-11-2026

**Correspondência funcional aprovada. Prova positiva MockMvc confirmada, com fonte sintética de assinatura exclusivamente de teste.** [Mandato #46/6654412431](https://github.com/amgsrosario/fac/issues/46#issuecomment-6654412431). FT/1 sintética `2/1/1,false` e FiscalSignatureTestConfiguration exclusivamente em testes. Emissão EMITIDO, total12,30, dívida e saldo em aberto12,30, zero recebimentos/afectações/SETTLE. Repetição409 preserva a dívida única. Recusas de assinatura e série sem código conservam snapshots financeiros. Leituras persistidas fora de transacção exterior de teste.

Um teste dirigido aprovado no HEAD limpo `e090a4add3fe084bad2244d2e66b76de6ab63b35`, tree `bd75df3aadd8d456753c340f8ea8db9965f53a62`. O arquivo contém três tentativas anteriores de preparação, logs/XML e resultados. Recursos PostgreSQL16.3 tmpfs próprios removidos e segredos efémeros retirados. Arquivo `tuuli-backups/m4-02-positive/2026-11-05/`,70 ficheiros+manifesto `d0e36a81e967a4c8b35e0ff7188197d11b7637981c6ef3f83ee0d48558e8e08f`. Parecer anterior original e sidecar `2026-11-05_TUULI_M4-02_ensaio-FT-diferida_revisao-independente_Claude.md` conferidos, SHA-256 `2478a7d7c383fc40d232ee35579c3078a8a6f92b03b4f1c170ad0d4b522b3ea8`. [Validação](../planning/m4-02-positive/VALIDATION.md). Os gates fiscais, de publicação e integração permanecem aplicáveis.


### M4-02 — Correcção documental MINOR-001, 05-11-2026

**Correspondência funcional aprovada. Prova positiva MockMvc confirmada. Matérias produtivas pendentes.** O parecer sobre `835c8fc` confirmou os seis critérios no alcance descrito e identificou uma reserva documental na matriz (0 BLOCKER, 0 MAJOR, 1 MINOR). A linha M4-02 explicita a fonte sintética e a dependência de assinatura na emissão. A emissão positiva pelo JAR, a assinatura real e as validações fiscais fora da correspondência aprovada pela deliberação #46/6638699008 continuam pendentes. O original `2026-11-05_TUULI_M4-02-positivo_revisao-independente_Claude.md` coincide com o sidecar, SHA-256 `e212d533b3ef1f75a0fcd5b169c16bdbb733a11117884464c800e18e6d51ab33`. Confirmação incremental da correcção documental pendente. Os seis critérios MockMvc e as evidências anteriores mantêm o alcance reconhecido pelo revisor.


### M4-02 — Aprovação independente R1, 05-11-2026

A confirmação incremental sobre `c24348a8cc8857957e276ed97c679951ed3a6657`, tree `d7dfaae27e2be43429a8ff1ac14b4030359bdecd`, é **APROVADO, 0 BLOCKER, 0 MAJOR e 0 MINOR**. O MINOR-001 documental está encerrado. A prova positiva MockMvc está confirmada com fonte sintética exclusivamente de teste. **Correspondência funcional aprovada. Prova positiva MockMvc confirmada. Matérias produtivas pendentes.** Emissão positiva pelo JAR, assinatura real e validações fiscais exteriores à correspondência aprovada continuam dependentes de mandatos e evidências próprios. [Seguimento #46/6655672950](https://github.com/amgsrosario/fac/issues/46#issuecomment-6655672950)

Original `2026-11-05_TUULI_M4-02-positivo-R1_confirmacao-incremental_Claude.md` e sidecar `2026-11-05_TUULI_M4-02-positivo-R1_confirmacao-incremental_Claude.md.sha256` preservados em `Relatorios-AR2/TUULI` e copiados byte a byte para `tuuli-backups/m4-02-positive-r1/2026-11-05/`. SHA-256 calculado sobre o original e coincidente com o sidecar, `04b22642b57115efaf5a50a234e1eb20d5bda17fc7a4d1da426dd87b7bd048d9`. O-28 tratada na sequência da matriz, com a aprovação funcional explícita, e O-29 tratada no estado da revisão em VALIDATION.md. As restantes observações conservam o alcance do parecer.


### Continuidade M05/M4-02 e M04, 05-11-2026

Base remota consultada em leitura, d17de43f714a18c7f7bb7b1fc719f71d77b4e14f. M05 em 828eefa é ancestral da M4-02 em 43bea3f, que contém os 17 commits necessários. Proposta de entrega única pela sequência consolidada, sem conflito por ancestralidade face à base consultada. Alterações documentais locais da worktree M05 preservadas e já representadas na entrega M4-02, sem reaplicação. A proposta mantém as revisões e arquivos existentes. [Preparação de integração](../planning/continuity-2026-11-05/INTEGRATION.md)

Levantamento actualizado dos 17 requisitos, com provas no respectivo perímetro, lacunas e dependências fiscais/assinatura. M4-10 recomendado como próxima fatia dirigida de leitura/reimpressão e preservação do QR persistido, com mandato próprio. A proposta de podeEmitir descreve contrato actual/aditivo, avaliação pura das pré-condições conhecidas e regressões, preservando a validação transaccional noPOST. Decisões de publicação/integração e execução das próximas fatias reservadas à Administração. [Estado M04](../planning/continuity-2026-11-05/M04_STATUS.md) · [Proposta podeEmitir](../planning/continuity-2026-11-05/PODE_EMITIR_PROPOSAL.md) · [Ordem #46/6656010004](https://github.com/amgsrosario/fac/issues/46#issuecomment-6656010004)


### Continuidade — Correcções documentais para revisão incremental, 05-11-2026

Parecer original `2026-11-05_TUULI_continuidade-M05-M4-02-M04_revisao-independente_Claude.md` conferido com o sidecar, SHA-256 `dfa2be3f749a73ffb26cb7aa288df6ba5fe7d6bde77a3d4454c70eb7b61f9335`, APROVADO COM RESERVAS, 0 BLOCKER, 0 MAJOR e 6 MINOR. A correcção identifica `feature/visual-redesign` como destino proposto e inclui o commit de preparação na lista de entrega. Delimita M4-10 como prova parcial do documento comercial, com transição real para ANULADO e auditoria permitida na reimpressão. Homologação QR/PDF e matérias produtivas continuam pendentes.

A proposta podeEmitir trata a confirmação de recálculo como aviso, define cálculo projectado sem persistência e explicita o limite actual dos métodos que gravam. Mapeia as recusas verificáveis do POST e as exclusões dinâmicas, sem impor um gate de catálogo novo. Define os três consumidores UI, a reavaliação após guardar e a separação entre elegibilidade documental e permissão do actor. Referências, salvaguardas e reservas O-31 a O-39 alinhadas. A confirmação incremental das seis correcções está pendente. A implementação continua dependente dos mandatos próprios.


### Continuidade — Correcções documentais MINOR-007/008, 05-11-2026

O parecer incremental R1 encerrou MINOR-001 a MINOR-006 e identificou duas reservas novas, documentais (0 BLOCKER, 0 MAJOR, 2 MINOR). Original `2026-11-05_TUULI_continuidade-M05-M4-02-M04-R1_confirmacao-incremental_Claude.md` conferido com o sidecar e com o digest comunicado, `8498183e33dcfa736ed59760cdbac9190b4f2ae610033961cff9971577427d3a`. A matriz passa a descrever M4-10 como prova comercial parcial, com fixture apenas na preparação e transição real para ANULADO. A reserva de homologação QR/PDF permanece.

O aviso de recálculo da proposta usa `fiscalRecalculoAviso` persistido ou a activação projectada num legado pelas comparações do POST. Indicador activo exige confirmação mesmo com totais iguais; uma diferença em documento não legado com indicador inactivo não cria a exigência por si. O aviso isolado permite prosseguir para confirmação. O-40 acrescenta a recusa verificável do prefixo ATCUD: e O-41 actualiza o delta cumulativo. Confirmação incremental limitada a MINOR-007/008 pendente. A implementação funcional continua dependente dos mandatos próprios.


### M4-10 — Prova comercial parcial após PR #53, 05-11-2026

Mandato #46/6661589044. Base `5ec635686a15c19f2603ba26f048bc5ab8c10b26`. Claude Code: APPROVE, 0/0/0, limitado ao HEAD `217e89fe6ef2f60cb473fd3466867fee7af9b9ca`. Original e sidecar conferidos: SHA-256 `ba7b8bd5acd76ecc0ca28d35a48af8c9108094d18420dcebc462ae52d40ce36f`. Prova HTTP por MockMvc, com fonte sintética; não prova JAR nem emissão produtiva. Anulação pelo POST e QR preservado nas três reimpressões comerciais; apenas auditoria permitida nas tabelas comparadas. Numerador coberto, estado das sequências não demonstrado. Evidências e primeira tentativa preservadas em tuuli-backups/m4-10/2026-11-05. Sem novos ensaios neste fecho. PDF financeiro, homologação QR/PDF, assinatura real e impressão física pendentes. Entrega preparada para decisão de publicação; sem autorização implícita de push, PR, merge ou activação. [Validação](../planning/m4-10-commercial/VALIDATION.md).
