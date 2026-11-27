# C6 — Encerrado sem alteração funcional

**Determinação da Administração, Ordem 09, 27/11/2026.** O encerramento refere-se à ocorrência observada na demonstração sintética da Ordem 08: o comportamento corresponde à regra funcional definida abaixo. A proposta anterior de encaminhamento para FR fica retirada. Não foi determinada nem realizada alteração funcional.

## Regra funcional

O operador escolhe expressamente FT ou FR. A FT constitui um valor a receber, liquidável de imediato ou mais tarde através de **Recebimentos**. A FR pressupõe a confirmação do recebimento integral, gera o movimento financeiro correspondente e deixa o saldo a zero. O meio de pagamento e a condição de pagamento são dados distintos do recebimento efectivo.

## Evidência da Ordem 08

Ensaio pela interface na instalação sintética isolada `127.0.0.1:18172`, com a árvore `61a3ec9f5edcc449a4c4b438791bae904270d636` do merge `d2ab22476cdc1065ea699e01b5e05c8dc6fb6213`. Os artefactos foram compilados a partir de `2031a2b060d7ceabad3ed2694e5b6f7bcf7f8fd9`, cuja árvore é idêntica. Os valores financeiros foram também conferidos em leitura na base do ensaio.

| Percurso | Resultado observado | Capturas |
| --- | --- | --- |
| FT com meio cartão | **FT DEMO26/7**, 123,00 €; nenhum recebimento associado e 123,00 € a receber. | [Cabeçalho](evidencias/c6-cabecalho.png); [pendentes](evidencias/pendentes-antes.png) |
| FR paga no acto | **FRE FR26/1**, 123,00 €; um único movimento de recebimento de 123,00 € e saldo zero, conferidos na base. | [FR emitida](evidencias/FR-emitido.png) |
| FT a cobrar e liquidação posterior | **FT DEMO26/8**, 123,00 € pendentes; **RC DEMO26/3** liquidou 123,00 €, com saldo posterior zero. | [Pendentes antes](evidencias/pendentes-antes.png); [recibo e saldo posterior](evidencias/ft8-rc-liquidado.png) |

A captura da FR demonstra o documento emitido; a contagem do movimento e o saldo foram apurados na base, não nessa captura. Na captura dos pendentes, a coluna de valor a liquidar da FT /8 já foi preenchida para o ensaio do recibo; a FT /7 permanece sem distribuição.

## Limite do ensaio

O único prazo disponível na configuração sintética era **30 dias**. A combinação **FT com prazo «pronto pagamento» não foi ensaiada**; não se infere o seu resultado. A demonstração não constitui validação fiscal ou activação produtiva.

## Integridade das capturas

SHA-256 dos ficheiros copiados sem alteração da execução da Ordem 08:

- `c6-cabecalho.png`: `6bdbc116f575a0d561e469a73eec3bff8a3c89e238ebb52a30c1daca22b10468`
- `pendentes-antes.png`: `8be7cb59162c9c5747172c477389b149b0e35e101cc5d6792f035c2e318de480`
- `FR-emitido.png`: `2851b5c6b07f3a43c2198d9d9d9f094c0cb6b002c02b403579121d99c5de5440`
- `ft8-rc-liquidado.png`: `889a2d027bba80596f4f683ac15cb4a2e6c9ba0dbc34f12c1a363f2b0f98222e`
