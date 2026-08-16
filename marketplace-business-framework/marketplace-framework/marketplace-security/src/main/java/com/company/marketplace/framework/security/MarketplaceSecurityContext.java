package com.company.marketplace.framework.security;
import java.util.Optional;
public final class MarketplaceSecurityContext {
 private static final ThreadLocal<MarketplacePrincipal> CURRENT=new ThreadLocal<>();
 private MarketplaceSecurityContext(){}
 public static void set(MarketplacePrincipal p){ CURRENT.set(p); }
 public static Optional<MarketplacePrincipal> current(){ return Optional.ofNullable(CURRENT.get()); }
 public static MarketplacePrincipal required(){ return current().orElseThrow(() -> new SecurityException("Unauthenticated")); }
 public static void clear(){ CURRENT.remove(); }
}
