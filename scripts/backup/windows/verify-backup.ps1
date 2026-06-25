[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$BackupFile,
    [switch]$UseDockerDemo
)
. "$PSScriptRoot\common.ps1"
if (-not (Test-Path -LiteralPath $BackupFile)) { Fail "Ficheiro inexistente: $BackupFile" }
if ((Get-Item -LiteralPath $BackupFile).Length -le 0) { Fail "Ficheiro vazio: $BackupFile" }
$meta = Read-Metadata $BackupFile
$checksum = Get-BackupChecksum $BackupFile
if ($checksum -ne $meta.sha256) { Fail "Checksum invalido: esperado $($meta.sha256), obtido $checksum" }
if ($UseDockerDemo) {
    Invoke-DockerCompose @("up","-d","db")
    Invoke-DockerCompose @("cp",$BackupFile,"db:/tmp/verify.backup")
    $list = Invoke-DockerComposeOutput @("exec","-T","db","pg_restore","--list","/tmp/verify.backup")
    if (-not $list) { Fail "pg_restore --list nao conseguiu ler o backup no container." }
    Invoke-DockerCompose @("exec","-T","db","rm","-f","/tmp/verify.backup")
} else {
    $list = (& pg_restore --list $BackupFile)
    if ($LASTEXITCODE -ne 0 -or -not $list) { Fail "pg_restore --list nao conseguiu ler o backup." }
}
$text = $list -join "`n"
foreach ($object in @("cliente","artigo","documento_comercial","linha_documento_comercial","documento_financeiro","utilizador","auditoria_evento","mpagamento","serie","empresa","importacao_dados_mestres","flyway_schema_history")) {
    if ($text -notmatch "TABLE public $object") { Fail "Objeto essencial ausente: $object" }
}
Write-Output "FAC_BACKUP_VERIFY_OK $BackupFile $checksum"
