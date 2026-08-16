$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Build = Join-Path $Root "build\domain-harness"
if (Test-Path $Build) { Remove-Item $Build -Recurse -Force }
New-Item -ItemType Directory -Path "$Build\billing","$Build\framework","$Build\simulator","$Build\harness" -Force | Out-Null

$BillingSources = Get-ChildItem "$Root\backend\charging-core\src\main\java\com\example\evcharging\core\billing\domain\*.java" | ForEach-Object { $_.FullName }
& javac --release 21 -d "$Build\billing" $BillingSources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& javac --release 21 -d "$Build\framework" "$Root\backend\charging-framework\src\main\java\com\example\evcharging\framework\contract\DeviceRouteLease.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& javac --release 21 -d "$Build\simulator" "$Root\device-simulator\src\main\java\com\example\evcharging\simulator\DeviceSimulator.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$Harness = @'
import com.example.evcharging.core.billing.domain.*;
import com.example.evcharging.framework.contract.DeviceRouteLease;
import java.time.*;
import java.util.*;
public class BillingHarness {
  static void eq(long a,long b,String m){if(a!=b)throw new AssertionError(m+": "+a+" != "+b);}
  public static void main(String[] args){
    var e=new TimeOfUseBillingEngine(); var z=ZoneId.of("Asia/Shanghai");
    var s=ZonedDateTime.of(2026,8,10,7,30,0,0,z).toInstant(); var t=ZonedDateTime.of(2026,8,10,8,30,0,0,z).toInstant();
    var p=List.of(new PricingPeriod(1,"VALLEY",0,480,400000,200000),new PricingPeriod(2,"PEAK",480,1440,1000000,200000));
    var r=e.calculate(new TimeOfUseBillingContext(z,s,t,0,10000,List.of(),p));
    eq(r.energyWh(),10000,"energy"); eq(r.segments().size(),2,"segments"); eq(r.segments().get(0).energyWh(),5000,"seg1"); eq(r.segments().get(1).energyWh(),5000,"seg2"); eq(r.energyAmountFen(),700,"energy fee"); eq(r.serviceAmountFen(),200,"service fee"); eq(r.receivableAmountFen(),900,"total");
    var route=DeviceRouteLease.parse(new DeviceRouteLease("gw-01","token-01").encode());
    if(!route.gatewayId().equals("gw-01"))throw new AssertionError("route parse");
    System.out.println("BILLING_GOLDEN_HARNESS=PASS");
    System.out.println("DEVICE_ROUTE_LEASE_HARNESS=PASS");
  }
}
'@
$HarnessPath = Join-Path $Build "BillingHarness.java"
Set-Content -Path $HarnessPath -Value $Harness -Encoding UTF8
& javac --release 21 -cp "$Build\billing;$Build\framework" -d "$Build\harness" $HarnessPath
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java -cp "$Build\harness;$Build\billing;$Build\framework" BillingHarness
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "PURE_JAVA_DOMAIN_HARNESS=PASS"
