# M4-09 — Validação efectiva

## Prova inicial — identidade e execução (066a984)

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

O HEAD final do relatório externo pode acrescentar apenas documentação ao HEAD ensaiado; confirmar igualdade dos blobs do teste, runner e oráculo antes da revisão. Memória/estado/matriz actualizados. **Parecer inicial Claude: APPROVE WITH CONDITIONS, 0/0/2**, sobre 09ddfd6. A confirmação incremental R1 foi posteriormente aprovada, conforme o fecho abaixo; não se presume aprovação por existir um pedido. Sem push, PR, merge ou activação. A melhoria bruto/IVA/total no diálogo continua pendente e não foi implementada nesta missão.

## R1 — execução adicional exclusiva de M409-06

Deliberação da Administração AR2, transmitida nesta conversa; textos integrais e proveniência em [ordem original](../../governance/sources/2026-11-06_M4-09_deliberacao-administracao.md) e [ordem R1](../../governance/sources/2026-11-06_M4-09-R1_deliberacao-administracao.md). SHA-256 dos documentos arquivados: original bbdec00fa43ba1a2ff167164d438c1627b5a40e6bc32e1ea4083e238c57543d4; R1 4d89ba3c7585ade0a7ccf13e05c2917da47a9358f617c4af403ba2196f704d0c. Não se inventou identificador GitHub nem foi publicada uma ordem nova. Corrige-se a designação anterior «PO» para deliberação da Administração, preservando o texto recebido.

Parecer original `2026-11-06_TUULI_M4-09_revisao-independente_Claude.md` e `.md.sha256`: conferidos, SHA-256 d01eb6ef1c9dad0d03d793a5dbd123ff2a6cf9ce3981677471de56f676fdcd16. Ambos preservados sem alteração em Relatorios-AR2/TUULI e tuuli-backups/reviews/2026-11-06/M4-09. O incidente do revisor com __pycache__ está relatado no original; não se altera nem omite esse registo.

| Achado | Tratamento R1 |
| --- | --- |
| MINOR-001 | Caso M409-06 fixado antes da execução: quantidade 1/preço 1,005/desconto 50%. Bruto 1,01, desconto 0,51, base 0,50, IVA 0,12, total 0,62. A alternativa sobre bruto cru dá desconto 0,50/base 0,51/total 0,63 e é calculada racionalmente no oráculo. Consolidados, persistência, snapshots e QR I7/I8/N/O conferidos por MockMvc. |
| MINOR-002 | Repostas nas duas linhas canónicas MATRIX.md/M04_STATUS.md: **não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos**; proibição expressa de generalizar a conclusão. |

HEAD ensaiado R1 **1528b1f7e70eca43fb7f9da704b7e4e330e19c8c**, tree **391899d11863c0bddf2df01729460bf7f912b4d8**, limpo antes/depois. Execução **20261106-144506-2844e5**, mesmo comando `python3 docs/planning/m4-09-qr-amounts/run_validation.py`, agora com selecção explícita M409_CASE_FILTER=M409-06 registada em selection.json. O runner exige exactamente um teste e zero ignorados. **1 aprovado, 0 falhas/erros/ignorados, código 0, BUILD SUCCESS**. Os cinco casos anteriores NÃO foram executados; os seus oráculos mantêm o mesmo conteúdo. O recurso contém seis expectativas, mas isto não é uma execução de seis testes. O gerador reproduziu o recurso antes dos pedidos, sem código da aplicação.

QR observado I7=0.50, I8=0.12, N=0.12, O=0.62. Teste e oráculos confirmam o desconto 0,51, distinguindo-o de0,50 sem executar uma aplicação mutada. Não surgiu divergência funcional nem houve alteração src/main. Só uma tentativa R1, bem-sucedida; não há falhas ocultadas. Maven3.9.16/JDK21.0.12.1/PostgreSQL16.3, imagens existentes por digest; comandos/versões/XML/logs arquivados. Não se repetiram unidades, browser, package ou percursos não afectados.

PostgreSQL novo/tmpfs e recursos isolados como na prova inicial, com novo cluster e IDs registados. Remoção conferida pelo runner e consulta posterior somente de leitura: zero contentores/redes ar2.mission=M409-qr-amounts, códigos 0. O suplemento confirma que só M409-06 produziu capturas. Originais e prova inicial permanecem separados.

Arquivo R1: `/home/arosario/tuuli-backups/m4-09/2026-11-06/20261106-144506-2844e5/`, 33 ficheiros original/supplement conferidos. Manifesto original 876deee36c9c9f7d60da3c338013c46d9813090ccb8c2f36f87b5b3089845984; ARCHIVE_MANIFEST.json fc7a901527c14cc7c7a005a4e96ea220c7f07b34c051008f63fcb58eb5ee7080. expected.json R1: 71ec4cbb275fde5adf2ff7b67e38c024edffeb9f5c29f8531d52a1430a7d3fad. Teste: 970b650d2e2a7fda4421111623da10713eeda577d017c92f70b4897fe621095f.

A prova anterior continua: 5 aprovados em 066a984, manifesto aa6e66af…f5e3; não se soma como suite única com esta execução. As observações não condicionantes mantêm os seus limites: recalculoConfirmado=true é decisão explícita do ensaio; a prova não abrange PDF/descodificação de imagem nem valida a regra fiscal. O revisor confirmou os cálculos anteriores, não uma execução independente.

A confirmação incremental foi solicitada apenas sobre os dois MINOR e a proveniência; o resultado está registado abaixo. Sem homologação, generalização, assinatura produtiva, JAR positivo, activação, bases conservadas, push, PR ou merge. O HEAD final documental e equivalência ao HEAD ensaiado constam do relatório externo.

## Conclusão documental após confirmação R1

Novo parecer original `2026-11-06_TUULI_M4-09-R1_confirmacao-incremental_Claude.md`: **APPROVE 0/0/0** sobre a79e86d89ed60981332cfa1261a7d722bd8dc061. SHA-256 integral **921303c688390d528885423dd8c7f37afada2511c2e6271af74ff0835d4bd881**, igual ao sidecar; original e sidecar preservados sem alteração. O digest d01eb6ef…cd16 pertence ao parecer inicial, não à confirmação. Os dois MINOR estão encerrados no alcance técnico aprovado.

O-1: [distinção de fontes](../../governance/sources/2026-11-06_M4-09_PROVENANCE.md) e registo consolidado da issue #46 preparados. Ordens executivas integrais preservadas; acto administrativo original posteriormente recebido e preservado, distinto das instruções executivas e sem identificador externo disponível. O-2: [instruções de selecção](RUNNER_SELECTION.md), sem alteração do runner. O-3: correlacionar XML `[1]` com selection.json e M409-06-*; não inferir o caso do nome XML isolado. O-4: espaços corrigidos nos documentos; não nos textos originais transcritos ou evidências.

Mantêm-se duas execuções distintas, MockMvc/assinatura sintética e todas as reservas nomeadas. O revisor não repetiu os ensaios; conferiu cálculos e capturas. Este fecho não executa testes nem altera a aplicação. A prova técnica parcial está aprovada; M4-09 integral e homologação/uso produtivo não estão concluídos. Preparado para decisão de publicação com a proveniência administrativa explicitada.

### Fonte administrativa original disponibilizada após a clarificação

O utilizador forneceu o texto integral «AR2 | Administração — TUULI AIR / Autorização do mandato M4-09», com origem declarada na mensagem da Administração no chat Executivo TUULI AIR, sem identificador externo disponível. Preservado em [fonte original](../../governance/sources/2026-11-06_M4-09_autorizacao-administrativa-original.md), sem alterar o corpo. SHA-256 do ficheiro com metadados f4fcee16cfdb52313d44316236111581625a44bbcb535dc9a21ce00f8617e57f; corpo 7c82c9cb974407b6ec85aea05d6b2c02611358a05e396879d97416a1d5e651d2. Não se substituem os hashes das ordens executivas anteriores. Pendência de exemplar encerrada; não se afirma autenticação externa nem autorização de publicação nesta tarefa. Nenhum teste/runner/oráculo foi alterado ou executado.
