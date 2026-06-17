param(
    [string]$InstallRoot = "C:\ssm-shop",
    [string]$RepoZipUrl = "https://github.com/nian-xiu/xiu/archive/refs/heads/main.zip",
    [string]$DbName = "ssm_shop",
    [string]$DbUsername = "root",
    [string]$DbPassword = "123456",
    [string]$AppPasswordSalt = "ssm-shop-prod-salt-change-later",
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

function Assert-Admin {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = [Security.Principal.WindowsPrincipal]::new($identity)
    if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
        throw "Please run PowerShell as Administrator."
    }
}

function Write-Step([string]$Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Test-ZipArchive([string]$Path) {
    if (-not (Test-Path $Path)) {
        return $false
    }
    try {
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        $archive = [System.IO.Compression.ZipFile]::OpenRead($Path)
        $archive.Dispose()
        return $true
    } catch {
        return $false
    }
}

function Download-File([string]$Url, [string]$OutFile) {
    if (Test-Path $OutFile) {
        if ($OutFile.ToLowerInvariant().EndsWith(".zip") -and -not (Test-ZipArchive $OutFile)) {
            Write-Warning "Existing download is not a valid zip, deleting: $OutFile"
            Remove-Item -LiteralPath $OutFile -Force
        } else {
            Write-Host "Already downloaded: $OutFile"
            return
        }
    }
    Write-Host "Downloading: $Url"
    Invoke-WebRequest -Uri $Url -OutFile $OutFile -UseBasicParsing
    if ($OutFile.ToLowerInvariant().EndsWith(".zip") -and -not (Test-ZipArchive $OutFile)) {
        Remove-Item -LiteralPath $OutFile -Force -ErrorAction SilentlyContinue
        throw "Downloaded file is not a valid zip: $Url"
    }
}

function Remove-TreeInsideInstallRoot([string]$Path) {
    $rootFull = [System.IO.Path]::GetFullPath($InstallRoot).TrimEnd('\')
    $pathFull = [System.IO.Path]::GetFullPath($Path).TrimEnd('\')
    if (-not $pathFull.StartsWith($rootFull + "\", [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to delete outside install root: $pathFull"
    }
    if (Test-Path $pathFull) {
        Remove-Item -LiteralPath $pathFull -Recurse -Force
    }
}

function Expand-ZipFresh([string]$ZipFile, [string]$Destination) {
    Remove-TreeInsideInstallRoot $Destination
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Expand-Archive -LiteralPath $ZipFile -DestinationPath $Destination -Force
}

Assert-Admin

$runtimeDir = Join-Path $InstallRoot "runtime"
$downloadDir = Join-Path $InstallRoot "downloads"
$sourceDir = Join-Path $InstallRoot "source"
$appDir = Join-Path $InstallRoot "app"
$logsDir = Join-Path $InstallRoot "logs"
$tmpDir = Join-Path $InstallRoot "tmp"
$mysqlDataDir = Join-Path $InstallRoot "mysql-data"

New-Item -ItemType Directory -Force -Path $runtimeDir, $downloadDir, $sourceDir, $appDir, $logsDir, $tmpDir | Out-Null

Write-Step "Installing JDK 21"
$jdkZip = Join-Path $downloadDir "jdk21.zip"
$jdkExtract = Join-Path $runtimeDir "jdk21-extract"
$jdkHome = Join-Path $runtimeDir "jdk-21"
Download-File "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse" $jdkZip
if (-not (Test-Path (Join-Path $jdkHome "bin\java.exe"))) {
    Expand-ZipFresh $jdkZip $jdkExtract
    $jdkFolder = Get-ChildItem $jdkExtract -Directory | Select-Object -First 1
    if (-not $jdkFolder) { throw "JDK archive did not contain a folder." }
    if (Test-Path $jdkHome) { Remove-Item -LiteralPath $jdkHome -Recurse -Force }
    Move-Item -LiteralPath $jdkFolder.FullName -Destination $jdkHome
}
$javaExe = Join-Path $jdkHome "bin\java.exe"
& $javaExe -version

Write-Step "Installing Maven"
$mavenVersion = "3.9.16"
$mavenZip = Join-Path $downloadDir "apache-maven-$mavenVersion-bin.zip"
$mavenExtract = Join-Path $runtimeDir "maven-extract"
$mavenHome = Join-Path $runtimeDir "apache-maven-$mavenVersion"
Download-File "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip" $mavenZip
if (-not (Test-Path (Join-Path $mavenHome "bin\mvn.cmd"))) {
    Expand-ZipFresh $mavenZip $mavenExtract
    $mavenFolder = Get-ChildItem $mavenExtract -Directory | Select-Object -First 1
    if (-not $mavenFolder) { throw "Maven archive did not contain a folder." }
    if (Test-Path $mavenHome) { Remove-Item -LiteralPath $mavenHome -Recurse -Force }
    Move-Item -LiteralPath $mavenFolder.FullName -Destination $mavenHome
}
$mvnCmd = Join-Path $mavenHome "bin\mvn.cmd"
& $mvnCmd -version

Write-Step "Installing MySQL 8.4"
$mysqlVersion = "8.4.10"
$mysqlZip = Join-Path $downloadDir "mysql-$mysqlVersion-winx64.zip"
$mysqlExtract = Join-Path $runtimeDir "mysql-extract"
$mysqlHome = Join-Path $runtimeDir "mysql-$mysqlVersion-winx64"
$mysqlService = "SsmShopMySQL"
Download-File "https://cdn.mysql.com/Downloads/MySQL-8.4/mysql-$mysqlVersion-winx64.zip" $mysqlZip
if (-not (Test-Path (Join-Path $mysqlHome "bin\mysqld.exe"))) {
    Expand-ZipFresh $mysqlZip $mysqlExtract
    $mysqlFolder = Get-ChildItem $mysqlExtract -Directory | Select-Object -First 1
    if (-not $mysqlFolder) { throw "MySQL archive did not contain a folder." }
    if (Test-Path $mysqlHome) { Remove-Item -LiteralPath $mysqlHome -Recurse -Force }
    Move-Item -LiteralPath $mysqlFolder.FullName -Destination $mysqlHome
}
$mysqlBin = Join-Path $mysqlHome "bin"
$mysqld = Join-Path $mysqlBin "mysqld.exe"
$mysql = Join-Path $mysqlBin "mysql.exe"
$mysqlAdmin = Join-Path $mysqlBin "mysqladmin.exe"
$mysqlIni = Join-Path $InstallRoot "mysql.ini"

if (-not (Test-Path $mysqlIni)) {
    @"
[mysqld]
basedir=$($mysqlHome -replace "\\", "/")
datadir=$($mysqlDataDir -replace "\\", "/")
port=3306
character-set-server=utf8mb4
collation-server=utf8mb4_0900_ai_ci
default-time-zone=+08:00
max_connections=100

[client]
default-character-set=utf8mb4
"@ | Set-Content -LiteralPath $mysqlIni -Encoding ASCII
}

if (-not (Test-Path $mysqlDataDir)) {
    New-Item -ItemType Directory -Force -Path $mysqlDataDir | Out-Null
    & $mysqld --defaults-file="$mysqlIni" --initialize-insecure
}

if (-not (Get-Service -Name $mysqlService -ErrorAction SilentlyContinue)) {
    & $mysqld --install $mysqlService --defaults-file="$mysqlIni"
}

if ((Get-Service -Name $mysqlService).Status -ne "Running") {
    Start-Service $mysqlService
    Start-Sleep -Seconds 8
}

$rootPasswordWorks = $false
try {
    & $mysqlAdmin -u root "-p$DbPassword" ping | Out-Null
    $rootPasswordWorks = $true
} catch {
    $rootPasswordWorks = $false
}

if (-not $rootPasswordWorks) {
    try {
        & $mysqlAdmin -u root password $DbPassword
    } catch {
        Write-Warning "Could not set MySQL root password automatically. If MySQL was previously initialized, make sure the supplied password is correct."
    }
}

Write-Step "Creating database"
$createDbSql = "CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
& $mysql -u $DbUsername "-p$DbPassword" -e $createDbSql

Write-Step "Downloading project source"
$repoZip = Join-Path $downloadDir "xiu-main.zip"
if (Test-Path $repoZip) { Remove-Item -LiteralPath $repoZip -Force }
Download-File $RepoZipUrl $repoZip
Expand-ZipFresh $repoZip $sourceDir
$repoRoot = Get-ChildItem $sourceDir -Directory | Select-Object -First 1
if (-not $repoRoot) { throw "Project archive did not contain a folder." }

Write-Step "Building Spring Boot jar"
Push-Location $repoRoot.FullName
try {
    $env:JAVA_HOME = $jdkHome
    $env:Path = "$jdkHome\bin;$mavenHome\bin;$env:Path"
    & $mvnCmd package -DskipTests
} finally {
    Pop-Location
}

Write-Step "Publishing app files"
Copy-Item -LiteralPath (Join-Path $repoRoot.FullName "target\ssm-shop-1.0.0.jar") -Destination (Join-Path $appDir "ssm-shop-1.0.0.jar") -Force
Copy-Item -LiteralPath (Join-Path $repoRoot.FullName "uploads") -Destination $appDir -Recurse -Force
New-Item -ItemType Directory -Force -Path (Join-Path $appDir "uploads\products") | Out-Null

$runScript = Join-Path $appDir "run-foreground.ps1"
@"
`$ErrorActionPreference = "Stop"
`$env:DB_URL = "jdbc:mysql://localhost:3306/$DbName?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false&rewriteBatchedStatements=true"
`$env:DB_USERNAME = "$DbUsername"
`$env:DB_PASSWORD = "$DbPassword"
`$env:APP_PASSWORD_SALT = "$AppPasswordSalt"
Set-Location "$appDir"
& "$javaExe" "-Djava.io.tmpdir=$tmpDir" -jar "$appDir\ssm-shop-1.0.0.jar" *>> "$logsDir\ssm-shop.log"
"@ | Set-Content -LiteralPath $runScript -Encoding ASCII

$stopScript = Join-Path $appDir "stop.ps1"
@"
`$task = Get-ScheduledTask -TaskName "SsmShopApp" -ErrorAction SilentlyContinue
if (`$task) { Stop-ScheduledTask -TaskName "SsmShopApp" -ErrorAction SilentlyContinue }
Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
    Where-Object { `$_.State -eq "Listen" } |
    ForEach-Object { Stop-Process -Id `$_.OwningProcess -Force -ErrorAction SilentlyContinue }
"@ | Set-Content -LiteralPath $stopScript -Encoding ASCII

$statusScript = Join-Path $appDir "status.ps1"
@"
Get-ScheduledTask -TaskName "SsmShopApp" -ErrorAction SilentlyContinue | Get-ScheduledTaskInfo
Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { `$_.State -eq "Listen" }
try {
    Invoke-WebRequest -Uri "http://localhost:$Port/" -UseBasicParsing -TimeoutSec 10 | Select-Object StatusCode, StatusDescription
} catch {
    Write-Host `$_.Exception.Message
}
"@ | Set-Content -LiteralPath $statusScript -Encoding ASCII

Write-Step "Registering startup task"
$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$runScript`""
$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -RunLevel Highest
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit ([TimeSpan]::Zero) -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)
Register-ScheduledTask -TaskName "SsmShopApp" -Action $action -Trigger $trigger -Principal $principal -Settings $settings -Force | Out-Null

Write-Step "Opening Windows firewall port $Port"
if (-not (Get-NetFirewallRule -DisplayName "SsmShop $Port" -ErrorAction SilentlyContinue)) {
    New-NetFirewallRule -DisplayName "SsmShop $Port" -Direction Inbound -Protocol TCP -LocalPort $Port -Action Allow | Out-Null
}

Write-Step "Starting app"
Stop-ScheduledTask -TaskName "SsmShopApp" -ErrorAction SilentlyContinue
Start-ScheduledTask -TaskName "SsmShopApp"

Write-Step "Waiting for local health check"
$ok = $false
for ($i = 1; $i -le 60; $i++) {
    Start-Sleep -Seconds 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$Port/" -UseBasicParsing -TimeoutSec 5
        Write-Host "Local app is reachable: http://localhost:$Port/ ($($response.StatusCode))" -ForegroundColor Green
        $ok = $true
        break
    } catch {
        Write-Host "Waiting for app... $i/60"
    }
}

if (-not $ok) {
    Write-Warning "App did not become reachable. Check $logsDir\ssm-shop.log"
    Get-Content -LiteralPath (Join-Path $logsDir "ssm-shop.log") -Tail 80 -ErrorAction SilentlyContinue
    exit 1
}

Write-Host ""
Write-Host "Deployment completed." -ForegroundColor Green
Write-Host "Local URL:  http://localhost:$Port/"
Write-Host "Public URL: http://8.163.123.191:$Port/"
Write-Host "Logs:       $logsDir\ssm-shop.log"
Write-Host "Status:     powershell -ExecutionPolicy Bypass -File `"$statusScript`""
Write-Host "Stop:       powershell -ExecutionPolicy Bypass -File `"$stopScript`""
