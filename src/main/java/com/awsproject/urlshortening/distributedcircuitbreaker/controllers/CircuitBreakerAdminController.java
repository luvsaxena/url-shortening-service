package com.awsproject.urlshortening.distributedcircuitbreaker.controllers;

import com.awsproject.urlshortening.distributedcircuitbreaker.DistributedCircuitManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/circuit-breaker")
public class CircuitBreakerAdminController {

    private final DistributedCircuitManager manager;
    private final StringRedisTemplate redisTemplate;

    public CircuitBreakerAdminController(DistributedCircuitManager manager, StringRedisTemplate redisTemplate) {
        this.manager = manager;
        this.redisTemplate = redisTemplate;
    }

    // 1. FORCE OPEN: Manual Kill Switch
    @PostMapping("/{name}/force-open")
    public String forceOpen(@PathVariable String name, @RequestParam(defaultValue = "3600") int duration) {
        String stateKey = name + ":state";
        // Set state to OPEN for 1 hour (default)
        redisTemplate.opsForValue().set(stateKey, "OPEN", duration, TimeUnit.SECONDS);
        // Sync local L1 cache
        manager.updateLocalCache(name, "OPEN");
        return "Circuit [" + name + "] is now FORCED OPEN for " + duration + "s";
    }

    // 2. FORCE CLOSE: Manual Reset
    @PostMapping("/{name}/reset")
    public String reset(@PathVariable String name) {
        String stateKey = name + ":state";
        String failKey = name + ":fail_count";

        redisTemplate.delete(List.of(stateKey, failKey));
        // Clear local L1 cache
        manager.updateLocalCache(name, "CLOSED");

        return "Circuit [" + name + "] has been RESET to CLOSED";
    }

    // 3. MONITOR: Current Global State
    @GetMapping("/{name}/status")
    public Map<String, Object> getStatus(@PathVariable String name) {
        String state = redisTemplate.opsForValue().get(name + ":state");
        String count = redisTemplate.opsForValue().get(name + ":fail_count");

        return Map.of(
                "service", name,
                "globalState", state != null ? state : "CLOSED",
                "globalFailCount", count != null ? count : "0"
        );
    }
}