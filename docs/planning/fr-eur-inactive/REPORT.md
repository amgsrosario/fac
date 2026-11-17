# FR/EUR inactivo — entrega local para revisão

Data: 17/11/2026. Executor: Codex Desktop. Revisor previsto: Claude Code, ainda pendente.

## 1 — Entrega e evidência

Branch `codex/fr-eur-inactive`; worktree isolada `fr-eur-inactive`; base integrada `f15388d0ac926cc39082869e898d5ef9998059f7` (PR #57). Código/testes finais `678e3f3bf4fb85835ec2c17437375f56d7bdef3c`, tree `8047edf7d9cde975d2e741920655a4d9604caa30`. O commit documental de entrega será posterior; obter o seu HEAD com `git rev-parse HEAD`, sem atribuir-lhe retroactivamente os ensaios.

Implementação: serviço interno FinancialFrIssuer, vínculo imutável V30 no ledger existente, associação comercial nas consultas e data financeira derivada da factura única. Expectativas, contrato e limites em [DESIGN.md](DESIGN.md); resultados e hashes em [VALIDATION.md](VALIDATION.md). As migrations anteriores permanecem intactas.

## 2 — Circuito comercial

| Dimensão | Estado e alcance |
| --- | --- |
| Modelo aprovado | Facturação e quitação integral num documento, sem RC fiscal artificial; D-CAT-2A/H1 e mandato arquivado em AUTHORIZATION.md |
| Implementação | FR/EUR interna implementada; CASH_IN único; residual zero; replay/concorrência/rollback; não exposta a consumidores operacionais |
| Prova | 61 testes sobre o código final; provas anteriores FT/RC/consultas/upgrade reutilizadas com HEADs e limites discriminados; package aprovado |
| Revisão independente | Pendente Claude; pedido técnico e de fidelidade ao modelo pronto, sem auto-aprovação |
| Integração | Nenhuma; commits locais; sem push/PR/merge |
| Activação e fiscalidade | Nenhuma; catálogo não publicado; projecção fiscal FR e restantes gates reservados |

O estado “implementada” não significa circuito comercial habilitado nem produto fiscalmente homologado. A impressão/projecção fiscal FR e a anulação FR não foram implementadas por arrasto. Os circuitos operacionais FT/RC permanecem os existentes.

## 3 — Próximo passo concreto

Transmitir ao Claude o pedido [REVIEW_HANDOFF.md](REVIEW_HANDOFF.md), com acesso à worktree, autorização, patch e arquivo. Só depois tratar os achados dentro do mandato e devolver à Administração para os gates próprios. Não iniciar outro incremento.
