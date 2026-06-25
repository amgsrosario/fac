[CmdletBinding()]
param([Parameter(Mandatory=$true)][string]$BackupFile, [string]$TargetDatabase = "fac_restore_test", [string]$User = $(if ($env:FAC_DEMO_DB_USER) { $env:FAC_DEMO_DB_USER } else { "fac_demo_user" }), [switch]$DropAfterValidation)
$ErrorActionPreference = "Stop"
& "$PSScriptRoot\..\backup\windows\restore-database.ps1" -BackupFile $BackupFile -TargetDatabase $TargetDatabase -Environment "demo" -User $User -ConfirmDatabase $TargetDatabase -NonInteractive -DropAfterValidation:$DropAfterValidation -UseDockerDemo
