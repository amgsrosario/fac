# M4-09 — Pedido de revisão independente Claude Code

Mandato directo do PO de 06/11/2026, AR2-GOV-03. Base integrada 5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07. Branch codex/m4-09-qr-amounts; HEAD final/tree identificados no relatório externo de entrega. HEAD efectivamente ensaiado 066a98490a4db43698ef25d937cd1d16e33cd91e, tree be657782f0a1554717944320fda11ed1122d96e1, limpo antes/depois.

Ler AGENTS.md, MATRIX.md, VALIDATION.md, independent_oracles.py, expected.json, QrAmountsJourneyTests e runner. Conferir:

1. Cobertura prévia reutilizada versus cinco cenários novos: múltiplas linhas, quantidade/preço fraccionários, descontos percentual/em valor e arredondamentos discriminantes. Sem novas regras ou alteração src/main.
2. Independência do oráculo racional, ordem do fecho por linha conforme regra aprovada, passos exactos e expectativa fixada antes dos pedidos. Confrontar o caso05 com as alternativas de soma/arredondamento, sem tomar o código da aplicação como oráculo.
3. Percurso exclusivamente MockMvc com segurança/controlos normais, FT/1,2/1/1,false e FiscalSignatureTestConfiguration de teste existente. Comparação de resposta consolidada, SQL persistido, snapshots e campos monetários I7/I8/N/O, sem generalizar para outros regimes.
4. XML/log: 5 aprovados, zero falhas/erros/ignorados. Git/versões/comandos, manifests e identidade dos blobs entre ensaio e HEAD final. SQL de leitura e preparação sintética não são emissão produtiva; não houve JAR positivo.
5. Isolamento tmpfs/rede/cluster, remoção por IDs/labels e consulta posterior 0/0; preservação de df71e68, origem, demo e outras worktrees. Não executar bases conservadas nem repetir a suite por rotina.

Arquivo reservado e hashes completos em VALIDATION.md/relatório. Os originais e suplementos são distintos. Emitir APPROVE / APPROVE WITH CONDITIONS / REQUEST CHANGES, achados BLOCKER/MAJOR/MINOR com referências, HEAD/tree e SHA-256 do parecer original com sidecar. O PO encaminhará este pedido; não se presume execução independente. Sem push, PR, merge, homologação ou activação.
