$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
python scripts/validate_static.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
python scripts/check_jdbc_placeholders.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& .\scripts\domain_harness.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& .\scripts\finance_harness.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& .\scripts\operation_harness.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& .\scripts\product_harness.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& .\mvnw.cmd -B -ntp clean verify
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Push-Location admin-web
npm install
npm run build
Pop-Location
Push-Location merchant-web
npm install
npm run build
Pop-Location
New-Item -ItemType Directory -Path build\simulator -Force | Out-Null
javac --release 21 -d build\simulator device-simulator\src\main\java\com\example\evcharging\simulator\DeviceSimulator.java
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "VERIFY=PASS"

& .\scripts\openapi_harness.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
