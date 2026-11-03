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

Executado apenas em `tuuli_m01_http_test`, com a fixture/duplo de teste já existentes, fonte indisponível e nenhuma emissão bem-sucedida. Exercita controller real via MockMvc e ausência de consumo de numeração. Não é chamada comercial com login nem ensaio de deploy. Contextos de leitura M01, por contraste, exigem segurança activa e zero fornecedores de assinatura. Não se modificou o pom para obter estes resultados.

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
