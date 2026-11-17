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
