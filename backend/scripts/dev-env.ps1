# 在 PowerShell 中执行: . .\scripts\dev-env.ps1
# 设置 JAVA_HOME（Microsoft OpenJDK 17 默认路径）
$jdk = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
if (Test-Path $jdk) {
    $env:JAVA_HOME = $jdk
    $env:PATH = "$jdk\bin;$env:PATH"
    Write-Host "JAVA_HOME = $env:JAVA_HOME"
    java -version
} else {
    Write-Warning "未找到 JDK，请修改 scripts/dev-env.ps1 中的路径"
}
