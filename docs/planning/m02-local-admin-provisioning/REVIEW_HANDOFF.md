# M02 — Pedido de revisão independente Claude Code

Mandato: issue #46, comentário 6611272547. Base `ca1e80245d95362eb8c7a78c90350f1a930eb0bf`. Rever o HEAD local da branch `codex/m02-local-admin-provisioning` identificado no relatório externo, worktree `.worktrees/m02-local-admin-provisioning`; sem push/PR autorizados. Não substituir Claude por auto-aprovação Codex ou presumir activação por comentário GitHub. Encaminhar instrução operacional pelo PO/Coordenação, conforme o procedimento anterior.

Ler AGENTS, memória/estado, DESIGN.md, RUNBOOK.md, VALIDATION.md, diff completo e evidências. Confirmar primeiro hashes, sem abrir qualquer base conservada ou segredo. Arquivo `/home/arosario/tuuli-backups/m02/2026-11-03/`, manifesto geral SHA-256 `4cdcbd11d90c1c69c35fc6badf130f47f8e0fc550860418d5530f0ed622f83b0`.

Pontos prioritários:

1. Reconhecimento não duplicou mecanismo existente; CLI sem Spring/bean/runner/listener/endpoint, sem seed/password universal. Jar smoke não é ensaio humano da consola; limites declarados.
2. Autorização local 0600/owner/pais/sem symlink/duplicados/expiração e ligação loopback; fronteira de confiança no DBA/grants por instalação e custódia local, sem alegada assinatura administrativa. UUID não é segredo nem autorização isolada.
3. SECURITY DEFINER/search_path/tabelas qualificadas/revogação PUBLIC, role restrita sem DML/admin/memberships/ownership, exactidão base+cluster+UUID+role. Nenhuma role/grant/account criada por migration.
4. Lock singleton → tabela Utilizador, concorrência/repetição/INSERT normal e expiração pós-locks; atomismo da criação+auditoria+comprovativo, rollback perante falha, consumo monotónico e sem reset. Considerar sequências SQL e ambiguidades não cobertas pelos testes.
5. Passwords via consola sem eco, não argv/ambiente da CLI, BCrypt/limite bytes/DTO/política existente, erros genéricos e auditoria sem segredos. Não prometer limpeza absoluta de Strings em memória.
6. Login/JWT/permissões actuais, política partilhada sem alteração semântica, V25/V26 preservados, V29 apenas aditiva e instalação com utilizadores inelegível, validate/segundo arranque. Não incorporar SQL V29 da worktree desconhecida 7ce573f.
7. Prova: 99 casos distintos finais em execuções dirigidas, duas tentativas falhadas preservadas; HEAD/tree/status por classe, código funcional idêntico entre commits ensaiados e último commit apenas documental. Não confundir com suite integral ou CI.
8. Bases/Ubuntu/GOLDEN/worktrees/gates preservados; sem contas reais ou uso na preparação conservada, M03–M05 não iniciadas. Roles/HBA reais/identidades comerciais continuam dependentes de autorização legítima.

Emitir APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO com BLOCKER/MAJOR/MINOR, commit revisto, ficheiro/linha e evidência. Não executar testes/SQL ou alterar ficheiros sem risco concreto e mandato; não publicar, activar, fazer cutover ou merge. Parecer real ainda pendente.


## R1 — Confirmação incremental exclusivamente do delta

Comparar `5ae9e5038184122f38309c8293e368de9d27e451..HEAD` final desta branch (SHA exacto no relatório R1/issue #46). Ordem #46/6612496711; parecer original SHA-256 `790a988f0b9d5af3f0e037387c02a91cd389057d26338d151cfb3aa5bd52744a` conferido com sidecar.

Confirmar MINOR-001 (guarda real de terminal, Java 21 compatível/reflexão 22+, código 3, teste com stdin redireccionado não vazio e sem dependência de EOF), MINOR-002 (ownership relevante/schema/CREATE/DML, memberships e casos não-superuser discriminantes), e O-1 READ COMMITTED em SQL/Java antes de locks/leitura privilegiada. Conferir O-2–O-5 como limites, sem instalar/alterar hosts, corrigir dívida histórica ou incorporar 7ce573f.

Ler R1.md e arquivo restrito `tuuli-backups/m02-r1/2026-11-03/`, 41 artefactos + manifesto SHA-256 `56bc1818a820f8f054b74f17416a8de90865e8dff7f07015f5270035f2771f91`. 33 casos dirigidos, sem reexecução dos 99, M01 ou #38; HEAD de ensaio limpo `b3eb249fb1d13795af72fadc32dade62b5686896`, fecho posterior só documental. Java nativo 22+ não executado, estado/limites explícitos; testes de owners não-superuser são de recusa, não prova de instalação com dono SECURITY DEFINER restrito.

Emitir parecer incremental BLOCKER/MAJOR/MINOR e commit revisto. Sem SQL, testes ou alterações de bases/ficheiros, push/PR/merge, activação ou M03–M05. O pedido precisa de encaminhamento efectivo pela Administração; comentário GitHub não activa o Claude. Correcção do executor não constitui aprovação.
