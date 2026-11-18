# FR/EUR — identidade fiscal e projecção documental, inactiva

18/11/2026. Executor Codex Desktop. Plano autorizado `36c6aadf45ec4bd54a6c937b9cb8e997f6e92197`; base integrada `e6973910e1652dea85acdc9e0848c7683c34e6d5` (PR #58). Branch/worktree `codex/fr-eur-fiscal-identity` / `.worktrees/fr-eur-fiscal-identity`. Checkout principal e restantes worktrees preservados.

## Código entregue

- Revisão semântica v2 FT/especialização FR, pointer explícito `frImmediate`, validação Java/SQL e associação estável. V1 continua compatível; manifesto produtivo vazio e inalterado.
- Serviço de configuração exclusivamente interno, sob `fr-inactive-lab`; acto projectado exige o mesmo perfil e autorização. Não há endpoint/consumidor operacional novo. O endpoint do ensaio MockMvc existe apenas em src/test.
- Projecção externa FR/revisão/título persistidos antes da assinatura; tipo interno continua FT. Número completo e ATCUD únicos e persistidos. QR D=FR, G=número, H=ATCUD; PDF identifica Fatura-recibo, quitação integral e o mesmo número/QR, incluindo multipágina/reimpressões.
- Quitação integral no documento único, mantendo o vínculo V30 ao FinancialCore: um CASH_IN, residual zero, nenhuma criação artificial de documento financeiro/RC. Datas de efeito e instante técnico continuam distintos. Replay, concorrência e rollback usam as garantias transaccionais existentes, com confirmação dirigida.
- V31 é a única migration nova. Identidade externa durável por código interno entre tabelas; séries/validações próprias sem colisão ou reciclagem; serialização incluindo snapshots REPEATABLE READ. Nenhum backfill, mudança de V1–V30 ou reclassificação inferida. A origem histórica não demonstrada recusa nova utilização como FR; não altera a leitura antiga.

## Preservação do legado e riscos efectivos

I04(b) demonstra a criação e emissão ordinária de uma nova FT imediata sem vínculo: continua D=FT, sem acto FR/quitação. **C6 aberto**: a preservação não significa que o produto assegure quitação nesse caminho. Restringi-lo exige decisão administrativa separada.

A anulação FR não foi implementada; reversões avulsas continuam recusadas. Anulação comercial FT/reimpressão e recebimentos RC são regressões dos percursos existentes, sem decidir temporalidade FT ou regime RC/RG.

A V31 protege códigos/séries e pode recusar reutilizações antes aceites de identidades já reservadas; este é o alcance histórico aprovado, não autorização para limpar dados. Só dados sintéticos foram usados. A configuração demo/produção, bases conservadas e Ubuntu não foram tocados.

## Prova e revisão

Resultados, HEADs ensaiados, falhas preservadas, manifests e limites em VALIDATION.md e DELIVERY.json. Código funcional final `1bbc5fd`; posteriores `84570d6` e `15fd59a` só alteram testes. O commit documental final não recebe retrospectivamente a proveniência dos ensaios.

Revisão **Claude Code pendente** desta implementação. Os pareceres anteriores do núcleo FR e do plano não são aprovação deste código. Pedido em REVIEW_HANDOFF.md, para encaminhamento efectivo ao revisor. Sem push, PR, merge ou activação.

## Fronteira produtiva

Sem assinatura real, emissão positiva pelo JAR, comunicação AT, catálogo publicado, homologação QR/PDF/SAF-T ou certificação. FT/1 e FT/9000100, FRE/TESTFR e códigos AT dos ensaios são sintéticos; não aprovam novas combinações comerciais. Anulação FR, P12, custódia produtiva da assinatura e activação têm gates próprios. Mantêm-se o espaço após `ATCUD:` e bruto/IVA/total no diálogo final como pendências identificadas.

Trabalho fiscal independente, mapeamento SAF-T e decisões indispensáveis em INDEPENDENT_FISCAL_WORK.md. Nenhum exportador/XML implementado.
