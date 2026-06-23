# Modelo fiscal histórico e baseline Flyway

## Estratégia de migração

O Flyway é a autoridade sobre a evolução do esquema a partir deste bloco. O Hibernate usa `ddl-auto=validate` e não cria nem atualiza tabelas.

- `V1__baseline_esquema_fac.sql` representa o esquema completo anterior ao Bloco 2. Numa base vazia é executada e cria todas as tabelas, constraints, índices e os dados de referência mínimos.
- Uma base existente, sem `flyway_schema_history`, usa `baselineOnMigrate=true` e `baselineVersion=1`. A V1 não é executada nessa base; o Flyway regista o baseline e aplica apenas versões posteriores.
- `V2__snapshot_fiscal_historico.sql` acrescenta campos de snapshot ao documento e às linhas. Todos são anuláveis para não inventar dados de documentos legados.
- `V3__normalizar_sequencias_identity.sql` alinha as sequências com os identificadores existentes, sem alterar linhas.

Antes de adotar o Flyway numa base real existente deve ser feito um backup e ensaiada a migração numa cópia. `clean` está desativado nos perfis de teste e produção.

## Dados históricos

Na emissão, e na mesma transação, o documento passa a conservar:

- emitente: denominação, NIF, endereço, país, contactos, capital social, matrícula e CAE;
- adquirente: os campos de nome, NIF e endereço já integrantes do documento, atualizados no momento da emissão;
- identificação: tipo e descrição documental, série e descrição, número completo, código AT, ATCUD e QR;
- moeda: código, símbolo e casas decimais; a coluna de taxa de câmbio fica preparada, mas não é preenchida enquanto o FAC não aplicar efetivamente uma taxa ao cálculo;
- linhas: código e unidade do artigo, descrição, tipo de IVA, taxa, base tributável, imposto e total.

`fiscal_snapshot_version=1` só é atribuído a novas emissões. Documentos anteriores mantêm os campos novos nulos e não são considerados fiscalmente consolidados.

O modelo de impressão preserva a propriedade JSON `empresa`, mas o seu tipo passou a ser um DTO de snapshot. Para documentos consolidados, o PDF recebe apenas valores persistidos e não reconstrói ATCUD, QR ou totais.

## Dados de referência

A baseline inclui apenas os valores indispensáveis aos testes e à inicialização: Portugal, EUR, taxas de IVA, regime continental, prazo de 30 dias e a freguesia usada pelos testes existentes. Não inclui clientes, artigos, séries, documentos ou outros dados comerciais de demonstração.

## Limitações preparadas

- O domínio ainda não associa um motivo de isenção a cada linha; por isso não foi inventado qualquer código ou descrição fiscal.
- Não existe um módulo cambial. A taxa histórica é anulável e permanece nula para não apresentar uma taxa do mestre como se tivesse sido aplicada ao documento.
- Relatórios contabilísticos e extratos existentes continuam a usar relações com os mestres; a independência histórica implementada neste bloco aplica-se ao modelo fiscal de impressão comercial.
- Hash, assinatura, certificado real, SAF-T e integrações AT permanecem fora deste bloco.

## Validação local

```powershell
mvn test
```

Para provar o percurso de base vazia, criar uma base terminada em `_test` e executar:

```powershell
mvn -DFAC_TEST_DATASOURCE_URL=jdbc:postgresql://localhost:25432/fac_vazia_test test
```

A proteção central do perfil de testes continua a validar o perfil, a autorização destrutiva e o nome real da base.
