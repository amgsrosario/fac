# H2-07 — validação do corte inactivo

30-10-2026, Europe/Lisbon. [Contrato e limites](CONTRACT.md).
Revisão independente Claude: **pendente**, não substituída por auto-aprovação.

## Ambiente

- Base real consultada por HTTPS: `ac07247da7f4dce973b471e5334e2b86bb4e97e4`.
- Worktree `h2-07-signature-isolated`, independente do trabalho não atribuído.
- PostgreSQL 16.3, contentor criado exclusivamente para esta missão:
  `tuuli-h207-isolated-20261030`, ID
  `2840aa4f3f62b773910f97ccd0e08920a431d2c211c84be41ec7a9a15dbcf93d`.
  Porta `127.0.0.1:50584`, dados tmpfs, sem volume persistente. Autenticação
  trust somente neste recurso descartável, sem dados ou credenciais comerciais.
- URL de testes `jdbc:postgresql://127.0.0.1:50584/h207_isolated_test`.
  Testes de persistência criam e removem base própria `h207_<UUID>_test`;
  nunca ligam à origem `fac/facdb` ou ao destino #38.
- Java 21, Maven offline; nenhuma dependência nova. Chaves privadas somente
  geradas em memória para os testes; não arquivadas nem versionadas.

## Resultados efectivos

| Classe | Testes | Falhas / erros / ignorados no resultado final |
| --- | ---: | --- |
| TrialCanonicalTests | 11 | 0 / 0 / 0 |
| TrialPersistenceTests | 18 | 0 / 0 / 0 |
| FiscalQrServiceTests | 10 | 0 / 0 / 0 |
| FiscalQrIdentityTests | 13 | 0 / 0 / 0 |
| DemoSignatureSourceTests | 13 | 0 / 0 / 0 |
| GovernedFtPersistenceTests | 34 | 0 / 0 / 0 |
| DocumentoComercialControllerTests | 46 | 0 / 0 / 0 |
| DocumentoComercialEmissaoIntegridadeTests | 26 | 0 / 0 / 0 |
| PermissionMatrixIntegrationTests | 6 | 0 / 0 / 0 |
| DocumentoComercialPdfServiceTests | 17 | 0 / 0 / 0 |
| Total distinto validado | **194** | **0 / 0 / 0 finais** |

**Não é uma execução única sem erros:** a execução das dez classes aprovou os
165 testes de regressão, mas dois testes novos tiveram erro por não reconhecerem
a tradução Spring de SQLSTATE 40001. Corrigida apenas essa expectativa de teste,
repetiram-se as duas classes novas: 29 aprovados, zero falhas/erros/ignorados.
Não somar repetições como testes únicos. Os XML mais recentes e os XML anteriores
preservados distinguem as execuções. Não se executou a suite completa FAC.

## Oráculos e garantias demonstradas

- Texto literal UTF-8 `2026-10-30;2026-10-30T12:34:56;FT H207TTEST/1;123.40;`,
  independente de locale. Valores com perda de precisão e famílias fora de FT
  posterior/EUR recusados. Não é validação fiscal da combinação 2/1/1.
- RSA 1024 bits; assinatura 172 Base64 sem quebras. O teste inspecciona o
  resultado de RSA/PKCS#1 v1.5 e compara DigestInfo SHA-1 explícito com digest
  dos bytes, além de testar adulteração e chave pública errada. Não é um
  certificado real, validador AT ou prova de vigência produtiva.
- Cadeia, replay, conflito de payload/série, scope distinto, continuidade
  plurianual, data regressiva, relógio global e rotação não autorizada.
- Corridas reais de duas threads: replay, dois appends e ordens opostas de
  séries. READ COMMITTED confirma cadeia linear; REPEATABLE READ/SERIALIZABLE
  abortam um concorrente com SQLSTATE 40001, sem dois heads incompatíveis.
- JPA testemunho + JDBC prova/número/relógio revertidos na mesma transacção;
  ligação autocommit recusada. Não se testou integração nova com emissor real.
- SQL directo: alteração/eliminação/truncate, sequência e mensagem forjadas;
  constraints antecipadas/diferidas não contornam a imutabilidade.
- Teste explícito da limitação criptográfica SQL: assinatura estruturalmente
  válida mas falsa armazenada por SQL é recusada em leitura e novo append.
- Releitura após perda da chave efémera, sem reassinar; projecção NONFISCAL,
  ausência de registo Spring/FiscalDocumentSignatureSource e recusa de URL
  produtivo em mock, sem ligar à origem.
- Migrations produtivas V1–V28 aplicadas/validadas; instalação separada do
  schema de ensaio, validate e segundo migrate sem alterações. V25–V28 intactas;
  nenhum catálogo fiscal publicado. Migration V29 desconhecida não executada.

## Comandos e iterações

Ambiente das execuções PostgreSQL:
`FAC_TEST_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:50584/h207_isolated_test`,
`FAC_TEST_DATASOURCE_USERNAME=postgres`, password vazia apenas no recurso trust.

1. `mvn -o -q -Dtest=TrialCanonicalTests,TrialPersistenceTests,FiscalQrServiceTests,FiscalQrIdentityTests,DemoSignatureSourceTests,GovernedFtPersistenceTests,DocumentoComercialControllerTests,DocumentoComercialEmissaoIntegridadeTests,PermissionMatrixIntegrationTests,DocumentoComercialPdfServiceTests test`
2. Após correcção da expectativa SQLSTATE: `mvn -o -q -Dtest=TrialCanonicalTests,TrialPersistenceTests test` — sucesso.
3. `mvn -o -DskipTests package` — resultado no relatório de execução externo.
4. `git diff --check` e referências relativas Markdown.

Iterações iniciais preservadas, sem as ocultar: socket recusado pelo sandbox
(repetido com acesso autorizado só ao recurso efémero), configuração do fixture
JPA sem dialecto partilhado (detectou prova sobrevivente ao rollback), suporte
Hibernate a isolamento e tradução de 40001. A biblioteca passou a recusar JDBC
autocommit. Os resultados finais acima são posteriores às correcções.

Evidência técnica: `target/h207-evidence` e arquivo reservado H2-07 com logs,
XML, metadados Git/recursos e manifesto SHA-256, identificados no relatório externo.
CI remoto, se existente, tem âmbito próprio e não comprova estes 194 testes.

## Pedido de revisão independente

Rever a branch contra `ac07247`, distinguindo os dois commits documentais H2-06
aprovados da implementação H2-07. Confirmar a não inclusão de `7ce573f`, a
localização Flyway separada e a ausência de ligação ao emissor produtivo.
Inspeccionar especialmente transacções, guardas SQL, cadeia/cronologia,
serialização, aceitação de 40001, canonicalização, limites da prova e chaves
só de ensaio. Verificar as evidências, incluindo falhas iniciais e repetições.
Emitir findings por severidade, sem homologar ou activar. PR deverá permanecer
draft/sem merge até revisão e decisão da Administração. D-SIGN-02..05 pendentes.

## H2-07-R1 — evidência única fixada ao commit

02-11-2026. Parecer inicial Claude APROVADO COM RESERVAS, 0/0/3,
[ordem incremental #44](https://github.com/amgsrosario/fac/issues/44#issuecomment-6575613728).
A tabela de 30-10-2026 acima é histórica e não substitui a evidência R1.

MINOR-001: `tools/validation/h207_r1.py` exige HEAD limpo e datasource localhost
explícito com base `_test`; executa as dez classes/194 casos numa única chamada
Maven, preserva logs completos e XML, valida contagens/ausência de ignorados,
regista HEAD/tree/status antes e depois e exige igualdade. Executa também
package e inspecção do JAR. Os artefactos ficam fora de target/worktree e são
manifestados com tamanho/SHA-256, inclusive em falha. Um commit fixo contendo
a R1 é criado **antes** da execução. O relatório externo e a issue #44 indicam
o SHA exacto e o resultado efectivo; não encadear commits autorreferenciais.
Se houver correcção de código/testes, criar novo commit e repetir a chamada.

MINOR-002: RSA contra a chave pública armazenada na própria prova verifica
coerência, não proveniência. Não há âncora de confiança independente; uma
prova auto-consistente produzida com chave de atacante não é autenticada por
esta arquitectura. O desenho produtivo exige chave/keyVersion e âmbito em
registo confiável separado e protegido, com autorização/proveniência e rotação
verificáveis. Não se afirma que os testes de adulteração provem essa confiança.

MINOR-003: biblioteca e SQL passaram para âmbito Maven de testes. Build limpo
elimina classes/resources antigos de target; o runner verifica ausência desses
artefactos e de referências das classes produtivas no JAR. Não foi introduzida
migration produtiva ou excepção aos gates. A mudança de localização não muda
a semântica dos ensaios.

Confirmação incremental Claude pendente: rever exclusivamente o complemento
sobre `eff724f`, os artefactos da execução única e a ausência experimental no
JAR. O resultado do executor não equivale a aprovação independente. Preservados
409, gates fiscais, obra de autoria desconhecida e ausência de activação/merge.
