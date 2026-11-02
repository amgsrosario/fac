# H2-06 / issue #43 — Contrato actualmente observado

30-10-2026. Análise exclusivamente documental. Base fixa
`ac07247da7f4dce973b471e5334e2b86bb4e97e4`, merge do [PR #42](https://github.com/amgsrosario/fac/pull/42)
para `feature/visual-redesign`, confirmado no remoto, API e ancestralidade de
`e1e9e63c478d84f52bc508076d438a76b0eca897`. Worktree documental nova:
`codex/h2-06-signature-contract`. O principal permanece em `11d5635`, com
`docker-compose.yml` e `.worktrees/` preexistentes, sem alteração.

Legenda: **F** facto inspeccionado; **N** conteúdo de fonte normativa oficial
consultada, sujeito à validação fiscal indicada; **H** hipótese a testar;
**P** proposta não aprovada. As referências de código abaixo são relativas à
base fixa, não a uma futura branch móvel. [Fontes e limites](SOURCES_REVIEW.md).

## 1. Estado de integração e limites da evidência

- **F:** H2-05 integrada; parecer Claude final original lido integralmente:
  APPROVE, 0/0/0, SHA-256 `d377b2f572fb9f3c95e1b19dc2bc06e3cd7f0eb663842290086b325c21d6d5af`.
  Não equivale a aprovação H2-06 ou a certificação fiscal.
- **F:** API Actions: run `41083075726`, SUCCESS no head `e1e9e63` do PR #42.
  O workflow existente executa apenas `TestDatabaseSafetyValidatorTests` e
  `mvn -B -DskipTests package`; não confundir com os 286 testes PostgreSQL/
  dirigidos locais previamente arquivados e revistos. Não se repetiram testes.
- **F:** não existe fornecedor fiscal produtivo no âmbito de código observado;
  existe apenas o fornecedor demo separado. Não foram inspeccionados segredos,
  infraestrutura produtiva, cadastro de certificação ou serviços externos.
  **Não identificado no repositório não significa inexistente na organização.**
- **F:** estado e memória integrados ainda referiam confirmação R2 pendente.
  A presente actualização documental regista o merge e o parecer final; não
  reabre nem altera H2-05.

## 2. Mapa técnico, sem implementações presumidas

| Evidência na base | F: comportamento observado | Consequência para contrato futuro |
| --- | --- | --- |
| `service/DocumentoComercialService.java:350–398` | Transacção, lock do documento e configuração, validações, EUR obrigatório, totais/snapshots, numeração/ATCUD, QR, estado/instante/emissor, pendente/originação/auditoria | O assinador terá de participar na mesma fronteira e não alterar o circuito financeiro |
| Mesmo ficheiro, `:357`, `:371–388` | Validação de data antecede lock de série; QR antecede `setMomentoEmissao(OffsetDateTime.now())` | H: cronologia concorrente precisa de oráculo sob lock. F: o DTO entregue à fonte não contém ainda o instante final de emissão |
| `repository/SerieRepository.java:15–26`; `service/SerieService.java:107–121` | Lock pessimista por tipo interno/série; incremento transaccional e código AT não vazio | Há base de serialização, mas não estado da cadeia de assinaturas ou prova de comunicação AT |
| `service/FiscalDocumentSignatureSource.java:16–23` | `Signature(documentId, documentNumber, atcud, fullSignature, softwareCertificateNumber)`; `commercial()` opcional; `financial()` vazio por omissão | Não transporta versão de chave, antecessor, bytes canónicos, âmbito do emitente ou perfil criptográfico |
| `service/FiscalQrService.java:25–39,62–74` | Exige exactamente uma fonte; ausência/ambiguidade/vínculo ID-número-ATCUD errado dá 409; demo é caminho separado | Existência de um bean não é autorização para homologar tipos ou iniciar emissão comercial |
| `service/FiscalQrIdentity.java:16–27` | Base64 canónica, pelo menos 31 caracteres; Q nas posições 1/11/21/31; R positivo com 1–4 dígitos | Não verifica criptografia, cadeia, tamanho normativo da assinatura ou atribuição real do certificado |
| `service/QrFiscalPayloadBuilder.java:55–70`; `QrFiscalValidator.java:36–70` | Montantes formatados e Q/R validados sintacticamente; QR comercial construído no momento da emissão | Q/R correctos exigem prova anterior; não são substitutos de Hash/versão da chave |
| `model/DocumentoComercial.java:62–75,471–479` | Persiste código AT, ATCUD, QR/versão e snapshot; QR atribuído uma vez no modelo | Não foi encontrada persistência própria de assinatura completa produtiva/versão de chave/antecessor |
| `model/Serie.java`; migrations V1–V28 | Série tem tipo, identificador, código AT, data/numerador; protecções V25/V26/V27/V28 existentes | Não existe cabeça de cadeia assinada nem revisão de chave por documento nessas estruturas |
| `controller/dto/DocumentoComercialEmitirDto.java` | Pedido contém emissorId e confirmação de recálculo | Cliente não fornece assinatura, certificado, chave ou SystemEntryDate; manter essa fronteira |
| `service/DocumentoComercialPdfService.java:50–68,313–324` | PDF exige documento emitido/anulado e payload consolidado; reimprime esse payload | Hoje não volta a validar assinatura real nem imprime a menção de certificação com base numa prova produtiva persistida |
| `service/DocumentoFinanceiroService.java:100–139,417–426`; `FiscalQrService.java:42–58` | RC legado é circuito distinto; QR opcional, ausência/invalidez pode resultar em vazio | Não atribuir aos recibos o contrato obrigatório de assinatura comercial sem gate RC/RG |
| `service/DocumentoFinanceiroPdfService.java:193–199` | Usa QR persistido; se ausente tenta geração opcional | Um futuro produtor não pode assinar ou emitir durante reimpressão |
| `demo/DemoSignatureSource.java:96–127` | Assina JSON sintético com SHA256withRSA e devolve formato TUULI-DEMO-1, R não fiscal | Não é perfil normativo produtivo; não reutilizar chave, algoritmo ou serialização da demo |
| `financial/FinancialFtOrigin.java:26–40` | Originação existente MANDATORY, flush JPA e escrita FinancialCore por JDBC para FT não imediata/EUR elegível | Provar atomicidade JPA/JDBC com a futura prova; não activar o circuito RC |
| `model/TipoAuditoriaEvento.java` | Emissão, anulação e PDF têm eventos; não há eventos específicos de assinatura/rotação | Auditoria nova exige especificação, não se declara existente |
| `fiscal/catalog/FiscalConfigurationReadiness.java` | Diagnóstico de leitura, inclui DEMO_ARTIFACTS; activação sempre falsa | Não é guard universal chamado pela emissão, nem prova de certificação |

Os caminhos da tabela têm prefixo `src/main/java/com/ar2lda/fac/`, excepto
migrations (`src/main/resources/db/migration/`). Buscas delimitadas por
`HashControl`, `SoftwareCertificateNumber`, `SystemEntryDate` e `SourceBilling`
em Java/migrations não identificaram contrato/exportador SAF-T equivalente.
`IvaSaftService` gere um catálogo de impostos, não comprova exportação fiscal.
Não se fez auditoria geral de módulos sem relação com a missão.

## 3. Conceitos que não se podem confundir

| Conceito | Proveniência / significado | Persistência/consumo pretendido, ainda P |
| --- | --- | --- |
| Assinatura documental / Hash | Resultado do mecanismo fiscal sobre conteúdo canónico; não o SHA do ficheiro PDF | Prova ligada ao documento, íntegra e verificável |
| Q | Quatro caracteres dessa assinatura no caso assinado | Derivado da prova, nunca editável pelo utilizador |
| R | Número do certificado do programa atribuído pela AT, não certificado da empresa/série | Valor aplicável congelado à emissão, sem default fictício |
| Versão da chave / HashControl | Versão do material de assinatura utilizado | Inteiro/referência distinta da versão do QR, snapshot fiscal e catálogo |
| Código AT da série / ATCUD | Identificação da série/tipo e sequência documental | Continua independente da assinatura e do certificado do programa |
| Emitente fiscal | Empresa que emite o documento; snapshot empresarial | Não é o utilizador `emissorId` nem o produtor do software |
| Produtor e custódia da chave | Responsável pelo programa/mecanismo de assinatura | Não equivale a login da empresa ou credenciais AT do cliente |
| Certificado TLS/CSR e credenciais de integração | Autenticam eventual serviço ou comunicação | Rotação operacional não muda automaticamente R ou a versão da chave fiscal |
| SystemEntryDate fiscal | Instante de gravação documental na representação aplicável | Não usar Data Valor ou timestamp de operação financeira como sinónimo |

**N:** Portaria 363/2010, artigos 3.º–6.º, distingue chave do produtor,
certificado do programa, assinatura e versão de chave persistidas; especifica
os cinco elementos ordenados da mensagem e os quatro caracteres impressos.
**N:** QR v1.1 identifica Q e R; o exemplo numérico de R não é credencial.
**N:** Despacho 8632/2014 distingue recibos dos documentos a assinar e prevê
menções impressas e exportação. A aplicabilidade exacta aos recibos TUULI
continua por validar; não se inventa assinatura, Q, R ou equivalência RC/RG.

## 4. Lacunas para produção, não novos defeitos de H2-05

1. Fonte legítima e certificado/versão aplicáveis sem prova organizacional apresentada.
2. Perfil normativo criptográfico/codificação por validar; demo e fixture não servem de oráculo.
3. Falta de armazenamento próprio da prova, versão de chave e encadeamento.
4. Instante final atribuído depois do ponto de geração QR; não pode ser adivinhado pelo fornecedor.
5. Vínculo actual insuficiente para provar montante/conteúdo/scope assinados.
6. Menção impressa e projecção SAF-T ainda sem prova de integração produtiva.
7. Cronologia global/série, concorrência, rotação/restauro exigem testes próprios.
8. Histórico sem assinatura real não pode ser retroassinado ou promovido por inferência.
9. Adicionar fornecedor não pode desbloquear FR/RC/outras tipologias ou gates em aberto.

[Proposta e decisões](PROPOSAL.md) · [Riscos e testes futuros](RISKS_TESTS.md).
