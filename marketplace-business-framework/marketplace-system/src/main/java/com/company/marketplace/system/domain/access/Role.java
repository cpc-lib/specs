package com.company.marketplace.system.domain.access;
import java.util.Set;
public record Role(long id, String code, String name, Set<String> permissions) { public Role { permissions=Set.copyOf(permissions); } }
