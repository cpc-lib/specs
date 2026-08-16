$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Out = Join-Path $Root ".operation-harness"
if (Test-Path $Out) { Remove-Item $Out -Recurse -Force }
New-Item -ItemType Directory -Path $Out -Force | Out-Null

javac --release 21 -d $Out `
  "$Root\backend\charging-operation\src\main\java\com\example\evcharging\operation\alarm\AlarmSeverity.java" `
  "$Root\backend\charging-operation\src\main\java\com\example\evcharging\operation\alarm\AlarmFingerprint.java" `
  "$Root\backend\charging-operation\src\main\java\com\example\evcharging\operation\sla\SlaPolicy.java" `
  "$Root\backend\charging-operation\src\main\java\com\example\evcharging\operation\workorder\WorkOrderState.java" `
  "$Root\backend\charging-operation\src\main\java\com\example\evcharging\operation\inspection\InspectionCadence.java" `
  "$Root\backend\charging-iot\src\main\java\com\example\evcharging\iot\lifecycle\HeartbeatDeadlineMember.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$Harness = @'
import com.example.evcharging.operation.alarm.*;
import com.example.evcharging.operation.sla.*;
import com.example.evcharging.operation.workorder.*;
import com.example.evcharging.operation.inspection.*;
import com.example.evcharging.iot.lifecycle.*;
import java.time.*;
public class OperationHarness {
  public static void main(String[] args){
    if(!AlarmFingerprint.of("CP1",1,"over_temp").equals("CP1|1|OVER_TEMP")) throw new AssertionError();
    if(AlarmSeverity.max(AlarmSeverity.WARNING,AlarmSeverity.CRITICAL)!=AlarmSeverity.CRITICAL) throw new AssertionError();
    var t=LocalDateTime.of(2026,8,10,10,0);var due=new SlaPolicy(10,120).dueFrom(t);
    if(!due.responseDueTime().equals(t.plusMinutes(10))) throw new AssertionError();
    if(!WorkOrderState.CLOSED.terminal()||WorkOrderState.IN_PROGRESS.terminal()) throw new AssertionError();
    if(!InspectionCadence.next(LocalDate.of(2026,8,10),7).equals(LocalDate.of(2026,8,17))) throw new AssertionError();
    var member=new HeartbeatDeadlineMember(1001,"CP-001","gateway-1|token-abc");
    if(!HeartbeatDeadlineMember.parse(member.encode()).equals(member)) throw new AssertionError();
    System.out.println("OPERATION_HARDENING_HARNESS=PASS");
  }
}
'@
$Harness | Set-Content -Encoding UTF8 (Join-Path $Out "OperationHarness.java")
javac --release 21 -cp $Out -d $Out (Join-Path $Out "OperationHarness.java")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp $Out OperationHarness
Remove-Item $Out -Recurse -Force
