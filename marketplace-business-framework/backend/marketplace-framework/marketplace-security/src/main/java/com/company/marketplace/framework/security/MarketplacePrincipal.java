package com.company.marketplace.framework.security;
import java.util.Set;
public record MarketplacePrincipal(long userId, PrincipalType principalType, Long merchantId, Set<Long> shopIds, Set<String> roles, Set<String> permissions, DataScope dataScope) {
 public MarketplacePrincipal { shopIds=Set.copyOf(shopIds); roles=Set.copyOf(roles); permissions=Set.copyOf(permissions); }
 public boolean hasPermission(String permission){ return permissions.contains(permission); }
}
