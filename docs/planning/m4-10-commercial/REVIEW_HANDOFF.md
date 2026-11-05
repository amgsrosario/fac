# M4-10 — Pedido de revisão independente

Rever a branch `codex/m4-10-commercial-reprint-pr53`, desde `5ec635686a15c19f2603ba26f048bc5ab8c10b26` até ao HEAD final identificado no relatório externo. Conferir CommercialReprintJourneyTests, runner e VALIDATION.md.

A fixture prepara a emissão sintética; o estado ANULADO deve resultar do POST real. Verificar igualdade do payload persistido e do QR descodificado nos PDFs, texto ANULADO, três eventos de auditoria e ausência de outras escritas durante as reimpressões. Os efeitos da anulação são permitidos e conferidos separadamente. Examinar o spy sem interacção na reimpressão e a fonte indisponível após emissão.

Conferir XML, logs, manifestos, snapshots, isolamento e descarte. Emitir parecer com HEAD/tree, achados, limites e SHA-256 do original. O perímetro é parcial/comercial; a homologação QR/PDF permanece reservada. Pedido preparado para encaminhamento pelo PO. Não existe aprovação independente desta entrega.
