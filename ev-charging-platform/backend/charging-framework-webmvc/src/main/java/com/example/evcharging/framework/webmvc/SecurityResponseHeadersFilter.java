package com.example.evcharging.framework.webmvc;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityResponseHeadersFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException{
        response.setHeader("X-Content-Type-Options","nosniff");
        response.setHeader("X-Frame-Options","DENY");
        response.setHeader("Referrer-Policy","no-referrer");
        response.setHeader("Permissions-Policy","camera=(), microphone=(), geolocation=()");
        response.setHeader("Cache-Control",request.getRequestURI().startsWith("/actuator/")?"no-store":"no-cache, no-store");
        chain.doFilter(request,response);
    }
}
