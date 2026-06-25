# FAC - MD30: Backup, restauro e recuperacao controlada

## Diagnostico

O ambiente demo usa `compose.demo.yaml`, servico PostgreSQL `db`, imagem `postgres:16.3-alpine`, base `fac_demo` e volume `fac-demo-postgres-data`. As variaveis locais vivem em `.env.demo`, derivado de `.env.demo.example`, sem passwords em Git.

Os scripts existentes de demo ja tinham reset e check. Em Linux usam Docker Compose v2 atraves de `scripts/demo/linux/common.sh`; em Windows havia scripts historicos que apontavam para uma porta/container de desenvolvimento. O MD30 introduz scripts administrativos dedicados para backup/restauro sem alterar o reset demo existente.

As migrations Flyway estao em `src/main/resources/db/migration` e a tabela de controlo e `flyway_schema_history`. Os objetos essenciais validados incluem `empresa`, `utilizador`, `cliente`, `artigo`, `documento_comercial`, `linha_documento_comercial`, `documento_financeiro`, `mpagamento`, `serie`, `auditoria_evento` e `importacao_dados_mestres`.

## Arquitetura escolhida

O formato de backup e o custom format do PostgreSQL:

```text
pg_dump -Fc
```

Este formato foi escolhido porque e compativel com `pg_restore`, permite inspecao com `pg_restore --list`, suporta compressao, permite restauro por objetos e e mais robusto que SQL plano para validacao/restauro controlado.

Cada backup gera:

```text
backups/<ambiente>/fac_<ambiente>_<base>_<data>_<hora>_<commit>.backup
backups/<ambiente>/fac_<ambiente>_<base>_<data>_<hora>_<commit>.metadata.json
```

Os ficheiros reais ficam ignorados pelo Git. Apenas `backups/.gitkeep` e documentacao podem ser versionados.

## Salvaguardas

Os scripts recusam bases vazias, nomes com wildcards, `fac`, `postgres`, `template0` e `template1`. Restauro so e permitido para `fac_restore_test`, `fac_demo` ou `fac_test`, sempre com confirmacao explicita do nome completo da base.

O destino recomendado para ensaio e `fac_restore_test`. O MD30 nao exige UI nem endpoint publico de restauro.

## Scripts criados

Windows:

```text
scripts/backup/windows/backup-database.ps1
scripts/backup/windows/restore-database.ps1
scripts/backup/windows/verify-backup.ps1
scripts/backup/windows/list-backups.ps1
scripts/backup/windows/cleanup-backups.ps1
scripts/demo/backup-demo.ps1
scripts/demo/restore-demo.ps1
```

Linux:

```text
scripts/backup/linux/backup-database.sh
scripts/backup/linux/restore-database.sh
scripts/backup/linux/verify-backup.sh
scripts/backup/linux/list-backups.sh
scripts/backup/linux/cleanup-backups.sh
scripts/demo/linux/backup-demo.sh
scripts/demo/linux/restore-demo.sh
```

## Validacao

A validacao estrutural confirma:

- existencia e tamanho do ficheiro;
- checksum SHA-256;
- metadados coerentes;
- leitura por `pg_restore --list`;
- presenca de objetos essenciais;
- tabela Flyway;
- objetos comerciais, financeiros, auditoria, utilizadores e configuracao da empresa.

O restauro valida contagens e constraints apos `pg_restore`. Quando o destino contem uma copia da demo, o verificador funcional Spring pode ser executado apontando `DATASOURCE_URL` para `fac_restore_test` e definindo `FAC_DEMO_EXPECTED_DATABASE=fac_restore_test`.

## Resultado esperado

Um ensaio completo deve terminar com:

```text
FAC_BACKUP_RESTORE_OK
```

O ensaio nao usa nem altera a base `fac`.

## Retencao

A limpeza tem modo seguro e simulado. A implementacao preserva a uniao dos ultimos 7 backups diarios, 4 semanais e 6 mensais do diretorio configurado, e nunca atua fora de `backups/`. Ficheiros com sufixo `.protected.backup` nao sao removidos.

## Cifragem

A cifragem nao e obrigatoria neste modulo. Opcoes recomendadas para fase futura: `age`, GPG, arquivo cifrado ou armazenamento cifrado pelo sistema operativo. O MD30 garante ausencia de segredos nos metadados/logs e backups fora do Git.

## Limites

Nao foram implementados cloud backup, agendamento centralizado, PITR, WAL archiving, replicacao, alta disponibilidade, UI publica ou restauro remoto.
