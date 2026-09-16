# Catálogos de referência TUULI

Estes dados são estruturais, não dados de demonstração. O schema é gerido por
Flyway; a carga dos catálogos deve ser executada explicitamente na criação ou
reposição de uma base, antes da configuração base e do cenário demo. Não deve
ser executada no arranque normal da aplicação.

## Códigos postais

- Origem original declarada: GeoNames, dataset português.
- Snapshot inicial TUULI aprovado: tabela `codpostal` da base local `fac`.
- Total esperado para a exportação integral: 197.772 registos.
- Snapshot integral: `codigos_postais_portugal.csv`, exportado da base local
  `fac` após confirmar os 197.772 registos.
- Não usar os códigos pontuais de `demo-base.sql` como substituto do catálogo.

## Freguesias

- Fonte documental: `sources/TabelaFreguesias (1).pdf`, obtida da Autoridade
  Tributária e Aduaneira.
- Dataset normalizado: `freguesias_portugal.csv`.
- Total: 5.188 registos, incluindo 3.093 ativos e 2.095 extintos.
- A chave `codigo` preserva zeros à esquerda e segmentos alfanuméricos.
- Freguesias extintas pertencem ao catálogo integral.

## Carga explícita

Depois de aplicar as migrations Flyway à base de destino, executar
`scripts/database/load-reference.ps1 -Database NOME` em Windows ou
`scripts/database/load-reference.sh NOME` em Linux, indicando container e
utilizador da base quando forem diferentes dos valores locais. A carga usa
`COPY` para tabelas temporárias, verifica contagens e compatibilidade dos
dados existentes e insere apenas chaves ausentes numa transação. Não altera
dados divergentes automaticamente e não corre no startup normal.
