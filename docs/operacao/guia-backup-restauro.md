# Guia operacional - Backup e restauro FAC

## Conceitos

Um backup FAC e composto por um ficheiro PostgreSQL custom (`.backup`) e um ficheiro de metadados (`.metadata.json`). O checksum SHA-256 dos metadados tem de coincidir com o ficheiro antes de qualquer restauro.

Nunca usar a base `fac` para testes. Para validacao usar `fac_restore_test`.

## Primeira configuracao

No demo Docker, preencher `.env.demo` a partir de `.env.demo.example`. Nao versionar passwords.

Ferramentas esperadas:

- Windows: PowerShell, Docker, PostgreSQL client tools (`pg_dump`, `pg_restore`, `psql`) quando a operacao nao for totalmente Docker.
- Ubuntu: Bash, Docker Compose v2, `pg_restore`; para modo host tambem `pg_dump`, `psql`, `createdb` e `dropdb`.

## Criar backup da demo

Windows:

```powershell
.\scripts\demo\backup-demo.ps1
```

Linux:

```bash
./scripts/demo/linux/backup-demo.sh
```

Resultado esperado:

```text
FAC_BACKUP_OK <ficheiro.backup> <sha256>
```

## Criar backup parametrizado

Windows:

```powershell
.\scripts\backup\windows\backup-database.ps1 `
  -Database fac_test `
  -Environment test `
  -HostName localhost `
  -Port 5432 `
  -User fac_test_user
```

Linux:

```bash
./scripts/backup/linux/backup-database.sh \
  --database fac_test \
  --environment test \
  --host localhost \
  --port 5432 \
  --user fac_test_user
```

Definir `PGPASSWORD` apenas na sessao local quando necessario. Nao colocar passwords nos comandos nem em ficheiros versionados.

## Listar backups

Windows:

```powershell
.\scripts\backup\windows\list-backups.ps1 -Environment demo
```

Linux:

```bash
./scripts/backup/linux/list-backups.sh demo
```

## Validar backup

Windows:

```powershell
.\scripts\backup\windows\verify-backup.ps1 -BackupFile .\backups\demo\<ficheiro>.backup
```

Linux:

```bash
./scripts/backup/linux/verify-backup.sh ./backups/demo/<ficheiro>.backup
```

Resultado esperado:

```text
FAC_BACKUP_VERIFY_OK
```

## Restaurar para base temporaria

Windows, modo host:

```powershell
.\scripts\backup\windows\restore-database.ps1 `
  -BackupFile .\backups\demo\<ficheiro>.backup `
  -TargetDatabase fac_restore_test `
  -Environment demo `
  -HostName localhost `
  -Port 25432 `
  -User fac_demo_user `
  -ConfirmDatabase fac_restore_test `
  -DropAfterValidation
```

Linux, modo Docker demo:

```bash
./scripts/backup/linux/restore-database.sh \
  --backup ./backups/demo/<ficheiro>.backup \
  --target-database fac_restore_test \
  --environment demo \
  --user "$FAC_DEMO_DB_USER" \
  --confirm-database fac_restore_test \
  --docker-demo \
  --drop-after-validation
```

Resultado esperado:

```text
FAC_BACKUP_RESTORE_OK origem=<ficheiro.backup> destino=fac_restore_test
```

## Verificacao funcional da demo restaurada

Para executar o check Spring contra `fac_restore_test`, usar o perfil demo com:

```text
DATASOURCE_URL=jdbc:postgresql://localhost:25432/fac_restore_test
FAC_DEMO_EXPECTED_DATABASE=fac_restore_test
FAC_DEMO_CHECK_ON_STARTUP=true
FAC_DEMO_EXIT_AFTER_CHECK=true
SPRING_FLYWAY_ENABLED=false
```

O resultado funcional esperado e:

```text
FAC_DEMO_CHECK OK
```

## Limpeza e retencao

Simular limpeza:

```powershell
.\scripts\backup\windows\cleanup-backups.ps1 -Environment demo -DryRun
```

```bash
./scripts/backup/linux/cleanup-backups.sh --environment demo --dry-run
```

A limpeza atua apenas dentro de `backups/<ambiente>` e preserva a uniao dos ultimos 7 backups diarios, 4 semanais e 6 mensais. Marcar um ficheiro como protegido usando o sufixo `.protected.backup`.

## Mensagens de erro comuns

`Base recusada`: foi indicada `fac`, `postgres`, `template0`, `template1` ou nome invalido.

`Destino nao autorizado`: o restauro tentou usar uma base fora da lista controlada.

`Checksum invalido`: o ficheiro nao corresponde aos metadados e nao deve ser restaurado.

`Objeto essencial ausente`: o backup nao contem uma tabela obrigatoria da FAC.

## Recuperacao apos falha

Se a restauracao falhar em `fac_restore_test`, eliminar a base temporaria e repetir a partir de um backup validado. Se a falha ocorrer em `fac_demo`, criar um backup preventivo antes de qualquer tentativa e preferir reset demo seguido de novo ensaio.

## Seguranca

Nao guardar backups reais, dumps SQL, passwords, connection strings completas ou dados pessoais no Git. Os metadados nao incluem passwords. Os logs administrativos ficam em `backups/logs/backup-restore-audit.log` e tambem sao ignorados.
