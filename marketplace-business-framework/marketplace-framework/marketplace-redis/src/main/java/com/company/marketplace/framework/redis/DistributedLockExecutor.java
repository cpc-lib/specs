package com.company.marketplace.framework.redis;
import org.redisson.api.RLock; import org.redisson.api.RedissonClient; import java.time.Duration; import java.util.concurrent.TimeUnit; import java.util.function.Supplier;
public final class DistributedLockExecutor {
 private final RedissonClient redisson; public DistributedLockExecutor(RedissonClient redisson){this.redisson=redisson;}
 public <T> T execute(String key, Duration wait, Duration lease, Supplier<T> action){ RLock lock=redisson.getLock(key); boolean acquired=false; try { acquired=lock.tryLock(wait.toMillis(), lease.toMillis(), TimeUnit.MILLISECONDS); if(!acquired) throw new IllegalStateException("Lock busy: "+key); return action.get(); } catch(InterruptedException e){ Thread.currentThread().interrupt(); throw new IllegalStateException("Interrupted while waiting lock",e); } finally { if(acquired && lock.isHeldByCurrentThread()) lock.unlock(); } }
}
