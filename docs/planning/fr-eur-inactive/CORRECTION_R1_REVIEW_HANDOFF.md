# FR/EUR R1 — confirmação incremental Claude Code

Base da revisão anterior `182382c8cec86da367ec876a775d436246f199e9`. Código e testes ensaiados `7505860b136198d4f315f6ddbfe68a81208133a6`, tree `a6dc55b9678633c2b483efe2fb7beb26f4560f64`. Branch `codex/fr-eur-correction`. O delta incremental vai da base revista ao commit documental que introduz este dossier. Parecer anterior e sidecar conferidos. SHA-256 `22877ad47f5d64dbe88733e3d3758d23c1d5786abe878f413bc422f37cf7d6d3`.

| Achado | Correcção e prova a confirmar |
| --- | --- |
| MAJOR-001 | [Proveniência da decisão](CORRECTION_MANDATE.md) com fontes do chat e distinção entre regra e desenho. Fundos nunca recebidos confirmados expressamente pelo operador. Data de efeito pendente. |
| MAJOR-002 | Sucessos dos dois actos na mesma transacção e recusas de negócio/autorização em transacção isolada, com tipos de evento equivalentes aos percursos FT/RC. A prova verifica que uma repetição idêntica conserva o evento original, uma recusa fica registada e rollback não deixa sucesso. |
| MINOR-001 | Oráculos até V32 actualizados. Upgrade parte de uma FR emitida na V31, compara identidade e estado antes/depois, verifica segundo migrate sem alterações e executa a correcção. Inclui FinancialFrIntegrationTests e FinancialQueriesIntegrationTests. |
| MINOR-002 | Antes da correcção o extracto tem factura e quitação. Entre os actos aparece a anulação da quitação e a dívida reaparece, com a posição selada para novos pagamentos. A FR continua emitida. Após o segundo acto aparece a anulação da factura. O PDF mostra os dados consolidados da emissão e acrescenta o estado actual da quitação e a menção de anulação documental quando aplicável. Os três PDFs são inspeccionados com texto extraído e QR descodificado. |
| MINOR-003 | Recusa SQL da mudança directa a ANULADO, reversão de facturação com quitação activa e RESTORE indevido. A tentativa de gravar nova identidade fiscal falha e reverte a transacção. Testa ainda a reversão genérica que geraria CASH_OUT, POST ordinário de anulação, conflito de motivo numa repetição e consulta histórica de outro cliente. Número, ATCUD, QR e projecção foram comparados antes e depois. |

A V32 ficou idêntica neste delta R1. A leitura do extracto continua restrita ao laboratório e a declaração de fundos não recebidos depende do operador autorizado. O plano de execução SQL do extracto continua por medir antes de utilização operacional. FR/EUR inactiva e C6 aberto.

## Execução e arquivo

225 testes aprovados em 12 classes, zero falhas, erros ou ignorados. A primeira tentativa R1 encontrou dois oráculos incompletos: a reversão chegava primeiro à guarda Java e a prova procurava o QR apenas na primeira página. A execução final usa SQL directo e inspecciona todas as páginas. A tentativa inicial permanece nos logs locais.

Arquivo reservado TUULI `FR-EUR-Correccao/2026-11-18/R1/20261118-182428-c2e6c4`. Conferidos 88/88 ficheiros por tamanho e SHA-256 no destino. Manifesto e sidecar conferidos, SHA-256 `4b023d5bcb454e480cc97648de15fa3eb6ac1607d361eaff28a584dbf5971262`. O disposal confirma remoção de contentores, redes e segredo efémero. A pasta R1 preserva o parecer Claude original com o sidecar e a proveniência da decisão. A confirmação dos cinco achados aguarda encaminhamento ao Claude Code.
