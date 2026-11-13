# M4-04 — Validação e relatório técnico

> **Remissão de continuidade — 13-11-2026:** as referências abaixo à falta do acto inicial descrevem o estado documental da preparação/revisão anterior. O [acto inicial integral](../../governance/sources/2026-11-06_M4-04_autorizacao-administrativa-inicial.md) foi entretanto arquivado, com [proveniência separada](../../governance/sources/2026-11-06_M4-04_autorizacao-administrativa-inicial.provenance.json), e o MINOR-001 foi declarado RESOLVIDO pelo Claude R2 sobre d80ad6a, APPROVE 0/0/0 nesse âmbito. [Fecho e dossier corrente](DELIVERY.md). A análise documental foi aprovada; a questão fiscal do espaço após ATCUD: continua aberta, sem autorização de alterar a aplicação.


06-11-2026. **Prova técnica parcial executada; parecer inicial REQUEST CHANGES 0/1/2 recebido; correcção documental R1 em revisão pendente.** Sem homologação ou autorização operacional.

## Mandato, base e preparação

[Ordem integral recebida](../../governance/sources/2026-11-06_M4-04_ordem-execucao.md), SHA-256 `f28ef9e133fcc89539359b4fcca471b3385a08a4a690122d1cba11e5a70ba5cb`; [proveniência separada](../../governance/sources/2026-11-06_M4-04_ordem-execucao.provenance.json). O utilizador autorizou a proposta `0fcb2d6d4c185eaee9c04a990d9a42f285869669`. Não foi disponibilizado exemplar separado da deliberação administrativa original: preservou-se a ordem efectivamente recebida, sem inventar identificador ou autenticação externa.

Remoto HTTPS confirmado em `f2fa46765fbd1a2c0ab65d3978f321ef050c3424`/PR #56. Objecto ainda ausente localmente obtido por fetch explícito do ramo, sem mudar o checkout principal. Criada branch `codex/m4-04-ft-identity`, worktree `.worktrees/m4-04-ft-identity`, directamente nesse merge. Cherry-picks documentais de bb5ef0e e 0fcb2d6 produziram 3ed0f19 e d051281, preservando o fecho integrado e a proposta aprovada; não se copiava implementação desconhecida. O estado preexistente do checkout principal e as outras worktrees não foram alterados. O registo df71e68 continua preservado.

Antes de executar, [matriz de oráculos](MATRIX.md), `src/test/resources/m404/expected.json`, teste e runner foram fixados no commit ensaiado:

- **HEAD b9ce8ea2b4d3abf91595dd679ac2a53286e1942e**.
- **Tree 32eed3ca509fded788c024bc64ced1bd3a25ce9e**.
- Git limpo antes e depois; evidência git-before.json/git-after.json coincidente.
- Teste SHA-256 `e37be23f7c09471b745e94261775957bae28fc1bc293897f3b15a9d0444a34a2`.
- Oráculo SHA-256 `5bddfa9338fc4043e41a60ee5556b863dba1c1dd92fffc0d0e1a701310500b70`.

As expectativas de identidade e valor resultam dos fundamentos N03/N04/N05 e da fixture explícita, não dos formatadores Java. A forma gráfica com espaço após ATCUD: foi verificada como comportamento existente; não se apresenta agora como formato fiscal aprovado. O padrão literal de N03 e a admissibilidade do espaço são tratados separadamente na R1. Cópia expected-before-execution.json anterior aos pedidos, igual ao recurso versionado. Não se reabriram semântica FT/FR, M4-02, M4-09 ou M4-10.

## Execução exacta e resultado

Run **20261106-182822-392888**. Uma única execução de `python3 docs/planning/m4-04-ft-identity/run_validation.py`, seguida do Maven:

```sh
/mnt/c/Tools/apache-maven-3.9.16/bin/mvn -o -B -Dspring.profiles.active=test -Dtest=FtIdentityJourneyTests test
```

**1 teste aprovado; 0 falhas, 0 erros, 0 ignorados; Maven e runner com código de saída 0.** Classe única `FtIdentityJourneyTests`, método `governedFtIdentityMatchesPersistedSnapshotAndActualPdfQr`. Logs e XML originais em `0-FtIdentityJourneyTests/`; não é suite integral, package ou teste de JAR. A compilação Maven inclui a aplicação/testes; a selecção de execução contém só esta classe. Nenhuma tentativa de ensaio falhada. A recusa inicial do worktree add por objecto Git ausente e a necessidade de permissão Docker foram resolvidas antes do ensaio, não são tentativas JUnit omitidas.

Versões: Maven 3.9.16, Java 21.0.12.1, PostgreSQL 16.3, Docker cliente/servidor 29.8.0, Python 3.14.4; WSL2/Linux, UTF-8, perfil test. Flyway aplicou **29 migrations V1–V29, todas com sucesso**, numa base vazia; última V29 «local first administrator», conforme flyway.json. Não é upgrade de uma base conservada nem nova migration nesta missão. Imagens locais fixadas:

- PostgreSQL `sha256:d0f363f8366fbc3f52d172c6e76bc27151c3d643b870e1062b4e8bfe65baf609`.
- Proxy Node `sha256:fb4cd12c85ee03686f6af5362a0b0d56d50c58a04632e6c0fb8363f609372293`.

Sem instalação/download de dependências. Nome/cluster guardados na fixture e no runner. A classe confirma nome `tuuli_m404_*_test` e cluster esperado antes de criar dados. `FiscalSignatureTestConfiguration` existente fornece chave/assinatura e certificado **sintéticos**, não uma credencial fiscal. O manifesto produtivo permanece vazio e inalterado. FT/1 é materializada só na base descartável.

## Correspondências demonstradas no mesmo percurso

Criação de empresa/cliente/artigo sintéticos e configuração por endpoints com login/segurança activos; revisão e série exclusivamente como fixture SQL. Emissão e leitura do documento por MockMvc, fora de transacção exterior de teste. Não se invocou directamente serviço de emissão nem se alteraram gates.

| Elemento | Esperado fixado antes | Observado / asserção |
| --- | --- | --- |
| Configuração governada | GFT, fiscal FT, FT/1, 2/1/1,false, EUR | Configuração SQL e catálogo capturados; criação normal aceite |
| Série/sequência | M404A, numerador 0→1 | Leituras SQL antes/depois; apenas um documento no cenário |
| Classificação consolidada | Interna GFT; fiscal FT | Nova leitura HTTP e snapshots SQL `tipo_documento_codigo`/`tipo_documento_codigo_fiscal` coincidem; fiscalSnapshotVersion >0 |
| Número completo | GFT M404A/1 | Resposta de nova leitura e persistência coincidem; prefixo interno distinto de FT |
| ATCUD | SYNM404A-1 | Código de validação e ATCUD persistidos; nova leitura coincidente. Código exclusivamente sintético de oito caracteres |
| QR D/G/H | FT / GFT M404A/1 / SYNM404A-1 | Payload persistido=HTTP=QR da imagem embebida no PDF=QR descodificado da página renderizada a 300 dpi; campos comparados ao oráculo literal |
| PDF | Mesmo número; ATCUD: SYNM404A-1; descrição configurada preservada | PDF de uma página, texto extraído e render arquivados; uma imagem QR. Valor ATCUD/H confirmado; rótulo com espaço observado, com validação fiscal pendente. Identidade do documento persistido inalterada após GET PDF |

PDFBox/ZXing reutilizados da M4-10, sem alterar helpers. Inspecção visual do render confirmou os identificadores legíveis; não é impressão física, medição de equipamento ou homologação. Métricas geométricas gravadas incidentalmente pelo helper não são novos critérios certificados. Os montantes simples presentes não constituem repetição da prova monetária M4-09.

## Isolamento, remoção e integridade

Novo cluster **7705127104236970029**, controlo `tuuli_m404_control_test`, base do ensaio `tuuli_m404_0_test`; PostgreSQL tmpfs, sem volumes/mounts conservados e sem porta PostgreSQL publicada. Rede interna e proxy próprio publicado apenas em loopback. isolation.json contém IDs exactos; commands.jsonl contém comandos do runner sem segredos. Não se exportou ambiente, token ou chave privada. A senha efémera esteve fora da pasta de evidências e foi removida.

Runner removeu apenas os seus IDs/labels `ar2.mission=M404-ft-identity`, com verificação prévia. disposal.json e consulta posterior independente do runner em supplement/resource-recheck.json: **zero contentores e zero redes** dessa label, códigos0. Não se removeu recurso desconhecido. A origem histórica, base preparada, demo Ubuntu e GOLDEN não foram acedidos. A prova de preservação aqui consiste no isolamento dos recursos/comandos; não se executaram consultas ou comparações de conteúdo nessas bases.

Originais em `target/m404-qr-evidence/20261106-182822-392888/`, com cópia byte a byte fora de target em:

`backups/test/m4-04/20261106-182822-392888/`

38 ficheiros do manifesto original conferidos. Com manifesto original e suplemento: **45 ficheiros copiados/conferidos**, mais ARCHIVE_MANIFEST.json. Não alterar os originais para acrescentar conclusões.

- MANIFEST_SHA256.json original: **3d05cac662416fb48119aa046dc2bb609731c31fe3b7996cbbc9ce0fa96821f8**.
- ARCHIVE_MANIFEST.json: **12dd223ed5772556ece09f39946acdfcef46dbf0d22f052c0293e733a3a4be10**.

O suplemento distingue a transcrição posterior dos comandos de criação da worktree/invocação dos logs automáticos originais. Não é reconstrução apresentada como gravação contemporânea. Uma verificação documental adicional confirmou delta vazio para src/main, frontend e pom.xml contra a base integrada.

## Alcance, pendências e revisão

Resultado positivo limitado à **identidade da FT diferida/EUR sintética por MockMvc, com fonte sintética**, num documento/página. FR/FT imediata, SAF-T e outras projecções não demonstradas; M4-04 completo continua parcial. Reservados não-EUR, isenções, não sujeição, retenção, liquidação imediata e recibos. Sem assinatura real, emissão positiva pelo JAR, homologação QR/PDF, comunicação AT, impressão física, activação, backfill ou alteração funcional/fiscal. Bruto/IVA/total no diálogo continua melhoria pré-produtiva pendente.

Não surgiu divergência de identidade ou de valores. A menção gráfica permanece em validação fiscal, sem concluir não conformidade. Só se executou a classe dirigida; suites anteriores reutilizadas, sem lhes atribuir um novo HEAD de execução. O commit final posterior acrescenta apenas documentação: verificar igualdade dos blobs do teste, runner e expected.json com b9ce8ea. HEAD final na entrega e no pedido externo, sem auto-referência no commit.

O pedido inicial foi encaminhado pelo PO e o parecer original Claude foi recebido e conferido. A revisão R1 continua pendente; [pedido incremental](R1_REVIEW_HANDOFF.md) preparado para transmissão pela Coordenação, sem presumir activação automática do revisor.

## R1 — Correcção documental, sem novo ensaio

Deliberação R1 recebida e preservada integralmente, SHA-256 e24ed036b9a37c28dd1a8f4e56060811ccd247ad8fae6560a157e06975e50d93, com proveniência separada. Parecer inicial original/sidecar iguais: **55e89d39dedc2deedb001c9054321f552411c0f35d811cff7f899645b8725d87**. Mantidos sem alteração; ver [análise e tabela de tratamento](R1_ATCUD_ANALYSIS.md).

Arquivo externo concluído em `/home/arosario/tuuli-backups/m4-04/2026-11-06/20261106-182822-392888/`, fora da worktree: 52 ficheiros originais/entrega/revisão conferidos, mais manifesto próprio. [Inventário e digests](R1_ARCHIVE.md). Não elimina nem altera os arquivos iniciais. Exemplar separado do acto administrativo inicial continua não recuperado; a deliberação R1 efectivamente recebida não é apresentada como reconstrução desse acto.

Observações preservadas: id e sequência ambos 1 não são discriminados; recalculoConfirmado=true foi enviado com indicador false; contains não exclui outro número adicional no PDF. O render foi inspeccionado, não uma asserção de unicidade textual. Estas limitações não foram corrigidas artificialmente mediante novo ensaio. Patch/hash do delta inicial e R1 preparados; espaços editoriais corrigidos apenas nos documentos de trabalho.

Resultado técnico positivo do valor ATCUD/H não alterado. A menção `ATCUD: SYNM404A-1` não é agora declarada fiscalmente homologada. Fontes oficiais apresentam padrão/exemplo sem espaço, sem esclarecimento expresso localizado sobre tolerância: **validação fiscal pendente**. Não se alteraram aplicação, expectativas executáveis, testes, runner, PDF ou evidências originais; zero testes/SQL/Docker na R1. Sem push, PR, merge ou activação. Próximo passo: confirmação independente limitada a esta correcção e análise, não homologação fiscal.
