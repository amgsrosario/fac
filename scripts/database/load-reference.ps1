[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Database,
    [string]$Container = 'facdb',
    [string]$DbUser = 'postgres'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$files = @(
    @{ Source = (Join-Path $root 'data\reference\codigos_postais_portugal.csv'); Target = '/tmp/tuuli-codigos-postais.csv' },
    @{ Source = (Join-Path $root 'data\reference\freguesias_portugal.csv'); Target = '/tmp/tuuli-freguesias.csv' },
    @{ Source = (Join-Path $PSScriptRoot 'load-reference.sql'); Target = '/tmp/tuuli-load-reference.sql' }
)

foreach ($file in $files) {
    if (-not (Test-Path -LiteralPath $file.Source)) { throw "Ficheiro ausente: $($file.Source)" }
    & docker cp $file.Source "${Container}:$($file.Target)"
    if ($LASTEXITCODE -ne 0) { throw "Falha ao copiar $($file.Source) para $Container" }
}

& docker exec $Container psql -X -v ON_ERROR_STOP=1 -U $DbUser -d $Database -f '/tmp/tuuli-load-reference.sql'
if ($LASTEXITCODE -ne 0) { throw "Carga de referência falhou na base $Database" }
