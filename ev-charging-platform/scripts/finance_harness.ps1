$ErrorActionPreference = 'Stop'
$Root = (Resolve-Path "$PSScriptRoot\..").Path
$Out = Join-Path $Root '.finance-harness'
Remove-Item $Out -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $Out | Out-Null
javac --release 21 -d $Out `
  "$Root\backend\charging-finance\src\main\java\com\example\evcharging\finance\reconciliation\ReconciliationResultType.java" `
  "$Root\backend\charging-finance\src\main\java\com\example\evcharging\finance\reconciliation\ReconciliationMatcher.java" `
  "$Root\backend\charging-finance\src\main\java\com\example\evcharging\finance\settlement\SettlementCalculator.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host 'FINANCE_CORE_COMPILE=PASS'
Remove-Item $Out -Recurse -Force
