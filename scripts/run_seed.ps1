# Usage:
# .\run_seed.ps1 -Clients 500 -Products 1000 -Batch 500 -Force
# or set env vars: $env:MINIERP_SEED_CLIENTS=500; $env:MINIERP_SEED_PRODUCTS=1000; .\run_seed.ps1
param(
    [int]$Clients = 1000,
    [int]$Products = 1000,
    [int]$Batch = 500,
    [switch]$Force
)

# Start transcript to capture all output to a log file
$logFile = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "seed_run_verbose.log"
try { Start-Transcript -Path $logFile -Append -ErrorAction Stop } catch { Write-Host "[diagnostic] Could not start transcript: $_" }

# Ensure project's classes are built
Write-Host "Building project (mvn -DskipTests package) ..."
cd "$(Split-Path -Parent $MyInvocation.MyCommand.Path)\.." | Out-Null

# Diagnostics: show PATH and java/mvn versions so users can see why nothing printed earlier
Write-Host "[diagnostic] Current directory: " (Get-Location)
Write-Host "[diagnostic] PATH=" $env:PATH
Write-Host "[diagnostic] Checking java and maven availability..."
try {
    & java -version 2>&1 | ForEach-Object { Write-Host "[java] " $_ }
} catch {
    Write-Host "[diagnostic] java not found or failed to run: $_";
}
try {
    & mvn -v 2>&1 | ForEach-Object { Write-Host "[mvn] " $_ }
} catch {
    Write-Host "[diagnostic] mvn not found or failed to run: $_";
}

$mvStatus = $LASTEXITCODE

# run maven, capture success
Write-Host "Running: mvn -DskipTests package"
$mvnOk = $true
try {
    mvn -DskipTests package 2>&1 | ForEach-Object { Write-Host "[mvn-build] " $_ }
} catch {
    Write-Host "[diagnostic] mvn build failed: $_"
    $mvnOk = $false
}

if (-not $mvnOk) {
    Write-Host "Maven build failed — aborting seed run."; Stop-Transcript -ErrorAction SilentlyContinue; exit 1
}

# prepare JVM args
$db = "./.minierp/minierp.db"
$cp = "target/classes;target/dependency/*"
$forceFlag = if ($Force) { "--force" } else { "" }

# Prefer env vars if set
if ($env:MINIERP_SEED_CLIENTS) { $Clients = [int]$env:MINIERP_SEED_CLIENTS }
if ($env:MINIERP_SEED_PRODUCTS) { $Products = [int]$env:MINIERP_SEED_PRODUCTS }
if ($env:MINIERP_SEED_BATCH) { $Batch = [int]$env:MINIERP_SEED_BATCH }
if ($env:MINIERP_SEED_FORCE) { $forceFlag = "--force" }

Write-Host "Running seed with clients=$Clients products=$Products batch=$Batch force=$Force"

$cmd = "java -Dminierp.db=\"$db\" -cp $cp com.example.ERP.tools.SeedData $Clients $Products --batch=$Batch $forceFlag"
Write-Host "Executing: $cmd"

# Execute and stream output
try {
    # Build argument array to avoid PowerShell splitting on ';' inside classpath
    $javaArgs = @("-Dminierp.db=$db", "-cp", "$cp", "com.example.ERP.tools.SeedData", "$Clients", "$Products", "--batch=$Batch")
    if ($forceFlag -eq "--force") { $javaArgs += "--force" }
    Write-Host "Invoking java with args: $($javaArgs -join ' ')"
    & java @javaArgs 2>&1 | ForEach-Object { Write-Host "[seed] " $_ }
} catch {
    Write-Host "[diagnostic] Seed process failed: $_"; Stop-Transcript -ErrorAction SilentlyContinue; exit 1
}

Write-Host "Seed complete."

# Stop transcript
try { Stop-Transcript -ErrorAction SilentlyContinue } catch { }
