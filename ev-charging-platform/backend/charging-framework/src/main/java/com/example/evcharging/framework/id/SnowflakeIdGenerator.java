package com.example.evcharging.framework.id;

/**
 * 41-bit timestamp + 5-bit datacenter + 5-bit worker + 12-bit sequence.
 * Production deployments must assign a unique (datacenterId, workerId) pair.
 */
public final class SnowflakeIdGenerator implements IdGenerator {
    private static final long EPOCH = 1767225600000L; // 2026-01-01T00:00:00Z
    private static final long WORKER_BITS = 5L;
    private static final long DATACENTER_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER = ~(-1L << WORKER_BITS);
    private static final long MAX_DATACENTER = ~(-1L << DATACENTER_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER) {
            throw new IllegalArgumentException("workerId must be between 0 and " + MAX_WORKER);
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER) {
            throw new IllegalArgumentException("datacenterId must be between 0 and " + MAX_DATACENTER);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    @Override
    public synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now < lastTimestamp) {
            throw new IllegalStateException("clock moved backwards by " + (lastTimestamp - now) + "ms");
        }
        if (now == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) now = waitNextMillis(lastTimestamp);
        } else {
            sequence = 0;
        }
        lastTimestamp = now;
        return ((now - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    private static long waitNextMillis(long timestamp) {
        long now = System.currentTimeMillis();
        while (now <= timestamp) {
            Thread.onSpinWait();
            now = System.currentTimeMillis();
        }
        return now;
    }
}
