# RUNNER-01 v2 — Evidência das correcções R1

Base revista: `bb8da2e11428531bf74de12198661cd266e8751c`. Código ensaiado: `03880b571954f0e36dd7dfd456f2be92351a7ffc`, tree `4e76ac5439e8396ec4060d7f7e066562afa1908d`. Os cenários B usaram o mesmo runner na worktree de fixtures, commit `15f6385a6be90fca55e29682822945f763c3b1e9` e tree `84c2867b73a108e6f3bda61b998349525e0a6e2d`. Este documento é posterior ao ensaio. As fixtures ficam apenas no ramo separado e no arquivo.

Arquivo reservado: `RUNNER-01/2026-11-20-v2-R1/`. Os sufixos `-opt0` e `-opt1` identificam as execuções sem optimização e com `PYTHONOPTIMIZE=1`. Cada execução tem manifesto, sidecar e os ficheiros referenciados. `summary.json` regista comandos, resultados, códigos de saída e estados por classe.

| Achado / observação | Correcção | Prova |
| --- | --- | --- |
| MAJOR-001 | O resultado inicial é FAIL. O PASS exige igualdade entre o conjunto concluído com prova Maven/Surefire e o conjunto pedido, e zero erros. O manifesto discrimina `completed`, `failed` e `not_executed`. Em caso de SIGINT durante Maven, o runner termina o grupo Maven, regista `KeyboardInterrupt`, limpa os recursos e devolve código 1 depois de escrever manifesto e sidecar. | `interrupted-opt0/1`: SIGINT ao runner depois de Maven escrever «Scanning for projects». Primeira classe `failed`; segunda `not_executed`. `deliberate-failure-opt0/1`: Maven 1, uma falha JUnit. `distinct-sources-opt0/1`: ambas as classes `completed`, Maven 0, hashes distintos conferidos com as fontes. |
| MINOR-001 | O manifesto de ambiguidade regista todos os caminhos e SHA-256 encontrados, sem seleccionar fonte. A origem do wrapper de Docker fica identificada e é copiada para a evidência. | `ambiguous-opt0/1` e fontes `fixtures/src/test/java/runnerfixture/{left,right}/AmbiguousRunnerFixture.java`. `prohibited-opt0/1`: caminho, hash e cópia do simulador; apenas a resposta da consulta de identidade do cluster descartável foi substituída. |
| O-2 | As guardas de branch e worktree têm diagnósticos distintos, antes da inicialização do Docker. | `wrong-branch-opt0/1`, `untracked-opt0/1` e `modified-opt0/1`. O Git confirma o tipo de alteração induzida, todos os pedidos ficam `not_executed` e o comando Docker sentinela não é atingido. |
| O-3 | A opção ineficaz foi retirada. O runner limpa o XML anterior com o nome exacto da classe e recolhe o novo XML da pasta Surefire por omissão. | `command.json`, XML e `result.json` por classe. |
| O-6 | O README exemplifica os valores da missão M03, incluindo `codex/m03-commercial-journey` e `tuuli_m03_`. | `docs/planning/README.md`; as nove cópias históricas conservam os conteúdos da base. |
| Regressão B | Classe inexistente continua recusada sem fonte seleccionada e antes do Docker. | `missing-opt0/1`. |
| Referência SAF-T | Classe completa inalterada, dados e assinatura sintéticos em PostgreSQL descartável. | `reference-opt0`: 7 testes, zero falhas/erros/omissões. Manifesto SHA-256 `f4ca3eef7a3b2fd6e517ba2480832c2be7d9f9bc8231eb15d8db9263738ec88c`. Registo de remoção sem recursos remanescentes. |

A primeira invocação do verificador externo foi interrompida pelo próprio verificador ao comparar o espaço inicial do status Git do cenário modificado. A guarda do runner já tinha recusado correctamente a worktree. A invocação completa ajustou apenas o verificador externo à normalização existente do status. As versões e os resultados parciais do verificador ficam no arquivo R1.

Os 18 resultados B corresponderam ao esperado. O índice do arquivo R1, `ARCHIVE_SHA256.txt`, tem SHA-256 `db09acb61c6899d01a737c20798646c086c53964ac45985f3cdcece099b68e94`. O sidecar e as 196 entradas foram conferidos no destino. O arquivo v2 foi novamente conferido pelo seu índice original.
