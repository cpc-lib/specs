package com.example.evcharging.open.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.*;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE+20)
public class PartnerAuthenticationFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    private final SecretCipher cipher;
    private final PartnerReplayRateGuard replayRate;
    private final ObjectMapper mapper;
    private final long skewSeconds;
    private final int maxBodyBytes;

    public PartnerAuthenticationFilter(
            JdbcTemplate jdbc,SecretCipher cipher,PartnerReplayRateGuard replayRate,ObjectMapper mapper,
            @Value("${charging.open.signature-skew-seconds:300}") long skewSeconds,
            @Value("${charging.open.max-body-bytes:1048576}") int maxBodyBytes){
        this.jdbc=jdbc;this.cipher=cipher;this.replayRate=replayRate;this.mapper=mapper;
        this.skewSeconds=skewSeconds;this.maxBodyBytes=maxBodyBytes;
    }

    @Override protected boolean shouldNotFilter(HttpServletRequest request){
        return !request.getRequestURI().startsWith("/open-api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException{
        long started=System.nanoTime();PartnerRow partner=null;String bodyHash=null;
        String requestId=Optional.ofNullable(request.getHeader("X-Request-Id"))
                .filter(v->!v.isBlank()).orElse(UUID.randomUUID().toString());
        try{
            byte[] body=readBody(request);
            bodyHash=OpenApiSignature.sha256Hex(body);
            String appKey=requiredHeader(request,"X-App-Key");
            String timestamp=requiredHeader(request,"X-Timestamp");
            String nonce=requiredHeader(request,"X-Nonce");
            String signature=requiredHeader(request,"X-Signature");
            String version=requiredHeader(request,"X-Signature-Version");
            if(!"v1".equals(version))throw new SecurityException("unsupported signature version");

            long epoch;
            try{epoch=Long.parseLong(timestamp);}catch(NumberFormatException e){throw new SecurityException("invalid timestamp");}
            if(Math.abs(Instant.now().getEpochSecond()-epoch)>skewSeconds)throw new SecurityException("request timestamp outside allowed window");

            partner=load(appKey);
            String canonical=OpenApiSignature.canonical(
                    request.getMethod(),request.getRequestURI(),request.getQueryString(),body,timestamp,nonce);
            String expected=OpenApiSignature.signHex(cipher.decrypt(partner.secretCiphertext()),canonical);
            if(!OpenApiSignature.constantTimeEquals(expected,signature))throw new SecurityException("invalid signature");

            // Replay protection is consumed only after a valid signature.
            replayRate.requireFreshNonce(appKey,nonce);
            replayRate.requireWithinRate(appKey,partner.rateLimitPerMinute(),epoch);

            Set<String> scopes=new LinkedHashSet<>(jdbc.query(
                    "SELECT scope_code FROM open_partner_scope WHERE partner_id=? ORDER BY scope_code",
                    (rs,n)->rs.getString(1),partner.id()));
            Set<Long> stations=new LinkedHashSet<>(jdbc.query(
                    "SELECT station_id FROM open_partner_station_scope WHERE partner_id=? ORDER BY station_id",
                    (rs,n)->rs.getLong(1),partner.id()));
            PartnerContext.set(new PartnerContext.Principal(
                    partner.tenantId(),partner.id(),partner.partnerCode(),partner.appKey(),scopes,
                    partner.dataScopeType(),stations,partner.rateLimitPerMinute()));

            response.setHeader("X-Request-Id",requestId);
            chain.doFilter(new CachedBodyRequest(request,body),response);
        }catch(PayloadTooLargeException e){
            writeError(response,413,200413,e.getMessage(),requestId);
        }catch(PartnerReplayRateGuard.RateLimitException e){
            writeError(response,429,200429,e.getMessage(),requestId);
        }catch(SecurityException|IllegalArgumentException e){
            writeError(response,401,200401,e.getMessage(),requestId);
        }finally{
            long latency=(System.nanoTime()-started)/1_000_000L;
            audit(partner,request,requestId,bodyHash,response.getStatus(),latency);
            PartnerContext.clear();
        }
    }

    private PartnerRow load(String appKey){
        List<PartnerRow> rows=jdbc.query("""
            SELECT id,tenant_id,partner_code,app_key,secret_ciphertext,status,data_scope_type,rate_limit_per_minute
            FROM open_partner_app WHERE app_key=?
            """,(rs,n)->new PartnerRow(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6),rs.getString(7),rs.getInt(8)),appKey);
        if(rows.isEmpty()||!"ACTIVE".equals(rows.getFirst().status()))throw new SecurityException("partner app disabled or unknown");
        return rows.getFirst();
    }

    private byte[] readBody(HttpServletRequest request)throws IOException{
        byte[] data=request.getInputStream().readNBytes(maxBodyBytes+1);
        if(data.length>maxBodyBytes)throw new PayloadTooLargeException("openapi request body too large");
        return data;
    }

    private void audit(PartnerRow partner,HttpServletRequest req,String requestId,String bodyHash,int status,long latency){
        try{
            jdbc.update("""
                INSERT INTO open_api_audit_log(
                  tenant_id,partner_id,app_key,request_id,method,request_path,request_body_sha256,
                  response_status,latency_ms,remote_ip,create_time
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """,partner==null?null:partner.tenantId(),partner==null?null:partner.id(),
                req.getHeader("X-App-Key"),requestId,req.getMethod(),req.getRequestURI(),
                bodyHash,status,latency,req.getRemoteAddr(),LocalDateTime.ofInstant(Instant.now(),ZoneOffset.UTC));
        }catch(Exception ignored){
            // Audit persistence failure must not leak partner secrets or mask the original response.
        }
    }

    private void writeError(HttpServletResponse response,int httpStatus,int code,String message,String requestId)throws IOException{
        response.setStatus(httpStatus);response.setContentType("application/json;charset=UTF-8");response.setHeader("X-Request-Id",requestId);
        mapper.writeValue(response.getOutputStream(),Map.of("code",code,"message",message,"data",Map.of(),"requestId",requestId));
    }
    private String requiredHeader(HttpServletRequest request,String name){
        String v=request.getHeader(name);
        if(v==null||v.isBlank())throw new SecurityException("missing header: "+name);
        return v.trim();
    }

    private static final class PayloadTooLargeException extends IllegalArgumentException{
        private PayloadTooLargeException(String message){super(message);}
    }

    private record PartnerRow(long id,long tenantId,String partnerCode,String appKey,String secretCiphertext,
                              String status,String dataScopeType,int rateLimitPerMinute){}
}
