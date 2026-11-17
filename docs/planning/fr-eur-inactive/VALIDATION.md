# FR/EUR — validação efectiva

Ensaios de 16/11/2026; conferência e arquivo em 17/11/2026. Não é a suite integral do produto.

## Código final e proveniência

- Base integrada: `f15388d0ac926cc39082869e898d5ef9998059f7` (PR #57), confirmada pela API GitHub no início da missão. Tracking local antigo não usado.
- Código/testes finais: `678e3f3bf4fb85835ec2c17437375f56d7bdef3c`; tree `8047edf7d9cde975d2e741920655a4d9604caa30`.
- Em cada execução: HEAD/tree/branch/status e hash da classe FR antes/depois, worktree limpa e identidade igual. XML, log integral, comando, versões, fonte do runner, identificação PostgreSQL e remoção preservados.
- Java `21.0.13.0.3`, Maven `3.9.16`, PostgreSQL `16.3`; Docker cliente/servidor `29.8.0`, contexto `default`, após restabelecimento da integração WSL pelo titular. Não foram alteradas configurações Docker pelo executor.

## Execuções separadas

| Execução | HEAD | Testes | Falhas / erros / ignorados | SHA-256 manifesto original |
| --- | --- | ---: | --- | --- |
| 20261116-174350-10ce35 | `abaa9b6b271ff32067fd707f77140df1b0c48419` | 10 | 5 / 3 / 0 | `b2f196cb49185fc5b9cf7e91b0957c576553e09f90856424dc6b4e188afe55a3` |
| 20261116-174552-75c800 | `0fd9aeda4a859903cf433ef604a024a00ceec369` | 12 | 0 / 0 / 0 | `cf4abac42b163063fff618b7eb25e7802f2727d96b34d786805347618e21f374` |
| 20261116-174808-a51f6e | `504fa345c2da69a45e3c49dea25559ed03ba78f3` | 107 | 0 / 0 / 0 | `19f75296fa1b5c9fcfb4352b36bda927f3e78016cb10ebf01fb53b7fadaa2e80` |
| 20261116-175250-a79ac5 | `d3d0ddab382029267834f5c2d111e929269815c5` | 85 | 0 / 26 / 0 | `a4d53d3816c4c6c36e982bf3d1331b106bb0e23fe8ef9dbe6deb5caede753c44` |
| 20261116-175705-2b9892 | `678e3f3bf4fb85835ec2c17437375f56d7bdef3c` | 61 | 0 / 0 / 0 | `b81484696cbdb850b85b6f82e8afd313c8a6e4ea066720b50a459a60e63d8364` |

Não somar estas fases como testes únicos: repetem classes e têm deltas diferentes. A primeira tentativa revelou a exigência V22 de identidade Pendente e uma fixture de autenticação que o MockMvc limpava. Ambas foram corrigidas. A execução `175250` parou nos 26 setups da classe de integridade: o reset não incluía a nova FK V30. Nenhum desses 26 erros foi apresentado como falha funcional de emissão. A adaptação é exclusivamente da fixture descartável, com validação de segurança e recusa se houver actos FR inesperados; o guard produtivo permanece activo. A execução final voltou a testar essa classe com sucesso.

## Resultado sobre o código final

| Classe | Executados | Aprovados | Falhados / erros / ignorados |
| --- | ---: | ---: | --- |
| FinancialFrIntegrationTests | 12 | 12 | 0 / 0 / 0 |
| FinancialFrIsolationTests | 2 | 2 | 0 / 0 / 0 |
| DocumentoComercialEmissaoIntegridadeTests | 26 | 26 | 0 / 0 / 0 |
| PermissionMatrixIntegrationTests | 6 | 6 | 0 / 0 / 0 |
| FinancialCoreIntegrationTests | 15 | 15 | 0 / 0 / 0 |
| **Total desta execução** | **61** | **61** | **0 / 0 / 0** |

Prova: um documento EMITIDO/liquidado, dívida original e CASH_IN de EUR 12,30, uma afectação, residual zero, nenhum RC/Pendente; datas de efeito e instante técnico distintos; replay e duas concorrências; recusa de assinatura/código AT/permissão/valor/tipo; rollback do numerador, documento, auditoria e ledger; SQL imutável/completude diferida; ausência do bean por omissão. A fonte sintética de assinatura vê já o meio de pagamento e a quitação finais. A declaração de recebimento é sintética, não uma prova de dinheiro bancário real.

## Evidência anterior reutilizada

| Classe | HEAD ensaiado | Aprovados | Fundamento da reutilização |
| --- | --- | ---: | --- |
| FinancialFrMigrationTests | `504fa345c2da69a45e3c49dea25559ed03ba78f3` | 1 | V30 e classe inalteradas: V29→V30, validação, segundo arranque, mestre preservado, catálogo/ledger sem backfill |
| GovernedFtPositiveTests | `504fa345c2da69a45e3c49dea25559ed03ba78f3` | 1 | Emissor FT, fixture e SQL respectivo inalterados |
| FinancialQueriesIntegrationTests | `504fa345c2da69a45e3c49dea25559ed03ba78f3` | 8 | Consulta e V30 inalteradas; guardas históricas preservadas |
| FinancialReceiptIntegrationTests | `504fa345c2da69a45e3c49dea25559ed03ba78f3` | 80 | RC, FinancialCore e guardas V30/V22 inalterados |
| ReceiptPayloadCompatibilityTests | `504fa345c2da69a45e3c49dea25559ed03ba78f3` | 4 | Contrato RC inalterado |
| DocumentoComercialControllerTests | `d3d0ddab382029267834f5c2d111e929269815c5` | 46 | Controller/emissor comercial e classe inalterados; delta funcional posterior só na autenticação do adaptador FR |

Todas estas classes terminaram com 0 falhas, 0 erros e 0 ignorados. Os deltas posteriores de FR não alteraram os componentes reutilizados; a interacção final e as permissões foram novamente ensaiadas nas 61 provas acima. Não se afirma execução integral de todas as classes sobre um único HEAD.

## Package e preservação

`mvn -o -B -DskipTests package`: exit 0, mesmo HEAD/tree finais e worktree limpa antes/depois. O comando **não executa testes**. JAR `fac-0.0.1-SNAPSHOT.jar`: 82 541 863 bytes, SHA-256 `5bf7d926f02882c3dc5a59f6164fc1efc4ca585049ab825fae2be2296651e6ed`. Não houve emissão positiva pelo JAR.

V1–V29 conferidas byte a byte contra a base integrada: 29 ficheiros inalterados (`provenance/previous-migrations.json`). Apenas V30 é nova. Não se executou migration em bases conservadas.

## Isolamento e arquivo

O runner usa recursos próprios `ar2.mission=FR-EUR-inactive`, PostgreSQL tmpfs sem volumes conservados, rede interna e proxy exclusivamente loopback, passwords efémeras não arquivadas, identidades sintéticas e assinatura de teste. Confere o cluster contra identificadores proibidos; não monta nem consulta bases conservadas. Em todas as cinco execuções, `disposal.json` confirma zero contentores/redes da missão e segredo temporário removido. Isto demonstra os recursos desta missão, não actividade de terceiros.

Imagens fixadas por digest e comandos completos em `run_validation.py`. Requer imagens locais e Docker acessível; o runner não descarrega dependências. Seleccionar classes por argumentos; por omissão executa o conjunto FR/migration/FT/consultas/RC. Para uma classe: `python3 docs/planning/fr-eur-inactive/run_validation.py FinancialFrIntegrationTests`. Não repetir provas válidas apenas para mudar uma data documental.

Arquivo: `Relatorios-AR2/TUULI/Arquivo-Reservado/FR-EUR-Inactivo/2026-11-17`. Foram copiados e comparados 185 ficheiros; originais, incluindo falhas, intactos. Manifesto global SHA-256 `7bb09a00ae72457b889c213e6692a68bd17a91a9a0302f8b985c94bb2d0447a8`; sidecar conferido. Cada fase mantém o seu manifesto original. O archive contém patch de implementação, identidades Git e as expectativas anteriores aos ensaios. O binário JAR permanece em target; arquivaram-se o log, a proveniência e o hash do package, não o binário de 82 MB.

## Revisão e limites

À data dos ensaios, a revisão estava pendente. Posteriormente, o PO transmitiu o pedido e Claude emitiu **APPROVE, 0/0/0**, em 17/11/2026, sobre `68c85f0`, conferindo o posterior documental `d1401b7`. O revisor não executou testes; leu código, XML, hashes e proveniência. Original/sidecar conferidos: SHA-256 `46c8904275d9bb17b908a1ee3dfd80f3f1ad4054a8c209a031910a59eff8a9a1`. Ver REVIEW_CLOSEOUT.md. Este fecho documental não altera as provas nem as transforma em homologação fiscal ou autorização de integração.

Sem endpoint/consumidor operacional, publicação fiscal, integração, push, PR, activação ou levantamento de gates. A projecção externa FR, QR/PDF/SAF-T, assinatura produtiva, certificação, anulação FR e activação financeira permanecem fora desta entrega. Mantêm-se a questão do espaço após `ATCUD:` e a melhoria pré-produtiva de bruto/IVA/total no diálogo final. Não generalizar para não-EUR, isenções, não sujeição, retenção, outros recebimentos ou liquidação parcial.
