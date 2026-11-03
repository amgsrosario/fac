# M01 — Delta da base limpa #38 — 03-11-2026

## Mandato e base

[Ordem executiva #46/6609266825](https://github.com/amgsrosario/fac/issues/46#issuecomment-6609266825), conforme [contrato M01](../reprioritization-2026-11-02/MISSIONS.md).
Base integrada confirmada por fetch HTTPS para FETCH_HEAD: `472cf5847949c9c7d2ad6b75ae5dd444a60bf9f8`, merge PR #48. Branch `codex/m01-clean-base-readiness`, worktree isolada homónima em `.worktrees/`. Tracking origin antigo não usado como prova. Nenhuma actualização do checkout principal.

## Identidade e entrada reutilizada — FACTOS

- Preparação conservada: `tuuli-clean38-834881`, ID `5dd74c5ae85b5e20ea459d76766b73e8cb74e6e26fb86bf9ceaf683847158a01`, base `tuuli_clean_38_test`, cluster `7701042457621884972`, PostgreSQL 16.3. Running, sem porta PostgreSQL publicada. SQL restrito a transacções **READ ONLY**.
- Estado SQL efectivamente medido: V27, 27 migrations bem-sucedidas, nenhuma falhada. Controlos agregados/digests das 42 tabelas coincidem com `prepared_controls_final.json` de R2, antes e após M01. Não inferido do nome do contentor.
- Snapshot autorizado reutilizado: `tuuli-backups/issue38/2026-10-26-incremental-minors/clean_base_complete_references_private.backup`, 1 660 748 bytes; SHA-256 `d26dc761a64fdf4886b531acb0f5b5e03a11b6bde45e86af556858b71e538851` conferido.
- Prova de restauro #38 reutilizada, não repetida como missão de restaurabilidade: manifesto R2 de 35 ficheiros com SHA-256 `b0fee41611cc8321562a8ab4b1d424329f4c8da7eea38ff0308b9f982c581545` conferido. Só se materializou um clone deste snapshot para medir o delta, sem importação selectiva nova nem backup/restauro das bases conservadas.

## Isolamento do ensaio

Clone `tuuli-m01-751a4b`, base `tuuli_m01_test`, cluster `7703982634876346413`, imagem local PostgreSQL 16.3. PGDATA em tmpfs, rede interna própria, sem volumes preservados nem porta PG publicada. Proxy transitório exclusivamente loopback ligado só ao clone. Base separada `tuuli_m01_http_test` para a fixture sintética HTTP; não se mistura com o snapshot em análise.
Credencial técnica aleatória efémera, fora das evidências; não é conta comercial ou segredo de uma instalação conservada. Nenhuma chave fiscal criada por M01. A fixture HTTP reutilizada contém duplo de assinatura de teste já existente, com fonte explicitamente indisponível no cenário negativo; não é credencial ou assinatura produtiva.

## Delta e integridade — FACTOS

- Primeiro contexto Spring aplicou exactamente **V28** sobre V27; validate aprovou 28 migrations. Nenhuma migration operacional nova criada ou alterada por M01. V29 experimental não faz parte das migrations de produção integradas.
- Reinicializações posteriores: schema V28 actualizado, nenhuma migration necessária, JPA valida e contexto arranca.
- Controlos integrais das 42 tabelas conservados no clone, exceptuando a alteração esperada do histórico Flyway 27→28. Mestres: 4 clientes, 10 artigos, 197 772 códigos postais e 5 188 freguesias. **84 FKs, zero órfãos, zero constraints não validadas.**
- Zero documentos comerciais/financeiros, linhas, pendentes, auditoria histórica, movimentos/posições/afectações FinancialCore, tipos documentais, séries, utilizadores, parâmetros documentais e revisões fiscais. Sem DEMO26, códigos AT ou transacções transportadas.
- Emitente: 1 registo herdado; presença não constitui validação da identidade/regime fiscal. Sem publicação de nomes, NIF, contactos ou outros dados pessoais. Parâmetros de aplicação/cliente presentes não são homologação de valores operacionais.
- Catálogo vazio continua a permitir o arranque legado, mas `ftDeferredCreation` recusa; readiness não pronta e activação não permitida. Nenhum fornecedor de assinatura registado no contexto M01 de leitura, segurança explicitamente activa.
- Diagnóstico FinancialReconciliation vazio por cliente: zero documentos, diferença comparável **NULL**, activação recusada. Ausência de movimentos não é saldo comercial reconciliado ou autorização de operação.

## Testes e limitações da prova

Execuções Maven offline, perfil `test`, exclusivamente em bases M01 com sufixo `_test` e guard de identidade de cluster/base. Comando de leitura: `mvn -o -B -Dspring.profiles.active=test -Dtest=M01PreparedSnapshotTests test`, variáveis técnicas injectadas sem as imprimir. Teste condicionado a `M01_CLUSTER`; não se executa por defeito contra outra instalação. Cada método de inspecção usa transacção READ ONLY.

Resultados finais e manifesto: ver [VALIDATION.md](VALIDATION.md).

Eventos de preparação não ocultados: primeira compilação do novo teste falhou por acesso package-private de `verifiedSnapshot`; corrigido o package do teste, sem mudar a aplicação. Na execução seguinte V28 foi aplicada e seis das sete verificações passaram; a asserção de segurança detectou o default **false do Surefire no pom**. O teste passou a exigir `fac.security.enabled=true` por propriedade explícita do contexto. Não é falha do runtime produtivo nem alteração ao contrato de segurança. Logs/XML dessa tentativa preservados.

O teste HTTP usa a operação real do controller via MockMvc com fixture sintética já existente e fonte indisponível, em segunda base descartável: verifica resposta 409 e ausência de consumo de número. O bypass de segurança dessa regressão é exclusivamente o mecanismo de teste existente; não é usado no contexto M01 de leitura. Não se afirma ensaio HTTP produtivo com login/conta comercial, TLS, roles/HBA ou segredo JWT persistente. Isso permanece nas etapas futuras de provisionamento/piloto; não se executam M02–M05.

## Reservas e decisão executiva

A preparação conservada **continua em V27**. O clone demonstra a compatibilidade com V28; não a actualiza nem autoriza reutilização operacional. Runtime demonstrado é o contexto de teste Spring/JPA sobre o clone, não deployment, prontidão comercial ou validação de ambiente produtivo completo.

Permanecem: dados legítimos do emitente, identidades/perfis e primeiro provisionamento (M02), configurações/tipos/séries/parâmetros aprovados, gates 2/1/1, QR/PDF, AT, RC/RG, manifesto fiscal vazio e assinatura reservada à fase final. FinancialCore inactivo, legado autoridade operacional, 1e suspensa. Trabalhos fiscais posteriores/auditoria final/certificação adiados, não cancelados. Não iniciados M02–M05, cutover, deploy ou activação.

Histórica `fac/facdb` não acedida; sem SQL ou operação de serviço/volume nessa origem. Ubuntu, GOLDEN, restantes bases e worktree `7ce573f` preservados. Principal mantém `docker-compose.yml` e `.worktrees/` preexistentes. Recursos efémeros M01 eliminados após guard de identidade/labels; nenhuma evidência original #38 é alterada.

Revisão independente Claude **pendente**: o pedido deve ser efectivamente encaminhado pela Coordenação, caso não exista runtime Claude acessível; comentário GitHub não o activa. [Handoff](REVIEW_HANDOFF.md).
