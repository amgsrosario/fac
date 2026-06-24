[CmdletBinding()]
param(
    [string]$Database = $(if ($env:FAC_DEMO_DATABASE) { $env:FAC_DEMO_DATABASE } else { "fac_demo" }),
    [string]$DbUser = $(if ($env:PGUSER) { $env:PGUSER } else { "postgres" })
)

$ErrorActionPreference = "Stop"
if ($Database -ne "fac_demo" -or $Database -match "(?i)(prod|fac$|fac_test)") {
    throw "Verificacao recusada: a unica base permitida e fac_demo."
}
if ($env:SPRING_PROFILES_ACTIVE -ne "demo") {
    throw "Verificacao recusada: SPRING_PROFILES_ACTIVE deve ser demo."
}

# Este comando valida apenas. Flyway e seed ficam explicitamente desativados.
$env:DATASOURCE_URL = "jdbc:postgresql://localhost:25432/$Database"
$env:DATASOURCE_USERNAME = $DbUser
$env:DATASOURCE_PASSWORD = $(if ($env:PGPASSWORD) { $env:PGPASSWORD } else { "postgres" })
$env:SPRING_FLYWAY_ENABLED = "false"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = "validate"
$env:FAC_DEMO_SEED_ON_STARTUP = "false"
$env:FAC_DEMO_RESET_AUTHORIZED = "false"
$env:FAC_DEMO_CHECK_ON_STARTUP = "true"
$env:FAC_DEMO_EXIT_AFTER_CHECK = "true"
$env:SPRING_JPA_SHOW_SQL = "false"
$env:LOGGING_LEVEL_ORG_HIBERNATE_SQL = "WARN"

& mvn -q -DskipTests package
if ($LASTEXITCODE -ne 0) { throw "Build do backend falhou com codigo $LASTEXITCODE." }
$jar = Get-ChildItem -Path "target" -Filter "fac-*.jar" | Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1
if (-not $jar) { throw "JAR executavel do FAC nao encontrado." }
& java -jar $jar.FullName --spring.profiles.active=demo --server.port=0
if ($LASTEXITCODE -ne 0) { throw "Verificacao demo falhou com codigo $LASTEXITCODE." }
