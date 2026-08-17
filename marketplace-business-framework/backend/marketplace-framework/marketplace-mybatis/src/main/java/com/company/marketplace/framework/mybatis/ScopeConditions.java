package com.company.marketplace.framework.mybatis;
import com.company.marketplace.framework.security.*;
public final class ScopeConditions {
 private ScopeConditions(){}
 public static Long currentMerchantIdOrNull(){ return MarketplaceSecurityContext.current().map(MarketplacePrincipal::merchantId).orElse(null); }
 public static void assertMerchant(long rowMerchantId){ long current=ScopeGuard.requiredMerchantId(); if(current!=rowMerchantId) throw new SecurityException("Cross-merchant access denied"); }
 public static void assertShop(long rowShopId){ ScopeGuard.requireShop(rowShopId); }
}
