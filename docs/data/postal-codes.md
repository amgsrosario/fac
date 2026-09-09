# Códigos postais portugueses

O catálogo previsto para o FAC utiliza o ficheiro `PT.txt` dos GeoNames Postal Code files (dataset PT). A licença declarada no `readme.txt` do pacote é **Creative Commons Attribution 4.0 (CC BY 4.0)**.

O ficheiro original (`PT.zip`) ainda não é distribuído no repositório. Quando a importação for implementada, a documentação deve manter a atribuição à fonte GeoNames e apontar para <https://www.geonames.org/> e para os termos da licença <https://creativecommons.org/licenses/by/4.0/>.

O modelo separa o código postal (`codpostal`) das localidades alternativas (`codpostal_localidade`). A carga futura deve validar códigos `NNNN-NNN`, deduplicar por código, preservar todas as associações e produzir um relatório idempotente.

## Importador

O importador é uma operação administrativa explícita. Não corre no arranque normal,
no seed demo ou numa migration Flyway. O wrapper recebe o caminho do ZIP e não
assume uma localização fixa:

```bash
./scripts/data/import-postal-codes.sh /caminho/para/PT.zip
```

Este modo valida o ZIP, o `PT.txt` UTF-8 e as 12 colunas, agrupa por código,
reporta linhas inválidas e colisões geográficas e não escreve dados. A importação
real exige ainda o perfil `demo`, a base `fac_demo` e uma confirmação separada:

```bash
FAC_POSTAL_IMPORT_CONFIRM='IMPORTAR CATALOGO POSTAL' \
  ./scripts/data/import-postal-codes.sh /caminho/para/PT.zip --import
```

O modo real usa transação, lock advisory PostgreSQL e batches de 1.000 códigos.
Faz upsert de `codpostal`, substitui as localidades desse código e insere todas
as localidades distintas em `codpostal_localidade`. A localidade principal é a
menor designação em ordenação Unicode estável; todas as restantes, incluindo a
principal, são preservadas na tabela filha. Uma segunda execução é idempotente.

Qualquer código associado a mais de um distrito, concelho ou freguesia impede a
carga: não é feita uma escolha silenciosa. Os três códigos demo existentes são
tratados apenas quando a carga real for deliberadamente executada; `28000` não
é um código postal português válido e não é criado pelo catálogo.

O dataset PT não é versionado. A atribuição GeoNames e CC BY 4.0 deve permanecer
associada a qualquer catálogo importado.
