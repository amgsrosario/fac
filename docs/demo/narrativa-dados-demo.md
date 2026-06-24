# Narrativa dos dados de demonstração

## Empresa

**Alentejo Sabores, Lda.** é uma empresa fictícia que comercializa azeites, vinhos, cabazes e serviços associados. Todos os NIF, endereços, emails e códigos do cenário são fictícios.

## Clientes

| Cliente | Papel na história |
|---|---|
| Mercearia Campo Dourado, Lda. | Compra nacional e fatura totalmente liquidada |
| Sabores de Madrid SL | Exemplo de cliente espanhol dentro dos limites fiscais atuais |
| Consumidor Final Demo | Destinatário do documento longo de 35 linhas |
| Casa dos Sabores do Sul, Lda. | Duas faturas, um recebimento e extrato com três movimentos |
| Cliente Documento Anulado, Lda. | Evidência de anulação documental preservada |

## Artigos e serviços

O catálogo contém oito itens: dois azeites, dois vinhos, um cabaz, preparação de cabaz, transporte nacional e transporte intracomunitário. Os preços e enquadramentos foram escolhidos para criar totais e impostos visualmente distintos; não representam uma tabela comercial real.

## Documentos

| Referência | História |
|---|---|
| FT DEMO26/1 | Primeira fatura da conta corrente demonstrativa |
| FT DEMO26/2 | Fatura com recebimento parcial |
| FT DEMO26/3 | Fatura totalmente liquidada |
| FT DEMO26/4 | Documento anulado com motivo e autor |
| FT DEMO26/5 | Venda ao cliente espanhol |
| FT DEMO26/6 | Documento de 35 linhas para paginação do PDF |
| RC DEMO26/1 | Recebimento parcial associado à FT DEMO26/2 |
| RC DEMO26/2 | Recebimento integral associado à FT DEMO26/3 |

## Perfis

- `admin.demo`: configuração, operação, anulação e auditoria.
- `operador.demo`: operação comercial sem poder anular.
- `consulta.demo`: consulta sem ações de alteração.

O seed executa as emissões, os recebimentos e a anulação em nome de `admin.demo`. A auditoria inclui ainda uma tentativa demonstrativa de anulação recusada a `operador.demo`, permitindo explicar a diferença entre ocultação na interface e proteção efetiva no backend.

As passwords nunca pertencem à narrativa nem ao repositório; são definidas por variáveis de ambiente antes da reposição.
