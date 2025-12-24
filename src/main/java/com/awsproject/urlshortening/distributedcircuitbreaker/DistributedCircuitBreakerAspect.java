package com.awsproject.urlshortening.distributedcircuitbreaker;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DistributedCircuitBreakerAspect {

    @Autowired
    private DistributedCircuitManager manager;

    @Around("@annotation(cbConfig)")
    public Object doUnderCircuitBreaker(ProceedingJoinPoint joinPoint, DistributedCircuitBreaker cbConfig) throws Throwable {
        String name = cbConfig.name();

        // 1. Fail Fast if state is cached as OPEN
        if ("OPEN".equals(manager.getCachedState(name))) {
            throw new CircuitBreakerOpenException(name);
        }

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {

            manager.recordFailures(name, cbConfig.threshold(), cbConfig.timeoutSeconds());

            throw e;
        }
    }


}
