#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/.finance-harness"
rm -rf "$OUT"; mkdir -p "$OUT"
javac --release 21 -d "$OUT" \
  "$ROOT/backend/charging-finance/src/main/java/com/example/evcharging/finance/reconciliation/ReconciliationResultType.java" \
  "$ROOT/backend/charging-finance/src/main/java/com/example/evcharging/finance/reconciliation/ReconciliationMatcher.java" \
  "$ROOT/backend/charging-finance/src/main/java/com/example/evcharging/finance/settlement/SettlementCalculator.java" \
  "$ROOT/backend/charging-finance/src/main/java/com/example/evcharging/finance/adjustment/AdjustmentMath.java" \
  "$ROOT/backend/charging-finance/src/main/java/com/example/evcharging/finance/ledger/LedgerPosting.java"
cat > "$OUT/FinanceHarness.java" <<'JAVA'
import com.example.evcharging.finance.reconciliation.*;
import com.example.evcharging.finance.settlement.*;
import com.example.evcharging.finance.adjustment.*;
import com.example.evcharging.finance.ledger.*;
import java.util.*;
public class FinanceHarness {
  public static void main(String[] args) {
    var match=ReconciliationMatcher.match(
      new ReconciliationMatcher.LocalFact("P","T",10000,0,"SUCCESS"),
      new ReconciliationMatcher.ChannelFact("P","T",10000,0,"SUCCESS"));
    if(match.type()!=ReconciliationResultType.MATCH) throw new AssertionError("exact match failed");
    var penny=ReconciliationMatcher.match(
      new ReconciliationMatcher.LocalFact("P","T",10000,0,"SUCCESS"),
      new ReconciliationMatcher.ChannelFact("P","T",9999,0,"SUCCESS"));
    if(penny.type()!=ReconciliationResultType.AMOUNT_MISMATCH||penny.differenceAmountFen()!=-1) throw new AssertionError("one-cent mismatch failed");
    var allocation=SettlementCalculator.calculate(10001,List.of(
      new SettlementCalculator.RuleItem("OPERATOR","OP",7000),
      new SettlementCalculator.RuleItem("STATION_OWNER","SO",2000),
      new SettlementCalculator.RuleItem("PLATFORM","PLATFORM",1000)));
    if(allocation.stream().mapToLong(SettlementCalculator.Allocation::amountFen).sum()!=10001) throw new AssertionError("settlement conservation failed");
    var adjusted=AdjustmentMath.calculate(10000,-1,1000,1);
    if(adjusted.paymentFen()!=9999||adjusted.refundFen()!=1001||adjusted.netFen()!=8998) throw new AssertionError("adjustment math failed");
    try { AdjustmentMath.calculate(10000,0,9000,2000); throw new AssertionError("invalid adjustment accepted"); } catch (IllegalArgumentException expected) {}
    new LedgerPosting(List.of(
      new LedgerPosting.Entry("A",LedgerPosting.Side.DEBIT,10001),
      new LedgerPosting.Entry("B",LedgerPosting.Side.CREDIT,10001)));
    try { new LedgerPosting(List.of(new LedgerPosting.Entry("A",LedgerPosting.Side.DEBIT,100))); throw new AssertionError("unbalanced ledger accepted"); } catch (IllegalArgumentException expected) {}
    System.out.println("FINANCE_HARDENING_HARNESS=PASS");
  }
}
JAVA
javac --release 21 -cp "$OUT" -d "$OUT" "$OUT/FinanceHarness.java"
java -cp "$OUT" FinanceHarness
rm -rf "$OUT"
