[CmdletBinding()]
param(
    [switch]$Reset,
    [switch]$SkipBackup,
    [string]$ComposeProjectDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Fail([string]$Message) {
    Write-Error $Message
    exit 1
}

function Invoke-Compose([string[]]$Arguments) {
    if (Get-Command "docker-compose" -ErrorAction SilentlyContinue) {
        & docker-compose --project-directory $ProjectRoot --env-file $EnvFile -f $ComposeFile @Arguments
    } else {
        & docker compose --project-directory $ProjectRoot --env-file $EnvFile -f $ComposeFile @Arguments
    }
    if ($LASTEXITCODE -ne 0) { Fail "docker compose falhou com codigo $LASTEXITCODE." }
}

function Load-DemoEnv {
    if (-not (Test-Path -LiteralPath $EnvFile)) {
        Copy-Item -LiteralPath (Join-Path $ProjectRoot ".env.demo.example") -Destination $EnvFile
        Fail "Foi criado .env.demo; preencha os valores locais e repita o comando."
    }
    Get-Content -LiteralPath $EnvFile | Where-Object { $_ -match '^\s*[^#][^=]+=' } | ForEach-Object {
        $parts = $_ -split '=', 2
        [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
    }
    if ($env:SPRING_PROFILES_ACTIVE -ne "demo") { Fail "SPRING_PROFILES_ACTIVE deve ser demo." }
    if ($env:FAC_DEMO_DATABASE -ne "fac_demo") { Fail "FAC_DEMO_DATABASE deve ser fac_demo." }
    foreach ($name in "FAC_DEMO_DB_USER","FAC_DEMO_DB_PASSWORD","FAC_JWT_SECRET") {
        $value = [Environment]::GetEnvironmentVariable($name)
        if ([string]::IsNullOrWhiteSpace($value) -or $value -like "*definir*") { Fail "$name e obrigatorio em .env.demo." }
    }
}

$ProjectRoot = if ($ComposeProjectDirectory) { (Resolve-Path $ComposeProjectDirectory).Path } else { (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path }
$EnvFile = Join-Path $ProjectRoot ".env.demo"
$ComposeFile = Join-Path $ProjectRoot "compose.demo.yaml"

Load-DemoEnv
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { Fail "Docker nao encontrado." }

Write-Output "FAC Commercial Demo: a validar Compose"
Invoke-Compose @("config","--quiet")
Write-Output "FAC Commercial Demo: a iniciar PostgreSQL"
Invoke-Compose @("up","-d","db")

if ($Reset) {
    Write-Output "FAC Commercial Demo: reset controlado da base fac_demo"
    if ($env:FAC_DEMO_DATABASE -ne "fac_demo") { Fail "Reset recusado: FAC_DEMO_DATABASE deve ser fac_demo." }
    Invoke-Compose @("exec","-T","db","dropdb","-U",$env:FAC_DEMO_DB_USER,"--maintenance-db=postgres","--if-exists","--force","fac_demo")
    Invoke-Compose @("exec","-T","db","createdb","-U",$env:FAC_DEMO_DB_USER,"fac_demo")
    Invoke-Compose @("run","--rm","--no-deps",
        "-e","FAC_DEMO_RESET_AUTHORIZED=true",
        "-e","FAC_DEMO_SEED_ON_STARTUP=true",
        "-e","FAC_DEMO_EXIT_AFTER_SEED=true",
        "-e","FAC_DEMO_PASSWORD_ADMIN",
        "-e","FAC_DEMO_PASSWORD_OPERADOR",
        "-e","FAC_DEMO_PASSWORD_CONSULTA",
        "backend","--server.port=0")
}

Write-Output "FAC Commercial Demo: check funcional"
Invoke-Compose @("run","--rm","--no-deps",
    "-e","SPRING_FLYWAY_ENABLED=false",
    "-e","SPRING_JPA_HIBERNATE_DDL_AUTO=validate",
    "-e","FAC_DEMO_RESET_AUTHORIZED=false",
    "-e","FAC_DEMO_SEED_ON_STARTUP=false",
    "-e","FAC_DEMO_CHECK_ON_STARTUP=true",
    "-e","FAC_DEMO_EXIT_AFTER_CHECK=true",
    "backend","--server.port=0")

Write-Output "FAC Commercial Demo: check comercial"
Invoke-Compose @("run","--rm","--no-deps",
    "-e","SPRING_FLYWAY_ENABLED=false",
    "-e","SPRING_JPA_HIBERNATE_DDL_AUTO=validate",
    "-e","FAC_DEMO_RESET_AUTHORIZED=false",
    "-e","FAC_DEMO_SEED_ON_STARTUP=false",
    "-e","FAC_COMMERCIAL_DEMO_CHECK_ON_STARTUP=true",
    "-e","FAC_COMMERCIAL_DEMO_EXIT_AFTER_CHECK=true",
    "backend","--server.port=0")

if (-not $SkipBackup) {
    Write-Output "FAC Commercial Demo: backup de referencia"
    & (Join-Path $ProjectRoot "scripts\demo\backup-demo.ps1")
    if ($LASTEXITCODE -ne 0) { Fail "Backup de referencia falhou." }
}

Write-Output "FAC Commercial Demo: a iniciar aplicacao"
Invoke-Compose @("up","-d","backend","frontend")

$port = if ([string]::IsNullOrWhiteSpace($env:FAC_DEMO_HTTP_PORT)) { "8088" } else { $env:FAC_DEMO_HTTP_PORT }
Write-Output "FAC_COMMERCIAL_DEMO_READY url=http://127.0.0.1:$port base=fac_demo perfil=demo"
