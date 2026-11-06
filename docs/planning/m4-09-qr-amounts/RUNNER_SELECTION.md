# M4-09 — Selecção dos casos, sem nova execução

Este documento clarifica o comportamento do código ensaiado; não altera runner, teste ou oráculo e não autoriza repetir ensaios.

| Código / comando | Selecção efectiva |
| --- | --- |
| Runner histórico no commit 066a984 | Executou M409-01 a M409-05: cinco casos, sem o filtro acrescentado na R1. O comando histórico deve ser interpretado com esse commit. |
| Runner actual, idêntico a 1528b1f: `python3 docs/planning/m4-09-qr-amounts/run_validation.py` | Fixa internamente `CASE_FILTER = 'M409-06'`, transmite `M409_CASE_FILTER=M409-06` ao Maven e exige exactamente um teste, zero ignorados. Executa só M409-06. |
| Classe JUnit actual, fora do runner, com `M409_CASE_FILTER=M409-06` | O MethodSource selecciona apenas M409-06. Exige todo o ambiente descartável/guards, perfil test e dados sintéticos que a classe valida. |
| Classe JUnit actual, fora do runner, com `M409_CASE_FILTER` ausente ou vazio | O MethodSource selecciona os seis casos disponíveis. Este percurso não foi executado nem autorizado nesta conclusão documental. |

Definir uma variável vazia antes de chamar o runner **não** selecciona os seis: o runner sobrepõe-a. Alterar apenas CASE_FILTER também seria insuficiente, pois a asserção de contagem do runner R1 continua a exigir um teste. Não se recomenda modificar silenciosamente o runner ensaiado para outra selecção.

Uma futura execução conjunta exigirá mandato, ambiente sintético descartável próprio, uma invocação/runner que registe a selecção e a contagem esperada, Git limpo e novas evidências. Não se apresenta um comando dependente de credenciais/cluster ainda não preparados como procedimento executado. Nesta tarefa não foi executado teste algum.

A evidência inicial continua em 20261106-124452-5cbed5 e a R1 em 20261106-144506-2844e5. O nome XML `[1]` não identifica sozinho o caso: correlacionar com selection.json e as capturas `M409-06-*`. Os seis resultados provêm de duas execuções distintas (5 + 1).
