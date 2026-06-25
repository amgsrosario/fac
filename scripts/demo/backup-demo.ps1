[CmdletBinding()]
param([string]$User = $(if ($env:FAC_DEMO_DB_USER) { $env:FAC_DEMO_DB_USER } else { "fac_demo_user" }))
$ErrorActionPreference = "Stop"
$envFile = Join-Path (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path ".env.demo"
if (Test-Path $envFile) {
    Get-Content $envFile | Where-Object { $_ -match '^\s*[^#][^=]+=' } | ForEach-Object {
        $parts = $_ -split '=', 2
        [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
    }
}
& "$PSScriptRoot\..\backup\windows\backup-database.ps1" -Database "fac_demo" -Environment "demo" -User $User -UseDockerDemo
