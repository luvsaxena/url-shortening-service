package com.awsproject.urlshortening.distributedcircuitbreaker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException(String serviceName) {
        super("Service [" + serviceName + "] is currently unavailable. Please try again later.");
    }
}