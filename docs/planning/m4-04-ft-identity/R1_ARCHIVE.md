# M4-04 R1 — Preservação fora da worktree

Arquivo externo: `/home/arosario/tuuli-backups/m4-04/2026-11-06/20261106-182822-392888/`.

## Originais preservados

| Grupo | Âmbito / identidade |
| --- | --- |
| evidence-original/ | Cópia integral dos 46 ficheiros da preservação inicial:45 entradas mais ARCHIVE_MANIFEST.json. Inclui logs/XML, PDF/render, capturas, oráculo, runner e suplemento, sem alteração |
| delivery-original/ | Relatório e pedido inicial do executor com os respectivos sidecars,4 ficheiros |
| reviews/ | Parecer original Claude e sidecar,2 ficheiros; digest recalculado 55e89d39dedc2deedb001c9054321f552411c0f35d811cff7f899645b8725d87 igual ao sidecar |
| PRESERVATION_MANIFEST.json | 52 ficheiros únicos dos três grupos, com origem, tamanho e SHA-256; cada cópia conferida byte a byte |
| R1/ | Acréscimos desta correcção, separados dos originais: deliberação, fontes oficiais, metadados e entrega documental. Não altera o manifesto original nem a prova |

**SHA-256 de PRESERVATION_MANIFEST.json: edd3557141928ca789396ca0767b74ad1d1e1df529ccf723da38748b8ce73098.** O manifesto original da execução permanece 3d05cac662416fb48119aa046dc2bb609731c31fe3b7996cbbc9ce0fa96821f8; ARCHIVE_MANIFEST.json inicial permanece 12dd223ed5772556ece09f39946acdfcef46dbf0d22f052c0293e733a3a4be10. Os três digests referem âmbitos diferentes.

O arquivo externo está fora de qualquer worktree, pelo que a remoção da worktree não elimina esta cópia. Isto não demonstra redundância geográfica, durabilidade do suporte ou restauro independente; não se declara tal prova.

## Fontes e delta

R1/sources contém os exemplares N03/N04 reutilizados sem alteração, PDF original do DR e HTML integral da FAQ 4448 obtidos em 06-11-2026, mais retrieval.json. [Metadados versionados](R1_SOURCES.json).

- DR PDF: 4319c5dd00bd766e829bfad231d73e30c0d726f6726bec38d13bf42bd8edf494.
- FAQ HTML: 9d9316b1bdaa4a774e55d7fad421268549a9c03d0d3753c2fb3e7a686a156e56.
- Delta inicial revisto `git diff --binary f2fa467..12c252a`: **b44beab20f96c3f947b7bda674c04848955de6aeed25b0e9324ae1b7c5f6de37**, coincidente com o prefixo referido pelo Claude; preservado em R1/initial-reviewed.patch.

O pacote final acrescenta o patch exacto R1, HEAD/tree e relatórios/sidecars, sem reescrever estes originais. O seu manifesto independente é entregue com o HEAD final; evita-se inserir no próprio commit o hash de um patch que inclui esse documento.

Parecer inicial e relatórios também ficam em `Relatorios-AR2/TUULI`, com hashes iguais aos originais. Entrega R1 com nome próprio; não substituir o relatório inicial nem o parecer. Sem publicação remota, ensaios, SQL ou Docker nesta correcção.
