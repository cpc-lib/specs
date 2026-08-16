$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $Root "deploy/docker")
try {
  docker compose up -d
  docker compose ps
} finally { Pop-Location }
