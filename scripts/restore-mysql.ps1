# NL2-157 MySQL restore drill (local Docker)
# Usage: .\scripts\restore-mysql.ps1 -BackupFile data\backups\mysql\market-20260609-120000.sql.gz
param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,
    [switch]$Force,
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"
$ComposeFile = Join-Path $ProjectRoot "docker-compose.yml"
$Container = "market-mysql"
$DbUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "market" }
$DbPass = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "market_pass" }
$DbName = "market"
$RootPass = if ($env:MYSQL_ROOT_PASSWORD) { $env:MYSQL_ROOT_PASSWORD } else { "market_root_pass" }

if (-not (Test-Path $BackupFile)) {
    throw "Backup file not found: $BackupFile"
}

$id = docker ps -q -f "name=^/${Container}$"
if (-not $id) {
    throw "Container $Container is not running. Run: docker compose up -d mysql"
}

if (-not $Force) {
    Write-Host "Will DROP and recreate database: $DbName" -ForegroundColor Yellow
    Write-Host "From backup: $BackupFile"
    $confirm = Read-Host "Type RESTORE to continue"
    if ($confirm -ne "RESTORE") {
        Write-Host "Cancelled."
        exit 0
    }
}

$tempSql = Join-Path $env:TEMP ("market-restore-{0}.sql" -f (Get-Date -Format 'yyyyMMddHHmmss'))
$remoteSql = "/tmp/market-restore.sql"

Write-Host "Decompressing..."
if ($BackupFile.EndsWith(".gz")) {
    $in = [System.IO.File]::OpenRead($BackupFile)
    $gzip = New-Object System.IO.Compression.GZipStream($in, [System.IO.Compression.CompressionMode]::Decompress)
    $out = [System.IO.File]::Create($tempSql)
    $gzip.CopyTo($out)
    $out.Close()
    $gzip.Close()
    $in.Close()
} else {
    Copy-Item $BackupFile $tempSql
}

Write-Host "Restoring..."
$initSql = "DROP DATABASE IF EXISTS ``$DbName``; CREATE DATABASE ``$DbName`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker compose -f $ComposeFile exec -T mysql mysql -uroot "-p$RootPass" -e $initSql
if ($LASTEXITCODE -ne 0) { throw "Failed to recreate database" }

docker cp $tempSql "${Container}:${remoteSql}"
docker compose -f $ComposeFile exec -T mysql sh -c "mysql -u$DbUser -p$DbPass $DbName < $remoteSql"
if ($LASTEXITCODE -ne 0) { throw "mysql import failed" }

docker compose -f $ComposeFile exec -T mysql rm -f $remoteSql | Out-Null
Remove-Item $tempSql -Force -ErrorAction SilentlyContinue

Write-Host "Restore OK. Check http://localhost:8082/api/health" -ForegroundColor Green
