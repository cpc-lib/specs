package com.example.evcharging.framework.webmvc;

import com.example.evcharging.framework.context.RequestContext;
import com.example.evcharging.framework.security.RequirePermission;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception {
        if(!(handler instanceof HandlerMethod method)) return true;
        RequirePermission required=method.getMethodAnnotation(RequirePermission.class);
        if(required==null) required=method.getBeanType().getAnnotation(RequirePermission.class);
        if(required==null) return true;
        var principal=RequestContext.requirePrincipal();
        if(!principal.hasPermission(required.value())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"permission denied: "+required.value());
            return false;
        }
        return true;
    }
}
