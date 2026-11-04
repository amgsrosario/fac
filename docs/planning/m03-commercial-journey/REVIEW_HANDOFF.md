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
