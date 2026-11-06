# podeEmitir — implementação e validação

## Mandato e contrato

Ordem #46/6661589044, pontos 2 e 3. Base integrada PR #53: `5ec635686a15c19f2603ba26f048bc5ab8c10b26`. Branch `codex/emission-readiness-pr53`.

O diagnóstico JSON/HTML calcula impedimentos, avisos, total apresentado, total projectado e confirmação de recálculo sem escrever entidades, reservar numeração ou chamar a assinatura. As leituras usam transacção read-only REPEATABLE READ. A elegibilidade é informativa e não substitui os controlos transaccionais do POST de emissão, que permanece inalterado.

EmissionReadinessService reutiliza MotorFiscalService e validadores conhecidos de identidade QR. Discrimina estado, linhas, cronologia, moeda, série/código AT, QR desactivado, ausência/ambiguidade de fontes, empresa/identidade, retenção, cálculo e total. CALCULO_FISCAL_INVALIDO mantém a mensagem concreta. Não cria um gate novo de catálogo.

A confirmação respeita fiscalRecalculoAviso persistido e o cálculo legado usado pelo POST. Uma diferença entre totais é mostrada ao operador; isoladamente não cria confirmação adicional obrigatória. Não há assinatura nem reserva de números durante o diagnóstico. A contagem de fontes não demonstra disponibilidade dinâmica de chave, certificado ou assinatura por documento: essa validação continua no POST.

Os três consumidores são DocumentosView legado, DocumentsView V2 e DraftDocumentEditor. Reavaliam antes de emitir. O editor grava antes de obter nova avaliação; alterações por gravar usam «Guardar e conferir». Confirmação explícita envia recalculoConfirmado. Um diagnóstico desconhecido não equivale a autorização. A interface mostra impedimentos/avisos/diferença de totais e fecha a sobreposição de detalhe no percurso legado. A protecção inicial contra respostas obsoletas estava limitada a DocumentsView.loadDetail; a R1 abaixo delimita as protecções acrescentadas.

## Evidência da entrega inicial (1814d86, preservada)

- Backend: execução `20261105-170018-92c768`, HEAD `5327efebd637edce0761457df829ca1830732d7e`, tree `3682c3f8afb2996f073a33f5f58d42c97001a091`, estado limpo antes/depois. 51 testes: EmissionReadinessServiceTests 3, EmissionReadinessJourneyTests 1, DocumentoComercialControllerTests 46, GovernedFtPositiveTests 1. Todos aprovados; zero falhas, erros ou ignorados. Manifesto original SHA-256 `a0ac5030ab5f3f2cbb0d3e6569d01d01c83b9616d356e6241881a8138ea3bae7`.
- Jornada API: leituras persistidas antes/depois do diagnóstico, ausência de assinatura/escritas, total projectado, confirmação persistida/legada, avisos, recusas AT/QR/retenção/cálculo e emissão sintética após confirmação. Não representa uma suite integral nem assinatura produtiva.
- Browser: HEAD `48b7edabd92f1ade1b7e22daabda581ab698b234`, tree `b953e885a90a2b51fb3364af1f3e1af5bd3c92f2`. 11/11 aprovados em browser-release.log: sete casos novos dos três consumidores e quatro regressões fiscal-riva. API simulada; verificam pedidos e confirmação, não integração browser/backend real.
- TypeScript e build frontend aprovados no mesmo HEAD 48b7eda, logs typescript-final.log/frontend-build-final.log. Package backend aprovado em `81f897da1eb35b787037605769187fa90c52c254`, estado limpo documentado; `mvn -o -B -DskipTests package` não executa testes. Java não mudou desde 5327efe. Alterações posteriores foram testes browser e rótulo do editor, não recompilação backend ensaiada.
- Comando backend: `python3 docs/planning/emission-readiness/run_validation.py`. Browser: Node 22.16.0, Playwright 1.63.0, testes emission-readiness.spec.ts e fiscal-riva.spec.ts, Chromium existente. Não foram instaladas dependências. Dependências frontend reutilizadas de outra worktree; avisos de fontes PrimeIcons fora da allowlist e bundle grande não invalidaram os testes, mas não demonstram fidelidade visual das fontes.

PostgreSQL 16.3 novo, tmpfs, rede interna e proxy loopback; dados e contas sintéticos. isolation.json/disposal.json identificam recursos, ausência de volumes conservados e remoção de contentores/redes/segredo temporário. O runner reutiliza nomes internos M03, com label própria Emission-readiness. Origem, demo, GOLDEN e bases conservadas não são alvos.

A primeira tentativa backend desta base foi interrompida por estado Git alterado por artefactos browser; a execução final acima foi limpa. Tentativas browser anteriores detectaram sobreposição do detalhe e expectativas/fixtures antigas; foram preservadas e corrigidas, não somadas aos casos finais. A versão Java ensaiada coincide com a final; o delta posterior é verificável por Git. Os logs originais não foram reconstruídos.

Arquivo reservado: `/home/arosario/tuuli-backups/emission-readiness/2026-11-05/`. Manifesto suplementar e relatório externo identificam ficheiros, tamanhos e hashes; os manifestos originais permanecem intactos.

## Limites e revisão

Sem migration, alteração do POST, manifesto fiscal, assinatura real, activação ou publicação. FinancialCore continua inactivo e o legado mantém autoridade operacional. Não demonstra disponibilidade dinâmica de fornecedor, autorização fiscal, integração produtiva ou todos os erros possíveis de persistência. A revisão da entrega inicial pediu correcções (1 MAJOR e 2 MINOR). A confirmação incremental R1 foi posteriormente aprovada pelo Claude, conforme o registo de fecho abaixo; não existe auto-aprovação.

## R1 — correcção dos três achados, concluída documentalmente em 2026-11-06

Parecer original `2026-11-05_TUULI_podeEmitir_revisao-independente_Claude.md`, SHA-256 `ff8142b6757b57e5551d699b5f5462331eb4373df550b911870c7ca3273a0d61`, igual ao sidecar. Ambos preservados sem alteração na pasta de relatórios e em tuuli-backups/reviews/2026-11-05. Objecto anterior: HEAD 1814d86a19f097a6c0d5d2dbed61799253c6bdcb. REQUEST CHANGES: 0 BLOCKER, 1 MAJOR, 2 MINOR.

| Achado | Correcção e prova dirigida |
| --- | --- |
| MAJOR-001 | Se a gravação activar um novo aviso de recálculo, o editor grava e reavalia, mostra aviso e valores e pára sem diálogo nem POST de emissão. Só nova acção explícita prossegue. As regressões emission-readiness e fiscal-riva verificam ausência de POST/diálogo no primeiro clique, total actualizado e confirmação no segundo. Um aviso já conhecido continua a permitir confirmação; não se cria bloqueio permanente. |
| MINOR-001 | Fora de RASCUNHO, o serviço devolve apenas ESTADO_INCOMPATIVEL, sem cálculo projectado, avisos ou confirmação de recálculo. Os dois consumidores V2 ocultam mensagens de emissão fora do rascunho; o legado já restringia o painel a rascunhos. Dois casos unitários EMITIDO/ANULADO verificam ausência de interacções com motor, fontes e repositórios. Jornada JSON/HTML e browser de emitido complementam a prova. |
| MINOR-002 | Cobertura ampliada conforme inventário abaixo, proveniência browser com HEAD/tree/status antes/depois, comando e código de saída. Rectificado o alcance das provas iniciais e da protecção de respostas obsoletas. |

### Execuções R1, sem somar repetições

- Backend dirigido: `20261105-183823-4de55b`, HEAD `8b5e341bf00f811418699a26db3c4e47d6efd1a3`, tree `22de6b5bfd957a1b6ea732708dabf2250093c575`, limpo antes/depois. EmissionReadinessServiceTests: 13; EmissionReadinessJourneyTests: 1. Total **14 aprovados, 0 falhas/erros/ignorados**, códigos de saída 0. Comando: `python3 docs/planning/emission-readiness/run_validation.py EmissionReadinessServiceTests EmissionReadinessJourneyTests`. Manifesto original `309ad691ea04bbcd533f0197c83d6b8a9accd9fe6016dd71db7868a3dc6dabf1`.
- Browser final: HEAD `a431f81378071f83cea05f63037467697df3a318`, tree `c03d00b50300a59a0e8fa28e705de1ce36ef9dab`, limpo antes/depois. **26/26 aprovados**: 22 emission-readiness e 4 fiscal-riva. `target/r1-browser-a431f81` contém comandos, logs, resultados e identidades Git. Testes, TypeScript e build com código de saída 0. Versões recolhidas depois da execução, identificadas como tal; não se reconstruíram capturas prévias.
- Package: mesmo HEAD a431f81, limpo antes/depois, `mvn -o -B -DskipTests package`, código 0 e BUILD SUCCESS, em r1-package-a431f81. Não executa testes.
- A primeira tentativa browser foi impedida no arranque do servidor pelo sandbox, código 1, e preservada em r1-browser-8b5e341. A repetição autorizada nesse HEAD passou 24 testes; após acrescentar novos casos/protecções, a execução final passou 26. Não são 50 casos únicos. Java não mudou entre 8b5e341 e a431f81; não se repetiram os 14 testes sem necessidade.
- PostgreSQL descartável 16.3, dados e contas sintéticos. isolation/disposal registam cluster próprio, tmpfs, rede interna, proxy loopback, remoção de contentores/redes e segredo temporário. Não se reutilizaram bases conservadas.
- Arquivo R1: `/home/arosario/tuuli-backups/emission-readiness/2026-11-05/R1/`. 53 ficheiros copiados e conferidos; ARCHIVE_MANIFEST.json SHA-256 `187131d7a5913b34eb9be3c51b1793d980c0438e9f6607f8f8c634d90bebb8c3`. Manifestos originais e logs anteriores intactos.

### Cobertura e exclusões explícitas

| Pedido do parecer | Prova e limite |
| --- | --- |
| Bloqueios backend em falta | Oito casos unitários verificam individualmente a presença de SEM_LINHAS_COMERCIAIS, DATA_EMISSAO_ANTERIOR_SERIE, MOEDA_NAO_HABILITADA, SERIE_AUSENTE, EMPRESA_AUSENTE, DADOS_QR_INVALIDOS, TOTAL_NAO_POSITIVO_PARA_PENDENTE e DOCUMENTO_ANULADO. Mocks, não oito jornadas persistidas independentes; impedimentos podem coexistir. A jornada persistida cobre os outros motivos identificados na entrega inicial. |
| Aviso legado por linha | Jornada altera quantidade de linha de rascunho legado com total de cabeçalho igual ao anterior: projecta 24,60 e exige confirmação sem persistir o cálculo. Reposição explícita antes de prosseguir. |
| HTML e transacção | Jornada confere motivo AT e total projectado no HTML e ausência dos avisos de recálculo no emitido. Spy observa transacção read-only e REPEATABLE_READ na entrada do serviço, nos percursos JSON/HTML. Não é um ensaio de anomalias sob escritas concorrentes. Comparações persistidas e verificação de ausência de assinatura permanecem. |
| Três consumidores | Cada um cobre bloqueio, nova avaliação, diferença de totais, cancelamento, diagnóstico indisponível (503), POST 403 e espera por diagnóstico atrasado. São APIs simuladas, não integração browser/backend nem matriz completa de permissões. |
| Editor novo e gravação falhada | Caso de novo rascunho pelo formulário até emissão simulada; caso de erro de gravação sem POST de emissão. A gravação não é executada num servidor real nestes testes. |
| Respostas obsoletas | DocumentsView já tinha geração de pedido no detalhe. R1 acrescenta geração/invalidação no carregamento do editor e descarta preparação de emissão legada se a selecção mudar. Browser troca o documento do editor enquanto o diagnóstico anterior está pendente e verifica que o resultado antigo não reaparece. A espera por resposta fresca é testada nos três consumidores; a troca de selecção do legado e de DocumentsView é conferida por leitura, não por novo caso browser de concorrência. Não se afirma protecção universal de todos os pedidos da aplicação. |
| Regressão anterior | Reutilizam-se como evidência histórica os 46 testes de controller e GovernedFtPositiveTests, sem os apresentar como reexecutados na R1. POST de emissão, permissões e circuitos financeiros não foram alterados. Os 14 casos novos/afectados exercitam o serviço alterado. DocumentoComercialEmissaoIntegridadeTests e lookups.spec.ts não foram repetidos; não houve alteração aos seus componentes de emissão/lookup, nem se declara cobertura integral. |

O-1: os códigos DADOS_QR_INVALIDOS e CALCULO_FISCAL_INVALIDO agregam causas, mantendo a mensagem concreta. O-2: TOTAIS_INCOERENTES continua protegido no POST; a projecção não inventa uma nova condição. O-4 tratado com captura de identidade/código de saída. O-5 mantém-se a limitação das fontes PrimeIcons reutilizadas de outra worktree; fidelidade visual não demonstrada. O-6 refere texto antigo M04_STATUS.md, fora do delta R1; não foi reescrito como resultado novo.

Sem mudanças no POST, migrations, manifesto fiscal ou gates produtivos. Implementação R1 pronta para confirmação incremental Claude, limitada aos três achados e ao delta desde 1814d86. À data do pedido R1 ainda não existia aprovação independente; o resultado posterior está registado abaixo. Continua sem autorização de publicação.

## Confirmação independente e observações posteriores

Claude Code confirmou os três achados como encerrados, APPROVE 0/0/0 no HEAD 7c96e5819ee2ee8c47f39363675323f611943b73. Original e sidecar preservados, SHA-256 ec8905c589068024c375285ad9b509f28ca7c815b778bb3a479cabf1eb4ad4a0. A revalidação Docker posterior confirmou acessibilidade e ausência de recursos das duas labels, com evidência 1ffc48c0c22ba061c8a8f68350b4e781133a7d8a38720259e0197469203b149f; não é repetição de ensaios nem verificação independente do revisor.

O-1/O-2: melhoria pendente antes de utilização produtiva — mostrar bruto, IVA e total no diálogo final, incluindo aviso já conhecido e operador no cabeçalho. Não implementada nesta entrega documental e não condicionante do veredicto. O-3/O-4: limites dos bloqueios unitários e da protecção contra respostas antigas permanecem como acima descritos. O-5: HTML do emitido mantém linha Total projectado nula, sem mudança funcional. O-6: a limitação Docker histórica e a sua revalidação estão agora explicitadas na memória, estado e neste documento.
