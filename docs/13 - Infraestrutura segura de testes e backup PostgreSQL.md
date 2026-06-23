# 21 — Infraestrutura segura de testes e backup PostgreSQL

## Bases e perfis

| Perfil | Base esperada | Finalidade |
|---|---|---|
| `dev` | `fac` | desenvolvimento normal |
| `test` | nome terminado em `_test`, por omissão `fac_test` | testes automatizados |
| `prod` | definida externamente | produção futura |

O perfil por omissão da aplicação é `dev`. O Maven ativa `test` apenas durante a suite. A configuração de `fac_test` está em `src/test/resources` e não faz parte do runtime normal.

Antes de qualquer teste Spring com `DataSource`, um listener central confirma simultaneamente:

1. perfil `test` ativo;
2. propriedade `fac.test-database.destructive-operations-enabled=true`;
3. nome real devolvido pelo PostgreSQL terminado em `_test`.

Se uma condição falhar, a suite aborta antes dos métodos `@BeforeEach` e das limpezas de fixtures.

## Preparar `fac_test`

É necessário PostgreSQL real. Os testes de lock e concorrência não usam H2.

Enquanto o projeto não possuir migrações formais, a base de teste deve receber o mesmo schema da base de desenvolvimento, mas não os documentos comerciais. Exemplo conceptual com ferramentas PostgreSQL:

```bash
createdb -h localhost -p 25432 -U postgres fac_test
pg_dump -h localhost -p 25432 -U postgres --schema-only --no-owner --no-privileges fac | psql -h localhost -p 25432 -U postgres fac_test
pg_dump -h localhost -p 25432 -U postgres --data-only --inserts \
  --table=codpostal --table=moeda --table=ppagamento --table=ivasaft \
  --table=misencao --table=tipotaxaiva --table=riva --table=riva_taxa \
  --table=pais --table=freguesia --table=empresa fac \
  | psql -h localhost -p 25432 -U postgres fac_test
```

Na instalação Docker local também se pode criar inicialmente a base a partir do template `fac`, desde que se compreenda que é apenas uma fotografia isolada e que os testes poderão limpar os dados copiados:

```powershell
docker exec facdb createdb -U postgres --template=fac fac_test
```

Nunca usar `fac` como destino da suite.

## Executar testes

```powershell
mvn test
```

Configuração opcional, sempre apontando para uma base terminada em `_test`:

```powershell
$env:FAC_TEST_DATASOURCE_URL='jdbc:postgresql://localhost:25432/fac_test'
$env:FAC_TEST_DATASOURCE_USERNAME='postgres'
$env:FAC_TEST_DATASOURCE_PASSWORD='...'
mvn test
```

Mesmo que uma variável seja configurada incorretamente para `fac`, a validação do nome real da base recusa a execução.

## Fixtures

- Preferir rollback transacional quando compatível.
- Fixtures não transacionais devem usar tipos, séries e códigos exclusivos.
- A fixture de concorrência usa `IC1`/`IC2` e elimina apenas esses universos.
- `deleteAll`, `DELETE` e outras limpezas nunca ficam protegidos apenas por convenção: o listener central é obrigatório.

## Backup de desenvolvimento

Os scripts não guardam palavras-passe. Usar `PGPASSWORD`, `.pgpass`/`pgpass.conf` ou outro mecanismo externo.

PowerShell:

```powershell
.\scripts\database\backup-dev.ps1
.\scripts\database\restore-dev.ps1 -BackupFile .\backups\database\fac-dev-AAAAMMDD-HHMMSS.dump -ConfirmDatabase fac
```

Linux/macOS:

```bash
./scripts/database/backup-dev.sh
CONFIRM_DATABASE=fac ./scripts/database/restore-dev.sh backups/database/fac-dev-AAAAMMDD-HHMMSS.dump
```

Os scripts aceitam `PGHOST`, `PGPORT`, `PGUSER` e `FAC_DEV_DATABASE`. Recusam nomes diferentes de `fac` e qualquer nome que contenha `prod`. A reposição exige ainda confirmação explícita.

Os dumps são ignorados pelo Git. A reposição nunca é automática.

## Limitação atual

A criação reproduzível de uma base vazia ainda depende do schema PostgreSQL existente porque a adoção de Flyway pertence a outro bloco. Esta limitação não permite que os testes usem `fac`: apenas afeta a preparação inicial de `fac_test` e deverá ser eliminada quando existir um mecanismo formal de migração.

