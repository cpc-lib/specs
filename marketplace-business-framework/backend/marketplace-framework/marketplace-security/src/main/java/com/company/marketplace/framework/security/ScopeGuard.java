package com.company.marketplace.framework.security;
public final class ScopeGuard {
 private ScopeGuard(){}
 public static long requiredMerchantId(){ MarketplacePrincipal p=MarketplaceSecurityContext.required(); if(p.merchantId()==null) throw new SecurityException("Merchant scope required"); return p.merchantId(); }
 public static void requireShop(long shopId){ MarketplacePrincipal p=MarketplaceSecurityContext.required(); if(p.dataScope()==DataScope.ALL) return; if(!p.shopIds().contains(shopId)) throw new SecurityException("Shop scope denied"); }
 public static void requirePermission(String permission){ if(!MarketplaceSecurityContext.required().hasPermission(permission)) throw new SecurityException("Permission denied: "+permission); }
}
