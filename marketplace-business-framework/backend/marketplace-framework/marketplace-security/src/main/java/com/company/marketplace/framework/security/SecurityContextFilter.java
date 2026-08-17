package com.company.marketplace.framework.security;
import jakarta.servlet.*; import jakarta.servlet.http.HttpServletRequest; import java.io.IOException;
public final class SecurityContextFilter implements Filter {
 private final PrincipalResolver resolver;
 public SecurityContextFilter(PrincipalResolver resolver){ this.resolver=resolver; }
 public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
  try { MarketplaceSecurityContext.set(resolver.resolve((HttpServletRequest)req)); chain.doFilter(req,res); } finally { MarketplaceSecurityContext.clear(); }
 }
}
