$ErrorActionPreference = "Stop"
$Version = "3.9.16"
$ExpectedSha512 = "ed41650d42485cfc243fad22158caf9cbb5dc408ce7a09ddb94dd42a019de929ca43065bfa450612cf12bf78b5cafa3884b96c090de326ff590448c933454af3"
$Root = Split-Path -Parent $PSScriptRoot
$Tools = Join-Path $Root ".tools"
$Zip = Join-Path $Tools "apache-maven-$Version-bin.zip"
$MavenHome = Join-Path $Tools "apache-maven-$Version"
New-Item -ItemType Directory -Force -Path $Tools | Out-Null
if (!(Test-Path $MavenHome)) {
  $Url = "https://dlcdn.apache.org/maven/maven-3/$Version/binaries/apache-maven-$Version-bin.zip"
  Invoke-WebRequest -Uri $Url -OutFile $Zip
  $Actual = (Get-FileHash -Algorithm SHA512 $Zip).Hash.ToLowerInvariant()
  if ($Actual -ne $ExpectedSha512) { throw "Maven checksum mismatch: $Actual" }
  Expand-Archive -Path $Zip -DestinationPath $Tools -Force
}
$env:MAVEN_HOME = $MavenHome
$env:Path = "$MavenHome\bin;$env:Path"
Write-Host "Maven installed for this shell:"
mvn -version
Write-Host "Run: cd backend; mvn clean verify"
