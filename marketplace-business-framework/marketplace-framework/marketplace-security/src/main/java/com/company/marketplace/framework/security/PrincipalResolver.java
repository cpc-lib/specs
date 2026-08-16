package com.company.marketplace.framework.security;
import jakarta.servlet.http.HttpServletRequest;
public interface PrincipalResolver { MarketplacePrincipal resolve(HttpServletRequest request); }
