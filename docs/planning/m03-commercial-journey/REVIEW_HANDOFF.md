# M03 — Pedido de revisão independente Claude Code

Mandato [#46/6613324580](https://github.com/amgsrosario/fac/issues/46#issuecomment-6613324580). Branch local `codex/m03-commercial-journey`, worktree `/home/arosario/Projetos/fac/.worktrees/m03-commercial-journey`, base `e6bb33f76523a268b39978733e099c96e98d7999`/PR #50. Rever o HEAD final identificado no relatório externo e conferir worktree limpa. Sem push/PR/merge autorizados.

Ler AGENTS, memória/estado, COVERAGE.md, VALIDATION.md e o diff completo base..HEAD, incluindo teste novo e executor. Confirmar os manifestos do arquivo restrito `/home/arosario/tuuli-backups/m03/2026-11-04/`, global SHA-256 `1af9232f2bc54289fcf490806dc995f7017930520f25c0cd1e3a68cce2791cd3`. Preservar todos os originais; sem operar bases conservadas.

Prioridades:

1. Inventário dirigido e selecção proporcional; reutilização explícita M01/M02, sem repetição ou reabertura da segurança. Base PR #50 efectivamente integrada.
2. Novo teste HTTP com transacções reais concluídas e sem transacção exterior: mestres, rascunhos, linhas, totais, parâmetros, listagens, exportações não fiscais, permissões e auditoria. Procurar falsos positivos, dependências entre testes e asserções insuficientes.
3. Fixtures sintéticas: tipo com código fiscal NULL não homologado; marcador sintético de série só para negativo. Nenhuma revisão fiscal, fornecedor de assinatura ou emissão bem-sucedida. Não confundir percurso legado de teste com FT comercial governada.
4. Negativos 409 assinatura/FT, rollback de número/cabeçalho/auditoria, 400 série sem código, 401/403, referências pai/filho e erros de dependência. Conferir o alcance efectivo, sem alegações de isolamento global/SAF-T.
5. Proveniência Git limpa `7acfc9e`, tree `dfc0a646f7d348e7d687c137640669f0d9fc0746`, antes/depois por classe, 27 casos finais (9 novos +18 seleccionados), package. Duas tentativas anteriores preservadas e sobreposição não somada. Código testado idêntico no fecho documental.
6. Isolamento PG16.3/tmpfs/IDs/labels/recursos/segredos; arquivo restrito de 87 ficheiros e identidade conferida. Sem tocar na origem/preparação/Ubuntu/GOLDEN/7ce573f.
7. Nenhuma mudança src/main/migrations/consumidores/gates; M02 observações mantidas, FinancialCore inactivo, legado autoridade operacional, 1e suspensa, M04/M05 não iniciadas. Decisões fiscais e trabalhos adiados não cancelados.

Emitir APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO, contagem BLOCKER/MAJOR/MINOR, HEAD revisto e referências exactas. Não é necessário repetir testes sem risco concreto. Não alterar ficheiros, bases, Git ou serviços; sem publicação, activação, cutover ou merge.

**Transmissão efectiva:** não foi encontrado runtime Claude nesta sessão. O PO indicou anteriormente que encaminhará o pedido apresentado no chat. Este documento e eventual comentário GitHub são handoff/rastreabilidade; não constituem activação do revisor nem aprovação independente.

## M03-R1 — Pedido de confirmação incremental Claude

Rever exclusivamente `97db276a61dc8ba5928c51c3d0b54f6bfb3b8c69..HEAD` final identificado no relatório externo R1, branch/worktree M03 existentes. Ordem #46/6627651498. Parecer original SHA-256 `34d9bd8b279705b8bde4c975b5d6495718d169b58ef766bb6e048e7c739453b7`, original/sidecar conferidos e cópias inalteradas no arquivo. Não substituir por conferência Codex nem repetir revisão global.

Confirmar MINOR-001: alteração efectiva da descrição do artigo e GET discriminante; ausência de alegação de propagação do mestre aos totais. MINOR-002: PUT/DELETE de linha positivos, GETs e totais discriminantes 24,60→30,75→12,30, permanência da primeira linha e ausência da removida, @AfterEach inalterado. Confrontar matriz, fontes e XML. O-1–O-5 permanecem observações não condicionantes, não tarefas novas.

Apenas nove casos sobrepostos de CommercialJourneyTests executados em PG16.3 sintético descartável no HEAD limpo `49b1623d34c1dae4061313a5476bab98cbd08261`, tree `9b33526066b69397db29de1b2c0a2ede19f6238b`; últimos ajustes só documentais. Arquivo `/home/arosario/tuuli-backups/m03-r1/2026-11-04/`, 16 ficheiros + manifesto SHA-256 `8c67239595d917a9ccd341b49b2cccb131f8341d190aeb9916b6a5b22d93a378`. Original M03/87 ficheiros preservados; sem repetir outras classes/package, M01/#38.

Emitir parecer incremental com HEAD exacto, estado de cada MINOR e BLOCKER/MAJOR/MINOR. Sem testes/bases/Git/serviços ou publicação; preservar manifesto vazio/409/limites M03. Pedido a encaminhar efectivamente pelo PO/Coordenação: nenhum runtime Claude disponível nesta sessão; documento não activa revisor.
