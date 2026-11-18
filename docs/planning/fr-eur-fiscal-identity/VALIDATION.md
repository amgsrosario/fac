# FR/EUR — validação da identidade/projecção inactiva

18/11/2026. Base integrada PR #58 `e697391`; plano `36c6aad`. Código funcional final `1bbc5fd`; commits `84570d6` e `15fd59a` só ajustam testes. HEAD final de código/testes `15fd59aff0c093bb01c657e72bbcd9c125766bf7`, tree `46518855141fe40c60b29855318439754afc8242`. As três fases seleccionadas reutilizam fontes funcionais byte a byte iguais; alterações posteriores à primeira afectam apenas as classes de teste indicadas. A tabela escolhe uma execução válida por classe, não soma repetições nem constitui uma única suite integral.

## Resultados seleccionados

**342 casos aprovados em 23 classes; 0 falhas, 0 erros, 0 ignorados nos resultados seleccionados.** Logs/XML originais e resultados completos de cada fase preservados, incluindo tentativas falhadas.

| Classe | Casos aprovados | Execução |
| --- | ---: | --- |
| ProjectedFrIntegrationTests | 19 | 20261118-113606-8c4c4b |
| ProjectedFrMigrationTests | 1 | 20261118-112838-169578 |
| FrProjectionContractTests | 4 | 20261118-113606-8c4c4b |
| FiscalCatalogTests | 26 | 20261118-112838-169578 |
| FiscalCatalogInitializationTests | 5 | 20261118-112838-169578 |
| FinancialFrIntegrationTests | 12 | 20261118-112838-169578 |
| FinancialFrIsolationTests | 2 | 20261118-112838-169578 |
| FinancialQueriesIntegrationTests | 8 | 20261118-112838-169578 |
| FinancialReceiptIntegrationTests | 80 | 20261118-113606-8c4c4b |
| ReceiptPayloadCompatibilityTests | 4 | 20261118-113606-8c4c4b |
| GovernedFtControllerTests | 19 | 20261118-113606-8c4c4b |
| GovernedFtPersistenceTests | 34 | 20261118-114208-8f7d23 |
| TipoDocumentoControllerTests | 3 | 20261118-114208-8f7d23 |
| DocumentoComercialControllerTests | 46 | 20261118-114208-8f7d23 |
| DocumentoComercialEmissaoIntegridadeTests | 26 | 20261118-114208-8f7d23 |
| PermissionMatrixIntegrationTests | 6 | 20261118-114208-8f7d23 |
| CommercialReprintJourneyTests | 1 | 20261118-114208-8f7d23 |
| AtcudServiceTests | 5 | 20261118-114208-8f7d23 |
| DocumentoComercialAnulacaoTests | 2 | 20261118-114208-8f7d23 |
| QrFiscalPayloadBuilderTests | 4 | 20261118-114208-8f7d23 |
| FiscalQrIdentityTests | 13 | 20261118-114208-8f7d23 |
| DocumentoComercialPdfServiceTests | 17 | 20261118-114208-8f7d23 |
| DocumentoFinanceiroPdfServiceTests | 5 | 20261118-114208-8f7d23 |

Java 21.0.13.0.3, Maven 3.9.16 offline, PostgreSQL 16.3. Comandos exactos, HEAD/tree/status antes/depois, imagens por digest, versões, cluster, isolamento e eliminação em cada directório de evidência. Runner versionado em run_validation.py; nomes de classes explícitos seleccionam regressões. Não lançar duas cópias simultâneas na mesma worktree/target. `M402_EVIDENCE` e prefixo `tuuli_m03_` são nomes técnicos herdados de testes reutilizados, não acesso às bases dessas missões.

## Tentativas preservadas e causas

- `20261118-112313-0854e6`, 703327c: setup recusado 409, pois manifesto sintético tinha duas revisões e a fixture só materializava uma. Corrigida a fixture, sem relaxar catálogo.
- `20261118-112556-defff8`, 6f12f6c: 17 FR + 1 upgrade aprovados; prova anterior à extensão dos casos.
- `20261118-112838-169578`, 1bbc5fd: provas FR/catálogo/consultas aprovadas; RC 80 executados, 9 erros de setup, 0 falhas. Um caso negativo não transaccional deixava snapshot ND artificial sob código FT, agora registado pela guarda de identidade. Correcção exclusiva do teste: rollback da corrupção deliberada após verificar a recusa; guardas produtivas intactas. RC reexecutada: 80 aprovados.
- `20261118-113606-8c4c4b`, 84570d6: 19 FR, 4 contrato, 80 RC, 4 payload e 19 controller FT aprovados; interrompida por falha do oráculo de upgrade FT que assumia V28 como última versão (esperava 1 migration, foram 4). Teste passa a preservar V27→V28 e depois validar as 3 adicionais até V31. Nenhuma migration anterior alterada.
- `20261118-113912-4081cc`: duplicação instrumental iniciada porque o runner original não aparecia no namespace de processos local. Interrompido apenas o subprocesso Maven confirmado pelo PPID do duplicado. Exit 143, não falha do produto. XML pré-existente copiado pelo runner nesse ponto **não é prova válida dessa execução**; todos os resultados do duplicado excluídos da tabela. Logs/manifesto e remoção próprios preservados. Não alterar retroactivamente estes ficheiros.
- `20261118-114208-8f7d23`, 15fd59a: restantes 12 classes, todas aprovadas; recursos removidos, segredo efémero apagado.

## Package e integridade

`mvn -o -B -DskipTests package` sobre HEAD/tree limpos 15fd59a: aprovado, **sem execução de testes**. SHA-256 do JAR e comandos em DELIVERY.json e package-15fd59a/jar.json. Não houve emissão positiva pelo JAR.

`git diff --check` limpo; V1–V30 e manifesto produtivo iguais à base integrada. V31 é aditiva, sem definições publicadas/backfill; V30→V31 e segundo arranque aprovados. Isolamento: PostgreSQL tmpfs novo, redes/labels próprias, proxy loopback, sem mounts/snapshots de bases conservadas; finally valida IDs/labels antes de remover recursos. Último disposal tem contentores e redes vazios. As evidências incluem PDF real/QR descodificado, leituras financeiras, I04b-C6-OPEN.json e resposta MockMvc.

## Limites e revisão

C6 aberto; anulação FR fora de âmbito; FT/RC preservados nos cenários seleccionados, não prova exaustiva de todo o produto. Assinatura sintética e perfil experimental, EUR apenas; não generalizar para não-EUR, isenções, não sujeição, retenção, regimes especiais ou recibos. Nenhum catálogo publicado, assinatura real, comunicação AT, certificação/homologação ou activação.

A correspondência funcional previamente aprovada não é reaberta. Revisão independente desta implementação **pendente Claude Code**. Ver TEST_MATRIX.md, INDEPENDENT_FISCAL_WORK.md e REVIEW_HANDOFF.md. Pedido preparado para encaminhamento; nenhuma aprovação independente inventada.

## Preservação externa

306 ficheiros originais copiados e comparados por tamanho/SHA-256 para `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Projeccao/2026-11-18`. Manifesto global `ARCHIVE_MANIFEST_SHA256.json`, SHA-256 `b6ac845e6eee75cd4aa57b5d4126517475807530580c3d11f8f94b1cf532283b`; sidecar preservado. Inclui sete fases/package, patches cumulativo/implementação e fontes de código/testes fixadas em 15fd59a. Não inclui passwords efémeras, bases conservadas ou chaves reais. O JAR não foi copiado: hash/tamanho e origem da compilação ficam em jar.json.
