$ErrorActionPreference = "Stop"
$Headers = @{ "X-Tenant-Id" = "1"; "Content-Type" = "application/json" }
function Wait-Http($Name, $Url) {
  for ($i=0; $i -lt 60; $i++) {
    try { $r = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 3; Write-Host "[PASS] $Name $($r.StatusCode)"; return }
    catch { Start-Sleep -Seconds 2 }
  }
  throw "Timeout waiting for $Name: $Url"
}
Wait-Http "Nacos console" "http://127.0.0.1:18080/"
Wait-Http "Gateway" "http://127.0.0.1:8080/actuator/health"
Wait-Http "Asset" "http://127.0.0.1:8082/actuator/health"
Wait-Http "IoT" "http://127.0.0.1:8087/actuator/health"

$Code = "SMOKE-" + [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$Body = @{ operatorId=1; stationCode=$Code; stationName="Smoke Station" } | ConvertTo-Json
$Created = Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/admin-api/v1/assets/stations" -Headers $Headers -Body $Body
if ($Created.code -ne 0) { throw "Create station failed" }
$List = Invoke-RestMethod -Method Get -Uri "http://127.0.0.1:8080/admin-api/v1/assets/stations" -Headers $Headers
if (-not ($List.data | Where-Object { $_.stationCode -eq $Code })) { throw "Created station not visible" }
Write-Host "[PASS] Station vertical slice"
