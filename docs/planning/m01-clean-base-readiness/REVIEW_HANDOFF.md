# M01 — Pedido operacional de revisão independente Claude Code

Destinatário: Claude Code, via Coordenação Executiva TUULI. Não presumir activação pelo comentário GitHub. Executor Codex não substitui a revisão independente.

Rever o commit M01 identificado no relatório de entrega, branch `codex/m01-clean-base-readiness`, base `472cf5847949c9c7d2ad6b75ae5dd444a60bf9f8`, contra a ordem [#46/6609266825](https://github.com/amgsrosario/fac/issues/46#issuecomment-6609266825) e contrato M01.

Ler REPORT.md, VALIDATION.md, teste M01 e manifesto de evidências em `tuuli-backups/m01/2026-11-03/`. Conferir hashes antes da leitura dos logs/XML. Não abrir snapshot/dados mestres/segredos. Não repetir #38, restauro ou suites sem risco concreto.

Confirmar:

1. Identidade V27 da preparação medida em READ ONLY, distinta do código V28; snapshot autorizado e R2 reutilizados.
2. Isolamento do clone/tmpfs/proxy e segundo banco sintético; preservação antes/depois das 42 tabelas da preparação, sem acesso à origem histórica.
3. Aplicação V28, validate/rearranques sem nova migration; controlos dos dados excepto Flyway, 84 FKs/zero órfãos, exclusões e catálogos completos.
4. Catálogo vazio não activa FT/manifesto, FinancialCore inactivo, nenhuma conta/série real; diagnóstico vazio não convertido em saldo reconciliado.
5. Separação entre contextos de leitura com segurança activa e teste HTTP MockMvc com fonte indisponível e bypass exclusivo de teste. 409/numeração demonstrados na fixture, não alegados como ensaio produtivo com login.
6. Contagens finais e tentativas falhadas explicitadas; cobertura dirigida não apresentada como suite integral, CI ou prontidão comercial.
7. M02–M05 e todos os gates mantidos; nenhuma mudança a código operacional, bases conservadas, Ubuntu/GOLDEN ou worktree 7ce573f.

Emitir APROVADO / APROVADO COM RESERVAS / DEVOLVER PARA CORRECÇÃO, com BLOCKER/MAJOR/MINOR, referências exactas, cobertura e limitações. Não implementar, emitir, activar, fazer cutover/deploy ou merge. Parecer ainda pendente até recepção de original independente verificável.


## R1 — Pedido incremental, exclusivamente dois MINOR

O PO indicou que transmitirá ao Claude o pedido disponibilizado na conversa. Não há runtime Claude acessível nesta sessão; esta indicação não é parecer ou confirmação de execução.

Comparar `bf80a2fb648d84e056c2c6f36a126a2d253aec03..HEAD` final da branch (SHA exacto no relatório externo e na issue #46). O código de teste foi validado no commit `1bf2f135074b1141913afdc1dffd723516951cb9`; o commit seguinte é apenas documental.

- MINOR-001: verificar `SecuritySettings.enabled()`, filtros Bearer/Authorization e 401 do GET anónimo `/clientes`, com limites expressos. Ensaio sintético dirigido 1/1, sem snapshots ou upgrade da preparação. Não interpretar como matriz de segurança, login, 409 repetido ou prontidão produtiva.
- MINOR-002: conferir declaração de ausência de estado Git dos ensaios originais, sem atribuição retroactiva ao commit `bf80a2f`; conferir captura antes/depois e fonte idêntica no novo ensaio.
- Conferir manifesto incremental `a6c480f9eccc7fecdee7d736b3da77fba1bdaac94194d8457fcfcd4a26c1563e` no arquivo `tuuli-backups/m01-r1/2026-11-03/`, sem repetir testes, #38 ou V27→V28. Originais M01 preservados.
- Confirmar diff sem src/main, migrations, configurações operacionais, dados ou alteração de gates. M02 não iniciada.

Emitir parecer incremental com BLOCKER/MAJOR/MINOR, commit revisto e fontes concretas. Não executar SQL/testes, publicar, fazer cutover ou activar circuitos. Aguardar parecer real; resolução do executor ainda sujeita a confirmação independente.
