package com.example.evcharging.framework.webmvc;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.UUID;

@Component
public class RequestContextFilter extends OncePerRequestFilter {
    private final boolean devTenantHeaderEnabled;
    private final AccessTokenCodec tokenCodec;
    private final String internalServiceKey;
    private final TokenRevocationChecker revocations;

    public RequestContextFilter(
            @Value("${charging.security.dev-tenant-header-enabled:${DEV_TENANT_HEADER_ENABLED:false}}") boolean devTenantHeaderEnabled,
            @Value("${charging.security.access-token-secret:${ACCESS_TOKEN_SECRET:dev-access-token-secret-change-me-1234567890}}") String tokenSecret,
            @Value("${charging.security.internal-service-key:${INTERNAL_SERVICE_KEY:dev-internal-service-key-change-me}}") String internalServiceKey,
            ObjectMapper mapper,TokenRevocationChecker revocations) {
        this.devTenantHeaderEnabled=devTenantHeaderEnabled;
        this.tokenCodec=new AccessTokenCodec(mapper,tokenSecret);
        this.internalServiceKey=internalServiceKey;
        this.revocations=revocations;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        String requestId=Optional.ofNullable(request.getHeader("X-Request-Id"))
                .filter(x->!x.isBlank()).orElse(UUID.randomUUID().toString());
        response.setHeader("X-Request-Id",requestId);

        try {
            AccessPrincipal principal = bearer(request);
            if (principal != null) {
                RequestContext.set(principal,requestId);
            } else if (internalService(request,requestId)) {
                // service context already populated
            } else if (devTenantHeaderEnabled) {
                setDevContext(request,requestId);
            } else {
                RequestContext.set((Long)null,(Long)null,requestId);
            }

            if (requiresAuthentication(request) && RequestContext.principal()==null && !hasDevIdentity()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"authentication required");
                return;
            }
            if (RequestContext.principal()!=null && !surfaceRoleAllowed(request,RequestContext.principal())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"role is not allowed for this API surface");
                return;
            }
            chain.doFilter(request,response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,e.getMessage());
        } finally {
            RequestContext.clear();
        }
    }

    private AccessPrincipal bearer(HttpServletRequest request) {
        String authorization=request.getHeader("Authorization");
        if(authorization==null||!authorization.startsWith("Bearer ")) return null;
        VerifiedAccessToken token=tokenCodec.verifyToken(authorization.substring(7).trim());
        revocations.requireActive(token);
        request.setAttribute("accessTokenId",token.tokenId());
        request.setAttribute("accessSessionId",token.sessionId());
        request.setAttribute("accessExpiresAt",token.expiresAt());
        return token.principal();
    }

    private boolean internalService(HttpServletRequest request,String requestId) {
        String supplied=request.getHeader("X-Service-Key");
        if(supplied==null||!java.security.MessageDigest.isEqual(
                internalServiceKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8))) return false;
        String tenant=request.getHeader("X-Internal-Tenant-Id");
        if(tenant==null||tenant.isBlank()) throw new SecurityException("internal tenant context missing");
        try{
            long tenantId=Long.parseLong(tenant);
            String user=request.getHeader("X-Internal-User-Id");
            long userId=user==null||user.isBlank()?0:Long.parseLong(user);
            RequestContext.set(new AccessPrincipal(
                    tenantId,userId==0?1:userId,"service",
                    Set.of("SERVICE"),Set.of("*"),DataScopeType.TENANT,Set.of()),requestId);
            return true;
        }catch(NumberFormatException e){
            throw new SecurityException("invalid internal service context");
        }
    }

    private void setDevContext(HttpServletRequest request,String requestId) {
        try {
            String tenant=request.getHeader("X-Tenant-Id");
            String user=request.getHeader("X-User-Id");
            if(tenant==null||tenant.isBlank()) {
                RequestContext.set((Long)null,(Long)null,requestId);
                return;
            }
            long tenantId=Long.parseLong(tenant);
            long userId=user==null||user.isBlank()?0:Long.parseLong(user);
            String role=Optional.ofNullable(request.getHeader("X-Dev-Role")).orElse("ADMIN");
            var principal=new AccessPrincipal(
                    tenantId,userId==0?10001:userId,"dev",
                    Set.of(role),Set.of("*"),DataScopeType.ALL,Set.of());
            RequestContext.set(principal,requestId);
        } catch(NumberFormatException e) {
            throw new SecurityException("invalid tenant/user header");
        }
    }

    private boolean hasDevIdentity() {
        return devTenantHeaderEnabled && RequestContext.tenantId()!=null;
    }

    private boolean surfaceRoleAllowed(HttpServletRequest request, AccessPrincipal principal) {
        String path=request.getRequestURI();
        if(path.startsWith("/internal-api/")) return principal.hasRole("SERVICE");
        if(path.startsWith("/admin-api/")) return principal.hasRole("ADMIN");
        if(path.startsWith("/merchant-api/")) return principal.hasRole("MERCHANT")||principal.hasRole("MERCHANT_STATION")||principal.hasRole("ADMIN");
        if(path.startsWith("/technician-api/")) return principal.hasRole("TECHNICIAN")||principal.hasRole("ADMIN");
        if(path.startsWith("/app-api/v1/charging/")||path.startsWith("/app-api/v1/payments")||path.startsWith("/app-api/v1/orders")) {
            return principal.hasRole("MEMBER")||principal.hasRole("ADMIN");
        }
        return true;
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String path=request.getRequestURI();
        if(path.startsWith("/actuator")||path.equals("/auth-api/v1/login")||path.equals("/auth-api/v1/refresh")) return false;
        if(path.startsWith("/app-api/v1/stations") && "GET".equalsIgnoreCase(request.getMethod())) return false;
        return path.startsWith("/internal-api/")
                || path.equals("/auth-api/v1/me")||path.equals("/auth-api/v1/logout")||path.equals("/auth-api/v1/change-password")
                || path.startsWith("/admin-api/")
                || path.startsWith("/merchant-api/")
                || path.startsWith("/technician-api/")
                || path.startsWith("/app-api/v1/charging/")
                || path.startsWith("/app-api/v1/payments")
                || path.startsWith("/app-api/v1/orders");
    }
}
