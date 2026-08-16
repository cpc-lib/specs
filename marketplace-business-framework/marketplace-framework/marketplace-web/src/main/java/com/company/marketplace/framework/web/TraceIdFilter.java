package com.company.marketplace.framework.web;

import jakarta.servlet.*; import jakarta.servlet.http.*; import org.slf4j.MDC; import org.springframework.core.Ordered; import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component;
import java.io.IOException; import java.util.UUID;
@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public final class TraceIdFilter implements Filter {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest r=(HttpServletRequest)req; HttpServletResponse s=(HttpServletResponse)res;
    String id=r.getHeader("X-Trace-Id"); if(id==null || id.isBlank()) id=UUID.randomUUID().toString().replace("-","");
    MDC.put(TraceContext.TRACE_ID,id); s.setHeader("X-Trace-Id",id);
    try { chain.doFilter(req,res); } finally { MDC.remove(TraceContext.TRACE_ID); }
  }
}
