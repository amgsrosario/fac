# SAF-T F / FT-EUR R2 — resultado do ensaio

Base confirmada por HTTPS: merge do PR #64, `8d1931d966fd8a201e45d49862b0ee6f55e4cda9`. HEAD ensaiado: `0bccb4cc9448aec218b2714524d6bd45a9a30edc`, tree `4d774fbe3ea946c6c063366454ff62afff1ab6e1`. O commit posterior acrescenta este registo.

| Ponto | Esperado | Resultado observado |
| --- | --- | --- |
| 1a | Aceitar `2026-11-03T23:59:59Z` e exportar `SystemEntryDate=2026-11-03T23:59:59`. | Recusa `cliente_localidade: source missing` antes de avaliar o instante. |
| 1b | Aceitar `2026-07-03T22:59:59Z` e exportar `SystemEntryDate=2026-07-03T23:59:59`; recusar `23:00:00Z` com `SystemEntryDate after DateCreated`. | A primeira tentativa foi recusada por `cliente_localidade: source missing`. A asserção negativa não foi alcançada. |
| 1c | No anulado, aceitar `2026-07-03T22:59:59Z` e exportar `InvoiceStatusDate=2026-07-03T23:59:59`; recusar `23:00:00Z` com `InvoiceStatusDate after DateCreated`. | A primeira tentativa foi recusada por `cliente_localidade: source missing`. A asserção negativa não foi alcançada. |
| 3 | Registar granularidade de data civil, aceitação no próprio dia e condição para eventual comparação por instante. | [Contrato actualizado](SAFT_F_FT_EUR.md#cronologia-e-fuso-da-instalação). A fronteira prevista continua por demonstrar. |
| 4 | Executar a classe completa nos dois modos com o runner integrado. | Sem optimização: 10 testes, 0 falhas de asserção, 3 erros e 0 omissões. O XML Surefire regista `user.timezone=Europe/Lisbon` por omissão. A execução optimizada ficou por iniciar devido à condição de paragem. |

Os três erros ocorreram em `SaftFtExport.export`, linha 153, na leitura obrigatória da localidade. A preparação da nova fixture não forneceu essa localidade, ao contrário das fixtures de exportação positivas já existentes. O ensaio não demonstrou o comportamento temporal nem um defeito nesse comportamento. Os três casos e as respectivas expectativas foram preservados após a recusa.

Arquivo reservado `SAFT-F-FT-EUR/2026-11-20-R2/opt0-20261120-144351-f6288ca88ccc/`. O manifesto, com SHA-256 `421c031fb8d27c4513b2409cc80e883e9c07e24a963cf4087227320b60ba1f01`, liga a fonte, comandos, log, Surefire e prova de remoção. `disposal.json` regista zero contentores ou redes remanescentes e zero erros. O índice do arquivo tem SHA-256 `aa2eb1064b8d6da9bd1d450402dac8465886dc2dd5dd8d6009583993bdaa5f1b`. Sidecar e 18 entradas conferidos no destino.
