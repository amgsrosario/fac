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

## Diagnóstico R1 anterior à correcção

| Teste | Classificação | Pedido em falta e causa |
| --- | --- | --- |
| `new editor saves before diagnosing and emitting` | C, comportamento novo | O `POST /api/documentos-comerciais/9/emitir` não ocorreu. A gravação já tinha hidratado o ecrã com o documento devolvido por `saveDraft` (DraftDocumentEditor.tsx:649–653). A comparação em DraftDocumentEditor.tsx:707 usou a variável `header` capturada antes da gravação. A diferença em relação ao GET da linha 702 entrou indevidamente no ramo que termina na linha 717. O mock já devolvia o documento em emission-readiness.spec.ts:43. A asserção preexistente na linha 167 observou 0 em vez de 1. |
| `fresh service date requests renewed confirmation and does not change the issuance payload` | I, sincronização da prova nova | O PUT do cabeçalho concluiu e o mock registou a data na linha 260. O teste aguardou «Guardar alterações» na linha 261, mas o botão só é renderizado com `showSaveDraftAction` (DraftDocumentEditor.tsx:831) e desaparece após guardar. A simulação de alteração concorrente e os pedidos de releitura e emissão ainda não tinham ocorrido. |

As referências de linha acima são do HEAD ensaiado da primeira execução, `5839de607de99fb0410971356369fca17aa68bb3`.

## Resultado R1

HEAD ensaiado `617b239edef7f5633282dd927518b3229381fbad`, tree `21a8d4326c484eb2580136005a1a2b5cff813b9c`. A comparação usa o documento persistido que hidratou o editor após guardar, ou o rascunho que já estava carregado. O GET continua em paralelo com o diagnóstico. Uma mudança posterior à versão apresentada actualiza as datas e pede nova confirmação. A requisição de emissão mantém o mesmo payload.

| Prova | Resultado |
| --- | --- |
| Fluxo preexistente | O cenário de guardar, diagnosticar, confirmar e emitir voltou a passar com um POST. O diff `testes-r1.diff` só ajusta a espera do teste novo. Na comparação com a base, `testes-base.diff` identifica as extensões do mock e os testes novos. Os corpos dos testes preexistentes foram comparados e coincidem. |
| GET concorrente | O teste novo confirmou o PUT, simulou a alteração da prestação de 07/11 para 20/10 e verificou o aviso derivado do GET fresco. O editor mostrou a data fresca e não houve POST na primeira tentativa. A segunda tentativa mostrou o aviso e a nota de imutabilidade. O POST foi igual ao do caso sem aviso e o editor apresentou a emissão concluída. |
| Evidência visual | `01-criacao.png` é a captura do percurso activo de criação. `02-editor.png` mostra o aviso junto ao campo. A caixa nativa fica documentada pelo seu texto capturado no evento Playwright, em `03-confirmacao-mensagem.txt`, e pela transcrição visual rotulada `03-confirmacao-transcricao.png`. A captura directa da janela nativa ficou indisponível nesta sessão. |
| Execução | Build tsc/Vite com saída 0. Suite integral Playwright com Edge nativo, 46 aprovados, zero falhas, instáveis ou omissões, saída 0. Node 24.19.0, npm 11.17.0, Playwright 1.63.0 e Edge 157.0.4341.60. `versions.log` identifica os comandos, HEAD e tree. |

O arquivo R1 em `AVISO-01/2026-11-23-R1/` tem 11/11 entradas conferidas. SHA-256 do índice `6e9b23ff1e93ae47e85083df96268bab4e19f31518fce74fca06586e3ac58532`, com sidecar. As 9 entradas do ensaio falhado foram reconferidas no arquivo original.

## R2 — cobertura de todos os consumidores de emissão

O inventário `git grep -n '/emitir' -- frontend/src` identificou três chamadas de emissão comercial. As linhas abaixo referem-se ao HEAD ensaiado `0d23ede11897ad2c4ed4f1955a4a5d8593afd0ce`, tree `662b4ade779f6cfe6ec000c367bdf929029e0344`.

| Caminho | Prova |
| --- | --- |
| `DocumentosView.emitDocument`, vista antiga, POST na linha 366 | MINOR-001. GET fresco em paralelo com o diagnóstico. O teste legado exige uma confirmação, texto de referência integral sem aviso e POST de referência. Com data alterada no GET, exige as datas frescas, aviso, nota de imutabilidade, o mesmo POST e estado Emitido. |
| `DocumentsView.emitir`, lista, POST na linha 486 | MINOR-001. Mesma prova, incluindo espera pelo carregamento da selecção antes de simular a alteração. |
| `DraftDocumentEditor.emitDocument`, editor, POST na linha 730 | Regressão R1. Confirma GET fresco e actualização do editor antes de nova confirmação, emissão com aviso e igualdade do POST com o percurso sem aviso. |
| O-1, calendário | Casos adicionais aprovados para Sexta-feira Santa e Corpo de Deus em 2025 e 2027, início num sábado, início no feriado fixo de 1/12 e transição de 31/12/2026 para 08/01/2027 e 11/01/2027. |
| O-2, criação na vista antiga | O harness monta o formulário interno de `DocumentosView` aberto e verifica o aviso e o seu desaparecimento ao limpar a data. A navegação activa de produção para criação mantém o encaminhamento para o editor. |
| O-4, apresentação partilhada | `ServiceDateWarningMessage` substitui as duas funções de renderização locais. Os três consumidores de emissão partilham a composição do texto de aviso, baseada na mesma função de calendário. |

A janela residual entre a leitura fresca e o POST existe nos três caminhos. A EMISSAO-CONC-01 terá de os cobrir a todos. [DEC-EMISSAO-CONC-01](https://github.com/amgsrosario/fac/issues/46#issuecomment-7100219523)

A primeira execução R2 em `1167343` passou 48 testes e falhou dois casos acrescentados nesta R2. Na lista, o teste alterava a fixture antes de a selecção aparecer. Na vista antiga, a emissão completava-se mas o teste procurava uma frase de sucesso diferente. O delta até ao HEAD ensaiado corrige a sincronização e verifica o estado Emitido. O conteúdo preexistente de `emission-readiness.spec.ts` permanece integralmente no início do ficheiro, byte a byte. A única alteração no harness adiciona a montagem do formulário antigo para a prova O-2.

Build tsc/Vite com saída 0. Suite integral Edge nativo com 50 testes aprovados, zero falhas, instáveis ou omissões, saída 0. Node 24.19.0, npm 11.17.0, Playwright 1.63.0 e Edge 157.0.4341.60. Os dois ensaios registam comandos, HEAD, tree e versões. As capturas de criação e editor e a transcrição visual da confirmação nativa estão junto dos logs.

Arquivo `AVISO-01/2026-11-23-R2/`, 21/21 entradas conferidas. SHA-256 do índice `58d05cb8aed2b272223b2320839837cec0e297670479e76fc4e1f2b590103dd3`, com sidecar. Os arquivos original e R1 foram reconferidos em 9/9 e 11/11 entradas.
