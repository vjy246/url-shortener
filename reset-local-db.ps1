param(
    [string]$DataDir = ".\data",
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$resolvedDataDir = Resolve-Path -Path $DataDir -ErrorAction SilentlyContinue
if (-not $resolvedDataDir) {
    Write-Host "Data directory not found: $DataDir"
    exit 0
}

$dbFiles = Get-ChildItem -Path $resolvedDataDir -File -Filter "urlshortenerdb*.db" -ErrorAction SilentlyContinue
if (-not $dbFiles) {
    Write-Host "No urlshortenerdb*.db files found under $resolvedDataDir"
    exit 0
}

if (-not $Force) {
    Write-Host "About to reset local H2 files:"
    $dbFiles | ForEach-Object { Write-Host " - $($_.FullName)" }
    $confirm = Read-Host "Type YES to continue"
    if ($confirm -ne "YES") {
        Write-Host "Aborted."
        exit 1
    }
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupDir = Join-Path -Path $resolvedDataDir -ChildPath "backup-$timestamp"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

foreach ($file in $dbFiles) {
    Move-Item -Path $file.FullName -Destination $backupDir
}

Write-Host "Moved $($dbFiles.Count) database file(s) to: $backupDir"
Write-Host "You can now restart the application to recreate a fresh local database."

