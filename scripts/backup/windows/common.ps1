Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-ProjectRoot {
    return (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
}

function Fail([string]$Message) {
    Write-Error $Message
    exit 1
}

function Require-Value([string]$Name, [string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) { Fail "$Name e obrigatorio." }
}

function Assert-DatabaseName([string]$Database) {
    Require-Value "Database" $Database
    if ($Database -match '[*?]' -or $Database -notmatch '^[A-Za-z_][A-Za-z0-9_]{0,62}$') { Fail "Nome de base invalido: $Database" }
    $blocked = @("fac","postgres","template0","template1")
    if ($blocked -contains $Database.ToLowerInvariant()) { Fail "Base recusada: $Database" }
}

function Assert-RestoreTarget([string]$Database) {
    Assert-DatabaseName $Database
    $allowed = @("fac_restore_test","fac_demo","fac_test")
    if ($allowed -notcontains $Database.ToLowerInvariant()) { Fail "Destino nao autorizado: $Database" }
}

function Assert-Environment([string]$Environment) {
    Require-Value "Environment" $Environment
    if (@("demo","test","production") -notcontains $Environment.ToLowerInvariant()) { Fail "Ambiente nao reconhecido: $Environment" }
}

function Get-BackupRoot([string]$Environment, [string]$BackupRoot) {
    $projectRoot = Get-ProjectRoot
    if ([string]::IsNullOrWhiteSpace($BackupRoot)) {
        $BackupRoot = Join-Path $projectRoot "backups\$Environment"
    }
    $path = New-Item -ItemType Directory -Force -Path $BackupRoot
    return $path.FullName
}

function Get-AuditLogPath {
    $root = Get-ProjectRoot
    $logDir = New-Item -ItemType Directory -Force -Path (Join-Path $root "backups\logs")
    return Join-Path $logDir.FullName "backup-restore-audit.log"
}

function Write-Audit([string]$Operation, [string]$Environment, [string]$Database, [string]$Result, [string]$Details) {
    $line = [ordered]@{
        timestamp = (Get-Date).ToString("o")
        operation = $Operation
        environment = $Environment
        database = $Database
        result = $Result
        user = [Environment]::UserName
        details = $Details
    } | ConvertTo-Json -Compress
    Add-Content -Path (Get-AuditLogPath) -Value $line
}

function Get-GitCommit {
    $root = Get-ProjectRoot
    $commit = (& git -C $root rev-parse --short HEAD 2>$null)
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($commit)) { return "unknown" }
    $dirty = (& git -C $root status --short 2>$null)
    if ($dirty) { return "$commit-dirty" }
    return $commit
}

function Get-BackupBaseName([string]$Environment, [string]$Database) {
    $stamp = Get-Date -Format "yyyy-MM-dd_HHmmss"
    $commit = (Get-GitCommit) -replace '[^A-Za-z0-9_.-]', '-'
    return "fac_${Environment}_${Database}_${stamp}_${commit}"
}

function Invoke-Pg {
    param(
        [Parameter(Mandatory=$true)][string]$Tool,
        [Parameter(Mandatory=$true)][string[]]$Arguments,
        [string]$Password
    )
    if (-not (Get-Command $Tool -ErrorAction SilentlyContinue)) { Fail "$Tool nao encontrado no PATH." }
    $old = $env:PGPASSWORD
    try {
        if ($Password) { $env:PGPASSWORD = $Password }
        & $Tool @Arguments
        if ($LASTEXITCODE -ne 0) { Fail "$Tool falhou com codigo $LASTEXITCODE." }
    } finally {
        $env:PGPASSWORD = $old
    }
}

function Invoke-PsqlScalar {
    param(
        [Parameter(Mandatory=$true)][string[]]$Arguments,
        [string]$Password
    )
    if (-not (Get-Command "psql" -ErrorAction SilentlyContinue)) { Fail "psql nao encontrado no PATH." }
    $old = $env:PGPASSWORD
    try {
        if ($Password) { $env:PGPASSWORD = $Password }
        $result = & psql @Arguments
        if ($LASTEXITCODE -ne 0) { Fail "psql falhou com codigo $LASTEXITCODE." }
        return $result
    } finally {
        $env:PGPASSWORD = $old
    }
}

function Invoke-DockerCompose {
    param([Parameter(Mandatory=$true)][string[]]$Arguments)
    $root = Get-ProjectRoot
    if (Get-Command "docker-compose" -ErrorAction SilentlyContinue) {
        & docker-compose --project-directory $root --env-file (Join-Path $root ".env.demo") -f (Join-Path $root "compose.demo.yaml") @Arguments
    } else {
        & docker compose --project-directory $root --env-file (Join-Path $root ".env.demo") -f (Join-Path $root "compose.demo.yaml") @Arguments
    }
    if ($LASTEXITCODE -ne 0) { Fail "docker compose falhou com codigo $LASTEXITCODE." }
}

function Invoke-DockerComposeOutput {
    param([Parameter(Mandatory=$true)][string[]]$Arguments)
    $root = Get-ProjectRoot
    if (Get-Command "docker-compose" -ErrorAction SilentlyContinue) {
        $output = & docker-compose --project-directory $root --env-file (Join-Path $root ".env.demo") -f (Join-Path $root "compose.demo.yaml") @Arguments
    } else {
        $output = & docker compose --project-directory $root --env-file (Join-Path $root ".env.demo") -f (Join-Path $root "compose.demo.yaml") @Arguments
    }
    if ($LASTEXITCODE -ne 0) { Fail "docker compose falhou com codigo $LASTEXITCODE." }
    return $output
}

function Get-BackupChecksum([string]$Path) {
    return (Get-FileHash -Algorithm SHA256 -Path $Path).Hash.ToLowerInvariant()
}

function Read-Metadata([string]$BackupFile) {
    $metadata = [IO.Path]::ChangeExtension($BackupFile, ".metadata.json")
    if (-not (Test-Path -LiteralPath $metadata)) { Fail "Metadados inexistentes: $metadata" }
    return Get-Content -LiteralPath $metadata -Raw | ConvertFrom-Json
}

function Confirm-Strong([string]$Expected, [string]$Provided, [switch]$NonInteractive) {
    if ($Provided -ne $Expected) { Fail "Confirmacao recusada: escreva exatamente '$Expected'." }
    if (-not $NonInteractive) {
        Write-Host "Confirmacao aceite para $Expected."
    }
}
