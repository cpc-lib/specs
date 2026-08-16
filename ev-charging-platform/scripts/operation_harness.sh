#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/.operation-harness"
rm -rf "$OUT";mkdir -p "$OUT"
javac --release 21 -d "$OUT" \
  "$ROOT/backend/charging-operation/src/main/java/com/example/evcharging/operation/alarm/AlarmSeverity.java" \
  "$ROOT/backend/charging-operation/src/main/java/com/example/evcharging/operation/alarm/AlarmFingerprint.java" \
  "$ROOT/backend/charging-operation/src/main/java/com/example/evcharging/operation/sla/SlaPolicy.java" \
  "$ROOT/backend/charging-operation/src/main/java/com/example/evcharging/operation/workorder/WorkOrderState.java" \
  "$ROOT/backend/charging-operation/src/main/java/com/example/evcharging/operation/inspection/InspectionCadence.java" \
  "$ROOT/backend/charging-iot/src/main/java/com/example/evcharging/iot/lifecycle/HeartbeatDeadlineMember.java"
cat > "$OUT/OperationHarness.java" <<'EOF'
import com.example.evcharging.operation.alarm.*;
import com.example.evcharging.operation.sla.*;
import com.example.evcharging.operation.workorder.*;
import com.example.evcharging.operation.inspection.*;
import com.example.evcharging.iot.lifecycle.*;
import java.time.*;
public class OperationHarness {
  public static void main(String[] args){
    if(!AlarmFingerprint.of("CP1",1,"over_temp").equals("CP1|1|OVER_TEMP")) throw new AssertionError("fingerprint");
    if(AlarmSeverity.max(AlarmSeverity.WARNING,AlarmSeverity.CRITICAL)!=AlarmSeverity.CRITICAL) throw new AssertionError("severity");
    var t=LocalDateTime.of(2026,8,10,10,0);var due=new SlaPolicy(10,120).dueFrom(t);
    if(!due.responseDueTime().equals(t.plusMinutes(10))) throw new AssertionError("sla");
    if(!WorkOrderState.CLOSED.terminal()||WorkOrderState.IN_PROGRESS.terminal()) throw new AssertionError("state");
    if(!InspectionCadence.next(LocalDate.of(2026,8,10),7).equals(LocalDate.of(2026,8,17))) throw new AssertionError("inspection cadence");
    var member=new HeartbeatDeadlineMember(1001,"CP-001","gateway-1|token-abc");
    if(!HeartbeatDeadlineMember.parse(member.encode()).equals(member)) throw new AssertionError("heartbeat deadline encoding");
    System.out.println("OPERATION_HARDENING_HARNESS=PASS");
  }
}
EOF
javac --release 21 -cp "$OUT" -d "$OUT" "$OUT/OperationHarness.java"
java -cp "$OUT" OperationHarness
rm -rf "$OUT"
