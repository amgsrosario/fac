# AVISO-01 v2 — ensaio e paragem

Ordem integral recebida da chefia executiva com a linha de fecho AVISO-01 v2. Base ebc762e6a3bd43fea7f0cf5f4cd7751a0bccf3a7. HEAD ensaiado 5839de607de99fb0410971356369fca17aa68bb3, tree d287d7810831aecbfca5eb4a1e6142908d143154. Este documento é posterior ao ensaio.

| Ponto | Evidência e resultado |
| --- | --- |
| Calendário | O Playwright aprovou a função partilhada. Cobre aviso posterior, prazo de cinco dias úteis, fronteira de 07/12, feriados fixos, Sexta-feira Santa e Corpo de Deus, Páscoa coincidente com domingo, data vazia e igualdade. O calendário exclui feriados municipais e Carnaval. |
| Criação | O caso de teste aprovado cobre o percurso activo de criação, que encaminha para o editor. A captura `01-criacao.png` mostra o aviso reagindo à data de emissão. A inserção no formulário interno de DocumentosView consta do diff. |
| Rascunho | `02-editor.png` mostra o aviso de prestação posterior. O teste combinado falhou a seguir à gravação, à espera de um botão «Guardar alterações» que já não estava presente. |
| Confirmação | O GET paralelo ao diagnóstico consta de DraftDocumentEditor.emitDocument. A prova combinada terminou antes de simular a divergência do GET. Confirmação com aviso, emissão apesar do aviso, igualdade de payload e terceira captura ficam por demonstrar. |
| Ensaio | Build TypeScript/Vite com saída 0. Playwright com Edge nativo, 44 aprovados e dois falhados, saída 1. Logs, JSON e versões arquivados. `git diff --check` passou e `src/` ficou intacto. |

## Condição de paragem

O teste preexistente «new editor saves before diagnosing and emitting» esperava um POST de emissão depois de «Guardar e conferir» e observou zero. A fixture do GET devolveu 2026-11-05 após a gravação do novo documento. A comparação usou o cabeçalho anterior à gravação e apresentou «Datas actualizadas. Emissão: 2026-11-05. Prestação: sem data. Reveja as datas e confirme novamente a emissão.» O resultado invalida a regressão e activa a condição de paragem. O HEAD ensaiado foi preservado, sem tentativa de correcção nem repetição.

A pausa inicial de 23/11 ocorreu porque a emissão relê e bloqueia a versão persistida e podia usar datas que o operador ainda não tinha visto. A v2 prevê releitura antes da confirmação. Entre o GET e o POST mantém-se a janela residual para edições concorrentes, abrangida pela DEC-EMISSAO-CONC-01. [Deliberação de concorrência](https://github.com/amgsrosario/fac/issues/46#issuecomment-7100219523)

Arquivo reservado `AVISO-01/2026-11-23/`. Índice e sidecar conferidos com 9/9 entradas. SHA-256 do índice `5d53f035296bb1e317f5f8977b10242f0ae7fad73f3ea369901baa65c5fe8581`.
