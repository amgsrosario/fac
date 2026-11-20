# SAF-T F / FT-EUR — confirmação incremental R1

Revisor independente solicitado: Claude Code. Comparar o HEAD revisto `4e3dd5f331d122f97b034c5174edbf36775428eb` com o código ensaiado `4f849d9d5fc56823b42c9e44da05a3d0fec88dbf` (tree `49ce58c44f74f9a9412fe5e0553797a9796c0d3d`). Este registo documental é posterior ao ensaio.

| Achado | Alteração e prova a conferir |
| --- | --- |
| MAJOR-001 | Classe `ProjectedFrSaftFtExportTests`, ordens 1, 4, 5 e 6. Conferir a exclusão de uma FT emitida fora do período, a recusa de `Hash` divergente e de `HashControl` incorrecto antes do validador XML, autor/instante/motivo da anulação, contagem e exclusão dos anulados dos totais. No caso contratual, quantidade 3 a EUR 10, desconto de linha EUR 3 e parcela de cabeçalho EUR 2 produzem líquido EUR 25, preço unitário 8,333333333333 e desconto total EUR 5. Conferir o resíduo da divisão e a recusa quando faltar a repartição explícita. |
| MINOR-001 | Ordem 4. Conferir os diagnósticos distintos para `SystemEntryDate` e `InvoiceStatusDate` posteriores a `DateCreated`. A comparação é pela data civil no fuso da instalação, com instantes de emissão e anulação fixados na fixture. Eventos no próprio dia permanecem admissíveis. |
| MINOR-002 | Conferir manifestos, estado Git antes/depois, XML Surefire, comandos, fontes `ProjectedFrSaftFtExportTests.java` e `validate.py`, caminho e digest do XSD, Python e dependências. O validador executou com optimização activa. Os controlos negativos autónomos recusaram um XSD alterado e uma versão de biblioteca divergente em modo optimizado. |

## Responsabilidades e limites

O fornecedor `Evidence` resolve o material histórico independentemente da assinatura candidata. Confirma `HashControl` contra a versão de chave do documento e verifica `Hash` contra o payload original e a chave pública histórica. O exportador exige ambos os resultados e recusa com diagnóstico por documento antes de devolver XML. A fixture usa assinatura e chave sintéticas. A repartição de cabeçalho é uma evidência explícita da fixture e a implementação recusa a sua ausência quando os totais divergem. A origem comercial da repartição, a cadeia de confiança produtiva e os restantes gates P12 e D-SIGN continuam pendentes.

A classe completa reutiliza a prova de snapshot concorrente numa transacção `REPEATABLE READ` só de leitura. As datas fixas e o caso de desconto usam registos sintéticos na base descartável. A aceitação XSD é verificação estrutural e os controlos de conteúdo são separados.

## Evidência

- Arquivo reservado: `SAFT-F-FT-EUR/2026-11-20-R1`. A execução original da implementação permanece no arquivo de 19/11.
- Execução de referência `20261120-101759-12b9e9`: 7 testes aprovados, sem falhas, erros ou omissões. Manifesto SHA-256 `f96839c5f352e6ecac22d723b9d406ebf911e1ef3bc05d173d3ac69828450546`; 21/21 ficheiros verificados no destino. PostgreSQL 16.3 com Flyway V32, Java 21.0.13.1 e Maven 3.9.16. O registo final confirma a remoção dos contentores, redes e segredo temporário da execução.
- A primeira execução R1, `20261120-101623-e40c70`, também passou os 7 testes, mas a optimização estava activa no runner e desactivava as suas asserções Python. Foi preservada separadamente (21/21 ficheiros; manifesto `8dc8817ad5a68685a2c382072aa6b78a3011d2423791a0494f5e4b8a5cd93fa9`). Na execução de referência, a optimização foi aplicada exclusivamente aos subprocessos do ensaio, incluindo o validador.
- Fonte Java SHA-256 `9bf16a4694ac96ecb46525acf9e6f6a30f9961cfc179b98e4e4576804a5832f0`; validador SHA-256 `1b904f649f4c0a3f6f84c68cb3423fef2df2288a1ee4656e9eb049b63124cc25`.
- XSD `/home/arosario/tuuli-backups/m04/2026-11-04/saftpt1.04_01.xsd`, SHA-256 `292c0ab4611e3f5a0cdf2abb4e62d9bd41254dc2e76a1fae4d35a8b132d79350`. Validador `/usr/bin/python3` 3.14.4, `xmlschema` 4.3.0, `elementpath` 5.1.1; os logs registam `optimize=1`.
- Controlos negativos em `optimized-negative/`, ambos com saída 1: `wrong-xsd.log` (`7c9c03eb1e6d5ec95b940b30c1bc2029c15c29d73bc3fa2b2cc71c09e4c782b1`) após acrescentar uma quebra de linha a uma cópia do XSD e `wrong-version.log` (`f5ce504817478415c9c764d65eb98f5372e3a3cad38f39c5f606aafe32c83a5f`) após simular `elementpath.__version__ = "0"` no processo de validação.

Solicita-se confirmação incremental dos três achados. Revisão independente e decisão administrativa de integração pendentes.
