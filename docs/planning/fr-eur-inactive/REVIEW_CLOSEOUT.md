# FR/EUR — dossier de fecho para decisão de integração

Data: 17/11/2026. Executor: Codex Desktop. Revisor independente: Claude Code.

## Resultado e proveniência do parecer

**APPROVE — BLOCKER 0 / MAJOR 0 / MINOR 0.** Aprovação de fidelidade ao mandato e integridade técnica do circuito interno inicialmente inactivo. Revisão exclusivamente de leitura: o revisor leu código e provas arquivadas, sem executar testes, Maven, Docker ou SQL; não verificou runtime ou bases conservadas nem autentica a origem das mensagens administrativas.

Originais em `Relatorios-AR2/TUULI`:

- `2026-11-17_TUULI_FR-EUR_inactivo_revisao-independente_Claude.md` — 14 769 bytes; SHA-256 `46c8904275d9bb17b908a1ee3dfd80f3f1ad4054a8c209a031910a59eff8a9a1`.
- `2026-11-17_TUULI_FR-EUR_inactivo_revisao-independente_Claude.md.sha256` — digest declarado coincidente; SHA-256 do próprio sidecar `b5d69f9a4c75d3edbf5d54a2de7f9143e296b86a80976fd732ab2efd135c6937`.

Cópias byte a byte, sem alterar os originais: `Arquivo-Reservado/FR-EUR-Inactivo/Revisao-2026-11-17`. Manifesto próprio SHA-256 `e5e53236418dcb897f4f5fc8e9ff1df5ccabf40c38071a4accbbfd869465ec00`. O arquivo técnico anterior (185 ficheiros e manifesto `7bb09a00…47a8`) permanece separado e intacto.

## HEADs e delta

| Referência | Identidade e alcance |
| --- | --- |
| Base usada | `f15388d0ac926cc39082869e898d5ef9998059f7`, PR #57; destino proposto `feature/visual-redesign` |
| Código/testes finais ensaiados | `678e3f3bf4fb85835ec2c17437375f56d7bdef3c`; tree `8047edf7d9cde975d2e741920655a4d9604caa30` |
| HEAD principal revisto | `68c85f094b7c2c3427cd5cc3f25e0cd2fb9c5463`; tree `bfb9b352303abf00aa06e4f44bf354c42ff2c719`; último delta só documental |
| Posterior documental conferido pelo revisor | `d1401b78964199897215460e4a99fed6c490b2a1`; tree `b258d0cf40540a385f0843181fed62953f610f3d`; +32 linhas em 5 documentos, sem código |
| Fecho actual | Posterior aos dois HEADs anteriores e exclusivamente documental. Obter HEAD/tree finais do commit de entrega; não atribuir-lhe retroactivamente ensaios ou uma aprovação independente própria |

Branch/worktree: `codex/fr-eur-inactive`, `.worktrees/fr-eur-inactive`. Não houve rebase, squash ou reescrita dos commits. O dossier inclui a sequência integral abaixo, mais o commit documental de fecho que o contém. Na eventual publicação, reconfirmar base/delta e obter autorização administrativa específica para o SHA completo; esta preparação não publica nem integra e não afirma actualidade futura do remoto.

## Cronologia preservada — O-1 e O-2

A implementação e testes foram fixados em **16/11/2026, 17:43–17:57 UTC**, antes da clarificação de **17/11**, que levantou expressamente a suspensão apenas para FR/EUR. A ordem inicial não tratava expressamente a suspensão. Conforme o parecer, a clarificação confirmou o mandato e **regularizou posteriormente o incremento**. Não se afirma que esse levantamento expresso antecedeu a implementação. A transcrição inicial, a clarificação e as datas Git continuam distintas.

```text
abaa9b6b271ff32067fd707f77140df1b0c48419 | 2026-11-16T17:43:39Z | feat(financial): add inactive single-document FR EUR settlement
0fd9aeda4a859903cf433ef604a024a00ceec369 | 2026-11-16T17:45:40Z | fix(financial): require atomic FR completion alongside V22 guards
6e3ec0ea38de8a7087a4f2c3a5f96f1ece7faeac | 2026-11-16T17:47:18Z | test(financial): cover incomplete SQL FR acts and legacy exclusion
504fa345c2da69a45e3c49dea25559ed03ba78f3 | 2026-11-16T17:48:05Z | test(financial): isolate each FR customer historical scope
d3d0ddab382029267834f5c2d111e929269815c5 | 2026-11-16T17:52:47Z | fix(financial): consolidate FR quittance before test signature projection
678e3f3bf4fb85835ec2c17437375f56d7bdef3c | 2026-11-16T17:57:03Z | test(financial): enforce FR authentication and adapt disposable legacy reset
68c85f094b7c2c3427cd5cc3f25e0cd2fb9c5463 | 2026-11-17T16:31:25Z | docs(financial): record inactive FR evidence and independent review handoff
d1401b78964199897215460e4a99fed6c490b2a1 | 2026-11-17T17:51:30Z | docs(financial): record limited suspension lift for FR EUR mandate

```

Proposta de registo administrativo, não publicada: “FR/EUR interna implementada em 16/11; clarificação de 17/11 regularizou o mandato e levantou a suspensão apenas para este incremento. Claude APPROVE 0/0/0 sobre 68c85f0 e delta documental d1401b7 conferido. Integração e activação não realizadas; restantes gates mantidos.”

## Observações e condições preservadas para decisão

São observações **sem condição do parecer**, não novos findings ou autorizações. A execução não alterou funcionalidades para as tratar.

| Observação | Facto e tratamento documental | Reserva para activação futura |
| --- | --- | --- |
| O-1 — Cronologia | Implementação anterior à clarificação; regularização posterior explicitada acima | Não reescrever o histórico nem retrodatá-lo |
| O-2 — HEADs | 68c85f0 e posterior documental d1401b7 separados | Aprovar apenas SHA completo identificado; o fecho documental actual não recebe aprovação antecipada |
| O-3 — Identidade fiscal | IFR sintético FT/imediato fora do catálogo vinculado; V28 recusa imediato nos tipos FT vinculados; ramo V30 abrange qualquer FT imediato. QR mantém D=FT. `/emitir` ordinário imediato continua sem quitação, comportamento pré-existente | Identidade governada e projecção FR, D/QR/PDF/SAF-T precisam de validação/homologação antes de activação; não inferir que o booleano implementa todo o circuito |
| O-4 — Anulação e documento | Vínculo impede reversões avulsas e alterações documentais excepto impresso; anulação FR permanece fail-closed | Acto de anulação próprio e revisão das escritas pós-emissão permitidas antes de habilitação; nenhuma mudança agora |
| O-5 — Perfil experimental | Teste demonstra ausência do bean por omissão; ausência de consumidores foi conferida pelo revisor por inspecção, não por aquele teste | Não activar fr-inactive-lab em configuração operacional/variáveis de ambiente; preservar ausência de consumidores operacionais |
| O-6 — Empresa | Empresa 1 fixa, coerente com o produto actual | Revisitar apenas com suporte multempresa autorizado |
| Fronteira financeira | Um ledger existente, vínculos explícitos e acto FR integral; nenhum RC fiscal artificial ou Pendente novo. O legado mantém a autoridade operacional vigente; provas no core interno não são cutover | Futuro desenho de autoridade única para escrita/leitura/anulação e habilitação integral exige gates próprios; sem dupla autoridade ou cutover parcial |

## Evidência e alcance

61 testes aprovados sobre o código final, XML e proveniência conferidos; provas anteriores FT/RC/consultas/upgrade e controller reutilizadas com HEADs explícitos em VALIDATION.md. O revisor confirmou o arquivo global 185/185 e a distinção entre execuções. Não houve repetição neste fecho. Somente dados sintéticos, PostgreSQL descartável e fonte de assinatura de teste. Nenhuma prova de dinheiro bancário real, assinatura produtiva, emissão positiva pelo JAR ou homologação geral. Package sem testes e recursos descartáveis removidos, conforme evidências existentes.

## Decisão seguinte e gates

Entrega preparada **para decisão administrativa de integração inactiva**. Sem push, PR, merge ou activação nesta missão. Antes de eventual integração/publicação, reconfirmar base e delta, HEAD imutável autorizado, revisão aplicável e checks exigidos. Esta aprovação não habilita o perfil, não publica revisão/configuração fiscal, não activa FinancialCore/RC/RL/FR/1e nem levanta bloqueios restantes. Integração eventual deverá preservar explicitamente esta inactividade.

Mantêm-se ainda a questão do espaço após ATCUD:, a melhoria pré-produtiva de bruto/IVA/total no diálogo final, assinatura real, certificação e os gates fiscais/operacionais. Não foram tomadas decisões de produto adicionais.
