package com.awsproject.urlshortening.distributedcircuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedCircuitBreaker {
    String name();
    int threshold() default 50;     // Failures before opening
    int timeoutSeconds() default 30;
}
