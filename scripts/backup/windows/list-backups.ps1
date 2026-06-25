[CmdletBinding()]
param([ValidateSet("demo","test","production")][string]$Environment = "demo", [string]$BackupRoot)
. "$PSScriptRoot\common.ps1"
$dir = Get-BackupRoot $Environment $BackupRoot
Get-ChildItem -LiteralPath $dir -Filter "*.backup" | Sort-Object LastWriteTime -Descending | ForEach-Object {
    $meta = [IO.Path]::ChangeExtension($_.FullName, ".metadata.json")
    [pscustomobject]@{ Name=$_.Name; Size=$_.Length; Modified=$_.LastWriteTime; Metadata=(Test-Path $meta) }
}
