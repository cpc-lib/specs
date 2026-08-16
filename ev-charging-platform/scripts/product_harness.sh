#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/.product-harness"
rm -rf "$OUT";mkdir -p "$OUT"
javac --release 21 -d "$OUT" \
  "$ROOT/backend/charging-framework/src/main/java/com/example/evcharging/framework/security/DataScopeType.java" \
  "$ROOT/backend/charging-framework/src/main/java/com/example/evcharging/framework/security/AccessPrincipal.java" \
  "$ROOT/backend/charging-system/src/main/java/com/example/evcharging/system/auth/PasswordHasher.java" \
  "$ROOT/backend/charging-system/src/main/java/com/example/evcharging/system/auth/PasswordPolicy.java" \
  "$ROOT/backend/charging-system/src/main/java/com/example/evcharging/system/auth/RefreshTokenHasher.java"
cat > "$OUT/ProductHarness.java" <<'EOF'
import com.example.evcharging.framework.security.*;
import com.example.evcharging.system.auth.*;
import java.util.*;
public class ProductHarness{
 public static void main(String[]a){
   String hash=PasswordHasher.hash("secret123!".toCharArray());
   if(!PasswordHasher.verify("secret123!".toCharArray(),hash))throw new AssertionError("password verify");
   if(PasswordHasher.verify("wrong".toCharArray(),hash))throw new AssertionError("password rejection");
   PasswordPolicy.validate("Hardening123!");
   try{PasswordPolicy.validate("abcdefghij");throw new AssertionError("weak password accepted");}catch(IllegalArgumentException expected){}
   String r1=RefreshTokenHasher.newToken(),r2=RefreshTokenHasher.newToken();
   if(r1.equals(r2)||!RefreshTokenHasher.hash(r1).equals(RefreshTokenHasher.hash(r1)))throw new AssertionError("refresh token hashing");
   var station=new AccessPrincipal(1,2,"m",Set.of("MERCHANT_STATION"),Set.of("asset:read"),DataScopeType.STATION,Set.of(100L));
   if(!station.mayAccessStation(100)||station.mayAccessStation(101))throw new AssertionError("station scope");
   var tenant=new AccessPrincipal(1,2,"m",Set.of("MERCHANT"),Set.of(),DataScopeType.TENANT,Set.of());
   if(!tenant.mayAccessStation(999))throw new AssertionError("tenant scope");
   if(!station.hasRole("merchant_station"))throw new AssertionError("role");
   System.out.println("PRODUCT_HARDENING_HARNESS=PASS");
 }
}
EOF
javac --release 21 -cp "$OUT" -d "$OUT" "$OUT/ProductHarness.java"
java -cp "$OUT" ProductHarness
rm -rf "$OUT"
