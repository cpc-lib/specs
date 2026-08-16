#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD="$ROOT/build/domain-harness"
rm -rf "$BUILD"
mkdir -p "$BUILD/billing" "$BUILD/framework" "$BUILD/simulator" "$BUILD/payment" "$BUILD/ledger" "$BUILD/harness"

javac --release 21 -d "$BUILD/billing" "$ROOT"/backend/charging-core/src/main/java/com/example/evcharging/core/billing/domain/*.java
javac --release 21 -d "$BUILD/framework" "$ROOT"/backend/charging-framework/src/main/java/com/example/evcharging/framework/contract/DeviceRouteLease.java
javac --release 21 -d "$BUILD/simulator" "$ROOT"/device-simulator/src/main/java/com/example/evcharging/simulator/DeviceSimulator.java
javac --release 21 -d "$BUILD/payment" "$ROOT"/backend/charging-payment/src/main/java/com/example/evcharging/payment/domain/PaymentStatus.java "$ROOT"/backend/charging-payment/src/main/java/com/example/evcharging/payment/domain/PaymentStateMachine.java
javac --release 21 -d "$BUILD/ledger" "$ROOT"/backend/charging-finance/src/main/java/com/example/evcharging/finance/ledger/LedgerPosting.java
cat > "$BUILD/BillingHarness.java" <<'JAVA'
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
    var s2=ZonedDateTime.of(2026,8,10,23,30,0,0,z).toInstant(); var t2=ZonedDateTime.of(2026,8,11,0,30,0,0,z).toInstant();
    var p2=List.of(new PricingPeriod(1,"VALLEY",0,480,350000,100000),new PricingPeriod(2,"FLAT",480,1320,650000,100000),new PricingPeriod(3,"VALLEY",1320,1440,350000,100000));
    var r2=e.calculate(new TimeOfUseBillingContext(z,s2,t2,1000,7000,List.of(),p2));
    eq(r2.energyWh(),6000,"cross-midnight energy"); eq(r2.segments().size(),2,"cross-midnight segments"); eq(r2.energyAmountFen(),210,"cross-midnight energy fee"); eq(r2.serviceAmountFen(),60,"cross-midnight service");

    var observedStart=ZonedDateTime.of(2026,8,10,7,0,0,0,z).toInstant();
    var observedEnd=ZonedDateTime.of(2026,8,10,9,0,0,0,z).toInstant();
    var observed=e.calculate(new TimeOfUseBillingContext(z,observedStart,observedEnd,0,10000,List.of(new MeterPoint(ZonedDateTime.of(2026,8,10,7,45,0,0,z).toInstant(),6000)),p));
    eq(observed.segments().get(0).energyWh(),6800,"observed interpolation seg1");
    eq(observed.segments().get(1).energyWh(),3200,"observed interpolation seg2");
    eq(observed.energyAmountFen(),592,"observed interpolation amount");

    boolean rollback=false; try{e.calculate(new TimeOfUseBillingContext(z,s,t,10000,9000,List.of(),p));}catch(IllegalArgumentException expected){rollback=true;}
    if(!rollback)throw new AssertionError("meter rollback must fail");
    boolean gap=false; try{e.calculate(new TimeOfUseBillingContext(z,s,t,0,1000,List.of(),List.of(new PricingPeriod(1,"A",0,400,1,1),new PricingPeriod(2,"B",500,1440,1,1))));}catch(IllegalArgumentException expected){gap=true;}
    if(!gap)throw new AssertionError("pricing gap must fail");

    var route=DeviceRouteLease.parse(new DeviceRouteLease("gw-01","token-01").encode());
    if(!route.gatewayId().equals("gw-01")||!route.connectionToken().equals("token-01"))throw new AssertionError("device route lease round trip");
    System.out.println("BILLING_GOLDEN_HARNESS=PASS");
    System.out.println("DEVICE_ROUTE_LEASE_HARNESS=PASS");
  }
}
JAVA
javac --release 21 -cp "$BUILD/billing:$BUILD/framework" -d "$BUILD/harness" "$BUILD/BillingHarness.java"
java -cp "$BUILD/harness:$BUILD/billing:$BUILD/framework" BillingHarness

cat > "$BUILD/PaymentLedgerHarness.java" <<'JAVA'
import com.example.evcharging.payment.domain.*;
import com.example.evcharging.finance.ledger.*;
import java.util.*;
public class PaymentLedgerHarness { public static void main(String[] args){ if(!PaymentStateMachine.canSucceed(PaymentStatus.UNKNOWN)||PaymentStateMachine.canSucceed(PaymentStatus.CLOSED)) throw new AssertionError(); new LedgerPosting(List.of(new LedgerPosting.Entry("A",LedgerPosting.Side.DEBIT,100),new LedgerPosting.Entry("B",LedgerPosting.Side.CREDIT,100))); boolean bad=false; try{new LedgerPosting(List.of(new LedgerPosting.Entry("A",LedgerPosting.Side.DEBIT,100),new LedgerPosting.Entry("B",LedgerPosting.Side.CREDIT,99)));}catch(IllegalArgumentException e){bad=true;} if(!bad)throw new AssertionError(); System.out.println("PAYMENT_LEDGER_HARNESS=PASS"); }}
JAVA
javac --release 21 -cp "$BUILD/payment:$BUILD/ledger" -d "$BUILD/harness" "$BUILD/PaymentLedgerHarness.java"
java -cp "$BUILD/harness:$BUILD/payment:$BUILD/ledger" PaymentLedgerHarness
echo "PURE_JAVA_DOMAIN_HARNESS=PASS"
