# Fontes do inventário executivo — 02-11-2026

Base `051b1fc1eb78e5c0c81c51b77d0d2f94bde20435`. Leitura dirigida, sem testes,
SQL, manipulação de dados ou execução de ferramentas da #38. Datas de provas
anteriores são mantidas; integridade de arquivo não demonstra estado SQL actual.
Caminhos Java são relativos a `src/main/java/com/ar2lda/fac/`.

| ID | Fonte verificável | Alcance e limites |
| --- | --- | --- |
| E01 | [Repriorização #46/6584848804](https://github.com/amgsrosario/fac/issues/46#issuecomment-6584848804); HEAD remoto HTTPS 051b1fc; Git merge/ancestralidade 2523095 | Ordem actual e integração documental H2-08; não decide D-SIGN nem activa nada. Principal em 11d5635, origin tracking antigo, ficheiros preexistentes preservados |
| E02 | [Ordem AR2 privada e74f45b](https://github.com/amgsrosario/AR2-Executive-Reports/blob/e74f45b12e4895f675d72787882ddba31448f7ea/TUULI/2026/10/2026-10-26_TUULI_AR2_ordem-desbloqueio-executivo_e_handoff.md), blob e97217f52f9358cf56b736c6a40e7f343af39d7f, lida integralmente | Alternativa B aprovada para preparação isolada; origem fac/facdb intocável; nomes finais não autorizam cutover; não voltar a pedir escolha A/B |
| E03 | Relatório `2026-10-27_TUULI_Issue38_restauro-e-prontidao-operacional_Codex.md`, 16078 bytes, SHA-256 98a7da3b73460a94264b263edf38767e071c91a0512655c52883b8be342c7855; parecer R2 original, 8454 bytes, SHA-256 a8f3cd7681f07cff703ae977183b795370eea47e09cba133c5fd985c2a131411 | Ambos lidos; SHA do relatório coincide com o declarado no R2 (sem sidecar local encontrado); parecer coincide com sidecar. Restauro V27, cinco testes, agregados e reserva MINOR-003R, prova de 27-10 |
| E04 | [Fecho #38](https://github.com/amgsrosario/fac/issues/38#issuecomment-6446559236), [errata cc67e3f](https://github.com/amgsrosario/AR2-Executive-Reports/blob/cc67e3fed235628e238a65445b47866bd7904bde/TUULI/2026/10/2026-10-27_TUULI_Issue38_R2_errata-metadado-restauro.md), blob 4bba83cf7db49e2e31873fe7dbcf0c23e584cd33 | Restauro não continua pendente. Errata resolve só metadado temporal; MINOR-003R operacional mantido. A menção histórica H2-03 pendente foi superada pela implementação posterior |
| E05 | PR #40 merge 91da67bc5e799d30abfd1c569ec7ef22fbd5f703 ancestral do HEAD; `docs/fiscal/catalog/H2_03_VALIDATION.md`; `service/TipoDocumentoService.java:47–61,73–104`; V28; `src/main/resources/fiscal/catalog.json` | V28 e contrato Java/SQL integrados; 318 casos em 15 invocações/base por classe na consolidação anterior, não uma chamada integral. Manifesto actual revisions=[]; não há homologação/publicação real pela migration |
| E06 | `docs/security/SECURITY_GATE.md`, OPERATIONAL_STATE secções JWT/Permissões/Security Gate; merges #8/#9/#12/#14 ancestrais; `security/SecurityConfig.java:86–102`, `SecuritySettings.java`, `service/UtilizadorService.java:48–67`, `controller/UtilizadorController.java:35–39` | CRUD autenticado, BCrypt/controlo de sessão e testes entregues. Pesquisa restrita de bootstrap/runners em security/config/service/demo só identificou runners demo, não bootstrap comercial. Não prova inexistência de ferramenta externa |
| E07 | PR #42 merge ac07247da7f4dce973b471e5334e2b86bb4e97e4 ancestral; `docs/fiscal/h2-05/{VALIDATION,R1,R2}.md`; `DocumentoComercialCriacaoService`, `DocumentoComercialService.emitir`, `FiscalQrService:62–74`, PDFs e classes de testes | Geometria/ATCUD e fornecedor demo entregues; não há fonte produtiva. Rascunhos/linhas e emissão são componentes distintos. 194 casos H2-07-R1 não são prova integral actual de todas as jornadas |
| E08 | PR #23/#24/#27/#28/#30 ancestrais; `DocumentoFinanceiroService.java:95,109,339–376`, `FinancialReceiptIssuer`, `FinancialReceiptReversal`, `JdbcFinancialQueries.java:147–178`, `FinancialReconciliation`; `FINANCIAL_HISTORY.md` e validações op22-b2-2 | Legado escreve Pendente; novo RC/reversor internos, diagnóstico e consultas limitadas já existem. Sem cutover/consumidores/endpoint público entregues por esses contratos. Temporalidade FT não suportada não é resolvida só por base vazia |
| E09 | `docs/fiscal/catalog/D-CAT-2_MATRIX.md`, ADR-003, `H1_1_FT_SPEC.md`; relatório H2-04-R1 e parecer `2026-10-28_TUULI_H2-04-R1_verificacao-incremental_Claude.md` | Semântica não equivale a combinação/projecção homologada. Parecer H2-04-R1 mantém FAQ 4443 pendente de fonte, não inventa exemplar integral. `demo-base.sql:169–170` tem FT/RC sem identidade fiscal; seed não é configuração comercial autorizada |
| E10 | Metadata Docker lida em 02-11: `tuuli-clean38-834881`, ID 5dd74c5ae85b5e20ea459d76766b73e8cb74e6e26fb86bf9ceaf683847158a01, image postgres:16.3, running, 5432/tcp sem porta publicada | Nenhum SQL/credencial consultado. Cluster/versão/digests actuais não verificados. Não se infere V27, V28 ou integridade actual só do nome/container |
| E11 | `.github/workflows/pilot-validation.yml`; testes `EmpresaControllerTests`, `UtilizadorControllerTests`, `AuditoriaControllerTests`, `PermissionMatrixIntegrationTests`, `FiscalConfigurationReadinessTests`, `GovernedFtPersistenceTests` | Código/testes existem. Workflow executa safety tests e package, não suite PostgreSQL. Presença/contagem histórica não comprova execução no HEAD/instalação de hoje; não se executaram testes nesta missão |
| E12 | `controller/IvaSaftController.java`, `DadosMestresExportacaoController.java`; exportadores em reporting; pesquisa delimitada SourceDocuments/SalesInvoices/HashControl/PaymentType/SAF-T em src/main/java e recursos fiscais | /iva-saft é CRUD de catálogo, /exportacoes clientes/artigos é CSV/XLSX. Não identificado exportador fiscal completo; não se afirma inexistência de módulo/sistema externo. Contrato de futura exportação requer perímetro/oráculos antes de implementação |
| E13 | `docs/demo/golden-demo.md` início e `h2-05-new-demo.md`; `DemoSignatureSource` guards | Nova instalação/GOLDEN pós-H2-05 exigem ensaio e aprovação; não demonstrados como feitos. Ubuntu/Partner Demo excluídos desta missão. Não usar comandos históricos como autorização |
| E14 | Busca de issues com topn=100, seis resultados, seguida de consulta individual de #38/#39/#41/#43/#44/#46; Git log merges #32–#47 | #38/#39/#41 closed; #43/#44/#46 open. Históricos de integração prevalecem sobre aparência do estado da issue. Não foram alterados esses estados |
| E15 | Parecer original `2026-11-02_TUULI_H2-08-R1_confirmacao-incremental_Claude.md`, SHA-256 518ef2c1c58a04a223ef2042b84ef3e6f42bc33885e8babcd6315a8226b237e7, 7028 bytes, sidecar conferido; integração #47/051b1fc | APROVADO 0/0/0, achados encerrados, sem reabrir H2-08. Não se presume publicação do original no arquivo privado nem decisão produtiva |

## Registos técnicos preservados e lacunas de actualidade

- Arquivo #38 de restauro: `tuuli-backups/issue38/2026-10-27-restore-readiness/`,
  MANIFEST_RESTORE_SHA256.json com SHA-256
  `b0fee41611cc8321562a8ab4b1d424329f4c8da7eea38ff0308b9f982c581545`,
  conferido. Não se abriram snapshots com mestres, ficheiros secretos ou dados
  pessoais. A validação de todas as tabelas/FKs é resultado anterior documentado,
  não SQL executado nesta missão.
- Snapshot ensaiado no relatório E03: SHA-256
  `d26dc761a64fdf4886b531acb0f5b5e03a11b6bde45e86af556858b71e538851`.
  Não se publicou dump ou conteúdo de mestres. A cópia foi demonstrada a V27;
  H2-03 possui teste genérico V27→V28, não prova automática desse mesmo snapshot.
- Relatório OP-FC-01 de 26-10 examinado como **análise anterior**, não aprovação
  independente presumida: distingue riscos históricos dispensados de requisitos
  permanentes. Não se revogam os seis gates da Fatia 1d nem se activa a 1e.
- Segurança: não executado scanner/CVE ou teste ofensivo; resíduos aceites e
  deployment mantêm as respectivas fronteiras. Não se escolheu novo controlo ou
  arquitectura de segurança nesta análise.

## Discrepâncias documentais a não propagar

1. Estado institucional ainda não regista #38 e fecho H2-08 pós-merge de forma
   corrente; este inventário acrescenta síntese sem alterar os relatórios antigos.
2. «H2-03 por implementar» no relatório de 27-10 descreve essa data. O código e
   PR #40 posteriores resolvem a implementação, não a homologação fiscal.
3. «restore_not_tested» de metadata inicial não é impedimento actual: E04 contém
   a errata e R2 confirma restauro.
4. A linha antiga «verificar/migrar demo Ubuntu antes do corte» não autoriza tocar
   no servidor nem exige transportar histórico. A alternativa B e a instalação
   futura têm perímetro próprio; a origem e demo mantêm-se conservadas.
5. O comentário de fecho #38/6446559236 chama ao R2 documental «GPT-6»; o
   original local E03 identifica revisor «Claude Code», e o comentário posterior
   6446567501 não nomeia modelo. Há divergência de atribuição nos registos:
   ligar original/hashes à proveniência institucional, sem inferir autoria só
   do nome/sidecar ou repetir restauro para corrigir metadados. Isto não altera
   as provas técnicas examinadas nem autoriza declarar arquivo público/privado.
6. Uma issue aberta não reabre entrega integrada; uma issue fechada não valida
   emitente/séries/utilizadores. Estado técnico e estado administrativo separados.

## Limites do levantamento

Não é auditoria geral de qualidade, certificação fiscal, validação jurídica ou
aprovação de activação. Não se verificaram plataformas externas nem SQL actual.
Áreas sem prova suficiente estão marcadas como não demonstradas, não como falhas
confirmadas. As missões propostas exigem ordem própria; [pedido Claude](MISSIONS.md).
