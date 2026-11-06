# M4-09 — Validação efectiva

## Identidade e execução

Base integrada: `5342255e329f3a2ebe6d7b5ad97e9b6f8c09cf07`, PR #55, reconfirmada por HTTPS. Branch `codex/m4-09-qr-amounts`, worktree `.worktrees/m4-09-qr-amounts`. O registo local anterior `df71e68d017c2ee730211c40584fc8ac5f06ef11` continua intacto na worktree emission-readiness-pr53, sem cherry-pick, rebase ou alteração.

HEAD ensaiado `066a98490a4db43698ef25d937cd1d16e33cd91e`, tree `be657782f0a1554717944320fda11ed1122d96e1`, estado limpo antes/depois. Execução `20261106-124452-5cbed5`, comando `python3 docs/planning/m4-09-qr-amounts/run_validation.py`. Única classe executada: QrAmountsJourneyTests. **5 casos parametrizados aprovados, 0 falhas, 0 erros, 0 ignorados, código de saída 0**, Maven BUILD SUCCESS. Não são uma suite integral nem cinco repetições do mesmo cenário. Não houve tentativas falhadas; se as houvesse, seriam preservadas separadamente, nunca substituídas por logs reconstruídos.

Maven 3.9.16, Java 21.0.12.1, PostgreSQL 16.3, UTF-8. O runner usa Maven offline e imagens já presentes fixadas por digest (PostgreSQL d0f363f8366fbc3f52d172c6e76bc27151c3d643b870e1062b4e8bfe65baf609; proxy Node fb4cd12c85ee03686f6af5362a0b0d56d50c58a04632e6c0fb8363f609372293). Sem instalação de dependências ou download implícito.

## Resultado e alcance

A [matriz e cálculos](MATRIX.md) foram fixados antes da execução. Os cinco casos comparam resposta consolidada, linhas/snapshots e cabeçalho persistidos e campos I7/I8/N/O do payload QR persistido e retornado: todos coincidiram com o oráculo independente. Não se identificou divergência funcional e **não foi alterada a aplicação**. Base/IVA/total finais:

| Caso | Base normal | IVA | Total |
| --- | ---: | ---: | ---: |
| M409-01 múltiplas linhas | 25.00 | 5.75 | 30.75 |
| M409-02 quantidade/preço fraccionários | 16.08 | 3.70 | 19.78 |
| M409-03 desconto percentual | 19.36 | 4.45 | 23.81 |
| M409-04 desconto em valor e meio cêntimo | 2.65 | 0.61 | 3.26 |
| M409-05 fecho por linha versus agregado | 0.09 | 0.03 | 0.12 |

O oráculo usa Fraction e operações inteiras, sem código Java ou float. expected.json SHA-256 `bac1eb653a91793a8db6518525318de86d72ffdea3f1d5edb05e0cd99d6291d8`. O runner exige reprodução byte a byte antes de executar os pedidos. Cada caso grava entradas, respostas, expectativas com passos racionais, persistência e campos QR. A resposta de emissão é seguida de novas leituras fora de transacção exterior de teste. FT/1 e classificação 2/1/1,false são lidas da persistência; uma conferência suplementar das cinco capturas confirmou todos os atributos (não acrescenta casos JUnit).

Exclusivamente MockMvc: POST de criação de documento/linhas e emissão, autenticação e serviços reais com segurança activa, sem chamadas directas a serviços para emitir nem alteração de controlos. Fonte FiscalSignatureTestConfiguration existente, de src/test, gera exclusivamente material sintético. Revisão FT/1 materializada apenas na base descartável. Não houve JAR positivo, assinatura real, activação, publicação de catálogo ou uso de dados persistentes.

## Isolamento, remoção e arquivo

PostgreSQL novo em tmpfs, sem mounts conservados e sem porta PostgreSQL publicada; rede interna e proxy loopback próprios. Guarda de nome `_test` e cluster esperado no teste, recusa dos identificadores conservados no runner. isolation.json identifica os IDs exactos, cluster e redes. Segredo temporário fora dos artefactos e removido. O runner só elimina recursos que criou, após conferir IDs/labels.

`disposal.json` mediu zero contentores/redes com `ar2.mission=M409-qr-amounts`. Uma consulta posterior, exclusivamente de leitura, confirmou novamente 0/0, códigos de saída 0, sem remover outros recursos. Captura em supplement/resource-recheck.json. Não se operou sobre bases conservadas, demo, GOLDEN, checkout principal ou outras worktrees.

Arquivo reservado: `/home/arosario/tuuli-backups/m4-09/2026-11-06/20261106-124452-5cbed5/`. Originais em target/m409-qr-evidence conservados; 92 ficheiros copiados e conferidos byte a byte, com manifesto suplementar que mantém o manifesto original inalterado.

- Manifesto original: `aa6e66af7956d00067f9baf7cf737363a55636ca56f5d9500fcfff4a78d2f5e3`.
- ARCHIVE_MANIFEST.json: `8ee5a6e42b2c9c51af89eb25ae242c470476318be091a1eae5939a22b6d61020`.
- Teste ensaiado: SHA-256 `d8869285ba9d2a6c5bc9d501f60bdde75a6e32e2c10d19d0585518c0a28194e3`.

O suplemento identifica verificações posteriores, versão Python e transcrição dos comandos reais de criação da worktree/invocação. Não se apresenta essa transcrição posterior como log automático inicial. Os XML, logs e capturas Git são originais. As expectativas e o runner arquivados permitem reproduzir o ensaio; nova execução só quando justificada/autorizada.

## Limites e entrega

Não homologa QR/PDF ou taxa/regime, não abrange outros territórios/regimes/tipologias/moedas, retenção/isenção, PDF físico, SAF-T ou disponibilidade de assinatura produtiva. Q/R são sintéticos; a prova visa os montantes normal/EUR. Não foram reexecutadas as classes anteriores de cobertura nem package/frontend por não existir alteração funcional. O teste compila a aplicação e a classe dirigida, mas só a classe declarada foi executada.

O HEAD final do relatório externo pode acrescentar apenas documentação ao HEAD ensaiado; confirmar igualdade dos blobs do teste, runner e oráculo antes da revisão. Memória/estado/matriz actualizados. **Revisão independente Claude pendente**: não presumir aprovação por este relatório nem activação do revisor por ficheiro. Sem push, PR, merge ou activação. A melhoria bruto/IVA/total no diálogo continua pendente e não foi implementada nesta missão.
