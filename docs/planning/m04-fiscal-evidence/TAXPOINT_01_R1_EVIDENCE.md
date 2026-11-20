# TAXPOINT-01 R1 — ligação entre revisão e prova

Base integrada `464e5b052997f1790b365188097d54552e00048d`. Delta revisto a partir de `c4381cb072789275d1d051db00e6fe9d6fbe4d91`. HEAD ensaiado `d5770283aa4727fc99d9ff97dad8268e016b978e`, tree `df57eb8cefd9c6b65eda78da59a408a861b4387b`. O commit posterior acrescenta este documento.

| Condição ou observação | Prova |
| --- | --- |
| MINOR-001 | `DocumentTypeImmutabilityTests` confere nove migrations no upgrade V24→V33, verifica que as cinco colunas acrescentadas ficam NULL e exige igualdade JSONB depois de as remover explicitamente. Na base `464e5b0`, o oráculo esperava cinco migrations embora V25–V32 somassem oito e a igualdade não considerasse as quatro colunas aditivas da V31. O [registo do primeiro ensaio](TAXPOINT_01_EVIDENCE.md) identifica o FAIL em `969b0e6`, que observou nove migrations com a V33. O arquivo original conserva o manifesto `fa9c13150ce2466851b340648b0a1faf6b6dd52535ca18029bab95b56d0803ba`. |
| MINOR-002 | [Mapa de campos](SAFT_FIELD_MAP.md) e [A12-07](SAFT_ACCEPTANCE.md) conservam a redacção original e incluem nota de supersessão D1 limitada a TaxPointDate da FT/EUR. As notas remetem para [§5.1](SAFT_F_FT_EUR.md) e mantêm «diagnóstico, não fallback» para os demais campos e eventos obrigatórios. |
| MINOR-003 | `pdf/base/` e `pdf/candidate/` no arquivo R1 contêm a fonte de harness, manifestos, PDF e texto extraído. A comparação substitui apenas o instante UTC imediatamente após «Emitido por M3ADMIN em » no rodapé, por ser atribuído em cada execução. Número, ATCUD, identificadores e resto do texto são comparados literalmente. |
| O-1 | Inventário dos deltas a testes existentes abaixo. |
| O-2 | `issueDatedFixture` recebe a quantidade de linhas. Os casos de cronologia pedem uma linha e a prova de datas pede duas. |
| O-3 | O teste 11 usa SoftAssertions para comparar separadamente as datas de cada documento, a contagem do ficheiro e o valor de origem do anulado. |
| O-4 | `CommercialServiceDateTests` confirma o estado ANULADO após o POST de anulação e a recusa do UPDATE de data com «Data de prestação imutável». |
| O-5 | O `PUT /documentos-comerciais/{id}` substitui os campos editáveis do rascunho. Omitir `dataPrestacao`, ou enviar `null`, apaga o valor anterior. Este registo documenta o contrato do PUT do cabeçalho, para o qual não se localizou contrato documental próprio. |
| O-6 | O novo Playwright preenche, guarda e recarrega a data num rascunho, depois limpa, verifica `null` explícito no PUT e recarrega. O editor é real e as respostas da API pertencem à fixture Playwright. `frontend/versions.log` fixa as versões e os códigos de saída. |
| O-7 | O único delta em produção está em `SaftFtExport.resolveTaxPointDate` do perfil de laboratório. A prova isolada `resolver/GuardProbe.java` verifica o tipo da recusa e o diagnóstico com a classe Java inesperada e os identificadores sintéticos. |

## Deltas a testes existentes

Consultar `git diff 464e5b0..d577028 -- src/test/java`. Os seguintes pontos identificam as secções do diff e as expectativas que mudaram.

| Secção | Delta | Expectativa |
| --- | --- | --- |
| SAF-T, teste 7, asserção anterior na linha 320/321 | Sai a recusa pela data em falta no fornecedor Evidence. Entra a exportação esperada com data de emissão de um documento identificado com data de prestação NULL. Diff textual no [registo inicial](TAXPOINT_01_EVIDENCE.md). | Única alteração de expectativa nos dez testes SAF-T anteriores, autorizada pela deliberação D1 na issue #46. |
| SAF-T, fixture `issueFt` | O payload sintético agora grava 02/11 no rascunho. | O XML continua a exigir 02/11 no caso preexistente. |
| SAF-T, `evidence(...)` e chamadores dos testes 1–10 | A fixture deixa de fornecer data em `LineFacts`. Os casos de cronologia usam o mesmo fornecedor de evidência restante, e o helper explicita uma linha. | Mantêm-se as restantes asserções, recusas e datas de fronteira. |
| Upgrade, `upgradeFromV24LocksCancelledHistoryWithoutChangingDocumentsAndRestarts` | Contagem 5→9, verificação das cinco colunas novas NULL e igualdade da projecção das colunas históricas. | Muda o oráculo do número de migrations. Preserva-se a expectativa de identidade dos dados históricos e de imutabilidade. |
| Playwright, `setup` | O mock do PUT guarda e devolve a data nas leituras seguintes para suportar o novo caso. | Asserções dos 42 casos anteriores preservadas. |

`DocumentoComercialEmissaoIntegridadeTests` e `DocumentoComercialPdfServiceTests` foram executados com o conteúdo integrado original.

## Resultados e arquivo

As duas execuções integrais sobre o HEAD ensaiado passaram em PostgreSQL descartável. Em cada modo, as cinco classes tiveram respectivamente 26, 17, 17, 2 e 11 testes, total 73, sem falhas, erros ou omissões. O runner verificou a remoção dos recursos. A origem do XSD e os pacotes fixados constam dos logs e da proveniência arquivada.

| Execução | SHA-256 do manifesto |
| --- | --- |
| opt0 | `35f833264156d247e5bc5106673a80d6bb51d4f1524601775ddb630162570805` |
| opt1 | `b25859b5b1c1a52f7d09649b05c4347103ebb47737f0e8172e4271804f0ce9ef` |
| PDF base, harness `a2c2b0786bf36bfe47998dad369f93518227e342` | `0862c4452ac3898227bf69e972a1172d7242f82537e8376ba372caef51c12482` |
| PDF R1, harness `6a18a71c3b5b45ebfc1a08a182b3a9df5b72fb59` | `dbe8cbddb8b7b846bb126d6eaf6cfe692f012b9074251eb438564bb06e26f21f` |

O harness é idêntico nos dois commits isolados, SHA-256 `f655d128ef8b126500ac7057f3b958ea7d82ca4418f086dde7e15d90c6662af8`. O texto original da base tem SHA-256 `774c3ea72afb0a57577914d350195baf46f8612071b979ec1f45c964181154f0`. O texto original R1 tem SHA-256 `1725f7aeb58289e74df60561c70f68533d9a3fa5956b9b8b2b5ee36da0f0a295`. Ambos os textos normalizados têm SHA-256 `6a2cc8a8d49da5fd61af6df17086f6a60c6eecb818f07a1f289789cfe26c8b7b`. O diff tem zero bytes, SHA-256 `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`.

O build TypeScript/Vite e toda a suite Playwright terminaram com saída 0, 43 testes aprovados. Node 24.19.0, npm 11.17.0, Playwright 1.63.0 e Edge 157.0.4341.60.

Arquivo `Arquivo-Reservado/TAXPOINT-01/2026-11-20-R1/`, 144 entradas conferidas. SHA-256 do índice `48b8279b7a45f8fe9d830b86cfff2103b0190a5cc7b1c7fc7d8a2a8d4fa8c4b5`, com sidecar. O índice inicial e as suas 226 entradas foram reconferidos.
