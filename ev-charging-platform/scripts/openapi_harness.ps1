$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Out = Join-Path $Root ".openapi-harness"
if (Test-Path $Out) { Remove-Item $Out -Recurse -Force }
New-Item -ItemType Directory -Path $Out -Force | Out-Null

javac --release 21 -d $Out `
 "$Root\backend\charging-open\src\main\java\com\example\evcharging\open\security\OpenApiSignature.java" `
 "$Root\backend\charging-open\src\main\java\com\example\evcharging\open\callback\PartnerCallbackSigner.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$Code=@'
package com.example.evcharging.open.security;
import com.example.evcharging.open.callback.PartnerCallbackSigner;
public class OpenApiHarness {
 public static void main(String[] args){
  String c=OpenApiSignature.canonical("GET","/open-api/v1/stations","z=2&a=hello%20world&a=alpha",new byte[0],"1700000000","n1");
  if(!c.contains("a=alpha&a=hello%20world&z=2"))throw new AssertionError();
  String s=OpenApiSignature.signHex("secret",c);
  if(!OpenApiSignature.constantTimeEquals(s,s.toUpperCase()))throw new AssertionError();
  if(PartnerCallbackSigner.sign("secret",new byte[0],1700000000L).signature().length()!=64)throw new AssertionError();
  System.out.println("OPENAPI_SECURITY_HARNESS=PASS");
 }
}
'@
$Code | Set-Content -Encoding UTF8 (Join-Path $Out "OpenApiHarness.java")
javac --release 21 -cp $Out -d $Out (Join-Path $Out "OpenApiHarness.java")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp $Out com.example.evcharging.open.security.OpenApiHarness
Remove-Item $Out -Recurse -Force
