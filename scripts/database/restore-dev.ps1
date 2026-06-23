[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$BackupFile,
    [Parameter(Mandatory = $true)][string]$ConfirmDatabase,
    [string]$Database = $(if ($env:FAC_DEV_DATABASE) { $env:FAC_DEV_DATABASE } else { "fac" }),
    [string]$HostName = $(if ($env:PGHOST) { $env:PGHOST } else { "localhost" }),
    [int]$Port = $(if ($env:PGPORT) { [int]$env:PGPORT } else { 25432 }),
    [string]$User = $(if ($env:PGUSER) { $env:PGUSER } else { "postgres" })
)

$ErrorActionPreference = "Stop"
if ($Database -ne "fac" -or $Database -match "(?i)prod") {
    throw "Reposição recusada: a base de desenvolvimento esperada é 'fac'."
}
if ($ConfirmDatabase -ne $Database) {
    throw "Reposição recusada: indique -ConfirmDatabase $Database explicitamente."
}
if (-not (Test-Path -LiteralPath $BackupFile -PathType Leaf)) {
    throw "Ficheiro de backup não encontrado: $BackupFile"
}
if (-not (Get-Command pg_restore -ErrorAction SilentlyContinue)) {
    throw "pg_restore não foi encontrado no PATH. Instale as ferramentas cliente do PostgreSQL."
}

& pg_restore --host=$HostName --port=$Port --username=$User --dbname=$Database --clean --if-exists --no-owner --no-privileges $BackupFile
if ($LASTEXITCODE -ne 0) {
    throw "pg_restore terminou com o código $LASTEXITCODE."
}
Write-Output "Base '$Database' reposta a partir de: $BackupFile"

