# M4-02 — Prova positiva MockMvc

A execução final aprovou um caso JUnit dirigido em PostgreSQL16.3 sintético, com zero falhas, erros ou ignorados. Branch `codex/m4-02-governed-ft-proof`, base `828eefa02bd4fbda6f3ad8d0283c879c11cd911f`. Teste executado no HEAD limpo `e090a4add3fe084bad2244d2e66b76de6ab63b35`, tree `bd75df3aadd8d456753c340f8ea8db9965f53a62`, iguais antes/depois.

## Comandos e configuração

Criada worktree com `git worktree add -b codex/m4-02-governed-ft-proof .worktrees/m4-02-governed-ft-proof 828eefa02bd4fbda6f3ad8d0283c879c11cd911f`. Após cada correcção do teste foi criado commit local, com estado limpo para a execução. Invocação `python3 docs/planning/m4-02-positive/run_validation.py`. O runner arquiva comando Maven `mvn -o -B -Dspring.profiles.active=test -Dtest=GovernedFtPositiveTests test`, metadados, XML e logs. Apenas a classe indicada foi executada. Maven3.9.16/JDK21.0.12.1; PostgreSQL16.3, imagens por digest no runner.

O runner criou clusters independentes por tentativa, com PGDATA tmpfs, rede interna e proxy publicado só em127.0.0.1, sem mounts de bases conservadas. Na execução final, cluster7704621978920181805. A base control é `tuuli_m03_control_test`; a base efectiva do teste é `tuuli_m03_0_test`, criada no mesmo cluster. Estes nomes M03 foram herdados para reutilizar a guarda de fixture. A label própria é M402-positive. A identidade do cluster é validada no teste. O perfil test aplica migrations e JPA validate. Snapshot histórico não utilizado. A fonte sintética de assinatura importada é a existente FiscalSignatureTestConfiguration. A revisão FT/1 é criada apenas no teste, com definição igual no catálogo Java de teste e PostgreSQL. O CRUD normal cria e vincula GFT. Dados/contas, marcador SYNM402 e série SYNTH exclusivamente sintéticos.

## Critérios observados

| Critério | Verificação persistida |
| --- | --- |
| EMITIDO | Resposta HTTP200 e SELECT do estado após o pedido |
| Dívida total | Factura12,30, pendente documental12,30 e posição DEBT original12,30, cliente correspondente e origem vinculada |
| Saldo aberto | valor_pendente12,30 e available12,30 |
| Zero recebimentos/liquidações | Zero financeiro legado, recibos do núcleo, afectações, componentes e SETTLE/REVERSE |
| Repetição | HTTP409 e snapshot depois da repetição igual ao snapshot da emissão, com uma única CREATE |
| Controlos/integridade | Fonte indisponível409 antes da emissão, snapshot igual; série sem código400 e snapshot igual; constraints financeiras diferidas activas nos commits normais, FKs validadas |

O teste não tem transacção exterior. As leituras via JdbcTemplate ocorrem após os pedidos, com transacções do serviço concluídas. Os snapshots `persistent/before.json`, `after.json`, `after-repeat.json` e as respostas documentam o percurso principal. As outras recusas são verificadas pelas asserções da classe e XML/log. O teste verifica todas as linhas das pequenas tabelas financeiras sintéticas e não só valores da resposta HTTP.

## Proveniência e tentativas

1. `3235034` falhou em testCompile, antes de executar casos, por visibilidade do construtor do catálogo.
2. `083934a` executou um caso com erro numa leitura SQL do teste, coluna cliente_id do legado. Corrigida para id_cliente.
3. `5acca86` executou um caso com falha na expectativa da repetição, que devolve409 em vez de400. As asserções financeiras anteriores passaram.
4. `e090a4a`, execução `20261105-094813-f0f2f8`, um caso aprovado. Sem alterações funcionais.

Todos os originais e tentativas mantidos. A contagem final é um caso, com seis critérios e verificações negativas. Não somar tentativas como testes distintos. Nenhum package ou suite alheia executado. O compilador compila as fontes de teste normais do projecto, mas Surefire selecciona apenas esta classe.

## Arquivo e limites

Arquivo `/home/arosario/tuuli-backups/m4-02-positive/2026-11-05/`,70 ficheiros+manifesto global SHA-256 `d0e36a81e967a4c8b35e0ff7188197d11b7637981c6ef3f83ee0d48558e8e08f`. Manifesto da execução final `01c2b2935279c0014f85a7a5e54934e41867e7ebbca871cfd2c6189cbb0b8166`. Cópias conferidas byte a byte por tamanho/hash. Os logs de tentativas falhadas podem incluir tokens e respostas sintéticas do MockMvc e têm acesso restrito. Recursos/credenciais associados eram efémeros. Não publicar os logs brutos.

disposal.json verifica ausência de contentores e redes com a label própria e remoção do ficheiro secreto. A worktree de desenvolvimento é mantida para revisão. As bases conservadas e demo não foram operadas. A prova demonstra o cenário MockMvc, sem ensaio positivo pelo JAR. A homologação e activação fiscal mantêm gates próprios. Prova positiva MockMvc confirmada pelo parecer original, cuja correcção documental R1 foi aprovada com 0 BLOCKER, 0 MAJOR e 0 MINOR em #46/6655672950. O MINOR-001 está encerrado. Permanecem pendentes a emissão positiva pelo JAR, a assinatura real e as validações fiscais exteriores à correspondência funcional aprovada.

Parecer anterior `2026-11-05_TUULI_M4-02_ensaio-FT-diferida_revisao-independente_Claude.md` e sidecar conferidos, SHA-256 `2478a7d7c383fc40d232ee35579c3078a8a6f92b03b4f1c170ad0d4b522b3ea8`. Cópias preservadas no arquivo.

Parecer incremental R1 `2026-11-05_TUULI_M4-02-positivo-R1_confirmacao-incremental_Claude.md`, SHA-256 `04b22642b57115efaf5a50a234e1eb20d5bda17fc7a4d1da426dd87b7bd048d9`, conferido com o sidecar e preservado no arquivo incremental.
