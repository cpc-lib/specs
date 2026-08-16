package com.example.evcharging.open.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

final class CachedBodyRequest extends HttpServletRequestWrapper {
    private final byte[] body;
    CachedBodyRequest(HttpServletRequest request,byte[] body){super(request);this.body=body;}

    byte[] body(){return body;}

    @Override public ServletInputStream getInputStream(){
        ByteArrayInputStream in=new ByteArrayInputStream(body);
        return new ServletInputStream(){
            @Override public boolean isFinished(){return in.available()==0;}
            @Override public boolean isReady(){return true;}
            @Override public void setReadListener(ReadListener listener){}
            @Override public int read(){return in.read();}
            @Override public int read(byte[] b,int off,int len){return in.read(b,off,len);}
        };
    }
    @Override public BufferedReader getReader(){
        return new BufferedReader(new InputStreamReader(getInputStream(),
                getCharacterEncoding()==null?StandardCharsets.UTF_8:java.nio.charset.Charset.forName(getCharacterEncoding())));
    }
}
