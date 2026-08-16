$ErrorActionPreference = "Stop"
$Version = "3.9.16"
$ExpectedSha512 = "ed41650d42485cfc243fad22158caf9cbb5dc408ce7a09ddb94dd42a019de929ca43065bfa450612cf12bf78b5cafa3884b96c090de326ff590448c933454af3"
$Root = Split-Path -Parent $PSScriptRoot
$Tools = Join-Path $Root ".tools"
$MavenHome = Join-Path $Tools "apache-maven-$Version"
$Zip = Join-Path $Tools "apache-maven-$Version-bin.zip"
$Mvn = Join-Path $MavenHome "bin\mvn.cmd"
if (-not (Test-Path $Mvn)) {
  New-Item -ItemType Directory -Path $Tools -Force | Out-Null
  $Url = "https://downloads.apache.org/maven/maven-3/$Version/binaries/apache-maven-$Version-bin.zip"
  Invoke-WebRequest -Uri $Url -OutFile $Zip
  $Actual = (Get-FileHash -Algorithm SHA512 $Zip).Hash.ToLowerInvariant()
  if ($Actual -ne $ExpectedSha512) { throw "Maven SHA-512 mismatch" }
  Expand-Archive -Path $Zip -DestinationPath $Tools -Force
}
& $Mvn @args
exit $LASTEXITCODE
