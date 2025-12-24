package com.awsproject.urlshortening.distributedcircuitbreaker.controllers;

import com.awsproject.urlshortening.distributedcircuitbreaker.DistributedCircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DcbTestController {

    @GetMapping("/dcb-test/execution/failed")
    @DistributedCircuitBreaker(name = "pay-svc", threshold = 20, timeoutSeconds = 50)
    public String processPayment(@RequestParam boolean fail) {
        if (fail) throw new RuntimeException("Payment Gateway Down!");
        return "Payment Successful";
    }

}
