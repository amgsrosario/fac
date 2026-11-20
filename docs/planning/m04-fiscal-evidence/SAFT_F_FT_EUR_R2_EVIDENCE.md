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

## Continuação autorizada — run2

O primeiro ensaio permanece no arquivo acima como registo da paragem. A continuação acrescentou à fixture de cronologia a localidade sintética usada pelos casos existentes, antes de gravar e emitir o documento. Os instantes UTC, expectativas e os sete testes anteriores foram preservados. HEAD ensaiado `63ff1f0a8b3f08ff95cc7afc27be3bc63b14b7ad`, tree `e53296ebaad9bdebff716faba056e5b0ccbde537`.

| Prova | Resultado nas três execuções |
| --- | --- |
| 1a | `2026-11-03T23:59:59Z` aceite. `SAFT-R2-winter-entry.xml` regista `SystemEntryDate=2026-11-03T23:59:59`, com `DateCreated=2026-11-03`. |
| 1b | `2026-07-03T22:59:59Z` aceite e exportado como `2026-07-03T23:59:59` em `SAFT-R2-summer-entry.xml`. `23:00:00Z` recusado com `SystemEntryDate after DateCreated`, antes do validador. |
| 1c | No anulado, `2026-07-03T22:59:59Z` aceite e exportado como `2026-07-03T23:59:59` em `SAFT-R2-summer-status.xml`. `23:00:00Z` recusado com `InvoiceStatusDate after DateCreated`, antes do validador. |
| 3 | O [contrato](SAFT_F_FT_EUR.md#cronologia-e-fuso-da-instalação) regista a decisão da Administração sobre datas civis. Os dois pares de Verão, também confirmados com a JVM em UTC, demonstram a aplicação do fuso Europe/Lisbon da instalação. |
| 4 | Runner integrado, PostgreSQL descartável, assinatura e dados sintéticos. Cada Surefire confirma 10 testes e zero falhas, erros ou omissões. Cada `disposal.json` confirma zero contentores e redes remanescentes. |

Arquivo `SAFT-F-FT-EUR/2026-11-20-R2-run2/`. O fuso efectivo indicado abaixo é `user.timezone` do XML Surefire. A terceira execução usa `TZ=UTC`, já admitido pelo runner. Cada manifesto tem sidecar.

| Execução | Optimização | Fuso JVM | SHA-256 do manifesto |
| --- | --- | --- | --- |
| `opt0-20261120-150452-1ad8d6762980` | 0 | Europe/Lisbon | `0afb9cb12817526ef3b2c84db8bdb83010c64dfe884b9bbe18f980d0cb6f4313` |
| `opt1-20261120-150602-9c5feabbc0f3` | 1 | Europe/Lisbon | `596ad2dc2234fa7ba99df05663a423d551bf2b1f1e07652484dd288bdd313650` |
| `opt0-20261120-150657-1d571c7d10cb` | 0 | UTC | `09ea67bf7e985d5cea63cc4a36a600922bed9fe659fe40bfdc832de00ec99c9c` |

Índice novo `10921340c4f7121654fe53fce33273068fd15e330d563ea65d2816a44ce83a4d`, com sidecar e 66/66 entradas conferidas. O índice original `aa2eb1064b8d6da9bd1d450402dac8465886dc2dd5dd8d6009583993bdaa5f1b` e as suas 18 entradas foram reconferidos.
