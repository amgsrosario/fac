[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$BackupFile,
    [Parameter(Mandatory=$true)][string]$TargetDatabase,
    [Parameter(Mandatory=$true)][ValidateSet("demo","test","production")][string]$Environment,
    [string]$HostName = "localhost",
    [int]$Port = 5432,
    [Parameter(Mandatory=$true)][string]$User,
    [string]$Password = $env:PGPASSWORD,
    [string]$ConfirmDatabase,
    [switch]$DropAfterValidation,
    [switch]$NonInteractive,
    [switch]$UseDockerDemo
)
. "$PSScriptRoot\common.ps1"
Assert-RestoreTarget $TargetDatabase
Assert-Environment $Environment
Confirm-Strong $TargetDatabase $ConfirmDatabase -NonInteractive:$NonInteractive
& "$PSScriptRoot\verify-backup.ps1" -BackupFile $BackupFile -UseDockerDemo:$UseDockerDemo

$started = Get-Date
try {
    if ($UseDockerDemo) {
        Invoke-DockerCompose @("up","-d","db")
        Invoke-DockerCompose @("cp",$BackupFile,"db:/tmp/restore.backup")
        Invoke-DockerCompose @("exec","-T","db","dropdb","-U",$User,"--maintenance-db=postgres","--if-exists","--force",$TargetDatabase)
        Invoke-DockerCompose @("exec","-T","db","createdb","-U",$User,$TargetDatabase)
        Invoke-DockerCompose @("exec","-T","db","pg_restore","-U",$User,"-d",$TargetDatabase,"--clean","--if-exists","--no-owner","--exit-on-error","/tmp/restore.backup")
    } else {
        Invoke-Pg -Tool "dropdb" -Password $Password -Arguments @("-h",$HostName,"-p","$Port","-U",$User,"--if-exists","--force",$TargetDatabase)
        Invoke-Pg -Tool "createdb" -Password $Password -Arguments @("-h",$HostName,"-p","$Port","-U",$User,$TargetDatabase)
        Invoke-Pg -Tool "pg_restore" -Password $Password -Arguments @("-h",$HostName,"-p","$Port","-U",$User,"-d",$TargetDatabase,"--clean","--if-exists","--no-owner","--exit-on-error",$BackupFile)
    }
    $checks = @(
        "select count(*) from flyway_schema_history where success",
        "select count(*) from empresa where id=1",
        "select count(*) from cliente",
        "select count(*) from artigo",
        "select count(*) from documento_comercial",
        "select count(*) from linha_documento_comercial",
        "select count(*) from documento_financeiro",
        "select count(*) from auditoria_evento",
        "select count(*) from serie",
        "select count(*) from importacao_dados_mestres"
    )
    foreach ($sql in $checks) {
        if ($UseDockerDemo) {
            $value = Invoke-DockerComposeOutput @("exec","-T","db","psql","-v","ON_ERROR_STOP=1","-U",$User,"-d",$TargetDatabase,"-At","-c",$sql)
        } else {
            $value = Invoke-PsqlScalar -Password $Password -Arguments @("-h",$HostName,"-p",$Port,"-U",$User,"-d",$TargetDatabase,"-At","-c",$sql)
        }
        if ($LASTEXITCODE -ne 0 -or [int64]$value -lt 0) { Fail "Check pos-restauro falhou: $sql" }
    }
    if ($UseDockerDemo) {
        $fkProblems = Invoke-DockerComposeOutput @("exec","-T","db","psql","-v","ON_ERROR_STOP=1","-U",$User,"-d",$TargetDatabase,"-At","-c","select count(*) from pg_constraint where contype='f' and not convalidated")
    } else {
        $fkProblems = Invoke-PsqlScalar -Password $Password -Arguments @("-h",$HostName,"-p",$Port,"-U",$User,"-d",$TargetDatabase,"-At","-c","select count(*) from pg_constraint where contype='f' and not convalidated")
    }
    if ([int64]$fkProblems -ne 0) { Fail "Existem foreign keys nao validadas." }
    Write-Audit "restore" $Environment $TargetDatabase "OK" "backup=$BackupFile duration=$((Get-Date)-$started)"
    Write-Output "FAC_BACKUP_RESTORE_OK origem=$BackupFile destino=$TargetDatabase"
    if ($DropAfterValidation) {
        if ($UseDockerDemo) { Invoke-DockerCompose @("exec","-T","db","dropdb","-U",$User,"--maintenance-db=postgres","--if-exists","--force",$TargetDatabase) }
        else { Invoke-Pg -Tool "dropdb" -Password $Password -Arguments @("-h",$HostName,"-p","$Port","-U",$User,"--if-exists","--force",$TargetDatabase) }
        Write-Output "FAC_RESTORE_TEST_DROPPED $TargetDatabase"
    }
} catch {
    Write-Audit "restore" $Environment $TargetDatabase "FAIL" $_.Exception.Message
    throw
}
