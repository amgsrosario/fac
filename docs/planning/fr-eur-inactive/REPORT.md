# FR/EUR inactivo — entrega para decisão de integração

Data: 17/11/2026. Executor: Codex Desktop. Revisor: Claude Code; parecer APPROVE, 0/0/0, recebido e conferido.

## 1 — Entrega e evidência

Branch `codex/fr-eur-inactive`; worktree isolada `fr-eur-inactive`; base integrada `f15388d0ac926cc39082869e898d5ef9998059f7` (PR #57). Código/testes finais `678e3f3bf4fb85835ec2c17437375f56d7bdef3c`, tree `8047edf7d9cde975d2e741920655a4d9604caa30`. HEAD revisto `68c85f094b7c2c3427cd5cc3f25e0cd2fb9c5463`, tree `bfb9b352303abf00aa06e4f44bf354c42ff2c719`. Posterior documental `d1401b78964199897215460e4a99fed6c490b2a1`, tree `b258d0cf40540a385f0843181fed62953f610f3d`, também conferido pelo revisor. O fecho documental actual é posterior: não se atribuem ensaios ou aprovação antecipada ao seu HEAD.

Implementação: serviço interno FinancialFrIssuer, vínculo imutável V30 no ledger existente, associação comercial nas consultas e data financeira derivada da factura única. Expectativas, contrato e limites em [DESIGN.md](DESIGN.md); resultados e hashes em [VALIDATION.md](VALIDATION.md). As migrations anteriores permanecem intactas.

Clarificação administrativa de 17/11/2026: [mandato confirmado](MANDATE_CLARIFICATION_2026-11-17.md), suspensão levantada apenas para este incremento. FR é implementação em falta do produto aprovado; não se reabrem decisões nem se levantam os restantes bloqueios. Código e evidências anteriores mantidos, sem novos ensaios.

## 2 — Circuito comercial

| Dimensão | Estado e alcance |
| --- | --- |
| Modelo aprovado | Facturação e quitação integral num documento, sem RC fiscal artificial; D-CAT-2A/H1 e mandato arquivado em AUTHORIZATION.md |
| Implementação | FR/EUR interna implementada; CASH_IN único; residual zero; replay/concorrência/rollback; não exposta a consumidores operacionais |
| Prova | 61 testes sobre o código final; provas anteriores FT/RC/consultas/upgrade reutilizadas com HEADs e limites discriminados; package aprovado |
| Revisão independente | Claude APPROVE, 0/0/0: `68c85f0` e delta documental `d1401b7` conferidos; revisão só de leitura, sem execução própria de testes |
| Integração | PR #58 integrado em e6973910e1652dea85acdc9e0848c7683c34e6d5, head b8b0c0f; 4ad1edf documental posterior continua local na branch anterior; sem activação |
| Activação e fiscalidade | Nenhuma; catálogo não publicado; projecção fiscal FR e restantes gates reservados |

O estado “implementada” não significa circuito comercial habilitado nem produto fiscalmente homologado. A impressão/projecção fiscal FR e a anulação FR não foram implementadas por arrasto. Os circuitos operacionais FT/RC permanecem os existentes.

## 3 — Próximo passo concreto

Integração concluída pelo PR #58 em e697391, preservando a inactividade e gates. A ordem posterior autoriza apenas [preparar o plano de identidade/projecção FR](../fr-eur-identity-plan/PLAN.md) e revisão independente. Não implementar antes de mandato específico; anulação em incremento próprio. O dossier mantém o seu contexto histórico de preparação anterior ao merge.

Cronologia preservada: implementação em 16/11, antes da clarificação de 17/11 que levantou expressamente a suspensão e regularizou este incremento. Observações O-1 a O-6 discriminadas no dossier; sem alterações de produto.

## Actualização documental R1 — 18/11/2026

**Ponto 2:** núcleo FR/EUR integrado e inactivo; identidade/projecção fiscal em preparação. O plano recebeu APPROVE WITH CONDITIONS (0/0/2) sobre 3c30ddb; R1 apenas documental, sem testes ou alterações funcionais. Código interno deve manter um único tipo externo em todo o histórico, também entre tabelas. A FT imediata sem vínculo mantém o caminho legado (novas e existentes), com emissão ordinária potencial sem quitação: **C6 aberto**; restrição adicional só por decisão administrativa separada. [Confirmação incremental Claude pendente](../fr-eur-identity-plan/R1_REVIEW_HANDOFF.md).

## Ponto 2 — actualização de implementação local em 18/11/2026

Núcleo V30 integrado pelo PR #58 continua inactivo. O mandato posterior autorizou identidade/projecção FR segundo `36c6aad`, agora implementada **localmente** na branch `codex/fr-eur-fiscal-identity`: V31, identidade externa, série/numeração, QR/PDF e vínculo à quitação única. Código/testes `15fd59a`; 342 casos seleccionados em fases/HEADs discriminados e package aprovado. I04(b) demonstra preservação do legado, C6 aberto. [Entrega/limites](../fr-eur-fiscal-identity/IMPLEMENTATION.md), [prova](../fr-eur-fiscal-identity/VALIDATION.md) e [revisão Claude pendente](../fr-eur-fiscal-identity/REVIEW_HANDOFF.md). A aprovação anterior do núcleo não é aprovação da nova implementação; esta não foi publicada/integrada/activada. Anulação FR, assinatura produtiva, SAF-T/P12 e homologações mantêm gates próprios. Registos anteriores preservados como cronologia.

## Ponto 2 — aprovação independente da identidade e projecção FR/EUR

Claude Code aprovou a implementação R1 com **0 BLOCKER / 0 MAJOR / 0 MINOR** sobre o HEAD `3411dbb5d6a881641445a352d28b2c018f2655a3`, tree `26ad692aadff719e9d326481c9c19caa5eeaf077`. O parecer confirma os dois achados da R1. A execução final aprovou 158 testes em sete classes. A comparação demonstrou que desapareceu a espera adicional entre tipos distintos com o mesmo nome de série, com numeração persistida correcta e guardas de identidade preservadas. [Dossier R1](../fr-eur-fiscal-identity/R1_CORRECTION_HANDOFF.md).

Arquivo reservado em `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Projeccao/2026-11-18/R1`. As execuções `20261118-125206-88e97d` e `20261118-142658-dec609` foram copiadas integralmente. Verificação no destino por tamanho e SHA-256 com 26/26 e 57/57 entradas válidas, respectivamente. Manifestos idênticos aos originais, SHA-256 `e6d01e772ad9dc49029ba15ff923960db72cab7a072bd03418257ee09ebb819c` e `92363c5fec79d735c4a5c5993912e14666612ba50b65e259d2c7a6d0ed1343ef`.

Parecer original `2026-11-18_TUULI_FR-EUR_identidade-implementacao-R1_confirmacao-incremental_Claude.md` e sidecar preservados e conferidos no destino. SHA-256 do parecer `9e6f98728ce37edff8ef164169af3c1c356a78d25248ec1c02c8fdf73adc76b7`. FR/EUR permanece inactiva e **C6 aberto**. A integração da identidade/projecção aguarda decisão administrativa. O presente registo documental é posterior ao HEAD revisto.
