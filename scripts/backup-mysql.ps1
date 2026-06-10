# NL2-157 MySQL logical backup (local Docker)
# Usage: .\scripts\backup-mysql.ps1 [-RetentionDays 7]
param(
    [int]$RetentionDays = 7,
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"
$ComposeFile = Join-Path $ProjectRoot "docker-compose.yml"
$BackupDir = Join-Path $ProjectRoot "data\backups\mysql"
$Container = "market-mysql"
$DbUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "market" }
$DbPass = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "market_pass" }
$DbName = "market"

$id = docker ps -q -f "name=^/${Container}$"
if (-not $id) {
    throw "Container $Container is not running. Run: docker compose up -d mysql"
}

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null

$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$gzFile = Join-Path $BackupDir "market-$stamp.sql.gz"
$remoteSql = "/tmp/market-$stamp.sql"

$dumpCmd = @(
    "mysqldump -u$DbUser -p$DbPass",
    "--single-transaction --no-tablespaces",
    "--routines --triggers --set-gtid-purged=OFF",
    "$DbName > $remoteSql"
) -join " "

Write-Host "Backing up $DbName -> $gzFile"
docker compose -f $ComposeFile exec -T mysql sh -c $dumpCmd
if ($LASTEXITCODE -ne 0) { throw "mysqldump failed" }

docker cp "${Container}:${remoteSql}" $gzFile.Replace(".sql.gz", ".sql")
docker compose -f $ComposeFile exec -T mysql rm -f $remoteSql | Out-Null

$sqlFile = $gzFile.Replace(".sql.gz", ".sql")
if (-not (Test-Path $sqlFile) -or (Get-Item $sqlFile).Length -lt 100) {
    throw "Backup SQL file missing or too small"
}

$bytes = [System.IO.File]::ReadAllBytes($sqlFile)
$ms = New-Object System.IO.MemoryStream
$gzip = New-Object System.IO.Compression.GZipStream($ms, [System.IO.Compression.CompressionMode]::Compress)
$gzip.Write($bytes, 0, $bytes.Length)
$gzip.Close()
[System.IO.File]::WriteAllBytes($gzFile, $ms.ToArray())
Remove-Item $sqlFile -Force

$size = (Get-Item $gzFile).Length
Write-Host ('Backup OK: {0} ({1:N0} bytes)' -f $gzFile, $size) -ForegroundColor Green

if ($RetentionDays -gt 0) {
    $cutoff = (Get-Date).AddDays(-$RetentionDays)
    Get-ChildItem -Path $BackupDir -Filter 'market-*.sql.gz' |
        Where-Object { $_.LastWriteTime -lt $cutoff } |
        ForEach-Object {
            Write-Host ('Prune: {0}' -f $_.Name)
            Remove-Item $_.FullName -Force
        }
}

Write-Host ('Restore: .\scripts\restore-mysql.ps1 -BackupFile {0}' -f $gzFile)
