$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$java = "D:\soft\OracleJdk\bin\java.exe"
$jar = Join-Path $projectRoot "target\ssm-shop-1.0.0.jar"
$tmp = Join-Path $projectRoot "tmp"
$out = Join-Path $projectRoot "app.log"
$err = Join-Path $projectRoot "app.err.log"
$url = "http://localhost:8080/"

if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) {
    $env:DB_USERNAME = "root"
}

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $env:DB_PASSWORD = "123456"
}

if (!(Test-Path $java)) {
    throw "Java not found: $java"
}

if (!(Test-Path $jar)) {
    throw "Jar not found. Build first with: mvn.cmd package -DskipTests"
}

New-Item -ItemType Directory -Force -Path $tmp | Out-Null

$existing = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
    Where-Object { $_.State -eq "Listen" } |
    Select-Object -First 1

if ($existing) {
    Write-Host "Port 8080 is already listening. PID: $($existing.OwningProcess)"
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        Write-Host "Application already reachable: $url ($($response.StatusCode))"
        exit 0
    } catch {
        Write-Host "Port is occupied but the app did not respond. Stop PID $($existing.OwningProcess) if this is stale."
        exit 1
    }
}

$processInfo = [System.Diagnostics.ProcessStartInfo]::new()
$processInfo.FileName = $java
$processInfo.WorkingDirectory = $projectRoot
$processInfo.Arguments = "`"-Djava.io.tmpdir=$tmp`" -jar `"$jar`""
$processInfo.UseShellExecute = $true
$processInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden

$process = [System.Diagnostics.Process]::Start($processInfo)
Write-Host "Started Java PID $($process.Id)"

for ($i = 1; $i -le 20; $i++) {
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
        Write-Host "Application reachable: $url ($($response.StatusCode))"
        Write-Host "Logs: $out"
        exit 0
    } catch {
        if ($process.HasExited) {
            Write-Host "Application exited early. Check logs:"
            Write-Host $out
            Write-Host $err
            exit 1
        }
    }
}

Write-Host "Application did not respond within 20 seconds. Check logs:"
Write-Host $out
Write-Host $err
exit 1
