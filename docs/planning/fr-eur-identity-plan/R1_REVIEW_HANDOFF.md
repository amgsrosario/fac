# Pedido de confirmação incremental Claude — FR/EUR documental R1

Executor: Codex Desktop. Revisor: Claude Code. **Confirmação pendente.** Não repetir a revisão integral nem ensaios anteriores.

## Delta e fontes

Branch `codex/fr-eur-identity-plan`, worktree `/home/arosario/Projetos/fac/.worktrees/fr-eur-identity-plan`. Comparar exclusivamente o HEAD final entregue com `3c30ddb77804bda16028f46e3641a0b4748e8116` (tree `541921f66e33e295c901a42378b484e7441105c0`). O relatório/pedido externo fixa o SHA completo final e o hash do patch; o commit é exclusivamente documental.

Parecer original `2026-11-18_TUULI_FR-EUR_identidade-plano_revisao-independente_Claude.md`, SHA-256 `f2be85ea98f5974d928bd58a14532fbfa440ee38750859fa10c795b422df9ab2`; sidecar conferido, cópias em `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Identidade/Revisao-2026-11-18`. Ler R1.md e a tabela de resolução. Fontes N01 e documentos aprovados anteriores permanecem os mesmos, sem nova interpretação fiscal ou implementação.

## Conferência limitada

1. **MINOR-001:** PLAN fixa um único tipo fiscal externo por código interno em toda a história e entre tabelas, sem confundir tipologia FT com ID interno. Prevê protecção Java/SQL para reutilização/reclassificação, operações concorrentes e histórico comprovado; I07/I13 exigem uma recusa com estado preservado, inclusive quando o código já foi usado noutro suporte documental. Nenhum backfill, inferência ou fabricação de fixture inválida.
2. **MINOR-002:** confirma expressamente manutenção de novas/existentes FT imediatas sem vínculo no caminho legado, C6 aberto e possibilidade de emissão ordinária D=FT sem quitação. A recusa para a nova FR governada não se estende automaticamente ao legado. Restringir novas configurações sem vínculo é decisão administrativa separada, não implementada ou presumida aprovada.
3. Coerência estritamente associada: memória, estado, ponto 2, SOURCES e handoffs sem declarar revisão inicial pendente ou R1 já aprovada; núcleo integrado/inactivo e projecção ainda em preparação. Confirmar delta apenas documental, sem alteração de testes/funcionalidade, manifestação fiscal ou limites. Observações O-1/O-2/O-3 preservadas.

## Entrega

Declarar RESOLVIDO/NÃO RESOLVIDO para cada MINOR, com referências exactas e eventual novo achado material do delta. Veredicto, BLOCKER/MAJOR/MINOR, HEAD/tree realmente revistos e hash SHA-256 do original com sidecar. O fecho documental dos achados não autoriza implementar, homologar, publicar ou activar. Sem testes, SQL, Docker, alterações a bases, código ou Git durante esta confirmação.
