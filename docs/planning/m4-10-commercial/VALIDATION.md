# M4-10 — Prova parcial comercial

Mandato #46/6661589044. Base integrada PR #53, 5ec635686a15c19f2603ba26f048bc5ab8c10b26. Branch codex/m4-10-commercial-reprint-pr53. HEAD ensaiado `9954ff56ff161740ff54da2e4c26921388b6bbb5`, tree `8111e62afc1a6b61abd4f9f57cb81119005c8389`, limpo antes/depois. Um caso JUnit aprovado, zero falhas/erros/ignorados, execução `20261105-165644-e46fe3`.

O teste usa a preparação FT/1 com fonte sintética existente de src/test para criar o documento emitido e o payload inicial. Reimprime pela rota real GET /pdf, anula por POST /anular e exige estado ANULADO persistido. O QR descodificado do PDF e a mensagem inspeccionada coincidem com o payload original nas três impressões (emitido,anulado,anulado repetido). O texto ANULADO está presente. A fonte ficou indisponível depois da emissão inicial e o spy não teve interacções nas reimpressões.

Cada impressão compara todas as tabelas públicas, excepto auditoria_evento, antes/depois, incluindo documento/linhas/posições/numeração e configurações. Exige exactamente um evento de auditoria adicional por PDF e três DOCUMENTO_PDF_GERADO no fim. A anulação e as suas alterações documentais/financeiras são uma fase separada, registada em emitted.json/cancelled.json/after-reprints.json. O teste não redefine a temporalidade financeira.

Evidência em `/home/arosario/tuuli-backups/m4-10/2026-11-05/20261105-165644-e46fe3`, 22 entradas conferidas por tamanho/hash, manifesto `d2f92c3d9f6c098cd079d8e96de4d90937e8cb14b5925dffc2675bc58013c39a`. PDFs e snapshots preservados. PostgreSQL 16.3 novo/tmpfs, conta e dados sintéticos, sem snapshots/mounts conservados. Rede interna e proxy apenas loopback. IDs exactos e label M410-commercial em isolation.json/disposal.json, contentores/redes próprios removidos e ficheiro secreto retirado. Maven3.9.16/JDK 21.0.12.1 conforme versions.json.

Comando `python3 docs/planning/m4-10-commercial/run_validation.py`; Maven selecciona apenas CommercialReprintJourneyTests com perfiltest. Reutiliza o teste de preparação M4-02, sem repetir a sua suite. A primeira execução nesta base passou; a execução final reforçou a comparação de todas as tabelas e voltou a passar. Não somar execuções como casos únicos. Os artefactos de ambas ficaram emtarget, o arquivo acima corresponde à execução final.

Alcance parcial do documento comercial. A prova não abrangePDF financeiro, assinatura real, homologação QR/PDF, impressão física ou todos os históricos sempayload. O método comercial validateQrConsolidado continua a recusar payload ausente, com evidência anterior de código/testes reutilizada; não foi criado histórico por inferência. A revisão independente deve avaliar essa delimitação.

## Conclusão documental — 2026-11-05

Claude Code aprovou a prova parcial comercial: 0 BLOCKER, 0 MAJOR, 0 MINOR, no HEAD `217e89fe6ef2f60cb473fd3466867fee7af9b9ca`, tree `18e3b720db4c37744c54f6238c0124e89b10aa14`. Original `2026-11-05_TUULI_M4-10_revisao-independente_Claude.md`, SHA-256 `ba7b8bd5acd76ecc0ca28d35a48af8c9108094d18420dcebc462ae52d40ce36f`, conferido com o sidecar; ambos preservados no arquivo de relatórios e em tuuli-backups/reviews/2026-11-05. A aprovação não autoriza publicação nem integração.

O-1: «percurso real» significa controladores, segurança e serviços via HTTP MockMvc, com fonte de assinatura sintética na emissão. Não foi executado o JAR nem demonstrada emissão produtiva. O-2: a primeira execução 20261105-165450-7ef89b foi agora copiada integralmente para tuuli-backups/m4-10/2026-11-05, com COPY_MANIFEST.json; não representa um novo ensaio. O-3: comparam-se linhas das tabelas; não se demonstra o estado de todas as sequências PostgreSQL. O numerador da série é coluna e está coberto. O-4: nomes internos M03 são herdados pelo runner, mas a label de isolamento é M410-commercial. Não alterado o runner ensaiado. O-5: corrigida a legibilidade do registo corrente, sem alterar resultados.

Permanecem pendentes a linha M4-10 completa, o PDF financeiro, assinatura real, impressão física e homologação QR/PDF. Entrega local preparada para decisão de publicação; sem push, PR, merge ou activação.
