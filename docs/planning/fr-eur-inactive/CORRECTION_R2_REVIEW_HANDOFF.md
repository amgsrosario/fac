# Confirmação incremental Claude Code da FR/EUR R2

Base revista `107b593cdafc4631c77c9eebcab0d51843187b9e`. Commit ensaiado `e7ecb881c46f5ab7a17a545157e71060d9dcae51`, tree `ed9f16b5f386e0413c6ffe059026778bb41b0d9e`. O delta de testes termina nesse commit. O delta documental termina no commit que introduz este pedido.

- **MINOR-003R.** A inserção SQL directa de RESTORE usa a operação errada. A fixture emite a FR na mesma transacção disponibilizando uma operação recém-criada, de modo a passar pelas guardas anteriores de consolidação e originação. A asserção exige `Immediate invoice position is sealed`. Após rollback o estado persistido coincide com o inicial.
- **MINOR-004.** As duas classes estendem `ProjectedFrFixture`, que tem configuração e auxiliares, sem métodos de teste. A execução das classes completas por nome, sem selecção de métodos, produziu 22 testes de projecção e 1 teste de upgrade. Todos passaram, sem falhas, erros ou ignorados. O upgrade começa com uma FR emitida na V31.

A execução em PostgreSQL descartável `20261118-184040-397106` está arquivada em `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Correccao/2026-11-18/R2`. Foram conferidos 27/27 ficheiros no destino. Manifesto e sidecar SHA-256 `ccdbd286226562225f417e668bae85b59227a607d967284b2ac7609210b68f33`. Consultar comandos, logs, XML e o registo de remoção dos recursos. O parecer R1 e o seu sidecar estão no mesmo arquivo R2, SHA-256 `d386c6395923397f99e29263eb04e3b78add070bf8c25fcfff837412d7aa7fdb`.

Confirmar os dois achados pelo diff e pelas evidências. A implementação de produção permanece a mesma. FR/EUR inactiva, C6 aberto. Confirmação pendente de encaminhamento para Claude Code.
