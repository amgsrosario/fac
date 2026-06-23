[CmdletBinding()]
param(
    [string]$Database = $(if ($env:FAC_DEV_DATABASE) { $env:FAC_DEV_DATABASE } else { "fac" }),
    [string]$HostName = $(if ($env:PGHOST) { $env:PGHOST } else { "localhost" }),
    [int]$Port = $(if ($env:PGPORT) { [int]$env:PGPORT } else { 25432 }),
    [string]$User = $(if ($env:PGUSER) { $env:PGUSER } else { "postgres" }),
    [string]$OutputDirectory = "backups/database"
)

$ErrorActionPreference = "Stop"
if ($Database -ne "fac" -or $Database -match "(?i)prod") {
    throw "Backup recusado: a base de desenvolvimento esperada é 'fac'."
}
if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) {
    throw "pg_dump não foi encontrado no PATH. Instale as ferramentas cliente do PostgreSQL."
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$output = Join-Path $OutputDirectory "fac-dev-$timestamp.dump"

& pg_dump --host=$HostName --port=$Port --username=$User --format=custom --no-owner --no-privileges --file=$output $Database
if ($LASTEXITCODE -ne 0) {
    throw "pg_dump terminou com o código $LASTEXITCODE."
}
Write-Output "Backup criado: $output"

