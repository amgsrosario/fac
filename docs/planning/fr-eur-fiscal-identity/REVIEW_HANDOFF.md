# Pedido de revisão independente Claude Code — FR/EUR, identidade/projecção inactiva

Executor: Codex Desktop. Revisor solicitado: Claude Code. Revisão ainda **pendente**; este documento não activa uma sessão externa nem constitui aprovação.

## Fontes e perímetro

- Plano autorizado: `36c6aadf45ec4bd54a6c937b9cb8e997f6e92197`, PLAN/TEST_PLAN/SOURCES em `docs/planning/fr-eur-identity-plan`.
- Base integrada: `e6973910e1652dea85acdc9e0848c7683c34e6d5`, PR #58.
- Worktree: `/home/arosario/Projetos/fac/.worktrees/fr-eur-fiscal-identity`; branch `codex/fr-eur-fiscal-identity`.
- Código funcional fixado em `1bbc5fd`; `84570d6`/`15fd59a` alteram exclusivamente testes. O HEAD exacto, tree, delta e manifesto final constam do DELIVERY.json anexo; não atribuir ensaios retroactivamente ao commit documental final.
- Rever `git diff 36c6aad..<HEAD>` para implementação; `git diff e697391..<HEAD>` inclui ainda a sequência documental do plano. Nenhuma parte da worktree de proveniência desconhecida foi reutilizada.

## Perguntas da revisão

1. Fidelidade: tipo interno FT com especialização governada e identidade externa FR explícita; documento único com quitação, nunca RC fiscal artificial. Não inferir por booleano.
2. V31: compatibilidade V25/V26/V27/V28/V30; protecção independente Java/PostgreSQL INSERT/UPDATE/UPSERT; unicidade histórica do código entre tabelas, incluindo eliminação/recriação; não inferir históricos incompletos. Avaliar locks/tombstones e snapshots REPEATABLE READ.
3. Séries: número persistido, validação AT sintética, ausência de colisões/reciclagem; replay/concurrency não consomem outro número.
4. Identidade consolidada antes da assinatura; QR D/G/H e PDF real (título/quitação/identificação/ATCUD multipágina) vêm do snapshot; reimpressão sem novo recebimento.
5. Dinheiro: original/recebido/afectado, residual zero, um CASH_IN, datas de efeito/técnicas e documento de origem; rollback de falhas esperadas e commit diferido. Nenhum novo ledger/autoridade operacional.
6. I04(b) demonstra exactamente a preservação do legado FT imediata sem vínculo, D=FT e sem quitação: **C6 continua aberto**. Não aceitar a prova como resolução ou aptidão comercial.
7. Isolamento: perfil `fr-inactive-lab`, ausência dos beans por omissão, controlador MockMvc exclusivamente src/test, manifesto produtivo vazio, sem assinatura real/endpoint operacional. Anulação FR não implementada; reversão avulsa recusada.
8. Evidências: distinguir fases/HEADs, falhas de desenvolvimento e duplicado interrompido; não somar execuções como testes únicos. Conferir XML/logs/proveniência/manifestações e remoção dos recursos.
9. SAF-T permanece mapa documental: sem XML, perfil escolhido, HashControl fictício ou PaymentMechanism inferido. Manter P12 e gates materiais.

Emitir parecer com commit/tree revistos, achados BLOCKER/MAJOR/MINOR, riscos efectivos, limites e SHA-256 do original/sidecar. Não publicar, integrar, activar ou tocar em bases conservadas. O executor não dispõe nesta sessão de canal Claude; o pedido fica pronto para encaminhamento pelo utilizador, sem simular revisão independente.
