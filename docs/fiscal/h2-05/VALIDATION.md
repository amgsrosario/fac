# H2-05 — Evidência de validação local

28-10-2026. Java 21, Maven 3.9.16 offline, ZXing 3.5.3. Testes executados pelo
Codex sobre a implementação local; não são execução de CI ou testes do Claude.

## Resultado consolidado final

| Classe | Executados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: |
| DocumentoComercialPdfServiceTests | 13 | 0 | 0 | 0 |
| DocumentoFinanceiroPdfServiceTests | 3 | 0 | 0 | 0 |
| QrFiscalPayloadBuilderTests | 4 | 0 | 0 | 0 |
| QrFiscalValidatorTests | 3 | 0 | 0 | 0 |
| DocumentoFiscalQrTests | 3 | 0 | 0 | 0 |
| FiscalQrIdentityTests | 13 | 0 | 0 | 0 |
| QrCodeImageServiceTests | 8 | 0 | 0 | 0 |
| FiscalQrServiceTests | 10 | 0 | 0 | 0 |
| DocumentoComercialControllerTests | 46 | 0 | 0 | 0 |
| DocumentoComercialEmissaoIntegridadeTests | 26 | 0 | 0 | 0 |
| FinancialReceiptIntegrationTests | 80 | 0 | 0 | 0 |
| FinancialReconciliationIntegrationTests | 22 | 0 | 0 | 0 |
| **Total dirigido + regressão** | **231** | **0** | **0** | **0** |

As execuções intermédias de 31, 50 e 53 testes não se somam ao total final.
Não se executou a suite integral da aplicação. Não se executou frontend,
inalterado neste incremento.

Comando dirigido: `mvn -o -B -Dtest=QrCodeImageServiceTests,FiscalQrIdentityTests,FiscalQrServiceTests,DocumentoComercialPdfServiceTests,DocumentoFinanceiroPdfServiceTests,QrFiscalPayloadBuilderTests,QrFiscalValidatorTests,DocumentoFiscalQrTests test`.
Regressão: um `mvn -o -B -Dspring.profiles.active=test -Dspring.jpa.show-sql=false -Dtest=<classe> test`
por cada uma das quatro últimas classes, com base descartável independente.
Variáveis `FAC_TEST_DATASOURCE_*` foram fornecidas sem arquivar o segredo.

## PostgreSQL e integridade

PostgreSQL 16.3 descartável em tmpfs, contentor `tuuli-h205-26a98286`, porta
loopback 54430, cluster `7701746512381100071`, bases terminadas em `_test`.
Identidade conferida antes da execução; não corresponde à origem histórica
`7689035507522658342` nem ao destino isolado da issue #38.
Migrations existentes aplicadas nas bases novas; nenhuma migration criada ou
alterada. V25/V26/V27/V28 permanecem intactas. Não é ensaio de upgrade de uma
base persistente nem autorização para executar migrations na demo.

Rejeição de assinatura inválida e ausência de fonte verificam rollback integral,
incluindo rascunho sem dados fiscais e numerador. A regressão financeira testa
os contratos existentes sem activar o circuito. O manifesto continua vazio.

## PDF final e oráculos

`pdfFinalMantemAtcudModoVersaoDimensoesEMargem`: três casos, 1/35/90 linhas,
incluindo documento anulado e várias páginas. Guarda PDF e cada página PNG a
300 dpi. Verifica texto ATCUD em todas as páginas e posição acima do QR.
Inspecciona transformações da imagem no PDF, não apenas CSS: 40 × 40 mm,
símbolo activo ≥30 mm, margem branca ≥2,5 mm. Descodifica matriz para versão
≥9 / ECC M / um segmento Byte e payload exacto; volta a descodificar a página
renderizada a 300 dpi. Primeira e última página do PDF de 35 linhas também
inspeccionadas visualmente pelo executor, sem sobreposição observada.

Medição independente dos três PDFs: 1, 2 e 4 páginas; imagem 40,005 mm,
símbolo activo 34,758 mm, margem mínima 2,623 mm, versão 9/M/Byte.
Estes PDFs usam o payload sintético de reimpressão da fixture preexistente
(`Q:TESTE`, sem R); são oráculos de geometria/preservação de texto, não exemplos
de payload fiscal homologado. A derivação Q/R válida e por documento é testada
separadamente em `FiscalQrServiceTests`, com fonte sintética explícita.

Casos dirigidos adicionais cobrem números, maiúsculas, payload curto/normal,
Unicode com ECI, crescimento de versão e capacidade excedida. Q/R incluem
índices exactos, assinatura/certificado ausentes ou inválidos, origem ambígua
e vínculo ID/número/ATCUD incoerente.

`FiscalSignatureTestConfiguration` é exclusivamente fixture de teste importada
explicitamente: chave RSA efémera em memória e certificado sintético 1234.
Não é produtor fiscal, não representa certificado AT real e não é incluída
no JAR produtivo. Os testes demonstram consumo e rollback, não certificação,
persistência real da assinatura ou validade de uma cadeia fiscal.

## Preservação

Arquivo local: `/home/arosario/tuuli-backups/h2-05/2026-10-28/`.
`directed-final/` contém log/XML/resumo dos 57; `regression/`, os 174 com
comandos e bases por classe; `pdf-final/`, PDFs/PNGs efectivamente gerados;
`package.log`, package backend; `SHA256SUMS`, integridade dos artefactos.
O manifesto não inclui o seu próprio hash. Credenciais descartáveis e chaves
privadas não integram a evidência. Não se publicou o arquivo no GitHub.

Revisão Claude pendente. [Limitações e fonte fiscal em falta](IMPLEMENTATION.md).


## R1 — confirmação local após alternativa B (28-10-2026)

Parecer original Claude: APPROVE WITH CONDITIONS, 0 BLOCKER / 1 MAJOR /
3 MINOR. Decisão executiva escolheu fornecedor demo separado.
[Resolução dos achados e reservas](R1.md).

- **74 testes dirigidos**: os 57 originais, mais dois cenários PDF, treze do
  fornecedor/arranque demo e dois DemoResetSafetyTests. Zero falhas/erros/ignorados.
- **180 testes de regressão**: DocumentoComercialControllerTests 46,
  DocumentoComercialEmissaoIntegridadeTests 26, PermissionMatrixIntegrationTests 6,
  FinancialReceiptIntegrationTests 80, FinancialReconciliationIntegrationTests 22.
  Zero falhas/erros/ignorados na selecção final. Total **254**, não suite integral.
- PermissionMatrixIntegrationTests passou após importar a fonte exclusiva de
  testes e usar o helper de vínculo governado V28 na fixture. As asserções de
  acesso não foram alteradas. O manifesto produtivo continua vazio.
- Sete cenários **adicionais, fora da contagem JUnit**, pelo JAR empacotado:
  seed e oito PDFs por API autenticada; repetição do seed sem duplicação nem
  avanço de numeradores; recusa com perfis demo+prod; recusa de base errada
  antes de migrations, confirmando zero tabelas; recusa de cluster errado;
  recusa de código AT não demonstrativo; reprodução do reset/seed numa base
  fac_demo nova no mesmo cluster exclusivamente descartável.
- Package offline e smoke QR executado via PropertiesLauncher do JAR: ZXing
  3.5.3, Byte, ECC M, versão ≥9, descodificação exacta, sem arranque Spring.
- Oito PDFs **efectivamente emitidos**, 16 páginas, aviso sem validade fiscal
  em todas. Os oito QR foram descodificados das páginas a 300 dpi: versão 19,
  ECC M, Byte, símbolo activo mínimo 34,771 mm, margem mínima 2,617 mm.
  Aviso na primeira página do documento multipágina inspeccionado visualmente.

PostgreSQL 16.3 descartável: cluster `7701754504284078125`, contentor
`tuuli-h205r1-18b86ac7`, tmpfs e porta loopback 50275. Recursos removidos após
a validação; credenciais e todas as chaves privadas geradas também removidas.
Sem operações na origem histórica, destino #38 ou demo persistente.

Arquivo separado `/home/arosario/tuuli-backups/h2-05/2026-10-28-r1/`:
`consolidated.json`, `directed-post-demo-fix/`, `regression-r1/`,
`permission-fixed/`, `jar-demo-verified/`, `demo-pdf-summary.json`,
`demo-pdf-inspection.log`, `package-verified.log`, `jar-smoke-verified.log`,
`jar-integrity.json`, `cleanup.json` e manifesto SHA256SUMS. As tentativas
intermédias falhadas também permanecem no arquivo; a consolidação substitui
apenas o resultado falhado de permissões pela sua reexecução corrigida.

Os ensaios de reset foram pelo JAR e bases novas, não pelos scripts de deploy
Linux/Windows nem pela demo Ubuntu. O Compose existente não foi alterado:
para eventual utilização futura será necessária configuração explícita do
fornecedor, identidade do cluster e montagem segura da chave; nenhum opt-in
ou deployment é efectuado por este commit. Sem credenciais comerciais,
comunicação AT ou homologação. A confirmação incremental Claude fica pendente.
