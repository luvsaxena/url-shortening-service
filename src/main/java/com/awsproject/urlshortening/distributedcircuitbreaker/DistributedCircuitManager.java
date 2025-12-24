package com.awsproject.urlshortening.distributedcircuitbreaker;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Service
public class DistributedCircuitManager {
    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;
    private final RedisScript<String> script;

    private static final int BATCH_THRESHOLD = 10;

    // Local cache of failure counts to prevent Redis thrashing
    private final ConcurrentHashMap<String, LongAdder> localFailures = new ConcurrentHashMap<>();

    // Local cache of global state (refreshed periodically or on demand)
//    private final ConcurrentHashMap<String, String> globalStateCache = new ConcurrentHashMap<>();
    private final Cache<String, String> globalStateCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();


    public DistributedCircuitManager(StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(
                "local fail_key = KEYS[1]; " +
                        "local state_key = KEYS[2]; " +
                        "local inc = tonumber(ARGV[1]); " +
                        "local thresh = tonumber(ARGV[2]); " +
                        "local state = redis.call('GET', state_key); " +
                        "if state == 'OPEN' then return 'OPEN' end; " +
                        "local total = redis.call('INCRBY', fail_key, inc); " +
                        "if total >= thresh then " +
                        "  redis.call('SET', state_key, 'OPEN', 'EX', ARGV[3]); " +
                        "   redis.call('DEL', fail_key); " +
                        "  return 'OPEN'; " +
                        "end; " +
                        "return 'CLOSED';",
                String.class
        );
        this.meterRegistry = meterRegistry;
    }

    public String getCachedState(String serviceName) {
        // 1. Check Caffeine first (L1)
        String state = globalStateCache.getIfPresent(serviceName);

        if (state == null) {
            // 2. If L1 is empty/expired, check Redis (L2)
            String redisState = redisTemplate.opsForValue().get(serviceName + ":state");

            // 3. If Redis key is gone, circuit is CLOSED
            state = (redisState != null) ? redisState : "CLOSED";

            // 4. Update L1 so we don't hit Redis again for another 1 second
            globalStateCache.put(serviceName, state);
        }

        return state;
    }

    public void recordFailures(String serviceName, int threshold, int timeout) {
        LongAdder counter = localFailures.computeIfAbsent(serviceName, k -> new LongAdder());
        counter.increment();

        meterRegistry.counter("cb.failures.local", "service", serviceName).increment();

        if(counter.sum()>=BATCH_THRESHOLD) {
            synchronized (counter) {
                if(counter.sum()>=BATCH_THRESHOLD) {
                    long countToSync = counter.sumThenReset();

                    String newState = redisTemplate.execute(script,
                            List.of(serviceName + ":fail_count", serviceName + ":state"),
                            String.valueOf(countToSync),
                            String.valueOf(threshold),
                            String.valueOf(timeout)
                    );

                    checkStateChange(serviceName, newState);
                    globalStateCache.put(serviceName, newState);
                }
            }

        }
    }

    private void checkStateChange(String serviceName, String newState) {
        String oldState = globalStateCache.getIfPresent(serviceName);
        if ("OPEN".equals(newState) && !"OPEN".equals(oldState)) {
            // Metric: Count the trip event
            meterRegistry.counter("cb.trips.total", "service", serviceName).increment();
        }
    }

    public String syncWithRedis(String serviceName, int threshold, int timeout) {
        long localCount = localFailures.getOrDefault(serviceName, new LongAdder()).sumThenReset();

        // Atomic update in Redis
        String newState = redisTemplate.execute(script,
                List.of(serviceName + ":fail_count", serviceName + ":state"),
                String.valueOf(localCount),
                String.valueOf(threshold),
                String.valueOf(timeout)
        );

        globalStateCache.put(serviceName, newState);
        return newState;
    }

    public void updateLocalCache(String serviceName, String state) {
        globalStateCache.put(serviceName, state);
    }

    @PostConstruct
    public void setupGauges() {
        // In a real system, you'd register these dynamically as services are discovered
        meterRegistry.gauge("cb.state", List.of(Tag.of("service", "pay-svc")), this,
                val -> "OPEN".equals(val.getCachedState("pay-svc")) ? 1.0 : 0.0);
    }

//    public String getCachedState(String serviceName) {
//        return globalStateCache.getOrDefault(serviceName, "CLOSED");
//    }
}
