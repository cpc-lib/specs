package com.company.marketplace.framework.common.domain;

public abstract class BaseAggregate<ID> {
    private final ID id;
    private long version;
    protected BaseAggregate(ID id, long version) { this.id = id; this.version = version; }
    public ID id() { return id; }
    public long version() { return version; }
    protected void bumpVersion() { version++; }
}
