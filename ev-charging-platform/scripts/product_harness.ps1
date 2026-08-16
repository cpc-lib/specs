$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Out = Join-Path $Root ".product-harness"
if (Test-Path $Out) { Remove-Item $Out -Recurse -Force }
New-Item -ItemType Directory -Path $Out -Force | Out-Null
javac --release 21 -d $Out `
 "$Root\backend\charging-framework\src\main\java\com\example\evcharging\framework\security\DataScopeType.java" `
 "$Root\backend\charging-framework\src\main\java\com\example\evcharging\framework\security\AccessPrincipal.java" `
 "$Root\backend\charging-system\src\main\java\com\example\evcharging\system\auth\PasswordHasher.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$Code=@'
import com.example.evcharging.framework.security.*;
import com.example.evcharging.system.auth.*;
import java.util.*;
public class ProductHarness {
 public static void main(String[] args){
  String hash=PasswordHasher.hash("secret123".toCharArray());
  if(!PasswordHasher.verify("secret123".toCharArray(),hash)||PasswordHasher.verify("wrong".toCharArray(),hash))throw new AssertionError();
  var p=new AccessPrincipal(1,2,"m",Set.of("MERCHANT_STATION"),Set.of(),DataScopeType.STATION,Set.of(99L));
  if(!p.mayAccessStation(99)||p.mayAccessStation(100))throw new AssertionError();
  System.out.println("PRODUCT_MVP_HARNESS=PASS");
 }
}
'@
$Code | Set-Content -Encoding UTF8 (Join-Path $Out "ProductHarness.java")
javac --release 21 -cp $Out -d $Out (Join-Path $Out "ProductHarness.java")
java -cp $Out ProductHarness
Remove-Item $Out -Recurse -Force
