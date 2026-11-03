# M01 — Evidência de validação

03-11-2026. Código operacional da base integrada `472cf5847949c9c7d2ad6b75ae5dd444a60bf9f8` inalterado. Novo teste de diagnóstico condicionado a `M01_CLUSTER`, exclusivamente para o snapshot autorizado. Não é suite integral nem validação do CI.

| Execução | Executados | Aprovados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: | ---: |
| Primeira compilação do novo teste: package incorrecto | 0 | 0 | — | erro de compilação | — |
| Tentativa inicial que aplicou V28; segurança herdou false do pom | 7 | 6 | 1 | 0 | 0 |
| Arranque com segurança explicitamente activa | 7 | 7 | 0 | 0 | 0 |
| Segundo arranque, nenhuma migration adicional | 7 | 7 | 0 | 0 | 0 |
| Fonte final do teste com condição de ambiente | 7 | 7 | 0 | 0 | 0 |
| Regressão HTTP 409/numeração, método existente | 1 | 1 | 0 | 0 | 0 |

**Oito casos distintos**, sete novos e um reutilizado; as reinicializações repetem os mesmos sete e não devem ser somadas como testes únicos. A tentativa falhada é preservada, não contada como aprovação. Fonte final: `src/test/java/com/ar2lda/fac/fiscal/catalog/M01PreparedSnapshotTests.java`. Novo teste não introduz fixtures ou escritas; transacções READ ONLY e identidade exacta verificadas.

Comando HTTP dirigido:

```sh
mvn -o -B -Dspring.profiles.active=test -Dfac.security.enabled=false \
  '-Dtest=DocumentoComercialEmissaoIntegridadeTests#ausenciaDeAssinaturaFiscalRecusaEmissaoSemConsumirNumero' test
```

Executado apenas em `tuuli_m01_http_test`, com a fixture/duplo de teste já existentes, fonte indisponível e nenhuma emissão bem-sucedida. Exercita controller real via MockMvc e ausência de consumo de numeração. Não é chamada comercial com login nem ensaio de deploy. Os contextos de leitura M01 declaravam a propriedade de segurança activa e verificavam zero fornecedores de assinatura; a prova original não media o estado efectivo do bean/cadeia, conforme MINOR-001 abaixo. Não se modificou o pom para obter estes resultados.

## Medições SQL e descarte

- Preparação V27: 42 tabelas iguais ao controlo R2, antes/depois de M01; SELECTs em READ ONLY.
- Clone V28: mesmos dados nas 42 tabelas, excepto histórico Flyway; 28 entradas/zero falhadas, 84 FKs/zero órfãos, zero constraints não validadas.
- Repetição do contexto: validate de 28 migrations, nenhuma migration necessária. Catálogo/readiness/diagnóstico mantêm recusa de activação.
- Zero contas/tipos/séries/parâmetros documentais/transacções/DEMO26; mestres e directório postal completos preservados. Emissão real, regime/identidade do emitente, provisionamento, HBA/roles/TLS e configuração operacional não validados nesta missão.
- Só os dois contentores M01 e respectivas redes próprios foram removidos, após conferência de IDs/labels. PGDATA tmpfs descartado e segredo efémero invalidado/removido. Preparação conservada continua running/V27, sem actualização. Histórica, Ubuntu, GOLDEN e restantes recursos não foram alterados.

## Arquivo e integridade

Arquivo restrito: `/home/arosario/tuuli-backups/m01/2026-11-03/`, 32 artefactos + manifesto, sem snapshot ou credenciais. Inclui logs completos, XML de cada execução, tentativas falhadas, controlos antes/depois, identidade/isolation/disposal, fonte do teste e harnesses.

`MANIFEST_SHA256.json`: **5 521 bytes**, SHA-256 **`9800d974d4f793954c4082b8a68b0b506cee8b07e4160702b627c6b25128b0e8`**. Cada entrada contém nome/tamanho/SHA-256. Evidências brutas não são publicadas no PR. Os scripts privados são evidência, não comandos para executar em bases preservadas; não repetir provisionamento ou restauro sem mandato.

**Resultado executor: M01_PASS no perímetro do delta isolado.** Não significa prontidão comercial, autorização de actualizar a preparação ou aprovação independente. Revisão Claude pendente; [pedido operacional](REVIEW_HANDOFF.md). M02–M05 não iniciadas. Sem alterações operacionais, push, PR, merge, activação ou cutover nesta entrega.


## R1 — Correcções da qualidade da prova e ensaio incremental

Parecer original Claude de 03-11-2026: **APROVADO COM RESERVAS, 0 BLOCKER / 0 MAJOR / 2 MINOR**, sobre `bf80a2fb648d84e056c2c6f36a126a2d253aec03`. Original preservado, SHA-256 `9bd30872d2399cc941385460c8e3ec4d77ae8f326b4335e726d3a14ed533e719`, conferido com sidecar. Não confundir o parecer original com uma confirmação de R1.

**MINOR-001:** a asserção original lia a propriedade que o próprio teste definia. Os ensaios originais, por si, **não demonstram estado efectivo de SecuritySettings nem da cadeia autenticada**. R1 substitui-a por `SecuritySettings.enabled()`, presença de filtros Bearer/Authorization na cadeia seleccionada para `/clientes` e GET anónimo com resposta 401, via MockMvc real. Este é apenas um percurso protegido, sem prova de login, matriz completa de permissões, TLS/HBA, segurança de produção ou ausência universal de bypass.

**MINOR-002:** os ensaios originais antecederam o commit `bf80a2f` e não capturaram HEAD, tree ou estado da worktree por execução. Correram sobre a base `472cf58` mais o teste ainda não commitado; **não são execuções fixadas ao commit final**. A coincidência byte a byte da fonte arquivada com a fonte do commit é uma mitigação, não substitui esse registo. Não se pode reconstruir retrospectivamente um estado Git que não foi capturado. Logs, XML e relatórios originais permanecem intactos.

Para os ensaios incrementais/futuros, `run_security_r1.py` regista HEAD, tree, branch, status completo e SHA-256 da fonte do teste **antes e depois** da chamada Maven. Exige worktree limpa no início e identidade Git inalterada no fim; não regista ambiente nem credenciais. Usa apenas um PostgreSQL sintético novo e efémero, sem snapshot, importação ou restauro #38. Inicializar um schema vazio não reensaia o delta V27→V28 da preparação. O método R1 é seleccionado isoladamente; os outros seis casos M01 não se repetem.

### Resultado incremental R1

**MINOR-001 corrigido pelo executor:** 1 executado / 1 aprovado / 0 falhas / 0 erros / 0 ignorados, BUILD SUCCESS. Método `signatureSourceAbsentAndProtectedRequestRequiresAuthentication`: fonte de assinatura ausente, `SecuritySettings.enabled()` verdadeiro, cadeia `/clientes` contém `BearerTokenAuthenticationFilter` e `AuthorizationFilter`, GET anónimo `/clientes` recusado com **401**. Não prova login, todos os endpoints/perfis ou segurança produtiva; não se repetiu o caso 409 ou os seis restantes casos M01.

Execução em base **sintética vazia**, `tuuli_m01_r1_test`, PGDATA tmpfs/PostgreSQL 16.3, com rede interna e proxy apenas loopback. Sem snapshot, restauro ou importação. O schema foi inicializado de raiz pelas migrations existentes; não foi repetido o ensaio de upgrade do snapshot V27→V28 nem acedida qualquer base conservada. Contentores/redes próprios eliminados após guard de IDs/labels e segredo técnico efémero removido.

**MINOR-002 documentado e captura operacional demonstrada:** antes/depois do Maven, HEAD `1bf2f135074b1141913afdc1dffd723516951cb9`, tree `55dd470b30b41fd77b05511e196ce2c8ce8c5ca1`, branch `codex/m01-clean-base-readiness` e status vazio, exactamente iguais. SHA-256 do teste antes/depois: `1d3f18a2cfd629cf1aab282d7bd43f818d174048fd5fe0ea33ad0da2e59fdcf1`. Não reconstruído retrospectivamente. O commit final posterior apenas acrescenta documentação dos resultados; não altera a fonte testada.

Evidência R1 separada: `tuuli-backups/m01-r1/2026-11-03/`, **13 artefactos + manifesto** de 2 061 bytes, SHA-256 `a6c480f9eccc7fecdee7d736b3da77fba1bdaac94194d8457fcfcd4a26c1563e`. Git antes/depois, isolamento, comando sem segredos, log, XML, fonte, runner, resultado e descarte. Identidade da cópia e 13 entradas conferidas. Evidência M01 original: 32/32 artefactos e manifesto originais novamente conferidos, sem alterações; relatório original e parecer Claude com hashes preservados.

A correcção do executor não é aprovação independente. Confirmação R1 pendente; o PO indicou que encaminhará o pedido disponibilizado nesta conversa. Sem runtime Claude nesta sessão, não se afirma transmissão automática/execução nem se usa comentário GitHub como activação.
