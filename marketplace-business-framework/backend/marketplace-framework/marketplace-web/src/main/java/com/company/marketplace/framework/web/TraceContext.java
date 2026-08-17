package com.company.marketplace.framework.web;

import org.slf4j.MDC;
public final class TraceContext {
    public static final String TRACE_ID = "traceId";
    private TraceContext() {}
    public static String traceId() { String id=MDC.get(TRACE_ID); return id == null ? "" : id; }
}
