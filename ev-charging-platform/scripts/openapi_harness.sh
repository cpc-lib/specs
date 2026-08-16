#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/.openapi-harness"
rm -rf "$OUT";mkdir -p "$OUT"
javac --release 21 -d "$OUT" \
 "$ROOT/backend/charging-open/src/main/java/com/example/evcharging/open/security/OpenApiSignature.java" \
 "$ROOT/backend/charging-open/src/main/java/com/example/evcharging/open/callback/PartnerCallbackSigner.java"
cat > "$OUT/OpenApiHarness.java" <<'EOF'
package com.example.evcharging.open.security;
import com.example.evcharging.open.callback.PartnerCallbackSigner;
import java.nio.charset.StandardCharsets;
public class OpenApiHarness{
 public static void main(String[]args){
  String c=OpenApiSignature.canonical("GET","/open-api/v1/stations","z=2&a=hello%20world&a=alpha",
      new byte[0],"1700000000","n1");
  if(!c.contains("a=alpha&a=hello%20world&z=2"))throw new AssertionError("query canonicalization");
  String sig=OpenApiSignature.signHex("secret",c);
  if(!OpenApiSignature.constantTimeEquals(sig,sig.toUpperCase()))throw new AssertionError("signature compare");
  var cb=PartnerCallbackSigner.sign("callback-secret","{}".getBytes(StandardCharsets.UTF_8),1700000000L);
  if(cb.signature()==null||cb.signature().length()!=64)throw new AssertionError("callback signature");
  System.out.println("OPENAPI_SECURITY_HARNESS=PASS");
 }
}
EOF
javac --release 21 -cp "$OUT" -d "$OUT" "$OUT/OpenApiHarness.java"
java -cp "$OUT" com.example.evcharging.open.security.OpenApiHarness
rm -rf "$OUT"
