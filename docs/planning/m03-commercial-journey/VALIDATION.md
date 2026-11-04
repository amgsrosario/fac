# M03 — Validação dirigida da jornada comercial não fiscal

Data: 04-11-2026, Europe/Lisbon. Mandato [#46/6613324580](https://github.com/amgsrosario/fac/issues/46#issuecomment-6613324580). [Inventário anterior aos ensaios](COVERAGE.md). **Resultado do executor: PASS no perímetro abaixo; revisão Claude pendente.** Não é suite integral, CI, deploy ou prontidão fiscal/comercial.

## Proveniência e ambiente

Base integrada `e6bb33f76523a268b39978733e099c96e98d7999`, PR #50/M02. HEAD final ensaiado `7acfc9ec71d9f5c46b61b162ec19a48c3e76c843`, tree `dfc0a646f7d348e7d687c137640669f0d9fc0746`. Branch `codex/m03-commercial-journey`, limpa antes/depois de cada classe e package; identidades iguais. SHA-256 do teste novo `63425242b3b139b51f8e789b09bf605b65cda4c7befd2a8b419372cf0db760a2`. Fecho posterior exclusivamente documental, sem alteração deste código ensaiado ou de src/main.

Executor versionado [run_validation.py](run_validation.py): `python3 docs/planning/m03-commercial-journey/run_validation.py`. Por selecção: `/mnt/c/Tools/apache-maven-3.9.16/bin/mvn -o -B -Dspring.profiles.active=test -Dtest=<selecção exacta em command.json> test`; depois `mvn -o -B -DskipTests package` com o mesmo executável. Java 21.0.12.1, PostgreSQL 16.3 local já disponível; nenhuma dependência instalada. As oito bases são novas, nomes `tuuli_m03_0_test` a `tuuli_m03_7_test`; migrations V1–V29 aplicadas de raiz. Não é repetição do upgrade M01 V27→V28 ou do restauro #38.

Cluster novo tmpfs, sem mounts persistentes, rede própria interna e proxy apenas loopback; guards recusam IDs dos clusters conservados. Registo `isolation.json` guarda os IDs, sem credenciais. Apenas IDs/labels M03 criados pelo executor são removidos; `disposal.json` e consulta Docker final sem contentores M03 confirmam limpeza. Password de PostgreSQL e segredo JWT aleatórios por execução; ficheiro efémero eliminado. Os dados/contas das fixtures são inteiramente sintéticos e não constituem bootstrap comercial, conta real ou configuração homologada. Origem, preparação conservada, Ubuntu/GOLDEN e worktree `7ce573f` não utilizados.

## Execução final — oito selecções, 27 casos distintos

| Classe/selecção dirigida | Aprovados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: |
| CommercialJourneyTests | 9 | 0 | 0 | 0 |
| EmpresaControllerTests | 2 | 0 | 0 | 0 |
| ClienteControllerTests | 3 | 0 | 0 | 0 |
| ArtigoControllerTests | 4 | 0 | 0 | 0 |
| ParametrosClienteControllerTests | 3 | 0 | 0 | 0 |
| ParametrosDocumentoComercialControllerTests | 2 | 0 | 0 | 0 |
| DadosMestresImportExportTests | 2 | 0 | 0 | 0 |
| AuditoriaControllerTests | 2 | 0 | 0 | 0 |
| Total | 27 | 0 | 0 | 0 |

Package: BUILD SUCCESS. Logs completos, XML Surefire, comandos, HEAD/tree/status antes/depois, fonte/runner exactos, hashes e prova de descarte arquivados. Não se repetiram as suites gerais M01/M02/#38 nem todos os testes comerciais. Dos 27, nove são novos casos de jornada e 18 são regressões seleccionadas; repetir casos em tentativas anteriores não aumenta o número de casos distintos.

## Tentativas anteriores preservadas

1. `20261104-090957-001e99`, HEAD `87052cc`: falha de inicialização da fixture, uma ocorrência de erro no XML. Coluna errada `data_comunicacao_at`; schema efectivo usa `data_codigo_at`. Não executou os nove percursos. Correcção exclusiva do teste no commit `3dab4ce`.
2. `20261104-091132-b28aba`, HEAD `3dab4ce`: nove casos, oito aprovados/uma falha/zero erros/zero ignorados. PUT novo de cabeçalho omitira prazo de pagamento; API existente recusa 400. Corrigido payload do teste no commit `7acfc9e`, mantendo contrato integral de actualização. Não se alterou a aplicação para fazer passar o teste.
3. `20261104-091300-309346`, HEAD `7acfc9e`: execução final acima, sem falhas. Não ocultar nem somar as tentativas como testes únicos. Primeira chamada de preparação anterior aos ensaios não executou por timeout da revisão automática de permissões; nenhuma prova ou commit atribuído a essa chamada.

## Evidência preservada

Arquivo restrito: `tuuli-backups/m03/2026-11-04/` no host THINKPAD; caminho local `/home/arosario/tuuli-backups/m03/2026-11-04/`. **87 ficheiros copiados**, identidade byte a byte e manifestos individuais conferidos; originais mantidos em `target/m03-evidence/`. Manifesto global `MANIFEST_ALL_SHA256.json`, 17524 bytes, SHA-256 `1af9232f2bc54289fcf490806dc995f7017930520f25c0cd1e3a68cce2791cd3`.

Manifesto da execução final: `20261104-091300-309346/MANIFEST_SHA256.json`, SHA-256 `670460da25429a6a41bb3f2e8ab4ff2d97ca59e51af2de7f5577b8ec5141b4a9`. Logs de tentativas falhadas podem conter payloads/JWT de identidades exclusivamente sintéticas; conservar no arquivo restrito, sem publicar logs brutos. Banco/segredos aleatórios descartados, nenhuma credencial comercial recolhida. O manifesto permite conferir todos os ficheiros, não representa assinatura independente.

## Matriz de resultados e bloqueios

| Percurso | Facto demonstrado | Limite / impedimento operacional |
| --- | --- | --- |
| Empresa, clientes e artigos | Inicial: empresa/clientes editados; artigos criados/consultados e negativos de identificação/dependências/tipo; cliente estrangeiro. Edição efectiva do artigo demonstrada apenas na R1 abaixo | Emitente/dados comerciais reais não validados por fixtures |
| Parâmetros | Leitura/criação/alteração e rejeição de duplicados/referências inexistentes | Configurações sintéticas não homologam tipos/séries |
| Rascunho/linhas | Criação atómica com primeira linha; acrescentar/reordenar, total 24,60 da fixture, cabeçalho/lista/consulta/exclusão | Tipo não classificado legado de teste; não é percurso FT governado produtivo |
| Rollback | Artigo inexistente/quantidade inválida não deixam cabeçalho nem evento DOCUMENTO_CRIADO órfão | HTTP sem transacção exterior no novo teste; não prova toda a concorrência da aplicação |
| Perfis e auditoria | JWT/login existentes, ADMINISTRADOR/OPERADOR/CONSULTA; 401 anónimo, 403 proibidos; evento com actor e filtros/paginação | Prova dos percursos seleccionados, não auditoria global de segurança/HBA/TLS |
| Relação pai/filho e erros | Linha de outro documento recusada 404, sem eliminação; sort inválido 400 | Não equivale a isolamento multiempresa global |
| Exportação não fiscal | CSV/XLSX, permissões, múltiplas páginas e protecção CSV existentes | Não é SAF-T/Payments nem certificação AT |
| Assinatura ausente | 409, documento continua RASCUNHO, sem número/ATCUD/QR/auditoria de emissão nem incremento da série | Bloqueio esperado; assinatura reservada à fase final |
| Série sem código | 400 antes da numeração | Não criar nem reutilizar série fiscal legítima por inferência |
| FT governada | Catálogo vazio recusa criação 409, sem persistir tipo; zero revisões | Homologações/manifesto continuam gates, não defeito a contornar |
| FinancialCore | Novo teste mantém zero posições/operações/origens/RC/pendentes e nenhum fornecedor fiscal | Não ensaia activação, RC, anulação ou consultas financeiras operacionais |

**Defeitos funcionais demonstrados dentro do perímetro: nenhum.** Corrigidos apenas dois erros introduzidos na preparação do novo teste. Nenhuma mudança de código funcional, DTO, migration, contrato público ou regra fiscal necessária. Não inferir ausência de defeitos fora destes percursos.

Observações M02 O2–O9 continuam nos relatórios respectivos; não resolvidas implicitamente pela M03. M04/M05, trabalhos fiscais adiados, auditoria funcional final e certificação AT permanecem pendentes/reservados, não cancelados. Sem FinancialCore, RC/RL, Fatia 1e, emissão produtiva, dados reais, deploy, cutover, push, PR ou merge.

## Revisão e continuidade

[Pedido de revisão](REVIEW_HANDOFF.md). Sem runtime Claude disponível, a Coordenação/PO deve transmitir a instrução; um comentário na issue não activa o revisor. Não declarar aprovação independente. Qualquer publicação/início de M04/M05 aguarda ordem própria. `git diff --check` e referências locais devem ser conferidos no fecho; não executar novamente testes só por mudanças documentais.

## M03-R1 — Resolução dirigida das reservas Claude

[Ordem #46/6627651498](https://github.com/amgsrosario/fac/issues/46#issuecomment-6627651498). Parecer Claude original sobre `97db276`: **APROVADO COM RESERVAS, 0 BLOCKER / 0 MAJOR / 2 MINOR**, SHA-256 `34d9bd8b279705b8bde4c975b5d6495718d169b58ef766bb6e048e7c739453b7`. Leitura integral e igualdade original/sidecar/cópia recebida conferidas; preservado inalterado no arquivo R1. O parecer não é a conferência técnica Codex anterior. Observações O-1 a O-5 mantêm-se não condicionantes, sem alegada resolução pela R1.

| Finding | Correcção e prova | Limite mantido |
| --- | --- | --- |
| MINOR-001 | PUT do artigo passa a alterar a descrição para `Artigo M03 editado`; GET posterior exige esse valor, pelo que ignorar a actualização faz falhar a asserção | Não se afirma propagação de preço/descrição do mestre às linhas ou totais. A matriz retirou essa alegação; preços das linhas são enviados explicitamente |
| MINOR-002 | Após duas linhas/24,60, PUT da segunda para quantidade3 × preço5; GET da linha exige quantidade3/preço5/total18,45. GET do documento exige bruto25/IVA5,75/total30,75. DELETE da segunda exige204, GET da linha404, lista apenas com o ID da primeira; GET do documento exige bruto10/IVA2,30/total12,30 | Mantidos os guards/asserções de @AfterEach, ausência de fonte/revisão e efeitos financeiros e numeração0. Não é operação fiscal |

Execução **apenas CommercialJourneyTests: 9 casos/9 aprovados/0 falhas/0 erros/0 ignorados**. Nenhuma outra classe repetida; sem package, restauro/importação ou ensaio M01/#38. Estes nove casos sobrepõem os nove anteriores, com asserções reforçadas; **não são 36 casos únicos**. Os 18 casos seleccionados da execução original são evidência anterior conservada, não reexecutada em R1.

HEAD ensaiado limpo `49b1623d34c1dae4061313a5476bab98cbd08261`, tree `9b33526066b69397db29de1b2c0a2ede19f6238b`, antes/depois iguais. Log/XML/command.json e hash da fonte em git-before.json. Comando efectivo Maven `-o -B -Dspring.profiles.active=test -Dtest=CommercialJourneyTests test`. Runner de ensaio `/tmp/m03-r1-run_validation.py`, derivado do runner versionado: ROOT explícito, selecção única, directórios/labels próprios M03-R1 e supressão do package; cópia exacta arquivada como `run_validation.py`. Sem alteração do runner versionado nem do código funcional.

PostgreSQL 16.3 novo, dados/contas sintéticos, tmpfs/rede próprios e loopback, guardas de identidade e limpeza por IDs/labels. Nenhum snapshot ou base conservada utilizado. Apenas V1–V29 na base descartável. Recursos/segredo efémero R1 removidos; consulta final sem contentores M03-R1. Original M03 e seus 87 ficheiros/manifesto mantidos.

Arquivo separado `/home/arosario/tuuli-backups/m03-r1/2026-11-04/`, **16 ficheiros + manifesto global**, incluindo parecer original/sidecar. `MANIFEST_ALL_SHA256.json`, 3062 bytes, SHA-256 `8c67239595d917a9ccd341b49b2cccb131f8341d190aeb9916b6a5b22d93a378`. Execução `20261104-094321-1692bb`, manifesto individual SHA-256 `e86ac48b9e78208e6109e368f34218678abaf3b3138c12423dc1935a9d883dda`. Cópias byte a byte/manifestos conferidos. Não publicar logs brutos.

Alterações apenas em teste/documentação. Não identificado defeito de aplicação, nenhum gate enfraquecido. Resultado executor: correcções implementadas e validadas; **confirmação incremental Claude pendente**, sem auto-aprovação nem autorização de publicação. Sem push/PR/merge, contas reais, bases conservadas/demo, activação ou M04/M05.
