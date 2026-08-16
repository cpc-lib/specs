param(
  [Parameter(Mandatory=$true)]
  [ValidateSet("mysql-outage","redis-outage","kafka-outage","rabbitmq-outage","nacos-outage")]
  [string]$Scenario,
  [int]$DurationSeconds=30
)
$ErrorActionPreference="Stop"
$Root=Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$Compose=Join-Path $Root "deploy\docker\docker-compose.yml"
$Service=@{
 "mysql-outage"="mysql";"redis-outage"="redis";"kafka-outage"="kafka";
 "rabbitmq-outage"="rabbitmq";"nacos-outage"="nacos"
}[$Scenario]
try{
  docker compose -f $Compose stop $Service
  Start-Sleep -Seconds $DurationSeconds
}finally{
  docker compose -f $Compose start $Service
}
Write-Host "CHAOS_RECOVERED scenario=$Scenario"
