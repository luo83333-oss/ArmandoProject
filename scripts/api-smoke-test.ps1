# Epic 4.1 API 冒烟脚本 — 要求后端已启动在 8082
$Base = "http://localhost:8082"
$Failed = 0

function Test-Api {
    param([string]$Name, [string]$Url, [int]$ExpectCode = 0, [hashtable]$Headers = @{})
    try {
        $params = @{ Uri = $Url; UseBasicParsing = $true; TimeoutSec = 8 }
        if ($Headers.Count -gt 0) { $params.Headers = $Headers }
        $r = Invoke-WebRequest @params
        $json = $r.Content | ConvertFrom-Json
        if ($json.code -ne $ExpectCode) {
            Write-Host "[FAIL] $Name — expected code $ExpectCode got $($json.code)" -ForegroundColor Red
            $script:Failed++
        } else {
            Write-Host "[OK]   $Name" -ForegroundColor Green
        }
    } catch {
        Write-Host "[FAIL] $Name — $($_.Exception.Message)" -ForegroundColor Red
        $script:Failed++
    }
}

Write-Host "API smoke test -> $Base"
Test-Api "health" "$Base/api/health"
Test-Api "products public" "$Base/api/products"
Test-Api "shops rank public" "$Base/api/shops/rank"
Test-Api "favorites needs auth" "$Base/api/favorites" -ExpectCode 401
Test-Api "sql injection keyword" "$Base/api/products?keyword=' OR 1=1--"

if ($Failed -gt 0) {
    Write-Host "`n$Failed check(s) failed." -ForegroundColor Red
    exit 1
}
Write-Host "`nAll smoke checks passed." -ForegroundColor Green
exit 0
