# Pedido de revisão independente — podeEmitir

Rever o delta desde 5ec635686a15c19f2603ba26f048bc5ab8c10b26 até ao HEAD final indicado no relatório externo, branch codex/emission-readiness-pr53. Conferir VALIDATION.md, contrato revisto em docs/planning/continuity-2026-11-05 e implementação real.

Prioridades: diagnóstico sem efeitos persistentes, paridade justificada com recusas verificáveis do POST, fiscalRecalculoAviso e diferença de totais, fontes ausentes/ambíguas sem assinatura, consistência JSON/HTML, reavaliação dos três consumidores após gravar, permissões e preservação dos gates. Examinar testes, XML, logs, manifestos, proveniência por commit e limites da API simulada.

Não assumir que 51 testes backend e 11 browser foram executados no mesmo HEAD. Confirmar a equivalência do Java e o delta posterior descrito. A suite não é integral. Emitir parecer com HEAD/tree, achados BLOCKER/MAJOR/MINOR, veredicto e SHA-256 do original. Não alterar bases, activar, publicar ou integrar. Pedido preparado para transmissão pelo PO; revisão ainda não realizada.
