[CmdletBinding()]
param([ValidateSet("demo","test","production")][string]$Environment = "demo", [string]$BackupRoot, [switch]$DryRun)
. "$PSScriptRoot\common.ps1"
$dir = (Resolve-Path (Get-BackupRoot $Environment $BackupRoot)).Path
$root = (Resolve-Path (Join-Path (Get-ProjectRoot) "backups")).Path
if (-not $dir.StartsWith($root, [StringComparison]::OrdinalIgnoreCase)) { Fail "Cleanup recusado fora de backups/: $dir" }
$files = Get-ChildItem -LiteralPath $dir -Filter "*.backup" | Sort-Object LastWriteTime -Descending
$calendar = [Globalization.CultureInfo]::InvariantCulture.Calendar
$weekRule = [Globalization.CalendarWeekRule]::FirstFourDayWeek
$firstDay = [DayOfWeek]::Monday
$keepSet = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$files | Group-Object { $_.LastWriteTime.ToString("yyyy-MM-dd") } | Sort-Object Name -Descending | Select-Object -First 7 | ForEach-Object { [void]$keepSet.Add(($_.Group | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName) }
$files | Group-Object { "{0:0000}-W{1:00}" -f $_.LastWriteTime.Year, $calendar.GetWeekOfYear($_.LastWriteTime, $weekRule, $firstDay) } | Sort-Object Name -Descending | Select-Object -First 4 | ForEach-Object { [void]$keepSet.Add(($_.Group | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName) }
$files | Group-Object { $_.LastWriteTime.ToString("yyyy-MM") } | Sort-Object Name -Descending | Select-Object -First 6 | ForEach-Object { [void]$keepSet.Add(($_.Group | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName) }
$delete = $files | Where-Object { -not $keepSet.Contains($_.FullName) -and $_.Name -notmatch '\.protected\.backup$' }
foreach ($file in $delete) {
    $meta = [IO.Path]::ChangeExtension($file.FullName, ".metadata.json")
    if ($DryRun) { Write-Output "DRY-RUN delete $($file.FullName)" }
    else {
        Remove-Item -LiteralPath $file.FullName
        if (Test-Path $meta) { Remove-Item -LiteralPath $meta }
        Write-Output "deleted $($file.FullName)"
    }
}
