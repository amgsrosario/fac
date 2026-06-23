[CmdletBinding()]
param(
    [string]$Database = $(if ($env:FAC_DEMO_DATABASE) { $env:FAC_DEMO_DATABASE } else { "fac_demo" }),
    [string]$Container = $(if ($env:FAC_DEMO_DB_CONTAINER) { $env:FAC_DEMO_DB_CONTAINER } else { "facdb" }),
    [string]$DbUser = $(if ($env:PGUSER) { $env:PGUSER } else { "postgres" })
)

$ErrorActionPreference = "Stop"
if ($Database -ne "fac_demo" -or $Database -match "(?i)(prod|fac$|fac_test)") { throw "Reset recusado: a unica base permitida e fac_demo." }
if ($env:SPRING_PROFILES_ACTIVE -ne "demo") { throw "Reset recusado: SPRING_PROFILES_ACTIVE deve ser demo." }
if ($env:FAC_DEMO_RESET_AUTHORIZED -ne "true") { throw "Reset recusado: defina FAC_DEMO_RESET_AUTHORIZED=true." }
foreach ($name in 'FAC_DEMO_PASSWORD_ADMIN','FAC_DEMO_PASSWORD_OPERADOR','FAC_DEMO_PASSWORD_CONSULTA') {
    $value = [Environment]::GetEnvironmentVariable($name)
    if ([string]::IsNullOrWhiteSpace($value) -or $value.Length -lt 8) { throw "Reset recusado: $name deve estar definido com pelo menos 8 caracteres." }
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw "Docker nao encontrado." }

& docker exec $Container dropdb -U $DbUser --if-exists --force $Database
if ($LASTEXITCODE -ne 0) { throw "Nao foi possivel remover a base demo." }
& docker exec $Container createdb -U $DbUser $Database
if ($LASTEXITCODE -ne 0) { throw "Nao foi possivel criar a base demo." }

$env:DATASOURCE_URL = "jdbc:postgresql://localhost:25432/$Database"
$env:DATASOURCE_USERNAME = $DbUser
$env:DATASOURCE_PASSWORD = $(if ($env:PGPASSWORD) { $env:PGPASSWORD } else { "postgres" })
$env:FAC_DEMO_SEED_ON_STARTUP = "true"
$env:FAC_DEMO_EXIT_AFTER_SEED = "true"

& mvn -q spring-boot:run "-Dspring-boot.run.profiles=demo" "-Dspring-boot.run.arguments=--server.port=0"
if ($LASTEXITCODE -ne 0) { throw "Seed demo falhou com codigo $LASTEXITCODE." }
Write-Output "FAC Demo Partner Edition reposta e validada na base '$Database'."
