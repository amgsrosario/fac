[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$Database,
    [Parameter(Mandatory=$true)][ValidateSet("demo","test","production")][string]$Environment,
    [string]$HostName = "localhost",
    [int]$Port = 5432,
    [Parameter(Mandatory=$true)][string]$User,
    [string]$Password = $env:PGPASSWORD,
    [string]$BackupRoot,
    [switch]$UseDockerDemo
)
. "$PSScriptRoot\common.ps1"
Assert-DatabaseName $Database
Assert-Environment $Environment
Require-Value "HostName" $HostName
Require-Value "User" $User

$dir = Get-BackupRoot $Environment $BackupRoot
$base = Get-BackupBaseName $Environment $Database
$backup = Join-Path $dir "$base.backup"
$metadata = Join-Path $dir "$base.metadata.json"
$started = Get-Date

try {
    if ($UseDockerDemo) {
        if ($Environment -ne "demo" -or $Database -ne "fac_demo") { Fail "UseDockerDemo apenas permite demo/fac_demo." }
        Invoke-DockerCompose @("up","-d","db")
        Invoke-DockerCompose @("exec","-T","db","pg_dump","-U",$User,"-d",$Database,"-Fc","-f","/tmp/$base.backup")
        Invoke-DockerCompose @("cp","db:/tmp/$base.backup",$backup)
        $dockerList = Invoke-DockerComposeOutput @("exec","-T","db","pg_restore","--list","/tmp/$base.backup")
        if (-not $dockerList) { Fail "pg_restore --list falhou no container." }
        Invoke-DockerCompose @("exec","-T","db","rm","-f","/tmp/$base.backup")
        $pgVersion = "docker-postgres"
    } else {
        Invoke-Pg -Tool "pg_dump" -Password $Password -Arguments @("-h",$HostName,"-p","$Port","-U",$User,"-d",$Database,"-Fc","-f",$backup)
        $pgVersion = Invoke-PsqlScalar -Password $Password -Arguments @("-h",$HostName,"-p","$Port","-U",$User,"-d",$Database,"-At","-c","show server_version")
    }
    if ((Get-Item -LiteralPath $backup).Length -le 0) { Fail "Backup vazio." }
    $checksum = Get-BackupChecksum $backup
    if ($UseDockerDemo) {
        $objects = $dockerList -join "`n"
    } else {
        Invoke-Pg -Tool "pg_restore" -Arguments @("--list",$backup)
        $objects = (& pg_restore --list $backup) -join "`n"
    }
    foreach ($object in @("TABLE public cliente","TABLE public artigo","TABLE public documento_comercial","TABLE public utilizador","TABLE public auditoria_evento","TABLE public flyway_schema_history","TABLE public empresa")) {
        if ($objects -notmatch [regex]::Escape($object)) { Fail "Objeto essencial ausente no backup: $object" }
    }
    $meta = [ordered]@{
        project = "fac"; environment = $Environment; database = $Database
        createdAt = $started.ToString("o"); finishedAt = (Get-Date).ToString("o")
        postgresVersion = $pgVersion; applicationVersion = "0.0.1-SNAPSHOT"; gitCommit = Get-GitCommit
        flywayVersion = "schema_history"; backupFile = (Split-Path $backup -Leaf)
        backupSizeBytes = (Get-Item -LiteralPath $backup).Length; sha256 = $checksum
        executedBy = [Environment]::UserName; validationResult = "OK"; encrypted = $false
        controlObjectCount = (($objects -split "`n") | Where-Object { $_ -match "TABLE public|SCHEMA|CONSTRAINT" }).Count
        notes = "PostgreSQL custom format (-Fc). Passwords omitted."
    }
    $meta | ConvertTo-Json -Depth 4 | Set-Content -Path $metadata -Encoding UTF8
    Write-Audit "backup" $Environment $Database "OK" "$backup $checksum"
    Write-Output "FAC_BACKUP_OK $backup $checksum"
} catch {
    Write-Audit "backup" $Environment $Database "FAIL" $_.Exception.Message
    throw
}
