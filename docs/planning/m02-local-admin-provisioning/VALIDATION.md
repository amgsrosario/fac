# M02 — Plano de validação e evidência

Base ca1e802 (PR #49); nova branch isolada codex/m02-local-admin-provisioning. Sem instalação real. Resultados executados abaixo. O plano antecedeu os ensaios e não constitui prova por si.

Runner `run_validation.py`: imagens locais existentes PostgreSQL 16.3/Node, sem download; cluster tmpfs/rede interna/proxy loopback, sem volumes conservados. Role SQL sintética restrita e identidades @example.test. Cada classe usa base descartável própria; os testes de provisionamento usam bases adicionais próprias por caso, para não resetar o comprovativo artificialmente. Sem snapshot ou repetição #38/M01.

Capturar HEAD/tree/status/fonte antes/depois de cada execução, exigindo worktree limpa. Preservar logs/XML e manifesto SHA-256 sem segredos; remover somente recursos com IDs/labels M02. Executar primeiro as três classes novas, depois UtilizadorControllerTests, SecurityIntegrationTests, PermissionMatrixIntegrationTests, SecuritySettingsTests e as três classes de upgrades afectadas por V29. Ajustes destas últimas são apenas contagens/versão esperadas pela migration aditiva; não alteram V25/V26/V27. Package e smoke `--help` pelo JAR, sem Spring/DB.

Cobertura dirigida: identidade incorrecta/base/cluster/UUID/role, expiração, administrador activo/inactivo e outros utilizadores, repetição, duas chamadas simultâneas, INSERT normal concorrente, auditoria falhada com rollback e retry, ausência de permissões directas/PUBLIC, comprovativo imutável, password policy/memória, V28→V29 com utilizadores preservados/inelegibilidade persistente, validate/segundo arranque, ficheiro 0600/symlink/duplicados/URL, ausência de consola, login/JWT/permissões/auditoria após provisionamento. Não é auditoria geral nem certificação.


## Resultados efectivamente executados — 03-11-2026

| Classe | Casos aprovados finais | Commit do ensaio aprovado |
| --- | ---: | --- |
| FirstAdministratorAuthorizationTests | 6 | 3a5f49b4330d860f98089e2a4dd35874a1575d65 |
| FirstAdministratorProvisioningTests | 15 | 3a5f49b4330d860f98089e2a4dd35874a1575d65 |
| FirstAdministratorLoginTests | 1 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| UtilizadorControllerTests | 7 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| SecurityIntegrationTests | 26 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| PermissionMatrixIntegrationTests | 6 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| SecuritySettingsTests | 6 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| DocumentTypeImmutabilityTests | 17 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| FinancialDocumentClassificationTests | 14 | 7fc4e28aaeaeda4a91402698f94345320198ab92 |
| FiscalMigrationCompatibilityTests | 1 | 077a44a1f6defd60e1a498ce3a333f9fc726c77f |

**99 casos distintos aprovados, zero falhas/erros/ignorados nas execuções finais de cada classe**, em dez invocações Maven aprovadas. Não é uma execução consolidada única nem a suite integral. Total novo M02: 22; regressões dirigidas: 77. Não há CI/PR nesta missão.

Duas tentativas falhadas preservadas, sem as contar como aprovações ou casos únicos adicionais:

1. Login: o novo teste esperava 401 para password incorrecta; `AuthService` e os testes existentes usam 400. Corrigida apenas a expectativa para preservar esse contrato. O acesso protegido sem JWT continua a exigir 401; login válido/autoridade administrativa passaram.
2. Compatibilidade de migrations: fixture histórica começava em V15 mas ainda esperava oito migrations e V23. A primeira adaptação numérica não contemplou a defasagem preexistente e falhou (esperava 9, recebeu 14). Corrigidas exclusivamente contagem 14 (V16–V29) e versão final 29; a preservação dos catálogos/documento antigo e segunda aplicação sem migrations passou. Não se alteraram os respectivos contratos ou dados reais.

O código funcional/migration V29 é idêntico em `3a5f49b`, `7fc4e28` e `077a44a`; os dois últimos só alteram testes/runner. O commit final de fecho acrescenta apenas documentação. Capturas HEAD/tree/status limpo antes/depois por classe, logs e XML preservados; não se apresenta o ensaio como realizado no commit documental posterior.

Package offline `mvn -o -B -DskipTests package`: BUILD SUCCESS em `077a44a`. Smoke a partir do JAR por PropertiesLauncher/FirstAdministratorCli `--help`: saída 0, texto de uso local, sem arranque Spring/servidor. CLI sem consola recusada no teste automatizado. Não houve execução interactiva de conta real, nem teste humano completo da consola.

## Arquivo verificável

Arquivo separado `/home/arosario/tuuli-backups/m02/2026-11-03/`, **100 entradas + manifesto geral**, sem ficheiros de credenciais/snapshots. `MANIFEST_ALL_SHA256.json`: 20 463 bytes, SHA-256 **`4cdcbd11d90c1c69c35fc6badf130f47f8e0fc550860418d5530f0ed622f83b0`**. Três subarquivos, com manifestos por tentativa:

- `20261103-175832-799957`: 25 entradas, SHA-256 `3a739e3ccae570c33f538dd9f3fc2ac08173421c054b59315f326e3e497c05d3`.
- `20261103-180111-103206`: 55 entradas, SHA-256 `6657bf72da7526dc3b79cec4a83045c22d90b9abdd14e50f64a7348f2bd6d768`.
- `20261103-180602-8bb2de`: 17 entradas, SHA-256 `a910e43295c2e2ef4b20d146e69ea3dcc154bc7b295c44144883f382ed858e98`.

Cada entrada possui caminho relativo, tamanho e SHA-256; ficheiros/digests e cópia conferidos. Logs das falhas contêm apenas fixtures e tokens sintéticos efémeros e permanecem privados; não os publicar como logs comerciais. Segredos técnicos não constam dos artefactos (scanner antes do manifesto), todos os contentores/redes M02 foram removidos com guards de ID/label e PGDATA tmpfs descartado. Metadata final filtrada por label M02: zero contentores/redes. Sem SQL na preparação conservada/histórica; M01/#38 não repetidas e respectivos arquivos não modificados.

Verificações adicionais: referências documentais, sintaxe Python, fonte funcional idêntica entre commits ensaiados e `git diff --check`. `FlywaySchemaIntegrationTests` conserva uma expectativa V23 preexistente e não faz parte da execução dirigida; não se declara suite integral ou validação de todos os testes históricos. A correcção funcional M02 não depende dessa expectativa desactualizada.

**Estado: IMPLEMENTADO E TESTADO EM ISOLAMENTO; revisão independente Claude pendente.** Ainda sem aprovação independente da solução. [Pedido de revisão](REVIEW_HANDOFF.md). Sem autorização operacional, deploy, push/PR/merge, M03–M05 ou alterações de gates.


## M02-R1 — Prova incremental posterior ao parecer

Primeiro parecer Claude sobre `5ae9e50`: APROVADO COM RESERVAS 0/0/2, original/sidecar SHA-256 `790a988f0b9d5af3f0e037387c02a91cd389057d26338d151cfb3aa5bd52744a` conferidos. Supera a indicação histórica de revisão ainda não recebida; não aprova R1. [Resolução/evidência R1](R1.md): MINOR-001/002 corrigidos e O-1 com READ COMMITTED em Java/SQL. **33 casos dirigidos aprovados**, package/smoke aprovados no commit limpo `b3eb249fb1d13795af72fadc32dade62b5686896`, tree `a5462552377b318c7afef448e1d427817d61c45d`, Git antes/depois igual. Não se repetiram os 99 casos ou M01/#38.

Arquivo R1, 41 artefactos + manifesto de 7698 bytes: `tuuli-backups/m02-r1/2026-11-03/`, SHA-256 `56bc1818a820f8f054b74f17416a8de90865e8dff7f07015f5270035f2771f91`. O arquivo e relatório originais M02 permanecem intactos. O-2 dono restrito não ensaiado positivamente, O-3 resolução localhost, O-4 dívida de testes e O-5 colisão condicional com worktree desconhecida explicitados; não alegar prova inexistente ou nova missão. Java nativo disponível: 21.0.12.1; caminhos de decisão 22/25 testados por probes, não por execução de JDK 22+. Guarda efectiva implementada, não mera redução da garantia. Confirmação incremental Claude pendente.
