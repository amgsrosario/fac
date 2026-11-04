# M05 — Evidência da instalação e recuperação sintética

04-11-2026. Mandato [#46/6629520296](https://github.com/amgsrosario/fac/issues/46#issuecomment-6629520296). Base d17de43f714a18c7f7bb7b1fc719f71d77b4e14f/PR #52. **Resultado do executor: 47/47 verificações aprovadas no perímetro não produtivo; revisão independente pendente.** Não é suite JUnit, prontidão comercial ou autorização de activação.

## Versão e proveniência

Runner versionado executado no HEAD limpo `244abddbfd91ee9091becd87a060ab0d81551c33`, tree `1f24738f59141c21dc72aa1ff93b06bcb284a5dc`, antes/depois iguais. Último fecho apenas documental. Não há src/main, migrations, manifesto, frontend ou pom alterados. [run_pilot.py](run_pilot.py) guarda checks, comandos/configuração de execução, IDs e logs sem segredos; [runbook](RUNBOOK.md) explica a reprodução.

Package offline efectivamente aprovado em `c0968cd49fc3f76cd9b3c1232a9f36cb0dc61373`, tree `a42ec72f2b25dcceb1b35cd408e53c1433e771db`, limpo. JAR 82521664 bytes, SHA-256 `a1e73e8f67aeaf813775587b64e69ae9a0230118116fcd0bce8efcad544e8b09`. Reutilizado no ensaio final após hash igual e diff funcional src/frontend/pom vazio. Não reconstruído nem apresentado como build do244abdd. As alterações de runner não alteram o JAR. Java de build 21.0.12.1, Maven 3.9.16 offline; runtime oficial Temurin **21.0.13**, perfil prod em quatro arranques, todos não-root e read-only.

Imagens/digests completos em images.json: postgres16.3 `d0f363f...f609`, node20-alpine `fb4cd12...2293`, eclipse-temurin21-jre `0b99b337...6901a`. Só a JRE foi obtida para esta missão, sem instalar pacotes no host. Imagem imutável identificada por digest na evidência; não inferir versão da tag.

## Instâncias e isolamento

| Item | A (instalação) | B (restauro) |
| --- | --- | --- |
| Base | tuuli_m05_a_test | tuuli_m05_b_test |
| Cluster PostgreSQL16.3 | 7704282213028896813 | 7704282502779654189 |
| Contentor PG | tuuli-m05-709f54-a | tuuli-m05-709f54-b |
| ID PG | 7eba3830c611af04e490e02fa4a844f08e326b3f97004a5fa1bd8f128ba45d4a | 63bb37a149619311588526b863e02cab2c4c02f7f0cf0f1372e2ea50479aa4d1 |
| Proxy HTTP host | 127.0.0.1:53061 | 127.0.0.1:65262 |

PGDATA tmpfs, sem mounts conservados e sem 5432 publicada; redes internas próprias, proxy HTTP read-only/cap-drop/no-new-privileges e publicação só loopback. HBA só permite TCP DB por loopback, SCRAM; DBA usa socket no contentor criado. JRE partilha apenas o namespace de rede do PG correspondente, não mounts de dados da origem. JAR mount read-only. Runtime UID10001, sem capabilities adicionais, rootfs read-only e tmpfs de trabalho. IDs PG/proxy e hashes HBA/imagens arquivados; IDs transitórios dos outros contentores são guardados pelo executor para limpeza, não há inventário persistido de todos eles. Não se alega inspecção independente da configuração Docker.

Roles por cluster: m05_owner dona/migradora **não-superuser**, m05_app runtime DML sem CREATE/ownership ou DML do comprovativo e m05_operator temporária apenas CONNECT/USAGE/EXECUTE nas duas funções M02. Permissão explícita pg_control_system apenas ao dono das funções, não ao operador. Arranque inicial de migração separado e parado antes da CLI; runtime usa só credencial app e Flyway off/JPA validate. Em B, fase de validação usa dono, termina antes do runtime. Não são contas comerciais ou alterações a roles de outras bases.

## Matriz efectivamente exercitada

| Percurso | Prova / resultado | Limite |
| --- | --- | --- |
| Instalação vazia | Flyway aplica 29 migrations V1–V29, JPA/health UP, utilizadores 0 | Não é upgrade da preparação #38, que não foi acedida |
| Primeira administração | CLI real via PropertiesLauncher/PTY local, 7 prompts, exit0, passwords sem eco, sem Spring; comprovativo consumido liga M05ADMIN ao evento1 | Identidade/admin sintéticos; nenhum transcript terminal arquivado. Não é canal remoto ou provisionamento de conta real |
| Dono das funções | Owner m05_owner não-superuser e grants mínimos do ensaio permitem provisionamento bem-sucedido | Acrescenta prova positiva ao limite O-2 M02 **neste ambiente**, não demonstra instalação/HBA/TLS comerciais universais |
| Revogação | Operador fica NOLOGIN/passwordNULL/sem grants de funções, autorização/tmpfs removidos | Segredos efémeros do ensaio, não política comercial de custódia |
| HTTP e perfis | health UP, anónimo401, login ADMINISTRADOR com capacidade administrativa; cria CONSULTA por API e login; consulta de auditoria/alteração da empresa403 | HTTP real, não browser/TLS/exposição pública; sem reabrir auditoria geral |
| Jornada | Empresa/cliente/artigo criados por API com dados @example.test; rascunho e linha persistidos/total 12,30, leitura por CONSULTA, diagnóstico200, exportação CSV e auditoria documental visíveis | Fixtures de referências por SQL no cluster sintético; tipo não classificado/área1 é de teste, não FT homologada; não repete os9 casos M03 |
| Gates actuais | POST emitir recusa400: série sem código AT. POST criar FT governada recusa409: catálogo/revisão indisponível. Não foi fabricado código AT para alcançar assinatura | **Não é novo teste HTTP409 de emissão por assinatura ausente**. Esse percurso é prova reutilizada M03-R1 em 49b1623, fonte 8c0f885a2c36f0eb72d88d32cbbcd146c8e76530803e8924a9cb248caeb4e73b igual à integrada. Preservar distinção e submeter suficiência ao revisor |
| Ausência de efeitos financeiros | A e B com 0 pendentes/financeiros/posições/operações/origens/recebimentos/revisões; nenhum emitido/número/ATCUD/QR/códigoAT/numerador avançado | Não é reconciliação operacional nem activação FinancialCore |
| Backup e restauro | Backup custom 237662 bytes SHA-256 `7a58aa67f8da916a9c58a38082c34266c23d2e11cf3713e994e8924cdcc75e6a`. Restauro single-transaction em B distinto; **43 tabelas e 15 sequences** iguais em contagem/digest/estado | Backup apenas sintético, sem roles globais/passwords/JWT em texto. Mantém BCrypt das contas sintéticas. Não é restauro #38, GOLDEN ou produção |
| Integridade | A e B: 86 FKs validadas, 0 órfãos; validate 29 e arranque de restauro sem migrations novas ou reescrita de dados | Contagens/digests comparados antes de novos logins, que escrevem auditoria legitimamente |
| Recuperação | Comprovativo/UUID consumido preservados, nova CLI no destino com identidade correcta recusa exit1 sem criar utilizador; runtime B autentica admin e lê mesmo rascunho12,30 | Reutilização de bootstrap não é recuperação de admin; nenhuma limpeza/reset do comprovativo |

No snapshot: 1 cliente, 1 artigo, 1 rascunho, 2 utilizadores sintéticos, 8 eventos de auditoria e 29 linhas Flyway. Após login B podem mudar auditoria/último acesso; não se oculta essa escrita legítima na comparação de restauro, feita antes. Roles/HBA/segredos são configurados explicitamente no destino, não supostos transportados pelo dump.

## Tentativas preservadas e resultado

- `20261104-114708-4295c0`, c0968cd: package aprovado; falha ao consultar identidade após pg_isready por socket. A mensagem stderr original dessa falha não foi arquivada pelo primeiro runner; a hipótese de corrida com o servidor transitório de init foi tratada exigindo TCP. Não apresentar causa como observação directa que não existe. Recursos removidos.
- `20261104-114841-042dac`, a635d62: JAR reutilizado por hash; JVM não-root não leu mount600. Log real «Unable to access jarfile». Só o modo do JAR compilado passou a0644, bytes iguais. Recursos removidos. Nenhum defeito da aplicação demonstrado.
- `20261104-114945-709f54`, 244abdd: **47/47 checks aprovados**, sem casos JUnit nem package repetido. Código funcional idêntico em todas as tentativas. Originais de todas as fases mantidos; não somar checks repetidos nem suites M01–M04.

## Arquivo e limites

Arquivo restrito `/home/arosario/tuuli-backups/m05/2026-11-04/`, **54 ficheiros + manifesto geral 9438 bytes**, SHA-256 `b016d9fccaf74b2d19d2b58ca2eded52326914b4ff4d82c6c4878d49d37516b3`. Contém três tentativas, JAR exacto, dump exclusivamente sintético, logs, configuração/identidades sem segredos, controlos e runner exacto. Manifesto final da execução SHA-256 `d8299a487bef35f3cc352e977105fdabc703f059611cef57778eeab7c4268ee2`. Cópias byte a byte e hashes conferidos; originais mantidos em target. Segredos/JWT e ficheiros temporários privados removidos, sem transcript CLI; scanner em artefactos de texto e proveniência do dump, não promessa de eliminação física de memória.

Consulta Docker pós-limpeza: nenhum contentor/rede M05; remoção só por IDs/labels próprios. As imagens oficiais permanecem na cache. Origem fac/facdb, preparação conservada, Ubuntu/GOLDEN, outras worktrees/7ce573f não operadas; preservação sustentada no isolamento, não nova medição dessas bases. Não houve alteração funcional, nova regra fiscal, revisão publicada, SAF-T, assinatura real, activação ou cutover.

Condições comerciais: emitente/regime e identidades reais, roles/HBA/TLS/custódia/backups operacionais, séries/códigos AT legítimos, 2/1/1 e projecções/RC-RG, perfil/exportador SAF-T, assinatura/certificação, F01/autoridade e semântica futura onde reservadas, auditoria final e ordens de instalação/activação. M04 documental não fecha gates. Esta M05 não implementa essas matérias nem promove GOLDEN. [Pedido Claude](REVIEW_HANDOFF.md) pendente; não apresentar o resultado executor como aprovação independente ou piloto comercial pronto.

## M05-R1 — Reservas do parecer e provas delimitadas

Ordem #46/6631045763. Original Claude/sidecar conferidos: SHA-256 `e511c301b4894aeb5869f98776720bdc81fc0b53afc45f797837428117cb4ffb`. O hash `4b54cac3171ecb84627ca0f3d2d1d7baddcdfcb059a43bb76416d62f32014cd8` citado na ordem corresponde à cópia recebida pelo Executivo; não é o original local e não se declara equivalência de bytes. Parecer original preservado, APROVADO COM RESERVAS 0/0/3, O-1–O-6 não condicionantes mantidas.

| Reserva | Resolução delimitada / alcance |
| --- | --- |
| MINOR-001 | Branch/Maven parametrizados, imagens fixadas por digest, obtenção explícita e limpeza manual segura no runbook; ensaio por omissão com build novo a registar após execução limpa |
| MINOR-002 | journey-diagnostic.json mostra podeEmitir=true/bloqueios vazios apesar de emissão400; condição de sinal aberta para mandato próprio. Health UP, respostas HTTP/perfis, logs arquivados, auditoria visível, contagens/digests/Git/manifests são sinais do ensaio; logs sem avaliação automatizada, sem métricas/alertas/tracing/TLS |
| MINOR-003 | M03=MockMvc/test/fixtureSQL/409 assinatura; M05=runtime HTTP/400 emissão sem AT e409 criaçãoFT catálogo vazio. Fonte igual permite inferência da lógica, não demonstração runtime da ausência de assinatura. Decisão de aceitação/ensaio adicional/adiamento reservada à Administração |

Sem alteração do produto, gates, permissões, emissores ou diagnóstico. Uma nova execução não transforma observações O-1–O-6 em garantias adicionais. Originais das 47 verificações e tentativas anteriores preservados; não somar verificações repetidas como casos únicos.
