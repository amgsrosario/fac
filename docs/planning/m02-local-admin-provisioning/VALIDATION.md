# M02 — Plano de validação e evidência

Base ca1e802 (PR #49); nova branch isolada codex/m02-local-admin-provisioning. Sem instalação real. Resultados pendentes até execução; não são testes aprovados por planeamento.

Runner `run_validation.py`: imagens locais existentes PostgreSQL 16.3/Node, sem download; cluster tmpfs/rede interna/proxy loopback, sem volumes conservados. Role SQL sintética restrita e identidades @example.test. Cada classe usa base descartável própria; os testes de provisionamento usam bases adicionais próprias por caso, para não resetar o comprovativo artificialmente. Sem snapshot ou repetição #38/M01.

Capturar HEAD/tree/status/fonte antes/depois de cada execução, exigindo worktree limpa. Preservar logs/XML e manifesto SHA-256 sem segredos; remover somente recursos com IDs/labels M02. Executar primeiro as três classes novas, depois UtilizadorControllerTests, SecurityIntegrationTests, PermissionMatrixIntegrationTests, SecuritySettingsTests e as três classes de upgrades afectadas por V29. Ajustes destas últimas são apenas contagens/versão esperadas pela migration aditiva; não alteram V25/V26/V27. Package e smoke `--help` pelo JAR, sem Spring/DB.

Cobertura dirigida: identidade incorrecta/base/cluster/UUID/role, expiração, administrador activo/inactivo e outros utilizadores, repetição, duas chamadas simultâneas, INSERT normal concorrente, auditoria falhada com rollback e retry, ausência de permissões directas/PUBLIC, comprovativo imutável, password policy/memória, V28→V29 com utilizadores preservados/inelegibilidade persistente, validate/segundo arranque, ficheiro 0600/symlink/duplicados/URL, ausência de consola, login/JWT/permissões/auditoria após provisionamento. Não é auditoria geral nem certificação.
