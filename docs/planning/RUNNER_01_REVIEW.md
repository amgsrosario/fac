# RUNNER-01 v2 — pedido de revisão independente

**Destinatário:** Claude Code, por intermédio da Administração AR2. Rever o delta da branch `codex/evidence-runner` desde `8dc6fe80f0e072b876f203db4ff1a174d6615792` (merge do PR #63). O runner ensaiado está em `424a65e973a9c82428a650fd954cdaa29166d739`, tree `0dae232fba25f70fd9b8f50a000df85098a0c332`. Confirmar o HEAD final local comunicado no relatório do executor. O delta posterior ao ensaio é documental.

**Acesso:** worktree `/home/arosario/Projetos/fac/.worktrees/evidence-runner`. As fixtures deliberadas estão na worktree separada `/home/arosario/Projetos/fac/.worktrees/evidence-runner-fixtures`, HEAD `067183760bdb73751661b60c5dd7f6a160b1faa3`, tree `49f5050d19954ce7e5e3c222807bb4ebda580560`. O código do runner é idêntico nas duas árvores. Os artefactos de ensaio estão em `Arquivo-Reservado/RUNNER-01/2026-11-20-v2/`, sob a pasta de relatórios TUULI. Conferir o sidecar do índice do arquivo e os manifestos por execução.

| Ponto | O que rever | Evidência |
| --- | --- | --- |
| A.1 | Argumentos obrigatórios de missão, branch, classes, label e prefixo, em `main` | Argumentos nos manifestos e comandos em `summary.json` |
| A.2 | Guardas explícitas e diagnóstico no runner, incluindo estado Git, cluster, mounts, portas, remoção e detecção dos segredos efémeros no arquivo | Manifestos negativos; `isolation.json`, `disposal.json` e identidade Git da execução de referência |
| A.3 | FAIL e código não nulo perante falha; PASS exige código zero e prova Surefire de cada classe | `deliberate-failure-opt0/1` e `distinct-sources-opt0/1`, logs e resultados por classe |
| A.4 | Origem Java versionada e única, SHA-256 e identidade por classe | `distinct-sources-opt0/1`, `missing-opt0/1` e `ambiguous-opt0/1`; cópias Java e hashes |
| A.5 | Executável, versão Python, optimização e versões das dependências do validador | Campo `runtime` de cada manifesto e log SAF-T |
| A.6 | Imagens fixas sem pull implícito, PostgreSQL em tmpfs e rede interna, proxy loopback, segredo temporário, recusa de cluster e remoção por identidade/label | `reference-opt0/isolation.json` e `disposal.json`; `prohibited-opt0/1` com simulação só da resposta à consulta de identidade |
| A.7 | Nove fontes históricas preservadas e reserva SAF-T R1 registada | `docs/planning/README.md`; comparar os caminhos históricos com a base |
| B | Seis cenários com os níveis 0 e 1 | `summary.json`, script dos cenários, manifestos e sidecars. Nos cenários de pré-validação, o comando Docker sentinela é `/bin/false`, e não se chega à inicialização Docker |
| Referência | Executar a classe SAF-T sem alterações ao produto nem aos testes | `reference-opt0`, classe `ProjectedFrSaftFtExportTests`, 7 testes aprovados, sem falhas, erros ou omissões; manifesto SHA-256 `4243536b5f2d308ae26d336418a0d1637f11dc27449d3a01a422a1e1a3d5497d` |

O ensaio de cluster proibido usa recursos descartáveis reais e substitui apenas o identificador devolvido pelo comando de leitura. Não consulta os clusters conservados. A prova da classe deliberadamente falhada é um teste JUnit executado por Maven. A prova de fontes distintas usa duas classes JUnit próprias, sem impacto no produto. A optimização dos cenários B aplica-se ao processo do runner; o script externo de conferência é executado sem optimização.

Rever apenas a infraestrutura de evidência e a fidelidade das provas. A R2 do SAF-T conserva o seu mandato próprio. PR pendente de parecer e merge reservado à Administração.

## Resultado e integridade

Os 12 resultados B corresponderam ao esperado: cinco recusas e um PASS em cada modo. A classe de falha devolveu código Maven 1 e uma falha JUnit nos dois modos. Na prova positiva, ambas as classes passaram e os SHA-256 foram comparados com os das respectivas fontes. O índice `ARCHIVE_SHA256.txt` tem SHA-256 `04691488806117fda4b938dbc0dd6dd0d2db0271362e2e15648817a0983b3bad`. As 133 entradas foram verificadas no destino.
